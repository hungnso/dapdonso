/**
 * Shared attack-skill selection policy used by Auto Tan Sat and Auto NV chinh.
 * Keep all farm/combat autos on one rule so one module cannot switch back to
 * a support/buff skill chosen by another module.
 */
public final class CombatSkillPolicy {
    private CombatSkillPolicy() {
    }

    public static Skill selectBestAttackSkill(Char ch) {
        if (ch == null) return null;
        Skill best = findBestAttackSkill(ch, ch.vSkillFight, true);
        if (best == null) best = findBestAttackSkill(ch, ch.vSkillFight, false);
        return best != null ? best : (isDirectDamageSkill(ch, ch.myskill) ? ch.myskill : null);
    }

    /** Selects one farm AOE skill, prioritising target count then a short cycle. */
    public static Skill selectBestAoeAttackSkill(Char ch) {
        if (ch == null || ch.vSkillFight == null) return null;
        Skill best = null;
        for (int i = 0; i < ch.vSkillFight.size(); ++i) {
            Object value = ch.vSkillFight.elementAt(i);
            if (!(value instanceof Skill)) continue;
            Skill candidate = (Skill) value;
            if (!isDirectDamageSkill(ch, candidate) || safeTargets(candidate) <= 1) continue;
            if (best == null || isBetterAoeSkill(candidate, best)) best = candidate;
        }
        // Do not stall accounts which have not learned an AOE yet. They still
        // lock to exactly one direct-damage skill for the whole Daily run.
        return best != null ? best : selectBestAttackSkill(ch);
    }

    private static boolean isBetterAoeSkill(Skill candidate, Skill current) {
        int candidateTargets = safeTargets(candidate);
        int currentTargets = safeTargets(current);
        if (candidateTargets != currentTargets) return candidateTargets > currentTargets;

        int candidateCooldown = safeCooldown(candidate);
        int currentCooldown = safeCooldown(current);
        if (candidateCooldown != currentCooldown) return candidateCooldown < currentCooldown;

        boolean candidateMaxed = isMaxed(candidate);
        boolean currentMaxed = isMaxed(current);
        if (candidateMaxed != currentMaxed) return candidateMaxed;

        int candidateLevel = getSkillLevel(candidate);
        int currentLevel = getSkillLevel(current);
        if (candidateLevel != currentLevel) return candidateLevel > currentLevel;
        if (candidate.point != current.point) return candidate.point > current.point;
        if (candidate.manaUse != current.manaUse) return candidate.manaUse < current.manaUse;
        return candidate.skillId < current.skillId;
    }

    public static Skill findBestAttackSkill(Char ch, MyVector skills, boolean preferMaxed) {
        if (ch == null || skills == null) return null;
        Skill best = null;
        for (int i = 0; i < skills.size(); ++i) {
            Object o = skills.elementAt(i);
            if (!(o instanceof Skill)) continue;
            Skill s = (Skill)o;
            if (!isDirectDamageSkill(ch, s)) continue;
            if (preferMaxed && !isMaxed(s)) continue;
            if (best == null || isBetterAttackSkill(ch, s, best)) best = s;
        }
        return best;
    }

    public static boolean isDirectDamageSkill(Char ch, Skill s) {
        // point == 0 is still a learned level-1 skill in this client. Auto
        // 1-70 accepts it, and it is often the short-cooldown massacre skill.
        if (s == null || s.template == null || s.point < 0) return false;
        // The common massacre loop sends both type 1 and type 3 through the
        // player-attack packet. Auto 1-70 uses the same rule; rejecting all
        // type 3 skills unnecessarily reduced the ready damage-skill pool and
        // introduced cooldown gaps. Class 3 skill 25 is the known exception.
        if (s.template.type != 1 && s.template.type != 3) return false;
        if (ch != null && ch.nClass != null && ch.nClass.classId == 3
                && s.template.id == 25) return false;
        // Prefer learned skills exposed by the game's own fight list.  Some
        // server builds do not populate it early, so do not hard reject when
        // the list is empty.
        return ch == null || ch.vSkillFight == null || ch.vSkillFight.size() == 0 || isInFightList(ch, s);
    }

    public static boolean isMaxed(Skill s) {
        return s != null && s.template != null && s.template.maxPoint > 0 && s.point >= s.template.maxPoint;
    }

    public static boolean isBetterAttackSkill(Skill candidate, Skill current) {
        return isBetterAttackSkill(Char.getMyChar(), candidate, current);
    }

    private static boolean isBetterAttackSkill(Char ch, Skill a, Skill b) {
        if (a == null) return false;
        if (b == null) return true;

        boolean af = isInFightList(ch, a);
        boolean bf = isInFightList(ch, b);
        if (af != bf) return af;

        boolean am = isMaxed(a);
        boolean bm = isMaxed(b);
        if (am != bm) return am;

        // Massacre/farm: number of targets matters before raw skill level.
        int at = safeTargets(a);
        int bt = safeTargets(b);
        if (at != bt) return at > bt;

        // For sword and kunai this avoids choosing a high-level support-ish
        // combat slot with a very slow cycle over the fast farm skill.
        int ac = safeCooldown(a);
        int bc = safeCooldown(b);
        if (ac != bc) return ac < bc;

        int ar = safeRange(a);
        int br = safeRange(b);
        if (ar != br) return ar > br;

        int al = getSkillLevel(a);
        int bl = getSkillLevel(b);
        if (al != bl) return al > bl;

        if (a.point != b.point) return a.point > b.point;
        if (a.manaUse != b.manaUse) return a.manaUse < b.manaUse;
        return a.skillId < b.skillId;
    }

    private static boolean isInFightList(Char ch, Skill s) {
        if (ch == null || s == null || ch.vSkillFight == null) return false;
        for (int i = 0; i < ch.vSkillFight.size(); ++i) {
            Object o = ch.vSkillFight.elementAt(i);
            if (o == s) return true;
            if (o instanceof Skill && ((Skill)o).skillId == s.skillId) return true;
        }
        return false;
    }

    private static int safeTargets(Skill s) {
        if (s == null) return 0;
        int n = s.maxFight;
        if (n <= 0) n = 1;
        return n;
    }

    private static int safeRange(Skill s) {
        if (s == null) return 0;
        return Math.max(Math.abs(s.dx), Math.abs(s.dy));
    }

    private static int safeCooldown(Skill s) {
        if (s == null || s.coolDown <= 0) return Integer.MAX_VALUE / 4;
        return s.coolDown;
    }

    public static int getSkillLevel(Skill s) {
        return s == null ? 0 : s.level;
    }
}

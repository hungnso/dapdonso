/** Compatibility wrapper kept for old callers. New code should use CombatSkillPolicy. */
public final class SwordSkillPolicy {
    private SwordSkillPolicy() {}

    public static boolean isMaxedOrSword(Skill s) {
        return CombatSkillPolicy.isMaxed(s) || isSwordClass();
    }

    public static boolean isBetterDamageSkill(Skill a, Skill b) {
        return CombatSkillPolicy.isBetterAttackSkill(a, b);
    }

    private static boolean isBetterSwordMassacreSkill(Skill a, Skill b) {
        return CombatSkillPolicy.isBetterAttackSkill(a, b);
    }

    private static boolean isBetterDefault(Skill a, Skill b) {
        return CombatSkillPolicy.isBetterAttackSkill(a, b);
    }

    private static boolean isSwordClass() {
        Char ch = Char.getMyChar();
        // In the standard Ninja School class table sword is the first class.
        return ch != null && ch.nClass != null && ch.nClass.classId == 1;
    }

    private static boolean isInFightList(Skill s) {
        Char ch = Char.getMyChar();
        if (ch == null || ch.vSkillFight == null || s == null) return false;
        for (int i = 0; i < ch.vSkillFight.size(); ++i) {
            Object o = ch.vSkillFight.elementAt(i);
            if (o == s || o instanceof Skill && ((Skill)o).skillId == s.skillId) return true;
        }
        return false;
    }

    private static int safeTargets(Skill s) { return s == null ? 0 : Math.max(1, s.maxFight); }
    private static int safeRange(Skill s) { return s == null ? 0 : Math.max(Math.abs(s.dx), Math.abs(s.dy)); }
    private static int safeCooldown(Skill s) { return s == null || s.coolDown <= 0 ? Integer.MAX_VALUE / 4 : s.coolDown; }
    private static int getSkillLevel(Skill s) { return CombatSkillPolicy.getSkillLevel(s); }
}

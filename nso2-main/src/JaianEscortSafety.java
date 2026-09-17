public final class JaianEscortSafety {
    private JaianEscortSafety() {
    }

    public static boolean isEscortStep(int taskId, int taskIndex, boolean reportStep) {
        return taskId == 17 && taskIndex > 0 && !reportStep;
    }

    public static boolean mayRouteTo(int taskId, int taskIndex, boolean reportStep, int targetMap) {
        return mayRouteTo(taskId, taskIndex, reportStep, targetMap, false);
    }

    public static boolean mayRouteTo(int taskId, int taskIndex, boolean reportStep, int targetMap, boolean escortActive) {
        return !isEscortStep(taskId, taskIndex, reportStep) || targetMap == 33;
    }

    public static boolean shouldUsePotion(int hpPercent) {
        return hpPercent < 70;
    }

    public static boolean shouldReturnTownToRestock(int potionCount, boolean escortActive) {
        return !escortActive && potionCount < 50;
    }

    public static boolean shouldAttackJaianAggressor(boolean targetsJaian, boolean inSkillRange, boolean guardIsClose) {
        return targetsJaian && inSkillRange && guardIsClose;
    }

    public static boolean shouldKeepRestocking(int potionCount, boolean escortActive) {
        return !escortActive && potionCount < 50;
    }

    public static int guardOffsetForDirection(int escortDirection) {
        return escortDirection < 0 ? -18 : 18;
    }

    public static int initialEscortDirection(int guardX, int jaianX) {
        return guardX <= jaianX ? 1 : -1;
    }

    public static boolean shouldUseSingleTargetEscortAttack(boolean targetsJaian, boolean inSkillRange, boolean guardIsAhead) {
        return targetsJaian && inSkillRange && guardIsAhead;
    }

    public static boolean isInsideDefenseRange(int distanceX, int distanceY) {
        return isInsideDefenseRange(distanceX, distanceY, 100);
    }

    public static boolean isInsideDefenseRange(int distanceX, int distanceY, int jaianHpPercent) {
        int rangeX = jaianHpPercent < 80 ? 180 : 140;
        int rangeY = jaianHpPercent < 80 ? 110 : 90;
        return distanceX <= rangeX && distanceY <= rangeY;
    }

    public static int directionFromJaianDelta(int deltaX) {
        if (deltaX >= 6) {
            return 1;
        }
        if (deltaX <= -6) {
            return -1;
        }
        return 0;
    }

    public static boolean shouldApplyEscortFreeze(boolean dontMove, boolean disabled) {
        return !dontMove || !disabled;
    }

    public static boolean shouldRescanDefense(long now, long lastScanAt) {
        return lastScanAt <= 0L || now - lastScanAt >= 120L;
    }
}

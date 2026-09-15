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
}

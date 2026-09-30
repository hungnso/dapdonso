/** Decides whether a changed daily order needs one return to the school. */
public final class DailyTaskTransitionPolicy {
    private DailyTaskTransitionPolicy() {
    }

    public static boolean shouldReturnToSchool(int previousTaskId, int previousMapId,
            int previousKillId, int nextTaskId, int nextMapId, int nextKillId,
            boolean awayFromSchool) {
        return awayFromSchool && !isSameObjective(previousTaskId, previousMapId, previousKillId,
                nextTaskId, nextMapId, nextKillId);
    }

    public static boolean isSameObjective(int firstTaskId, int firstMapId, int firstKillId,
            int secondTaskId, int secondMapId, int secondKillId) {
        return firstTaskId == secondTaskId && firstMapId == secondMapId
                && firstKillId == secondKillId;
    }
}

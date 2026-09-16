/** Safety decisions shared by the main-quest automation paths. */
public final class AutoNvcSafetyPolicy {
    private static final int JAIAN_BOUNDARY_WAIT_DISTANCE = 120;

    private AutoNvcSafetyPolicy() {
    }

    public static boolean shouldSuicideAfterMpDepletion(boolean autoActive, boolean suicideWhenMpEmptyEnabled) {
        return autoActive && suicideWhenMpEmptyEnabled;
    }

    public static boolean shouldHoldJaianAtMapBoundary(int taskId, int taskIndex, boolean reportStep,
            int jaianX, int mapWidth, int escortDirection) {
        if (taskId != 17 || taskIndex <= 0 || reportStep || mapWidth <= 0) {
            return false;
        }
        if (escortDirection < 0) {
            return jaianX <= JAIAN_BOUNDARY_WAIT_DISTANCE;
        }
        return escortDirection > 0 && jaianX >= mapWidth - JAIAN_BOUNDARY_WAIT_DISTANCE;
    }

    public static boolean shouldChangeZoneBeforeJaianEscort(boolean jaianAlreadyActive, boolean leaderPresent) {
        return !jaianAlreadyActive && leaderPresent;
    }
}

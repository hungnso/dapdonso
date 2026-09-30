/** Identifies respawn locations where a Daily route may safely resume. */
public final class DailyRecoveryHubPolicy {
    private DailyRecoveryHubPolicy() {
    }

    public static boolean hasReachedSafeHub(boolean inVillage, boolean inSchool) {
        return inVillage || inSchool;
    }
}

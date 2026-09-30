public final class DailyRecoveryHubPolicyTest {
    public static void main(String[] args) {
        assertEquals("village is a safe respawn hub", true,
                DailyRecoveryHubPolicy.hasReachedSafeHub(true, false));
        assertEquals("school is a safe respawn hub", true,
                DailyRecoveryHubPolicy.hasReachedSafeHub(false, true));
        assertEquals("combat map is not a safe respawn hub", false,
                DailyRecoveryHubPolicy.hasReachedSafeHub(false, false));
    }

    private static void assertEquals(String name, boolean expected, boolean actual) {
        if (expected != actual) {
            throw new AssertionError(name + ": expected=" + expected + " actual=" + actual);
        }
    }
}

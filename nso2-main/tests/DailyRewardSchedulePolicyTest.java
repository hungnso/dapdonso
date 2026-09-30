public final class DailyRewardSchedulePolicyTest {
    public static void main(String[] args) {
        assertEquals("waits before 23:00", false, DailyRewardSchedulePolicy.shouldStart(22 * 60 + 59, false, false));
        assertEquals("starts at 23:00", true, DailyRewardSchedulePolicy.shouldStart(23 * 60, false, false));
        assertEquals("does not repeat after today marker", false, DailyRewardSchedulePolicy.shouldStart(23 * 60, true, false));
        assertEquals("waits for a manual claim", false, DailyRewardSchedulePolicy.shouldStart(23 * 60, false, true));
    }

    private static void assertEquals(String name, boolean expected, boolean actual) {
        if (expected != actual) {
            throw new AssertionError(name + ": expected=" + expected + " actual=" + actual);
        }
    }
}

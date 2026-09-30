public final class DailyScheduleStatusTest {
    public static void main(String[] args) {
        assertEquals("waiting for time", "Cho 21:40", DailyScheduleStatus.describe(true, 21 * 60 + 39, 21 * 60 + 40, false, false));
        assertEquals("already run today", "Da chay hom nay", DailyScheduleStatus.describe(true, 22 * 60, 21 * 60 + 40, true, false));
        assertEquals("blocked by ui", "Dang doi dong menu", DailyScheduleStatus.describe(true, 22 * 60, 21 * 60 + 40, false, true));
        assertEquals("disabled", "Tat", DailyScheduleStatus.describe(false, 22 * 60, 21 * 60 + 40, false, false));
    }

    private static void assertEquals(String name, String expected, String actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError(name + ": expected=" + expected + " actual=" + actual);
        }
    }
}

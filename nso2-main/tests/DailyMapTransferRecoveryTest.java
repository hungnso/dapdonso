public final class DailyMapTransferRecoveryTest {
    public static void main(String[] args) {
        DailyMapTransferRecovery recovery = new DailyMapTransferRecovery();

        assertEquals("starts once", true, recovery.begin(1000L));
        assertEquals("duplicate failure does not restart timer", false, recovery.begin(5000L));
        assertEquals("waits before ten seconds", DailyMapTransferRecovery.WAIT,
                recovery.nextAction(10999L, false));
        assertEquals("reconnects at ten seconds when not in village", DailyMapTransferRecovery.RECONNECT,
                recovery.nextAction(11000L, false));
        assertEquals("is inactive after reconnect decision", DailyMapTransferRecovery.NONE,
                recovery.nextAction(11001L, false));

        assertEquals("starts next recovery", true, recovery.begin(20000L));
        assertEquals("village arrival resumes without reconnect", DailyMapTransferRecovery.RESUMED,
                recovery.nextAction(20001L, true));
        assertEquals("is inactive after village arrival", DailyMapTransferRecovery.NONE,
                recovery.nextAction(20002L, true));
    }

    private static void assertEquals(String name, Object expected, Object actual) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(name + ": expected=" + expected + " actual=" + actual);
        }
    }
}

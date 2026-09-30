public final class TaThuFastWarpPolicyTest {
    public static void main(String[] args) {
        assertEquals("task map starts combat", TaThuFastWarpPolicy.FIGHT_CURRENT_MAP,
                TaThuFastWarpPolicy.nextAction(false, true, false, 0L, 1000L));
        assertEquals("server warp to a non-school map is accepted", TaThuFastWarpPolicy.ACCEPT_SERVER_WARP,
                TaThuFastWarpPolicy.nextAction(false, false, false, 1000L, 1500L));
        assertEquals("wait while server warp is pending at school", TaThuFastWarpPolicy.WAIT_FOR_SERVER_WARP,
                TaThuFastWarpPolicy.nextAction(true, false, false, 1000L, 8999L));
        assertEquals("retry menu when warp times out at school", TaThuFastWarpPolicy.OPEN_FAST_WARP_MENU,
                TaThuFastWarpPolicy.nextAction(true, false, false, 1000L, 9000L));
    }

    private static void assertEquals(String name, int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError(name + ": expected=" + expected + " actual=" + actual);
        }
    }
}

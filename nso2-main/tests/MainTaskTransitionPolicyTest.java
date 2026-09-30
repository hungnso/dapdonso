public final class MainTaskTransitionPolicyTest {
    public static void main(String[] args) {
        assertEquals("a new task away from town starts one fast return", MainTaskTransitionPolicy.START_FAST_RETURN,
                MainTaskTransitionPolicy.nextAction(false, true, 0L, 1000L));
        assertEquals("alive character waits after the one return request", MainTaskTransitionPolicy.WAIT_FOR_RETURN,
                MainTaskTransitionPolicy.nextAction(false, true, 1000L, 5000L));
        assertEquals("death is left to normal respawn handling", MainTaskTransitionPolicy.WAIT_FOR_RETURN,
                MainTaskTransitionPolicy.nextAction(false, false, 1000L, 5000L));
        assertEquals("town arrival routes the new task", MainTaskTransitionPolicy.ROUTE_NEW_TASK,
                MainTaskTransitionPolicy.nextAction(true, true, 1000L, 2000L));
        assertEquals("a failed fast return falls back to normal routing", MainTaskTransitionPolicy.ROUTE_NEW_TASK,
                MainTaskTransitionPolicy.nextAction(false, true, 1000L, 8001L));
    }

    private static void assertEquals(String name, int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError(name + ": expected=" + expected + " actual=" + actual);
        }
    }
}

public final class DailyTaskTransitionPolicyTest {
    public static void main(String[] args) {
        assertEquals("same server refresh stays on current map", false,
                DailyTaskTransitionPolicy.shouldReturnToSchool(
                        18, 64, 121, 18, 64, 121, true));
        assertEquals("new daily objective outside school returns once", true,
                DailyTaskTransitionPolicy.shouldReturnToSchool(
                        18, 64, 121, 19, 15, 122, true));
        assertEquals("new daily objective at school does not self-kill", false,
                DailyTaskTransitionPolicy.shouldReturnToSchool(
                        18, 64, 121, 19, 15, 122, false));
    }

    private static void assertEquals(String name, boolean expected, boolean actual) {
        if (expected != actual) {
            throw new AssertionError(name + ": expected=" + expected + " actual=" + actual);
        }
    }
}

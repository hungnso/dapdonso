public final class PotentialBuildPolicyTest {
    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected=" + expected + " actual=" + actual);
        }
    }

    public static void main(String[] args) {
        assertEquals(0, PotentialBuildPolicy.primaryStatFor(2, 9), "before level 10 always use strength");
        assertEquals(0, PotentialBuildPolicy.primaryStatFor(1, 10), "class 1 primary stat");
        assertEquals(3, PotentialBuildPolicy.primaryStatFor(2, 10), "class 2 primary stat");
        assertEquals(0, PotentialBuildPolicy.primaryStatFor(3, 10), "class 3 primary stat");
        assertEquals(3, PotentialBuildPolicy.primaryStatFor(4, 10), "class 4 primary stat");
        assertEquals(0, PotentialBuildPolicy.primaryStatFor(5, 10), "class 5 primary stat");
        assertEquals(3, PotentialBuildPolicy.primaryStatFor(6, 10), "class 6 primary stat");

        assertEquals(70, PotentialBuildPolicy.primaryPoints(100, true), "class entry allocates 70 primary points");
        assertEquals(30, PotentialBuildPolicy.vitalityPoints(100, true), "class entry allocates 30 vitality points");
        assertEquals(100, PotentialBuildPolicy.primaryPoints(100, false), "later 100 points all go to primary");
        assertEquals(0, PotentialBuildPolicy.vitalityPoints(100, false), "later points add no vitality");
        assertEquals(35, PotentialBuildPolicy.primaryPoints(35, false), "normal level-up points all go to primary");
    }
}

public final class PotentialBuildPolicyTest {
    private static void assertEquals(int expected, int actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + ": expected=" + expected + " actual=" + actual);
        }
    }

    public static void main(String[] args) {
        assertEquals(0, PotentialBuildPolicy.primaryStatFor(2, 9), "before level 10 always use strength");
        assertEquals(0, PotentialBuildPolicy.primaryStatFor(1, 10), "class 1 primary stat");
        assertEquals(3, PotentialBuildPolicy.primaryStatFor(2, 10), "Phi tieu primary stat is Chakra");
        assertEquals(0, PotentialBuildPolicy.primaryStatFor(3, 10), "class 3 primary stat");
        assertEquals(3, PotentialBuildPolicy.primaryStatFor(4, 10), "Cung primary stat is Chakra");
        assertEquals(0, PotentialBuildPolicy.primaryStatFor(5, 10), "class 5 primary stat");
        assertEquals(3, PotentialBuildPolicy.primaryStatFor(6, 10), "Quat primary stat is Chakra");

        assertEquals(60, PotentialBuildPolicy.primaryPoints(100, true), "level 10 allocates 60 primary points");
        assertEquals(40, PotentialBuildPolicy.vitalityPoints(100, true), "level 10 allocates 40 vitality points");
        assertEquals(100, PotentialBuildPolicy.primaryPoints(100, false), "later 100 points all go to primary");
        assertEquals(0, PotentialBuildPolicy.vitalityPoints(100, false), "later points add no vitality");
        assertEquals(35, PotentialBuildPolicy.primaryPoints(35, false), "normal level-up points all go to primary");
        assertEquals(1, PotentialBuildPolicy.shouldSplitLevelTenPoints(10, 100) ? 1 : 0,
                "level 10 with 100 points must use the 60/40 split regardless of task step");
        assertEquals(0, PotentialBuildPolicy.shouldSplitLevelTenPoints(11, 100) ? 1 : 0,
                "level 11 must not split points");

        assertEquals(0, PotentialBuildPolicy.mayAllocatePotentialForTask(9, 0) ? 1 : 0,
                "NV9 must not allocate before the potential quest is active");
        assertEquals(0, PotentialBuildPolicy.mayAllocatePotentialForTask(9, 1) ? 1 : 0,
                "NV9 skill step must not consume its potential points");
        assertEquals(1, PotentialBuildPolicy.mayAllocatePotentialForTask(9, 2) ? 1 : 0,
                "NV9 potential step must allocate points");
        assertEquals(1, PotentialBuildPolicy.mayAllocatePotentialForTask(10, 0) ? 1 : 0,
                "other tasks keep normal automatic allocation");

        assertEquals(26, PotentialBuildPolicy.nv9RecoveryFarmMap(),
                "NV9 recovery must farm in the already-unlocked map, not map 28");
    }
}

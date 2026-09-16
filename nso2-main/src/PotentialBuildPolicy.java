public final class PotentialBuildPolicy {
    private PotentialBuildPolicy() {
    }

    public static int primaryStatFor(int classId, int level) {
        if (level < 10) {
            return 0;
        }
        return classId == 2 || classId == 4 || classId == 6 ? 3 : 0;
    }

    public static int vitalityPoints(int availablePoints, boolean classEntryAllocation) {
        return classEntryAllocation && availablePoints >= 100 ? 40 : 0;
    }

    public static boolean shouldSplitLevelTenPoints(int level, int availablePoints) {
        return level == 10 && availablePoints >= 100;
    }

    /**
     * Task 9 grants the 100 class-entry potential points before its dedicated
     * "Tang diem tiem nang" step.  Spending them earlier prevents the server
     * from recognizing that step as complete.
     */
    public static boolean mayAllocatePotentialForTask(int taskId, int taskIndex) {
        return taskId != 9 || taskIndex == 2;
    }

    /** Map 28 remains locked until class-entry task 9 is completed. */
    public static int nv9RecoveryFarmMap() {
        return 26;
    }

    public static int primaryPoints(int availablePoints, boolean classEntryAllocation) {
        return availablePoints - vitalityPoints(availablePoints, classEntryAllocation);
    }
}

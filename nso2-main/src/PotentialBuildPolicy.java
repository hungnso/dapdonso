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

    public static int primaryPoints(int availablePoints, boolean classEntryAllocation) {
        return availablePoints - vitalityPoints(availablePoints, classEntryAllocation);
    }
}

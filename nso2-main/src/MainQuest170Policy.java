/** Main-quest range and level-first farm maps for Auto NV 1-70. */
public final class MainQuest170Policy {
    private static final int LAST_MAIN_TASK = 42;
    private static final int TARGET_LEVEL = 70;
    private static final int[] NEXT_LEVELS = new int[]{51, 53, 55, 57, 59, 61, 63, 65, 67, 69};
    private static final int[] NEXT_LEVEL_MAPS = new int[]{16, 16, 42, 42, 62, 44, 18, 59, 45, 53};

    private MainQuest170Policy() {
    }

    public static boolean supportsMainTask(int taskId) {
        return taskId >= 0 && taskId <= LAST_MAIN_TASK;
    }

    public static int targetLevel() {
        return TARGET_LEVEL;
    }

    /** Prefer the map for the next level-gated main task after level 50. */
    public static int preferredFarmMap(int level, int fallbackMap) {
        if (level < 49) {
            return fallbackMap;
        }
        for (int index = 0; index < NEXT_LEVELS.length; ++index) {
            if (level < NEXT_LEVELS[index]) {
                return NEXT_LEVEL_MAPS[index];
            }
        }
        return NEXT_LEVEL_MAPS[NEXT_LEVEL_MAPS.length - 1];
    }
}

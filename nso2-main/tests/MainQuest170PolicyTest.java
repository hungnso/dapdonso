public final class MainQuest170PolicyTest {
    public static void main(String[] args) {
        assertEquals("continues main quest through task 42", true,
                MainQuest170Policy.supportsMainTask(42));
        assertEquals("does not treat task 43 as a supported main quest", false,
                MainQuest170Policy.supportsMainTask(43));
        assertEquals("runs until level 70", 70, MainQuest170Policy.targetLevel());
        assertEquals("level 50 prepares map for the level-51 task", 16,
                MainQuest170Policy.preferredFarmMap(50, 15));
        assertEquals("level 54 prepares map for the level-55 task", 42,
                MainQuest170Policy.preferredFarmMap(54, 16));
        assertEquals("level 68 prepares map for the level-69 task", 53,
                MainQuest170Policy.preferredFarmMap(68, 45));
        assertEquals("level 69 keeps the final quest map until 70", 53,
                MainQuest170Policy.preferredFarmMap(69, 45));
    }

    private static void assertEquals(String name, int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError(name + ": expected=" + expected + " actual=" + actual);
        }
    }

    private static void assertEquals(String name, boolean expected, boolean actual) {
        if (expected != actual) {
            throw new AssertionError(name + ": expected=" + expected + " actual=" + actual);
        }
    }
}

public final class AutoNvhn3xBatchPolicyTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        boolean[] occupied = new boolean[]{true, true, true};
        check(AutoNvhn3xBatchPolicy.nextSlot(0, occupied) == 0, "batch starts at slot 0");
        check(AutoNvhn3xBatchPolicy.nextSlot(1, occupied) == 1, "slot 0 done advances to slot 1");
        check(AutoNvhn3xBatchPolicy.nextSlot(3, occupied) == 2, "slots 0 and 1 done advance to slot 2");
        check(AutoNvhn3xBatchPolicy.markDone(0, 1) == 2, "markDone stores the slot bit");
        check(AutoNvhn3xBatchPolicy.allDone(7, occupied), "mask 7 completes all three slots");
        check(AutoNvhn3xBatchPolicy.nextSlot(0, new boolean[]{false, true, true}) == 1,
                "empty slot 0 is skipped");
        check(AutoNvhn3xBatchPolicy.storageKey("acc", "2026-261").indexOf("acc") >= 0,
                "checkpoint key includes account identity");
    }
}

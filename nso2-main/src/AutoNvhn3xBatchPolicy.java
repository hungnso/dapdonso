/** Pure rules for the three-character Auto NVHN 3x batch. */
public final class AutoNvhn3xBatchPolicy {
    private AutoNvhn3xBatchPolicy() {
    }

    public static int nextSlot(int doneMask, boolean[] occupied) {
        if (occupied == null) return -1;
        for (int slot = 0; slot < 3 && slot < occupied.length; ++slot) {
            if (occupied[slot] && (doneMask & (1 << slot)) == 0) return slot;
        }
        return -1;
    }

    public static int markDone(int doneMask, int slot) {
        return slot >= 0 && slot < 3 ? doneMask | (1 << slot) : doneMask;
    }

    public static boolean allDone(int doneMask, boolean[] occupied) {
        return nextSlot(doneMask, occupied) < 0;
    }

    public static String storageKey(String account, String date) {
        String safeAccount = account == null ? "" : account;
        String safeDate = date == null ? "" : date;
        return "AutoNvhn3xBatch_" + safeAccount + "_" + safeDate;
    }
}

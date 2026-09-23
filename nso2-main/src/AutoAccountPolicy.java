/** Pure rules for per-tab account rotation. */
public final class AutoAccountPolicy {
    public static final int MAX_ACCOUNTS = 20;
    private AutoAccountPolicy() {
    }

    public static boolean isValidUsername(String username) {
        return username != null && username.trim().length() > 0;
    }

    public static int nextIndex(AutoAccountEntry[] entries, int currentIndex) {
        if (entries == null) return -1;
        int start = currentIndex < 0 ? 0 : currentIndex + 1;
        for (int i = start; i < entries.length && i < MAX_ACCOUNTS; ++i) {
            AutoAccountEntry entry = entries[i];
            if (entry != null && entry.enabled && isValidUsername(entry.username)
                    && entry.status != AutoAccountEntry.DONE) return i;
        }
        return -1;
    }

    public static boolean shouldRetry(int retries) {
        return retries >= 0 && retries < 3;
    }

    public static int recoverStatus(int status) {
        return status == AutoAccountEntry.RUNNING ? AutoAccountEntry.PENDING : status;
    }
}

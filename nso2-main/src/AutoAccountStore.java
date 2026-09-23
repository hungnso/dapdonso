/** Persistence boundary for one tab's account rotation data. */
public final class AutoAccountStore {
    public static final int MAX_ACCOUNTS = 20;
    private static final String PREFIX = "AutoAccountRotation_";
    private static final String KEY_ENABLED = PREFIX + "enabled";
    private static final String KEY_START = PREFIX + "start";
    private static final String KEY_INDEX = PREFIX + "index";
    private static final String KEY_PHASE = PREFIX + "phase";
    private static final String KEY_RETRIES = PREFIX + "retries";
    private static final String KEY_DATE = PREFIX + "date";

    private AutoAccountStore() {
    }

    public static AutoAccountEntry[] load() {
        AutoAccountEntry[] entries = new AutoAccountEntry[MAX_ACCOUNTS];
        for (int i = 0; i < MAX_ACCOUNTS; ++i) {
            String username = mResources.c(key("u", i));
            String password = AutoAccountSecretCodec.decode(mResources.c(key("p", i)));
            int enabled = mResources.d(key("e", i));
            int status = mResources.d(key("s", i));
            entries[i] = new AutoAccountEntry(username == null ? "" : username,
                    password == null ? "" : password, enabled != 0,
                    status < AutoAccountEntry.PENDING || status > AutoAccountEntry.LOGIN_ERROR
                            ? AutoAccountEntry.PENDING : status);
        }
        return entries;
    }

    public static void save(AutoAccountEntry[] entries) {
        for (int i = 0; i < MAX_ACCOUNTS; ++i) {
            AutoAccountEntry entry = entries != null && i < entries.length ? entries[i] : null;
            if (entry == null) entry = new AutoAccountEntry("", "", false, AutoAccountEntry.PENDING);
            mResources.a(key("u", i), entry.username == null ? "" : entry.username);
            mResources.a(key("p", i), AutoAccountSecretCodec.encode(entry.password));
            mResources.a(key("e", i), entry.enabled ? 1 : 0);
            mResources.a(key("s", i), entry.status);
        }
    }

    public static boolean isEnabled() { return mResources.d(KEY_ENABLED) == 1; }
    public static void setEnabled(boolean enabled) { mResources.a(KEY_ENABLED, enabled ? 1 : 0); }
    public static int startIndex() { int value = mResources.d(KEY_START); return value < 0 ? 0 : value; }
    public static void setStartIndex(int index) { mResources.a(KEY_START, index); }
    public static int checkpointIndex() { return mResources.d(KEY_INDEX); }
    public static int checkpointPhase() { return mResources.d(KEY_PHASE); }
    public static int checkpointRetries() { return mResources.d(KEY_RETRIES); }
    public static String checkpointDate() { return mResources.c(KEY_DATE); }
    public static void saveCheckpoint(int index, int phase, int retries, String date) {
        mResources.a(KEY_INDEX, index); mResources.a(KEY_PHASE, phase);
        mResources.a(KEY_RETRIES, retries); mResources.a(KEY_DATE, date == null ? "" : date);
    }
    public static void clearCheckpoint() {
        saveCheckpoint(-1, 0, 0, "");
    }
    public static void resetStatuses() {
        AutoAccountEntry[] entries = load();
        for (int i = 0; i < entries.length; ++i) {
            if (entries[i] != null) entries[i].status = AutoAccountEntry.PENDING;
        }
        save(entries);
        clearCheckpoint();
    }

    private static String key(String kind, int index) {
        return PREFIX + kind + index;
    }
}

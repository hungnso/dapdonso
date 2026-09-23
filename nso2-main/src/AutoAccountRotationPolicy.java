/** Pure state rules for the per-tab account rotation coordinator. */
public final class AutoAccountRotationPolicy {
    public static final int LOAD = 0;
    public static final int ENSURE_LOGIN = 1;
    public static final int RUN_NVHN3X = 2;
    public static final int WAIT_NVHN3X = 3;
    public static final int MARK_DONE = 4;
    public static final int LOGOUT = 5;
    public static final int NEXT_ACCOUNT = 6;
    public static final int COMPLETE = 7;
    public static final int PAUSED = 8;

    private AutoAccountRotationPolicy() {
    }

    public static int firstIndex(AutoAccountEntry[] entries, int currentIndex) {
        return AutoAccountPolicy.nextIndex(entries, currentIndex);
    }

    public static int nextPhase(int phase, boolean allDone) {
        if (phase == MARK_DONE) return allDone ? COMPLETE : LOGOUT;
        return phase;
    }

    public static int loginRetryPhase(int retries) {
        return AutoAccountPolicy.shouldRetry(retries) ? ENSURE_LOGIN : PAUSED;
    }

    public static int recoverPhase(int phase) {
        return phase == RUN_NVHN3X || phase == WAIT_NVHN3X ? ENSURE_LOGIN : phase;
    }
}

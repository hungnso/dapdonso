/** One-shot state machine for a Daily route that cannot change maps. */
public final class DailyMapTransferRecovery {
    public static final int NONE = 0;
    public static final int WAIT = 1;
    public static final int RESUMED = 2;
    public static final int RECONNECT = 3;

    private static final long RESPAWN_TIMEOUT_MS = 10000L;
    private boolean active;
    private long startedAt;

    public boolean begin(long now) {
        if (this.active) {
            return false;
        }
        this.active = true;
        this.startedAt = now;
        return true;
    }

    public int nextAction(long now, boolean inVillage) {
        if (!this.active) {
            return NONE;
        }
        if (inVillage) {
            this.active = false;
            return RESUMED;
        }
        if (now - this.startedAt >= RESPAWN_TIMEOUT_MS) {
            this.active = false;
            return RECONNECT;
        }
        return WAIT;
    }

    public void reset() {
        this.active = false;
        this.startedAt = 0L;
    }
}

/** Chooses Ta Thu's server-authoritative "Di lam NV" warp flow. */
public final class TaThuFastWarpPolicy {
    public static final int FIGHT_CURRENT_MAP = 0;
    public static final int ACCEPT_SERVER_WARP = 1;
    public static final int WAIT_FOR_SERVER_WARP = 2;
    public static final int OPEN_FAST_WARP_MENU = 3;

    private static final long WARP_TIMEOUT_MS = 8000L;

    private TaThuFastWarpPolicy() {
    }

    public static int nextAction(boolean inSchool, boolean onTaskMap, boolean onAcceptedWarpMap,
            long warpRequestedAt, long now) {
        if (onTaskMap || onAcceptedWarpMap) {
            return FIGHT_CURRENT_MAP;
        }
        if (warpRequestedAt > 0L) {
            if (!inSchool) {
                return ACCEPT_SERVER_WARP;
            }
            if (now - warpRequestedAt < WARP_TIMEOUT_MS) {
                return WAIT_FOR_SERVER_WARP;
            }
        }
        return OPEN_FAST_WARP_MENU;
    }
}

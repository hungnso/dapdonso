/**
 * Reconnects when the client remains dead and repeatedly fails to return to
 * town. This is intentionally time based: a normal death keeps using the
 * standard revive flow and is never disconnected.
 */
public final class DeathRouteReconnectRecovery {
    private static final long DEAD_STUCK_TIMEOUT_MS = 120000L;
    private static final long RECONNECT_SETTLE_MS = 8000L;
    private static final int MIN_RETURN_ATTEMPTS = 6;

    private static long deadSince;
    private static int returnAttempts;
    private static boolean reconnecting;
    private static long reconnectRequestedAt;

    private DeathRouteReconnectRecovery() {
    }

    /** Returns true when the normal dead-return request must be skipped. */
    public static synchronized boolean beforeDeadReturnAttempt() {
        Char me = Char.getMyChar();
        if (me == null || (me.cHp > 0 && me.statusMe != 14 && me.statusMe != 5)) {
            reset();
            return false;
        }

        long now = System.currentTimeMillis();
        if (reconnecting) {
            Session_ME session = Session_ME.getInstance();
            if (session == null || !session.connected
                    || GameCanvas.currentScreen != GameScr.instance
                    || now - reconnectRequestedAt < RECONNECT_SETTLE_MS) {
                return true;
            }

            // The new game session is ready but the server still reports the
            // character dead. Give the standard return-town flow a fresh
            // timeout window instead of reconnecting in a loop.
            reconnecting = false;
            deadSince = now;
            returnAttempts = 0;
        }

        if (deadSince == 0L) {
            deadSince = now;
        }
        ++returnAttempts;
        if (now - deadSince < DEAD_STUCK_TIMEOUT_MS
                || returnAttempts < MIN_RETURN_ATTEMPTS) {
            return false;
        }

        reconnecting = true;
        reconnectRequestedAt = now;
        System.out.println("[DEATH][RECONNECT] stuck dead map=" + TileMap.mapID
                + " attempts=" + returnAttempts
                + " durationMs=" + (now - deadSince));
        GameScr.addChatPopup("Ket khi dang chet - tu dang nhap lai de tiep tuc auto");

        // Preserve Daily/Ta Thu (including the interrupted VDMQ/1-70 parent)
        // before forcing the same reconnect mechanism used by Auto 1-70.
        DailyReconnectRecovery.captureBeforeDisconnect();
        Session_ME session = Session_ME.getInstance();
        if (session != null) {
            session.cleanNetwork();
            session.e();
        }
        return true;
    }

    private static void reset() {
        deadSince = 0L;
        returnAttempts = 0;
        reconnecting = false;
        reconnectRequestedAt = 0L;
    }
}

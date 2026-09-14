public final class DailyCharacterProgress {
    private static final long REFRESH_INTERVAL = 5000L;
    private static final long SNAPSHOT_WAIT_TIMEOUT = 8000L;
    private static long lastRequestAt;
    private static long firstRequestAt;
    private static int requestedCharId = Integer.MIN_VALUE;
    private static int snapshotCharId = Integer.MIN_VALUE;

    private DailyCharacterProgress() {
    }

    public static void forceRefresh() {
        Char me = Char.getMyChar();
        if (me == null) {
            return;
        }
        requestedCharId = me.charID;
        snapshotCharId = Integer.MIN_VALUE;
        lastRequestAt = 0L;
        firstRequestAt = 0L;
        if (GameScr.currentCharViewInfo == me) {
            GameScr.currentCharViewInfo = null;
        }
        refreshIfNeeded();
    }

    public static void refreshIfNeeded() {
        Char me = Char.getMyChar();
        if (me == null || me.cName == null || me.cName.length() == 0
                || !Session_ME.getInstance().connected) {
            return;
        }

        if (requestedCharId != me.charID) {
            requestedCharId = me.charID;
            snapshotCharId = Integer.MIN_VALUE;
            lastRequestAt = 0L;
            firstRequestAt = 0L;
            if (GameScr.currentCharViewInfo == me) {
                GameScr.currentCharViewInfo = null;
            }
        }

        long now = System.currentTimeMillis();
        if (lastRequestAt == 0L || now - lastRequestAt >= REFRESH_INTERVAL) {
            lastRequestAt = now;
            if (firstRequestAt == 0L) {
                firstRequestAt = now;
            }
            Service.gI().viewInfo(me.cName);
        }
    }

    public static void onServerSnapshot() {
        Char me = Char.getMyChar();
        if (me != null && GameScr.currentCharViewInfo == me) {
            snapshotCharId = me.charID;
        }
    }

    public static boolean ensureSnapshot() {
        refreshIfNeeded();
        onServerSnapshot();
        Char me = Char.getMyChar();
        if (me == null) {
            return false;
        }
        if (snapshotCharId == me.charID) {
            return true;
        }
        return firstRequestAt > 0L
                && System.currentTimeMillis() - firstRequestAt >= SNAPSHOT_WAIT_TIMEOUT;
    }

    /**
     * Server field countFinishDay is the number of daily quests remaining.
     * The character-info screen displays that raw value, while the HUD must
     * display completed progress (20 - remaining).
     */
    public static int dailyCompleted() {
        Char me = Char.getMyChar();
        if (me == null) {
            return 0;
        }
        int remaining = me.countFinishDay;
        if (remaining < 0) {
            remaining = 0;
        } else if (remaining > 20) {
            remaining = 20;
        }
        return 20 - remaining;
    }

    public static int mergeDailyCompleted(int localCompleted) {
        refreshIfNeeded();
        // The response can arrive while TaskAuto is above the coordinator on
        // the auto stack, so recognize it from the HUD/update path as well.
        onServerSnapshot();
        Char me = Char.getMyChar();
        if (me == null || snapshotCharId != me.charID) {
            return localCompleted;
        }
        int serverCompleted = dailyCompleted();
        return serverCompleted > localCompleted ? serverCompleted : localCompleted;
    }

    public static int taThuRemaining() {
        Char me = Char.getMyChar();
        if (me == null || me.countLoopBoos < 0) {
            return 0;
        }
        return me.countLoopBoos;
    }

    public static int taThuCompleted() {
        int remaining = taThuRemaining();
        if (remaining > 2) {
            remaining = 2;
        }
        return 2 - remaining;
    }

    public static boolean dailyDone() {
        Char me = Char.getMyChar();
        return me != null && snapshotCharId == me.charID && me.countFinishDay <= 0;
    }

    public static boolean taThuDone() {
        Char me = Char.getMyChar();
        return me != null && snapshotCharId == me.charID
                && taThuRemaining() <= 0 && Char.j(1) == null;
    }

    public static String hudText() {
        refreshIfNeeded();
        int completed = dailyCompleted();
        int taThu = taThuCompleted();
        if (GameCanvas.z < 240) {
            return "NV " + completed + "/20 | T\u00e0 th\u00fa " + taThu + "/2";
        }
        return "NV h\u1eb1ng ng\u00e0y " + completed + "/20 | T\u00e0 th\u00fa " + taThu + "/2";
    }
}

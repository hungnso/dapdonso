/** Keeps the one-shot fast-return state separate from map routing. */
public final class MainTaskTransitionPolicy {
    public static final int START_FAST_RETURN = 1;
    public static final int WAIT_FOR_RETURN = 2;
    public static final int ROUTE_NEW_TASK = 3;

    private static final long FAST_RETURN_WAIT_MS = 7000L;

    private MainTaskTransitionPolicy() {
    }

    public static int nextAction(boolean inVillage, boolean alive,
            long fastReturnRequestedAt, long now) {
        if (inVillage) {
            return ROUTE_NEW_TASK;
        }
        if (!alive) {
            return WAIT_FOR_RETURN;
        }
        if (fastReturnRequestedAt <= 0L) {
            return START_FAST_RETURN;
        }
        return now - fastReturnRequestedAt < FAST_RETURN_WAIT_MS
                ? WAIT_FOR_RETURN : ROUTE_NEW_TASK;
    }
}

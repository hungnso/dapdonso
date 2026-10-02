/** Limits non-urgent work without slowing the game logic clock. */
public final class LiteRuntimePolicy {
    private final long interval;
    private long lastRun;

    public LiteRuntimePolicy(long interval) {
        this.interval = interval;
    }

    public synchronized boolean tryAcquire(long now) {
        if (!isDue(now, this.lastRun, this.interval)) return false;
        this.lastRun = now;
        return true;
    }

    public synchronized void reset() { this.lastRun = 0L; }

    public static boolean isDue(long now, long lastRun, long interval) {
        return lastRun == 0L || now < lastRun || now - lastRun >= interval;
    }

    public static boolean shouldPaint(boolean lite, boolean gameplay, boolean uiBlocked,
                                      boolean activeInput, long now, long lastPaint) {
        return !lite || !gameplay || uiBlocked || activeInput || isDue(now, lastPaint, 200L);
    }
}

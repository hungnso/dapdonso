/** Cave entry used by the daily chain; no standalone cave combat. */
public final class DailyHangPolicy {
    private DailyHangPolicy() { }

    public static int mapForLevel(int level) {
        if (level < 30) return -1;
        if (level < 40) return 91;
        if (level < 50) return 94;
        if (level < 60) return 105;
        if (level < 70) return 114;
        if (level < 90) return 125;
        return 157;
    }
}

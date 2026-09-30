public final class DailyRewardSchedulePolicy {
    private DailyRewardSchedulePolicy() {
    }

    public static boolean shouldStart(int nowMinutes, boolean ranToday, boolean claimBusy) {
        return nowMinutes >= 23 * 60 && !ranToday && !claimBusy;
    }
}

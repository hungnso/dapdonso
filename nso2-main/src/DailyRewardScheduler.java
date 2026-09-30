import java.util.Calendar;
import java.util.TimeZone;

public final class DailyRewardScheduler {
    private static final String RMS_LAST_DATE = "DailyRewardLastDate";

    private DailyRewardScheduler() {
    }

    public static void tick(Calendar calendar) {
        if (calendar == null || !(GameCanvas.currentScreen instanceof GameScr)
                || Char.getMyChar() == null || !Session_ME.getInstance().connected) {
            return;
        }
        int nowMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
        String today = dateKey(calendar);
        boolean ranToday = today.equals(mResources.c(RMS_LAST_DATE));
        if (!DailyRewardSchedulePolicy.shouldStart(nowMinutes, ranToday, ActivityQuickClaim.isBusy())) {
            return;
        }
        mResources.a(RMS_LAST_DATE, today);
        System.out.println("[REWARD][SCHEDULE] start date=" + today);
        ActivityQuickClaim.startScheduledClaims();
    }

    private static String dateKey(Calendar calendar) {
        Calendar local = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"));
        local.setTimeInMillis(calendar.getTime().getTime());
        return String.valueOf(local.get(Calendar.YEAR)) + "-" + String.valueOf(local.get(Calendar.DAY_OF_YEAR));
    }
}

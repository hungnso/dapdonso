public final class DailyScheduleStatus {
    private DailyScheduleStatus() {
    }

    public static String describe(boolean enabled, int nowMinutes, int scheduledMinutes, boolean ranToday, boolean uiBlocked) {
        if (!enabled) {
            return "Tat";
        }
        if (nowMinutes < scheduledMinutes) {
            int hour = scheduledMinutes / 60;
            int minute = scheduledMinutes % 60;
            return "Cho " + (hour < 10 ? "0" : "") + hour + ":" + (minute < 10 ? "0" : "") + minute;
        }
        if (ranToday) {
            return "Da chay hom nay";
        }
        if (uiBlocked) {
            return "Dang doi dong menu";
        }
        return "San sang chay";
    }
}

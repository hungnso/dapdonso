import java.util.Calendar;

/** RMS checkpoint for the account-wide Auto NVHN 3x character batch. */
public final class AutoNvhn3xBatchState {
    private static final String RMS = "AutoNvhn3xBatchState";
    public int doneMask;
    public int currentSlot;
    public int phase;

    public static AutoNvhn3xBatchState load() {
        AutoNvhn3xBatchState state = new AutoNvhn3xBatchState();
        String raw = mResources.c(RMS);
        String today = today();
        String account = SelectServerScr.uname == null ? "" : SelectServerScr.uname;
        if (raw == null || raw.length() == 0 || raw.indexOf(account + "|" + today + "|") != 0) {
            state.currentSlot = -1;
            return state;
        }
        try {
            String[] parts = raw.split("\\|");
            state.doneMask = Integer.parseInt(parts[2]);
            state.currentSlot = Integer.parseInt(parts[3]);
            state.phase = Integer.parseInt(parts[4]);
        } catch (Exception ignored) {
            state.doneMask = 0;
            state.currentSlot = -1;
            state.phase = 0;
        }
        return state;
    }

    public void save() {
        String account = SelectServerScr.uname == null ? "" : SelectServerScr.uname;
        mResources.a(RMS, account + "|" + today() + "|" + doneMask + "|" + currentSlot + "|" + phase);
    }

    public static void clear() {
        mResources.a(RMS, "");
    }

    private static String today() {
        Calendar now = Calendar.getInstance();
        return now.get(Calendar.YEAR) + "-" + now.get(Calendar.DAY_OF_YEAR);
    }
}

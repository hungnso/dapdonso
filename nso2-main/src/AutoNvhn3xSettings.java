/** Applies the dedicated Auto NVHN 3x profile while preserving global settings. */
public final class AutoNvhn3xSettings {
    private static final String RMS_SNAPSHOT = "AutoNvhn3xSettingsSnapshot";
    private static boolean foodPurchasePaused;

    private AutoNvhn3xSettings() {
    }

    public static void begin() {
        if (mResources.c(RMS_SNAPSHOT) == null) {
            mResources.a(RMS_SNAPSHOT, encodeCurrent());
        }
        applyProfile();
        foodPurchasePaused = false;
    }

    public static void restore() {
        String snapshot = mResources.c(RMS_SNAPSHOT);
        if (snapshot != null && snapshot.length() > 0) {
            decodeAndApply(snapshot);
        }
        mResources.a(RMS_SNAPSHOT, "");
        foodPurchasePaused = false;
    }

    public static boolean isActive() {
        String value = mResources.c(RMS_SNAPSHOT);
        return value != null && value.length() > 0;
    }

    /**
     * The legacy auto loop opens the food NPC UI as soon as food is low.  That
     * menu captures input and prevents the Goosho fashion menu from being
     * selected.  Pause it once before entering the mask purchase flow.
     */
    public static void beginMaskPurchase() {
        if (!isActive() || foodPurchasePaused
                || !AutoNvhn3xProfile.pauseFoodPurchaseDuringMask()) return;
        foodPurchasePaused = true;
        Char.dw = false;
        // Equivalent to tapping outside the food shop: close every transient
        // UI before the fashion flow opens Goosho's menu.
        if (GameScr.instance != null) {
            GameScr.instance.resetButton();
        }
        System.out.println("[AUTO-NVHN3X] food purchase paused for mask flow");
    }

    /** Restore the profile's auto-food purchase option after mask handling. */
    public static void endMaskPurchase() {
        if (!foodPurchasePaused) return;
        foodPurchasePaused = false;
        if (isActive()) Char.dw = AutoNvhn3xProfile.enabled("autoFoodBuy");
        System.out.println("[AUTO-NVHN3X] food purchase resumed after mask flow");
    }

    public static boolean isMaskPurchaseActive() {
        return foodPurchasePaused;
    }

    private static void applyProfile() {
        Char.timeStartBlink = true;
        Char.isAHP = true;
        Char.isAMP = true;
        Char.isAFood = true;
        Char.isABuff = true;
        Char.isAResuscitate = false;
        Char.isAPickYen = false;
        Char.isAPickYHM = true;
        Char.isAPickYHMS = true;
        Char.dm = true;
        Char.dn = false;
        Char.doa = true;
        Char.dp = true;
        Char.dq = true;
        Char.dr = false;
        Char.ds = false;
        Char.dt = false;
        Char.du = true;
        Char.dv = true;
        Char.dw = true;
        Char.dx = false;
        Char.dy = true;
        Char.dz = true;
        Char.ea = false;
        Char.eb = true;
        Char.ec = true;
        Char.ed = false;
        Char.ee = true;
        Char.ef = true;
        Char.eg = true;
        Char.eh = true;
        Char.ek = AutoNvhn3xProfile.hpThreshold();
        Char.el = AutoNvhn3xProfile.mpThreshold();
        Char.em = AutoNvhn3xProfile.foodLevel();
        Char.en = AutoNvhn3xProfile.hpMpPickupLevel();
        Char.eo = AutoNvhn3xProfile.materialPickupLevel();
        Char.ep = 7;
        Char.eq = AutoNvhn3xProfile.equipmentPickupLevel();
        AutoDailyPanel.weaponOnlyPickup = false;
    }

    private static String encodeCurrent() {
        long mask = 0L;
        boolean[] flags = currentFlags();
        for (int i = 0; i < flags.length; ++i) if (flags[i]) mask |= 1L << i;
        return mask + "|" + Char.ek + "|" + Char.el + "|" + Char.em + "|"
                + Char.en + "|" + Char.eo + "|" + Char.ep + "|" + Char.eq
                + "|" + (AutoDailyPanel.weaponOnlyPickup ? 1 : 0);
    }

    private static void decodeAndApply(String value) {
        try {
            String[] parts = value.split("\\|");
            long mask = Long.parseLong(parts[0]);
            boolean[] flags = new boolean[32];
            for (int i = 0; i < flags.length; ++i) flags[i] = (mask & (1L << i)) != 0L;
            applyFlags(flags);
            Char.ek = Integer.parseInt(parts[1]);
            Char.el = Integer.parseInt(parts[2]);
            Char.em = Integer.parseInt(parts[3]);
            Char.en = Integer.parseInt(parts[4]);
            Char.eo = Integer.parseInt(parts[5]);
            Char.ep = Integer.parseInt(parts[6]);
            Char.eq = Integer.parseInt(parts[7]);
            AutoDailyPanel.weaponOnlyPickup = Integer.parseInt(parts[8]) != 0;
        } catch (Exception ignored) {
            // A malformed snapshot must never prevent the game from starting.
        }
    }

    private static boolean[] currentFlags() {
        return new boolean[]{Char.timeStartBlink, Char.isAHP, Char.isAMP, Char.isAFood,
                Char.isABuff, Char.isAResuscitate, Char.isAPickYen, Char.isAPickYHM,
                Char.isAPickYHMS, Char.dm, Char.dn, Char.doa, Char.dp, Char.dq,
                Char.dr, Char.ds, Char.dt, Char.du, Char.dv, Char.dw, Char.dx,
                Char.dy, Char.dz, Char.ea, Char.eb, Char.ec, Char.ed, Char.ee,
                Char.ef, Char.eg, Char.eh};
    }

    private static void applyFlags(boolean[] flags) {
        Char.timeStartBlink = flags[0]; Char.isAHP = flags[1]; Char.isAMP = flags[2];
        Char.isAFood = flags[3]; Char.isABuff = flags[4]; Char.isAResuscitate = flags[5];
        Char.isAPickYen = flags[6]; Char.isAPickYHM = flags[7]; Char.isAPickYHMS = flags[8];
        Char.dm = flags[9]; Char.dn = flags[10]; Char.doa = flags[11]; Char.dp = flags[12];
        Char.dq = flags[13]; Char.dr = flags[14]; Char.ds = flags[15]; Char.dt = flags[16];
        Char.du = flags[17]; Char.dv = flags[18]; Char.dw = flags[19]; Char.dx = flags[20];
        Char.dy = flags[21]; Char.dz = flags[22]; Char.ea = flags[23]; Char.eb = flags[24];
        Char.ec = flags[25]; Char.ed = flags[26]; Char.ee = flags[27]; Char.ef = flags[28];
        Char.eg = flags[29]; Char.eh = flags[30];
    }
}

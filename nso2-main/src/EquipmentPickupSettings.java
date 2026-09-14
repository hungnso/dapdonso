public final class EquipmentPickupSettings {
    private EquipmentPickupSettings() {
    }

    public static void handleAutoSettingsTouch() {
        if (!GameScr.isPaintAuto || GameScr.aq == null || !GameCanvas.n) {
            return;
        }
        // Clicking the original "Nhat Trang Bi" checkbox switches back to
        // the all-equipment mode before GameScr toggles Char.doa itself.
        if (AutoDailyPanel.weaponOnlyPickup
                && GameCanvas.a(GameScr.aq.c + 16, GameScr.aq.d + 335, 16, 16, GameScr.aq)) {
            AutoDailyPanel.disableWeaponOnlyPickupForEquipmentMode();
            return;
        }
        if (GameCanvas.a(GameScr.aq.c + 16, GameScr.aq.d + 935, 16, 16, GameScr.aq)) {
            AutoDailyPanel.toggleWeaponOnlyPickup();
            GameCanvas.m = false;
        }
    }

    public static void handleAutoSettingsKey() {
        if (!GameScr.isPaintAuto) {
            return;
        }
        if (GameScr.indexRow == 11 && AutoDailyPanel.weaponOnlyPickup) {
            AutoDailyPanel.toggleWeaponOnlyPickup();
        } else if (GameScr.indexRow == 31) {
            AutoDailyPanel.toggleWeaponOnlyPickup();
        }
    }
}

/** Rules shared by the level-1-to-30 quick-start controller and its pickup flow. */
public final class AutoNv130QuickPolicy {
    private static final int TARGET_LEVEL = 30;
    private static final int LINH_CHI_QUANTITY = 4;

    private AutoNv130QuickPolicy() {
    }

    public static boolean canStartAtLevel(int level) {
        return level > 0;
    }

    public static boolean shouldRunStarterPreparation(int level) {
        return level == 1;
    }

    public static boolean shouldRunQuickClaim(int level) {
        return level > 0;
    }

    public static boolean shouldMaintainLinhChi(int level) {
        return level > 0;
    }

    public static boolean canRenewLinhChi(int taskId) {
        return canBuyStarterSupplies(taskId);
    }

    public static int targetLevel() {
        return TARGET_LEVEL;
    }

    public static int linhChiQuantity() {
        return LINH_CHI_QUANTITY;
    }

    /** Task 4 is “Diệt sên trừ cóc”; Goosho supplies unlock only after it advances. */
    public static boolean canBuyStarterSupplies(int taskId) {
        return taskId >= 5;
    }

    public static String startupStepName(int step) {
        switch (step) {
            case 0: return "NHAN_THUONG";
            case 1: return "DIEM_DANH";
            case 2: return "TUI_VAI";
            case 3: return "LINH_CHI_X2";
            default: return "AUTO_NV";
        }
    }

    /** type 1 is weapon; types 2..9 cover clothing and jewellery. Gender 2 is unisex. */
    public static boolean shouldKeepEquipment(int level, int type, int playerGender,
                                              int itemGender, int saleYen) {
        return level >= 20
                && type >= 1 && type <= 9
                && saleYen > 5
                && (itemGender == 2 || itemGender == playerGender);
    }
}

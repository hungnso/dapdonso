/** Rules shared by the level-1-to-30 quick-start controller and its pickup flow. */
public final class AutoNv130QuickPolicy {
    private static final int TARGET_LEVEL = 31;
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

    public static boolean shouldBuyFashionMaskAtLevel(int targetLevel, int characterLevel) {
        return targetLevel == TARGET_LEVEL && characterLevel >= 10;
    }

    public static String questFlowLabel(int targetLevel) {
        return targetLevel == TARGET_LEVEL ? "Auto NV nhanh Lv1-31" : "Auto NV chinh Lv1-50";
    }

    public static int gooshoSchoolMap(int classId) {
        return classId <= 2 ? 1 : (classId <= 4 ? 27 : 72);
    }

    public static boolean shouldWaitForGoosho(long routeStartedAt, long now) {
        return routeStartedAt > 0L && now - routeStartedAt < 8000L;
    }

    /** Fashion headwear is type 11; the requested masks cost exactly 30 gold. */
    public static boolean isMatchingFashionMask(int templateType, int itemGender,
                                                int characterGender, int goldCost) {
        return templateType == 11 && (itemGender == characterGender || itemGender == 2) && goldCost == 30;
    }

    public static boolean shouldInspectFashionHatListing(int templateType, int itemGender,
                                                         int characterGender) {
        return templateType == 11 && (itemGender == characterGender || itemGender == 2);
    }

    public static boolean isRequestedFashionHat(int templateType, int itemGender,
                                                 int characterGender, String itemName) {
        return shouldInspectFashionHatListing(templateType, itemGender, characterGender)
                && isRequestedFashionHatName(itemName);
    }

    public static boolean isRequestedFashionHatName(String itemName) {
        return itemName != null && itemName.trim().toLowerCase().equals("m\u0169 th\u1eddi trang");
    }

    public static boolean shouldWaitForFashionListingAction(long lastActionAt, long now) {
        return now - lastActionAt < 300L;
    }

    public static boolean shouldRouteToGooshoBeforeFashionPurchase(boolean atGoosho) {
        return !atGoosho;
    }

    public static boolean shouldOpenFashionMenuBeforeListing(boolean fashionMenuSelected) {
        return !fashionMenuSelected;
    }

    public static int fashionHatListedIndexForGender(int characterGender) {
        return characterGender == 0 ? 11 : (characterGender == 1 ? 10 : -1);
    }

    public static boolean canBuyFashionMaskAtListedIndex(int templateType, int itemGender,
                                                         int characterGender, int listedGold,
                                                         int availableGold) {
        return templateType == 11 && (itemGender == characterGender || itemGender == 2)
                && (listedGold == 0 || listedGold == 30) && availableGold >= 30;
    }

    /** The requested Mũ thời trang is the first listing in Goosho's Fashion tab. */
    public static boolean isRequestedFashionHatAtListedIndex(int listedIndex, int templateType,
                                                              int itemGender, int characterGender,
                                                              int listedGold) {
        return listedIndex == 0 && templateType == 11
                && (itemGender == characterGender || itemGender == 2)
                && (listedGold == 0 || listedGold == 30);
    }

    /** The narrow emulator can strand the item-information overlay without a close command. */
    public static boolean shouldRequestEquipmentInfo(boolean quickFlow) {
        return !quickFlow;
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

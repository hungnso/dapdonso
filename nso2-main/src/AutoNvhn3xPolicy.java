/** Pure rules for the daily quest flow exposed as Auto NVHN 3x. */
public final class AutoNvhn3xPolicy {
    private AutoNvhn3xPolicy() {
    }

    public static String menuLabel() {
        return "Auto NVHN 3x";
    }

    public static int menuCommandId() {
        return 1100110;
    }

    public static String flowLabel() {
        return menuLabel();
    }

    public static boolean shouldEnterHang() {
        return true;
    }

    public static boolean shouldRunDailyQuest() {
        return true;
    }

    public static boolean shouldRunTaThu() {
        return false;
    }

    public static int firstQuickClaimMode() {
        return 2; // ActivityQuickClaim attendance mode.
    }

    public static int secondQuickClaimMode() {
        return 1; // ActivityQuickClaim activity mode.
    }

    public static boolean shouldStopAfterClaims() {
        return true;
    }

    public static boolean shouldBuyMask(boolean ownsMask, boolean hasInventorySlot,
                                        int gold, int characterGender) {
        return !ownsMask && hasInventorySlot && gold >= 30
                && maskListingIndex(characterGender) >= 0;
    }

    public static int maskListingIndex(int characterGender) {
        return AutoNv130QuickPolicy.fashionHatListedIndexForGender(characterGender);
    }

    public static boolean isFashionListingIndexForGender(int listedIndex, int characterGender) {
        return listedIndex == maskListingIndex(characterGender);
    }

    public static boolean shouldEquipPurchasedMask() {
        return true;
    }

    /** The configured shop index is authoritative; some servers invert metadata gender. */
    public static boolean shouldAcceptConfiguredMaskIndex(int listedIndex,
                                                          int characterGender,
                                                          int serverItemGender) {
        return isFashionListingIndexForGender(listedIndex, characterGender);
    }
}

public final class AutoNvhn3xPolicyTest {
    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void main(String[] args) {
        check(AutoNvhn3xPolicy.menuLabel().equals("Auto NVHN 3x"),
                "the new root menu entry must be named Auto NVHN 3x");
        check(AutoNvhn3xPolicy.menuCommandId() == 1100110,
                "Auto NVHN 3x must have a dedicated menu command");
        check(AutoNvhn3xPolicy.flowLabel().equals("Auto NVHN 3x"),
                "the running auto label must identify the new flow");
        check(AutoNvhn3xPolicy.shouldEnterHang(),
                "Auto NVHN 3x must enter the level-appropriate cave first");
        check(AutoNvhn3xPolicy.shouldRunDailyQuest(),
                "Auto NVHN 3x must run daily quests");
        check(!AutoNvhn3xPolicy.shouldRunTaThu(),
                "Auto NVHN 3x must never run Ta Thu");
        check(AutoNvhn3xPolicy.firstQuickClaimMode() == 2,
                "attendance must be claimed before activity rewards");
        check(AutoNvhn3xPolicy.secondQuickClaimMode() == 1,
                "activity rewards must be claimed after attendance");
        check(AutoNvhn3xPolicy.shouldStopAfterClaims(),
                "Auto NVHN 3x must stop after both quick reward tabs");
        check(AutoNvhn3xPolicy.shouldBuyMask(false, true, 30, 1),
                "missing mask with enough gold must start purchase");
        check(!AutoNvhn3xPolicy.shouldBuyMask(true, true, 30, 1),
                "owned mask must skip purchase");
        check(!AutoNvhn3xPolicy.shouldBuyMask(false, true, 29, 1),
                "insufficient gold must skip purchase");
        check(!AutoNvhn3xPolicy.shouldBuyMask(false, false, 30, 1),
                "no inventory slot must skip purchase");
        check(AutoNvhn3xPolicy.maskListingIndex(1) == 10,
                "male characters must use fashion listing index 10");
        check(AutoNvhn3xPolicy.maskListingIndex(0) == 11,
                "female characters must use fashion listing index 11");
        check(AutoNvhn3xPolicy.isFashionListingIndexForGender(10, 1),
                "fashion index 10 must be the male slot");
        check(AutoNvhn3xPolicy.isFashionListingIndexForGender(11, 0),
                "fashion index 11 must be the female slot");
        check(!AutoNvhn3xPolicy.isFashionListingIndexForGender(11, 1),
                "male flow must never select fashion index 11");
        check(AutoNvhn3xPolicy.shouldEquipPurchasedMask(),
                "purchased mask must be equipped immediately");
        check(AutoNv130QuickPolicy.shouldMatchFashionMenuCaption("th?i trang"),
                "mojibake Fashion caption must still be recognized");
        check(AutoNvhn3xPolicy.shouldAcceptConfiguredMaskIndex(11, 0, 1),
                "configured female index 11 must be accepted even if server gender metadata is inverted");
        check(!AutoNvhn3xPolicy.shouldAcceptConfiguredMaskIndex(10, 0, 1),
                "female flow must still reject configured male index 10");
    }
}

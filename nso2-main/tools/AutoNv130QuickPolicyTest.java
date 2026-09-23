public final class AutoNv130QuickPolicyTest {
    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void main(String[] args) {
        check(AutoNv130QuickPolicy.canStartAtLevel(1), "level 1 must start the full quick flow");
        check(AutoNv130QuickPolicy.canStartAtLevel(2), "higher levels must continue quests without starter shopping");
        check(AutoNv130QuickPolicy.shouldRunStarterPreparation(1), "level 1 must run starter preparation");
        check(!AutoNv130QuickPolicy.shouldRunStarterPreparation(2), "higher levels must skip starter preparation");
        check(AutoNv130QuickPolicy.shouldRunQuickClaim(2), "higher levels must still claim rewards and attendance");
        check(AutoNv130QuickPolicy.shouldMaintainLinhChi(2), "higher levels must renew x2 Linh Chi when it expires");
        check(!AutoNv130QuickPolicy.canRenewLinhChi(3), "x2 renewal must not route to Goosho before task 4 is complete");
        check(AutoNv130QuickPolicy.canRenewLinhChi(5), "x2 renewal may route to Goosho after task 4 advances");
        check(AutoNv130QuickPolicy.targetLevel() == 31, "quick flow must stop at level 31");
        check(AutoNv130QuickPolicy.startupStepName(0).equals("NHAN_THUONG"), "first step must claim rewards");
        check(AutoNv130QuickPolicy.startupStepName(3).equals("LINH_CHI_X2"), "fourth step must prepare x2 Linh Chi");
        check(AutoNv130QuickPolicy.linhChiQuantity() == 4, "quick flow must buy and use exactly four Linh Chi");
        check(!AutoNv130QuickPolicy.canBuyStarterSupplies(4),
                "starter supplies must wait until task 4 Diet sen tru coc is complete");
        check(AutoNv130QuickPolicy.canBuyStarterSupplies(5),
                "starter supplies may be bought only after task 4 advances to task 5");
        check(AutoNv130QuickPolicy.shouldKeepEquipment(20, 1, 1, 1, 6),
                "level 20 must keep a matching weapon worth more than 5 yen");
        check(AutoNv130QuickPolicy.shouldKeepEquipment(20, 2, 1, 2, 6),
                "level 20 must keep matching clothing/accessories worth more than 5 yen");
        check(!AutoNv130QuickPolicy.shouldKeepEquipment(19, 1, 1, 1, 6),
                "before level 20 the equipment-priority rule must not apply");
        check(!AutoNv130QuickPolicy.shouldKeepEquipment(20, 1, 0, 1, 6),
                "wrong-gender equipment must not be kept");
        check(!AutoNv130QuickPolicy.shouldKeepEquipment(20, 1, 1, 1, 5),
                "equipment worth exactly 5 yen must not be kept");
        check(AutoNv130QuickPolicy.shouldKeepEquipment(20, 3, 1, 2, 7),
                "unisex equipment must be kept when worth more than 5 yen");
        check(AutoNv130QuickPolicy.shouldBuyFashionMaskAtLevel(31, 10),
                "Auto NV 1-31 must buy and equip the fashion headwear at level 10");
        check(AutoNv130QuickPolicy.shouldBuyFashionMaskAtLevel(31, 11),
                "Auto NV 1-31 must buy the fashion headwear after level 10 regardless of task");
        check(!AutoNv130QuickPolicy.shouldBuyFashionMaskAtLevel(31, 9),
                "Auto NV 1-31 must wait until level 10 before buying the fashion headwear");
        check(!AutoNv130QuickPolicy.shouldBuyFashionMaskAtLevel(50, 11),
                "normal Auto NV must not run the Auto NV 1-31 fashion purchase flow");
        check(AutoNv130QuickPolicy.gooshoSchoolMap(2) == 1,
                "classes 1-2 must route to Goosho at school 1");
        check(AutoNv130QuickPolicy.gooshoSchoolMap(3) == 27,
                "classes 3-4 must route to Goosho at school 27");
        check(AutoNv130QuickPolicy.gooshoSchoolMap(5) == 72,
                "classes 5-6 must route to Goosho at school 72");
        check(AutoNv130QuickPolicy.shouldWaitForGoosho(1000L, 8999L),
                "fashion flow may briefly wait for Goosho after routing");
        check(!AutoNv130QuickPolicy.shouldWaitForGoosho(1000L, 9000L),
                "fashion flow must release the quest after Goosho route times out");
        check(AutoNv130QuickPolicy.questFlowLabel(31).equals("Auto NV nhanh Lv1-31"),
                "the quick flow must retain its 1-31 label while task data is temporarily unavailable");
        check(AutoNv130QuickPolicy.questFlowLabel(50).equals("Auto NV chinh Lv1-50"),
                "the normal quest flow must retain its 1-50 label");
        check(AutoNv130QuickPolicy.isMatchingFashionMask(11, 0, 0, 30),
                "male mask at 30 gold matches male character");
        check(AutoNv130QuickPolicy.isMatchingFashionMask(11, 1, 1, 30),
                "female mask at 30 gold matches female character");
        check(AutoNv130QuickPolicy.isMatchingFashionMask(11, 2, 1, 30),
                "unisex fashion headwear at 30 gold matches either character gender");
        check(!AutoNv130QuickPolicy.isMatchingFashionMask(11, 1, 0, 30),
                "wrong-gender mask must not be purchased");
        check(!AutoNv130QuickPolicy.isMatchingFashionMask(11, 0, 0, 29),
                "only the 30-gold mask is eligible");
        check(!AutoNv130QuickPolicy.isMatchingFashionMask(12, 0, 0, 30),
                "type 12 is not the fashion-headwear slot");
        check(AutoNv130QuickPolicy.shouldInspectFashionHatListing(11, 0, 0),
                "the requested fashion hat listing must load server item details before buying");
        check(!AutoNv130QuickPolicy.shouldInspectFashionHatListing(12, 0, 0),
                "a non-headwear listing must not request details for this purchase flow");
        check(AutoNv130QuickPolicy.isRequestedFashionHat(11, 0, 0, "M\u0169 th\u1eddi trang"),
                "only Mũ thời trang is the requested fashion purchase");
        check(!AutoNv130QuickPolicy.isRequestedFashionHat(11, 0, 0, "M\u1eb7t n\u1ea1"),
                "a 5-gold mask must not be treated as the requested fashion hat");
        check(AutoNv130QuickPolicy.isRequestedFashionHatName("M\u0169 th\u1eddi trang"),
                "the Fashion listing must be selected by its requested name after details load");
        check(!AutoNv130QuickPolicy.isRequestedFashionHatName("M\u1eb7t n\u1ea1"),
                "the mask listing must not match the requested fashion hat name");
        check(AutoNv130QuickPolicy.shouldWaitForFashionListingAction(1000L, 1299L),
                "Fashion listing scan must briefly wait for the item-detail response");
        check(!AutoNv130QuickPolicy.shouldWaitForFashionListingAction(1000L, 1300L),
                "Fashion listing scan must advance after 300ms");
        check(AutoNv130QuickPolicy.shouldRouteToGooshoBeforeFashionPurchase(false),
                "fashion purchase must route to Goosho before scanning the shop");
        check(!AutoNv130QuickPolicy.shouldRouteToGooshoBeforeFashionPurchase(true),
                "fashion purchase may scan the shop only after reaching Goosho");
        check(AutoNv130QuickPolicy.shouldOpenFashionMenuBeforeListing(false),
                "Goosho's Fashion menu must be selected before requesting listings");
        check(!AutoNv130QuickPolicy.shouldOpenFashionMenuBeforeListing(true),
                "listing requests may begin after Fashion menu selection");
        check(AutoNv130QuickPolicy.fashionHatListedIndexForGender(1) == 10,
                "male characters must buy the Fashion listing at index 10");
        check(AutoNv130QuickPolicy.fashionHatListedIndexForGender(0) == 11,
                "female characters must buy the Fashion listing at index 11");
        check(AutoNv130QuickPolicy.isFashionMaskType(11),
                "an existing fashion mask must skip the purchase flow");
        check(!AutoNv130QuickPolicy.isFashionMaskType(12),
                "unrelated special equipment must not skip the fashion-mask purchase");
        check(AutoNv130QuickPolicy.canBuyFashionMaskAtListedIndex(11, 0, 0, 30, 30),
                "a listed matching headwear index may be bought with 30 gold before item details load");
        check(!AutoNv130QuickPolicy.canBuyFashionMaskAtListedIndex(11, 0, 0, 30, 29),
                "a listed headwear index must not be bought below 30 gold");
        check(AutoNv130QuickPolicy.canBuyFashionMaskAtListedIndex(11, 0, 0, 30, 30),
                "the 30-gold fashion headwear may be bought with 30 gold");
        check(!AutoNv130QuickPolicy.canBuyFashionMaskAtListedIndex(11, 0, 0, 5, 30),
                "the 5-gold fashion mask must never be bought for the 1-31 flow");
        check(AutoNv130QuickPolicy.canBuyFashionMaskAtListedIndex(11, 0, 0, 0, 30),
                "the requested listing may be bought when the server has not sent its price detail yet");
        check(AutoNv130QuickPolicy.isRequestedFashionHatAtListedIndex(0, 11, 0, 0, 30),
                "the first Fashion-tab listing is the requested 30-gold fashion hat");
        check(AutoNv130QuickPolicy.isRequestedFashionHatAtListedIndex(0, 11, 0, 0, 0),
                "the first Fashion-tab listing may be bought while its price detail is not loaded");
        check(!AutoNv130QuickPolicy.isRequestedFashionHatAtListedIndex(0, 11, 0, 0, 5),
                "a first listing explicitly priced at 5 gold must not be bought");
        check(!AutoNv130QuickPolicy.isRequestedFashionHatAtListedIndex(1, 11, 0, 0, 30),
                "other Fashion-tab listings must not replace the requested fashion hat");
        check(!AutoNv130QuickPolicy.shouldRequestEquipmentInfo(true),
                "quick flow must not open equipment information panels");
        check(AutoNv130QuickPolicy.shouldRequestEquipmentInfo(false),
                "normal auto may retain equipment price inspection");
    }
}

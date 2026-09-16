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
        check(AutoNv130QuickPolicy.shouldBuyFashionMaskAtTarget(31, 31),
                "quick flow must process the fashion mask at level 31");
        check(!AutoNv130QuickPolicy.shouldBuyFashionMaskAtTarget(50, 50),
                "normal level-50 auto must not buy a fashion mask");
        check(AutoNv130QuickPolicy.isMatchingFashionMask(12, 0, 0, 30),
                "male mask at 30 gold matches male character");
        check(AutoNv130QuickPolicy.isMatchingFashionMask(12, 1, 1, 30),
                "female mask at 30 gold matches female character");
        check(!AutoNv130QuickPolicy.isMatchingFashionMask(12, 1, 0, 30),
                "wrong-gender mask must not be purchased");
        check(!AutoNv130QuickPolicy.isMatchingFashionMask(12, 0, 0, 29),
                "only the 30-gold mask is eligible");
        check(!AutoNv130QuickPolicy.shouldRequestEquipmentInfo(true),
                "quick flow must not open equipment information panels");
        check(AutoNv130QuickPolicy.shouldRequestEquipmentInfo(false),
                "normal auto may retain equipment price inspection");
    }
}

public final class AutoUpgradeEquipmentTest {
    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new RuntimeException(message);
        }
    }

    public static void main(String[] args) {
        check("NORMAL".equals(AutoUpgradeEquipment.evaluatePolicyForTest(5, true, true, false, false)),
                "+5 must remain normal and ignore careful-only resources");
        check("NO_LUONG".equals(AutoUpgradeEquipment.evaluatePolicyForTest(6, true, true, false, true)),
                "+6 must pause when Luong is insufficient");
        check("NO_INSURANCE".equals(AutoUpgradeEquipment.evaluatePolicyForTest(7, true, true, true, false)),
                "+7 must pause when insurance is absent");
        check("CAREFUL".equals(AutoUpgradeEquipment.evaluatePolicyForTest(6, true, true, true, true)),
                "+6 must use careful mode");
        check("COMPLETE".equals(AutoUpgradeEquipment.evaluatePolicyForTest(8, false, false, false, false)),
                "+8 must complete without another request");
        check("NO_STONE".equals(AutoUpgradeEquipment.evaluatePolicyForTest(5, false, true, false, false)),
                "missing stones must pause before a normal request");
        check("NO_MONEY".equals(AutoUpgradeEquipment.evaluatePolicyForTest(5, true, false, false, false)),
                "missing money must pause before a normal request");
        check("CAREFUL".equals(AutoUpgradeEquipment.evaluatePolicyForTest(7, true, true, true, true)),
                "+7 must remain careful");
        check(AutoUpgradeEquipment.MENU_ROOT == 1100100,
                "root menu command must remain the documented non-conflicting ID");

        ItemTemplate weapon = new ItemTemplate((short)11, (byte)1, (byte)0, "Test weapon", "", (byte)40, (short)0, (short)0, false);
        Item first = new Item();
        first.template = weapon;
        first.upgrade = 3;
        first.indexUI = 2;
        Item second = new Item();
        second.template = weapon;
        second.upgrade = 3;
        second.indexUI = 3;
        AutoUpgradeEquipment.TargetDescriptor selected = AutoUpgradeEquipment.TargetDescriptor.forTest(first, false, 2);
        check(selected.matches(first), "descriptor must match selected item");
        check(!selected.matches(second), "descriptor must not match a same-template item in another original slot");
        check(AutoUpgradeEquipment.stoneTierForUpgradeForTest(0) == 5,
                "+0 -> +1 must only use stone tier 5");
        check(AutoUpgradeEquipment.stoneTierForUpgradeForTest(3) == 3,
                "+3 -> +4 must only use stone tier 3");
        check(AutoUpgradeEquipment.stoneTierForUpgradeForTest(6) == 5,
                "+6 -> +7 must only use stone tier 5");
        check(AutoUpgradeEquipment.stoneTierForUpgradeForTest(7) == 6,
                "+7 -> +8 must only use stone tier 6");
        check(AutoUpgradeEquipment.shouldSplitStoneTierForTest(100, 0, 100),
                "stacked tier-5 stones must be split before selection when total power is sufficient");
        check(AutoUpgradeEquipment.shouldSplitStoneTierForTest(100, 40, 100),
                "the splitter must continue until enough tier-5 stones are separated for this upgrade");
        check(!AutoUpgradeEquipment.shouldSplitStoneTierForTest(90, 0, 100),
                "the splitter must not run when the total tier-5 power is insufficient");
        ItemTemplate namedStone = new ItemTemplate((short)42, (byte)26, (byte)0, "Đá cấp 5", "", (byte)1, (short)0, (short)0, false);
        Item stone = new Item();
        stone.template = namedStone;
        stone.quantity = 1;
        check(AutoUpgradeEquipment.matchesStoneTierForTest(stone, 5),
                "a runtime stone named Đá cấp 5 must match tier 5 even when its template ID is not 5");
    }
}

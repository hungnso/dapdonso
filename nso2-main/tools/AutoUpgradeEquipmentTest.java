public final class AutoUpgradeEquipmentTest {
    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new RuntimeException(message);
        }
    }

    public static void main(String[] args) throws Exception {
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
        check(AutoUpgradeEquipment.stoneTierForUpgradeForTest(3) == 5,
                "+3 -> +4 must only use stone tier 5");
        check(AutoUpgradeEquipment.stoneTierForUpgradeForTest(6) == 5,
                "+6 -> +7 must only use stone tier 5");
        check(AutoUpgradeEquipment.stoneTierForUpgradeForTest(7) == 6,
                "+7 -> +8 must only use stone tier 6");
        check(AutoUpgradeEquipment.stoneCountForUpgradeForTest(0) == 1,
                "+0 -> +1 must send exactly one tier-5 stone");
        check(AutoUpgradeEquipment.stoneCountForUpgradeForTest(4) == 1,
                "+4 -> +5 must send exactly one tier-5 stone");
        check(AutoUpgradeEquipment.stoneCountForUpgradeForTest(5) == 2,
                "+5 -> +6 must send exactly two tier-5 stones");
        check(AutoUpgradeEquipment.stoneCountForUpgradeForTest(6) == 3,
                "+6 -> +7 must send exactly three tier-5 stones");
        check(AutoUpgradeEquipment.stoneCountForUpgradeForTest(7) == 3,
                "+7 -> +8 must send exactly three tier-6 stones");
        check("SPLIT".equals(AutoUpgradeEquipment.stoneCountActionForTest(16, 0, 3)),
                "sixteen stacked stones must split instead of reporting a shortage for three stones");
        check("SELECT".equals(AutoUpgradeEquipment.stoneCountActionForTest(16, 3, 3)),
                "three separated stones must be selected immediately from the same inventory snapshot");
        check("SHORTAGE".equals(AutoUpgradeEquipment.stoneCountActionForTest(2, 0, 3)),
                "a shortage is valid only when the total stone count is below the required count");
        check(AutoUpgradeEquipment.shouldSplitStoneTierForTest(100, 0, 100),
                "stacked tier-5 stones must be split before selection when total power is sufficient");
        check(AutoUpgradeEquipment.shouldSplitStoneTierForTest(100, 40, 100),
                "the splitter must continue until enough tier-5 stones are separated for this upgrade");
        check(!AutoUpgradeEquipment.shouldSplitStoneTierForTest(90, 0, 100),
                "the splitter must not run when the total tier-5 power is insufficient");
        check("Auto dap do: thieu da 5 (can 100, co 40)".equals(AutoUpgradeEquipment.stoneShortageReasonForTest(5, 100, 40)),
                "a material stop must show both required and available tier-5 stone power");
        ItemTemplate namedStone = new ItemTemplate((short)42, (byte)26, (byte)0, "Đá cấp 5", "", (byte)1, (short)0, (short)0, false);
        Item stone = new Item();
        stone.template = namedStone;
        stone.quantity = 1;
        check(AutoUpgradeEquipment.matchesStoneTierForTest(stone, 5),
                "a runtime stone named Đá cấp 5 must match tier 5 even when its template ID is not 5");
        AutoUpgradeEquipment auto = AutoUpgradeEquipment.gI();
        java.lang.reflect.Field pauseNotified = AutoUpgradeEquipment.class.getDeclaredField("pauseNotified");
        pauseNotified.setAccessible(true);
        pauseNotified.setBoolean(auto, true);
        java.lang.reflect.Method stopForMissingMaterial = AutoUpgradeEquipment.class.getDeclaredMethod("pause", String.class);
        stopForMissingMaterial.setAccessible(true);
        stopForMissingMaterial.invoke(auto, "missing upgrade material");
        check(auto.getStatusText().contains("| IDLE"),
                "missing upgrade material must stop the auto in IDLE state");

        java.lang.reflect.Field state = AutoUpgradeEquipment.class.getDeclaredField("state");
        state.setAccessible(true);
        Class<?> stateType = Class.forName("AutoUpgradeEquipment$State");
        Object selectTargetState = stateType.getMethod("valueOf", String.class).invoke(null, "SELECT_TARGET");
        state.set(auto, selectTargetState);
        java.lang.reflect.Field descriptor = AutoUpgradeEquipment.class.getDeclaredField("descriptor");
        descriptor.setAccessible(true);
        descriptor.set(auto, AutoUpgradeEquipment.TargetDescriptor.forTest(first, false, 2));
        GameCanvas.currentDialog = new Dialog() { };
        java.lang.reflect.Method start = AutoUpgradeEquipment.class.getDeclaredMethod("start");
        start.setAccessible(true);
        start.invoke(auto);
        check(GameCanvas.currentDialog == null,
                "starting auto upgrade must dismiss its confirmation dialog so menu keys remain usable");

        java.lang.reflect.Field splitPending = AutoUpgradeEquipment.class.getDeclaredField("splitPending");
        splitPending.setAccessible(true);
        GameCanvas.inputDlg = new InputDlg();
        GameCanvas.input2Dlg = new Input2Dlg();
        GameCanvas.currentDialog = GameCanvas.inputDlg;
        splitPending.setBoolean(auto, true);
        pauseNotified.setBoolean(auto, true);
        stopForMissingMaterial.invoke(auto, "thieu da 5");
        check(GameCanvas.currentDialog == null,
                "stopping while auto splits stones must release its input dialog for menu keys");
        check(auto.getStatusText().contains("thieu da 5"),
                "IDLE status must retain the material-shortage reason for the next menu open");
    }
}

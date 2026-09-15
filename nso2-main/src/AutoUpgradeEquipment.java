public final class AutoUpgradeEquipment {
    public static final int MENU_ROOT = 1100100;
    public static final int MENU_SELECT = 1100101;
    public static final int MENU_START = 1100102;
    public static final int MENU_STOP = 1100103;
    public static final int MENU_CLOSE = 1100104;
    private static final int TARGET_UPGRADE = 8;
    private static final int STONE_TIER_THREE = 3;
    private static final int STONE_TIER_FIVE = 5;
    private static final int STONE_TIER_SIX = 6;
    private static final long ACTION_DELAY = 1200L;
    private static final long RESULT_TIMEOUT = 20000L;
    private static final int BAG = 0;
    private static final int BODY = 1;
    private static final AutoUpgradeEquipment INSTANCE = new AutoUpgradeEquipment();

    private enum State {
        IDLE, SELECT_TARGET, CHECK_TARGET, UNEQUIP_TARGET, PREPARE,
        SEND_UPGRADE, WAIT_RESULT, VERIFY_RESULT, REEQUIP_TARGET,
        PAUSED, COMPLETE, ERROR
    }

    private State state = State.IDLE;
    private TargetDescriptor descriptor;
    private Item target;
    private Item[] selectedStones;
    private Item selectedInsurance;
    private boolean wasEquipped;
    private boolean cancelRequested;
    private boolean pauseNotified;
    private int oldUpgrade = -1;
    private long actionAt;
    private boolean splitPending;
    private int splitIndex = -1;
    private int splitQuantity;
    private boolean unequipRequested;
    private boolean reequippedRequested;
    private boolean combineStoneSixPending;
    private long combineStoneSixAt;

    private AutoUpgradeEquipment() {
    }

    public static AutoUpgradeEquipment gI() {
        return INSTANCE;
    }

    public static String evaluatePolicyForTest(int upgrade, boolean hasStone, boolean hasMoney, boolean hasLuong, boolean hasInsurance) {
        if (upgrade >= TARGET_UPGRADE) return "COMPLETE";
        if (!hasStone) return "NO_STONE";
        if (!hasMoney) return "NO_MONEY";
        if (upgrade < 6) return "NORMAL";
        if (!hasLuong) return "NO_LUONG";
        return hasInsurance ? "CAREFUL" : "NO_INSURANCE";
    }

    public static int stoneTierForUpgradeForTest(int currentUpgrade) {
        if (currentUpgrade == 3) return STONE_TIER_THREE;
        return currentUpgrade >= 7 ? STONE_TIER_SIX : STONE_TIER_FIVE;
    }

    public static boolean matchesStoneTierForTest(Item item, int stoneTier) {
        return isStoneTierItem(item, stoneTier);
    }

    public static boolean shouldSplitStoneTierForTest(int totalPower, int separatedPower, int neededPower) {
        return totalPower >= neededPower && separatedPower < neededPower;
    }

    public boolean isActive() {
        return this.state != State.IDLE && this.state != State.COMPLETE && this.state != State.ERROR;
    }

    public String getStatusText() {
        String name = this.target == null || this.target.template == null ? "?" : this.target.template.name;
        String level = this.target == null ? "" : " +" + this.target.upgrade;
        return "Auto dap do: " + name + level + " | " + this.state;
    }

    public void handleCommand(int command, Object data) {
        if (command == MENU_ROOT) this.showMenu();
        else if (command == MENU_SELECT && data instanceof Selection) this.selectTarget((Selection)data);
        else if (command == MENU_START) this.start();
        else if (command == MENU_STOP) this.cancel();
        else if (command == MENU_CLOSE) GameCanvas.n();
    }

    public void showMenu() {
        if (this.isActive() || this.state == State.PAUSED) {
            MyVector menu = new MyVector();
            menu.addElement(new Command1(this.getStatusText(), MENU_CLOSE));
            menu.addElement(new Command1("Dung Auto", MENU_STOP));
            menu.addElement(new Command1("Dong", MENU_CLOSE));
            GameCanvas.menu.startAt(menu);
            return;
        }
        MyVector menu = new MyVector();
        Char me = Char.getMyChar();
        if (me != null) {
            this.addTargets(menu, me.arrItemBody, BODY);
            this.addTargets(menu, me.arrItemBag, BAG);
        }
        if (menu.size() == 0) {
            GameScr.addChatPopup("Auto dap do: khong co trang bi co the nang cap");
            return;
        }
        GameCanvas.menu.startAt(menu);
    }

    private void addTargets(MyVector menu, Item[] items, int location) {
        if (items == null) return;
        for (int i = 0; i < items.length; ++i) {
            Item item = items[i];
            if (!AutoNhiemVuChinh.isUpgradeEquipmentForStandalone(item)) continue;
            String where = location == BODY ? "Dang mac" : "Tui";
            menu.addElement(new Command1(item.template.name + " +" + item.upgrade + " [" + where + "]", MENU_SELECT, new Selection(item, location, i)));
        }
    }

    private void selectTarget(Selection selection) {
        if (selection == null || !AutoNhiemVuChinh.isUpgradeEquipmentForStandalone(selection.item)) {
            GameScr.addChatPopup("Auto dap do: target khong hop le");
            return;
        }
        this.resetLocal();
        this.target = selection.item;
        this.wasEquipped = selection.location == BODY;
        this.descriptor = new TargetDescriptor(selection.item, selection.slot);
        this.state = State.SELECT_TARGET;
        if (this.target.upgrade >= TARGET_UPGRADE) {
            this.complete();
            return;
        }
        GameCanvas.a("Auto dap do\nItem: " + this.target.template.name + " +" + this.target.upgrade + "\nMuc tieu: +" + TARGET_UPGRADE,
                new Command1("Bat dau", MENU_START), new Command1("Huy", MENU_CLOSE));
    }

    private void start() {
        if (this.state != State.SELECT_TARGET || this.descriptor == null) return;
        if (this.otherUpgradeOwnsState()) {
            this.pause("Auto dap do: dang co luong nang cap khac");
            return;
        }
        this.cancelRequested = false;
        this.pauseNotified = false;
        this.transition(State.CHECK_TARGET);
    }

    public void cancel() {
        if (!this.isActive() && this.state != State.PAUSED) return;
        this.cancelRequested = true;
        if (this.splitPending && GameCanvas.inputDlg != null && GameCanvas.currentDialog == GameCanvas.inputDlg) {
            GameCanvas.n();
        }
        this.splitPending = false;
        this.splitIndex = -1;
        this.splitQuantity = 0;
        if (this.state != State.WAIT_RESULT) this.cleanupAndReequip();
    }

    public void tick() {
        if (this.state == State.IDLE || this.state == State.SELECT_TARGET || this.state == State.PAUSED || this.state == State.COMPLETE || this.state == State.ERROR) return;
        long now = System.currentTimeMillis();
        if (this.cancelRequested && this.state != State.WAIT_RESULT) {
            this.cleanupAndReequip();
            return;
        }
        if (this.state == State.WAIT_RESULT) {
            this.waitResult(now);
            return;
        }
        if (this.otherUpgradeOwnsState()) {
            this.pause("Auto dap do: dang co luong nang cap khac");
            return;
        }
        if (this.state == State.CHECK_TARGET) this.checkTarget();
        else if (this.state == State.UNEQUIP_TARGET) this.unequipTarget(now);
        else if (this.state == State.PREPARE) this.prepareUpgrade(now);
        else if (this.state == State.SEND_UPGRADE) this.sendUpgrade(now);
        else if (this.state == State.VERIFY_RESULT) this.verifyResult();
        else if (this.state == State.REEQUIP_TARGET) this.reequippedTarget(now);
    }

    private void checkTarget() {
        this.target = this.findTarget();
        if (this.target == null) {
            this.pause("Auto dap do: khong tim thay dung target");
            return;
        }
        if (this.target.upgrade >= TARGET_UPGRADE) {
            this.finishOrReequip();
            return;
        }
        if (this.target.isUpMax()) {
            this.pause("Auto dap do: item da dat cap nang toi da truoc +" + TARGET_UPGRADE);
            return;
        }
        if (AutoNhiemVuChinh.findBodySlotForStandalone(Char.getMyChar(), this.target) >= 0) this.transition(State.UNEQUIP_TARGET);
        else this.transition(State.PREPARE);
    }

    private void unequipTarget(long now) {
        Char me = Char.getMyChar();
        int bodySlot = AutoNhiemVuChinh.findBodySlotForStandalone(me, this.target);
        if (bodySlot < 0) {
            this.unequipRequested = false;
            this.transition(State.PREPARE);
            return;
        }
        if (this.unequipRequested) {
            if (now - this.actionAt > 5000L) this.pause("Auto dap do: timeout thao target");
            return;
        }
        if (AutoNhiemVuChinh.findFreeBagIndexForStandalone(me) < 0) {
            this.pause("Auto dap do: can 1 o tui trong de thao do");
            return;
        }
        if (now - this.actionAt < ACTION_DELAY) return;
        this.log("unequip slot=" + bodySlot);
        Service.gI().itemBodyToBag(bodySlot);
        this.actionAt = now;
        this.unequipRequested = true;
    }

    private void prepareUpgrade(long now) {
        Char me = Char.getMyChar();
        this.target = this.findTarget();
        if (me == null || this.target == null) {
            this.pause("Auto dap do: target da thay doi");
            return;
        }
        if (this.target.upgrade >= TARGET_UPGRADE) {
            this.finishOrReequip();
            return;
        }
        if (this.target.isUpMax()) {
            this.pause("Auto dap do: item da dat cap nang toi da truoc +" + TARGET_UPGRADE);
            return;
        }
        int need = AutoNhiemVuChinh.getUpgradeRequirementForStandalone(this.target);
        int cost = AutoNhiemVuChinh.getUpgradeCostForStandalone(this.target);
        if (need <= 0) {
            this.pause("Auto dap do: khong doc duoc yeu cau da");
            return;
        }
        if (cost < 0 || me.xu + me.yen < cost) {
            this.pause("Auto dap do: khong du Yen/Xu");
            return;
        }
        int requiredStoneTier = stoneTierForUpgradeForTest(this.target.upgrade);
        if (requiredStoneTier == STONE_TIER_THREE && this.ensureStoneThree(me, now)) return;
        if (requiredStoneTier == STONE_TIER_SIX && this.ensureStoneSix(me, now)) return;
        if (this.processStoneSplit(me, need, requiredStoneTier, now)) return;
        this.selectedStones = this.chooseOnlyStoneTier(me, need, requiredStoneTier);
        if (this.selectedStones == null) {
            this.pause("Auto dap do: thieu da " + requiredStoneTier);
            return;
        }
        this.selectedInsurance = null;
        if (this.target.upgrade >= 6) {
            int luongNeed = GameScr.cw != null && this.target.upgrade < GameScr.cw.length ? GameScr.cw[this.target.upgrade] : -1;
            if (luongNeed < 0 || me.luong < luongNeed) {
                this.pause("Auto dap do: khong du Luong nang Can Than");
                return;
            }
            this.selectedInsurance = AutoNhiemVuChinh.findInsuranceForStandalone(me);
            if (this.selectedInsurance == null) {
                this.pause("Auto dap do: thieu Bao hiem");
                return;
            }
        }
        this.log("prepare level=" + this.target.upgrade + " stone=" + requiredStoneTier + " need=" + need + " mode=" + (this.selectedInsurance == null ? "NORMAL" : "CAREFUL"));
        this.transition(State.SEND_UPGRADE);
    }

    private boolean ensureStoneSix(Char me, long now) {
        if (this.hasStoneTier(me, STONE_TIER_SIX)) {
            if (this.combineStoneSixPending) this.clearStoneCombineState();
            return false;
        }
        if (this.combineStoneSixPending) {
            if (GameScr.arrItemUpPeal != null && GameScr.arrItemUpPeal.length > 0 && GameScr.arrItemUpPeal[0] != null) {
                this.putBackInBag(GameScr.arrItemUpPeal[0]);
                this.clearStoneCombineState();
                this.pause("Auto dap do: gop da 6 khong thanh cong");
            } else if (now - this.combineStoneSixAt > RESULT_TIMEOUT) {
                this.clearStoneCombineState();
                this.pause("Auto dap do: timeout gop da 6");
            }
            return true;
        }
        if (GameScr.arrItemUpPeal != null && this.hasItem(GameScr.arrItemUpPeal)) {
            this.pause("Auto dap do: dang co luong gop da khac");
            return true;
        }
        int requiredPower = this.getStoneTierRequiredPower(STONE_TIER_SIX);
        int combineCost = this.getStoneTierCombineCost(STONE_TIER_SIX);
        if (requiredPower <= 0 || combineCost < 0) {
            this.pause("Auto dap do: chua nhan duoc du lieu da 6");
            return true;
        }
        if (me.xu < combineCost) {
            this.pause("Auto dap do: khong du Xu de gop da 6");
            return true;
        }
        if (this.processStoneSplit(me, requiredPower, STONE_TIER_FIVE, now)) return true;
        Item[] materials = new Item[24];
        int count = 0;
        int value = 0;
        for (int i = 0; i < me.arrItemBag.length && count < materials.length && value < requiredPower; ++i) {
            Item item = me.arrItemBag[i];
            if (!this.isStoneTier(item, STONE_TIER_FIVE) || item.quantity != 1) continue;
            materials[count++] = item;
            value += AutoNhiemVuChinh.getStoneValueForStandalone(item);
        }
        if (count < 2 || value < requiredPower) {
            this.pause("Auto dap do: khong du da de gop da 6");
            return true;
        }
        GameScr.arrItemUpPeal = materials;
        for (int i = 0; i < materials.length; ++i) this.removeIfPresent(me, materials[i]);
        this.combineStoneSixPending = true;
        this.combineStoneSixAt = now;
        this.log("combine-stone-6 value=" + value + " count=" + count);
        Service.gI().crystalCollect(materials);
        return true;
    }

    private boolean ensureStoneThree(Char me, long now) {
        if (this.hasStoneTier(me, STONE_TIER_THREE)) {
            if (this.combineStoneSixPending) this.clearStoneCombineState();
            return false;
        }
        if (this.combineStoneSixPending) {
            if (GameScr.arrItemUpPeal != null && GameScr.arrItemUpPeal.length > 0 && GameScr.arrItemUpPeal[0] != null) {
                this.putBackInBag(GameScr.arrItemUpPeal[0]);
                this.clearStoneCombineState();
                this.pause("Auto dap do: gop da 3 khong thanh cong");
            } else if (now - this.combineStoneSixAt > RESULT_TIMEOUT) {
                this.clearStoneCombineState();
                this.pause("Auto dap do: timeout gop da 3");
            }
            return true;
        }
        if (GameScr.arrItemUpPeal != null && this.hasItem(GameScr.arrItemUpPeal)) {
            this.pause("Auto dap do: dang co luong gop da khac");
            return true;
        }
        int requiredPower = this.getStoneTierRequiredPower(STONE_TIER_THREE);
        int combineCost = this.getStoneTierCombineCost(STONE_TIER_THREE);
        if (requiredPower <= 0 || combineCost < 0) {
            this.pause("Auto dap do: chua nhan duoc du lieu da 3");
            return true;
        }
        if (me.xu < combineCost) {
            this.pause("Auto dap do: khong du Xu de gop da 3");
            return true;
        }
        if (this.processLowerStoneSplit(me, STONE_TIER_THREE, requiredPower, now)) return true;
        Item[] materials = this.chooseLowerTierStones(me, STONE_TIER_THREE, requiredPower);
        if (materials == null) {
            this.pause("Auto dap do: khong du da de gop da 3");
            return true;
        }
        GameScr.arrItemUpPeal = materials;
        for (int i = 0; i < materials.length; ++i) this.removeIfPresent(me, materials[i]);
        this.combineStoneSixPending = true;
        this.combineStoneSixAt = now;
        this.log("combine-stone-3");
        Service.gI().crystalCollect(materials);
        return true;
    }

    private boolean processLowerStoneSplit(Char me, int targetTier, int need, long now) {
        if (this.splitPending) return this.processPendingStoneSplit(me, now);
        int separated = this.getLowerStonePower(me, targetTier, true);
        if (separated >= need) return false;
        if (this.getLowerStonePower(me, targetTier, false) < need) {
            this.pause("Auto dap do: khong du da de gop da " + targetTier);
            return true;
        }
        if (AutoNhiemVuChinh.findFreeBagIndexForStandalone(me) < 0) {
            this.pause("Auto dap do: can o trong de tach da gop " + targetTier);
            return true;
        }
        for (int tier = targetTier - 1; tier >= 0; --tier) {
            for (int i = 0; i < me.arrItemBag.length; ++i) {
                Item item = me.arrItemBag[i];
                if (!this.isStoneTier(item, tier) || item.quantity <= 1) continue;
                return this.openStoneSplit(item, now);
            }
        }
        this.pause("Auto dap do: khong tach duoc da de gop da " + targetTier);
        return true;
    }

    private int getLowerStonePower(Char me, int targetTier, boolean separatedOnly) {
        int total = 0;
        if (me == null || me.arrItemBag == null) return total;
        for (int tier = targetTier - 1; tier >= 0; --tier) {
            for (int i = 0; i < me.arrItemBag.length; ++i) {
                Item item = me.arrItemBag[i];
                if (!this.isStoneTier(item, tier) || item.quantity <= 0) continue;
                if (separatedOnly && item.quantity != 1) continue;
                total += AutoNhiemVuChinh.getStoneValueForStandalone(item) * (separatedOnly ? 1 : item.quantity);
            }
        }
        return total;
    }

    private Item[] chooseLowerTierStones(Char me, int targetTier, int need) {
        Item[] stones = new Item[24];
        int total = 0;
        int count = 0;
        for (int tier = targetTier - 1; tier >= 0 && total < need; --tier) {
            for (int i = 0; i < me.arrItemBag.length && count < stones.length && total < need; ++i) {
                Item item = me.arrItemBag[i];
                if (!this.isStoneTier(item, tier) || item.quantity != 1) continue;
                stones[count++] = item;
                total += AutoNhiemVuChinh.getStoneValueForStandalone(item);
            }
        }
        return total >= need ? stones : null;
    }

    private boolean hasStoneTier(Char me, int stoneTier) {
        if (me == null || me.arrItemBag == null) return false;
        for (int i = 0; i < me.arrItemBag.length; ++i) {
            Item item = me.arrItemBag[i];
            if (this.isStoneTier(item, stoneTier) && item.quantity > 0) return true;
        }
        return false;
    }

    private boolean hasItem(Item[] items) {
        if (items == null) return false;
        for (int i = 0; i < items.length; ++i) if (items[i] != null) return true;
        return false;
    }

    private void clearStoneCombineState() {
        if (GameScr.arrItemUpPeal != null) {
            for (int i = 0; i < GameScr.arrItemUpPeal.length; ++i) GameScr.arrItemUpPeal[i] = null;
        }
        this.combineStoneSixPending = false;
        this.combineStoneSixAt = 0L;
    }

    private boolean processStoneSplit(Char me, int need, int stoneTier, long now) {
        if (this.splitPending) return this.processPendingStoneSplit(me, now);
        int separatedPower = this.getSeparatedStoneTierPower(me, stoneTier);
        if (separatedPower >= need) return false;
        if (this.getStoneTierPower(me, stoneTier) < need) {
            this.pause("Auto dap do: thieu da " + stoneTier);
            return true;
        }
        if (AutoNhiemVuChinh.findFreeBagIndexForStandalone(me) < 0) {
            this.pause("Auto dap do: can o trong de tach da nang cap");
            return true;
        }
        for (int i = 0; i < me.arrItemBag.length; ++i) {
            Item item = me.arrItemBag[i];
            if (!this.isStoneTier(item, stoneTier) || item.quantity <= 1) continue;
            return this.openStoneSplit(item, now);
        }
        this.pause("Auto dap do: thieu da " + stoneTier);
        return true;
    }

    private boolean openStoneSplit(Item item, long now) {
        GameScr.indexSelect = item.indexUI;
        GameScr.gI().b(110244, null);
        if (GameCanvas.inputDlg == null || GameCanvas.currentDialog != GameCanvas.inputDlg) {
            this.pause("Auto dap do: khong mo duoc tach da");
            return true;
        }
        GameCanvas.inputDlg.left = new Command1("Dung Auto", MENU_STOP);
        this.splitPending = true;
        this.splitIndex = item.indexUI;
        this.splitQuantity = item.quantity;
        this.actionAt = now;
        this.log("split-open bag=" + this.splitIndex);
        return true;
    }

    private boolean processPendingStoneSplit(Char me, long now) {
        Item stack = this.splitIndex >= 0 && this.splitIndex < me.arrItemBag.length ? me.arrItemBag[this.splitIndex] : null;
        if (stack == null || stack.quantity != this.splitQuantity) {
            this.splitPending = false;
            this.splitIndex = -1;
            return false;
        }
        if (GameCanvas.inputDlg == null || GameCanvas.currentDialog != GameCanvas.inputDlg) {
            if (now - this.actionAt > 3000L) this.pause("Auto dap do: timeout tach da nang cap");
            return true;
        }
        if (now - this.actionAt < 500L) return true;
        GameCanvas.inputDlg.tfInput.a("1");
        GameCanvas.instance.perform(88835, String.valueOf(this.splitIndex));
        this.actionAt = now;
        this.log("split-confirm bag=" + this.splitIndex);
        return true;
    }

    private Item[] chooseOnlyStoneTier(Char me, int need, int stoneTier) {
        Item[] stones = new Item[18];
        int total = 0;
        int count = 0;
        if (me == null || me.arrItemBag == null) return null;
        for (int i = 0; i < me.arrItemBag.length && count < stones.length && total < need; ++i) {
            Item item = me.arrItemBag[i];
            if (!this.isStoneTier(item, stoneTier) || item.quantity != 1) continue;
            stones[count++] = item;
            total += AutoNhiemVuChinh.getStoneValueForStandalone(item);
        }
        return total >= need ? stones : null;
    }

    private int getStoneTierPower(Char me, int stoneTier) {
        int total = 0;
        if (me == null || me.arrItemBag == null) return total;
        for (int i = 0; i < me.arrItemBag.length; ++i) {
            Item item = me.arrItemBag[i];
            if (this.isStoneTier(item, stoneTier) && item.quantity > 0) {
                total += AutoNhiemVuChinh.getStoneValueForStandalone(item) * item.quantity;
            }
        }
        return total;
    }

    private int getSeparatedStoneTierPower(Char me, int stoneTier) {
        int total = 0;
        if (me == null || me.arrItemBag == null) return total;
        for (int i = 0; i < me.arrItemBag.length; ++i) {
            Item item = me.arrItemBag[i];
            if (this.isStoneTier(item, stoneTier) && item.quantity == 1) {
                total += AutoNhiemVuChinh.getStoneValueForStandalone(item);
            }
        }
        return total;
    }

    private boolean isStoneTier(Item item, int stoneTier) {
        return isStoneTierItem(item, stoneTier);
    }

    private static boolean isStoneTierItem(Item item, int stoneTier) {
        return item != null && isStoneTierTemplate(item.template, stoneTier);
    }

    private static boolean isStoneTierTemplate(ItemTemplate template, int stoneTier) {
        if (template == null || template.type != 26 || template.name == null) return false;
        return ("\u0110\u00e1 c\u1ea5p " + stoneTier).equalsIgnoreCase(template.name)
                || ("Da cap " + stoneTier).equalsIgnoreCase(template.name);
    }

    private ItemTemplate findStoneTierTemplate(int stoneTier) {
        if (ItemTemplates.itemTemplates == null) return null;
        java.util.Enumeration templates = ItemTemplates.itemTemplates.elements();
        while (templates.hasMoreElements()) {
            Object value = templates.nextElement();
            if (value instanceof ItemTemplate && isStoneTierTemplate((ItemTemplate)value, stoneTier)) return (ItemTemplate)value;
        }
        return null;
    }

    private int getStoneTierRequiredPower(int stoneTier) {
        ItemTemplate template = this.findStoneTierTemplate(stoneTier);
        int id = template == null ? -1 : template.id;
        return GameScr.cn != null && id >= 0 && id < GameScr.cn.length ? GameScr.cn[id] : -1;
    }

    private int getStoneTierCombineCost(int stoneTier) {
        ItemTemplate template = this.findStoneTierTemplate(stoneTier);
        int id = template == null ? -1 : template.id;
        return GameScr.cr != null && id >= 0 && id < GameScr.cr.length ? GameScr.cr[id] : -1;
    }

    private void sendUpgrade(long now) {
        Char me = Char.getMyChar();
        this.target = this.findTarget();
        if (me == null || this.target == null || this.selectedStones == null) {
            this.pause("Auto dap do: target hoac da da thay doi");
            return;
        }
        if (now - this.actionAt < ACTION_DELAY) return;
        boolean careful = this.target.upgrade >= 6;
        if (careful && this.selectedInsurance == null) {
            this.pause("Auto dap do: thieu Bao hiem");
            return;
        }
        Item[] materials = this.withInsurance(this.selectedStones, this.selectedInsurance);
        this.oldUpgrade = this.target.upgrade;
        GameScr.itemUpGrade = this.target;
        GameScr.arrItemUpGrade = materials;
        this.removePendingItems(me, this.target, this.selectedStones);
        this.log("send old=" + this.oldUpgrade + " careful=" + careful);
        Service.gI().upgradeItem(this.target, materials, careful);
        this.actionAt = now;
        this.transition(State.WAIT_RESULT);
    }

    private Item[] withInsurance(Item[] stones, Item insurance) {
        if (insurance == null) return stones;
        Item[] materials = new Item[stones.length + 1];
        for (int i = 0; i < stones.length; ++i) materials[i] = stones[i];
        materials[stones.length] = insurance;
        return materials;
    }

    private void removePendingItems(Char me, Item upgradeTarget, Item[] stones) {
        this.removeIfPresent(me, upgradeTarget);
        for (int i = 0; i < stones.length; ++i) this.removeIfPresent(me, stones[i]);
    }

    private void removeIfPresent(Char me, Item item) {
        if (item != null && item.indexUI >= 0 && item.indexUI < me.arrItemBag.length && me.arrItemBag[item.indexUI] == item) me.arrItemBag[item.indexUI] = null;
    }

    private void waitResult(long now) {
        if (now - this.actionAt > RESULT_TIMEOUT) {
            this.restorePendingLocally();
            this.pause("Auto dap do: timeout cho server nang cap");
            return;
        }
        if (!AutoNhiemVuChinh.areUpgradeMaterialsClearedForStandalone()) return;
        this.log("wait-result-ready");
        this.transition(State.VERIFY_RESULT);
    }

    private void verifyResult() {
        Item result = GameScr.itemUpGrade == this.target ? GameScr.itemUpGrade : this.target;
        if (result == null) {
            this.pause("Auto dap do: khong doc duoc ket qua server");
            return;
        }
        this.target = result;
        this.putBackInBag(result);
        this.clearOwnedState();
        this.selectedStones = null;
        this.selectedInsurance = null;
        this.log("result old=" + this.oldUpgrade + " new=" + result.upgrade);
        if (this.cancelRequested) {
            this.cleanupAndReequip();
            return;
        }
        if (result.upgrade >= TARGET_UPGRADE) this.finishOrReequip();
        else this.transition(State.CHECK_TARGET);
    }

    private void finishOrReequip() {
        if (this.wasEquipped) this.transition(State.REEQUIP_TARGET);
        else this.complete();
    }

    private void reequippedTarget(long now) {
        if (now - this.actionAt < ACTION_DELAY) return;
        Item item = this.findTarget();
        if (item == null) {
            this.pause("Auto dap do: khong tim thay target de mac lai");
            return;
        }
        if (AutoNhiemVuChinh.findBodySlotForStandalone(Char.getMyChar(), item) >= 0) {
            this.complete();
            return;
        }
        if (this.reequippedRequested) {
            if (now - this.actionAt > 5000L) this.pause("Auto dap do: timeout mac lai target");
            return;
        }
        this.log("reequip bag=" + item.indexUI);
        Service.gI().useItem(item.indexUI);
        this.actionAt = now;
        this.reequippedRequested = true;
    }

    private Item findTarget() {
        Char me = Char.getMyChar();
        if (me == null || this.descriptor == null) return null;
        if (GameScr.itemUpGrade == this.target && this.target != null) return this.target;
        Item found = this.findIn(me.arrItemBag);
        if (found == null) found = this.findIn(me.arrItemBody);
        return found;
    }

    private Item findIn(Item[] items) {
        if (items == null) return null;
        Item found = null;
        for (int i = 0; i < items.length; ++i) {
            Item item = items[i];
            if (!this.descriptor.matches(item)) continue;
            if (found != null) return null;
            found = item;
        }
        return found;
    }

    private boolean otherUpgradeOwnsState() {
        return GameScr.itemUpGrade != null && GameScr.itemUpGrade != this.target;
    }

    private void putBackInBag(Item item) {
        Char me = Char.getMyChar();
        if (me != null && !this.isInBag(me, item)) AutoNhiemVuChinh.putBackInBagForStandalone(me, item);
    }

    private boolean isInBag(Char me, Item item) {
        if (me == null || me.arrItemBag == null || item == null) return false;
        for (int i = 0; i < me.arrItemBag.length; ++i) {
            if (me.arrItemBag[i] == item) return true;
        }
        return false;
    }

    private void restorePendingLocally() {
        Char me = Char.getMyChar();
        if (me != null) {
            if (this.selectedStones != null) {
                for (int i = 0; i < this.selectedStones.length; ++i) this.putBackInBag(this.selectedStones[i]);
            }
            this.putBackInBag(this.target);
        }
        this.clearOwnedState();
    }

    private void clearOwnedState() {
        if (GameScr.itemUpGrade == this.target) {
            GameScr.itemUpGrade = null;
            GameScr.arrItemUpGrade = null;
        }
    }

    private void cleanupAndReequip() {
        this.restorePendingLocally();
        this.selectedStones = null;
        this.selectedInsurance = null;
        this.cancelRequested = false;
        if (this.wasEquipped) this.transition(State.REEQUIP_TARGET);
        else this.transition(State.IDLE);
    }

    private void pause(String reason) {
        this.restorePendingLocally();
        this.transition(State.PAUSED);
        if (!this.pauseNotified) {
            GameScr.addChatPopup(reason);
            this.pauseNotified = true;
        }
    }

    private void complete() {
        this.clearOwnedState();
        this.transition(State.COMPLETE);
        GameScr.addChatPopup("Auto dap do: hoan thanh +" + TARGET_UPGRADE);
    }

    private void transition(State next) {
        if (this.state != next) this.log("state=" + next);
        this.state = next;
    }

    private void log(String text) {
        System.out.println("AutoUpgrade " + text);
    }

    private void resetLocal() {
        this.descriptor = null;
        this.target = null;
        this.selectedStones = null;
        this.selectedInsurance = null;
        this.wasEquipped = false;
        this.cancelRequested = false;
        this.pauseNotified = false;
        this.oldUpgrade = -1;
        this.actionAt = 0L;
        this.splitPending = false;
        this.splitIndex = -1;
        this.splitQuantity = 0;
        this.unequipRequested = false;
        this.reequippedRequested = false;
    }

    public static final class Selection {
        final Item item;
        final int location;
        final int slot;

        Selection(Item item, int location, int slot) {
            this.item = item;
            this.location = location;
            this.slot = slot;
        }
    }

    public static final class TargetDescriptor {
        private final Item item;
        private final int originalSlot;

        private TargetDescriptor(Item item, int originalSlot) {
            this.item = item;
            this.originalSlot = originalSlot;
        }

        public static TargetDescriptor forTest(Item item, boolean equipped, int slot) {
            return new TargetDescriptor(item, slot);
        }

        public boolean matches(Item candidate) {
            return candidate == this.item || candidate != null && candidate.template == this.item.template && candidate.indexUI == this.originalSlot;
        }
    }
}

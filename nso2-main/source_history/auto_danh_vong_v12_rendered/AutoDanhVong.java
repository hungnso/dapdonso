public final class AutoDanhVong extends Auto {
   private static final long ACTION_DELAY = 1500L;
   private static final long EXISTING_QUEST_PROBE_DELAY = 3500L;
   private static final String RMS_CURRENT_QUEST = "AutoDVCurrentQuest";
   private static final long NOTICE_DELAY = 5000L;
   private static final long DEATH_DELAY = 1800L;
   private static final long ELITE_ZONE_DELAY = 12000L;
   private static final int[] NORMAL_FARM_MAPS = new int[]{
         23, 25, 26, 28, 29, 30, 31, 33, 34, 35, 52, 64, 15, 16,
         41, 42, 62, 44, 18, 24, 53, 19, 36, 54, 37, 55, 58
   };
   private static final int[] NORMAL_FARM_LEVELS = new int[]{
         3, 6, 9, 12, 15, 18, 21, 24, 27, 30, 36, 40, 44, 48,
         52, 54, 56, 59, 60, 63, 66, 71, 75, 80, 86, 92, 98
   };
   private static final int[] VDMQ_MAPS = new int[]{139, 140, 141, 142, 143, 144, 145, 146, 147, 148};
   private static final int[] VDMQ_LEVELS = new int[]{66, 75, 85, 92, 106, 116, 125, 134, 145, 156};

   private DanhVongQuest quest;
   private long lastAction;
   private long lastNotice;
   private long lastDeathAction;
   private long lastEliteSeen;
   private long popupAt;
   private int schoolMap;
   private int localKills;
   private int localYen;
   private int localUpgradeCount;
   private boolean reporting;
   private long reportAt;
   private boolean dailyLimitReached;
   private boolean stoppedUnsupported;
   private int completedTasks;
   private boolean existingQuestDetected;
   private boolean existingQuestProbeTried;
   private boolean existingQuestUnknown;
   private long existingQuestProbeAt;
   private int pendingNpcAction;

   private int shopCursor;
   private int requestedShopType;
   private long shopRequestAt;
   private String buyingItemName;
   private long buyRequestedAt;

   private int upgradeState;
   private int upgradeTemplateId;
   private int upgradeFromLevel;
   private long upgradeActionAt;
   private boolean upgradeCandidateBuyRequested;

   public final void g() {
      super.g();
      Char me = Char.getMyChar();
      this.quest = null;
      this.lastAction = 0L;
      this.lastNotice = 0L;
      this.lastDeathAction = 0L;
      this.lastEliteSeen = System.currentTimeMillis();
      this.popupAt = 0L;
      this.schoolMap = getSchoolMap(me);
      this.localKills = 0;
      this.localYen = 0;
      this.localUpgradeCount = 0;
      this.reporting = false;
      this.reportAt = 0L;
      this.dailyLimitReached = false;
      this.stoppedUnsupported = false;
      this.completedTasks = 0;
      this.existingQuestDetected = false;
      this.existingQuestProbeTried = false;
      this.existingQuestUnknown = false;
      this.existingQuestProbeAt = 0L;
      this.pendingNpcAction = 0;
      this.resetShopSearch();
      this.resetUpgrade();
      if (!this.restoreQuestFromTaskOrders(me)) {
         this.restoreCachedQuest();
      }
      GameScr.addChatPopup(this.quest == null ? "Auto Danh Vong: dang tim Ameji"
            : "Auto DV: tiep tuc " + this.quest.typeName() + " " + this.quest.current + "/" + this.quest.max);
      System.out.println("AutoDV start level=" + (me == null ? -1 : me.clevel) + " school=" + this.schoolMap
            + " restored=" + (this.quest != null));
   }

   public final void update() {
      Char me = Char.getMyChar();
      if (me == null) {
         return;
      }
      long now = System.currentTimeMillis();

      if (Auto.i()) {
         if (now - this.lastDeathAction >= DEATH_DELAY) {
            this.lastDeathAction = now;
            me.mobFocus = null;
            me.charFocus = null;
            me.currentMovePoint = null;
            Auto.a(true);
         }
         return;
      }

      if (this.popupAt > 0L && GameScr.isPaintAlert && now - this.popupAt >= 650L) {
         GameScr.gI().ah();
         this.popupAt = 0L;
         this.lastAction = now;
         return;
      }

      if (this.dailyLimitReached) {
         if (AutoDanhVongPanel.useDanhVongPhu && this.useDanhVongPhu(me, now)) {
            return;
         }
         this.notice("Auto Danh Vong: da het luot trong ngay");
         return;
      }

      if (this.stoppedUnsupported) {
         return;
      }

      if (this.reporting) {
         this.handleReport(me, now);
         return;
      }

      if (this.quest == null) {
         if (this.restoreQuestFromTaskOrders(me) || this.restoreCachedQuest()) {
            return;
         }
         this.acquireQuest(me, now);
         return;
      }

      if (!this.isQuestEnabled(this.quest.type)) {
         this.stoppedUnsupported = true;
         this.notice("Auto DV tam dung: da tat loai NV " + this.quest.typeName());
         return;
      }

      if (!this.ensureRequiredEquipment(me, now)) {
         return;
      }

      if (this.isLocallyComplete()) {
         this.reporting = true;
         this.reportAt = 0L;
         me.mobFocus = null;
         GameScr.addChatPopup("Auto DV: xong " + this.quest.typeName() + ", ve Ameji");
         return;
      }

      switch (this.quest.type) {
      case DanhVongQuest.NORMAL_MOB:
         this.farmMobs(me, false, now);
         return;
      case DanhVongQuest.YEN_FROM_MOB:
         this.farmMobs(me, false, now);
         return;
      case DanhVongQuest.ELITE_MOB:
         this.farmMobs(me, true, now);
         return;
      case DanhVongQuest.UPGRADE_ITEM:
         this.doUpgradeQuest(me, now);
         return;
      default:
         this.stoppedUnsupported = true;
         this.notice("Auto DV chua ho tro: " + this.quest.title);
      }
   }

   private void acquireQuest(Char me, long now) {
      if (this.existingQuestUnknown) {
         this.notice("Auto DV: da co NV cu nhung client chua co chi tiet. Mo popup NV 1 lan hoac Huy > Nhan lai.");
         return;
      }
      if (TileMap.mapID != this.schoolMap) {
         this.a(this.schoolMap, -2, -1, -1);
         return;
      }
      Npc ameji = findAmeji();
      if (ameji == null) {
         this.notice("Auto DV: khong thay NPC Ameji tai truong " + this.schoolMap);
         return;
      }

      if (GameCanvas.menu.showMenu) {
         // If Ameji told us a task was already assigned, never spam "Nhan".
         // Probe "Tra" once: some servers resend the current task popup here;
         // if the task is already complete this also submits it safely.
         if (this.existingQuestDetected) {
            if (!this.existingQuestProbeTried) {
               if (GameCanvas.menu.selectCaptionForStandaloneAuto(
                     new String[]{"trả", "tra", "hoàn thành", "hoan thanh"}, "AutoDV probe-existing")) {
                  this.existingQuestProbeTried = true;
                  this.existingQuestProbeAt = now;
                  this.pendingNpcAction = 2;
                  this.lastAction = now;
               }
               return;
            }
            // Leave an unknown menu alone. Closing/reopening it every tick is
            // exactly what caused the visible jitter in v1/v1.1.
            if (now - this.existingQuestProbeAt >= EXISTING_QUEST_PROBE_DELAY) {
               GameCanvas.menu.showMenu = false;
               this.existingQuestDetected = false;
               this.existingQuestProbeTried = false;
               this.existingQuestProbeAt = 0L;
               this.pendingNpcAction = 0;
            }
            return;
         }

         if (GameCanvas.menu.selectCaptionForStandaloneAuto(
               new String[]{"nhận nhiệm vụ", "nhan nhiem vu", "nhận nv", "nhan nv", "nhận", "nhan"}, "AutoDV receive")) {
            this.pendingNpcAction = 1;
            this.lastAction = now;
            return;
         }
         if (GameCanvas.menu.selectCaptionForStandaloneAuto(
               new String[]{"nhiệm vụ danh vọng", "nhiem vu danh vong", "danh vọng", "danh vong"}, "AutoDV root")) {
            this.lastAction = now;
            return;
         }
         if (GameCanvas.menu.selectCaptionForStandaloneAuto(
               new String[]{"xem nhiệm vụ", "xem nhiem vu", "nhiệm vụ", "nhiem vu"}, "AutoDV inspect")) {
            this.lastAction = now;
            return;
         }
         this.lastAction = now;
         return;
      }

      if (this.existingQuestDetected && this.existingQuestProbeTried) {
         if (now - this.existingQuestProbeAt < EXISTING_QUEST_PROBE_DELAY) {
            return;
         }
         // No rejection and no popup arrived after "Tra": most likely the old
         // task was complete and got submitted. Try receiving a new task once.
         this.existingQuestDetected = false;
         this.existingQuestProbeTried = false;
         this.existingQuestProbeAt = 0L;
         this.pendingNpcAction = 0;
         this.lastAction = 0L;
      }

      if (now - this.lastAction < ACTION_DELAY) {
         return;
      }
      me.mobFocus = null;
      me.charFocus = null;
      me.currentMovePoint = null;
      me.npcFocus = ameji;
      Service.gI().openMenu(ameji.template.npcTemplateId);
      this.lastAction = now;
      System.out.println("AutoDV open Ameji id=" + ameji.template.npcTemplateId + " map=" + TileMap.mapID
            + " player=" + me.cx + "," + me.cy + " npc=" + ameji.cx + "," + ameji.cy
            + " existing=" + this.existingQuestDetected + " probe=" + this.existingQuestProbeTried);
   }

   private void handleReport(Char me, long now) {
      if (TileMap.mapID != this.schoolMap) {
         this.a(this.schoolMap, -2, -1, -1);
         return;
      }
      Npc ameji = findAmeji();
      if (ameji == null) {
         this.notice("Auto DV: khong thay Ameji de tra NV");
         return;
      }
      if (GameCanvas.menu.showMenu) {
         if (GameCanvas.menu.selectCaptionForStandaloneAuto(
               new String[]{"hoàn thành", "hoan thanh", "trả nhiệm vụ", "tra nhiem vu", "báo cáo", "bao cao", "trả", "tra"}, "AutoDV complete")) {
            this.pendingNpcAction = 3;
            this.reportAt = now;
            this.lastAction = now;
            return;
         }
         if (GameCanvas.menu.selectCaptionForStandaloneAuto(
               new String[]{"nhiệm vụ danh vọng", "nhiem vu danh vong", "danh vọng", "danh vong"}, "AutoDV complete-root")) {
            this.lastAction = now;
            return;
         }
         this.lastAction = now;
         return;
      }

      if (this.reportAt > 0L && now - this.reportAt >= 3500L) {
         ++this.completedTasks;
         System.out.println("AutoDV report-finished count=" + this.completedTasks);
         this.quest = null;
         this.localKills = 0;
         this.localYen = 0;
         this.localUpgradeCount = 0;
         this.reporting = false;
         this.reportAt = 0L;
         this.resetShopSearch();
         this.resetUpgrade();
         clearCachedQuest();
         return;
      }

      if (now - this.lastAction < ACTION_DELAY) {
         return;
      }
      me.mobFocus = null;
      me.charFocus = null;
      me.currentMovePoint = null;
      me.npcFocus = ameji;
      Service.gI().openMenu(ameji.template.npcTemplateId);
      this.lastAction = now;
   }

   private boolean ensureRequiredEquipment(Char me, long now) {
      String name = this.quest.requiredItemName;
      if (name == null || name.length() == 0) {
         return true;
      }
      Item body = findItemByName(me.arrItemBody, name);
      if (body != null) {
         this.buyingItemName = null;
         return true;
      }
      Item bag = findItemByName(me.arrItemBag, name);
      if (bag != null) {
         if (now - this.lastAction >= ACTION_DELAY) {
            System.out.println("AutoDV equip required=" + name + " bag=" + bag.indexUI);
            Service.gI().useItem(bag.indexUI);
            this.lastAction = now;
         }
         return false;
      }
      if (!AutoDanhVongPanel.autoBuyEquipment) {
         this.notice("Auto DV can trang bi: " + name);
         return false;
      }
      if (TileMap.mapID != this.schoolMap) {
         this.a(this.schoolMap, -2, -1, -1);
         return false;
      }
      if (Char.af() <= 0) {
         this.notice("Auto DV: hanh trang day, khong mua duoc " + name);
         return false;
      }
      return this.searchAndBuyRequiredItem(me, name, now);
   }

   private boolean searchAndBuyRequiredItem(Char me, String name, long now) {
      if (this.buyingItemName == null || !this.buyingItemName.equals(name)) {
         this.resetShopSearch();
         this.buyingItemName = name;
      }
      Item bag = findItemByName(me.arrItemBag, name);
      if (bag != null) {
         return false;
      }
      if (this.buyRequestedAt > 0L) {
         if (now - this.buyRequestedAt < 2500L) {
            return false;
         }
         this.buyRequestedAt = 0L;
      }

      int[] shopTypes = getEquipmentShopTypes(me);
      if (this.shopCursor >= shopTypes.length) {
         this.notice("Auto DV: khong tim thay trong shop: " + name);
         return false;
      }
      int type = shopTypes[this.shopCursor];
      Item[] shop = getShopItems(type);
      if (shop == null) {
         if (this.requestedShopType != type || now - this.shopRequestAt >= 2500L) {
            Service.gI().requestItem(type);
            this.requestedShopType = type;
            this.shopRequestAt = now;
            System.out.println("AutoDV request shop=" + type + " item=" + name);
         }
         return false;
      }

      Item found = findItemByName(shop, name);
      if (found != null) {
         System.out.println("AutoDV buy required=" + name + " shop=" + type + " index=" + found.indexUI);
         Service.gI().buyItem(found.typeUI, found.indexUI, 1);
         this.buyRequestedAt = now;
         this.lastAction = now;
         return false;
      }
      ++this.shopCursor;
      this.requestedShopType = -1;
      return false;
   }

   private void farmMobs(Char me, boolean elite, long now) {
      int averageLevel = getAverageNormalMobLevel();
      if (averageLevel <= 0 || Math.abs(averageLevel - me.clevel) > 10) {
         int map = selectFarmMap(me.clevel);
         if (TileMap.mapID != map) {
            this.notice("Auto DV: di map " + map + " danh quai +/-10 cap");
            this.a(map, -2, -1, -1);
            return;
         }
      }

      if (elite) {
         Mob eliteMob = findNearestElite(me);
         if (eliteMob != null) {
            this.lastEliteSeen = now;
         } else if (now - this.lastEliteSeen >= ELITE_ZONE_DELAY) {
            this.lastEliteSeen = now;
            this.notice("Auto DV: doi khu tim Tinh Anh");
            this.b(-1);
            return;
         }
         this.c(-1, 2);
      } else {
         this.c(-1, 1);
      }
      this.c(-1);
   }

   private void doUpgradeQuest(Char me, long now) {
      int target = this.quest.upgradeTarget;
      if (target < 1) {
         target = 1;
      }

      if (this.upgradeState == 1) {
         if (now - this.upgradeActionAt < 1200L) {
            return;
         }
         if (!AutoNhiemVuChinh.areUpgradeMaterialsClearedForStandalone()) {
            if (now - this.upgradeActionAt < 20000L) {
               return;
            }
            this.notice("Auto DV: dap da timeout, se thu lai");
         }
         Item result = GameScr.itemUpGrade;
         if (result != null) {
            int newLevel = result.upgrade;
            int bagIndex = AutoNhiemVuChinh.putBackInBagForStandalone(me, result);
            GameScr.itemUpGrade = null;
            GameScr.arrItemUpGrade = null;
            System.out.println("AutoDV upgrade result item=" + result.template.id + " +" + this.upgradeFromLevel + " -> +" + newLevel);
            if (newLevel >= target) {
               ++this.localUpgradeCount;
               this.resetUpgrade();
               this.lastAction = now;
               return;
            }
            if (bagIndex >= 0) {
               this.upgradeTemplateId = result.template.id;
            }
         }
         this.upgradeState = 0;
         this.upgradeActionAt = now;
         return;
      }

      Item candidate = findUpgradeCandidate(me, target, this.quest.requiredItemName, this.upgradeTemplateId);
      if (candidate == null) {
         if (!this.buyUpgradeCandidate(me, target, now)) {
            this.notice("Auto DV: dang chuan bi do + thap de nang cap");
         }
         return;
      }
      this.upgradeTemplateId = candidate.template.id;
      if (candidate.upgrade >= target) {
         ++this.localUpgradeCount;
         this.resetUpgrade();
         return;
      }

      int need = AutoNhiemVuChinh.getUpgradeRequirementForStandalone(candidate);
      int cost = AutoNhiemVuChinh.getUpgradeCostForStandalone(candidate);
      Item[] stones = AutoNhiemVuChinh.chooseUpgradeStonesForStandalone(me, need);
      if (need <= 0 || cost < 0 || stones == null || me.xu + me.yen < cost) {
         this.notice("Auto DV: thieu da/yen dap +, dang farm them");
         this.farmMobs(me, false, now);
         return;
      }
      if (now - this.upgradeActionAt < ACTION_DELAY) {
         return;
      }

      this.upgradeFromLevel = candidate.upgrade;
      GameScr.itemUpGrade = candidate;
      GameScr.arrItemUpGrade = stones;
      if (candidate.indexUI >= 0 && candidate.indexUI < me.arrItemBag.length && me.arrItemBag[candidate.indexUI] == candidate) {
         me.arrItemBag[candidate.indexUI] = null;
      }
      for (int i = 0; i < stones.length; ++i) {
         Item stone = stones[i];
         if (stone != null && stone.indexUI >= 0 && stone.indexUI < me.arrItemBag.length && me.arrItemBag[stone.indexUI] == stone) {
            me.arrItemBag[stone.indexUI] = null;
         }
      }
      System.out.println("AutoDV upgrade send item=" + candidate.template.id + " from=+" + candidate.upgrade
            + " target=+" + target + " needStone=" + need + " cost=" + cost);
      Service.gI().upgradeItem(candidate, stones, false);
      this.upgradeState = 1;
      this.upgradeActionAt = now;
   }

   private boolean buyUpgradeCandidate(Char me, int target, long now) {
      if (TileMap.mapID != this.schoolMap) {
         this.a(this.schoolMap, -2, -1, -1);
         return true;
      }
      if (Char.af() <= 0) {
         this.notice("Auto DV: hanh trang day, khong mua duoc do dap +");
         return true;
      }
      int type = me.cgender == 0 ? 20 : 21;
      Item[] shop = getShopItems(type);
      if (shop == null) {
         if (now - this.shopRequestAt >= 2000L || this.requestedShopType != type) {
            Service.gI().requestItem(type);
            this.requestedShopType = type;
            this.shopRequestAt = now;
         }
         return true;
      }
      if (this.upgradeCandidateBuyRequested && now - this.buyRequestedAt < 3000L) {
         return true;
      }
      Item best = null;
      for (int i = 0; i < shop.length; ++i) {
         Item item = shop[i];
         if (item == null || item.template == null || item.template.level < 10 || item.template.level > me.clevel) {
            continue;
         }
         if (item.getUpMax() < target || sameItemName(item.template.name, this.quest.requiredItemName)) {
            continue;
         }
         if (best == null || item.template.level < best.template.level) {
            best = item;
         }
      }
      if (best == null) {
         return false;
      }
      Service.gI().buyItem(best.typeUI, best.indexUI, 1);
      this.upgradeCandidateBuyRequested = true;
      this.buyRequestedAt = now;
      System.out.println("AutoDV buy upgrade-candidate=" + best.template.name + " level=" + best.template.level);
      return true;
   }

   private boolean isLocallyComplete() {
      if (this.quest == null) {
         return false;
      }
      int remaining = this.quest.max - this.quest.current;
      if (remaining <= 0) {
         return true;
      }
      switch (this.quest.type) {
      case DanhVongQuest.NORMAL_MOB:
      case DanhVongQuest.ELITE_MOB:
         return this.localKills >= remaining;
      case DanhVongQuest.YEN_FROM_MOB:
         return this.localYen >= remaining;
      case DanhVongQuest.UPGRADE_ITEM:
         return this.localUpgradeCount >= remaining;
      default:
         return false;
      }
   }

   private boolean isQuestEnabled(int type) {
      if (type == DanhVongQuest.NORMAL_MOB) return AutoDanhVongPanel.doNormalMob;
      if (type == DanhVongQuest.YEN_FROM_MOB) return AutoDanhVongPanel.doYen;
      if (type == DanhVongQuest.ELITE_MOB) return AutoDanhVongPanel.doElite;
      if (type == DanhVongQuest.UPGRADE_ITEM) return AutoDanhVongPanel.doUpgrade;
      return false;
   }

   private boolean useDanhVongPhu(Char me, long now) {
      Item phu = findItemContaining(me.arrItemBag, new String[]{"danh vọng", "phù"});
      if (phu == null) {
         phu = findItemContaining(me.arrItemBag, new String[]{"danh vong", "phu"});
      }
      if (phu == null) {
         this.notice("Auto DV: het luot va khong co Danh vong phu");
         return false;
      }
      if (now - this.lastAction < ACTION_DELAY) {
         return true;
      }
      Service.gI().useItem(phu.indexUI);
      this.lastAction = now;
      this.dailyLimitReached = false;
      this.quest = null;
      this.notice("Auto DV: da dung Danh vong phu");
      return true;
   }

   private void acceptPopup(String title, String content) {
      this.popupAt = System.currentTimeMillis();
      String lower = content == null ? "" : content.toLowerCase();
      if ((lower.indexOf("hết") >= 0 || lower.indexOf("het") >= 0 || lower.indexOf("đã làm hết") >= 0)
            && (lower.indexOf("nhiệm vụ") >= 0 || lower.indexOf("nhiem vu") >= 0)) {
         this.dailyLimitReached = true;
         return;
      }
      DanhVongQuest parsed = DanhVongQuest.parse(title, content);
      if (parsed == null) {
         if (lower.indexOf("lôi đài") >= 0 || lower.indexOf("loi dai") >= 0
               || lower.indexOf("chiến trường") >= 0 || lower.indexOf("chien truong") >= 0
               || lower.indexOf("cừu sát") >= 0 || lower.indexOf("cuu sat") >= 0
               || lower.indexOf("thủ lĩnh") >= 0 || lower.indexOf("thu linh") >= 0) {
            this.stoppedUnsupported = true;
            this.notice("Auto DV tam dung: NV hien tai chua ho tro");
         }
         return;
      }
      saveCachedQuest(title, content);
      this.existingQuestDetected = false;
      this.existingQuestProbeTried = false;
      this.existingQuestUnknown = false;
      this.existingQuestProbeAt = 0L;
      this.pendingNpcAction = 0;
      boolean changed = this.quest == null || !sameText(this.quest.title, parsed.title)
            || this.quest.type != parsed.type || !sameText(this.quest.requiredItemName, parsed.requiredItemName);
      this.quest = parsed;
      this.localKills = 0;
      this.localYen = 0;
      this.localUpgradeCount = 0;
      this.reporting = false;
      this.reportAt = 0L;
      this.stoppedUnsupported = false;
      this.resetShopSearch();
      this.resetUpgrade();
      System.out.println("AutoDV quest title=" + parsed.title + " type=" + parsed.typeName()
            + " progress=" + parsed.current + "/" + parsed.max + " equip=" + parsed.requiredItemName
            + " upgradeTarget=+" + parsed.upgradeTarget);
      if (changed) {
         GameScr.addChatPopup("Auto DV: " + parsed.typeName() + " " + parsed.current + "/" + parsed.max);
      }
   }

   public static void onTaskPopup(String title, String content) {
      if (!isReputationPopup(content)) {
         return;
      }
      saveCachedQuest(title, content);
      System.out.println("AutoDV cache reputation popup title=" + title);
      if (NSOT_MOB.b instanceof AutoDanhVong) {
         ((AutoDanhVong)NSOT_MOB.b).acceptPopup(title, content);
      }
   }

   public static void onNpcMessage(int npcTemplateId, String message) {
      if (!(NSOT_MOB.b instanceof AutoDanhVong) || message == null) {
         return;
      }
      AutoDanhVong auto = (AutoDanhVong)NSOT_MOB.b;
      Npc focus = Char.getMyChar() == null ? null : Char.getMyChar().npcFocus;
      if (focus == null || focus.template == null || focus.template.npcTemplateId != npcTemplateId
            || focus.template.name == null || focus.template.name.toLowerCase().indexOf("ameji") < 0) {
         return;
      }
      String lower = message.toLowerCase();
      boolean assignedBefore = (lower.indexOf("hoàn thành") >= 0 || lower.indexOf("hoan thanh") >= 0)
            && (lower.indexOf("đã giao trước") >= 0 || lower.indexOf("da giao truoc") >= 0);
      if (!assignedBefore) {
         return;
      }
      long now = System.currentTimeMillis();
      System.out.println("AutoDV Ameji says existing-task pending=" + auto.pendingNpcAction + " msg=" + message);
      if (auto.pendingNpcAction == 2) {
         // "Tra" also says incomplete: the server does not expose old-task
         // details through this menu. Stop safely instead of looping forever.
         auto.existingQuestUnknown = true;
         auto.existingQuestDetected = false;
         auto.existingQuestProbeTried = true;
         auto.existingQuestProbeAt = now;
         auto.pendingNpcAction = 0;
         GameCanvas.menu.showMenu = false;
         auto.notice("Auto DV: NV cu da nhan truoc khi bat auto. Mo popup NV 1 lan hoac Huy > Nhan lai.");
      } else {
         auto.existingQuestDetected = true;
         auto.existingQuestProbeTried = false;
         auto.existingQuestProbeAt = 0L;
         auto.pendingNpcAction = 0;
         auto.lastAction = 0L;
         auto.notice("Auto DV: phat hien NV Danh Vong dang lam, dang doc lai NV");
      }
   }

   private boolean restoreQuestFromTaskOrders(Char me) {
      if (me == null || me.taskOrders == null) {
         return false;
      }
      for (int i = 0; i < me.taskOrders.size(); ++i) {
         Object value = me.taskOrders.elementAt(i);
         if (!(value instanceof TaskOrder)) {
            continue;
         }
         TaskOrder order = (TaskOrder)value;
         String name = order.name == null ? "" : order.name;
         String description = order.description == null ? "" : order.description;
         String lower = (name + " " + description).toLowerCase();
         // Reputation tasks require a specific piece of equipment. This marker
         // avoids mistaking daily/Ta Thu TaskOrder entries for reputation.
         if (lower.indexOf("sử dụng") < 0 && lower.indexOf("su dung") < 0
               && lower.indexOf("danh vọng") < 0 && lower.indexOf("danh vong") < 0
               && lower.indexOf("nông dân chăm chỉ") < 0 && lower.indexOf("nong dan cham chi") < 0) {
            continue;
         }
         String content = description + "\n" + order.count + "/" + order.maxCount;
         DanhVongQuest parsed = DanhVongQuest.parse(name, content);
         if (parsed == null) {
            continue;
         }
         parsed.current = order.count;
         parsed.max = order.maxCount > 0 ? order.maxCount : parsed.max;
         this.quest = parsed;
         this.localKills = 0;
         this.localYen = 0;
         this.localUpgradeCount = 0;
         this.reporting = false;
         this.existingQuestDetected = false;
         this.existingQuestUnknown = false;
         saveCachedQuest(parsed.title, parsed.content);
         System.out.println("AutoDV restored from TaskOrder id=" + order.taskId + " name=" + order.name
               + " progress=" + order.count + "/" + order.maxCount);
         return true;
      }
      return false;
   }

   private boolean restoreCachedQuest() {
      String raw = mResources.c(currentQuestRmsKey());
      if (raw == null || raw.length() == 0) {
         return false;
      }
      int split = raw.indexOf('\u0001');
      if (split <= 0 || split >= raw.length() - 1) {
         return false;
      }
      DanhVongQuest parsed = DanhVongQuest.parse(raw.substring(0, split), raw.substring(split + 1));
      if (parsed == null) {
         return false;
      }
      this.quest = parsed;
      this.localKills = 0;
      this.localYen = 0;
      this.localUpgradeCount = 0;
      this.reporting = false;
      this.existingQuestDetected = false;
      this.existingQuestUnknown = false;
      System.out.println("AutoDV restored cached quest=" + parsed.title + " type=" + parsed.typeName()
            + " progress=" + parsed.current + "/" + parsed.max);
      return true;
   }

   private static boolean isReputationPopup(String content) {
      if (content == null) {
         return false;
      }
      String lower = content.toLowerCase();
      return lower.indexOf("danh vọng phù") >= 0 || lower.indexOf("danh vong phu") >= 0
            || (lower.indexOf("nhiệm vụ trong ngày") >= 0 || lower.indexOf("nhiem vu trong ngay") >= 0)
               && (lower.indexOf("có thể nhận thêm") >= 0 || lower.indexOf("co the nhan them") >= 0);
   }

   private static void saveCachedQuest(String title, String content) {
      if (!isReputationPopup(content)) {
         return;
      }
      String safeTitle = title == null ? "Nhiem vu" : title;
      mResources.a(currentQuestRmsKey(), safeTitle + '\u0001' + content);
   }

   private static void clearCachedQuest() {
      mResources.a(currentQuestRmsKey(), "");
   }

   private static String currentQuestRmsKey() {
      Char me = Char.getMyChar();
      return RMS_CURRENT_QUEST + (me == null ? "" : String.valueOf(me.charID));
   }

   public static void onYenGain(int amount) {
      if (amount <= 0 || !(NSOT_MOB.b instanceof AutoDanhVong)) {
         return;
      }
      AutoDanhVong auto = (AutoDanhVong)NSOT_MOB.b;
      if (auto.quest == null || auto.quest.type != DanhVongQuest.YEN_FROM_MOB || !auto.requiredEquipmentEquipped()) {
         return;
      }
      auto.localYen += amount;
      System.out.println("AutoDV yen +" + amount + " local=" + auto.localYen + " need=" + (auto.quest.max - auto.quest.current));
   }

   public static void onMobKilled(Mob mob, boolean wasMyFocus) {
      if (!wasMyFocus || mob == null || !(NSOT_MOB.b instanceof AutoDanhVong)) {
         return;
      }
      AutoDanhVong auto = (AutoDanhVong)NSOT_MOB.b;
      if (auto.quest == null || !auto.requiredEquipmentEquipped()) {
         return;
      }
      if (auto.quest.type == DanhVongQuest.NORMAL_MOB && mob.levelBoss == 0) {
         ++auto.localKills;
      } else if (auto.quest.type == DanhVongQuest.ELITE_MOB && mob.levelBoss == 1) {
         ++auto.localKills;
      } else {
         return;
      }
      System.out.println("AutoDV kill type=" + mob.levelBoss + " local=" + auto.localKills
            + " need=" + (auto.quest.max - auto.quest.current));
   }

   private boolean requiredEquipmentEquipped() {
      Char me = Char.getMyChar();
      return this.quest == null || this.quest.requiredItemName == null || me != null
            && findItemByName(me.arrItemBody, this.quest.requiredItemName) != null;
   }

   private static Npc findAmeji() {
      for (int i = 0; i < GameScr.vSet.size(); ++i) {
         Object value = GameScr.vSet.elementAt(i);
         if (!(value instanceof Npc)) {
            continue;
         }
         Npc npc = (Npc)value;
         if (npc.template != null && npc.template.name != null
               && npc.template.name.toLowerCase().indexOf("ameji") >= 0) {
            return npc;
         }
      }
      return null;
   }

   private static int getSchoolMap(Char me) {
      int classId = me == null || me.nClass == null ? 1 : me.nClass.classId;
      return classId <= 2 ? 1 : (classId <= 4 ? 27 : 72);
   }

   private static int selectFarmMap(int level) {
      int bestMap = NORMAL_FARM_MAPS[0];
      int bestDiff = 9999;
      for (int i = 0; i < NORMAL_FARM_MAPS.length; ++i) {
         int diff = Math.abs(level - NORMAL_FARM_LEVELS[i]);
         if (diff < bestDiff) {
            bestDiff = diff;
            bestMap = NORMAL_FARM_MAPS[i];
         }
      }
      if (level >= 101) {
         for (int i = 0; i < VDMQ_MAPS.length; ++i) {
            int diff = Math.abs(level - VDMQ_LEVELS[i]);
            if (diff < bestDiff) {
               bestDiff = diff;
               bestMap = VDMQ_MAPS[i];
            }
         }
      }
      return bestMap;
   }

   private static int getAverageNormalMobLevel() {
      if (GameScr.vMobAttack == null || GameScr.vMobAttack.size() == 0) {
         return -1;
      }
      int total = 0;
      int count = 0;
      for (int i = 0; i < GameScr.vMobAttack.size(); ++i) {
         Mob mob = (Mob)GameScr.vMobAttack.elementAt(i);
         if (mob == null || mob.isBoss || mob.levelBoss > 0 || mob.status == 0) {
            continue;
         }
         int level = mob.getLevelForStandaloneAuto();
         if (level > 0) {
            total += level;
            ++count;
         }
      }
      return count == 0 ? -1 : total / count;
   }

   private static Mob findNearestElite(Char me) {
      Mob best = null;
      int distance = 999999;
      for (int i = 0; i < GameScr.vMobAttack.size(); ++i) {
         Mob mob = (Mob)GameScr.vMobAttack.elementAt(i);
         if (mob == null || mob.status == 0 || mob.status == 1 || mob.hp <= 0 || mob.levelBoss != 1) {
            continue;
         }
         if (Math.abs(mob.getLevelForStandaloneAuto() - me.clevel) > 10) {
            continue;
         }
         int d = Math.abs(me.cx - mob.xFirst) + Math.abs(me.cy - mob.yFirst);
         if (d < distance) {
            distance = d;
            best = mob;
         }
      }
      return best;
   }

   private static Item findUpgradeCandidate(Char me, int target, String requiredName, int preferredTemplateId) {
      if (me.arrItemBag == null) {
         return null;
      }
      Item best = null;
      for (int pass = 0; pass < 2; ++pass) {
         for (int i = 0; i < me.arrItemBag.length; ++i) {
            Item item = me.arrItemBag[i];
            if (item == null || item.template == null || !item.isTypeBody()
                  || item.template.level < 10 || item.template.level > me.clevel
                  || item.upgrade >= target || item.getUpMax() < target
                  || sameItemName(item.template.name, requiredName)) {
               continue;
            }
            if (pass == 0 && preferredTemplateId >= 0 && item.template.id != preferredTemplateId) {
               continue;
            }
            if (best == null || item.upgrade > best.upgrade
                  || item.upgrade == best.upgrade && item.template.level < best.template.level) {
               best = item;
            }
         }
         if (best != null || preferredTemplateId < 0) {
            break;
         }
      }
      return best;
   }

   private static int[] getEquipmentShopTypes(Char me) {
      if (me.cgender == 0) {
         return new int[]{2, 16, 17, 18, 19, 20, 22, 24, 26, 28};
      }
      return new int[]{2, 16, 17, 18, 19, 21, 23, 25, 27, 29};
   }

   private static Item[] getShopItems(int type) {
      switch (type) {
      case 2: return GameScr.arrItemWeapon;
      case 16: return GameScr.arrItemLien;
      case 17: return GameScr.arrItemNhan;
      case 18: return GameScr.arrItemNgocBoi;
      case 19: return GameScr.arrItemPhu;
      case 20: return GameScr.arrItemNonNam;
      case 21: return GameScr.arrItemNonNu;
      case 22: return GameScr.arrItemAoNam;
      case 23: return GameScr.arrItemAoNu;
      case 24: return GameScr.arrItemGangTayNam;
      case 25: return GameScr.arrItemGangTayNu;
      case 26: return GameScr.arrItemQuanNam;
      case 27: return GameScr.arrItemQuanNu;
      case 28: return GameScr.arrItemGiayNam;
      case 29: return GameScr.arrItemGiayNu;
      default: return null;
      }
   }

   private static Item findItemByName(Item[] items, String name) {
      if (items == null || name == null) {
         return null;
      }
      for (int i = 0; i < items.length; ++i) {
         Item item = items[i];
         if (item != null && item.template != null && sameItemName(item.template.name, name)) {
            return item;
         }
      }
      return null;
   }

   private static Item findItemContaining(Item[] items, String[] words) {
      if (items == null || words == null) {
         return null;
      }
      for (int i = 0; i < items.length; ++i) {
         Item item = items[i];
         if (item == null || item.template == null || item.template.name == null) {
            continue;
         }
         String value = item.template.name.toLowerCase();
         boolean ok = true;
         for (int k = 0; k < words.length; ++k) {
            if (value.indexOf(words[k]) < 0) {
               ok = false;
               break;
            }
         }
         if (ok) {
            return item;
         }
      }
      return null;
   }

   private static boolean sameItemName(String a, String b) {
      if (a == null || b == null) {
         return false;
      }
      String aa = a.trim().toLowerCase();
      String bb = b.trim().toLowerCase();
      return aa.equals(bb) || aa.indexOf(bb) >= 0 || bb.indexOf(aa) >= 0;
   }

   private static boolean sameText(String a, String b) {
      return a == null ? b == null : b != null && a.equalsIgnoreCase(b);
   }

   private void resetShopSearch() {
      this.shopCursor = 0;
      this.requestedShopType = -1;
      this.shopRequestAt = 0L;
      this.buyingItemName = null;
      this.buyRequestedAt = 0L;
   }

   private void resetUpgrade() {
      this.upgradeState = 0;
      this.upgradeTemplateId = -1;
      this.upgradeFromLevel = -1;
      this.upgradeActionAt = 0L;
      this.upgradeCandidateBuyRequested = false;
   }

   private void notice(String message) {
      long now = System.currentTimeMillis();
      if (now - this.lastNotice < NOTICE_DELAY) {
         return;
      }
      this.lastNotice = now;
      System.out.println(message);
      GameScr.addChatPopup(message);
   }

   public final String toString() {
      if (this.quest == null) {
         if (this.existingQuestUnknown) {
            return "Auto Danh Vong - can doc NV cu";
         }
         if (this.existingQuestDetected) {
            return "Auto Danh Vong - dang doc NV cu";
         }
         return "Auto Danh Vong - dang nhan NV";
      }
      int local = this.quest.type == DanhVongQuest.YEN_FROM_MOB ? this.localYen
            : (this.quest.type == DanhVongQuest.UPGRADE_ITEM ? this.localUpgradeCount : this.localKills);
      return "Auto Danh Vong - " + this.quest.typeName() + " " + (this.quest.current + local) + "/" + this.quest.max;
   }
}

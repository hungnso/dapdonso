public final class AutoVungDatMaQuai extends Auto implements IActionListener {
   private static final int THIEN_NHAN_PHU = 538;
   private static final int THIEN_NHAN_BATCH = 5;
   private static final int GOOSHO_NPC = 26;
   private static final int KAMAKURA_NPC = 5;
   private static final int VDMQ_FIRST_MAP = 139;
   private static final int VDMQ_LAST_MAP = 148;
   private static final int MENU_AUTO_LEVEL = 27100;
   private static final int MENU_MAP_BASE = 27200;
   private static final int MENU_CONFIG_ZONES = 27099;
   private static final int MENU_SAVE_ZONES = 27098;
   private static final int MAX_PLAYERS_PER_ZONE = 10;
   private static final String RMS_VDMQ_ZONES = "AutoVdmqZones";

   private long lastAction;
   private long lastNotice;
   private int lastObservedMap;
   private long mapEnteredAt;
   private boolean charmUsed;
   private boolean mapSelected;
   private boolean waitingMapChoice;
   private int requestedMapId;
   private int[] configuredZones;
   private int zoneDataMap;
   private long zoneDataRequestedAt;

   // Chỉ dùng trong lúc Auto VDMQ đang mua Thiên Nhãn Phù.
   // Không có vòng quét/background nào chạy ngoài luồng này.
   private boolean waitingCharmShop;
   private long waitingCharmShopAt;
   private boolean charmBuyPending;
   private long charmBuyPendingAt;
   private long lastCharmShopScan;

   public final void g() {
      super.g();
      this.lastAction = 0L;
      this.lastNotice = 0L;
      this.lastObservedMap = -1;
      this.mapEnteredAt = 0L;
      this.charmUsed = false;
      this.mapSelected = false;
      this.requestedMapId = -1;
      this.waitingMapChoice = true;
      this.configuredZones = this.loadConfiguredZones();
      this.zoneDataMap = -1;
      this.zoneDataRequestedAt = 0L;
      this.resetCharmBuyState();
      this.showMapChoiceMenu();
   }

   private void showMapChoiceMenu() {
      MyVector menu = new MyVector();
      menu.addElement(new Command1("Cau hinh khu up", this, MENU_CONFIG_ZONES, null));
      menu.addElement(new Command1("Tự chọn theo level", this, MENU_AUTO_LEVEL, null));
      menu.addElement(new Command1("Quỷ Sơn 64-68", this, MENU_MAP_BASE + 139, null));
      menu.addElement(new Command1("Sơn Hải Vực 74-77", this, MENU_MAP_BASE + 140, null));
      menu.addElement(new Command1("Đoạn Sơn 83-88", this, MENU_MAP_BASE + 141, null));
      menu.addElement(new Command1("Đảo Quỷ 85-92", this, MENU_MAP_BASE + 142, null));
      menu.addElement(new Command1("Sinh Tử Kiều 103-108", this, MENU_MAP_BASE + 143, null));
      menu.addElement(new Command1("Nhân Duyên Lộ 114-117", this, MENU_MAP_BASE + 144, null));
      menu.addElement(new Command1("Hoang Trấn 123-126", this, MENU_MAP_BASE + 145, null));
      menu.addElement(new Command1("Mê Tâm Lộ 131-137", this, MENU_MAP_BASE + 146, null));
      menu.addElement(new Command1("Bất Tử Hoang 142-148", this, MENU_MAP_BASE + 147, null));
      menu.addElement(new Command1("Cửu Mộc Hoang 153-159", this, MENU_MAP_BASE + 148, null));
      GameCanvas.menu.startAt(menu);
      System.out.println("AutoVDMQ show map menu");
   }

   public final void perform(int command, Object data) {
      if (command == MENU_CONFIG_ZONES) {
         GameCanvas.inputDlg.a("Khu up VDMQ (cach nhau bang dau cach)", new Command1("Dat", this, MENU_SAVE_ZONES, null), 0);
         GameCanvas.inputDlg.tfInput.a(this.formatConfiguredZones());
         return;
      }
      if (command == MENU_SAVE_ZONES) {
         this.configuredZones = this.parseConfiguredZones(GameCanvas.inputDlg.tfInput.e());
         mResources.a(RMS_VDMQ_ZONES, this.formatConfiguredZones());
         GameCanvas.n();
         GameScr.addChatPopup(this.configuredZones.length == 0 ? "Auto VDMQ: dung tat ca khu" : "Auto VDMQ: khu up " + this.formatConfiguredZones());
         this.showMapChoiceMenu();
         return;
      }
      if (command == MENU_AUTO_LEVEL) {
         this.requestedMapId = -1;
         this.waitingMapChoice = false;
         this.mapSelected = false;
         this.lastObservedMap = -1;
         System.out.println("AutoVDMQ menu select=AUTO_LEVEL");
         GameScr.addChatPopup("Auto VDMQ: tự chọn map theo level");
         return;
      }

      int mapId = command - MENU_MAP_BASE;
      if (mapId >= VDMQ_FIRST_MAP && mapId <= VDMQ_LAST_MAP) {
         this.requestedMapId = mapId;
         this.waitingMapChoice = false;
         this.mapSelected = false;
         this.lastObservedMap = -1;
         System.out.println("AutoVDMQ menu select map=" + mapId + " name=" + this.getVdmqMapName(mapId));
         GameScr.addChatPopup("Auto VDMQ: " + this.getVdmqMapName(mapId) + " (map " + mapId + ")");
      }
   }

   static boolean isGroundMob(Mob mob) {
      if (mob == null || Mob.arrMobTemplate == null || mob.templateId < 0 || mob.templateId >= Mob.arrMobTemplate.length) {
         return false;
      }
      MobTemplate template = Mob.arrMobTemplate[mob.templateId];
      return template != null && template.typeFly == 0 && template.type != 4;
   }

   public final void update() {
      Char me = Char.getMyChar();
      if (me == null || this.waitingMapChoice) {
         return;
      }
      if (Auto.i()) {
         Auto.a(true);
         return;
      }
      if (AutoFoodRecovery.maintain(this, this.getSchoolMap(me))) {
         return;
      }
      if (me.clevel < 60) {
         this.notice("Auto VDMQ cần level 60 để dùng Thiên Nhãn Phù");
         return;
      }

      if (TileMap.mapID != this.lastObservedMap) {
         this.lastObservedMap = TileMap.mapID;
         this.mapEnteredAt = System.currentTimeMillis();
         this.mapSelected = this.requestedMapId >= VDMQ_FIRST_MAP && TileMap.mapID == this.requestedMapId;
         this.zoneDataMap = -1;
         this.zoneDataRequestedAt = 0L;
      }

      if (TileMap.mapID < VDMQ_FIRST_MAP || TileMap.mapID > VDMQ_LAST_MAP) {
         if (!this.charmUsed) {
            Item charm = Char.f(THIEN_NHAN_PHU);
            int quantity = charm == null ? 0 : charm.quantity;

            // Có phù thì dùng luôn. Chỉ khi hết mới mua thêm đúng 5 cái.
            if (quantity <= 0) {
               if (this.buyThienNhanPhu(me, quantity)) {
                  return;
               }
            }

            charm = Char.f(THIEN_NHAN_PHU);
            if (charm != null && charm.quantity > 0) {
               this.resetCharmBuyState();
               if (System.currentTimeMillis() - this.lastAction < 900L) {
                  return;
               }
               System.out.println("AutoVDMQ use ThienNhanPhu bag=" + charm.indexUI + " quantity=" + charm.quantity);
               Service.gI().useItem(charm.indexUI);
               this.lastAction = System.currentTimeMillis();
               this.charmUsed = true;
               GameScr.addChatPopup("Auto VDMQ: đã dùng Thiên Nhãn Phù, còn " + (charm.quantity - 1));
               return;
            }
            return;
         }

         this.enterVungDatMaQuai(me);
         return;
      }

      if (this.requestedMapId >= VDMQ_FIRST_MAP) {
         if (TileMap.mapID != this.requestedMapId) {
            if (System.currentTimeMillis() - this.lastAction < 900L) {
               return;
            }
            int next = TileMap.mapID < this.requestedMapId ? TileMap.mapID + 1 : TileMap.mapID - 1;
            if (next < VDMQ_FIRST_MAP) next = VDMQ_FIRST_MAP;
            if (next > VDMQ_LAST_MAP) next = VDMQ_LAST_MAP;
            System.out.println("AutoVDMQ route selected current=" + TileMap.mapID + " next=" + next + " target=" + this.requestedMapId);
            this.a(next, -2, -1, -1);
            this.lastAction = System.currentTimeMillis();
            return;
         }

         if (!this.mapSelected) {
            this.mapSelected = true;
            super.b = TileMap.mapID;
            super.c = TileMap.zoneID;
            int mobLevel = this.getMapMobLevel();
            System.out.println("AutoVDMQ selected manual map=" + TileMap.mapID + " mobLevel=" + mobLevel + " charLevel=" + me.clevel);
            GameScr.addChatPopup("Auto VDMQ: " + this.getVdmqMapName(TileMap.mapID) + ", quái cấp " + mobLevel);
         }
      } else if (!this.mapSelected) {
         if (System.currentTimeMillis() - this.mapEnteredAt < 1800L) {
            return;
         }
         int mobLevel = this.getMapMobLevel();
         if (mobLevel < 0) {
            this.notice("Auto VDMQ đang chờ dữ liệu quái map " + TileMap.mapID);
            return;
         }
         int difference = mobLevel - me.clevel;
         if (difference < -10 && TileMap.mapID < VDMQ_LAST_MAP) {
            System.out.println("AutoVDMQ map=" + TileMap.mapID + " mobLevel=" + mobLevel + " charLevel=" + me.clevel + " -> next");
            this.a(TileMap.mapID + 1, -2, -1, -1);
            return;
         }
         if (difference > 10 && TileMap.mapID > VDMQ_FIRST_MAP) {
            System.out.println("AutoVDMQ map=" + TileMap.mapID + " mobLevel=" + mobLevel + " charLevel=" + me.clevel + " -> previous");
            this.a(TileMap.mapID - 1, -2, -1, -1);
            return;
         }
         this.mapSelected = true;
         super.b = TileMap.mapID;
         super.c = TileMap.zoneID;
         System.out.println("AutoVDMQ selected auto map=" + TileMap.mapID + " mobLevel=" + mobLevel + " charLevel=" + me.clevel);
         GameScr.addChatPopup("Auto VDMQ: " + this.getVdmqMapName(TileMap.mapID) + ", quái cấp " + mobLevel);
      }

      if (!this.selectFarmZone()) {
         return;
      }

      this.c(-1, this.a(true, Char.ec, Char.ed, false));
      this.c(-1);
   }

   private boolean buyThienNhanPhu(Char me, int currentQuantity) {
      long now = System.currentTimeMillis();
      int schoolMap = this.getSchoolMap(me);
      if (TileMap.mapID != schoolMap) {
         this.resetCharmBuyState();
         this.a(schoolMap, -2, -1, -1);
         return true;
      }

      // Sau khi đã gửi lệnh mua, chỉ chờ túi đồ cập nhật; tuyệt đối không spam request.
      if (this.charmBuyPending) {
         if (Char.f(THIEN_NHAN_PHU) != null) {
            this.resetCharmBuyState();
            return true;
         }
         if (now - this.charmBuyPendingAt < 3000L) {
            return true;
         }
         this.charmBuyPending = false;
      }

      // Chỉ scan mảng shop trong tối đa 4 giây sau khi chính Auto vừa chọn "Cửa hàng".
      // Không scan khi login/chọn nhân vật/đánh quái nên giữ độ mượt như v33.
      if (this.waitingCharmShop) {
         if (now - this.waitingCharmShopAt >= 250L && now - this.lastCharmShopScan >= 300L) {
            this.lastCharmShopScan = now;
            Item shopCharm = this.findThienNhanPhuInShop();
            if (shopCharm != null) {
               System.out.println("AutoVDMQ buy ThienNhanPhu id=" + shopCharm.template.id
                     + " typeUI=" + shopCharm.typeUI + " indexUI=" + shopCharm.indexUI
                     + " quantity=" + THIEN_NHAN_BATCH);
               Service.gI().buyItem(shopCharm.typeUI, shopCharm.indexUI, THIEN_NHAN_BATCH);
               this.lastAction = now;
               this.charmBuyPending = true;
               this.charmBuyPendingAt = now;
               this.waitingCharmShop = false;
               return true;
            }
         }

         if (now - this.waitingCharmShopAt < 4000L) {
            return true;
         }
         this.waitingCharmShop = false;
      }

      if (GameCanvas.menu.showMenu) {
         if (GameCanvas.menu.selectCaptionForStandaloneAuto(
               new String[]{"mua", "cửa hàng", "cua hang", "vật phẩm", "vat pham"}, "AutoVDMQ open-shop")) {
            this.lastAction = now;
            this.waitingCharmShop = true;
            this.waitingCharmShopAt = now;
            this.lastCharmShopScan = 0L;
            return true;
         }
         GameCanvas.menu.showMenu = false;
      }

      Npc goosho = GameScr.i(GOOSHO_NPC);
      if (goosho == null) {
         this.notice("Auto VDMQ chưa thấy NPC bán Thiên Nhãn Phù tại trường " + schoolMap);
         return true;
      }
      if (Math.abs(me.cx - goosho.cx) > 22 || Math.abs(me.cy - goosho.cy) > 22) {
         Char.b(goosho.cx, goosho.cy);
         return true;
      }
      if (now - this.lastAction >= 1200L) {
         me.npcFocus = goosho;
         Service.gI().openMenu(GOOSHO_NPC);
         this.lastAction = now;
         System.out.println("AutoVDMQ open charm NPC menu map=" + TileMap.mapID + " charm=" + currentQuantity);
      }
      return true;
   }

   private Item findThienNhanPhuInShop() {
      Item item = this.findThienNhanPhu(GameScr.arrItemStore);
      if (item != null) return item;
      item = this.findThienNhanPhu(GameScr.arrItemGrocery);
      if (item != null) return item;
      return this.findThienNhanPhu(GameScr.arrItemGroceryLock);
   }

   private Item findThienNhanPhu(Item[] items) {
      if (items == null) return null;
      int i;
      Item item;

      // ID check trước: không tạo String, rất nhẹ.
      for (i = 0; i < items.length; ++i) {
         item = items[i];
         if (item != null && item.template != null && item.template.id == THIEN_NHAN_PHU) {
            return item;
         }
      }

      // Fallback tên chỉ chạy trong cửa sổ shop và tối đa 1 lần / 300ms.
      // equalsIgnoreCase không tạo chuỗi lower-case mới như bản v34.
      for (i = 0; i < items.length; ++i) {
         item = items[i];
         if (item != null && item.template != null && item.template.name != null) {
            String name = item.template.name;
            if (name.equalsIgnoreCase("Thiên nhãn phù") || name.equalsIgnoreCase("Thien nhan phu")) {
               return item;
            }
         }
      }
      return null;
   }

   private void resetCharmBuyState() {
      this.waitingCharmShop = false;
      this.waitingCharmShopAt = 0L;
      this.charmBuyPending = false;
      this.charmBuyPendingAt = 0L;
      this.lastCharmShopScan = 0L;
   }

   private void enterVungDatMaQuai(Char me) {
      int schoolMap = this.getSchoolMap(me);
      if (TileMap.mapID != schoolMap) {
         this.a(schoolMap, -2, -1, -1);
         return;
      }
      if (GameCanvas.menu.showMenu) {
         if (GameCanvas.menu.selectCaptionForStandaloneAuto(
               new String[]{"vùng đất ma", "vung dat ma", "ma quỷ", "ma quy"}, "AutoVDMQ enter")) {
            this.lastAction = System.currentTimeMillis();
            return;
         }
         GameCanvas.menu.showMenu = false;
      }
      Npc kamakura = GameScr.i(KAMAKURA_NPC);
      if (kamakura == null) {
         this.notice("Auto VDMQ chưa thấy Kamakura tại trường " + schoolMap);
         return;
      }
      if (Math.abs(me.cx - kamakura.cx) > 22 || Math.abs(me.cy - kamakura.cy) > 22) {
         Char.b(kamakura.cx, kamakura.cy);
         return;
      }
      if (System.currentTimeMillis() - this.lastAction >= 1200L) {
         me.npcFocus = kamakura;
         Service.gI().openMenu(KAMAKURA_NPC);
         this.lastAction = System.currentTimeMillis();
         System.out.println("AutoVDMQ open Kamakura menu map=" + TileMap.mapID + " target=" + this.requestedMapId);
      }
   }

   private int getMapMobLevel() {
      if (GameScr.vMobAttack == null || GameScr.vMobAttack.size() == 0) {
         return -1;
      }
      int total = 0;
      int count = 0;
      for (int i = 0; i < GameScr.vMobAttack.size(); ++i) {
         Mob mob = (Mob) GameScr.vMobAttack.elementAt(i);
         if (mob == null || mob.isBoss || mob.levelBoss > 0) continue;
         int level = mob.getLevelForStandaloneAuto();
         if (level <= 0) continue;
         total += level;
         ++count;
      }
      return count == 0 ? -1 : total / count;
   }

   private boolean selectFarmZone() {
      long now = System.currentTimeMillis();
      GameScr game = GameScr.gI();
      if (this.zoneDataMap != TileMap.mapID) {
         if (this.zoneDataRequestedAt == 0L || now - this.zoneDataRequestedAt >= 3000L) {
            game.cx = null;
            GameScr.indexSelect = TileMap.zoneID;
            game.fj();
            this.zoneDataRequestedAt = now;
         }
         if (game.cx == null || now - this.zoneDataRequestedAt < 500L) {
            return false;
         }
         this.zoneDataMap = TileMap.mapID;
      }

      int targetZone = VdmqZonePolicy.chooseZone(game.cx, this.configuredZones, MAX_PLAYERS_PER_ZONE, (int)now);
      if (targetZone < 0) {
         this.notice("Auto VDMQ chua nhan duoc danh sach khu");
         this.zoneDataMap = -1;
         return false;
      }
      if (targetZone == TileMap.zoneID) {
         return true;
      }

      System.out.println("AutoVDMQ change zone map=" + TileMap.mapID + " current=" + TileMap.zoneID
            + " target=" + targetZone + " players=" + game.cx[targetZone]);
      Service.gI().requestChangeZone(targetZone, -1);
      this.zoneDataMap = -1;
      this.zoneDataRequestedAt = now;
      return false;
   }

   private int[] loadConfiguredZones() {
      return this.parseConfiguredZones(mResources.c(RMS_VDMQ_ZONES));
   }

   private int[] parseConfiguredZones(String value) {
      if (value == null || value.trim().length() == 0) {
         return new int[0];
      }
      String[] parts = value.trim().split(" ");
      int[] zones = new int[parts.length];
      int count = 0;
      for (int i = 0; i < parts.length; ++i) {
         try {
            int zone = Integer.parseInt(parts[i]);
            if (zone >= 0) {
               zones[count++] = zone;
            }
         } catch (Exception ignored) {
         }
      }
      int[] result = new int[count];
      System.arraycopy(zones, 0, result, 0, count);
      return result;
   }

   private String formatConfiguredZones() {
      String value = "";
      for (int i = 0; i < this.configuredZones.length; ++i) {
         value += (i == 0 ? "" : " ") + this.configuredZones[i];
      }
      return value;
   }

   private int getSchoolMap(Char me) {
      int classId = me.nClass == null ? 1 : me.nClass.classId;
      return classId <= 2 ? 1 : (classId <= 4 ? 27 : 72);
   }

   private String getVdmqMapName(int mapId) {
      switch (mapId) {
         case 139: return "Quỷ Sơn";
         case 140: return "Sơn Hải Vực";
         case 141: return "Đoạn Sơn";
         case 142: return "Đảo Quỷ";
         case 143: return "Sinh Tử Kiều";
         case 144: return "Nhân Duyên Lộ";
         case 145: return "Hoang Trấn";
         case 146: return "Mê Tâm Lộ";
         case 147: return "Bất Tử Hoang";
         case 148: return "Cửu Mộc Hoang";
         default: return "map " + mapId;
      }
   }

   private void notice(String message) {
      long now = System.currentTimeMillis();
      if (now - this.lastNotice < 5000L) {
         return;
      }
      this.lastNotice = now;
      System.out.println(message);
      GameScr.addChatPopup(message);
   }

   public final String toString() {
      if (this.waitingMapChoice) {
         return "Auto VDMQ - chọn map";
      }
      if (this.requestedMapId >= VDMQ_FIRST_MAP) {
         return "Auto VDMQ - " + this.getVdmqMapName(this.requestedMapId);
      }
      return "Auto VDMQ" + (this.mapSelected ? " - map " + TileMap.mapID : " - tự chọn level");
   }
}

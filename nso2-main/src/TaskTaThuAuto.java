public final class TaskTaThuAuto extends Auto {
   private static final int ASSIST_STOP_HP = 10000;
   private TaskOrder r;
   public int o;
   public static boolean p;
   public static long q;
   private boolean autoTaskLifecycle;
   private long lastTaskAction;
   private long foodUsePendingUntil;
   private int receiveAttempts;
   private int assistState;

   public final void g() {
      super.g();
      DailyCharacterProgress.refreshIfNeeded();
      this.autoTaskLifecycle = true;
      this.lastTaskAction = 0L;
      this.foodUsePendingUntil = 0L;
      this.receiveAttempts = 0;
      this.assistState = 0;
      p = false;
      this.r = Char.j(1);
      if (this.r != null) {
         this.o = this.r.killId;
         super.b = this.r.count >= this.r.maxCount ? getSchoolMap() : this.r.mapId;
         if (TileMap.mapID == this.r.mapId && TileMap.zoneID % 5 == 0) {
            super.c = TileMap.zoneID;
            return;
         }
      } else {
         super.b = getSchoolMap();
      }
      super.c = 5;
   }

   public final void a(int var1, int var2) {
      super.g();
      this.autoTaskLifecycle = false;
      this.lastTaskAction = 0L;
      this.foodUsePendingUntil = 0L;
      this.receiveAttempts = 0;
      this.assistState = 0;
      p = false;
      this.r = null;
      this.o = var2;
      super.b = var1;
      if (TileMap.mapID == var1 && TileMap.zoneID % 5 == 0) {
         super.c = TileMap.zoneID;
      } else {
         super.c = 5;
      }

   }

   public final void h() {
      this.r = Char.j(1);
      if (this.autoTaskLifecycle) {
         if (this.r == null || this.r.count >= this.r.maxCount) {
            super.b = getSchoolMap();
         } else {
            this.o = this.r.killId;
            super.b = this.r.mapId;
         }
      }
      super.h();
   }

   public final void update() {
      if (this.maintainFood()) {
         return;
      }

      if (this.autoTaskLifecycle && this.updateTaskLifecycle()) {
         return;
      }

      NSOT_MOB var1;
      if (super.b < 0) {
         var1 = NSOT_MOB.mod_nst;
         NSOT_MOB.d();
      } else {
         boolean var2;
         int var3;
         if (Auto.i()) {
            if (Char.eg && TileMap.mapID == super.b && TileMap.zoneID == super.c && Char.getMyChar().mobFocus != null && Char.getMyChar().mobFocus.hp < Char.getMyChar().mobFocus.maxHp / 20) {
               var3 = 0;

               while(true) {
                  if (var3 >= GameScr.vParty.size()) {
                     var2 = false;
                     break;
                  }

                  Party var4;
                  if ((var4 = (Party)GameScr.vParty.elementAt(var3)).c != null && var4.c.cHp > 0) {
                     var2 = true;
                     break;
                  }

                  ++var3;
               }
            } else {
               var2 = false;
            }

            if (!var2) {
               long var6;
               NSOT_MOB.a(var6 = 100L * (long)NSOT_MOB.u / 10L);
               Auto.a(true);
               NSOT_MOB.a(var6);
               return;
            }
         } else if (TileMap.mapID == super.b && TileMap.zoneID == super.c) {
            if (this.r != null && this.r.count >= this.r.maxCount) {
               super.b = getSchoolMap();
               Auto.j();
               return;
            }

            var2 = this.shouldWaitForGroupAssist();

            if (!var2) {
               this.c(this.o, 8);
            }

            if (Char.getMyChar().cMP < Char.getMyChar().cMaxMP * Char.el / 100) {
               Char.getMyChar().doUsePotion(17);
            }

            if (Char.getMyChar().cHp < Char.getMyChar().cMaxHp * Char.ek / 100) {
               var3 = (int)(System.currentTimeMillis() / 1000L);

               for(int var7 = 0; var7 < Char.getMyChar().vEff.size(); ++var7) {
                  Effect var5;
                  if ((var5 = (Effect)Char.getMyChar().vEff.elementAt(var7)).template.id == 21 && var5.timeLenght - (var3 - var5.timeStart) >= 2) {
                     return;
                  }
               }

               Char.getMyChar().doUsePotion(16);
               return;
            }
         } else {
            this.a(super.b, super.c, super.e, super.f);
         }
      }

   }

   public final String toString() {
      return DailyCharacterProgress.hudText();
   }

   private boolean updateTaskLifecycle() {
      TaskOrder current = Char.j(1);
      if (current != null) {
         this.r = current;
         this.receiveAttempts = 0;
      } else {
         this.r = null;
      }

      int schoolMap = getSchoolMap();
      if (this.r == null) {
         super.b = schoolMap;
         if (TileMap.mapID != schoolMap) {
            this.a(schoolMap, -2, -1, -1);
            return true;
         }

         if (System.currentTimeMillis() - this.lastTaskAction < 4000L) {
            return true;
         }
         if (this.receiveAttempts >= 8) {
            DailyCharacterProgress.refreshIfNeeded();
            if (DailyCharacterProgress.taThuRemaining() > 0) {
               this.receiveAttempts = 0;
               this.lastTaskAction = 0L;
               GameScr.addChatPopup("Ta Thu van con luot, tiep tuc thu nhan nhiem vu");
               return true;
            }
            GameScr.addChatPopup("Ta Thu da het luot hom nay");
            NSOT_MOB.d();
            return true;
         }
         if (GameScr.i(25) == null) {
            GameScr.addChatPopup("ATT: Dang cho NPC giao nhiem vu xuat hien");
            return true;
         }

         if (!TaThuMenuNavigator.tick(0)) {
            return true;
         }
         System.out.println("ATT task=receive-by-caption option=0 attempt=" + (this.receiveAttempts + 1));
         GameScr.addChatPopup("ATT: Tu dong nhan nhiem vu Ta Thu");
         this.lastTaskAction = System.currentTimeMillis();
         ++this.receiveAttempts;
         return true;
      }

      if (this.r.count >= this.r.maxCount) {
         super.b = schoolMap;
         if (TileMap.mapID != schoolMap) {
            System.out.println("ATT task=return-school from=" + TileMap.mapID + " to=" + schoolMap);
            this.a(schoolMap, -2, -1, -1);
            return true;
         }
         if (Char.af() <= 0) {
            Item sale = AutoNhiemVuPanel.autoSellUnsuitable ? findSafeSaleItem(Char.getMyChar()) : null;
            if (sale != null && System.currentTimeMillis() - this.lastTaskAction >= 1800L) {
               System.out.println("ATT inventory=sell-for-reward item=" + sale.template.id + " name="
                     + sale.template.name + " bag=" + sale.indexUI);
               Service.gI().saleItem(sale.indexUI, 1);
               this.lastTaskAction = System.currentTimeMillis();
               return true;
            }
            GameScr.addChatPopup("ATT: Hanh trang day, can trong 1 o de nhan thuong");
            return true;
         }
         if (System.currentTimeMillis() - this.lastTaskAction < 2500L) {
            return true;
         }
         if (GameScr.i(25) == null) {
            GameScr.addChatPopup("ATT: Dang cho NPC tra nhiem vu xuat hien");
            return true;
         }

         if (!TaThuMenuNavigator.tick(2)) {
            return true;
         }
         System.out.println("ATT task=complete-by-caption option=2 progress="
               + this.r.count + "/" + this.r.maxCount);
         GameScr.addChatPopup("ATT: Tu dong hoan thanh nhiem vu Ta Thu");
         this.lastTaskAction = System.currentTimeMillis();
         return true;
      }

      this.o = this.r.killId;
      super.b = this.r.mapId;
      return false;
   }

   private boolean shouldWaitForGroupAssist() {
      // Ta Thu now uses the same uninterrupted massacre loop as VDMQ and
      // Auto 1-70. Party-assist waiting used to clear mobFocus and introduce
      // visible pauses between attacks.
      this.assistState = 0;
      return false;
   }

   private static Mob getTaThuTarget(int templateId) {
      Char me = Char.getMyChar();
      if (me != null && me.mobFocus != null && me.mobFocus.hp > 0
            && (templateId < 0 || me.mobFocus.templateId == templateId)) {
         return me.mobFocus;
      }
      if (GameScr.vMobAttack != null) {
         for(int index = 0; index < GameScr.vMobAttack.size(); ++index) {
            Mob mob = (Mob)GameScr.vMobAttack.elementAt(index);
            if (mob != null && mob.hp > 0 && mob.status != 0 && mob.status != 1
                  && (templateId < 0 || mob.templateId == templateId)) {
               return mob;
            }
         }
      }

      return null;
   }

   private static boolean hasAlivePartyHelper() {
      Char me = Char.getMyChar();
      for(int index = 0; index < GameScr.vParty.size(); ++index) {
         Party member = (Party)GameScr.vParty.elementAt(index);
         if (member != null && member.charId != me.charID && member.c != null && member.c.cHp > 0
               && member.c.statusMe != 14 && member.c.statusMe != 5) {
            return true;
         }
      }

      return false;
   }

   private boolean maintainFood() {
      return AutoFoodRecovery.maintain(this, getSchoolMap());
   }

   private static boolean hasFoodEffect(Char me) {
      for(int index = 0; index < me.vEff.size(); ++index) {
         Effect effect = (Effect)me.vEff.elementAt(index);
         if (effect != null && effect.template != null && effect.template.type == 0) {
            return true;
         }
      }

      return false;
   }

   private static Item findFood(Char me, int level) {
      Item best = null;
      for(int index = 0; index < me.arrItemBag.length; ++index) {
         Item item = me.arrItemBag[index];
         if (item != null && item.template != null && item.template.type == 18
               && item.template.level <= level
               && (best == null || item.template.level > best.template.level)) {
            best = item;
         }
      }

      return best;
   }

   private static Item findSafeSaleItem(Char me) {
      if (me == null || me.arrItemBag == null || me.arrItemBody == null) {
         return null;
      }

      for(int index = 0; index < me.arrItemBag.length; ++index) {
         Item item = me.arrItemBag[index];
         if (item == null || item.template == null || item.isLock || item.upgrade != 0
               || item.template.type < 0 || item.template.type > 9) {
            continue;
         }

         boolean wrongGender = item.template.gender != 2 && item.template.gender != me.cgender;
         Item equipped = item.template.type < me.arrItemBody.length ? me.arrItemBody[item.template.type] : null;
         boolean clearlyWeaker = equipped != null && equipped.template != null
               && item.template.level < equipped.template.level;
         if (wrongGender || clearlyWeaker) {
            return item;
         }
      }

      return null;
   }

   private static int getSchoolMap() {
      Char me = Char.getMyChar();
      int classId = me == null || me.nClass == null ? 0 : me.nClass.classId;
      if (classId <= 2) {
         return 1;
      }
      return classId <= 4 ? 72 : 27;
   }

   public static void a() {
   }

   public static void c() {
      p = false;
      q = 0L;
   }

   static {
      Main.main(50);
      a();
   }
}

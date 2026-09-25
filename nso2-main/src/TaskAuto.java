public final class TaskAuto extends Auto {
   public static boolean o;
   private static String[] p;
   private static int q;
   private TaskOrder r;
   private long s;
   private int schoolMap;
   private int completedTasks;
   private boolean returningAfterCompletion;
   private long dailyWarpRequestedAt;
   private long returnSchoolRequestedAt;
   private int dailyCombatMap;
   private int watchdogTaskCount;
   private long noTargetSince;
   private static final long DAILY_ROUTE_TIMEOUT_MS = 8000L;

   public static void a() {
      o = false;
      p = new String[]{"H\u00f4m nay con \u0111\u00e3 l\u00e0m h\u1ebft nhi\u1ec7m v\u1ee5 ta giao. H\u00e3y quay l\u1ea1i v\u00e0o ng\u00e0y h\u00f4m sau.", "\u0110\u00e2y l\u00e0 l\u1ea7n nh\u1eadn nhi\u1ec7m v\u1ee5 th\u1ee9 ", " trong ng\u00e0y h\u00f4m nay. M\u1ed7i ng\u00e0y \u0111\u01b0\u1ee3c nh\u1eadn t\u1ed1i \u0111a 20 l\u1ea7n con nh\u00e9."};
   }

   public static void a(String var0) {
      if (var0.equals(p[0])) {
         q = 21;
         Class_cl.m();
      } else {
         int var1;
         if ((var1 = var0.indexOf(p[1])) >= 0) {
            var0 = var0.substring(var1 + p[1].length(), var0.indexOf(p[2])).trim();

            try {
               q = Integer.parseInt(var0);
               return;
            } catch (NumberFormatException var3) {
            }
         }
      }

   }

   public final void g() {
      q = 0;
      this.r = Char.j(0);
      this.s = 0L;
      this.schoolMap = TileMap.f(TileMap.mapID) ? TileMap.mapID : getClassSchoolMap();
      // Continue from the server's daily total instead of restarting the
      // session counter at 0 (for example, an account already at 13/20).
      this.completedTasks = DailyCharacterProgress.dailyCompleted();
      this.returningAfterCompletion = false;
      this.dailyWarpRequestedAt = 0L;
      this.returnSchoolRequestedAt = 0L;
      this.dailyCombatMap = -1;
      this.watchdogTaskCount = -1;
      this.noTargetSince = 0L;
      super.g();
      // Keep the player's current massacre skill exactly as Auto 1-70 and
      // VDMQ do. Replacing it with the widest AOE could select a long-cooldown
      // skill and was the remaining source of visible attack pauses.
   }

   public final void h() {
      this.r = Char.j(0);
      super.h();
   }

   public final void update() {
      this.completedTasks = DailyCharacterProgress.mergeDailyCompleted(this.completedTasks);
      // q is only the task-order label parsed from server text.  It can
      // temporarily become 21 after a stale "finished" dialog even while
      // the character-info counter is still 1/20, so it must never decide
      // completion by itself.  The server-backed completedTasks counter is
      // the sole completion gate.
      if (this.completedTasks < 20) {
         if (Char.getMyChar().cHp <= 0) {
            long var1;
            NSOT_MOB.a(var1 = 100L * (long)NSOT_MOB.u / 10L);
            Auto.a(true);
            NSOT_MOB.a(var1);
            return;
         }

         if (AutoFoodRecovery.maintain(this,
               this.schoolMap >= 0 ? this.schoolMap : getClassSchoolMap())) {
            return;
         }

         TaskOrder current = Char.j(0);
         if (current != null) {
            // Ronin removes the completed order and supplies the next one
            // itself.  Seeing an incomplete replacement confirms that no
            // "Hoàn thành" button has to be pressed at the school.
            if (current != this.r && current.count < current.maxCount) {
               // A reconnect can place the character in a village while the
               // server replaces the old order.  A village is already a safe
               // routing point, so do not treat it as a stuck combat map and
               // suicide there (that only respawns in the same village).
               boolean changedAwayFromSchool = this.r != null
                     && !TileMap.d(TileMap.mapID) && !TileMap.f(TileMap.mapID);
               this.returningAfterCompletion = false;
               this.returnSchoolRequestedAt = 0L;
               this.dailyWarpRequestedAt = 0L;
               this.dailyCombatMap = -1;
               this.watchdogTaskCount = current.count;
               this.noTargetSince = 0L;
               this.r = current;
               if (changedAwayFromSchool) {
                  this.recoverFromRouteFailure("new-daily-task-while-away task=" + current.taskId
                        + " target=" + current.mapId);
                  return;
               }
            }
            this.r = current;
         } else if (this.r != null && this.r.count >= this.r.maxCount
               && this.returningAfterCompletion && TileMap.f(TileMap.mapID)) {
            // We are back at the school and the server has removed the old
            // order.  Ask for the next task; this is not a report/complete
            // action.
            this.r = null;
            this.returningAfterCompletion = false;
         }

         // After reconnect/death the character normally appears in a
         // village.  Route directly to the class school from here and never
         // start the wrong-map suicide watchdog: suicide in a village simply
         // revives at that same village and creates an endless loop.
         if (TileMap.d(TileMap.mapID)) {
            this.dailyWarpRequestedAt = 0L;
            this.returnSchoolRequestedAt = 0L;
            if (System.currentTimeMillis() - this.s >= 1800L) {
               this.s = System.currentTimeMillis();
               this.a(this.schoolMap >= 0 ? this.schoolMap : getClassSchoolMap(), -2, -1, -1);
            }
            return;
         }

         // Never report a daily task on Ronin.  Once its counter is full,
         // return to the starting school and wait for the server's next task
         // order (or use Nhận if no order has arrived yet).
         if (this.r != null && this.r.count >= this.r.maxCount) {
            if (!this.returningAfterCompletion) {
               ++this.completedTasks;
               this.returningAfterCompletion = true;
               this.dailyCombatMap = -1;
               System.out.println("AutoNVHN complete=" + this.completedTasks + "/20 task=" + this.r.name);
            }
            if (!TileMap.f(TileMap.mapID)) {
               long now = System.currentTimeMillis();
               if (this.returnSchoolRequestedAt == 0L) {
                  this.returnSchoolRequestedAt = now;
               } else if (now - this.returnSchoolRequestedAt >= DAILY_ROUTE_TIMEOUT_MS) {
                  this.recoverFromRouteFailure("return-school-timeout");
                  return;
               }
               if (now - this.s >= 1800L) {
                  this.s = now;
                  this.a(this.schoolMap >= 0 ? this.schoolMap : 1, -2, -1, -1);
               }
               return;
            }
            this.returnSchoolRequestedAt = 0L;
            GameScr.addChatPopup("Đã xong NV " + this.completedTasks + "/20, chờ NV mới");
            return;
         }

         if (TileMap.f(TileMap.mapID)) {
            this.returnSchoolRequestedAt = 0L;
            if (this.dailyWarpRequestedAt > 0L
                  && System.currentTimeMillis() - this.dailyWarpRequestedAt >= DAILY_ROUTE_TIMEOUT_MS) {
               this.recoverFromRouteFailure("daily-warp-stuck-at-school");
               return;
            }
            if (GameCanvas.menu.selectDailyRootForAuto()) {
               this.s = System.currentTimeMillis();
               return;
            }
            // Option 3 is Ronin's fast route to the task map.  Option 2
            // (Hoàn thành) is deliberately never selected here.
            int option = this.r == null ? 0 : 3;
            boolean selected = GameCanvas.menu.selectDailyTaskForAuto(option);
            if (selected) {
               this.s = System.currentTimeMillis();
               if (option == 3 && this.dailyWarpRequestedAt == 0L) {
                  // Command 11057 already performs Ronin's daily-task warp.
                  // Calling the normal Auto zone-change routine afterwards
                  // overwrites that destination and can send the character
                  // into a main-quest route instead.  Manual selection does
                  // not do either extra call, so mirror that exact behavior.
                  this.dailyWarpRequestedAt = this.s;
                  this.dailyCombatMap = -1;
               }
               return;
            }

            if (System.currentTimeMillis() - this.s < 1800L) {
               return;
            }

            if (this.r == null) {
               GameScr.addChatPopup("Nh\u1eadn NV " + (q + 1) + "/20");
               // Open Rikudou only. GameScr.b(...) also sends a menu packet
               // using GameScr.fi, which on Ronin belongs to the visible main
               // quest and can warp to its monster map before we select the
               // daily button.
               GameScr.h(25);
               this.s = System.currentTimeMillis();
               return;
            }

            if (this.r.count >= this.r.maxCount) {
               if (Char.af() <= 0) {
                  GameScr.addChatPopup("H\u00e0nh trang \u0111\u1ea7y");
                  return;
               }

               GameScr.addChatPopup("Ho\u00e0n th\u00e0nh NV " + q + "/20");
               GameScr.b(25, GameScr.fi, 2);
               this.s = System.currentTimeMillis();
               return;
            }

            GameScr.addChatPopup("\u0110i l\u00e0m NV " + q + "/20");
            // Match a manual NPC tap: open the four-button menu first, then
            // let the next update select "Di lam NV". Do not pre-send a main
            // task menu index through GameScr.b().
            GameScr.h(25);
            this.s = System.currentTimeMillis();
            // This only opens the four-button Rikudou menu.  Waiting on
            // TileMap.h() here freezes this auto thread before its next tick
            // can select the visible "Di lam NV" button; wait for the menu
            // normally and let the selection branch above trigger the warp.
            return;
         }

         if (this.r != null && !TileMap.d(TileMap.mapID) && !TileMap.f(TileMap.mapID)) {
            if (this.r.mapId >= 0 && TileMap.mapID != this.r.mapId) {
               long now = System.currentTimeMillis();
               if (this.dailyWarpRequestedAt == 0L) {
                  this.dailyWarpRequestedAt = now;
                  this.dailyCombatMap = -1;
                  GameCanvas.currentDialog = null;
               }
               if (now - this.dailyWarpRequestedAt >= DAILY_ROUTE_TIMEOUT_MS) {
                  System.out.println("AutoNVHN wrong-map timeout task=" + this.r.taskId
                        + " current=" + TileMap.mapID + " target=" + this.r.mapId
                        + " -> reconnect and retry");
                  GameCanvas.currentDialog = null;
                  this.recoverFromRouteFailure("daily-task-wrong-map");
                  return;
               }
               if (now - this.s >= 1800L) {
                  this.s = now;
                  System.out.println("AutoNVHN route task=" + this.r.taskId
                        + " current=" + TileMap.mapID + " target=" + this.r.mapId);
                  this.a(this.r.mapId, -2, -1, -1);
               }
               return;
            }
            if (this.dailyWarpRequestedAt > 0L
                  && System.currentTimeMillis() - this.dailyWarpRequestedAt >= DAILY_ROUTE_TIMEOUT_MS
                  && this.r.mapId >= 0 && TileMap.mapID != this.r.mapId) {
               System.out.println("AutoNVHN wrong-map task=" + this.r.taskId
                     + " current=" + TileMap.mapID + " target=" + this.r.mapId
                     + " -> reconnect and retry");
               GameCanvas.currentDialog = null;
               this.recoverFromRouteFailure("daily-task-wrong-map");
               return;
            }
            // The server warp from "Di lam NV" is authoritative. On Ronin,
            // TaskOrder.mapId can point at the concurrently displayed main
            // quest instead of this daily instance. Requiring equality here
            // made the auto stand still, then suicide as a false route error.
            // Pin the actual non-town map received from the server and fight
            // there; killId/count remain the reliable task fields.
            if (this.dailyCombatMap != TileMap.mapID) {
               this.dailyCombatMap = TileMap.mapID;
               System.out.println("AutoNVHN combat-map actual=" + this.dailyCombatMap
                     + " taskOrderMap=" + this.r.mapId
                     + " target=" + this.r.killId);
            }
            this.dailyWarpRequestedAt = 0L;
            if (this.watchdogTaskCount != this.r.count) {
               this.watchdogTaskCount = this.r.count;
               this.noTargetSince = 0L;
            }
            if (!hasDailyTargetMob()) {
               long now = System.currentTimeMillis();
               if (this.noTargetSince == 0L) this.noTargetSince = now;
               if (now - this.noTargetSince >= DAILY_ROUTE_TIMEOUT_MS) {
                  System.out.println("AutoNVHN no-target task=" + this.r.taskId
                        + " map=" + TileMap.mapID + " -> reconnect and retry");
                  this.recoverFromRouteFailure("daily-task-no-target");
                  return;
               }
            } else {
               this.noTargetSince = 0L;
            }
            this.c(this.r.killId, 1);
            this.c(-1);
            if (o) {
               GameScr.addChatPopup("Nhi\u1ec7m v\u1ee5 " + q + "/20: " + this.r.count + "/" + this.r.maxCount + " " + Mob.arrMobTemplate[this.r.killId].name);
               o = false;
               return;
            }
         }
      } else {
         if (!TileMap.f(TileMap.mapID)) {
            this.a(this.schoolMap >= 0 ? this.schoolMap : 1, -2, -1, -1);
            return;
         }
         GameScr.addChatPopup("Ho\u00e0n th\u00e0nh!");
         NSOT_MOB var3 = NSOT_MOB.mod_nst;
         NSOT_MOB.d();
      }

   }

   private void recoverFromRouteFailure(String reason) {
      int failedMap = TileMap.mapID;
      this.dailyWarpRequestedAt = 0L;
      this.returnSchoolRequestedAt = 0L;
      this.s = System.currentTimeMillis();
      GameCanvas.n();
      System.out.println("AutoNVHN route-failure reason=" + reason + " map=" + failedMap
            + " -> reconnect");
      GameScr.addChatPopup("Không chuyển được map -> đăng nhập lại");

      // The old recovery tried suicide/return-school.  In the desync shown in
      // the user's video those commands are ignored by the server while HP is
      // still alive, so the account remains stuck.  A reconnect is the one
      // action already verified to restore next-map state.
      DailyStallRecovery.forceReconnect("nvg-route-timeout-" + reason);
   }

   private boolean hasDailyTargetMob() {
      if (this.r == null || GameScr.vMobAttack == null) return false;
      for (int i = 0; i < GameScr.vMobAttack.size(); ++i) {
         Mob mob = (Mob)GameScr.vMobAttack.elementAt(i);
         if (mob != null && mob.templateId == this.r.killId && mob.hp > 0
               && mob.status != 0 && mob.status != 1) return true;
      }
      return false;
   }

   private static int getClassSchoolMap() {
      Char me = Char.getMyChar();
      int classId = me == null || me.nClass == null ? 0 : me.nClass.classId;
      if (classId <= 2) {
         return 1;
      }
      return classId <= 4 ? 72 : 27;
   }

   public final String toString() {
      return DailyCharacterProgress.hudText();
   }

   public static void c() {
      o = false;
      p = null;
      q = 0;
   }

   static {
      Main.main(10);
      a();
   }
}

/** Two-stage recovery for Auto NV combat: try another nearby mob before relogin. */
public final class CombatStallRecovery {
   private static final long ACTIVE_ATTACK_DELAY = 12000L;
   private static final long NO_EXP_DELAY = 15000L;
   private static int watchedCharId = Integer.MIN_VALUE;
   private static int watchedMap = -1;
   private static long watchedExp;
   private static long noExpSince;
   private static boolean nearbyMobTried;
   private static int firstMobId = -1;
   private static int retryMobId = -1;

   private CombatStallRecovery() {
   }

   public static void observe(Char me, long lastAttackAt, int attempts) {
      if (me == null) {
         reset(null);
         return;
      }

      long now = System.currentTimeMillis();
      if (watchedCharId != me.charID || watchedMap != TileMap.mapID) {
         reset(me);
      }

      if (me.cEXP != watchedExp) {
         if (nearbyMobTried) {
            System.out.println("AutoNVC combat=nearby-mob-exp-ok mob=" + retryMobId
                  + " exp=" + me.cEXP);
         }
         watchedExp = me.cEXP;
         noExpSince = now;
         nearbyMobTried = false;
         firstMobId = -1;
         retryMobId = -1;
         return;
      }

      boolean activelyAttacking = lastAttackAt > 0L
            && now - lastAttackAt <= ACTIVE_ATTACK_DELAY;
      if (!activelyAttacking) {
         // Do not count walking, skill selection or normal cooldown as no-EXP time.
         noExpSince = now;
      }
   }

   public static boolean isNoExpTimedOut(Char me) {
      return me != null && watchedCharId == me.charID && watchedMap == TileMap.mapID
            && System.currentTimeMillis() - noExpSince >= NO_EXP_DELAY;
   }

   public static boolean tryNearbyMob(Char me, int stalledMobId) {
      if (me == null || GameScr.vMobAttack == null) {
         nearbyMobTried = false;
         return false;
      }

      // Reaching the watchdog a second time without EXP means the retry failed.
      if (nearbyMobTried) {
         System.out.println("AutoNVC combat=nearby-mob-no-exp first=" + firstMobId
               + " retry=" + retryMobId + " -> relogin");
         nearbyMobTried = false;
         return false;
      }

      Mob nearest = null;
      int bestDistance = Integer.MAX_VALUE;
      for (int i = 0; i < GameScr.vMobAttack.size(); ++i) {
         Mob mob = (Mob)GameScr.vMobAttack.elementAt(i);
         if (mob == null || mob.mobId == stalledMobId || mob.hp <= 0
               || mob.status == 0 || mob.status == 1 || mob.levelBoss == 3) {
            continue;
         }
         int distance = Math.abs(me.cx - mob.xFirst) + Math.abs(me.cy - mob.yFirst);
         if (distance < bestDistance) {
            nearest = mob;
            bestDistance = distance;
         }
      }

      if (nearest == null) {
         System.out.println("AutoNVC combat=no-nearby-mob-except=" + stalledMobId + " -> relogin");
         return false;
      }

      nearbyMobTried = true;
      firstMobId = stalledMobId;
      retryMobId = nearest.mobId;
      watchedExp = me.cEXP;
      noExpSince = System.currentTimeMillis();

      int targetX = nearest.xFirst + (me.cx <= nearest.xFirst ? -30 : 30);
      int targetY = nearest.yFirst;
      if (!Char.c(targetX, targetY)) {
         Char.b(targetX, targetY);
      }
      me.mobFocus = nearest;
      GameScr.addChatPopup("Khong co EXP, thu quai gan khac");
      System.out.println("AutoNVC combat=try-nearby-mob first=" + firstMobId
            + " retry=" + retryMobId + " distance=" + bestDistance
            + " exp=" + watchedExp);
      return true;
   }

   private static void reset(Char me) {
      watchedCharId = me == null ? Integer.MIN_VALUE : me.charID;
      watchedMap = me == null ? -1 : TileMap.mapID;
      watchedExp = me == null ? 0L : me.cEXP;
      noExpSince = System.currentTimeMillis();
      nearbyMobTried = false;
      firstMobId = -1;
      retryMobId = -1;
   }
}

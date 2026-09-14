/** Shared food recovery for VDMQ, Daily and Ta Thu autos. */
public final class AutoFoodRecovery {
    private static final long USE_WAIT_MS = 3000L;
    private static final long ROUTE_RETRY_MS = 1500L;
    private static final long NOTICE_INTERVAL_MS = 5000L;
    private static Auto activeOwner;
    private static long usePendingUntil;
    private static long lastRouteAt;
    private static long lastNoticeAt;

    private AutoFoodRecovery() {
    }

    /** Returns true while food recovery owns the current auto tick. */
    public static boolean maintain(Auto owner, int schoolMap) {
        Char me = Char.getMyChar();
        if (owner == null || me == null || me.arrItemBag == null
                || me.cHp <= 0 || me.statusMe == 14 || me.statusMe == 5) {
            return false;
        }
        if (activeOwner != owner) {
            activeOwner = owner;
            usePendingUntil = 0L;
            lastRouteAt = 0L;
        }

        // These autos require uninterrupted food maintenance. Keep the
        // shared Auto Mua Thuc An option enabled and persist it, even if it
        // was disabled manually while the auto was running.
        if (!Char.dw) {
            Char.dw = true;
            Char.b();
            System.out.println("AutoFood enabled and saved Auto Mua Thuc An");
            GameScr.addChatPopup("Da tu bat Auto Mua Thuc An");
        }

        int foodLevel = compatibleFoodLevel(me.clevel);
        Char.em = foodLevel;
        if (hasFoodEffect(me)) {
            Char.isAMP = true;
            usePendingUntil = 0L;
            return false;
        }

        long now = System.currentTimeMillis();
        if (now < usePendingUntil) {
            // Prevent Char.update() from sending the same use-item packet
            // again before the server has returned the food effect.
            Char.isAMP = false;
            return true;
        }

        Item food = findBestFood(me, foodLevel);
        if (food != null) {
            if (GameCanvas.menu.showMenu || ChatPopup.b != null || GameCanvas.currentDialog != null) {
                return true;
            }
            System.out.println("AutoFood use level=" + food.template.level
                    + " template=" + food.template.id + " bag=" + food.indexUI
                    + " owner=" + owner.getClass().getName());
            Service.gI().useItem(food.indexUI);
            Char.isAMP = false;
            usePendingUntil = now + USE_WAIT_MS;
            return true;
        }

        // Re-enable the client's established 1-70 food buyer. It buys at a
        // village/school when Char.dw is enabled and Char.em has the desired tier.
        Char.isAMP = true;
        if (Char.af() <= 1) {
            notice("Het thuc an - can trong it nhat 2 o hanh trang");
            return true;
        }

        if (TileMap.mapID == schoolMap || TileMap.d(TileMap.mapID)) {
            notice("Dang mua thuc an cap " + foodLevel);
            // Yield this tick. NSOT_MOB's shared maintenance loop runs next
            // and performs the same buyItem_food flow used by Auto 1-70.
            return true;
        }

        if (now - lastRouteAt >= ROUTE_RETRY_MS) {
            lastRouteAt = now;
            if (TileMap.mapID >= 139 && TileMap.mapID <= 148) {
                System.out.println("AutoFood leave VDMQ map=" + TileMap.mapID + " for school=" + schoolMap);
                Char.b(me.cx, TileMap.d);
            } else {
                System.out.println("AutoFood route map=" + TileMap.mapID + " -> school=" + schoolMap);
                owner.a(schoolMap, -2, -1, -1);
            }
        }
        return true;
    }

    private static int compatibleFoodLevel(int level) {
        return level < 10 ? 1 : (level >= 50 ? 50 : level / 10 * 10);
    }

    private static boolean hasFoodEffect(Char me) {
        for (int index = 0; index < me.vEff.size(); ++index) {
            Effect effect = (Effect) me.vEff.elementAt(index);
            if (effect != null && effect.template != null && effect.template.type == 0) {
                return true;
            }
        }
        return false;
    }

    private static Item findBestFood(Char me, int maxLevel) {
        Item best = null;
        for (int index = 0; index < me.arrItemBag.length; ++index) {
            Item item = me.arrItemBag[index];
            if (item != null && item.template != null && item.template.type == 18
                    && item.template.level <= maxLevel
                    && (best == null || item.template.level > best.template.level)) {
                best = item;
            }
        }
        return best;
    }

    private static void notice(String message) {
        long now = System.currentTimeMillis();
        if (now - lastNoticeAt < NOTICE_INTERVAL_MS) {
            return;
        }
        lastNoticeAt = now;
        System.out.println("AutoFood " + message);
        GameScr.addChatPopup(message);
    }
}

public final class AutoNvcLinhChiShop {
    private static final int GOOSHO_NPC = 26;
    private static final int STORE_TYPE = 14;
    private static final int BUY_BATCH = 5;
    private static final long ACTION_DELAY = 450L;
    private static final long ROUTE_DELAY = 1800L;
    private static final long STORE_DELAY = 1400L;
    private static final long BUY_VERIFY_DELAY = 8000L;
    private static final long FAIL_COOLDOWN = 15000L;

    private static int state;
    private static long lastAction;
    private static long failUntil;
    private static int beforeCount;
    private static int targetSchool = -1;
    private static int blockedYen = -1;
    private static int linhChiEffectId = -1;
    private static final int[] effectStartSnapshot = new int[256];
    private static final int[] effectLengthSnapshot = new int[256];

    private AutoNvcLinhChiShop() {
    }

    public static boolean tick() {
        try {
            Char player = Char.getMyChar();
            if (player == null || player.arrItemBag == null || player.cHp <= 0) {
                resetTransient();
                return false;
            }

            long now = System.currentTimeMillis();
            enableNativeAutoUseFromBag(player);

            int effectRemain = getTrackedEffectRemainSeconds(player);
            if (effectRemain <= 0 && linhChiEffectId < 0) {
                int existingEffectId = findLikelyExistingLinhChiEffect(player);
                if (existingEffectId >= 0) {
                    linhChiEffectId = existingEffectId;
                    effectRemain = getEffectRemainSeconds(player, linhChiEffectId);
                    System.out.println("[AutoNVC][LINHCHI] reuse existing X2-like effect id="
                            + linhChiEffectId + " remain=" + effectRemain + "s");
                }
            }

            if (effectRemain > 0) {
                blockedYen = -1;
                failUntil = 0L;
                resetBuyStateOnly();
                return false;
            }

            int bagCount = countLinhChi(player);
            if (bagCount > 0) {
                blockedYen = -1;
                failUntil = 0L;
                resetBuyStateOnly();
                return false;
            }

            if (now < failUntil) {
                return false;
            }
            if (Char.af() <= 0) {
                resetTransientKeepFail();
                return false;
            }

            if (state == 4) {
                int changedEffectId = findChangedActiveEffect(player);
                if (changedEffectId >= 0) {
                    linhChiEffectId = changedEffectId;
                    blockedYen = -1;
                    int remain = getEffectRemainSeconds(player, linhChiEffectId);
                    System.out.println("[AutoNVC][LINHCHI] buy verified by server effect id="
                            + linhChiEffectId + " remain=" + remain + "s");
                    resetTransient();
                    return false;
                }

                int currentCount = countLinhChi(player);
                if (currentCount > beforeCount) {
                    blockedYen = -1;
                    System.out.println("[AutoNVC][LINHCHI] buy verified by bag total=" + currentCount);
                    resetTransient();
                    return false;
                }
                if (now - lastAction < BUY_VERIFY_DELAY) {
                    return true;
                }

                blockedYen = player.yen;
                failUntil = now + FAIL_COOLDOWN;
                System.out.println("[AutoNVC][LINHCHI] buy failed/unconfirmed; continue farming; yen="
                        + player.yen);
                resetTransientKeepFail();
                return false;
            }

            Npc goosho = GameScr.i(GOOSHO_NPC);
            if (goosho == null) {
                if (state != 1) {
                    targetSchool = getSchoolMap(player);
                    state = 1;
                    System.out.println("[AutoNVC][LINHCHI] empty -> route Goosho school="
                            + targetSchool + " from=" + TileMap.mapID);
                }
                if (TileMap.mapID == targetSchool) {
                    return true;
                }
                if (now - lastAction >= ROUTE_DELAY) {
                    boolean previousIgnoreDailyRoute = TileMap.ignoreDailyTaskRouteForMainAuto;
                    try {
                        TileMap.ignoreDailyTaskRouteForMainAuto = true;
                        TileMap.l(targetSchool);
                    } finally {
                        TileMap.ignoreDailyTaskRouteForMainAuto = previousIgnoreDailyRoute;
                    }
                    lastAction = now;
                }
                return true;
            }

            targetSchool = TileMap.mapID;
            if (Math.abs(player.cx - goosho.cx) > 28 || Math.abs(player.cy - goosho.cy) > 28) {
                if (now - lastAction >= ACTION_DELAY) {
                    Char.b(goosho.cx, goosho.cy);
                    lastAction = now;
                }
                state = 2;
                return true;
            }

            player.npcFocus = goosho;
            Item linhChi = findLinhChiInStore();
            if (linhChi == null) {
                if (now - lastAction >= STORE_DELAY) {
                    Service.gI().requestItem(STORE_TYPE);
                    lastAction = now;
                    state = 2;
                    System.out.println("[AutoNVC][LINHCHI] request Goosho store type=" + STORE_TYPE);
                }
                return true;
            }

            enableNativeAutoUse(linhChi);
            if (now - lastAction < STORE_DELAY) {
                return true;
            }

            int freeSlots = countFreeBagSlots(player);
            int quantity = freeSlots < BUY_BATCH ? freeSlots : BUY_BATCH;
            if (quantity <= 0) {
                System.out.println("[AutoNVC][LINHCHI] X2 expired but bag is full; skip shop and continue farming");
                resetTransient();
                return false;
            }

            long batchPrice = getBatchPriceYen(linhChi, quantity);
            if (batchPrice > 0L && (long) player.yen < batchPrice) {
                blockedYen = player.yen;
                failUntil = now + FAIL_COOLDOWN;
                System.out.println("[AutoNVC][LINHCHI] insufficient yen; qty=" + quantity
                        + " need=" + batchPrice + " have=" + player.yen + "; continue farming");
                resetTransientKeepFail();
                return false;
            }
            if (blockedYen >= 0 && player.yen <= blockedYen) {
                failUntil = now + FAIL_COOLDOWN;
                resetTransientKeepFail();
                return false;
            }

            beforeCount = countLinhChi(player);
            snapshotEffects(player);
            Service.gI().buyItem(linhChi.typeUI, linhChi.indexUI, quantity);
            lastAction = now;
            state = 4;
            System.out.println("[AutoNVC][LINHCHI] X2 expired -> buy qty=" + quantity
                    + " (max 5 / freeSlots=" + freeSlots + "); wait server effect");
            return true;
        } catch (Exception error) {
            System.out.println("[AutoNVC][LINHCHI] error=" + error);
            failUntil = System.currentTimeMillis() + FAIL_COOLDOWN;
            resetTransientKeepFail();
            return false;
        }
    }

    private static int countFreeBagSlots(Char player) {
        if (player == null || player.arrItemBag == null) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < player.arrItemBag.length; ++i) {
            if (player.arrItemBag[i] == null) {
                ++count;
            }
        }
        return count;
    }

    private static long getBatchPriceYen(Item item, int quantity) {
        if (item == null || quantity <= 0 || item.buyCoinLock <= 0) {
            return 0L;
        }
        return (long) item.buyCoinLock * (long) quantity;
    }

    private static int getTrackedEffectRemainSeconds(Char player) {
        if (linhChiEffectId < 0) {
            return 0;
        }
        return getEffectRemainSeconds(player, linhChiEffectId);
    }

    private static int getEffectRemainSeconds(Char player, int effectId) {
        if (player == null || player.vEff == null || effectId < 0) {
            return 0;
        }
        for (int i = 0; i < player.vEff.size(); ++i) {
            Effect effect = (Effect) player.vEff.elementAt(i);
            if (effect != null && effect.template != null && (effect.template.id & 255) == effectId) {
                return getEffectRemainSeconds(effect);
            }
        }
        return 0;
    }

    private static int getEffectRemainSeconds(Effect effect) {
        if (effect == null) {
            return 0;
        }
        int nowSeconds = (int) (System.currentTimeMillis() / 1000L);
        long remain = (long) effect.timeStart + (long) effect.timeLenght - (long) nowSeconds;
        if (remain <= 0L) {
            return 0;
        }
        return remain > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) remain;
    }

    private static void snapshotEffects(Char player) {
        int i;
        for (i = 0; i < effectStartSnapshot.length; ++i) {
            effectStartSnapshot[i] = Integer.MIN_VALUE;
            effectLengthSnapshot[i] = Integer.MIN_VALUE;
        }
        if (player == null || player.vEff == null) {
            return;
        }
        for (i = 0; i < player.vEff.size(); ++i) {
            Effect effect = (Effect) player.vEff.elementAt(i);
            if (effect == null || effect.template == null) {
                continue;
            }
            int effectId = effect.template.id & 255;
            effectStartSnapshot[effectId] = effect.timeStart;
            effectLengthSnapshot[effectId] = effect.timeLenght;
        }
    }

    private static int findChangedActiveEffect(Char player) {
        if (player == null || player.vEff == null) {
            return -1;
        }
        int bestEffectId = -1;
        int bestRemain = 0;
        for (int i = 0; i < player.vEff.size(); ++i) {
            Effect effect = (Effect) player.vEff.elementAt(i);
            if (effect == null || effect.template == null) {
                continue;
            }
            int effectId = effect.template.id & 255;
            int remain = getEffectRemainSeconds(effect);
            boolean unchanged = effectStartSnapshot[effectId] == effect.timeStart
                    && effectLengthSnapshot[effectId] == effect.timeLenght;
            if (remain > 0 && !unchanged && remain > bestRemain) {
                bestRemain = remain;
                bestEffectId = effectId;
            }
        }
        return bestEffectId;
    }

    private static int findLikelyExistingLinhChiEffect(Char player) {
        if (player == null || player.vEff == null) {
            return -1;
        }
        int bestEffectId = -1;
        int bestRemain = 0;
        for (int i = 0; i < player.vEff.size(); ++i) {
            Effect effect = (Effect) player.vEff.elementAt(i);
            if (effect == null || effect.template == null) {
                continue;
            }
            int remain = getEffectRemainSeconds(effect);
            int type = effect.template.type & 255;
            boolean excludedType = type == 1 || type == 2 || type == 3 || type == 7
                    || type == 11 || type == 12 || type == 14;
            if (remain >= 1800 && !excludedType && remain > bestRemain) {
                bestRemain = remain;
                bestEffectId = effect.template.id & 255;
            }
        }
        return bestEffectId;
    }

    private static void enableNativeAutoUseFromBag(Char player) {
        if (player == null || player.arrItemBag == null) {
            return;
        }
        for (int i = 0; i < player.arrItemBag.length; ++i) {
            Item item = player.arrItemBag[i];
            if (isLinhChiX2(item)) {
                enableNativeAutoUse(item);
                return;
            }
        }
    }

    private static void enableNativeAutoUse(Item item) {
        if (item == null || item.template == null) {
            return;
        }
        short templateId = item.template.id;
        if (templateId >= 0 && !NSOT_MOB.a((int) templateId)) {
            NSOT_MOB.b((int) templateId);
            System.out.println("[AutoNVC][LINHCHI] enable native Tu dung templateId=" + templateId);
        }
    }

    private static int countLinhChi(Char player) {
        if (player == null || player.arrItemBag == null) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < player.arrItemBag.length; ++i) {
            Item item = player.arrItemBag[i];
            if (isLinhChiX2(item)) {
                count += item.quantity > 0 ? item.quantity : 1;
            }
        }
        return count;
    }

    private static Item findLinhChiInStore() {
        if (GameScr.arrItemStore == null) {
            return null;
        }
        for (int i = 0; i < GameScr.arrItemStore.length; ++i) {
            Item item = GameScr.arrItemStore[i];
            if (isLinhChiX2(item)) {
                return item;
            }
        }
        return null;
    }

    private static boolean isLinhChiX2(Item item) {
        if (item == null || item.template == null || item.template.name == null) {
            return false;
        }
        String name = normalize(item.template.name);
        if (name.indexOf("linh chi") < 0) {
            return false;
        }
        String description = normalize(item.template.description == null ? "" : item.template.description);
        return name.indexOf("x2") >= 0 || description.indexOf("x2") >= 0
                || description.indexOf("2 kinh") >= 0 || description.indexOf("nhan 2 kinh") >= 0;
    }

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String normalized = text.toLowerCase();
        normalized = normalized.replace('\u00e1', 'a').replace('\u00e0', 'a').replace('\u1ea3', 'a').replace('\u00e3', 'a').replace('\u1ea1', 'a');
        normalized = normalized.replace('\u0103', 'a').replace('\u1eaf', 'a').replace('\u1eb1', 'a').replace('\u1eb3', 'a').replace('\u1eb5', 'a').replace('\u1eb7', 'a');
        normalized = normalized.replace('\u00e2', 'a').replace('\u1ea5', 'a').replace('\u1ea7', 'a').replace('\u1ea9', 'a').replace('\u1eab', 'a').replace('\u1ead', 'a');
        normalized = normalized.replace('\u00e9', 'e').replace('\u00e8', 'e').replace('\u1ebb', 'e').replace('\u1ebd', 'e').replace('\u1eb9', 'e');
        normalized = normalized.replace('\u00ea', 'e').replace('\u1ebf', 'e').replace('\u1ec1', 'e').replace('\u1ec3', 'e').replace('\u1ec5', 'e').replace('\u1ec7', 'e');
        normalized = normalized.replace('\u00ed', 'i').replace('\u00ec', 'i').replace('\u1ec9', 'i').replace('\u0129', 'i').replace('\u1ecb', 'i');
        normalized = normalized.replace('\u00f3', 'o').replace('\u00f2', 'o').replace('\u1ecf', 'o').replace('\u00f5', 'o').replace('\u1ecd', 'o');
        normalized = normalized.replace('\u00f4', 'o').replace('\u1ed1', 'o').replace('\u1ed3', 'o').replace('\u1ed5', 'o').replace('\u1ed7', 'o').replace('\u1ed9', 'o');
        normalized = normalized.replace('\u01a1', 'o').replace('\u1edb', 'o').replace('\u1edd', 'o').replace('\u1edf', 'o').replace('\u1ee1', 'o').replace('\u1ee3', 'o');
        normalized = normalized.replace('\u00fa', 'u').replace('\u00f9', 'u').replace('\u1ee7', 'u').replace('\u0169', 'u').replace('\u1ee5', 'u');
        normalized = normalized.replace('\u01b0', 'u').replace('\u1ee9', 'u').replace('\u1eeb', 'u').replace('\u1eed', 'u').replace('\u1eef', 'u').replace('\u1ef1', 'u');
        normalized = normalized.replace('\u00fd', 'y').replace('\u1ef3', 'y').replace('\u1ef7', 'y').replace('\u1ef9', 'y').replace('\u1ef5', 'y');
        return normalized.replace('\u0111', 'd');
    }

    private static int getSchoolMap(Char player) {
        int classId = player.nClass == null ? 1 : player.nClass.classId;
        return classId <= 2 ? 1 : (classId <= 4 ? 27 : 72);
    }

    private static void resetBuyStateOnly() {
        state = 0;
        beforeCount = 0;
        targetSchool = -1;
    }

    private static void resetTransient() {
        resetBuyStateOnly();
    }

    private static void resetTransientKeepFail() {
        resetBuyStateOnly();
    }
}

/** Buys and equips the requested 30-gold Mũ thời trang. */
public final class AutoNv130FashionSupply {
    private static final int GOOSHO_NPC = 26;
    private static final int FASHION_STORE_TYPE = 32;
    private static final long ACTION_DELAY = 1200L;
    private int stage;
    private int inspectedListingIndex;
    private short purchasedTemplateId = -1;
    private long lastAction;
    private boolean atGoosho;
    private boolean fashionMenuSelected;

    public void reset() {
        stage = 0;
        inspectedListingIndex = 0;
        purchasedTemplateId = -1;
        lastAction = 0L;
        atGoosho = false;
        fashionMenuSelected = false;
    }

    /** @return true until the requested fashion hat has been equipped. */
    public boolean tick() {
        Char me = Char.getMyChar();
        if (me == null || me.arrItemBag == null) return true;
        if (stage == 3) return false;
        long now = System.currentTimeMillis();
        if (stage == 0 && GameScr.arrItemFashion != null) {
            if (AutoNv130QuickPolicy.shouldWaitForFashionListingAction(lastAction, now)) return true;
        } else if (now - lastAction < ACTION_DELAY) return true;

        Item ownedHat = findOwnedHat(me);
        if (ownedHat != null) {
            Service.gI().useItem(ownedHat.indexUI);
            stage = 3;
            lastAction = now;
            return true;
        }
        if (hasEquippedHat(me)) {
            stage = 3;
            return false;
        }
        if (stage == 0 && AutoNv130QuickPolicy.shouldRouteToGooshoBeforeFashionPurchase(atGoosho)) {
            if (now - lastAction < ACTION_DELAY) return true;
            if (!reachGoosho(me, now)) return true;
            atGoosho = true;
            fashionMenuSelected = false;
            inspectedListingIndex = 0;
            GameScr.arrItemFashion = null;
            lastAction = now;
            System.out.println("AutoNV130 fashion=at-goosho request-fresh-list");
            return true;
        }
        if (stage == 0 && AutoNv130QuickPolicy.shouldOpenFashionMenuBeforeListing(fashionMenuSelected)) {
            if (now - lastAction < ACTION_DELAY) return true;
            openFashionMenu(now);
            return true;
        }

        if (stage == 0) {
            if (GameScr.arrItemFashion == null) {
                System.out.println("AutoNV130 fashion=request-list");
                Service.gI().requestItem(FASHION_STORE_TYPE);
                lastAction = now;
                return true;
            }
            int listedIndex = AutoNv130QuickPolicy.fashionHatListedIndexForGender(me.cgender);
            if (listedIndex < 0 || listedIndex >= GameScr.arrItemFashion.length
                    || GameScr.arrItemFashion[listedIndex] == null
                    || GameScr.arrItemFashion[listedIndex].template == null) {
                System.out.println("AutoNV130 fashion=target-index-missing index=" + listedIndex);
                GameScr.arrItemFashion = null;
                lastAction = now;
                return true;
            }
            Item listing = GameScr.arrItemFashion[listedIndex];
            if (me.luong < 30 || Char.af() <= 0) {
                System.out.println("AutoNV130 fashion=wait-resources gold=" + me.luong + "/30 slots=" + Char.af());
                lastAction = now;
                return true;
            }
            purchasedTemplateId = listing.template.id;
            System.out.println("AutoNV130 fashion=buy-target list=" + listedIndex + " server=" + listing.indexUI + " template=" + purchasedTemplateId);
            Service.gI().buyItem(listing.typeUI, listing.indexUI, 1);
            stage = 1;
            lastAction = now;
            return true;
        }

        Item purchasedHat = findPurchasedHat(me);
        if (purchasedHat == null) return true;
        Service.gI().useItem(purchasedHat.indexUI);
        stage = 3;
        lastAction = now;
        return true;
    }

    private Item nextFashionListing() {
        while (inspectedListingIndex < GameScr.arrItemFashion.length) {
            Item item = GameScr.arrItemFashion[inspectedListingIndex];
            if (item != null && item.template != null) return item;
            ++inspectedListingIndex;
        }
        return null;
    }

    private void openFashionMenu(long now) {
        if (GameCanvas.menu.showMenu) {
            if (GameCanvas.menu.selectCaptionForStandaloneAuto(
                    new String[]{"th\u1eddi trang", "thoi trang"}, "AutoNV130 Fashion")) {
                fashionMenuSelected = true;
                System.out.println("AutoNV130 fashion=menu-fashion-selected");
            } else if (GameCanvas.menu.selectCaptionForStandaloneAuto(
                    new String[]{"c\u1eeda h\u00e0ng", "cua hang"}, "AutoNV130 Goosho shop")) {
                System.out.println("AutoNV130 fashion=menu-shop-selected");
            } else {
                System.out.println("AutoNV130 fashion=wait-menu-caption");
            }
            lastAction = now;
            return;
        }
        Npc goosho = findGoosho();
        if (goosho == null || goosho.template == null) {
            atGoosho = false;
            lastAction = now;
            return;
        }
        System.out.println("AutoNV130 fashion=open-goosho-menu npc=" + goosho.template.npcTemplateId);
        Service.gI().openMenu(goosho.template.npcTemplateId);
        lastAction = now;
    }

    private boolean reachGoosho(Char me, long now) {
        Npc goosho = findGoosho();
        if (goosho == null) {
            int classId = me.nClass == null ? 1 : me.nClass.classId;
            int schoolMap = AutoNv130QuickPolicy.gooshoSchoolMap(classId);
            if (TileMap.mapID != schoolMap) {
                System.out.println("AutoNV130 fashion=route-goosho map=" + TileMap.mapID + " target=" + schoolMap);
                TileMap.l(schoolMap);
            } else {
                System.out.println("AutoNV130 fashion=wait-goosho-npc map=" + schoolMap);
            }
            lastAction = now;
            return false;
        }
        if (Math.abs(me.cx - goosho.cx) > 28 || Math.abs(me.cy - goosho.cy) > 28) {
            System.out.println("AutoNV130 fashion=move-goosho from=" + me.cx + "," + me.cy + " to=" + goosho.cx + "," + goosho.cy);
            Char.b(goosho.cx, goosho.cy);
            lastAction = now;
            return false;
        }
        me.npcFocus = goosho;
        return true;
    }

    private static Npc findGoosho() {
        Npc npc = GameScr.i(GOOSHO_NPC);
        if (npc != null) return npc;
        for (int i = 0; i < GameScr.vSet.size(); ++i) {
            Object object = GameScr.vSet.elementAt(i);
            if (!(object instanceof Npc)) continue;
            Npc candidate = (Npc)object;
            if (candidate.template != null && candidate.template.name != null
                    && candidate.template.name.toLowerCase().indexOf("goosho") >= 0) return candidate;
        }
        return null;
    }

    private static Item findOwnedHat(Char me) {
        for (int i = 0; i < me.arrItemBag.length; ++i) {
            Item item = me.arrItemBag[i];
            if (item != null && item.template != null
                    && AutoNv130QuickPolicy.isFashionMaskType(item.template.type)) return item;
        }
        return null;
    }

    private static boolean hasEquippedHat(Char me) {
        if (me.arrItemBody == null) return false;
        for (int i = 0; i < me.arrItemBody.length; ++i) {
            Item item = me.arrItemBody[i];
            if (item != null && item.template != null
                    && AutoNv130QuickPolicy.isFashionMaskType(item.template.type)) return true;
        }
        return false;
    }

    private Item findPurchasedHat(Char me) {
        for (int i = 0; i < me.arrItemBag.length; ++i) {
            Item item = me.arrItemBag[i];
            if (item != null && item.template != null && item.template.id == purchasedTemplateId) return item;
        }
        return null;
    }
}

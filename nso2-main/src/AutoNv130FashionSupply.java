/** Purchases and equips the gender-specific 30-gold fashion mask without opening the store UI. */
public final class AutoNv130FashionSupply {
    private static final int FASHION_STORE_TYPE = 32;
    private static final long ACTION_DELAY = 1200L;
    private int stage;
    private short purchasedTemplateId = -1;
    private long lastAction;

    public void reset() {
        stage = 0;
        purchasedTemplateId = -1;
        lastAction = 0L;
    }

    /** @return true while a fetch, purchase, or equip action is pending. */
    public boolean tick() {
        Char me = Char.getMyChar();
        if (me == null || me.arrItemBag == null) return true;
        long now = System.currentTimeMillis();
        if (now - lastAction < ACTION_DELAY) return true;
        if (stage == 3) return false;

        if (stage == 0) {
            if (GameScr.arrItemFashion == null) {
                Service.gI().requestItem(FASHION_STORE_TYPE);
                lastAction = now;
                return true;
            }
            Item mask = findMatchingStoreMask(me);
            if (mask == null) {
                stage = 3;
                return false;
            }
            if (!mask.s) {
                Service.gI().requestItemInfo(mask.typeUI, mask.indexUI);
                lastAction = now;
                return true;
            }
            if (me.luong < mask.buyGold || Char.af() <= 0) {
                stage = 3;
                return false;
            }
            purchasedTemplateId = mask.template.id;
            Service.gI().buyItem(mask.typeUI, mask.indexUI, 1);
            stage = 1;
            lastAction = now;
            return true;
        }

        Item purchasedMask = findPurchasedMask(me);
        if (purchasedMask == null) return true;
        Service.gI().useItem(purchasedMask.indexUI);
        stage = 3;
        lastAction = now;
        return true;
    }

    private static Item findMatchingStoreMask(Char me) {
        for (int i = 0; i < GameScr.arrItemFashion.length; ++i) {
            Item item = GameScr.arrItemFashion[i];
            if (item == null || item.template == null) continue;
            if (AutoNv130QuickPolicy.isMatchingFashionMask(item.template.type, item.template.gender,
                    me.cgender, item.buyGold)) return item;
            if (!item.s && item.template.type == 12 && item.template.gender == me.cgender) return item;
        }
        return null;
    }

    private Item findPurchasedMask(Char me) {
        for (int i = 0; i < me.arrItemBag.length; ++i) {
            Item item = me.arrItemBag[i];
            if (item != null && item.template != null && item.template.id == purchasedTemplateId) return item;
        }
        return null;
    }
}

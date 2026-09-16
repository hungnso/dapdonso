/** Buys and uses the one-time starter supplies from Goosho's store. */
public final class AutoNv130QuickSupply {
    private static final int GOOSHO_NPC = 26;
    private static final int STORE_TYPE = 14;
    private static final long ACTION_DELAY = 1200L;
    private static final String[] NAMES = {"tui vai cap 1", "tui vai cap 2", "linh chi"};
    private int stage;
    private int linhChiUses;
    private long lastAction;

    public void reset() {
        stage = 0;
        linhChiUses = 0;
        lastAction = 0L;
    }

    /** @return true while a purchase/use is still in progress. */
    public boolean tick() {
        Char me = Char.getMyChar();
        if (me == null || me.arrItemBag == null) return true;
        if (stage >= NAMES.length) return false;
        long now = System.currentTimeMillis();
        if (now - lastAction < ACTION_DELAY) return true;
        if (stage == 2 && linhChiUses >= AutoNv130QuickPolicy.linhChiQuantity()) {
            ++stage;
            return false;
        }

        Npc goosho = GameScr.i(GOOSHO_NPC);
        if (goosho == null) {
            int school = AutoNhiemVuPanel.selectedClass <= 2 ? 1 : (AutoNhiemVuPanel.selectedClass <= 4 ? 27 : 72);
            if (TileMap.mapID != school) TileMap.l(school);
            lastAction = now;
            return true;
        }
        if (Math.abs(me.cx - goosho.cx) > 28 || Math.abs(me.cy - goosho.cy) > 28) {
            Char.b(goosho.cx, goosho.cy);
            lastAction = now;
            return true;
        }
        Item bagItem = findInBag(stage);
        if (bagItem != null) {
            Service.gI().useItem(bagItem.indexUI);
            if (stage == 2) ++linhChiUses;
            else ++stage;
            lastAction = now;
            return true;
        }
        if (GameScr.arrItemStore == null) {
            Service.gI().requestItem(STORE_TYPE);
            lastAction = now;
            return true;
        }
        Item storeItem = findInStore(stage);
        if (storeItem == null || Char.af() <= 0) return true;
        int quantity = stage == 2 ? AutoNv130QuickPolicy.linhChiQuantity() : 1;
        Service.gI().buyItem(storeItem.typeUI, storeItem.indexUI, quantity);
        lastAction = now;
        return true;
    }

    private static Item findInBag(int stage) { return find(Char.getMyChar().arrItemBag, stage); }
    private static Item findInStore(int stage) { return find(GameScr.arrItemStore, stage); }
    private static Item find(Item[] items, int stage) {
        if (items == null) return null;
        for (int i = 0; i < items.length; ++i) {
            Item item = items[i];
            if (item != null && item.template != null && matches(item, stage)) return item;
        }
        return null;
    }
    private static boolean matches(Item item, int stage) {
        String name = normalize(item.template.name);
        if (stage < 2) return name.indexOf(NAMES[stage]) >= 0;
        String description = normalize(item.template.description);
        return name.indexOf("linh chi") >= 0 && (name.indexOf("x2") >= 0 || description.indexOf("x2") >= 0
                || description.indexOf("nhan 2 kinh") >= 0 || description.indexOf("2 kinh") >= 0);
    }
    private static String normalize(String text) {
        if (text == null) return "";
        String s = text.toLowerCase();
        String source = "àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ";
        String target = "aaaaaaaaaaaaaaaaaeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyyd";
        StringBuffer out = new StringBuffer(s.length());
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i); int index = source.indexOf(c);
            out.append(index >= 0 ? target.charAt(index) : c);
        }
        return out.toString();
    }
}

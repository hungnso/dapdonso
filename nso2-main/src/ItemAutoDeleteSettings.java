/**
 * Supplies a second list for GameScr's existing two-tab "Item Nhat" UI.
 * Pickup and auto-delete settings remain completely separate.
 */
public final class ItemAutoDeleteSettings {
    private static final int PICKUP_MENU_COMMAND = 1100076;
    // Keep this outside the runtime's occupied 1100090-1100099 range.
    private static final int DELETE_MENU_COMMAND = 11990076;
    private static final String RMS_KEY = "ItemAutoDeleteList";
    private static final long DELETE_INTERVAL_MS = 1800L;

    private static short[] deleteIds = new short[30];
    private static boolean deleteUiMode;
    private static long nextDeleteAt;
    private static String pickupTitle;

    private ItemAutoDeleteSettings() {
    }

    /** Converts Item Xoa to the existing Item Nhat UI command. */
    public static int normalizeMenuCommand(int commandId) {
        if (commandId == DELETE_MENU_COMMAND) {
            deleteUiMode = true;
            setFirstTabTitle("X\u00f3a \u0111\u1ed3");
            return PICKUP_MENU_COMMAND;
        }
        if (commandId == PICKUP_MENU_COMMAND) {
            deleteUiMode = false;
            setFirstTabTitle(pickupTitle == null ? "Nh\u1eb7t \u0111\u1ed3" : pickupTitle);
        }
        return commandId;
    }

    public static short[] getActiveList() {
        return deleteUiMode ? deleteIds : NSOT_MOB.f;
    }

    /** Prevents a discarded item from being picked straight back up. */
    public static boolean allowPickup(boolean originalDecision, ItemTemplate template) {
        return originalDecision && (template == null || !contains(template.id));
    }

    public static void addActive(short itemId) {
        if (!deleteUiMode) {
            NSOT_MOB.a(itemId);
            return;
        }
        if (contains(itemId)) {
            return;
        }
        int empty = -1;
        for (int i = 0; i < deleteIds.length; ++i) {
            if (deleteIds[i] < 0) {
                empty = i;
                break;
            }
        }
        if (empty < 0) {
            short[] expanded = new short[deleteIds.length + 10];
            System.arraycopy(deleteIds, 0, expanded, 0, deleteIds.length);
            fillEmpty(expanded, deleteIds.length);
            empty = deleteIds.length;
            deleteIds = expanded;
        }
        deleteIds[empty] = itemId;
        save();
    }

    public static void removeActive(short itemId) {
        if (!deleteUiMode) {
            NSOT_MOB.b(itemId);
            return;
        }
        for (int i = 0; i < deleteIds.length; ++i) {
            if (deleteIds[i] == itemId) {
                deleteIds[i] = -1;
            }
        }
        save();
    }

    public static void compactActive() {
        if (!deleteUiMode) {
            NSOT_MOB.n();
            return;
        }
        int destination = 0;
        for (int source = 0; source < deleteIds.length; ++source) {
            if (deleteIds[source] > 0) {
                short value = deleteIds[source];
                deleteIds[source] = -1;
                deleteIds[destination++] = value;
            }
        }
        save();
    }

    /** Sends at most one discard request per interval. */
    public static void tick() {
        long now = System.currentTimeMillis();
        if (now < nextDeleteAt || GameCanvas.currentScreen != GameScr.instance
                || Session_ME.getInstance() == null || !Session_ME.getInstance().connected) {
            return;
        }
        Char me = Char.getMyChar();
        if (me == null || me.arrItemBag == null) {
            return;
        }
        for (int i = 0; i < me.arrItemBag.length; ++i) {
            Item item = me.arrItemBag[i];
            if (item != null && item.template != null && contains(item.template.id)
                    && !item.isLock && item.upgrade <= 0) {
                // Waiting for the server response prevents shifted bag slots
                // from causing a request for the wrong item.
                Service.gI().throwItem(item.indexUI);
                nextDeleteAt = now + DELETE_INTERVAL_MS;
                return;
            }
        }
        nextDeleteAt = now + 500L;
    }

    private static boolean contains(short itemId) {
        for (int i = 0; i < deleteIds.length; ++i) {
            if (deleteIds[i] == itemId) {
                return true;
            }
        }
        return false;
    }

    private static void setFirstTabTitle(String title) {
        if (mResources.st != null && mResources.st.length > 0) {
            if (pickupTitle == null) {
                pickupTitle = mResources.st[0];
            }
            mResources.st[0] = title;
        }
    }

    private static void fillEmpty(short[] values, int start) {
        for (int i = start; i < values.length; ++i) {
            values[i] = -1;
        }
    }

    private static void save() {
        StringBuffer value = new StringBuffer();
        for (int i = 0; i < deleteIds.length; ++i) {
            if (deleteIds[i] > 0) {
                if (value.length() > 0) {
                    value.append(',');
                }
                value.append(deleteIds[i]);
            }
        }
        mResources.a(RMS_KEY, value.toString());
    }

    private static void load() {
        fillEmpty(deleteIds, 0);
        String saved = mResources.c(RMS_KEY);
        if (saved == null || saved.length() == 0) {
            return;
        }
        int start = 0;
        while (start < saved.length()) {
            int comma = saved.indexOf(',', start);
            String part = comma < 0 ? saved.substring(start) : saved.substring(start, comma);
            try {
                addLoaded(Short.parseShort(part.trim()));
            } catch (Exception ignored) {
            }
            if (comma < 0) {
                break;
            }
            start = comma + 1;
        }
    }

    private static void addLoaded(short itemId) {
        if (contains(itemId)) {
            return;
        }
        for (int i = 0; i < deleteIds.length; ++i) {
            if (deleteIds[i] < 0) {
                deleteIds[i] = itemId;
                return;
            }
        }
    }

    static {
        load();
    }
}

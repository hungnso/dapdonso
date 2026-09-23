/** Immutable defaults captured from the requested Auto NVHN 3x emulator profile. */
public final class AutoNvhn3xProfile {
    private AutoNvhn3xProfile() {
    }

    public static int hpThreshold() { return 50; }
    public static int mpThreshold() { return 20; }
    public static int foodLevel() { return 30; }
    public static int hpMpPickupLevel() { return 10; }
    public static int materialPickupLevel() { return 3; }
    public static int equipmentPickupLevel() { return 20; }

    /** Auto-food purchase must be paused while the mask shop is opened. */
    public static boolean pauseFoodPurchaseDuringMask() { return true; }

    public static boolean enabled(String name) {
        if (name == null) return false;
        if (name.equals("hp") || name.equals("mp") || name.equals("food")
                || name.equals("support") || name.equals("manaShield")
                || name.equals("yen") || name.equals("hpMpPickup")
                || name.equals("materialPickup") || name.equals("equipment")
                || name.equals("questItems") || name.equals("eventItems")
                || name.equals("remap") || name.equals("emptyMapCombat")
                || name.equals("autoFoodBuy") || name.equals("reconnect")
                || name.equals("changeMapAfterBoss") || name.equals("normalCombat")
                || name.equals("eliteCombat") || name.equals("potential")
                || name.equals("skill") || name.equals("partyCombat")
                || name.equals("antiPk")) return true;
        return false;
    }
}

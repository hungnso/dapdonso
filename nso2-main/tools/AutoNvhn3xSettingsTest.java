public final class AutoNvhn3xSettingsTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        check(AutoNvhn3xProfile.hpThreshold() == 50, "HP threshold must be 50%");
        check(AutoNvhn3xProfile.mpThreshold() == 20, "MP threshold must be 20%");
        check(AutoNvhn3xProfile.foodLevel() == 30, "food level must be 30");
        check(AutoNvhn3xProfile.hpMpPickupLevel() == 10, "HP/MP pickup level must be 10");
        check(AutoNvhn3xProfile.materialPickupLevel() == 3, "material pickup level must be 3");
        check(AutoNvhn3xProfile.equipmentPickupLevel() == 20, "equipment pickup level must be 20");
        check(AutoNvhn3xProfile.enabled("hp"), "HP recovery must be enabled");
        check(AutoNvhn3xProfile.enabled("mp"), "MP recovery must be enabled");
        check(AutoNvhn3xProfile.enabled("food"), "food use must be enabled");
        check(AutoNvhn3xProfile.enabled("support"), "support skills must be enabled");
        check(AutoNvhn3xProfile.enabled("manaShield"), "mana shield must be enabled");
        check(!AutoNvhn3xProfile.enabled("stealth"), "stealth attack must be disabled");
        check(!AutoNvhn3xProfile.enabled("clone"), "clone must be disabled");
        check(AutoNvhn3xProfile.enabled("yen"), "yen pickup must be enabled");
        check(AutoNvhn3xProfile.enabled("hpMpPickup"), "HP/MP pickup must be enabled");
        check(AutoNvhn3xProfile.enabled("materialPickup"), "material pickup must be enabled");
        check(AutoNvhn3xProfile.enabled("equipment"), "equipment pickup must be enabled");
        check(AutoNvhn3xProfile.enabled("questItems"), "quest pickup must be enabled");
        check(AutoNvhn3xProfile.enabled("eventItems"), "event pickup must be enabled");
        check(AutoNvhn3xProfile.enabled("remap"), "remap must be enabled");
        check(AutoNvhn3xProfile.enabled("emptyMapCombat"), "empty-map combat must be enabled");
        check(AutoNvhn3xProfile.enabled("autoFoodBuy"), "auto food purchase must be enabled");
        check(AutoNvhn3xProfile.pauseFoodPurchaseDuringMask(), "food purchase must pause during mask flow");
        check(AutoNvhn3xProfile.enabled("reconnect"), "auto reconnect must be enabled");
        check(AutoNvhn3xProfile.enabled("changeMapAfterBoss"), "boss map change must be enabled");
        check(AutoNvhn3xProfile.enabled("normalCombat"), "normal combat must be enabled");
        check(AutoNvhn3xProfile.enabled("eliteCombat"), "elite combat must be enabled");
        check(!AutoNvhn3xProfile.enabled("chiefCombat"), "chief combat must be disabled");
        check(AutoNvhn3xProfile.enabled("potential"), "potential allocation must be enabled");
        check(AutoNvhn3xProfile.enabled("skill"), "skill allocation must be enabled");
        check(AutoNvhn3xProfile.enabled("partyCombat"), "party combat must be enabled");
        check(AutoNvhn3xProfile.enabled("antiPk"), "anti-PK must be enabled");
        check(!AutoNvhn3xProfile.enabled("weaponOnly"), "weapon-only pickup must be disabled");
    }
}

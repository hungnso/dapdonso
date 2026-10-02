public final class ItemDetailIdPolicy {
    private ItemDetailIdPolicy() {
    }

    public static String appendToName(int typeUI, String name, int templateId) {
        if (typeUI == 3 || typeUI == 4 || typeUI == 5 || typeUI == 39) {
            return name + " [ID: " + templateId + "]";
        }
        return name;
    }
}

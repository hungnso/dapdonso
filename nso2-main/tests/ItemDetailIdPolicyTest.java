public final class ItemDetailIdPolicyTest {
    private static int assertions;

    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void main(String[] args) {
        check("Chuyen tinh thach [ID: 454]".equals(ItemDetailIdPolicy.appendToName(3, "Chuyen tinh thach", 454)), "Bag item detail must append its template ID to the name.");
        check("Ao giap [ID: 454]".equals(ItemDetailIdPolicy.appendToName(5, "Ao giap", 454)), "Equipped item detail must append its template ID to the name.");
        check("Chuyen tinh thach".equals(ItemDetailIdPolicy.appendToName(14, "Chuyen tinh thach", 454)), "Store item detail must not append its template ID.");
        check("Chuyen tinh thach".equals(ItemDetailIdPolicy.appendToName(20, "Chuyen tinh thach", 454)), "Shop item detail must not append its template ID.");
        System.out.println("PASS: " + assertions + " item-detail ID visibility assertions");
    }
}

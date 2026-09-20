public final class Daily3xPolicyTest {
    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void main(String[] args) {
        String[] characters = new String[] {"NhanVat1", null, "NhanVat3"};
        boolean[] completed = new boolean[] {true, false, false};

        check(Daily3xPolicy.nextCharacterIndex(characters, completed, 0) == 2,
                "must skip empty slots and select the next unfinished character");
        check(Daily3xPolicy.nextCharacterIndex(characters, new boolean[] {true, false, true}, 0) == -1,
                "must stop when every existing character is complete");
        check(Daily3xPolicy.isCharacterSlotUsable("NhanVat2"),
                "a named character slot must be usable");
        check(!Daily3xPolicy.isCharacterSlotUsable("   "),
                "a blank character slot must be ignored");
        check(Daily3xPolicy.displayName().equals("NV Hàng ngày 3x"),
                "menu must use the requested name");
    }
}

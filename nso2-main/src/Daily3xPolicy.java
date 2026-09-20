public final class Daily3xPolicy {
    private Daily3xPolicy() {
    }

    public static String displayName() {
        return "NV Hàng ngày 3x";
    }

    public static boolean isCharacterSlotUsable(String name) {
        return name != null && name.trim().length() > 0;
    }

    public static int nextCharacterIndex(String[] names, boolean[] completed, int afterIndex) {
        if (names == null || completed == null) {
            return -1;
        }
        int length = names.length < completed.length ? names.length : completed.length;
        for (int i = afterIndex + 1; i < length; ++i) {
            if (isCharacterSlotUsable(names[i]) && !completed[i]) {
                return i;
            }
        }
        return -1;
    }
}

import java.lang.reflect.Modifier;

/** QLTK checks these members before it opens the MIDlet. No game initialization. */
public final class QltkContractTest {
    public static void main(String[] args) throws Exception {
        Class<?> login = type("LoginScr");
        login.getDeclaredMethod("mgI");
        check(Modifier.isPublic(login.getDeclaredMethod("autoLogin", String.class, String.class).getModifiers()),
                "QLTK must be able to invoke autoLogin");
        fields("GameCanvas", "currentScreen", "menu", "isLoading");
        fields("GameMidlet", "nameServer", "portList", "language", "serverLoginList", "g", "port", "serverLogin");
        fields("SelectCharScr", "name", "indexSelect");
        type("SelectCharScr").getDeclaredMethod("perform", int.class, Object.class);
        type("Char").getDeclaredMethod("getMyChar");
        fields("Char", "cName", "clevel", "xu", "luong", "xuInBox", "arrItemBox", "arrItemBody");
        fields("Item", "template", "upgrade");
        fields("ItemTemplate", "type", "level");
        type("Service").getDeclaredMethod("gI");
        type("Service").getDeclaredMethod("requestItem", int.class);
        System.out.println("QltkContractTest PASS");
    }
    private static Class<?> type(String name) throws ClassNotFoundException {
        return Class.forName(name, false, QltkContractTest.class.getClassLoader());
    }
    private static void fields(String name, String... members) throws Exception {
        for (String member : members) type(name).getDeclaredField(member);
    }
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}

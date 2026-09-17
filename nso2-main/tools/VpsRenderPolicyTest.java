public final class VpsRenderPolicyTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        check(VpsRenderPolicy.menuLabel(false).equals("Che do tiet kiem (1-31): Tat"),
                "menu must show the lightweight mode as off");
        check(VpsRenderPolicy.menuLabel(true).equals("Che do tiet kiem (1-31): Bat"),
                "menu must show the lightweight mode as on");
    }
}

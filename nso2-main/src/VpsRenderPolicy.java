/** Label policy for the persisted lightweight-render toggle. */
public final class VpsRenderPolicy {
    private VpsRenderPolicy() {
    }

    public static String menuLabel(boolean enabled) {
        return "Che do tiet kiem (1-31): " + (enabled ? "Bat" : "Tat");
    }
}

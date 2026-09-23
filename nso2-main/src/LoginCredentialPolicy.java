/** Normalizes login fields without changing case-sensitive passwords. */
public final class LoginCredentialPolicy {
    private LoginCredentialPolicy() {
    }

    public static String username(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    public static String password(String value) {
        return value == null ? "" : value.trim();
    }
}

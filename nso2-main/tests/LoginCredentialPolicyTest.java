public final class LoginCredentialPolicyTest {
    public static void main(String[] args) {
        check("member".equals(LoginCredentialPolicy.username("  Member  ")), "normalize username");
        check("MixedCase123".equals(LoginCredentialPolicy.password(" MixedCase123 ")), "preserve password case");
        check("".equals(LoginCredentialPolicy.username(null)), "null username");
        check("".equals(LoginCredentialPolicy.password(null)), "null password");
        System.out.println("LoginCredentialPolicyTest PASS");
    }
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}

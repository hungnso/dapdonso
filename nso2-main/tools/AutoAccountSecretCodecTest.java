public final class AutoAccountSecretCodecTest {
    public static void main(String[] args) {
        String[] values = new String[]{"PassWord01!", "", "mật khẩu"};
        for (int i = 0; i < values.length; ++i) {
            String encoded = AutoAccountSecretCodec.encode(values[i]);
            if (values[i].equals(encoded) && values[i].length() > 0) {
                throw new AssertionError("secret must not be stored as plaintext");
            }
            if (!values[i].equals(AutoAccountSecretCodec.decode(encoded))) {
                throw new AssertionError("secret round-trip failed at index " + i);
            }
        }
        if (!"PassWord01!".equals(LoginCredentialPolicy.password("PassWord01!"))) {
            throw new AssertionError("password case must be preserved");
        }
        System.out.println("AutoAccountSecretCodecTest passed");
    }
}

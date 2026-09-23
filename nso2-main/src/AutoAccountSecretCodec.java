/** Lightweight local obfuscation for account passwords stored in RMS. */
public final class AutoAccountSecretCodec {
    private static final int KEY = 0x5A;

    private AutoAccountSecretCodec() {
    }

    public static String encode(String value) {
        if (value == null) value = "";
        try {
            byte[] bytes = value.getBytes("UTF-8");
            StringBuffer out = new StringBuffer(bytes.length * 2 + 3);
            out.append("x1:");
            for (int i = 0; i < bytes.length; ++i) {
                int b = (bytes[i] ^ KEY) & 255;
                if (b < 16) out.append('0');
                out.append(Integer.toHexString(b));
            }
            return out.toString();
        } catch (Exception error) {
            return "x1:";
        }
    }

    public static String decode(String value) {
        if (value == null || !value.startsWith("x1:")) return value == null ? "" : value;
        try {
            String hex = value.substring(3);
            byte[] bytes = new byte[hex.length() / 2];
            for (int i = 0; i < bytes.length; ++i) {
                bytes[i] = (byte)((Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16) ^ KEY) & 255);
            }
            return new String(bytes, "UTF-8");
        } catch (Exception error) {
            return "";
        }
    }
}

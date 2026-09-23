/** One account slot owned by a single emulator tab. */
public final class AutoAccountEntry {
    public static final int PENDING = 0;
    public static final int RUNNING = 1;
    public static final int DONE = 2;
    public static final int LOGIN_ERROR = 3;

    public String username;
    public String password;
    public boolean enabled;
    public int status;

    public AutoAccountEntry(String username, String password, boolean enabled, int status) {
        this.username = username == null ? "" : username;
        this.password = password == null ? "" : password;
        this.enabled = enabled;
        this.status = status;
    }
}

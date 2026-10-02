import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.microedition.midlet.MIDlet;
import org.microemu.MIDletBridge;
import org.microemu.app.Headless;

/** Checks actual MIDlet state; MICRO_NST suppresses the game's stdout. */
public final class LiteBootProbe {
    public static void main(String[] args) {
        try {
            Headless.main(new String[] { args[0] });
            long deadline = System.currentTimeMillis() + 10000;
            while (System.currentTimeMillis() < deadline) {
                MIDlet midlet = MIDletBridge.getCurrentMIDlet();
                if (midlet != null && "GameMidlet".equals(midlet.getClass().getSimpleName())) {
                    Class<?> canvas = Class.forName("GameCanvas", false, midlet.getClass().getClassLoader());
                    Object screen = canvas.getField("currentScreen").get(null);
                    // Empty RMS first shows language selection after creating LoginScr.
                    if (canvas.getField("loginScr").get(null) != null && screen != null
                            && ("LoginScr".equals(screen.getClass().getSimpleName())
                            || "SelectServerScr".equals(screen.getClass().getSimpleName())
                            || "LanguageScr".equals(screen.getClass().getSimpleName()))) {
                        Files.write(Paths.get(args[1]), "MIDLET_LOGIN_READY".getBytes(StandardCharsets.UTF_8));
                        return;
                    }
                }
                Thread.sleep(100);
            }
            throw new IllegalStateException("MIDlet did not initialize LoginScr and reach its start screen");
        } catch (Throwable error) {
            error.printStackTrace();
            System.exit(1);
        }
    }
}

import java.io.File;
import java.util.jar.JarFile;

/** Guards the release artifact: Menu's quiz policy must be present in the JAR. */
public final class AutoNvcQuizPackageTest {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("expected JAR path");
        JarFile jar = new JarFile(new File(args[0]));
        try {
            if (jar.getJarEntry("AutoNvcQuizPolicy.class") == null) {
                throw new AssertionError("release JAR is missing AutoNvcQuizPolicy.class");
            }
            if (jar.getJarEntry("Menu.class") == null) {
                throw new AssertionError("release JAR is missing Menu.class");
            }
        } finally {
            jar.close();
        }
    }
}

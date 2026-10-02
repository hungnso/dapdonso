import java.util.concurrent.atomic.AtomicReference;

public final class AutoCoordinateTraversalTest {
    public static void main(String[] args) throws Exception {
        NSOT_MOB.n.removeAllElements();
        NSOT_MOB.o.removeAllElements();
        NSOT_MOB.n.addElement(Integer.valueOf(100));
        NSOT_MOB.o.addElement(Integer.valueOf(100));
        GameScr.vMobAttack.removeAllElements();
        Char.dz = false;
        Char.getMyChar().nClass = new NClass();
        final FakeAuto auto = new FakeAuto();
        final AtomicReference<Throwable> failure = new AtomicReference<Throwable>();
        Thread emptyScan = new Thread(new Runnable() {
            public void run() {
                try { auto.scan(); } catch (Throwable error) { failure.set(error); }
            }
        });
        emptyScan.setDaemon(true);
        emptyScan.start();
        emptyScan.join(1000);
        check(!emptyScan.isAlive(), "coordinate scan with no live mob must return after one pass");
        check(failure.get() == null, "scan failed: " + failure.get());

        NSOT_MOB.n.removeAllElements();
        NSOT_MOB.o.removeAllElements();
        auto.scan();
        Thread.currentThread().interrupt();
        try {
            auto.scan();
            check(Thread.currentThread().isInterrupted(), "scan preserves cancellation");
        } finally { Thread.interrupted(); }
        System.out.println("AutoCoordinateTraversalTest PASS");
    }

    private static final class FakeAuto extends Auto {
        public void update() { }
        void scan() { a(4, false); }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

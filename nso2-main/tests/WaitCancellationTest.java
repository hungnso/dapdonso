public final class WaitCancellationTest {
    public static void main(String[] args) {
        Class_cl.a();
        Thread.currentThread().interrupt();
        try {
            Class_cl.b();
            check(Thread.currentThread().isInterrupted(), "map wait must preserve cancellation");
            Class_cl.d();
            check(Thread.currentThread().isInterrupted(), "response wait must preserve cancellation");
            check(!TileMap.k(-1), "cancelled route must return without loading map graph");
            check(Thread.currentThread().isInterrupted(), "route must preserve cancellation");
        } finally { Thread.interrupted(); }
        System.out.println("WaitCancellationTest PASS");
    }
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

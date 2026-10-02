import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class AutoWorkerLifecycleTest {
    public static void main(String[] args) throws Exception {
        neverOverlapsAnOldBlockedWorker();
        doesNotBuildResumeCycles();
        System.out.println("AutoWorkerLifecycleTest PASS");
    }

    private static void neverOverlapsAnOldBlockedWorker() throws Exception {
        final AutoWorkerLifecycle lifecycle = new AutoWorkerLifecycle();
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final CountDownLatch nextEntered = new CountDownLatch(1);
        final AtomicInteger concurrent = new AtomicInteger();
        final AtomicInteger oldStillOwns = new AtomicInteger();
        Runnable oldTask = new Runnable() {
            public void run() {
                concurrent.incrementAndGet();
                entered.countDown();
                boolean done = false;
                while (!done) {
                    try { release.await(); done = true; }
                    catch (InterruptedException ignored) { }
                }
                if (lifecycle.isCurrent()) oldStillOwns.incrementAndGet();
                concurrent.decrementAndGet();
            }
        };
        Thread old = lifecycle.start(oldTask);
        check(entered.await(2, TimeUnit.SECONDS), "old worker entered");
        check(lifecycle.start(oldTask) == old, "repeated start must reuse active worker");
        lifecycle.stop();
        Thread next = lifecycle.start(new Runnable() {
            public void run() {
                if (concurrent.incrementAndGet() != 1) oldStillOwns.incrementAndGet();
                nextEntered.countDown();
                concurrent.decrementAndGet();
            }
        });
        try {
            check(!nextEntered.await(30, TimeUnit.MILLISECONDS), "new auto must not overlap blocked old update");
        } finally {
            release.countDown();
            old.join(2000);
            next.join(2000);
            lifecycle.stop();
        }
        check(!old.isAlive() && !next.isAlive(), "both workers terminated");
        check(oldStillOwns.get() == 0, "old worker loses ownership and updates never overlap");
        check(!lifecycle.isActive(), "completed worker must release ownership");
    }

    private static void doesNotBuildResumeCycles() {
        FakeAuto main = new FakeAuto();
        FakeAuto daily = new FakeAuto();
        FakeAuto child = new FakeAuto();
        Auto active = AutoResumeStack.push(null, main);
        active = AutoResumeStack.push(active, daily);
        active = AutoResumeStack.push(active, child);
        check(AutoResumeStack.push(active, child) == child && child.l == daily, "same child push is idempotent");
        check(AutoResumeStack.push(active, main) == child && main.l == null, "ancestor push must not create a cycle");
        check(AutoResumeStack.pop(child) == daily && child.l == null, "pop releases finished child");
        check(AutoResumeStack.pop(daily) == main && daily.l == null, "daily resumes original main auto");
        check(AutoResumeStack.pop(null) == null, "empty stack pop is safe");
    }

    private static final class FakeAuto extends Auto {
        public void update() { }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

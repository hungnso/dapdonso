public final class LiteRuntimePolicyTest {
    public static void main(String[] args) {
        LiteRuntimePolicy schedule = new LiteRuntimePolicy(1000L);
        check(schedule.tryAcquire(1000), "schedule starts");
        for (long now = 1001; now < 2000; now++) {
            check(!schedule.tryAcquire(now), "repeated ticks must not repeat background work");
        }
        check(schedule.tryAcquire(2000), "next second runs");
        schedule.reset();
        check(schedule.tryAcquire(2001), "config reset allows immediate check");
        check(schedule.tryAcquire(500), "clock rollback recovers gate");
        check(LiteRuntimePolicy.isDue(1000, 0, 1000), "first check runs immediately");
        check(!LiteRuntimePolicy.isDue(1999, 1000, 1000), "scan waits a full second");
        check(LiteRuntimePolicy.isDue(2000, 1000, 1000), "scan runs at one second");
        check(LiteRuntimePolicy.isDue(900, 1000, 1000), "clock rollback must not freeze scheduler");
        check(!LiteRuntimePolicy.shouldPaint(true, true, false, false, 1199, 1000), "idle paint is capped");
        check(LiteRuntimePolicy.shouldPaint(true, true, false, false, 1200, 1000), "idle paint resumes after 200ms");
        check(LiteRuntimePolicy.shouldPaint(true, true, false, true, 1001, 1000), "input paints immediately");
        check(LiteRuntimePolicy.shouldPaint(true, true, true, false, 1001, 1000), "dialogs paint immediately");
        check(LiteRuntimePolicy.shouldPaint(false, true, false, false, 1001, 1000), "full rendering stays available");
        check(LiteRuntimePolicy.shouldPaint(true, false, false, false, 1001, 1000), "login/menu screens paint normally");
        System.out.println("LiteRuntimePolicyTest PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

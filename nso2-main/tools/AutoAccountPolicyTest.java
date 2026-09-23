public final class AutoAccountPolicyTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        check(AutoAccountPolicy.MAX_ACCOUNTS == 20, "maximum account count must be 20");
        check(!AutoAccountPolicy.isValidUsername(null), "null username must be rejected");
        check(!AutoAccountPolicy.isValidUsername("  "), "blank username must be rejected");
        check(AutoAccountPolicy.isValidUsername("acc01"), "normal username must be accepted");

        AutoAccountEntry[] entries = new AutoAccountEntry[]{
                new AutoAccountEntry("done", "p", true, AutoAccountEntry.DONE),
                new AutoAccountEntry("disabled", "p", false, AutoAccountEntry.PENDING),
                new AutoAccountEntry("next", "p", true, AutoAccountEntry.PENDING)
        };
        check(AutoAccountPolicy.nextIndex(entries, -1) == 2, "must select first pending enabled account");
        check(AutoAccountPolicy.nextIndex(entries, 2) == -1, "must stop after final account");
        check(AutoAccountPolicy.hasPending(entries), "pending account before current slot must prevent completion");
        entries[2].status = AutoAccountEntry.DONE;
        check(!AutoAccountPolicy.hasPending(entries), "all enabled accounts done must allow completion");
        check(AutoAccountPolicy.recoverStatus(AutoAccountEntry.RUNNING) == AutoAccountEntry.PENDING,
                "running account must resume as pending");
        check(AutoAccountPolicy.recoverStatus(AutoAccountEntry.DONE) == AutoAccountEntry.DONE,
                "done account must remain done");
        check(AutoAccountPolicy.shouldRetry(0), "retry 0 must be allowed");
        check(AutoAccountPolicy.shouldRetry(1), "retry 1 must be allowed");
        check(AutoAccountPolicy.shouldRetry(2), "retry 2 must be allowed");
        check(!AutoAccountPolicy.shouldRetry(3), "retry 3 must pause rotation");
        System.out.println("AutoAccountPolicyTest passed");
    }
}

public final class AutoAccountRotationPolicyTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        AutoAccountEntry[] entries = new AutoAccountEntry[]{
                new AutoAccountEntry("a", "p", true, AutoAccountEntry.DONE),
                new AutoAccountEntry("b", "p", true, AutoAccountEntry.PENDING),
                new AutoAccountEntry("c", "p", false, AutoAccountEntry.PENDING)
        };
        check(AutoAccountRotationPolicy.firstIndex(entries, 0) == 1,
                "must select next enabled pending account");
        check(AutoAccountRotationPolicy.firstIndex(entries, 1) == -1,
                "must finish when no pending account remains");
        check(AutoAccountRotationPolicy.nextPhase(AutoAccountRotationPolicy.MARK_DONE, false)
                == AutoAccountRotationPolicy.LOGOUT, "completed account must logout before next");
        check(AutoAccountRotationPolicy.nextPhase(AutoAccountRotationPolicy.MARK_DONE, true)
                == AutoAccountRotationPolicy.COMPLETE, "final account must complete");
        check(AutoAccountRotationPolicy.loginRetryPhase(0) == AutoAccountRotationPolicy.ENSURE_LOGIN,
                "first login failure must retry");
        check(AutoAccountRotationPolicy.loginRetryPhase(2) == AutoAccountRotationPolicy.ENSURE_LOGIN,
                "third login attempt must be allowed");
        check(AutoAccountRotationPolicy.loginRetryPhase(3) == AutoAccountRotationPolicy.PAUSED,
                "fourth failure must pause");
        check(AutoAccountRotationPolicy.recoverPhase(AutoAccountRotationPolicy.RUN_NVHN3X)
                == AutoAccountRotationPolicy.ENSURE_LOGIN,
                "restart during child run must login current account again");
        System.out.println("AutoAccountRotationPolicyTest passed");
    }
}

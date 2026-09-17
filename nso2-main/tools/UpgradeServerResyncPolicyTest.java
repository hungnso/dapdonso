public final class UpgradeServerResyncPolicyTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        check(!UpgradeServerResyncPolicy.shouldReequipAfterUnacknowledgedUpgrade(5999L, false),
                "must give the server six seconds before re-equipping");
        check(UpgradeServerResyncPolicy.shouldReequipAfterUnacknowledgedUpgrade(6000L, false),
                "must re-equip once when the server has not advanced NV12 after six seconds");
        check(!UpgradeServerResyncPolicy.shouldReequipAfterUnacknowledgedUpgrade(10000L, true),
                "must not loop re-equip attempts for the same upgrade step");
    }
}

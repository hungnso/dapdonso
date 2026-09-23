public final class AutoAccountRotationSmokeTest {
    public static void main(String[] args) {
        AutoAccountEntry[] entries = new AutoAccountEntry[20];
        for (int i = 0; i < entries.length; ++i) {
            entries[i] = new AutoAccountEntry("acc" + i, "pass", true,
                    i == 19 ? AutoAccountEntry.PENDING : AutoAccountEntry.DONE);
        }
        if (AutoAccountRotationPolicy.firstIndex(entries, -1) != 19) {
            throw new AssertionError("smoke: must select the only pending account");
        }
        if (AutoAccountRotationPolicy.nextPhase(AutoAccountRotationPolicy.MARK_DONE, true)
                != AutoAccountRotationPolicy.COMPLETE) {
            throw new AssertionError("smoke: final account must complete rotation");
        }
        if (AutoAccountRotationPolicy.loginRetryPhase(3) != AutoAccountRotationPolicy.PAUSED) {
            throw new AssertionError("smoke: login retry limit must pause");
        }
        System.out.println("AutoAccountRotationSmokeTest passed");
    }
}

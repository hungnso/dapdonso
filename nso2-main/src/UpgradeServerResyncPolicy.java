public final class UpgradeServerResyncPolicy {
    private static final long SERVER_ACK_TIMEOUT = 6000L;

    private UpgradeServerResyncPolicy() {
    }

    public static boolean shouldReequipAfterUnacknowledgedUpgrade(long elapsedMillis, boolean alreadyAttempted) {
        return !alreadyAttempted && elapsedMillis >= SERVER_ACK_TIMEOUT;
    }
}

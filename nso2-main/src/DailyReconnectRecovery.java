public final class DailyReconnectRecovery {
    private static boolean resumeDailyAfterReconnect;
    private static boolean hangExitReconnectPending;
    private static Auto interruptedAutoBeforeReconnect;

    private DailyReconnectRecovery() {
    }

    public static void captureBeforeDisconnect() {
        Auto coordinator = findCoordinator(NSOT_MOB.b);
        if (coordinator != null || isDailyChain(NSOT_MOB.b)) {
            resumeDailyAfterReconnect = true;
            // Preserve the exact auto that Daily interrupted (VDMQ/1-70/etc.).
            // This matters for the new Hang step because exiting Hang uses a
            // reconnect before Daily/Ta Thu start.
            interruptedAutoBeforeReconnect = coordinator == null ? null : coordinator.l;
            System.out.println("[DAILY][RECONNECT] captured active daily chain"
                    + (interruptedAutoBeforeReconnect == null ? ""
                    : " resume=" + interruptedAutoBeforeReconnect.getClass().getName()));
        }
    }

    public static void restoreBeforeAutoThreadStarts() {
        if (!resumeDailyAfterReconnect) {
            return;
        }
        resumeDailyAfterReconnect = false;

        if (isDailyChain(NSOT_MOB.b)) {
            System.out.println("[DAILY][RECONNECT] existing daily chain retained");
            interruptedAutoBeforeReconnect = null;
            return;
        }

        Auto interrupted = interruptedAutoBeforeReconnect;
        interruptedAutoBeforeReconnect = null;
        if (interrupted != null) {
            // startAutoDaily() pushes a fresh coordinator on top of NSOT_MOB.b.
            // Restore this reference first so finishAndResume() pops back to
            // exactly the VDMQ/1-70 instance that was running before Daily.
            NSOT_MOB.b = interrupted;
            System.out.println("[DAILY][RECONNECT] restored interrupted auto="
                    + interrupted.getClass().getName());
        }

        System.out.println("[DAILY][RECONNECT] restoring daily before Auto 1-70");
        GameScr.addChatPopup("Dang nhap lai -> tiep tuc NV hang ngay");
        NSOT_MOB.mod_nst.startAutoDaily(true);
    }

    public static void markHangExitReconnect() {
        hangExitReconnectPending = true;
    }

    public static void cancelHangExitReconnect() {
        hangExitReconnectPending = false;
    }

    public static boolean consumeHangExitReconnect() {
        if (!hangExitReconnectPending) {
            return false;
        }
        hangExitReconnectPending = false;
        return true;
    }

    private static Auto findCoordinator(Auto auto) {
        int depth = 0;
        while (auto != null && depth++ < 16) {
            if (auto instanceof AutoDailyCoordinator) {
                return auto;
            }
            auto = auto.l;
        }
        return null;
    }

    private static boolean isDailyChain(Auto auto) {
        int depth = 0;
        while (auto != null && depth++ < 16) {
            if (auto instanceof TaskAuto || auto instanceof TaskTaThuAuto
                    || auto instanceof AutoDailyCoordinator) {
                return true;
            }
            auto = auto.l;
        }
        return false;
    }
}

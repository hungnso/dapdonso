import java.util.Calendar;

/** Runs one tab's account list around the existing single-character NVHN flow. */
public final class AutoAccountRotationCoordinator extends Auto implements AutoNvhn3xCompletionListener {
    private int state;
    private AutoAccountEntry[] entries;
    private int currentIndex = -1;
    private int retries;
    private long lastLoginAttempt;
    private boolean settingsStarted;

    public static AutoAccountRotationCoordinator active() {
        Auto current = NSOT_MOB.b;
        int depth = 0;
        while (current != null && depth++ < 8) {
            if (current instanceof AutoAccountRotationCoordinator) {
                return (AutoAccountRotationCoordinator) current;
            }
            current = current.l;
        }
        return null;
    }

    public static boolean shouldResume() {
        if (!AutoAccountPanel.isRotationEnabled()) return false;
        AutoAccountEntry[] entries = AutoAccountStore.load();
        return AutoAccountPolicy.hasPending(entries);
    }

    public final void g() {
        super.g();
        state = AutoAccountRotationPolicy.LOAD;
        entries = null;
        currentIndex = -1;
        retries = 0;
        lastLoginAttempt = 0L;
        settingsStarted = false;
    }

    public final void update() {
        switch (state) {
            case AutoAccountRotationPolicy.LOAD:
                loadCheckpoint();
                return;
            case AutoAccountRotationPolicy.ENSURE_LOGIN:
                ensureLogin();
                return;
            case AutoAccountRotationPolicy.RUN_NVHN3X:
                launchChild();
                return;
            case AutoAccountRotationPolicy.WAIT_NVHN3X:
                return;
            case AutoAccountRotationPolicy.MARK_DONE:
                markDone();
                return;
            case AutoAccountRotationPolicy.LOGOUT:
                logoutForNextAccount();
                return;
            case AutoAccountRotationPolicy.NEXT_ACCOUNT:
                chooseNextAccount();
                return;
            case AutoAccountRotationPolicy.COMPLETE:
                finishRotation();
                return;
            case AutoAccountRotationPolicy.PAUSED:
                return;
            default:
                state = AutoAccountRotationPolicy.LOAD;
        }
    }

    public final void onNvhn3xCompleted() {
        if (state == AutoAccountRotationPolicy.WAIT_NVHN3X) {
            state = AutoAccountRotationPolicy.MARK_DONE;
        }
    }

    public final void stop() {
        if (entries != null && currentIndex >= 0 && currentIndex < entries.length) {
            AutoAccountStore.save(entries);
            AutoAccountStore.saveCheckpoint(currentIndex, state, retries, today());
        }
        if (settingsStarted) AutoNvhn3xSettings.restore();
        settingsStarted = false;
    }

    public final String toString() {
        int completed = 0;
        int total = 0;
        if (entries != null) {
            int displayCompleted = 0;
            int displayTotal = 0;
            for (int i = 0; i < entries.length; ++i) {
                AutoAccountEntry entry = entries[i];
                if (entry != null && entry.enabled && AutoAccountPolicy.isValidUsername(entry.username)) {
                    ++displayTotal;
                    if (entry.status == AutoAccountEntry.DONE) ++displayCompleted;
                }
            }
            return "\u0110\u00e3 ho\u00e0n th\u00e0nh " + displayCompleted + "/" + displayTotal + " acc";
        }
        if (entries != null) {
            for (int i = 0; i < entries.length; ++i) {
                AutoAccountEntry entry = entries[i];
                if (entry != null && entry.enabled && AutoAccountPolicy.isValidUsername(entry.username)) {
                    ++total;
                    if (entry.status == AutoAccountEntry.DONE) ++completed;
                }
            }
        }
        return "Đã hoàn thành " + completed + "/" + total + " acc";
    }

    private void loadCheckpoint() {
        entries = AutoAccountStore.load();
        int saved = AutoAccountStore.checkpointIndex();
        int savedPhase = AutoAccountStore.checkpointPhase();
        if (savedPhase == AutoAccountRotationPolicy.COMPLETE) {
            if (!AutoAccountPolicy.hasPending(entries)) {
                state = AutoAccountRotationPolicy.COMPLETE;
                return;
            }
            // Recover old checkpoints that were marked complete by the
            // previous forward-only completion bug.
            saved = -1;
        }
        if (saved >= 0 && saved < entries.length) {
            currentIndex = saved;
            entries[currentIndex].status = AutoAccountPolicy.recoverStatus(entries[currentIndex].status);
            if (entries[currentIndex].status == AutoAccountEntry.DONE) {
                currentIndex = AutoAccountRotationPolicy.firstIndex(entries, currentIndex);
            }
        } else {
            currentIndex = AutoAccountRotationPolicy.firstIndex(entries, AutoAccountStore.startIndex() - 1);
        }
        if (currentIndex < 0) {
            state = AutoAccountRotationPolicy.COMPLETE;
            return;
        }
        retries = Math.max(0, AutoAccountStore.checkpointRetries());
        entries[currentIndex].status = AutoAccountEntry.RUNNING;
        AutoAccountStore.save(entries);
        AutoAccountStore.saveCheckpoint(currentIndex, AutoAccountRotationPolicy.ENSURE_LOGIN,
                retries, today());
        state = AutoAccountRotationPolicy.ENSURE_LOGIN;
    }

    private void ensureLogin() {
        if (entries == null || currentIndex < 0 || currentIndex >= entries.length) {
            state = AutoAccountRotationPolicy.COMPLETE;
            return;
        }
        AutoAccountEntry entry = entries[currentIndex];
        if (GameCanvas.currentScreen instanceof SelectCharScr) {
            SelectCharScr.gI().autoSelectDefault();
            return;
        }
        if (GameCanvas.currentScreen == GameScr.instance
                && entry.username.equalsIgnoreCase(SelectServerScr.uname)) {
            state = AutoAccountRotationPolicy.RUN_NVHN3X;
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastLoginAttempt < 5000L) return;
        if (!AutoAccountPolicy.shouldRetry(retries)) {
            entry.status = AutoAccountEntry.LOGIN_ERROR;
            AutoAccountStore.save(entries);
            state = AutoAccountRotationPolicy.PAUSED;
            GameScr.addChatPopup("Auto tai khoan: login loi acc " + (currentIndex + 1));
            return;
        }
        if (GameCanvas.loginScr == null) return;
        ++retries;
        lastLoginAttempt = now;
        AutoAccountStore.saveCheckpoint(currentIndex, AutoAccountRotationPolicy.ENSURE_LOGIN,
                retries, today());
        Class_cl.ac();
        Session_ME oldSession = Session_ME.getInstance();
        if (oldSession != null) {
            // Stop the reconnect worker, but do not call Session_ME.e(): that
            // method starts Class_cj and races the explicit account login.
            Session_ME.isCancel = false;
            oldSession.cleanNetwork();
        }
        GameCanvas.loginScr.autoLogin(entry.username, entry.password);
    }

    private void launchChild() {
        if (!settingsStarted) {
            AutoNvhn3xSettings.begin();
            settingsStarted = true;
        }
        AutoDailyCoordinator child = new AutoDailyCoordinator(true, true, null, this);
        child.g();
        state = AutoAccountRotationPolicy.WAIT_NVHN3X;
        AutoAccountStore.saveCheckpoint(currentIndex, state, retries, today());
        NSOT_MOB.a(child);
    }

    private void markDone() {
        entries[currentIndex].status = AutoAccountEntry.DONE;
        AutoAccountStore.save(entries);
        AutoNvhn3xSettings.restore();
        settingsStarted = false;
        boolean allDone = !AutoAccountPolicy.hasPending(entries);
        state = AutoAccountRotationPolicy.nextPhase(AutoAccountRotationPolicy.MARK_DONE, allDone);
        AutoAccountStore.saveCheckpoint(currentIndex, state, 0, today());
    }

    private void logoutForNextAccount() {
        // Keep the NST worker alive: this coordinator is its parent and must
        // continue ticking after the login screen appears.
        Class_cl.ac();
        Session_ME session = Session_ME.getInstance();
        if (session != null) {
            // The next account will establish its own connection through
            // LoginScr.autoLogin. Starting Session_ME.e() here would launch
            // a second reconnect/login thread and can bounce back to login.
            Session_ME.isCancel = false;
            session.cleanNetwork();
        }
        if (GameCanvas.loginScr != null) GameCanvas.loginScr.switchToMe();
        state = AutoAccountRotationPolicy.NEXT_ACCOUNT;
    }

    private void chooseNextAccount() {
        currentIndex = AutoAccountRotationPolicy.firstIndex(entries, currentIndex);
        if (currentIndex < 0) {
            // A restart/checkpoint can leave unfinished slots before the
            // current slot; wrap once instead of declaring completion.
            currentIndex = AutoAccountRotationPolicy.firstIndex(entries, -1);
        }
        if (currentIndex < 0) {
            state = AutoAccountRotationPolicy.COMPLETE;
            return;
        }
        retries = 0;
        entries[currentIndex].status = AutoAccountEntry.RUNNING;
        AutoAccountStore.save(entries);
        AutoAccountStore.saveCheckpoint(currentIndex, AutoAccountRotationPolicy.ENSURE_LOGIN,
                retries, today());
        state = AutoAccountRotationPolicy.ENSURE_LOGIN;
    }

    private void finishRotation() {
        AutoAccountStore.saveCheckpoint(currentIndex, AutoAccountRotationPolicy.COMPLETE, 0, today());
        GameScr.addChatPopup("Auto tai khoan: da xong danh sach");
        if (NSOT_MOB.b == this) NSOT_MOB.d();
        state = AutoAccountRotationPolicy.PAUSED;
    }

    private void showProgressPopup() {
        int completed = 0;
        int total = 0;
        if (entries != null) {
            for (int i = 0; i < entries.length; ++i) {
                AutoAccountEntry entry = entries[i];
                if (entry != null && entry.enabled && AutoAccountPolicy.isValidUsername(entry.username)) {
                    ++total;
                    if (entry.status == AutoAccountEntry.DONE) ++completed;
                }
            }
        }
        GameScr.addChatPopup("Đã hoàn thành " + completed + "/" + total + " acc");
    }

    private static String today() {
        Calendar now = Calendar.getInstance();
        return now.get(Calendar.YEAR) + "-" + now.get(Calendar.DAY_OF_YEAR);
    }
}

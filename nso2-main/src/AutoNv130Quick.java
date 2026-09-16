/** One-time level-1 preparation, then delegates quest/combat to AutoNhiemVuChinh. */
public final class AutoNv130Quick extends Auto {
    private static final int REWARD = 0, WAIT_REWARD = 1, ATTENDANCE = 2, WAIT_ATTENDANCE = 3;
    private int state;
    private long stateAt;

    public final void g() {
        super.g(); state = REWARD; stateAt = 0L;
    }
    public final void update() {
        Char me = Char.getMyChar();
        if (me == null) return;
        if (state == REWARD) { ActivityQuickClaim.startQuickReward(); state = WAIT_REWARD; stateAt = System.currentTimeMillis(); return; }
        if (state == WAIT_REWARD) { if (System.currentTimeMillis() - stateAt > 1000L && !ActivityQuickClaim.isBusy()) state = ATTENDANCE; return; }
        if (state == ATTENDANCE) { ActivityQuickClaim.startQuickAttendance(); state = WAIT_ATTENDANCE; stateAt = System.currentTimeMillis(); return; }
        if (state == WAIT_ATTENDANCE && System.currentTimeMillis() - stateAt > 1000L && !ActivityQuickClaim.isBusy()) {
            if (AutoNv130QuickPolicy.shouldRunStarterPreparation(me.clevel)) {
                GameScr.addChatPopup("Hoan thanh nhiem vu diet coc truoc khi mua do Goosho");
            }
            NSOT_MOB.mod_nst.startAutoNhiemVuChinhQuick(AutoNv130QuickPolicy.shouldRunStarterPreparation(me.clevel));
        }
    }
    public final String toString() { return "Auto NV nhanh Lv1-30 - " + AutoNv130QuickPolicy.startupStepName(state); }
}

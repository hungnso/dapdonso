/** Runs Auto NVHN 3x sequentially for character slots 0, 1, and 2. */
public final class AutoNvhn3xBatchCoordinator extends Auto {
    private static final int LOAD = 0;
    private static final int SELECT_SLOT = 1;
    private static final int WAIT_LOGIN = 2;
    private static final int WAIT_CHILD = 3;
    private static final int LOGOUT = 4;
    private static final int DONE = 5;

    private int state;
    private int targetSlot;
    private String targetName;
    private AutoNvhn3xBatchState checkpoint;

    public final void g() {
        super.g();
        AutoNvhn3xSettings.begin();
        state = LOAD;
        targetSlot = -1;
        targetName = null;
        checkpoint = null;
    }

    public final void update() {
        switch (state) {
            case LOAD:
                checkpoint = AutoNvhn3xBatchState.load();
                state = SELECT_SLOT;
                return;
            case SELECT_SLOT:
                if (!loadTargetSlot()) return;
                if (isCurrentCharacter(targetName)) {
                    launchChild();
                } else {
                    checkpoint.phase = WAIT_LOGIN;
                    checkpoint.save();
                    SelectCharScr.k = targetName;
                    reconnectForNextCharacter();
                    state = WAIT_LOGIN;
                }
                return;
            case WAIT_LOGIN:
                if (isCurrentCharacter(targetName) && GameCanvas.currentScreen == GameScr.instance) {
                    launchChild();
                }
                return;
            case WAIT_CHILD:
                return;
            case LOGOUT:
                AutoNvhn3xSettings.restore();
                checkpoint.clear();
                GameScr.addChatPopup("Auto NVHN 3x da xong 3/3 nhan vat");
                Class_cl.ac();
                Session_ME session = Session_ME.getInstance();
                if (session != null) {
                    session.cleanNetwork();
                    session.e();
                }
                GameCanvas.loginScr.switchToMe();
                state = DONE;
                return;
            default:
                return;
        }
    }

    public final String toString() {
        return "Auto NVHN 3x - slot " + (targetSlot < 0 ? "?" : targetSlot);
    }

    public void onCharacterCompleted() {
        if (checkpoint == null || targetSlot < 0) return;
        checkpoint.doneMask = AutoNvhn3xBatchPolicy.markDone(checkpoint.doneMask, targetSlot);
        checkpoint.currentSlot = targetSlot;
        checkpoint.phase = AutoNvhn3xBatchPolicy.allDone(checkpoint.doneMask, occupiedSlots())
                ? LOGOUT : SELECT_SLOT;
        checkpoint.save();
        state = checkpoint.phase;
    }

    private boolean loadTargetSlot() {
        boolean[] occupied = occupiedSlots();
        if (occupied == null) return false;
        int next = AutoNvhn3xBatchPolicy.nextSlot(checkpoint.doneMask, occupied);
        if (next < 0) {
            state = LOGOUT;
            checkpoint.phase = LOGOUT;
            checkpoint.save();
            return false;
        }
        String[] names = SelectCharScr.gI().name;
        if (names == null || next >= names.length || names[next] == null || names[next].length() == 0) {
            return false;
        }
        targetSlot = next;
        targetName = names[next];
        checkpoint.currentSlot = next;
        checkpoint.phase = SELECT_SLOT;
        checkpoint.save();
        return true;
    }

    private boolean[] occupiedSlots() {
        String[] names = SelectCharScr.gI().name;
        if (names == null) return null;
        boolean[] occupied = new boolean[3];
        for (int i = 0; i < occupied.length && i < names.length; ++i) {
            occupied[i] = names[i] != null && names[i].length() > 0;
        }
        return occupied;
    }

    private boolean isCurrentCharacter(String name) {
        Char me = Char.getMyChar();
        return me != null && name != null && name.equals(me.cName);
    }

    private void launchChild() {
        AutoDailyCoordinator child = new AutoDailyCoordinator(true, true, this);
        child.g();
        state = WAIT_CHILD;
        checkpoint.phase = WAIT_CHILD;
        checkpoint.save();
        NSOT_MOB.a(child);
    }

    private void reconnectForNextCharacter() {
        NSOT_MOB.c();
        Session_ME session = Session_ME.getInstance();
        if (session != null) {
            session.cleanNetwork();
            session.e();
        }
    }
}

public final class AutoDaily3xSession {
    private static String[] characterNames;
    private static boolean[] completed;
    private static int currentIndex = -1;
    private static boolean running;

    private AutoDaily3xSession() {
    }

    public static void startFromMenu() {
        String[] available = SelectCharScr.gI().name;
        if (available == null) {
            GameScr.addChatPopup("Chua tai duoc danh sach nhan vat");
            return;
        }
        characterNames = new String[available.length];
        completed = new boolean[available.length];
        System.arraycopy(available, 0, characterNames, 0, available.length);
        running = true;
        currentIndex = indexOfCurrentCharacter();
        if (currentIndex < 0) {
            currentIndex = Daily3xPolicy.nextCharacterIndex(characterNames, completed, -1);
        }
        if (currentIndex < 0) {
            finishAtLogin();
            return;
        }
        System.out.println("[DAILY3X] start character=" + characterNames[currentIndex]);
        NSOT_MOB.mod_nst.startAutoDaily3x();
    }

    public static boolean isRunning() {
        return running;
    }

    public static void completeCurrentCharacter() {
        if (!running || characterNames == null || completed == null) {
            return;
        }
        if (currentIndex >= 0 && currentIndex < completed.length) {
            completed[currentIndex] = true;
        }
        int next = Daily3xPolicy.nextCharacterIndex(characterNames, completed, currentIndex);
        if (next < 0) {
            finishAtLogin();
            return;
        }
        currentIndex = next;
        SelectCharScr.k = characterNames[currentIndex];
        GameScr.addChatPopup("NVHN 3x: doi sang " + characterNames[currentIndex]);
        DailyStallRecovery.forceReconnect("daily3x-next-character");
    }

    private static int indexOfCurrentCharacter() {
        Char me = Char.getMyChar();
        if (me == null || me.cName == null || characterNames == null) {
            return -1;
        }
        for (int i = 0; i < characterNames.length; ++i) {
            if (me.cName.equals(characterNames[i])) {
                return i;
            }
        }
        return -1;
    }

    private static void finishAtLogin() {
        running = false;
        currentIndex = -1;
        NSOT_MOB.c();
        Session_ME.getInstance().close();
        GameCanvas.loginScr.b();
    }
}

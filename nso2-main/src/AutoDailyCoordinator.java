import java.util.Calendar;
import java.util.TimeZone;

public final class AutoDailyCoordinator extends Auto {
    private static final String RMS_LAST_SCHEDULE_DATE = "AutoDailyLastDate";
    private static final String RMS_HANG_DATE_PREFIX = "AutoDailyHangDate";

    // Hang dong is always the first phase of Auto Hang Ngay.  We only need to
    // enter the level-appropriate instance once; no combat is started there.
    private static final int CHECK_HANG = 0;
    private static final int GO_HANG_SCHOOL = 1;
    private static final int ENTER_HANG = 2;
    private static final int WAIT_HANG_ENTER = 3;
    private static final int EXIT_HANG = 4;
    private static final int CHECK = 5;
    private static final int GO_DAILY_SCHOOL = 6;
    private static final int START_DAILY = 7;
    private static final int WAIT_DAILY_RETURN = 8;
    private static final int START_TA_THU = 9;
    private static final int WAIT_TA_THU_RETURN = 10;
    private static final int RESUME = 11;
    private static final int DONE = 12;

    private static final long HANG_ENTER_RETRY_MS = 5000L;
    private static final long HANG_ENTER_TIMEOUT_MS = 22000L;
    private static final long HANG_REGISTER_WAIT_MS = 2000L;
    private static final int HANG_MAX_ENTER_ATTEMPTS = 3;

    private static boolean schedulerLaunching;
    private int state;
    private int schoolMap;
    private int hangMap;
    private int hangMenuIndex;
    private int hangEnterAttempts;
    private long hangPhaseStartedAt;
    private long lastHangEnterAttemptAt;
    private long hangEnteredAt;
    private long stateChangedAt;
    private long lastVdmqExitAttempt;
    private final boolean manualRun;
    private final boolean daily3x;

    public AutoDailyCoordinator(boolean manualRun) {
        this(manualRun, false);
    }

    public AutoDailyCoordinator(boolean manualRun, boolean daily3x) {
        this.manualRun = manualRun;
        this.daily3x = daily3x;
    }

    public final void g() {
        DailyCharacterProgress.forceRefresh();
        super.g();
        this.state = CHECK_HANG;
        this.schoolMap = getSchoolMap();
        this.hangMap = AutoHangDong.map();
        this.hangMenuIndex = getHangMenuIndex(this.hangMap);
        this.hangEnterAttempts = 0;
        this.hangPhaseStartedAt = System.currentTimeMillis();
        this.lastHangEnterAttemptAt = 0L;
        this.hangEnteredAt = 0L;
        this.stateChangedAt = System.currentTimeMillis();
        this.lastVdmqExitAttempt = 0L;
        Char me = Char.getMyChar();
        System.out.println("[DAILY][STATE] START manual=" + this.manualRun
                + " level=" + (me == null ? -1 : me.clevel)
                + " hangMap=" + this.hangMap);
    }

    public final void update() {
        Char me = Char.getMyChar();
        if (me == null) {
            return;
        }
        if (me.cHp <= 0 || me.statusMe == 14 || me.statusMe == 5) {
            a(true);
            return;
        }
        if (!DailyCharacterProgress.ensureSnapshot()) {
            return;
        }

        // Controller can keep the same coordinator object across reconnect.
        // Consume the one-shot Hang-exit signal before executing the old
        // EXIT_HANG state again, otherwise it would reconnect in a loop.
        if (DailyReconnectRecovery.consumeHangExitReconnect() && isHangDoneToday(me)) {
            this.hangEnteredAt = 0L;
            this.hangEnterAttempts = 0;
            setState(CHECK, "hang-exit-reconnect-complete");
            System.out.println("[DAILY][HANG] reconnect complete -> continue daily");
        }

        switch (this.state) {
            case CHECK_HANG:
                this.hangMap = AutoHangDong.map();
                this.hangMenuIndex = getHangMenuIndex(this.hangMap);
                if (this.hangMap < 0 || this.hangMenuIndex < 0) {
                    setState(CHECK, "hang-unavailable-for-level=" + me.clevel);
                    return;
                }
                if (isHangDoneToday(me)) {
                    setState(CHECK, "hang-already-entered-today");
                    return;
                }
                if (TileMap.mapID == this.hangMap) {
                    markHangDoneToday(me);
                    this.hangEnteredAt = System.currentTimeMillis();
                    setState(EXIT_HANG, "already-inside-target-hang=" + this.hangMap);
                    return;
                }
                if (TileMap.g(TileMap.mapID)) {
                    // We are in a cave instance from a previous/manual action.
                    // Logout/login is the safest no-combat exit; after reconnect
                    // CHECK_HANG runs again and enters the correct level cave.
                    System.out.println("[DAILY][HANG] inside-other-hang map=" + TileMap.mapID
                            + " target=" + this.hangMap + " -> reconnect");
                    DailyStallRecovery.forceReconnect("hang-sai-cap-thoat-ra");
                    return;
                }
                this.hangPhaseStartedAt = System.currentTimeMillis();
                setState(GO_HANG_SCHOOL, "hang-first-before-daily target=" + this.hangMap);
                return;

            case GO_HANG_SCHOOL:
                if (leaveVdmqIfNeeded(me)) {
                    return;
                }
                if (TileMap.f(TileMap.mapID)) {
                    this.schoolMap = TileMap.mapID;
                    this.hangPhaseStartedAt = System.currentTimeMillis();
                    setState(ENTER_HANG, "at-school-for-hang=" + this.schoolMap);
                    return;
                }
                System.out.println("[DAILY][HANG] return-school map=" + this.schoolMap
                        + " from=" + TileMap.mapID);
                this.a(this.schoolMap, -2, -1, -1);
                return;

            case ENTER_HANG:
                if (TileMap.mapID == this.hangMap) {
                    markHangDoneToday(me);
                    this.hangEnteredAt = System.currentTimeMillis();
                    setState(EXIT_HANG, "hang-entered=" + this.hangMap);
                    return;
                }
                if (!TileMap.f(TileMap.mapID)) {
                    setState(GO_HANG_SCHOOL, "left-school-before-hang");
                    return;
                }
                // The legacy AutoHangDong always leaves party before entering.
                // Keep that server-compatible behaviour, but never wait forever.
                if (GameScr.vParty.size() > 1 && System.currentTimeMillis() - this.stateChangedAt < 2000L) {
                    Service.gI().outParty();
                    return;
                }
                sendHangEnter();
                setState(WAIT_HANG_ENTER, "hang-enter-request-" + this.hangEnterAttempts);
                return;

            case WAIT_HANG_ENTER:
                if (TileMap.mapID == this.hangMap) {
                    markHangDoneToday(me);
                    this.hangEnteredAt = System.currentTimeMillis();
                    GameScr.addChatPopup("Da vao hang dong cap " + hangNameForMap(this.hangMap)
                            + " -> thoat ra");
                    setState(EXIT_HANG, "hang-enter-confirmed=" + this.hangMap);
                    return;
                }
                // Trên server hiện tại, lệnh vào Hang có thể chỉ mở menu trung gian
                // (ví dụ: "Nhận thưởng sớm" / "Cấp 65") thay vì chuyển map
                // ngay. Bấm đúng command đang hiển thị giống thao tác tay; nếu server
                // trả về menu gốc trước thì chọn "Hang động" rồi tick sau chọn cấp.
                if (selectHangMenuIfVisible()) {
                    return;
                }
                long now = System.currentTimeMillis();
                if (now - this.hangPhaseStartedAt >= HANG_ENTER_TIMEOUT_MS) {
                    System.out.println("[DAILY][HANG] enter-timeout target=" + this.hangMap
                            + " attempts=" + this.hangEnterAttempts
                            + " currentMap=" + TileMap.mapID);
                    GameScr.addChatPopup("Khong vao duoc hang dong -> tiep tuc NV hang ngay");
                    setState(CHECK, "hang-enter-timeout-skip");
                    return;
                }
                if (TileMap.f(TileMap.mapID)
                        && this.hangEnterAttempts < HANG_MAX_ENTER_ATTEMPTS
                        && now - this.lastHangEnterAttemptAt >= HANG_ENTER_RETRY_MS) {
                    sendHangEnter();
                    return;
                }
                if (!TileMap.f(TileMap.mapID) && !TileMap.g(TileMap.mapID)) {
                    setState(GO_HANG_SCHOOL, "hang-entry-route-moved-away");
                }
                return;

            case EXIT_HANG:
                // Reaching the instance map is enough to count the requested
                // activity. Wait briefly so the server can persist the entry,
                // then logout/login instead of attacking or intentionally dying.
                if (this.hangEnteredAt == 0L) {
                    this.hangEnteredAt = System.currentTimeMillis();
                }
                if (System.currentTimeMillis() - this.hangEnteredAt < HANG_REGISTER_WAIT_MS) {
                    return;
                }
                DailyReconnectRecovery.markHangExitReconnect();
                if (DailyStallRecovery.forceReconnect("hang-da-vao-thoat-ra")) {
                    System.out.println("[DAILY][HANG] entered map=" + this.hangMap
                            + " -> reconnect exit; daily will resume after login");
                } else {
                    DailyReconnectRecovery.cancelHangExitReconnect();
                }
                return;

            case CHECK:
                if (DailyCharacterProgress.dailyDone()) {
                    setState(this.daily3x ? RESUME : START_TA_THU,
                            "daily-confirmed-20-of-20");
                } else if (AutoDailyPanel.runDailyQuest) {
                    setState(GO_DAILY_SCHOOL, "daily-enabled");
                } else {
                    setState(START_TA_THU, "daily-disabled");
                }
                return;

            case GO_DAILY_SCHOOL:
                // Map 139-148 has no normal world-map route. Auto.a(...) used
                // to enter its blocking suicide helper on every update and
                // made the client look frozen. Move below the terrain in a
                // non-blocking tick; the normal death recovery then returns
                // to town and routing continues to the school.
                if (leaveVdmqIfNeeded(me)) {
                    return;
                }
                if (TileMap.f(TileMap.mapID)) {
                    this.schoolMap = TileMap.mapID;
                    setState(START_DAILY, "at-school=" + this.schoolMap);
                    return;
                }
                System.out.println("[DAILY][ACTION] return-school map=" + this.schoolMap
                        + " from=" + TileMap.mapID);
                this.a(this.schoolMap, -2, -1, -1);
                return;

            case START_DAILY:
                setState(WAIT_DAILY_RETURN, "launch-daily");
                NSOT_MOB.mod_nst.startDailyTaskChild();
                return;

            case WAIT_DAILY_RETURN:
                System.out.println("[DAILY][STATE] daily-returned; refresh completion count");
                DailyCharacterProgress.forceRefresh();
                setState(CHECK, "daily-returned-recheck-server-progress");
                return;

            case START_TA_THU:
                // A stale/local transition must never let Ta Thu jump ahead of
                // an enabled daily-quest phase. Only a confirmed 20/20 server
                // snapshot is allowed to pass this guard.
                if (AutoDailyPanel.runDailyQuest && !DailyCharacterProgress.dailyDone()) {
                    DailyCharacterProgress.forceRefresh();
                    setState(CHECK, "block-tathu-until-daily-20-of-20");
                    return;
                }
                if (DailyCharacterProgress.taThuDone()) {
                    setState(RESUME, "tathu-confirmed-2-of-2");
                    return;
                }
                if (!AutoDailyPanel.runTaThu) {
                    setState(RESUME, "tathu-disabled");
                    return;
                }
                setState(WAIT_TA_THU_RETURN, "launch-tathu");
                NSOT_MOB.mod_nst.startTaThuChild();
                return;

            case WAIT_TA_THU_RETURN:
                System.out.println("[DAILY][STATE] tathu-returned; refresh completion count");
                DailyCharacterProgress.forceRefresh();
                setState(START_TA_THU, "tathu-returned-recheck-server-progress");
                return;

            case RESUME:
                if (this.daily3x) {
                    AutoDaily3xSession.completeCurrentCharacter();
                    this.state = DONE;
                    return;
                }
                finishAndResume();
                this.state = DONE;
                return;

            default:
                NSOT_MOB.g();
        }
    }

    public final String toString() {
        return DailyCharacterProgress.hudText();
    }

    private boolean leaveVdmqIfNeeded(Char me) {
        if (TileMap.mapID < 139 || TileMap.mapID > 148) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (now - this.lastVdmqExitAttempt >= 1200L) {
            this.lastVdmqExitAttempt = now;
            System.out.println("[DAILY][ACTION] leave-vdmq-nonblocking map=" + TileMap.mapID);
            Char.b(me.cx, TileMap.d);
        }
        return true;
    }

    private boolean selectHangMenuIfVisible() {
        if (GameCanvas.menu == null || !GameCanvas.menu.showMenu) {
            return false;
        }

        Char me = Char.getMyChar();
        if (me == null || me.npcFocus == null || me.npcFocus.template == null
                || me.npcFocus.template.npcTemplateId != 0) {
            return false;
        }

        String level = hangNameForMap(this.hangMap);
        String tag = "[DAILY][HANG]";

        // Ưu tiên nút cấp đang hiện trong ảnh/menu động của server.
        if (GameCanvas.menu.selectCaptionForStandaloneAuto(
                new String[] {"cấp " + level, "cap " + level, "c?p " + level}, tag)) {
            this.lastHangEnterAttemptAt = System.currentTimeMillis();
            System.out.println(tag + " selected level=" + level + " targetMap=" + this.hangMap);
            return true;
        }

        // Fallback nếu server trả menu gốc trước khi mở menu chọn cấp.
        if (GameCanvas.menu.selectCaptionForStandaloneAuto(
                new String[] {"hang động", "hang dong", "hang ??ng", "hang d?ng"}, tag)) {
            this.lastHangEnterAttemptAt = System.currentTimeMillis();
            System.out.println(tag + " selected hang root; waiting level menu=" + level);
            return true;
        }
        return false;
    }

    private void sendHangEnter() {
        this.hangEnterAttempts++;
        this.lastHangEnterAttemptAt = System.currentTimeMillis();
        if (this.hangPhaseStartedAt == 0L) {
            this.hangPhaseStartedAt = this.lastHangEnterAttemptAt;
        }
        System.out.println("[DAILY][HANG] enter request target=" + this.hangMap
                + " menuIndex=" + this.hangMenuIndex
                + " attempt=" + this.hangEnterAttempts
                + " school=" + TileMap.mapID);
        // Same command encoded by TileMap.k(...) for hang maps:
        // NPC Kanata template 0 -> menu 2 -> level slot 1..6.
        GameScr.b(0, 2, this.hangMenuIndex);
    }

    private void finishAndResume() {
        Char me = Char.getMyChar();
        int level = me == null ? 0 : me.clevel;
        System.out.println("[DAILY][STATE] COMPLETE level=" + level);
        GameScr.addChatPopup("Auto Hang Ngay da xong");
        Auto interruptedAuto = this.l;
        if (NSOT_MOB.b == this) {
            NSOT_MOB.d();
        } else {
            NSOT_MOB.g();
            interruptedAuto = null;
        }
        if (interruptedAuto != null) {
            System.out.println("[DAILY][ACTION] resume interrupted auto="
                    + interruptedAuto.getClass().getName());
            GameScr.addChatPopup("Daily xong -> tiep tuc " + interruptedAuto.toString());
            return;
        }
        if (AutoDailyPanel.resumeAutoNvc && level < 70) {
            System.out.println("[DAILY][ACTION] resume Auto NV 1-70 level=" + level);
            GameScr.addChatPopup("Daily xong -> tiep tuc Auto 1-70");
            NSOT_MOB.mod_nst.startAutoNhiemVuChinh();
            return;
        }
        if (level >= 70) {
            onLevel70Plus();
        }
    }

    private static void onLevel70Plus() {
        System.out.println("[DAILY][STATE] POST_70_IDLE - extension point");
        GameScr.addChatPopup("Daily xong - Lv >= 70, dang cho cau hinh buoc tiep theo");
    }

    private void setState(int nextState, String reason) {
        if (this.state != nextState) {
            System.out.println("[DAILY][STATE] " + stateName(this.state) + " -> "
                    + stateName(nextState) + " reason=" + reason);
            this.state = nextState;
            this.stateChangedAt = System.currentTimeMillis();
        }
    }

    private static String stateName(int state) {
        switch (state) {
            case CHECK_HANG:
                return "CHECK_HANG";
            case GO_HANG_SCHOOL:
                return "GO_HANG_SCHOOL";
            case ENTER_HANG:
                return "ENTER_HANG";
            case WAIT_HANG_ENTER:
                return "WAIT_HANG_ENTER";
            case EXIT_HANG:
                return "EXIT_HANG";
            case CHECK:
                return "CHECK";
            case GO_DAILY_SCHOOL:
                return "GO_DAILY_SCHOOL";
            case START_DAILY:
                return "START_DAILY";
            case WAIT_DAILY_RETURN:
                return "WAIT_DAILY_RETURN";
            case START_TA_THU:
                return "START_TA_THU";
            case WAIT_TA_THU_RETURN:
                return "WAIT_TA_THU_RETURN";
            case RESUME:
                return "RESUME";
            case DONE:
                return "DONE";
            default:
                return "UNKNOWN";
        }
    }

    private static int getSchoolMap() {
        Char me = Char.getMyChar();
        int classId = me == null || me.nClass == null ? 0 : me.nClass.classId;
        if (classId <= 2) {
            return 1;
        }
        return classId <= 4 ? 72 : 27;
    }

    private static int getHangMenuIndex(int mapId) {
        switch (mapId) {
            case 91:
                return 1;
            case 94:
                return 2;
            case 105:
                return 3;
            case 114:
                return 4;
            case 125:
                return 5;
            case 157:
                return 6;
            default:
                return -1;
        }
    }

    private static String hangNameForMap(int mapId) {
        switch (mapId) {
            case 91:
                return "35";
            case 94:
                return "45";
            case 105:
                return "55";
            case 114:
                return "65";
            case 125:
                return "75";
            case 157:
                return "95";
            default:
                return "?";
        }
    }

    private static boolean isHangDoneToday(Char me) {
        if (me == null) {
            return false;
        }
        String saved = mResources.c(hangDateKey(me));
        return todayKey().equals(saved);
    }

    private static void markHangDoneToday(Char me) {
        if (me == null) {
            return;
        }
        String today = todayKey();
        mResources.a(hangDateKey(me), today);
        System.out.println("[DAILY][HANG] marked entered date=" + today
                + " char=" + me.charID + " map=" + TileMap.mapID);
    }

    private static String hangDateKey(Char me) {
        return RMS_HANG_DATE_PREFIX + me.charID;
    }

    private static String todayKey() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        return dateKey(calendar);
    }

    public static void tickSchedule(Calendar calendar) {
        if (!AutoDailyPanel.scheduleEnabled || schedulerLaunching || calendar == null
                || !(GameCanvas.currentScreen instanceof GameScr) || Char.getMyChar() == null) {
            return;
        }
        int nowMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
        int scheduledMinutes = AutoDailyPanel.scheduleHour * 60 + AutoDailyPanel.scheduleMinute;
        if (nowMinutes < scheduledMinutes) {
            return;
        }
        String today = dateKey(calendar);
        if (today.equals(mResources.c(RMS_LAST_SCHEDULE_DATE))) {
            return;
        }
        if (GameCanvas.menu.showMenu || GameCanvas.currentDialog != null || ChatPopup.b != null) {
            return;
        }

        schedulerLaunching = true;
        try {
            mResources.a(RMS_LAST_SCHEDULE_DATE, today);
            System.out.println("[DAILY][SCHEDULE] trigger date=" + today
                    + " time=" + AutoDailyPanel.formatTime());
            GameScr.addChatPopup("Den gio Auto Hang Ngay " + AutoDailyPanel.formatTime());
            NSOT_MOB.mod_nst.startAutoDaily(false);
        } finally {
            schedulerLaunching = false;
        }
    }

    public static void reloadSchedule() {
        System.out.println("[DAILY][SCHEDULE] enabled=" + AutoDailyPanel.scheduleEnabled
                + " time=" + AutoDailyPanel.formatTime());
    }

    private static String dateKey(Calendar calendar) {
        return String.valueOf(calendar.get(Calendar.YEAR)) + "-"
                + String.valueOf(calendar.get(Calendar.DAY_OF_YEAR));
    }
}

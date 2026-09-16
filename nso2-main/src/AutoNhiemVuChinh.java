/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  Auto
 *  AutoNhiemVuPanel
 *  Char
 *  ChatPopup
 *  Class_cl
 *  Effect
 *  Friend
 *  GameCanvas
 *  GameScr
 *  Item
 *  ItemMap
 *  ItemTemplate
 *  Mob
 *  MobTemplate
 *  MovePoint
 *  MyVector
 *  NSOT_MOB
 *  Npc
 *  Res
 *  Service
 *  Skill
 *  SkillTemplate
 *  Task
 *  TileMap
 *  Waypoint
 *  javax.microedition.lcdui.Image
 *  mResources
 */
import javax.microedition.lcdui.Image;

public final class AutoNhiemVuChinh
extends Auto {
    private static final int LAST_SUPPORTED_TASK = 32;
    private static final int TARGET_LEVEL = 50;
    private int targetLevel = TARGET_LEVEL;
    private boolean starterSuppliesPending;
    private final AutoNv130QuickSupply starterSupply = new AutoNv130QuickSupply();
    private boolean linhChiRenewalEnabled;
    private static final int[] CLASS_WEAPON = new int[]{-1, 94, 114, 99, 109, 105, 119};
    private static final int[] CLASS_SKILL_BOOK = new int[]{-1, 40, 49, 58, 67, 76, 85};
    private static final int[] SPECIAL_CLASS_WEAPON = new int[]{311, 375, 397, 552, 558, 312, 376, 398, 553, 559, 313, 377, 399, 554, 560, 314, 378, 400, 555, 561, 315, 379, 401, 556, 562, 316, 380, 402, 557, 563};
    private static final int[] TASK_LEVEL_GATE = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 25, 26, 27, 29, 30, 32, 34, 35, 37, 38, 39, 41, 43, 45, 47, 49, 51, 53, 55, 57, 59, 61, 63, 65, 67, 69};
    private static final int[] TASK_LEVEL_MAP = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 33, 33, 33, 34, 34, 34, 34, 52, 52, 64, 64, 15, 15, 15, 16, 16, 41, 42, 42, 62, 44, 18, 59, 45, 45, 53};
    private static final int[] FARM_MAPS = new int[]{23, 25, 26, 28, 29, 30, 31, 33, 34, 35, 52, 64, 15, 16};
    private static final int[] FARM_MOB_LEVELS = new int[]{3, 6, 9, 12, 15, 18, 21, 24, 27, 30, 36, 40, 44, 48};
    private static final long NPC_DELAY = 1600L;
    private static final long NOTICE_DELAY = 5000L;
    private static final long STUCK_DELAY = 20000L;
    private static final long COMBAT_STALL_DELAY = 15000L;
    private static final long COMBAT_ATTEMPT_ACTIVE_DELAY = 12000L;
    private static final int COMBAT_STALL_MIN_ATTEMPTS = 4;
    private static final long COMBAT_RX_STALL_DELAY = 20000L;
    private static final long DEATH_RETURN_COOLDOWN = 1800L;
    private static final long RESPAWN_GRACE_DELAY = 2500L;
    private static final long RECONNECT_GAME_READY_DELAY = 3000L;
    private static final int JAIAN_DIRECTION_SWITCH_DISTANCE = 24;
    private static final int JAIAN_MOVE_TARGET_TOLERANCE = 20;
    private static final long JAIAN_TERRAIN_STUCK_DELAY = 550L;
    private static final long JAIAN_TERRAIN_MOVE_COOLDOWN = 650L;
    private static final int JAIAN_TERRAIN_VERTICAL_THRESHOLD = 24;
    private static final int JAIAN_PROGRESS_DISTANCE = 10;
    private static final int JAIAN_JUMP_HORIZONTAL_SPEED = 5;
    private static final int JAIAN_THREAT_X_RANGE = 300;
    private static final int JAIAN_THREAT_Y_RANGE = 125;
    private static final int JAIAN_THREAT_CLOSE_X = 105;
    private static final int JAIAN_THREAT_CLOSE_Y = 90;
    private static final String COMPLETE_TASK_MENU = "#complete-task";
    private static final String JOIN_CLASS_MENU = "#join-class:";
    private static final String UPGRADE_ITEM_MENU = "#upgrade-item";
    private static final String TALK_ONLY_MENU = "#talk-only";
    private static final String LEVEL_REWARD_MENU = "#level-reward";
    private static final int LEVEL_REWARD_MAP = 22;
    private static final int UPGRADE_TASK_FARM_MAP = 28;
    private static final int OKANECHAN_NPC = 24;
    private static final boolean BACKGROUND_WEAPON_UPGRADE_ENABLED = true;
    private static boolean holdBagSortForStoneUpgrade;
    private long lastAction;
    private long lastNotice;
    private long lastProgress;
    private long lastBuildAction;
    private long lastFoodAction;
    private long lastManaPotionAction;
    private long lastCompleteTaskSubmitAt;
    private int lastCompleteTaskSubmitId = -1;
    private int lastCompleteTaskSubmitIndex = -1;
    private long lastKimchiUseAt;
    private long lastFoodCleanupAction;
    private long foodUsePendingUntil;
    private long lastFarmProgress;
    private long lastPickAction;
    private static final int LOCAL_PICKUP_DISTANCE = 120;
    private int pendingPickMapItem;
    private long pendingPickAt;
    private int ignoredPickMap;
    private int ignoredPickZone;
    private final int[] ignoredPickMapItems = new int[128];
    private int ignoredPickCount;
    private long lastInventoryAction;
    private long lastBoxRequest;
    private long lastLevelBoostAction;
    private long levelRewardStartedAt;
    private long lastFriendAction;
    private long lastFarmExp;
    private int lastPreferredCombatSkillId = -1;
    private int savedReturnMap = -1;
    private int savedReturnX;
    private int savedReturnY;
    private boolean restoreSavedReturnPoint;
    private int treasureImageItem = -1;
    private int treasureImageMap = -1;
    private int treasureImageX = -1;
    private int treasureImageY = -1;
    private int lastFarmMap = -1;
    private int lastTaskId = -1;
    private int lastTaskIndex = -1;
    private int lastTaskCount = -1;
    private int taskTownResetId = -1;
    private long lastTaskTownResetAt;
    private int taskTownResetAttempts;
    private int menuSelections;
    private int classPrepareStep;
    private int weaponUpgradeState;
    private int upgradeWeaponTemplateId;
    private int upgradeWeaponLevel;
    private int stoneSplitState;
    private int stoneSplitIndex = -1;
    private int stoneSplitQuantity;
    private long stoneSplitAt;
    private boolean weaponUpgradeUnavailable;
    private int completedUpgradeTaskId;
    private int completedUpgradeTaskIndex;
    private long completedUpgradeAt;
    private long challengeEnteredAt;
    private long lastChallengeEntryAction;
    private long lastChallengeAttack;
    private int challengeBattleMap;
    private long lastEscortMove;
    private long lastEscortAttack;
    private long lastEscortPotionAction;
    private int lastEscortX;
    private int escortDirection;
    private boolean escortSeen;
    private long lastJaianTerrainMove;
    private long jaianMoveProgressAt;
    private int jaianMoveProgressX;
    private int jaianMoveProgressY;
    private int jaianMoveTargetX = -1;
    private int jaianMoveTargetY = -1;
    private boolean jaianHpStockReady;
    private int jaianHpBuyAttempts;
    private long lastJaianHpBuyAction;
    private boolean jaianFreezeApplied;
    private boolean jaianSavedMobFreeze;
    private boolean jaianSavedSkillFreeze;
    private final MyVector jaianDontMoveMobs = new MyVector();
    private final MyVector jaianDisabledMobs = new MyVector();
    private int levelBoostState;
    private int levelRewardGoldBefore;
    private int linhChiBagIndex = -1;
    private int linhChiQuantityBefore;
    private final int[] friendAttemptIds = new int[64];
    private int friendAttemptCount;
    private boolean skillStateLogged;
    private long combatLastAttempt;
    private long combatLastProgress;
    private long combatExp;
    private int combatMap = -1;
    private int combatMobId = -1;
    private int combatMobHp = -1;
    private int combatAttempts;
    private boolean combatReloginRequested;
    private long combatReconnectReadyAt;
    private boolean deathRecoveryActive;
    private long lastDeathReturnAction;
    private long respawnGraceUntil;
    private boolean networkRecoveryActive;
    private long networkRecoveryReadyAt;

    public final void g() {
        super.g();
        this.lastAction = 0L;
        this.lastNotice = 0L;
        this.lastProgress = System.currentTimeMillis();
        this.lastBuildAction = 0L;
        this.lastFoodAction = 0L;
        this.lastManaPotionAction = 0L;
        this.lastCompleteTaskSubmitAt = 0L;
        this.lastCompleteTaskSubmitId = -1;
        this.lastCompleteTaskSubmitIndex = -1;
        this.lastKimchiUseAt = 0L;
        this.lastFoodCleanupAction = 0L;
        this.foodUsePendingUntil = 0L;
        this.lastFarmProgress = System.currentTimeMillis();
        this.lastPickAction = 0L;
        this.pendingPickMapItem = -1;
        this.pendingPickAt = 0L;
        this.ignoredPickMap = -1;
        this.ignoredPickZone = -1;
        this.ignoredPickCount = 0;
        this.lastInventoryAction = 0L;
        this.lastBoxRequest = 0L;
        this.lastLevelBoostAction = 0L;
        this.levelRewardStartedAt = 0L;
        this.stoneSplitState = 0;
        this.stoneSplitIndex = -1;
        this.stoneSplitQuantity = 0;
        this.stoneSplitAt = 0L;
        holdBagSortForStoneUpgrade = false;
        this.lastFriendAction = 0L;
        this.lastFarmExp = Char.getMyChar() == null ? 0L : Char.getMyChar().cEXP;
        this.savedReturnMap = -1;
        this.savedReturnX = 0;
        this.savedReturnY = 0;
        this.restoreSavedReturnPoint = false;
        this.lastFarmMap = -1;
        this.lastTaskId = -1;
        this.lastTaskIndex = -1;
        this.lastTaskCount = -1;
        this.taskTownResetId = -1;
        this.lastTaskTownResetAt = 0L;
        this.taskTownResetAttempts = 0;
        this.menuSelections = 0;
        this.classPrepareStep = 0;
        this.weaponUpgradeState = 0;
        this.upgradeWeaponTemplateId = -1;
        this.upgradeWeaponLevel = -1;
        this.weaponUpgradeUnavailable = false;
        this.completedUpgradeTaskId = -1;
        this.completedUpgradeTaskIndex = -1;
        this.completedUpgradeAt = 0L;
        this.challengeEnteredAt = 0L;
        this.lastChallengeEntryAction = 0L;
        this.lastChallengeAttack = 0L;
        this.challengeBattleMap = -1;
        this.lastEscortMove = 0L;
        this.lastEscortAttack = 0L;
        this.lastEscortPotionAction = 0L;
        this.lastEscortX = -1;
        this.escortDirection = -1;
        this.escortSeen = false;
        this.resetJaianMoveTracking();
        this.jaianHpStockReady = false;
        this.jaianHpBuyAttempts = 0;
        this.lastJaianHpBuyAction = 0L;
        this.jaianFreezeApplied = false;
        this.jaianDontMoveMobs.removeAllElements();
        this.jaianDisabledMobs.removeAllElements();
        this.levelBoostState = 0;
        this.levelRewardGoldBefore = 0;
        this.linhChiBagIndex = -1;
        this.linhChiQuantityBefore = 0;
        this.friendAttemptCount = 0;
        this.skillStateLogged = false;
        this.combatReconnectReadyAt = 0L;
        this.deathRecoveryActive = false;
        this.lastDeathReturnAction = 0L;
        this.respawnGraceUntil = 0L;
        this.networkRecoveryActive = false;
        this.networkRecoveryReadyAt = 0L;
        this.resetCombatWatchdog();
        this.starterSuppliesPending = false;
        this.starterSupply.reset();
        this.linhChiRenewalEnabled = false;
    }

    public final void setTargetLevel(int level) {
        this.targetLevel = level > 0 && level < TARGET_LEVEL ? level : TARGET_LEVEL;
    }

    public final void prepareStarterSuppliesAfterFrog() {
        this.starterSuppliesPending = true;
    }

    public final void enableLinhChiRenewal() {
        this.linhChiRenewalEnabled = true;
    }

    protected final void h() {
        this.setJaianFreeze(false);
        super.h();
        this.resetCombatWatchdog();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void a(int n, int n2, int n3, int n4) {
        Char char_ = Char.getMyChar();
        if (GameCanvas.menu.showMenu && char_ != null && char_.npcFocus != null && char_.npcFocus.template != null && char_.npcFocus.template.npcTemplateId == 25) {
            GameCanvas.menu.showMenu = false;
            GameScr.gI().resetButton();
            System.out.println("AutoNVC route=close-rikudou-daily-menu map=" + TileMap.mapID);
        }
        TileMap.ignoreDailyTaskRouteForMainAuto = true;
        try {
            super.a(n, n2, n3, n4);
        }
        finally {
            TileMap.ignoreDailyTaskRouteForMainAuto = false;
        }
    }

    public final void update() {
        try {
            this.updateTask();
        }
        catch (Exception exception) {
            System.out.println("AutoNVC recovered error: " + exception);
            exception.printStackTrace();
            this.lastAction = System.currentTimeMillis();
            this.notice("Auto NV vua bo qua mot loi client, dang thu lai");
        }
    }

    private void updateTask() {
        int n;
        boolean bl;
        int n2;
        Char char_ = Char.getMyChar();
        if (char_ == null) {
            return;
        }
        Session_ME session = Session_ME.getInstance();
        if (session == null || !session.connected) {
            this.networkRecoveryActive = true;
            this.networkRecoveryReadyAt = 0L;
            this.clearTransientCombatState(char_);
            return;
        }
        if (char_.clevel >= this.targetLevel) {
            GameScr.addChatPopup("Da dat level " + this.targetLevel + ", ket thuc Auto NV");
            NSOT_MOB.d();
            return;
        }
        if (this.starterSuppliesPending && AutoNv130QuickPolicy.canBuyStarterSupplies(char_.ctaskId)) {
            if (this.starterSupply.tick()) {
                return;
            }
            this.starterSuppliesPending = false;
            GameScr.addChatPopup("Da mua do Goosho, tiep tuc Auto NV Lv1-30");
        }
        if (this.linhChiRenewalEnabled && AutoNv130QuickPolicy.canRenewLinhChi(char_.ctaskId)
                && AutoNvcLinhChiShop.tick()) {
            return;
        }
        if (this.combatReloginRequested) {
            if (this.waitForCombatReconnect(char_, session)) {
                return;
            }
        }
        if (this.networkRecoveryActive && this.waitForNetworkRecovery(char_, session)) {
            return;
        }
        if (char_.ctaskId > 32) {
            // This branch returns on every tick while farming after the last
            // supported quest, and also stops the auto as soon as level 50 is
            // reached.  Flush newly earned potential points before either
            // return so the final level-up points are not left unassigned.
            this.maintainBuild(char_);
            if (char_.clevel < 50) {
                this.doLevelFarm(char_, AutoNhiemVuChinh.getOptimalFarmMap(char_.ctaskId, char_.clevel), 50, "sau nhiem vu chinh");
                return;
            }
            GameScr.addChatPopup((String)"Da dat level 50 va hoan thanh chuoi NV chinh ho tro");
            NSOT_MOB.d();
            return;
        }
        if (char_.cHp <= 0 || char_.statusMe == 14 || char_.statusMe == 5) {
            this.handleDeathRecovery(char_);
            return;
        }
        if (this.deathRecoveryActive) {
            this.handleRespawnRecovery(char_);
            return;
        }
        if (System.currentTimeMillis() < this.respawnGraceUntil) {
            return;
        }
        this.freezeFlyingMobs();
        if (this.restoreSavedReturnPoint) {
            this.restoreSavedReturnPoint = false;
            if (TileMap.mapID == this.savedReturnMap && (Math.abs(char_.cx - this.savedReturnX) > 18 || Math.abs(char_.cy - this.savedReturnY) > 18)) {
                System.out.println("AutoNVC death=restore-safe-point map=" + this.savedReturnMap + " at=" + this.savedReturnX + "," + this.savedReturnY);
                Char.b((int)this.savedReturnX, (int)this.savedReturnY);
                this.notice("Da ve diem luu lang/truong, tiep tuc duong di nhiem vu");
                return;
            }
        }
        this.rememberSafeReturnPoint(char_);
        Task task = char_.taskMaint;
        int n3 = task == null ? -1 : task.index;
        int n4 = n2 = task == null ? -1 : (int)task.count;
        if (char_.ctaskId != 17 || n3 <= 0 || AutoNhiemVuChinh.isJaianReportStep(task)) {
            this.setJaianFreeze(false);
        }
        if (this.lastTaskId != char_.ctaskId && this.returnTownBeforeNewTaskRoute(char_)) {
            return;
        }
        if (char_.ctaskId == 29 && n3 == 1 && char_.clevel < 50) {
            // Task 29 deliberately pauses questing to farm from roughly level
            // 43 to 50.  Because this path returns before the normal build
            // maintenance below, potential points used to accumulate here.
            this.maintainBuild(char_);
            this.lastTaskId = char_.ctaskId;
            this.lastTaskIndex = n3;
            this.lastTaskCount = n2;
            this.lastProgress = System.currentTimeMillis();
            this.taskTownResetId = char_.ctaskId;
            this.taskTownResetAttempts = 0;
            this.doLevelFarm(char_, 64, 50, "tam hoan NV29 buoc 2 de up level 50", true);
            return;
        }
        if (char_.ctaskId == 29 && n3 == 1 && TileMap.mapID != 15) {
            // Level 50 can be reached immediately before this routing branch;
            // assign that level's points before returning to change maps.
            this.maintainBuild(char_);
            this.lastTaskId = char_.ctaskId;
            this.lastTaskIndex = n3;
            this.lastTaskCount = n2;
            this.lastProgress = System.currentTimeMillis();
            this.taskTownResetId = char_.ctaskId;
            this.taskTownResetAttempts = 0;
            System.out.println("AutoNVC task29=priorityRoute from=" + TileMap.mapID + " target=15 count=" + n2 + "/" + AutoNhiemVuChinh.getMaxCount(task));
            this.routeToTaskMap(15, "NV29 buoc 2 tim Di ma Tinh Anh");
            return;
        }
        this.maintainBuild(char_);
        this.maintainPreferredCombatSkill(char_);
        if (this.maintainSkillBooks(char_)) {
            return;
        }
        if (this.maintainManaPotion(char_)) {
            return;
        }
        this.maintainFood(char_);
        if (this.maintainQuickPickupInventory(char_)) {
            return;
        }
        if (this.maintainInventory(char_)) {
            return;
        }
        boolean bl2 = this.lastTaskId != char_.ctaskId || this.lastTaskIndex != n3;
        boolean bl3 = bl = bl2 || this.lastTaskCount != n2;
        if (bl) {
            this.lastProgress = System.currentTimeMillis();
            this.lastTaskCount = n2;
            this.markCombatProgress();
        }
        if (bl2) {
            this.lastTaskId = char_.ctaskId;
            this.lastTaskIndex = n3;
            this.lastAction = 0L;
            this.menuSelections = 0;
            this.lastCompleteTaskSubmitAt = 0L;
            this.lastCompleteTaskSubmitId = -1;
            this.lastCompleteTaskSubmitIndex = -1;
            this.friendAttemptCount = 0;
            this.lastFriendAction = 0L;
            this.challengeEnteredAt = 0L;
            this.lastChallengeEntryAction = 0L;
            this.lastChallengeAttack = 0L;
            this.challengeBattleMap = -1;
            this.lastEscortMove = 0L;
            this.lastEscortAttack = 0L;
            this.lastEscortPotionAction = 0L;
            this.lastEscortX = -1;
            this.escortDirection = -1;
            this.escortSeen = false;
            this.resetJaianMoveTracking();
            this.jaianHpStockReady = false;
            this.jaianHpBuyAttempts = 0;
            this.lastJaianHpBuyAction = 0L;
            if (char_.ctaskId != 9) {
                this.classPrepareStep = 0;
                this.skillStateLogged = false;
            }
        }
        if (this.monitorCombatStall(char_)) {
            return;
        }
        int n5 = AutoNhiemVuChinh.getTaskMap(char_, task);
        int n6 = AutoNhiemVuChinh.getTaskNpc(char_, task);
        int n7 = AutoNhiemVuChinh.getMaxCount(task);
        if (bl2) {
            System.out.println("AutoNVC task=" + char_.ctaskId + " index=" + n3 + " map=" + n5 + " npc=" + n6 + " count=" + n2 + "/" + n7 + " type=" + AutoNhiemVuChinh.getTaskType(char_.ctaskId, n3) + " step=" + AutoNhiemVuChinh.getStepName(task));
        } else if (System.currentTimeMillis() - this.lastProgress >= 20000L) {
            System.out.println("AutoNVC watchdog task=" + char_.ctaskId + " index=" + n3 + " count=" + n2 + "/" + n7 + " map=" + TileMap.mapID + " menuTry=" + this.menuSelections);
            this.lastProgress = System.currentTimeMillis();
        }
        if (char_.ctaskId == 24 && char_.clevel < 40) {
            int n8 = AutoNhiemVuChinh.getOptimalFarmMap(char_.ctaskId, char_.clevel);
            if (n8 == 35) {
                n8 = 34;
            }
            this.doLevelFarm(char_, n8, 40, "tam hoan NV24 den cap 40", true);
            return;
        }
        if (task == null) {
            if (char_.ctaskId == 9 && AutoNhiemVuChinh.hasNoClass(char_)) {
                this.doChooseClass(char_);
                return;
            }
            String string = char_.ctaskId == 24 ? "nv truy" : null;
            this.goToNpc(n5, n6, "nhan nhiem vu", string);
            return;
        }
        if (!AutoNhiemVuChinh.isTaskDataValid(task)) {
            this.notice("Server gui du lieu buoc nhiem vu khong hop le");
            return;
        }
        // NV17 la mot luong ho tong lien tuc. Xu ly truoc cac tac vu chung
        // (tui do, food, mana, route...) de chung khong doi khu bo roi Jaian.
        if (JaianEscortSafety.isEscortStep(char_.ctaskId, n3, AutoNhiemVuChinh.isJaianReportStep(task))) {
            this.doJaianEscort(char_, n3);
            return;
        }
        if (char_.ctaskId == 9 && AutoNhiemVuChinh.hasNoClass(char_)) {
            this.doChooseClass(char_);
            return;
        }
        int n9 = AutoNhiemVuChinh.getRequiredLevel(char_.ctaskId);
        if (n3 == 0 && n9 > 0 && char_.clevel < n9) {
            this.doLevelFarm(char_, AutoNhiemVuChinh.getTaskLevelMap(char_.ctaskId, char_.clevel), n9, "mo task " + char_.ctaskId);
            return;
        }
        int n10 = AutoNhiemVuChinh.getStepRequiredLevel(char_.ctaskId, task);
        if (n10 > char_.clevel) {
            int n11 = AutoNhiemVuChinh.getStepLevelFarmMap(char_.ctaskId, n3, char_.clevel);
            this.doLevelFarm(char_, n11, n10, "buoc dat cap " + n10 + " cua task " + char_.ctaskId);
            return;
        }
        if (char_.ctaskId == 3 && n3 == 0 && n7 > 0 && task.count < n7) {
            this.doBuyQuestFood(n5, n6, task.count, n7);
            return;
        }
        if (char_.ctaskId == 3 && n3 == 1) {
            this.doUseQuestFood();
            return;
        }
        if (AutoNhiemVuChinh.isUpgradeTaskStep(char_.ctaskId, task)) {
            this.doQuestEquipmentUpgrade(char_, task);
            return;
        }
        int n12 = n = char_.ctaskId == 13 ? AutoNhiemVuChinh.getChallengeBattleMap(n3) : -1;
        if (n >= 0 && TileMap.mapID == n) {
            this.doTeacherChallenge(char_, n3);
            return;
        }
        if (char_.ctaskId == 13 && n3 > 0 && n3 < 4) {
            this.enterTeacherChallengeRoom(char_, n3);
            return;
        }
        if (char_.ctaskId == 17 && AutoNhiemVuChinh.isJaianReportStep(task)) {
            this.doJaianReport(char_, n5, n6);
            return;
        }
        if (char_.ctaskId == 15 && n3 > 0) {
            this.doLetterDelivery(char_, n5, n6, n3);
            return;
        }
        if (char_.ctaskId == 20 && n3 == 1) {
            this.doTaskTwentyWildBoarCave();
            return;
        }
        if (n6 >= 0) {
            String string = AutoNhiemVuChinh.isReportStep(char_.ctaskId, task) ? COMPLETE_TASK_MENU
                    : (char_.ctaskId == 0 && n3 == 3 ? TALK_ONLY_MENU : null);
            this.goToNpc(n5, n6, "noi chuyen/bao cao", string);
            return;
        }
        switch (char_.ctaskId) {
            case 0: 
            case 1: 
            case 7: 
            case 8: {
                this.doTalkFallback(n5);
                return;
            }
            case 2: {
                this.doWeaponTraining(n3, n5, n7);
                return;
            }
            case 3: {
                this.doTaskThreeCombat(n3, n5, n7, task.count);
                return;
            }
            case 4: {
                this.doTaskFourCombat(n3, n5, n7, task.count);
                return;
            }
            case 5: {
                this.doTaskFiveCombat(n3, n5, n7, task.count);
                return;
            }
            case 10: 
            case 14: 
            case 16: {
                this.doProgressTask(n5, n7, task.count);
                return;
            }
            case 6: {
                this.doVisitTask(n5);
                return;
            }
            case 9: {
                this.doClassSetup(char_);
                return;
            }
            case 11: {
                this.doFriendTask(char_, n3, n7, task.count);
                return;
            }
            case 12: {
                this.doQuestEquipmentUpgrade(char_, task);
                return;
            }
            case 13: 
            case 15: {
                if (n7 > 0 && n5 >= 0) {
                    this.doProgressTask(n5, n7, task.count);
                } else if (n5 >= 0) {
                    this.doVisitTask(n5);
                } else {
                    this.doSpecialTask("Buoc dac biet: " + AutoNhiemVuChinh.getStepName(task));
                }
                return;
            }
            case 17: 
            case 18: 
            case 19: 
            case 20: 
            case 21: 
            case 22: 
            case 23: 
            case 24: 
            case 25: 
            case 26: 
            case 27: 
            case 28: 
            case 29: 
            case 30: 
            case 31: 
            case 32: {
                this.doAdvancedTask(char_, task, n5, n7);
                return;
            }
        }
        this.doSpecialTask("Task ngoai bang xu ly an toan: " + char_.ctaskId + "/" + n3);
    }

    private void doBuyQuestFood(int n, int n2, int n3, int n4) {
        if (n < 0) {
            n = 22;
        }
        if (n2 < 0) {
            n2 = 4;
        }
        if (TileMap.mapID != n) {
            this.a(n, -2, -1, -1);
            return;
        }
        Npc npc = GameScr.i((int)n2);
        if (npc == null) {
            this.notice("Khong tim thay NPC ban thuc an " + n2 + " tren map " + n);
            return;
        }
        Char char_ = Char.getMyChar();
        if (Math.abs(char_.cx - npc.cx) > 22 || Math.abs(char_.cy - npc.cy) > 22) {
            Char.b((int)npc.cx, (int)npc.cy);
            return;
        }
        long l = System.currentTimeMillis();
        if (l - this.lastAction < 5000L) {
            return;
        }
        if (this.menuSelections >= 2) {
            this.notice("Da gui lenh mua com nam 2 lan, dang cho server cap nhat " + n3 + "/" + n4);
            return;
        }
        System.out.println("AutoNVC action=buyQuestFood npc=" + n2 + " shop=0 item=0 quantity=3 attempt=" + (this.menuSelections + 1) + " count=" + n3 + "/" + n4);
        char_.npcFocus = npc;
        GameScr.b((int)n2, (int)0, (int)0);
        Service.gI().buyItem_food(9, 0, 3);
        GameCanvas.menu.showMenu = false;
        ++this.menuSelections;
        this.lastAction = l;
        Class_cl.h();
    }

    private void doUseQuestFood() {
        long l = System.currentTimeMillis();
        if (l - this.lastAction < 3000L) {
            return;
        }
        Item item = AutoNhiemVuChinh.findBagItem(23);
        if (item == null) {
            this.notice("Khong tim thay com nam template 23 trong hanh trang");
            return;
        }
        if (this.menuSelections >= 2) {
            this.notice("Da gui lenh su dung com nam 2 lan, dang cho server chuyen buoc");
            return;
        }
        System.out.println("AutoNVC action=useQuestFood template=23 bag=" + item.indexUI + " attempt=" + (this.menuSelections + 1));
        Service.gI().useItem(item.indexUI);
        ++this.menuSelections;
        this.lastAction = l;
        Class_cl.h();
    }

    private void doTaskThreeCombat(int n, int n2, int n3, int n4) {
        if (n3 <= 0 || n4 >= n3) {
            this.notice("Da du tien do task 3, dang cho server chuyen buoc");
            return;
        }
        if (n2 < 0) {
            n2 = 23;
        }
        int n5 = n == 2 ? 1 : 2;

        // NV3 buoc 3 co the nhan map dich tu bang task khac voi map thuc te
        // dang co Oc sen. Neu cu goi next-map, TileMap.k() se bao
        // "Khong the chuyen map" do gioi han map cua task dau game.
        // Uu tien spawn nhiem vu dang co tren map hien tai; ke ca mob dang chet
        // va cho hoi sinh, khong route di map khac nua.
        boolean localQuestSpawn = n == 2 && AutoNhiemVuChinh.hasMobTemplateOnCurrentMap(n5);
        if (TileMap.mapID != n2 && !localQuestSpawn) {
            this.a(n2, -2, -1, -1);
            return;
        }
        if (TileMap.mapID != n2 && localQuestSpawn) {
            System.out.println("AutoNVC task3=keep-local-map current=" + TileMap.mapID
                    + " taskMap=" + n2 + " mobTemplate=" + n5 + " count=" + n4 + "/" + n3);
        }
        System.out.println("AutoNVC task3=attack index=" + n + " mobTemplate=" + n5 + " count=" + n4 + "/" + n3 + " map=" + TileMap.mapID);
        if (this.autoPickItems(-1)) {
            return;
        }
        this.attackTracked(n5, 3);
    }

    private static boolean hasMobTemplateOnCurrentMap(int mobTemplateId) {
        if (GameScr.vMobAttack == null) {
            return false;
        }
        for (int i = 0; i < GameScr.vMobAttack.size(); ++i) {
            Mob mob = (Mob)GameScr.vMobAttack.elementAt(i);
            if (mob != null && mob.templateId == mobTemplateId) {
                return true;
            }
        }
        return false;
    }

    private void doTaskFourCombat(int n, int n2, int n3, int n4) {
        int n5;
        int n6;
        int n7;
        if (n == 0) {
            n7 = 21;
            n6 = -1;
            n5 = -1;
        } else if (n == 1) {
            n7 = 21;
            n6 = 3;
            n5 = 209;
        } else {
            n7 = 23;
            n6 = 4;
            n5 = 210;
        }
        this.doFixedQuestCombat(4, n, n2 >= 0 ? n2 : n7, n7, n6, n5, n3, n4);
    }

    private void doTaskFiveCombat(int n, int n2, int n3, int n4) {
        int n5 = n == 0 ? 3 : 54;
        int n6 = n == 0 ? -1 : 211;
        this.doFixedQuestCombat(5, n, n2 >= 0 ? n2 : 20, 20, n5, n6, n3, n4);
    }

    private void doFixedQuestCombat(int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8) {
        int n9;
        int n10 = n9 = n3 >= 0 ? n3 : n4;
        if (n7 > 0 && n8 >= n7) {
            this.notice("Da du tien do task " + n + ", dang cho server chuyen buoc");
            return;
        }
        if (TileMap.mapID != n9) {
            this.a(n9, -2, -1, -1);
            return;
        }
        System.out.println("AutoNVC fixedCombat task=" + n + " index=" + n2 + " map=" + n9 + " mobTemplate=" + n5 + " itemTemplate=" + n6 + " count=" + n8 + "/" + n7);
        if (this.autoPickItems(n6)) {
            return;
        }
        this.attackTracked(n5, 3);
    }

    private void doJaianEscort(Char char_, int n) {
        Char jaian = AutoNhiemVuChinh.findJaianEscort(char_);
        if (jaian != null && TileMap.mapID == 33) {
            this.setJaianFreeze(true);
            this.freezeJaianEscortMobs();
        }
        if (!this.jaianHpStockReady && this.maintainJaianHpStock(char_, jaian != null)) {
            return;
        }
        if (TileMap.mapID != 33) {
            if (JaianEscortSafety.mayRouteTo(char_.ctaskId, char_.taskMaint.index,
                    AutoNhiemVuChinh.isJaianReportStep(char_.taskMaint), 33, jaian != null)) {
                this.notice("Đang đi qua map 33 để vào khu giúp Jaian");
                this.routeToTaskMap(33, "vao khu giup Jaian");
            }
            return;
        }
        this.setJaianFreeze(true);
        this.freezeJaianEscortMobs();
        Char.timeStartBlink = true;
        long now = System.currentTimeMillis();
        int hpPercent = char_.cMaxHp <= 0 ? 100 : (int)((long)char_.cHp * 100L / (long)char_.cMaxHp);
        if (JaianEscortSafety.shouldUsePotion(hpPercent)
                && now - this.lastEscortPotionAction >= 800L
                && !GameCanvas.menu.showMenu && ChatPopup.b == null && GameCanvas.currentDialog == null) {
            GameScr.x();
            this.lastEscortPotionAction = now;
        }
        if (jaian == null) {
            this.escortSeen = false;
            Npc npc = GameScr.i((int)17);
            if (npc != null && (Math.abs(char_.cx - npc.cx) > 22 || Math.abs(char_.cy - npc.cy) > 22)) {
                Char.c((int)npc.cx, (int)npc.cy);
                return;
            }
            if (now - this.lastAction >= 2000L) {
                GameCanvas.menu.showMenu = false;
                char_.npcFocus = npc;
                System.out.println("AutoNVC escort=start npc=17 map=" + TileMap.mapID + " option=0 attempt=" + ++this.menuSelections);
                GameScr.b((int)17, (int)0, (int)0);
                this.lastAction = now;
            }
            return;
        }

        this.escortSeen = true;
        if (this.lastEscortX < 0) {
            this.lastEscortX = jaian.cx;
        } else {
            int jaianDeltaX = jaian.cx - this.lastEscortX;
            if (jaianDeltaX >= JAIAN_DIRECTION_SWITCH_DISTANCE) {
                if (this.escortDirection != 1) {
                    this.escortDirection = 1;
                    char_.currentMovePoint = null;
                    System.out.println("AutoNVC escort=direction-stable direction=1 jaian=" + jaian.cx + "," + jaian.cy);
                }
                this.lastEscortX = jaian.cx;
            } else if (jaianDeltaX <= -JAIAN_DIRECTION_SWITCH_DISTANCE) {
                if (this.escortDirection != -1) {
                    this.escortDirection = -1;
                    char_.currentMovePoint = null;
                    System.out.println("AutoNVC escort=direction-stable direction=-1 jaian=" + jaian.cx + "," + jaian.cy);
                }
                this.lastEscortX = jaian.cx;
            }
        }
        if (this.escortDirection == 0) {
            this.escortDirection = char_.cx <= jaian.cx ? 1 : -1;
        }

        int distanceToJaianX = Math.abs(char_.cx - jaian.cx);
        int distanceToJaianY = Math.abs(char_.cy - jaian.cy);
        int leadDistance = this.escortDirection * (char_.cx - jaian.cx);

        // Bao ve Jaian la uu tien cao nhat. Tim quai ca PHIA TRUOC LAN PHIA SAU
        // cua Jaian va xu ly truoc khi tiep tuc chay dan duong/thu hep khoang cach.
        Mob threat = AutoNhiemVuChinh.findJaianThreat(jaian, this.escortDirection);
        if (threat != null) {
            char_.mobFocus = threat;
            Skill skill = char_.myskill;
            int attackRangeX = skill == null || skill.dx < 25 ? 25 : skill.dx;
            int attackRangeY = skill == null || skill.dy < 20 ? 20 : skill.dy;
            int mobDistanceX = Math.abs(char_.cx - threat.xFirst);
            int mobDistanceY = Math.abs(char_.cy - threat.yFirst);
            if (mobDistanceX > attackRangeX || mobDistanceY > attackRangeY) {
                int targetX = threat.xFirst;
                int minX = jaian.cx - 250;
                int maxX = jaian.cx + 250;
                if (targetX < minX) {
                    targetX = minX;
                } else if (targetX > maxX) {
                    targetX = maxX;
                }
                int targetY = threat.yFirst;
                int minY = jaian.cy - JAIAN_THREAT_Y_RANGE;
                int maxY = jaian.cy + JAIAN_THREAT_Y_RANGE;
                if (targetY < minY) {
                    targetY = minY;
                } else if (targetY > maxY) {
                    targetY = maxY;
                }
                this.moveJaianSmooth(char_, targetX, targetY);
                if (now - this.lastEscortMove >= 650L) {
                    int side = threat.xFirst < jaian.cx ? -1 : 1;
                    System.out.println("AutoNVC escort=move-to-threat side=" + side
                            + " mob=" + threat.mobId + " at=" + threat.xFirst + "," + threat.yFirst
                            + " me=" + char_.cx + "," + char_.cy + " jaian=" + jaian.cx + "," + jaian.cy
                            + " target=" + targetX + "," + targetY);
                    this.lastEscortMove = now;
                }
                return;
            }
            char_.currentMovePoint = null;
            char_.mobFocus = threat;
            if (now - this.lastEscortAttack >= 900L) {
                int side = threat.xFirst < jaian.cx ? -1 : 1;
                System.out.println("AutoNVC escort=protect-jaian side=" + side
                        + " mob=" + threat.mobId + " template=" + threat.templateId
                        + " rank=" + threat.levelBoss + " mobAt=" + threat.xFirst + "," + threat.yFirst
                        + " jaianAt=" + jaian.cx + "," + jaian.cy);
                this.lastEscortAttack = now;
            }
            this.attackTracked(-1, 3);
            return;
        }

        // Chi khi xung quanh Jaian da sach quai moi uu tien bat lai khoang cach.
        if (leadDistance < -45 || distanceToJaianX > 235 || distanceToJaianY > 85) {
            int targetX = jaian.cx + this.escortDirection * 110;
            if (targetX < 12) {
                targetX = 12;
            }
            this.moveJaianSmooth(char_, targetX, jaian.cy);
            if (now - this.lastEscortMove >= 450L) {
                System.out.println("AutoNVC escort=recover-distance me=" + char_.cx + "," + char_.cy
                        + " jaian=" + jaian.cx + "," + jaian.cy + " target=" + targetX + "," + jaian.cy
                        + " distance=" + distanceToJaianX + "," + distanceToJaianY);
                this.lastEscortMove = now;
            }
            return;
        }

        if (leadDistance >= 90 && leadDistance <= 145 && distanceToJaianY <= 55) {
            char_.currentMovePoint = null;
            if (now - this.lastEscortMove >= 3000L) {
                System.out.println("AutoNVC escort=lead-jaian map=" + TileMap.mapID + " me=" + char_.cx + "," + char_.cy
                        + " jaian=" + jaian.cx + "," + jaian.cy + " hp=" + jaian.cHp
                        + " direction=" + this.escortDirection + " lead=" + leadDistance);
                this.lastEscortMove = now;
            }
            return;
        }
        if (now - this.lastEscortMove < 450L) {
            return;
        }
        int targetX = jaian.cx + this.escortDirection * 115;
        if (targetX < 12) {
            targetX = 12;
        }
        boolean moved = this.moveJaianSmooth(char_, targetX, jaian.cy);
        this.lastEscortMove = now;
        System.out.println("AutoNVC escort=move-ahead map=" + TileMap.mapID + " me=" + char_.cx + "," + char_.cy
                + " jaian=" + jaian.cx + "," + jaian.cy + " target=" + targetX + "," + jaian.cy
                + " distance=" + distanceToJaianX + "," + distanceToJaianY
                + " direction=" + this.escortDirection + " smooth=" + moved);
    }

    private boolean moveJaianSmooth(Char char_, int targetX, int targetY) {
        if (char_ == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        boolean targetChanged = this.jaianMoveTargetX < 0
                || Math.abs(this.jaianMoveTargetX - targetX) > JAIAN_MOVE_TARGET_TOLERANCE
                || Math.abs(this.jaianMoveTargetY - targetY) > 55;
        if (targetChanged) {
            this.jaianMoveTargetX = targetX;
            this.jaianMoveTargetY = targetY;
        }
        if (this.jaianMoveProgressAt == 0L) {
            this.jaianMoveProgressX = char_.cx;
            this.jaianMoveProgressY = char_.cy;
            this.jaianMoveProgressAt = now;
        } else if (Math.abs(char_.cx - this.jaianMoveProgressX) >= JAIAN_PROGRESS_DISTANCE
                || Math.abs(char_.cy - this.jaianMoveProgressY) >= JAIAN_PROGRESS_DISTANCE) {
            this.jaianMoveProgressX = char_.cx;
            this.jaianMoveProgressY = char_.cy;
            this.jaianMoveProgressAt = now;
        }

        int distanceX = Math.abs(char_.cx - targetX);
        int distanceY = Math.abs(char_.cy - targetY);
        if (distanceX <= 12 && distanceY <= 20) {
            char_.currentMovePoint = null;
            this.jaianMoveProgressAt = now;
            return false;
        }

        // Khi dang bay qua mep dia hinh, tiep tuc "giu" huong ngang. Char update
        // cua game se dua cvx ve 0 ngay khi cham vao vach. Neu khong cap lai cvx,
        // nhan vat chi nhay thang dung roi roi xuong dung vi tri cu -> ket map.
        if (char_.statusMe == 3) {
            char_.currentMovePoint = null;
            int deltaX = targetX - char_.cx;
            if (Math.abs(deltaX) > 12 && char_.cvy <= 4) {
                int direction = deltaX < 0 ? -1 : 1;
                char_.cdir = direction;
                char_.cvx = JAIAN_JUMP_HORIZONTAL_SPEED * direction;
            }
            return true;
        }
        if (char_.statusMe == 4) {
            char_.currentMovePoint = null;
            int deltaX = targetX - char_.cx;
            // Cho phep chinh nhe quy dao luc roi de dap len mat bac ke tiep.
            if (Math.abs(deltaX) > 24 && targetY >= char_.cy - 12 && char_.cvy <= 7) {
                int direction = deltaX < 0 ? -1 : 1;
                char_.cdir = direction;
                char_.cvx = 3 * direction;
            }
            return true;
        }

        boolean needsUpwardTerrainMove = targetY < char_.cy - JAIAN_TERRAIN_VERTICAL_THRESHOLD;
        boolean stuckOnTerrain = distanceX > 28
                && this.jaianMoveProgressAt > 0L
                && now - this.jaianMoveProgressAt >= JAIAN_TERRAIN_STUCK_DELAY;
        int horizontalDirection = targetX < char_.cx ? -1 : targetX > char_.cx ? 1 : this.escortDirection;
        if (horizontalDirection == 0) {
            horizontalDirection = 1;
        }
        int wallFlag = horizontalDirection > 0 ? 4 : 8;
        boolean wallImmediatelyAhead = distanceX > 18
                && TileMap.a(char_.cx + horizontalDirection * 14, char_.cy - 12, wallFlag);

        if ((needsUpwardTerrainMove || stuckOnTerrain || wallImmediatelyAhead)
                && now - this.lastJaianTerrainMove >= JAIAN_TERRAIN_MOVE_COOLDOWN
                && this.tryJaianPhysicalJump(char_, targetX, targetY, now)) {
            return true;
        }

        // Neu dich nam thap hon gan nhu cung X, can buoc khoi mep san de game roi
        // xuong tang duoi thay vi dung im cho vi currentMovePoint chi xu ly truc X.
        if (distanceX <= 18 && targetY > char_.cy + 30
                && (char_.statusMe == 1 || char_.statusMe == 2)) {
            int dropDirection = this.escortDirection != 0 ? this.escortDirection : horizontalDirection;
            int nudgeX = char_.cx + dropDirection * 48;
            if (nudgeX < 12) {
                nudgeX = 12;
            } else if (nudgeX > TileMap.c - 12) {
                nudgeX = TileMap.c - 12;
            }
            char_.currentMovePoint = new MovePoint(nudgeX, char_.cy);
            return true;
        }

        MovePoint movePoint = char_.currentMovePoint;
        if (movePoint != null && Math.abs(movePoint.xEnd - targetX) <= JAIAN_MOVE_TARGET_TOLERANCE
                && Math.abs(movePoint.yEnd - targetY) <= 55) {
            return false;
        }
        if (distanceX <= 12 && distanceY <= 65) {
            char_.currentMovePoint = null;
            return false;
        }
        char_.currentMovePoint = new MovePoint(targetX, char_.cy);
        return true;
    }

    private boolean tryJaianPhysicalJump(Char char_, int targetX, int targetY, long now) {
        if (char_ == null) {
            return false;
        }
        if (char_.statusMe != 1 && char_.statusMe != 2 && char_.statusMe != 10 && char_.statusMe != 11) {
            return false;
        }
        int deltaX = targetX - char_.cx;
        int direction = deltaX < 0 ? -1 : deltaX > 0 ? 1 : this.escortDirection;
        if (direction == 0) {
            direction = char_.cdir == 0 ? 1 : char_.cdir;
        }
        boolean fromWater = char_.statusMe == 10 || char_.statusMe == 11;
        char_.currentMovePoint = null;
        char_.cdir = direction;
        char_.cvx = Math.abs(deltaX) > 12 ? JAIAN_JUMP_HORIZONTAL_SPEED * direction : 0;
        // -10 la luc nhay tieu chuan ma client dung cho MovePoint dir=3.
        // Dung cung luc nhay nay de vuot bac cao, khong teleport toa do.
        char_.cvy = -10;
        char_.statusMe = 3;
        char_.cp1 = 0;
        this.lastJaianTerrainMove = now;
        this.jaianMoveProgressX = char_.cx;
        this.jaianMoveProgressY = char_.cy;
        this.jaianMoveProgressAt = now;
        System.out.println("AutoNVC escort=terrain-jump fromWater=" + fromWater
                + " me=" + char_.cx + "," + char_.cy
                + " target=" + targetX + "," + targetY
                + " speedX=" + char_.cvx + " speedY=" + char_.cvy);
        return true;
    }

    private void resetJaianMoveTracking() {
        this.lastJaianTerrainMove = 0L;
        this.jaianMoveProgressAt = 0L;
        this.jaianMoveProgressX = 0;
        this.jaianMoveProgressY = 0;
        this.jaianMoveTargetX = -1;
        this.jaianMoveTargetY = -1;
    }

    private void doJaianReport(Char char_, int n, int n2) {
        int n3;
        int n4 = n3 = n >= 0 ? n : 33;
        if (TileMap.mapID != n3) {
            this.notice("Dang di map " + n3 + " de bao tin voi ba Rei");
            this.routeToTaskMap(n3, "bao tin voi ba Rei");
            return;
        }
        Npc npc = AutoNhiemVuChinh.findNpcByName("rei");
        if (npc != null && npc.template != null) {
            this.goToNpc(n3, npc.template.npcTemplateId, "bao tin voi ba Rei", COMPLETE_TASK_MENU);
        } else if (n2 >= 0) {
            this.goToNpc(n3, n2, "bao tin voi ba Rei", COMPLETE_TASK_MENU);
        } else {
            this.notice("Chua thay ba Rei tai map " + n3 + ", dang cho NPC tai xong");
        }
    }

    private boolean maintainJaianHpStock(Char char_, boolean escortActive) {
        final int target = 100;
        int count = AutoNhiemVuChinh.countSuitableHpPotions(char_, char_.clevel);
        if (count >= target) {
            this.jaianHpStockReady = true;
            System.out.println("AutoNVC escort=hp-stock-ready count=" + count + " level=" + char_.clevel);
            return false;
        }
        long now = System.currentTimeMillis();
        if (this.jaianHpBuyAttempts >= 3) {
            if (now - this.lastJaianHpBuyAction < 3500L) {
                return true;
            }
            this.jaianHpStockReady = true;
            this.notice("Khong mua du 100 binh HP, tiep tuc cuu Jaian voi " + count + " binh");
            return false;
        }
        // Khong route qua map khac de mua HP trong NV17; giu nhan vat tai khu
        // cuu Jaian va thu tai medicine shop hien co cua server.
        if (now - this.lastJaianHpBuyAction < 2500L) {
            return true;
        }
        if (GameScr.arrItemStackLock == null) {
            Service.gI().requestItem(7);
            GameCanvas.menu.showMenu = false;
            this.lastJaianHpBuyAction = now;
            System.out.println("AutoNVC escort=load-medicine-shop type=7 map=" + TileMap.mapID + " active=" + escortActive);
            return true;
        }
        Item potion = AutoNhiemVuChinh.findShopHpPotion(GameScr.arrItemStackLock, char_.clevel);
        if (potion == null) {
            GameCanvas.menu.showMenu = false;
            this.jaianHpStockReady = true;
            this.notice("Cua hang khong co binh HP phu hop cap " + char_.clevel + ", tiep tuc cuu Jaian");
            return false;
        }
        int quantity = target - count;
        System.out.println("AutoNVC escort=buy-hp template=" + potion.template.id + " level=" + potion.template.level + " quantity=" + quantity + " have=" + count + " attempt=" + (this.jaianHpBuyAttempts + 1));
        Service.gI().buyItem(potion.typeUI, potion.indexUI, quantity);
        GameCanvas.menu.showMenu = false;
        ++this.jaianHpBuyAttempts;
        this.lastJaianHpBuyAction = now;
        return true;
    }

    private static int countSuitableHpPotions(Char char_, int level) {
        int count = 0;
        if (char_ == null || char_.arrItemBag == null) {
            return 0;
        }
        for (int i = 0; i < char_.arrItemBag.length; ++i) {
            Item item = char_.arrItemBag[i];
            if (item == null || item.template == null || item.template.type != 16 || item.template.level > level) continue;
            count += item.quantity > 0 ? item.quantity : 1;
        }
        return count;
    }

    private static Item findShopHpPotion(Item[] items, int level) {
        Item best = null;
        if (items == null) {
            return null;
        }
        for (int i = 0; i < items.length; ++i) {
            Item item = items[i];
            if (item == null || item.template == null || item.template.type != 16 || item.template.level > level || best != null && item.template.level <= best.template.level) continue;
            best = item;
        }
        return best;
    }

    private static Npc findNpcByName(String string) {
        if (GameScr.vSet == null || string == null) {
            return null;
        }
        String string2 = string.toLowerCase();
        for (int i = 0; i < GameScr.vSet.size(); ++i) {
            Object object = GameScr.vSet.elementAt(i);
            if (!(object instanceof Npc)) continue;
            Npc npc = (Npc)object;
            if (npc.template == null || npc.template.name == null || npc.template.name.toLowerCase().indexOf(string2) < 0) continue;
            return npc;
        }
        return null;
    }

    private void doLetterDelivery(Char char_, int n, int n2, int n3) {
        if (n < 0 || n2 < 0) {
            this.notice("Kh\u00f4ng x\u00e1c \u0111\u1ecbnh \u0111\u01b0\u1ee3c ng\u01b0\u1eddi nh\u1eadn th\u01b0 (map=" + n + ", npc=" + n2 + ")");
            return;
        }
        if (TileMap.mapID != n) {
            this.notice("\u0110ang mang th\u01b0 t\u1edbi map " + n);
            this.a(n, -2, -1, -1);
            return;
        }
        Npc npc = GameScr.i((int)n2);
        if (npc == null) {
            this.notice("Ch\u01b0a th\u1ea5y ng\u01b0\u1eddi nh\u1eadn th\u01b0 NPC " + n2 + " t\u1ea1i map " + n);
            return;
        }
        if (Math.abs(char_.cx - npc.cx) > 22 || Math.abs(char_.cy - npc.cy) > 22) {
            Char.b((int)npc.cx, (int)npc.cy);
            return;
        }
        long l = System.currentTimeMillis();
        if (l - this.lastAction < 1600L) {
            return;
        }
        GameCanvas.menu.showMenu = false;
        char_.npcFocus = npc;
        System.out.println("AutoNVC delivery=send task=15 index=" + n3 + " map=" + n + " npc=" + n2 + " option=0");
        GameScr.b((int)n2, (int)0, (int)0);
        this.lastAction = l;
    }

    private static Char findJaianEscort(Char char_) {
        if (char_ != null && char_.b != null && GameScr.vCharInMap != null && GameScr.vCharInMap.contains((Object)char_.b)) {
            return char_.b;
        }
        if (GameScr.vCharInMap == null || char_ == null) {
            return null;
        }
        for (int i = 0; i < GameScr.vCharInMap.size(); ++i) {
            Char char_2 = (Char)GameScr.vCharInMap.elementAt(i);
            if (char_2 == null || char_2 == char_ || char_2.charID != -char_.charID) continue;
            char_.b = char_2;
            return char_2;
        }
        return null;
    }

    private static Mob findJaianThreat(Char jaian, int escortDirection) {
        if (jaian == null || GameScr.vMobAttack == null) {
            return null;
        }
        Mob best = null;
        int bestScore = Integer.MAX_VALUE;
        for (int i = 0; i < GameScr.vMobAttack.size(); ++i) {
            Mob mob = (Mob)GameScr.vMobAttack.elementAt(i);
            if (mob == null || mob.hp <= 0 || mob.status == 0 || mob.status == 1
                    || mob.isBoss || mob.levelBoss >= 2) {
                continue;
            }
            int dx = Math.abs(jaian.cx - mob.xFirst);
            int dy = Math.abs(jaian.cy - mob.yFirst);
            if (dx > JAIAN_THREAT_X_RANGE || dy > JAIAN_THREAT_Y_RANGE) {
                continue;
            }

            // Xet deu hai phia Jaian. Quai nam sat Jaian duoc uu tien tuyet doi;
            // sau do moi xet khoang cach. Khong bo qua quai phia sau nhu logic cu.
            int score = dx + dy * 2;
            if (dx <= JAIAN_THREAT_CLOSE_X && dy <= JAIAN_THREAT_CLOSE_Y) {
                score -= 1000;
            }
            if (score < bestScore) {
                best = mob;
                bestScore = score;
            }
        }
        return best;
    }

    private static int[] getLeftEscortExit(int n) {
        int[] nArray = new int[]{10, n};
        if (TileMap.vGo == null || TileMap.vGo.size() == 0) {
            return nArray;
        }
        Waypoint waypoint = null;
        for (int i = 0; i < TileMap.vGo.size(); ++i) {
            Waypoint waypoint2 = (Waypoint)TileMap.vGo.elementAt(i);
            if (waypoint2 == null || waypoint != null && waypoint2.minX >= waypoint.minX) continue;
            waypoint = waypoint2;
        }
        if (waypoint != null) {
            nArray[0] = Math.max(10, waypoint.minX + 12);
            nArray[1] = (waypoint.minY + waypoint.maxY) / 2;
        }
        return nArray;
    }

    private void doAdvancedTask(Char char_, Task task, int n, int n2) {
        int n3 = char_.ctaskId;
        int n4 = task.index;
        if (n3 == 29 && n4 == 1) {
            if (n != 15) {
                System.out.println("AutoNVC task29=overrideCombatMap metadata=" + n + " target=15 step=" + n4 + " count=" + task.count + "/" + n2);
            }
            n = 15;
        }
        if (n < 0) {
            n = AutoNhiemVuChinh.getAdvancedTaskMap(n3, n4);
        }
        if (n3 == 19) {
            this.doTaskNineteenWater(char_, task, n2);
            return;
        }
        if (n3 == 20) {
            this.doTaskTwentyWildBoarCave();
            return;
        }
        if (n3 == 23) {
            this.doTaskTwentyThreeDungeon();
            return;
        }
        if (n3 == 24 && this.doTreasureItemTask()) {
            return;
        }
        if (n3 == 27 && AutoNhiemVuChinh.findBagItem(237) == null) {
            this.notice("Task 27 thieu tinh the bang 237; can nhat/xin lai vat pham de tiep tuc");
        }
        if (n2 > 0 && task.count < n2) {
            this.doProgressTask(n, n2, task.count);
            return;
        }
        if (n >= 0) {
            this.doVisitTask(n);
            return;
        }
        this.doSpecialTask("Task dac biet " + n3 + "/" + n4 + ": " + AutoNhiemVuChinh.getStepName(task));
    }

    private void doTaskNineteenWater(Char char_, Task task, int n) {
        long l;
        if (n > 0 && task.count >= n) {
            this.notice("NV19: da lay du nuoc, dang cho server chuyen buoc");
            return;
        }
        if (TileMap.mapID != 63) {
            System.out.println("AutoNVC task19=routeWater from=" + TileMap.mapID + " target=63");
            this.routeToTaskMap(63, "lay nuoc NV19");
            return;
        }
        Item item = AutoNhiemVuChinh.findBagItem(219);
        if (item == null) {
            this.notice("NV19: khong tim thay binh rong 219 trong hanh trang");
            return;
        }
        AutoNhiemVuChinh.freezeMobsNear(1788, 330, 220);
        if (Math.abs(char_.cx - 1788) > 22 || Math.abs(char_.cy - 330) > 22) {
            char_.mobFocus = null;
            char_.itemFocus = null;
            Char.b((int)1788, (int)330);
            Auto.a((long)100L);
        }
        if ((l = System.currentTimeMillis()) - this.lastAction < 4000L) {
            return;
        }
        char_.mobFocus = null;
        char_.charFocus = null;
        char_.npcFocus = null;
        char_.itemFocus = null;
        short s = task.count;
        System.out.println("AutoNVC task19=useEmptyBottle bag=" + item.indexUI + " at=" + 1788 + "," + 330 + " countBefore=" + s);
        Service.gI().useItem(item.indexUI);
        this.lastAction = l;
        Auto.a((long)4000L);
        int n2 = char_.taskMaint == null ? -1 : (int)char_.taskMaint.count;
        System.out.println("AutoNVC task19=waterResult count=" + s + "->" + n2);
    }

    private void doTaskTwentyWildBoarCave() {
        if (TileMap.mapID == 74) {
            if (this.autoPickItems(221, true)) {
                return;
            }
            Mob mob = null;
            int n = Integer.MAX_VALUE;
            Char char_ = Char.getMyChar();
            if (GameScr.vMobAttack != null) {
                for (int i = 0; i < GameScr.vMobAttack.size(); ++i) {
                    int n2;
                    Mob mob2 = (Mob)GameScr.vMobAttack.elementAt(i);
                    if (mob2 == null || mob2.templateId != 69 || mob2.hp <= 0 || mob2.status == 0 || mob2.status == 1 || (n2 = Math.abs(char_.cx - mob2.xFirst) + Math.abs(char_.cy - mob2.yFirst)) >= n) continue;
                    mob = mob2;
                    n = n2;
                }
            }
            if (mob != null) {
                char_.itemFocus = null;
                char_.charFocus = null;
                char_.npcFocus = null;
                char_.mobFocus = mob;
                this.attackTaskTwentyBoarWithSkill(char_, mob);
            } else {
                this.notice("NV20: dang cho Heo Rung template 69 xuat hien trong hang");
            }
            return;
        }
        if (TileMap.mapID != 8) {
            System.out.println("AutoNVC task20=routeCave from=" + TileMap.mapID + " target=8");
            this.routeToTaskMap(8, "vao hang danh Heo Rung NV20");
            return;
        }
        if (GameCanvas.menu.showMenu) {
            if (GameCanvas.menu.selectMenuIndexForAuto(0)) {
                System.out.println("AutoNVC task20=selectEnterCave index=0");
                this.lastAction = System.currentTimeMillis();
            } else {
                this.notice("NV20: menu NPC 15 chua co nut Vao trong hang");
            }
            return;
        }
        if (System.currentTimeMillis() - this.lastAction < 2000L) {
            return;
        }
        Npc npc = GameScr.i((int)15);
        if (npc == null) {
            this.notice("NV20: khong tim thay NPC 15 de vao hang");
            return;
        }
        Char char_ = Char.getMyChar();
        char_.mobFocus = null;
        char_.itemFocus = null;
        char_.npcFocus = npc;
        if (Math.abs(char_.cx - npc.cx) > 22 || Math.abs(char_.cy - npc.cy) > 22) {
            Char.b((int)npc.cx, (int)npc.cy);
            return;
        }
        System.out.println("AutoNVC task20=openCaveMenu npc=15 map=8");
        Service.gI().openMenu(15);
        this.lastAction = System.currentTimeMillis();
    }

    private void attackTaskTwentyBoarWithSkill(Char char_, Mob mob) {
        int n;
        Skill skill = char_.myskill;
        if (!AutoNhiemVuChinh.isUsableAttackSkill(skill)) {
            skill = Auto.g;
        }
        if (!AutoNhiemVuChinh.isUsableAttackSkill(skill) && char_.vSkillFight != null) {
            for (n = 0; n < char_.vSkillFight.size(); ++n) {
                Skill skill2 = (Skill)char_.vSkillFight.elementAt(n);
                if (!AutoNhiemVuChinh.isUsableAttackSkill(skill2)) continue;
                skill = skill2;
                break;
            }
        }
        if (!AutoNhiemVuChinh.isUsableAttackSkill(skill)) {
            this.notice("NV20: khong tim thay ky nang tan cong hop le");
            return;
        }
        if (char_.cMP < skill.manaUse) {
            this.notice("NV20: dang cho hoi/phuc hoi MP de danh Heo Rung");
            return;
        }
        n = Math.abs(char_.cx - mob.xFirst);
        int n2 = Math.abs(char_.cy - mob.yFirst);
        if (n > skill.b() || n2 > skill.c()) {
            int n3 = TileMap.d((int)mob.xFirst, (int)mob.yFirst);
            System.out.println("AutoNVC task20=moveForSkill from=" + char_.cx + "," + char_.cy + " to=" + mob.xFirst + "," + n3 + " rawTargetY=" + mob.yFirst + " range=" + skill.b() + "," + skill.c());
            Char.b((int)mob.xFirst, (int)n3);
            Auto.a((long)100L);
        }
        long l = System.currentTimeMillis();
        long l2 = (long)skill.coolDown + 50L;
        if (l2 < 350L) {
            l2 = 350L;
        }
        if (l - skill.lastTimeUseThisSkill < l2) {
            return;
        }
        if (char_.myskill != skill) {
            Service.gI().selectSkill((int)skill.template.id);
            char_.myskill = skill;
            this.lastAction = l;
            return;
        }
        MyVector myVector = new MyVector();
        myVector.addElement((Object)mob);
        Service.gI().sendPlayerAttack(myVector, new MyVector(), 1);
        this.noteCombatAttempt(mob);
        skill.lastTimeUseThisSkill = l;
        skill.l = true;
        char_.cMP -= skill.manaUse;
        if (char_.cMP < 0) {
            char_.cMP = 0;
        }
        if (GameScr.s != null && skill.template.id >= 0 && skill.template.id < GameScr.s.length && GameScr.s[skill.template.id] != null) {
            char_.b(GameScr.s[skill.template.id], 0);
        }
        this.lastAction = l;
        System.out.println("AutoNVC task20=skillAttack mob=" + mob.mobId + " hp=" + mob.hp + "/" + mob.maxHp + " skill=" + skill.template.id + " mana=" + skill.manaUse + " mpLeft=" + char_.cMP);
    }

    private static boolean isUsableAttackSkill(Skill skill) {
        return skill != null && skill.template != null && skill.template.type == 1;
    }

    private static void freezeMobsNear(int n, int n2, int n3) {
        if (GameScr.vMobAttack == null) {
            return;
        }
        for (int i = 0; i < GameScr.vMobAttack.size(); ++i) {
            int n4;
            Mob mob = (Mob)GameScr.vMobAttack.elementAt(i);
            if (mob == null || mob.hp <= 0 || mob.status == 0 || mob.status == 1 || (n4 = Math.abs(mob.xFirst - n) + Math.abs(mob.yFirst - n2)) > n3) continue;
            mob.isDontMove = true;
            mob.isDisable = true;
        }
    }

    private void doTaskTwentyThreeDungeon() {
        if (TileMap.mapID == 78) {
            this.freezeTaskTwentyThreeMobs();
            if (this.autoPickItems(232, true)) {
                return;
            }
            if (this.autoPickItems(231, true)) {
                return;
            }
            if (GameScr.vMobAttack != null && GameScr.vMobAttack.size() > 0) {
                this.attackTracked(-1, -1);
            } else {
                this.notice("NV23: dang cho quai/thu linh hoac manh dia do trong pho ban");
            }
            return;
        }
        Item item = AutoNhiemVuChinh.findBagItem(231);
        if (item != null) {
            if (TileMap.mapID != 35) {
                this.routeToTaskMap(35, "dung chia khoa vao Hang Meiro NV23");
                return;
            }
            if (!this.useQuestItemAt(231, 1824, 432)) {
                this.notice("NV23: dang cho dung chia khoa vao Hang Meiro");
            }
            return;
        }
        if (TileMap.mapID != 48) {
            this.routeToTaskMap(48, "vao ph\u00f3 ban lay tam dia do NV23");
            return;
        }
        if (System.currentTimeMillis() - this.lastAction < 1600L) {
            return;
        }
        Npc npc = GameScr.i((int)23);
        if (npc == null) {
            this.notice("NV23: khong tim thay NPC 23 de vao ph\u00f3 ban");
            return;
        }
        System.out.println("AutoNVC task23=enterDungeon npc=23 map=48");
        GameScr.b((int)23, (int)0, (int)0);
        GameScr.gI().resetButton();
        this.lastAction = System.currentTimeMillis();
    }

    private void freezeTaskTwentyThreeMobs() {
        if (GameScr.vMobAttack == null) {
            return;
        }
        int n = 0;
        int n2 = 0;
        for (int i = 0; i < GameScr.vMobAttack.size(); ++i) {
            Mob mob = (Mob)GameScr.vMobAttack.elementAt(i);
            if (mob == null || mob.hp <= 0 || mob.status == 0 || mob.status == 1) continue;
            mob.isDontMove = true;
            mob.isDisable = true;
            ++n;
            if (mob.levelBoss != 3 && !mob.isBoss) continue;
            ++n2;
        }
        if (System.currentTimeMillis() - this.lastNotice >= 5000L) {
            System.out.println("AutoNVC task23=freezeMobs count=" + n + " leaders=" + n2);
        }
    }

    private void freezeFlyingMobs() {
        if (GameScr.vMobAttack == null || Mob.arrMobTemplate == null) {
            return;
        }
        for (int i = 0; i < GameScr.vMobAttack.size(); ++i) {
            MobTemplate mobTemplate;
            Mob mob = (Mob)GameScr.vMobAttack.elementAt(i);
            if (mob == null || mob.hp <= 0 || mob.status == 0 || mob.status == 1 || mob.templateId < 0 || mob.templateId >= Mob.arrMobTemplate.length || (mobTemplate = Mob.arrMobTemplate[mob.templateId]) == null || mobTemplate.typeFly == 0 && mobTemplate.type != 4) continue;
            mob.isDontMove = true;
            mob.isDisable = true;
        }
    }

    private void setJaianFreeze(boolean enabled) {
        if (enabled) {
            if (!this.jaianFreezeApplied) {
                this.jaianSavedMobFreeze = NSOT_MOB.ai;
                this.jaianSavedSkillFreeze = NSOT_MOB.aj;
                this.jaianFreezeApplied = true;
                System.out.println("AutoNVC escort=freeze-enabled savedMob=" + this.jaianSavedMobFreeze + " savedSkill=" + this.jaianSavedSkillFreeze);
            }
            NSOT_MOB.ai = true;
            NSOT_MOB.aj = true;
            return;
        }
        if (!this.jaianFreezeApplied) {
            return;
        }
        NSOT_MOB.ai = this.jaianSavedMobFreeze;
        NSOT_MOB.aj = this.jaianSavedSkillFreeze;
        for (int i = 0; i < this.jaianDontMoveMobs.size(); ++i) {
            Mob mob = (Mob)this.jaianDontMoveMobs.elementAt(i);
            if (mob != null) {
                mob.isDontMove = false;
            }
        }
        for (int i = 0; i < this.jaianDisabledMobs.size(); ++i) {
            Mob mob = (Mob)this.jaianDisabledMobs.elementAt(i);
            if (mob != null) {
                mob.isDisable = false;
            }
        }
        this.jaianDontMoveMobs.removeAllElements();
        this.jaianDisabledMobs.removeAllElements();
        this.jaianFreezeApplied = false;
        System.out.println("AutoNVC escort=freeze-restored mob=" + NSOT_MOB.ai + " skill=" + NSOT_MOB.aj);
    }

    private void freezeJaianEscortMobs() {
        if (GameScr.vMobAttack == null) {
            return;
        }
        for (int i = 0; i < GameScr.vMobAttack.size(); ++i) {
            Mob mob = (Mob)GameScr.vMobAttack.elementAt(i);
            if (mob == null || mob.hp <= 0 || mob.status == 0 || mob.status == 1) continue;
            if (!mob.isDontMove && !this.jaianDontMoveMobs.contains((Object)mob)) {
                this.jaianDontMoveMobs.addElement((Object)mob);
            }
            if (!mob.isDisable && !this.jaianDisabledMobs.contains((Object)mob)) {
                this.jaianDisabledMobs.addElement((Object)mob);
            }
            mob.isDontMove = true;
            mob.isDisable = true;
            mob.x = mob.xFirst;
            mob.y = mob.yFirst;
        }
    }

    private void doLevelFarm(Char char_, int n, int n2, String string) {
        this.doLevelFarm(char_, n, n2, string, false);
    }

    private void doLevelFarm(Char char_, int n, int n2, String string, boolean bl) {
        boolean bl2;
        if (!bl && !AutoNhiemVuPanel.autoOptimalMap) {
            n = TileMap.mapID;
        }
        long l = System.currentTimeMillis();
        if (this.lastFarmMap != n) {
            this.lastFarmMap = n;
            this.lastFarmProgress = l;
            this.lastFarmExp = char_.cEXP;
            System.out.println("AutoNVC farm=selectOptimal level=" + char_.clevel + " task=" + char_.ctaskId + " map=" + n + " targetLevel=" + n2 + " reason=" + string);
        }
        if (TileMap.mapID != n) {
            this.notice("Dang di map " + n + " de cay level " + n2 + " (" + string + ")");
            this.a(n, -2, -1, -1);
            return;
        }
        if (char_.cEXP != this.lastFarmExp) {
            this.lastFarmExp = char_.cEXP;
            this.lastFarmProgress = l;
        }
        // Level-gate farming (notably task 29 from Lv43 to Lv50) returns before
        // the normal task watchdog, so it needs the same combat recovery here.
        if (this.monitorCombatStall(char_)) {
            return;
        }
        int n3 = AutoNhiemVuChinh.countLiveMobs();
        if ((bl || AutoNhiemVuPanel.autoOptimalMap) && (n3 == 0 && l - this.lastFarmProgress >= 8000L || l - this.lastFarmProgress >= 45000L)) {
            System.out.println("AutoNVC farm=changeZone map=" + n + " zone=" + TileMap.zoneID + " liveMobs=" + n3 + " noExpMs=" + (l - this.lastFarmProgress));
            this.notice("Map " + n + " het quai/khong tang EXP, dang doi khu");
            this.b(TileMap.zoneID);
            this.lastFarmProgress = l;
            return;
        }
        this.notice("Dang cay level " + char_.clevel + "/" + n2 + " tai map " + n);
        boolean bl3 = bl2 = char_.ctaskId == 12 && AutoNhiemVuChinh.isUpgradeTaskStep(char_.ctaskId, char_.taskMaint);
        if (!bl2 && this.autoPickItems(-1)) {
            return;
        }
        this.attackFarmMob(char_);
    }

    private boolean attackFarmMob(Char char_) {
        Skill skill = AutoNhiemVuChinh.getPreferredAttackSkill(char_);
        if (skill == null || skill.template == null) {
            this.notice("Khong co ky nang tan cong de tan sat");
            return false;
        }
        if (char_.myskill != skill) {
            Auto.g = skill;
            Service.gI().selectSkill((int)skill.template.id);
            char_.myskill = skill;
            this.lastAction = System.currentTimeMillis();
            return true;
        }
        this.attackTracked(-1, this.a(true, true, false, false));
        return true;
    }

    private void attackTracked(int mobTemplateId, int mobRankMask) {
        this.c(mobTemplateId, mobRankMask);
        Char char_ = Char.getMyChar();
        this.noteCombatAttempt(char_ == null ? null : char_.mobFocus);
    }

    private void noteCombatAttempt(Mob mob) {
        if (mob == null || mob.hp <= 0 || mob.status == 0 || mob.status == 1) {
            return;
        }
        long now = System.currentTimeMillis();
        if (this.combatMap != TileMap.mapID) {
            this.resetCombatWatchdog();
            this.combatMap = TileMap.mapID;
        }
        if (this.combatLastAttempt <= 0L
                || now - this.combatLastAttempt > COMBAT_ATTEMPT_ACTIVE_DELAY) {
            this.combatLastProgress = now;
            this.combatAttempts = 0;
            this.combatMobId = -1;
            this.combatMobHp = -1;
        }
        this.combatLastAttempt = now;
        ++this.combatAttempts;
        if (this.combatMobId != mob.mobId) {
            this.combatMobId = mob.mobId;
            this.combatMobHp = mob.hp;
        }
    }

    private boolean monitorCombatStall(Char char_) {
        if (this.combatReloginRequested || System.currentTimeMillis() < this.respawnGraceUntil) {
            return this.combatReloginRequested;
        }
        long now = System.currentTimeMillis();
        CombatStallRecovery.observe(char_, this.combatLastAttempt, this.combatAttempts);
        if (this.combatMap != TileMap.mapID) {
            this.resetCombatWatchdog();
            this.combatMap = TileMap.mapID;
            return false;
        }
        if (char_.cEXP != this.combatExp) {
            this.combatExp = char_.cEXP;
            this.combatLastProgress = now;
            this.combatAttempts = 0;
        }
        Mob mob = char_.mobFocus;
        if (mob != null) {
            if (this.combatMobId == mob.mobId) {
                if (this.combatMobHp >= 0 && mob.hp < this.combatMobHp) {
                    this.combatLastProgress = now;
                    this.combatAttempts = 0;
                }
                this.combatMobHp = mob.hp;
            } else {
                this.combatMobId = mob.mobId;
                this.combatMobHp = mob.hp;
            }
        }
        if (this.combatLastAttempt <= 0L
                || now - this.combatLastAttempt > COMBAT_ATTEMPT_ACTIVE_DELAY) {
            return false;
        }
        Session_ME session = Session_ME.getInstance();
        boolean receiveStalled = session != null && session.connected && session.j > 0L
                && now - session.j >= COMBAT_RX_STALL_DELAY;
        boolean noExpStalled = CombatStallRecovery.isNoExpTimedOut(char_);
        if (!receiveStalled && !noExpStalled) {
            return false;
        }
        if (CombatStallRecovery.tryNearbyMob(char_, this.combatMobId)) {
            this.resetCombatWatchdog();
            return true;
        }
        return this.forceCombatReconnect(receiveStalled ? "server-khong-phan-hoi" : "danh-khong-co-exp-sau-khi-doi-quai");
    }

    private boolean forceCombatReconnect(String reason) {
        if (this.combatReloginRequested) {
            return true;
        }
        this.combatReloginRequested = true;
        this.combatReconnectReadyAt = 0L;
        Char char_ = Char.getMyChar();
        this.clearTransientCombatState(char_);
        long now = System.currentTimeMillis();
        Session_ME session = Session_ME.getInstance();
        long lastReceiveMs = session == null || session.j <= 0L ? -1L : now - session.j;
        System.out.println("AutoNVC combat=force-relogin reason=" + reason
                + " map=" + TileMap.mapID + " mob=" + this.combatMobId
                + " hp=" + this.combatMobHp + " attempts=" + this.combatAttempts
                + " noProgressMs=" + (now - this.combatLastProgress)
                + " lastReceiveMs=" + lastReceiveMs);
        GameScr.addChatPopup((String)"Mat dong bo server, dang dang nhap lai de tiep tuc auto");
        if (session != null) {
            session.cleanNetwork();
            session.e();
        }
        return true;
    }

    private boolean waitForCombatReconnect(Char char_, Session_ME session) {
        if (session == null || !session.connected || GameCanvas.currentScreen != GameScr.instance) {
            this.combatReconnectReadyAt = 0L;
            this.clearTransientCombatState(char_);
            return true;
        }
        long now = System.currentTimeMillis();
        if (this.combatReconnectReadyAt <= 0L) {
            this.combatReconnectReadyAt = now + RECONNECT_GAME_READY_DELAY;
            this.clearTransientCombatState(char_);
            return true;
        }
        if (now < this.combatReconnectReadyAt || char_ == null || char_.cHp <= 0
                || char_.statusMe == 14 || char_.statusMe == 5) {
            this.clearTransientCombatState(char_);
            return true;
        }
        this.combatReloginRequested = false;
        this.combatReconnectReadyAt = 0L;
        this.networkRecoveryActive = false;
        this.networkRecoveryReadyAt = 0L;
        this.respawnGraceUntil = now + RESPAWN_GRACE_DELAY;
        this.resetCombatWatchdog();
        this.lastAction = 0L;
        this.lastEscortAttack = 0L;
        this.lastEscortMove = 0L;
        this.notice("Da dong bo lai ket noi, tiep tuc Auto NV");
        System.out.println("AutoNVC combat=relogin-recovered map=" + TileMap.mapID);
        return true;
    }

    private boolean waitForNetworkRecovery(Char char_, Session_ME session) {
        if (session == null || !session.connected || GameCanvas.currentScreen != GameScr.instance) {
            this.networkRecoveryReadyAt = 0L;
            this.clearTransientCombatState(char_);
            return true;
        }
        long now = System.currentTimeMillis();
        if (this.networkRecoveryReadyAt <= 0L) {
            this.networkRecoveryReadyAt = now + RECONNECT_GAME_READY_DELAY;
            this.clearTransientCombatState(char_);
            return true;
        }
        if (now < this.networkRecoveryReadyAt || char_ == null || char_.cHp <= 0
                || char_.statusMe == 14 || char_.statusMe == 5) {
            this.clearTransientCombatState(char_);
            return true;
        }
        this.networkRecoveryActive = false;
        this.networkRecoveryReadyAt = 0L;
        this.respawnGraceUntil = now + RESPAWN_GRACE_DELAY;
        this.resetCombatWatchdog();
        this.lastAction = 0L;
        this.lastEscortAttack = 0L;
        this.lastEscortMove = 0L;
        System.out.println("AutoNVC network=recovered map=" + TileMap.mapID);
        return true;
    }

    private void handleDeathRecovery(Char char_) {
        long now = System.currentTimeMillis();
        if (!this.deathRecoveryActive) {
            this.deathRecoveryActive = true;
            this.restoreSavedReturnPoint = this.savedReturnMap >= 0;
            this.lastDeathReturnAction = 0L;
            this.setJaianFreeze(false);
            this.resetJaianMoveTracking();
            this.clearTransientCombatState(char_);
            this.resetCombatWatchdog();
            System.out.println("AutoNVC death=detected map=" + TileMap.mapID + " hp=" + char_.cHp
                    + " status=" + char_.statusMe);
        }
        this.clearTransientCombatState(char_);
        if (now - this.lastDeathReturnAction >= DEATH_RETURN_COOLDOWN) {
            this.lastDeathReturnAction = now;
            Auto.a((boolean)true);
        }
    }

    private void handleRespawnRecovery(Char char_) {
        long now = System.currentTimeMillis();
        this.deathRecoveryActive = false;
        this.lastDeathReturnAction = 0L;
        this.respawnGraceUntil = now + RESPAWN_GRACE_DELAY;
        this.clearTransientCombatState(char_);
        this.resetCombatWatchdog();
        this.lastAction = 0L;
        this.lastEscortAttack = 0L;
        this.lastEscortMove = 0L;
        this.resetJaianMoveTracking();
        Skill skill = AutoNhiemVuChinh.getPreferredAttackSkill(char_);
        if (skill != null && skill.template != null) {
            Auto.g = skill;
            char_.myskill = skill;
            Service.gI().selectSkill((int)skill.template.id);
        }
        System.out.println("AutoNVC death=respawn-reset map=" + TileMap.mapID + " at=" + char_.cx + "," + char_.cy);
        this.notice("Da hoi sinh, reset trang thai danh va dong bo lai auto");
    }

    private void clearTransientCombatState(Char char_) {
        if (char_ == null) {
            return;
        }
        char_.mobFocus = null;
        char_.charFocus = null;
        char_.itemFocus = null;
        char_.npcFocus = null;
        char_.currentMovePoint = null;
    }

    private void markCombatProgress() {
        Char char_ = Char.getMyChar();
        this.combatLastProgress = System.currentTimeMillis();
        this.combatExp = char_ == null ? 0L : char_.cEXP;
        this.combatAttempts = 0;
    }

    private void resetCombatWatchdog() {
        Char char_ = Char.getMyChar();
        this.combatLastAttempt = 0L;
        this.combatLastProgress = System.currentTimeMillis();
        this.combatExp = char_ == null ? 0L : char_.cEXP;
        this.combatMap = TileMap.mapID;
        this.combatMobId = -1;
        this.combatMobHp = -1;
        this.combatAttempts = 0;
        if (this.combatReconnectReadyAt == 0L) {
            this.combatReloginRequested = false;
        }
    }

    private static Mob getNearestFarmMob(Char char_) {
        int n;
        Mob mob;
        int n2;
        if (char_ == null || GameScr.vMobAttack == null) {
            return null;
        }
        Mob mob2 = null;
        int n3 = Integer.MAX_VALUE;
        for (n2 = 0; n2 < GameScr.vMobAttack.size(); ++n2) {
            mob = (Mob)GameScr.vMobAttack.elementAt(n2);
            if (mob == null || mob.hp <= 0 || mob.status == 0 || mob.status == 1 || mob.levelBoss == 3 || (n = Math.abs(char_.cx - mob.xFirst) + Math.abs(char_.cy - mob.yFirst)) >= n3) continue;
            mob2 = mob;
            n3 = n;
        }
        if (mob2 == null) {
            for (n2 = 0; n2 < GameScr.vMobAttack.size(); ++n2) {
                mob = (Mob)GameScr.vMobAttack.elementAt(n2);
                if (mob == null || mob.hp <= 0 || mob.levelBoss == 3 || (n = Math.abs(char_.cx - mob.xFirst) + Math.abs(char_.cy - mob.yFirst)) >= n3) continue;
                mob2 = mob;
                n3 = n;
            }
            if (mob2 != null) {
                System.out.println("AutoNVC farm=fallback-stale-mob-status mob=" + mob2.mobId + " status=" + mob2.status + " hp=" + mob2.hp);
            }
        }
        return mob2;
    }

    private static int countLiveMobs() {
        Mob mob;
        int n;
        if (GameScr.vMobAttack == null) {
            return 0;
        }
        int n2 = 0;
        for (n = 0; n < GameScr.vMobAttack.size(); ++n) {
            mob = (Mob)GameScr.vMobAttack.elementAt(n);
            if (mob == null || mob.hp <= 0 || mob.status == 0 || mob.status == 1 || mob.levelBoss == 3) continue;
            ++n2;
        }
        if (n2 == 0) {
            for (n = 0; n < GameScr.vMobAttack.size(); ++n) {
                mob = (Mob)GameScr.vMobAttack.elementAt(n);
                if (mob == null || mob.hp <= 0 || mob.levelBoss == 3) continue;
                ++n2;
            }
        }
        return n2;
    }

    private void rememberSafeReturnPoint(Char char_) {
        if (!AutoNhiemVuChinh.isVillageMap(TileMap.mapID) || char_.cHp <= 0) {
            return;
        }
        if (this.savedReturnMap == TileMap.mapID && Math.abs(this.savedReturnX - char_.cx) < 12 && Math.abs(this.savedReturnY - char_.cy) < 12) {
            return;
        }
        this.savedReturnMap = TileMap.mapID;
        this.savedReturnX = char_.cx;
        this.savedReturnY = char_.cy;
        System.out.println("AutoNVC death=save-safe-point map=" + this.savedReturnMap + " at=" + this.savedReturnX + "," + this.savedReturnY);
    }

    private boolean doTreasureItemTask() {
        int[] nArray = new int[]{233, 234, 235};
        int[] nArray2 = new int[]{66, 13, 52};
        int[] nArray3 = new int[]{68, 528, 1260};
        int[] nArray4 = new int[]{360, 456, 432};
        for (int i = 0; i < nArray.length; ++i) {
            Item item = AutoNhiemVuChinh.findBagItem(nArray[i]);
            if (item == null) continue;
            if (TileMap.mapID != nArray2[i]) {
                this.routeToTreasureMap(nArray2[i], nArray[i]);
                return true;
            }
            int[] nArray5 = this.findTreasureImageTarget(item);
            if (nArray5 != null) {
                return this.useQuestItemAt(nArray[i], nArray5[0], nArray5[1]);
            }
            System.out.println("AutoNVC treasure=image-not-found item=" + nArray[i] + " map=" + TileMap.mapID + " fallback=" + nArray3[i] + "," + nArray4[i]);
            return this.useQuestItemAt(nArray[i], nArray3[i], nArray4[i]);
        }
        this.notice("Task 24 khong con dia do 233/234/235; can xin lai vat pham nhiem vu");
        return false;
    }

    private void routeToTreasureMap(int n, int n2) {
        if (n == 13 && !AutoNhiemVuChinh.isTreasureThirteenCorridor(TileMap.mapID)) {
            if (AutoNhiemVuChinh.isLangChaiLandCorridorMap(TileMap.mapID) && TileMap.mapID != 27) {
                System.out.println("AutoNVC treasure=leaveLangChai item=" + n2 + " from=" + TileMap.mapID + " via=27 target=13");
                this.notice("NV24: dang roi Lang Chai bang duong bo qua map 27");
                this.a(27, -2, -1, -1);
                return;
            }
            System.out.println("AutoNVC treasure=route item=" + n2 + " from=" + TileMap.mapID + " via=17 target=13");
            this.notice("NV24: dang di map 13 qua tuyen 17-16-15-14, tranh cong map 57 bi khoa");
            this.a(17, -2, -1, -1);
            return;
        }
        System.out.println("AutoNVC treasure=route item=" + n2 + " from=" + TileMap.mapID + " target=" + n);
        this.a(n, -2, -1, -1);
    }

    private static boolean isTreasureThirteenCorridor(int n) {
        return n == 17 || n == 16 || n == 15 || n == 14 || n == 13;
    }

    private int[] findTreasureImageTarget(Item item) {
        if (item == null || item.r == null || TileMap.f == null || TileMap.a <= 0 || TileMap.b <= 0) {
            return null;
        }
        if (this.treasureImageItem == item.template.id && this.treasureImageMap == TileMap.mapID && this.treasureImageX >= 0 && this.treasureImageY >= 0) {
            return new int[]{this.treasureImageX, this.treasureImageY};
        }
        try {
            int n;
            int n2;
            int n3;
            int n4;
            int n5;
            Image image = item.r;
            int n6 = image.getWidth();
            int n7 = image.getHeight();
            Image image2 = GameCanvas.loadImage((String)("/t/" + TileMap.tileID + ".png"));
            if (image2 == null || n6 < 8 || n7 < 8) {
                return null;
            }
            int[] nArray = new int[n6 * n7];
            int[] nArray2 = new int[image2.getWidth() * image2.getHeight()];
            image.getRGB(nArray, 0, n6, 0, 0, n6, n7);
            image2.getRGB(nArray2, 0, image2.getWidth(), 0, 0, image2.getWidth(), image2.getHeight());
            int[] nArray3 = new int[16];
            int[] nArray4 = new int[16];
            int n8 = 0;
            for (n5 = 1; n5 <= 5 && n8 < nArray3.length; ++n5) {
                for (n4 = 1; n4 <= 5 && n8 < nArray3.length; ++n4) {
                    n3 = n5 * (n7 - 1) / 6;
                    n2 = n4 * (n6 - 1) / 6;
                    n = nArray[n3 * n6 + n2];
                    if (n >>> 24 == 0 || (n >> 16 & 0xFF) < 18 && (n >> 8 & 0xFF) < 18 && (n & 0xFF) < 18) continue;
                    nArray3[n8] = n2;
                    nArray4[n8++] = n3;
                }
            }
            if (n8 < 6) {
                return null;
            }
            n5 = image2.getWidth();
            n4 = image2.getHeight();
            n2 = -1;
            n3 = -1;
            n = -1;
            int n9 = TileMap.a * 24 - n6;
            int n10 = TileMap.b * 24 - n7;
            for (int i = 0; i <= n10; i += 2) {
                for (int j = 0; j <= n9; j += 2) {
                    int n11 = 0;
                    for (int k = 0; k < n8; ++k) {
                        int n12;
                        int n13;
                        int n14;
                        int n15 = this.getTreasureMapPixel(j + nArray3[k], i + nArray4[k], nArray2, n5, n4);
                        int n16 = Math.abs((n15 >> 16 & 0xFF) - ((n14 = nArray[nArray4[k] * n6 + nArray3[k]]) >> 16 & 0xFF));
                        if (n16 + (n13 = Math.abs((n15 >> 8 & 0xFF) - (n14 >> 8 & 0xFF))) + (n12 = Math.abs((n15 & 0xFF) - (n14 & 0xFF))) > 42) continue;
                        ++n11;
                    }
                    if (n11 <= n2) continue;
                    n2 = n11;
                    n3 = j;
                    n = i;
                }
            }
            if (n2 * 100 < n8 * 70) {
                System.out.println("AutoNVC treasure=image-no-match item=" + item.template.id + " map=" + TileMap.mapID + " image=" + n6 + "x" + n7 + " score=" + n2 + "/" + n8);
                return null;
            }
            this.treasureImageItem = item.template.id;
            this.treasureImageMap = TileMap.mapID;
            this.treasureImageX = n3 + n6 / 2;
            this.treasureImageY = n + n7 / 2;
            System.out.println("AutoNVC treasure=image-match item=" + item.template.id + " map=" + TileMap.mapID + " image=" + n6 + "x" + n7 + " score=" + n2 + "/" + n8 + " target=" + this.treasureImageX + "," + this.treasureImageY);
            return new int[]{this.treasureImageX, this.treasureImageY};
        }
        catch (Exception exception) {
            System.out.println("AutoNVC treasure=image-error " + exception);
            return null;
        }
    }

    private int getTreasureMapPixel(int n, int n2, int[] nArray, int n3, int n4) {
        if (n < 0 || n2 < 0 || n >= TileMap.a * 24 || n2 >= TileMap.b * 24) {
            return 0;
        }
        int n5 = n / 24;
        int n6 = n2 / 24;
        int n7 = TileMap.f[n6 * TileMap.a + n5] - '\u0001';
        int n8 = n % 24;
        int n9 = n7 * 24 + n2 % 24;
        if (n7 < 0 || n8 >= n3 || n9 < 0 || n9 >= n4) {
            return 0;
        }
        return nArray[n9 * n3 + n8];
    }

    private boolean useQuestItemAt(int n, int n2, int n3) {
        int n4;
        Item item = AutoNhiemVuChinh.findBagItem(n);
        if (item == null || System.currentTimeMillis() - this.lastAction < 1600L) {
            return false;
        }
        Char char_ = Char.getMyChar();
        int n5 = n4 = n2 >= 0 && n3 >= 0 ? TileMap.d((int)n2, (int)n3) : n3;
        if (n2 >= 0 && n3 >= 0 && (Math.abs(char_.cx - n2) > 22 || Math.abs(char_.cy - n4) > 22)) {
            System.out.println("AutoNVC treasure=move item=" + n + " map=" + TileMap.mapID + " raw=" + n2 + "," + n3 + " stand=" + n2 + "," + n4 + " from=" + char_.cx + "," + char_.cy);
            Char.b((int)n2, (int)n4);
            this.lastAction = System.currentTimeMillis();
            return true;
        }
        System.out.println("AutoNVC action=useQuestItem template=" + n + " bag=" + item.indexUI + " map=" + TileMap.mapID + " at=" + char_.cx + "," + char_.cy);
        Service.gI().useItem(item.indexUI);
        this.lastAction = System.currentTimeMillis();
        return true;
    }

    private static Item findBagItem(int n) {
        Char char_ = Char.getMyChar();
        if (char_ == null || char_.arrItemBag == null) {
            return null;
        }
        for (int i = 0; i < char_.arrItemBag.length; ++i) {
            Item item = char_.arrItemBag[i];
            if (item == null || item.template == null || item.template.id != n) continue;
            return item;
        }
        return null;
    }

    private boolean returnTownBeforeNewTaskRoute(Char char_) {
        if (AutoNhiemVuChinh.isVillageMap(TileMap.mapID)) {
            this.taskTownResetAttempts = 0;
            return false;
        }
        long l = System.currentTimeMillis();
        if (this.taskTownResetId != char_.ctaskId) {
            this.taskTownResetId = char_.ctaskId;
            this.taskTownResetAttempts = 0;
            this.lastTaskTownResetAt = 0L;
        }
        if (l - this.lastTaskTownResetAt < 4500L) {
            return true;
        }
        if (this.taskTownResetAttempts > 0) {
            this.notice("Khong ve lang bang tu sat duoc, dang di duong bo an toan");
            return false;
        }
        char_.mobFocus = null;
        char_.charFocus = null;
        char_.npcFocus = null;
        char_.currentMovePoint = null;
        GameCanvas.menu.showMenu = false;
        Service.gI().returnTownFromDead();
        TileMap.h();
        Char.b((int)char_.cx, (int)TileMap.d);
        this.lastTaskTownResetAt = l;
        ++this.taskTownResetAttempts;
        System.out.println("AutoNVC taskTransition=suicide task=" + char_.ctaskId + " fromMap=" + TileMap.mapID + " attempt=" + this.taskTownResetAttempts + " bottomY=" + TileMap.d);
        this.notice("Da nhan/xong nhiem vu, dang tu sat ve lang/truong truoc khi di map moi");
        return true;
    }

    private static boolean isVillageMap(int n) {
        return n == 1 || n == 10 || n == 17 || n == 22 || n == 27 || n == 32 || n == 38 || n == 43 || n == 48 || n == 72;
    }

    private static int getRequiredLevel(int n) {
        return n >= 0 && n < TASK_LEVEL_GATE.length ? TASK_LEVEL_GATE[n] : 0;
    }

    private static int getStepRequiredLevel(int n, Task task) {
        String string;
        if (task != null && task.index == 0) {
            if (n == 7) {
                return 8;
            }
            if (n == 8) {
                return 9;
            }
        }
        if (!AutoNhiemVuChinh.isLevelStep(string = AutoNhiemVuChinh.getStepName(task))) {
            return 0;
        }
        int n2 = 0;
        boolean bl = false;
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if (c >= '0' && c <= '9') {
                n2 = n2 * 10 + c - 48;
                bl = true;
                continue;
            }
            if (bl) break;
        }
        return n2;
    }

    private static boolean isLevelStep(String string) {
        if (string == null) {
            return false;
        }
        String string2 = string.toLowerCase();
        return string2.indexOf("dat cap") >= 0 || string2.indexOf("cap do") >= 0 || string2.indexOf("level") >= 0 || string2.indexOf("\u0111\u1ea1t c\u1ea5p") >= 0 || string2.indexOf("c\u1ea5p \u0111\u1ed9") >= 0 || string2.indexOf("tr\u00ecnh \u0111\u1ed9") >= 0;
    }

    private static int getStepLevelFarmMap(int n, int n2, int n3) {
        if (n2 == 0 && n == 7) {
            return 71;
        }
        if (n2 == 0 && n == 8) {
            return 26;
        }
        return AutoNhiemVuChinh.getOptimalFarmMap(n, n3);
    }

    private static int getLevelFarmMap(int n) {
        return n < 30 ? 37 : 52;
    }

    private static int getTaskLevelMap(int n, int n2) {
        if (n == 17 && n2 < 25) {
            System.out.println("AutoNVC farm=task17-level-gate map=31 charLevel=" + n2);
            return 31;
        }
        return AutoNhiemVuChinh.getOptimalFarmMap(n, n2);
    }

    private static int getOptimalFarmMap(int n, int n2) {
        int n3;
        int n4 = -1;
        int n5 = -1;
        for (n3 = 0; n3 < FARM_MAPS.length; ++n3) {
            int n6 = FARM_MOB_LEVELS[n3];
            if (n6 > n2 || n6 <= n5) continue;
            n5 = n6;
            n4 = FARM_MAPS[n3];
        }
        if (n4 < 0) {
            for (n3 = 0; n3 < FARM_MAPS.length; ++n3) {
                if (n4 >= 0 && FARM_MOB_LEVELS[n3] >= n5) continue;
                n4 = FARM_MAPS[n3];
                n5 = FARM_MOB_LEVELS[n3];
            }
        }
        n3 = Math.abs(n2 - n5);
        System.out.println("AutoNVC farm=findByLevel charLevel=" + n2 + " mobLevel=" + n5 + " difference=" + n3 + " map=" + n4 + " task=" + n + " lowerFirst=true");
        return n4;
    }

    private static int getAdvancedTaskMap(int n, int n2) {
        switch (n) {
            case 17: {
                return 33;
            }
            case 18: {
                return n2 == 1 ? 240 : 11;
            }
            case 19: {
                return 63;
            }
            case 20: {
                return 8;
            }
            case 21: {
                return n2 == 1 ? 12 : 14;
            }
            case 22: {
                return 28;
            }
            case 23: {
                return 78;
            }
            case 24: {
                return 66;
            }
            case 25: {
                return n2 == 1 ? 13 : 37;
            }
            case 26: {
                return 52;
            }
            case 27: {
                return 14;
            }
            case 28: {
                return n2 == 1 ? 14 : 15;
            }
            case 29: {
                return 15;
            }
            case 31: {
                return n2 == 1 ? 16 : 68;
            }
            case 32: {
                return 31;
            }
        }
        return -3;
    }

    private void doTalkFallback(int n) {
        if (n >= 0) {
            this.doVisitTask(n);
        } else {
            this.doSpecialTask("Dang cho server mo buoc hoi thoai tiep theo");
        }
    }

    private void doChooseClass(Char char_) {
        int n;
        Item item;
        int n2;
        short s;
        if (char_.clevel < 10) {
            this.doLevelFarm(char_, 26, 10, "du level 10 de nhap hoc", true);
            return;
        }
        int n3 = AutoNhiemVuPanel.selectedClass;
        if (n3 <= 2) {
            s = 1;
            n2 = 9;
        } else if (n3 <= 4) {
            s = 72;
            n2 = 10;
        } else {
            s = 27;
            n2 = 11;
        }
        String string = AutoNhiemVuPanel.getSelectedClassName();
        System.out.println("AutoNVC chooseClass id=" + n3 + " name=" + string + " map=" + s + " npc=" + n2);
        if (TileMap.mapID != s) {
            this.a(s, -2, -1, -1);
            return;
        }
        if (this.classPrepareStep == 0) {
            if (System.currentTimeMillis() - this.lastAction < 1600L) {
                return;
            }
            if (GameScr.i((int)5) != null) {
                System.out.println("AutoNVC chooseClass=prepare npc=5 menu=1 option=0");
                GameScr.b((int)5, (int)1, (int)0);
                GameCanvas.menu.showMenu = false;
                this.lastAction = System.currentTimeMillis();
                this.classPrepareStep = 1;
                return;
            }
            this.classPrepareStep = 1;
        }
        if ((item = AutoNhiemVuChinh.findBagItem(22)) == null) {
            item = AutoNhiemVuChinh.findBagItem(27);
        }
        if (item != null) {
            if (System.currentTimeMillis() - this.lastAction >= 1600L) {
                System.out.println("AutoNVC chooseClass=useTutorialItem template=" + item.template.id + " bag=" + item.indexUI);
                Service.gI().useItem(item.indexUI);
                this.lastAction = System.currentTimeMillis();
            }
            return;
        }
        boolean bl = char_.arrItemBody != null && char_.arrItemBody.length > 1 && char_.arrItemBody[1] != null;
        int n4 = n = bl ? 3 : 2;
        if (Char.af() < n) {
            Item item2 = AutoNhiemVuChinh.findClassEntrySaleItem(char_);
            if (item2 != null && System.currentTimeMillis() - this.lastAction >= 1600L) {
                int n5 = item2.template.isUpToUp && item2.quantity > 1 ? item2.quantity : 1;
                System.out.println("AutoNVC chooseClass=freeBag sale=" + item2.template.id + " name=" + item2.template.name + " bag=" + item2.indexUI + " quantity=" + n5 + " lock=" + item2.isLock + " free=" + Char.af() + "/" + n);
                Service.gI().saleItem(item2.indexUI, n5);
                this.lastInventoryAction = this.lastAction = System.currentTimeMillis();
                return;
            }
            this.notice("Can trong " + n + " o hanh trang de nhan sach va vu khi cua phai");
            return;
        }
        if (bl) {
            if (System.currentTimeMillis() - this.lastAction >= 1600L) {
                System.out.println("AutoNVC chooseClass=unequipWeapon template=" + char_.arrItemBody[1].template.id);
                Service.gI().itemBodyToBag(1);
                this.lastAction = System.currentTimeMillis();
            }
            return;
        }
        System.out.println("AutoNVC chooseClass=menu npc=" + n2 + " class=" + string);
        this.goToNpc(s, n2, "nhap phai " + string, JOIN_CLASS_MENU + n3);
    }

    private static boolean hasNoClass(Char char_) {
        return char_ == null || char_.nClass == null || char_.nClass.classId == 0;
    }

    private static Item findClassEntrySaleItem(Char char_) {
        Item item = AutoNhiemVuChinh.findUnsuitableEquipment(char_);
        if (item != null) {
            return item;
        }
        if (char_.arrItemBag != null) {
            Item item2;
            int n;
            int n2;
            for (n2 = 0; n2 < char_.arrItemBag.length; ++n2) {
                Item item3 = char_.arrItemBag[n2];
                if (item3 == null || item3.template == null || item3.upgrade != 0 || item3.template.type < 0 || item3.template.type > 9 || item3.template.level >= 10) continue;
                return item3;
            }
            n2 = AutoNhiemVuPanel.selectedClass;
            int s = n2 > 0 && n2 < CLASS_WEAPON.length ? CLASS_WEAPON[n2] : -1;
            for (n = 0; n < char_.arrItemBag.length; ++n) {
                item2 = char_.arrItemBag[n];
                if (item2 == null || item2.template == null || item2.template.id == s || item2.isLock || item2.upgrade != 0 || item2.template.type < 0 || item2.template.type > 9) continue;
                return item2;
            }
            for (n = 0; n < char_.arrItemBag.length; ++n) {
                item2 = char_.arrItemBag[n];
                if (item2 == null || item2.template == null || item2.isLock || item2.template.type != 16 && item2.template.type != 17 && item2.template.type != 18) continue;
                return item2;
            }
        }
        return null;
    }

    private void doClassSetup(Char char_) {
        Item item;
        int n;
        int n2 = n = char_.nClass == null ? 0 : char_.nClass.classId;
        if (n <= 0 || n >= CLASS_WEAPON.length) {
            this.doChooseClass(char_);
            return;
        }
        if (System.currentTimeMillis() - this.lastAction < 1600L) {
            return;
        }
        Item item2 = AutoNhiemVuChinh.findClassSkillBook(char_, n);
        if (item2 != null) {
            System.out.println("AutoNVC classSetup=useSkillBook class=" + n + " template=" + item2.template.id + " type=" + item2.template.type + " name=" + item2.template.name + " bag=" + item2.indexUI);
            Service.gI().useItem(item2.indexUI);
            this.lastAction = System.currentTimeMillis();
            return;
        }
        if (char_.arrItemBody != null && char_.arrItemBody.length > 1 && char_.arrItemBody[1] == null && (item = AutoNhiemVuChinh.findBagItem(CLASS_WEAPON[n])) != null) {
            System.out.println("AutoNVC classSetup=equipWeapon class=" + n + " template=" + item.template.id + " bag=" + item.indexUI);
            Service.gI().useItem(item.indexUI);
            this.lastAction = System.currentTimeMillis();
            return;
        }
        if (char_.ctaskId == 9 && char_.taskMaint != null && char_.taskMaint.index == 1 && char_.sPoint > 0) {
            SkillTemplate skillTemplate = AutoNhiemVuChinh.getEligibleQuestSkill(char_);
            if (skillTemplate != null) {
                Skill skill = char_.a(skillTemplate);
                int n3 = skill == null ? 0 : skill.point;
                int n4 = n3 + 1;
                int n5 = skillTemplate.skills[n4].level;
                System.out.println("AutoNVC classSetup=questSkill id=" + skillTemplate.id + " name=" + skillTemplate.name + " currentPoint=" + n3 + " nextPoint=" + n4 + " requiredLevel=" + n5 + " charLevel=" + char_.clevel + " availablePoint=" + char_.sPoint);
                Service.gI().upSkill((int)skillTemplate.id, 1);
                this.lastBuildAction = this.lastAction = System.currentTimeMillis();
                return;
            }
            if (!this.skillStateLogged) {
                AutoNhiemVuChinh.logSkillState(char_);
                this.skillStateLogged = true;
            }
            if (char_.clevel < 11) {
                this.doLevelFarm(char_, 28, 11, "mo cap ky nang dau tien cua phai");
                return;
            }
            this.notice("Khong co ky nang nao du dieu kien nang o level " + char_.clevel);
            return;
        }
        if (char_.ctaskId == 9 && char_.taskMaint != null && char_.taskMaint.index == 2 && char_.pPoint <= 0) {
            if (System.currentTimeMillis() - this.lastBuildAction < 5000L) {
                this.notice("Da cong tiem nang, dang cho server chuyen buoc");
                return;
            }
            this.doLevelFarm(char_, 28, char_.clevel + 1, "lay diem tiem nang cho task 9/2");
            return;
        }
        if (char_.sPoint > 0 && AutoNhiemVuChinh.getPreferredAttackSkill(char_) == null) {
            this.notice("Chua thay ky nang phai de cong diem, dang cho server cap nhat");
        } else {
            this.doSpecialTask("Dang cho server ghi nhan ky nang/tiem nang da tu cong");
        }
    }

    private void doQuestEquipmentUpgrade(Char char_, Task task) {
        Item item;
        int n;
        int n2 = n = task == null ? -1 : task.index;
        if (!AutoNhiemVuChinh.isUpgradeTaskStep(char_.ctaskId, task)) {
            this.doSpecialTask("Dang cho server mo buoc nang cap vu khi");
            return;
        }
        if (this.weaponUpgradeState != 0) {
            this.continueWeaponUpgrade(char_, System.currentTimeMillis());
            return;
        }
        boolean bl = AutoNhiemVuChinh.isWeaponUpgradeStep(char_.ctaskId, task);
        if (this.completedUpgradeTaskId == char_.ctaskId && this.completedUpgradeTaskIndex == n) {
            if (System.currentTimeMillis() - this.completedUpgradeAt < 5000L) {
                this.notice("Da nang cap trang bi, dang cho server chuyen buoc");
                return;
            }
            this.completedUpgradeTaskId = -1;
            this.completedUpgradeTaskIndex = -1;
        }
        Item item2 = item = char_.ctaskId == 12 ? AutoNhiemVuChinh.findAuto50Task12UpgradeItem(char_, task) : AutoNhiemVuChinh.findQuestUpgradeItem(char_, bl);
        if (item == null) {
            if (this.autoPickUpgradeMaterials(char_, true, bl)) {
                return;
            }
            this.doLevelFarm(char_, 28, char_.clevel + 1, "task nang cap: tim trang bi tai map 28", true);
            return;
        }
        int n3 = AutoNhiemVuChinh.getUpgradeRequirement(item);
        int n4 = AutoNhiemVuChinh.getUpgradeCost(item);
        if (AutoNhiemVuChinh.getStonePower(char_) < n3 && this.takeUpgradeStoneFromBox(char_)) {
            return;
        }
        Item item3 = AutoNhiemVuChinh.findUpgradeJunk(char_, item);
        if (item3 != null && System.currentTimeMillis() - this.lastInventoryAction >= 1200L) {
            System.out.println("AutoNVC upgrade=sell-junk item=" + item3.template.id);
            Service.gI().saleItem(item3.indexUI, item3.quantity > 0 ? item3.quantity : 1);
            this.lastInventoryAction = System.currentTimeMillis();
            return;
        }
        if (item.upgrade >= 1) {
            this.notice("Trang bi da +1, dang cho server chuyen buoc nhiem vu");
            return;
        }
        if (this.splitStackedUpgradeStone(char_, n3)) {
            return;
        }
        int n5 = AutoNhiemVuChinh.getSeparatedStonePower(char_);
        if (n3 <= 0 || n5 < n3 || n4 < 0 || char_.xu + char_.yen < n4) {
            if (n3 > 0 && AutoNhiemVuChinh.getStonePower(char_) >= n3 && n5 < n3) {
                this.notice("Can it nhat 2 o trong de tach da truoc khi nang cap");
                return;
            }
            if (this.splitQuestEquipmentForStones(char_, item, n3)) {
                return;
            }
            if (AutoNhiemVuChinh.findBodySlot(char_, item) < 0 && item.isTypeWeapon() && char_.arrItemBody != null && char_.arrItemBody.length > 1 && char_.arrItemBody[1] == null && System.currentTimeMillis() - this.lastInventoryAction >= 1200L) {
                System.out.println("AutoNVC questUpgrade=equipForFarm item=" + item.template.id + " bag=" + item.indexUI);
                Service.gI().useItem(item.indexUI);
                this.lastInventoryAction = System.currentTimeMillis();
                return;
            }
            if (this.autoPickUpgradeMaterials(char_, false, bl)) {
                return;
            }
            this.doLevelFarm(char_, 28, char_.clevel + 1, "task nang cap: tim da/yen tai map 28 " + AutoNhiemVuChinh.getStonePower(char_) + "/" + n3, true);
            return;
        }
        if (AutoNhiemVuChinh.getStonePower(char_) < n3 && this.takeUpgradeStoneFromBox(char_)) {
            return;
        }
        if (AutoNhiemVuChinh.findBodySlot(char_, item) < 0) {
            if (item.indexUI >= 0 && System.currentTimeMillis() - this.lastInventoryAction >= 1200L) {
                System.out.println("AutoNVC questUpgrade=equip-target item=" + item.template.id + " bag=" + item.indexUI + " before-npc");
                Service.gI().useItem(item.indexUI);
                this.lastInventoryAction = System.currentTimeMillis();
            }
            return;
        }
        if (!GameScr.isUpgradeScreenForAuto()) {
            this.goToNpc(22, 6, "mo nang cap trang bi", UPGRADE_ITEM_MENU);
            return;
        }
        if (System.currentTimeMillis() - this.lastInventoryAction < 1200L) {
            return;
        }
        this.weaponUpgradeUnavailable = false;
        this.upgradeWeaponTemplateId = item.template.id;
        this.upgradeWeaponLevel = item.upgrade;
        this.weaponUpgradeState = 1;
        this.lastInventoryAction = System.currentTimeMillis();
        int n6 = AutoNhiemVuChinh.findBodySlot(char_, item);
        if (n6 >= 0) {
            if (char_.arrItemBody[n6] != item) {
                return;
            }
            if (Char.af() <= 0) {
                this.resetWeaponUpgrade();
                this.notice("Can trong 1 o hanh trang de thao trang bi nang cap");
                return;
            }
        } else {
            this.resetWeaponUpgrade();
            return;
        }
        System.out.println("AutoNVC questUpgrade=unequip item=" + item.template.id + " from=+" + item.upgrade + " body=" + n6 + " npc=6 map=22");
        Service.gI().itemBodyToBag(n6);
    }

    private boolean splitQuestEquipmentForStones(Char char_, Item item, int n) {
        if (char_ == null || n <= 0 || AutoNhiemVuChinh.getStonePower(char_) >= n || char_.arrItemBag == null) {
            return false;
        }
        long l = System.currentTimeMillis();
        if (GameScr.itemSplit != null) {
            if (l - this.lastInventoryAction < 12000L) {
                this.notice("Dang tach trang bi lay da nang cap");
                return true;
            }
            GameScr.itemSplit = null;
            GameScr.arrItemSplit = new Item[24];
        }
        Item item2 = null;
        if (item != null && item.indexUI >= 0 && item.indexUI < char_.arrItemBag.length && char_.arrItemBag[item.indexUI] == item && item.upgrade > 0 && !item.isLock && AutoNhiemVuChinh.isQuestEquipmentType(item)) {
            item2 = item;
        }
        if (item2 == null) {
            for (int i = 0; i < char_.arrItemBag.length; ++i) {
                Item item3 = char_.arrItemBag[i];
                if (item3 == null || item3.upgrade <= 0 || item3.isLock || !AutoNhiemVuChinh.isQuestEquipmentType(item3)) continue;
                item2 = item3;
                break;
            }
        }
        if (item2 == null || l - this.lastInventoryAction < 1200L) {
            return false;
        }
        GameScr.itemSplit = item2;
        GameScr.arrItemSplit = new Item[24];
        System.out.println("AutoNVC questUpgrade=split item=" + item2.template.id + " bag=" + item2.indexUI + " upgrade=+" + item2.upgrade + " stone=" + AutoNhiemVuChinh.getStonePower(char_) + "/" + n);
        Service.gI().splitItem(item2);
        this.lastInventoryAction = l;
        return true;
    }

    private boolean maintainSkillBooks(Char char_) {
        if (char_ == null || char_.nClass == null || char_.nClass.classId <= 0 || GameCanvas.menu.showMenu || ChatPopup.b != null || GameCanvas.currentDialog != null || System.currentTimeMillis() - this.lastAction < 1600L) {
            return false;
        }
        Item item = AutoNhiemVuChinh.findUsableMartialBook(char_.arrItemBag, char_);
        if (item != null) {
            System.out.println("AutoNVC skillBook=use bag=" + item.indexUI + " template=" + item.template.id + " level=" + item.template.level + " name=" + item.template.name + " charLevel=" + char_.clevel);
            Service.gI().useItem(item.indexUI);
            this.lastAction = System.currentTimeMillis();
            return true;
        }
        if (char_.arrItemBox == null) {
            if (System.currentTimeMillis() - this.lastBoxRequest >= 2500L) {
                System.out.println("AutoNVC skillBook=request-box");
                Service.gI().requestItem(4);
                this.lastBoxRequest = System.currentTimeMillis();
            }
            return false;
        }
        Item item2 = AutoNhiemVuChinh.findUsableMartialBook(char_.arrItemBox, char_);
        if (item2 != null && Char.af() > 0) {
            System.out.println("AutoNVC skillBook=take-from-box box=" + item2.indexUI + " template=" + item2.template.id + " level=" + item2.template.level + " name=" + item2.template.name + " charLevel=" + char_.clevel);
            Service.gI().itemBoxToBag(item2.indexUI);
            this.lastInventoryAction = this.lastAction = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    private static Item findUsableMartialBook(Item[] itemArray, Char char_) {
        if (itemArray == null || char_ == null || char_.nClass == null) {
            return null;
        }
        Item item = null;
        for (int i = 0; i < itemArray.length; ++i) {
            byte by;
            Item item2 = itemArray[i];
            if (item2 == null || item2.template == null || !AutoNhiemVuChinh.isMartialBook(item2)
                    || !AutoNhiemVuChinh.isMartialBookForClass(item2, char_.nClass.classId)
                    || AutoNhiemVuChinh.isMartialBookAlreadyLearned(char_, item2)
                    || (by = item2.template.level) > char_.clevel || item != null && by >= item.template.level) continue;
            item = item2;
        }
        return item;
    }

    private static boolean isMartialBookAlreadyLearned(Char char_, Item item) {
        if (char_ == null || char_.nClass == null || char_.nClass.c == null || item == null || item.template == null) {
            return false;
        }
        int classId = char_.nClass.classId;
        if (classId > 0 && classId < CLASS_SKILL_BOOK.length) {
            int skillIndex = item.template.id - CLASS_SKILL_BOOK[classId];
            if (skillIndex >= 0 && skillIndex < char_.nClass.c.length) {
                SkillTemplate target = char_.nClass.c[skillIndex];
                if (target != null && char_.a(target) != null) {
                    return true;
                }
            }
        }
        String bookText = ((item.template.name == null ? "" : item.template.name) + " " + (item.template.description == null ? "" : item.template.description)).toLowerCase();
        if (char_.vSkill == null) {
            return false;
        }
        for (int i = 0; i < char_.vSkill.size(); ++i) {
            Skill skill = (Skill)char_.vSkill.elementAt(i);
            if (skill == null || skill.template == null || skill.template.name == null) continue;
            String skillName = skill.template.name.toLowerCase();
            if (skillName.length() >= 3 && bookText.indexOf(skillName) >= 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean isMartialBook(Item item) {
        if (item == null || item.template == null) {
            return false;
        }
        if (item.template.type == 10) {
            return true;
        }
        String string = item.template.name == null ? "" : item.template.name.toLowerCase();
        String string2 = item.template.description == null ? "" : item.template.description.toLowerCase();
        if (string.indexOf("sách võ công") >= 0 || string.indexOf("võ công") >= 0
                || string.indexOf("bí kíp") >= 0 || string2.indexOf("sách võ công") >= 0
                || string2.indexOf("học võ công") >= 0) {
            return true;
        }
        return string.indexOf("sach vo cong") >= 0 || string.indexOf("s\u00c3\u00a1ch v\u00c3\u00b5 c\u00c3\u00b4ng") >= 0 || string.indexOf("vo cong") >= 0 || string.indexOf("v\u00c3\u00b5 c\u00c3\u00b4ng") >= 0 || string.indexOf("bi kip") >= 0 || string.indexOf("b\u00c3\u00ad k\u00c3\u00adp") >= 0 || string2.indexOf("sach vo cong") >= 0 || string2.indexOf("s\u00c3\u00a1ch v\u00c3\u00b5 c\u00c3\u00b4ng") >= 0 || string2.indexOf("hoc vo cong") >= 0 || string2.indexOf("h\u00e1\u00bb\u008dc v\u00c3\u00b5 c\u00c3\u00b4ng") >= 0;
    }

    private static boolean isMartialBookForClass(Item item, int classId) {
        if (item == null || item.template == null || classId <= 0 || classId >= CLASS_SKILL_BOOK.length) {
            return false;
        }
        int first = CLASS_SKILL_BOOK[classId];
        int end = classId + 1 < CLASS_SKILL_BOOK.length ? CLASS_SKILL_BOOK[classId + 1] : first + 9;
        return item.template.id >= first && item.template.id < end;
    }

    private static boolean isQuestEquipmentType(Item item) {
        return item != null && item.template != null && (item.isTypeWeapon() || item.isTypeClothe() || item.isTypeAdorn());
    }

    private boolean splitStackedUpgradeStone(Char char_, int n) {
        Item item;
        int n2;
        if (!AutoNhiemVuChinh.isUpgradeTaskStep(char_ == null ? -1 : char_.ctaskId, char_ == null ? null : char_.taskMaint)) {
            this.cancelNonQuestStoneSplit();
            return false;
        }
        if (char_ == null || n <= 0 || char_.arrItemBag == null) {
            return false;
        }
        int n3 = 0;
        for (int i = 0; i < char_.arrItemBag.length; ++i) {
            if (char_.arrItemBag[i] != null) continue;
            ++n3;
        }
        if (n3 < 2) {
            return false;
        }
        long l = System.currentTimeMillis();
        if (this.stoneSplitState != 0) {
            Item item2;
            Item item3 = item2 = this.stoneSplitIndex >= 0 && this.stoneSplitIndex < char_.arrItemBag.length ? char_.arrItemBag[this.stoneSplitIndex] : null;
            if (this.stoneSplitState == 1) {
                if (GameCanvas.inputDlg == null || GameCanvas.currentDialog != GameCanvas.inputDlg) {
                    this.stoneSplitState = 0;
                    holdBagSortForStoneUpgrade = false;
                    return false;
                }
                if (l - this.stoneSplitAt < 500L) {
                    return true;
                }
                GameCanvas.inputDlg.tfInput.a("1");
                GameCanvas.instance.perform(88835, (Object)String.valueOf(this.stoneSplitIndex));
                this.stoneSplitState = 2;
                this.stoneSplitAt = l;
                this.lastInventoryAction = l;
                return true;
            }
            if (item2 != null && item2.quantity != this.stoneSplitQuantity) {
                this.stoneSplitState = 0;
                this.stoneSplitIndex = -1;
                return true;
            }
            if (l - this.stoneSplitAt < 3000L) {
                return true;
            }
            this.stoneSplitState = 0;
            this.stoneSplitIndex = -1;
            holdBagSortForStoneUpgrade = false;
            return true;
        }
        if (l - this.lastInventoryAction < 1200L) {
            return true;
        }
        int n4 = 0;
        for (n2 = 0; n2 < char_.arrItemBag.length; ++n2) {
            item = char_.arrItemBag[n2];
            if (!AutoNhiemVuChinh.isUpgradeStoneLv2(item) || item.quantity != 1) continue;
            n4 += AutoNhiemVuChinh.getStoneValue(item);
        }
        if (n4 >= n) {
            return false;
        }
        for (n2 = 0; n2 < char_.arrItemBag.length; ++n2) {
            item = char_.arrItemBag[n2];
            if (!AutoNhiemVuChinh.isUpgradeStoneLv2(item) || item.quantity <= 1) continue;
            int n5 = item.quantity;
            System.out.println("AutoNVC upgrade=split-stacked-stone bag=" + item.indexUI + " quantity=" + n5);
            holdBagSortForStoneUpgrade = true;
            GameScr.indexSelect = item.indexUI;
            GameScr.gI().b(110244, null);
            if (GameCanvas.inputDlg == null || GameCanvas.currentDialog != GameCanvas.inputDlg) {
                holdBagSortForStoneUpgrade = false;
                return false;
            }
            GameCanvas.inputDlg.tfInput.a("1");
            this.stoneSplitState = 1;
            this.stoneSplitIndex = item.indexUI;
            this.stoneSplitQuantity = n5;
            this.stoneSplitAt = l;
            this.lastInventoryAction = l;
            return true;
        }
        return false;
    }

    private void cancelNonQuestStoneSplit() {
        if (this.stoneSplitState == 0 && !holdBagSortForStoneUpgrade) {
            return;
        }
        System.out.println("AutoNVC upgrade=cancel-stone-split reason=not-upgrade-task");
        this.stoneSplitState = 0;
        this.stoneSplitIndex = -1;
        this.stoneSplitQuantity = 0;
        this.stoneSplitAt = 0L;
        holdBagSortForStoneUpgrade = false;
        if (GameCanvas.inputDlg != null && GameCanvas.currentDialog == GameCanvas.inputDlg) {
            GameCanvas.currentDialog = null;
        }
    }

    public static boolean isHoldingBagSortForStoneUpgrade() {
        return holdBagSortForStoneUpgrade;
    }

    private boolean takeUpgradeStoneFromBox(Char char_) {
        int n;
        if (char_ == null || char_.arrItemBox == null || char_.arrItemBag == null || System.currentTimeMillis() - this.lastInventoryAction < 1800L) {
            return false;
        }
        int n2 = -1;
        int n3 = 0;
        for (n = 0; n < char_.arrItemBag.length; ++n) {
            if (char_.arrItemBag[n] != null) continue;
            n2 = n;
            ++n3;
        }
        if (n2 < 0 || n3 < 2) {
            return false;
        }
        for (n = 0; n < char_.arrItemBox.length; ++n) {
            Item item = char_.arrItemBox[n];
            if (!AutoNhiemVuChinh.isUpgradeStoneLv2(item) || item.quantity <= 1) continue;
            System.out.println("AutoNVC upgrade=take-stone-from-box box=" + item.indexUI + " quantity=" + item.quantity);
            Service.gI().itemBoxToBag(item.indexUI);
            this.lastInventoryAction = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    private static Item findQuestUpgradeItem(Char char_, boolean bl) {
        int n;
        if (char_ == null) {
            return null;
        }
        if (char_.arrItemBody != null) {
            if (bl && char_.arrItemBody.length > 1 && AutoNhiemVuChinh.isQuestUpgradeItem(char_, char_.arrItemBody[1], true)) {
                return char_.arrItemBody[1];
            }
            if (!bl) {
                for (n = 0; n < char_.arrItemBody.length; ++n) {
                    if (n == 1 || !AutoNhiemVuChinh.isQuestUpgradeItem(char_, char_.arrItemBody[n], false)) continue;
                    return char_.arrItemBody[n];
                }
            }
        }
        if (char_.arrItemBag != null) {
            n = char_.nClass == null ? 0 : char_.nClass.classId;
            int s = n > 0 && n < CLASS_WEAPON.length ? CLASS_WEAPON[n] : -1;
            for (int i = 0; i < 2; ++i) {
                for (int j = 0; j < char_.arrItemBag.length; ++j) {
                    Item item = char_.arrItemBag[j];
                    boolean typeMatches = bl ? item != null && item.isTypeWeapon() : item != null && !item.isTypeWeapon();
                    boolean preferred = bl ? item != null && item.template.id == s : item != null && item.template.level == 10;
                    if (!AutoNhiemVuChinh.isQuestUpgradeItem(char_, item, bl) || !typeMatches || i == 0 && !preferred) continue;
                    return item;
                }
            }
        }
        return null;
    }

    private static boolean isQuestUpgradeItem(Char char_, Item item, boolean bl) {
        int n;
        if (item == null || item.template == null || item.upgrade < 0 || item.upgrade >= 4 || item.isUpMax() || item.template.level < 10 && !item.isTypeAdorn() || item.template.level > char_.clevel) {
            return false;
        }
        if (bl && !item.isTypeWeapon()) {
            return false;
        }
        if (!(item.isTypeWeapon() || item.isTypeClothe() || item.isTypeAdorn())) {
            return false;
        }
        if (item.template.gender != 2 && item.template.gender != char_.cgender) {
            return false;
        }
        return !item.isTypeWeapon() || char_.nClass == null || char_.nClass.classId <= 0 || (n = AutoNhiemVuChinh.getWeaponClass(item.template.id)) <= 0 || n == char_.nClass.classId;
    }

    private static int findBodySlot(Char char_, Item item) {
        if (char_ != null && char_.arrItemBody != null && item != null) {
            for (int i = 0; i < char_.arrItemBody.length; ++i) {
                if (char_.arrItemBody[i] != item) continue;
                return i;
            }
        }
        return -1;
    }

    private static SkillTemplate getEligibleQuestSkill(Char char_) {
        Skill skill = AutoNhiemVuChinh.findEligibleLearnedSkill(char_.vSkill, char_.clevel, true);
        if (skill == null) {
            skill = AutoNhiemVuChinh.findEligibleLearnedSkill(char_.vSkill, char_.clevel, false);
        }
        if (skill != null) {
            return skill.template;
        }
        if (char_.nClass == null || char_.nClass.c == null) {
            return null;
        }
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < char_.nClass.c.length; ++j) {
                int n;
                SkillTemplate skillTemplate = char_.nClass.c[j];
                if (skillTemplate == null || i == 0 && skillTemplate.type != 1 || i == 1 && skillTemplate.type == 1) continue;
                Skill skill2 = char_.a(skillTemplate);
                int n2 = n = skill2 == null ? 0 : skill2.point;
                if (!AutoNhiemVuChinh.isSkillUpgradeAvailable(skillTemplate, n, char_.clevel)) continue;
                return skillTemplate;
            }
        }
        return null;
    }

    private static Skill findEligibleLearnedSkill(MyVector myVector, int n, boolean bl) {
        if (myVector == null) {
            return null;
        }
        for (int i = 0; i < myVector.size(); ++i) {
            Skill skill = (Skill)myVector.elementAt(i);
            if (skill == null || skill.template == null || bl && skill.template.type != 1 || !bl && skill.template.type == 1 || !AutoNhiemVuChinh.isSkillUpgradeAvailable(skill.template, skill.point, n)) continue;
            return skill;
        }
        return null;
    }

    private static void logSkillState(Char char_) {
        System.out.println("AutoNVC skillDump class=" + (char_.nClass == null ? -1 : char_.nClass.classId) + " level=" + char_.clevel + " sPoint=" + char_.sPoint);
        if (char_.vSkill != null) {
            for (int i = 0; i < char_.vSkill.size(); ++i) {
                Skill skill = (Skill)char_.vSkill.elementAt(i);
                if (skill == null || skill.template == null) continue;
                int n = skill.point + 1;
                int n2 = skill.template.skills != null && n >= 0 && n < skill.template.skills.length && skill.template.skills[n] != null ? skill.template.skills[n].level : -1;
                System.out.println("AutoNVC skillDump learned id=" + skill.template.id + " name=" + skill.template.name + " type=" + skill.template.type + " point=" + skill.point + " nextLevel=" + n2);
            }
        }
    }

    private static boolean isSkillUpgradeAvailable(SkillTemplate skillTemplate, int n, int n2) {
        if (skillTemplate == null || skillTemplate.skills == null || n >= skillTemplate.maxPoint) {
            return false;
        }
        int n3 = n + 1;
        return n3 >= 0 && n3 < skillTemplate.skills.length && skillTemplate.skills[n3] != null && skillTemplate.skills[n3].level <= n2;
    }

    private void doFriendTask(Char char_, int n, int n2, int n3) {
        if (n != 1) {
            this.doSpecialTask("Dang cho server mo buoc giao luu ban huu");
            return;
        }
        if (n2 > 0 && n3 >= n2) {
            this.notice("Da giao luu du " + n3 + "/" + n2 + " nguoi choi");
            return;
        }
        if (GameScr.vCharInMap == null || GameCanvas.menu.showMenu || ChatPopup.b != null || GameCanvas.currentDialog != null) {
            return;
        }
        long l = System.currentTimeMillis();
        if (l - this.lastFriendAction < 1400L) {
            return;
        }
        Char char_2 = null;
        for (int i = 0; i < GameScr.vCharInMap.size(); ++i) {
            Char char_3 = (Char)GameScr.vCharInMap.elementAt(i);
            if (char_3 == null || char_3 == char_ || char_3.isNhanban || char_3.charID == char_.charID || char_3.cName == null || char_3.cName.length() <= 0 || this.wasFriendAttempted(char_3.charID) || AutoNhiemVuChinh.isExistingFriend(char_3.cName)) continue;
            char_2 = char_3;
            break;
        }
        if (char_2 == null) {
            if (this.friendAttemptCount > 0 && l - this.lastFriendAction >= 10000L) {
                this.friendAttemptCount = 0;
            }
            this.notice("Dang tim nguoi choi tren map de giao luu " + n3 + "/" + n2);
            return;
        }
        this.rememberFriendAttempt(char_2.charID);
        this.lastFriendAction = l;
        System.out.println("AutoNVC friend=add name=" + char_2.cName + " charId=" + char_2.charID + " progress=" + n3 + "/" + n2 + " attempted=" + this.friendAttemptCount);
        Service.gI().addFriend(char_2.cName);
    }

    private boolean wasFriendAttempted(int n) {
        for (int i = 0; i < this.friendAttemptCount; ++i) {
            if (this.friendAttemptIds[i] != n) continue;
            return true;
        }
        return false;
    }

    private void rememberFriendAttempt(int n) {
        if (this.friendAttemptCount >= this.friendAttemptIds.length) {
            this.friendAttemptCount = 0;
        }
        this.friendAttemptIds[this.friendAttemptCount++] = n;
    }

    private static boolean isExistingFriend(String string) {
        if (GameScr.vFriend != null) {
            for (int i = 0; i < GameScr.vFriend.size(); ++i) {
                Friend friend = (Friend)GameScr.vFriend.elementAt(i);
                if (friend == null || friend.friendName == null || !friend.friendName.equals(string)) continue;
                return true;
            }
        }
        return false;
    }

    private boolean maintainLevel20Boost(Char char_) {
        if (char_ == null || char_.clevel < 20 || char_.cHp <= 0) {
            return false;
        }
        String string = AutoNhiemVuChinh.getLevelBoostRmsKey(char_);
        if (this.levelBoostState == 0 && mResources.d((String)string) == 1) {
            this.levelBoostState = 6;
        }
        if (this.levelBoostState == 6) {
            return false;
        }
        if (this.weaponUpgradeState != 0 || AutoNhiemVuChinh.isUpgradeTaskStep(char_.ctaskId, char_.taskMaint) || char_.ctaskId == 13 && AutoNhiemVuChinh.getChallengeBattleMap(char_.taskMaint == null ? -1 : char_.taskMaint.index) >= 0 || char_.ctaskId == 17 && char_.taskMaint != null && char_.taskMaint.index == 2) {
            return false;
        }
        long l = System.currentTimeMillis();
        if (this.levelBoostState == 0) {
            this.levelRewardGoldBefore = char_.luong;
            this.levelRewardStartedAt = l;
            this.levelBoostState = 1;
            System.out.println("AutoNVC levelBoost=start char=" + char_.cName + " level=" + char_.clevel + " gold=" + char_.luong);
        }
        if (this.levelBoostState == 1) {
            if (char_.luong > this.levelRewardGoldBefore || l - this.levelRewardStartedAt >= 18000L) {
                System.out.println("AutoNVC levelBoost=reward-finished gold=" + this.levelRewardGoldBefore + "->" + char_.luong + " timeout=" + (l - this.levelRewardStartedAt));
                this.levelBoostState = 2;
                this.levelRewardStartedAt = l;
                GameCanvas.menu.showMenu = false;
                GameScr.gI().resetButton();
                return true;
            }
            this.goToNpc(22, 24, "nhan phan thuong thang cap 20", LEVEL_REWARD_MENU);
            return true;
        }
        if (ChatPopup.b != null && ChatPopup.b.a != null) {
            if (l - this.lastLevelBoostAction >= 1600L) {
                ChatPopup.b.a.a();
                this.lastLevelBoostAction = l;
            }
            return true;
        }
        if (GameCanvas.menu.showMenu) {
            GameCanvas.menu.showMenu = false;
            return true;
        }
        if (this.levelBoostState == 2) {
            Item item = AutoNhiemVuChinh.findLinhChiInBag(char_);
            if (item != null) {
                this.levelBoostState = 4;
                return true;
            }
            if (Char.af() <= 0) {
                this.notice("C\u1ea7n tr\u1ed1ng 1 \u00f4 h\u00e0nh trang \u0111\u1ec3 mua Linh Chi x2 kinh nghi\u1ec7m");
                return false;
            }
            Item item2 = AutoNhiemVuChinh.findLinhChiInStore();
            if (item2 == null) {
                if (l - this.lastLevelBoostAction >= 2500L) {
                    System.out.println("AutoNVC levelBoost=request gold-store type=14");
                    Service.gI().requestItem(14);
                    this.lastLevelBoostAction = l;
                }
                return l - this.levelRewardStartedAt < 10000L;
            }
            if (l - this.lastLevelBoostAction >= 1600L) {
                System.out.println("AutoNVC levelBoost=buy-linh-chi template=" + item2.template.id + " shopIndex=" + item2.indexUI + " typeUI=" + item2.typeUI + " gold=" + char_.luong);
                Service.gI().buyItem(item2.typeUI, item2.indexUI, 1);
                this.lastLevelBoostAction = l;
                this.levelBoostState = 3;
            }
            return true;
        }
        if (this.levelBoostState == 3) {
            if (AutoNhiemVuChinh.findLinhChiInBag(char_) != null) {
                this.levelBoostState = 4;
                return true;
            }
            if (l - this.lastLevelBoostAction >= 7000L) {
                this.levelBoostState = 2;
                this.levelRewardStartedAt = l - 10000L;
                return false;
            }
            return true;
        }
        if (this.levelBoostState == 4) {
            Item item = AutoNhiemVuChinh.findLinhChiInBag(char_);
            if (item == null) {
                this.levelBoostState = 2;
                return false;
            }
            if (l - this.lastLevelBoostAction >= 1600L) {
                this.linhChiBagIndex = item.indexUI;
                this.linhChiQuantityBefore = AutoNhiemVuChinh.countLinhChi(char_);
                System.out.println("AutoNVC levelBoost=use-linh-chi template=" + item.template.id + " bag=" + item.indexUI + " quantity=" + this.linhChiQuantityBefore);
                Service.gI().useItem(item.indexUI);
                this.lastLevelBoostAction = l;
                this.levelBoostState = 5;
            }
            return true;
        }
        if (this.levelBoostState == 5) {
            int n = AutoNhiemVuChinh.countLinhChi(char_);
            if (n < this.linhChiQuantityBefore) {
                mResources.a((String)string, (int)1);
                this.levelBoostState = 6;
                this.notice("\u0110\u00e3 nh\u1eadn th\u01b0\u1edfng c\u1ea5p 20 v\u00e0 s\u1eed d\u1ee5ng Linh Chi x2 kinh nghi\u1ec7m");
                System.out.println("AutoNVC levelBoost=complete bag=" + this.linhChiBagIndex + " quantity=" + this.linhChiQuantityBefore + "->" + n);
                return false;
            }
            if (l - this.lastLevelBoostAction >= 5000L) {
                this.levelBoostState = 4;
            }
            return true;
        }
        return false;
    }

    private static String getLevelBoostRmsKey(Char char_) {
        String string = char_ == null || char_.cName == null ? "unknown" : char_.cName;
        return "AutoNVCBoost20_" + string.hashCode();
    }

    private static Item findLinhChiInBag(Char char_) {
        if (char_ == null || char_.arrItemBag == null) {
            return null;
        }
        for (int i = 0; i < char_.arrItemBag.length; ++i) {
            Item item = char_.arrItemBag[i];
            if (!AutoNhiemVuChinh.isLinhChi(item)) continue;
            return item;
        }
        return null;
    }

    private static int countLinhChi(Char char_) {
        if (char_ == null || char_.arrItemBag == null) {
            return 0;
        }
        int n = 0;
        for (int i = 0; i < char_.arrItemBag.length; ++i) {
            Item item = char_.arrItemBag[i];
            if (!AutoNhiemVuChinh.isLinhChi(item)) continue;
            n += item.quantity > 0 ? item.quantity : 1;
        }
        return n;
    }

    private static Item findLinhChiInStore() {
        Item item = null;
        int n = -1;
        if (GameScr.arrItemStore == null) {
            return null;
        }
        for (int i = 0; i < GameScr.arrItemStore.length; ++i) {
            int n2;
            Item item2 = GameScr.arrItemStore[i];
            if (!AutoNhiemVuChinh.isLinhChi(item2)) continue;
            String string = item2.template.description == null ? "" : item2.template.description.toLowerCase();
            int n3 = n2 = string.indexOf("x2") >= 0 && (string.indexOf("kinh") >= 0 || string.indexOf("exp") >= 0) ? 2 : 1;
            if (n2 <= n) continue;
            item = item2;
            n = n2;
        }
        return item;
    }

    private static boolean isLinhChi(Item item) {
        if (item == null || item.template == null || item.template.name == null) {
            return false;
        }
        String string = item.template.name.toLowerCase();
        return string.indexOf("linh chi") >= 0;
    }

    private void maintainBuild(Char char_) {
        Skill skill;
        boolean bl;
        if ((char_.nClass == null || char_.nClass.classId <= 0) && char_.clevel >= 10
                || System.currentTimeMillis() - this.lastBuildAction < 2500L) {
            return;
        }
        int n = char_.taskMaint == null ? -1 : char_.taskMaint.index;
        boolean bl2 = char_.ctaskId == 9 && n == 2;
        boolean bl3 = char_.ctaskId == 9 && n == 1;
        boolean bl4 = char_.ctaskId != 9 || bl2 || bl3;
        boolean bl5 = bl = char_.ctaskId != 9 || bl3;
        if ((AutoNhiemVuPanel.autoPotential || bl2 || bl3) && bl4 && char_.pPoint > 0) {
            int classId = char_.nClass == null ? 0 : char_.nClass.classId;
            int primaryStat = PotentialBuildPolicy.primaryStatFor(classId, char_.clevel);
            boolean levelTenSplit = PotentialBuildPolicy.shouldSplitLevelTenPoints(char_.clevel, char_.pPoint);
            int vitalityPoints = PotentialBuildPolicy.vitalityPoints(char_.pPoint, levelTenSplit);
            int primaryPoints = PotentialBuildPolicy.primaryPoints(char_.pPoint, levelTenSplit);
            System.out.println("AutoNVC build=potential class=" + classId
                    + " main=" + primaryStat + " mainPoint=" + primaryPoints
                    + " hpPoint=" + vitalityPoints + " levelTenSplit=" + levelTenSplit);
            if (vitalityPoints > 0) {
                Service.gI().upPotential(2, vitalityPoints);
            }
            if (primaryPoints > 0) {
                Service.gI().upPotential(primaryStat, primaryPoints);
            }
            this.lastAction = this.lastBuildAction = System.currentTimeMillis();
            return;
        }
        if (char_.nClass == null || char_.nClass.classId <= 0) {
            return;
        }
        if (AutoNhiemVuPanel.autoSkill && bl && char_.sPoint > 0 && (skill = AutoNhiemVuChinh.getPreferredAttackSkill(char_)) != null && skill.template != null && skill.template.skills != null) {
            int n8 = 0;
            for (int i = skill.point + 1; i < skill.template.skills.length && i <= skill.template.maxPoint && skill.template.skills[i] != null && skill.template.skills[i].level <= char_.clevel && n8 < char_.sPoint; ++n8, ++i) {
            }
            if (n8 > 0) {
                System.out.println("AutoNVC build=skill id=" + skill.template.id + " name=" + skill.template.name + " point=" + n8);
                Service.gI().upSkill((int)skill.template.id, n8);
                this.lastAction = this.lastBuildAction = System.currentTimeMillis();
            }
        }
    }

    private static Skill getPreferredAttackSkill(Char char_) {
        Skill skill = AutoNhiemVuChinh.findBestMaxedAttackSkill(char_, char_.vSkillFight);
        Skill skill2 = AutoNhiemVuChinh.findBestMaxedAttackSkill(char_, char_.vSkill);
        if (AutoNhiemVuChinh.isBetterMaxedAttackSkill(skill2, skill)) {
            skill = skill2;
        }
        if (skill != null) {
            return skill;
        }
        Skill skill3 = AutoNhiemVuChinh.findHighestLevelAttackSkill(char_, char_.vSkillFight, 1);
        if (skill3 == null) {
            skill3 = AutoNhiemVuChinh.findHighestLevelAttackSkill(char_, char_.vSkill, 1);
        }
        if (skill3 == null) {
            skill3 = AutoNhiemVuChinh.findHighestLevelAttackSkill(char_, char_.vSkillFight, 3);
        }
        return skill3 != null ? skill3 : AutoNhiemVuChinh.findHighestLevelAttackSkill(char_, char_.vSkill, 3);
    }

    private static Skill findBestMaxedAttackSkill(Char char_, MyVector myVector) {
        Skill skill = null;
        if (myVector == null) {
            return null;
        }
        for (int i = 0; i < myVector.size(); ++i) {
            Skill skill2 = (Skill)myVector.elementAt(i);
            if (!AutoNhiemVuChinh.isLevelUsableAttackSkill(char_, skill2) || skill2.template.type != 1 || skill2.template.maxPoint < 0 || skill2.point < skill2.template.maxPoint || !AutoNhiemVuChinh.isBetterMaxedAttackSkill(skill2, skill)) continue;
            skill = skill2;
        }
        return skill;
    }

    private static boolean isBetterMaxedAttackSkill(Skill skill, Skill skill2) {
        int n;
        if (skill == null) {
            return false;
        }
        if (skill2 == null) {
            return true;
        }
        int n2 = AutoNhiemVuChinh.getSkillLevel(skill);
        return n2 > (n = AutoNhiemVuChinh.getSkillLevel(skill2)) || n2 == n && skill.manaUse < skill2.manaUse || n2 == n && skill.manaUse == skill2.manaUse && skill.template.id > skill2.template.id;
    }

    private static Skill findHighestLevelAttackSkill(Char char_, MyVector myVector, int n) {
        Skill skill = null;
        if (myVector != null) {
            for (int i = 0; i < myVector.size(); ++i) {
                Skill skill2 = (Skill)myVector.elementAt(i);
                if (!AutoNhiemVuChinh.isLevelUsableAttackSkill(char_, skill2) || skill2.template.type != n || skill != null && AutoNhiemVuChinh.getSkillLevel(skill2) <= AutoNhiemVuChinh.getSkillLevel(skill) && (AutoNhiemVuChinh.getSkillLevel(skill2) != AutoNhiemVuChinh.getSkillLevel(skill) || skill2.point <= skill.point) && (AutoNhiemVuChinh.getSkillLevel(skill2) != AutoNhiemVuChinh.getSkillLevel(skill) || skill2.point != skill.point || skill2.template.id <= skill.template.id)) continue;
                skill = skill2;
            }
        }
        return skill;
    }

    private void maintainPreferredCombatSkill(Char char_) {
        Skill skill = AutoNhiemVuChinh.getPreferredAttackSkill(char_);
        if (skill == null || skill.template == null) {
            return;
        }
        Auto.g = skill;
        if (char_.myskill != skill) {
            Service.gI().selectSkill((int)skill.template.id);
            char_.myskill = skill;
            this.lastPreferredCombatSkillId = skill.template.id;
            System.out.println("AutoNVC combat=select-best-skill id=" + skill.template.id + " name=" + skill.template.name + " requiredLevel=" + AutoNhiemVuChinh.getSkillLevel(skill) + " point=" + skill.point + " charLevel=" + char_.clevel);
        } else if (this.lastPreferredCombatSkillId != skill.template.id) {
            this.lastPreferredCombatSkillId = skill.template.id;
        }
    }

    private static boolean isLevelUsableAttackSkill(Char char_, Skill skill) {
        if (char_ == null || skill == null || skill.template == null || skill.point < 0) {
            return false;
        }
        if (skill.template.type != 1 && skill.template.type != 3) {
            return false;
        }
        if (char_.nClass != null && char_.nClass.classId == 3 && skill.template.id == 25) {
            return false;
        }
        return skill.manaUse <= char_.cMaxMP && AutoNhiemVuChinh.getSkillLevel(skill) <= char_.clevel;
    }

    private static int getSkillLevel(Skill skill) {
        if (skill == null) {
            return 0;
        }
        if (skill.template != null && skill.template.skills != null && skill.point >= 0 && skill.point < skill.template.skills.length && skill.template.skills[skill.point] != null && skill.template.skills[skill.point].level > 0) {
            return skill.template.skills[skill.point].level;
        }
        return skill.level;
    }

    private void maintainFood(Char char_) {
        int n;
        Item item;
        int n2;
        if (!AutoNhiemVuPanel.autoFood) {
            Char.isAMP = false;
            return;
        }
        if (char_.arrItemBag == null) {
            return;
        }
        Char.em = n2 = AutoNhiemVuChinh.getSuitableFoodLevel(char_.clevel);
        if (char_.ctaskId <= 3) {
            Char.isAMP = true;
            return;
        }
        long l = System.currentTimeMillis();
        boolean bl = !GameCanvas.menu.showMenu && ChatPopup.b == null && GameCanvas.currentDialog == null && char_.statusMe != 14 && char_.statusMe != 5 && char_.cHp > 0;
        boolean bl2 = AutoNhiemVuChinh.hasFoodEffect(char_);
        if (bl2) {
            Char.isAMP = true;
            this.foodUsePendingUntil = 0L;
        } else {
            if (l < this.foodUsePendingUntil) {
                Char.isAMP = false;
                return;
            }
            item = AutoNhiemVuChinh.findFood(char_, n2);
            if (item != null && bl) {
                System.out.println("AutoNVC food=use level=" + n2 + " template=" + item.template.id + " bag=" + item.indexUI);
                Service.gI().useItem(item.indexUI);
                Char.isAMP = false;
                this.foodUsePendingUntil = l + 3000L;
                return;
            }
            Char.isAMP = false;
        }
        if (bl && l - this.lastFoodCleanupAction >= 1800L && (item = AutoNhiemVuChinh.findLowLevelFood(char_, n2)) != null) {
            System.out.println("AutoNVC food=throw-low level=" + item.template.level + " desired=" + n2 + " template=" + item.template.id + " bag=" + item.indexUI + " quantity=" + item.quantity);
            Service.gI().throwItem(item.indexUI);
            this.lastFoodCleanupAction = l;
            return;
        }
        int n3 = AutoNhiemVuChinh.countFood(char_, n2);
        int n4 = n = char_.ctaskId == 8 || char_.ctaskId == 9 ? 1 : 0;
        if (n3 >= AutoNhiemVuPanel.foodCount || Char.af() <= n || l - this.lastFoodAction < 10000L || GameCanvas.menu.showMenu || ChatPopup.b != null || !TileMap.d((int)TileMap.mapID) && !TileMap.f((int)TileMap.mapID)) {
            return;
        }
        int n5 = AutoNhiemVuPanel.foodCount - n3;
        int n6 = n2 == 50 ? 7 : n2 / 10;
        System.out.println("AutoNVC food=buy level=" + n2 + " shopIndex=" + n6 + " quantity=" + n5);
        Service.gI().buyItem_food(9, n6, n5);
        this.lastFoodAction = System.currentTimeMillis();
    }

    private static int getSuitableFoodLevel(int n) {
        return n < 10 ? 1 : (n >= 50 ? 50 : n / 10 * 10);
    }

    private static boolean hasFoodEffect(Char char_) {
        if (char_.vEff != null) {
            for (int i = 0; i < char_.vEff.size(); ++i) {
                Effect effect = (Effect)char_.vEff.elementAt(i);
                if (effect == null || effect.template == null || effect.template.type != 0) continue;
                return true;
            }
        }
        return false;
    }

    private static Item findFood(Char char_, int n) {
        for (int i = 0; i < char_.arrItemBag.length; ++i) {
            Item item = char_.arrItemBag[i];
            if (item == null || item.template == null || item.template.type != 18 || item.template.level != n) continue;
            return item;
        }
        return null;
    }

    private static Item findLowLevelFood(Char char_, int n) {
        Item item = null;
        for (int i = 0; i < char_.arrItemBag.length; ++i) {
            Item item2 = char_.arrItemBag[i];
            if (item2 == null || item2.template == null || item2.template.type != 18 || item2.isLock || item2.template.level >= n || item != null && item2.template.level >= item.template.level) continue;
            item = item2;
        }
        return item;
    }

    private static int countFood(Char char_, int n) {
        int n2 = 0;
        for (int i = 0; i < char_.arrItemBag.length; ++i) {
            Item item = char_.arrItemBag[i];
            if (item == null || item.template == null || item.template.type != 18 || item.template.level != n) continue;
            n2 += item.quantity > 0 ? item.quantity : 1;
        }
        return n2;
    }

    private boolean maintainInventory(Char char_) {
        Item item;
        Item item2;
        long l = System.currentTimeMillis();
        if (this.weaponUpgradeState != 0) {
            return this.continueWeaponUpgrade(char_, l);
        }
        boolean bl = AutoNhiemVuChinh.isUpgradeTaskStep(char_.ctaskId, char_.taskMaint);
        if (bl) {
            return false;
        }
        if (char_.nClass != null && char_.nClass.classId > 0 && char_.nClass.classId < CLASS_WEAPON.length && char_.arrItemBag != null && char_.arrItemBody != null && char_.arrItemBody.length > 1 && char_.arrItemBody[1] == null && l - this.lastInventoryAction >= 1200L && (item2 = AutoNhiemVuChinh.findBagItem(CLASS_WEAPON[char_.nClass.classId])) != null) {
            System.out.println("AutoNVC inventory=equipClassWeapon class=" + char_.nClass.classId + " item=" + item2.template.id + " bag=" + item2.indexUI);
            Service.gI().useItem(item2.indexUI);
            this.lastInventoryAction = l;
            return true;
        }
        if (char_.ctaskId <= 9 || char_.arrItemBag == null || char_.arrItemBody == null || GameCanvas.menu.showMenu || ChatPopup.b != null || GameCanvas.currentDialog != null || l - this.lastInventoryAction < 1800L || l - this.lastBuildAction < 2000L) {
            return false;
        }
        item2 = AutoNhiemVuChinh.findClassWeaponToStore(char_);
        if (item2 != null) {
            if (char_.arrItemBox == null) {
                if (l - this.lastBoxRequest >= 2500L) {
                    System.out.println("AutoNVC inventory=request-box for class weapons");
                    Service.gI().requestItem(4);
                    this.lastBoxRequest = l;
                }
                return true;
            }
            if (AutoNhiemVuChinh.hasFreeBoxSlot(char_)) {
                System.out.println("AutoNVC inventory=store-class-weapon bag=" + item2.indexUI + " item=" + item2.template.id + " name=" + item2.template.name + " level=" + item2.template.level + " class=" + char_.nClass.classId);
                Service.gI().itemBagToBox(item2.indexUI);
                this.lastInventoryAction = l;
                return true;
            }
            this.notice("R\u01b0\u01a1ng \u0111\u00e3 \u0111\u1ea7y, t\u1ea1m ng\u1eebng nh\u1eb7t v\u0169 kh\u00ed \u0111\u00fang class");
        }
        if (AutoNhiemVuPanel.autoSellUnsuitable && (item = AutoNhiemVuChinh.findUnsuitableEquipment(char_)) != null) {
            System.out.println("AutoNVC inventory=sell bag=" + item.indexUI + " item=" + item.template.id + " name=" + item.template.name + " level=" + item.template.level);
            Service.gI().saleItem(item.indexUI, 1);
            this.lastInventoryAction = l;
            return true;
        }
        if (!AutoNhiemVuPanel.autoUpgradeWeapon || this.weaponUpgradeUnavailable || char_.arrItemBody.length <= 1) {
            return false;
        }
        item = char_.arrItemBody[1];
        if (item == null || item.template == null || !item.isTypeWeapon() || item.template.level < 10) {
            return false;
        }
        int n = AutoNhiemVuPanel.upgradeTarget;
        if (n > 4) {
            n = 4;
        }
        if (n > item.getUpMax()) {
            n = item.getUpMax();
        }
        if (this.splitStackedUpgradeStone(char_, AutoNhiemVuChinh.getUpgradeRequirement(item))) {
            return true;
        }
        if (item.upgrade >= n || !AutoNhiemVuChinh.canUpgradeItem(char_, item)) {
            return false;
        }
        if (Char.af() <= 0) {
            this.notice("Hanh trang day, chua co do an toan de ban nen chua the thao vu khi");
            return false;
        }
        this.upgradeWeaponTemplateId = item.template.id;
        this.upgradeWeaponLevel = item.upgrade;
        this.weaponUpgradeState = 1;
        this.lastInventoryAction = l;
        System.out.println("AutoNVC upgrade=unequip item=" + item.template.id + " from=+" + item.upgrade + " target=+" + n + " stone=" + AutoNhiemVuChinh.getStonePower(char_));
        Service.gI().itemBodyToBag(1);
        return true;
    }

    private boolean continueWeaponUpgrade(Char char_, long l) {
        if (char_.arrItemBag == null || char_.arrItemBody == null) {
            return true;
        }
        if (this.weaponUpgradeState == 1) {
            if (l - this.lastInventoryAction < 1000L) {
                return true;
            }
            Item item = AutoNhiemVuChinh.findBagUpgradeItem(char_, this.upgradeWeaponTemplateId, this.upgradeWeaponLevel);
            if (item == null) {
                if (char_.arrItemBody.length > 1) {
                    for (int i = 0; i < char_.arrItemBody.length; ++i) {
                        Item item2 = char_.arrItemBody[i];
                        if (item2 == null || item2.template == null || item2.template.id != this.upgradeWeaponTemplateId) continue;
                        return true;
                    }
                }
                if (char_.arrItemBody.length > 1 && char_.arrItemBody[1] != null || l - this.lastInventoryAction > 10000L) {
                    this.resetWeaponUpgrade();
                }
                return true;
            }
            if (GameCanvas.menu.showMenu || ChatPopup.b != null || GameCanvas.currentDialog != null) {
                if (l - this.lastInventoryAction > 10000L) {
                    Service.gI().useItem(item.indexUI);
                    this.resetWeaponUpgrade();
                }
                return true;
            }
            int n = AutoNhiemVuChinh.getUpgradeRequirement(item);
            Item[] itemArray = AutoNhiemVuChinh.chooseUpgradeStones(char_, n);
            int n2 = AutoNhiemVuChinh.getUpgradeCost(item);
            if (itemArray == null || n2 < 0 || char_.xu + char_.yen < n2) {
                System.out.println("AutoNVC upgrade=cancel item=" + item.template.id + " reason=stone-or-money");
                Service.gI().useItem(item.indexUI);
                this.resetWeaponUpgrade();
                return true;
            }
            GameScr.itemUpGrade = item;
            GameScr.arrItemUpGrade = itemArray;
            char_.arrItemBag[item.indexUI] = null;
            int n3 = 0;
            for (int i = 0; i < itemArray.length; ++i) {
                Item item3 = itemArray[i];
                if (item3 == null) continue;
                n3 += AutoNhiemVuChinh.getStoneValue(item3);
                char_.arrItemBag[item3.indexUI] = null;
            }
            System.out.println("AutoNVC upgrade=send item=" + item.template.id + " from=+" + item.upgrade + " stone=" + n3 + "/" + n + " cost=" + n2);
            Service.gI().upgradeItem(item, itemArray, false);
            holdBagSortForStoneUpgrade = false;
            this.weaponUpgradeState = 2;
            this.lastInventoryAction = l;
            return true;
        }
        if (this.weaponUpgradeState == 2) {
            if (l - this.lastInventoryAction > 20000L) {
                System.out.println("AutoNVC upgrade=timeout restore-local-items");
                this.weaponUpgradeUnavailable = true;
                GameCanvas.n();
                this.restorePendingUpgrade(char_);
                this.notice("Server khong ho tro lenh dap da cua client nay; da trang bi lai vu khi");
                return true;
            }
            if (l - this.lastInventoryAction < 1200L || GameCanvas.currentDialog != null || !AutoNhiemVuChinh.areUpgradeMaterialsCleared()) {
                return true;
            }
            Item item = GameScr.itemUpGrade;
            if (item == null) {
                item = AutoNhiemVuChinh.findBagUpgradeResult(char_, this.upgradeWeaponTemplateId, this.upgradeWeaponLevel);
                if (item != null) {
                    System.out.println("AutoNVC upgrade=equip-result-from-bag item=" + item.template.id + " level=+" + item.upgrade);
                    Service.gI().useItem(item.indexUI);
                    if (char_.taskMaint != null && AutoNhiemVuChinh.isUpgradeTaskStep(char_.ctaskId, char_.taskMaint)) {
                        this.completedUpgradeTaskId = char_.ctaskId;
                        this.completedUpgradeTaskIndex = char_.taskMaint.index;
                        this.completedUpgradeAt = l;
                    }
                    this.resetWeaponUpgrade();
                    this.lastInventoryAction = l;
                    return true;
                }
                this.resetWeaponUpgrade();
                return true;
            }
            if (item.isUpMax() || item.upgrade >= 4) {
                System.out.println("AutoNVC upgrade=skip-max item=" + item.template.id + " level=+" + item.upgrade);
                Service.gI().useItem(item.indexUI);
                this.resetWeaponUpgrade();
                this.lastInventoryAction = l;
                return true;
            }
            int n = item.indexUI;
            if (n < 0 || n >= char_.arrItemBag.length || char_.arrItemBag[n] != null) {
                n = AutoNhiemVuChinh.findFreeBagIndex(char_);
            }
            if (n < 0) {
                this.notice("Khong con o trong de trang bi lai vu khi sau khi dap da");
                return true;
            }
            item.indexUI = n;
            char_.arrItemBag[n] = item;
            GameScr.itemUpGrade = null;
            GameScr.arrItemUpGrade = null;
            System.out.println("AutoNVC upgrade=equip item=" + item.template.id + " result=+" + item.upgrade + " bag=" + n);
            Service.gI().useItem(n);
            if (char_.taskMaint != null && AutoNhiemVuChinh.isUpgradeTaskStep(char_.ctaskId, char_.taskMaint)) {
                this.completedUpgradeTaskId = char_.ctaskId;
                this.completedUpgradeTaskIndex = char_.taskMaint.index;
                this.completedUpgradeAt = l;
            }
            this.resetWeaponUpgrade();
            this.lastInventoryAction = l;
            return true;
        }
        this.resetWeaponUpgrade();
        return false;
    }

    private void restorePendingUpgrade(Char char_) {
        int n;
        Item item = GameScr.itemUpGrade;
        if (GameScr.arrItemUpGrade != null) {
            for (n = 0; n < GameScr.arrItemUpGrade.length; ++n) {
                Item item2 = GameScr.arrItemUpGrade[n];
                if (item2 == null) continue;
                AutoNhiemVuChinh.putBackInBag(char_, item2);
                GameScr.arrItemUpGrade[n] = null;
            }
        }
        n = item == null ? -1 : AutoNhiemVuChinh.putBackInBag(char_, item);
        GameScr.itemUpGrade = null;
        GameScr.arrItemUpGrade = null;
        this.resetWeaponUpgrade();
        this.lastInventoryAction = System.currentTimeMillis();
        if (n >= 0) {
            Service.gI().useItem(n);
        } else {
            this.notice("Khong khoi phuc duoc vu khi sau khi dap da qua thoi gian");
        }
    }

    private static int putBackInBag(Char char_, Item item) {
        if (item == null) {
            return -1;
        }
        int n = item.indexUI;
        if (n < 0 || n >= char_.arrItemBag.length || char_.arrItemBag[n] != null) {
            n = AutoNhiemVuChinh.findFreeBagIndex(char_);
        }
        if (n >= 0) {
            item.indexUI = n;
            char_.arrItemBag[n] = item;
        }
        return n;
    }

    private static boolean canUpgradeItem(Char char_, Item item) {
        int n = AutoNhiemVuChinh.getUpgradeRequirement(item);
        int n2 = AutoNhiemVuChinh.getUpgradeCost(item);
        return n > 0 && n2 >= 0 && char_.xu + char_.yen >= n2 && AutoNhiemVuChinh.getSeparatedStonePower(char_) >= n;
    }

    private static int getUpgradeRequirement(Item item) {
        if (item == null || item.template == null || item.upgrade < 0) {
            return -1;
        }
        int[] nArray = item.isTypeWeapon() ? GameScr.cq : (item.isTypeClothe() ? GameScr.co : (int[])(item.isTypeAdorn() ? GameScr.cp : null));
        return nArray != null && item.upgrade < nArray.length ? nArray[item.upgrade] : -1;
    }

    private static int getUpgradeCost(Item item) {
        if (item == null || item.template == null || item.upgrade < 0) {
            return -1;
        }
        int[] nArray = item.isTypeWeapon() ? GameScr.cu : (item.isTypeClothe() ? GameScr.cs : (int[])(item.isTypeAdorn() ? GameScr.ct : null));
        return nArray != null && item.upgrade < nArray.length ? nArray[item.upgrade] : -1;
    }

    private static int getStonePower(Char char_) {
        int n = 0;
        if (char_.arrItemBag != null) {
            for (int i = 0; i < char_.arrItemBag.length; ++i) {
                Item item = char_.arrItemBag[i];
                int n2 = item == null || item.quantity <= 0 ? 1 : item.quantity;
                n += AutoNhiemVuChinh.getStoneValue(item) * n2;
            }
        }
        return n;
    }

    private boolean maintainManaPotion(Char char_) {
        if (char_ == null || char_.cMaxMP <= 0 || char_.cHp <= 0 || char_.statusMe == 14 || char_.statusMe == 5) {
            return false;
        }
        Char.isAHP = true;
        long l = System.currentTimeMillis();
        if ((long)char_.cMP * 100L >= (long)char_.cMaxMP * 35L || l - this.lastManaPotionAction < 700L || GameCanvas.menu.showMenu || ChatPopup.b != null || GameCanvas.currentDialog != null) {
            return false;
        }
        Item item = AutoNhiemVuChinh.findUsableManaPotion(char_.arrItemBag, char_.clevel);
        if (item != null) {
            System.out.println("AutoNVC mana=use-potion mp=" + char_.cMP + "/" + char_.cMaxMP + " bag=" + item.indexUI + " template=" + item.template.id + " level=" + item.template.level);
            Service.gI().useItem(item.indexUI);
            this.lastManaPotionAction = l;
            return true;
        }
        if (char_.arrItemBox == null) {
            if (l - this.lastBoxRequest >= 2500L) {
                System.out.println("AutoNVC mana=request-box mp=" + char_.cMP + "/" + char_.cMaxMP);
                Service.gI().requestItem(4);
                this.lastBoxRequest = l;
            }
            return false;
        }
        Item item2 = AutoNhiemVuChinh.findUsableManaPotion(char_.arrItemBox, char_.clevel);
        if (item2 != null && Char.af() > 0) {
            System.out.println("AutoNVC mana=take-from-box mp=" + char_.cMP + "/" + char_.cMaxMP + " box=" + item2.indexUI + " template=" + item2.template.id + " level=" + item2.template.level);
            Service.gI().itemBoxToBag(item2.indexUI);
            this.lastManaPotionAction = l;
            this.lastInventoryAction = l;
            return true;
        }
        Item item3 = AutoNhiemVuChinh.findFood(char_, AutoNhiemVuChinh.getSuitableFoodLevel(char_.clevel));
        if (item3 != null && !AutoNhiemVuChinh.hasFoodEffect(char_)) {
            System.out.println("AutoNVC mana=use-food-fallback mp=" + char_.cMP + "/" + char_.cMaxMP + " bag=" + item3.indexUI + " template=" + item3.template.id);
            Service.gI().useItem(item3.indexUI);
            this.lastManaPotionAction = l;
            return true;
        }
        this.notice("Het binh MP phu hop (chi dung binh cap <= " + char_.clevel + ")");
        return false;
    }

    private static Item findUsableManaPotion(Item[] itemArray, int n) {
        if (itemArray == null) {
            return null;
        }
        Item item = null;
        for (int i = 0; i < itemArray.length; ++i) {
            Item item2 = itemArray[i];
            if (item2 == null || item2.template == null || item2.template.type != 17 || item2.template.level > n || item != null && item2.template.level <= item.template.level) continue;
            item = item2;
        }
        return item;
    }

    private boolean maintainKimchiX2(Char char_) {
        if (char_ == null || char_.arrItemBag == null || char_.cHp <= 0 || char_.statusMe == 14 || char_.statusMe == 5 || GameCanvas.menu.showMenu || ChatPopup.b != null || GameCanvas.currentDialog != null) {
            return false;
        }
        long l = System.currentTimeMillis();
        if (l - this.lastKimchiUseAt < 1800000L) {
            return false;
        }
        for (int i = 0; i < char_.arrItemBag.length; ++i) {
            Item item = char_.arrItemBag[i];
            if (!AutoNhiemVuChinh.isKimchiX2(item)) continue;
            System.out.println("AutoNVC kimchi=use-x2 bag=" + item.indexUI + " template=" + item.template.id + " name=" + item.template.name);
            Service.gI().useItem(item.indexUI);
            this.lastKimchiUseAt = l;
            this.lastAction = l;
            return true;
        }
        return false;
    }

    private static boolean isKimchiX2(Item item) {
        if (item == null || item.template == null) {
            return false;
        }
        String string = item.template.name == null ? "" : item.template.name.toLowerCase();
        String string2 = item.template.description == null ? "" : item.template.description.toLowerCase();
        return string.indexOf("kim chi") >= 0 || string.indexOf("kimchi") >= 0 || string2.indexOf("kim chi") >= 0 || string2.indexOf("kimchi") >= 0;
    }

    private static int getSeparatedStonePower(Char char_) {
        int n = 0;
        if (char_ != null && char_.arrItemBag != null) {
            for (int i = 0; i < char_.arrItemBag.length; ++i) {
                Item item = char_.arrItemBag[i];
                if (!AutoNhiemVuChinh.isUpgradeStoneLv2(item) || item.quantity != 1) continue;
                n += AutoNhiemVuChinh.getStoneValue(item);
            }
        }
        return n;
    }

    private static int getStoneValue(Item item) {
        int n = item == null || item.template == null ? -1 : (int)item.template.id;
        return AutoNhiemVuChinh.isUpgradeStoneLv2(item) && GameScr.cn != null && n >= 0 && n < GameScr.cn.length ? GameScr.cn[n] : 0;
    }

    private static boolean isUpgradeStoneLv2(Item item) {
        return item != null && item.template != null && item.template.type == 26;
    }

    private static boolean isUpgradeStoneLv2(ItemTemplate itemTemplate) {
        return itemTemplate != null && itemTemplate.type == 26;
    }

    private static Item[] chooseUpgradeStones(Char char_, int n) {
        int n2;
        if (n <= 0 || AutoNhiemVuChinh.getSeparatedStonePower(char_) < n) {
            return null;
        }
        Item[] itemArray = new Item[18];
        boolean[] blArray = new boolean[char_.arrItemBag.length];
        int n3 = 0;
        for (int i = 0; i < itemArray.length && n3 < n; n3 += n2, ++i) {
            int n4 = n - n3;
            int n5 = -1;
            n2 = -1;
            int n6 = -1;
            int n7 = Integer.MAX_VALUE;
            for (int j = 0; j < char_.arrItemBag.length; ++j) {
                Item item = char_.arrItemBag[j];
                if (blArray[j] || item == null || item.quantity != 1) continue;
                int n8 = AutoNhiemVuChinh.getStoneValue(item);
                if (n8 > 0 && n8 <= n4 && n8 > n2) {
                    n2 = n8;
                    n5 = j;
                    continue;
                }
                if (n8 <= n4 || n8 >= n7) continue;
                n7 = n8;
                n6 = j;
            }
            if (n5 < 0) {
                n5 = n6;
                n2 = n7;
            }
            if (n5 < 0) {
                return null;
            }
            blArray[n5] = true;
            itemArray[i] = char_.arrItemBag[n5];
        }
        return n3 >= n ? itemArray : null;
    }

    private static boolean areUpgradeMaterialsCleared() {
        if (GameScr.arrItemUpGrade == null) {
            return true;
        }
        for (int i = 0; i < GameScr.arrItemUpGrade.length; ++i) {
            if (GameScr.arrItemUpGrade[i] == null) continue;
            return false;
        }
        return true;
    }

    private static Item findBagWeapon(Char char_, int n, int n2) {
        Item item = AutoNhiemVuChinh.findBagUpgradeItem(char_, n, n2);
        return item != null && item.isTypeWeapon() ? item : null;
    }

    private static Item findBagUpgradeItem(Char char_, int n, int n2) {
        for (int i = 0; i < char_.arrItemBag.length; ++i) {
            Item item = char_.arrItemBag[i];
            if (item == null || item.template == null || item.template.id != n || item.upgrade != n2 || !item.isTypeWeapon() && !item.isTypeClothe() && !item.isTypeAdorn()) continue;
            return item;
        }
        return null;
    }

    private static Item findAuto50Task12UpgradeItem(Char char_, Task task) {
        if (char_ == null) {
            return null;
        }
        if (AutoNhiemVuChinh.isTask12CharmStep(task)) {
            return AutoNhiemVuChinh.findTask12ItemByName(char_, true);
        }
        if (AutoNhiemVuChinh.isTask12ShoesStep(task)) {
            return AutoNhiemVuChinh.findTask12ItemByName(char_, false);
        }
        int n = task == null ? -1 : task.index;
        int n2 = AutoNhiemVuChinh.getAuto50Task12TargetTemplate(char_, n);
        if (n2 < 0) {
            return AutoNhiemVuChinh.findQuestUpgradeItem(char_, AutoNhiemVuChinh.isWeaponUpgradeStep(12, char_.taskMaint));
        }
        return AutoNhiemVuChinh.findItemByTemplateForUpgrade(char_, n2);
    }

    private static int getAuto50Task12TargetTemplate(Char char_, int n) {
        if (n == 1) {
            int n2 = char_.nClass == null ? 0 : char_.nClass.classId;
            return n2 > 0 && n2 < CLASS_WEAPON.length ? CLASS_WEAPON[n2] : -1;
        }
        if (n == 2) {
            return 52;
        }
        if (n == 3) {
            return char_.cgender == 1 ? 53 : 54;
        }
        return -1;
    }

    private static Item findTask12ItemByName(Char char_, boolean bl) {
        Item item;
        int n;
        if (char_.arrItemBody != null) {
            for (n = 0; n < char_.arrItemBody.length; ++n) {
                item = char_.arrItemBody[n];
                if (!AutoNhiemVuChinh.isTask12NamedItem(char_, item, bl)) continue;
                return item;
            }
        }
        if (char_.arrItemBag != null) {
            for (n = 0; n < char_.arrItemBag.length; ++n) {
                item = char_.arrItemBag[n];
                if (!AutoNhiemVuChinh.isTask12NamedItem(char_, item, bl)) continue;
                return item;
            }
        }
        return null;
    }

    private static boolean isTask12NamedItem(Char char_, Item item, boolean bl) {
        return item != null && item.template != null && AutoNhiemVuChinh.isQuestUpgradeItem(char_, item, false) && (bl ? AutoNhiemVuChinh.isCharmTemplate(item.template) : AutoNhiemVuChinh.isShoesTemplate(item.template));
    }

    private static boolean isTask12CharmStep(Task task) {
        String string = AutoNhiemVuChinh.getStepName(task).toLowerCase();
        return string.indexOf("trang s") >= 0 || string.indexOf("b\u00f9a") >= 0 || string.indexOf("bua") >= 0;
    }

    private static boolean isTask12ShoesStep(Task task) {
        String string = AutoNhiemVuChinh.getStepName(task).toLowerCase();
        return string.indexOf("y ph") >= 0 || string.indexOf("gi\u00e0y") >= 0 || string.indexOf("giay") >= 0;
    }

    private static boolean isCharmTemplate(ItemTemplate itemTemplate) {
        if (itemTemplate == null || itemTemplate.name == null) {
            return false;
        }
        String string = itemTemplate.name.toLowerCase();
        return string.indexOf("b\u00f9a") >= 0 || string.indexOf("bua") >= 0;
    }

    private static boolean isShoesTemplate(ItemTemplate itemTemplate) {
        if (itemTemplate == null || itemTemplate.name == null) {
            return false;
        }
        String string = itemTemplate.name.toLowerCase();
        return string.indexOf("gi\u00e0y") >= 0 || string.indexOf("giay") >= 0;
    }

    private static Item findItemByTemplateForUpgrade(Char char_, int n) {
        Item item;
        int n2;
        if (char_.arrItemBody != null) {
            for (n2 = 0; n2 < char_.arrItemBody.length; ++n2) {
                item = char_.arrItemBody[n2];
                if (item == null || item.template == null || item.template.id != n || !AutoNhiemVuChinh.isQuestUpgradeItem(char_, item, false)) continue;
                return item;
            }
        }
        if (char_.arrItemBag != null) {
            for (n2 = 0; n2 < char_.arrItemBag.length; ++n2) {
                item = char_.arrItemBag[n2];
                if (item == null || item.template == null || item.template.id != n || !AutoNhiemVuChinh.isQuestUpgradeItem(char_, item, false)) continue;
                return item;
            }
        }
        return null;
    }

    private static Item findBagUpgradeResult(Char char_, int n, int n2) {
        if (char_ == null || char_.arrItemBag == null) {
            return null;
        }
        for (int i = 0; i < char_.arrItemBag.length; ++i) {
            Item item = char_.arrItemBag[i];
            if (item == null || item.template == null || item.template.id != n || item.upgrade <= n2 || !item.isTypeWeapon() && !item.isTypeClothe() && !item.isTypeAdorn()) continue;
            return item;
        }
        return null;
    }

    private static int findFreeBagIndex(Char char_) {
        for (int i = 0; i < char_.arrItemBag.length; ++i) {
            if (char_.arrItemBag[i] != null) continue;
            return i;
        }
        return -1;
    }

    private void resetWeaponUpgrade() {
        this.weaponUpgradeState = 0;
        this.upgradeWeaponTemplateId = -1;
        this.upgradeWeaponLevel = -1;
        this.stoneSplitState = 0;
        this.stoneSplitIndex = -1;
        holdBagSortForStoneUpgrade = false;
    }

    private static boolean isRoninServer() {
        return mResources.d((String)"indServer") == 13;
    }

    private static Item findUnsuitableEquipment(Char char_) {
        int n = char_.nClass == null ? 0 : char_.nClass.classId;
        for (int i = 0; i < char_.arrItemBag.length; ++i) {
            boolean bl;
            Item item = char_.arrItemBag[i];
            if (item == null || item.template == null || item.isLock || item.upgrade != 0 || item.template.type < 0 || item.template.type > 9) continue;
            boolean bl2 = item.template.gender != 2 && item.template.gender != char_.cgender;
            int n2 = item.isTypeWeapon() ? AutoNhiemVuChinh.getWeaponClass(item.template.id) : 0;
            boolean bl3 = n2 > 0 && n > 0 && n2 != n;
            Item item2 = item.template.type < char_.arrItemBody.length ? char_.arrItemBody[item.template.type] : null;
            boolean bl4 = bl = item2 != null && item2.template != null && item.template.level < item2.template.level;
            if (!bl2 && !bl3 && !bl) continue;
            return item;
        }
        return null;
    }

    private static int getWeaponClass(int n) {
        int n2;
        for (n2 = 1; n2 < CLASS_WEAPON.length; ++n2) {
            if (CLASS_WEAPON[n2] != n) continue;
            return n2;
        }
        if (n >= 40 && n <= 93) {
            return (n - 40) / 9 + 1;
        }
        for (n2 = 0; n2 < SPECIAL_CLASS_WEAPON.length; ++n2) {
            if (SPECIAL_CLASS_WEAPON[n2] != n) continue;
            return n2 / 5 + 1;
        }
        return 0;
    }

    private static boolean isCurrentClassWeapon(Char char_, ItemTemplate itemTemplate) {
        return char_ != null && char_.nClass != null && char_.nClass.classId > 0 && itemTemplate != null && itemTemplate.type == 1 && AutoNhiemVuChinh.getWeaponClass(itemTemplate.id) == char_.nClass.classId;
    }

    private static boolean hasFreeBoxSlot(Char char_) {
        if (char_ == null || char_.arrItemBox == null) {
            return false;
        }
        for (int i = 0; i < char_.arrItemBox.length; ++i) {
            if (char_.arrItemBox[i] != null) continue;
            return true;
        }
        return false;
    }

    private static Item findClassWeaponToStore(Char char_) {
        if (char_ == null || char_.arrItemBag == null || char_.nClass == null || char_.nClass.classId <= 0) {
            return null;
        }
        Item item = char_.ctaskId == 12 && AutoNhiemVuChinh.isUpgradeTaskStep(char_.ctaskId, char_.taskMaint) ? AutoNhiemVuChinh.findAuto50Task12UpgradeItem(char_, char_.taskMaint) : null;
        for (int i = 0; i < char_.arrItemBag.length; ++i) {
            Item item2 = char_.arrItemBag[i];
            if (item2 == null || item2 == item || item2.template == null || !AutoNhiemVuChinh.isCurrentClassWeapon(char_, item2.template)) continue;
            return item2;
        }
        return null;
    }

    private void doWeaponTraining(int n, int n2, int n3) {
        if (n == 0) {
            Char char_ = Char.getMyChar();
            if (char_.arrItemBody != null && char_.arrItemBody.length > 1 && char_.arrItemBody[1] != null) {
                this.notice("Da trang bi vu khi, dang cho server chuyen sang bu nhin");
                return;
            }
            if (char_.arrItemBag != null && System.currentTimeMillis() - this.lastAction >= 1600L) {
                for (int i = 0; i < char_.arrItemBag.length; ++i) {
                    Item item = char_.arrItemBag[i];
                    if (item == null || item.template == null || !item.isTypeWeapon() || item.template.level > char_.clevel) continue;
                    System.out.println("AutoNVC action=equipWeapon bag=" + i + " item=" + item.template.id);
                    Service.gI().useItem(i);
                    this.lastAction = System.currentTimeMillis();
                    return;
                }
            }
            this.doSpecialTask("Khong tim thay vu khi phu hop trong hanh trang");
            return;
        }
        this.doTrainingDummy(n2, n3, Char.getMyChar().taskMaint.count);
    }

    private void doTrainingDummy(int n, int n2, int n3) {
        int n4;
        if (n2 <= 0 || n3 >= n2) {
            this.notice("Da danh du bu nhin, dang cho server chuyen buoc");
            return;
        }
        if (n < 0) {
            n = 22;
        }
        if (TileMap.mapID != n) {
            this.a(n, -2, -1, -1);
            return;
        }
        Char char_ = Char.getMyChar();
        Mob mob = AutoNhiemVuChinh.getNearestTrainingMob(char_);
        if (mob == null) {
            this.notice("Dang cho bu nhin xuat hien");
            return;
        }
        Skill skill = char_.myskill;
        int n5 = skill == null ? 35 : skill.dx;
        int n6 = n4 = skill == null ? 25 : skill.dy;
        if (n5 < 25) {
            n5 = 25;
        }
        if (n4 < 20) {
            n4 = 20;
        }
        int n7 = Math.abs(char_.cx - mob.xFirst);
        int n8 = Math.abs(char_.cy - mob.yFirst);
        if (n7 > n5 || n8 > n4) {
            int n9 = n5 / 2;
            if (n9 < 18) {
                n9 = 18;
            } else if (n9 > 35) {
                n9 = 35;
            }
            int n10 = mob.xFirst + (char_.cx <= mob.xFirst ? -n9 : n9);
            System.out.println("AutoNVC train=approach mob=" + mob.mobId + " from=" + char_.cx + "," + char_.cy + " to=" + n10 + "," + mob.yFirst + " range=" + n5 + "," + n4);
            char_.mobFocus = null;
            if (!Char.c((int)n10, (int)mob.yFirst)) {
                Char.b((int)n10, (int)mob.yFirst);
            }
            return;
        }
        char_.mobFocus = mob;
        System.out.println("AutoNVC train=attack mob=" + mob.mobId + " distance=" + n7 + "," + n8 + " count=" + n3 + "/" + n2);
        this.attackTracked(-1, 3);
    }

    private static Mob getNearestTrainingMob(Char char_) {
        if (GameScr.vMobAttack == null) {
            return null;
        }
        Mob mob = null;
        int n = -1;
        for (int i = 0; i < GameScr.vMobAttack.size(); ++i) {
            Mob mob2 = (Mob)GameScr.vMobAttack.elementAt(i);
            if (mob2 == null || mob2.hp <= 0 || mob2.status == 0 || mob2.status == 1) continue;
            int n2 = Math.abs(char_.cx - mob2.xFirst) + Math.abs(char_.cy - mob2.yFirst);
            if (n >= 0 && n2 >= n) continue;
            mob = mob2;
            n = n2;
        }
        return mob;
    }

    private void doProgressTask(int n, int n2, int n3) {
        if (n2 <= 0 || n3 >= n2) {
            this.notice("Da du tien do, dang cho server chuyen buoc");
            return;
        }
        if (n < 0) {
            this.doSpecialTask("Khong co map cho buoc dem " + n3 + "/" + n2);
            return;
        }
        if (TileMap.mapID != n) {
            this.a(n, -2, -1, -1);
            return;
        }
        Char char_ = Char.getMyChar();
        if (AutoNhiemVuChinh.isMineralCollectionStep(char_) && this.seekQuestItemAcrossMap(char_, 212)) {
            return;
        }
        if (this.autoPickItems(-1)) {
            return;
        }
        if (GameScr.vMobAttack != null && GameScr.vMobAttack.size() > 0) {
            this.attackTracked(-1, 3);
        } else {
            this.notice("Dang cho quai/vat pham nhiem vu xuat hien tren map " + n);
        }
    }

    private static boolean isMineralCollectionStep(Char char_) {
        if (char_ == null || char_.ctaskId != 14 || char_.taskMaint == null) {
            return false;
        }
        return AutoNhiemVuChinh.taskContainsText(char_.taskMaint, "kho\u00e1ng th\u1ea1ch") || AutoNhiemVuChinh.taskContainsText(char_.taskMaint, "khoang thach");
    }

    private boolean seekQuestItemAcrossMap(Char char_, int n) {
        this.updatePendingPickState();
        if (this.pendingPickMapItem >= 0 || char_ == null || char_.cHp <= 0 || GameScr.vItemMap == null || GameScr.vItemMap.size() == 0) {
            return false;
        }
        ItemMap itemMap = null;
        int n2 = Integer.MAX_VALUE;
        for (int i = 0; i < GameScr.vItemMap.size(); ++i) {
            int n3;
            ItemMap itemMap2 = (ItemMap)GameScr.vItemMap.elementAt(i);
            if (!this.isAvailableItem(itemMap2) || itemMap2.template.id != n || !AutoNhiemVuChinh.canStoreItem(itemMap2.template, true) || (n3 = Math.abs(char_.cx - itemMap2.xEnd) + Math.abs(char_.cy - itemMap2.yEnd)) >= n2) continue;
            itemMap = itemMap2;
            n2 = n3;
        }
        if (itemMap == null) {
            return false;
        }
        long l = System.currentTimeMillis();
        if (l - itemMap.l < 1200L || l - this.lastPickAction < 350L) {
            return true;
        }
        System.out.println("AutoNVC quest=seek-map-item template=" + n + " mapItem=" + itemMap.g + " at=" + itemMap.xEnd + "," + itemMap.yEnd + " distance=" + n2);
        char_.mobFocus = null;
        char_.itemFocus = itemMap;
        Char.b((int)itemMap.xEnd, (int)TileMap.d((int)itemMap.xEnd, (int)itemMap.yEnd));
        Auto.a((long)100L);
        Service.gI().pickItem(itemMap.g);
        itemMap.l = l;
        this.lastPickAction = l;
        this.pendingPickMapItem = itemMap.g;
        this.pendingPickAt = l;
        return true;
    }

    private static Item findClassSkillBook(Char char_, int n) {
        Item item;
        if (char_ == null || char_.arrItemBag == null) {
            return null;
        }
        Item item2 = null;
        for (int i = 0; i < char_.arrItemBag.length; ++i) {
            boolean bl;
            Item item3 = char_.arrItemBag[i];
            if (item3 == null || item3.template == null) continue;
            if (AutoNhiemVuChinh.isMartialBookAlreadyLearned(char_, item3)) continue;
            String string = item3.template.name == null ? "" : item3.template.name.toLowerCase();
            String string2 = item3.template.description == null ? "" : item3.template.description.toLowerCase();
            boolean bl2 = bl = string.indexOf("s\u00e1ch v\u00f5 c\u00f4ng") >= 0 || string.indexOf("sach vo cong") >= 0 || string.indexOf("v\u00f5 c\u00f4ng") >= 0 || string.indexOf("vo cong") >= 0 || string.indexOf("b\u00ed k\u00edp") >= 0 || string.indexOf("bi kip") >= 0 || string2.indexOf("s\u00e1ch v\u00f5 c\u00f4ng") >= 0 || string2.indexOf("sach vo cong") >= 0 || string2.indexOf("h\u1ecdc v\u00f5 c\u00f4ng") >= 0 || string2.indexOf("hoc vo cong") >= 0;
            if (bl) {
                return item3;
            }
            if (item2 != null || item3.template.type != 10) continue;
            item2 = item3;
        }
        if (item2 != null) {
            return item2;
        }
        if (n > 0 && n < CLASS_SKILL_BOOK.length && (item = AutoNhiemVuChinh.findBagItem(CLASS_SKILL_BOOK[n])) != null && item.template != null && item.template.type != 1 && !AutoNhiemVuChinh.isMartialBookAlreadyLearned(char_, item)) {
            return item;
        }
        return null;
    }

    private static Item findUpgradeJunk(Char char_, Item item) {
        if (char_ == null || char_.arrItemBag == null) {
            return null;
        }
        int n = char_.nClass == null ? 0 : char_.nClass.classId;
        for (int i = 0; i < char_.arrItemBag.length; ++i) {
            boolean bl;
            Item item2 = char_.arrItemBag[i];
            if (item2 == null || item2.template == null || item2.isLock || item2.upgrade != 0 || item2.template.type < 0 || item2.template.type > 9 || item2 == item) continue;
            boolean bl2 = item2.template.gender != 2 && item2.template.gender != char_.cgender;
            int n2 = item2.isTypeWeapon() ? AutoNhiemVuChinh.getWeaponClass(item2.template.id) : 0;
            boolean bl3 = n2 > 0 && n > 0 && n2 != n;
            Item item3 = item2.template.type < char_.arrItemBody.length ? char_.arrItemBody[item2.template.type] : null;
            boolean bl4 = bl = item3 != null && item3.template != null && item2.template.level <= item3.template.level;
            if (item2.template.level >= 10 && !bl2 && !bl3 && !bl) continue;
            return item2;
        }
        return null;
    }

    private void doTeacherChallenge(Char char_, int n) {
        boolean bl;
        int n2 = AutoNhiemVuChinh.getChallengeBattleMap(n);
        long l = System.currentTimeMillis();
        if (this.challengeBattleMap != n2) {
            this.challengeBattleMap = n2;
            this.challengeEnteredAt = l;
            this.lastChallengeAttack = 0L;
            System.out.println("AutoNVC challenge=entered taskIndex=" + n + " map=" + n2);
        }
        if (char_.cMaxHp > 0 && (long)char_.cHp * 100L < (long)char_.cMaxHp * 50L) {
            if (l - this.lastAction >= 1200L && char_.doUsePotion(16)) {
                this.lastAction = l;
            }
            return;
        }
        if (char_.cMaxMP > 0 && (long)char_.cMP * 100L < (long)char_.cMaxMP * 25L && l - this.lastAction >= 1200L && char_.doUsePotion(17)) {
            this.lastAction = l;
            return;
        }
        Char char_2 = AutoNhiemVuChinh.findChallengeTeacher(char_);
        if (char_2 == null) {
            char_.charFocus = null;
            this.notice("Dang cho thay co xuat hien trong phong thach dau map " + n2);
            return;
        }
        char_.mobFocus = null;
        char_.npcFocus = null;
        char_.itemFocus = null;
        char_.charFocus = char_2;
        if (l - this.challengeEnteredAt < 2500L) {
            return;
        }
        Skill skill = char_.myskill;
        if (skill == null || skill.template == null || skill.template.type == 2 || skill.template.type == 4) {
            skill = Auto.g;
        }
        if (skill == null || skill.template == null || skill.template.type == 2 || skill.template.type == 4) {
            this.notice("Khong co ky nang tan cong de thach dau thay co");
            return;
        }
        if (!Char.isMeCanAttackOtherPlayer((Char)char_2)) {
            this.notice("Dang cho server bat trang thai thach dau voi " + char_2.cName);
            return;
        }
        long l2 = (long)skill.coolDown + 50L;
        if (l2 < 350L) {
            l2 = 350L;
        }
        if (l - this.lastChallengeAttack < l2) {
            return;
        }
        int n3 = Math.abs(char_.cx - char_2.cx);
        int n4 = Math.abs(char_.cy - char_2.cy);
        boolean bl2 = bl = n3 <= skill.b() && n4 <= skill.c() && (char_.g() || char_.cy >= char_2.cy);
        if (!bl) {
            int n5 = skill.b() / 2;
            if (n5 < 18) {
                n5 = 18;
            } else if (n5 > 35) {
                n5 = 35;
            }
            int n6 = char_2.cx + (char_.cx <= char_2.cx ? -n5 : n5);
            boolean bl3 = Char.c((int)n6, (int)char_2.cy);
            if (!bl3) {
                char_.currentMovePoint = new MovePoint(n6, char_2.cy);
            }
            this.lastChallengeAttack = l;
            if (l - this.lastNotice >= 5000L) {
                System.out.println("AutoNVC challenge=route map=" + n2 + " teacher=" + char_2.cName + " from=" + char_.cx + "," + char_.cy + " to=" + n6 + "," + char_2.cy + " routed=" + bl3 + " range=" + skill.b() + "," + skill.c());
                this.notice("Dang vuot dia hinh den cho thay " + char_2.cName);
            }
            return;
        }
        if (char_.myskill != skill) {
            Service.gI().selectSkill((int)skill.template.id);
            char_.myskill = skill;
            this.lastAction = l;
            this.lastChallengeAttack = l;
            return;
        }
        MyVector myVector = new MyVector();
        MyVector myVector2 = new MyVector();
        myVector2.addElement((Object)char_2);
        Service.gI().sendPlayerAttack(myVector, myVector2, 2);
        skill.lastTimeUseThisSkill = l;
        skill.l = true;
        char_.cMP -= skill.manaUse;
        if (char_.cMP < 0) {
            char_.cMP = 0;
        }
        if (GameScr.s != null && skill.template.id >= 0 && skill.template.id < GameScr.s.length && GameScr.s[skill.template.id] != null) {
            char_.b(GameScr.s[skill.template.id], 0);
        }
        this.lastChallengeAttack = l;
        System.out.println("AutoNVC challenge=playerAttack map=" + n2 + " teacher=" + char_2.cName + " id=" + char_2.charID + " hp=" + char_2.cHp + "/" + char_2.cMaxHp + " skill=" + skill.template.id + " distance=" + n3 + "," + n4);
    }

    private void enterTeacherChallengeRoom(Char char_, int n) {
        int n2 = AutoNhiemVuChinh.getChallengeBattleMap(n);
        int n3 = AutoNhiemVuChinh.getChallengeSchoolMap(n);
        int n4 = AutoNhiemVuChinh.getChallengeTeacherNpc(n3);
        if (n3 < 0 || n4 < 0) {
            this.notice("Khong xac dinh duoc thay co de vao phong thach dau");
            return;
        }
        if (TileMap.mapID != n3) {
            this.routeToTaskMap(n3, "vao truong thach dau");
            return;
        }
        Npc npc = GameScr.i((int)n4);
        if (npc == null) {
            this.notice("Dang cho thay co tai truong de vao phong thach dau");
            return;
        }
        long l = System.currentTimeMillis();
        if (GameCanvas.menu.showMenu) {
            if (GameCanvas.menu.selectMenuIndexForAuto(0)) {
                System.out.println("AutoNVC challenge=select-room taskIndex=" + n + " school=" + n3 + " teacher=" + n4 + " menu=0");
                this.lastChallengeEntryAction = l;
                this.lastAction = l;
                this.notice("Da chon vao phong thi dau");
            }
            return;
        }
        if (l - this.lastChallengeEntryAction < 2000L) {
            return;
        }
        char_.mobFocus = null;
        char_.charFocus = null;
        char_.itemFocus = null;
        char_.npcFocus = npc;
        if (ChatPopup.b != null) {
            ChatPopup.b = null;
        }
        System.out.println("AutoNVC challenge=enter-room taskIndex=" + n + " school=" + n3 + " teacher=" + n4 + " battle=" + n2);
        GameScr.b((int)n4, (int)0, (int)0);
        this.lastChallengeEntryAction = l;
        this.lastAction = l;
        this.notice("Dang an thay co va vao phong thach dau");
    }

    private static Char findChallengeTeacher(Char char_) {
        if (GameScr.vCharInMap == null) {
            return null;
        }
        Char char_2 = null;
        int n = Integer.MAX_VALUE;
        for (int i = 0; i < GameScr.vCharInMap.size(); ++i) {
            int n2;
            Char char_3 = (Char)GameScr.vCharInMap.elementAt(i);
            if (char_3 == null || char_3 == char_ || char_3.cHp <= 0 || char_3.statusMe == 14 || char_3.statusMe == 5 || char_3.statusMe == 15 || (n2 = Math.abs(char_.cx - char_3.cx) + Math.abs(char_.cy - char_3.cy)) >= n) continue;
            char_2 = char_3;
            n = n2;
        }
        return char_2;
    }

    private static int getChallengeBattleMap(int n) {
        switch (n) {
            case 1: {
                return 56;
            }
            case 2: {
                return 0;
            }
            case 3: {
                return 73;
            }
        }
        return -1;
    }

    private static int getChallengeSchoolMap(int n) {
        switch (n) {
            case 1: {
                return 72;
            }
            case 2: {
                return 27;
            }
            case 3: {
                return 1;
            }
        }
        return -1;
    }

    private static int getChallengeTeacherNpc(int n) {
        switch (n) {
            case 1: {
                return 9;
            }
            case 72: {
                return 10;
            }
            case 27: {
                return 11;
            }
        }
        return -1;
    }

    private boolean autoPickItems(int n) {
        return this.autoPickItems(n, false);
    }

    private boolean autoPickItems(int n, boolean bl) {
        int n2;
        int n3;
        ItemMap itemMap;
        int n4;
        boolean bl2;
        this.updatePendingPickState();
        if (this.pendingPickMapItem >= 0) {
            return false;
        }
        if (GameScr.vItemMap == null || GameScr.vItemMap.size() == 0) {
            return false;
        }
        Char char_ = Char.getMyChar();
        if (char_ == null || char_.cHp <= 0) {
            return false;
        }
        ItemMap itemMap2 = null;
        boolean bl3 = false;
        int n5 = Integer.MAX_VALUE;
        boolean bl4 = char_.ctaskId == 12 && AutoNhiemVuChinh.isUpgradeTaskStep(char_.ctaskId, char_.taskMaint);
        boolean bl5 = bl2 = bl4 && AutoNhiemVuChinh.isWeaponUpgradeStep(char_.ctaskId, char_.taskMaint);
        if (n >= 0) {
            for (n4 = 0; n4 < GameScr.vItemMap.size(); ++n4) {
                itemMap = (ItemMap)GameScr.vItemMap.elementAt(n4);
                boolean available = bl ? this.isAvailableItem(itemMap) : this.isLocalOwnDrop(char_, itemMap);
                if (!available || itemMap.template.id != n || !AutoNhiemVuChinh.canStoreItem(itemMap.template, true) || (n2 = Math.abs(char_.cx - itemMap.xEnd) + Math.abs(char_.cy - itemMap.yEnd)) >= n5) continue;
                itemMap2 = itemMap;
                n5 = n2;
                bl3 = true;
            }
        }
        if (itemMap2 == null) {
            if (this.targetLevel == AutoNv130QuickPolicy.targetLevel() && char_.clevel >= 20) {
                for (n4 = 0; n4 < GameScr.vItemMap.size(); ++n4) {
                    itemMap = (ItemMap)GameScr.vItemMap.elementAt(n4);
                    if (!this.isLocalOwnDrop(char_, itemMap) || !AutoNhiemVuChinh.isQuickPickupEquipment(itemMap.template, char_) || !AutoNhiemVuChinh.canStoreItem(itemMap.template, false) || (n3 = Math.abs(char_.cx - itemMap.xEnd) + Math.abs(char_.cy - itemMap.yEnd)) >= n5) continue;
                    itemMap2 = itemMap;
                    n5 = n3;
                }
            }
        }
        if (itemMap2 == null) {
            for (n4 = 0; n4 < GameScr.vItemMap.size(); ++n4) {
                itemMap = (ItemMap)GameScr.vItemMap.elementAt(n4);
                if (!this.isLocalOwnDrop(char_, itemMap) || !AutoNhiemVuChinh.isQuestGroundItem(itemMap.template) || !AutoNhiemVuChinh.canStoreItem(itemMap.template, true) || (n3 = Math.abs(char_.cx - itemMap.xEnd) + Math.abs(char_.cy - itemMap.yEnd)) >= n5) continue;
                itemMap2 = itemMap;
                n5 = n3;
                bl3 = true;
            }
        }
        if (itemMap2 == null) {
            for (n4 = 0; n4 < GameScr.vItemMap.size(); ++n4) {
                itemMap = (ItemMap)GameScr.vItemMap.elementAt(n4);
                if (!this.isLocalOwnDrop(char_, itemMap) || !AutoNhiemVuChinh.isPriorityResourceGroundItem(itemMap.template) || !AutoNhiemVuChinh.canStoreItem(itemMap.template, false) || (n3 = Math.abs(char_.cx - itemMap.xEnd) + Math.abs(char_.cy - itemMap.yEnd)) >= n5) continue;
                itemMap2 = itemMap;
                n5 = n3;
            }
        }
        if (itemMap2 == null && AutoNhiemVuChinh.hasNearbyAttackableMob(char_)) {
            return false;
        }
        if (itemMap2 == null && bl4 && AutoNhiemVuChinh.canPickClassWeaponIntoBox(char_)) {
            for (n4 = 0; n4 < GameScr.vItemMap.size(); ++n4) {
                itemMap = (ItemMap)GameScr.vItemMap.elementAt(n4);
                if (!this.isLocalOwnDrop(char_, itemMap) || !AutoNhiemVuChinh.isCurrentClassWeapon(char_, itemMap.template) || !AutoNhiemVuChinh.isTask12UpgradeDrop(char_, itemMap.template, bl2) || !AutoNhiemVuChinh.canStoreItem(itemMap.template, true) || (n3 = Math.abs(char_.cx - itemMap.xEnd) + Math.abs(char_.cy - itemMap.yEnd)) >= n5) continue;
                itemMap2 = itemMap;
                n5 = n3;
            }
        }
        if (itemMap2 == null && AutoNhiemVuPanel.autoPickNormal) {
            for (n4 = 0; n4 < GameScr.vItemMap.size(); ++n4) {
                int n7;
                boolean bl6;
                itemMap = (ItemMap)GameScr.vItemMap.elementAt(n4);
                n3 = 0;
                boolean equipment = AutoNhiemVuChinh.isEquipmentTemplate(itemMap == null ? null : itemMap.template);
                boolean bl7 = bl6 = !equipment || bl4 && AutoNhiemVuChinh.isTask12UpgradeDrop(char_, itemMap.template, bl2);
                if (n3 != 0 || !bl6 || !this.isLocalOwnDrop(char_, itemMap) || !AutoNhiemVuChinh.canStoreItem(itemMap.template, false) || (n7 = Math.abs(char_.cx - itemMap.xEnd) + Math.abs(char_.cy - itemMap.yEnd)) >= n5) continue;
                itemMap2 = itemMap;
                n5 = n7;
                bl3 = n >= 0 && itemMap.template.id == n;
            }
        }
        if (itemMap2 == null) {
            return false;
        }
        long l = System.currentTimeMillis();
        if (l - itemMap2.l < 1200L || l - this.lastPickAction < 350L) {
            return true;
        }
        n3 = char_.cx;
        n2 = char_.cy;
        Mob mob = char_.mobFocus;
        System.out.println("AutoNVC pickup=" + (bl3 ? "quest" : "normal") + " template=" + itemMap2.template.id + " mapItem=" + itemMap2.g + " at=" + itemMap2.xEnd + "," + itemMap2.yEnd + " freeBag=" + Char.af() + " force=" + bl);
        char_.mobFocus = null;
        Char.b((int)itemMap2.xEnd, (int)TileMap.d((int)itemMap2.xEnd, (int)itemMap2.yEnd));
        Auto.a((long)100L);
        char_.itemFocus = itemMap2;
        Service.gI().pickItem(itemMap2.g);
        itemMap2.l = l;
        this.lastPickAction = l;
        this.pendingPickMapItem = itemMap2.g;
        this.pendingPickAt = l;
        Auto.a((long)100L);
        if (!bl) {
            Char.b((int)n3, (int)n2);
            char_.mobFocus = mob;
        }
        return true;
    }

    private boolean autoPickUpgradeMaterials(Char char_, boolean bl, boolean bl2) {
        int n;
        this.updatePendingPickState();
        if (this.pendingPickMapItem >= 0) {
            return false;
        }
        if (char_ == null || char_.cHp <= 0 || GameScr.vItemMap == null || GameScr.vItemMap.size() == 0) {
            return false;
        }
        ItemMap itemMap = null;
        boolean bl3 = false;
        int n2 = Integer.MAX_VALUE;
        for (int i = 0; i < 3 && itemMap == null; ++i) {
            boolean bl4 = bl && char_.ctaskId == 12 && i == 0;
            for (n = 0; n < GameScr.vItemMap.size(); ++n) {
                int n3;
                ItemMap itemMap2 = (ItemMap)GameScr.vItemMap.elementAt(n);
                if (!this.isAvailableItem(itemMap2) || !AutoNhiemVuChinh.canStoreItem(itemMap2.template, true)) continue;
                boolean candidate = i == 0 ? bl4 && AutoNhiemVuChinh.isTask12UpgradeDrop(char_, itemMap2.template, bl2) : (i == 1 ? AutoNhiemVuChinh.isUpgradeStoneLv2(itemMap2.template) : char_.ctaskId == 12 && AutoNhiemVuChinh.canPickClassWeaponIntoBox(char_) && AutoNhiemVuChinh.isCurrentClassWeapon(char_, itemMap2.template));
                if (!candidate || (n3 = Math.abs(char_.cx - itemMap2.xEnd) + Math.abs(char_.cy - itemMap2.yEnd)) >= n2) continue;
                itemMap = itemMap2;
                bl3 = bl4;
                n2 = n3;
            }
        }
        if (itemMap == null) {
            return false;
        }
        long l = System.currentTimeMillis();
        if (l - itemMap.l < 1200L || l - this.lastPickAction < 350L) {
            return true;
        }
        n = char_.cx;
        int n4 = char_.cy;
        Mob mob = char_.mobFocus;
        System.out.println("AutoNVC pickup=upgrade-" + (bl3 ? "equipment" : "stone") + " template=" + itemMap.template.id + " mapItem=" + itemMap.g + " at=" + itemMap.xEnd + "," + itemMap.yEnd + " freeBag=" + Char.af());
        char_.mobFocus = null;
        Char.b((int)itemMap.xEnd, (int)TileMap.d((int)itemMap.xEnd, (int)itemMap.yEnd));
        Auto.a((long)100L);
        char_.itemFocus = itemMap;
        Service.gI().pickItem(itemMap.g);
        itemMap.l = l;
        this.lastPickAction = l;
        this.pendingPickMapItem = itemMap.g;
        this.pendingPickAt = l;
        Auto.a((long)100L);
        Char.b((int)n, (int)n4);
        char_.mobFocus = mob;
        return true;
    }

    private static boolean canPickClassWeaponIntoBox(Char char_) {
        return char_ != null && (char_.arrItemBox == null ? Char.af() > 2 : AutoNhiemVuChinh.hasFreeBoxSlot(char_));
    }

    private static boolean isEquipmentTemplate(ItemTemplate itemTemplate) {
        return itemTemplate != null && itemTemplate.type >= 0 && itemTemplate.type <= 9;
    }

    /** Loads sale price before retaining quick-flow equipment, then drops cheap/wrong-gender pieces. */
    private boolean maintainQuickPickupInventory(Char char_) {
        if (this.targetLevel != AutoNv130QuickPolicy.targetLevel() || char_.clevel < 20
                || char_.arrItemBag == null || System.currentTimeMillis() - this.lastInventoryAction < 1800L) {
            return false;
        }
        for (int i = 0; i < char_.arrItemBag.length; ++i) {
            Item item = char_.arrItemBag[i];
            if (item == null || item.template == null || item.isLock || item.upgrade != 0
                    || item.template.type < 1 || item.template.type > 9) continue;
            boolean matchingGender = item.template.gender == 2 || item.template.gender == char_.cgender;
            if (!matchingGender) {
                Service.gI().saleItem(item.indexUI, 1);
                this.lastInventoryAction = System.currentTimeMillis();
                return true;
            }
            if (!item.s) {
                item.t = System.currentTimeMillis();
                Service.gI().requestItemInfo(item.typeUI, item.indexUI);
                this.lastInventoryAction = item.t;
                return true;
            }
            if (item.saleCoinLock <= 5) {
                Service.gI().saleItem(item.indexUI, 1);
                this.lastInventoryAction = System.currentTimeMillis();
                return true;
            }
        }
        return false;
    }

    private static boolean isQuickPickupEquipment(ItemTemplate itemTemplate, Char char_) {
        return itemTemplate != null && char_ != null && itemTemplate.type >= 1 && itemTemplate.type <= 9
                && (itemTemplate.gender == 2 || itemTemplate.gender == char_.cgender);
    }

    private static boolean isTask12UpgradeDrop(Char char_, ItemTemplate itemTemplate, boolean bl) {
        if (itemTemplate == null) {
            return false;
        }
        Task task = char_.taskMaint;
        if (AutoNhiemVuChinh.isTask12CharmStep(task)) {
            return AutoNhiemVuChinh.isCharmTemplate(itemTemplate);
        }
        if (AutoNhiemVuChinh.isTask12ShoesStep(task)) {
            return AutoNhiemVuChinh.isShoesTemplate(itemTemplate);
        }
        int n = char_.taskMaint == null ? -1 : char_.taskMaint.index;
        int n2 = AutoNhiemVuChinh.getAuto50Task12TargetTemplate(char_, n);
        if (n2 >= 0) {
            return itemTemplate.id == n2;
        }
        return AutoNhiemVuChinh.isQuestUpgradeDrop(char_, itemTemplate, bl) && bl;
    }

    private static boolean isQuestUpgradeDrop(Char char_, ItemTemplate itemTemplate, boolean bl) {
        if (itemTemplate == null || itemTemplate.level < 10 || itemTemplate.level > char_.clevel || itemTemplate.gender != 2 && itemTemplate.gender != char_.cgender) {
            return false;
        }
        if (bl && itemTemplate.type != 1) {
            return false;
        }
        if (itemTemplate.type < 0 || itemTemplate.type > 9) {
            return false;
        }
        if (itemTemplate.type == 1 && char_.nClass != null && char_.nClass.classId > 0) {
            int n = AutoNhiemVuChinh.getWeaponClass(itemTemplate.id);
            return n == 0 || n == char_.nClass.classId;
        }
        return true;
    }

    private boolean isAvailableItem(ItemMap itemMap) {
        return itemMap != null && itemMap.template != null && !itemMap.k && itemMap.i != 2 && itemMap.g != this.pendingPickMapItem && !this.isIgnoredPick(itemMap.g);
    }

    private boolean isLocalOwnDrop(Char char_, ItemMap itemMap) {
        if (!this.isAvailableItem(itemMap) || char_ == null) {
            return false;
        }
        return Res.a((int)char_.cx, (int)char_.cy, (int)itemMap.xEnd, (int)itemMap.yEnd) <= 150;
    }

    private static boolean hasNearbyAttackableMob(Char char_) {
        if (char_ == null || GameScr.vMobAttack == null) {
            return false;
        }
        for (int i = 0; i < GameScr.vMobAttack.size(); ++i) {
            Mob mob = (Mob)GameScr.vMobAttack.elementAt(i);
            if (mob == null || mob.hp <= 0 || mob.levelBoss == 3 || Math.abs(char_.cx - mob.xFirst) + Math.abs(char_.cy - mob.yFirst) > 120) continue;
            return true;
        }
        return false;
    }

    private void updatePendingPickState() {
        short s = TileMap.mapID;
        byte by = TileMap.zoneID;
        if (s != this.ignoredPickMap || by != this.ignoredPickZone) {
            this.ignoredPickMap = s;
            this.ignoredPickZone = by;
            this.ignoredPickCount = 0;
            this.pendingPickMapItem = -1;
            this.pendingPickAt = 0L;
            return;
        }
        if (this.pendingPickMapItem < 0) {
            return;
        }
        ItemMap itemMap = AutoNhiemVuChinh.findGroundMapItem(this.pendingPickMapItem);
        if (itemMap == null || itemMap.k || itemMap.i == 2) {
            this.pendingPickMapItem = -1;
            this.pendingPickAt = 0L;
            return;
        }
        if (System.currentTimeMillis() - this.pendingPickAt < 1800L) {
            return;
        }
        this.ignorePick(this.pendingPickMapItem);
        System.out.println("AutoNVC pickup=skip-owner-locked mapItem=" + this.pendingPickMapItem + " template=" + (itemMap.template == null ? -1 : (int)itemMap.template.id) + " map=" + s + " zone=" + by);
        this.pendingPickMapItem = -1;
        this.pendingPickAt = 0L;
    }

    private static ItemMap findGroundMapItem(int n) {
        if (GameScr.vItemMap == null) {
            return null;
        }
        for (int i = 0; i < GameScr.vItemMap.size(); ++i) {
            ItemMap itemMap = (ItemMap)GameScr.vItemMap.elementAt(i);
            if (itemMap == null || itemMap.g != n) continue;
            return itemMap;
        }
        return null;
    }

    private boolean isIgnoredPick(int n) {
        for (int i = 0; i < this.ignoredPickCount; ++i) {
            if (this.ignoredPickMapItems[i] != n) continue;
            return true;
        }
        return false;
    }

    private void ignorePick(int n) {
        if (this.isIgnoredPick(n)) {
            return;
        }
        if (this.ignoredPickCount < this.ignoredPickMapItems.length) {
            this.ignoredPickMapItems[this.ignoredPickCount++] = n;
        }
    }

    private static boolean isQuestGroundItem(ItemTemplate itemTemplate) {
        String string;
        if (itemTemplate == null) {
            return false;
        }
        if (itemTemplate.b()) {
            return true;
        }
        String string2 = itemTemplate.name == null ? "" : itemTemplate.name.toLowerCase();
        String string3 = string = itemTemplate.description == null ? "" : itemTemplate.description.toLowerCase();
        if (string2.indexOf("kho\u00e1ng th\u1ea1ch") >= 0 || string2.indexOf("khoang thach") >= 0 || string2.indexOf("x\u00e1c ch\u00e2u ch\u1ea5u") >= 0 || string2.indexOf("xac chau chau") >= 0 || string.indexOf("v\u1eadt ph\u1ea9m nhi\u1ec7m v\u1ee5") >= 0 || string.indexOf("vat pham nhiem vu") >= 0 || string.indexOf("quest item") >= 0) {
            return true;
        }
        switch (itemTemplate.id) {
            case 209: 
            case 210: 
            case 211: 
            case 221: 
            case 231: 
            case 232: 
            case 233: 
            case 234: 
            case 235: 
            case 237: {
                return true;
            }
        }
        Char char_ = Char.getMyChar();
        return string2.length() >= 4 && char_ != null && AutoNhiemVuChinh.taskContainsText(char_.taskMaint, string2);
    }

    private static boolean taskContainsText(Task task, String string) {
        if (task == null || string == null || string.length() == 0) {
            return false;
        }
        return AutoNhiemVuChinh.containsTaskText(task.subNames, string) || AutoNhiemVuChinh.containsTaskText(task.names, string) || AutoNhiemVuChinh.containsTaskText(task.details, string);
    }

    private static boolean containsTaskText(String[] stringArray, String string) {
        if (stringArray == null) {
            return false;
        }
        for (int i = 0; i < stringArray.length; ++i) {
            String string2 = stringArray[i];
            if (string2 == null || string2.toLowerCase().indexOf(string) < 0) continue;
            return true;
        }
        return false;
    }

    private static boolean isHpMpGroundItem(ItemTemplate itemTemplate) {
        return itemTemplate != null && (itemTemplate.type == 16 || itemTemplate.type == 17);
    }

    private static boolean isPriorityResourceGroundItem(ItemTemplate itemTemplate) {
        return AutoNhiemVuChinh.isYenGroundItem(itemTemplate) || AutoNhiemVuChinh.isHpMpGroundItem(itemTemplate) || AutoNhiemVuChinh.isUpgradeStoneLevelTwoOrAbove(itemTemplate);
    }

    private static boolean isYenGroundItem(ItemTemplate itemTemplate) {
        return itemTemplate != null && (itemTemplate.id == -1 || itemTemplate.type == 19);
    }

    private static boolean isUpgradeStoneLevelTwoOrAbove(ItemTemplate itemTemplate) {
        return itemTemplate != null && itemTemplate.type == 26 && itemTemplate.id >= 1;
    }

    private static boolean canStoreItem(ItemTemplate itemTemplate, boolean bl) {
        boolean bl2;
        if (itemTemplate == null) {
            return false;
        }
        if (!bl && itemTemplate.type == 18 && Char.getMyChar() != null && itemTemplate.level < AutoNhiemVuChinh.getSuitableFoodLevel(Char.getMyChar().clevel)) {
            return false;
        }
        boolean bl3 = bl2 = itemTemplate.id == -1 || itemTemplate.type == 19 || itemTemplate.isUpToUp && AutoNhiemVuChinh.findBagItem(itemTemplate.id) != null;
        if (bl) {
            return Char.af() > 0 || bl2;
        }
        return Char.af() > 2 || bl2;
    }

    private void doVisitTask(int n) {
        if (n < 0) {
            this.doSpecialTask("Buoc tham map khong co dich den hop le");
            return;
        }
        if (TileMap.mapID != n) {
            this.routeToTaskMap(n, "tham map nhiem vu");
        } else {
            this.notice("Da toi map " + n + ", dang cho server ghi nhan");
        }
    }

    private void doSpecialTask(String string) {
        this.notice(string);
    }

    private void goToNpc(int n, int n2, String string) {
        this.goToNpc(n, n2, string, null);
    }

    private void goToNpc(int n, int n2, String string, String string2) {
        if (n < 0 || n2 < 0) {
            this.notice("Khong xac dinh duoc NPC de " + string + " (map=" + n + ", npc=" + n2 + ")");
            return;
        }
        if (TileMap.mapID != n) {
            this.routeToTaskMap(n, string);
            return;
        }
        Npc npc = GameScr.i((int)n2);
        if (npc == null) {
            this.notice("Khong tim thay NPC " + n2 + " tren map " + n);
            return;
        }
        Char char_ = Char.getMyChar();
        if (Math.abs(char_.cx - npc.cx) > 22 || Math.abs(char_.cy - npc.cy) > 22) {
            Char.b((int)npc.cx, (int)npc.cy);
            return;
        }
        if (System.currentTimeMillis() - this.lastAction < 1600L) {
            return;
        }
        long now = System.currentTimeMillis();
        if (COMPLETE_TASK_MENU.equals(string2) && this.lastCompleteTaskSubmitId == char_.ctaskId
                && this.lastCompleteTaskSubmitIndex == (char_.taskMaint == null ? -1 : char_.taskMaint.index)
                && now - this.lastCompleteTaskSubmitAt < 12000L) {
            this.notice("Dang cho server xac nhan hoan thanh nhiem vu");
            return;
        }
        char_.npcFocus = npc;
        if (GameCanvas.menu.selectTaskForAuto(this.menuSelections, string2)) {
            System.out.println("AutoNVC action=selectMenu npc=" + n2 + " try=" + this.menuSelections);
            ++this.menuSelections;
            if (COMPLETE_TASK_MENU.equals(string2)) {
                this.lastCompleteTaskSubmitAt = now;
                this.lastCompleteTaskSubmitId = char_.ctaskId;
                this.lastCompleteTaskSubmitIndex = char_.taskMaint == null ? -1 : char_.taskMaint.index;
                System.out.println("AutoNVC completeTask=locked task=" + this.lastCompleteTaskSubmitId
                        + " index=" + this.lastCompleteTaskSubmitIndex + " forMs=12000");
            }
        } else if (GameCanvas.menu.showMenu) {
            this.notice("Menu hien tai khong co nut nhiem vu an toan, khong tu bam bua");
        } else if (ChatPopup.b != null && ChatPopup.b.a != null) {
            System.out.println("AutoNVC action=nextDialog npc=" + n2);
            ChatPopup.b.a.a();
        } else if (!(npc.chatPopup == null || COMPLETE_TASK_MENU.equals(string2)
                || UPGRADE_ITEM_MENU.equals(string2) || TALK_ONLY_MENU.equals(string2)
                || string2 != null && string2.startsWith(JOIN_CLASS_MENU))) {
            System.out.println("AutoNVC action=acceptTask npc=" + n2);
            Service.gI().getTask(n2, 0);
            npc.chatPopup = null;
            GameScr.gI().resetButton();
        } else {
            System.out.println("AutoNVC action=openTaskMenu npc=" + n2);
            Service.gI().openMenu(n2);
        }
        this.lastAction = System.currentTimeMillis();
    }

    private static boolean isTaskDataValid(Task task) {
        return task != null && task.counts != null && task.subNames != null && task.index >= 0 && task.index < task.counts.length && task.index < task.subNames.length;
    }

    private static int getMaxCount(Task task) {
        return task != null && task.counts != null && task.index >= 0 && task.index < task.counts.length ? task.counts[task.index] : -1;
    }

    private static int getTaskMap(Char char_, Task task) {
        try {
            int n;
            if (GameScr.dg == null || char_.ctaskId < 0 || char_.ctaskId >= GameScr.dg.length || GameScr.dg[char_.ctaskId] == null) {
                return -3;
            }
            int n2 = n = task == null ? 0 : task.index + 1;
            if (n < 0 || n >= GameScr.dg[char_.ctaskId].length) {
                return -3;
            }
            return GameScr.ae();
        }
        catch (Exception exception) {
            return -3;
        }
    }

    private static int getTaskNpc(Char char_, Task task) {
        try {
            int n;
            if (GameScr.df == null || char_.ctaskId < 0 || char_.ctaskId >= GameScr.df.length || GameScr.df[char_.ctaskId] == null) {
                return -3;
            }
            int n2 = n = task == null ? 0 : task.index + 1;
            if (n < 0 || n >= GameScr.df[char_.ctaskId].length) {
                return -3;
            }
            return GameScr.af();
        }
        catch (Exception exception) {
            return -3;
        }
    }

    private static String getStepName(Task task) {
        if (task != null && task.subNames != null && task.index >= 0 && task.index < task.subNames.length && task.subNames[task.index] != null) {
            return task.subNames[task.index];
        }
        return "khong ro";
    }

    private static boolean isReportStep(int n, Task task) {
        if (task == null) {
            return false;
        }
        if (n == 1 && task.index == 5) {
            return true;
        }
        String string = AutoNhiemVuChinh.getStepName(task).toLowerCase();
        return string.indexOf("b\u00e1o c\u00e1o") >= 0 || string.indexOf("bao cao") >= 0 || string.indexOf("b\u00e1o tin") >= 0 || string.indexOf("bao tin") >= 0 || string.indexOf("ho\u00e0n th\u00e0nh") >= 0 || string.indexOf("hoan thanh") >= 0 || string.indexOf("report") >= 0;
    }

    private boolean routeToTaskMap(int n, String string) {
        if (TileMap.mapID == n) {
            return true;
        }
        if (!(n != 32 && n != 33 || AutoNhiemVuChinh.isLangChaiLandCorridorMap(TileMap.mapID))) {
            System.out.println("AutoNVC route=langChaiSafe from=" + TileMap.mapID + " via=27 target=" + n + " reason=" + string);
            this.notice("Dang di duong bo an toan qua map 27, tranh cong map khoa");
            this.a(27, -2, -1, -1);
            return false;
        }
        if (n == 32 || n == 33) {
            System.out.println("AutoNVC route=langChaiSafe from=" + TileMap.mapID + " target=32 reason=" + string);
        }
        this.a(n, -2, -1, -1);
        return false;
    }

    private static boolean isLangChaiLandCorridorMap(int n) {
        return n == 27 || n == 28 || n == 60 || n == 29 || n == 30 || n == 31 || n == 32 || n == 61 || n == 33;
    }

    private static boolean isJaianReportStep(Task task) {
        if (task == null) {
            return false;
        }
        String string = AutoNhiemVuChinh.getStepName(task).toLowerCase();
        return string.indexOf("b\u00e1o tin") >= 0 || string.indexOf("bao tin") >= 0 || string.indexOf("b\u00e1o c\u00e1o") >= 0 || string.indexOf("bao cao") >= 0 || string.indexOf("b\u00e0 rei") >= 0 || string.indexOf("ba rei") >= 0;
    }

    private static boolean isUpgradeTaskStep(int n, Task task) {
        if (task == null) {
            return false;
        }
        if (n == 12 && task.index == 1) {
            return true;
        }
        String string = AutoNhiemVuChinh.getStepName(task).toLowerCase();
        return string.indexOf("n\u00e2ng c\u1ea5p") >= 0 || string.indexOf("nang cap") >= 0 || string.indexOf("upgrade") >= 0;
    }

    private static boolean isWeaponUpgradeStep(int n, Task task) {
        if (task == null) {
            return false;
        }
        String string = AutoNhiemVuChinh.getStepName(task).toLowerCase();
        return string.indexOf("v\u0169 kh\u00ed") >= 0 || string.indexOf("vu khi") >= 0 || string.indexOf("weapon") >= 0;
    }

    private static String getTaskType(int n, int n2) {
        switch (n) {
            case 0: 
            case 1: 
            case 7: 
            case 8: {
                return "NPC/QUIZ";
            }
            case 2: {
                return n2 == 0 ? "EQUIP" : "TRAIN";
            }
            case 3: 
            case 4: 
            case 5: 
            case 10: 
            case 14: {
                return "FIGHT/COLLECT";
            }
            case 6: {
                return "VISIT";
            }
            case 9: 
            case 11: 
            case 12: {
                return "SPECIAL_SAFE";
            }
            case 13: 
            case 15: {
                return "MIXED";
            }
            case 16: {
                return "FIGHT/COLLECT";
            }
            case 17: {
                return "JAIAN";
            }
            case 19: {
                return "WATER";
            }
            case 23: {
                return "DUNGEON";
            }
            case 24: {
                return "TREASURE";
            }
            case 26: 
            case 27: {
                return "ICE";
            }
            case 32: {
                return "FISHING";
            }
            case 18: 
            case 20: 
            case 21: 
            case 22: 
            case 25: 
            case 28: 
            case 29: 
            case 30: 
            case 31: {
                return "ADVANCED";
            }
        }
        return "UNKNOWN";
    }

    public static int getUpgradeRequirementForStandalone(Item item) {
        return getUpgradeRequirement(item);
    }

    public static int getUpgradeCostForStandalone(Item item) {
        return getUpgradeCost(item);
    }

    public static Item[] chooseUpgradeStonesForStandalone(Char char_, int need) {
        return chooseUpgradeStones(char_, need);
    }

    public static boolean areUpgradeMaterialsClearedForStandalone() {
        return areUpgradeMaterialsCleared();
    }

    public static int putBackInBagForStandalone(Char char_, Item item) {
        return putBackInBag(char_, item);
    }

    public static boolean isUpgradeEquipmentForStandalone(Item item) {
        return isQuestEquipmentType(item);
    }

    public static int getSeparatedStonePowerForStandalone(Char char_) {
        return getSeparatedStonePower(char_);
    }

    public static int getStoneValueForStandalone(Item item) {
        return getStoneValue(item);
    }

    public static Item findInsuranceForStandalone(Char char_) {
        if (char_ == null || char_.arrItemBag == null) {
            return null;
        }
        for (int i = 0; i < char_.arrItemBag.length; ++i) {
            Item item = char_.arrItemBag[i];
            if (item != null && item.template != null && item.template.type == 28 && item.quantity != 0) {
                return item;
            }
        }
        return null;
    }

    public static int findFreeBagIndexForStandalone(Char char_) {
        return char_ == null || char_.arrItemBag == null ? -1 : findFreeBagIndex(char_);
    }

    public static int findBodySlotForStandalone(Char char_, Item item) {
        return findBodySlot(char_, item);
    }

    private void notice(String string) {
        if (System.currentTimeMillis() - this.lastNotice >= 5000L) {
            GameScr.addChatPopup((String)string);
            System.out.println("AutoNVC wait: " + string);
            this.lastNotice = System.currentTimeMillis();
        }
    }

    public final String toString() {
        Char char_ = Char.getMyChar();
        if (char_ == null || char_.taskMaint == null) {
            return "Auto NV chinh Lv1-50";
        }
        Task task = char_.taskMaint;
        return "Auto NV " + char_.ctaskId + " - buoc " + (task.index + 1) + " [" + AutoNhiemVuChinh.getTaskType(char_.ctaskId, task.index) + "]";
    }
}

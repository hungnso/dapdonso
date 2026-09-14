import java.io.DataInputStream;
import java.io.IOException;

/** Quick claims the PC activity/attendance rewards through the shared game protocol. */
public final class ActivityQuickClaim {
    public static final int MENU_ACTIVITY = 11990080;
    public static final int MENU_ATTENDANCE = 11990081;
    public static final int MENU_ROOT = 11990079;

    private static final byte COMMAND = (byte)-46;
    private static final int MODE_NONE = 0;
    private static final int MODE_ACTIVITY = 1;
    private static final int MODE_ATTENDANCE = 2;

    private static int mode;
    private static int selectedSubId;
    private static int selectedContentType;
    private static int[] pendingClaims = new int[0];
    private static int pendingIndex;
    private static int claimedCount;

    private ActivityQuickClaim() {
    }

    /** Runs our menu action, then maps it to an unused command so GameScr returns normally. */
    public static int normalizeMenuCommand(int commandId) {
        if (commandId == MENU_ROOT) {
            showQuickRewardMenu();
            return Integer.MIN_VALUE;
        }
        if (commandId == MENU_ACTIVITY) {
            start(MODE_ACTIVITY);
            return Integer.MIN_VALUE;
        }
        if (commandId == MENU_ATTENDANCE) {
            start(MODE_ATTENDANCE);
            return Integer.MIN_VALUE;
        }
        return commandId;
    }

    private static void showQuickRewardMenu() {
        MyVector menu = new MyVector();
        menu.addElement(new Command1("Nh\u1eadn th\u01b0\u1edfng ho\u1ea1t \u0111\u1ed9ng", MENU_ACTIVITY));
        menu.addElement(new Command1("\u0110i\u1ec3m danh nhanh", MENU_ATTENDANCE));
        GameCanvas.menu.startAt(menu);
    }

    /** Consumes command -46 before the legacy Controller switch, where that command is absent. */
    public static void normalizeIncomingMessage(Message message) {
        if (message == null || message.command != COMMAND) {
            return;
        }
        try {
            handle(message.reader());
        } catch (Exception error) {
            reset();
            notifyUser("Lỗi đọc dữ liệu Hoạt động: " + error.getClass().getName());
        } finally {
            // -128 is outside Controller's -30..126 switch and therefore safely returns.
            message.command = Byte.MIN_VALUE;
        }
    }

    private static synchronized void start(int requestedMode) {
        Session_ME session = Session_ME.getInstance();
        if (session == null || !session.connected) {
            notifyUser("Chưa kết nối máy chủ.");
            return;
        }
        mode = requestedMode;
        selectedSubId = -1;
        selectedContentType = 0;
        pendingClaims = new int[0];
        pendingIndex = 0;
        claimedCount = 0;
        sendCategories();
        notifyUser(requestedMode == MODE_ATTENDANCE
                ? "Đang kiểm tra thưởng điểm danh..."
                : "Đang kiểm tra thưởng hoạt động...");
    }

    private static synchronized void handle(DataInputStream input) throws IOException {
        if (input == null) {
            return;
        }
        int subCommand = input.readUnsignedByte();
        if (mode == MODE_NONE) {
            return;
        }
        if (subCommand == 0) {
            handleCategories(input);
        } else if (subCommand == 1) {
            handleDetail(input);
        } else if (subCommand == 2 && mode == MODE_ACTIVITY) {
            handleClaimResult(input);
        } else if (subCommand == 3 && mode == MODE_ATTENDANCE) {
            handleClaimResult(input);
        }
    }

    private static void handleCategories(DataInputStream input) throws IOException {
        int categoryCount = input.readUnsignedByte();
        int bestScore = -1;
        int bestSubId = -1;
        int bestType = 0;

        for (int category = 0; category < categoryCount; ++category) {
            input.readInt(); // category id
            String categoryName = input.readUTF();
            int subCount = input.readUnsignedByte();
            for (int sub = 0; sub < subCount; ++sub) {
                int subId = input.readInt();
                String subName = input.readUTF();
                int contentType = input.readInt();
                input.readBoolean(); // notification/availability flag used by the PC UI

                int score = candidateScore(categoryName, subName, contentType);
                if (score > bestScore) {
                    bestScore = score;
                    bestSubId = subId;
                    bestType = contentType;
                }
            }
        }

        if (bestSubId < 0) {
            int oldMode = mode;
            reset();
            notifyUser(oldMode == MODE_ATTENDANCE
                    ? "Máy chủ chưa có mục Điểm danh."
                    : "Máy chủ chưa có mục Hoạt động.");
            return;
        }
        selectedSubId = bestSubId;
        selectedContentType = bestType;
        sendDetail(bestSubId);
    }

    private static int candidateScore(String categoryName, String subName, int contentType) {
        int requiredType = mode == MODE_ATTENDANCE ? 4 : 1;
        if (contentType != requiredType) {
            return -1;
        }
        String category = fold(categoryName);
        String sub = fold(subName);
        String wanted = mode == MODE_ATTENDANCE ? "diem danh" : "hoat dong";
        int score = 10;
        if (wanted.equals(category)) {
            score += 100;
        } else if (category.indexOf(wanted) >= 0) {
            score += 60;
        }
        if (wanted.equals(sub)) {
            score += 80;
        } else if (sub.indexOf(wanted) >= 0) {
            score += 40;
        }
        return score;
    }

    private static void handleDetail(DataInputStream input) throws IOException {
        int activityId = input.readInt();
        input.readUTF(); // server title/name
        input.readInt(); // server detail metadata
        if (activityId != selectedSubId) {
            return;
        }

        if (selectedContentType == 1 && mode == MODE_ACTIVITY) {
            readTimeline(input);
        } else if (selectedContentType == 4 && mode == MODE_ATTENDANCE) {
            readAttendance(input);
        } else {
            reset();
            notifyUser("Dữ liệu phần thưởng không đúng loại.");
        }
    }

    private static void readTimeline(DataInputStream input) throws IOException {
        int progress = input.readInt();
        int dailyCount = input.readUnsignedByte();
        for (int i = 0; i < dailyCount; ++i) {
            input.readUTF();
            input.readInt();
            input.readInt();
            input.readInt();
            input.readBoolean();
        }

        int milestoneCount = input.readUnsignedByte();
        int[] claimable = new int[milestoneCount];
        int count = 0;
        for (int i = 0; i < milestoneCount; ++i) {
            int milestoneId = input.readInt();
            input.readBoolean(); // completed/status flag
            boolean claimed = input.readBoolean();
            // The PC client uses milestoneId as requiredProgress for this activity type.
            if (!claimed && progress >= milestoneId) {
                claimable[count++] = milestoneId;
            }
        }
        beginClaims(claimable, count);
    }

    private static void readAttendance(DataInputStream input) throws IOException {
        int dayCount = input.readUnsignedByte();
        int[] claimable = new int[dayCount];
        int count = 0;
        for (int i = 0; i < dayCount; ++i) {
            int day = input.readUnsignedByte();
            input.readUnsignedByte(); // reward type
            input.readInt();          // reward id/value
            input.readInt();          // reward quantity
            int status = input.readUnsignedByte();
            if (status == 1) {
                claimable[count++] = day;
            }
        }
        beginClaims(claimable, count);
    }

    private static void beginClaims(int[] values, int count) {
        if (count == 0) {
            int oldMode = mode;
            reset();
            notifyUser(oldMode == MODE_ATTENDANCE
                    ? "Không có thưởng điểm danh có thể nhận."
                    : "Không có mốc hoạt động có thể nhận.");
            return;
        }
        pendingClaims = new int[count];
        System.arraycopy(values, 0, pendingClaims, 0, count);
        pendingIndex = 0;
        claimedCount = 0;
        sendNextClaim();
    }

    private static void handleClaimResult(DataInputStream input) throws IOException {
        if (input.available() >= 4) {
            input.readInt();
        }
        boolean success = input.available() > 0 && input.readBoolean();
        if (success) {
            ++claimedCount;
        }
        ++pendingIndex;
        sendNextClaim();
    }

    private static void sendNextClaim() {
        if (pendingIndex >= pendingClaims.length) {
            int oldMode = mode;
            int total = claimedCount;
            reset();
            notifyUser((oldMode == MODE_ATTENDANCE ? "Điểm danh" : "Hoạt động")
                    + ": đã nhận " + total + " phần thưởng.");
            return;
        }
        int value = pendingClaims[pendingIndex];
        if (mode == MODE_ATTENDANCE) {
            sendClaim((byte)3, selectedSubId, value);
        } else {
            sendClaim((byte)2, selectedSubId, value);
        }
    }

    private static void sendCategories() {
        send((byte)0, 0, false, 0, false);
    }

    private static void sendDetail(int subId) {
        send((byte)1, subId, true, 0, false);
    }

    private static void sendClaim(byte subCommand, int activityId, int value) {
        send(subCommand, activityId, true, value, true);
    }

    private static void send(byte subCommand, int first, boolean hasFirst,
                             int second, boolean hasSecond) {
        Message message = null;
        try {
            message = new Message(COMMAND);
            message.writer().writeByte(subCommand);
            if (hasFirst) {
                message.writer().writeInt(first);
            }
            if (hasSecond) {
                message.writer().writeInt(second);
            }
            Session_ME.getInstance().sendMessage(message);
        } catch (Exception error) {
            reset();
            notifyUser("Không thể gửi yêu cầu phần thưởng.");
        } finally {
            if (message != null) {
                message.close();
            }
        }
    }

    private static void reset() {
        mode = MODE_NONE;
        selectedSubId = -1;
        selectedContentType = 0;
        pendingClaims = new int[0];
        pendingIndex = 0;
        claimedCount = 0;
    }

    private static void notifyUser(String text) {
        InfoMe.addInfo(text);
    }

    private static String fold(String value) {
        if (value == null) {
            return "";
        }
        String source = value.toLowerCase();
        StringBuffer result = new StringBuffer(source.length());
        for (int i = 0; i < source.length(); ++i) {
            char c = source.charAt(i);
            if ("àáạảãâầấậẩẫăằắặẳẵ".indexOf(c) >= 0) c = 'a';
            else if ("èéẹẻẽêềếệểễ".indexOf(c) >= 0) c = 'e';
            else if ("ìíịỉĩ".indexOf(c) >= 0) c = 'i';
            else if ("òóọỏõôồốộổỗơờớợởỡ".indexOf(c) >= 0) c = 'o';
            else if ("ùúụủũưừứựửữ".indexOf(c) >= 0) c = 'u';
            else if ("ỳýỵỷỹ".indexOf(c) >= 0) c = 'y';
            else if (c == 'đ') c = 'd';
            result.append(c);
        }
        return result.toString().trim();
    }
}

/**
 * Sequentially lists matching inventory items on Shinwa using the game's
 * native sale packet.  Only one request is in flight at a time; the next item
 * is sent after Controller receives the normal command-102 success response.
 */
public final class AutoShinwaSale {
    private static final int CMD_OPEN = 11990110;
    private static final int CMD_PRICE = 11990111;
    private static final int CMD_COUNT = 11990112;
    private static final int CMD_STOP = 11990113;

    private static final long NEXT_SALE_DELAY_MS = 900L;
    private static final long ACK_TIMEOUT_MS = 10000L;
    private static final long NO_MATCH_GRACE_MS = 5000L;
    private static final int SALE_FEE_XU = 5000;

    private static boolean active;
    private static boolean waitingAck;
    private static long nextSaleAt;
    private static long ackDeadline;
    private static long noMatchSince;

    private static short templateId;
    private static byte templateType;
    private static int upgrade;
    private static int sys;

    private static int pendingPrice;
    private static int requestedCount;
    private static int remainingCount;
    private static int soldCount;
    private static int plannedCount;
    private static String itemName = "";

    private AutoShinwaSale() {
    }

    /**
     * Hooks GameScr's command dispatcher.  1500 is the native menu opened on
     * the selected Shinwa sale item; we preserve Info/Sell and append Auto.
     */
    public static synchronized int normalizeMenuCommand(int commandId) {
        if (commandId == 1500 && GameScr.isPaintAuctionSale && GameScr.itemSell != null) {
            showSaleMenu();
            return Integer.MIN_VALUE;
        }
        if (commandId == CMD_OPEN) {
            promptPrice();
            return Integer.MIN_VALUE;
        }
        if (commandId == CMD_PRICE) {
            acceptPrice();
            return Integer.MIN_VALUE;
        }
        if (commandId == CMD_COUNT) {
            acceptCountAndStart();
            return Integer.MIN_VALUE;
        }
        if (commandId == CMD_STOP) {
            stop("Đã dừng Auto Shinwa.");
            return Integer.MIN_VALUE;
        }
        return commandId;
    }

    private static void showSaleMenu() {
        MyVector menu = new MyVector();
        menu.addElement(new Command1(mResources.bt, 15001));
        if (Char.getMyChar() != null && Char.getMyChar().xu >= SALE_FEE_XU) {
            menu.addElement(new Command1(mResources.by, 15002));
        }
        if (active) {
            menu.addElement(new Command1("Dừng Auto Shinwa", CMD_STOP));
        } else {
            menu.addElement(new Command1("Auto treo cùng loại", CMD_OPEN));
        }
        GameCanvas.menu.startAt(menu);
    }

    private static void promptPrice() {
        Item sample = GameScr.itemSell;
        if (!canUseSample(sample)) {
            notifyUser("Hãy chọn một món có thể bán trước.");
            return;
        }
        captureSample(sample);
        GameCanvas.n();
        GameCanvas.inputDlg.a("Giá treo cho mỗi lượt bán", new Command1("Tiếp", CMD_PRICE), 1);
    }

    private static void acceptPrice() {
        String value = GameCanvas.inputDlg.tfInput.e();
        int price;
        try {
            price = Integer.parseInt(value == null ? "" : value.trim());
        } catch (Exception ignored) {
            price = 0;
        }
        if (price <= 0) {
            GameCanvas.n();
            notifyUser("Giá bán không hợp lệ.");
            return;
        }
        pendingPrice = price;
        GameCanvas.n();
        GameCanvas.inputDlg.a("Số lượt cần treo (0 = tất cả)", new Command1("Bắt đầu", CMD_COUNT), 1);
    }

    private static void acceptCountAndStart() {
        String value = GameCanvas.inputDlg.tfInput.e();
        int count;
        try {
            count = Integer.parseInt(value == null ? "" : value.trim());
        } catch (Exception ignored) {
            count = -1;
        }
        GameCanvas.n();
        if (count < 0) {
            notifyUser("Số lượng không hợp lệ. Nhập 0 để treo tất cả.");
            return;
        }
        requestedCount = count;
        start();
    }

    private static void start() {
        if (!GameScr.isPaintAuctionSale || GameScr.itemSell == null || !matches(GameScr.itemSell)) {
            notifyUser("Món mẫu đã thay đổi, hãy chọn lại.");
            return;
        }
        if (!isConnected()) {
            notifyUser("Chưa kết nối máy chủ.");
            return;
        }
        Char me = Char.getMyChar();
        if (me == null || me.xu < SALE_FEE_XU) {
            notifyUser("Không đủ 5.000 xu phí treo Shinwa.");
            return;
        }

        int available = countAvailable();
        if (available <= 0) {
            notifyUser("Không còn món phù hợp để treo.");
            return;
        }
        plannedCount = requestedCount == 0 ? available : Math.min(requestedCount, available);
        remainingCount = plannedCount;
        soldCount = 0;
        waitingAck = false;
        active = true;
        nextSaleAt = 0L;
        ackDeadline = 0L;
        noMatchSince = 0L;

        notifyUser("Auto Shinwa: " + itemName + " - " + plannedCount + " lượt.");
        System.out.println("[SHINWA] start item=" + itemName + " price=" + pendingPrice
                + " count=" + plannedCount);
        tick();
    }

    /** Called from Controller immediately after the normal sale-success ACK. */
    public static synchronized void onSaleAck() {
        if (!active || !waitingAck) {
            return;
        }
        waitingAck = false;
        ++soldCount;
        --remainingCount;
        nextSaleAt = System.currentTimeMillis() + NEXT_SALE_DELAY_MS;
        ackDeadline = 0L;
        noMatchSince = 0L;
        System.out.println("[SHINWA] ack " + soldCount + "/" + plannedCount
                + " bagRemaining=" + countBagMatches());

        if (remainingCount <= 0) {
            finish();
        } else if (soldCount == 1 || soldCount % 5 == 0) {
            notifyUser("Auto Shinwa: đã treo " + soldCount + "/" + plannedCount + ".");
        }
    }

    /** Runs from GameScr.update(); sends at most one listing at a time. */
    public static synchronized void tick() {
        if (!active) {
            return;
        }
        long now = System.currentTimeMillis();

        // Sau mỗi lần treo thành công, Controller.resetButton() của game gốc
        // tự đóng màn Gian hàng (isPaintAuctionSale=false). Đây là lifecycle
        // bình thường của command 102, không phải người chơi chủ động dừng.
        // Vì Auto đã được khởi động hợp lệ từ màn Shinwa, các lượt sau tiếp tục
        // gửi trực tiếp theo slot hành trang ngay trên GameScr. Chỉ dừng nếu đã
        // rời hẳn GameScr (login/menu khác).
        if (GameCanvas.currentScreen != GameScr.instance) {
            stop("Auto Shinwa dừng vì đã rời màn chơi.");
            return;
        }
        if (!isConnected()) {
            stop("Auto Shinwa dừng vì mất kết nối.");
            return;
        }
        if (waitingAck) {
            if (ackDeadline > 0L && now >= ackDeadline) {
                stop("Auto Shinwa timeout phản hồi. Hãy vào lại để đồng bộ túi đồ.");
            }
            return;
        }
        if (remainingCount <= 0) {
            finish();
            return;
        }
        if (now < nextSaleAt) {
            return;
        }

        Char me = Char.getMyChar();
        if (me == null || me.arrItemBag == null) {
            stop("Không đọc được hành trang.");
            return;
        }
        if (me.xu < SALE_FEE_XU) {
            stop("Auto Shinwa dừng: không đủ 5.000 xu phí treo.");
            return;
        }

        Item current = GameScr.itemSell;
        if (current != null) {
            if (!matches(current)) {
                stop("Món đang chọn khác mẫu Auto Shinwa.");
                return;
            }
            send(current, now);
            return;
        }

        Item next = findNext(me.arrItemBag);
        if (next == null) {
            // Sau ACK server có thể đang đồng bộ lại túi. Không kết thúc ngay ở
            // frame đầu tiên vì v26 có thể hiểu nhầm là đã hết đồ và dừng sau
            // đúng một món. Chờ một khoảng ngắn rồi quét lại.
            if (noMatchSince == 0L) {
                noMatchSince = now;
                System.out.println("[SHINWA] waiting bag sync sold=" + soldCount
                        + "/" + plannedCount);
                return;
            }
            if (now - noMatchSince < NO_MATCH_GRACE_MS) {
                return;
            }
            if (soldCount >= plannedCount) {
                finish();
            } else {
                stop("Auto Shinwa dừng: không còn món cùng loại trong hành trang (đã treo "
                        + soldCount + "/" + plannedCount + ").");
            }
            return;
        }
        noMatchSince = 0L;

        // Không tự xoá item khỏi arrItemBag và không gán itemSell ở đây.
        // Packet 102 chỉ cần indexUI; khi ACK về, Controller gốc sẽ lấy đúng
        // item từ arrItemBag[index], gán vào itemSell rồi tự xoá slot. Cách này
        // giữ nguyên lifecycle native và tránh trạng thái túi/itemSell lệch sau
        // món đầu tiên.
        send(next, now);
    }

    private static void send(Item item, long now) {
        waitingAck = true;
        ackDeadline = now + ACK_TIMEOUT_MS;
        Service.gI().sendToSaleItem(item, pendingPrice);
        System.out.println("[SHINWA] send slot=" + item.indexUI + " item=" + itemName
                + " price=" + pendingPrice + " progress=" + (soldCount + 1) + "/" + plannedCount);
    }

    private static Item findNext(Item[] bag) {
        for (int i = 0; i < bag.length; ++i) {
            Item item = bag[i];
            if (matches(item)) {
                return item;
            }
        }
        return null;
    }

    private static int findSlot(Item[] bag, Item wanted) {
        for (int i = 0; i < bag.length; ++i) {
            if (bag[i] == wanted) {
                return i;
            }
        }
        return -1;
    }

    private static int countAvailable() {
        int count = matches(GameScr.itemSell) ? 1 : 0;
        Char me = Char.getMyChar();
        if (me != null && me.arrItemBag != null) {
            for (int i = 0; i < me.arrItemBag.length; ++i) {
                if (matches(me.arrItemBag[i])) {
                    ++count;
                }
            }
        }
        return count;
    }

    private static boolean canUseSample(Item item) {
        return item != null && item.template != null && !item.isLock && !item.isExpires;
    }

    private static void captureSample(Item sample) {
        templateId = sample.template.id;
        templateType = sample.template.type;
        upgrade = sample.upgrade;
        sys = sample.sys;
        itemName = sample.template.name == null ? ("ID " + templateId) : sample.template.name;

    }

    /**
     * "Cùng loại" được xác định bằng templateId. v26 vẫn còn lọc upgrade/sys,
     * nên nhiều trang bị cùng tên/cùng ID nhưng khác hệ hoặc cấp cộng chỉ còn
     * đúng món mẫu và Auto kết thúc sau lượt đầu. Các món khoá/hết hạn vẫn bị
     * loại bởi canUseSample().
     */
    private static boolean matches(Item item) {
        return canUseSample(item) && item.template.id == templateId;
    }

    private static int countBagMatches() {
        Char me = Char.getMyChar();
        if (me == null || me.arrItemBag == null) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < me.arrItemBag.length; ++i) {
            if (matches(me.arrItemBag[i])) {
                ++count;
            }
        }
        return count;
    }

    private static boolean isConnected() {
        Session_ME session = Session_ME.getInstance();
        return session != null && session.connected;
    }

    private static void finish() {
        int total = soldCount;
        active = false;
        waitingAck = false;
        remainingCount = 0;
        notifyUser("Auto Shinwa hoàn tất: đã treo " + total + " lượt.");
        System.out.println("[SHINWA] done sold=" + total);
    }

    private static void stop(String message) {
        active = false;
        waitingAck = false;
        remainingCount = 0;
        ackDeadline = 0L;
        nextSaleAt = 0L;
        noMatchSince = 0L;
        notifyUser(message);
        System.out.println("[SHINWA] stop: " + message);
    }

    private static void notifyUser(String message) {
        try {
            InfoMe.addInfo(message);
        } catch (Exception ignored) {
            System.out.println("[SHINWA] " + message);
        }
    }
}

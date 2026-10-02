# Lightweight Runtime Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox syntax for tracking.

**Goal:** Tạo bản JAR nhẹ trên nhánh hiện tại, chỉ giữ VDMQ, Daily/tà thú, 1–70 và các tiện ích đã duyệt.

**Architecture:** Giữ bộ điều phối hiện có, cắt chế độ auto thừa cùng tất cả điểm vào. Sender chờ sự kiện; lịch và phụ trợ dùng cooldown; build lite đóng gói source hiện tại và loại class ứng dụng cũ từ JAR nền.

**Tech Stack:** Java, Java ME CLDC-1.1/MIDP-2.0, javac --release 8, PowerShell, MicroEmulator. Không thêm thư viện runtime.

**Spec:** `docs/superpowers/specs/2026-10-02-lite-runtime-design.md`

## Global Constraints

- Thiết kế này áp dụng trực tiếp nhánh hiện tại.
- Giữ bước vào hang theo cấp rồi reconnect để ghi nhận hoạt động trong Daily.
- Giới hạn kiểm tra lịch Daily và nhận thưởng tối đa một lần mỗi giây.
- Đồng hồ dùng GMT+7.
- Giữ nhịp cập nhật logic game hiện có.
- Không overlay latest_classes và không chạy các ASM patch cũ.
- Không xóa source_history, bộ build cũ hoặc các bản JAR lịch sử.
- Không báo tỷ lệ giảm RAM/CPU nếu chưa đo trên cùng cấu hình thực tế.

## Review Focus

- Nhận key sau khi enqueue: worker phải thức dậy, gửi đúng FIFO, không gửi payload trước key (Task 2).
- Reconnect nhanh khi worker cũ đang chờ: worker cũ phải thoát và không lấy gói tin của kết nối mới (Tasks 2–3).
- Bấm lại auto đang chạy: stack không được tự trỏ hoặc tạo chuỗi resume bị lặp (Task 3).
- Đổi ngày, đổi nhân vật hoặc đồng hồ quay lùi: cooldown phải hồi phục, lịch không chạy trùng ngoài chính sách RMS hiện có (Task 4).
- JAR gốc chứa class auto bị xóa: output lite phải loại chúng và vẫn có SmsData/tài nguyên/manifest (Task 5).

## Task 1: Cắt các chế độ auto thừa

**Files:** Modify `src/NSOT_MOB.java`, `src/GameScr.java`, `src/Controller.java`, `src/Session_ME.java`, `src/Auto.java`, `src/AutoDailyCoordinator.java`; create `src/DailyHangPolicy.java`, `tests/DailyHangPolicyTest.java`; delete các source bên dưới sau khi bỏ tham chiếu.

**Interfaces:** Produce `DailyHangPolicy.mapForLevel(int level): int`, trả -1 dưới Lv30; 91 cho 30–39, 94 cho 40–49, 105 cho 50–59, 114 cho 60–69, 125 cho 70–89, 157 từ Lv90. Các API Daily/VDMQ/1–70/tà thú hiện có giữ nguyên.

- [ ] Kiểm tra `git status`, đọc spec và baseline source; build baseline bằng `powershell -ExecutionPolicy Bypass -File ./build.ps1 -OutputJar build/e72_lite_baseline.jar`. Ghi nhận kích thước, danh sách class và giới hạn chạy emulator. Xác minh đường dẫn tuyệt đối trước mọi recursive delete của script build.
- [ ] Viết `DailyHangPolicyTest`: assert mapping cho 29, 30, 39, 40, 49, 50, 59, 60, 69, 70, 89, 90; compile test với policy chưa tồn tại để xác nhận lỗi, rồi thêm policy và chạy đến exit 0.
- [ ] Thay hai lời gọi `AutoHangDong.map()` trong coordinator bằng `DailyHangPolicy.mapForLevel(me.clevel)`; giữ toàn bộ state vào/thoát hang, marker và recovery.
- [ ] Bỏ khởi tạo, API khởi động, so sánh instance, menu, action switch, lệnh chat/tổ đội và hook của `AutoTanSat`, `AutoDanhVong`, `AutoAttack`, `AutoAttackPk`, `AutoPkAm`, `AutoHSXa`, `Class_af` (auto đứng chờ PK), `AutoHangDong`, `ToolCuoc`. Bỏ `AutoDanhVongPanel`, `DanhVongQuest`, `PK_AM_PANEL`, `MenuHangDong` khi không còn phụ thuộc. Giữ `AutoUpPanel`/`AutoNhayPanel` nếu chức năng của chúng là phụ trợ đã duyệt.
- [ ] Cập nhật nhánh `Auto.java` kiểm tra `instanceof AutoAttack`: giữ trường hợp TaskTaThuAuto, bỏ trường hợp chế độ đã xóa. Không cắt helper chiến đấu/skill/recovery dùng chung.
- [ ] Xóa các source chế độ bỏ, chạy `rg` trên `src/` để xác nhận không còn tham chiếu tên bị xóa; compile source bằng build.ps1 đến exit 0.
- [ ] Chạy policy tests Daily/VDMQ/1–70/tà thú hiện có; commit chỉ các file Task 1 với message `refactor: remove unused automation modes`.

## Task 2: Sender chờ sự kiện và thoát sạch

**Files:** Modify `src/Sender.java`, `src/Session_ME.java`, `src/MessageCollector.java`, `src/NetworkInit.java`; create `tests/SenderLifecycleTest.java`.

**Interfaces:** Preserve `Sender.a(): void` (clear), `Sender.a(Message): void` (enqueue), `Sender.run(): void`. Add `Sender.wakeUp(): void`; gọi khi Session_ME nhận key. Các cờ kết nối/key đọc xuyên thread phải có visibility phù hợp.

- [ ] Viết test chạy Sender thật với Session_ME, dùng ByteArrayOutputStream và phản xạ test để đặt key/stream mà không mở socket. Assertions: trạng thái WAITING khi queue trống; gói đã enqueue chờ key; sau đánh thức gửi hai command đúng thứ tự; clear bỏ gói đang chờ; interrupt/disconnect kết thúc thread trong 2 giây. Test thêm enqueue giữa hai đợt chờ để bắt lỗi mất notify.
- [ ] Compile bằng javac với `MICRO.jar`, baseline classes và test, chạy `java -cp 'build/lite-tests;build/v37-full-source-classes;MICRO.jar' SenderLifecycleTest`; xác nhận baseline thất bại vì polling/thiếu wakeUp.
- [ ] Đồng bộ queue trên một monitor; chờ trong vòng kiểm tra điều kiện queue/key/connected và interrupt. Lấy Message khỏi queue dưới khóa, gửi ngoài khóa. enqueue/clear/wakeUp dùng notifyAll. Không thêm polling 10 ms thay cho wait.
- [ ] Hook chuyển getKeyComplete sang true để đánh thức Sender; cleanNetwork clear queue và interrupt worker. Worker kiểm tra interrupt trước dequeue/send và thoát ngay khi InterruptedException. Giữ handshake -27 gửi trực tiếp như NetworkInit hiện tại.
- [ ] Thêm kiểm tra lặp 50 vòng start/wait/disconnect/reconnect: không còn worker cũ sống và gói cũ không sang phiên mới. Chạy tests đến exit 0; compile source; commit `perf: park idle sender and stop stale workers`.

## Task 3: Vòng đời auto và stack resume

**Files:** Modify `src/NSOT_MOB.java`, `src/ThreadUtil.java`, `src/ThreadSleep.java` nếu cần; create `tests/AutoWorkerLifecycleTest.java`.

**Interfaces:** Preserve `NSOT_MOB.b(): void` (start), `NSOT_MOB.c(): void` (stop), `NSOT_MOB.a(Auto): void` (push), `NSOT_MOB.d(): void` (pop). Ownership của worker là identity Thread hiện hành và cờ chạy visible xuyên thread.

- [ ] Viết test push cùng instance hai lần: `active.l != active`; pop trả đúng auto trước đó. Test start/stop/start nhanh với worker bị chặn: worker cũ không chạy thêm update sau khi mất ownership. Dùng fake Auto đếm lần update và latch test, không phụ thuộc server.
- [ ] Chạy test trước sửa, xác nhận stack hoặc worker test thất bại. Sửa push/pop null/self guard và start/stop ownership dưới khóa; không giữ khóa trong update/pathing hoặc join vô hạn.
- [ ] Các helper sleep được sửa phải giữ interrupt status; run loop kiểm tra ownership và interrupt trước mỗi update. Giữ watchdog ở GameCanvas vì pathing vẫn có thể chặn.
- [ ] Kiểm tra stop/reset/chuyển map chỉ giữ dữ liệu cần resume; không xóa trạng thái daily đang recovery. Chạy tests lifecycle, DailyMapTransferRecoveryTest và DailyTaskTransitionPolicyTest; compile; commit `fix: prevent duplicate auto workers and cyclic resume stacks`.

## Task 4: Giảm lịch, quét và vẽ khi treo

**Files:** Modify `src/NSOT_MOB.java`, `src/GameCanvas.java`, `src/AutoDailyCoordinator.java`, `src/DailyRewardScheduler.java`, `src/AutoDailyPanel.java` nếu reset lịch cần invalidation; create `src/LiteRuntimePolicy.java`, `tests/LiteRuntimePolicyTest.java`.

**Interfaces:** Produce `LiteRuntimePolicy.isDue(long now, long lastRun, long interval): boolean`, true khi lastRun chưa đặt, khi now quay lùi, hoặc elapsed >= interval. `tickSchedule(Calendar)` và `DailyRewardScheduler.tick(Calendar)` vẫn tương thích lời gọi cũ; thêm entry không đối số để kiểm tra cooldown trước khi tạo Calendar.

- [ ] Viết tests isDue: 999 ms false, 1000 ms true, quay lùi true, chưa chạy true. Viết integration assertions lịch bị giới hạn không đọc RMS/tạo Calendar mỗi tick; reset lịch/config phải invalidation cooldown đúng.
- [ ] Xác nhận test thất bại rồi thêm policy; giới hạn lịch ở entry trước `Res.c()`/dateKey/RMS. Giữ các marker RMS hiện có và chính sách thời điểm chạy; không tự thay marker global thành per-character trong tối ưu này.
- [ ] Quét túi để xóa/lọc/các mua bổ sung không khẩn cấp tối đa một lần mỗi giây. Item use/buff giữ cooldown riêng hiện có; không trì hoãn xử lý HP/MP, chết, captcha hoặc combat.
- [ ] Chế độ VPS_LOW_RENDER vẽ tối đa mỗi 200 ms khi gameplay treo; menu/dialog/chat popup và input chủ động vẽ ngay. Giữ update 25 ms và auto tick 80 ms hiện có. Thêm assertions policy cho nhịp vẽ và input ưu tiên.
- [ ] Rà log trong các loop giữ lại: loại thông báo lặp mỗi tick, giữ state-change/error/recovery. Xác minh cache map/mob cũ được clear tại hook map/disconnect hiện có, chỉ sửa nếu có tham chiếu thừa được chứng minh.
- [ ] Chạy LiteRuntimePolicyTest và toàn bộ schedule/recovery/transition tests; compile; commit `perf: throttle background scans and idle rendering`.

## Task 5: JAR lite sạch và hướng dẫn kiểm chứng

**Files:** Create `build-lite.ps1`, `tools/Test-LiteJar.ps1`, `tools/Test-LiteRuntime.ps1`, `README_LITE.md`; modify `build.ps1` chỉ nếu cần dùng chung phần build an toàn.

**Interfaces:** `build-lite.ps1 -OutputJar build/e72_lite.jar -BaseJar e72_x1.jar -ManifestFile META-INF/current_manifest.mf`; `Test-LiteJar.ps1 -JarPath build/e72_lite.jar`; `Test-LiteRuntime.ps1` compile/chạy policy và lifecycle tests bằng javac/java hiện có.

- [ ] Viết Test-LiteJar kiểm tra thiếu các class chế độ bỏ (bao gồm inner classes), có GameMidlet/SmsData/DailyHangPolicy và các auto giữ lại; manifest đúng MIDlet/NST-GameID; tài nguyên nền giữ nguyên trừ class bị loại. Chạy trên baseline và xác nhận thất bại do class cũ.
- [ ] Build compile vào `build/lite-classes` được xác minh nằm dưới workspace trước xóa. Tạo JAR mới từ tài nguyên JAR gốc; mọi class root ứng dụng được thay bằng class source mới, chỉ giữ SmsData và phụ thuộc ngoài source được xác minh. Giữ class thư viện trong namespace cần thiết. Thay manifest bằng nội dung đã duyệt; không overlay bytecode/ASM.
- [ ] Chạy build.ps1 source và build-lite.ps1 đến exit 0; chạy Test-LiteJar và Test-V37Manifest trên JAR lite. Runner tests phải có exit code lỗi nếu bất cứ compile/test nào thất bại.
- [ ] Chạy toàn bộ tests có liên quan một lần sau thay đổi cuối; `git diff --check`, rà diff để phát hiện loại nhầm utility. Nếu review phát hiện lỗi, sửa và chạy lại đúng tests bị ảnh hưởng.
- [ ] Nếu emulator chạy được, kiểm tra boot/menu/disconnect; không mở cửa sổ mới trừ khi cần người dùng tương tác. Ghi nhận giới hạn không có tài khoản/server, không tuyên bố test end-to-end đã qua khi chưa chạy.
- [ ] README ghi lệnh build/test, auto giữ/bỏ, nhịp nhẹ, đường dẫn JAR và kịch bản đo cùng cấu hình: idle, VDMQ, Daily/resume, 50 reconnect, treo >=2 giờ; báo CPU/heap/thread theo dữ liệu thực tế hoặc rõ chưa đo.
- [ ] Commit `build: package and verify lightweight client`; bàn giao JAR cùng kết quả kiểm chứng và các hạn chế.

## Tự rà soát kế hoạch

Tasks 1–5 bao phủ phạm vi, phụ thuộc hang, worker, Sender, schedule/render/scan và đóng gói của spec. Năm điều kiện Review Focus có test hoặc kiểm tra artifact tại task sở hữu. Baseline được giữ làm mốc; các bước sau không dùng latest_classes để ghi đè thay đổi. Triển khai trực tiếp trong phiên hiện tại là lựa chọn đề xuất vì NSOT_MOB, Controller và GameScr được nhiều task sửa tuần tự.

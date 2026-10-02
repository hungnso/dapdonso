# Bản nhẹ NSO

Bản này giữ up VDMQ, nhiệm vụ chính 1–70, nhiệm vụ hằng ngày và tà thú. Daily vẫn vào hang theo cấp để ghi nhận hoạt động, thoát bằng reconnect, chạy nhiệm vụ rồi quay lại đúng auto trước đó.

Giữ các tiện ích hồi HP/MP, thức ăn/linh chi, nhặt/lọc/xóa đồ, tăng điểm, nâng đồ, đăng nhập lại, watchdog phục hồi và nhận thưởng. Bán Shinwa vẫn là tiện ích thao tác khi cần.

Đã bỏ auto tàn sát riêng, danh vọng, đánh người/PK/âm/chờ PK, hồi sinh/buff xa, đánh hang riêng, nhảy chờ PK và tool cược. Các helper chiến đấu/đi map dùng cho những luồng giữ lại vẫn tồn tại.

## Build và kiểm tra

Cần JDK có `javac` trong PATH, `MICRO.jar` và `e72_x1.jar` tại thư mục dự án. Chạy từ thư mục dự án:

```powershell
powershell -ExecutionPolicy Bypass -File .\build-lite.ps1
powershell -ExecutionPolicy Bypass -File .\tools\Test-LiteJar.ps1
powershell -ExecutionPolicy Bypass -File .\tools\Test-V37Manifest.ps1 -JarPath build\e72_lite.jar
powershell -ExecutionPolicy Bypass -File .\tools\Test-LiteRuntime.ps1 -ClassesDir build\lite-classes
powershell -ExecutionPolicy Bypass -File .\tools\Test-LiteBoot.ps1
```

Output: `build/e72_lite.jar`. Script compile source hiện tại và đóng gói tài nguyên từ JAR nền; không lấy lại class auto cũ, không overlay `latest_classes`, không ASM patch. Manifest giữ ID tương thích QLTK. `SmsData.class` vẫn lấy từ JAR nền vì API Java ME cục bộ thiếu JSR-120.

Dùng `build-lite.ps1` để tạo bản phát hành nhẹ. Các script build cũ vẫn lưu cho lịch sử và có thể lấy lại class đã bỏ từ JAR nền.

## Tải nền

- Sender ngủ khi không có gói tin hoặc đang chờ key; enqueue/key/disconnect đánh thức worker. Worker cũ không lấy queue của kết nối mới.
- Worker auto chạy tuần tự qua stop/start, không chạy chồng khi route cũ đang kết thúc. Nhấn lại auto đang ở trong stack không tạo vòng resume.
- Lịch Daily/nhận thưởng và quét túi/phụ trợ không khẩn cấp chạy tối đa một lần/giây. Nhịp hồi HP/MP, chết, chiến đấu và logic game được giữ.
- Chế độ `Giam do hoa VPS` mặc định bật: gameplay không có tương tác vẽ tối đa khoảng 5 FPS. Menu/dialog và một giây sau thao tác tay vẽ theo nhịp bình thường. Có thể tắt trong menu để trở lại vẽ đầy đủ.
- Bỏ log lặp trên từng lần đánh/nhặt; giữ log tiến độ, chuyển trạng thái và hồi phục.

## Kết quả và giới hạn kiểm chứng

23 chương trình kiểm tra đã chạy qua, gồm chọn map VDMQ, mapping hang theo cấp, các policy nhiệm vụ/transition/recovery, nâng đồ, FIFO/key/clear/interrupt Sender, 50 vòng reconnect, worker không chạy chồng và stack resume không có vòng, giữ chuỗi khi bấm lại tà thú, tìm quái theo tọa độ có giới hạn và dừng chờ khi bị interrupt. Test nâng đồ cũ có log ngoại lệ đã được bắt khi chưa có dữ liệu RMS; các assertions của test vẫn qua.

MicroEmulator headless tải được MIDlet và tiếp tục chạy trong phép thử khởi động 15 giây. RMS trong phép thử nằm trong bộ nhớ, nên bộ đọc cài đặt cũ ghi log ngoại lệ đã bắt khi chưa có dữ liệu. Phép thử này chưa kiểm tra đăng nhập server, menu trên giao diện thật hoặc luồng game đầu cuối.

Chưa đo mức giảm CPU/heap khi treo game thật. Dung lượng JAR không thay thế số liệu RAM/CPU.

Để đo, dùng cùng JVM/emulator, độ phân giải, số tài khoản và cấu hình auto cho bản đối chiếu và bản nhẹ. Ghi CPU, heap sau GC, số thread và độ dài queue tại các mốc trước/sau:

1. Idle sau khởi động, rồi chạy VDMQ.
2. VDMQ → Daily → tà thú → resume; 1–70 → Daily → resume.
3. Chết, chuyển map và mất kết nối giữa chuỗi; thực hiện nhiều reconnect.
4. Treo tối thiểu hai giờ; kiểm tra heap/thread không tăng liên tục sau nhiều lượt chuyển map/reconnect.

Không dùng số working set của toàn bộ emulator ở bước khởi động làm số RAM của auto trong game.

## Tương thích QLTK

Giữ API `LoginScr.autoLogin(String, String)` cho account bridge. Thiếu API này làm QLTK báo lỗi khởi động Java 2 trước khi mở MIDlet. Build nhẹ kiểm tra hợp đồng bridge trên JAR trước khi thay output. Tên tài khoản được chuẩn hóa như bản cũ; mật khẩu giữ nguyên chữ hoa/thường, và đăng nhập qua bridge không ghi đè thông tin RMS do QLTK chuẩn bị.

Có thể kiểm tra khởi động với Java/emulator đi kèm QLTK bằng các tham số `-JavaPath` và `-EmulatorPath` của `tools/Test-LiteBoot.ps1`. Kiểm tra này dùng dữ liệu riêng, không đăng nhập server.

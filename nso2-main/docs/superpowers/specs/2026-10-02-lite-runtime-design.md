# Thiết kế bản nhẹ trên nhánh vdmq_toiuu

## Mục tiêu và phạm vi đã duyệt

Tối ưu client để treo lâu và chạy nhiều tài khoản với ít CPU/RAM hơn. Giữ up VDMQ, nhiệm vụ hằng ngày, tà thú, nhiệm vụ chính 1–70 và các tiện ích phụ trợ cần thiết. Người dùng đã duyệt phạm vi trong chat ngày 2026-10-02. Thiết kế này áp dụng trực tiếp nhánh hiện tại.

Giữ các phụ trợ hồi HP/MP, thức ăn và linh chi, nhặt/lọc/xóa đồ, tăng điểm, nâng đồ, đăng nhập lại, phục hồi khi chết/kẹt và nhận thưởng hoạt động. Giữ AutoShinwaSale như tiện ích quản lý đồ được kích hoạt thủ công; không mở rộng nó thành một chế độ treo mới.

Bỏ chế độ tàn sát riêng, danh vọng, PK/âm, hồi sinh xa, auto đánh hang, auto đánh người, auto cược và các mục menu/lệnh khởi động chúng. Rà soát Class_af và các class tên rút gọn theo hành vi thực tế trước khi loại bỏ. Các helper chiến đấu, đi map, buff, chọn skill dùng chung cho những luồng giữ lại vẫn cần tồn tại.

## Hướng triển khai

Chọn cắt bỏ code của các chế độ thừa và đóng gói JAR từ source hiện tại. Chỉ ẩn menu vẫn để lại code và các đối tượng khởi tạo; một bộ điều phối mới hoàn toàn làm tăng rủi ro cho luồng đang hoạt động. Giữ bộ điều phối hiện có và thay đổi đúng các điểm phát sinh tải.

## Bộ điều phối và phụ thuộc

- NSOT_MOB chỉ khởi tạo và cho phép chạy các auto thuộc phạm vi giữ lại. Loại cả điểm khởi động qua menu, phím tắt, lệnh chat và lệnh tổ đội của các chế độ bỏ.
- Daily vẫn tạm dừng VDMQ/1–70, chạy các bước con rồi trở về đúng instance trước đó. Watchdog của Daily vẫn chạy ngoài auto thread để xử lý trường hợp đường đi chặn luồng.
- Giữ bước vào hang theo cấp rồi reconnect để ghi nhận hoạt động trong Daily. Tách mapping cấp/map cần dùng ra helper nhỏ trước khi loại AutoHangDong và MenuHangDong; cập nhật hook Controller và reconnect liên quan.
- Giữ CombatSkillPolicy và các helper được nhiệm vụ chính/VDMQ/Daily/tà thú sử dụng. Loại hook AutoDanhVong khỏi Controller/GameScr cùng với panel và nguồn tương ứng.
- Không cho phép stack auto trỏ vào chính instance đang chạy; dừng/khởi động và reconnect phải có vòng đời worker rõ ràng, tránh hai worker cùng cập nhật một auto.

## Giảm tải CPU và cấp phát

- Sender chờ bằng monitor khi hàng đợi trống; enqueue hoặc thay đổi trạng thái kết nối đánh thức worker. Phải giữ nguyên thứ tự gói tin, bước chờ nhận key và thoát sạch khi disconnect. Queue và thao tác gửi được đồng bộ phù hợp; không giữ khóa queue trong lúc I/O mạng.
- Giới hạn kiểm tra lịch Daily và nhận thưởng tối đa một lần mỗi giây; chỉ tạo Calendar và đọc RMS sau khi qua giới hạn. Đồng hồ dùng GMT+7; xử lý đổi ngày/nhân vật và chỉnh giờ hệ thống mà không bỏ lịch.
- Giữ nhịp cập nhật logic game hiện có vì di chuyển, chiến đấu và timeout đang phụ thuộc vào tick. Chế độ nhẹ giảm vẽ khi treo, nhưng menu/dialog/thao tác thủ công phải phản hồi bình thường.
- Quét túi đồ và các phụ trợ ít khẩn cấp theo cooldown phù hợp, không giảm nhịp hồi HP/MP hay xử lý chết. Giữ guard chống gửi lại một yêu cầu trước khi server phản hồi.
- Giảm log lặp trong vòng treo, giữ log chuyển trạng thái, lỗi và hồi phục. Không gọi System.gc định kỳ để thay thế quản lý vòng đời.
- Kiểm tra các tham chiếu giữ map/mob/auto cũ sau chuyển map, dừng auto và reconnect; giải phóng đúng các dữ liệu không còn dùng. ChatTab hiện có giới hạn 50 dòng nên không cần thêm một cơ chế giới hạn trùng lặp.

## Đóng gói

Tạo build-lite.ps1 và output build/e72_lite.jar. Compile source hiện tại; giữ manifest tương thích QLTK và tài nguyên game cần thiết. Khi dùng JAR gốc làm kho tài nguyên, loại các class ứng dụng gốc trước khi chèn class vừa compile, chỉ giữ class ngoài source còn cần thiết như SmsData và phụ thuộc được kiểm tra. Không overlay latest_classes và không chạy các ASM patch cũ.

Kiểm tra nội dung JAR để chắc chắn class chế độ bỏ không quay lại qua JAR gốc. Chỉ loại tài nguyên khi đã xác minh không còn được các luồng giữ lại tham chiếu. Không xóa source_history, bộ build cũ hoặc các bản JAR lịch sử để tránh mất dữ liệu ngoài phạm vi bản nhẹ.

## Kiểm chứng và tiêu chí đạt

1. Compile thành công bản source và bản lite; manifest QLTK đúng, không có class auto đã bỏ trong output lite.
2. Chạy các policy test hiện có cho VDMQ, 1–70, chuyển nhiệm vụ, tà thú, Daily và recovery. Bổ sung kiểm tra hành vi Sender chờ/enqueue/disconnect và vòng đời worker nếu sửa các cơ chế này.
3. Kiểm tra static các điểm vào bị bỏ và mọi tham chiếu class đã loại. Kiểm tra menu bản nhẹ chỉ hiển thị chế độ còn hỗ trợ.
4. Nếu môi trường chạy được emulator, kiểm tra khởi động, tương tác menu và disconnect. Các hành vi server cần tài khoản game: VDMQ → Daily → tà thú → resume; 1–70 → Daily → resume; chết, chuyển map và reconnect giữa chuỗi.
5. Đo CPU, heap và số thread trên cùng emulator/JVM, cùng số tài khoản và cấu hình trước/sau khi có thể chạy thực tế. Theo dõi nhiều reconnect và treo tối thiểu hai giờ; dùng cùng điều kiện để so sánh. Nếu chưa có phiên game thật, báo rõ chưa kiểm chứng treo lâu và không đưa tỷ lệ tiết kiệm giả định.

Kết quả bàn giao gồm code bản nhẹ, JAR build được, hướng dẫn build/test và các giới hạn kiểm chứng thực tế. Dung lượng JAR giảm không được dùng thay cho bằng chứng RAM/CPU giảm.

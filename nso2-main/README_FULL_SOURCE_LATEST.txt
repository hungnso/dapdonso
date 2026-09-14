FULL SOURCE MOD NSO - SNAPSHOT CUOI DOAN CHAT
==============================================

Baseline runtime cuoi cung:
  dist/e72_v22_shared_combat_skill_policy_test.jar

Muc dich goi source nay:
- Luu toan bo source Java editable hien co cua project.
- Luu source history cua cac dot sua lon trong doan chat.
- Luu CHINH XAC cac class runtime cua ban JAR cuoi cung trong latest_classes/.
- Luu bytecode javap cua cac class runtime moi trong bytecode_reference/ de doi chieu.
- Co script build-latest-exact.ps1 de build source + overlay class runtime cuoi, giup giu dung behavior ban test cuoi.

QUAN TRONG VE TINH CHINH XAC
----------------------------
Trong qua trinh test, mot so ban ve sau duoc patch/build truc tiep tu class da compile thay vi moi lan deu export lai file .java.
Vi vay:
1) src/ = source Java editable day du nhat da duoc export trong chat + cac helper source moi da khoi phuc.
2) latest_classes/ = bytecode CHINH XAC cua nhung class da thay doi o baseline runtime cuoi.
3) build-latest-exact.ps1 compile src/ sau do overlay latest_classes/ de JAR output co logic runtime dung baseline cuoi.
4) Neu ban sua mot file Java co ten trung voi class nam trong latest_classes/, build-latest-exact.ps1 se overlay class cu va thay doi Java cua ban se KHONG co hieu luc. Khi dev tiep class do, dung build.ps1 (source-only) hoac xoa class tu latest_classes/ sau khi da merge source moi.

CAC CHUC NANG DA SUA TRONG DOAN CHAT
------------------------------------
1. Auto NV chinh / Auto 1-70
- Jaian: giam giat, vuot dia hinh, thoat nuoc an toan, uu tien bao ve Jaian.
- Death recovery: HP=0 uu tien cao nhat, clear target/move/combat, ve lang co cooldown.
- Farm sau chuoi NV duoc ho tro den Lv70.
- Chon map farm theo level quai thuc, blacklist/fallback map khong phu hop.
- Neu map farm bi khoa: uu tien lam NV chinh de mo map.
- Task 35 buoc muc nuoc: tai su dung water handler.
- Level gate: "Tim vat pham o thac nuoc" farm Lv60 truoc; "Thu thap Oan hon" farm Lv70 truoc.
- Bao cao NPC sau task 32: helper tim NPC/ve lang/hoan thanh.
- Smart home: di chuyen lang/truong bang NPC, uu tien home gan map farm, luu toa do hoi sinh.
- Nam linh chi x2: tu dung; neu het thi tim Goosho mua toi da 5, gioi han boi so o trong hanh trang.
- Cap nhat v49: theo doi effect X2 phia server, chi mua khi effect het; tu bat lai
  "Tu dung" native qua NSOT_MOB; kiem tra so o trong va yen truoc khi mua.
- Dong bang mob khi Auto NV chay.
- Chon skill chien dau dung CombatSkillPolicy dung chung voi Tan Sat.

2. Tan Sat
- Dong bang quai khi bat.
- Full-map trong khu hien tai, di toi cum quai.
- Uu tien skill damage/AOE/multi-target, loai skill buff/bo tro.
- Kiem: dieu chinh hit/sync/reposition de giam miss.
- Policy skill dung chung voi Auto NV.

3. Mob level
- Tach/giu level quai thuc de Auto dung dung level.
- Sua field UI de khong con hien Lv0 neu server da gui level.

4. Vung Dat Ma Quai
- Menu chon map 139-148 + tu chon theo level.
- Quỷ Sơn, Sơn Hải Vực, Đoạn Sơn, Đảo Quỷ, Sinh Tử Kiều,
  Nhân Duyên Lộ, Hoang Trấn, Mài Tâm Lộ, Bát Thụ Hoang, Cửu Mộc Hoang.
- Thu vao VDMQ truc tiep, khong tu chan vi hieu luc nhan/phu.
- Thien Nhan Phu chi fallback khi can.

5. Auto Danh Vong
- Da phat trien den FSM v2.2 nhung trang thai du an: CHUA ON DINH, tam dong o v2.2.
- Server TaskOrder la source of truth, generation/state/recovery/cancel unsupported.
- NV nang cap duoc bo qua/cancel.

6. Auto Daily Coordinator
- Mot coordinator: NV hang ngay -> Ta Thu -> resume Auto 1-70 neu level < 70.
- Co chay ngay + dat gio hang ngay khi client dang mo.
- Extension point cho level >=70.
- Daily: recovery neu chet ve lang, quay truong va "Di lam NV".
- Daily DONE: nhan text "Hom nay con da lam het nhiem vu...".
- Ta Thu: fast warp qua NPC/"Di lam NV" thay vi next tung map.

7. NextMap
- Default 100; migrate setting mac dinh cu 20 -> 100, giu custom value khac.

CAU TRUC QUAN TRONG
-------------------
src/                    Source Java editable.
latest_classes/         Class exact tu JAR runtime cuoi.
bytecode_reference/     javap -p -c cua class runtime cuoi.
source_history/         Source render lich su de merge/doi chieu.
dist/                   JAR runtime cuoi.
build.ps1               Build source-only.
build-latest-exact.ps1  Build source + overlay runtime exact.
run-emulator.ps1        Chay emulator.

BUILD TREN VS CODE / POWERSHELL
-------------------------------
1) Build source-only (dung khi ban muon code Java vua sua co hieu luc):
   powershell -ExecutionPolicy Bypass -File .\build.ps1 -OutputJar "build\e72_source_only.jar"

2) Build baseline exact (giu cac class da patch o runtime cuoi):
   powershell -ExecutionPolicy Bypass -File .\build-latest-exact.ps1 -OutputJar "build\e72_latest_exact.jar"

3) Chay:
   powershell -ExecutionPolicy Bypass -File .\run-emulator.ps1 -GameJar "build\e72_latest_exact.jar"

GHI CHU DEV TIEP
----------------
Neu tiep tuc sua mot class trong latest_classes/:
- merge logic vao src/<Class>.java,
- test build.ps1 source-only,
- khi source da ngang runtime thi xoa <Class>.class khoi latest_classes/.

AutoNvcLinhChiShop.java da duoc merge tu release v49. AutoVungDatMaQuai va
AutoDailyCoordinator cung duoc danh dau source-owned de build-latest-exact.ps1
khong overlay cac class baseline cu len source moi.
Cach nay dan dan dua project ve 100% source Java thuần ma khong mat behavior cua cac ban test cuoi.

UPDATE V33 - DAILY HANG FIRST (2026-09-13)
------------------------------------------
Auto Hang Ngay moi:
  Hang dong theo cap -> thoat bang reconnect an toan -> NV hang ngay -> Ta Thu
  -> pop ve dung auto da bi tam dung (VDMQ / Auto 1-70).

Hang theo level dung mapping goc AutoHangDong:
  Lv30-39 -> Hang 35 (map 91)
  Lv40-49 -> Hang 45 (map 94)
  Lv50-59 -> Hang 55 (map 105)
  Lv60-69 -> Hang 65 (map 114)
  Lv70-89 -> Hang 75 (map 125)
  Lv90+   -> Hang 95 (map 157)
  Lv<30   -> bo qua Hang va chay Daily nhu cu.

V33 KHONG danh quai trong Hang. Khi client xac nhan da vao dung map Hang, bot
cho 2 giay de server ghi nhan, sau do reconnect de thoat Hang. Marker RMS theo
char/ngay ngan viec reconnect xong lai vao Hang lan 2.

Build dung baseline v32 da test:
  powershell -ExecutionPolicy Bypass -File .\build-v33-daily-hang.ps1

V34 DAILY HANG MENU FIX
- Fix server mở menu trung gian khi vào Hang nhưng AutoDailyCoordinator không bấm nút cấp.
- Khi WAIT_HANG_ENTER và menu NPC Kanata (template 0) đang mở:
  + ưu tiên tự chọn "Cấp XX" đúng cấp nhân vật;
  + nếu đang ở menu gốc thì tự chọn "Hang động" rồi chờ menu cấp.
- Giữ nguyên flow v33: Hang -> logout/login thoát -> NV hàng ngày -> Tà Thú -> resume auto trước đó.

V36 AUTO QUIZ ANSWER FIX
- Fix Auto 1-70 bi dung o menu cau hoi cua NPC Tabemono (3 dap an phoi hop phai).
- Khong phu thuoc action-id 88821 vi mot so server tra action id khac.
- Chi kich hoat khi menu co nhieu dap an gom 3 phai; tu chon dap an Kiem + Kunai + Dao.
- Build tren baseline v35, chi thay Menu.class.

UPDATE V37 - FULL SOURCE MERGE (2026-09-14)
-------------------------------------------
Da hop nhat cac thay doi runtime v28-v36 va cac hook can thiet vao src/ de dev
tiep khong phai lay class tu JAR v36. Dung build-v37-full-source.ps1 de build
truc tiep source, khong overlay latest_classes va khong ASM patch cac feature
Shinwa/Daily/weapon-pickup/Item-Xoa/Ta-Thu o tren.

Chi tiet: SOURCE_MERGE_V37.txt
Runtime build tu source: build/e72_v37_full_source_merged.jar

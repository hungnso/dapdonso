# SDD ledger - plan: docs/superpowers/plans/2026-09-23-auto-account-rotation.md

Pre-flight: tasks share AutoDailyCoordinator completion and NSOT_MOB menu interfaces; preserve existing single-character path while adding the rotation parent.
Task 1: Ruling: git commit unavailable because the parent .git index is read-only under the workspace permission; retain changes in working tree and continue with verification.
Task 1: complete (tests: javac AutoAccountEntry/Store/Policy + AutoAccountPolicyTest -> AutoAccountPolicyTest passed)
Task 2: complete (tests: AutoAccountPolicyTest passed; build account_rotation_ui.jar succeeded; known javac AccessDenied cleanup warning only).
Task 3: complete (build account_rotation_login_adapter.jar succeeded; completion listener and LoginScr.autoLogin compiled).
Task 4: Ruling: full build exposed AutoAccountPolicy/AutoAccountRotationPolicy naming mismatch; corrected coordinator calls to AutoAccountRotationPolicy.firstIndex, then account_rotation_core.jar built successfully.
Task 4: complete (ALL PURE TESTS PASSED; e76_auto_account_rotation.jar built; JAR contains account panel/store/coordinator/login adapter).
Task 5: complete (git diff --check clean; artifact inspection passed; build warnings are existing Java 8/locked e72_x1.jar cleanup warnings).
Task 5: final pure suite rerun after smoke test -> FINAL PURE SUITE PASSED.
Final Ruling: account logout keeps NSOT_MOB worker alive and calls Class_cl.ac instead of NSOT_MOB.c, because the rotation coordinator is the worker parent and must tick after login screen returns; cost if wrong is stale auto actions during logout, covered by final build/runtime review.
Final: fixed password case corruption and plaintext RMS writes -> AutoAccountSecretCodecTest RED->GREEN; FINAL REVIEW SUITE PASSED.
Final: fixed old-session overlap and stop cleanup -> e76 build and FINAL REVIEW SUITE PASSED.
Final: Ruling: COMPLETE checkpoint persists until explicit Reset progress, matching spec requirement not to silently rerun on date change; cost if wrong is manual reset required for a new daily run.
Final: minor (deferred): per-slot enabled checkbox and password length >40 remain UI limitations; blank username disables a slot and current game fields use 40-character limits.
Final: fixed login stopping at SelectCharScr -> SelectCharScr.autoSelectDefault() selects the first available character before coordinator waits for GameScr.
Final verification: e77_auto_account_rotation_select_char.jar built; pure policy/security/smoke suite passed; JAR inspection contains SelectCharScr, AutoAccount*, and LoginCredentialPolicy.
Final: fixed account-switch login race by removing Session_ME.e() reconnect-thread startup from rotation logout/login; e78 build and final pure suite passed.
Final: added in-game completion popup `Đã hoàn thành X/Y acc`, counting enabled configured accounts and updating at rotation start and after each completed account; e79 build and tests passed.
Final: moved progress into the persistent GameScr auto-status line via AutoAccountRotationCoordinator.toString(), matching the daily-task UI; e81 build and diff check passed.
Final: made account progress visible while the daily child auto is active by traversing the Auto parent stack and drawing the rotation coordinator status; e82 build and diff check passed.
Final: LoginScr now prefills configured slot 1 from AutoAccountStore on fresh screen initialization; empty/disabled slot 1 leaves fields blank; e83 build and diff check passed.
Final: fixed premature completion by scanning all configured slots with hasPending() and wrapping next-account selection; AutoAccountPolicy/rotation smoke tests passed and e84 build succeeded.
Final: COMPLETE checkpoints with remaining pending accounts now recover automatically; e85 build succeeded.
Final: built consolidated x5 artifact with all current account-rotation, progress UI, login-prefill, and completion-recovery changes; x5_auto_account_rotation.jar and final pure suite passed.
Final: fixed TaskAuto premature daily completion by removing q<=20 as a completion gate; only server-backed completedTasks>=20 can finish; e86 build succeeded.
Final: added wrong-map watchdog after daily-task warp; if still on old map/route dialog after timeout, close dialog and reconnect to retry task; e87 build succeeded.
Final: strengthened TaskAuto wrong-map handling to route directly to TaskOrder.mapId every 1.8s, then reconnect after 8s if it cannot arrive; e88 build succeeded.
Final: added no-target watchdog on task map; if target killId is absent for 8s with no count progress, reconnect and retry instead of zone-hopping; e89 build succeeded.
Final: added automatic coordinator resume when Auto account is enabled, pending accounts remain, GameScr is restored, and the Auto stack was lost during reconnect/logout; e90 build succeeded.

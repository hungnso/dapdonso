# Auto Account Rotation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let each QLTK tab run up to 20 configured accounts sequentially, so 20 tabs can process 400 accounts while preserving the existing single-character Auto NVHN 3x flow.

**Architecture:** Add a per-tab RMS account store and a parent rotation coordinator. The coordinator owns login/logout/checkpoint state and launches the existing `AutoDailyCoordinator` for exactly one logged-in account at a time. A small `AutoAccountPolicy` keeps slot selection and retry rules pure and unit-testable; the UI only edits the store and starts/stops the coordinator.

**Tech Stack:** Java ME/J2ME forms and existing `mResources` RMS helpers; plain Java policy tests compiled with `javac`; existing `build.ps1` JAR packaging.

**Spec:** `docs/superpowers/specs/2026-09-23-auto-account-rotation-design.md`

## Global Constraints

- Each tab owns a private list of at most 20 accounts and never reads another tab's RMS data.
- The current single-character Auto NVHN 3x menu remains available and bypasses rotation.
- Rotation marks an account complete only after both quick reward flows finish.
- Passwords must not be printed to logs or chat popups.
- A failed login is retried three times, then rotation pauses with a visible error.
- Existing mask, food-UI suppression, cave, daily quest, attendance, and activity-reward behavior remains unchanged.

## Review Focus

- Empty or malformed slots: the UI and coordinator must reject them before login; test store validation and slot selection.
- Restart during `RUNNING`: it must resume the same account as `PENDING`; test checkpoint recovery.
- Login failure and retry boundary: exactly three attempts, then pause without advancing; test retry policy.
- Reward completion timing: 20/20 daily progress alone must not advance the account; test the completion callback only after both claims.
- Per-tab isolation: RMS keys must not contain a shared/global account list; test keys include the local tab namespace supplied by the existing RMS context.

### Task 1: Pure account model, persistence contract, and policy

**Files:**
- Create: `src/AutoAccountEntry.java`
- Create: `src/AutoAccountStore.java`
- Create: `src/AutoAccountPolicy.java`
- Create: `tools/AutoAccountPolicyTest.java`

**Interfaces:**
- `AutoAccountEntry(String username, String password, boolean enabled, int status)` with constants `PENDING`, `RUNNING`, `DONE`, `LOGIN_ERROR`.
- `AutoAccountStore.MAX_ACCOUNTS = 20`, `load()`, `save(AutoAccountEntry[] entries)`, `loadCheckpoint()`, `saveCheckpoint(int index, int phase, int retries, String date)`, `clearCheckpoint()`, and `resetStatuses()`.
- `AutoAccountPolicy.nextIndex(AutoAccountEntry[] entries, int currentIndex)`, `shouldRetry(int retries)`, and `recoverStatus(int status)`.

- [ ] **Step 1: Write failing pure tests**

  Add assertions for: maximum 20 entries; empty usernames rejected; next enabled non-`DONE` slot selection; `RUNNING` recovery to `PENDING`; retries allowed for `0`, `1`, `2` but not `3`; no slot returned when all are done.

- [ ] **Step 2: Run the focused test and verify it fails**

  Run:

  ```powershell
  $tmp = Join-Path $env:TEMP 'nso2-account-policy'; Remove-Item -Recurse -Force $tmp -ErrorAction SilentlyContinue; New-Item -ItemType Directory $tmp | Out-Null
  javac -encoding UTF-8 -d $tmp src\AutoAccountEntry.java src\AutoAccountPolicy.java tools\AutoAccountPolicyTest.java
  java -cp $tmp AutoAccountPolicyTest
  ```

  Expected: compilation failure because the new model and policy do not exist yet.

- [ ] **Step 3: Implement the model and policy**

  Keep policy methods free of game classes. Store at most 20 entries under keys prefixed with `AutoAccountRotation_`; encode records with an explicit length-safe format so usernames/passwords containing `|` cannot corrupt later records. Store checkpoint index, phase, retries, and date separately under the same prefix. Never log stored values.

- [ ] **Step 4: Run the focused test and verify it passes**

  Run the same `javac`/`java` command; expected `AutoAccountPolicyTest` PASS.

- [ ] **Step 5: Commit the self-contained persistence/policy task**

  ```powershell
  git add src\AutoAccountEntry.java src\AutoAccountStore.java src\AutoAccountPolicy.java tools\AutoAccountPolicyTest.java
  git commit -m "feat: add per-tab account rotation store and policy"
  ```

### Task 2: Account-management UI

**Files:**
- Create: `src/AutoAccountPanel.java`
- Modify: `src/GameScr.java` (menu construction and command dispatch near the existing NST menu commands)
- Test: `tools/AutoAccountPolicyTest.java` (validation cases remain pure; UI is covered by build verification)

**Interfaces:**
- `AutoAccountPanel.show()` opens the J2ME `Form`.
- `AutoAccountPanel.isRotationEnabled()` and `AutoAccountPanel.startIndex()` expose saved settings to the coordinator.
- Add command id `1100120` for the root menu and `1100121`–`1100126` for add/edit/delete/start/reset actions.

- [ ] **Step 1: Add pure validation tests**

  Extend `AutoAccountPolicyTest` with valid/invalid username checks, start-index bounds `0..19`, and a list containing disabled gaps.

- [ ] **Step 2: Run tests to verify the new cases fail**

  Run the focused test command from Task 1; expected failure for the new validation APIs.

- [ ] **Step 3: Implement the form and menu wiring**

  Render 20 slots with masked password fields, save through `AutoAccountStore`, show status/index, and expose enable/disable, starting slot, and daily reset. Reject empty usernames and attempts to save more than 20 slots; do not echo passwords in popup messages. Use existing `GameCanvas.inputDlg`/`TextField` conventions instead of adding a new UI framework.

- [ ] **Step 4: Run policy tests and build the UI sources**

  Run the focused tests, then run `powershell -ExecutionPolicy Bypass -File .\build.ps1 -OutputJar 'build\account_rotation_ui.jar'`; expected tests PASS and a successful JAR build.

- [ ] **Step 5: Commit the UI task**

  ```powershell
  git add src\AutoAccountPanel.java src\GameScr.java tools\AutoAccountPolicyTest.java
  git commit -m "feat: add in-game account rotation settings"
  ```

### Task 3: Login adapter and completion callback

**Files:**
- Create: `src\AutoNvhn3xCompletionListener.java`
- Modify: `src\LoginScr.java`
- Modify: `src\AutoDailyCoordinator.java`
- Modify: `src\AutoNvhn3xBatchCoordinator.java` (adapt existing compatibility path to the listener without reintroducing account switching from the normal menu)
- Test: `tools\AutoAccountPolicyTest.java` (completion/retry transition assertions)

**Interfaces:**
- `AutoNvhn3xCompletionListener.onNvhn3xCompleted()` is called only after attendance and activity claim states are no longer busy.
- `LoginScr.autoLogin(String username, String password)` fills the existing login fields and invokes the normal private login path.
- `AutoDailyCoordinator(boolean manualRun, boolean quickNvhn3x, AutoNvhn3xCompletionListener listener)` preserves existing constructors and invokes the listener for quick flow completion.

- [ ] **Step 1: Add a failing completion-order test**

  Add a pure state assertion that daily 20/20 is not an account completion event and that the completion event is emitted only after both claim modes report idle.

- [ ] **Step 2: Run the focused test and verify failure**

  Run the Task 1 test command; expected failure because the callback contract is absent.

- [ ] **Step 3: Implement the listener and login adapter**

  Preserve direct single-character behavior when no listener is supplied. Replace the account-specific completion branch with listener notification, then keep the existing stop/restore behavior. `autoLogin` must set `SelectServerScr.uname/pass` and call the same `Service.gI().login` path used by the UI; never print credentials.

- [ ] **Step 4: Run tests and build**

  Run all existing pure tests plus `AutoAccountPolicyTest`, then build `build\account_rotation_login_adapter.jar`; expected PASS and successful packaging.

- [ ] **Step 5: Commit the callback/login task**

  ```powershell
  git add src\AutoNvhn3xCompletionListener.java src\LoginScr.java src\AutoDailyCoordinator.java src\AutoNvhn3xBatchCoordinator.java tools\AutoAccountPolicyTest.java
  git commit -m "feat: expose NVHN completion and automatic login adapter"
  ```

### Task 4: Account rotation coordinator and restart recovery

**Files:**
- Create: `src\AutoAccountRotationCoordinator.java`
- Modify: `src\NSOT_MOB.java`
- Modify: `src\GameScr.java`
- Test: `tools\AutoAccountRotationPolicyTest.java`

**Interfaces:**
- `AutoAccountRotationCoordinator.g()` loads the store/checkpoint.
- `update()` implements phases `LOAD`, `ENSURE_LOGIN`, `RUN_NVHN3X`, `WAIT_NVHN3X`, `MARK_DONE`, `LOGOUT`, `NEXT_ACCOUNT`, and `COMPLETE`.
- `stop()` persists the current checkpoint and restores Auto NVHN 3x settings.
- `NSOT_MOB.startAutoAccountRotation()` starts exactly one coordinator per tab.

- [ ] **Step 1: Write failing coordinator-policy tests**

  Test transitions for: first enabled slot, completion to next slot, final completion to login screen, restart conversion `RUNNING → PENDING`, three failed logins pausing, and disabled/empty gaps.

- [ ] **Step 2: Run tests to verify failure**

  Run:

  ```powershell
  javac -encoding UTF-8 -d $tmp src\AutoAccountEntry.java src\AutoAccountPolicy.java tools\AutoAccountRotationPolicyTest.java
  java -cp $tmp AutoAccountRotationPolicyTest
  ```

  Expected: failure because the coordinator policy is not implemented.

- [ ] **Step 3: Implement the coordinator**

  On `RUN_NVHN3X`, construct the existing single-character coordinator with `this` as listener. On callback, mark the current record `DONE`, persist, close the old session, switch to `LoginScr`, assign the next credentials, and call `LoginScr.autoLogin`. Start the child only after `GameScr` is active and the logged-in username equals the selected record. Treat a `RUNNING` checkpoint as `PENDING` on startup. Never advance on login failure.

- [ ] **Step 4: Wire the menu command and singleton guard**

  Add a menu action that refuses to start when an Auto chain is active, and add `NSOT_MOB.startAutoAccountRotation()` without changing the existing `startAutoNvhn3x()` single-character action.

- [ ] **Step 5: Run policy tests and full build**

  Run all pure tests, then build `build\auto_account_rotation.jar`; inspect the JAR for `AutoAccountPanel`, `AutoAccountStore`, and `AutoAccountRotationCoordinator`.

- [ ] **Step 6: Commit the coordinator task**

  ```powershell
  git add src\AutoAccountRotationCoordinator.java src\NSOT_MOB.java src\GameScr.java tools\AutoAccountRotationPolicyTest.java
  git commit -m "feat: rotate through per-tab account list"
  ```

### Task 5: End-to-end verification and operator handoff

**Files:**
- Modify: `docs\superpowers\specs\2026-09-23-auto-account-rotation-design.md` only if implementation constraints require a clarified sentence.
- Create: `tools\AutoAccountRotationSmokeTest.java`

- [ ] **Step 1: Run every pure test**

  Compile and run `AutoNv130QuickPolicyTest`, `AutoNvhn3xPolicyTest`, `AutoNvhn3xBatchPolicyTest`, `AutoNvhn3xSettingsTest`, `AutoAccountPolicyTest`, and `AutoAccountRotationPolicyTest`.

- [ ] **Step 2: Build a uniquely named release JAR**

  ```powershell
  powershell -ExecutionPolicy Bypass -File .\build.ps1 -OutputJar 'build\e76_auto_account_rotation.jar'
  ```

- [ ] **Step 3: Verify artifact contents and source hygiene**

  Confirm the JAR contains the account store, panel, coordinator, and login adapter. Run `git diff --check`; confirm no password literals or credential values appear in new log strings.

- [ ] **Step 4: Document the 20-tab setup**

  Add a short operator note to the final response: tab 1 receives accounts 1–20, tab 2 receives 21–40, through tab 20 receiving 381–400; QLTK's `New data` is used only for first-time tab creation.

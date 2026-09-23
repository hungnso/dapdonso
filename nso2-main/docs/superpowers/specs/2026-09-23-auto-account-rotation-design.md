# Auto Account Rotation for 20 QLTK Tabs

## Goal

Allow each QLTK tab to process a private list of up to 20 accounts. With 20
tabs, the operator can divide 400 accounts into 20 groups and run the existing
single-character Auto NVHN 3x flow sequentially for each account in a tab.

The feature must not coordinate accounts between tabs. Each tab owns its own
account list and checkpoint.

## Scope

Included:

- Store up to 20 username/password entries per tab.
- Add an in-game account-management menu.
- Run the current Auto NVHN 3x flow for the logged-in account.
- After daily quests, quick attendance, and activity reward complete, logout and
  login the next account in the same tab.
- Resume the current account after restart or network recovery.
- Stop after all configured accounts are complete.

Excluded:

- QLTK integration or changes to the external launcher.
- Cross-tab scheduling, duplicate detection, or shared account pools.
- Reading `accounts.init` from the host filesystem.
- Automatic account creation, password recovery, or captcha handling.

## Account data and persistence

Each tab uses its own RMS namespace. The store contains at most 20 records,
where each record has username, password, enabled flag, and status. Status is
one of `PENDING`, `RUNNING`, `DONE`, or `LOGIN_ERROR`.

The checkpoint stores the list version, current account index, retry count,
phase, and local date. A date change resets `DONE`/`LOGIN_ERROR` statuses to
`PENDING` only when the operator chooses the daily reset action; it must not
silently rerun accounts during the same session.

Passwords are never printed to logs or chat popups. RMS is local persistence,
not strong encryption; the UI must warn the operator about this limitation.

## User interface

Add an `Auto tài khoản` menu with:

- Enable/disable rotation.
- List and edit account slots 1–20.
- Add, edit, delete, and clear an account slot.
- Choose the starting slot.
- Show current slot and status summary.
- Reset today's progress.

The menu must validate non-empty usernames and prevent starting rotation with
an empty enabled slot before the selected range.

## Runtime state machine

Create a parent account-rotation coordinator around the existing
`AutoDailyCoordinator`:

1. `LOAD`: read the per-tab store and checkpoint.
2. `ENSURE_LOGIN`: verify the selected account is logged in; otherwise fill
   the login credentials and initiate the normal login flow.
3. `RUN_NVHN3X`: start the existing single-character Auto NVHN 3x coordinator.
4. `WAIT_NVHN3X`: wait for its completion callback. Completion means the two
   quick reward tabs have finished, not merely that daily quests reached 20/20.
5. `MARK_DONE`: persist `DONE` and clear retry state.
6. `LOGOUT`: stop the child auto, close the old session, and switch to the
   login screen.
7. `NEXT_ACCOUNT`: select the next enabled non-`DONE` slot and continue.
8. `COMPLETE`: stop rotation and remain on the login screen.

The current single-character menu remains available and bypasses this parent
coordinator. Existing mask, food-UI suppression, cave, daily quest, and reward
logic are unchanged.

## Login and failure handling

The coordinator assigns the next record to `SelectServerScr.uname/pass` and
uses the existing login request path. It waits for the normal transition to
`GameScr` before starting Auto NVHN 3x.

Transient network failure keeps the same account and retries. A failed login is
retried up to three times. After the third failure, the account is marked
`LOGIN_ERROR` and rotation pauses with a visible message; it does not silently
skip credentials. The operator can retry or manually choose another start
slot.

On restart, `RUNNING` is treated as `PENDING` for the same account, so an
account cannot be marked complete until the reward flow callback is received.

## Safety and isolation

- Only one rotation coordinator may be active in a tab.
- The coordinator restores the normal Auto NVHN 3x settings after completion or
  explicit stop.
- Account credentials are excluded from diagnostic output.
- A tab never reads or writes another tab's RMS data.

## Verification

Pure tests will cover slot selection, status transitions, retry limits, restart
recovery, and completion after 20 accounts. Integration checks will verify that
the parent starts the existing single-character flow, waits for both reward
claims, logs out only after completion, and stops after the final slot.

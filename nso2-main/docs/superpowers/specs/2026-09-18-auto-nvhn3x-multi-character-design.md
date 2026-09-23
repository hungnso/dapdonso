# Auto NVHN 3x Multi-Character Design

## Goal

Run Auto NVHN 3x for account character slots 0, 1, and 2 in order, persist progress across restarts, and return to the account login screen after 3/3 characters complete.

## Behavior

- The batch starts at slot 0 and skips empty character slots.
- Each character runs the existing Auto NVHN 3x flow unchanged: mask check, cave entry, daily quests, attendance reward, activity reward, then character completion.
- Completion is checkpointed before switching characters.
- A restart resumes the first unfinished slot. Server-side daily progress remains authoritative for already-completed phases.
- Character switching uses the existing select-character screen and `Service.selectCharToPlay` protocol.
- After all three slots are complete, the batch clears its checkpoint and returns to the account login/logout screen.
- Checkpoints are scoped by account username and current day; a new day starts a fresh batch.

## Persistence

Use RMS keys containing account identity and date. Store current slot, completed-slot bitmask, and phase. Writes must be idempotent so a crash during reconnect cannot repeat a completed character.

## Safety

The batch must never select an empty slot, send character-selection requests before the select-character screen is ready, or clear its checkpoint before the server confirms the current character's daily flow is complete.

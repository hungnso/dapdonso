# Auto NVHN 3x Multi-Character Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Run Auto NVHN 3x for character slots 0 → 1 → 2 with restart-safe checkpoints and logout after 3/3.

**Architecture:** Add a small RMS-backed batch state object and a parent Auto coordinator. The parent launches the existing `AutoDailyCoordinator` as a child for one selected character, checkpoints completion, selects the next slot through the existing character-selection protocol, and finally switches to `LoginScr`.

**Tech Stack:** Java ME-compatible Java 8 source, existing `Auto` stack, `mResources` RMS storage, `SelectCharScr`, `Service`, and standalone Java policy tests.

**Spec:** `docs/superpowers/specs/2026-09-18-auto-nvhn3x-multi-character-design.md`

## Global Constraints

- Preserve the existing single-character Auto NVHN 3x behavior.
- Slot order is exactly 0 → 1 → 2.
- Server daily progress is authoritative after reconnect.
- Do not clear progress until a character is confirmed complete.
- Keep source compatible with `javac --release 8`.

### Task 1: Add pure batch-state policy and tests

**Files:**
- Create: `src/AutoNvhn3xBatchPolicy.java`
- Create: `tools/AutoNvhn3xBatchPolicyTest.java`

- [ ] Write failing tests for slot selection, completion mask, day/account key, and resume slot.
- [ ] Run `javac`/`java` and verify the test fails because the policy is absent.
- [ ] Implement pure methods: `nextSlot(int mask, boolean[] occupied)`, `markDone(int mask, int slot)`, `allDone(int mask, boolean[] occupied)`, and `storageKey(String account, String date)`.
- [ ] Run the policy test and verify it passes.

### Task 2: Add RMS checkpoint storage

**Files:**
- Create: `src/AutoNvhn3xBatchState.java`
- Test: `tools/AutoNvhn3xBatchPolicyTest.java`

- [ ] Add load/save/clear methods using `mResources.c`/`mResources.a`.
- [ ] Persist account, date, current slot, completed mask, and phase.
- [ ] Make malformed or stale state reset to an empty batch.
- [ ] Extend the policy test with round-trip-compatible field validation and run it.

### Task 3: Add the parent multi-character coordinator

**Files:**
- Create: `src/AutoNvhn3xBatchCoordinator.java`
- Modify: `src/AutoDailyCoordinator.java`
- Modify: `src/NSOT_MOB.java`

- [ ] Add a failing lifecycle test/policy assertion that a completed child advances to the next occupied slot and 3/3 ends in logout.
- [ ] Implement parent states `LOAD`, `SELECT_SLOT`, `WAIT_LOGIN`, `RUN_DAILY`, `WAIT_CHILD_DONE`, `MARK_DONE`, `LOGOUT`, and `DONE`.
- [ ] Pass a parent callback/reference into quick daily child mode so child completion pops back to the parent instead of stopping the whole stack.
- [ ] Persist before and after every character transition.
- [ ] Select characters with `SelectCharScr.name[slot]` and `Service.selectCharToPlay`; wait until `Char.getMyChar()` and `SelectCharScr.k` reflect the selected slot.
- [ ] Add `NSOT_MOB.startAutoNvhn3xBatch()` and preserve the existing single-character start method.

### Task 4: Wire the menu and logout behavior

**Files:**
- Modify: `src/GameScr.java`
- Modify: `src/LoginScr.java` or existing logout command path only if required by the current screen API.

- [ ] Add a dedicated batch command beside `Auto NVHN 3x`.
- [ ] Route it to `startAutoNvhn3xBatch()`.
- [ ] Use the existing login/logout screen transition, without deleting account credentials or RMS settings.

### Task 5: Build and verify

**Files:**
- Output: `build/e72_auto_nvhn3x_multi_character.jar`

- [ ] Run all new policy tests and the existing `AutoNv130QuickPolicyTest`.
- [ ] Build with `build.ps1` using the explicit output path.
- [ ] Inspect the JAR for `AutoNvhn3xBatchCoordinator.class` and `AutoNvhn3xBatchState.class`.
- [ ] Report any existing Java 8/manifest warnings separately from failures.

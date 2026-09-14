# Auto Upgrade Equipment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let a player select exactly one weapon, clothing, or adornment item and automatically upgrade it through +8 using the existing server protocol and material rules.

**Architecture:** `AutoUpgradeEquipment` is a standalone singleton FSM ticked from `GameScr.update()`. It owns a pinned target descriptor and, only while a request is pending, the existing global upgrade transaction fields. Narrow static wrappers in `AutoNhiemVuChinh` expose existing table/stone/inventory calculations without changing NV12 control flow; GameScr contributes the root-menu command and commands for selection, start, and stop.

**Tech Stack:** Java ME-compatible Java 8 source, existing `MyVector`/`Command1` UI, existing `Service` protocol, PowerShell V37 build script, self-contained Java test harness.

**Spec:** `docs/superpowers/specs/2026-09-14-auto-upgrade-equipment-design.md`

## Global Constraints

- Command ID is exactly `1100100`; new child command IDs must not collide with existing `GameScr` cases.
- `TARGET_UPGRADE` is exactly `8` and is declared once in `AutoUpgradeEquipment`.
- `< +6` sends `upgradeItem(target, stones, false)`; `+6` and `+7` require one type-28 insurance, enough `GameScr.cw[level]` Lượng, and send `upgradeItem(target, stonesAndInsurance, true)`.
- Requirements, costs, stone values, material splitting, inventory operations, and response semantics must use the existing game data/protocol; do not hard-code resource tables or mutate `Item.upgrade` locally.
- Never change AutoNhiemVuChinh/NV12 state transitions, timers, or behavior. Do not refactor unrelated sources.
- A tick may make at most one externally visible inventory, split, upgrade, or re-equip action. `WAIT_RESULT` sends no request and times out after 20 seconds without retry.
- The source tree has no Git metadata; do not include Git commit commands in execution.

---

### Task 1: Write an executable policy and target-identity test harness

**Files:**
- Create: `tools/AutoUpgradeEquipmentTest.java`
- Test: `tools/AutoUpgradeEquipmentTest.java`

**Interfaces:**
- Consumes: `AutoUpgradeEquipment.evaluatePolicyForTest(int upgrade, boolean hasStone, boolean hasMoney, boolean hasLuong, boolean hasInsurance)` and `AutoUpgradeEquipment.TargetDescriptor.forTest(Item item, boolean equipped, int slot)`.
- Produces: a no-framework executable regression harness returning non-zero on any failure.

- [ ] **Step 1: Write the failing test**

```java
public final class AutoUpgradeEquipmentTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new RuntimeException(message);
    }

    public static void main(String[] args) {
        check("NORMAL".equals(AutoUpgradeEquipment.evaluatePolicyForTest(5, true, true, false, false)),
                "+5 must remain normal and ignore careful-only resources");
        check("NO_LUONG".equals(AutoUpgradeEquipment.evaluatePolicyForTest(6, true, true, false, true)),
                "+6 must pause when Lượng is insufficient");
        check("NO_INSURANCE".equals(AutoUpgradeEquipment.evaluatePolicyForTest(7, true, true, true, false)),
                "+7 must pause when insurance is absent");
        check("CAREFUL".equals(AutoUpgradeEquipment.evaluatePolicyForTest(6, true, true, true, true)),
                "+6 must use careful mode");
        check("COMPLETE".equals(AutoUpgradeEquipment.evaluatePolicyForTest(8, false, false, false, false)),
                "+8 must complete without another request");
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `javac --release 8 -encoding UTF-8 -cp "MICRO.jar;e72_x1.jar;src" tools\AutoUpgradeEquipmentTest.java`

Expected: FAIL because `AutoUpgradeEquipment` does not exist.

- [ ] **Step 3: Preserve this test unchanged until the production API exists**

The test intentionally verifies policy boundaries independent of live inventory and server state. Do not replace it with direct field assertions.

- [ ] **Step 4: Commit**

No commit: this source snapshot has no `.git` repository.

### Task 2: Expose the existing upgrade primitives without changing NV12 behavior

**Files:**
- Modify: `src/AutoNhiemVuChinh.java:3711-3934, 5158-5175`
- Test: `tools/AutoUpgradeEquipmentTest.java`

**Interfaces:**
- Consumes: existing private `getUpgradeRequirement`, `getUpgradeCost`, `getStoneValue`, `chooseUpgradeStones`, `areUpgradeMaterialsCleared`, `putBackInBag`, and inventory split protocol.
- Produces: `public static` standalone helpers for requirement/cost lookup, separated-stone selection, insurance lookup, eligible-item check, bag/body lookup, descriptor-safe result lookup, material restoration, and a one-action standalone stone splitter.

- [ ] **Step 1: Write the failing test addition**

```java
ItemTemplate weapon = new ItemTemplate((short) 11, (byte) 1, (byte) 0, "Test weapon", "", (byte) 40, (short) 0, (short) 0, false);
Item first = new Item(); first.template = weapon; first.upgrade = 3; first.indexUI = 2;
Item second = new Item(); second.template = weapon; second.upgrade = 3; second.indexUI = 3;
AutoUpgradeEquipment.TargetDescriptor selected = AutoUpgradeEquipment.TargetDescriptor.forTest(first, false, 2);
check(selected.matches(first), "descriptor must match selected item");
check(!selected.matches(second), "descriptor must not match a same-template item in another original slot");
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `javac --release 8 -encoding UTF-8 -cp "MICRO.jar;e72_x1.jar;src" tools\AutoUpgradeEquipmentTest.java`

Expected: FAIL because the descriptor API is still undefined.

- [ ] **Step 3: Add narrow wrappers, not copied logic**

Add named `ForStandalone` wrappers adjacent to the existing wrappers at the bottom of `AutoNhiemVuChinh`. Keep the existing private implementations authoritative. Extract only the reusable portion of `splitStackedUpgradeStone` into a parameterized helper that accepts standalone split state; NV12 continues to call it with its current state fields and task guard unchanged. The standalone branch must use the same UI split command and wait for quantity change before proceeding.

- [ ] **Step 4: Compile the full source**

Run: `powershell -ExecutionPolicy Bypass -File .\build-v37-full-source.ps1 -OutputJar "build\auto-upgrade-task2.jar"`

Expected: successful V37 source build.

- [ ] **Step 5: Commit**

No commit: this source snapshot has no `.git` repository.

### Task 3: Implement the standalone FSM and server-authoritative transaction lifecycle

**Files:**
- Create: `src/AutoUpgradeEquipment.java`
- Test: `tools/AutoUpgradeEquipmentTest.java`

**Interfaces:**
- Consumes: Task 2 helpers, `Char.getMyChar()`, `GameScr.itemUpGrade`, `GameScr.arrItemUpGrade`, `GameScr.cw`, and `Service.gI()`.
- Produces: `AutoUpgradeEquipment.gI()`, `tick()`, `showMenu()`, `selectTarget(Item, boolean, int)`, `start()`, `cancel()`, `isActive()`, `getStatusText()`, and test-only pure policy/descriptor APIs.

- [ ] **Step 1: Write the failing FSM test additions**

```java
check("NO_STONE".equals(AutoUpgradeEquipment.evaluatePolicyForTest(5, false, true, false, false)),
        "missing stones must pause before a normal request");
check("NO_MONEY".equals(AutoUpgradeEquipment.evaluatePolicyForTest(5, true, false, false, false)),
        "missing money must pause before a normal request");
check("CAREFUL".equals(AutoUpgradeEquipment.evaluatePolicyForTest(7, true, true, true, true)),
        "+7 must remain careful");
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `javac --release 8 -encoding UTF-8 -cp "MICRO.jar;e72_x1.jar;src" tools\AutoUpgradeEquipmentTest.java`

Expected: FAIL because the production API and descriptor implementation are absent.

- [ ] **Step 3: Implement the minimal FSM**

Implement one enum state and fields for pinned descriptor, original equipped body slot, current target reference, pending stones, pending insurance, old upgrade, one action timestamp, and one-time pause notice. Each `tick()` must return immediately after a single action. In `CHECK_TARGET`, rescan only a descriptor match; in `UNEQUIP_TARGET`, require a free bag slot then call `itemBodyToBag`. In material preparation, calculate requirement and cost fresh from the current item, split only if needed, choose type-26 stones by total `cn` value, and append exactly one type-28 insurance only for levels 6 and 7. Move material references out of the local bag only immediately before `upgradeItem`.

On `SEND_UPGRADE`, set `GameScr.itemUpGrade` and `GameScr.arrItemUpGrade`, save `oldUpgrade`, call the correct careful flag once, then immediately enter `WAIT_RESULT`. In `WAIT_RESULT`, perform no request; wait until material entries are cleared by Controller message 21, then enter `VERIFY_RESULT`. In verification, read the server-updated level, clear only owned pending state, rescan the descriptor/result, and return to fresh checks on equal/changed below-goal result. At `>= TARGET_UPGRADE`, enter re-equip or complete. Never increment the local upgrade field.

`cancel()` must defer cleanup while waiting, then restore locally held references at the next safe tick; it must never send a replacement request. Timeout must use 20,000 ms, pause with the timeout reason, and not retry.

- [ ] **Step 4: Run the test to verify it passes**

Run: `javac --release 8 -encoding UTF-8 -cp "MICRO.jar;e72_x1.jar;src" tools\AutoUpgradeEquipmentTest.java; if ($LASTEXITCODE -eq 0) { java -cp "MICRO.jar;e72_x1.jar;src;tools" AutoUpgradeEquipmentTest }`

Expected: PASS with no output.

- [ ] **Step 5: Compile the full source**

Run: `powershell -ExecutionPolicy Bypass -File .\build-v37-full-source.ps1 -OutputJar "build\auto-upgrade-task3.jar"`

Expected: successful V37 source build.

- [ ] **Step 6: Commit**

No commit: this source snapshot has no `.git` repository.

### Task 4: Integrate target/status menus and the frame tick

**Files:**
- Modify: `src/GameScr.java:1260-1327, 3898+, 14279-14285, 14995-15025`
- Modify: `src/AutoUpgradeEquipment.java`
- Test: `tools/AutoUpgradeEquipmentTest.java`

**Interfaces:**
- Consumes: `AutoUpgradeEquipment.showMenu()` and command IDs owned by the new module.
- Produces: root command `1100100`, item target choices containing the actual selected item plus location/slot metadata, confirmation/start/cancel commands, and a once-per-frame `AutoUpgradeEquipment.gI().tick()` call.

- [ ] **Step 1: Write the failing integration assertion**

```java
check(AutoUpgradeEquipment.MENU_ROOT == 1100100,
        "root menu command must remain the documented non-conflicting ID");
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `javac --release 8 -encoding UTF-8 -cp "MICRO.jar;e72_x1.jar;src" tools\AutoUpgradeEquipmentTest.java`

Expected: FAIL because `MENU_ROOT` has not been exposed yet.

- [ ] **Step 3: Add minimal GameScr integration**

Append `new Command1("Auto đập đồ", AutoUpgradeEquipment.MENU_ROOT)` to `bi()`. Add `case 1100100` and module-owned child cases to the existing command switch; pass `Item` plus location metadata through `Command1` data rather than reading `GameScr.indexSelect`. Call the singleton tick once near the beginning of `GameScr.update()` after existing lightweight ticks. Do not alter AUTO NST command `110021`, `bg()`, or its command flow.

- [ ] **Step 4: Run the harness to verify it passes**

Run: `javac --release 8 -encoding UTF-8 -cp "MICRO.jar;e72_x1.jar;src" tools\AutoUpgradeEquipmentTest.java; if ($LASTEXITCODE -eq 0) { java -cp "MICRO.jar;e72_x1.jar;src;tools" AutoUpgradeEquipmentTest }`

Expected: PASS with no output.

- [ ] **Step 5: Build and inspect command wiring**

Run: `powershell -ExecutionPolicy Bypass -File .\build-v37-full-source.ps1 -OutputJar "build\e72_auto_upgrade_equipment.jar"`

Run: `rg -n "1100100|Auto đập đồ|AutoUpgradeEquipment" src\GameScr.java src\AutoUpgradeEquipment.java`

Expected: build succeeds; root command, dispatcher, tick, and FSM references are all present.

- [ ] **Step 6: Commit**

No commit: this source snapshot has no `.git` repository.

### Task 5: Verify behavior and regression boundaries

**Files:**
- Modify only if verification exposes a defect: `src/AutoUpgradeEquipment.java`, `src/AutoNhiemVuChinh.java`, or `src/GameScr.java`
- Test: `tools/AutoUpgradeEquipmentTest.java`

**Interfaces:**
- Consumes: completed build and the requested 17-case acceptance list.
- Produces: a verified V37 JAR and explicit manual-check results.

- [ ] **Step 1: Run source build and harness**

Run: `powershell -ExecutionPolicy Bypass -File .\build-v37-full-source.ps1 -OutputJar "build\e72_auto_upgrade_equipment.jar"`

Run: `javac --release 8 -encoding UTF-8 -cp "MICRO.jar;e72_x1.jar;build\v37-full-source-classes" tools\AutoUpgradeEquipmentTest.java; if ($LASTEXITCODE -eq 0) { java -cp "MICRO.jar;e72_x1.jar;build\v37-full-source-classes;tools" AutoUpgradeEquipmentTest }`

Expected: both commands succeed.

- [ ] **Step 2: Run static regression checks**

Run: `rg -n "upgradeItem\(.*false|upgradeItem\(.*true|item\.upgrade\+\+|110021|1100100" src\AutoUpgradeEquipment.java src\GameScr.java src\AutoNhiemVuChinh.java`

Expected: no `item.upgrade++`; level 6/7 send path is only `true`; AUTO NST `110021` remains unchanged.

- [ ] **Step 3: Manually exercise the acceptance scenarios in emulator**

Run: `powershell -ExecutionPolicy Bypass -File .\run-emulator.ps1 -GameJar "build\e72_auto_upgrade_equipment.jar"`

Expected: validate all seventeen requested cases, recording the state/log for normal +0/+5, careful +6/+7, immediate +8 completion, each missing resource pause, fail/slow/timeout responses, equipped/bag target behavior, duplicate templates, stop, and NV12 non-regression.

- [ ] **Step 4: Commit**

No commit: this source snapshot has no `.git` repository.

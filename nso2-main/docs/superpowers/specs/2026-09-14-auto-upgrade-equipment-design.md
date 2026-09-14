# Auto Upgrade Equipment Design

## Goal

Add a standalone "Auto đập đồ" command that lets the player select one equipped or bag equipment item and safely automate server-authoritative upgrades through +8.

## Scope and compatibility

- Add a new root-menu command with ID `1100100`; it is independent of AUTO NST.
- Support weapon, clothing, and adornment items accepted by the existing upgrade tables.
- Preserve the current AutoNhiemVuChinh/NV12 behavior. Reusable upgrade calculations are exposed only through narrow wrappers; its control flow is not invoked or changed.
- Do not alter the `Service.upgradeItem` protocol.
- `GameScr.itemUpGrade` and `GameScr.arrItemUpGrade` are a single shared upgrade transaction. The standalone auto owns them only while it is active and pauses if another transaction owns them.

## Runtime facts

- Upgrade requests are sent by `Service.gI().upgradeItem(target, materials, careful)`.
- Controller message `21` updates `GameScr.itemUpGrade.upgrade` from the server and clears selected materials. The FSM uses this state as its result signal and never mutates `Item.upgrade` optimistically.
- Item has no reliable per-instance ID. The selected target is pinned using a descriptor containing template ID, initial location and slot, initial upgrade, and an options fingerprint. A rescan only accepts a matching descriptor or the known in-flight result object.

## UI

Selecting "Auto đập đồ" presents either:

1. A target list of eligible equipment in body and bag, labelled with name, current `+level`, and location; or
2. A status menu for an active/pause state, with Stop and Close.

After selecting a target, a confirmation/status dialog shows the target and the fixed +8 goal. Starting initializes the FSM. Stop prevents all future requests, waits without sending if a response is pending, then clears local state and re-equips when safe.

## State machine

`AutoUpgradeEquipment` owns a single explicit state:

`IDLE`, `SELECT_TARGET`, `CHECK_TARGET`, `UNEQUIP_TARGET`, `CHECK_MATERIALS`, `PREPARE_STONES`, `PREPARE_UPGRADE`, `SEND_UPGRADE`, `WAIT_RESULT`, `VERIFY_RESULT`, `REEQUIP_TARGET`, `PAUSED`, `COMPLETE`, `ERROR`.

The update hook invokes one FSM tick. Each tick performs at most one observable action: an inventory request, one stone-split request, one upgrade request, or one re-equip request. `WAIT_RESULT` performs no inventory/material operation and has a 20-second timeout. Timeout pauses, safely restores any local pending references when possible, clears shared state, and logs the reason; it never retries automatically.

## Upgrade policy

`TARGET_UPGRADE` is a single constant set to `8`.

- From +0 through +5, calculate current requirements each attempt and send normal mode: `upgradeItem(target, stones, false)`.
- From +6 through +7, calculate current requirements each attempt, require `Char.getMyChar().luong >= GameScr.cw[target.upgrade]`, select exactly one `template.type == 28` insurance item, and send `upgradeItem(target, stonesAndInsurance, true)`.
- Missing stone value, money (`xu + yen` using existing cost tables), Lượng, insurance, bag capacity, target, or shared-upgrade availability transitions once to `PAUSED` with a descriptive notice and no packet.
- The normal mode is never used at +6 or +7.

## Shared helper boundary

The existing NV12 helpers remain the authority for table selection and stone values:

- requirement: `cq`, `co`, `cp`;
- cost: `cu`, `cs`, `ct`;
- stone value: `cn`, only type `26`;
- stack splitting and the selection of separated stones follow the existing inventory protocol.

New standalone wrappers or a small shared helper will expose only the operations AutoUpgradeEquipment needs: requirement/cost lookup, value-aware stone selection, stack splitting, bag/body target lookup, pending-material restoration, and re-equip. These helpers do not change NV12 states or timing.

## Server result and cleanup

After send, the FSM stores the old level and waits for Controller message `21` to update the in-flight item. It then rescans body/bag, reads the actual server level, and recalculates all inputs before a subsequent attempt. A same-level result is a failure and remains at the same policy step; level at least +8 completes immediately. On complete, error, cancel, or timeout, clear local selections and timestamps plus owned `GameScr.itemUpGrade`/`arrItemUpGrade`. Re-equip only targets that began equipped; bag targets remain in bag.

## Verification

Add focused source-level tests or a deterministic harness for policy selection, descriptor matching, result handling, and pause reasons. Build with `build-v37-full-source.ps1`. Manually verify the seventeen requested scenarios, including no duplicate sends during `WAIT_RESULT`, no careful-policy fallback, duplicate-template safety, re-equip behavior, and NV12 regression.

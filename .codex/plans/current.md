# Slice 27B3 - Installed Layout Force-Stop Restoration Verification

**Status:** planned
**Cycle ID:** `2026-09-08-slice-27b3-installed-layout-restoration`
**Planning basis:** Slice 27B1/27B2 storage/state and Settings transaction UI
were committed at `b68ca19`; post-commit authority sync is being committed
separately.
**Next action:** run one ADB-driven installed-app force-stop/relaunch journey
that saves Simple through the production Settings / Appearance surface and
observes restored Simple Home after relaunch without claiming provider, theme,
icon, effects, or release behavior.

## Selected Behavior

Verify that a saved Simple/Standard layout choice survives a real installed app
force-stop/relaunch through the production `MainActivity -> OxygenAppStateHolder
-> OxygenApp -> Home` path.

## Acceptance Boundary

- From the installed app, select Simple through Settings / Appearance.
- Force-stop and relaunch `com.oxygen.weather/.MainActivity`.
- Observe Home restored in Simple layout (`Now -> Forecast`) before any ready
  Standard Home is claimed.
- Return to Settings / Appearance and observe Simple selected with saved status.
- Repeat or reset to Standard only if needed to leave the emulator in a clean
  local state.

## Evidence and Verification

Use:

```text
.codex/test-artifacts/2026-09-08-slice-27b3-installed-layout-restoration/
```

The prior committed evidence for `b68ca19` is recorded at:

```text
.codex/test-artifacts/2026-09-08-slice-27b-persisted-layout-selection/ledger.md
```

Required command/evidence set for this verification-only slice:

```sh
. scripts/android-env.sh && ./gradlew :app:assembleDebug
scripts/install-debug.sh
adb shell am force-stop com.oxygen.weather
adb shell am start -n com.oxygen.weather/.MainActivity
git diff --check
```

Use the minimum ADB/UIAutomator steps needed to make the saved layout choice
observable. Stop after one bounded platform timeout or one repeated UI-driving
failure and record the exact blocker.

## Closeout and Authority Sync

Slice 27B1/27B2 implementation is committed at `b68ca19`. Do not update README
or specification to claim installed persisted layout restoration until this
27B3 installed force-stop/relaunch evidence passes.

## Out of Scope

- Detailed or Meteorologist layout rendering, controls, aliases, migration, or
  storage.
- Theme, icon, effects, units, provider, alert semantics, location behavior,
  geocoding behavior, Room/cache behavior, release readiness, and MVP claims.
- Production or test code changes unless the installed verification uncovers a
  real defect in the committed 27B path.

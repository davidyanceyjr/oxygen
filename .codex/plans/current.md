# Slice 27B3 - Installed Layout Restoration Verification

**Status:** verified
**Cycle ID:** `2026-09-08-slice-27b3-installed-layout-restoration`
**Planning basis:** Slice 27B1/27B2 layout storage, state, and Settings UI are
committed at `b68ca19`; the post-commit authority sync is complete in
`84e16a8`, included in merge commit `5ec1201`. No 27B3 installed evidence
existed before this run.
**Next action:** close this verified slice in version control when requested;
the next roadmap candidate is Slice 28A1, Paper Theme Rendering Baseline.

**Outcome:** pass. The installed production journey restored Simple as `Now,
Page 1 of 2` and Standard as `Now, Page 1 of 4` after Activity recreation and
force-stop/relaunch. The complete evidence package is retained at
`.codex/test-artifacts/2026-09-08-slice-27b3-installed-layout-restoration/`.

## Selected Behavior

Verify that a saved Simple or Standard layout choice survives both an Android
Activity recreation and an installed-app force-stop/relaunch through the
production `MainActivity -> OxygenAppStateHolder -> OxygenApp -> Home` path.
Verify both restored layouts remain usable at compact dimensions, large font,
and Effects Off.

## Starting-State Contract

- Use one emulator selected by `scripts/start-emulator.sh`. Run
  `scripts/list-avds.sh`, start `oxygen_starter` (or the explicitly selected
  `OXYGEN_AVD_NAME`), record the single serial reported by `adb devices` as
  `OXYGEN_SERIAL`, and use `adb -s "$OXYGEN_SERIAL"` for every later ADB
  command.
- Record device state before changes: `wm size`, `settings get system
  font_scale`, `settings get system accelerometer_rotation`, and `settings get
  system user_rotation`. Configure the device to `360x640`, font scale `1.3`,
  and portrait after recording those values. These are the compact and large-
  font conditions for every captured Home state; Effects Off is selected in
  Oxygen through Settings / Appearance, not seeded through storage or ADB.
- Build once and install once with `adb -s "$OXYGEN_SERIAL" install -r
  app/build/outputs/apk/debug/app-debug.apk`. The `-r` install intentionally
  preserves existing DataStore/Room state; record whether the run began with
  existing selected-location and forecast-cache data. Do not use
  `scripts/install-debug.sh`, because it assembles again and launches the app
  implicitly. Do not clear app data.
- Launch `MainActivity`. If Home is not reachable, use only the production
  first-run location surface to search/select a real location and choose `Use
  now`; do not seed fake weather or bypass the provider/cache path. Continue
  only when a selected location reaches ready Home with `home-page-title`
  showing `Now` and `home-page-position` showing `Page 1 of 4` after Standard
  is established. If ready Home cannot be reached within 30 seconds, capture
  the visible state and hierarchy, record the exact blocker, and stop without
  claiming layout restoration.
- From ready Home, open `home-about-entry` (visible text `Settings`), open
  `settings-destination-appearance` (visible text `Appearance`), select
  `settings-effects-off`, wait for it to be selected, and select
  `settings-layout-standard` until `layout_preference_saved` / visible text
  `Layout saved` is present. Return to Home and capture the ready Standard
  baseline. This establishes the known starting layout without a provider
  refetch claim.

## Acceptance Boundary

Run this matrix in the stated order at the starting-state configuration.

1. In Settings / Appearance, select Simple. Assert
   `settings-layout-simple` is selected, `settings-layout-standard` is not
   selected, `layout_preference_saved` is displayed, and visible text is
   `Layout saved`. Capture the pre-recreation Simple Settings state.
2. With Simple selected, cause an explicit Activity recreation by changing
   the pinned emulator orientation to landscape and back to portrait using
   `accelerometer_rotation=0` and `user_rotation`. After returning to the
   recorded compact portrait configuration, assert ready Home is `Now, Page 1
   of 2` through `home-page-container`/`home-page-title` and
   `home-page-position`. This proves Simple restored after recreation, not just
   after a process restart. Assert `home-page-tab-forecast` is usable, open it,
   assert `home-simple-forecast-hourly` is selected, then select
   `home-simple-forecast-daily` and assert it is selected before returning to
   `Now`. Capture the Home state.
3. Force-stop and relaunch only with
   `adb -s "$OXYGEN_SERIAL" shell am force-stop com.oxygen.weather` and
   `adb -s "$OXYGEN_SERIAL" shell am start -n
   com.oxygen.weather/.MainActivity`. On the first ready Home observed after
   relaunch, assert `Now, Page 1 of 2`; record that no ready `Now, Page 1 of 4`
   appears before it. Capture the first restored Simple Home and then Settings
   / Appearance, asserting Simple selected, `Layout saved`, and Effects Off
   selected. Capture both states.
4. Select Standard and wait for `settings-layout-standard` selected plus
   `layout_preference_saved` / `Layout saved`. Return Home and assert ready
   `Now, Page 1 of 4`; capture the pre-recreation Standard Settings state.
5. Repeat the same explicit orientation recreation and return to compact
   portrait. Assert ready `Now, Page 1 of 4`, with the Standard Home controls
   usable: open and observe `Hourly`, `Daily`, and `Details` through
   `home-page-tab-hourly`, `home-page-tab-daily`, and
   `home-page-tab-details`, then return to `Now`. Capture the restored Standard
   Home state.
6. Force-stop and relaunch using the same pinned commands. Assert the first
   ready Home is `Now, Page 1 of 4`, then open Settings / Appearance and assert
   Standard selected, `Layout saved`, and Effects Off selected. Capture both
   states. This is the required Standard restoration evidence.

For both layouts, a selector failure repeated twice or any readiness wait over
30 seconds ends the journey. Capture the current screenshot and hierarchy,
record the exact selector/state and command result, and classify the run as
blocked; do not retry the same failure or convert loading/error UI into a
restoration pass.

## Evidence and Verification

Use this artifact directory:

```text
.codex/test-artifacts/2026-09-08-slice-27b3-installed-layout-restoration/
```

Save the following files there:

```text
commands.log
ledger.md
device-state-before.txt
device-state-after.txt
00-baseline-standard-home.png
00-baseline-standard-home.xml
01-simple-saved-settings.png
01-simple-saved-settings.xml
02-simple-after-activity-recreation-home.png
02-simple-after-activity-recreation-home.xml
03-simple-after-force-stop-first-home.png
03-simple-after-force-stop-first-home.xml
04-simple-after-force-stop-settings.png
04-simple-after-force-stop-settings.xml
05-standard-saved-settings.png
05-standard-saved-settings.xml
06-standard-after-activity-recreation-home.png
06-standard-after-activity-recreation-home.xml
07-standard-after-force-stop-first-home.png
07-standard-after-force-stop-first-home.xml
08-standard-after-force-stop-settings.png
08-standard-after-force-stop-settings.xml
```

For each screenshot, dump the UI hierarchy from the same state with
`uiautomator dump`, copy it into the matching `.xml`, and append the command,
timestamp, observable selector/text result, and pass/fail result to
`ledger.md`. The ledger must explicitly record `Page 1 of 2` versus `Page 1 of
4`, the first ready page after each relaunch, and the absence of ready Standard
Home before restored Simple. Record the non-clearing `install -r`, selected
serial, configuration changes, and any provider/cache readiness state in
`commands.log`/`ledger.md` without making a provider-success claim.

Required commands, with no duplicate assembly or implicit launch, are:

```sh
. scripts/android-env.sh && ./gradlew :app:assembleDebug
scripts/list-avds.sh
OXYGEN_AVD_NAME=oxygen_starter scripts/start-emulator.sh
adb devices
adb -s "$OXYGEN_SERIAL" wait-for-device
adb -s "$OXYGEN_SERIAL" shell getprop sys.boot_completed
adb -s "$OXYGEN_SERIAL" install -r app/build/outputs/apk/debug/app-debug.apk
adb -s "$OXYGEN_SERIAL" shell am start -n com.oxygen.weather/.MainActivity
. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.LayoutPreferenceDataStoreInstrumentedTest
git diff --check
```

The prior committed evidence for `b68ca19` remains at:

```text
.codex/test-artifacts/2026-09-08-slice-27b-persisted-layout-selection/ledger.md
```

After the installed journey, restore the recorded device settings and leave
the app on Standard Home only after all Standard evidence is captured. Record
the restored state. The applicable focused automated evidence is the existing
single-test `LayoutPreferenceDataStoreInstrumentedTest` Activity-recreation
coverage; it supports the installed result and cannot replace this real-path
journey. Broad Android checks are limited to the assemble command above and
`git diff --check` unless a production defect is discovered.

## Closeout and Authority Sync

- On a pass, retain the complete artifact directory, set this plan to the
  verified state with the exact commands and artifact path, append a concise
  self-contained verified Slice 27B3 entry to `.codex/cycles/history.md`, and
  update the Slice 27B3 status and next-candidate sequencing in
  `.codex/plans/mvp-roadmap.md`. Reconcile the layout-restoration wording in
  `README.md` and the relevant section of
  `docs/OXYGEN_FULL_SPECIFICATION.md` only to the behavior actually evidenced.
- On a failed or platform-blocked run, retain the named artifacts, keep this
  slice below verified/committed status, and record `Outcome: blocked` with
  the exact blocker and artifact path in this plan and the cycle history. Do
  not update roadmap, README, or specification text to claim restoration.
- Neither outcome upgrades provider, theme, icon, effects implementation,
  release-readiness, or MVP status. No authority file is changed by this
  planning revision beyond this active plan; the closeout sync occurs only
  after execution and review of the retained evidence.

## Out of Scope

- New layout types; Detailed or Meteorologist rendering, controls, aliases,
  migration, or storage.
- Theme, icon, high-contrast, effects implementation, units, provider,
  alert semantics, location/geocoding behavior, Room/cache behavior, release
  readiness, and MVP claims.
- Production or test code changes unless the installed verification exposes a
  real defect in the committed 27B path; such a defect becomes a separately
  scoped implementation slice rather than unverified plan evidence.

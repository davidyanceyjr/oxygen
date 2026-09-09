# Slice 29B — High-Contrast Preference UI

**Status:** committed at `441d05d`
**Cycle ID:** `2026-09-09-slice-29b-high-contrast-preference-ui`
**Mode:** bounded persisted accessibility preference, Settings integration, and
installed restoration

**Basis:** Slice 29A committed the non-persisted `ContrastLevel.STANDARD/HIGH`
rendering contract for Oxygen, Paper, and Terminal. Slice 25A committed the
installed Settings / Appearance destination, and the layout/theme preference
paths establish the versioned DataStore and confirmed-write transaction
pattern. This slice makes the existing contrast axis user-reachable and
restorable; it does not redesign high-contrast rendering.

**Next action:** select Gate 30 as a new bounded plan before implementation;
do not claim its broader accessibility matrix until exercised.

## Selected behavior and acceptance boundary

From installed Settings / Appearance, a user can choose Standard or High
contrast. Standard is the conservative default. A choice becomes effective and
selected only after its local write succeeds. Loading, saving, read failure,
write failure, and retry are stated truthfully; a failed write retains the last
confirmed contrast and the failed target for retry.

A confirmed High choice immediately reaches the existing production
`OxygenTheme` contrast input and survives Activity recreation and force-stop /
relaunch. Missing, malformed, unknown, or unsupported-version records resolve
to Standard. A read failure retains the last confirmed value when one exists,
otherwise uses Standard, and remains retryable.

Contrast selection must not change or rewrite theme, layout, effects, units,
location, forecast, alert, cache, or provider state. It must not construct a new
`OxygenAppStateHolder` or issue another weather request. Unmanaged previews and
tests continue to use `OxygenAppearance.contrast`, so existing call sites and
29A rendering fixtures remain source-compatible.

Primary acceptance boundary: the real Settings / Appearance control commits
through production preference/state wiring, renders the confirmed contrast,
and restores it in the installed app after both recreation and process relaunch.
The no-refetch and preference-independence obligations are proved with a
counting repository and explicit state assertions rather than a live provider.

## Implementation contract

1. Add `ContrastPreferenceStorage.kt` in `:app`, following the established
   small-state boundary: `ContrastPreferenceStorage`, an empty implementation,
   `DataStoreContrastPreferenceStorage`, explicit read-result types, and a pure
   versioned codec. Use a contrast-specific DataStore file and keys; encode only
   canonical Standard/High values and do not migrate or alter the theme,
   layout, effects, or unit records.
2. Extend `OxygenAppStateHolder` with defaulted, trailing contrast inputs and a
   managed contrast presentation state. Managed startup is Standard while the
   record loads. Add confirmed, pending, failed-read, failed-write, and
   retained-target retry transitions equivalent to the committed theme
   transaction. Include contrast in both startup load branches and in the
   condition that schedules preference restoration, without adding it to
   forecast identity, cache keys, request keys, or repository construction.
3. During a selection, keep the confirmed contrast effective and selected while
   the target is pending. On success, publish the target as confirmed; on
   failure, clear pending, keep the prior confirmed value, expose the failure,
   and retry the same target. Ignore duplicate, loading, failed-read, and
   concurrent-pending selections. A read retry must preserve a prior
   confirmed choice.
4. Construct `DataStoreContrastPreferenceStorage(applicationContext)` once in
   `MainActivity` and pass it into the remembered holder. Do not add contrast to
   the `remember` keys or otherwise recreate the holder when presentation
   changes.
5. In `OxygenApp`, use managed confirmed contrast when storage is configured;
   otherwise retain `appearance.contrast`. Carry that value in the existing
   effective `OxygenAppearance`, pass it to `OxygenTheme`, and pass the contrast
   presentation state plus selection/retry callbacks to Settings. Do not change
   the 29A theme-role resolver or Home/alert semantics.
6. Extend Settings / Appearance with an effective Contrast value and Standard /
   High controls. Use confirmed-only selected semantics, at least 48dp targets,
   disabled controls while loading/saving or awaiting retry, and distinct
   loading/saving/load-failure/save-failure/saved copy. The section must remain
   scroll-reachable and non-overlapping at 360x640 dp and font scale 1.3 with
   Effects Off.
7. Append new defaulted parameters/properties rather than reordering existing
   constructors or Composable parameters. Preserve existing Settings navigation
   and Back behavior and all unmanaged preview/test behavior.

## Intended production and test files

- `app/src/main/kotlin/com/oxygen/weather/app/ContrastPreferenceStorage.kt`
- `app/src/main/kotlin/com/oxygen/weather/MainActivity.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/OxygenAppStateHolder.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/OxygenApp.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/settings/SettingsScreen.kt`
- `app/src/test/kotlin/com/oxygen/weather/app/ContrastPreferenceStorageTest.kt`
- `app/src/test/kotlin/com/oxygen/weather/app/ContrastPreferenceStateHolderTest.kt`
- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/settings/ContrastPreferenceUiTest.kt`
- `app/src/androidTest/kotlin/com/oxygen/weather/app/ContrastPreferenceDataStoreInstrumentedTest.kt`
- existing `HomeDashboardUiTest.kt` only to rerun the committed 29A
  recomposition/no-refetch regression; change it only if the managed production
  path cannot be exercised meaningfully in the new focused UI test.

Do not change `:core`, provider clients, repositories, Room schemas, manifests,
permissions, dependencies, theme IDs, `OxygenTheme.kt`, or the 29A Home/alert
role resolution unless discovery proves a direct contract conflict. Stop and
re-plan rather than widening 29B if such a conflict appears.

## Tests and focused evidence

1. Treat the committed 28B2 preference tests and 29A rendering tests as the
   behavioral baseline; do not rerun them before edits because relevant code and
   environment have not changed. Before the Settings UI edit, capture one
   current installed Standard-contrast Appearance screenshot and UI hierarchy,
   recording commit, AVD/serial, physical and test viewport size, font scale,
   and Effects state.
2. Add JVM codec tests for stable Standard/High encoding and rejection of
   missing, partial, malformed, noncanonical, and unsupported-version records.
   Add state-holder tests for managed startup/default/restore, confirmed-only
   pending selection, duplicate/concurrent input rejection, read failure and
   retry with/without a prior confirmation, write failure and same-target retry,
   and preservation of theme/layout/effects plus forecast data and request
   count. Record the genuine pre-production red failure, then the focused green
   result.
3. Add two compact, font-scale-1.3 Compose cases through `OxygenApp` and the real
   `SettingsScreen`: one covers Standard/High selected semantics, 48dp targets,
   pending confirmation, immediate post-write visual application, retained
   Paper + Simple + Effects Off state, Back, and no refetch; the other covers
   disabled loading/failure states, truthful copy, retained confirmed selection,
   and read/write retry.
4. Add one production-DataStore connected case that selects High through the
   Appearance control, reads the stored record, creates a new storage and
   `OxygenAppStateHolder`, and proves High restores. Name this evidence
   state-holder/storage recreation, not Activity or process restart.
5. Rerun the existing 29A
   `highContrastRecompositionPreservesAppearanceAndRequestCount` case after the
   production wiring changes. Retain the new Settings Standard/High screenshots
   and semantics/hierarchy used for visual review under the cycle artifact
   directory. Do not rerun the full Home or Settings connected classes.
6. Install the changed APK once. Without seeding a location, provider result, or
   sample weather, select Paper, Simple, Effects Off, and High through the real
   Settings UI and verify the effective summary and High selection. Recreate the
   Activity once, navigate back to Appearance, and confirm restoration; then
   force-stop/relaunch once, navigate back again, and repeat the confirmation.
   Confirm all four saved choices and the high-contrast rendering remain
   effective, restore any emulator rotation setting changed by the exercise,
   and retain screenshots plus UI hierarchy. This installed journey is the only
   basis for Activity/process-restoration claims.

Artifacts and the short command/result/rerun ledger belong under:

`.codex/test-artifacts/2026-09-09-slice-29b-high-contrast-preference-ui/`

## Verification budget and planned commands

Budget: one emulator session, one pre-edit visual baseline, one JVM red/green
cycle, four focused connected cases, one installed recreation/relaunch journey,
and one broad pass. Do not rerun a pass unless production code, its test input,
or the relevant environment changed. Stop after one bounded platform timeout
and record the remaining gap.

```sh
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest \
  --tests 'com.oxygen.weather.app.ContrastPreferenceStorageTest' \
  --tests 'com.oxygen.weather.app.ContrastPreferenceStateHolderTest'
. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.settings.ContrastPreferenceUiTest#selectionCommitsConfirmedContrastAndPreservesAppearanceWithoutRefetch,com.oxygen.weather.app.ui.settings.ContrastPreferenceUiTest#readAndWriteFailuresRetainConfirmedContrastAndRetry,com.oxygen.weather.app.ContrastPreferenceDataStoreInstrumentedTest#highContrastSurvivesStorageAndStateHolderRecreation,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#highContrastRecompositionPreservesAppearanceAndRequestCount'
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
scripts/install-debug.sh
git diff --check
```

Exact new method names may change to match implemented fixtures, but the final
connected command must name individual methods and stay within the four-case
budget. `scripts/list-avds.sh` and one `scripts/start-emulator.sh` session are
setup, not additional acceptance evidence.

## Required commit and authoritative document sync

After focused, installed, broad, and review evidence passes:

1. Commit only the implementation and tests as the verified 29B behavior.
2. Perform the required post-commit authority sync without changing app
   behavior:
   - `README.md`: move high contrast from “implemented but not active” into the
     installed-app list and state only the restoration/independence actually
     proved;
   - `docs/OXYGEN_FULL_SPECIFICATION.md`: update sections 23, 51, and 53 from
     non-persisted/later-work wording to the verified installed preference;
     leave sections 20 and 37 unchanged unless implementation exposes a real
     contract conflict;
   - `.codex/plans/mvp-roadmap.md`: mark 29B committed with its implementation
     commit/evidence and make Gate 30 the next specified candidate without
     planning or claiming it complete;
   - `.codex/cycles/history.md`: append one concise, self-contained 29B entry
     with result, focused/broad/installed evidence, artifact path, blockers or
     skips, boundaries, and implementation commit; do not rewrite/archive the
     ledger for an ordinary append;
   - this plan: change status only as evidence advances, record exact commands
     actually run, the implementation commit, installed environment, artifacts,
     skips/blockers, and the next action.
3. Run `git diff --check`, review the documentation diff against the commit,
   then commit the authority sync separately. Do not claim 29B committed until
   the implementation commit exists or Gate 30 planned/verified until separately
   selected and exercised.

## Actual evidence

- Baseline: committed installed APK before UI edits, `oxygen_starter` /
  `emulator-5554`, physical 1080x2400, rotation 0, font scale 1.0; Standard
  Appearance screenshot and hierarchy are retained in the cycle artifact.
- Red JVM phase: the planned contrast storage/state command failed before
  production edits with unresolved contrast codec/state symbols.
- Focused green JVM phase: `:app:testDebugUnitTest --tests
  'com.oxygen.weather.app.ContrastPreferenceStorageTest' --tests
  'com.oxygen.weather.app.ContrastPreferenceStateHolderTest'` passed.
- Focused connected evidence: the corrected four-case filter passed the
  selection `ContrastPreferenceUiTest` case, the named
  `ContrastPreferenceDataStoreInstrumentedTest` case, and
  `HomeDashboardUiTest#highContrastRecompositionPreservesAppearanceAndRequestCount`
  on `oxygen_starter` / `emulator-5554`; the corrected failure/retry
  `ContrastPreferenceUiTest` case passed in a subsequent named single-method
  rerun. A preceding run exposed scroll-clipped assertions.
- Broad evidence passed: `:app:compileDebugKotlin`, `:app:testDebugUnitTest
  :core:testDebugUnitTest`, `:app:assembleDebug`, and `git diff --check`.
- Installed evidence: changed APK installed once after broad checks. At font
  scale 1.3, the real Appearance UI selected Paper, Simple, Effects Off, and
  High; screenshots/hierarchies are retained for initial, final, Activity
  recreation, and force-stop/relaunch states. All four choices restored.
- Implementation commits: `441d05d` (`Implement persisted high contrast
  preference`) and `86e696c` (retained-confirmed read-failure coverage).
- Remaining verification not run: TalkBack service traversal, RTL, automatic
  system contrast detection, and release/MVP gates are out of scope.

## Out of scope

- Any new high-contrast palette, role, Home, alert, or theme rendering contract
  already completed by Slice 29A.
- Automatic Android high-contrast/accessibility detection or changing the
  stored choice from system settings.
- A fourth theme, Detailed/Meteorologist layouts, Full effects, icon-pack
  settings, custom units, or unrelated Settings redesign.
- Provider/domain/repository/cache/location/alert/notification behavior,
  preference-schema consolidation, new dependencies, or migrations of existing
  preference records.
- TalkBack service traversal, RTL, the broad accessibility matrix, release
  readiness, MVP completion, or any Gate 30 claim.

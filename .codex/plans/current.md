# Slice 28B2 — Persisted Theme Settings UI

**Status:** committed at `2c88b9c`
**Cycle ID:** `2026-09-09-slice-28b2-persisted-theme-settings-ui`
**Mode:** bounded settings integration and installed restoration verification

**Basis:** Slice 28B1's storage/state implementation is committed in
`708172f` (merged by `82cf281`): it already accepts only `OXYGEN`, `PAPER`,
and `TERMINAL`, preserves the confirmed theme during a failed/pending write,
and exposes retryable read/write state. Paper and Terminal rendering baselines
are committed in `06c987b` and `80dd961`. The remaining production gap is that
`MainActivity` does not configure theme storage and Appearance exposes only a
read-only theme summary. The roadmap/history summaries that still call 28B1
planned are documentation drift to correct during this slice's post-commit sync.

**Next action:** select the next bounded candidate, Slice 29A, through the
roadmap; do not treat it as planned until a new active plan selects it.

## Selected behavior and acceptance boundary

From Settings > Appearance, a user can select Oxygen, Paper, or Terminal. The
saved choice becomes effective only after the existing DataStore write succeeds;
the Appearance summary and the rendered app then use that confirmed theme.
The choice survives Activity recreation and force-stop/relaunch. Selecting a
theme does not initiate a forecast, alert, geocoding, cache, location, unit,
layout, effects, or navigation change.

The primary acceptance boundary is one installed `oxygen_starter` emulator at
360x640, portrait, font scale 1.3, with Effects Off: select Paper and Terminal
through the real Appearance UI, observe each rendered Home/Settings result,
recreate the Activity, then force-stop/relaunch and observe the last confirmed
theme before any user selection. Use an already-selected production location
when available; do not seed sample weather, a provider result, or app-private
theme data for this journey.

## Implementation contract

- Keep `ThemePreferenceStorage`, its dedicated `oxygen_theme_preferences`
  DataStore name, version `1`, canonical values, and 28B1 state transitions
  unchanged. This slice adds no migration, preference abstraction, theme, or
  rendering work.
- In `MainActivity`, create one application-context
  `DataStoreThemePreferenceStorage` in the existing Compose `remember` setup
  and pass it to the existing `OxygenAppStateHolder` parameter. Do not alter
  repository, cache, location, permissions, or executor construction.
- In `OxygenApp`, pass `presentationState.themePreference`,
  `onThemeSelected`, and `onThemePreferenceRetry` to Settings. Continue to use
  the existing confirmed/effective `theme` for both `OxygenTheme` and
  `OxygenAppearance`; preserve unmanaged `appearance.theme` injection for
  previews and fixtures.
- In `SettingsScreen`'s existing Appearance section, show a "Theme" control
  only when the preference is managed. Use three full-width, vertically ordered
  `FilterChip` choices—Oxygen, Paper, Terminal—each with a 48dp minimum target.
  This avoids cramped three-column labels at the compact large-font boundary.
  Use the confirmed effective `themeId` for selected semantics, not a pending
  target, so the UI never presents an uncommitted theme as active.
- While a managed read is loading, a write is pending, or a read/write failure
  awaits retry, disable all theme choices. Show distinct, truthful loading,
  "Saving <theme>…", read-failure, write-failure, retry, and saved states.
  The retry control invokes the existing state-holder retry event; it must
  replay the retained failed write target, not silently substitute Oxygen.
  Keep the last confirmed theme selected and rendered after failure.
- Add stable test tags `settings-theme-oxygen`, `settings-theme-paper`,
  `settings-theme-terminal`, `theme_preference_loading`,
  `theme_preference_error`, `theme_preference_retry`, and
  `theme_preference_saved`. Preserve selected/disabled semantics and source
  order. Do not add production-only test affordances or modify `OxygenTheme`.

## Intended files

- `app/src/main/kotlin/com/oxygen/weather/MainActivity.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/OxygenApp.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/settings/SettingsScreen.kt`
- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/settings/ThemePreferenceUiTest.kt`
  — new compact Appearance transaction/no-refetch and failure/retry cases.
- `app/src/androidTest/kotlin/com/oxygen/weather/app/ThemePreferenceDataStoreInstrumentedTest.kt`
  — revise its direct state-holder selection to use the Appearance control and
  retain production-DataStore recreation coverage.

Existing `ThemePreferenceStorageTest` and `ThemePreferenceStateHolderTest`
remain the 28B1 codec/transaction regression boundary; no new JVM state model
is warranted unless implementation exposes a real regression.

## Focused tests and evidence

1. Add a red connected UI assertion for the missing theme controls. After the
   implementation, `ThemePreferenceUiTest` must use a controlled executor and
   recording repository to prove all three choices are reachable, selected
   semantics and Appearance summary change only after successful commits, a
   pending write disables every choice, and Paper → Terminal → Oxygen produces
   no additional repository request or presentation-state change apart from
   confirmed theme.
2. Its failure/retry case must make the theme store fail a write, verify that
   the confirmed theme and Home/Settings return state remain intact with the
   retry control visible, then make the same retained target succeed exactly
   once. It must also measure the three choice and retry targets at least 48dp
   and verify their vertical ordering at 360x640/font-scale-1.3.
3. Update
   `ThemePreferenceDataStoreInstrumentedTest#appearanceSelectionSurvivesProductionDataStoreAndAppRecreation`
   to select Paper through the real Appearance UI backed by the production
   DataStore adapter, replace the holder/app composition, and verify Paper's
   selected summary/control after recreation. Restore the test-owned record in
   cleanup. This is storage/app recreation evidence, not an installed Activity
   or force-stop claim.
4. Preserve the existing focused JVM coverage for malformed/unsupported
   storage records, loading, duplicate/pending selection, read/write retry, and
   independence from layout/effects/forecast state. Do not duplicate those
   state-machine cases in UI tests.
5. Capture an installed baseline before the UI edit and final PNG plus UI
   hierarchy evidence for Paper and Terminal selection, Activity recreation,
   and force-stop/relaunch. Record commands, serial, AVD, size, font scale,
   selected location provenance, and any bounded platform failure under
   `.codex/test-artifacts/2026-09-09-slice-28b2-persisted-theme-settings-ui/`.

## Verification budget and sequence

Budget: one baseline capture, one focused JVM baseline, one red connected run,
one focused green run of the three named connected cases, one installed journey,
and one broad pass. Use one emulator session; do not rerun a passing command
unless relevant code, test input, or environment changes.

1. Confirm a ready ADB device with `scripts/list-avds.sh`; start one
   `oxygen_starter` session only if needed. Pin its serial, capture the current
   Appearance baseline, and record `wm size` and `font_scale`.
2. Run the existing theme JVM baseline. Add the red UI test and record its
   actual missing-control result before production edits.
3. Implement only the wiring and Appearance controls above. Run focused JVM
   green, then the two focused connected classes (three relevant cases total).
4. Assemble/install once for the changed APK. On the same emulator, set Effects
   Off, select Paper and Terminal from Appearance, capture each Home/Settings
   result, recreate by portrait rotation (`user_rotation` 1 then 0), and
   force-stop/relaunch with `am force-stop` followed by explicit
   `am start -n com.oxygen.weather/.MainActivity`. Restore device rotation
   settings after capture. If a real provider path cannot become ready within a
   bounded attempt, retain truthful Settings/restoration evidence and report
   the exact gap; do not fabricate a ready Home.
5. Run the broad checks once, inspect the diff for source compatibility,
   unchanged DataStore contract, semantic/target behavior, and scope leakage.

Planned commands:

```sh
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest \
  --tests 'com.oxygen.weather.app.ThemePreferenceStorageTest' \
  --tests 'com.oxygen.weather.app.ThemePreferenceStateHolderTest'
. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.settings.ThemePreferenceUiTest'
. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ThemePreferenceDataStoreInstrumentedTest'
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

## Actual evidence

- Baseline: `scripts/list-avds.sh`; one `oxygen_starter` emulator on
  `emulator-5554`, physical 1080x2400, font scale 1.3. Baseline Appearance PNG
  and hierarchy are under
  `.codex/test-artifacts/2026-09-09-slice-28b2-persisted-theme-settings-ui/`.
- Focused JVM baseline and green rerun passed the two named theme tests.
- Focused connected green evidence passed `ThemePreferenceUiTest` (2 cases)
  and `ThemePreferenceDataStoreInstrumentedTest` (1 case) on the same AVD.
  Initial attempts exposed the missing storage import, off-screen saved-state
  assertion, and an assertion made before the controlled write drained; each
  rerun followed the relevant code/test-input change.
- Installed evidence: Effects Off; Paper and Terminal rendered and were
  selected in Appearance. Paper survived relaunch before another selection;
  Terminal survived rotation (`user_rotation` 1 then 0) and force-stop/relaunch
  in Settings. PNG and UI hierarchy evidence plus command logs are in the
  cycle artifact directory. Rotation settings were restored to the baseline.
- Broad checks passed: `:app:compileDebugKotlin`, full app/core debug unit
  tests, `:app:assembleDebug`, and `git diff --check`.
- Bounded gap: the installed Activity returned to first-run location entry
  after rotation/relaunch, so no post-relaunch Home forecast screenshot is
  claimed. No location, provider result, sample data, or app-private theme
  data was seeded.
- The planned red connected run was not separately recorded because the test
  was added and compiled with the production wiring in the same edit; the
  focused green boundary is retained instead.

## Required post-commit document sync

After verified implementation is committed, update only factual authorities:

- `.codex/plans/mvp-roadmap.md`: mark 28B1 committed at `708172f`/`82cf281`,
  mark 28B2 with its actual commit/evidence, update the Active Slice chain, and
  leave Slice 29A merely the next candidate.
- `.codex/cycles/history.md`: correct the recent-state summary and append one
  concise 28B2 entry with production files, exact focused/broad/manual evidence,
  artifact path, skips/blockers, and commit state.
- `README.md`: move Paper/Terminal from "implemented but not active" to the
  installed-app list only if this journey passes; state the actual selectable
  set and restart restoration, and remove only the now-false "persisted theme"
  item from Not implemented yet. Keep Full effects and icon packs unfinished.
- `docs/OXYGEN_FULL_SPECIFICATION.md`: replace the current claim that Paper and
  Terminal are not selectable/persisted with the verified 28B1 storage and
  28B2 installed-selection/restoration facts. Do not change product
  requirements, provider documentation, or release status.
- This plan: record actual evidence while active; after the commit/doc sync,
  select a later bounded slice only through the roadmap rather than claiming it
  implemented.

## Out of scope

- Theme storage/state-machine redesign, DataStore migration, new dependencies,
  new themes, automatic/system theme, high contrast, icon packs, Full effects,
  or Paper/Terminal/Oxygen visual redesign.
- Changes to forecast values, providers, cache, alerts, locations, permission,
  units, layout/effects behavior, navigation, accessibility meaning, or
  canonical data.
- Release readiness, MVP-complete claims, and any provider/data-source document
  update.

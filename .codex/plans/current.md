# Slice 28B1 - Theme Preference Storage and State

**Status:** planned
**Cycle ID:** `2026-09-09-slice-28b1-theme-preference-storage-state`
**Mode:** bounded persistence/state implementation
**Basis:** Paper (`06c987b`) and Terminal (`80dd961`) are committed,
connected rendering baselines. The roadmap next requires a conservative theme
preference boundary before Settings selection and installed restoration.
**Next action:** run the focused layout/effects preference regression baseline,
then add the failing theme codec and state-holder tests.

## Selected Behavior and Acceptance Boundary

Persist and restore exactly the three verified `OxygenThemeId` values:
`OXYGEN`, `PAPER`, and `TERMINAL`. A missing, malformed, unknown, or
unsupported-version record resolves to Oxygen. A storage exception is distinct
from an unsupported record: it remains observable and retryable while effective
rendering stays on the last confirmed theme, or Oxygen if none was confirmed.

The primary acceptance boundary is `OxygenAppStateHolder` configured with the
new production storage interface: startup restoration, pending/confirmed write
transactions, failures, retry, and theme/layout/effects independence are
observable without changing forecast state or calling a provider. One focused
connected case must exercise the production Preferences DataStore and
`OxygenApp` consumption across storage/state-holder recreation. It is not an
Activity, process-restart, or user-reachable Settings-selection claim.

## Storage Contract

- Add `ThemePreferenceStorage` and `DataStoreThemePreferenceStorage` in `:app`,
  following the established small-preference boundary without refactoring the
  layout, effects, or unit stores.
- Store an integer schema version `1` and a stable lowercase value:
  `oxygen`, `paper`, or `terminal`. Do not persist enum ordinals or derive the
  disk contract from `enum.name`.
- Decode only the supported version and exact canonical values. Missing keys,
  blank or case/whitespace-aliased values, unknown values, and other versions
  return `ThemePreferenceReadResult.NoSupportedChoice`; they do not throw and
  do not alias a future theme to a current theme.
- Encode every currently supported `OxygenThemeId` explicitly. A later enum
  addition must require an intentional codec decision through exhaustive Kotlin
  handling.
- Use a dedicated `oxygen_theme_preferences` DataStore and one atomic edit per
  write. DataStore read/write exceptions must propagate to the state holder so
  failure cannot be reported as a valid Oxygen record.
- Do not add a migration from layout/effects/unit storage: no prior persisted
  theme format exists.

## State and Rendering Contract

- Extend `OxygenAppStateHolder` with optional theme storage and an
  `initialTheme` defaulted to Oxygen. Add new constructor parameters with
  defaults without reordering existing parameters, and add new presentation
  fields with defaults, to preserve current Kotlin call sites and fixtures.
- Model `NotConfigured`, `Loading`, `Loaded`, and `Failed` reads plus
  `confirmed`, `pending`, and `writeError`, consistent with the existing layout
  transaction vocabulary. Keep the last failed selection only for a write
  retry.
- With managed storage, startup and an initial read failure render Oxygen until
  a supported choice is confirmed. A later failed reread preserves the prior
  confirmed/effective theme.
- A selection made through the state-holder event is pending while storage is
  writing. Continue rendering the confirmed theme until the durable write
  succeeds; then atomically confirm and render the new theme. A failed write
  clears pending state, exposes `writeError`, and leaves the confirmed/effective
  theme unchanged. Retry replays that failed write; read-failure retry rereads.
- Ignore duplicate selection while the same choice is confirmed or any theme
  write is pending. Theme events must not mutate layout, effects, units,
  location, visible/return screens, canonical/presented forecast, alerts, or
  repository request counts.
- When theme storage is not configured, preserve current behavior exactly:
  `OxygenApp` continues to honor its injected `OxygenAppearance.theme` for
  previews and tests, and the default remains Oxygen.
- When theme storage is configured, `OxygenApp` uses the state holder's
  effective theme consistently for `OxygenTheme`, the effective
  `OxygenAppearance` passed to Home, and the existing read-only Appearance
  summary. Do not add controls, copy, test tags, or callbacks to
  `SettingsScreen` in this slice.

## Intended Production Files

- `app/src/main/kotlin/com/oxygen/weather/app/ThemePreferenceStorage.kt` — new
  interface, read result, stable codec, and Preferences DataStore adapter.
- `app/src/main/kotlin/com/oxygen/weather/app/OxygenAppStateHolder.kt` — managed
  theme state, startup load, durable selection, and retry transitions.
- `app/src/main/kotlin/com/oxygen/weather/app/OxygenApp.kt` — choose the managed
  effective theme while retaining unmanaged `OxygenAppearance` injection.

No change is planned for `MainActivity.kt`, `SettingsScreen.kt`,
`OxygenTheme.kt`, Gradle files, resources, or provider/core code. If the
implementation requires one of those files, stop and revise the boundary before
editing it.

## Test Plan

Add:

- `app/src/test/kotlin/com/oxygen/weather/app/ThemePreferenceStorageTest.kt`;
- `app/src/test/kotlin/com/oxygen/weather/app/ThemePreferenceStateHolderTest.kt`;
- `app/src/androidTest/kotlin/com/oxygen/weather/app/ThemePreferenceDataStoreInstrumentedTest.kt`.

Focused JVM cases must prove:

1. Oxygen, Paper, and Terminal encode to stable values and round-trip.
2. Missing keys, partial records, wrong versions, blank/unknown values, and
   non-canonical case/whitespace values yield `NoSupportedChoice` and therefore
   the Oxygen state-holder default.
3. Managed startup exposes Loading, then restores each supported theme; an
   unmanaged holder retains its injected initial theme.
4. A pending Paper/Terminal write keeps the confirmed theme effective; success
   changes it only after storage commits, and duplicate/pending input does not
   create extra writes.
5. Read failure and retry are observable; an initial failure uses Oxygen, while
   a failed reread retains the last confirmed theme.
6. Write failure and retry retain the confirmed theme, preserve the failed
   target, and commit it once exactly when retry succeeds.
7. A theme transaction preserves layout/effects/unit/location and the complete
   Home ready/Settings return state, with no additional weather refresh.

Use a controlled executor wherever Loading or pending state must be observed;
do not rely on timing or sleeps.

The single connected case,
`ThemePreferenceDataStoreInstrumentedTest#supportedThemeSurvivesStorageAndStateHolderRecreation`,
must use the production DataStore adapter with test-owned app storage, commit an
alternate theme through the state holder, construct a new storage adapter and
state holder, and observe the restored theme through the existing
`OxygenApp -> Settings / Appearance` summary. Reset the test-owned record during
cleanup. Do not launch `MainActivity`, alter the installed app's preferences,
or describe state-holder recreation as Activity/process restart evidence.

## Implementation Sequence

1. Run the focused existing layout/effects preference tests as a regression
   baseline; record the command and result once.
2. Add the codec and state-holder tests first and retain the expected red result
   for missing theme storage/state behavior.
3. Implement the stable codec and dedicated DataStore adapter without changing
   existing preference formats or dependencies.
4. Add managed/unmanaged theme state and transaction handling to
   `OxygenAppStateHolder`, reusing the existing background-executor and state
   publication pattern. Keep effective state confirmed-only during writes.
5. Update `OxygenApp` to consume managed theme state at all existing theme
   rendering/summary boundaries while preserving unmanaged injected appearance.
6. Run focused JVM green once, then the one named connected case on one pinned
   emulator. Rerun only after a relevant code, test-input, or environment change
   and record the reason.
7. Run broad checks once, inspect the complete diff for source compatibility,
   exact persisted values, truthful failure handling, and scope leakage, then
   mark the slice verified only if every acceptance claim has evidence.

## Evidence and Verification Budget

Artifacts and a short command/result/rerun ledger belong under:

```text
.codex/test-artifacts/2026-09-09-slice-28b1-theme-preference-storage-state/
```

Focused baseline:

```sh
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest \
  --tests '*LayoutPreferenceStorageTest' \
  --tests '*LayoutPreferenceStateHolderTest' \
  --tests '*EffectsPreferenceStorageTest' \
  --tests '*EffectsPreferenceStateHolderTest'
```

Red/focused green:

```sh
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest \
  --tests '*ThemePreferenceStorageTest' \
  --tests '*ThemePreferenceStateHolderTest'
```

Connected production-storage boundary (one case, within the eight-case limit):

```sh
. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ThemePreferenceDataStoreInstrumentedTest#supportedThemeSurvivesStorageAndStateHolderRecreation'
```

Use `scripts/list-avds.sh` and one `scripts/start-emulator.sh` session only if no
ready device exists; pin and record the ADB serial before the connected run.
This state/storage slice requires logs, not visual screenshots.

Broad checks:

```sh
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

Budget: one baseline unit run, one expected red run, one focused green run, one
connected case, and one broad pass. Do not substitute compilation or source
inspection for the connected storage/state observation.

## Required Completion and Document Sync

After verified implementation is committed, perform the required authoritative
doc sync as a separate, factual closure step:

- `.codex/plans/current.md`: record actual focused/connected/broad results,
  artifact paths, blockers/skips, implementation commit, and next action.
- `.codex/plans/mvp-roadmap.md`: mark 28B1 committed with its actual result and
  evidence; select 28B2 only after 28B1 is committed.
- `.codex/cycles/history.md`: update the recent summary and append one concise,
  self-contained 28B1 entry. Ordinary append does not require an archive copy.
- `README.md`: describe the versioned theme storage/state only under
  "Implemented but not active" and keep installed theme choice/restoration in
  "Not implemented yet."
- `docs/OXYGEN_FULL_SPECIFICATION.md`: reconcile the current implementation
  status in the theme/Immediate Tasks text: Paper and Terminal rendering plus
  28B1 storage/state exist, while MainActivity wiring, user selection, and
  installed restart restoration remain 28B2.

No provider contract or data-source disclosure changes are required. Run and
record `git diff --check` for the doc sync; do not claim `committed` until the
implementation commit exists, and do not claim installed persistence from the
connected state-holder recreation test.

## Out of Scope

- `MainActivity` theme-storage wiring, Settings theme controls, user-facing
  transaction copy, Activity recreation, force-stop/relaunch, and an installed
  selection journey (Slice 28B2).
- New themes, high contrast, icon packs, Full effects, or visual changes to
  Oxygen, Paper, Terminal, Home, Settings, weather marks, typography, or roles.
- Generic preference abstraction/consolidation, DataStore migrations for other
  settings, new dependencies, or module changes.
- Provider/repository/cache/location/unit/layout/effects/alert semantics,
  canonical or presented forecast values, network behavior, release status, or
  MVP-readiness claims.

## Ready Criteria

- `specified`: specification/roadmap contracts remain authoritative.
- `planned`: this file selects only theme preference storage/state.
- `covered`: the named JVM and connected tests encode the stated boundaries.
- `implemented`: production codec, DataStore, state transaction, and
  `OxygenApp` consumption exist without 28B2 reachability.
- `verified`: focused tests, the one connected case, broad checks, and diff
  review pass with retained logs.
- `committed`: verified implementation exists in Git. Do not close the cycle
  until the required post-commit authority sync is also complete.

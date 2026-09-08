# Slice 27B1 - Layout Preference Storage and State

**Status:** planned
**Cycle ID:** `2026-09-08-slice-27b1-layout-preference-storage-state`
**Planning basis:** local `main` at `a7b8434`; Slice 27A implementation is
committed at `660e376` and its authority sync is committed at `a7b8434`.
**Next action:** create the verification ledger, run focused behavioral-red
state/storage tests against the current session-only layout path, then add the
layout preference storage and state-holder transitions.

## Selected Behavior

Add the local storage and app-state boundary for a persisted Simple/Standard
layout preference without changing the installed Settings UI or claiming
installed restart restoration.

This slice stops when the production state-holder can read, expose, retry, and
write a confirmed layout choice through injected storage while preserving the
existing forecast, alert, selected-location, units, and effects state.

## Acceptance Boundary

1. A missing, malformed, incomplete, future-version, unknown-value, Detailed, or
   Meteorologist record resolves to Standard without persisting or presenting
   unsupported choices.
2. A valid Simple or Standard record becomes the confirmed effective layout
   before a ready Home state is published.
3. Initial read failure exposes retryable layout load failure and uses Standard
   only until a confirmed choice exists.
4. Retrying after a confirmed Simple choice preserves Simple while loading and
   while a retry failure is reported.
5. A new selection is transactional: pending state is exposed, the confirmed
   layout remains effective until write success, a failed write preserves the
   prior confirmed layout, and retry can commit the requested layout.
6. Layout state changes do not issue forecast, alert, location, geocoding, unit,
   or effects requests and do not alter the ready weather presentation.

## Intended Files

Expected production changes:

- `app/src/main/kotlin/com/oxygen/weather/app/LayoutPreferenceStorage.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/OxygenAppStateHolder.kt`
- narrowly scoped constructor/state plumbing where required by tests

Expected test changes:

- `app/src/test/kotlin/com/oxygen/weather/app/LayoutPreferenceStorageTest.kt`
- `app/src/test/kotlin/com/oxygen/weather/app/LayoutPreferenceStateHolderTest.kt`
- focused updates to existing app state-holder contract tests only if needed

No `MainActivity`, installed DataStore wiring, Settings UI copy, connected UI
test, screenshot, or installed relaunch work belongs in this sub-slice unless a
compile break requires a minimal adapter.

## Focused Evidence

Create:

```text
.codex/test-artifacts/2026-09-08-slice-27b1-layout-preference-storage-state/ledger.md
```

Run focused tests once before implementation to record red behavior, then once
after implementation:

```sh
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*LayoutPreferenceStorageTest' --tests '*LayoutPreferenceStateHolderTest' --tests '*OxygenAppContractTest'
```

The expected red result should fail because no layout preference storage/state
path exists yet. The green result must prove codec behavior, read/retry/write
transitions, and no forecast/request mutation at the app-state boundary.

## Broad Checks

After focused green on the final revision, run once:

```sh
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
git diff --check
```

`assembleDebug`, connected tests, emulator startup, screenshots, and installed
manual checks are deferred to 27B2/27B3 because this sub-slice deliberately
stops at the storage/state boundary.

## Documentation Closeout

If this slice is committed, append a concise history entry and update this plan
to the actual committed state. Do not move README/specification installed-app
claims to persisted layout behavior until 27B3 verifies the production
installed restoration path.

## Out Of Scope

- Settings / Appearance transaction UI, status text, retry controls, and
  selected semantics.
- `MainActivity` production DataStore creation and installed Activity
  recreation or force-stop/relaunch evidence.
- Compact rendered Appearance screenshots.
- Detailed or Meteorologist layout rendering, controls, storage, aliases, or
  migration.
- Theme, icon, high-contrast, effects, units, provider, alert, location,
  geocoding, Room/cache, release, MVP, or privacy-audit behavior changes.

## Planning Evidence

This plan was adjusted to honor the AGENTS 40% context target and the roadmap's
split 27B1/27B2/27B3 sequence. Commands used were read-only `nl`, `sed`, `rg`,
`wc`, and prior `git diff --check`. No Gradle, connected, emulator,
installation, screenshot, or live-provider command was run for this planning
adjustment.

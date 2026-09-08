# Slice 27B - Persisted Simple/Standard Layout Selection and Restoration

**Status:** implemented; focused unit, targeted connected, and broad checks passed in the uncommitted worktree
**Cycle ID:** `2026-09-08-slice-27b-persisted-layout-selection`
**Planning basis:** committed `main` at `61cfe8c1c38917254e33d3618a7941600864fafc`.
The working tree contains an uncommitted 27B candidate with focused unit,
targeted connected, and broad verification recorded in the ledger; no commit or
post-commit authority sync has been performed.
**Next action:** review the final uncommitted diff against this plan, then
either run a separate ADB-driven installed force-stop/relaunch journey if that
evidence is still required beyond the targeted connected restoration path, or
commit 27B and perform the required post-commit authority sync.

## Selected Behavior and Explicit Split

Persist a user's supported Simple/Standard layout choice locally and restore it
without refetching or changing the existing weather presentation.

The current worktree is explicitly reconciled into these evidence boundaries;
it must not be described as 27B1-only:

- **27B1 - storage/state:** `LayoutPreferenceStorage.kt`, the
  `OxygenAppStateHolder` layout state and transitions, codec/state-holder unit
  tests, and the existing app contract-test adjustment.
- **27B2 - installed surface:** `MainActivity.kt` DataStore construction,
  `OxygenApp.kt` state plumbing, `SettingsScreen.kt` Appearance controls and
  retry/status semantics, plus the focused connected UI path.
- **27B3 - installed restoration:** production DataStore readback across
  Activity recreation and force-stop/relaunch evidence.

These boundaries are sequential acceptance gates over the already-present
uncommitted candidate. No additional production scope is authorized beyond the
files named above and the tests/artifact needed to prove these gates.

## Acceptance Boundary

### Storage contract

The production DataStore uses preference file `oxygen_layout_preferences` with
integer key `layout_preference_version` and string key
`layout_preference_value`. Version `1` encodes only `simple` and `standard`;
the encoder emits those lowercase values exactly.

Decoding trims surrounding whitespace and accepts case-insensitive
`simple`/`standard`. Missing either key, a non-`1` version (including future
versions), a blank/unknown value, or `detailed`/`meteorologist` produces
`NoSupportedChoice`. That result selects Standard and does not rewrite the
stored record. Encoding Detailed or Meteorologist fails before any write.

DataStore writes are one `edit` transaction: remove the old version/value pair
and write the new complete pair. The storage adapter exposes blocking read/write
methods backed by `runBlocking`; exceptions cross that adapter unchanged and
are caught by the state holder on its existing forecast executor.

### State and publication contract

The managed state holder uses Standard only as the unconfirmed conservative
effective layout. A ready Home state is not published until the initial layout
read has reached `Loaded` or `Failed`; a stored Simple choice must therefore be
confirmed before the first ready Home publication. A read failure may publish a
ready Home with Standard and `Failed`, but never presents Standard as a
confirmed stored choice.

Required observable transitions are:

- startup: `Loading(confirmed = null)` -> `Loaded(Simple|Standard)` or
  `Failed(confirmed = null)`;
- retry after a confirmed choice: retain that effective choice through
  `Loading(confirmed = choice)` and through a possible `Failed(confirmed = choice)`;
- supported read: effective layout changes only when `Loaded` is published;
- same-choice selection: no write and no state transition beyond existing
  presentation state;
- new selection: publish `pending` while the confirmed/effective layout remains
  unchanged; on success clear `pending`, set the new confirmed/effective value,
  and publish `Loaded`; on failure clear `pending`, set `writeError`, and retain
  the prior confirmed/effective value;
- write retry: repeat the last failed choice and clear `writeError` only after
  the write succeeds.

The state-holder test must collect emitted states and assert ordering, not only
the final value: no `ForecastReady` precedes layout `Loaded`/`Failed`, and a
confirmed Simple value is visible before any ready Home carrying Simple.
Cached restoration, selected-location restoration, and forecast startup must
preserve that ordering.

### No-request and preservation contract

The authoritative app-boundary test uses recording fakes at these seams:

- `WeatherRepository.refresh` count is the forecast boundary and, because
  alert lookup is merged inside that repository path, is also the authoritative
  alert-request boundary for this slice;
- `GeocodingRepository.search`, `SelectedLocationStorage` reads/writes,
  `DeviceLocationSource`, and `CoordinateTimeZoneResolver` counters cover
  location/geocoding work;
- `UnitPreferenceStorage` and `EffectsPreferenceStorage` read/write counters
  cover unit/effects work.

From a ready Home, layout selection, read retry, write failure, and write retry
must leave all those counters unchanged. The same test snapshots and compares
selected location, canonical/ready dashboard data, freshness, source and
provenance, alert status/details, unit preference, and effects preference.
Only the layout state and its settings status may change.

## Exact Worktree and Test Scope

Production files already in the candidate scope:

- `app/src/main/kotlin/com/oxygen/weather/app/LayoutPreferenceStorage.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/OxygenAppStateHolder.kt`
- `app/src/main/kotlin/com/oxygen/weather/MainActivity.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/OxygenApp.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/settings/SettingsScreen.kt`

Test files already in the candidate scope:

- `app/src/test/kotlin/com/oxygen/weather/app/LayoutPreferenceStorageTest.kt`
- `app/src/test/kotlin/com/oxygen/weather/app/LayoutPreferenceStateHolderTest.kt`
- `app/src/test/kotlin/com/oxygen/weather/app/OxygenAppContractTest.kt`
- `app/src/androidTest/kotlin/com/oxygen/weather/app/LayoutPreferenceDataStoreInstrumentedTest.kt`
- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/settings/LayoutPreferenceUiTest.kt`

The only permitted test additions within this plan are the missing decode-matrix,
event-order, request-counter, or presentation-preservation assertions in those
files. No vague constructor plumbing or compile-break adapter is in scope.

## Evidence and Verification

Use:

```text
.codex/test-artifacts/2026-09-08-slice-27b-persisted-layout-selection/ledger.md
```

The review itself ran only read-only discovery. It did not run Gradle,
connected, emulator, install, screenshot, or provider commands. The prior
red phase was missed because the storage/state path and tests were already in
the worktree; record that fact and the current revision explicitly in the
ledger instead of fabricating a before-change failure.

Run one focused unit pass for 27B1:

```sh
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*LayoutPreferenceStorageTest' --tests '*LayoutPreferenceStateHolderTest' --tests '*OxygenAppContractTest'
```

Run only the targeted connected cases needed for 27B2/27B3, limited to eight
cases total: DataStore readback/activity recreation, layout UI commit and
failure/retry, and installed force-stop/relaunch restoration. The connected
acceptance boundary is the installed Settings-to-restoration path, not an
entire historical test class.

After focused and connected evidence passes, run once:

```sh
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

Verification budget: one focused pass, one connected/install pass, and one
broad pass. Stop after a bounded platform timeout or one repeated failure; do
not convert an unavailable emulator or provider into mock success or retry the
same blocked attempt indefinitely. Record every command, result, and skipped
command in the ledger.

## Closeout and Authority Sync

Do not mark any 27B boundary committed until its stated evidence exists. On
commit, update this plan to the actual state, update the roadmap's 27B1/27B2/
27B3 status and next candidate only as each gate is evidenced, append a
self-contained cycle-history entry, and refresh the recent-state summary.

That post-commit documentation sync must also correct the roadmap metadata to
the observed `origin/main` at `6fa6663` and the actual planning base
`61cfe8c`, and remove or restate the history summary's old description of the
findings edit as a pre-existing caveat. Leave README and specification claims
that layout persistence is unfinished unchanged until 27B3 installed
restoration evidence passes.

## Out of Scope

- Detailed or Meteorologist layout rendering, controls, aliases, migration, or
  storage.
- Theme, icon, effects, units, provider, alert semantics, location behavior,
  geocoding behavior, Room/cache behavior, release readiness, and MVP claims.
- Any production or test file outside the exact scope listed above.

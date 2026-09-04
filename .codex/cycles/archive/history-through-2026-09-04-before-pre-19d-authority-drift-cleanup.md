# Cycle History

## Reading Contract

Normal discovery must not read the full archived cycle ledger.

Read `AGENTS.md`, `.codex/plans/current.md`, and this file first. For cycle
history context, use this live file plus at most the most recent three cycle
entries unless a specific implementation detail, regression, artifact, commit,
or authority conflict requires older evidence.

Older detailed history is retained in:

```text
.codex/cycles/archive/history-through-2026-09-01-before-tail-limited-history.md
.codex/cycles/archive/history-through-2026-09-03-before-19a-tail-refresh.md
.codex/cycles/archive/history-through-2026-09-03-before-post-19c-doc-sync.md
.codex/cycles/archive/history-through-2026-09-03-before-slice-32-planning.md
.codex/cycles/archive/history-through-2026-09-04-before-plan-gap-fixes.md
```

When adding a new history entry, append it to this file as a self-contained
section with status, changed behavior or documents, focused evidence, broad
evidence, artifacts, blockers, and commit state. Keep each entry concise enough
that the last one to three entries remain usable within roughly 1,000 tokens.

Before replacing or compressing this live file, archive the previous live file
under `.codex/cycles/archive/`. Do not create a full duplicate archive before
ordinary append-only writes; Git history plus the archive file preserve previous
ledger states.

## Recent State Summary

- Last committed implementation slice: Slice 31B, Fallback Cache and
  Provenance, committed at `4028044`.
- Latest ready-to-commit slice: Slice 32, Fallback Real-Path Verification,
  committed in this changeset.
- Current documentation drift under review: none known. Slice 32 reconciled the
  MET Norway provider contract with verified active installed-app fallback
  status while leaving conditional GET/304, provider health/backoff, and
  release-candidate fallback behavior unclaimed.
- Current process correction: the live cycle history was compressed on
  2026-09-04 after archiving the previous live file at
  `.codex/cycles/archive/history-through-2026-09-04-before-plan-gap-fixes.md`.

## Recent Cycles

### 2026-09-03-post-19c-doc-sync

Status: committed
Mode: documentation-only
Slice: Post-19C documentation sync
Commit: committed in `4cdecdd`

Result:
- Updated the live history summary to reflect committed Slice 19C at `e2efdd3`.
- Synchronized the roadmap saved-location sub-slice statuses through 19C and
  moved the next candidate from stale Slice 19A text to Slice 31A.
- Corrected Data Sources and Privacy local-data text so installed Room forecast
  cache and saved-location persistence matched verified behavior, while
  save/remove UI and installed-app MET Norway fallback remained not implemented.

Evidence:
- `git diff --check` passed.

Artifacts:
- `.codex/test-artifacts/2026-09-03-post-19c-doc-sync/git-diff-check.log`.

Boundaries:
- No Kotlin, Compose, Gradle, manifest, provider request, Room schema,
  DataStore format, forecast-cache format, UI, saved-location save/remove,
  unit preference, alert, air quality, radar, release, or MVP behavior changed.
- Android compile, unit tests, connected tests, and assemble were not run
  because this was a documentation-only status sync.

### 2026-09-03-installed-app-fallback-wiring

Status: committed
Mode: feature
Slice: Slice 31A, Installed-App Fallback Wiring
Commit: `4cdecdd`

Result:
- Added an installed forecast repository factory that composes Open-Meteo as
  default, MET Norway as fallback, and the existing Room-backed cache wrapper
  for MainActivity's selected-location Home path.
- Preserved core fallback eligibility: Open-Meteo success, network/offline
  failure, and provider-rejected requests do not call MET Norway; eligible
  terminal provider failures can call MET Norway.
- Verified MET Norway fallback success reaches the provider-neutral Home ready
  presentation with MET Norway source/provenance text and an identifying
  Oxygen User-Agent.

Evidence:
- Focused checks passed: app `InstalledForecastRepositoryFactoryTest`,
  `OxygenAppContractTest`, `AboutDisclosureStateHolderTest`,
  `HomeForecastStateHolderTest`, and core `FallbackWeatherRepositoryTest`.
- Connected real-path exercise passed:
  `:app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.InstalledFallbackRepositoryInstrumentedTest`.
- Broad checks passed: compileDebugKotlin, app/core debug unit tests,
  assembleDebug, and `git diff --check`.

Artifacts:
- `.codex/test-artifacts/2026-09-03-installed-app-fallback-wiring/`.

Boundaries:
- No saved-location save/remove UI, provider preference UI, unit preference,
  alerts, air quality, radar/map, Room schema change, DataStore format change,
  provider-specific MET Norway cache-header persistence, conditional GET
  metadata, cached fallback restore claim, stale fallback provenance,
  release-candidate status, or MVP-readiness behavior was added.

### 2026-09-03-fallback-cache-provenance

Status: committed
Mode: feature
Slice: Slice 31B, Fallback Cache and Provenance
Commit: `4028044`

Result:
- Added cache-only forecast metadata for provider cache headers and provider
  response metadata without adding raw headers to `WeatherBundle` or Home UI.
- MET Norway repository success now carries Expires, Last-Modified, ETag,
  fetch time, response coordinates/elevation, provider updated time, and
  provider ID for cache storage.
- Room forecast cache storage persists that metadata with a v2-to-v3 migration,
  clears stale provider cache metadata on non-metadata replacement, and still
  restores cached MET Norway forecasts with MET Norway provenance.

Evidence:
- Baseline checks passed: `InstalledForecastRepositoryFactoryTest`,
  `HomeForecastStateHolderTest`, and `CachedWeatherRepositoryTest`.
- Focused checks passed: core `CachedWeatherRepositoryTest` plus
  `MetNoWeatherRepositoryTest`, app `AboutDisclosureStateHolderTest`, connected
  `RoomForecastCacheStorageInstrumentedTest`, and connected
  `RoomSavedLocationStorageInstrumentedTest`.
- Broad checks passed: compileDebugKotlin, app/core debug unit tests,
  assembleDebug, and `git diff --check`.

Artifacts:
- `.codex/test-artifacts/2026-09-03-fallback-cache-provenance/`.

Blockers:
- Initial connected Room attempt found no connected devices. The repo-local
  `oxygen_starter` emulator was started. A parallel connected rerun then
  crashed instrumentation/uninstall; sequential reruns passed.

Boundaries:
- No conditional GET request, 304 not-modified handling, provider health or
  backoff state, provider preference UI, saved-location save/remove UI, unit
  preference, alert, air quality, radar, release-candidate status, or
  MVP-readiness behavior was added.

### 2026-09-03-fallback-real-path-verification

Status: committed
Mode: feature
Slice: Slice 32, Fallback Real-Path Verification
Commit: committed in this changeset

Result:
- Added deterministic connected installed-boundary tests for fallback-served
  Room restore and later Open-Meteo replacement using
  `RoomForecastCacheStorageFactory.create(...)`,
  `DataStoreSelectedLocationStorage`,
  `InstalledForecastRepositoryFactory.create(...)`, and
  `OxygenAppStateHolder`.
- Verified fallback-served Home state keeps MET Norway source, combined
  NLOD/CC-BY license, issued/fetched/model-estimate provenance, no sample data,
  empty alert state, and no rendered alert section.
- Verified a later Open-Meteo success replaces cached MET Norway data through
  the normal selected-location refresh/cache path.
- Reconciled `docs/data-sources/MET_NORWAY_FORECAST.md` with active
  installed-app fallback status while leaving conditional GET/304, provider
  health/backoff, and release-candidate fallback behavior unclaimed.

Evidence:
- Baseline checks passed: app `InstalledForecastRepositoryFactoryTest`, app
  `HomeForecastStateHolderTest`, core `FallbackWeatherRepositoryTest`, and
  `scripts/list-avds.sh`.
- Focused checks passed: app installed factory, app Home forecast state, core
  fallback repository, connected `InstalledFallbackRepositoryInstrumentedTest`,
  and connected `RoomForecastCacheStorageInstrumentedTest`.
- Real-path exercise passed: `scripts/start-emulator.sh` and
  `scripts/install-debug.sh` on `oxygen_starter`.
- Broad checks passed: compileDebugKotlin, app/core debug unit tests,
  assembleDebug, and `git diff --check`.

Artifacts:
- `.codex/test-artifacts/2026-09-03-fallback-real-path-verification/`.

Blockers:
- Initial focused connected run failed because no device was connected. After
  starting `oxygen_starter`, the first rerun exposed a test expectation mismatch
  for MET Norway's combined license string; the corrected rerun passed.

Boundaries:
- No production Kotlin behavior, provider semantics, Home copy, UI layout,
  Room schema, DataStore format, selected-location behavior, saved-location
  behavior, official alert provider behavior, conditional GET/304 handling,
  provider health/backoff, unit preference, appearance setting, release, or
  MVP-readiness behavior changed.
- Live manual Open-Meteo geocoding selection was not run; deterministic
  connected installed-boundary coverage exercised the default Open-Meteo
  success/replacement path without relying on provider/network availability.

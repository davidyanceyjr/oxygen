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

- Last committed implementation slice: Slice 18G, Oxygen Home Design-System
  Consolidation, commit `fae63b3`.
- Active planned implementation slice: Slice 18H, Standard Home Accessibility
  and Visual Verification Gate, in `.codex/plans/current.md`.
- Current documentation drift under review: the tracked art sheet path/spec say
  Base Art Sheet v0.1 while the visible image title says v0.2.
- Current process correction: cycle history is now tail-limited for normal reads
  and older detailed ledger content has been archived.

## Recent Cycles

### 2026-09-01-home-operational-state-integration

Status: committed
Mode: feature
Slice: Slice 18F, Home Operational State Integration
Commit: `79bd830`

Result:
- `OxygenAppStateHolder` ignores duplicate Home refresh calls while the selected
  Home forecast is already refresh-in-progress.
- Visible restored/stale cached Home content is preserved as refresh-failed
  stale success if a foreground refresh failure arrives after cached content is
  already visible.

Evidence:
- Focused HomeForecast unit baseline passed before production edits.
- Focused HomeForecast unit tests passed after implementation and cover
  restored-cache foreground refresh failure retention plus duplicate-refresh
  prevention.
- Focused Home Compose instrumentation passed 15 tests on `oxygen_starter(AVD)
  - 17`, covering fresh success, restored-cache success across pages,
  stale/refresh-failed success, refresh-in-progress, loading, retryable no-cache
  error, source/provenance reachability, tab/swipe navigation, compact width,
  large font, and sibling non-overlap.
- Installed-app cached/offline screenshot and hierarchy:
  `.codex/test-artifacts/2026-09-01-home-operational-state-integration/installed-cached-now.png`
  and `.xml`.
- Installed-app no-cache offline screenshot and hierarchy:
  `.codex/test-artifacts/2026-09-01-home-operational-state-integration/installed-no-cache-error.png`
  and `.xml`.
- Broad checks passed: `. scripts/android-env.sh && ./gradlew
  :app:compileDebugKotlin`; `. scripts/android-env.sh && ./gradlew
  :app:testDebugUnitTest :core:testDebugUnitTest`; `. scripts/android-env.sh &&
  ./gradlew :app:assembleDebug`; `git diff --check`.

Boundaries:
- No provider behavior, repository/cache schema, DataStore/Room design,
  installed-app MET Norway fallback, saved-location switching, unit preferences,
  official alert lookup, air quality, radar, background refresh, persisted
  appearance settings, release readiness, or MVP readiness was added or claimed.

### 2026-09-01-project-status-sync-after-home-operational-state-integration

Status: ready
Mode: documentation-only
Slice: Project status sync after Slice 18F
Commit: not committed in this turn

Result:
- Compared README, specification section 53, roadmap Slice 18F/18G status,
  roadmap next-candidate guidance, active-cycle state, cycle history, `git log`,
  and worktree status after committed Slice 18F `79bd830`.
- README now records the committed Details visual baseline as installed-app
  behavior and no longer lists it as not implemented.
- Roadmap/spec next-candidate guidance now points to Slice 18G: Oxygen Home
  Design-System Consolidation.

Evidence:
- `git diff --check` passed.
- Android build/test/emulator commands were not run because this
  documentation-only sync changed only Markdown files and no Kotlin, Compose,
  Gradle, manifest, resources, provider, persistence, or test behavior.

### 2026-09-01-cycle-history-tail-read-contract

Status: ready
Mode: documentation-only
Slice: Cycle history token-size and archive contract
Commit: not committed in this turn

Result:
- Archived the previous full cycle ledger at
  `.codex/cycles/archive/history-through-2026-09-01-before-tail-limited-history.md`.
- Replaced the live `.codex/cycles/history.md` with a recent-history contract,
  current summary, and self-contained recent entries.
- Updated `AGENTS.md` so normal discovery reads only the live recent history and
  at most the most recent three cycle entries unless older evidence is
  specifically required.
- Clarified that full archive copies are required before replacing or
  compressing live history, not before ordinary append-only history writes.

Evidence:
- `git diff --check` passed.
- Android build/test/emulator commands were not run because this
  documentation-only process change touched only Markdown files and no Kotlin,
  Compose, Gradle, manifest, resources, provider, persistence, or test behavior.

### 2026-09-01-home-design-system-art-sheet-consolidation

Status: committed
Mode: feature
Slice: Slice 18G, Oxygen Home Design-System Consolidation
Commit: `fae63b3`

Result:
- Added static app-local Home design roles for spacing, card shape/padding,
  glass surfaces, outline accents, weather-mark colors, and Home typography.
- Replaced the generic blob-like `WeatherConditionMark` with provider-neutral
  gold-line marks for all current `WeatherCondition` values.
- Applied the Home roles across the installed Home Now, Hourly, Daily, Details,
  stale/status, metric/list, source, and shared glass-panel surfaces without
  changing provider, repository, Room, DataStore, weather mapping, Gradle,
  app identity, saved-location, unit-preference, or alert behavior.

Evidence:
- Baseline focused HomeForecast unit tests passed before production edits.
- Initial baseline connected Home instrumentation failed with `No connected
  devices`; after starting `oxygen_starter`, focused Home instrumentation
  passed 16 tests, including the new rendered-pixel gold-line weather-mark test.
- Final focused checks passed: `. scripts/android-env.sh && ./gradlew
  :app:testDebugUnitTest --tests '*HomeForecast*'`; `. scripts/android-env.sh
  && ./gradlew :app:connectedDebugAndroidTest
  -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest`.
- Broad checks passed: `. scripts/android-env.sh && ./gradlew
  :app:compileDebugKotlin`; `. scripts/android-env.sh && ./gradlew
  :app:testDebugUnitTest :core:testDebugUnitTest`; `. scripts/android-env.sh &&
  ./gradlew :app:assembleDebug`; `git diff --check`.

Artifacts:
- Logs and screenshots are under
  `.codex/test-artifacts/2026-09-01-home-design-system-art-sheet-consolidation/`.
- Valid installed Home evidence: `installed-final-home-now.png`/`.xml`,
  `installed-final-home-hourly.png`/`.xml`,
  `installed-final-home-daily.png`/`.xml`,
  `installed-final-home-details.png`/`.xml`, and
  `installed-final-cached-stale-now.png`/`.xml`.
- Final focused logs: `focused-homeforecast-unit.log`,
  `focused-home-compose-instrumentation-final.log`.
- Broad logs: `broad-compile-debug-kotlin.log`,
  `broad-debug-unit-tests.log`, `broad-assemble-debug.log`,
  `git-diff-check.log`.

Boundaries:
- The art-sheet naming drift remains documented only: the tracked path/spec say
  Base Art Sheet v0.1 while the rendered image title says v0.2.
- Atmospheric scene imagery, production bitmap scenes, downloaded fonts, full
  icon packs, persisted presentation settings, full theme engine, installed-app
  MET Norway fallback, saved-location switching, unit preferences, release
  readiness, and MVP readiness were not added or claimed.
- A final installed retryable no-cache screenshot was not captured; the attempt
  failed after the targeted instrumentation run left the debug activity
  unavailable. No-cache behavior remains covered by passing Home instrumentation,
  state-holder, and offline persistence instrumentation boundaries.

### 2026-09-01-project-status-sync-after-home-design-system-consolidation

Status: ready
Mode: documentation-only
Slice: Project status sync after Slice 18G
Commit: not committed in this turn

Result:
- Compared README, specification section 53, roadmap Slice 18G/18H status,
  roadmap next-candidate guidance, active-cycle state, cycle history, `git log`,
  and worktree status after committed Slice 18G `fae63b3`.
- README now records the installed Home art-sheet-aligned weather marks,
  surface roles, typography roles, and app-local design roles.
- Roadmap/spec next-candidate guidance now points to Slice 18H: Standard Home
  Accessibility and Visual Verification Gate.
- Live cycle history now records Slice 18G as committed at `fae63b3`.

Evidence:
- `git diff --check` passed.
- Android build/test/emulator commands were not run because this
  documentation-only sync changed only Markdown files and no Kotlin, Compose,
  Gradle, manifest, resources, provider, persistence, or test behavior.

# Active Cycle

Status: planned
Cycle ID: 2026-09-02-open-meteo-ready-forecast-recovery
Mode: fix
Goal: Plan and implement Slice 18J-R, Restore Installed Open-Meteo Ready
Forecast Path, as the bounded recovery slice required before completing Slice
18J visual verification. This cycle restores the production installed-app path
where a manually selected Open-Meteo geocoding result produces a usable
`ForecastReady` Home state instead of the current invalid-response no-cache
error.
Roadmap context: Slice 18J Standard Home Visual Convergence is partially
covered/implemented but blocked by installed-app real-path evidence. Selecting
"Madison, Wisconsin, United States" through the real Open-Meteo manual path
rendered "Weather data returned in a form Oxygen could not read. Try again
later." Slice 18J-R is inserted before resuming 18J evidence capture. Slice 19A
saved-location storage remains blocked until Slice 18J is committed.

## Contract

Selected Behavior:
- A real manually selected Open-Meteo geocoding result can fetch, parse, map,
  cache, and present a ready forecast in the installed app.
- The same installed production path reaches `ForecastReady` without using
  `SampleWeather.bundle`, mocked provider success, fabricated fallback data, or
  relaxed error handling.
- Open-Meteo invalid-response classification remains meaningful for malformed
  or contract-breaking provider responses.
- Provider-specific diagnostics remain outside Compose and user-facing Home
  copy.
- Existing manual-location, selected-location persistence, Room forecast-cache,
  stale-cache, refresh/retry, provenance, data-source disclosure, and Home UI
  semantics remain intact.
- The already implemented Slice 18J Home presentation changes remain
  untouched except where a minimal adjustment is required to exercise the
  restored ready forecast path.

Acceptance Boundary:
- Focused automated coverage reproduces the production invalid-response cause
  or the parser/mapper contract gap that caused the installed Madison forecast
  to fail.
- The fix is in the real Open-Meteo forecast production path, not in UI
  suppression, sample data, or a test-only branch.
- Focused provider/repository/mapper tests prove a representative real
  Open-Meteo forecast response shape maps to provider-neutral Oxygen forecast
  data with current, hourly, daily, provenance, and required Home fields.
- Existing invalid-response behavior remains covered for malformed JSON,
  missing required envelope/arrays, or unsupported required units/fields where
  applicable.
- Installed-app exercise on `oxygen_starter` uses manual location search,
  selects a real Open-Meteo geocoding result, and reaches a ready Home state
  with visible location, current weather, hourly, daily, source/update, and
  Open-Meteo attribution/provenance.
- The successful live fetch either writes the selected location forecast into
  the Room cache or records a cache-write failure as a blocker/regression
  instead of treating the ready screen alone as complete.
- After the successful live fetch has populated the cache, disabling network
  and refreshing or relaunching preserves the existing stale/restored behavior
  for the same selected location.
- No saved-location list/switching/removal, unit preferences, device-location
  expansion, MET Norway fallback wiring, alerts, air quality, radar/maps,
  widgets, persisted appearance settings, or release/MVP readiness behavior is
  added.

Out of Scope:
- Additional forecast providers or installed-app MET Norway fallback behavior.
- Saved-location storage/list/select/remove behavior and any schema work not
  strictly required by the existing selected-location/cache path.
- Unit conversion or unit preference UI.
- Device-location permission-flow expansion.
- Home visual redesign beyond preserving the current in-progress 18J UI state.
- Data-source disclosure expansion unless the restored behavior reveals an
  existing factual disclosure error.
- Broad provider resilience improvements unrelated to the reproduced
  invalid-response cause.
- Treating 18J-R completion as Slice 18J visual verification or as permission
  to start Slice 19A.

## Implementation Plan

1. Discover and baseline:
   - Read this active plan, the live recent history summary and most recent
     entries, roadmap/spec sections for 18J-R/18J/19A, Open-Meteo forecast and
     geocoding provider contracts, and the current Open-Meteo DTO/client,
     parser/mapper, repository/cache, selected-location, and Home state-holder
     tests.
   - Record `git status --short` before production edits and preserve unrelated
     existing 18J worktree changes.
   - Inspect the saved failed installed evidence under
     `.codex/test-artifacts/2026-09-02-standard-home-visual-convergence/`,
     especially `installed-real-path-forecast-invalid-response.png`,
     `installed-home-after-selection.xml`, and
     `installed-home-network-restored.xml`.
   - Capture or log the real provider response shape only if needed to identify
     the parser/mapper gap; store logs under the cycle artifact directory and
     keep provider-specific details out of UI state.

2. Red or baseline:
   - Add the smallest focused failing test at the provider DTO/parser/mapper or
     repository boundary that reproduces the invalid-response cause from a
     representative Open-Meteo forecast response.
   - If the failure is due to request construction, add focused coverage for
     the generated forecast request parameters instead.
   - Run the focused test command needed for that boundary and save the log.

3. Build:
   - Fix the real Open-Meteo forecast path at the narrowest responsible layer:
     DTO parsing, request fields, unit handling, mapper tolerance for optional
     values, or repository error classification.
   - Preserve required-value validation; do not convert unknown/missing values
     to zero or sample defaults.
   - Keep provider DTO/query names isolated from Composables and app UI models.

4. Focused green:
   - Run the focused provider/repository/mapper tests that failed or were added.
   - Run existing HomeForecast-focused state tests if the mapped Home state
     contract changes.
   - Save logs under
     `.codex/test-artifacts/2026-09-02-open-meteo-ready-forecast-recovery/`.

5. Real-path exercise:
   - Build/install the debug app on `oxygen_starter`.
   - Use manual search and select a real Open-Meteo geocoding result, starting
     with "Madison, Wisconsin, United States" because that is the known failed
     path unless provider availability requires another documented real result.
   - Capture installed ready Home screenshot/hierarchy evidence for Now,
     Hourly, Daily, and Details.
   - Confirm the successful live fetch populated the Room cache, then exercise
     one stale/restored or refresh-failed path for the same selected location.
     Treat a cache-write failure as a blocker/regression to diagnose rather
     than optional evidence to skip.

6. Broad checks:
   - Run:
     `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
   - Run:
     `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
   - Run:
     `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
   - Run:
     `git diff --check`
   - Save logs under the cycle artifact directory.

7. Review and handoff:
   - Confirm no saved-location, unit, fallback, alert, provider-disclosure,
     UI-redesign, or release/MVP scope leaked into the fix.
   - Record changed production/test files, focused evidence, broad evidence,
     installed-app evidence, and remaining risks.
   - If verified, leave Slice 18J-R ready and identify the immediate next step
     as resuming Slice 18J installed visual evidence capture, not starting
     Slice 19A.

## Phase Results

- specified: Slice 18J-R is specified in `.codex/plans/mvp-roadmap.md` as the
  Open-Meteo ready forecast recovery slice inserted after blocked Slice 18J
  discovery and before resuming 18J visual verification.
- planned: This document selects only Slice 18J-R. The behavior boundary is the
  production installed Open-Meteo manual-location forecast path reaching a ready
  Home state again.
- covered: not started.
- implemented: not started.
- verified: not started.
- artifacts target:
  `.codex/test-artifacts/2026-09-02-open-meteo-ready-forecast-recovery/`.
- deferred: Slice 18J visual verification remains blocked until this recovery
  slice restores a real ready forecast path. Slice 19A remains deferred until
  Slice 18J is committed.

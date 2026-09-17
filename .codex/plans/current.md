# Oxygen UI v0.3 — UI-01 Completion Record

**Status:** verified
**Commit state:** committed with this change
**Mode:** bounded production data prerequisite
**Cycle ID:** `2026-09-16-ui-01-forecast-horizon-transport`
**Active roadmap:** `.codex/plans/ui-roadmap.md`
**Completed slice:** UI-01 — Forecast-horizon transport and preservation
**Implementation slices since UI checkpoint:** 1; the required test-only and
documentation checkpoint follows the second production-changing slice.

## Verified behavior

For a selected `WeatherLocation`, the installed Open-Meteo production path now
requests `forecast_hours=72` and `forecast_days=10`. The default remains
configurable through `OpenMeteoForecastRequest`.

Open-Meteo hourly rows are mapped by indexed provider row and stably ordered by
`Instant`; daily rows are stably ordered by `dateEpochDay`. Valid duplicates,
null optional values, unequal optional arrays, sparse rows, and valid empty
timelines remain truthful. Fallback, cache, Room, provenance, selected
location, alert, and presentation paths were preserved.

The acceptance boundary passed through the installed factory, Open-Meteo
client/parser/repository, fallback wrapper, cache wrapper, Room storage, and
no-alert merge. It observed all 72 hourly and ten daily rows in the terminal
factory result and direct Room readback using a normal `WeatherLocation` and no
`SampleWeather` data.

## Changed files

- Production: `OpenMeteoForecastClient.kt` and `OpenMeteoForecastMapper.kt`.
- Core tests: Open-Meteo client/mapper, MET Norway mapper, fallback repository,
  and cached repository tests.
- Android test: one installed factory/Room acceptance method in
  `InstalledFallbackRepositoryInstrumentedTest.kt`.
- Documentation: `README.md`, `docs/data-sources/OPEN_METEO_FORECAST.md`, and
  UI-01 in `.codex/plans/ui-roadmap.md`.

## Evidence

Artifacts: `.codex/test-artifacts/2026-09-16-ui-01-forecast-horizon-transport/`.

- The required contracted-query red result is in `red-client-test.log`: the
  changed assertion failed against the old 48-hour default.
- The focused green core command passed 49 tests; output is in
  `focused-core-green.log`.
- Android test compilation passed in `android-test-compile-green.log`.
- The single connected case passed 1/1 with fresh XML, instrumentation log,
  textproto, device diagnostics, and no ANR in `android-factory/`.
- Broad checks completed successfully: `:app:compileDebugKotlin`, app/core
  debug unit tests, `:app:assembleDebug`, and `git diff --check`. Outputs are
  `broad-app-compile.log`, `broad-unit.log`, `broad-assemble.log`, and
  `diff-check.log`.
- The recovered API-37 `oxygen_starter` session was stopped after the connected
  case. The recovery host wrapper ended before writing its preflight record;
  the same session was completed with read-only preflight capture in
  `emulator/preflight.txt` and was not restarted.
- Screenshots, live-provider traffic, manual UI rendering, and visual matrices
  were inapplicable to this deterministic provider/repository/Room boundary.

The full product specification, UI specification, and MET Norway provider
contract were reviewed unchanged; none contradicted the verified slice.

## Limits and next action

Hourly/Daily window selection, partial-horizon copy, controls, pager/Back,
visual redesign, themes/effects, new providers, Room schema/migrations,
provider identity, fallback eligibility, cache policy, alert behavior, release,
and 1.0 completeness remain outside UI-01.

No next slice is selected here. UI-02 remains `specified` in the active
roadmap and must be selected in a new active plan.

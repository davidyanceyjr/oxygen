# Oxygen UI v0.3 — UI-01 Active Plan

**Status:** planned
**Mode:** bounded production data prerequisite
**Cycle ID:** `2026-09-16-ui-01-forecast-horizon-transport`
**Active roadmap:** `.codex/plans/ui-roadmap.md`
**Selected slice:** UI-01 — Forecast-horizon transport and preservation
**Implementation slices since UI checkpoint:** 0. This is the first
production-changing slice; a successful completion sets the count to 1.
**Sizing rationale:** medium. One provider-to-cache transport concern changes
the Open-Meteo request default and its chronological mapping. Existing MET
Norway, fallback, Room, and installed-factory paths are preservation boundaries
with focused evidence, not redesigned behaviors. Hourly/Daily selection and
rendering remain separate UI state-machine slices.
**Next action:** change the existing Open-Meteo contracted-query assertion to
72 hours and record its expected red result against the current 48-hour
default. Then add the mapper preservation tests before making the narrow code
change.

## Selected behavior and acceptance boundary

For a selected `WeatherLocation`, the installed production repository path
requests Open-Meteo with `forecast_hours=72` and the existing
`forecast_days=10`. The default request remains configurable through
`OpenMeteoForecastRequest`; only its default changes.

Open-Meteo domain mapping retains every row keyed by a valid provider
`hourly.time` or `daily.time` value, ordered earliest-to-latest with stable
equal-key order. Optional values remain null; unequal parallel optional arrays
continue to use `getOrNull` rather than dropping a timestamped row. No provider,
fallback, cache, or Room boundary truncates, deduplicates, pads, interpolates,
repeats, or invents hourly/daily values.

The deterministic Android acceptance travels through the real installed
composition: Open-Meteo client/parser/repository, fallback wrapper, cache
wrapper, Room storage, and alert merge. It records the request and observes all
72 hourly and ten daily rows both in the terminal factory result and after Room
readback. It uses only a normal `WeatherLocation`, never `SampleWeather`.

This is the data prerequisite for UI-05's rolling hourly windows and UI-07's
daily windows. The existing presentation mapper's `hourly.take(12)` and
`daily.take(10)` remain untouched: UI-05/UI-07 own selection, partial-horizon
copy, controls, and rendering. In particular, this slice preserves raw valid
MET Norway timestamps/dates for those later selection boundaries; its existing
mapper does not fabricate a dense 72-hour series.

## Production implementation

1. In `core/.../openmeteo/OpenMeteoForecastClient.kt`, change only
   `OpenMeteoForecastRequest.forecastHours` from `48` to `72`. Preserve explicit
   caller overrides, the ten-day default, endpoint, field lists, units,
   encoding, timeouts, and error classification.
2. In `core/.../openmeteo/OpenMeteoForecastMapper.kt`, first map each indexed
   provider row, then use stable chronological sorting: `Instant` for hourly
   and `dateEpochDay` for daily. Do not use maps, sets, grouping, a size cap,
   or a synthetic fill step. Preserve duplicate rows and their provider input
   order, canonical values, and provenance.
3. Do not change `OpenMeteoWeatherRepository`, `MetNoForecastMapper`,
   `FallbackWeatherRepository`, `CachedWeatherRepository`, Room entities/
   migrations, or `InstalledForecastRepositoryFactory` unless the focused red
   evidence exposes a concrete defect. The existing MET Norway mapper already
   maps actual sorted timesteps and aggregates them by selected-location local
   date; the fallback/cache/Room path must only preserve the bundle it receives.
4. Do not change `HomeForecastPresentationMapper`, `HomeScreen`, navigation,
   provider contracts beyond the verified Open-Meteo installed-request wording,
   or production test seams. The Android test may use the factory's existing
   dependency parameters, but must not alter factory construction for testing.

## Focused tests and deterministic inputs

Change or add only meaningful boundary tests in these existing locations:

- `core/src/test/kotlin/com/oxygen/weather/core/provider/openmeteo/OpenMeteoForecastClientTest.kt`
  — update `buildsContractedHomeForecastQueryFromConfigurableBaseUrl` to assert
  `forecast_hours=72` and retain its `forecast_days=10` assertion. Its initial
  48-versus-72 failure is the required red evidence.
- `core/src/test/kotlin/com/oxygen/weather/core/provider/openmeteo/OpenMeteoForecastMapperTest.kt`
  — add focused cases proving: a 72-hour/ten-date nullable response is not
  truncated; out-of-order hourly and daily inputs are stably ordered; duplicate
  time/date rows survive in original equal-key order; and valid empty timelines
  map to empty lists. Build small DTO copies from the parsed normal fixture;
  parser fixtures and parser coverage remain unchanged.
- `core/src/test/kotlin/com/oxygen/weather/core/provider/metno/MetNoForecastMapperTest.kt`
  — add one sparse, out-of-order, duplicate-timestep regression case. It must
  prove stable actual-hour ordering without interpolation and selected-location
  local-date aggregation. It must not expect duplicate daily rows, because MET
  Norway daily output is intentionally derived per local date. Retain the
  current empty-timeseries invalid-response coverage.
- `core/src/test/kotlin/com/oxygen/weather/core/provider/FallbackWeatherRepositoryTest.kt`
  and `core/src/test/kotlin/com/oxygen/weather/core/provider/cache/CachedWeatherRepositoryTest.kt`
  — extend one existing success path each with a long, nullable `WeatherBundle`
  and assert exact hourly/daily list equality at the terminal boundary. These
  tests lock pass-through behavior; they do not change fallback eligibility,
  freshness, or cache failure policy.
- `app/src/androidTest/kotlin/com/oxygen/weather/app/InstalledFallbackRepositoryInstrumentedTest.kt`
  — add exactly one non-Compose method named
  `openMeteoSeventyTwoHourHorizonSurvivesInstalledFactoryAndRoomReadback`.
  It creates `RoomForecastCacheStorageFactory` storage after deleting the known
  test database, calls `InstalledForecastRepositoryFactory.create` with a real
  `OpenMeteoWeatherRepository(OpenMeteoForecastClient(recordingTransport))`, a
  fixed clock, a fail-if-called fallback repository, and a deterministic
  no-alert provider. Clean up the database in `finally` and use a unique normal
  location ID. Assert the captured query has 72 hours/ten days, the terminal
  result and direct Room readback each contain the expected 72 ordered hourly
  instants and ten ordered local dates, selected sentinel null values survive,
  and fallback was not invoked.

The Android transport builds one minimal valid Open-Meteo JSON response at test
time: required envelope/current/unit objects, 72 timestamped hourly rows, ten
daily rows, and intentionally null optional values. It is deterministic test
input only, has no network dependency, reuses no `SampleWeather`, and does not
add a bulky checked-in fixture or production-only test hook.

## Focused evidence and verification budget

Create a concise ledger and command output under
`.codex/test-artifacts/2026-09-16-ui-01-forecast-horizon-transport/`.

1. Before production code changes, change the query expectation to 72 and run
   that one JVM method once, retaining the expected red result. Add the new
   preservation tests and record their baseline result once; do not repeat a
   known failure.

```bash
. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest \
  --tests com.oxygen.weather.core.provider.openmeteo.OpenMeteoForecastClientTest.buildsContractedHomeForecastQueryFromConfigurableBaseUrl
```

```bash
. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest \
  --tests com.oxygen.weather.core.provider.openmeteo.OpenMeteoForecastMapperTest \
  --tests com.oxygen.weather.core.provider.metno.MetNoForecastMapperTest
```

2. After the narrow implementation, run the named Open-Meteo, MET
   Norway, fallback, and cached-repository unit classes to green. The focused
   test set must prove request construction, both provider mapping boundaries,
   fallback pass-through, and cache readback rather than symbol existence.

```bash
. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest \
  --tests com.oxygen.weather.core.provider.openmeteo.OpenMeteoForecastClientTest \
  --tests com.oxygen.weather.core.provider.openmeteo.OpenMeteoForecastMapperTest \
  --tests com.oxygen.weather.core.provider.metno.MetNoForecastMapperTest \
  --tests com.oxygen.weather.core.provider.FallbackWeatherRepositoryTest \
  --tests com.oxygen.weather.core.provider.cache.CachedWeatherRepositoryTest
```

3. Start one recovered API-37 `oxygen_starter` session and run the one new
   connected case. The wrapper's 120-second deadline is the single permitted
   platform attempt; retain artifacts and stop on timeout, infrastructure
   failure, or assertion failure rather than retrying it.

```bash
scripts/start-emulator.sh --recover \
  --artifact-dir .codex/test-artifacts/2026-09-16-ui-01-forecast-horizon-transport/emulator
serial=$(tr -d '\r\n' < .codex/test-artifacts/2026-09-16-ui-01-forecast-horizon-transport/emulator/serial.txt)
scripts/run-connected-method.sh --serial "$serial" \
  com.oxygen.weather.app.InstalledFallbackRepositoryInstrumentedTest#openMeteoSeventyTwoHourHorizonSurvivesInstalledFactoryAndRoomReadback \
  .codex/test-artifacts/2026-09-16-ui-01-forecast-horizon-transport/android-factory
```
4. If focused acceptance passes, run these broad checks once and record their
   outputs. No rerun is warranted unless production code, test inputs, or the
   execution environment changes.

```bash
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

The connected budget is one relevant case. Screenshots, manual installation,
live-provider traffic, visual matrices, and a rendered UI journey are
inapplicable because this is a deterministic provider/repository/Room boundary;
record that exclusion in the ledger. The test's factory call is the required
real Android production-path exercise.

## Documentation and closure

After production behavior and the planned evidence pass, but before calling the
slice ready:

- Update `docs/data-sources/OPEN_METEO_FORECAST.md` so it truthfully says the
  installed default request uses 72 hourly hours and ten daily days; retain its
  configurable-base-URL and provider/privacy statements.
- Update the concise installed-behavior summary in `README.md` to name the
  verified 72-hour hourly and ten-day daily provider horizon. Do not expand it
  into a UI-05/UI-07 implementation claim.
- Update UI-01 in `.codex/plans/ui-roadmap.md` with actual behavior/evidence
  status and leave all unselected candidates `specified`. Set the next selected
  work only in the next active plan; after this production slice, the count is
  1 and UI-02 remains the ordered candidate.
- Review `docs/OXYGEN_FULL_SPECIFICATION.md`,
  `docs/OXYGEN_UI_SPECIFICATION.md`, and
  `docs/data-sources/MET_NORWAY_FORECAST.md`. They state the target contract
  already; edit only a statement proven false by the implementation, otherwise
  record the unchanged review.
- Replace this plan's forward-looking statements with actual status, commands,
  artifact paths, limits, and the next action. Append one concise,
  self-contained completion entry to `.codex/cycles/history.md` only when the
  slice is ready or committed. Complete the post-commit consistency review of
  the plan, roadmap, history, README, and affected provider contract; do not
  create a hash-only follow-up documentation commit.

## Explicit limits and invariants

- No Hourly/Daily window selection, partial-horizon presentation copy,
  controls, pager/Back changes, visual redesign, theme/effects, or screenshot
  claim.
- No weather fields/units, endpoint, provider identity, attribution, location
  behavior, alert behavior, fallback eligibility, cache policy, Room schema,
  migration, or persisted cache-format change.
- No fabricated/sample production data, live-network test, provider DTO/cache
  object in presentation, or assertion that the existing 12-row UI cap is a
  transport limit.
- Preserve current selected-location, stale-cache, source/provenance, privacy,
  manual-location, and error behavior; only the Open-Meteo hourly request
  horizon and deterministic chronological mapper contract change.
- No release, MVP/1.0-completeness, visual-verification, or unverified MET
  Norway-horizon claim.

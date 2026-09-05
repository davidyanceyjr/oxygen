# Active Cycle

Status: committed
Cycle ID: 2026-09-04-slice-20b-unit-conversion-presentation-boundary
Mode: feature
Slice: Slice 20B, Unit Conversion Presentation Boundary
Commit: committed in this changeset

## Objective

Convert canonical Home weather values into the selected 20A unit preference at
the presentation mapper boundary, while preserving the installed app's current
formatted output until Slice 20C supplies persisted user choice.

## Scope

Production changes are limited to:

- `app/src/main/kotlin/com/oxygen/weather/app/HomeForecastPresentationMapper.kt`.

Focused tests are limited to:

- `app/src/test/kotlin/com/oxygen/weather/app/HomeForecastPresentationMapperTest.kt`;
- `app/src/test/kotlin/com/oxygen/weather/app/HomeForecastStateHolderTest.kt`
  only for default-path regression coverage that cannot live in the mapper test.

Cycle records are limited to this plan and the appended/updated completed-cycle
history record when the slice is ready.

## Contract

- `WeatherBundle` remains canonical: Celsius, meters per second, hPa,
  millimeters, and meters are neither mutated nor copied into display storage.
- `toHomeSuccessPresentation(...)` accepts a `UnitPreference` (or its resolved
  equivalent) at its presentation boundary. The installed app continues to use
  an explicit app-local compatibility default matching its present output:
  Fahrenheit, km/h, hPa, mm, and km. For that compatibility default,
  visibility below one kilometer remains whole meters; visibility at or above
  one kilometer is one-decimal km. Miles are always one-decimal miles.
- Every displayed temperature uses the selected temperature unit: current,
  apparent, hero high/low, hourly, daily, apparent-temperature metric, and dew
  point metric.
- Displayed wind speed and gust use the selected wind-speed unit. Direction
  remains a degree-bearing direction value; it is never converted as speed,
  inferred from a formatted string, or dropped when it is the only available
  wind datum.
- Displayed pressure, visibility, precipitation metric, and near-term
  precipitation total use their corresponding selected units. Percentages,
  timestamps, condition identities, data-type labels, source, license, and
  provenance stay unchanged.
- Gate 20-0 semantic presentation values remain canonical and unchanged:
  `temperatureC`, `highC`, `lowC`, `HomeMetricNumericValues`, wind-direction
  degrees, and percentage fields. Slice 20B changes only the display text.
- Null canonical values remain null in semantic fields and retain existing
  unavailable presentation. Conversion never substitutes zero or infers a
  value from display text.
- Rounding is centralized, deterministic, and performed once after conversion
  with `RoundingMode.HALF_UP`: whole temperature and wind values; whole
  hPa/mmHg and compatibility meters; two-decimal inHg; one-decimal mm/km/mi;
  and two-decimal inches. Do not use device locale as an implicit numeric-format
  policy.
- Near-term precipitation totals are summed in canonical millimeters, then
  converted and rounded once in the selected precipitation unit.
- No Composable consumes or parses a display string to obtain weather data.

## Acceptance Evidence

Focused mapper tests must prove:

- Metric, US, and UK preferences produce the expected values and symbols
  across every displayed category. Custom coverage explicitly exercises knots,
  mmHg, inches, and miles, with separate Custom calls covering m/s and inHg.
- The default mapper call produces the exact current Home strings, so the
  installed app does not change before persisted units UI exists.
- Canonical `WeatherBundle` and Gate 20-0 presentation semantic values,
  source/provenance presentation, condition and metric identities, and section
  ordering are unchanged after mapping under every preference.
- Null current/hourly/daily/metric values remain unavailable/null rather than
  zero, including null speed, gust, direction, pressure, visibility, and
  precipitation cases.
- Boundary values cover negative and positive half-way rounding, zero,
  sub-unit visibility/precipitation, large values, and wind direction with and
  without a speed or gust. Near-term precipitation proves aggregate-then-
  convert-and-round behavior.

The installed default-path regression boundary is:
`WeatherBundle -> OxygenAppStateHolder -> HomeSuccessPresentation -> existing
Compose Home`. Mapper tests are the acceptance boundary for alternate units in
this slice because Slice 20C has not made preference selection reachable in the
installed app. Slice 20C must exercise a persisted preference through that
installed path.

## Workflow

1. Baseline the existing presentation and canonical-provider boundaries:

   ```bash
   . scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecastPresentationMapperTest' --tests '*HomeForecastStateHolderTest'
   . scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*UnitPreferenceTest' --tests '*OpenMeteoForecastMapperTest' --tests '*MetNoForecastMapperTest'
   ```

2. Add failing mapper tests for all resolved 20A preference families, the
   explicit Custom matrix, default compatibility output (including sub-km
   meters), `HALF_UP` rounding edges, aggregate precipitation, null
   preservation, direction semantics, canonical immutability, and
   source/provenance preservation.

3. Implement one private presentation conversion/formatting boundary in
   `HomeForecastPresentationMapper.kt`, reusing `:core` unit preference types.
   Thread the resolved preference through existing mapper helpers; do not add
   display-unit fields to core domain models or persistence.

4. Run the focused mapper and Home-state tests to green. Inspect the Home UI
   consumers to confirm they render mapper text and continue grouping by
   `HomeMetricIdentity`, never by a label or parsed value.

5. Run the installed default-path regression boundary. It proves the
   production mapper/state-holder/Compose path retains the compatibility
   output; it does not prove alternate-unit selection, which remains Slice
   20C work:

   ```bash
   scripts/start-emulator.sh
   . scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest
   scripts/install-debug.sh
   ```

   Save the connected-test log and install evidence under
   `.codex/test-artifacts/2026-09-04-slice-20b-unit-conversion-presentation-boundary/`.
   A screenshot is optional regression evidence, not proof of alternate-unit
   conversion, because the default installed output remains unchanged.

6. Run broad verification:

   ```bash
   . scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
   . scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
   . scripts/android-env.sh && ./gradlew :app:assembleDebug
   git diff --check
   ```

## Result

- Implemented the selected-unit presentation boundary in
  `HomeForecastPresentationMapper.kt` using resolved 20A preferences.
- Preserved the explicit compatibility default: Fahrenheit, km/h, hPa, mm,
  and km, including whole-meter visibility below one kilometer.
- Centralized deterministic `HALF_UP` conversion/rounding for temperatures,
  wind, pressure, precipitation, and visibility while retaining canonical
  semantic fields, condition identities, metric identities, source, and
  provenance.
- Added mapper coverage for Metric, US, UK, Custom, null/direction cases,
  aggregate precipitation, boundary rounding, and canonical immutability.

## Verification Evidence

- Baseline focused app and core tests passed before implementation.
- Focused app mapper/state tests passed after implementation, including the
  mapper and state-holder suites.
- Installed `HomeDashboardUiTest` passed all 33 tests on `oxygen_starter`.
- `scripts/install-debug.sh` successfully installed and launched the debug app.
- Broad checks passed: `:app:compileDebugKotlin`,
  `:app:testDebugUnitTest :core:testDebugUnitTest`, `:app:assembleDebug`, and
  `git diff --check`.
- UI consumer review and connected/install/build logs are saved under
  `.codex/test-artifacts/2026-09-04-slice-20b-unit-conversion-presentation-boundary/`.

## Remaining Boundary

- The slice is verified and committed in this changeset. Persisted preference selection and
  installed alternate-unit reachability remain Slice 20C.

## Out of Scope

Persisted unit preferences, Settings or Home unit controls, provider request
units, provider mappings, repositories, Room/DataStore/cache schemas,
location behavior, alerts, air quality, radar/maps, appearance settings,
widgets, notifications, translations beyond directly touched strings, release
readiness, and MVP-readiness claims.

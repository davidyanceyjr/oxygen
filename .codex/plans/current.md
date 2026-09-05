# Active Cycle

Status: committed
Cycle ID: 2026-09-04-gate-20-0-presentation-semantics-localization-safety
Mode: gate
Slice: Gate 20-0, Presentation Semantics and Localization Safety
Commit: committed in this changeset

## Objective

Establish a semantic presentation boundary before Slice 20B expands unit
conversion. Home presentation must use semantic identities and numeric values,
not parse English labels or formatted strings.

## Scope

Inspect and, only where required, update:

- `app/src/main/kotlin/com/oxygen/weather/app/HomeForecastPresentationMapper.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt`
- directly affected `app/src/main/res/values/strings.xml` entries;
- `app/src/test/kotlin/com/oxygen/weather/app/HomeForecastStateHolderTest.kt`;
- new `app/src/test/kotlin/com/oxygen/weather/app/HomeForecastPresentationMapperTest.kt`
  if the existing state tests cannot express the contract cleanly.

Preserve existing Home display output and current Fahrenheit, km/h, visibility,
and precipitation conversion formulas. Do not refactor or expand conversion;
Slice 20B owns conversion behavior.

## Contract

- `HomeMetricIdentity` remains the source of metric grouping and ordering;
  grouping must not depend on localized labels.
- Condition identity remains `WeatherCondition`, including icon and
  accessibility paths.
- Presentation models expose only the nullable numeric values needed for
  future conversion, visualization, or accessibility, alongside display text;
  each such value has an explicit semantic unit/identity.
- Semantic numeric null remains null. Existing user-facing fallback text such
  as `"Unavailable"` may remain display-only and must not become numeric data.
- Composables do not parse temperature, percentage, pressure, distance,
  precipitation, or wind strings back into numbers.
- Provider DTOs do not appear in presentation models or Composable parameters.
- Resource migration is limited to strings directly touched by a required fix;
  no broad localization cleanup is included.

## Acceptance Evidence

Focused tests must prove:

- changing a metric label does not change grouping or identity;
- required numeric presentation values are present, correctly nullable, and
  independent of their formatted text;
- condition/icon identity remains semantic;
- missing current, hourly, daily, metric, and accessibility values remain
  absent rather than becoming zero or inferred values;
- existing formatted Home output remains unchanged.

Static review must prove:

- no display string is parsed into a numeric value;
- `HomeMetricIdentity` and `WeatherCondition` are used for semantic paths;
- provider DTOs do not cross into presentation/UI;
- `WeatherBundle` and canonical domain units are unchanged;
- no persistence, provider request, cache, or unit-conversion scope drift.

## Workflow

1. Baseline the existing Home boundary:

   ```bash
   . scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecastStateHolderTest'
   ```

2. Inventory all `HomeMetricPresentation`, formatted numeric fields,
   `contentDescription`, grouping, and conditional-section consumers with
   `rg`. Decide the minimum semantic numeric fields required; do not redesign
   the presentation model speculatively.

3. Add or update the focused mapper/state tests, then make the smallest
   production change needed at the mapper boundary. Keep display formatting
   and existing conversion output stable.

4. Run focused green tests, including the new mapper test if added, and rerun
   the Home state test.

5. Run the static parsing review:

   ```bash
   rg -n 'toDouble\(|toFloat\(|parse.*(temperature|pressure|wind|precip|visibility)|split\(|substring\(' app/src/main
   ```

   Inspect every match; no display-to-number parsing may remain.

6. If production or accessibility code changes, run the connected Home test
   and installed-app exercise:

   ```bash
   . scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest
   scripts/install-debug.sh
   ```

   Capture the affected UI evidence under
   `.codex/test-artifacts/2026-09-04-gate-20-0-presentation-semantics-localization-safety/`.
   If only tests/static review change, record why installed behavior is
   unchanged and no real-path exercise is required.

7. Run broad checks:

   ```bash
   . scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
   . scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
   . scripts/android-env.sh && ./gradlew :app:assembleDebug
   git diff --check
   ```

## Out of Scope

Unit conversion math or formula changes, persisted unit preferences, Settings
controls, provider requests, Room/DataStore/cache changes, location flow,
alerts, air quality, radar/maps, appearance settings, widgets, notifications,
release readiness, and MVP-readiness claims.

## Verification

- Focused mapper and Home-state unit checks passed:
  `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecastPresentationMapperTest' --tests '*HomeForecastStateHolderTest'`.
- Static parsing review found no display-string-to-number parsing. The only
  matches were procedural weather-mark geometry conversions and location-name
  display splitting, neither of which consumes formatted weather values.
- Connected `HomeDashboardUiTest` passed all 33 tests on `oxygen_starter`,
  including the changed-metric-label grouping and rendered semantics boundary.
- `scripts/install-debug.sh` launched the installed app on `oxygen_starter`.
  The launch screenshot, test logs, and parsing review are in
  `.codex/test-artifacts/2026-09-04-gate-20-0-presentation-semantics-localization-safety/`.
- Broad checks passed: `:app:compileDebugKotlin`, app/core debug unit tests,
  `:app:assembleDebug`, and `git diff --check`.

# Active Cycle

Status: committed
Cycle ID: 2026-09-04-slice-20a-unit-preference-contract
Mode: feature
Slice: Slice 20A, Unit Preference Contract
Commit: committed in this changeset

Goal: Implement the provider-neutral unit preference contract required before
unit conversion or persisted units UI work, without changing canonical weather
storage, provider requests, Home presentation values, or installed app behavior.

Basis:
- Gate 19F is committed at `8386484`, with post-commit status correction at
  `4a58c96`.
- `.codex/plans/mvp-roadmap.md` names Slice 20A as the next candidate after
  Gate 19F and scopes it to defining unit preferences before conversion/UI.
- Roadmap prerequisite "small-state persistence foundation" is satisfied by
  saved-location and selected-location persistence committed through Slice 19E
  at `00cb88a`; this slice must not add new persistence.
- `docs/OXYGEN_FULL_SPECIFICATION.md` section 38 requires canonical internal
  values and presentation-only conversion.
- Current core weather models already store canonical units:
  `temperatureC`, `speedMetersPerSecond`, `pressureHpa`, `precipitationMm`,
  and `visibilityMeters`.
- Open-Meteo requests currently ask for Celsius, km/h, and mm; MET Norway
  mapping normalizes provider units into canonical domain values.

## Contract

Selected behavior:
- Add a provider-neutral unit preference model for temperature, wind speed,
  pressure, precipitation, and visibility.
- Define preset behavior for Metric, US, UK, and Custom.
- Document the Metric, US, and UK preset mappings in
  `docs/OXYGEN_FULL_SPECIFICATION.md` section 38 before relying on them in
  production code, because section 38 currently names presets without mapping
  their category defaults.
- Metric defaults:
  - temperature Celsius;
  - wind km/h;
  - pressure hPa;
  - precipitation mm;
  - visibility km.
- US defaults:
  - temperature Fahrenheit;
  - wind mph;
  - pressure inHg;
  - precipitation in;
  - visibility mi.
- UK defaults:
  - temperature Celsius;
  - wind mph;
  - pressure hPa;
  - precipitation mm;
  - visibility mi.
- Custom must carry explicit choices for every unit category and must not
  silently fall back per category.
- Preference resolution must be deterministic and testable without Android UI.
- Existing canonical weather model values and cache schema must remain
  unchanged.

Acceptance boundary:
- Production changes are allowed only for the unit preference contract model
  and small pure resolution helpers in `:core`, unless discovery finds a
  higher-authority conflict.
- Focused unit tests must prove:
  - every preset resolves all five unit categories exactly;
  - Custom preserves explicit category choices;
  - resolving Metric, US, UK, and Custom preferences for a representative
    `WeatherBundle` leaves canonical domain values unchanged, including
    `temperatureC`, `speedMetersPerSecond`, `pressureHpa`, `precipitationMm`,
    `visibilityMeters`, hourly temperatures/precipitation, and daily highs/lows;
  - canonical weather storage remains Celsius, meters per second, hPa,
    millimeters, and meters, without adding parallel display-unit fields to
    `WeatherBundle`.
- Static/diff review must prove the new contract has no imports or dependencies
  on provider DTOs, provider clients, Room cache entities, DataStore, Android UI,
  or Home formatted strings.
- Documentation/status changes are allowed only when needed to keep the active
  cycle and roadmap/history truthful.

Out of scope:
- Unit conversion math.
- Persisted unit preference storage.
- Settings or Home unit selection UI.
- Home presentation format changes.
- Provider request unit changes.
- Room schema, DataStore format, forecast-cache format, or saved-location
  storage changes.
- Device-location permission flow, alerts, air quality, radar/maps,
  appearance settings, widgets, background refresh, notifications, release
  readiness, or MVP-readiness claims.

## Workflow

Discover:
- Read required authorities and inspect current unit/canonical weather model
  boundaries.
- Confirm `:core` package placement for provider-neutral unit preference types,
  or stop on any higher-authority conflict.

Red/Baseline:
- Run focused baseline tests around current provider canonical units:
  `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*OpenMeteoForecastClientTest' --tests '*OpenMeteoForecastMapperTest' --tests '*MetNoForecastMapperTest'`.
- Add focused unit tests for the Slice 20A contract and confirm they fail before
  production implementation.

Build:
- Add unit preference enums/value types and preset/custom resolution helpers.
- Keep implementation pure Kotlin with no Android UI, Room, DataStore, provider
  request, or presentation formatting changes.

Focused Green:
- Run the focused Slice 20A unit tests and the canonical-provider baseline
  tests.

Real-Path Exercise:
- Not applicable for this pure provider-neutral contract slice because it must
  not change installed-app behavior; behavior preservation is checked through
  focused tests, broad checks, and static/diff review.

Broad Checks:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`

Review:
- Inspect `git diff --stat` and `git diff` for accidental provider, cache,
  DataStore, Room, UI, or documentation claim drift.
- Treat any empty or pre-existing artifact file as non-evidence; replace it only
  with output from a command actually run during this cycle.
- Save command logs under
  `.codex/test-artifacts/2026-09-04-slice-20a-unit-preference-contract/`.

## Phase Results

- specified: Slice 20A is specified by the MVP roadmap and specification unit
  section.
- planned: Bounded to provider-neutral unit preference contract behavior.
- covered: `UnitPreferenceTest` proves Metric, US, and UK preset resolution,
  Custom explicit-choice preservation, unchanged canonical `WeatherBundle`
  values after preference resolution, and absence of parallel display-unit
  fields on `WeatherBundle`.
- implemented: Added pure provider-neutral unit preference contract types and
  resolution helpers in `:core`; documented preset mappings in specification
  section 38.
- verified: Provider canonical baseline passed with
  `:core:testDebugUnitTest --tests '*OpenMeteoForecastClientTest' --tests
  '*OpenMeteoForecastMapperTest' --tests '*MetNoForecastMapperTest'`.
- verified: Focused Slice 20A test passed with
  `:core:testDebugUnitTest --tests '*UnitPreferenceTest'`.
- verified: Broad checks passed:
  `:app:compileDebugKotlin`,
  `:app:testDebugUnitTest :core:testDebugUnitTest`,
  `:app:assembleDebug`, and `git diff --check`.
- verified: Static review found unit preference symbols only under
  `core.model`; no app, provider, Room, DataStore, cache, Home formatting, or
  provider request path adopted unit preferences.
- verified: Real-path exercise is not applicable because this pure contract
  slice intentionally does not change installed-app behavior.
- artifacts: `.codex/test-artifacts/2026-09-04-slice-20a-unit-preference-contract/`.
- committed: Slice 20A is committed in this changeset.

Skipped commands:
- Emulator, install, connected Android tests, and screenshot capture were not
  run because this slice changes no installed UI or runtime behavior.

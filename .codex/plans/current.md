# Oxygen Standard Now — CD-04 Precipitation and Wind Satellites

**Status:** verified; commit pending
**Cycle ID:** `2026-09-18-cd-04-precipitation-wind-satellites`
**Active roadmap:** `.codex/plans/ui-roadmap.md`
**Implementation-slice count:** 1 of 2 since CD-00

## Selected behavior and acceptance boundary

Complete the normal-font Standard Now Celestial Dial with two lower, non-action
satellites below the unchanged 150dp current dial:

- a **near-term forecast precipitation** satellite derived from the existing
  first-six-hour hourly precipitation path; and
- a **current wind** satellite derived from the existing
  `CurrentConditions.wind` path.

This corrects the prior draft's mismatch with CD-04: it must not relabel
`CurrentConditions.precipitationMm` as near-term precipitation. The mapper
will expose a small typed app-presentation model for these two satellites. It
owns the compact display string, full accessibility description, and canonical
numeric inputs; the renderer consumes those fields and never parses existing
summary or metric strings. Near-term precipitation remains a forecast/model
value, never an observation or “actual” measurement.

The sole production boundary is this mapper-to-normal-font-Standard-Now
rendering path. The acceptance boundary is an installed Standard Now route at
360x640dp, font scale 1.0, LTR, Oxygen theme, Standard layout, Effects Off,
with a retained cache-backed same-route before/final PNG/XML pair and two named
focused connected tests.

Completion requires all of the following:

- when both values are available, a 64dp lower precipitation satellite and
  64dp lower wind satellite appear beneath the dial, are symmetric about the
  360dp centerline, do not overlap the dial or their siblings, and are plainly
  visible in the installed final capture;
- the precipitation satellite presents the existing up-to-six-hour forecast
  amount when any hourly amounts exist, converting only after the canonical
  millimetre total is formed; if amounts are absent but probabilities exist, it
  instead presents the existing maximum forecast probability and says so in
  its mapper-owned semantic text;
- the wind satellite presents speed when available, otherwise a truthful
  gust-only or direction-only compact value. Its mapper-owned semantic text
  includes every available speed, gust, and direction component using the
  resolved unit. A `Wind` object with no available component has no
  satellite;
- absent hourly precipitation and absent/displayless wind omit only their own
  satellite. A sole satellite is centered in the same lower row; no satellite
  shows `Unavailable`, a fabricated zero, or another substitute. A reported
  0 mm/in forecast amount is retained as a real zero;
- the existing `home-current-summary` content description remains exactly its
  current condition/temperature/apparent/high/low meaning. The lower
  constellation supplies one ordered merged semantic description
  (precipitation before wind), with decorative circles and child text hidden
  from accessibility;
- the high/low constellation, selected location, near-term summary section,
  source/update/provenance, alert outcomes, pager, Back, refresh, request
  behavior, cache/persistence, and all non-normal-font/Simple pages retain
  their current behavior; and
- the focused unit and connected results have zero failures, errors, and
  skips; the prescribed broad checks and `git diff --check` pass.

## Implementation contract

1. In `HomeForecastPresentationMapper.kt`, replace the string-only
   near-term precipitation intermediate with a typed internal/presentation
   value shared by the existing `precipitationSummary` and the new current
   satellite field. It carries the canonical hourly-amount aggregate (when
   present), maximum probability (when present), resolved compact display
   value, and forecast-specific accessibility description. Preserve the
   existing summary wording and all existing section-order behavior.
2. Add typed precipitation and wind satellite fields to
   `HomeCurrentPresentation`, nullable only when that measurement has no
   truthful display. Preserve raw precipitation millimetres and all three raw
   wind components in those presentation values for mapper-boundary tests.
   Reuse the existing resolved-unit conversions and rounding rules. Do not add
   or change `:core` models, provider DTOs/requests, repository interfaces,
   Room/DataStore schemas, cache content, or provider attribution.
3. In `CentralCurrentDial`, add a lower `home-current-lower-constellation`
   after the existing dial. Use a fixed 220dp-wide, 64dp-high row: both
   satellites occupy its ends; one is centered; neither omits the row. Give
   the satellites stable tags
   `home-current-precipitation-satellite` and
   `home-current-wind-satellite`. Use the existing glass/outline and
   precipitation/design roles; add a named home-design token only if a value
   is reused outside this composable. The composable may lay out typed text
   only; it may not convert, infer, aggregate, or reword weather data.
4. Retain the existing upper 52dp high/low constellation, 150dp dial,
   `home-current-summary` semantics, normal supporting-content order, and
   Effects-Off opaque/static treatment. This slice adds no interactive control,
   so it adds no 48dp action target.

## Tests and evidence

Add only these focused tests.

- `HomeForecastPresentationMapperTest`
  - `nearTermPrecipitationSatelliteUsesCanonicalAggregateAndResolvedUnits`:
    verify metric and US compact/semantic output, aggregate-before-conversion,
    raw millimetre preservation, and a reported zero amount.
  - `currentWindSatelliteUsesResolvedUnitsAndPreservesAllComponents`: verify
    speed/gust/direction, direction-only, and no-component wind handling
    without reparsing text.
  - `currentSatelliteAvailabilityNeverFabricatesMissingValues`: cover
    precipitation-only, wind-only, and neither states; confirm the existing
    near-term summary and current spoken description retain their contract.

- `HomeDashboardUiTest` (exactly two focused connected cases)
  - `standardNowShowsPrecipitationAndWindSatellitesWithoutChangingCurrentSummary`:
    available fixture at 360x640dp / 1.0 / LTR / Oxygen / Standard / Off;
    assert tags, 64dp sizes, symmetry, ordering, exact merged semantics,
    unchanged summary, fixed-section/root bounds, and no sibling overlap.
  - `standardNowLowerSatelliteStatesOmitOnlyUnavailableValues`: dynamically
    exercise both available, precipitation-only, wind-only, and neither;
    assert centered sole-satellite geometry, absence of fabricated/placeholder
    text, retained high/low/dial semantics, and no overlap.

The connected cases are the complete two-case Android budget for this slice.
Provider/repository tests are inapplicable: no provider, repository, request,
or cache behavior changes. The real-path evidence instead exercises the
installed cached production presentation path.

## Evidence result

- The three named mapper tests passed with zero failures, errors, and skips;
  focused evidence is in `focused-unit.log`.
- The first connected attempt was a deterministic semantics-observability
  failure because `clearAndSetSemantics` hid child tags. After replacing it
  with a row-level semantic description and retaining child decorative hiding,
  both named connected cases passed 1/1 with zero failures, errors, and skips
  on API-37 `oxygen_starter`. Accepted bundles are in
  `connected-standard-rerun/` and `connected-states/`; the failed diagnostic
  bundle is retained in `connected-standard/`.
- The installed cache-backed Chicago route was captured before and after the
  APK change with networking disabled for the final launch. The final PNG
  visibly shows both 64dp lower satellites; the final XML retains the exact
  current summary and ordered merged lower semantics. Evidence is in
  `baseline/` and `final/`.
- `:app:compileDebugKotlin`, `:app:testDebugUnitTest
  :core:testDebugUnitTest`, `:app:assembleDebug`, and `git diff --check` passed.
  The command ledger is `verification-ledger.md` and the broad log is
  `broad-checks.log`.

Create and retain
`.codex/test-artifacts/2026-09-18-cd-04-precipitation-wind-satellites/`:

- `baseline/` and `final/` same-route PNG plus UI-automator XML;
- `focused-unit.log`, two per-method connected bundles (wrapper result,
  runner XML, instrumentation output, textproto, acceptance marker, and device
  diagnostics), and `broad-checks.log`;
- `verification-ledger.md`, listing each command, result, artifact, and any
  bounded rerun hypothesis.

Before code changes, start one emulator session, navigate the installed app to
a selected-location Standard Now cache whose existing response has both an
hourly precipitation value and displayable wind, and capture the baseline
PNG/XML using `scripts/capture-screen.sh <artifact>.png` and
`adb shell uiautomator dump /sdcard/cd04.xml && adb pull /sdcard/cd04.xml
<artifact>.xml`. If that truthful state cannot be obtained from the existing
selected cache, preserve the capture and stop to select a separate
evidence/state slice; do not fabricate a full constellation. After the changed
APK is built, install it once with `scripts/install-debug.sh`, render the same
selected location with networking disabled, and save the final PNG/XML by the
same commands. This proves the normal cached production path and cannot induce
a provider request. Inspect the final PNG/XML against the baseline before broad
checks. Preserve every failed acceptance attempt and apply the three-attempt
diagnostic rule.

Run, once each unless relevant source, input, or environment changes:

```sh
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecastPresentationMapperTest.nearTermPrecipitationSatelliteUsesCanonicalAggregateAndResolvedUnits' --tests '*HomeForecastPresentationMapperTest.currentWindSatelliteUsesResolvedUnitsAndPreservesAllComponents' --tests '*HomeForecastPresentationMapperTest.currentSatelliteAvailabilityNeverFabricatesMissingValues'
scripts/run-connected-method.sh --serial <serial> com.oxygen.weather.app.ui.home.HomeDashboardUiTest#standardNowShowsPrecipitationAndWindSatellitesWithoutChangingCurrentSummary
scripts/run-connected-method.sh --serial <serial> com.oxygen.weather.app.ui.home.HomeDashboardUiTest#standardNowLowerSatelliteStatesOmitOnlyUnavailableValues
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
scripts/install-debug.sh
git diff --check
```

## Required documentation closure

After evidence is accepted, update only factual tracking/documentation:

- `.codex/plans/current.md`: replace planned claims with actual status,
  command outcomes, artifact paths, limits, and commit state;
- `.codex/plans/ui-roadmap.md`: mark CD-04 with its evidenced status, retain
  CD-05 as `specified`, update the 1-of-2 implementation count, and select
  no next production slice;
- `.codex/cycles/history.md`: append one concise self-contained CD-04
  record with result, focused/broad evidence, artifacts, limitations, and
  commit state;
- `README.md`: add the verified installed lower precipitation/wind
  constellation behavior and evidence limits alongside the existing CD-01/CD-02
  installed-app statement.

The full/UI specifications and provider contracts need no edit: this slice
conforms to their existing forecast, unit, semantic, effects-off, and provider
boundaries. Do not change them merely to mirror implementation detail.

## Limits, next action, and sizing audit

Out of scope: `CurrentConditions.precipitationMm` as a lower satellite,
new core/provider/repository/request/cache/persistence behavior, metric-card or
near-term-summary redesign, charts, compass, humidity/pressure/Details metrics,
halo/atmosphere, lower glass panels, action/footer redesign, Simple Now,
compact/large-font/RTL responsive redesign, theme/high-contrast translation,
localization, TalkBack service traversal, and release verification.

Next action: no next production slice is selected. CD-05 remains `specified`;
the next roadmap checkpoint is selected only after the separate implementation
slice count reaches its documented boundary.

Sizing audit: **45% of one context window**, including discovery, the single
typed mapping/rendering concern, two connected cases, installed evidence,
broad checks, review, and documentation closure. This is below the user's 60%
split threshold, so no >60% warning applies. It is deliberately capped at the
roadmap maximum; split before coding if unavailable live evidence, a new
provider/domain contract, responsive work, or a third connected case is needed.

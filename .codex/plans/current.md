# Slice 30A1 — Home Spoken-Weather Semantics

**Status:** committed at `da7b886`
**Cycle ID:** `2026-09-09-slice-30a1-home-spoken-weather-semantics`
**Mode:** bounded Home presentation/accessibility implementation

**Basis:** Home already exposes named page actions and visible current, hourly,
and daily weather, but the spoken contract is incomplete: the current mark
announces only the condition, while hourly/daily descriptions are assembled in
Compose from display strings and leave redundant child semantics exposed. This
slice moves that one concern to the existing presentation boundary. Later Home
layout, alert, Appearance, and service-level TalkBack work is split into the
ordered Gate 30 queue in the roadmap.

**Next action:** select Slice 30A2 as a new bounded plan; do not claim its
compact/large-font resilience or any later Gate 30 condition until exercised.

## Behavior and acceptance boundary

The production Home success path exposes one concise spoken description for the
current-weather summary and one for each rendered hourly and daily forecast
item. Descriptions come from provider-neutral values while their meaning and
resolved temperature unit are known; Compose does not parse visible labels or
reconstruct weather meaning.

Exact English contract for this non-localization slice:

- current: condition and current temperature, followed by available feels-like,
  high, and low values;
- hourly: local time, condition, temperature, and available precipitation
  probability;
- daily: local date, condition, available high/low values, and available
  precipitation probability;
- temperatures use `degree/degrees Celsius` or `degree/degrees Fahrenheit`, and
  probability uses `percent chance of precipitation`;
- a missing current/hourly temperature is announced as `Temperature
  unavailable`; missing optional facts are omitted; a daily row with neither a
  high nor low announces `High and low unavailable`; no missing value becomes
  zero.

For the committed fixture, exact examples are `Rain showers. 65 degrees
Fahrenheit. Feels like 63 degrees Fahrenheit. High 73 degrees Fahrenheit. Low
54 degrees Fahrenheit.`, `6 AM. Rain. 64 degrees Fahrenheit. 60 percent chance
of precipitation.`, and `Sat, Aug 22. Rain showers. High 73 degrees Fahrenheit.
Low 54 degrees Fahrenheit. 40 percent chance of precipitation.` Use those
sentence boundaries and fact order. Visible `deg C` / `deg F`, `Precip n/a`,
canonical nullable values, condition identity, and page content remain
unchanged.

Primary acceptance boundary: mapper tests prove exact Fahrenheit/Celsius and
missing-value meaning; the merged Compose tree exposes exactly one description
per current/hourly/daily item while the unmerged tree retains visual test tags;
and the installed 360x640 dp, font-scale-1.3, Effects-Off Home hierarchy exposes
the same production descriptions on Now, Hourly, and Daily.

## Implementation contract

1. Append a required trailing `spokenDescription` field to
   `HomeCurrentPresentation`, `HomeHourlyPresentation`, and
   `HomeDailyPresentation`. Discovery found their only construction sites in
   `HomeForecastPresentationMapper.kt`, so update those owned constructors and
   do not add an empty compatibility default or reorder existing fields.
2. Build each description in the mapper from canonical nullable temperatures,
   `WeatherCondition`, local time/date, precipitation probability, and the
   resolved `TemperatureUnit`. Share the existing conversion/round-half-up rule
   so spoken and visible numbers cannot diverge; add speech-specific unit wording
   instead of transforming `deg C` / `deg F` output.
3. Keep `home-current-mark` as the stable current-summary semantics and bitmap
   boundary. Give it the complete current description and hide only the
   redundant visible condition, temperature, feels-like, and Today high/low
   semantics with `hideFromAccessibility()`. Humidity, wind, update/source text,
   alerts, actions, and other Now content must remain independently reachable.
   Pass the Today range's redundant-semantic identity explicitly to the local
   UI item; do not infer it by comparing the rendered `Today` label.
4. Replace the hand-built hourly/daily card descriptions with each mapper-owned
   `spokenDescription`. Use `clearAndSetSemantics` on the complete forecast card
   so its covered descendants are not announced again. Preserve the existing
   row tags and visible descendants in the unmerged test tree; never clear
   semantics from content not represented by the replacement description.
5. Preserve Standard and Simple page identity/selection, named previous/next
   actions, visible controls, callbacks, chronology, layout geometry, RTL
   primitives, theme/contrast/effects rendering, and accessibility overflow.
   Do not reconstruct `OxygenAppStateHolder`, write preferences, or issue any
   forecast, alert, location, or geocoding request.

## Intended files and limits

Production:

- `app/src/main/kotlin/com/oxygen/weather/app/HomeForecastPresentationMapper.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt`

Tests:

- `app/src/test/kotlin/com/oxygen/weather/app/HomeForecastPresentationMapperTest.kt`
- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`

Extend the existing Home connected class because it owns the real compact
fixture, request-count fake, screenshot helpers, and weather-mark assertions.
Do not create a parallel accessibility fixture or production-only test screen.

Do not change `:core`, provider/repository/cache/storage models, preference
state, Settings, alert presentation, theme roles, weather-mark drawing,
navigation, manifests, dependencies, resources, or localization. Artifacts and
the verification ledger belong under
`.codex/test-artifacts/2026-09-09-slice-30a1-home-spoken-weather-semantics/` and
remain out of source control.

## Focused tests and real-path evidence

1. Before production edits, use the committed APK on one pinned emulator. Reach
   Home through the normal installed selected-location path (live or truthful
   cached weather; no sample bundle or app-private-state seeding), set Effects
   Off through Settings, configure 360x640 dp and font scale 1.3, and retain
   Now/Hourly/Daily screenshots plus UI hierarchies. Record commit, serial/AVD,
   physical and logical viewport, density, font scale, layout direction,
   animation scales, theme/contrast/layout/effects/units, commands, and any
   provider/cache state. Restore platform settings after the cycle.
2. Add two mapper tests before production code and retain the expected red log:
   exact current/hourly/daily descriptions for Fahrenheit and Celsius, and
   null handling for required temperature, partial/absent daily ranges, and
   absent precipitation. Also assert canonical values and condition identities
   remain unchanged.
3. Add one current-summary case to `HomeDashboardUiTest`; update the existing
   compact hourly and compact daily cases to the new exact descriptions and
   one-node merged-tree counts. Assert redundant condition-mark descriptions are
   absent from the merged tree, while visible text and `home-current-mark` /
   row tags remain available in the unmerged tree for layout/bitmap checks.
4. Extend the existing installed-path unit-selection/no-refetch case with the
   changed Celsius spoken description and retain its canonical fixture and
   single repository-request assertions. In the current-summary case, retain
   the existing named next-page action. Do not rerun the whole historical Home
   class.
5. After the final APK change, install once and repeat the installed
   Now/Hourly/Daily journey in the same recorded environment. Retain screenshots
   as presentation evidence and UI hierarchies as Android-node evidence; compare
   descriptions and page controls with the baseline. This does not prove spoken
   pronunciation, focus order, or service traversal—Gate 30E owns TalkBack.

## Verification budget and commands

Budget: one emulator session, one baseline/final installed journey, one mapper
red/green cycle, four connected cases, and one broad pass. Do not rerun a pass
unless relevant production code, test input, or environment changed. Stop after
one bounded platform timeout and record the gap.

Planned focused filters (use the final method names recorded by the red run):

```sh
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest \
  --tests 'com.oxygen.weather.app.HomeForecastPresentationMapperTest.spokenDescriptionsUseResolvedTemperatureUnits' \
  --tests 'com.oxygen.weather.app.HomeForecastPresentationMapperTest.spokenDescriptionsHandleMissingValuesWithoutInventingZero'
. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest#currentWeatherExposesOneCompleteSpokenSummaryWithoutRedundantChildren,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#compactHourlyPageShowsFourChronologicalEntriesWithHonestPrecipitation,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#compactDailyPageShowsFourChronologicalEntriesWithHonestPrecipitation,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#oxygenAppUnitsSelectionReturnsHomeWithAlternateUnitsAndKeepsPagesReachable'
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
scripts/install-debug.sh
git diff --check
```

The ledger must distinguish setup, red evidence, focused green, connected cases,
installed inspection, and broad checks. A screenshot, source-text assertion,
symbol-existence test, or build alone is not spoken-semantics evidence.

## Completion record

- Implementation commit: `da7b886`.
- Environment: `oxygen_starter` / `emulator-5554`; physical 1080x2400,
  logical override 360x640, physical density 420, logical density 160, font
  scale 1.3, LTR, animation scales 0/0/0, Oxygen theme, High contrast,
  Standard layout, Effects Off, Fahrenheit.
- Baseline setup used the committed APK after `:app:assembleDebug`; the normal
  installed path manually searched Chicago, saved it, selected the saved row,
  changed Effects Off and Standard through Settings, and captured Now/Hourly/
  Daily screenshots and hierarchies before edits.
- Red: both mapper tests failed at the missing `spokenDescription` contract in
  `mapper-red-final.log`. Green: both named mapper tests passed. Connected:
  the four named cases passed, with the hourly case passing in its named rerun
  after removing an ambiguous duplicate unmerged-text assertion. Final
  `scripts/install-debug.sh` plus the same normal Chicago selection path
  exposed mapper-owned descriptions on installed Now/Hourly/Daily.
- Broad: `:app:compileDebugKotlin`, `:app:testDebugUnitTest
  :core:testDebugUnitTest`, final `:app:assembleDebug` through
  `scripts/install-debug.sh`, and `git diff --check` passed. Artifacts are under
  `.codex/test-artifacts/2026-09-09-slice-30a1-home-spoken-weather-semantics/`.
- Skips/boundaries: no bounded platform timeout occurred; full Home-class
  rerun, TalkBack traversal/pronunciation, RTL, large-font resilience matrix,
  and later Gate 30 conditions were not run and remain out of scope.

## Post-commit authority sync

The separate authority sync updates README, specification sections 31.4–31.6,
37, 46, and 53, the roadmap, this plan, and live cycle history with the actual
`da7b886` implementation and retained evidence. Provider, privacy, license,
attribution, and data-source documents require no change. The sync is reviewed
with `git diff --check` and committed separately from the implementation.

## Out of scope and ready criteria

Out of scope: Home large-font/RTL/reduced-motion/theme matrix work; alert and
Appearance accessibility; service-level TalkBack; automatic system contrast;
visual redesign; localization/resource migration; custom units; provider,
domain, cache, location, persistence, background, notification, release, or MVP
work.

30A1 is ready only when mapper-owned descriptions exist on the production path,
the two mapper and four connected cases pass, merged/unmerged semantics retain
the intended accessibility and visual boundaries, the installed hierarchy shows
the descriptions without redundant decorative nodes, broad checks are recorded,
and the post-commit authority sync is committed. Later Gate 30 conditions remain
specified, not implied complete.

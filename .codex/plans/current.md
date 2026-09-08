# Slice 27A — Simple Layout Definition

**Status:** committed
**Cycle ID:** `2026-09-07-slice-27a-simple-layout-definition`
**Planning basis:** clean local `main` at merge `6fa6663`; Slice 26 behavior is
committed at `c7b578a`; the Slice 27A authority sync is committed at `6f3acfa`;
Slice 27A implementation is committed at `660e376`.
**Next action:** plan Slice 27B, Persisted Layout Selection, as a separate
bounded slice before adding layout storage or restart restoration.

## Completion evidence

Committed implementation:

- `660e376` — session-only Simple layout through Settings / Appearance.

Changed production files:

- `app/src/main/kotlin/com/oxygen/weather/app/OxygenApp.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/settings/SettingsScreen.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/theme/OxygenAppearance.kt`

Changed test file:

- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`

Verified behavior:

- The installed Settings / Appearance surface exposes session-only Simple and
  Standard layout controls. Selecting Simple uses the production app state and
  Home path, not a preview or test-only route.
- Simple renders `Now -> Forecast`; Forecast exposes Hourly and Daily choices.
  Switching layout or forecast choice does not trigger additional forecast,
  alert, location, or geocoding requests.
- Standard remains the launch/restart default and keeps the existing
  `Now -> Hourly -> Daily -> Details` behavior.
- Source/update/provenance, stale/cache failure context, official alert
  summary/detail/source reachability, missing-data honesty, page semantics,
  selected states, custom accessibility page actions, effects-off rendering,
  compact 360dp × 640dp layout, and font scale 1.3 readability were exercised
  at the connected Compose boundary.

Retained artifacts:

- `.codex/test-artifacts/2026-09-07-slice-27a-simple-layout-definition/baseline-installed-no-ready.png`
- `.codex/test-artifacts/2026-09-07-slice-27a-simple-layout-definition/baseline-installed-no-ready-hierarchy.xml`
- `.codex/test-artifacts/2026-09-07-slice-27a-simple-layout-definition/simple-now.png`
- `.codex/test-artifacts/2026-09-07-slice-27a-simple-layout-definition/simple-forecast-hourly.png`
- `.codex/test-artifacts/2026-09-07-slice-27a-simple-layout-definition/simple-forecast-daily.png`
- `.codex/test-artifacts/2026-09-07-slice-27a-simple-layout-definition/simple-now-semantics.txt`
- `.codex/test-artifacts/2026-09-07-slice-27a-simple-layout-definition/simple-forecast-hourly-semantics.txt`
- `.codex/test-artifacts/2026-09-07-slice-27a-simple-layout-definition/simple-forecast-daily-semantics.txt`
- `.codex/test-artifacts/2026-09-07-slice-27a-simple-layout-definition/ledger.md`

Verification commands run on the final implementation revision:

```sh
. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class='com.oxygen.weather.app.ui.home.HomeDashboardUiTest#simpleLayoutIsReachableAndPreservesRequiredMeaning,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#simpleLayoutReplacementResetsPagesAndPreservesReadyState,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#simpleSparseAndOperationalStatesRemainHonest,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#simpleLayoutIsReadableCompactLargeFontEffectsOff,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#freshSuccessRendersSemanticHomePagesAndPreservesDashboardContent,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#homePagerExposesNamedAccessibilityPageMovementActions'
. scripts/android-env.sh && ./gradlew :app:installDebug :app:installDebugAndroidTest
. scripts/android-env.sh && adb shell am instrument -w -e class com.oxygen.weather.app.ui.home.HomeDashboardUiTest#simpleLayoutIsReadableCompactLargeFontEffectsOff com.oxygen.weather.test/androidx.test.runner.AndroidJUnitRunner
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

All listed final implementation checks passed. The connected focused filter
ran six test cases with zero failures and zero skips.

Skipped/unverified:

- The installed baseline did not reach a ready forecast state, so only the
  truthful first-run/no-ready installed baseline is retained.
- TalkBack service-level traversal was not run because TalkBack was installed
  but disabled (`accessibility_enabled=0`,
  `enabled_accessibility_services=null`). Gate 30 traversal remains unverified.
  This slice verified semantics-tree order, labels, selected state, custom
  actions, and target sizes.

## Selected behavior and stopping boundary

Implement one usable production Home path for `LayoutPreset.SIMPLE` while
keeping `LayoutPreset.STANDARD` as the launch and restart default.

Simple has two primary semantic pages:

```text
Now -> Forecast
```

Forecast exposes visible, labelled `Hourly` and `Daily` choices. The choice is
local to the current Simple composition, starts at Hourly, survives movement
between Now and Forecast, and resets when the effective layout changes.
Primary page tabs, horizontal swipe, named accessibility actions, page
position, and the child forecast choice must remain observable and accessible.

### Reachability resolution

The roadmap says to define Simple before persisted selection. To satisfy the
vertical-slice and usable-app requirements without importing 27B persistence,
this slice adds a session-only layout control to the existing Appearance
destination. It is a real installed-app path: a user can choose Simple, return
to Home, and use the two-page layout. The session choice is in memory only;
launch/restart still begins in Standard, and no layout preference is read,
written, migrated, or restored. Slice 27B owns persistence/restoration and the
final persisted-selection contract.

This is not a preview, sample path, test-only injection, dead flag, new route,
or model-only contract. Tests may still inject `OxygenAppearance` where that
gives a deterministic fixture, but at least one acceptance case must reach
Simple through the production Appearance control and Home path.

Stop at this one rendering/session-selection slice. Do not add layout
persistence, provider or repository behavior, new weather data, Detailed or
Meteorologist rendering, or a broader Home redesign. Stop at focused green or
record an exact blocker; do not replace an unverified behavior with planning
prose.

Verification budget: one emulator session, no more than six connected test
cases, and 60 minutes. Do not repeat a passing command unless production code,
test inputs, or the relevant environment changed.

## Authorities and dependency facts

Use `AGENTS.md`, `README.md`, specification sections 2, 3.1, 20–25, 29, 31,
34–37, 40, 46, 49, 51, and 53, roadmap rules plus Slices 18H, 27A, and 27B,
`docs/UI_DEVELOPMENT_WORKFLOW.md`, the provider template, the module/root
Gradle files, `scripts/android-env.sh`, and the live history contract/summary
plus its latest three entries.

Slice 18H supplies the verified Standard Home reference, page semantics, named
actions, touch targets, and non-persisted appearance input. `OxygenAppearance`
already contains `SIMPLE` and defaults to `STANDARD`; `OxygenApp` already passes
the effective appearance to `HomeLoadingScreen`. The current Home renderer
always uses the four Standard pages. Existing `HomeDashboardUiTest` fixtures
cover ready, sparse, stale, alert, compact, large-font, effects-off,
navigation, and request-count behavior. Provider, repository, cache, location,
alert, canonical, and presentation-mapping behavior is upstream and remains
unchanged.

## Simple layout contract

1. Simple Now presents selected location, current temperature, condition,
   feels-like, high/low, and near-term precipitation when available. Missing
   data remains absent/unavailable according to the existing presentation
   model; it is never fabricated or parsed back from display text.
2. Current weather remains first, followed by refresh/cache-failure context,
   official alert summary and detail/source actions, precipitation, and
   source/update/provenance information. Alert severity remains textual.
3. Forecast shows the existing presented hourly or daily list, up to six items
   in each choice. Switching Hourly/Daily only changes composition; it causes
   no forecast, alert, location, or geocoding request.
4. Simple is a deliberate lower-density composition, not Standard with
   arbitrary content removed. Standard secondary metric groups and sun detail
   remain Standard responsibilities and remain unchanged.
5. Fresh, restored-cache, stale-after-failed-refresh, no-alert, one-alert, and
   many-alert meaning remain truthful. Source name, update/fetched time, data
   type, stale/cache age, failure context, provider attribution/privacy
   disclosure, and official alert reachability remain visible where supplied.
6. Refresh, Location, Settings, alert detail, and alert-source callbacks retain
   their existing behavior. Layout and Hourly/Daily changes do not refetch or
   mutate canonical/presentation weather values.
7. Standard retains `Now -> Hourly -> Daily -> Details`, its content, tags,
   position, swipe/direct/accessibility navigation, and default behavior.
   Detailed and Meteorologist remain unimplemented and unadvertised.

### Deterministic replacement policy

Every effective layout replacement resets the primary page to that layout's
first page and resets Simple's child forecast choice to Hourly. Therefore:

- Standard → Simple always yields Simple Now / Page 1 of 2 / Hourly.
- Simple Forecast / Daily → Standard always yields Standard Now / Page 1 of 4.
- Standard Details → Simple also yields Simple Now / Page 1 of 2 / Hourly.

The same ready forecast, location, units, freshness, alerts, source, theme,
effects, and repository request count remain in memory in both directions.
Key the layout boundary and local pager/choice state so no invalid index can be
retained during recomposition.

### Accessibility and visual constraints

- Verify 360dp × 640dp, density 160, font scale 1.3, long location/source
  strings, stale weather, multiple alerts, and Effects Off. No horizontal
  clipping or sibling overlap; vertical overflow may remain local.
- Page controls and Hourly/Daily controls have at least 48dp bounds, selected
  semantics, meaningful labels, logical semantics order, and named actions.
  Page titles announce `Now, Page 1 of 2` and `Forecast, Page 2 of 2`.
- Forecast child controls do not accidentally page. Safety, missing-state,
  source, and alert meaning remains reachable without discovering a gesture.
- Effects Off removes decoration but not information. Reuse existing Oxygen
  marks, roles, spacing, forecast rows/cards, and design tokens.

## Intended production changes

Keep the implementation in `:app`; no dependency or schema change is expected.

- `HomeLoadingScreen.kt`: derive typed page identities, titles, tags, count,
  controls, and actions from `appearance.layout`; add the bounded Simple Now
  and Forecast compositions; preserve all shared operational/source/alert
  semantics and the Standard branch.
- `OxygenApp.kt`: hold the session-only layout choice in composition state,
  initialize it from the supplied appearance, pass the effective layout to
  Home and Settings, and reset dependent page/forecast state on replacement.
  Do not add state-holder persistence or a new navigation route.
- `SettingsScreen.kt`: make the existing Appearance destination expose the
  session-only Simple/Standard choice with truthful summary text, selected
  semantics, and 48dp controls. Keep the existing effects controls and their
  pending/error behavior intact.
- `OxygenAppearance.kt`: add only stable layout labels/helpers required by the
  rendered control; no storage values, migration, or preference state.
- A small Home file split is allowed for readability. Do not change
  `OxygenAppStateHolder`, `MainActivity`, `:core`, providers, repositories,
  caches, canonical models, or presentation mappers.

## Focused evidence

Create `.codex/test-artifacts/2026-09-07-slice-27a-simple-layout-definition/ledger.md`
before verification. Record every command, result, rerun reason, retained
artifact, and any skipped/unverified condition there.

Before production edits:

1. Run `scripts/list-avds.sh`, start one `oxygen_starter`, confirm ADB, and
   install the unchanged debug APK once.
2. Save the original device configuration and set 360×640, density 160, and
   font scale 1.3. If an existing real selected-location/cache session is
   ready, capture `baseline-installed-standard-now.png` and its hierarchy.
   Otherwise capture the truthful installed first-run/no-ready state as
   `baseline-installed-no-ready.png` and its hierarchy. Make at most one normal
   manual search attempt; never clear data or seed production with fixtures.
3. Add the four new tests below and run the exact filtered command once to
   record a behavioral red result against the Standard-only renderer. A red
   result must fail on observable Simple behavior, not compilation or a symbol
   existence assertion.

Focused connected cases in the existing `HomeDashboardUiTest`:

1. `simpleLayoutIsReachableAndPreservesRequiredMeaning`: use the production
   Appearance control to select Simple and return Home; assert two named pages,
   current/high-low/precipitation, stale/source/update/provenance, alert text
   and actions, absence of Standard-only tabs, and visible Hourly/Daily
   controls with the presented hourly and daily entries reachable.
2. `simpleLayoutReplacementResetsPagesAndPreservesReadyState`: start at
   Standard Details, switch Standard → Simple, choose Forecast/Daily, then
   switch Simple → Standard; assert the deterministic titles/positions/
   selected choice above, unchanged ready values and state, and zero request
   delta. The same case must cover the reverse switch from Simple Forecast /
   Daily and an externally supplied layout replacement if practical.
3. `simpleSparseAndOperationalStatesRemainHonest`: use deterministic existing
   fixtures to cover null current, missing precipitation, empty hourly/daily,
   no alerts, restored cache, and failed-refresh text without invented values
   or hidden source disclosure. Keep this one state/meaning case rather than a
   combinatorial suite.
4. `simpleLayoutIsReadableCompactLargeFontEffectsOff`: use long location/source
   strings, stale weather, multiple official alerts, 360×640/font 1.3, and
   Effects Off. Assert readable non-overlapping bounds, logical semantics,
   minimum targets, alert-detail reachability/return, no scene decoration, and
   capture Simple Now, Forecast Hourly, Forecast Daily, and semantics files.

Run only these two genuine Standard regressions with the four cases above:

5. `freshSuccessRendersSemanticHomePagesAndPreservesDashboardContent`
6. `homePagerExposesNamedAccessibilityPageMovementActions`

The exact filtered command is:

```sh
. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class='com.oxygen.weather.app.ui.home.HomeDashboardUiTest#simpleLayoutIsReachableAndPreservesRequiredMeaning,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#simpleLayoutReplacementResetsPagesAndPreservesReadyState,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#simpleSparseAndOperationalStatesRemainHonest,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#simpleLayoutIsReadableCompactLargeFontEffectsOff,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#freshSuccessRendersSemanticHomePagesAndPreservesDashboardContent,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#homePagerExposesNamedAccessibilityPageMovementActions'
```

This command must run six cases, not the entire historical connected class.
The tests write retained UI evidence to the target app's `filesDir`. After the
run, pull only the named artifacts into the cycle directory:

```sh
mkdir -p .codex/test-artifacts/2026-09-07-slice-27a-simple-layout-definition
adb exec-out run-as com.oxygen.weather cat files/simple-now.png > .codex/test-artifacts/2026-09-07-slice-27a-simple-layout-definition/simple-now.png
adb exec-out run-as com.oxygen.weather cat files/simple-forecast-hourly.png > .codex/test-artifacts/2026-09-07-slice-27a-simple-layout-definition/simple-forecast-hourly.png
adb exec-out run-as com.oxygen.weather cat files/simple-forecast-daily.png > .codex/test-artifacts/2026-09-07-slice-27a-simple-layout-definition/simple-forecast-daily.png
adb exec-out run-as com.oxygen.weather cat files/simple-now-semantics.txt > .codex/test-artifacts/2026-09-07-slice-27a-simple-layout-definition/simple-now-semantics.txt
adb exec-out run-as com.oxygen.weather cat files/simple-forecast-hourly-semantics.txt > .codex/test-artifacts/2026-09-07-slice-27a-simple-layout-definition/simple-forecast-hourly-semantics.txt
adb exec-out run-as com.oxygen.weather cat files/simple-forecast-daily-semantics.txt > .codex/test-artifacts/2026-09-07-slice-27a-simple-layout-definition/simple-forecast-daily-semantics.txt
```

Use the same emulator for the edit/build/install/capture/inspect loop. Inspect
every retained screenshot. At least one focused case must cross the normal
`OxygenApp`/Home boundary with a recording repository; fixture data proves
deterministic behavior, not live-provider success.

Attempt TalkBack traversal if the configured emulator supports it. Record the
result in the ledger. If it cannot be run, mark service-level traversal
unverified for Gate 30; semantics-tree order, labels, selected state, targets,
and custom actions remain required evidence in this slice and are not claimed
to replace TalkBack traversal.

## Broad checks and closeout

After visual convergence and the single six-case connected run, run once on the
same final revision:

```sh
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

Compilation and assembly are broad evidence, not functional or visual proof.
If the conditional installed baseline cannot reach ready state, retain the
truthful no-ready evidence and do not claim an installed ready Simple journey.

Before declaring ready, record changed production/test files and focused,
rendered, broad, skipped, blocked, and TalkBack evidence in the ledger and
active plan. During the normal implementation doc sync, update README to put
the verified session-only Simple path under installed-app behavior while
keeping persisted layout selection/restoration under not implemented; update
specification section 53, roadmap status, and append one concise cycle-history
entry only to the level supported by evidence. If committed, perform the
required post-commit authority sync and rerun `git diff --check`.

## Explicitly out of scope

- Persisted layout selection, restart restoration, migration, or layout storage;
  those belong to Slice 27B. Standard remains the launch/restart default.
- Detailed or Meteorologist rendering, aliases, selection, or advertisement.
- Theme selection/translation, icon-pack settings, high contrast, Full effects,
  or changes to the committed Off/Subtle policy.
- New Home data, charts, provider requests, mapping, fallback, alert lookup,
  caching, location, units, canonical values, or presentation values.
- Redesign of Standard Home, Settings information architecture beyond the
  session-only Appearance control, alerts, or the Oxygen design system.
- Release, MVP-complete, accessibility-gate, privacy-audit, or live-provider
  success claims.

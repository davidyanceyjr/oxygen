# Oxygen UI v0.3 — UI-04 Standard Now hierarchy and truthful alert state

**Status:** verified
**Commit state:** ready to commit
**Mode:** bounded Android/Compose presentation slice
**Cycle ID:** `2026-09-17-ui-04-now-hierarchy`
**Active roadmap:** `.codex/plans/ui-roadmap.md`
**Selected slice:** UI-04 — Now hierarchy and first proven shared tokens
**Dependencies:** UI-02 and UI-03 are verified and committed at `4216d83` and `05ce18e`.
**Starting state:** only this plan and the UI-04 roadmap selection are intended dirty files; production and test source are unchanged.
**Implementation slices since last checkpoint:** 1 of 2; UI-04 is the first production-changing slice after UI-03.

## Outcome and acceptance boundary

Revise only Standard Home Now into a compact weather-first viewport. Location
and current conditions form a fixed visual anchor; daily high/low and near-term
precipitation form the secondary weather group; refresh/freshness and the
truthful official-alert lookup result form the operational group; concise
update/source context is tertiary. Humidity, wind, full source fields, provider
disclosure, and other details remain on Details. Simple keeps its current Now
composition and page-scroll contract.

The primary boundary is the installed production route on the manually
selected Chicago location at 360x640 dp, font scale 1.0, Standard layout,
Oxygen theme, Standard contrast, and Effects Off. Baseline and final captures
use the same location, units, route, and device state. Required visible order:

~~~text
location/context
current temperature, condition, apparent temperature
daily high/low and near-term precipitation when available
refresh/freshness state, then official-alert lookup state
concise update/source context
~~~

At font scale 1.0 and 1.3, ordinary fresh/no-alert content has no page scroll,
supporting overflow, clipping, overlap, or horizontal scroll. At font scale
2.0 with long active-alert content, only the named supporting region may
scroll; current weather and Home navigation stay fixed, and both alert actions
remain reachable 48 dp targets.

Completion requires mapper coverage, five passing connected cases, reviewed
installed before/after rendering, passing broad checks, and factual closure
documents. A screenshot alone cannot satisfy state, action, or semantics proof.

## Production contract

### Typed alert lookup presentation

Add a sealed app-presentation `HomeAlertLookupPresentation` in
`HomeForecastPresentationMapper.kt`; Home composables must not consume raw
`AlertLookupStatus` or `AlertProviderError`.

| Presentation | Mapper input | Retained UI data |
| --- | --- | --- |
| `Active` | `Available` with alerts; absent status with legacy alerts | existing `HomeAlertSummaryPresentation` |
| `NoActiveAlerts` | `NoAlerts`; `Available` with no alerts | formatted source-check time |
| `NotChecked` | `NotRequested`; absent status with no alerts | none |
| `UnavailableForLocation` | `UnsupportedRegion` | none |
| `UnableToCheck` | `Failed` | none; never expose provider errors |
| `Delayed` | `SkippedByRateLimit` | formatted next-eligible time |

`Active` owns the summary so category and summary cannot diverge. Preserve the
existing `HomeSuccessPresentation.alertSummary` read API as a derived
compatibility accessor, keep `alertDetails` only for `Active`, and retain alert
order, duplicate-ID rejection, detail selection/return, and validated HTTPS
fallback. `NoAlerts` wins over any inconsistent stray alert list. Do not add a
lookup entry to `HomeSuccessSection` or change its existing order.

New visible sentences come from Android resources: no active alerts plus check
time; not checked; unavailable for this location; unable to check; and delayed
until the mapped time. Only `Active` renders event, text severity, issuer,
expiry, count, detail action, and official-source action. Inactive states must
not imply a successful check, no-alert result, or provider diagnostic beyond
their defined meaning.

### Standard-only composition

Split the shared Now path before styling: retain the current Simple composition
and introduce `StandardNowPage`. Preserve page-level overflow behavior for
Simple, Hourly, Daily, and Details; only Standard Now uses a fixed region plus
local overflow.

- `home-now-fixed-content`: location, merged current summary, apparent
  temperature, and existing high/low and precipitation when available.
- `home-now-supporting-content`: refresh-in-progress, cached/failure freshness,
  alert lookup, then concise update/source context.

Preserve existing section/action tags and add one lookup wrapper present for
every lookup variant. Keep active `home-section-alert` nested within it. Remove
humidity and wind from Standard Now only; they remain on Details and unchanged
Simple Now. Move current update/source lines to the tertiary group. Do not add
provider disclosure or duplicate Details fields to Now.

`home-page-now` must expose no vertical scroll action. The supporting region
has zero scroll range for ordinary content and owns overflow for long/2.0
content. Do not shrink semantic text styles or ellipsize safety/status meaning
to force a fit. Preserve current spoken weather meaning and keep the mark
decorative.

### Roles and invariants

Use existing `OxygenHomeDesignRoles` for typography, spacing, padding, shape,
surface, outline, warning, and supporting content; remove local multipliers or
duplicates where those roles already fit. Do not retune shared roles in a way
that redesigns other pages. If installed comparison proves one repeated role
is missing, add only that reusable role, resolve it for all themes/contrasts,
use it at least twice, and extend `HighContrastThemeContractTest`. Add no
parallel token object, theme-ID branch, unused role, or tokenized one-off mark
geometry. Record at closure which roles UI-04 actually proves for later slices.

Preserve selected-location, forecast/fallback, NWS lookup, Room cache, units,
refresh, pager, Back, alert detail/link, values, ordering, provenance, stale
age, and refresh-failure behavior. Keep `SampleWeather.bundle` out of the
installed path. Do not change `:core`, providers, repositories, persistence,
schemas, preferences, navigation state, or provider contracts.

## Test contract

Add mapper tests first and retain the expected red result. Cover every table
row; `Available(emptyList())`; `NoAlerts` with stray alerts; a real Severe
alert with multiple-alert order/count, checked metadata, details, valid and
fallback links; every current `Failed` error mapping to the same opaque state;
rate-limit time without provider/point leakage; both legacy paths; duplicate
IDs; missing current with usable forecast; completely empty provider output;
and unchanged `sectionOrder`/compatibility `alertSummary` behavior.

Reshape affected `HomeDashboardUiTest` methods where possible. Run exactly
these five connected cases, one method at a time:

| Case | Required proof |
| --- | --- |
| `standardNowCompactHierarchyUsesFixedHeroAndNonOverflowingSupport` | At 360x640 and font 1.0/1.3, required order, unchanged current speech, bounds/no overlap, no page scroll, zero supporting range, visible navigation, source last. |
| `standardNowAlertLookupOutcomesAreTruthfulAndActionFree` | Recompose all inactive outcomes; distinct resource copy/metadata, one lookup wrapper, and no event/count/detail/source actions. |
| `standardNowOperationalMissingAndEmptyStatesRemainDistinct` | Refreshing, restored cache, failed refresh with cache, missing current, and empty provider remain distinct; weather is retained where valid, no fake zero, correct status order/actions. |
| `standardNowLargeFontRtlOverflowKeepsHeroNavigationAndAlertActionsReachable` | Font 2.0, RTL, High contrast, Effects Off, long Severe alert; only local scroll, fixed hero/navigation, valid reading/bounds, explicit severity, 48 dp detail/source callbacks and safe URLs. |
| `simpleNowContractRemainsScrollableAndUnchanged` | At 360x640/font 1.3, existing Simple content, humidity/wind, source/disclosure, alert actions, page scroll, and absence of Standard-only containers/status card. |

Audit all existing assertions that encode the old Standard order or call
page-level `performScrollTo`; update only those made stale by UI-04. Compile the
entire Android-test source set, but do not run the full connected class.
Provider/repository connected tests are inapplicable because those boundaries
do not change.

## Work and verification sequence

1. Create the ledger; record commit/status, intended dirty files, budget,
   device configuration, and retained UI-02 evidence review. Stop if unrelated
   production/test edits are present.
2. Start one recovered API-37 `oxygen_starter` session. Install unchanged
   production, confirm Chicago and Standard/Oxygen/Standard-contrast/Effects-
   Off, configure 360x640 dp/font 1.0, and capture baseline PNG plus UI XML.
3. Add mapper tests, record expected red, implement the typed mapping, and
   return the focused mapper suite to green.
4. Add/reshape the five Compose contracts and stale assertions; compile all
   Android-test Kotlin before changing the composable.
5. Split Simple/Standard Now and implement the fixed/local-overflow composition
   and lookup rendering. Iterate one visual change through build/install/
   capture/inspection; install once per changed APK.
6. After convergence, run the five connected methods. Pull deterministic
   screenshots/semantics only from accepted runs. Rerun a normal failure only
   after a recorded relevant change; do not repeat an unchanged timeout.
7. Install the final production APK, reproduce the baseline state, capture PNG/
   XML plus interaction record and APK hash, and compare with baseline. Live
   lookup may be no-alert; deterministic Compose evidence owns active-alert proof.
8. Run broad checks once, review all diffs/artifacts, close documents, commit
   with subject/body evidence and limits, then perform post-commit consistency
   review.

## Verification budget and artifacts

Budget: one emulator session; baseline plus one install per changed APK; five
connected cases (under the eight-case default); mapper red/final green; app and
Android-test compilation; one app/core unit suite; one debug assembly; one
whitespace check. Each connected method gets one 120-second attempt per
unchanged state.

Store ignored evidence under
`.codex/test-artifacts/2026-09-17-ui-04-now-hierarchy/`: `verification-ledger.md`,
`baseline/`, `red/mapper.log`, `connected/emulator/`, one directory per named
case, `connected/deterministic/`, `installed/`, and `broad/`. The ledger records
commands, exact results, selected-case count, serial/configuration, APK identity,
artifact paths, and every rerun/skip reason; distinguish wrapper status from
accepted instrumentation output.

Final changed-state commands:

~~~bash
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:compileDebugAndroidTestKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests com.oxygen.weather.app.HomeForecastPresentationMapperTest --tests com.oxygen.weather.app.HighContrastThemeContractTest
scripts/run-connected-method.sh --serial <serial> com.oxygen.weather.app.ui.home.HomeDashboardUiTest#standardNowCompactHierarchyUsesFixedHeroAndNonOverflowingSupport .codex/test-artifacts/2026-09-17-ui-04-now-hierarchy/connected/compact-hierarchy
scripts/run-connected-method.sh --serial <serial> com.oxygen.weather.app.ui.home.HomeDashboardUiTest#standardNowAlertLookupOutcomesAreTruthfulAndActionFree .codex/test-artifacts/2026-09-17-ui-04-now-hierarchy/connected/lookup-outcomes
scripts/run-connected-method.sh --serial <serial> com.oxygen.weather.app.ui.home.HomeDashboardUiTest#standardNowOperationalMissingAndEmptyStatesRemainDistinct .codex/test-artifacts/2026-09-17-ui-04-now-hierarchy/connected/operational-states
scripts/run-connected-method.sh --serial <serial> com.oxygen.weather.app.ui.home.HomeDashboardUiTest#standardNowLargeFontRtlOverflowKeepsHeroNavigationAndAlertActionsReachable .codex/test-artifacts/2026-09-17-ui-04-now-hierarchy/connected/large-font-rtl-alert
scripts/run-connected-method.sh --serial <serial> com.oxygen.weather.app.ui.home.HomeDashboardUiTest#simpleNowContractRemainsScrollableAndUnchanged .codex/test-artifacts/2026-09-17-ui-04-now-hierarchy/connected/simple-regression
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
~~~

Use the repository emulator/install/capture scripts. Record and restore device
size, density, font scale, direction, and animation settings before shutdown.

## Files, document closure, and limits

Expected source scope: `HomeForecastPresentationMapper.kt`,
`HomeLoadingScreen.kt`, `strings.xml`, `HomeForecastPresentationMapperTest.kt`,
and `HomeDashboardUiTest.kt`. `OxygenTheme.kt` and
`HighContrastThemeContractTest.kt` enter scope only if one missing repeated role
is proven. No Gradle/core/provider/storage file is expected.

After verification:

- update this plan with actual `covered`, `implemented`, `verified`, commit,
  commands/results, artifacts, limits, and slice count `1 of 2`;
- mark only UI-04 `verified` in `ui-roadmap.md`; leave UI-05 `specified` and
  name it only as the next candidate;
- append one concise UI-04 entry to `cycles/history.md`; and
- update README's implemented list with the verified Standard Now hierarchy
  and explicit lookup states while retaining live-active-alert, TalkBack,
  localization, alert persistence/background, notification, release, and 1.0
  gaps.

Review the full/UI specifications, UI workflow, and NWS/Open-Meteo/MET Norway
contracts after implementation. They currently authorize this slice; update
only a factual contradiction and otherwise record the unchanged review in the
ledger. Provider documents are not expected to change.

Out of scope: Simple redesign; Hourly/Daily/Details redesign; new metrics,
marks, scenes, providers, core/cache/schema/location/Settings/navigation/Back
behavior; alert-detail redesign; alert persistence/background/notifications;
localization completion; TalkBack service traversal; release or 1.0 claims.

Sizing: 7/10, medium: one Standard Now surface plus its mapper state, five
focused Android cases, and one installed before/after journey. Reserve roughly
40% of the implementation session for rendered iteration, evidence, broad
checks, review, closure, and handoff. Split/stop if compact fit requires page
scroll, another surface or provider/core/persistence change, or another
independent state machine. Never replace failed real evidence with a preview,
mock, screenshot-only claim, or documentation.

## Actual result and evidence

Status is `covered`, `implemented`, and `verified`; commit state remains
separate until the source and documentation change is committed. The Standard
Now production route now owns a fixed location/current-weather anchor and a
local supporting scroller for operational state, typed alert lookup outcome,
and concise source/update context. Simple Now retains its prior scrollable
composition. No provider, core, persistence, navigation, or shared-theme role
files changed.

Focused evidence:

- `HomeForecastPresentationMapperTest` and `HighContrastThemeContractTest`
  passed in the focused unit command.
- All five named connected methods passed 1/1 in the single recovered
  `oxygen_starter` session; accepted artifacts are under
  `.codex/test-artifacts/2026-09-17-ui-04-now-hierarchy/connected/`.
- The installed final route was manually restored through production Chicago
  search and Use now. Final screenshot/XML and APK hash are under
  `.codex/test-artifacts/2026-09-17-ui-04-now-hierarchy/installed/`; the
  before/after screenshots were visually reviewed at 360x640 dp, font 1.0,
  LTR, Effects Off.
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest
  :core:testDebugUnitTest`, `:app:assembleDebug`, and `git diff --check`
  passed. Compilation logs are under `connected/final-compile.log` and the
  broad logs are under `broad/`.

The mapper red attempt is retained at `red/mapper.log`; the later focused
green result is `focused-unit-final.log`. Provider/repository connected tests,
full connected classes, TalkBack service traversal, localization completion,
release checks, and live active-alert installed evidence were not applicable
or in scope for this slice. The live Chicago route returned no active alert;
the active-alert action proof is deterministic Compose evidence.

Post-implementation review found the full product/UI specifications, UI
workflow, and NWS/Open-Meteo/MET Norway contracts consistent and unchanged.
Next action after commit: select UI-05 as the next bounded slice only after
the next two-slice/checkpoint policy is applied.

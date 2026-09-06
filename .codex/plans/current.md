# Slice 24B — Official Alert Detail Navigation

**Status:** committed
**Cycle ID:** `2026-09-06-slice-24b-official-alert-detail-navigation`
**Implementation commit:** `ceb6253`
**Planning basis:** `d9bdb63` (Slice 24A completion evidence). The worktree also
contains an unrelated modification to `scripts/start-emulator.sh`; leave it
untouched.

## Decision and acceptance boundary

Add a usable in-app detail surface for every official alert currently available
in the active Home forecast session. From Home Now, a visible `View alert
details` action opens the first alert. If more than one alert is available, the
detail surface exposes an accessible selector for every active alert and opens
the selected one. System Back and the in-surface Back action return to the
same Home ready data state.

The detail surface shows the issuing authority, severity, event, optional
headline, urgency, certainty, effective and expiry times, optional sent/onset/
end times, affected area, description, instructions, alert-source check time,
and the already-validated official source link. It converts provider instants
with the selected location zone. It renders official description and
instruction text verbatim, without summarizing or changing its meaning.

The slice is complete only when a deterministic connected test follows the
normal Home action through the app state holder into the rendered detail
surface, selects a second alert, returns to Home, and proves that the external
source link remains a separate action.

This slice does not add alert persistence/cache, Room entities or migration,
background work/polling, notifications, retry controls, provider changes,
alert-rate-gate changes, forecast-cache changes, deep links, share actions,
maps/geometry rendering, or non-NWS regional routing.

## Compatibility contract

- Preserve `WeatherAlert`, `AlertLookupStatus`, NWS transport/classification,
  deduplication, the 30-second request gate, forecast freshness, provenance,
  and forecast-only cache composition unchanged.
- A detail route is eligible only for `AlertLookupStatus.Available` with a
  non-empty, deduplicated active-alert list. `NoAlerts`, `NotRequested`,
  `UnsupportedRegion`, `Failed`, and an unbacked `SkippedByRateLimit` state
  create neither a banner action nor a detail route.
- Keep the current Home summary's source link behavior and URL validation. It
  opens externally; it must not open the in-app detail surface.
- Preserve all existing Home tabs, refresh/location/settings behavior, unit
  remapping, and stale forecast behavior. A refresh result while the detail is
  visible updates its returned Home state; if it no longer contains the selected
  available alert, route to Home rather than display a stale alert as active.
  The detail remains visible across a failed refresh only when the existing
  `retainVisibleCacheAfterRefreshFailure` rule retains a restored/cache-derived
  `ForecastReady` dashboard. A failure after a fresh live dashboard continues
  to use the existing `NoCacheError` behavior; this slice must not add fresh
  in-memory retention or change forecast-freshness semantics.
- Retain the acknowledged Slice 24A Now-page overflow-scroll exception. This
  slice adds no Home pager, page-scroll, or normal-scale composition change;
  the standalone detail screen scrolls only to keep its long official text
  readable.
- Do not silently substitute a forecast time, lookup time, device time, or UTC
  for an absent alert time. Missing required detail values display `Unavailable`.
  Optional sent/onset/end values are omitted when absent.

## Production design

### Presentation boundary

Keep `HomeSuccessPresentation.alerts` as the existing direct complete
`List<WeatherAlert>` compatibility field. Add an immutable presentation-only
`alertDetails: List<HomeAlertDetailPresentation>` when, and only when, the
effective lookup status is `Available` and the list is non-empty. Its order is
the existing merged-result order and every stable alert ID must be unique; a
duplicate ID is a mapper/test failure rather than an ambiguous selector. Each
`HomeAlertDetailPresentation` contains:

- its stable alert ID;
- human-readable severity, urgency, and certainty labels;
- event, optional headline, issuer, selected-zone times, affected-area text,
  description, instruction, source-check time, attribution, and the validated
  per-alert source URL/semantic label;
- `Unavailable` for absent effective, expiry, affected-area, description, or
  instruction values. Do not expose provider DTOs, raw JSON, references,
  geometry, or parameters in Compose.

Use the existing `validAlertSourceUrl` helper for each detail link. Do not alter
the compact `HomeAlertSummaryPresentation` data contract except to add the
visible detail-action label/semantic label needed by Home. Its detail action is
parameterless: Compose never reads `alerts` or `alertDetails`; it calls
`onAlertDetailsRequested()`, and the state holder resolves the first eligible
detail from the active ready Home dashboard.

### App-state navigation

Add `OxygenAppScreen.AlertDetail(selectedAlertId, returnHome)`, where
`returnHome` is the ready Home screen carrying the one immutable
`alertDetails` collection. The detail screen derives both the selected item and
its selector choices from that collection; it does not receive a second
presentation copy. Add state-holder methods to open the first available alert,
select another available alert, and return Home. Resolve every ID against the
current `ForecastReady` dashboard; unknown IDs and non-ready/non-available
states are no-ops.

Make the route-update rules explicit. `visibleOrReturnScreen` descends through
both `About.returnScreen` and `AlertDetail.returnHome`. For a Home replacement,
`withVisibleOrReturnScreen` recurses through `About`; at `AlertDetail` it
copies only `returnHome`, retaining `selectedAlertId`. Thus unit remapping,
loading, refresh results, location-entry return handling, and About nesting
all update the one return Home dashboard consistently. `startHomeForecastLoad`
must use that Home-replacement path, so its immediate loading emission updates
the detail route's `returnHome` and leaves `AlertDetail` visible. A successful
refresh preserves the selected ID only when it resolves in the replacement
available details; otherwise replace the detail route with the new Home route.
The cache-derived failed-refresh result follows the same preserving
Home-replacement path; a fresh-result failure follows the existing Home
`NoCacheError` path. A cached restoration is `NotRequested` and cannot open
detail. No route update may fabricate or mutate alert content.

### Compose surfaces

Add `ui/alerts/AlertDetailScreen.kt`, using the existing theme roles and
Material 3 controls. It has a safe-area, vertically scrolling surface with:

- a top `Back to weather` action and an accessible screen title;
- a severity text heading plus event and optional headline;
- issuer, urgency, certainty, and time/affected-area fields in readable source
  order;
- unmodified description and instruction sections, each visibly labelled;
- NOAA/NWS attribution and the validated external source-link control;
- for multiple alerts, individually labelled selectable rows before the detail
  body, exposing event, severity, and expiry without relying on color alone.
  Each row exposes a semantic selected state and readable `Current alert` /
  `Select alert` state text; changing rows updates that state before the alert
  body, so assistive technology can identify which alert it is reading.

Wire `HomeLoadingScreen` with parameterless `onAlertDetailsRequested`. The Home
summary uses an explicit `View alert details` button tagged
`home-alert-details`; leave `home-alert-source-link` as a separate button.
Route the new screen from `OxygenApp`, including Android Back handling. Do not
wrap the alert card in a clickable container, which would steal the source-link
interaction.

## Focused coverage

1. Extend `HomeForecastPresentationMapperTest` to prove available alerts map
   complete detail values in the selected zone, valid/fallback HTTPS URLs per
   alert, readable enum labels, absent-value behavior, duplicate-ID rejection,
   and no detail model for every non-available lookup status.
2. Extend `HomeForecastStateHolderTest` to prove parameterless first-alert
   opening, unknown ID no-op, multi-alert selection, explicit back, and unit
   remap while detail is visible. Prove the immediate refresh-loading emission
   updates `AlertDetail.returnHome` while the visible screen remains selected
   detail; then prove a success retaining that ID replaces changed detail
   text/times and still permits selector navigation. Separately prove a
   successful refresh removing the selected alert returns Home, a failure from
   restored/cache-derived content retains the current detail with the existing
   stale-failure state, and a failure from fresh content reaches the existing
   `NoCacheError` route. Cover route-helper recursion for `About` around detail
   as well as direct detail. Assert no repository request, cache write, or
   alert status change is caused by navigation.
3. Extend `HomeDashboardUiTest` with an app-level Home-to-detail flow using at
   least two fixture alerts. Assert Home's detail and source controls are
   distinct; assert required text, labels, all selector choices, selected-zone
   timestamps, and exact fixture description/instruction text, including
   supplied line breaks. Assert the selected alert's issuer, NOAA/NWS
   attribution, and `AlertLookupStatus.Available.metadata` source-check time;
   distinguish that lookup time from alert event times. Assert alert one's
   selected/current selector semantics, then select alert two and assert its
   selected state, issuer, source-check time, validated source URI, and
   semantic label replace alert one's. In separate fresh detail visits, invoke
   the in-surface Back control and Android Back, and verify each returns to the
   same ready Home state. Include missing-value and one-alert variants without
   snapshots as the only proof.
4. Retain Slice 24A app/factory/core alert merge suites as regression coverage.

## Visual and real-path evidence

Create `.codex/test-artifacts/2026-09-06-slice-24b-official-alert-detail-navigation/`
with a ledger before changes. Capture the installed baseline using the existing
package/launch/window/capture sequence from `docs/UI_DEVELOPMENT_WORKFLOW.md`.
If the installed app has no selected location, record first-run status and use
the deterministic connected fixture as the Home/detail acceptance boundary.

The connected fixture uses two alerts at `360dp x 640dp`, density `1`, font
scale `1.3`, and `EffectsLevel.OFF`. It opens the Home detail action, selects
the second alert, scrolls through the instruction and source control, captures
`alert-detail-effects-off-360x640-font-1.3.png`, writes a matching semantics
artifact, and asserts positive readable bounds/non-overlap for the selector,
detail content, source control, and Back action. Extract both files from the
target app after the connected run. Install once per changed APK and relaunch
without restarting the emulator for ordinary retries.

## Verification budget

Budget: one emulator session, one APK install, 75 minutes, and 14,000 agent
tokens. Record each command, result, and rerun reason in `ledger.md`; rerun only
after relevant code, fixture, or environment changes.

```sh
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecastPresentationMapperTest' --tests '*HomeForecastStateHolderTest' --tests '*InstalledForecastRepositoryFactoryTest' --tests '*AboutDisclosureStateHolderTest'
. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest
. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*AlertMergingWeatherRepositoryTest' --tests '*AlertProviderContractTest' --tests '*NwsAlertProviderTest' --tests '*NwsAlertClientTest' --tests '*CachedWeatherRepositoryTest'
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

## Documentation and completion

Authority sync completed in commit `ceb6253`.

- Updated `README.md`, `DATA_SOURCES.md`, `PRIVACY.md`,
  `app/src/main/kotlin/com/oxygen/weather/app/AboutDisclosureContent.kt`,
  `docs/OXYGEN_FULL_SPECIFICATION.md`, and
  `docs/data-sources/NWS_ALERTS.md` to describe installed alert detail
  navigation as implemented and keep alert persistence/cache, background
  polling, and similar future work out of scope.
- The implementation commit adds the Home detail route, the alert detail
  screen, mapper/state-holder navigation, and the connected UI/state tests.
- Verification evidence in this session was limited to `git diff --check`; the
  user stated prior tests had already passed and did not ask for reruns.
- `scripts/start-emulator.sh` remains an unrelated worktree change and was not
  touched.

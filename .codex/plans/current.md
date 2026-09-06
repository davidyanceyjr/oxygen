# Slice 24A — Alert Summary/Banner UI

**Status:** committed
**Cycle ID:** `2026-09-06-slice-24a-alert-summary-banner-ui`
**Planning basis:** local `main` `be38405`; the post-Slice-23C authority-sync
commit is `24fa4ac`; implementation committed as `cf9ddaf`; reviewed and
revised 2026-09-06.

## Decision and acceptance boundary

Install one official-alert summary on Home's Now page after an eligible terminal
forecast success. It presents the first deduplicated active NWS alert, a total
count when more than one alert is active, NOAA/National Weather Service
attribution, a visible alert-source check time, and a working external source
link. The installed factory must compose the committed core merge outside the
forecast cache:

```text
Open-Meteo -> FallbackWeatherRepository -> CachedWeatherRepository
    -> AlertMergingWeatherRepository(NwsAlertProvider)
```

Slice 24A does not add alert-detail navigation, alert persistence/cache,
background polling, notifications, retry controls, provider routing changes,
or Room changes. Slice 24B owns the detail surface and navigation.

The completed mobile Home compatibility baseline is Slice 18J (`7950a42`), not
Slice 18I. The slice is ready only when the installed path rate-limits NWS
lookups, an available result reaches the actual Now surface, and the alert
source-check time remains distinct from stale forecast context.

## Production design

### Repository and session state

`InstalledForecastRepositoryFactory.create` receives an injectable
`AlertProvider` defaulting to `NwsAlertProvider` and an injectable `Clock`; it
uses the composition above. Its alert merger receives a process-local,
foreground `AlertRequestGate` with a fixed 30-second minimum interval. The key
is the alert provider ID plus the exact selected `GeoPoint`; different points
and providers have independent gates. The gate records the start of every
physical request, including a failed one, so Refresh cannot create a tight
failure loop. Each failure therefore receives at least this 30-second backoff;
a `RateLimited` response with a parseable positive delta-seconds `Retry-After`
extends the next eligible time to the larger value. A request at exactly its
next eligible time is allowed; one before then is skipped. The record is
memory-only, cleared on process death, and is neither an alert cache nor a
launch/offline source.

On a skip, the merger does not call NWS. If that key has an in-memory successful
alert result, it reuses its deduplicated list and successful lookup metadata;
an available result therefore keeps its visible summary and `Alert source
checked …` time, while a no-alert result still shows no banner. If there is no
prior successful result, the status is `SkippedByRateLimit` and no banner is
shown; this is neither a no-alert assertion nor a forecast error. A real lookup
failure continues to show no alert summary and never becomes forecast failure
or retry UI. Core merge/deduplication, forecast freshness/provenance, and
forecast-only Room caching remain unchanged.

Evolve `AlertLookupStatus.Available` and `NoAlerts` to retain their
`AlertSuccessMetadata`; add `SkippedByRateLimit(providerId, requestPoint,
nextEligibleAt)`. The retained metadata remains provider-neutral and makes a
successful alert lookup's provider, selected point, and `fetchedAt` available
at the presentation boundary without storing it in the forecast cache.

`OxygenAppStateHolder` owns one private active canonical forecast session,
containing the `WeatherBundle` and its `AlertLookupStatus`. It is the sole
source used for a unit remap. The merger, not the state holder, owns the
per-key request gate and retained in-memory successful result.

- A live `Success` replaces both values together and passes its status to the
  mapper.
- Cached restoration creates a session with `NotRequested`, because forecast
  cache storage contains no alert data.
- Loading/refresh-in-progress retains the visible ready state and its session
  status. A failed refresh retains that same status while changing only
  forecast freshness to stale-after-failed-refresh.
- A location change clears the whole session. A later success replaces it.

This preserves an available summary through immediate unit selection without
changing canonical values, forecast source/provenance, stale age, or refresh
failure text.

### Presentation and attribution

`WeatherBundle.toHomeSuccessPresentation` accepts `AlertLookupStatus`.
`HomeSuccessPresentation` retains the complete provider-neutral
`List<WeatherAlert>` directly for Slice 24B; it must not retain the current
lossy `HomeAlertPresentation` copy. This avoids duplicating core alert types
while preserving the detail-required event/headline, severity, urgency,
certainty, effective/onset/expires/ends, affected area, description,
instruction, issuer, web URL, lifecycle/references, and provenance.

Add only a compact `HomeAlertSummaryPresentation`, derived when and only when
the status is `AlertLookupStatus.Available` and the retained list is non-empty.
It contains the first alert's event, textual severity, issuer, selected-zone
expiration text (or `Expires unavailable`), total active-alert count,
`Alert source checked <selected-zone fetchedAt>`, and attribution link data.
That text reports the independent NWS response fetch time, not forecast time
or a claim that the alert is current. `NoAlerts`, `UnsupportedRegion`,
`Failed`, `NotRequested`, and `SkippedByRateLimit` without a retained success
produce no summary even if an unexpected list is present. Forecast source
presentation remains forecast provenance; alert provenance is not substituted
for it.

The mapper, rather than Compose, validates the source-link URL with
`java.net.URI`: trim input; accept it only when it parses as an absolute URI,
its scheme is case-insensitively `https`, and its host is nonblank. Malformed,
blank, relative, `http`, hostless HTTPS, and every other scheme resolve to the
fixed provider-local `https://www.weather.gov/` fallback. The banner's visible
link copy is `Official alerts from NOAA/National Weather Service`; its semantic
label is `Open official NOAA/National Weather Service alert source`. The link
calls the Compose URI handler, so it is an actionable external source link
rather than an unfinished detail control. Effects-off rendering keeps the same
visible copy, semantic action, and ordinary surface contrast.

### Now-page banner

Replace `dashboard.alerts.forEach` with one `home-section-alert` card after the
current-conditions hero and any forecast freshness card, before near-term
precipitation. Its section order reflects that placement. It contains:

- `Official alert`, event, textual severity, issuer, expiration, and alert
  source-check time;
- the NOAA/NWS link above, tagged `home-alert-source-link`;
- `N active alerts` only when `N > 1`, tagged `home-alert-count`. This is a
  total, not an additional-alert count: exact two- and three-alert values are
  `2 active alerts` and `3 active alerts`.

Keep text in natural visual/TalkBack order and use separate readable semantics.
Severity is never color- or icon-only. There is no detail, navigation, disabled
control, global banner, or notification in this slice.

### Existing Home scrolling exception

The current `HomePageContainer` unconditionally scrolls Now at normal font
scale, which is not specification-conformant normal Standard Home navigation.
Slice 24A does not claim that behavior as evidence and does not add a scroll
container, nested scroll, or navigation change. It is an explicit existing
drift to be remediated by a separately scoped Home-composition follow-up.

This slice's guard is narrower: at normal supported content it must not make
the card clip, overlap, or displace the current hero; at 1.3 font scale and
long alert content, existing overflow scrolling is used only to preserve
complete readable information. The review/commit record must state this
exception rather than report page scrolling as compliant.

## Focused coverage

1. `InstalledForecastRepositoryFactoryTest`: inject a recording alert provider
   and mutable clock; prove a first Open-Meteo success and eligible MET Norway
   fallback each call NWS once, while repeated same-point success and a failed
   request's immediate retry make no second call before 30 seconds. Prove the
   next request at 30 seconds is eligible, point/provider keys are independent,
   forecast-only cache writes remain alert-free, and no-alert/failure outcomes
   remain displayable. Add the required installed composition case: a seeded
   Room-style forecast cache plus eligible Open-Meteo/fallback failure yields a
   stale cached success, calls the recording alert provider once, preserves its
   stale age/failure/source context, and performs no alert-bearing cache write.
2. `HomeForecastPresentationMapperTest`: prove `Available` creates exact
   summary values, selected-zone expiry, explicit missing expiry, selected-zone
   alert source-check time, and exact total text for two and three alerts.
   Exercise valid HTTPS input and malformed, blank, relative, `http`, hostless
   HTTPS, and non-web input, all resolving to the fixed fallback where invalid.
   Prove every non-available status yields no summary while preserving forecast
   source and the direct complete `WeatherAlert` list.
3. `HomeForecastStateHolderTest`: assemble the installed repository with a
   recording provider and mutable clock, then trigger rapid completed Refresh
   actions. Prove the state-holder/repository boundary makes one physical NWS
   call in the 30-second window, keeps the existing in-memory available summary
   and its original alert source-check time on a skipped lookup, and does not
   mislabel a skip as no alerts or a forecast failure. Also prove an available
   alert survives a unit change with unchanged forecast freshness/provenance;
   cover restored-cache `NotRequested`, refresh-in-progress, and
   stale-after-failed-refresh retention. Include a stale forecast with a newly
   fetched available alert reaching `ForecastReady` with both the forecast stale
   age/failure copy and the distinct alert source-check time.
4. `HomeDashboardUiTest` (connected Compose): use the production Home
   composable to assert one-alert copy/order, exact two/three count copy,
   effects-off rendering, missing expiry, source-check time, and the link
   semantic action. Provide a recording URI handler and assert clicking
   `home-alert-source-link` opens both a valid supplied HTTPS URL and the fixed
   fallback for an invalid `http` URL; no browser or live alert is required.
5. `AboutDisclosureStateHolderTest`: replace the stale NWS-roadmap-only
   assertions with active NWS request/privacy/attribution assertions, while
   retaining the statements that non-NWS alert regions and alert detail/cache/
   background behavior remain unimplemented.

Keep the Slice 23C core merge suite as regression coverage. Do not add live
alert-count tests, HTTP fixtures, snapshot-only tests, dependencies, a DI
framework, alert Room entities, or documentation tests in place of the real
path.

## Deterministic visual evidence

Before UI edits, record the already-installed package path in
`baseline-installed-package.txt`, then run the following with `ARTIFACT` set:

```sh
adb shell pm path com.oxygen.weather | tee "$ARTIFACT/baseline-installed-package.txt"
adb shell am force-stop com.oxygen.weather
adb shell am start -W -n com.oxygen.weather/.MainActivity 2>&1 | tee "$ARTIFACT/baseline-launch.log"
# Within 30 seconds, visually confirm the existing selected location reached Home.
adb shell uiautomator dump /sdcard/oxygen-baseline-home.xml >/dev/null
adb exec-out cat /sdcard/oxygen-baseline-home.xml > "$ARTIFACT/baseline-home-window.xml"
scripts/capture-screen.sh "$ARTIFACT/baseline-installed-home.png"
```

If no suitable pre-edit APK is installed, record that exact fact in `ledger.md`
and capture only after installing the pre-edit assembled APK; do not label
another app's framebuffer as Oxygen. After the changed APK is installed, repeat
the force-stop/start, bounded Home-ready check, window dump, and capture with
the same commands and the `final-launch.log`, `final-home-window.xml`, and
`final-installed-home.png` paths. These are normal installed path checks, not
evidence that a live alert exists.

The connected fixture is the deterministic alert-content capture. In
`HomeDashboardUiTest`, create three active fixture alerts with the first
alert's valid `web` URL, select Now (the initial page), then call
`performScrollTo()` on `home-section-alert` before capture. Render at exactly
`360dp x 640dp`, density `1`, font scale `1.3`, and
`OxygenAppearance(effects = EffectsLevel.OFF)`. Capture the root with
`captureToImage()` into target-app files as:

```text
alert-summary-effects-off-360x640-font-1.3.png
alert-summary-effects-off-360x640-font-1.3-semantics.txt
```

The test asserts positive readable bounds after scroll for
`home-section-alert`, `home-alert-source-link`, and `home-alert-count`; it
asserts non-overlap in visual order for `home-section-current`,
`home-section-stale`, `home-section-alert`, `home-alert-source-link`,
`home-alert-count`, and `home-section-precipitation`. Extract both target-app
files after the connected run with `adb exec-out run-as` into
`.codex/test-artifacts/2026-09-06-slice-24a-alert-summary-banner-ui/` and
record the exact device/package command in `ledger.md`.

## Verification budget

Use one emulator session and one install per changed APK. The verification
budget is 90 minutes and 18,000 agent tokens; at that limit, stop and record
the evidence and remaining gap. Create
`ARTIFACT=.codex/test-artifacts/2026-09-06-slice-24a-alert-summary-banner-ui`,
run `mkdir -p "$ARTIFACT"`, and use Bash `set -o pipefail`; the concrete log
commands below name every output. Record each exit status and rerun reason in
`ledger.md`. Save the start/install/launch output as `emulator-start.log`,
`install.log`, `baseline-launch.log`, and `final-launch.log`. Do not commit
artifact payloads. Run each command once unless code, inputs, or environment
change:

```sh
set -o pipefail
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*InstalledForecastRepositoryFactoryTest' --tests '*HomeForecastPresentationMapperTest' --tests '*HomeForecastStateHolderTest' --tests '*AboutDisclosureStateHolderTest' 2>&1 | tee "$ARTIFACT/app-focused-unit.log"
. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest 2>&1 | tee "$ARTIFACT/home-dashboard-connected.log"
. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*AlertMergingWeatherRepositoryTest' --tests '*AlertProviderContractTest' --tests '*NwsAlertProviderTest' --tests '*NwsAlertClientTest' --tests '*CachedWeatherRepositoryTest' 2>&1 | tee "$ARTIFACT/core-focused-unit.log"
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin 2>&1 | tee "$ARTIFACT/compile-debug.log"
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest 2>&1 | tee "$ARTIFACT/all-unit.log"
. scripts/android-env.sh && ./gradlew :app:assembleDebug 2>&1 | tee "$ARTIFACT/assemble-debug.log"
git diff --check 2>&1 | tee "$ARTIFACT/git-diff-check.log"
```

Start `scripts/start-emulator.sh` with output to `emulator-start.log`, install
with `scripts/install-debug.sh 2>&1 | tee "$ARTIFACT/install.log"`, and use the
bounded launch/capture sequence above. There is no live-alert attempt in this
slice: deterministic provider and Compose fixtures prove alert behavior without
spending an extra NWS request.

## Documentation and completion

Before the implementation commit, update `README.md`, `DATA_SOURCES.md`,
`PRIVACY.md`, `AboutDisclosureContent.kt`, and the relevant implementation and
roadmap status in the specification. State exactly that foreground
selected-point NWS lookup and the Home summary/link are installed; retain the
out-of-scope detail, persistence/cache, background polling/notifications, and
non-NWS-region boundaries.

After the verified commit, synchronize this plan and append the self-contained
cycle-history result. In that authoritative sync, correct the live-history
summary's last documentation-sync commit from `3c658a8` to `24fa4ac`, retain
`be38405` as the merge basis, and correct specification section 53's stale
"current changeset" wording rather than copying it into a 24A completion
claim. Record changed files, commands actually run, artifacts, blockers, and
the acknowledged Home-scroll exception. Do not call this slice verified or
committed before those facts exist.

Expected production files are the installed factory, app state holder, Home
mapper, Home loading screen, and About disclosure content. Expected tests are
the five app tests named above, including `AboutDisclosureStateHolderTest`.

## Completion Evidence

Status: committed.

Implementation commit: `cf9ddaf` (`Implement foreground alert summary banner`).
Authority-sync commit: `cc2af8b` (`Sync Slice 24A authorities`).

Changed production boundaries:

- `:core` now merges terminal forecast successes with independent NWS alerts,
  deduplicates them, retains successful metadata, and enforces the process-local
  per-provider/per-point 30-second request gate, including failure backoff and
  positive `Retry-After` extension.
- `:app` now installs `Open-Meteo -> FallbackWeatherRepository ->
  CachedWeatherRepository -> AlertMergingWeatherRepository(NwsAlertProvider)`,
  carries alert status through canonical state and unit remapping, and renders
  the official alert summary/link on Home Now.
- Provider-neutral `WeatherAlert` values remain complete in the Home
  presentation; the compact banner is created only for an available non-empty
  lookup and validates/falls back source URLs in the mapper.

Changed tests and authorities include the installed factory, merge repository,
mapper, state holder, disclosure, and connected Home tests plus `README.md`,
`DATA_SOURCES.md`, `PRIVACY.md`, `AboutDisclosureContent.kt`, and section 53 of
the full specification.

Evidence ledger: `.codex/test-artifacts/2026-09-06-slice-24a-alert-summary-banner-ui/ledger.md`.

Final evidence:

- Focused core merge/provider/cache tests passed in
  `core-focused-unit-final.log`.
- Focused app factory/mapper/state/disclosure tests passed in
  `app-focused-unit-final-4.log`.
- `HomeDashboardUiTest` passed all 37 connected tests in
  `home-dashboard-connected-final-2.log`; the dedicated alert test was also
  rerun against the final APK/test APK.
- The exact effects-off fixture was captured at `360x640`, density `1`, font
  scale `1.3` in `alert-summary-effects-off-360x640-font-1.3.png` and its
  semantics artifact.
- Final app compilation, all app/core unit tests, debug assembly, and
  `git diff --check` passed; final logs use the `*-final-2.log` names in the
  artifact directory.
- The installed launch was successful, but the fresh target app had no
  selected location, so its final window/screenshot are first-run evidence.
  Deterministic connected fixture coverage is the Home acceptance evidence.

The existing Now-page scroll behavior remains an explicit compatibility
exception: the slice uses the existing overflow scrolling at large font scale
to preserve readable alert content and does not claim to remediate normal-scale
Home navigation composition.

Blockers: none.

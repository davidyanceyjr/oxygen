# Active Cycle

Status: committed
Cycle ID: 2026-09-01-home-operational-state-integration
Mode: feature
Goal: Implement Slice 18F: Home Operational State Integration without redesigning providers, persistence, or Standard Home page visuals.
Roadmap context: Slice 18F follows committed Slices 18A through 18E and precedes Slice 18G design-system consolidation.
Branch or work context: local `main` contains committed Slice 18E (`fe243d0`) and the working tree is expected to start from the committed Details visual baseline.

## Contract

Selected behavior:
- Verify and, where needed, adjust the existing Standard Home paged architecture so current operational states remain usable across the Now, Hourly, Daily, and Details pages.
- Keep loading, refresh-in-progress, fresh success, cached/stale success, refresh failure with useful cache, retryable no-cache error, retry, and source/update state observable in the paged Home model.
- Ensure non-happy-path states do not strand the user on meaningless, empty, or misleading pages.
- Preserve Slice 18 selected-location persistence and forecast-cache behavior through the existing DataStore, Room, and repository boundaries.

Acceptance boundary:
- The installed app and focused tests show that existing applicable Home operational states work with the paged architecture.
- Loading and retryable no-cache error states give the user useful location/status context and reachable retry/navigation actions without exposing empty forecast pages as real content.
- Refresh-in-progress remains visible on fresh or stale Home success without causing duplicate repository refreshes from recomposition or page navigation.
- Cached/stale success and refresh-failed-with-cache states keep forecast pages usable while presenting stale/source/update/failure context at an appropriate semantic location.
- Source/update and provenance remain reachable from success and stale success states.
- Page selection, tab navigation, swipe navigation, refresh, retry, About, and Change location controls do not corrupt selected-location or forecast state.
- No sample-weather, fabricated forecast, hidden default location, or provider-specific DTO/error data appears in the production Home path.
- Focused Compose tests cover user-visible paged operational-state behavior. State-holder tests cover refresh, retry, selected-location isolation, and duplicate-refresh prevention where those behaviors are claimed.
- Installed-app or Android-boundary screenshot and hierarchy evidence cover stale/cache or refresh-failed-with-cache presentation through the real Home UI.
- Installed-app or Android-boundary evidence covers retryable no-cache presentation for a selected location with no useful cache and network unavailable. If the deterministic harness cannot produce this state, record the exact blocker and the closest lower-boundary evidence used instead.
- `git diff --check` must pass.

Explicitly out of scope:
- New weather providers, installed-app MET Norway fallback activation, provider fallback redesign, or provider endpoint changes.
- Small production fixes in app UI/state code are allowed when directly required by 18F evidence.
- Provider changes, cache schema changes, Room/DataStore redesign, repository fallback changes, or lifecycle architecture changes stop this cycle and require a separate plan unless the change is a minimal defect fix with focused regression coverage and no contract expansion.
- New weather values, domain fields, unit preferences, saved-location list switching, official alert lookup, air quality, radar, background refresh, persisted appearance settings, release readiness, or MVP readiness.
- Redesigning Now, Hourly, Daily, or Details visual baselines beyond small operational-state adjustments needed for correctness.
- Placeholder screens, TODO-only paths, unused abstractions, sample-weather production fallback, or hard-coded production success.

## Implementation Plan

1. Baseline and discover:
   - Inspect the current Home state holder, presentation mapper, pager UI, and existing Home Compose instrumentation.
   - Run focused baseline checks before production edits:
     `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecast*'`
     `. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest`
   - If a baseline command cannot run, record the exact command and blocker before deciding whether to proceed.

2. Operational-state contract review:
   - Enumerate the existing app states that can be produced by the current production path: loading, fresh success, refresh-in-progress, stale restored cache, stale after refresh failure, retryable no-cache error, and retry.
   - Compare each state against the Standard Home page model and existing tests.
   - Treat missing coverage as a test gap; treat a user-visible empty/misleading state as an implementation defect.
   - Record an operational-state matrix before claiming `covered` or `verified`:
     `state | production path or fixture | Compose evidence | state-holder evidence | installed-app/Android-boundary evidence | result/blocker`
   - Known drift: About/Data Sources copy currently understates installed-app offline cache behavior relative to README and cycle history. If 18F changes About or disclosure surfaces, correct this in scope; otherwise defer it to a documentation/status sync gate before any release-readiness claim.

3. Focused implementation:
   - Prefer changes in the Home UI/state presentation layer when the issue is page behavior or status placement.
   - Keep provider-neutral data flow through `HomeForecastPresentationState` and existing dashboard presentation data.
   - Avoid provider, repository, DataStore, Room, Gradle, manifest, and dependency changes. If a focused defect appears to require one of those changes, stop and split the work unless it is a minimal defect fix with focused regression coverage and no contract expansion.

4. Focused tests:
   - Add or adjust Home state-holder tests for refresh, retry, duplicate-refresh prevention, and selected-location/page-state isolation only where state behavior is involved.
   - Add or adjust Compose instrumentation for every user-visible operational-state claim: loading, retryable no-cache error, refresh-in-progress, fresh success, cached/stale success, refresh-failed-with-cache, source/provenance reachability, tab/swipe stability, compact width, large font, and sibling non-overlap.
   - Verify that retry and refresh invoke the expected existing callbacks for the selected location and do not fabricate success.

5. Real-path exercise:
   - Use the deterministic DataStore/Room seeding path to launch the installed app with a selected location and cached forecast, disable emulator network, and capture a stale/cache or refresh-failed presentation through the real Home UI.
   - Capture a retryable no-cache installed-app or Android-boundary state from a selected location with no useful cache and network unavailable. If the harness cannot produce it, record the exact blocker and closest lower-boundary evidence.
   - Save screenshots, hierarchy dumps, and logs under:
     `.codex/test-artifacts/2026-09-01-home-operational-state-integration/`

6. Broad verification:
   - `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
   - `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
   - `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
   - `git diff --check`

7. Review and ready:
   - Review production and test diffs for scope creep, provider leakage, fabricated values, operational-state regressions, and unrelated churn.
   - Record the operational-state matrix with one row per claimed state before moving any state beyond `covered` or `verified`.
   - Record focused evidence, installed-app artifact paths, changed production/test files, broad evidence, and any commands not run.
   - Do not claim provider fallback, saved-location switching, unit preferences, alerts, background refresh, release readiness, or MVP readiness.

## Phase Results

- specified: Slice 18F is specified in `.codex/plans/mvp-roadmap.md`.
- planned: This cycle selects only Home Operational State Integration.
- covered: Focused Home state-holder tests cover restored-cache foreground refresh failure retention and duplicate-refresh prevention while preserving existing loading, fresh success, stale success, retryable no-cache error, retry, selected-location isolation, and refresh success/failure behavior. Focused Home Compose instrumentation covers fresh success, restored-cache success, stale/refresh-failed success, refresh-in-progress, loading, retryable no-cache error, source/provenance reachability, tab/swipe navigation, compact width, large font, and sibling non-overlap.
- implemented: `OxygenAppStateHolder` now ignores `onHomeForecastRefresh()` while the selected Home forecast is already refresh-in-progress, and preserves visible restored/stale cached Home content as `StaleAfterFailedRefresh` if a foreground refresh failure arrives after cached content is already visible.
- verified: Focused baseline before production edits passed with `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecast*'`. Initial connected baseline command `. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest` was blocked by `No connected devices!`; after starting `oxygen_starter`, the same connected HomeDashboard suite passed 15 tests on `oxygen_starter(AVD) - 17`.
- verified: Installed-app cached/offline evidence used manual APK install, deterministic DataStore/Room seed instrumentation, disabled emulator network, launched `com.oxygen.weather/.MainActivity`, and captured `.codex/test-artifacts/2026-09-01-home-operational-state-integration/installed-cached-now.png` plus `.codex/test-artifacts/2026-09-01-home-operational-state-integration/installed-cached-now.xml`. The hierarchy shows `Android Installed Screenshot City`, `Now`, `Page 1 of 4`, `Cached forecast`, `Refresh failed: Refresh could not reach the weather service or network.`, `65 deg F`, and `Open-Meteo | Fetched Aug 22, 7:00 AM CDT`.
- verified: Installed-app retryable no-cache evidence cleared app data, seeded a selected location with no useful Room cache through `OfflineLaunchPersistenceInstrumentedTest#startupWithSelectedLocationAndNoRoomCacheRendersRetryableNoCacheError`, disabled emulator network, launched `com.oxygen.weather/.MainActivity`, and captured `.codex/test-artifacts/2026-09-01-home-operational-state-integration/installed-no-cache-error.png` plus `.codex/test-artifacts/2026-09-01-home-operational-state-integration/installed-no-cache-error.xml`. The hierarchy shows `Android No Cache Launch City`, the no-cache network message, `Retry`, `Settings / About`, `Change location`, and Open-Meteo disclosure without page tabs pretending forecast content exists.
- verified: Broad checks passed: `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`; `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`; `. scripts/android-env.sh && ./gradlew :app:assembleDebug`; `git diff --check`.
- review: Production/test diff is scoped to Home state presentation and tests. Static search found `SampleWeather` only in scaffold/sample packages, not in `OxygenApp` or `MainActivity`; no provider DTO/error data was added to UI. No provider behavior, repository/cache schema, DataStore/Room design, installed-app MET Norway fallback, saved-location switching, unit preferences, alerts, air quality, radar, background refresh, persisted appearance settings, release readiness, or MVP readiness is claimed.

## Operational-State Matrix

| State | Production path or fixture | Compose evidence | State-holder evidence | Installed-app/Android-boundary evidence | Result/blocker |
|---|---|---|---|---|---|
| Loading | `HomeForecastPresentationState.Loading.from(selectedLocation)` from selected-location Home load | `loadingKeepsAboutAndDisclosureReachable` | Existing selected manual candidate and startup tests | Covered by HomeDashboard connected suite after AVD start | verified |
| Fresh success | `WeatherRepositoryResult.Success(..., ForecastFreshness.Fresh)` mapped to `ForecastReady` | `freshSuccessRendersSemanticHomePagesAndPreservesDashboardContent` | `repository success becomes visible non-loading terminal home state` | Covered by HomeDashboard connected suite after AVD start | verified |
| Refresh-in-progress | Ready Home receives `Loading` for the same selected location | `refreshInProgressKeepsDashboardAccessibleAndRefreshDisabled` | `explicit home refresh while ready keeps previous dashboard visible` | Covered by HomeDashboard connected suite after AVD start | verified |
| Duplicate refresh prevention | `onHomeForecastRefresh()` while `ForecastReady.isRefreshInProgress` | `oxygenAppRefreshClickRequestsExactSelectedLocationOnce` and disabled refresh UI coverage | `duplicate home refresh while refresh is in progress does not start another repository request` | Covered by HomeDashboard connected suite after AVD start | verified |
| Restored cached success | DataStore selected location plus useful Room/cache bundle before refresh terminal result | `restoredCacheSuccessKeepsForecastContentAndStatusReachableAcrossPages` | `startup restores selected location and matching useful cache before refresh completes` | `installed-cached-now.png` and `.xml` | verified |
| Refresh-failed with cache | Cached repository returns stale success after eligible provider failure | `staleSuccessKeepsForecastContentRefreshAndRefreshFailureVisible` | `stale success after failed refresh keeps dashboard visible with refresh metadata`; `startup restored cache remains visible when foreground refresh fails` | `installed-cached-now.png` and `.xml` | verified |
| Retryable no-cache error | Selected location with no useful cache and network/provider failure | `noCacheErrorKeepsAboutDisclosureAndRetryReachable` | `startup with selected location and no cache renders retryable no-cache error after offline refresh`; existing retry tests | `installed-no-cache-error.png` and `.xml` | verified |
| Retry action | `onHomeForecastRetry()` uses current selected location | Existing HomeDashboard retry/error coverage | `home retry requests weather again for same selected location`; obsolete retry isolation test | Covered by focused unit and HomeDashboard connected suite | verified |
| Source/update/provenance | Provider-neutral `HomeSuccessPresentation.source` and footer disclosure | Fresh, stale, restored-cache, Details, and compact-source Compose tests | Existing Open-Meteo/MET Norway/unavailable disclosure state-holder tests | Cached installed hierarchy includes Open-Meteo fetched/update text | verified |

# Active Cycle

Status: ready
Cycle ID: 2026-08-31-details-page-visual-baseline
Mode: feature
Goal: Implement Slice 18E: Details Page Visual Baseline without adding provider capabilities, fabricated values, or unrelated behavior changes.
Roadmap context: Slice 18E follows committed Slice 18D and precedes Slice 18F operational-state integration.
Branch or work context: local `main` contains `108a1f8` (`Add Daily page visual baseline`), `bf4913e` (`Restore manual location change path`), `6e45bbc` (`minor config change for emulator network access.`), and `411aea5` (`Sync project status after Daily baseline`). The worktree also contains an uncommitted documentation correction to `docs/OXYGEN_FULL_SPECIFICATION.md` aligning section 53 with Slice 18E.

## Contract

Selected behavior:
- Implement a real Details page visual baseline in the existing Standard Home pager.
- Present already-supported secondary weather measurements, sun information, source/update details, and provenance in a coherent, information-dense layout.
- Make fresh-data provenance readily accessible with tertiary visual weight.
- Make stale/cache/refresh-failure context more prominent when existing `HomeForecastFreshness` state indicates restored or stale data.
- Preserve missing-value honesty by omitting unavailable metrics or using existing unavailable wording, without hard-coded defaults or sample success.

Acceptance boundary:
- The installed app's Details page shows structured metric groups rather than one undifferentiated label/value list.
- Details uses only existing provider-neutral app presentation data from `HomeForecastPresentationState.ForecastReady.dashboard`.
- UV or any other future metric appears only if it is already exposed by the provider-neutral Home dashboard presentation data before this slice.
- Source, fetched time, issued time when present, license when present, forecast disclosure, and privacy note remain reachable.
- Details changes do not intentionally alter existing Now, Hourly, Daily, loading, retry/no-cache, refresh, About, manual location, selected-location persistence, forecast cache, or provider behavior. Manual-location, persistence, cache, or provider-specific checks are required only if those paths are touched.
- At normal `360dp x 640dp` / `fontScale = 1.0`, Details shows its title, at least two structured groups, and source/update summary in the first viewport without required page-level scrolling when fixture data supports that density. Large-font overflow may still scroll to preserve accessibility.
- Focused app unit tests cover provider-neutral presentation data and missing-value honesty. Home Compose instrumentation covers Details section semantics, section tags, provenance reachability, stale/failure prominence, compact width, large font, and sibling non-overlap.
- Details contains observable section labels or tags for applicable status, comfort/current metrics, wind, atmosphere, sun when available, and source/provenance.
- Installed-app screenshot and hierarchy evidence show the Details page title, at least two structured metric groups when fixture data supports them, source/update details, and reachable disclosure/privacy text.
- `git diff --check` must pass.

Explicitly out of scope:
- New weather providers or installed-app MET Norway fallback activation.
- Provider DTO, parser, mapper, client, repository, cache, DataStore, Room, Gradle, manifest, or dependency changes.
- New domain fields solely to populate Details, including fake or prematurely wired UV presentation.
- Displaying UV or other future metrics unless they already reach `HomeForecastPresentationState.ForecastReady.dashboard`.
- Fallback-specific Details treatment beyond existing Home presentation states. Installed-app MET Norway fallback has no active Home freshness state yet, so fallback-specific prominence remains deferred to installed-app fallback completion or Slice 18F operational-state work.
- Unit preferences, saved-location list switching, official alert lookup, air quality, radar, background refresh, persisted appearance settings, release readiness, or MVP readiness.
- Placeholder screens, TODO-only paths, unused abstractions, sample-weather production fallback, or hard-coded production success.

## Implementation Plan

1. Baseline and discover:
   - Inspect current Details implementation and Home presentation mapper.
   - Run focused baseline app tests before production edits:
     `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecast*'`
   - If the focused baseline cannot run, record the exact command, failure/blocker, and whether implementation proceeds from existing committed evidence before editing production code.

2. Presentation design:
   - Keep data flow provider-neutral.
   - Prefer UI grouping in `HomeLoadingScreen.kt`.
   - Only introduce mapper-level presentation structure in `HomeForecastPresentationMapper.kt` if it removes real UI string/grouping duplication.
   - Group existing metrics into small semantic sections such as comfort, wind, atmosphere, sun, and source/provenance.

3. Focused implementation:
   - Replace the current simple Details composition with compact structured sections.
   - Add a top Details status block only for existing restored-cache or stale-after-refresh-failure states.
   - Preserve the existing `ProviderDisclosure` path and source display.
   - Do not add values that are not already present in `dashboard.metrics`, `dashboard.sun`, or `dashboard.source`.

4. Focused tests:
   - Add/adjust app state tests for metric null honesty and provider-neutral presentation data only.
   - Add/adjust Compose instrumentation for structured Details groups, provenance reachability, stale/failure prominence, compact `360dp` and large-font bounds, and sibling spacing.
   - Add/adjust Compose instrumentation so normal `360dp x 640dp` / `fontScale = 1.0` fixture Details shows its title, at least two structured groups, and source/update summary in the first viewport without requiring page-level scrolling.
   - Run focused Home connected instrumentation:
     `. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest`

5. Real-path exercise:
   - Use the existing deterministic DataStore/Room seeding path to launch the installed app offline, navigate to Details through the real Home UI, and capture evidence after the app shows `Details` / `Page 4 of 4`.
   - Save Details screenshot and hierarchy under:
     `.codex/test-artifacts/2026-08-31-details-page-visual-baseline/`

6. Broad verification:
   - `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
   - `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
   - `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
   - `git diff --check`

7. Review and ready:
   - Review production and test diffs for scope creep, provider leakage, fabricated values, and unrelated churn.
   - Confirm the final diff has no provider/cache/DataStore/Room/Gradle changes, no new domain fields solely for Details, no hard-coded metric values, no sample-weather production path, no MVP/release/fallback completion claims, and no tests that only prove symbols exist.
   - Record focused Details evidence, broad evidence, installed-app artifact paths, changed production/test files, and any commands not run.
   - Do not claim full manual-location, persistence, cache, or provider-path reverification unless matching focused commands or installed-app exercises were actually run.

## Phase Results

- specified: Slice 18E is specified in `.codex/plans/mvp-roadmap.md` and `docs/OXYGEN_FULL_SPECIFICATION.md` section 53.
- planned: This cycle selects only the Details page visual baseline described above.
- covered: Focused HomeForecast app unit baseline passed before production edits. Final focused HomeForecast app unit tests passed and Home Compose instrumentation passed 14 tests on `oxygen_starter(AVD) - 17`; coverage includes Details structured groups, compact first-viewport source/update summary, missing metric group omission, stale/failure prominence, provenance reachability, compact width, large font, and sibling non-overlap.
- implemented: `HomeLoadingScreen.kt` replaces the flat Details metrics list with provider-neutral Details status, Comfort, Wind, Atmosphere, Source/update, Sun, and provenance sections using only `HomeForecastPresentationState.ForecastReady.dashboard` data. `OfflineLaunchPersistenceInstrumentedTest.kt` expands the deterministic installed-app screenshot fixture with already-supported current-condition metrics so real-path Details evidence contains multiple metric groups.
- verified: Offline installed-app launch from deterministic DataStore selected location plus Room cached forecast was exercised with emulator network disabled. Details was reached through the real Home tab UI and captured at `.codex/test-artifacts/2026-08-31-details-page-visual-baseline/details-after-installed.png`; hierarchy evidence is `.codex/test-artifacts/2026-08-31-details-page-visual-baseline/details-after-installed.xml`.
- verified: Broad checks passed: `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`, `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`, `. scripts/android-env.sh && ./gradlew :app:assembleDebug`, and `git diff --check`. Logs are saved under `.codex/test-artifacts/2026-08-31-details-page-visual-baseline/`.
- not run: No manual-location, selected-location persistence, cache, provider-client, Room production, DataStore production, or MET Norway fallback-specific regression suite beyond the seeded installed-app exercise was run, because this slice did not change those production paths.

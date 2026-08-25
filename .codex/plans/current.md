# Active Cycle

Status: planned
Cycle ID: 2026-08-25-in-app-disclosure-surface
Mode: feature
Goal: Implement Slice 15 by adding a real in-app Settings/About disclosure surface that is reachable through visible app navigation and whose Data Sources, Privacy, and Open Source Licenses content matches the currently implemented app/provider behavior without claiming unimplemented installed-app forecast fallback, alerts, cache, saved locations, unit settings, or release readiness.
Roadmap slice: Slice 15: In-App About, Privacy, Licenses, and Data-Source Surface from `.codex/plans/mvp-roadmap.md`.
Branch or work context: local `oxygen` Android scaffold.
Specification anchors:
- `docs/OXYGEN_FULL_SPECIFICATION.md` sections 1, 4, 5, 6.1, 6.2, 6.3, 12.1, 14, 30, 31, 40, 41, 44, 46, 48, and 50
- `.codex/plans/mvp-roadmap.md` Slice 15, UI Rule, Forecast Provider Scope, MVP Acceptance Boundary, and Release Gate
- `docs/data-sources/OPEN_METEO_FORECAST.md`
- `docs/data-sources/OPEN_METEO_GEOCODING.md`
- `docs/data-sources/MET_NORWAY_FORECAST.md`
- `DATA_SOURCES.md`, `PRIVACY.md`, `THIRD_PARTY_LICENSES.md`, `NOTICE`, and `LICENSE`
- Existing Slice 14 `FallbackWeatherRepository` as committed core fallback-selection evidence, not installed-app fallback wiring evidence
- `AGENTS.md`

Acceptance criteria:
- Add a user-facing Settings/About path in `:app` production code that is reachable from the existing first-run and Home app surfaces through visible navigation. The path must not depend on hidden gestures, debug-only entry points, sample/scaffold screens, or Android system settings.
- Settings/About must expose three distinct visible surfaces: Data Sources, Privacy, and Open Source Licenses. A user must be able to enter each surface and return to the prior app surface without losing the selected location, first-run search query/results, or current Home forecast presentation state.
- In-app Data Sources must split provider status into evidence-backed categories: active app providers, implemented provider paths/capabilities, and roadmap-only providers.
- Active app providers must list only Open-Meteo forecast as the installed-app default forecast provider and Open-Meteo Geocoding / GeoNames as the active location-search provider.
- MET Norway must be listed as an implemented forecast provider path and verified core fallback-selection capability from Slice 14, not as an active installed-app forecast fallback. The disclosure must explicitly say installed-app fallback wiring, fallback Home UI verification, cache persistence, and stale offline UI are not yet implemented/verified.
- In-app Data Sources must separately identify roadmap-only providers where shown, including NOAA/NWS alerts, Environment and Climate Change Canada alerts, and Open-Meteo/CAMS air quality. It must not present alerts, air quality, radar, cache persistence, saved locations, unit settings, or installed-app fallback wiring as implemented.
- Update root `DATA_SOURCES.md` and `PRIVACY.md` so repository-level disclosure matches the post-Slice-14 state: MET Norway is no longer roadmap-only as a provider/repository capability, but Oxygen still must not claim active installed-app forecast fallback, cache persistence, live fallback UI screenshots, alert behavior, or release-candidate status.
- In-app Privacy must disclose no ads, no behavioral tracking, no mandatory account, optional location permission, manual search without permission, Open-Meteo forecast request data, Open-Meteo geocoding request data, and MET Norway implemented-provider/fallback-capability request data including selected coordinates, optional altitude when present, identifying User-Agent/contact header, IP/network metadata, and provider log/privacy implications from the contract.
- In-app Open Source Licenses must separate Oxygen source-code licensing from weather-data attribution/licensing and must not imply government/provider endorsement. It may present concise license/notice text already maintained in root docs; it must not fabricate dependency license inventories beyond the current repository disclosure.
- Home success must continue to show visible source, update, and provenance for the provider that served the displayed forecast. Slice 15 may adjust disclosure copy to account for implemented core fallback behavior, but it must not remove provider-specific provenance from successful forecast presentation.
- For this reviewed plan, leave the default production forecast wiring unchanged as `OpenMeteoWeatherRepository()` in `OxygenAppStateHolder`. Do not compose `FallbackWeatherRepository(OpenMeteoWeatherRepository(), MetNoWeatherRepository())` in Slice 15. MET Norway Home provenance coverage for this slice is fixture-backed presentation evidence only, not installed-app fallback evidence.
- Provider DTOs, provider HTTP headers, raw terms text, raw response bodies, provider cache metadata, provider-specific error bodies, and provider IDs must not leak into Composables or presentation state except where existing provider-neutral provenance already exposes provider identity/source/license.
- The disclosure surface must be usable with large text and narrow phone width: no overlapping text, no clipped required provider names, and no card-in-card layout. Use concise rows/sections with stable spacing and scrollable content. Produce either focused UI/state test evidence or an emulator screenshot/log exercising long provider names at narrow width and enlarged font scale.

Acceptance boundary: Slice 15 is complete when focused app tests prove that Settings/About navigation is reachable from first-run and Home states, Data Sources/Privacy/Open Source Licenses surfaces contain the required active-app, implemented-capability, and roadmap-only-provider disclosure without unimplemented behavior claims, returning from the surfaces preserves prior app state, and Home success provenance remains tied to the served provider for Open-Meteo success plus controlled MET Norway success fixtures. Repository docs must match the in-app provider-status disclosure. Compose/emulator exercise must show the in-app disclosure path and at least one disclosure surface on a real debug build. MET Norway Home provenance evidence is fixture-backed presentation evidence unless a later slice wires app fallback. Slice 15 is not verified by installed-app forecast fallback, alert lookup, cache persistence, offline stale UI, saved-location persistence, unit preferences, background work, provider health/backoff, Open Source license generation automation, Play Store release checks, or live weather-provider requests.

Boundary decisions:
- Implement the Settings/About surface inside the existing `:app` architecture with presentation-state objects and Compose screens. Do not introduce Navigation Compose, DataStore, Room, Markdown rendering, WebView, a database-backed content system, dependency license scanners, remote content, or a broad settings framework for this slice.
- Use static, version-controlled disclosure content derived from root docs and provider contracts. Keep it concise enough for the app UI while preserving the required facts. Do not copy full provider contracts into the app.
- Treat `DATA_SOURCES.md` and `PRIVACY.md` as disclosure authorities for the in-app copy after this slice updates them. Keep weather-data attribution separate from Oxygen source-code license/notice text.
- Prefer provider-neutral presentation objects such as `AboutSurfaceState`, `DataSourceDisclosure`, and `PrivacyDisclosure` in `:app`. Do not add provider DTO imports to app UI.
- Do not add official alert providers, alert routes, cache tables, saved locations, unit controls, provider preference controls, endpoint controls, app-level forecast fallback wiring, backoff state, telemetry, or analytics.
- Do not perform live Open-Meteo or MET Norway forecast requests as acceptance evidence. Existing repository and fallback behavior are already covered by provider/core tests; this slice verifies disclosure and app navigation behavior.

Focused evidence to produce:
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*About*' --tests '*OxygenApp*' --tests '*HomeForecast*'`
- Focused UI/state evidence for narrow-width or enlarged-font disclosure rendering with long provider names. Save the test log or screenshot under `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface/` and record the project-local path in Phase Results.
- Static no-provider-implementation-leak check: `rg -n "OpenMeteoForecastResponse|OpenMeteoCurrent|OpenMeteoHourly|OpenMeteoDaily|OpenMeteoForecastClientResult|OpenMeteoForecastClientError|OpenMeteoGeocodingDto|OpenMeteoGeocodingResult|MetNoForecastResponse|MetNoGeometry|MetNoMeta|MetNoTimeStep|MetNoInstant|MetNoPeriod|MetNoForecastClientResult|MetNoForecastClientError|X-ErrorClass|symbolCode|weather_code" app/src/main/kotlin`
- Static disclosure consistency check: `rg -n "MET Norway|Open-Meteo|GeoNames|NOAA|Environment and Climate Change Canada|Open-Meteo/CAMS|active|roadmap|fallback|tracking|account|permission" DATA_SOURCES.md PRIVACY.md app/src/main/kotlin/com/oxygen/weather/app`
- Save focused test and static-check logs under `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface/` and record the project-local paths in Phase Results before reporting the slice ready.

Real-path command or procedure:
- Build/install the debug APK and exercise the Settings/About path on the emulator from first-run and, where practical, Home. Capture screenshots of the Settings/About entry and one disclosure surface under `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface/`.
- If Slice 15 unexpectedly changes default app forecast wiring, add real-path or debug-build state evidence showing MET Norway-served Home provenance through the production app path. Under this reviewed plan, app forecast wiring is intentionally unchanged and no installed-app MET Norway fallback evidence is claimed.
- If emulator input or launch is blocked, record the exact command, failure, and any partial artifact. Do not claim real-path verification without an installed debug build showing the disclosure UI.

Broad verification commands:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`
- Save broad verification logs under `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface/` and record the project-local paths in Phase Results before reporting the slice ready.

Current gate: verified
Current phase: ready
Last result: Slice 15 implemented and verified. The installed app now exposes a visible Settings/About path from first-run and Home, with Data Sources, Privacy, and Open Source Licenses surfaces. Root provider/privacy disclosures match the post-Slice-14 status: Open-Meteo forecast and Open-Meteo Geocoding/GeoNames are active app providers; MET Norway is an implemented provider path and core fallback-selection capability, not active installed-app fallback wiring; alerts, air quality, radar, cache persistence, saved locations, and unit settings remain unimplemented.
Blocker: none.

## Implementation Plan

1. Add focused app tests for a Settings/About presentation path reachable from first-run and Home, including back navigation/state preservation and distinct Data Sources, Privacy, and Open Source Licenses surfaces.
2. Add focused tests for disclosure content: active Open-Meteo forecast, implemented MET Norway provider path and core fallback-selection capability after Slice 14, active Open-Meteo Geocoding / GeoNames search, roadmap-only alert/air-quality providers, no unimplemented installed-app fallback/cache/saved-location/unit/alert claims, no ads/tracking/account, optional permission, and provider request/privacy facts.
3. Add focused Home provenance tests using controlled repository results for Open-Meteo success and MET Norway provider success fixtures so Home source/update/license remains derived from served provider provenance without claiming installed-app fallback wiring.
4. Implement minimal app presentation state and actions for opening Settings/About, selecting disclosure surfaces, and returning to the previous first-run or Home state without broad navigation-framework, persistence, or forecast-wiring changes.
5. Implement Compose screens for Settings/About and each disclosure surface using existing Material 3/Compose patterns, visible navigation controls, scrollable content, stable spacing, and no provider DTO imports.
6. Update `DATA_SOURCES.md` and `PRIVACY.md` to match post-Slice-14 provider capability disclosure while keeping active installed-app providers, implemented core/provider capabilities, and future behaviors clearly separate.
7. Compare `README.md`, `DATA_SOURCES.md`, `PRIVACY.md`, and in-app disclosure for user-visible provider-status contradictions. Make only minimal README status edits if the Slice 15 disclosure would otherwise conflict, or record a separate documentation cleanup slice before release-facing claims.
8. Run focused app tests and static checks for provider implementation leakage and disclosure consistency.
9. Run broad Android verification commands and `git diff --check`.
10. Exercise the disclosure path on the emulator/debug build, save screenshots/logs, and record command-backed evidence in this file. Append `.codex/cycles/history.md` only when the slice is actually verified or committed.

## Known Non-Blocking Drift

- `README.md` still contains scaffold-era wording that says no network weather provider has been wired yet and the screen displays `SampleWeather.bundle`. Do not fix it in Slice 15 unless the implementation directly touches release-facing disclosure copy; a separate documentation cleanup slice can reconcile broader README status after app disclosure is verified.
- Existing app production wiring still constructs `OpenMeteoWeatherRepository()` directly as of the start of this plan. Slice 15 will leave this unchanged and must leave the distinction explicit in Data Sources/Privacy copy: MET Norway is implemented and verified for provider/repository fallback selection, but it is not active installed-app forecast fallback wiring.
- There is no saved-location persistence, offline forecast cache, unit preference UI, official alert provider, radar provider, or air-quality provider at the start of this plan.

## Phase Results

- planned: Selected Slice 15 as the next bounded implementation slice after committed Slice 14. Acceptance is limited to observable Settings/About disclosure navigation, synchronized repository/in-app provider and privacy disclosures, and continued Home provenance for served forecast data.
- review-updated: Incorporated `.codex/review/findings.md` pre-implementation review. The plan now chooses the conservative non-breaking path: no app forecast fallback wiring in Slice 15; MET Norway is disclosed as an implemented provider path and core fallback-selection capability, not active installed-app fallback. Added evidence requirements for narrow/large-text disclosure rendering and final README/Data Sources/Privacy/in-app provider-status consistency review.
- covered: Added focused app tests in `app/src/test/kotlin/com/oxygen/weather/app/AboutDisclosureStateHolderTest.kt` covering Settings/About reachability from first-run and Home, state preservation on return, Data Sources/Privacy/Open Source Licenses disclosure content, no unimplemented provider-behavior claims, controlled MET Norway Home provenance presentation, and compact disclosure paragraph bounds for narrow/large-text rendering.
- implemented: Added `AboutDisclosureContent`, `OxygenAppScreen.About`, state-holder actions for opening/selecting/backing out of About, and `ui/about/AboutScreen`. Wired visible `Settings / About` buttons into first-run and Home. Updated `DATA_SOURCES.md` and `PRIVACY.md` to distinguish active app providers, implemented provider paths/capabilities, and roadmap-only providers without changing default app forecast wiring from `OpenMeteoWeatherRepository()`.
- verified: Focused tests passed with `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*About*' --tests '*OxygenApp*' --tests '*HomeForecast*'`; log saved at `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface/focused-app-tests.log`.
- verified: Static no-provider-implementation-leak check returned no matches; log saved at `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface/static-no-provider-implementation-leak.log`.
- verified: Static disclosure consistency check passed and README/Data Sources/Privacy/in-app consistency review was recorded. Logs saved at `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface/static-disclosure-consistency.log` and `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface/readme-data-privacy-app-consistency.log`. README still has the known scaffold-era drift and was left unchanged for a separate cleanup slice.
- verified: Broad checks passed: `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`, `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`, `. scripts/android-env.sh && ./gradlew :app:assembleDebug`, and `git diff --check`. Logs saved at `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface/broad-compile-debug-kotlin.log`, `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface/broad-unit-tests.log`, `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface/broad-assemble-debug.log`, and `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface/git-diff-check.log`.
- verified: Real debug APK install and launch passed through `scripts/install-debug.sh`; log saved at `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface/install-debug.log`. Emulator screenshots show first-run Settings/About entry, About overview, Data Sources surface, Home with Settings/About entry, and Home-to-About overview at `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface/first-run-about-entry.png`, `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface/about-overview.png`, `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface/data-sources-surface.png`, `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface/home-about-entry-or-status.png`, and `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface/home-about-overview.png`. Home screenshots are installed-app navigation evidence only, not live provider acceptance evidence.
- verified: `uiautomator dump` returned `ERROR: null root node returned by UiTestAutomationBridge` on the headless emulator after screenshot capture; this did not block screenshot-based real-path verification and no UIAutomator XML evidence is claimed.

# Active Cycle

Status: verified
Cycle ID: 2026-08-25-in-app-disclosure-surface-r1
Mode: feature
Goal: Resolve the Slice 15 implementation review findings without broadening app behavior: make Home forecast footer disclosure follow the provider that served the displayed forecast, remove present-tense saved-location claims from `PRIVACY.md`, correct README scaffold drift that contradicts current provider paths, and produce real narrow-width/enlarged-font disclosure UI evidence.
Roadmap slice: Slice 15r1: Review Revision for Slice 15 In-App Disclosure Surface.
Branch or work context: local `oxygen` Android scaffold on top of committed Slice 15 `a020ebd`.
Specification anchors:
- `AGENTS.md`
- `.codex/review/findings.md` Slice 15 implementation review findings from 2026-08-25
- Committed Slice 15 implementation `a020ebd Implement in-app disclosure surface`
- `docs/OXYGEN_FULL_SPECIFICATION.md` sections 1, 4, 5, 6.1, 6.2, 6.3, 12.1, 14, 30, 31, 40, 41, 44, 46, 48, and 50
- `.codex/plans/mvp-roadmap.md` Slice 15, UI Rule, Forecast Provider Scope, MVP Acceptance Boundary, and Release Gate
- `docs/data-sources/OPEN_METEO_FORECAST.md`
- `docs/data-sources/OPEN_METEO_GEOCODING.md`
- `docs/data-sources/MET_NORWAY_FORECAST.md`
- `DATA_SOURCES.md`, `PRIVACY.md`, `THIRD_PARTY_LICENSES.md`, `NOTICE`, and `LICENSE`
- Existing Slice 14 `FallbackWeatherRepository` as committed core fallback-selection evidence, not installed-app fallback wiring evidence

Acceptance criteria:
- Home forecast success must not render a hardcoded Open-Meteo footer when the served provider provenance is not Open-Meteo.
- Home success must continue to show visible source, update, data type, license/provenance, and footer disclosure for the provider that served the displayed forecast. Controlled Open-Meteo success must disclose Open-Meteo; controlled MET Norway success must disclose MET Norway. Provider IDs, DTOs, provider HTTP headers, raw response bodies, and provider-specific error bodies must not leak into Composables or presentation state beyond provider-neutral provenance fields.
- The default production forecast wiring must remain `OpenMeteoWeatherRepository()` in `OxygenAppStateHolder`. Slice 15r1 must not wire installed-app fallback, cache persistence, saved-location persistence, unit settings, alert lookup, air-quality lookup, radar, background work, provider health/backoff, or release behavior.
- In-app Data Sources, Privacy, and Open Source Licenses surfaces must keep the Slice 15 provider-status boundaries: Open-Meteo forecast and Open-Meteo Geocoding / GeoNames are active app providers; MET Norway is an implemented provider path and core fallback-selection capability, not active installed-app forecast fallback; NOAA/NWS alerts, Environment and Climate Change Canada alerts, and Open-Meteo/CAMS air quality remain roadmap-only.
- `PRIVACY.md` must not claim current saved-location behavior. Manual location search may be stated as implemented/current; saving locations must be framed only as future intended/local behavior until persistence exists.
- `README.md` must no longer say the app screen currently displays only `SampleWeather.bundle` or that no network weather provider is wired. README status must match current evidence: active installed-app Open-Meteo forecast and geocoding paths exist; MET Norway remains implemented core/provider capability, not active installed-app fallback; sample/scaffold data may be described only as a retained preview/scaffold surface if that remains true in code.
- Focused evidence must include a real Compose UI or emulator artifact exercising the disclosure surface at narrow phone width and enlarged font scale with long provider names. A paragraph-length unit test alone is not sufficient evidence for this acceptance item. The selected path for this revision is an emulator screenshot/log procedure, not a new instrumentation-test dependency slice.
- Existing Slice 15 navigation behavior must remain intact: Settings/About remains reachable from first-run and Home, each About surface remains selectable, and returning from About preserves first-run search state or Home forecast state.
- Repository disclosure consistency review must compare `README.md`, `DATA_SOURCES.md`, `PRIVACY.md`, and in-app disclosure after the fixes. README drift about current provider wiring, sample-data status, privacy, or persistence is blocking for this revision; broader roadmap wording may remain only if it does not contradict implemented behavior.

Acceptance boundary: Slice 15r1 is complete when focused app tests fail before the provider-specific Home footer fix and pass after it, `PRIVACY.md` no longer makes a current saved-location claim, `README.md` no longer contradicts active Open-Meteo forecast/geocoding production paths, a rendered narrow/enlarged-font emulator artifact proves the disclosure surface remains readable for long provider names, static leak/disclosure checks pass, and broad Android verification passes. Slice 15r1 does not prove live MET Norway app fallback, installed-app fallback wiring, cache persistence, stale offline UI, saved-location persistence, unit preferences, alert lookup, air-quality lookup, radar, dependency license generation, release checks, or live provider requests.

Boundary decisions:
- Prefer a provider-neutral presentation change that derives Home footer disclosure from existing `DataProvenance.sourceName` and related Home source presentation, rather than branching on provider IDs or importing provider implementation types into app UI.
- Keep the footer concise. If provenance is unavailable, use a provider-neutral unavailable disclosure instead of guessing Open-Meteo.
- Keep doc edits limited to `PRIVACY.md` and the minimal README status correction required to remove current-provider contradictions. Do not broaden README into a roadmap rewrite.
- Use the existing app state-holder and Compose structure. Do not add Navigation Compose, DataStore, Room, Markdown rendering, WebView, dependency license scanners, or a broader settings framework.
- Use artifact directory `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface-r1/` for all new logs/screenshots. Do not modify or replace prior Slice 15 evidence.

Focused evidence to produce:
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*About*' --tests '*HomeForecast*'`
- Save a pre-fix red/baseline focused-test log after adding the Home footer tests and before changing production footer derivation. The log must show the provider-specific Home footer test failure that proves the current hardcoded Open-Meteo behavior.
- A focused test proving controlled MET Norway success renders a MET Norway-specific Home footer disclosure and does not include `Weather data by Open-Meteo` in that footer.
- A focused test proving controlled Open-Meteo success still renders Open-Meteo footer disclosure.
- A focused test or static assertion proving unknown/unavailable provenance does not guess Open-Meteo.
- Focused UI/emulator evidence for narrow-width plus enlarged-font disclosure rendering with long provider names; save the test log or screenshot under `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface-r1/`.
- Static no-provider-implementation-leak check: `rg -n "OpenMeteoForecastResponse|OpenMeteoCurrent|OpenMeteoHourly|OpenMeteoDaily|OpenMeteoForecastClientResult|OpenMeteoForecastClientError|OpenMeteoGeocodingDto|OpenMeteoGeocodingResult|MetNoForecastResponse|MetNoGeometry|MetNoMeta|MetNoTimeStep|MetNoInstant|MetNoPeriod|MetNoForecastClientResult|MetNoForecastClientError|X-ErrorClass|symbolCode|weather_code" app/src/main/kotlin`
- Static disclosure consistency check: `rg -n "MET Norway|Open-Meteo|GeoNames|NOAA|Environment and Climate Change Canada|Open-Meteo/CAMS|active|roadmap|fallback|tracking|account|permission|saved|save locations|Weather data by Open-Meteo" README.md DATA_SOURCES.md PRIVACY.md app/src/main/kotlin/com/oxygen/weather/app`
- Save focused test and static-check logs under `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface-r1/` and record the project-local paths in Phase Results before reporting the slice ready.

Real-path command or procedure:
- Build/install the debug APK and exercise the Settings/About path on an emulator with narrow phone width and enlarged font scale. Capture evidence under `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface-r1/`.
- Use this concrete emulator procedure unless it is blocked: start an emulator with `scripts/start-emulator.sh`; install/launch with `scripts/install-debug.sh`; set a narrow display with `adb shell wm size 360x800`; set enlarged font with `adb shell settings put system font_scale 1.3`; navigate to Settings/About then Data Sources/Privacy/Open Source Licenses; capture screenshots with `adb exec-out screencap -p > .codex/test-artifacts/2026-08-25-in-app-disclosure-surface-r1/<surface>.png`; restore display/font settings with `adb shell wm size reset` and `adb shell settings put system font_scale 1.0` before finishing.
- If emulator startup, display resizing, font-scale changes, navigation, or screenshot capture is blocked, record the exact command, failure, and any partial artifact. Do not claim narrow/enlarged-font verification without a real rendered artifact.
- No live weather-provider requests are required for this revision. Provider-specific Home footer behavior may be verified with controlled repository fixtures.

Broad verification commands:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`
- Save broad verification logs under `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface-r1/` and record the project-local paths in Phase Results before reporting the slice ready.

Current gate: verified
Current phase: ready
Last result: Slice 15r1 implemented and verified. Home forecast success footer disclosure now follows the provider-neutral source name from the served forecast presentation, `PRIVACY.md` no longer claims current saved-location behavior, README current-provider wording matches active Open-Meteo forecast/geocoding paths, and narrow/enlarged-font emulator evidence was captured for the disclosure surface.
Blocker: none.

## Implementation Plan

1. Add or tighten focused app tests for Home forecast footer disclosure: Open-Meteo fixture shows Open-Meteo footer, MET Norway fixture shows MET Norway footer, and unavailable provenance does not default to Open-Meteo.
2. Save the pre-fix red/baseline focused-test log under the Slice 15r1 artifact directory before changing production footer derivation.
3. Implement provider-neutral Home footer disclosure derivation from served forecast provenance/source presentation while preserving existing dashboard provenance behavior.
4. Update `PRIVACY.md` to remove current saved-location wording and keep saved-location persistence framed as future/not implemented.
5. Update `README.md` minimally so its current-status wording no longer contradicts active Open-Meteo forecast/geocoding production paths or the retained sample/scaffold preview boundary.
6. Add emulator screenshot evidence for the About/Data Sources disclosure surface at narrow width and enlarged font scale with long provider names.
7. Run focused app tests plus static provider-leak and disclosure-consistency checks, saving logs under the Slice 15r1 artifact directory.
8. Run broad Android verification commands and `git diff --check`, saving logs under the Slice 15r1 artifact directory.
9. Review `README.md`, `DATA_SOURCES.md`, `PRIVACY.md`, and in-app disclosure for user-visible provider-status contradictions. Any remaining README drift is a blocker unless it is unrelated to current provider, sample-data, privacy, or persistence status.
10. Append `.codex/cycles/history.md` only when Slice 15r1 is actually verified or committed.

## Known Starting Conditions

- `README.md` contains scaffold-era wording that says no network weather provider has been wired yet and the screen displays `SampleWeather.bundle`; this is in scope for a minimal Slice 15r1 correction because it contradicts current active Open-Meteo forecast/geocoding paths. Broader README roadmap cleanup remains out of scope.
- Existing app production wiring still constructs `OpenMeteoWeatherRepository()` by default. Slice 15r1 must leave this unchanged and must not claim active installed-app MET Norway fallback.
- There is no saved-location persistence, offline forecast cache, unit preference UI, official alert provider, radar provider, or air-quality provider at the start of this revision.

## Phase Results

- planned: Selected Slice 15r1 to resolve the Slice 15 implementation review findings with a focused revision: provider-specific Home footer disclosure, corrected privacy wording around saved locations, minimal README current-status correction, preserved pre-fix red evidence for the footer defect, and real narrow/enlarged-font emulator evidence for the disclosure surface.
- covered: Added focused Home footer tests in `app/src/test/kotlin/com/oxygen/weather/app/HomeForecastStateHolderTest.kt` for Open-Meteo success, MET Norway success, and unavailable provenance. Pre-fix red log saved at `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface-r1/pre-fix-red-home-footer-tests.log`; failures were the intended MET Norway footer and unavailable-provenance footer assertions.
- implemented: Updated `OxygenAppStateHolder` so `ForecastReady` derives footer disclosure/privacy text from provider-neutral `HomeSourcePresentation.sourceName` and uses unavailable disclosure when provenance is absent. Kept default production forecast wiring as `OpenMeteoWeatherRepository()`. Reduced first-run, Home, and About screen title typography from `headlineMedium` to `titleLarge` after constrained emulator evidence showed oversized titles could hide controls at `wm size 360x800` with `font_scale 1.3`.
- implemented: Updated `PRIVACY.md` to remove current saved-location wording. Updated `README.md` to disclose active installed-app Open-Meteo forecast/geocoding paths, retained sample weather as scaffold/preview data only, MET Norway as implemented provider/core fallback capability rather than active installed-app fallback, and unimplemented saved-location/cache/unit/alert/air-quality/radar behavior.
- verified: Focused tests passed with `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*About*' --tests '*HomeForecast*'`; log saved at `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface-r1/focused-app-tests.log`.
- verified: Static no-provider-implementation-leak check returned no matches; log saved at `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface-r1/static-no-provider-implementation-leak.log`. Static disclosure consistency check produced the reviewed expected provider/privacy/status matches across README, DATA_SOURCES, PRIVACY, and app disclosure; log saved at `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface-r1/static-disclosure-consistency.log`.
- verified: Real emulator evidence used `scripts/start-emulator.sh`, `scripts/install-debug.sh`, `adb shell wm size 360x800`, and `adb shell settings put system font_scale 1.3`; logs saved at `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface-r1/start-emulator.log`, `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface-r1/install-debug.log`, `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface-r1/install-debug-after-layout-fix.log`, and `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface-r1/narrow-font-setup.log`. Screenshots/hierarchy dumps saved for first-run/About reachability and disclosure surfaces, including `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface-r1/narrow-font-first-run-about-button-visible.png`, `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface-r1/narrow-font-about-overview.png`, `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface-r1/narrow-font-data-sources-met-norway.png`, `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface-r1/narrow-font-data-sources-roadmap.png`, `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface-r1/narrow-font-privacy-met-norway-2.png`, and `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface-r1/narrow-font-open-source-licenses-top.png`. Display size and font scale were restored after capture.
- verified: Broad checks passed: `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`, `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`, `. scripts/android-env.sh && ./gradlew :app:assembleDebug`, and `git diff --check`. Logs saved at `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface-r1/broad-compile-debug-kotlin.log`, `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface-r1/broad-unit-tests.log`, `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface-r1/broad-assemble-debug.log`, and `.codex/test-artifacts/2026-08-25-in-app-disclosure-surface-r1/git-diff-check.log`.

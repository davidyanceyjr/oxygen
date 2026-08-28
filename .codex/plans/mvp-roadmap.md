# Oxygen MVP Release Map

Status: specified
Roadmap ID: mvp-2026-08
Source authority: `docs/OXYGEN_FULL_SPECIFICATION.md`
Created: 2026-08-18

Planning note: This roadmap specifies candidate MVP slices. Only `.codex/plans/current.md` may mark one bounded implementation slice as planned.

## Roadmap Rule

This document is a release map, not an active implementation plan. It records intended MVP behavior order and release gates. It does not make any slice planned, covered, implemented, or verified.

Roadmap entries are not evidence. A slice remains only specified until `.codex/plans/current.md` selects it, production code implements it, and focused plus real-path evidence is recorded.

Before implementation starts, copy one bounded behavior slice from this release map into `.codex/plans/current.md` with its acceptance boundary, focused evidence, real-path exercise, broad checks, and out-of-scope limits. Keep implementation slices small enough to stop at a verified boundary.

## Evidence Rule

Focused evidence means behavior-specific tests at the provider, repository, Android state, or Compose boundary. Live provider checks and emulator/manual exercises are real-path evidence. Gradle compilation, unit-test task execution, assembly, dependency reports, and `git diff --check` are broad checks unless a selected slice defines a narrower reason.

## UI Rule

Every user-facing active slice must carry the relevant UI specification with it. Do not defer UI obligations into a separate polish phase when they are part of the behavior being implemented.

For MVP user-facing slices:
- home remains a vertically scrolling weather dashboard with location header, alert area when present, current-condition hero, hourly forecast, daily forecast, metric grid, sun/update/source information where data exists, and provenance footer;
- important weather semantics remain readable with decorative effects, gradients, transparency, and animation disabled;
- safety information is visible text, not color alone, and does not require hidden gestures;
- UI supports large font, RTL where applicable, meaningful accessibility semantics, adequate touch targets, and logical TalkBack order;
- fixed-format elements such as forecast rows, metric cards, controls, weather marks, and scene containers use stable dimensions to avoid layout shifts;
- provider DTOs never reach Composables; UI receives presentation-ready state derived from domain models.

## MVP Acceptance Boundary

Oxygen MVP is ready when a user can install the app, choose or search a location without granting location permission, view real current/hourly/daily weather from the default Open-Meteo forecast path with verified MET Norway fallback, understand source/update/stale status, save and switch locations, retain the latest forecast offline, view supported official alerts, change units and core presentation settings, and use the app without advertising, tracking, account, cloud dependency, or Google Play Services as a core requirement.

## Forecast Provider Scope

This roadmap follows the full specification forecast provider rule: Open-Meteo is the default MVP forecast provider and MET Norway is the MVP forecast fallback.

MET Norway contract, production client/mapper, and repository fallback selection must be implemented before release-candidate status or before MET Norway appears as an active fallback in Data Sources. Cache schema, provenance metadata, stale-state UI, and provider attribution must be designed against both MVP forecast providers from the start, even if the first cache implementation is verified against Open-Meteo before MET Norway production fallback exists.

Do not present Open-Meteo-only behavior as MVP-complete or release-ready.

## Release Gate

Release-candidate status is blocked unless the roadmap and implementation match `docs/OXYGEN_FULL_SPECIFICATION.md` or the specification has been explicitly amended first.

For the current specification, release verification must prove:
- Open-Meteo default forecast behavior is verified.
- MET Norway fallback forecast behavior is verified.
- Provider provenance is visible for whichever forecast provider served the displayed forecast.
- Data Sources lists active providers only when their production paths can fetch or serve data.
- Forecast provider preference does not disable official alert lookup.

## Slice 1: Open-Meteo Provider Contract

Status: specified

Release intent: Specify the default forecast provider before code is added.

Must prove:
- The provider contract completes every field in `docs/data-sources/PROVIDER_TEMPLATE.md`, including endpoint, authentication, required headers, request/rate limits, caching rules, fields used, time/unit format, weather-code mapping, error responses, attribution, license, privacy implications, failover behavior, fixture locations, official documentation, and last terms review date.
- Contracted fields support Home current, hourly, daily, metrics, sun/update/source, provenance, and stale UI needs.
- Open-Meteo current-condition values are labeled model estimates unless provider documentation proves otherwise.
- Provider-specific fields are separated from provider-neutral Oxygen semantics.

## Slice 2: Open-Meteo Fixtures and DTO Parsing

Status: specified

Release intent: Parse representative Open-Meteo forecast fixtures without live internet.

Must prove:
- DTOs parse only first Home-path current/hourly/daily fields.
- Required envelope validation fails deterministically.
- Nullable weather values remain null, not fabricated as zero.
- Provider DTOs remain isolated from UI/domain consumers.

## Slice 3: Open-Meteo Weather-Code and Domain Mapping

Status: specified

Release intent: Convert parsed Open-Meteo data into provider-neutral Oxygen forecast domain data.

Must prove:
- Supported Open-Meteo weather codes map to `WeatherCondition`; unknown or unsupported codes map to `UNKNOWN`.
- Mapper produces provider-neutral current/hourly/daily data with canonical units, `Instant` timestamps, location timezone, and null preservation.
- Provenance identifies Open-Meteo, fetched time, source/license fields where available, and correct `DataType`.
- Current conditions remain model estimates unless the provider contract proves observation semantics.

## Gate 3A: Repository License and Privacy Document Baseline

Status: specified

Release intent: Repository-level license, privacy, and provider-disclosure documents exist before active network providers are presented as implemented or release-ready.

Must prove:
- Repository root contains deliberate `LICENSE`, `NOTICE`, `THIRD_PARTY_LICENSES.md`, `DATA_SOURCES.md`, and `PRIVACY.md` files.
- Weather-data licenses and attribution are presented separately from Oxygen source-code licensing.
- Data-source documents list only implemented providers as active/current and separately identify specified roadmap providers.
- Privacy text discloses no advertising, no tracking, no account requirement, optional location permission, and request data sent to active providers.
- This gate does not make any provider active; provider-specific active/current entries are updated only by the slice that implements the corresponding production path.

## Slice 4: Open-Meteo Client Transport and Error Classification

Status: specified

Release intent: Fetch Open-Meteo forecast data through an isolated production client.

Must prove:
- Base URL and query construction are isolated/configurable outside UI code.
- Client requests only fields required by the first provider-backed Home path.
- Successful responses parse through production DTO parsing.
- Client classifies network/offline failure, provider unavailable, HTTP/rate-limit where detectable, and invalid response.

## Slice 5: Explicit-Location Open-Meteo Repository Path

Status: specified

Release intent: Given an explicit `WeatherLocation`, return provider-neutral forecast data through the repository without sample weather.

Must prove:
- Repository accepts an explicit selected location and uses the Open-Meteo client plus mapper.
- Repository exposes loading, success, and error results suitable for later UI state.
- No hidden default location is introduced.
- `SampleWeather.bundle`, Open-Meteo DTOs, and provider-specific errors do not cross into the production repository/UI boundary.

## Slice 6: Geocoding Provider Contract

Status: specified

Release intent: Specify the MVP geocoding provider before code is added.

Must prove:
- The provider contract completes every field in `docs/data-sources/PROVIDER_TEMPLATE.md`, including endpoint, authentication, required headers, request/rate limits, caching rules, fields used, time/unit format, error responses, attribution, license, privacy implications, failover behavior, fixture locations, official documentation, and last terms review date.
- Provider fields support place search, coordinates, timezone, country, administrative area, and optional elevation.
- Provider identifiers are not user-facing `LocationId` values.
- The contract avoids making a public OSM Nominatim server the only production autocomplete backend.

## Slice 7: Geocoding Fixtures and Domain Mapping

Status: specified

Release intent: Parse geocoding fixtures and map them into provider-neutral location models.

Must prove:
- Fixtures cover normal, empty, ambiguous, malformed, missing-optional, invalid-coordinate, and invalid-timezone cases.
- Mapper returns display name, coordinates, IANA timezone, country/admin data, optional elevation, and stable local `LocationId`.
- Ambiguous places remain distinct through admin/country/coordinate data.
- Invalid required fields map to explicit domain errors.

## Slice 8: Geocoding Search Client and Repository Boundary

Status: specified

Release intent: Search locations through a replaceable production geocoding path.

Must prove:
- Base URL and query construction are isolated from UI code.
- Repository exposes loading, success, empty, provider unavailable, network/offline, rate-limit where detectable, and invalid-response states.
- Search ordering is deterministic for identical provider responses.
- Domain models, not DTOs or provider IDs, cross the repository boundary.

## Slice 9: First-Run Manual Location Entry

Status: specified

Release intent: A first-run user can start with manual location search without granting location permission.

Must prove:
- First-run UI offers manual search and a separate "use my location" action; manual search does not request location permission.
- With no selected location, production app state shows or routes to first-run manual selection, not sample weather.
- Permission-denied state returns to manual selection without blocking forecast functionality.
- No hard-coded, hidden, default, scaffold, or sample location satisfies Home success.

### Slice 9A: Manual Search Results Selection

Status: specified

Release intent: A manual search query returns production geocoding results that can be selected as provider-neutral Oxygen locations.

Must prove:
- Search results come from the production geocoding repository path and disambiguate long or similar names.
- Selected results produce provider-neutral `WeatherLocation` values with stable local `LocationId` values.
- Provider IDs and geocoding DTOs do not cross into Home state, saved-location UI, or Composables.
- Empty, offline/network, rate-limited, provider-unavailable, and invalid-response search states remain visible and retryable where applicable.

### Slice 9B: Selected Location Handoff To Home

Status: specified

Release intent: A selected manual location routes to Home loading for exactly that location.

Must prove:
- Manual selection routes Home using the exact selected `WeatherLocation`.
- Home receives only the selected `WeatherLocation`; no hidden default, scaffold, or sample location is substituted.
- The handoff is observable at the Android state or Compose boundary before Home success is implemented.
- Long selected place names remain readable during the handoff and loading states.

## Slice 10: Manual Selection Routes to Home Loading, Error, and Retry

Status: specified

Release intent: Home routing and state holder use the selected location and expose usable non-success states.

Must prove:
- With no selected location, production app state shows or routes to first-run manual selection, not sample weather.
- Manual selection routes Home using the exact selected location.
- Home loads through `WeatherRepository`, not `SampleWeather.bundle`.
- Loading, error, and retry states are visible, accessible, tied to the selected location, and provider-neutral.
- Retry uses the same selected location and does not substitute a default location.

## Slice 11: Provider-Backed Home Success Dashboard

Status: specified

Release intent: Home renders real provider-neutral forecast success data in MVP dashboard order.

Must prove:
- Success renders location header, current hero, hourly forecast, daily forecast, metrics, sun/update/source, and provenance where data exists.
- Rendered success data comes from the repository result, not scaffold/sample weather.
- Missing values are unavailable/unknown or omitted; they are never fabricated as zero.
- Long location names, large font, and effects-disabled mode remain readable.

## Slice 11A: Explicit Home Refresh and Retry

Status: specified

Release intent: Users can explicitly refresh provider-backed Home data for the selected location without causing duplicate refresh loops.

Must prove:
- Pull-to-refresh or a visible refresh control triggers repository refresh for the selected location.
- Retry after a Home error uses the same selected location and provider-neutral error state.
- Refresh is caused by explicit user action or a controlled state-holder trigger, not by every recomposition.
- Failed refresh keeps useful cached data visible with stale/source/failure metadata where cache exists, and no-cache failure remains retryable.

## Slice 12: MET Norway Provider Contract

Status: specified

Release intent: Specify the MVP forecast fallback provider before fallback code is added.

Must prove:
- The provider contract completes every field in `docs/data-sources/PROVIDER_TEMPLATE.md`, including endpoint, authentication, required User-Agent/header identity, request/rate limits, caching rules, fields used, time/unit format, weather-symbol mapping, error responses, attribution, license, privacy implications, failover behavior, fixture locations, official documentation, and last terms review date.
- Contract maps provider fields to the same Home and provenance needs as Open-Meteo.
- MET Norway-specific fields remain separate from provider-neutral Oxygen semantics.
- Fallback behavior is defined without averaging or merging provider values.

## Slice 13: MET Norway Forecast Production Path

Status: specified

Release intent: Given an explicit `WeatherLocation`, return MET Norway forecast data through the same provider-neutral boundary as Open-Meteo.

Planning note: This release intent is too broad for one active cycle. Select one of the bounded sub-slices below when implementation starts.

### Slice 13A: MET Norway Fixtures and DTO Parsing

Status: specified

Release intent: Parse representative MET Norway forecast fixtures without live internet.

Must prove:
- DTOs parse only first Home-path current/hourly/daily fields.
- Required envelope validation fails deterministically.
- Nullable weather values remain null, not fabricated as zero.
- MET Norway DTOs remain isolated from UI/domain consumers.

### Slice 13B: MET Norway Symbol and Domain Mapping

Status: specified

Release intent: Convert parsed MET Norway data into provider-neutral Oxygen forecast domain data.

Must prove:
- Supported MET Norway symbols map to `WeatherCondition`; unknown or unsupported symbols map to `UNKNOWN`.
- Mapper produces provider-neutral current/hourly/daily data with canonical units, `Instant` timestamps, location timezone, and null preservation.
- Provenance identifies MET Norway, issued/fetched time where available, source/license fields, and correct `DataType`.
- Fallback behavior remains defined without averaging or merging provider values.

### Slice 13C: MET Norway Client Transport and Error Classification

Status: specified

Release intent: Fetch MET Norway forecast data through an isolated production client.

Must prove:
- Required headers, User-Agent identity, base URL, and query parameters are isolated/configurable outside UI code.
- Client requests only fields required by the provider-backed Home path.
- Successful responses parse through production DTO parsing.
- Client classifies network/offline, provider unavailable, rate-limit where detectable, cache-not-modified where applicable, and invalid response.

### Slice 13D: Explicit-Location MET Norway Repository Path

Status: specified

Release intent: Given an explicit `WeatherLocation`, return provider-neutral MET Norway forecast data through the repository.

Must prove:
- Repository accepts an explicit selected location and uses the MET Norway client plus mapper.
- Repository exposes loading, success, and error results suitable for fallback selection and UI state.
- No hidden default location is introduced.
- MET Norway DTOs and provider-specific errors do not reach Composables, Home state, saved locations, unit presentation, or cache consumers.

## Slice 14: Forecast Fallback Selection

Status: specified

Release intent: Repository attempts Open-Meteo by default and falls back to MET Norway under explicit eligible failures without hiding provenance.

Must prove:
- Open-Meteo success does not call MET Norway.
- Fallback-eligible Open-Meteo failure followed by MET Norway success returns MET Norway provenance through provider-neutral state.
- Both-provider failure returns a retryable error while preserving both causes for diagnostics/logging.
- Repeated provider failures do not cause wasteful retry loops from repository refresh calls or location changes.
- This slice proves forecast repository fallback selection only; alert independence, cache provenance, saved-location behavior, unit presentation, and Home UI behavior are verified in their own later slices and release gates.

## Slice 15: In-App About, Privacy, Licenses, and Data-Source Surface

Status: specified

Release intent: The single Settings/About disclosure surface evolves as active providers are implemented.

Must prove:
- Settings/About exposes Data Sources, Open Source Licenses, and Privacy surfaces through visible navigation.
- In-app Data Sources lists only implemented providers as active/current and separately identifies specified roadmap providers where shown.
- Open-Meteo is listed as the active default forecast provider only after its production path is implemented.
- MET Norway is listed as active fallback only after fallback production behavior is implemented.
- Home success still shows visible source, update, and provenance for the provider that served the displayed forecast.
- Forecast, geocoding, and alert provider disclosures match repository `DATA_SOURCES.md` and `PRIVACY.md` as those providers become active.
- Weather-data licenses and attribution are presented separately from Oxygen source-code licensing.

## Slice 16: Cache One Forecast Bundle Through Repository

Status: specified

Release intent: Room stores normalized current/hourly/daily forecast data and emits it through the repository path.

Must prove:
- Repository refresh writes provider results in one transaction and emits success from Room.
- Entities preserve provenance, provider ID, timestamps, timezone, canonical units, provider cache metadata inputs, and null/missing values.
- Forecast rows are scoped by stable local `LocationId`.
- No failed-refresh retention, offline launch, or broad offline-first behavior is claimed yet.

## Slice 17: Failed Refresh Retains Cached Forecast

Status: specified

Release intent: Network failure with useful cached data keeps Home usable and visibly stale.

Must prove:
- Refresh failure with cache does not replace useful cached forecast with an empty error screen.
- UI state exposes stale age, source/update status, and refresh-failed metadata.
- Retry remains available.
- Refresh failure without cache produces retryable no-cache error.

## Slice 17A: Home Dashboard Presentation Alignment

Status: specified

Release intent: Home's provider-backed success and stale-success states match the specified dashboard hierarchy before offline launch builds on the same surface.

Must prove:
- Home success renders as a vertically scrolling dashboard with location header, current-condition hero, horizontal hourly forecast, daily forecast, metric grid, sun/update/source information, stale/refresh-failed status where present, and provenance footer.
- Rendered dashboard values still come from provider-neutral repository results and presentation state, not `SampleWeather.bundle`, provider DTOs, or fabricated fallback values.
- The current-condition hero integrates Oxygen weather identity, such as the weather mark or procedural scene, while keeping temperature, condition, feels-like, high/low, update, source, and stale status readable when decorative effects are disabled.
- Hourly, daily, metric, source, stale, and retry surfaces use stable dimensions and remain readable with long location names, narrow screens, and large font settings.
- Existing loading, no-cache error, retry, stale-cache, source/provenance, and disclosure behavior remains observable after component extraction.
- This slice does not add offline launch, saved-location persistence, unit preferences, appearance persistence, alert lookup, air-quality lookup, radar, background refresh, or new provider behavior.

## Slice 17B: Explicit Home Refresh Control

Status: specified

Release intent: Fresh and stale Home dashboards expose an explicit refresh action for the selected location without recomposition-driven refresh loops.

Must prove:
- A visible refresh control is reachable on provider-backed Home success and stale-success states.
- Refresh invokes the repository path for the exact selected `WeatherLocation` and does not substitute a default, sample, or stale previous location.
- Refresh is caused only by explicit user action or a controlled state-holder trigger, not by every recomposition.
- Refresh-in-progress, successful refresh replacement, failed-refresh stale retention, no-cache failure, and retry remain provider-neutral and observable.
- The refresh control has an adequate touch target, meaningful text or accessibility label, stable layout, and remains readable on narrow screens and large font settings.
- This slice does not add offline launch, saved-location persistence, background refresh, unit preferences, appearance persistence, alert lookup, air-quality lookup, radar, or new provider behavior.

## Slice 17C: Home Presentation Accessibility Evidence Baseline

Status: specified

Release intent: Home presentation slices have Compose or Android-boundary evidence for layout, semantics, and accessibility-oriented conditions before offline launch relies on the same UI.

Must prove:
- Home success, stale-success, loading, no-cache error, source/provenance, stale/refresh-failed, and refresh-control states are exercised at a Compose or Android UI boundary.
- Important weather semantics have meaningful text alternatives or semantics and preserve logical reading order.
- Long location names, provider names, timestamps, stale text, retry/refresh controls, hourly items, daily rows, metrics, and source/provenance text do not overlap at compact phone width and large font settings.
- The Home dashboard remains understandable with decorative effects disabled and without relying on color alone.
- Evidence is saved as screenshots, hierarchy dumps, Compose test logs, or equivalent Android-boundary artifacts under the active cycle artifact directory.
- This slice does not add new forecast behavior, offline launch, saved-location persistence, unit preferences, appearance persistence, alert lookup, air-quality lookup, radar, background refresh, or release-candidate claims.

## Slice 18: Offline Launch From Last Forecast

Status: specified

Release intent: Relaunching without network displays the last cached forecast for the selected location.

Must prove:
- Last selected location and forecast load from local storage.
- Home shows cached current/hourly/daily data and explicit stale age when network is unavailable.
- No-cache launch shows a retryable error.
- Offline claims are limited to the selected-location forecast path verified by this slice.

## Slice 19: Saved Locations Persistence

Status: specified

Release intent: Users can save, list, select, and remove multiple forecast locations.

Must prove:
- Saved locations persist locally and selected saved location controls Home forecast.
- Removing a location updates selection predictably.
- Manual location functionality remains full-featured without location permission.
- Saved-location UI disambiguates similar names and provides visible select/remove controls.

## Slice 20: Unit Preferences and Conversion

Status: specified

Release intent: Users can switch Metric, US, UK, and custom unit presentation without changing canonical stored values.

Must prove:
- Unit preferences persist locally.
- Temperature, wind, pressure, precipitation, and visibility convert only for presentation.
- Missing values remain unknown/unavailable instead of becoming zero after conversion.
- Converted values fit current hero, hourly items, daily rows, and metric cards at large font sizes.
- Converted values preserve stable Home layout, source/update/stale text, refresh controls, and provenance visibility.

## Slice 21: Optional Device Location

Status: specified

Release intent: Users may explicitly request device location while manual location remains fully functional.

Must prove:
- Location permission is requested only after explicit user action.
- Permission denied returns to manual search without blocking forecast functionality.
- Granted coarse/fine location resolves to a `WeatherLocation` or coordinates usable by the forecast path.
- No background location is introduced.

## Slice 22: NWS Alert Provider Contract

Status: specified

Release intent: Specify United States official alert integration before code is added.

Must prove:
- The provider contract completes every field in `docs/data-sources/PROVIDER_TEMPLATE.md`, including endpoint, authentication, required User-Agent/header identity, request/rate limits, caching rules, fields used, time/unit format, severity mapping, error responses, attribution, license, privacy implications, failover/unsupported-region behavior, fixture locations, official documentation, and last terms review date.
- Contract distinguishes official alerts from forecast-derived weather risk.
- Contract defines unsupported-region behavior.
- Contract identifies alert banner/detail fields required by UI.

## Slice 23: NWS Alert Parsing, Mapping, and Repository Merge

Status: specified

Release intent: Fetch and map official NWS alerts separately from forecast providers, then expose them with forecast data where supported.

Planning note: This release intent is too broad for one active cycle. Select one of the bounded sub-slices below when implementation starts.

### Slice 23A: NWS Alert Fixtures, Parsing, and Mapping

Status: specified

Release intent: Parse NWS alert fixtures and map them into provider-neutral official alert domain data.

Must prove:
- Parser and mapper handle no alerts, one alert, many alerts, missing optional fields, timestamps, affected areas, and unknown severity.
- Severity, urgency, certainty, event, issuer, effective/expires, description, instructions, affected geometry where available, and provenance are retained.
- Forecast-derived risks are not represented as official alerts.
- NWS DTOs remain isolated from UI/domain consumers.

### Slice 23B: NWS Alert Client and Error Classification

Status: specified

Release intent: Fetch NWS alerts through an isolated official alert provider client.

Must prove:
- Required headers, User-Agent identity, base URL, and request shape are isolated/configurable outside UI code.
- Successful responses parse through production DTO parsing.
- Client classifies network/offline, provider unavailable, rate-limit where detectable, unsupported region, no alerts, and invalid response.
- Client behavior respects provider cache guidance where applicable.

### Slice 23C: Alert Repository Merge

Status: specified

Release intent: Repository combines forecast results and official alert results without coupling provider selection.

Must prove:
- Repository combines forecast and alert results without inventing alerts from forecasts.
- Non-US or unsupported regions show unsupported/no-alert state plainly.
- Alert-provider failure does not block forecast display.
- Forecast provider preference and fallback selection do not disable alert lookup.

## Slice 24: Alert Banner and Detail UI

Status: specified

Release intent: Official alerts are visibly displayed and inspectable without hidden gestures.

Must prove:
- Home shows active alert severity text, event, issuer, and expiration where available.
- Detail view shows severity, event, issuer, effective/expires, affected area, description, instructions, and attribution where available.
- Multiple active alerts have visible navigation/list affordances.
- Severity meaning is explicit text/structure, with color only as reinforcement.

## Gate 25: Disclosure Baseline Check

Status: specified

Release intent: Before alert, appearance, and release-candidate work, confirm the disclosure baseline created before network provider work still matches implemented behavior.

Must prove:
- Repository `LICENSE`, `NOTICE`, `THIRD_PARTY_LICENSES.md`, `DATA_SOURCES.md`, and `PRIVACY.md` still exist and match implemented providers and dependencies.
- No provider appears as active/current unless its production path can fetch or serve data.
- Weather-data licenses and attribution remain separate from Oxygen source-code licensing.
- Privacy text still discloses no advertising, no tracking, no account requirement, optional location permission, and request data sent to active providers.

## Slice 26: Effects Off Preference Baseline

Status: specified

Release intent: Users can persist weather-effects settings, including effects Off.

Must prove:
- Production appearance settings are reachable from Settings/About or the app settings entry point without requiring hidden gestures.
- Effects Off removes continuous decorative animation while preserving weather information.
- Reduced-motion/accessibility preferences are respected where available.
- Weather semantics, alerts, source/update/stale text, and provenance remain visible with effects Off.
- Effects preference persists across restart and remains independent from theme and layout preferences.

## Slice 27: Layout Density Preference Baseline

Status: specified

Release intent: Users can switch Simple/Standard presentation without losing required MVP weather information.

Must prove:
- Production layout settings are reachable from Settings/About or the app settings entry point without requiring hidden gestures.
- Layout preference persists and Standard remains default.
- Simple and Standard preserve required weather fields, visible source/update/stale information, and alert visibility.
- Layout preference remains independent from theme and effects preferences.
- Long location names, alert names, and source/update/stale text fit without overlap in both layouts.

## Slice 28: Theme Selection Baseline

Status: specified

Release intent: Users can persist theme selection without changing weather semantics.

Must prove:
- Production theme settings are reachable from Settings/About or the app settings entry point without requiring hidden gestures.
- Theme selection persists across restart.
- Theme changes do not alter weather semantics, provider interpretation, alert severity meaning, source/update/stale text, or accessibility minimums.
- Theme, layout, and effects controls remain independent.
- Each implemented theme remains readable for Home success, Home error, alert, source/update/stale, and provenance states.

## Slice 29: High-Contrast Presentation Baseline

Status: specified

Release intent: Users can select at least one high-contrast/accessibility-oriented presentation without changing weather semantics.

Must prove:
- High-contrast presentation is reachable through production appearance settings.
- Required Home, alert, source/update/stale, and provenance information remains visible and understandable without relying on color alone.
- Theme changes do not alter weather semantics, provider interpretation, alert severity meaning, or accessibility minimums.
- Compact phone, large font, effects Off, and high-contrast mode remain readable without overlap.

## Gate 30: Accessibility Presentation Verification

Status: specified

Release intent: MVP presentation paths are verified under accessibility-oriented Android conditions. This is a verification gate, not an implementation slice.

Must prove:
- Compact phone, large font, RTL where applicable, TalkBack order, adequate touch targets, reduced motion, effects Off, and high-contrast paths remain usable.
- Important weather semantics, alerts, source/update/stale text, and provenance remain readable without color or decorative effects.
- Long location names, alert names, provider names, timestamps, and unit-converted values do not overlap adjacent content.
- Any skipped accessibility condition is named with the exact blocker.

## Slice 31: Fallback Cache and Provenance

Status: specified

Release intent: Cache metadata remains truthful for Open-Meteo and MET Norway fallback forecasts.

Must prove:
- Cache persists provider ID, source name, issued/fetched timestamps, data type, license ID where available, and provider cache metadata for both forecast providers.
- A MET Norway fallback forecast is cached and later emitted with MET Norway provenance.
- Later Open-Meteo refresh replaces fallback data only through the normal verified refresh transaction.
- Failed refresh/offline state keeps cached fallback data visible with explicit stale/source metadata.

## Slice 32: Fallback Real-Path Verification

Status: specified

Release intent: The installed app demonstrates default forecast and fallback forecast behavior at an Android boundary before release-candidate verification.

Must prove:
- Open-Meteo default success shows Open-Meteo provenance.
- Controlled fallback-eligible Open-Meteo failure with MET Norway success shows MET Norway provenance.
- Source/update/stale text remains visible for both default and fallback-served forecasts.
- Official alert state remains independent from forecast provider selection.

## Slice 33: MVP Privacy and Dependency Audit

Status: specified

Release intent: The standard MVP build is auditable for privacy, dependency, and provider-attribution claims.

Must prove:
- Dependency tree and manifest are reviewed for advertising, analytics, telemetry uploaders, account requirements, unnecessary Google Play Services dependency, and background location.
- Audit names active forecast, geocoding, and alert providers for the build.
- Every active network provider has reachable local attribution, source, license/privacy notes, and request-data disclosure.
- No provider appears as active/current unless its production path can fetch or serve data.

## Gate 34: About and Data-Source Release Check

Status: specified

Release intent: Before release-candidate verification, confirm the single Settings/About disclosure surface matches the final MVP provider set.

Must prove:
- In-app Data Sources lists only active providers as active/current and separately identifies specified roadmap providers where shown.
- Forecast, geocoding, and alert provider disclosures match repository `DATA_SOURCES.md` and `PRIVACY.md`.
- Weather-data licenses and attribution are presented separately from Oxygen source-code licensing.
- Open Source Licenses and Privacy remain reachable from Settings/About.

## Gate 35: Oxygen MVP Broad Verification and Release Candidate

Status: specified

Release intent: MVP behavior is verified as a release candidate against the repo completion standard. This is a release gate, not an implementation slice.

Must prove:
- Current, hourly, daily, Open-Meteo default forecast, MET Norway fallback, search, saved locations, offline cache, alerts, units, presentation settings, permission-denied manual path, attribution, and privacy audit all pass focused checks.
- Data Sources presents Open-Meteo as active default forecast provider and MET Norway as active fallback provider.
- App installs and launches on emulator.
- Focused behavior evidence, real-path emulator exercise, and broad verification commands all pass, with any skipped command named and justified.

Focused evidence:
- Focused test reports for MVP behavior slices: forecast mapping/repository, fallback selection, geocoding/manual selection, cache/stale display, alerts, units, presentation settings, attribution, and privacy audit.
- UI/state evidence that first-run manual search, provider-backed Home success, provider-backed error/retry, saved-location switching, alert detail, unit switching, effects Off, large font, and narrow-screen states behave as specified.
- Provider evidence that Open-Meteo default forecast and MET Norway fallback forecast expose correct provenance, attribution, privacy notes, and cache metadata.
- Evidence that no Home success path uses `SampleWeather.bundle`, hidden default locations, fabricated missing values, or provider-specific DTOs in Composables.

Real-path exercise:
- Install and launch the debug build on an emulator.
- Manually or automatically run through first-run manual search, Open-Meteo provider-backed Home, MET Norway fallback Home, refresh, offline stale display, saved-location switch, alert detail, units, effects Off, large font, and narrow-screen checks.
- Record the emulator/device used and the exact commands run.

Broad verification:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `scripts/list-avds.sh`
- `scripts/start-emulator.sh`
- `scripts/install-debug.sh`
- `git diff --check`

## Explicitly Deferred From MVP

- Air quality and AQI UI.
- Pollen.
- Radar and maps.
- Home-screen widgets.
- Daily summary/weather-change notifications.
- Background alert polling.
- Moon data.
- Detailed charts beyond what is needed for MVP readability.
- Forecast sharing.
- Favorite/reordered locations beyond basic saved-location selection/removal.
- Additional national alert sources beyond NWS.
- Community theme packaging.
- Self-hostable relay.

## Next Candidate Slice

Candidate: Slice 1: Open-Meteo Provider Contract.

To start work, update `.codex/plans/current.md` from `none` to this single bounded slice. Do not treat later roadmap entries as planned active work.

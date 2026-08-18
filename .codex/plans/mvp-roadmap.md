# Oxygen MVP Roadmap

Status: planned
Roadmap ID: mvp-2026-08
Source authority: `docs/OXYGEN_FULL_SPECIFICATION.md`
Created: 2026-08-18

## Roadmap Rule

Each implementation slice must fit within a working budget of no more than 23K tokens. If a slice starts to exceed that budget, stop at the last verified boundary, record evidence, and split the remaining work into the next slice.

Prefer narrow vertical slices that produce externally observable behavior over broad framework work. A slice is complete only when its production path exists and the acceptance boundary has been exercised.

## Evidence Rule

Deterministic automated tests are the required focused evidence for provider parsing, mapping, repository state, and UI state behavior. Live provider checks and emulator/manual exercises are real-path evidence only: they prove the production path can run against Android/runtime/provider boundaries, but they are not the sole proof of correctness and must not replace fixture-backed tests.

## UI Rule

Every user-facing slice must carry the relevant UI specification with it. Do not defer UI obligations into a separate polish phase when they are part of the behavior being implemented.

For MVP slices, this means:
- home remains a vertically scrolling weather dashboard with location header, alert area when present, current-condition hero, hourly forecast, daily forecast, metric grid, sun/update/source information where data exists, and provenance footer;
- important weather semantics remain readable with decorative effects, gradients, transparency, and animation disabled;
- safety information is visible text, not color alone, and does not require hidden gestures;
- UI supports large font, RTL where applicable, meaningful accessibility semantics, adequate touch targets, and logical TalkBack order;
- fixed-format elements such as forecast rows, metric cards, controls, weather marks, and scene containers use stable dimensions to avoid layout shifts;
- provider DTOs never reach Composables; UI receives presentation-ready state derived from domain models.

## MVP Acceptance Boundary

Oxygen MVP is ready when a user can install the app, choose or search a location without granting location permission, view real current/hourly/daily weather from a provider-backed path, understand source/update/stale status, save and switch locations, retain the latest forecast offline, view supported official alerts, change units and core presentation settings, and use the app without advertising, tracking, account, cloud dependency, or Google Play Services as a core requirement.

## Forecast Provider Scope

MVP forecast implementation uses Open-Meteo as the default general forecast provider. MET Norway fallback is specified by the full specification as the alternate forecast provider, but is deferred from MVP implementation.

Open-Meteo-specific DTOs, clients, mappings, contracts, attribution, cache metadata, and errors must remain isolated behind provider-neutral repository/domain boundaries so a later MET Norway fallback can be added without rewriting Home UI, saved locations, cache consumers, unit presentation, or alert behavior.

Do not present Open-Meteo-only MVP behavior as permanent provider policy. Forecast fallback remains planned outside MVP.

## Slice 1: Open-Meteo Provider Contract

Status: planned
Token budget: <=23K

Behavior: The Open-Meteo forecast provider is specified as an implementation contract before code is added.

Acceptance boundary:
- `docs/data-sources/` contains an Open-Meteo provider contract covering endpoint, parameters, fields, units, time format, attribution, license, limits, privacy implications, failure behavior, and fixture locations.
- The contract identifies which response fields map to current hero, hourly strip, daily rows, metric grid, sun card, source/provenance footer, and stale/update UI needs.
- The contract explicitly labels Open-Meteo current-condition data as model estimate unless source documentation proves otherwise.
- The contract identifies Open-Meteo as the MVP default forecast provider and states that MET Norway fallback is deferred from MVP, not rejected.
- The contract distinguishes provider-specific response fields from provider-neutral Oxygen domain semantics so a future fallback provider can map to the same domain model.

Focused evidence:
- Documentation review against current Open-Meteo docs and terms.
- `git diff --check`.

Out of scope:
- Kotlin client code.
- Repository/UI wiring.
- Room persistence.

## Slice 2: Open-Meteo Forecast DTO and Fixture Parsing

Status: planned
Token budget: <=23K

Behavior: A representative Open-Meteo forecast response can be parsed from test fixtures without live internet.

Acceptance boundary:
- Test fixtures exist under the provider fixture path.
- DTOs parse current, hourly, and daily forecast payloads used by MVP.
- Tests fail on malformed or missing required envelope fields and preserve nullable weather values as null rather than zero.

Focused evidence:
- `:core` or provider-focused unit tests for fixture parsing.
- `git diff --check`.

Out of scope:
- Live HTTP calls.
- UI changes.
- Database storage.

## Slice 3: Open-Meteo Weather-Code Mapping

Status: planned
Token budget: <=23K

Behavior: Open-Meteo weather codes map into Oxygen's provider-neutral weather taxonomy.

Acceptance boundary:
- Mapping function converts supported provider codes to `WeatherCondition`.
- Unknown or unsupported codes map to `UNKNOWN`.
- Unit tests cover clear, cloudy, fog, drizzle, rain, freezing precipitation, snow, showers, thunderstorm, hail, and unknown cases.

Focused evidence:
- Mapping unit tests.
- `git diff --check`.

Out of scope:
- HTTP client.
- Full weather bundle mapping.
- UI icons beyond existing semantic rendering.

## Slice 4: Open-Meteo Domain Mapper

Status: planned
Token budget: <=23K

Behavior: Parsed Open-Meteo DTOs map into Oxygen domain models with provenance, timestamps, timezone handling, units, and nullable missing values.

Acceptance boundary:
- Mapper produces a `WeatherBundle` with current, 24-48 hours, 7-10 days, location timezone, and provider provenance.
- Domain values remain canonical metric values.
- Provider timestamps enter as `Instant`; presentation remains responsible for local display.
- Mapper provides enough domain data to support the MVP home dashboard: current-condition hero, hourly forecast, daily forecast, metric grid, sun/update/source display, and condition semantics.
- Mapper tests cover normal fixture, missing optional values, timezone conversion, and provenance data types.

Focused evidence:
- Mapper unit tests.
- `git diff --check`.

Out of scope:
- Network client.
- Room persistence.
- Compose UI state.

## Slice 5: Open-Meteo HTTP Client

Status: planned
Token budget: <=23K

Behavior: Production code can request Open-Meteo forecast JSON for a selected `WeatherLocation`.

Acceptance boundary:
- Base URL and query parameters are isolated/configurable outside UI code.
- Client requests MVP forecast fields in as few calls as practical.
- Client distinguishes offline/network failure, rate limit, provider unavailable, and invalid response where available from transport/HTTP state.
- Tests use fake transport or local fixtures; parser tests do not require live internet.

Focused evidence:
- Client unit tests with fake responses.
- Real-path smoke check against Open-Meteo using one documented fixture location or stable test coordinate, recording request URL shape, HTTP status, and whether the response parses through the production client path.
- Dependency review for privacy-sensitive SDKs.
- `git diff --check`.

Out of scope:
- Live provider verification as sole proof; deterministic fake-transport and fixture tests remain required.
- Room cache.
- Saved locations.

## Slice 6: Forecast Repository Without Persistence

Status: planned
Token budget: <=23K

Behavior: The app has a real repository path from selected location to provider-backed `WeatherBundle`, without claiming offline-first behavior yet.

Acceptance boundary:
- `DefaultWeatherRepository` uses Open-Meteo client and mapper.
- Repository exposes loading, success, and error results suitable for UI state.
- Repository interfaces and UI-facing state remain provider-neutral; Open-Meteo-specific types and errors do not leak into Home UI or saved-location behavior.
- Repository real-path exercise uses an explicit selected location input; it does not create a hidden default app location or production scaffold success.
- Sample weather remains explicitly scaffold-only and is no longer the production home path for the real forecast mode.
- Tests cover success and provider error behavior through repository boundary.

Focused evidence:
- Repository unit tests.
- Real-path repository smoke check from a selected `WeatherLocation` through the production Open-Meteo client, mapper, and repository result path, recorded separately from deterministic unit tests.
- `:app:compileDebugKotlin` if repository is in `:app`, plus relevant unit tests.
- `git diff --check`.

Out of scope:
- Room source of truth.
- Multiple saved locations.
- Alert provider.
- App-level selected-location flow.
- Home UI real-weather activation.

## Slice 7: Geocoding Provider Contract

Status: planned
Token budget: <=23K

Behavior: The MVP geocoding provider is specified before code is added.

Acceptance boundary:
- `docs/data-sources/` contains a geocoding provider contract.
- Contract covers endpoint, query parameters, required/optional fields, units, timezone format, rate/usage limits, attribution, privacy implications, failure behavior, and fixture locations.
- Contract documents how provider fields support Oxygen requirements: place search, latitude/longitude, timezone, country, administrative area, optional elevation.
- Contract states that provider identifiers are not user-facing `LocationId`s.

Focused evidence:
- Provider contract review against current provider docs/terms.
- `git diff --check`.

Out of scope:
- Kotlin DTOs.
- Live HTTP client.
- Search UI.
- Location persistence.

## Slice 8: Geocoding DTOs and Fixture Parsing

Status: planned
Token budget: <=23K

Behavior: Representative geocoding responses can be parsed from local fixtures without live internet.

Acceptance boundary:
- Fixture responses cover normal results, empty results, ambiguous similarly named places, missing optional elevation/admin fields, malformed envelope, and invalid coordinate/timezone data.
- DTOs parse only provider response fields needed for MVP.
- Parser preserves nullable optional values as null rather than fabricating defaults.
- Invalid required fields fail deterministically.

Focused evidence:
- Geocoding fixture parsing unit tests.
- `git diff --check`.

Out of scope:
- Domain mapping.
- Stable local `LocationId`.
- Network calls.
- UI presentation.

## Slice 9: Geocoding Domain Mapping and Stable Location Identity

Status: planned
Token budget: <=23K

Behavior: Parsed geocoding results map into provider-neutral Oxygen location domain models.

Acceptance boundary:
- Mapper returns display name, coordinates, IANA timezone, country, administrative area where available, optional elevation, and stable local `LocationId`.
- Stable `LocationId` is derived independently of provider-specific IDs.
- Ambiguous places remain distinct through admin/country/coordinate data.
- Invalid timezone, missing required coordinates, or unusable names map to explicit domain errors.
- Provider-specific identifiers do not escape into user-facing names.

Focused evidence:
- Mapper tests for normal result, ambiguous names, missing optional fields, stable ID repeatability, invalid timezone, invalid coordinates, and provider-ID isolation.
- `git diff --check`.

Out of scope:
- HTTP client.
- Search repository.
- Search result UI.
- Persistence of selected locations.

## Slice 10: Geocoding Search Client and Repository Boundary

Status: planned
Token budget: <=23K

Behavior: Production code can search locations through a replaceable geocoding provider path.

Acceptance boundary:
- Search client keeps base URL and query construction isolated from UI code.
- Repository exposes loading, success, empty results, provider unavailable, network/offline failure, rate-limit where detectable, and invalid response states.
- Tests use fake transport or fixtures.
- Search result ordering is deterministic for identical provider responses.
- Domain models, not DTOs, cross the repository boundary.

Focused evidence:
- Client/repository unit tests with fake responses.
- Dependency/privacy review for added networking dependencies, if any.
- `:app:compileDebugKotlin` or relevant module compile if production code crosses modules.
- `git diff --check`.

Out of scope:
- First-run UI.
- Device location permission.
- Saved-location persistence.
- Autocomplete polish beyond callable search.

## Slice 11: Search Result Presentation Contract

Status: planned
Token budget: <=23K

Behavior: Search result display requirements are encoded at the UI state boundary without building the full first-run flow.

Acceptance boundary:
- Presentation state includes display name plus admin/country disambiguation where available.
- Long names and similarly named places have stable, readable state representations.
- Provider-specific identifiers are absent from presentation state.
- Empty, loading, and provider-error states are represented for a future UI to render.

Focused evidence:
- Presentation/state mapping tests.
- Long-name and ambiguous-place state tests.
- `git diff --check`.

Out of scope:
- Full first-run screen.
- Selecting a location to drive forecast.
- Saved locations.
- Device location permission.

## Slice 12: First-Run Manual Location Selection

Status: planned
Token budget: <=23K

Behavior: A user can start with location permission denied and manually select a location that drives the forecast path.

Acceptance boundary:
- First-run UI offers search and a separate "use my location" action.
- First-run UI is the actual usable app surface, not a landing page or marketing screen.
- Search field, results, empty state, loading state, provider error, and selection action are visible and accessible.
- Manual search selection stores or passes an explicit selected `WeatherLocation` for the provider-backed Home path.
- Location permission is not requested during manual search.
- Permission-denied state remains usable through manual location selection.
- The "use my location" path is visually available but does not block, obscure, or privilege itself over manual search.

Focused evidence:
- UI/state tests for first-run manual path, denied-permission path, empty search, and long location names.
- Manual emulator exercise of search-to-selection path.
- `:app:compileDebugKotlin`.
- `git diff --check`.

Out of scope:
- Saved multi-location list.
- Device GPS implementation.
- Offline cache.

## Slice 13: Home UI State and Refresh Path

Status: planned
Token budget: <=23K

Behavior: Home screen displays real forecast state with loading, error, retry, update time, and source attribution for an explicit selected location.

Acceptance boundary:
- `HomeViewModel` or screen state holder loads weather through the repository.
- Home loads real weather only from an explicit manually selected location or a later explicit device-location result.
- No hard-coded, default, hidden, or scaffold location is used to satisfy the production Home success state.
- If no location is selected, Home routes to or displays the first-run manual location selection surface instead of rendering sample/provider-backed success.
- Permission-denied users can complete search, select location, and reach provider-backed Home without granting location permission.
- Home screen renders loading, success, network error without cache, and retry states.
- Success state follows the MVP home dashboard order where data exists: location header, active alert area, current-condition hero, near-term precipitation summary, hourly forecast, daily forecast, metric grid, sun/update/source information, and provenance footer.
- First provider-backed success state includes visible provider source name, fetched/update time, data type semantics where relevant, and license/attribution text required by the provider contract.
- A minimal Settings/About/Data Sources surface is reachable once real provider data is shown.
- The Data Sources surface lists Open-Meteo forecast attribution, license/source notes required by the provider contract, and a plain-language privacy note that forecast requests are sent to the configured provider for the selected location.
- Attribution and privacy text are local app content, not a network-only help page.
- Current hero summarizes temperature, condition, feels-like, high/low where available, and update age without becoming a data dump.
- Hourly items include local time, semantic condition mark/text, temperature, and precipitation probability.
- Daily rows include day, semantic condition, precipitation probability, low/high, and a stable row layout.
- Metric grid includes available feels-like, humidity, wind, pressure, visibility, dew point, UV, and cloud-cover values without showing missing values as zero.
- Source/update information is visible in the success state.
- Loading, error, and retry states have meaningful accessibility labels and do not depend on decorative weather effects.
- Provider DTOs do not reach Composables.

Focused evidence:
- State-holder unit tests.
- Compose/UI state tests for loading, success, no-cache error, long location name, large font, and effects-disabled readability where available.
- UI/state test or Compose test proving provider attribution is visible in the provider-backed Home success state.
- UI/navigation or state test proving the minimal Data Sources surface is reachable after provider-backed Home is available.
- Emulator exercise of production Home loading, success, source/update display, error without cache, and retry using an explicit selected location.
- Manual/emulator evidence records whether sample weather is absent from the production Home success path.
- `:app:compileDebugKotlin`.
- `git diff --check`.

Out of scope:
- Saved locations beyond the single manually selected first-run location.
- Offline cache.
- Pull-to-refresh if it would exceed the slice.

## Slice 14: Room Forecast Cache Storage

Status: planned
Token budget: <=23K

Behavior: Forecast cache storage is implemented as infrastructure only: MVP forecast datasets can be written to and read from Room through DAO boundaries, without claiming offline behavior, repository source-of-truth behavior, or UI cache display.

Acceptance boundary:
- Room schema supports selected locations, forecast metadata, current conditions, hourly forecast, daily forecast, and provider cache metadata needed by the MVP forecast path.
- Entities preserve provenance, canonical units, timestamps, timezone, stale/cache metadata inputs, and null/missing values without fabricating defaults.
- DAO tests verify transactional replacement and readback for one complete current/hourly/daily forecast bundle.
- DAO tests verify that nullable provider fields remain null after persistence round trip.
- DAO tests verify forecast data is scoped by stable local `LocationId` so one location cannot overwrite another location's forecast rows.
- Schema work is not described as offline cache, source of truth, or user-visible persistence until Slice 15 connects it through the repository path.

Focused evidence:
- Room/DAO unit or instrumentation tests for insert/read transaction shape, replacement behavior, null preservation, location scoping, and current/hourly/daily readback.
- `:app:testDebugUnitTest` or the narrowest Gradle test task that exercises the Room/DAO tests.
- `git diff --check`.

Out of scope:
- Repository source-of-truth behavior.
- UI cache display.
- Offline/stale user-visible behavior.
- Saved location management UI.
- Alerts cache.
- Background refresh workers.

Status language constraint:
- This slice may be reported as `implemented` only for Room forecast storage capability.
- It must not be reported as `implemented` or `verified` for offline cache, stale display, repository source of truth, or user-visible persistence.

## Slice 15: Offline Source-of-Truth Forecast Flow

Status: planned
Token budget: <=23K

Behavior: Room becomes the local source of truth for forecast display.

Acceptance boundary:
- Repository emits cached forecast state from Room.
- Refresh writes provider data through a transaction, then UI observes updated cache.
- Network failure with cache retains forecast and exposes stale/error metadata.
- Network failure without cache produces retryable empty/error state.
- Cached UI visibly distinguishes fresh, stale, refreshing, refresh-failed-with-cache, and no-cache error states.
- Stale age appears in the home source/update area and remains understandable when effects are disabled.
- Slice 15 is the first slice allowed to claim offline forecast behavior or Room source-of-truth behavior.
- Repository behavior is verified through the production cache boundary, not only direct DAO calls.

Focused evidence:
- Repository/cache tests for success, stale cache, no-cache failure, and nullable fields.
- UI/state tests for stale banner/source text and refresh-failed-with-cache behavior where available.
- Emulator exercise showing cached forecast display after refresh, stale/cache-retained display after simulated network failure, and retryable no-cache error after cache clear.
- `:app:testDebugUnitTest :core:testDebugUnitTest`.
- `git diff --check`.

Out of scope:
- Multiple saved locations UI.
- Alerts.
- Background workers.

## Slice 16: Saved Locations Persistence

Status: planned
Token budget: <=23K

Behavior: Users can save, list, select, and remove multiple forecast locations.

Acceptance boundary:
- Saved locations persist locally.
- Selected saved location controls the home forecast.
- Removing a location updates selection predictably.
- Manual location functionality remains full-featured without location permission.
- Locations UI provides visible controls for selecting and removing locations; horizontal swipe may switch locations only if a visible alternative exists.
- Saved location rows disambiguate similarly named places with admin/country information where available.
- Removing the currently selected location does not leave the app in an incoherent blank state.

Focused evidence:
- Repository/DAO tests for saved locations.
- UI/state tests for list, select, remove, selected-location fallback, and long location names.
- Emulator exercise of save/select/remove.
- `git diff --check`.

Out of scope:
- Reordering/favorites if it would exceed the slice.
- Device location.
- Alert provider.

## Slice 17: Unit Preferences and Conversion

Status: planned
Token budget: <=23K

Behavior: Users can switch Metric, US, UK, and custom unit presentation without changing canonical stored values.

Acceptance boundary:
- Unit preferences persist locally.
- Temperature, wind, pressure, precipitation, and visibility convert only for presentation.
- Unit conversion tests cover representative values, rounding, and edge cases.
- Home UI reflects changed units.
- Unit controls use familiar selection components and show current choices clearly.
- Converted values fit within current hero, hourly items, daily rows, and metric cards at large font sizes.
- Missing values remain unknown/unavailable in UI instead of becoming zero after conversion.

Focused evidence:
- Unit conversion tests.
- Preference/state tests.
- UI or emulator exercise of unit switch across home hero, hourly, daily, and metrics.
- `git diff --check`.

Out of scope:
- Appearance theme persistence unless needed for shared preference infrastructure.
- Charts.
- Alerts.

## Slice 18: Optional Device Location

Status: planned
Token budget: <=23K

Behavior: Users may explicitly request device location, while manual location remains fully functional.

Acceptance boundary:
- Location permission is requested only after explicit user action.
- Permission denied returns to manual search path without blocking forecast functionality.
- Granted coarse/fine location resolves to a `WeatherLocation` or coordinates usable by the forecast path.
- No background location is introduced.
- Permission rationale and denied states are plain-language UI states with visible manual-search recovery.
- Device-location controls do not imply location permission is required for full weather functionality.

Focused evidence:
- Permission state tests where feasible.
- Manual emulator exercise for denied and granted paths.
- Manifest permission review.
- `git diff --check`.

Out of scope:
- Background location.
- Weather alerts.
- Widgets/notifications.

## Slice 19: NWS Alert Provider Contract

Status: planned
Token budget: <=23K

Behavior: United States official alert integration is specified before code is added.

Acceptance boundary:
- NWS provider contract covers endpoint, required User-Agent, request shape, alert fields, severity mapping, cache behavior, attribution, privacy implications, and fixture paths.
- Contract distinguishes official alerts from forecast-derived weather risk.
- Contract defines unsupported-region behavior for non-US locations.
- Contract identifies the fields required for alert banner and detail UI: severity, event, headline, issuer, effective/expires, affected area where available, description, and instructions.

Focused evidence:
- Documentation review against current NWS docs.
- `git diff --check`.

Out of scope:
- Alert client code.
- Alert UI changes.
- Background polling.

## Slice 20: NWS Alert Parsing and Mapping

Status: planned
Token budget: <=23K

Behavior: NWS alert fixtures map to Oxygen `WeatherAlert` records with provenance and safety semantics.

Acceptance boundary:
- Fixture parser handles no alerts, one alert, many alerts, missing optional fields, and expired/active timestamps.
- Severity/urgency/certainty fields map without relying on color-only meaning.
- Unsupported or unknown severity maps to `UNKNOWN`.
- Mapping preserves original instructions and descriptions for detail UI without paraphrasing critical safety text.

Focused evidence:
- Alert parsing/mapping unit tests.
- `git diff --check`.

Out of scope:
- Live NWS calls.
- UI detail screen.
- Alert cache.

## Slice 21: NWS Alert Client and Repository Merge

Status: planned
Token budget: <=23K

Behavior: Forecasts and official alerts are fetched through separate provider systems and shown together for supported US locations.

Acceptance boundary:
- Alert provider uses isolated/configurable endpoint and required headers.
- Forecast preference cannot disable official alert lookup.
- Non-US or unsupported regions show unsupported/no-alert state plainly.
- Repository combines forecast and alert results without inventing alerts from forecasts.
- UI state distinguishes no active alerts, unsupported alerts region, alert provider failure, and active alerts without blocking forecast display.

Focused evidence:
- Repository tests for US alerts, no alerts, unsupported region, and alert-provider failure with forecast success.
- Real-path NWS smoke check for one documented US test location or fixture-backed coordinate path, recording HTTP status, required headers, unsupported-region handling, and repository merge result.
- `git diff --check`.

Out of scope:
- Live NWS verification as sole proof; fixture-backed repository tests remain required.
- Background alert polling.
- Push notifications.
- Canada/Europe alerts.

## Slice 22: Alert Banner and Detail UI

Status: planned
Token budget: <=23K

Behavior: Official alerts are visibly displayed and inspectable without hidden gestures.

Acceptance boundary:
- Home shows active official alert banner with severity text, event, issuer, and expiration where available.
- Detail view shows severity, event, issuer, effective/expires, affected area when available, description, and instructions.
- Multiple active alerts have visible navigation/list affordances and do not hide severe/extreme alerts behind horizontal-only gestures.
- Severity is encoded with explicit text and structure, with color only as reinforcement.
- Alert detail preserves official instructions and source attribution visibly.
- Accessibility semantics read meaningful alert information.

Focused evidence:
- UI tests for no alert, unsupported region, one severe alert, many alerts, long instructions, and large font.
- Manual emulator exercise of alert detail.
- `git diff --check`.

Out of scope:
- Background polling.
- Notification delivery.
- Non-US alert providers.

## Slice 23: Appearance Preferences Baseline

Status: planned
Token budget: <=23K

Behavior: Users can persist theme and weather-effects settings, including effects off.

Acceptance boundary:
- Theme selection persists across app restart.
- Weather effects setting supports Off/Subtle/Full.
- Effects Off removes continuous decorative animation while preserving weather information.
- Accessibility/reduced-motion preference is respected where available.
- Theme, layout, and effects controls are independent in UI and do not change weather semantics.
- Effects Off keeps the home dashboard readable without atmospheric scene, transparency, gradient reliance, or motion.
- At least one high-contrast/accessibility-oriented presentation path is available or explicitly scheduled before MVP release-candidate verification.

Focused evidence:
- Preference/state tests.
- UI or emulator exercise of persisted theme/effects, including effects Off readability.
- `git diff --check`.

Out of scope:
- All layout density presets.
- Custom icon package.
- Theme schema validation.

## Slice 24: Layout Density Preference and Simple/Standard State

Status: planned
Token budget: <=23K

Behavior: Users can persist a layout density preference and switch Home presentation state between Simple and Standard without changing weather, source, stale, or alert semantics.

Acceptance boundary:
- Layout preference persists across app restart.
- Standard remains the default Oxygen home dashboard.
- Simple includes current temperature, condition, high/low, precipitation, hourly, daily, and alerts.
- Simple and Standard are presentation choices only; they do not change repository requests, cache behavior, alert lookup, unit conversion, provider attribution, or stale-state calculation.
- Both layouts preserve visible source/update/stale information.
- Both layouts preserve official alert visibility, including severity text and event name; alert meaning is not encoded by color alone.
- Both layouts preserve meaningful accessibility semantics and logical TalkBack order for the fields they display.
- Theme, layout, and effects controls remain independent in UI and state.

Focused evidence:
- Preference/state tests proving persisted layout selection and default Standard behavior.
- UI/state tests proving Simple and Standard expose required weather fields.
- UI/state tests proving source/update/stale and alert summary remain present in both layouts.
- `:app:compileDebugKotlin`.
- `git diff --check`.

Out of scope:
- Compact phone navigation restructuring.
- Larger-screen pane behavior.
- RTL visual validation.
- Full large-font and narrow-screen visual audit.
- Detailed/Meteorologist if it would exceed the slice.
- Charts.
- Home module customization.

## Slice 25: Layout Surface and Accessibility Validation

Status: planned
Token budget: <=23K

Behavior: MVP layout surfaces remain usable on compact phones and under accessibility-oriented display conditions.

Acceptance boundary:
- Compact phone layout uses Home, Alerts, and Locations surfaces appropriately; Settings remains reachable through app bar/menu.
- Source/update/stale information remains visible or reachable from the relevant Home forecast blocks on compact screens.
- Active official alerts remain visible without hidden gestures, and severe/extreme alerts are not de-emphasized by layout density, theme, or effects settings.
- Fixed-format UI elements keep stable dimensions on narrow screens and with large accessibility font where feasible.
- Long location names, long alert event names, and source/update/stale text do not overlap adjacent content.
- Large font, effects Off, and high-contrast/accessibility-oriented presentation remain readable.
- RTL is checked where applicable for layout order, truncation, and navigation reachability.

Focused evidence:
- Visual or automated checks for compact phone, large font, effects Off, and high-contrast/accessibility-oriented presentation.
- UI test or manual/emulator evidence for Settings reachability from compact navigation.
- UI test or manual/emulator evidence that active alerts and source/update/stale text remain visible or reachable.
- `:app:compileDebugKotlin`.
- `git diff --check`.

Out of scope:
- New layout density presets beyond Simple and Standard.
- Redesign of alert detail content.
- New source/provenance content beyond already implemented provider surfaces.
- Tablet-specific redesign beyond avoiding stretched compact layout failure.
- Detailed/Meteorologist layouts.
- Charts.
- Home module customization.

## Slice 26: MVP Privacy and Dependency Audit

Status: planned
Token budget: <=23K

Behavior: The standard MVP build is auditable for no ads, no tracking SDKs, no mandatory account, and no unnecessary Google Play Services dependency.

Acceptance boundary:
- Dependency tree and manifest are reviewed for advertising, analytics, attribution SDKs, telemetry uploaders, account requirements, and background location.
- Privacy/about text accurately describes local diagnostics and data-source requests.
- Any required network/provider privacy implications are documented.
- About/Data Sources UI is audited for completeness against all MVP data sources implemented by this point, including forecast, geocoding, alerts, fallback if implemented, and any local diagnostics/privacy text.
- The audit verifies that no implemented provider lacks visible attribution, source name, license/privacy notes, and reachable local app text.
- Open-source license and privacy surfaces are reachable from Settings/About.

Focused evidence:
- Gradle dependency report or equivalent dependency inspection.
- Manifest permission review.
- `git diff --check`.

Out of scope:
- Legal release approval.
- Store listing materials.
- Optional diagnostics export.

## Slice 27: MVP Broad Verification and Release Candidate

Status: planned
Token budget: <=23K

Behavior: MVP behavior is verified as a release candidate against the repo completion standard.

Acceptance boundary:
- Current, hourly, daily, search, saved locations, offline cache, alerts, units, presentation settings, permission-denied manual path, attribution, and privacy audit all pass their focused checks.
- App installs and launches on emulator.
- Manual or automated UI verification covers home dashboard order, first-run search, alert detail, stale-cache display, saved-location switching, unit switching, effects Off, large font, and narrow screen.
- All broad verification commands pass.
- Cycle evidence is appended to `.codex/cycles/history.md`.

Focused evidence:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `scripts/list-avds.sh`
- `scripts/start-emulator.sh`
- `scripts/install-debug.sh`
- Manual or automated emulator run through first-run search, provider-backed home, refresh, offline stale display, saved-location switch, alert detail, units, effects Off, large font, and narrow-screen checks.
- `git diff --check`

Out of scope:
- 1.x features: air quality, radar, widgets, daily notifications, detailed charts, additional national alerts, and forecast sharing.

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
- MET Norway forecast fallback, including provider contract, DTO/client, mapping tests, fallback selection behavior, cache metadata, attribution, and provider health/backoff.
- Community theme packaging.
- Self-hostable relay.

## First Recommended Implementation Slice

Start with Slice 1: Open-Meteo Provider Contract.

Reason: the repository rule forbids adding a weather provider without a matching Markdown provider contract, and the full specification identifies the Open-Meteo contract as the next required implementation document before DTO/client/mapper work.

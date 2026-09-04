# Oxygen MVP Release Map

Status: ready
Roadmap ID: mvp-2026-08
Source authority: `docs/OXYGEN_FULL_SPECIFICATION.md`
Created: 2026-08-18
Revised: 2026-09-02
Reconciled against remote `main`: `ca28c2c`

Planning note: This roadmap specifies candidate MVP slices. Only `.codex/plans/current.md` may mark one bounded implementation slice as planned.

## Roadmap Rule

This document is a release map, not an active implementation plan. It records intended MVP behavior order and release gates. It does not make any slice planned, covered, implemented, or verified.

Roadmap entries are not evidence. A slice remains only specified until `.codex/plans/current.md` selects it, production code implements it, and focused plus real-path evidence is recorded. A roadmap status may be synchronized to `committed` only when the corresponding implementation cycle and repository history support that state.

Before implementation starts, copy one bounded behavior slice from this release map into `.codex/plans/current.md` with its acceptance boundary, focused evidence, real-path exercise, broad checks, and out-of-scope limits. Keep implementation slices small enough to stop at a verified boundary.

## Evidence Rule

Focused evidence means behavior-specific tests at the provider, repository, Android state, persistence, presentation, or Compose boundary. Live provider checks and emulator/manual exercises are real-path evidence. Gradle compilation, unit-test task execution, assembly, dependency reports, and `git diff --check` are broad checks unless a selected slice defines a narrower reason.

Raw build/test output may remain ignored under `.codex/test-artifacts/`, but evidence required for roadmap, release-gate, or readiness claims must either be reproducible through CI or retained in a reviewable project artifact. Do not require every cycle log to be committed.

## Documentation Sync Rule

README, roadmap, disclosure, and active-cycle state are part of the product contract. Add a documentation-sync gate after every four completed non-documentation implementation cycles, and sooner when a slice changes any of these status surfaces:

- installed-app behavior listed in README;
- active/current provider or data-source disclosure;
- privacy, permission, license, dependency, or attribution claims;
- persistence, offline, stale-cache, saved-location, or release-readiness status;
- roadmap next-candidate sequencing.

Documentation-sync gates use the documentation-only workflow:
`discover -> contract/document -> review -> ready`.

They must correct status without upgrading implementation states beyond the evidence recorded in `.codex/plans/current.md`, `.codex/cycles/history.md`, CI, or retained artifacts.

Android build/test commands are not required for pure Markdown updates, but any skipped command must be named and justified.

## UI Rule

Every user-facing active slice must carry the relevant UI specification with it. Do not defer UI obligations into a separate polish phase when they are part of the behavior being implemented.

The initial Home look-and-feel direction is a reviewable product artifact:

```text
docs/assets/oxygen-weather-visual-language-base-art-sheet-v0.2.png
```

Use Base Art Sheet v0.2 as visual-direction authority for Standard Home visual work. It guides weather marks, atmospheric scene language, glass-like surfaces, strong numerals, palette references, forecast/metric/alert composition, and Oxygen/Paper/Terminal translation.

It does not:

- implement app behavior;
- authorize fabricated values;
- authorize provider semantics in UI;
- require runtime bitmap weather assets;
- weaken accessibility requirements;
- require every future theme to use glass, gradients, or the same shape vocabulary.

For MVP user-facing slices:

- primary Home navigation uses semantic viewport-oriented pages rather than one continuous vertical dashboard;
- the initial Standard Home page model is Now -> Hourly -> Daily -> Details;
- page identity is represented semantically rather than by unexplained numeric indexes;
- vertical scrolling is reserved for content whose length, reading nature, or accessibility overflow genuinely requires it;
- Standard Home should not require whole-dashboard vertical traversal at ordinary supported display/font configurations;
- important weather meaning remains readable with decorative effects, gradients, transparency, and animation disabled;
- safety information is visible text/structure, not color alone;
- UI supports large font, RTL where applicable, meaningful semantics, adequate touch targets, and logical TalkBack order;
- provider DTOs never reach Composables;
- UI receives presentation-ready state derived from provider-neutral domain models.

There is no future generic "make the UI good" phase.

Slices 18G and 18H already consolidated and verified the Standard Home design baseline, and Slice 18I completed the bounded mobile-ergonomics follow-up. Future user-facing features must build forward from that committed baseline and carry their own finished UI obligations rather than reopening completed 18-series work.

## Presentation Data Rule

Presentation must not alter weather semantics for convenience.

Composables must not:

- parse formatted display strings back into numeric values;
- identify metric semantics by matching localized display labels;
- fabricate missing weather values;
- treat unavailable values as zero;
- receive provider-specific DTOs or provider-specific errors.

When visualization requires numeric values, the presentation contract must deliberately expose numeric semantic values alongside formatted text.

Metric identity required for grouping, iconography, prominence, localization, themes, or alternate layouts must be represented semantically.

## MVP Acceptance Boundary

Oxygen MVP is ready when a user can install the app, choose or search a location without granting location permission, view real current/hourly/daily weather from the default Open-Meteo forecast path with verified MET Norway fallback, understand source/update/stale status, save and switch locations, retain the latest forecast offline, view supported official alerts, change units and core presentation settings, and use the app without advertising, tracking, account, cloud dependency, or Google Play Services as a core requirement.

## Forecast Provider Scope

Open-Meteo is the default MVP forecast provider and MET Norway is the MVP forecast fallback.

MET Norway contract, production client/mapper, repository fallback selection, installed-app fallback wiring, cache provenance, and real-path fallback verification must be complete before release-candidate status or before MET Norway is described as an active fallback in Data Sources.

Do not present Open-Meteo-only behavior as MVP-complete or release-ready.

## Release Gate

Release-candidate status is blocked unless the roadmap and implementation match `docs/OXYGEN_FULL_SPECIFICATION.md` or the specification has been explicitly amended first.

Release verification must prove:

- Open-Meteo default forecast behavior;
- MET Norway fallback forecast behavior;
- truthful provider provenance;
- Data Sources lists active providers only when their production paths can fetch or serve data;
- forecast provider preference does not disable official alert lookup.

---

## Repository Engineering Gate

Status: specified

Release intent: Repository hygiene and durable verification are established before major persistence work and before any release, beta, contributor-readiness, or MVP-complete claim.

Must prove:

- source-license intent is deliberate and repository notices are consistent;
- baseline GitHub CI runs Android compile, unit tests, assembly, and `git diff --check`, or an equivalent hosted setup;
- at least one hosted CI run passes before CI is cited as durable evidence;
- branch protection prevents force-push/deletion and requires baseline checks/PR flow as intended;
- README maturity/status does not imply unverified MVP behavior;
- evidence retention distinguishes ephemeral local logs from reviewable artifacts and CI-reproducible evidence.

Out of scope:

- app behavior;
- provider behavior;
- persistence;
- settings;
- alerts;
- release readiness.

---

## Slice 1: Open-Meteo Provider Contract

Status: specified

Release intent: Specify the default forecast provider before code is added.

Must prove:

- every required provider-template field is completed;
- contracted fields support Home current/hourly/daily/metrics/sun/update/source/provenance/stale needs;
- Open-Meteo current values are labeled model estimates unless documentation proves observation semantics;
- provider-specific fields remain separate from Oxygen semantics.

## Slice 2: Open-Meteo Fixtures and DTO Parsing

Status: specified

Release intent: Parse representative Open-Meteo fixtures without live internet.

Must prove:

- DTOs parse only required Home-path fields;
- required envelope validation fails deterministically;
- nullable values remain null;
- provider DTOs remain isolated from UI/domain consumers.

## Slice 3: Open-Meteo Weather-Code and Domain Mapping

Status: specified

Release intent: Convert parsed Open-Meteo data into provider-neutral Oxygen forecast domain data.

Must prove:

- supported weather codes map to `WeatherCondition`;
- unknown codes map to `UNKNOWN`;
- canonical units and `Instant` timestamps are used;
- null preservation remains intact;
- provenance identifies Open-Meteo and appropriate timestamps/source/license/data type.

## Gate 3A: Repository License and Privacy Document Baseline

Status: specified

Must prove:

- deliberate `LICENSE`, `NOTICE`, `THIRD_PARTY_LICENSES.md`, `DATA_SOURCES.md`, and `PRIVACY.md` exist;
- weather-data licensing remains separate from Oxygen source-code licensing;
- only implemented providers are active/current;
- privacy text covers no ads/tracking/account requirement, optional location permission, and request data sent to active providers.

## Slice 4: Open-Meteo Client Transport and Error Classification

Status: specified

Release intent: Fetch Open-Meteo through an isolated production client.

Must prove:

- URL/query construction is isolated from UI;
- only required fields are requested;
- successful responses use production DTO parsing;
- network/offline, provider unavailable, HTTP/rate-limit where detectable, and invalid-response failures are classified.

## Slice 5: Explicit-Location Open-Meteo Repository Path

Status: specified

Release intent: Given an explicit `WeatherLocation`, return provider-neutral forecast data without sample weather.

Must prove:

- repository uses the selected location exactly;
- loading/success/error are provider-neutral;
- no hidden default location exists;
- `SampleWeather.bundle`, DTOs, and provider-specific errors do not cross into production UI/domain boundaries.

## Slice 6: Geocoding Provider Contract

Status: specified

Release intent: Specify the MVP geocoding provider before code is added.

Must prove:

- provider-template fields are completed;
- fields support place search, coordinates, timezone, country/admin area, and optional elevation;
- provider IDs are not user-facing `LocationId` values;
- a public Nominatim server is not the only production autocomplete backend.

## Slice 7: Geocoding Fixtures and Domain Mapping

Status: specified

Must prove:

- fixtures cover normal, empty, ambiguous, malformed, missing optional, invalid coordinate, and invalid timezone cases;
- mapper returns provider-neutral location data and stable local `LocationId`;
- ambiguous places remain distinguishable;
- invalid required fields map to explicit domain errors.

## Slice 8: Geocoding Search Client and Repository Boundary

Status: specified

Must prove:

- network construction is isolated from UI;
- repository exposes loading/success/empty/provider-unavailable/network/rate-limit/invalid-response states;
- search ordering is deterministic for identical responses;
- only domain models cross the repository boundary.

## Slice 9: First-Run Manual Location Entry

Status: specified

Release intent: A first-run user can start with manual search without granting location permission.

Must prove:

- manual search and "use my location" are separate actions;
- manual search does not request location permission;
- no selected location routes to manual selection rather than sample weather;
- permission denial does not block manual forecast use;
- no hidden/scaffold/default/sample location satisfies Home success.

### Slice 9A: Manual Search Results Selection

Status: specified

Must prove:

- results come from the production geocoding repository;
- similar names are disambiguated;
- selected results produce provider-neutral `WeatherLocation`;
- provider DTOs/IDs do not cross into Home or saved-location UI;
- empty/offline/rate-limit/provider-unavailable/invalid-response states are visible.

### Slice 9B: Selected Location Handoff To Home

Status: specified

Must prove:

- Home receives exactly the selected `WeatherLocation`;
- no fallback location is substituted;
- the handoff is observable before Home success;
- long place names remain readable.

## Slice 10: Manual Selection Routes to Home Loading, Error, and Retry

Status: specified

Must prove:

- no selected location routes to first-run selection;
- selected location drives Home loading;
- Home loads through `WeatherRepository`;
- loading/error/retry are tied to the selected location and remain provider-neutral;
- retry never substitutes another location.

## Slice 11: Provider-Backed Home Success Presentation

Status: specified

Release intent: Home renders provider-neutral forecast success data in the initial pre-pager presentation.

Must prove:

- location/current/hourly/daily/metrics/sun/update/source/provenance are shown where available;
- values come from repository results;
- missing values are omitted/unknown rather than fabricated;
- long location names, large font, and effects-disabled presentation remain readable.

## Slice 11A: Explicit Home Refresh and Retry

Status: specified

Must prove:

- visible refresh or pull-to-refresh targets the selected location;
- retry uses the same location;
- recomposition does not trigger refresh loops;
- failed refresh retains useful cache with stale/failure/source metadata where available.

## Slice 12: MET Norway Provider Contract

Status: specified

Release intent: Specify the fallback provider before fallback code.

Must prove:

- provider-template fields including required User-Agent/header identity are completed;
- fields map to the same provider-neutral Home/provenance needs;
- provider-specific fields remain isolated;
- fallback never averages/merges provider values.

## Slice 13: MET Norway Forecast Production Path

Status: specified

Planning note: use bounded sub-slices.

### Slice 13A: MET Norway Fixtures and DTO Parsing

Status: specified

Must prove:

- required fields parse from fixtures;
- invalid envelopes fail deterministically;
- nullable values remain null;
- DTOs remain isolated.

### Slice 13B: MET Norway Symbol and Domain Mapping

Status: specified

Must prove:

- symbols map to provider-neutral `WeatherCondition`;
- unknown symbols map to `UNKNOWN`;
- canonical units/timestamps/nulls remain correct;
- provenance identifies MET Norway.

### Slice 13C: MET Norway Client Transport and Error Classification

Status: specified

Must prove:

- required headers/User-Agent/base URL/query are isolated;
- production parsing is used;
- network/offline/provider unavailable/rate-limit/cache-not-modified where applicable/invalid-response states are classified.

### Slice 13D: Explicit-Location MET Norway Repository Path

Status: specified

Must prove:

- repository accepts explicit selected location;
- provider-neutral success/error are exposed;
- no hidden location;
- provider DTOs/errors do not reach UI, saved locations, unit presentation, or cache consumers.

## Slice 14: Forecast Fallback Selection

Status: specified

Release intent: Repository attempts Open-Meteo and falls back to MET Norway only under eligible failures.

Must prove:

- Open-Meteo success does not call fallback;
- eligible primary failure plus fallback success returns MET Norway provenance;
- both-provider failure remains retryable and diagnostically preserves both causes;
- repeated failures do not create wasteful retry loops.

Boundary:

This proves repository fallback selection only. Installed-app fallback wiring, fallback cache provenance, and real-path fallback verification remain later work.

## Slice 15: In-App About, Privacy, Licenses, and Data-Source Surface

Status: specified

Must prove:

- Data Sources, Open Source Licenses, and Privacy are visibly reachable;
- active/current provider claims match production behavior;
- source/update/provenance remain visible on Home;
- repository disclosure files match in-app provider claims;
- weather-data licensing remains separate from Oxygen source licensing.

## Slice 16: Cache One Forecast Bundle Through Repository

Status: specified

Must prove:

- provider results write transactionally and read through provider-neutral storage;
- entities preserve location identity, current/hourly/daily data, provenance, timestamps, timezone, canonical units, and nulls;
- rows are scoped by stable local `LocationId`.

Boundary:

This does not claim failed-refresh retention, offline launch, or broad offline-first behavior.

## Slice 17: Failed Refresh Retains Cached Forecast

Status: specified

Must prove:

- failed refresh with useful cache keeps Home usable;
- stale age/source/update/refresh-failure metadata remain visible;
- retry remains available;
- failed refresh without cache becomes retryable no-cache error.

## Slice 17A: Home Presentation Alignment

Status: specified

Historical baseline work superseded for future Home interaction architecture by Slice 18A.

Must prove:

- provider-backed success/stale-success content remains complete;
- values remain provider-neutral and non-fabricated;
- current hero contains Oxygen weather identity;
- compact/large-font/effects-off behavior remains understandable;
- loading/error/retry/source/stale/provenance behavior remains observable.

## Slice 17B: Explicit Home Refresh Control

Status: specified

Must prove:

- explicit refresh is reachable on success and stale-success;
- refresh targets the exact selected location;
- recomposition does not trigger refresh;
- refresh-in-progress/success/failure/no-cache states remain provider-neutral;
- control remains accessible on narrow/large-font configurations.

## Slice 17C: Home Presentation Accessibility Evidence Baseline

Status: specified

Must prove:

- success/stale/loading/error/source/provenance/refresh states are exercised at Compose or Android boundary;
- important semantics have meaningful alternatives and logical reading order;
- compact/large-font presentation avoids overlap;
- effects-disabled presentation remains complete;
- screenshots/hierarchy/test evidence are retained.

## Persistence Architecture Gate

Status: specified

Prerequisites:

- Repository Engineering Gate.
- Slice 17B and Slice 17C unless an active cycle records a narrower reason.

Release intent: Settle production forecast persistence architecture before later local-state work depends on it.

Must prove:

- Room or an explicitly amended alternative is the canonical forecast persistence boundary;
- provider-neutral repository boundaries remain intact;
- forecast persistence preserves location identity, forecast rows, provenance, timestamps, timezone, canonical units, and missing values;
- provider-specific cache metadata may remain deferred to Slice 31B;
- provider success writes through transaction replacement semantics;
- same-location scoping prevents cross-location cache satisfaction;
- the role/removal path for `FileForecastCacheStorage` is explicit;
- persistence tests cover read/write, replacement, scoping, null preservation, provenance, and local failure mapping.

## Slice 18: Offline Launch From Last Forecast

Status: committed

Prerequisites:

- Persistence Architecture Gate.

Release intent: Relaunching without network displays the last cached forecast for the selected location.

Must prove:

- small-state persistence stores selected local `LocationId`;
- forecast remains in canonical forecast storage rather than DataStore;
- startup restores selected location and local forecast;
- offline Home shows cached data with explicit stale age;
- no-cache launch is retryable;
- startup refresh replaces persisted data on success and retains stale data on failure;
- installed-app state uses lifecycle-aware collection/cancellation/process recreation boundaries;
- online/offline with/without cache and failed foreground refresh with/without cache are observable.

## Slice 18A: Home Paged Interaction Foundation

Status: committed

Prerequisites:

- Slice 18.
- Screenshot feedback workflow established.

Release intent: Replace the continuous Home dashboard with the Standard semantic page container and navigation model while preserving provider-backed behavior.

Must prove:

- semantic Now, Hourly, Daily, Details pages exist;
- page identities are semantic;
- horizontal page navigation works;
- appropriate page-state indication exists;
- interactive children retain behavior;
- accessibility exposes page identity/navigation;
- existing Home information remains reachable;
- current/hourly/daily/metrics/sun/source/stale/refresh/retry behavior remains intact;
- normal Standard Home no longer depends on one page-level vertical dashboard;
- installed screenshots prove interaction structure.

Out of scope:

- substantial page visual redesign;
- theme engine;
- layout/effects preferences;
- new analytics;
- new providers;
- foldable behavior.

## Slice 18B: Now Page Visual Baseline

Status: committed

Prerequisite:

- Slice 18A committed.

Release intent: Establish the canonical Oxygen current-conditions experience.

Must prove:

- temperature/condition establish primary hierarchy;
- location is understandable without dominating;
- feels-like/high-low/current context form a coherent support group;
- fresh source/update are tertiary;
- stale/operational state can become prominent;
- refresh/retry remain available;
- Now behaves like a deliberate viewport rather than a generic card stack;
- long location, large-font, effects-off paths remain complete;
- screenshot evidence is retained.

## Slice 18C: Hourly Page Visual Baseline

Status: committed

Prerequisite:

- Slice 18B committed.

Release intent: Make Hourly a dedicated, highly scannable near-term composition.

Must prove:

- time/condition/temperature/precipitation communicate upcoming weather efficiently;
- condition identity is recognizable;
- page answers "what happens next?";
- visualization uses semantic numeric presentation data if introduced;
- Composables do not parse formatted strings;
- no data is fabricated;
- ordinary presentation is not a long scrolling document;
- screenshots and accessibility semantics validate density/hierarchy.

## Slice 18D: Daily Page Visual Baseline

Status: committed

Prerequisite:

- Slice 18C committed.

Release intent: Optimize Daily for fast multi-day comparison.

Must prove:

- multiple days compare quickly;
- Base Art Sheet direction informs strong numerals, compact marks, atmospheric surfaces, and calm density;
- condition identity remains clear;
- high/low information has comparative structure;
- precipitation is visible where available;
- temperature-range visualization may use semantic numeric data;
- no formatted-string parsing;
- sun data is used only where useful;
- ordinary presentation is not another long document;
- large-font/accessibility fallback remains complete.

## Slice 18E: Details Page Visual Baseline

Status: committed

Prerequisite:

- Slice 18D committed.

Release intent: Create a coherent information-dense secondary page for metrics and provenance.

Must prove:

- already-supported metrics are presented meaningfully;
- metrics are structured rather than dumped as one label/value list;
- novelty gauges are avoided;
- provenance remains reachable;
- fresh provenance is normally tertiary;
- stale/fallback/failure provenance can become prominent;
- missing values remain missing/unknown/omitted;
- ordinary Details composition avoids unnecessary scrolling;
- screenshots verify density/organization;
- provider capabilities are not added solely to populate Details.

Implementation note:

The committed Slice 18E cycle verified structured Comfort, Wind, Atmosphere, Source/update, Sun, and provenance sections through provider-neutral Home presentation data. The next roadmap candidate is therefore Slice 18F.

---


## Slice 18F: Home Operational State Integration

Status: committed
Implementation commit: `79bd830`

Prerequisites:

- Slices 18A through 18E committed.
- Existing offline restoration path intact.

Release intent: Verify and tighten the Standard Home pager across existing operational states without redesigning providers, persistence, or the established page visuals.

Committed result:

- duplicate Home refresh calls are ignored while ready-state refresh is already in progress;
- restored/stale cached Home content remains visible as stale-after-failed-refresh when foreground refresh fails;
- fresh, cached/stale, refresh-failed, loading, retryable no-cache, source/update, and page-navigation behavior remain observable;
- Now, Hourly, Daily, and Details remain reachable in restored-cache state;
- provider, Room, DataStore, fallback, saved-location, units, alerts, and appearance-persistence behavior were not expanded.

Verification boundary:

- focused HomeForecast state tests;
- focused Home Compose instrumentation;
- installed cached/offline Home evidence;
- installed retryable no-cache evidence;
- broad Android compile/unit/assemble checks;
- `git diff --check`.

Historical note:

This slice is complete. Do not create new 18F.x implementation slices or insert new gates before 18G. Any future improvement inspired by 18F must be planned after the current committed boundary.

---

## Slice 18G: Oxygen Home Design-System Consolidation

Status: committed
Implementation commit: `fae63b3`

Prerequisites:

- Slice 18F committed.

Release intent: Consolidate repeated Standard Home visual choices into app-local Oxygen design roles and make the Base Art Sheet direction visibly present in the installed Home UI.

Committed result:

- app-local Home spacing, card shape/padding, glass surface, outline/accent, weather-mark, and typography roles were introduced;
- generic blob-like weather marks were replaced by provider-neutral gold-line marks for existing `WeatherCondition` values;
- roles were applied across Now, Hourly, Daily, Details, status, metrics, source, and shared glass surfaces;
- provider, repository, Room, DataStore, weather mapping, units, saved locations, alerts, and persisted appearance behavior were unchanged;
- the full theme engine and persisted appearance selection remained deferred.

Verification boundary:

- focused HomeForecast tests;
- focused Home Compose instrumentation including rendered weather-mark treatment;
- installed Now/Hourly/Daily/Details/stale screenshots;
- broad Android checks;
- `git diff --check`.

Documentation governance note:

The tracked asset path and specification use Base Art Sheet v0.2 to match the
visible title in the reviewable source image. This naming cleanup does not
reopen Slice 18G.

---

## Slice 18H: Standard Home Accessibility and Visual Verification Gate

Status: committed
Implementation commit: `4f5f383`

Prerequisites:

- Slice 18G committed.

Release intent: Establish the completed Standard Home as the verified interaction/visual reference architecture for later MVP work.

Committed result:

- named previous/next accessibility actions were added to the existing pager;
- tabs and swipe remain the visible navigation model;
- Standard Home page tabs meet the 48dp minimum touch-height target;
- an app-local non-persisted `OxygenAppearance` input permits `EffectsLevel.OFF` to render opaque surfaces while preserving weather semantics;
- child-control isolation, compact width, large font, stale/source/error meaning, and effects-disabled meaning were exercised;
- installed Now, Hourly, Daily, Details, and stale refresh-failed evidence was captured;
- no persisted layout/theme/effects selection or new feature domain was added.

Historical rule:

Slice 18H freezes the Standard Home baseline for forward planning. Later feature work may extend the UI, but should not silently redefine the verified page architecture.

---

## Slice 18I: Mobile One-Handed Home Ergonomics

Status: committed
Implementation commit: `02f701`

Prerequisites:

- Slice 18H committed.

Release intent: Apply a bounded handheld ergonomics follow-up before Saved Locations without changing weather, provider, or persistence semantics.

Committed result:

- first-run and change-location content scroll above bottom-aligned Search, Use my location, Settings/About, and Back actions;
- About overview/detail content scrolls above a bottom Back action;
- scrollable Home pages receive footer clearance;
- Now keeps current weather visually ahead of stale/refresh status while preserving operational visibility;
- Details presents metrics and source/update before stale status/provenance;
- compact/large-font non-overlap, touch targets, return behavior, and About recovery were covered;
- no saved-location management, schema changes, provider changes, units, alerts, persisted appearance, installed-app MET Norway fallback, or release-readiness behavior was added.

Verification boundary:

- focused HomeForecast unit tests;
- 24 focused Home Compose instrumentation tests on `oxygen_starter`;
- installed first-run, location-result, Now, Hourly, Daily, Details, change-location return, About overview, and Privacy detail evidence;
- broad Android compile/unit/assemble checks;
- `git diff --check`.

Current committed boundary:

All implementation planning now moves forward from Slice 18J. Slice 18J-R
restored the Open-Meteo ready forecast path, and Slice 18J has ready Home Now,
Hourly, Daily, Details, and cached refresh-failed installed evidence through the
real Open-Meteo manual-location path.

---

## Slice 18J: Standard Home Visual Convergence

Status: committed

Prerequisites:

- Slice 18I committed.
- The committed Slice 18G design roles remain the design-system boundary.
- The committed Slice 18H/18I installed screenshots remain the behavioral and accessibility baseline.

Release intent: Complete the default Standard Oxygen Home presentation so it feels deliberately weather-first, atmospheric, and recognizably Oxygen before Saved Locations introduces another major user-facing surface.

This is a forward-only visual-convergence slice. It does not reopen or invalidate Slices 18F through 18I.

Must prove:

- Standard Oxygen Home uses an atmospheric weather scene or equivalent scene role as a deliberate visual foundation rather than reading primarily as a normal Material `Surface`.
- Weather condition and current temperature dominate the Now page more strongly than application chrome.
- Weather marks integrate naturally with the surrounding composition rather than appearing as isolated decorative icons inside generic cards.
- Home page navigation remains semantic and accessible but no longer looks like unmodified Material scaffold/navigation chrome.
- Glass/translucent surfaces are used selectively and semantically rather than making every content block visually identical.
- Typography hierarchy is weather-first: primary forecast values dominate; source/update/provenance and application controls remain appropriately quiet.
- Hourly answers “what happens next?” more efficiently than a collection of independent generic tiles. A compact temporal visualization may be introduced when supported by semantic numeric presentation data.
- Daily supports fast multi-day comparison. A temperature-range visualization may be introduced using existing semantic high/low numeric values.
- Details groups metrics by semantic identity rather than exact English display-label matching.
- Composables do not parse formatted display strings back into numeric values.
- Metric grouping and visualization do not depend on strings such as `"Humidity"` or `"Wind"`.
- Missing weather values remain missing/unknown/omitted rather than fabricated.
- Effects-disabled rendering remains complete and understandable.
- Current provider, repository, Room, DataStore, selected-location, cache, refresh/retry, and provenance behavior remain unchanged.
- Compact phone, large-font, TalkBack, and touch-target behavior established by Slice 18H/18I remain intact.
- Installed-app screenshots show a material visual improvement over the committed 18H/18I Standard Home reference for Now, Hourly, Daily, and Details.

Preferred implementation direction:

- Use `WeatherScene` or an equivalent semantic scene foundation in production Home where it improves the result.
- Preserve `OxygenHomeDesignRoles` as the semantic design-system boundary and extend roles only when repeated visual decisions justify it.
- Evolve Home presentation models deliberately when semantic metric identity or numeric visualization data is required.
- Prefer custom Oxygen navigation/surface composition over stock Material appearance while keeping Material accessibility semantics where useful.
- Keep weather marks procedural/vector where practical.
- Keep decoration independent from weather meaning.

Focused evidence:

- Home presentation/state tests for any presentation-model changes.
- Compose tests for semantic metric grouping, navigation semantics, compact/large-font non-overlap, effects-off meaning, and any new hourly/daily visualization semantics.
- Static or focused checks proving production Home does not group metrics by localized display labels or parse formatted weather strings.

Real-path evidence:

- Installed-app screenshots and hierarchy evidence for:
  - Now;
  - Hourly;
  - Daily;
  - Details;
  - one restored/stale or refresh-failed Home state;
  - Effects Off where feasible.
- Compare against the committed Slice 18H/18I reference screenshots and record the concrete visible improvements.

Broad verification:

```sh
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

Explicitly out of scope:

- Saved-location persistence, selection, list UI, or concurrency behavior.
- Unit preference persistence or conversion.
- Device-location expansion.
- Official alert provider implementation.
- Persisted theme/layout/effects settings.
- Paper/Terminal theme completion.
- New weather-provider fields added solely for richer decoration.
- Radar, maps, air quality, pollen, widgets, background refresh, or notifications.
- Rewriting completed 18-series history.

Completion gate:

Slice 19A must not begin until Slice 18J is committed with installed-app visual evidence and no unresolved regression against the Slice 18H/18I accessibility/behavior baseline.

---

## Slice 18J-R: Restore Installed Open-Meteo Ready Forecast Path

Status: committed

Prerequisites:

- Slice 18J installed-app evidence attempt identified a real Open-Meteo invalid-response blocker.
- Existing Open-Meteo forecast and geocoding provider contracts remain authoritative.

Release intent: Restore the production installed-app path where a manually selected Open-Meteo geocoding result fetches, parses, maps, caches, and presents a usable ready forecast.

Why this recovery slice exists:

Slice 18J automated checks passed for its covered Home presentation changes, but the installed app could not capture final visual evidence because selecting "Madison, Wisconsin, United States" through the real manual Open-Meteo path rendered "Weather data returned in a form Oxygen could not read. Try again later." Provider/forecast parsing behavior was out of scope for 18J, so the fix is separated here.

Must prove:

- the known real manual-location path reaches `ForecastReady` without sample data, mocked provider success, or fabricated fallback data;
- a representative real Open-Meteo response shape is covered at the provider, mapper, repository, or state boundary responsible for the failure;
- current, hourly, daily, source/update, provenance, and required Home fields remain available after mapping;
- invalid-response classification still applies to malformed or contract-breaking provider responses;
- provider-specific diagnostics do not cross into Compose or user-facing Home copy;
- the successful live fetch writes the selected location forecast into the Room cache, or a cache-write failure is recorded as a blocker/regression;
- selected-location persistence, Room forecast cache, stale/restored behavior, refresh/retry behavior, and Open-Meteo attribution remain intact.

Out of scope:

- MET Norway installed-app fallback wiring;
- saved-location list/switching/removal behavior;
- unit preferences or conversion UI;
- device-location expansion;
- Home visual redesign beyond preserving the current in-progress 18J state;
- alerts, air quality, radar/maps, widgets, background refresh, persisted appearance, release readiness, or MVP readiness.

Focused evidence:

- a focused failing-then-passing provider/parser/mapper/repository test, or request-construction test if request parameters caused the invalid response;
- existing invalid-response coverage remains passing;
- HomeForecast-focused state tests if the mapped Home contract changes.

Real-path evidence:

- installed debug app on `oxygen_starter`;
- manual Open-Meteo geocoding selection, starting with Madison, Wisconsin unless provider availability requires a documented equivalent real result;
- installed ready Home screenshot/hierarchy evidence for Now, Hourly, Daily, and Details;
- stale/restored or refresh-failed cached Home evidence for the same selected location after confirming the successful live fetch populated the Room cache.

Broad verification:

```sh
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

Completion gate:

After Slice 18J-R is verified, resume Slice 18J only for the remaining installed-app visual evidence and review. Slice 19A remains blocked until Slice 18J itself is committed.

---

## Slice 19: Saved Locations

Status: specified

Planning note: the original Slice 19 was too broad for one active cycle. Use one bounded sub-slice.

### Slice 19A: Saved Location Storage Model

Status: committed at `d97e2ea`

Prerequisites:

- Slice 18J committed.
- Persistence Architecture Gate.
- Slice 18.

Release intent: Persist a provider-neutral saved-location list independently from selected-location state.

Must prove:

- stable local `LocationId` remains identity;
- provider IDs never become user-facing identity;
- add/remove/list behavior is deterministic;
- duplicate policy is explicit;
- removing the selected location has a defined outcome;
- Room/DataStore responsibilities remain explicit.

Out of scope:

- UI switching;
- reordering;
- folders/groups;
- background refresh of all locations.

### Slice 19B: Saved Location Selection and Concurrency

Status: committed at `0f649aa`

Prerequisite:

- Slice 19A.

Release intent: Selecting a saved location controls Home safely under overlapping asynchronous work.

Must prove:

- obsolete refresh work is cancelled or isolated;
- late emissions for an older location cannot update the new location;
- selected location persists;
- matching cache may appear immediately;
- wrong-location cache never satisfies Home;
- refresh remains explicit/provider-neutral;
- a focused race test covers older completion after newer selection.

### Slice 19C: Saved Location List and Selection UI

Status: committed at `e2efdd3`

Prerequisites:

- Slice 19B.
- Slice 18I.

Release intent: Show existing saved locations on the location-entry surface and
let users select one through the committed saved-location app-state path.

Must prove:

- similar place names are disambiguated;
- current selection is obvious;
- select controls are visible;
- selecting a saved row drives Home through local `LocationId`;
- manual search remains fully available without permission;
- compact and large-font layouts work.

Out of scope:

- search-result save UI;
- saved-location removal UI;
- drag reorder;
- folders;
- automatic multi-location refresh.

### Slice 19D: Save Search Result UI

Status: specified

Prerequisite:

- Slice 19C.

Release intent: Let users save a searched place from the location-entry surface
without making saving a prerequisite for one-off manual selection.

Must prove:

- search result rows expose a clear save control;
- saving uses production `SavedLocationStorage`;
- save success refreshes the saved list;
- save failure surfaces as a local saved-location failure;
- manual `Use now` selection still works when saved storage is unavailable or
  save fails;
- compact and large-font layouts keep search, save, and use-now controls
  readable and reachable.

Out of scope:

- saved-location removal UI;
- drag reorder;
- folders/groups;
- automatic multi-location refresh.

### Slice 19E: Remove Saved Location UI

Status: specified

Prerequisite:

- Slice 19D.

Release intent: Let users remove saved locations from the location-entry surface
without accidentally deleting rows or changing the current Home forecast.

Must prove:

- saved rows expose a visible remove control;
- removal requires an explicit confirmation/cancel step before production
  storage deletion;
- cancel does not delete;
- confirmed removal refreshes only saved-location list state;
- removing the currently selected location does not clear or rewrite DataStore
  selected-location state, forecast-cache rows, or the visible Home forecast;
- compact and large-font layouts keep remove confirmation readable and
  reachable.

Out of scope:

- drag reorder;
- folders/groups;
- automatic multi-location refresh;
- automatic replacement when the removed row is currently selected.

### Gate 19F: Saved Locations Documentation Sync

Status: specified

Prerequisite:

- Slice 19E.

Release intent: Align README, roadmap, disclosure, and active-cycle status with
the saved-location behavior actually verified in Slices 19A through 19E.

Must prove:

- README implemented/not-implemented saved-location claims match verified
  installed-app behavior;
- roadmap saved-location sub-slice status does not exceed recorded evidence;
- data-source, privacy, cache, and provider claims remain unchanged unless a
  saved-location slice truly changed them;
- skipped Android commands are named and justified if the gate is
  documentation-only.

Out of scope:

- app behavior;
- provider behavior;
- persistence schema changes;
- release-readiness or MVP-readiness claims.

---

## Slice 20: Unit Preferences and Conversion

Status: specified

Planning note: use bounded sub-slices.

### Slice 20A: Unit Preference Contract

Status: specified

Prerequisite:

- small-state persistence foundation.

Release intent: Define unit preferences before conversion/UI work.

Must prove explicit preference behavior for:

- temperature;
- wind speed;
- pressure;
- precipitation;
- visibility.

Metric, US, UK, and custom behavior must be defined without changing canonical stored values.

## Gate 20-0: Presentation Semantics and Localization Safety

Status: specified

Recommended timing:

- after Slice 20A defines the unit contract;
- before Slice 20B adds conversion behavior;
- before theme/layout variants proliferate.

Release intent: Ensure presentation behavior depends on semantic data rather than English labels or formatted strings before units and alternate appearance modes multiply those paths.

Must prove:

- metric grouping does not depend on exact localized display labels;
- metric identity needed for grouping, iconography, emphasis, units, or themes is represented semantically;
- Composables do not parse formatted temperature, percentage, pressure, distance, precipitation, or wind strings back into numbers;
- numeric values needed for visualization/conversion are deliberately exposed by presentation models alongside display text;
- reusable touched Home/Settings strings move toward Android resources;
- accessibility descriptions are not reconstructed by parsing English display text;
- provider DTOs remain outside Composables;
- missing values remain missing.

This gate does not require shipping translations.

---

### Slice 20B: Unit Conversion Presentation Boundary

Status: specified

Prerequisites:

- Slice 20A.
- Gate 20-0.

Release intent: Convert canonical weather values only for presentation.

Must prove:

- stored canonical data is unchanged;
- null remains null;
- unavailable never becomes zero;
- deterministic rounding;
- correct wind-direction semantics;
- source/provenance unaffected;
- conversion edge cases are tested.

### Slice 20C: Persisted Units UI

Status: specified

Prerequisites:

- Slice 20B.
- Slice 18I.
- Slice 25A recommended before or with this UI.

Release intent: Users can persist units without destabilizing Home.

Must prove:

- settings are reachable;
- values update consistently across Now/Hourly/Daily/Details;
- long converted values fit;
- large-font remains usable;
- preferences survive restart;
- unit change does not trigger provider fetch unless independently required.

---

## Slice 21: Optional Device Location

Status: specified

Prerequisites:

- saved-location selection foundation.

Release intent: Device location is optional while manual search remains first-class.

Must prove:

- permission is requested only after explicit user action;
- permission denial returns to usable manual search;
- no background location;
- coordinates resolve into provider-neutral location state;
- device-resolved locations use the same selection/cache/forecast architecture;
- approximate location is represented honestly.

Manual location remains sufficient for successful onboarding and normal use.

---

## Slice 22: NWS Alert Provider Contract

Status: specified

Prerequisites:

- Persistence Architecture Gate.
- forecast fallback remains independent from alert lookup.

Release intent: Specify US official alert integration before implementation.

Must prove the provider contract defines:

- endpoint/authentication;
- required User-Agent/header identity;
- rate/request limits;
- caching;
- fields;
- timestamps;
- severity/urgency/certainty mapping;
- errors;
- attribution/license/privacy;
- unsupported-region behavior;
- fixtures/documentation;
- alert identity/deduplication;
- update/replacement semantics;
- expiration handling;
- geometry/affected-area fallback;
- UI-required banner/detail fields.

Official alerts must remain distinct from forecast-derived risk.

---

## Slice 23: NWS Alert Provider Path

Status: specified

Planning note: use bounded sub-slices.

### Slice 23A: NWS Alert Fixtures, Parsing, and Mapping

Status: specified

Must prove:

- no/one/many alerts;
- missing optional fields;
- timestamps;
- affected areas;
- unknown severity;
- severity/urgency/certainty/event/issuer/effective/expires/description/instructions/geometry/provenance retained where available;
- provider DTOs remain isolated;
- forecast risk is not represented as an official alert.

### Slice 23B: NWS Alert Client and Error Classification

Status: specified

Must prove:

- required headers/User-Agent/base URL/request are isolated;
- successful responses use production parsing;
- network/offline/provider unavailable/rate-limit/unsupported region/no alerts/invalid response are classified;
- provider cache guidance is respected.

### Slice 23C: Alert Repository Merge

Status: specified

Must prove:

- forecast and alert results combine without coupling provider selection;
- alert failure does not block forecast display;
- forecast fallback preference does not disable alert lookup;
- stale forecast plus fresh alert is representable;
- fresh forecast plus alert-provider failure is representable;
- duplicate alert IDs do not duplicate UI;
- unsupported regions are explicit.

---

## Slice 24: Official Alert UI

Status: specified

Planning note: use bounded sub-slices.

### Slice 24A: Alert Summary/Banner UI

Status: specified

Prerequisites:

- Slice 23C.
- Slice 18I.

Release intent: Expose active official alerts on Home without overwhelming normal weather.

Must prove:

- event and severity text are visible;
- issuer/expiration are reachable;
- severity is not color-only;
- multiple alerts have an explicit affordance;
- alerts do not destroy Now hierarchy;
- stale forecast and active-alert state can coexist.

### Slice 24B: Alert Detail UI

Status: specified

Prerequisite:

- Slice 24A.

Must expose where available:

- event;
- severity;
- urgency;
- certainty;
- issuer;
- effective;
- expires;
- affected area;
- description;
- instructions;
- attribution.

Long official text may scroll. Large font and TalkBack reading order must remain usable.

---

## Gate 25: Disclosure Baseline Check

Status: specified

Prerequisite:

- Repository Engineering Gate.

Release intent: Confirm disclosure still matches implemented behavior before appearance and release work.

Must prove:

- `LICENSE`, `NOTICE`, `THIRD_PARTY_LICENSES.md`, `DATA_SOURCES.md`, and `PRIVACY.md` remain present and accurate;
- active/current provider claims match production paths;
- weather-data licensing remains separate from source-code licensing;
- privacy still reflects no ads, no tracking, no account requirement, optional location permission, and provider request data.

---

## Slice 25A: Settings Information Architecture

Status: specified

Prerequisite:

- Slice 18I.

Release intent: Create a scalable Settings architecture before multiple preference families accumulate.

Must prove distinct reachable categories as appropriate:

- Appearance;
- Units;
- Locations;
- Data Sources;
- Privacy;
- Open Source Licenses;
- About.

Boundary:

This organizes navigation/surfaces. It does not implement new preference behavior by itself.

---

## Recurring Documentation Sync Gate

Status: specified

Cadence:

- after every four completed non-documentation implementation cycles since the previous documentation-sync gate;
- immediately when behavior/disclosure/privacy/persistence/release-readiness or roadmap sequencing changes.

Must prove:

- README status matches installed behavior;
- roadmap next-candidate guidance matches completed work;
- data-source/privacy/license documents distinguish active behavior from specified roadmap work;
- `.codex/plans/current.md` records the sync or the next bounded slice;
- `.codex/cycles/history.md` records evidence;
- `git diff --check` passes;
- skipped Android commands are named when documentation-only.

---

## Appearance Preference Relationship

Status: specified

Slice 18H establishes the canonical Standard Home reference and Slice 18I is the committed handheld-ergonomics follow-up. Later effects, layout, theme, and contrast variants must translate that current baseline without changing weather semantics, source/stale/error behavior, alerts, or accessibility guarantees.

Appearance work is not a generic polish backlog.

---

## Slice 26: Effects Off Preference Baseline

Status: specified

Prerequisites:

- Slice 18I.
- Slice 25A.
- small-state persistence.

Must prove:

- appearance settings are visibly reachable;
- Effects Off removes continuous decorative animation/effects while preserving meaning;
- reduced-motion preference is respected where available;
- alerts/source/stale/provenance remain visible;
- effects preference persists;
- effects remain independent from layout/theme.

---

## Slice 27: Layout Density Preference Baseline

Status: specified

Planning note: split definition from persistence.

### Slice 27A: Simple Layout Definition

Status: specified

Prerequisite:

- Slice 18H.

Release intent: Define Simple before making it selectable.

Must prove:

- required MVP weather meaning is retained;
- Simple is not Standard with arbitrary content removed;
- source/stale/alert information remains reachable;
- page semantics remain coherent or an explicitly specified alternative replaces them.

### Slice 27B: Persisted Layout Selection

Status: specified

Prerequisites:

- Slice 27A.
- Slice 25A.
- small-state persistence.

Must prove:

- Standard remains default;
- Simple/Standard switching requires no provider refetch;
- selection persists;
- both layouts pass compact/large-font checks;
- layout remains independent from effects/theme.

Out of scope:

Do not implement Detailed or Meteorologist merely because enum values already exist.

---

## Slice 28: Theme Selection Baseline

Status: specified

Planning note: split translation quality from persistence.

### Slice 28A: Theme Translation Completion

Status: specified

Prerequisites:

- Slice 18G.
- Slice 18I.

Release intent: Make every MVP theme a deliberate translation of semantic design roles.

Must prove for each theme intended for MVP:

- semantic surfaces are mapped deliberately;
- operational/warning states remain readable;
- weather marks remain readable;
- typography is intentional;
- effects-off remains complete;
- weather semantics do not change.

Theme quality rule:

Existing scaffold values do not guarantee inclusion. Paper or Terminal may be deferred rather than shipped weakly.

### Slice 28B: Persisted Theme Selection

Status: specified

Prerequisites:

- Slice 28A.
- Slice 25A.
- small-state persistence.

Must prove:

- theme settings are reachable;
- choice persists across restart;
- provider refetch is not required;
- theme remains independent from layout/effects.

---

## Slice 29: High-Contrast Presentation Baseline

Status: specified

Prerequisites:

- Slice 18G.
- Slice 18I.
- Slice 25A.

Must prove:

- high contrast is a semantic accessibility presentation, not merely brighter colors;
- required meaning never depends on color;
- compact + large font + effects off remains usable;
- operational and alert states remain distinct;
- preference persists if user-selectable.

---

## Gate 30: Accessibility Presentation Verification

Status: specified

Release intent: Verify MVP presentation paths under accessibility-oriented Android conditions.

Must prove where applicable:

- TalkBack order;
- meaningful labels;
- touch targets;
- compact phone;
- large font;
- RTL;
- reduced motion;
- effects Off;
- high contrast;
- long location/provider/alert names;
- unit-converted values;
- no important clipping/overlap.

Any skipped condition must name the exact blocker.

---

## Slice 31: Installed-App Forecast Fallback Completion

Status: specified

Planning note: split wiring from cache/provenance.

### Slice 31A: Installed-App Fallback Wiring

Status: ready

Prerequisites:

- forecast fallback repository selection;
- production Home forecast path.

Release intent: Wire installed-app forecast selection so Open-Meteo remains default and MET Norway can actually serve as fallback.

Must prove:

- fallback eligibility is explicit;
- failures that should not trigger fallback do not trigger it;
- Open-Meteo success remains default;
- fallback MET Norway success maps through provider-neutral state;
- provider-specific DTO/errors do not reach UI;
- installed app can reach the fallback-served Home state under a controlled eligible primary failure.

Important dependency correction:

Saved Locations is not a prerequisite unless implementation genuinely touches saved-location behavior.

### Slice 31B: Fallback Cache and Provenance

Status: specified

Prerequisites:

- Slice 31A.
- forecast persistence architecture.

Must prove:

- provider ID/source/license/timestamps/cache metadata remain truthful;
- cached MET Norway forecast restores as MET Norway forecast;
- later Open-Meteo refresh replaces it only through normal verified refresh transaction;
- failed refresh retains truthful stale fallback provenance.

---

## Slice 32: Fallback Real-Path Verification

Status: specified

Prerequisite:

- Slice 31B.

Must prove at installed Android boundary:

- Open-Meteo default success;
- controlled fallback-eligible Open-Meteo failure;
- MET Norway fallback success;
- correct source/update/provenance;
- offline restoration of fallback-served data;
- later successful Open-Meteo refresh;
- official alert lookup remains independent.

---

## Slice 33: MVP Privacy and Dependency Audit

Status: specified

Must prove review of:

- dependency tree;
- manifest;
- permissions;
- advertising/analytics/telemetry absence;
- account/cloud requirements;
- unnecessary Play Services;
- background location;
- exported components;
- backup/data-extraction behavior where relevant;
- cleartext/network-security configuration where relevant;
- active forecast/geocoding/alert providers;
- attribution/privacy/license reachability.

No provider is active/current in disclosures unless its production path can fetch or serve data.

---

## Gate 34: About, Settings, and Data-Source Release Check

Status: specified

Must prove:

- Settings IA matches implemented preferences;
- Data Sources lists only active providers as active;
- forecast/geocoding/alert claims match repository docs;
- Open Source Licenses and Privacy remain reachable;
- source-code license and weather-data licenses remain distinct;
- no placeholder appearance option is exposed as implemented.

---

## Gate 35: Oxygen MVP Broad Verification and Release Candidate

Status: specified

Release intent: Verify MVP behavior against the repository completion standard.

Must prove:

- current/hourly/daily;
- manual first-run search;
- permission-denied manual path;
- Open-Meteo default;
- MET Norway fallback;
- explicit refresh;
- offline restoration;
- stale-after-refresh-failure;
- saved-location add/select/remove;
- units;
- official alerts;
- implemented presentation settings;
- effects Off;
- high contrast if included;
- source/provenance;
- disclosure/privacy.

Required installed-app UI evidence includes:

- Now;
- Hourly;
- Daily;
- Details;
- saved locations;
- settings;
- alert summary;
- alert detail;
- units;
- representative alternate appearance;
- large font;
- compact phone;
- representative operational failure.

Release-candidate status is blocked if:

- `SampleWeather.bundle` satisfies production Home success;
- hidden default location satisfies first-run success;
- missing weather values are fabricated;
- provider DTOs enter Composables;
- fallback is repository-tested but not installed-app wired;
- Data Sources claims inactive providers as active;
- appearance controls are exposed but not persisted/verified;
- required UI semantics depend on English display-string matching.

Broad verification at minimum:

```sh
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
scripts/list-avds.sh
scripts/start-emulator.sh
scripts/install-debug.sh
git diff --check
```

CI must pass before CI is cited as durable release evidence.

---

## Cross-Cutting Forward Rules

### Async location and forecast race safety

Slice 19B must prove that obsolete requests cannot replace current-location state, refresh results are scoped to local location identity, lifecycle/cancellation behavior is explicit, and rapid location switching is deterministic.

### Standard Home visual reference maintenance

Use the committed Slice 18H/18I installed screenshots as the baseline reference set for later user-facing changes. This is review evidence, not pixel-perfect screenshot testing. Later UI slices should capture comparable installed evidence when they materially change Home, Settings, Locations, Alerts, or appearance.

### Feature-surface completion rule

For every post-18I user-facing feature, backend completion is insufficient. The corresponding UI slice must include:

- final Oxygen composition;
- semantic state;
- accessibility;
- compact/large-font behavior;
- operational/error behavior;
- installed screenshot evidence where applicable.

This rule applies especially to Saved Locations, Units, Alerts, Settings, and appearance controls.

---

## Explicitly Deferred From MVP

- Air quality and AQI UI.
- Pollen.
- Radar and maps.
- Home-screen widgets.
- Daily summary/weather-change notifications.
- Background alert polling.
- Moon data.
- Advanced meteorological charts beyond MVP readability.
- Forecast sharing.
- Saved-location reordering/favorites beyond basic management.
- Additional national alert providers beyond NWS.
- Community theme packaging.
- Self-hostable relay.
- Detailed layout.
- Meteorologist layout.

Existing enum/scaffold values do not make a deferred feature implemented.

---

## Recommended Sequence From Current Committed State

Remote `main` is reconciled through merge `ca28c2c`. The latest completed local
implementation slice is Slice 31B, committed in this changeset.

Use this as sequencing guidance, not permission to work multiple slices at once.

1. Slice 19A — Saved Location Storage Model
2. Slice 19B — Saved Location Selection and Concurrency
3. Slice 19C — Saved Locations UI
4. Slice 31A — Installed-App Fallback Wiring
5. Slice 31B — Fallback Cache and Provenance
6. Slice 32 — Fallback Real-Path Verification
7. Slice 20A — Unit Preference Contract
8. Gate 20-0 — Presentation Semantics and Localization Safety
9. Slice 20B — Unit Conversion Presentation Boundary
10. Slice 25A — Settings Information Architecture
11. Slice 20C — Persisted Units UI
12. Slice 21 — Optional Device Location
13. Slice 22 — NWS Alert Provider Contract
14. Slice 23A — NWS Fixtures/Parsing/Mapping
15. Slice 23B — NWS Client/Error Classification
16. Slice 23C — Alert Repository Merge
17. Slice 24A — Alert Summary/Banner UI
18. Slice 24B — Alert Detail UI
19. Gate 25 — Disclosure Baseline Check
20. Slice 26 — Effects Preference
21. Slice 27A / 27B — Simple Layout Definition and Selection
22. Slice 28A / 28B — Theme Translation and Selection
23. Slice 29 — High Contrast
24. Gate 30 — Accessibility Presentation Verification
25. Slice 33 — Privacy and Dependency Audit
26. Gate 34 — About/Settings/Data-Source Release Check
27. Gate 35 — MVP Release Candidate Verification

Run recurring documentation-sync gates at the defined cadence.

Sequencing rationale:

- Slice 18J-R restores the real installed Open-Meteo ready forecast path needed to verify the in-progress Slice 18J UI against production data.
- Slice 18J completes the originally intended Standard Oxygen visual convergence before another major user-facing surface is added.
- Saved-location persistence/switching follows immediately after 18J.
- Installed-app MET Norway fallback is pulled forward immediately after Saved Locations because fallback is an MVP acceptance requirement and repository-only fallback evidence is insufficient for release.
- Unit conversion follows once location switching and fallback provenance are stable.
- Settings information architecture is established before multiple preference families make the current Settings/About surface too broad.
- Appearance persistence remains after the Standard Home design system and accessibility baseline, which are already committed.

---

## Next Candidate Slice

Candidate: Slice 32: Fallback Real-Path Verification.

Immediate planning boundary:

```text
18I committed at 02f701
-> 18J-R committed at 15fc10e
-> resumed 18J Standard Home visual convergence evidence committed at 7950a42
-> 19A saved-location storage model committed at d97e2ea
-> 19B selection/concurrency committed at 0f649aa
-> 19C saved-location list/select UI committed at e2efdd3
-> 31A installed-app fallback wiring committed at 4cdecdd
-> 31B fallback cache and provenance committed in this changeset
-> next candidate: 32 fallback real-path verification
```

Do not reopen 18F, insert new 18F.x slices, or create a new pre-18G visual gate.
Those implementation boundaries are historical and already committed. Slice
18J-R was a provider-path recovery slice required by the blocked Slice 18J
evidence boundary, not a new visual gate.

To start the next implementation slice after committing Slice 31B, replace
`.codex/plans/current.md` with one bounded Slice 32 plan. Do not treat later
roadmap entries as active work.

# Oxygen MVP Release Map

Status: specified
Roadmap ID: mvp-2026-08
Source authority: `docs/OXYGEN_FULL_SPECIFICATION.md`
Created: 2026-08-18
Revised: 2026-09-09
Reconciled against local `origin/main` ref: `82cf281`
Synchronized through Slice 29B implementation `441d05d`, test follow-up
`86e696c`, and evidence sync `86f046b`

Planning note: This roadmap specifies candidate MVP slices. Only `.codex/plans/current.md` may mark one bounded implementation slice as planned.

## Current Implementation Queue

Gate 30's first draft combined independent surfaces and Android conditions.
Implement these bounded candidates in order; 30A1 is committed and 30A2 is the
next specified candidate:

1. Slice 30A1 — Home Spoken-Weather Semantics
2. Slice 30A2 — Home Compact and Large-Font Resilience
3. Gate 30A3 — Home Speech/Layout Evidence and Document Sync
4. Slice 30B1 — Home RTL Navigation and Chronology
5. Slice 30B2 — Home Reduced-Motion and Appearance Invariance
6. Gate 30B3 — Home Environment Evidence and Document Sync
7. Slice 30C1 — Official-Alert Summary Accessibility
8. Slice 30C2 — Official-Alert Detail Accessibility
9. Gate 30C3 — Alert Accessibility Evidence and Document Sync
10. Slice 30D1 — Appearance Control Semantics
11. Slice 30D2 — Appearance Layout and Environment Resilience
12. Gate 30D3 — Appearance Accessibility Evidence and Document Sync
13. Gate 30E — Installed TalkBack and Accessibility Closure

Do not start a later entry merely because it appears here. Each entry remains
`specified` until selected in `.codex/plans/current.md`. A production defect
found by a later verification gate must be repaired at a separately named,
bounded boundary before the affected gate can pass.

## Roadmap Rule

This document is a release map, not an active implementation plan. It records intended MVP behavior order and release gates. It does not make any slice planned, covered, implemented, or verified.

Roadmap entries are not evidence. A slice remains only specified until `.codex/plans/current.md` selects it, production code implements it, and focused plus real-path evidence is recorded. A roadmap status may be synchronized to `committed` only when the corresponding implementation cycle and repository history support that state.

Before implementation starts, copy one bounded behavior slice from this release map into `.codex/plans/current.md` with its acceptance boundary, focused evidence, real-path exercise, broad checks, and out-of-scope limits. Keep implementation slices small enough to stop at a verified boundary.

## Context Budget Rule

Active implementation plans should target completion within roughly 40% of the
available session context. When a candidate slice would combine several
high-context concerns, split it before implementation even if the roadmap gains
more entries.

For normal planning, read this header, the active plan, recent cycle history,
and only the current candidate plus the remaining-sequence tail. Do not reread
the full historical roadmap unless resolving a specific authority conflict,
regression, commit, or release claim.

Split remaining work by independently observable boundaries. Avoid active
slices that combine more than one new persistence format, state-machine
transition set, user-facing UI surface, installed/emulator journey, provider
path, platform adapter, or broad documentation sync.

## Evidence Rule

Focused evidence means behavior-specific tests at the provider, repository, Android state, persistence, presentation, or Compose boundary. Live provider checks and emulator/manual exercises are real-path evidence. Gradle compilation, unit-test task execution, assembly, dependency reports, and `git diff --check` are broad checks unless a selected slice defines a narrower reason.

Raw build/test output may remain ignored under `.codex/test-artifacts/`, but evidence required for roadmap, release-gate, or readiness claims must either be reproducible through CI or retained in a reviewable project artifact. Do not require every cycle log to be committed.

## Documentation Sync Rule

README, roadmap, disclosure, and active-cycle state are part of the product
contract. Make every third roadmap cycle a dedicated test-only and
documentation-sync session after two implementation slices. Sync sooner when a
slice changes any of these status surfaces:

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

Status: ready

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

Status: committed at `8599640`

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

Status: committed at `00cb88a`

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

Status: committed at `8386484`

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

Status: committed at `3f6d741`

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

Committed result:

- Added provider-neutral preference types and deterministic Metric, US, UK, and
  Custom resolution in `:core`.
- Proved all five categories and canonical `WeatherBundle` preservation with
  focused unit tests.
- No conversion math, persistence, UI, provider request, or cache behavior
  changed.

Evidence:

- Focused provider canonical-unit and `UnitPreferenceTest` checks passed.
- Broad compile, unit-test, assemble, and `git diff --check` checks passed.
- Artifacts: `.codex/test-artifacts/2026-09-04-slice-20a-unit-preference-contract/`.

## Gate 20-0: Presentation Semantics and Localization Safety

Status: committed at `587b0ad`

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

Committed result:

- Added explicit nullable canonical values and semantic unit identities to Home
  current, hourly, daily, and metric presentation models while preserving
  existing formatted output.
- Preserved `HomeMetricIdentity` for grouping and `WeatherCondition` for
  condition/icon semantics.
- Verified Home composables do not parse formatted weather text back into
  weather numbers.

Evidence:

- Focused `HomeForecastPresentationMapperTest` and
  `HomeForecastStateHolderTest` checks passed.
- Connected `HomeDashboardUiTest` passed on `oxygen_starter`.
- Installed debug app launched on `oxygen_starter`.
- Broad compile, app/core unit-test, assemble, and `git diff --check` checks
  passed.
- Artifacts:
  `.codex/test-artifacts/2026-09-04-gate-20-0-presentation-semantics-localization-safety/`.

---

### Slice 20B: Unit Conversion Presentation Boundary

Status: committed at `1a2b5a0`

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

Status: committed at `1b52718`

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

Status: committed at `3ea5ae6`

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

Status: committed at `858c0a4`

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

Status: committed at `17dab0c`

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

Status: committed at `dcf707b`

Must prove:

- required headers/User-Agent/base URL/request are isolated;
- successful responses use production parsing;
- network/offline/provider unavailable/rate-limit/unsupported region/no alerts/invalid response are classified;
- provider cache guidance is respected.

### Slice 23C: Alert Repository Merge

Status: committed at `3c658a8`

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

Status: committed at `cf9ddaf`

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

Status: committed at `ceb6253`

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

Status: committed
Commit: `23a9d49`

Prerequisite:

- Repository Engineering Gate.

Release intent: Confirm disclosure still matches implemented behavior before appearance and release work.

Must prove:

- `LICENSE`, `NOTICE`, `THIRD_PARTY_LICENSES.md`, `DATA_SOURCES.md`, and `PRIVACY.md` remain present and accurate;
- active/current provider claims match production paths;
- weather-data licensing remains separate from source-code licensing;
- privacy still reflects no ads, no tracking, no account requirement, optional location permission, and provider request data.

Committed result: Active Open-Meteo forecast/timezone and geocoding, GeoNames,
MET Norway fallback, and foreground NOAA/NWS alert disclosures now include
applicable attribution/license terms, provider links, and request/privacy
facts. The installed direct Settings destinations are scrollable and reachable
without repository calls or location permission requests. MET Norway's new and
legacy-cached Home provenance presents `NLOD-2.0 AND CC-BY-4.0`.

---

## Slice 25A: Settings Information Architecture

Status: committed
Commit state: committed
Commit: `2484e90`

Prerequisite:

- Slice 18I.

Release intent: Create a scalable Settings architecture before multiple preference families accumulate.

Committed result: The installed Settings root now exposes all seven destinations,
retains existing Units and disclosure behavior, opens the real Locations
surface, and returns through both in-surface and Android Back. Appearance is a
read-only summary of the effective presentation.

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

Status: committed at `660e376`

Prerequisite:

- Slice 18H.

Release intent: Define Simple before persisted selection.

Implemented result:

- The installed Settings / Appearance surface can select Simple for the current
  app session only. Standard remains the launch/restart default.
- Simple Home exposes `Now -> Forecast`; Forecast exposes Hourly and Daily
  choices without provider refetch.
- Source/stale/provenance and official alert reachability remain visible where
  supplied; Standard Home remains unchanged.

Must prove:

- required MVP weather meaning is retained;
- Simple is not Standard with arbitrary content removed;
- source/stale/alert information remains reachable;
- page semantics remain coherent or an explicitly specified alternative replaces them.

### Slice 27B1: Layout Preference Storage and State

Status: committed at `b68ca19`

Prerequisites:

- Slice 27A, committed at `660e376`.
- small-state persistence.

Must prove:

- the versioned DataStore codec accepts only Simple and Standard;
- malformed, incomplete, future, Detailed, and Meteorologist records resolve to
  the conservative Standard fallback without aliasing unsupported choices;
- state-holder startup, retry, pending write, failed write, and successful write
  transitions preserve the last confirmed effective layout;
- layout state changes do not rebuild forecast, alert, location, unit, or
  effects data.

Implemented result:

- Added versioned DataStore codec/storage for Simple and Standard layout
  preferences only.
- Added state-holder startup, retry, pending write, failed write, and
  successful write transitions that preserve the last confirmed effective
  layout.
- Added focused unit coverage for codec behavior, event ordering, failed retry,
  and preservation of forecast/location/unit/effects boundaries.

Out of scope:

- Settings UI changes;
- installed Activity recreation or force-stop verification;
- Detailed or Meteorologist storage.

### Slice 27B2: Layout Settings Transaction UI

Status: committed at `b68ca19`

Prerequisites:

- Slice 27B1.
- Slice 25A.

Must prove:

- Standard remains default;
- Simple/Standard switching requires no provider refetch;
- Settings / Appearance exposes loading, saved, pending, failure, and retry
  states truthfully;
- failed writes retain the last confirmed layout and allow retry;
- layout controls keep selected semantics, logical traversal, and at least 48dp
  touch height;
- layout remains independent from effects/theme.

Implemented result:

- Wired production `MainActivity`, `OxygenAppStateHolder`, `OxygenApp`, and
  Settings / Appearance layout preference state.
- Settings / Appearance exposes loading, saved, pending, failure, and retry
  states for Simple/Standard layout selection.
- Targeted connected UI coverage passed for commit, read/write failure retry,
  no forecast refetch, preference independence, selected semantics, and compact
  48dp layout controls.

Out of scope:

- Activity recreation and installed force-stop/relaunch persistence evidence;
- Detailed or Meteorologist controls.

### Slice 27B3: Installed Layout Restoration Verification

Status: verified on 2026-09-08

Evidence:

- `.codex/test-artifacts/2026-09-08-slice-27b3-installed-layout-restoration/`;
- one pinned-emulator production journey covered Activity recreation and
  force-stop/relaunch for Simple and Standard at 360x640/font-scale 1.3 with
  Effects Off;
- focused `LayoutPreferenceDataStoreInstrumentedTest` connected check passed
  1/1; assemble and `git diff --check` passed.

Prerequisites:

- Slice 27B2.

Must prove:

- a saved Simple or Standard choice restores through the production
  `MainActivity -> OxygenAppStateHolder -> OxygenApp -> Home` path;
- Activity recreation and installed-app force-stop/relaunch preserve the saved
  layout;
- restored Simple does not first expose a ready Standard Home;
- both restored layouts remain usable at compact large-font Effects Off
  settings.

Out of scope:

- new layout types;
- theme, icon, high-contrast, or effects behavior changes.

---

## Slice 28: Theme Selection Baseline

Status: specified

Planning note: split translation quality by one theme at a time, then persist
only themes that pass the rendering boundary.

### Slice 28A1: Paper Theme Rendering Baseline

Status: committed

Committed in `06c987b` after cycle
`2026-09-08-slice-28a1-paper-theme-rendering-baseline`; the
installed rendering evidence is retained under
`.codex/test-artifacts/2026-09-08-slice-28a1-paper-theme-rendering-baseline/`.

Prerequisites:

- Slice 18G.
- Slice 18I.

Release intent: Make Paper a deliberate translation of semantic design roles
before it can become a persisted choice.

Must prove for Paper:

- semantic surfaces are mapped deliberately;
- operational/warning states remain readable;
- weather marks remain readable;
- typography is intentional;
- effects-off remains complete;
- weather semantics do not change.

Theme quality rule:

Existing scaffold values do not guarantee inclusion. Paper may be deferred
rather than shipped weakly.

### Slice 28A2: Terminal Theme Rendering Baseline

Status: committed at `80dd961`

Prerequisites:

- Slice 18G.
- Slice 18I.
- Slice 28A1 or an explicit decision to defer Paper.

Release intent: Make Terminal a deliberate translation of semantic design roles
before it can become a persisted choice.

Must prove for Terminal:

- semantic surfaces are mapped deliberately;
- operational/warning states remain readable;
- weather marks remain readable;
- typography is intentional;
- effects-off remains complete;
- weather semantics do not change.

Theme quality rule:

Terminal may be deferred rather than shipped weakly.

Completion evidence: focused six-case connected rendering and broad local
Gradle checks passed on 2026-09-09. Slice 28B1 is committed at `708172f` and
merged by `82cf281`; Slice 28B2 is committed at `2c88b9c` with retained
Settings selection/restoration evidence under
`.codex/test-artifacts/2026-09-09-slice-28b2-persisted-theme-settings-ui/`.

### Slice 28B1: Theme Preference Storage and State

Status: committed at `708172f` (merged by `82cf281`)

Prerequisites:

- at least one verified alternate theme from Slice 28A1 or 28A2.
- small-state persistence.

Must prove:

- only verified MVP theme choices are accepted;
- unknown/future theme records fall back conservatively;
- state-holder read/write failure behavior is observable and retryable;
- theme state remains independent from layout/effects.

### Slice 28B2: Persisted Theme Settings UI

Status: committed at `2c88b9c`

Prerequisites:

- Slice 28B1.
- Slice 25A.

Must prove:

- theme settings are reachable;
- choice persists across restart;
- provider refetch is not required;
- theme remains independent from layout/effects.

---

## Slice 29A: High-Contrast Rendering Contract

Status: committed at `0dccc94`

Prerequisites:

- Slice 18G.
- Slice 18I.

Must prove:

- high contrast is a semantic accessibility presentation, not merely brighter colors;
- required meaning never depends on color;
- compact + large font + effects off remains usable;
- operational and alert states remain distinct.

Out of scope:

- persisted setting or Settings UI.

Completion evidence: high-contrast role contract tests passed; the focused
four-case and final seven-case connected Home/alert rendering filters passed
on `oxygen_starter` / `emulator-5554` at 360x640 and font scale 1.3 with
Effects Off; broad local Gradle checks and `git diff --check` passed. Artifacts
are retained under
`.codex/test-artifacts/2026-09-09-slice-29a-high-contrast-rendering-contract/`.

## Slice 29B: High-Contrast Preference UI

Status: committed at `441d05d` plus test coverage follow-up `86e696c`

Prerequisites:

- Slice 29A.
- Slice 25A.
- small-state persistence.

Must prove:

- high contrast is reachable only if the rendering contract passed;
- preference persists across restart;
- provider refetch is not required;
- contrast remains independent from theme/layout/effects.

Completion evidence: the versioned Standard/High contrast DataStore codec and
state-holder transaction passed focused JVM tests; the two Compose Settings
cases, production DataStore/state-holder recreation case, and existing 29A
recomposition/no-refetch regression passed on `oxygen_starter` /
`emulator-5554`. The installed 1080x2400 journey at font scale 1.3 selected
Paper, Simple, Effects Off, and High through Settings / Appearance, then
restored them after Activity recreation and force-stop/relaunch. Broad local
compile, app/core unit tests, assemble, and `git diff --check` passed. Artifacts
are retained under
`.codex/test-artifacts/2026-09-09-slice-29b-high-contrast-preference-ui/`.

Out of scope:

- automatic system contrast detection;
- TalkBack service traversal, RTL, release readiness, or Gate 30 completion.

---

## Gate 30: Accessibility Presentation Verification

Status: specified; split into bounded Slices/Gates 30A1 through 30E

Release intent: complete the MVP accessibility presentation boundary without a
single cross-surface implementation/test cycle or a combinatorial UI matrix.

Gate-wide invariants:

- provider-neutral weather and alert meaning must not change;
- accessibility meaning must not depend on color, animation, decoration, or an
  unexplained gesture;
- accessibility descriptions must be created while semantic values are
  available, not by parsing rendered English strings or inspecting pixels;
- theme, contrast, layout, effects, units, and provider requests remain
  independent;
- screenshots are presentation evidence, not semantic or TalkBack proof;
- any skipped Android condition names the exact blocker;
- a discovered production defect is fixed and verified at one bounded surface
  before the owning sub-slice or final gate advances.

### Slice 30A1: Home Spoken-Weather Semantics

Status: committed at `da7b886`

Release intent: give current, hourly, and daily Home weather a deliberate,
provider-neutral spoken presentation contract.

Must prove:

- current speech identifies condition/current temperature and includes only
  available feels-like/high/low facts;
- hourly and daily rows identify time/date, condition, temperatures, and
  available precipitation probability;
- resolved units are spoken unambiguously and missing values are not converted
  to zero;
- one merged node exposes each weather item without duplicate decorative-
  mark or child-text announcements;
- visible weather text, named Home page actions, callbacks, canonical data, and
  provider request count remain unchanged;
- focused mapper, Compose semantics, and installed UI-hierarchy evidence exists.

Out of scope:

- layout matrix, alerts, Settings, theme redesign, and service TalkBack traversal.

### Slice 30A2: Home Compact and Large-Font Resilience

Status: specified

Prerequisite: Slice 30A1.

Release intent: verify and, where necessary, correct Home overflow on compact
phones and at large font settings without changing weather meaning.

Must prove:

- Now, Hourly, Daily, Details, and the Simple Forecast choice remain reachable
  at 360x640 dp and font scale 1.3, plus one representative font-scale-2.0
  overflow case;
- long location/provider names and wide Celsius/Fahrenheit values use localized
  scrolling or wrapping rather than clipping, overlap, hidden content, or
  excessively reduced text;
- important content does not clip or overlap, controls remain at least 48dp,
  and accessibility overflow scrolls rather than hiding information;
- page identity, spoken descriptions, visible values, callbacks, canonical
  weather, and provider request count remain unchanged.

Focused evidence: no more than six named connected cases using a pairwise set of
Standard/Simple pages, long content, and both temperature-unit widths; one
installed compact font-scale-1.3/2.0 journey; and an exact environment/command
ledger. A failing geometry boundary receives a red assertion before a Home-only
layout correction.

Out of scope:

- RTL, disabled-animation/reduced-motion policy, cross-theme/contrast checks,
  alerts, Settings, TalkBack, provider/state changes, and visual redesign.

### Gate 30A3: Home Speech/Layout Evidence and Document Sync

Status: specified

Prerequisites: Slices 30A1 and 30A2.

Mode: the required third-cycle test-only and documentation-sync session. Do not
change production behavior in this gate.

Must prove:

- the named 30A1 speech cases and selected 30A2 compact/large-font cases pass
  without running the full historical Home connected class;
- installed Home evidence covers Now/Hourly/Daily/Details, Simple Forecast,
  360x640 dp, font scales 1.3 and one representative 2.0 overflow case, Effects
  Off, long location/provider text, and converted units without overstating
  untested combinations;
- screenshots, UI hierarchies, semantics, command/result/rerun ledger, exact
  environment, and any blockers are reviewable under the two cycle artifact
  directories;
- README, specification, roadmap, current plan, and live cycle history state
  only the Home accessibility behavior actually implemented and exercised.

A failing production boundary blocks this gate and creates a specifically named
Home repair slice; it is not fixed inside the test/doc session. Provider,
privacy, licensing, persistence, alert, Settings, and release status remain
unchanged.

### Slice 30B1: Home RTL Navigation and Chronology

Status: specified

Prerequisite: Gate 30A3.

Release intent: make the existing Standard/Simple Home navigation and forecast
progression deliberately correct under RTL without changing chronological data.

Must prove:

- directional layout and visible previous/next affordances mirror appropriately;
- semantic page identity and named previous/next actions continue to mean
  chronological backward/forward movement rather than raw left/right movement;
- hourly/daily data stays earliest-to-latest and spoken time/date meaning is
  unchanged in both Standard and Simple layouts;
- 360x640 dp, font scale 1.3, long location text, and both layouts retain usable
  page controls, at least 48dp targets, no overlap, and overflow reachability;
- switching layout/page in RTL does not persist unrelated state or refetch.

Focused evidence: no more than five named connected RTL cases and one installed
RTL Home journey with screenshots/hierarchies and restored device direction.
Production corrections, if necessary, remain in Home layout/navigation code and
receive a failing behavior assertion first.

Out of scope:

- font-scale-2.0 work already owned by 30A2, reduced motion, theme/contrast
  matrix, alerts, Settings, provider/state changes, and TalkBack traversal.

### Slice 30B2: Home Reduced-Motion and Appearance Invariance

Status: specified

Prerequisites: Gate 30A3 and Slice 30B1.

Release intent: verify Home accessibility meaning and navigation across the
implemented effects, animation-policy, theme, and contrast axes.

Must prove:

- Effects Off and Android disabled-animation policy retain all Home meaning and
  use non-animated page movement without overwriting the saved effects choice;
- spoken descriptions, named page actions, visible weather, alert summary, and
  source/provenance remain equivalent under Oxygen, Paper, Terminal and
  Standard/High contrast through a documented pairwise matrix;
- selection remains understandable without color and atmospheric decoration;
- 360x640 dp/font scale 1.3 has no new clipping, overlap, or missing controls;
- recomposition across presentation axes does not refetch, rewrite preferences,
  change canonical weather, or change semantic page identity.

Focused evidence: reuse the committed effects/theme/contrast fixtures, add no
more than six named pairwise connected cases through the real Home path, and
retain one installed disabled-animation/Effects-Off journey. Do not rerun whole
historical theme classes or add a second appearance model.

Out of scope:

- Settings control semantics/layout, new preferences, automatic system contrast,
  Full effects, icon packs, visual redesign, alerts detail, and TalkBack.

### Gate 30B3: Home Environment Evidence and Document Sync

Status: specified

Prerequisites: Slices 30B1 and 30B2.

Mode: the required third-cycle test-only and documentation-sync session. Do not
change production behavior in this gate.

Must prove:

- the selected RTL and reduced-motion/appearance cases pass together within the
  connected-test budget without rerunning full historical Home classes;
- installed RTL and disabled-animation journeys retain Home chronology, spoken
  meaning, page actions, 48dp controls, Effects-Off behavior, visible weather,
  and source/alert reachability;
- retained screenshots, UI hierarchies, semantics, command/result/rerun ledger,
  exact environment, and blockers support each claim;
- README, specification, roadmap, current plan, and live history distinguish
  deterministic/installed Home evidence from service-level TalkBack evidence
  still owned by Gate 30E.

A failing production boundary blocks this gate and creates a specifically named
Home repair slice. Do not change provider, alert transport, preference schema,
privacy/license/disclosure, or release status.

### Slice 30C1: Official-Alert Summary Accessibility

Status: specified

Prerequisite: Gate 30B3.

Release intent: make the Home official-alert summary complete, non-color-only,
and operable without changing official alert meaning or selection.

Must prove:

- the summary exposes event, explicit severity, issuer, expiry, source-check
  time, attribution, and active-alert count in logical non-duplicated semantics;
- detail and official-source actions have meaningful labels/roles, 48dp targets,
  and do not trigger Home page movement;
- no-alert, one-alert, and multiple-alert fixtures remain truthful;
- long event/issuer content remains reachable at 360x640 dp, font scales 1.3
  and one 2.0 case, LTR/RTL, Effects Off, and High contrast;
- opening the selected alert does not refetch or mutate weather/alert data.

Focused evidence: mapper tests only if a new presentation field is necessary,
no more than five named connected summary cases, and one installed real-alert
summary/detail-entry attempt. Live alert availability may be absent and must not
be replaced by seeded installed success.

Out of scope: detail-document reading layout, alert transport/cache/background
work, notifications, additional national providers, and TalkBack traversal.

### Slice 30C2: Official-Alert Detail Accessibility

Status: specified

Prerequisite: Slice 30C1.

Release intent: make the existing in-app official-alert detail a complete,
logically ordered, scrollable reading surface.

Must prove:

- event, explicit severity, issuer, effective/expiry, affected area, official
  description/instructions, source-check time, attribution, and source link stay
  complete without paraphrasing official text;
- multiple-alert selection, Back, and external source controls have meaningful
  roles/labels/selected state and at least 48dp targets;
- long provider/event/area/description/instruction content remains reachable at
  360x640 dp, font scales 1.3 and 2.0, LTR/RTL, Effects Off, and High contrast;
- reading/selecting/returning preserves Home page, alert selection, forecast,
  request count, and official fixture meaning.

Focused evidence: no more than six named deterministic connected cases using
official fixtures plus one installed navigation/return journey only when a real
alert is available. Do not make live NWS calls part of deterministic tests.

Out of scope: summary behavior already owned by 30C1, alert persistence,
background polling, notifications, provider transport, and release readiness.

### Gate 30C3: Alert Accessibility Evidence and Document Sync

Status: specified

Prerequisites: Slices 30C1 and 30C2.

Mode: required third-cycle test-only and documentation-sync session; no
production behavior changes.

Must prove the selected summary/detail cases together within budget; reconcile
long-content, LTR/RTL, large-font, Effects-Off/high-contrast, action, navigation,
and no-refetch evidence; record honest installed real-alert availability; and
sync README, specification, roadmap, current plan, and live history without
claiming TalkBack or notification/background-alert completion. A production
failure creates a separately named alert repair slice.

### Slice 30D1: Appearance Control Semantics

Status: specified

Prerequisite: Gate 30C3.

Release intent: give Theme, Layout, Effects, and Contrast controls deliberate
group/choice semantics through the real Settings / Appearance path.

Must prove:

- each group and choice exposes meaningful label, role, selected/disabled,
  pending, success, and read/write-failure state without color-only meaning;
- retry and Back actions are named, operable, and at least 48dp;
- confirmed-write selection, retained failed target, retry, preference
  independence, and no-refetch behavior remain unchanged;
- the implementation reuses existing preference state and control components,
  not a second state model or test-only screen.

Focused evidence: existing preference transaction JVM tests plus no more than
five named connected semantic/action cases through `OxygenApp`.

Out of scope: large-font/RTL layout matrix, new preferences, automatic system
contrast, Full effects, icon packs, persistence formats, Home/alert behavior.

### Slice 30D2: Appearance Layout and Environment Resilience

Status: specified

Prerequisite: Slice 30D1.

Release intent: keep the installed Appearance controls readable and reachable
under compact, large-font, RTL, reduced-motion, theme, and contrast conditions.

Must prove:

- every group, choice, retry, status message, and Back action is scroll-reachable
  at 360x640 dp and font scales 1.3/2.0 with no clipping or overlap;
- RTL preserves logical label/control order;
- selected/pending/error meaning remains understandable across Oxygen, Paper,
  Terminal and Standard/High contrast using a pairwise matrix;
- Effects Off/disabled animation does not hide state or rewrite the saved
  preference; selection/restoration and no-refetch remain unchanged.

Focused evidence: no more than six named pairwise connected cases plus installed
LTR/RTL and representative font-scale-2.0 hierarchies/screenshots. Production
repairs remain presentation-only and begin from a failing boundary assertion.

Out of scope: semantics already owned by 30D1, new preference/state/storage
behavior, Home/alert changes, visual redesign, and TalkBack traversal.

### Gate 30D3: Appearance Accessibility Evidence and Document Sync

Status: specified

Prerequisites: Slices 30D1 and 30D2.

Mode: required third-cycle test-only and documentation-sync session; no
production behavior changes.

Must prove the selected semantic/transaction/layout cases together within
budget; reconcile installed LTR/RTL/large-font/reduced-motion evidence and exact
pairwise coverage; and sync README, specification, roadmap, current plan, and
live history without claiming automatic contrast or TalkBack. A production
failure creates a separately named Appearance repair slice.

### Gate 30E: Installed TalkBack and Accessibility Closure

Status: specified

Prerequisites: Gates 30A3, 30B3, 30C3, and 30D3.

Release intent: exercise the completed production surfaces with Android
accessibility services and close Gate 30 using retained cross-slice evidence.

Must prove where the emulator supports it:

- TalkBack traverses Home Now/Hourly/Daily, an available official-alert
  summary/detail, and Settings / Appearance in logical order;
- named Home movement, alert selection/Back/source, Appearance selection/retry,
  and Back actions are operable without hidden gesture or color-only discovery;
- spoken weather/hazard output matches visible meaning without duplicate marks
  or omitted safety text;
- the retained compact/large-font, RTL, theme/contrast, long-content,
  unit-conversion, touch-target, reduced-motion, and Effects-Off matrix is
  complete without claiming untested cross-products.

Attempt one bounded TalkBack setup/traversal on the pinned emulator. If the
service is absent or cannot be enabled safely, retain exact package/service/
settings evidence and mark only service traversal blocked; never convert it to
mock success. A production defect creates a bounded repair slice ahead of 30E.

Gate 30E is test/evidence/documentation work, not a refactor. On success,
reconcile README, specification sections 25/31/33/34/37/46/53, this roadmap,
the active plan, and cycle history with only exercised conditions. Provider,
privacy, license, persistence, or release-readiness claims do not change.

---

## Slice 31: Installed-App Forecast Fallback Completion

Status: specified

Planning note: split wiring from cache/provenance.

### Slice 31A: Installed-App Fallback Wiring

Status: committed at `4cdecdd`

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

Status: committed at `4028044`

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

Status: committed

Prerequisite:

- Slice 31B.

Must prove at installed Android boundary:

- Open-Meteo default success;
- controlled fallback-eligible Open-Meteo failure;
- MET Norway fallback success;
- correct source/update/provenance;
- offline restoration of fallback-served data;
- later successful Open-Meteo refresh;
- fallback/cache replacement does not create or mutate official alert state.

---

## Slice 33A: Dependency and Manifest Privacy Audit

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
- cleartext/network-security configuration where relevant.

Out of scope:

- provider disclosure text and Settings navigation checks.

## Slice 33B: Provider Disclosure and Local Data Privacy Audit

Status: specified

Prerequisite:

- Slice 33A.

Must prove review of:

- active forecast/geocoding/alert providers;
- attribution/privacy/license reachability.

No provider is active/current in disclosures unless its production path can fetch or serve data.

---

## Gate 34A: Settings and About Release Check

Status: specified

Must prove:

- Settings IA matches implemented preferences;
- Open Source Licenses and Privacy remain reachable;
- source-code license and weather-data licenses remain distinct;
- no placeholder appearance option is exposed as implemented.

## Gate 34B: Data-Source Release Check

Status: specified

Prerequisite:

- Gate 34A.
- Slice 33B.

Must prove:

- Data Sources lists only active providers as active;
- forecast/geocoding/alert claims match repository docs;
- attribution links and provider privacy claims are reachable;
- release-facing provider claims match installed behavior.

---

## Gate 35A: MVP Core Behavior Verification

Status: specified

Release intent: Verify core weather and local-state MVP behavior against the
repository completion standard before presentation/release evidence is bundled.

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
- source/provenance.

## Gate 35B: MVP Presentation and Accessibility Verification

Status: specified

Prerequisite:

- Gate 35A.

Must prove:

- implemented presentation settings;
- effects Off;
- high contrast if included;
- disclosure/privacy;
- compact, large-font, reduced-motion, and RTL behavior where applicable.

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

## Gate 35C: Release Candidate Decision

Status: specified

Prerequisite:

- Gate 35A.
- Gate 35B.
- Gate 34B.

Release intent: Make the release-candidate status decision only after the broad
verification evidence exists.

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

The checked-out branch is reconciled with its local `origin/main` ref through
merge `82cf281`. The latest completed local implementation slice is Slice 29B,
implemented at `441d05d` with test follow-up `86e696c` and evidence sync
`86f046b`; retained evidence is under
`.codex/test-artifacts/2026-09-09-slice-29b-high-contrast-preference-ui/`.

Use this as sequencing guidance, not permission to work multiple slices at once.

1. Slice 19A — Saved Location Storage Model
2. Slice 19B — Saved Location Selection and Concurrency
3. Slice 19C — Saved Locations UI
4. Slice 31A — Installed-App Fallback Wiring
5. Slice 31B — Fallback Cache and Provenance
6. Slice 32 — Fallback Real-Path Verification
7. Slice 19D — Save Search Result UI
8. Slice 19E — Remove Saved Location UI
9. Gate 19F — Saved Locations Documentation Sync
10. Slice 20A — Unit Preference Contract
11. Gate 20-0 — Presentation Semantics and Localization Safety
12. Slice 20B — Unit Conversion Presentation Boundary
13. Slice 25A — Settings Information Architecture
14. Slice 20C — Persisted Units UI
15. Slice 21 — Optional Device Location
16. Slice 22 — NWS Alert Provider Contract
17. Slice 23A — NWS Fixtures/Parsing/Mapping
18. Slice 23B — NWS Client/Error Classification
19. Slice 23C — Alert Repository Merge
20. Slice 24A — Alert Summary/Banner UI
21. Slice 24B — Alert Detail UI
22. Gate 25 — Disclosure Baseline Check
23. Slice 26 — Effects Preference
24. Slice 27B1 — Layout Preference Storage and State
25. Slice 27B2 — Layout Settings Transaction UI
26. Slice 27B3 — Installed Layout Restoration Verification
27. Slice 28A1 — Paper Theme Rendering Baseline
28. Slice 28A2 — Terminal Theme Rendering Baseline, or explicitly defer it
29. Slice 28B1 — Theme Preference Storage and State
30. Slice 28B2 — Persisted Theme Settings UI
31. Slice 29A — High-Contrast Rendering Contract
32. Slice 29B — High-Contrast Preference UI
33. Slice 30A1 — Home Spoken-Weather Semantics
34. Slice 30A2 — Home Compact and Large-Font Resilience
35. Gate 30A3 — Home Speech/Layout Evidence and Document Sync
36. Slice 30B1 — Home RTL Navigation and Chronology
37. Slice 30B2 — Home Reduced-Motion and Appearance Invariance
38. Gate 30B3 — Home Environment Evidence and Document Sync
39. Slice 30C1 — Official-Alert Summary Accessibility
40. Slice 30C2 — Official-Alert Detail Accessibility
41. Gate 30C3 — Alert Accessibility Evidence and Document Sync
42. Slice 30D1 — Appearance Control Semantics
43. Slice 30D2 — Appearance Layout and Environment Resilience
44. Gate 30D3 — Appearance Accessibility Evidence and Document Sync
45. Gate 30E — Installed TalkBack and Accessibility Closure
46. Slice 33A — Dependency and Manifest Privacy Audit
47. Slice 33B — Provider Disclosure and Local Data Privacy Audit
48. Gate 34A — Settings and About Release Check
49. Gate 34B — Data-Source Release Check
50. Gate 35A — MVP Core Behavior Verification
51. Gate 35B — MVP Presentation and Accessibility Verification
52. Gate 35C — Release Candidate Decision

Run recurring documentation-sync gates at the defined cadence.

Sequencing rationale:

- Slice 18J-R restores the real installed Open-Meteo ready forecast path needed to verify the in-progress Slice 18J UI against production data.
- Slice 18J completes the originally intended Standard Oxygen visual convergence before another major user-facing surface is added.
- Saved-location persistence/switching follows immediately after 18J.
- Installed-app MET Norway fallback is pulled forward after the saved-location list/select UI because fallback is an MVP acceptance requirement and repository-only fallback evidence is insufficient for release.
- Save-result and remove-location UI return after fallback real-path verification so Saved Locations can complete before Units.
- Unit conversion follows once location switching and fallback provenance are stable.
- Settings information architecture is established before multiple preference families make the current Settings/About surface too broad; Slice 25A is now committed.
- Appearance persistence remains after the Standard Home design system and accessibility baseline, which are already committed.

---

## Active Slice

Slice 30A1: Home Spoken-Weather Semantics is committed at `da7b886`; Slice 30A2
is the next specified candidate in `.codex/plans/mvp-roadmap.md`. It was the
first implementation slice of the split Gate 30 accessibility boundary. Slice
29B is committed at `441d05d` with test
coverage follow-up `86e696c` and evidence sync `86f046b`. Slice 28A1 is
committed at `06c987b`, Slice 28A2 is committed at `80dd961`, and Slice 28B1 is
committed at `708172f` (merged by `82cf281`), with retained evidence under their
cycle artifact directories.
Slice 27B1/27B2 are committed together at `b68ca19`, and Slice 27B3 is
verified with retained installed evidence at `.codex/test-artifacts/2026-09-08-slice-27b3-installed-layout-restoration/`.

Immediate planning boundary:

```text
18I committed at 02f701
-> 18J-R committed at 15fc10e
-> resumed 18J Standard Home visual convergence evidence committed at 7950a42
-> 19A saved-location storage model committed at d97e2ea
-> 19B selection/concurrency committed at 0f649aa
-> 19C saved-location list/select UI committed at e2efdd3
-> 31A installed-app fallback wiring committed at 4cdecdd
-> 31B fallback cache and provenance committed at 4028044
-> 32 fallback real-path verification committed at 9b9d706
-> 19D save search result UI committed at 8599640
-> 19E remove saved location UI committed at 00cb88a
-> 19F saved locations documentation sync committed at 8386484
-> 20A unit preference contract committed at 3f6d741
-> 20-0 presentation semantics and localization safety committed at 587b0ad
-> 20B unit conversion presentation boundary committed at 1a2b5a0
-> 20C persisted units UI committed at 1b52718
-> 21 optional device location verified and committed at 3ea5ae6
-> 22 NWS alert provider contract committed at 858c0a4
-> 23A NWS alert fixtures/parsing/mapping committed at 17dab0c
-> 23B NWS transport/provider boundary committed at dcf707b
-> 23C alert repository merge committed at 3c658a8
-> 24A alert summary/banner UI committed at cf9ddaf
-> 24B alert detail UI committed at ceb6253
-> Slice 25A Settings information architecture committed at `2484e90`
-> Gate 25 disclosure baseline check committed at `23a9d49`
-> Slice 26 persisted effects preference committed at `c7b578a`
-> Slice 27A Simple Layout Definition committed at `660e376`
-> Slice 27B1/27B2 persisted layout storage and Settings UI committed at `b68ca19`
-> Slice 27B3 installed layout restoration verification verified on 2026-09-08
-> Slice 28A1 Paper theme rendering baseline committed at `06c987b`
-> Slice 28A2 Terminal theme rendering baseline committed at `80dd961`
-> Slice 28B1 theme preference storage/state committed at `708172f`, merged by `82cf281`
-> Slice 28B2 persisted theme Settings UI committed at `2c88b9c`
-> Slice 29A high-contrast rendering contract committed at `0dccc94`
-> Slice 29B persisted high-contrast preference UI committed at `441d05d`
-> Slice 30A1 Home spoken-weather semantics committed at `da7b886`
```

Gate 25, Slice 27A, committed 27B1/27B2, Slice 28B1, Slice 28B2, Slice 29A,
Slice 29B, and Slice 30A1 are complete. Gate 30 is split into the bounded
30A1–30E queue at the head of this roadmap; Slice 30A2 is next and remains
specified. Release
work and release-candidate claims remain outside this boundary.

Do not reopen 18F, insert new 18F.x slices, or create a new pre-18G visual gate.
Those implementation boundaries are historical and already committed. Slice
18J-R was a provider-path recovery slice required by the blocked Slice 18J
evidence boundary, not a new visual gate.

Do not treat later roadmap entries as active implementation work until a new
bounded plan selects one.

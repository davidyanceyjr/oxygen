# Oxygen MVP Release Map

Status: specified
Roadmap ID: mvp-2026-08
Source authority: `docs/OXYGEN_FULL_SPECIFICATION.md`
Created: 2026-08-18
Revised: 2026-09-02

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
docs/assets/oxygen-weather-visual-language-base-art-sheet-v0.1.png
```

Use Base Art Sheet v0.1 as visual-direction authority for Standard Home visual work. It guides weather marks, atmospheric scene language, glass-like surfaces, strong numerals, palette references, forecast/metric/alert composition, and Oxygen/Paper/Terminal translation.

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

Standard Home must be visually approved before its design decisions are consolidated into a design system, and every later user-facing feature must carry its own finished UI obligations.

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

Status: specified

Prerequisites:

- Slices 18A through 18E committed.
- Existing offline restoration path remains intact.

Release intent: Prove that the paged Standard Home architecture works correctly across operational states, not only fresh success.

Must prove:

- loading remains understandable;
- refresh-in-progress does not destabilize page state;
- fresh success works across semantic pages;
- cached/stale success remains usable;
- refresh failure while cache remains useful remains usable;
- retryable no-cache error remains actionable;
- refresh/retry controls remain accessible;
- source/update/stale communication remains semantically appropriate;
- operational state changes do not strand the user on meaningless pages;
- page state does not corrupt selected location or forecast state;
- no sample/fabricated production success appears;
- focused Compose/state tests cover applicable transitions;
- installed-app screenshot evidence covers representative non-happy paths.

Functional invariants:

Do not redesign:

- providers;
- fallback policy;
- repository selection;
- Room/DataStore schemas;
- weather semantics;
- selected-location identity.

Out of scope:

- visual-language redesign;
- theme engine;
- design-system consolidation;
- saved locations;
- units;
- new provider capabilities.

---

## Gate 18F-V: Standard Home Visual Convergence Review

Status: specified

Prerequisite:

- Slice 18F committed.

Release intent: Decide whether the default Standard Oxygen Home is visually strong enough to become the basis of the design system.

Why this gate exists:

The repository already contains an art direction, theme palettes, a glass primitive, procedural weather marks, and an atmospheric scene. Those primitives must not be consolidated while production Home still reads as framework-default/scaffold UI.

Must prove:

A screenshot review of Now, Hourly, Daily, Details, and at least one operational state explicitly evaluates:

- Home background/scene composition;
- condition/weather marks;
- page navigation chrome;
- primary typography hierarchy;
- control prominence;
- Hourly information density;
- Daily comparison efficiency;
- Details metric identity/grouping;
- surface/card vocabulary;
- source/stale/error prominence;
- effects-off completeness.

The review records which visual decisions are:

- approved;
- provisional;
- rejected;
- blocked by presentation-data limitations.

Gate result:

- If the default Standard Home is already strong enough, proceed to Gate 18G-0.
- If not, select only the bounded 18F.x slices actually justified by evidence.

Do not use Slice 18G as a hidden visual redesign phase.

---

## Slice 18F.1: Oxygen Home Composition Convergence

Status: specified

Prerequisites:

- Slice 18F committed.
- Gate 18F-V identifies concrete composition problems.

Release intent: Make Standard Home look deliberately like Oxygen rather than a themed Material scaffold without changing weather behavior.

Must prove:

- weather content dominates application chrome;
- location/settings/refresh controls remain accessible but visually subordinate;
- atmospheric visual language is used intentionally;
- surfaces have semantic distinction rather than every region becoming the same card;
- operational status remains distinct from decorative weather surfaces;
- effects-off presentation remains complete;
- compact phone and large-font presentation remain usable;
- screenshot comparison against the Base Art Sheet shows meaningful convergence.

Preferred direction where supported:

- make `WeatherScene` or an equivalent scene role a real Home foundation;
- use glass/translucent surfaces selectively rather than universally;
- reduce stock Material appearance in page navigation while preserving semantics/accessibility;
- preserve weather meaning independently from decoration.

Out of scope:

- persisted themes;
- Paper/Terminal completion;
- saved locations;
- units;
- provider additions;
- speculative animation expansion.

---

## Slice 18F.2: Weather Mark Semantic Vocabulary

Status: specified

Prerequisites:

- Slice 18F committed.
- Gate 18F-V identifies weather-mark differentiation as insufficient.

Release intent: Create a recognizably Oxygen weather-mark family that distinguishes provider-neutral weather conditions.

Must prove visually distinct treatment for meaningful condition families including:

- clear / mostly clear;
- partly cloudy / cloudy;
- fog;
- drizzle;
- rain / rain showers;
- freezing drizzle / freezing rain;
- snow / snow showers;
- sleet;
- hail;
- thunderstorm / thunderstorm with hail;
- unknown.

Preferred primitive vocabulary:

- sun/glow;
- cloud masses;
- fog bands;
- drizzle points;
- rain strokes;
- snow marks;
- ice/sleet marks;
- hail;
- lightning.

Accessibility rule:

Condition meaning remains available through text/semantics. Iconography is reinforcement, not the sole carrier of meaning.

Boundary:

Do not invent day/night semantics unless provider-neutral state can determine them correctly.

---

## Slice 18F.3: Home Presentation Semantics Boundary

Status: specified

Prerequisites:

- Slice 18F committed.
- May be selected directly when later visualization/localization/theme work would otherwise depend on strings.

Release intent: Remove brittle UI behavior based on formatted English strings and expose semantic/numeric presentation data intentionally.

Must prove:

- metric presentation carries semantic identity;
- Details grouping does not depend on exact English labels;
- visualization-required values are available numerically alongside display text;
- display formatting remains presentation-layer behavior;
- provider DTOs remain isolated;
- missing values remain missing.

Recommended semantic metric identity:

```kotlin
enum class HomeMetricKind {
    APPARENT_TEMPERATURE,
    HUMIDITY,
    DEW_POINT,
    WIND,
    PRESSURE,
    VISIBILITY,
    CLOUD_COVER,
    PRECIPITATION
}
```

Must not:

- parse `"73°F"`, `"40%"`, or similar display strings in Composables;
- group by labels such as `"Humidity"` or `"Wind"`;
- add fake values for visual completeness.

Localization boundary:

Move reusable Home UI text toward Android string resources as touched. This is not a full translation slice.

---

## Slice 18F.4: Daily Comparative Visualization

Status: specified when selected by Gate 18F-V

Prerequisites:

- semantic numeric high/low presentation data exists.

Release intent: Use existing daily numeric data to make multi-day comparison faster.

Must prove:

- several days remain visible at ordinary phone size;
- high/low relationship is immediately comparable;
- precipitation remains visible where available;
- missing values do not receive fake positions;
- textual/accessibility high-low meaning remains available;
- Composables do not parse display strings.

A normalized temperature-range track is permitted but not required.

---

## Slice 18F.5: Hourly Forecast Visualization

Status: specified when selected by Gate 18F-V

Prerequisites:

- semantic numeric hourly values required by the chosen visualization are present.

Release intent: Make Hourly answer "what happens next?" more efficiently than a grid of independent cards.

Must prove:

- time, condition, temperature, and precipitation remain readable;
- useful near-term temporal context increases;
- the page does not become a long document;
- numeric visualization uses semantic presentation fields;
- missing precipitation remains honestly unavailable;
- accessibility exposes equivalent textual meaning;
- effects-off remains complete.

Boundary:

Do not add provider fields solely to make a chart richer.

---

## Slice 18F.6: Home Navigation Visual Language

Status: specified when selected by Gate 18F-V

Release intent: Retain semantic Now/Hourly/Daily/Details navigation while giving pager chrome an Oxygen identity.

Must prove:

- current page remains obvious;
- direct page selection remains available;
- horizontal paging remains available;
- TalkBack retains page identity and position;
- visible implementation-like `"Page n of 4"` text is not required if equivalent visible and accessibility state exists;
- child controls do not accidentally page;
- semantic page model is unchanged.

---

## Gate 18G-0: Standard Oxygen Visual Baseline Approval

Status: specified

Prerequisites:

- Slice 18F committed.
- Gate 18F-V completed.
- Any selected 18F.x convergence slices committed.

Release intent: Approve the default Standard Oxygen Home before design-system extraction.

Must prove final reviewable screenshots exist for:

- Now;
- Hourly;
- Daily;
- Details;
- representative stale/error state.

The gate explicitly approves:

- typography hierarchy;
- spacing rhythm;
- surface vocabulary;
- weather marks;
- atmospheric treatment;
- navigation;
- information density;
- operational-state prominence;
- effects-off completeness.

If any major item remains knowingly provisional, create another bounded visual slice instead of hiding it inside 18G.

---

## Slice 18G: Oxygen Home Design-System Consolidation

Status: specified

Prerequisites:

- Gate 18G-0 passed.

Release intent: Extract proven visual decisions into reusable semantic Oxygen design-system roles.

Must prove:

Centralized roles exist where repeated patterns justify them, including as appropriate:

- spacing;
- typography;
- shapes;
- surfaces;
- atmospheric scene;
- primary weather number;
- secondary weather number;
- forecast mark;
- forecast track;
- operational status;
- warning status;
- quiet metadata;
- page navigation.

Design-system rule:

Do not create one generic component that erases semantic distinctions among Now, Hourly, Daily, and Details.

Theme rule:

Roles must remain theme-independent. The design system must not assume every future theme uses glass, gradients, atmospheric effects, the same corner radius, or the same font family.

Out of scope:

- persisted appearance selection;
- Simple/Detailed/Meteorologist layouts;
- full theme editor;
- community theme format.

---

## Slice 18H: Standard Home Accessibility and Visual Verification Gate

Status: specified

Prerequisites:

- Slice 18G committed.

Release intent: Freeze Standard Home as the verified reference architecture for later features and appearance variants.

Must prove:

- semantic page navigation;
- identifiable current page;
- deliberate next/previous behavior;
- adequate touch targets;
- logical TalkBack order;
- compact phone support;
- large-font support;
- no important clipping/overlap;
- safe accessibility overflow;
- long location names;
- effects-disabled completeness;
- source/stale/error communication;
- coherent typography/spacing across pages;
- final installed screenshots for all four pages;
- representative operational-state screenshot;
- focused UI checks;
- broad Android verification.

It must not implement:

- Simple layout;
- Detailed layout;
- Meteorologist layout;
- persisted theme/layout/effects choices;
- foldable-specific UI.

---

## Slice 19: Saved Locations

Status: specified

Planning note: the original Slice 19 was too broad for one active cycle. Use one bounded sub-slice.

### Slice 19A: Saved Location Storage Model

Status: specified

Prerequisites:

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

Status: specified

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

### Slice 19C: Saved Locations UI

Status: specified

Prerequisites:

- Slice 19B.
- Slice 18H.

Release intent: Expose saved-location management through a finished Oxygen UI.

Must prove:

- similar place names are disambiguated;
- current selection is obvious;
- select/remove controls are visible;
- destructive removal is not easy to trigger accidentally;
- manual search remains fully available without permission;
- compact and large-font layouts work.

Out of scope:

- drag reorder;
- folders;
- automatic multi-location refresh.

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

### Slice 20B: Unit Conversion Presentation Boundary

Status: specified

Prerequisites:

- Slice 20A.
- Slice 18F.3 recommended.

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
- Slice 18H.
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
- Slice 18H.

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

- Slice 18H.

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

Slice 18H establishes the canonical Standard Home reference. Later effects, layout, theme, and contrast variants must translate that architecture without changing weather semantics, source/stale/error behavior, alerts, or accessibility guarantees.

Appearance work is not a generic polish backlog.

---

## Slice 26: Effects Off Preference Baseline

Status: specified

Prerequisites:

- Slice 18H.
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
- Slice 18H.

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
- Slice 18H.
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

Status: specified

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

## Gate A: Presentation String and Localization Safety

Status: specified

Recommended timing:

- before units/themes/layout variants proliferate;
- may be satisfied by Slice 18F.3 plus resource cleanup evidence.

Must prove:

- semantic grouping never depends on localized labels;
- reusable Home strings move toward Android resources as touched;
- accessibility descriptions are not built by parsing English display text;
- formatting remains presentation-layer behavior.

This does not require shipping translations.

---

## Gate B: Async Location and Forecast Race Safety

Status: specified

Recommended timing:

- as part of Slice 19B or earlier if architecture changes.

Must prove:

- obsolete requests cannot replace current-location state;
- refresh results are scoped to location identity;
- cancellation/lifecycle behavior is explicit;
- rapid location switching is deterministic.

---

## Gate C: Standard Home Visual Regression Reference Set

Status: specified

Recommended timing:

- immediately after Slice 18H.

Maintain a small authoritative installed-app screenshot set for:

- Standard Oxygen Now;
- Hourly;
- Daily;
- Details;
- stale/operational state;
- large-font state.

This is review evidence, not pixel-perfect screenshot testing.

---

## Gate D: Feature-Surface Design Rule

Status: specified

Applies:

- to all post-18H user-facing feature slices.

A feature is not complete merely because its backend path exists.

Its user-facing slice must include:

- final Oxygen composition;
- semantic state;
- accessibility;
- compact/large-font behavior;
- operational/error behavior;
- installed screenshot evidence where applicable.

This prevents Saved Locations, Units, Alerts, and Settings from recreating scaffold-looking UI outside Home.

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

Use this as sequencing guidance, not permission to work multiple slices at once.

1. Slice 18F — Home Operational State Integration
2. Gate 18F-V — Standard Home Visual Convergence Review
3. Select only the 18F.x visual slices the review proves necessary
4. Gate 18G-0 — Standard Oxygen Visual Baseline Approval
5. Slice 18G — Oxygen Home Design-System Consolidation
6. Slice 18H — Standard Home Accessibility and Visual Verification Gate
7. Gate C — establish the Standard Home visual reference set
8. Slice 19A — Saved Location Storage Model
9. Slice 19B — Saved Location Selection and Concurrency
10. Slice 19C — Saved Locations UI
11. Slice 31A — Installed-App Fallback Wiring
12. Slice 31B — Fallback Cache and Provenance
13. Slice 32 — Fallback Real-Path Verification
14. Slice 20A — Unit Preference Contract
15. Slice 20B — Unit Conversion Presentation Boundary
16. Slice 25A — Settings Information Architecture
17. Slice 20C — Persisted Units UI
18. Slice 21 — Optional Device Location
19. Slice 22 — NWS Alert Provider Contract
20. Slice 23A — NWS Fixtures/Parsing/Mapping
21. Slice 23B — NWS Client/Error Classification
22. Slice 23C — Alert Repository Merge
23. Slice 24A — Alert Summary/Banner UI
24. Slice 24B — Alert Detail UI
25. Gate 25 — Disclosure Baseline Check
26. Slice 26 — Effects Preference
27. Slice 27A / 27B — Simple Layout Definition and Selection
28. Slice 28A / 28B — Theme Translation and Selection
29. Slice 29 — High Contrast
30. Gate 30 — Accessibility Presentation Verification
31. Slice 33 — Privacy and Dependency Audit
32. Gate 34 — About/Settings/Data-Source Release Check
33. Gate 35 — MVP Release Candidate Verification

Run recurring documentation-sync gates at the defined cadence.

---

## Next Candidate Slice

Candidate: Slice 18F: Home Operational State Integration.

Recommended immediate sequence:

```text
18F
-> 18F-V visual convergence review
-> only necessary 18F.x visual convergence slices
-> 18G-0 visual approval
-> 18G design-system consolidation
-> 18H verification
```

Do not begin the persisted theme engine before the Standard Oxygen default passes the visual approval gate.

To start work, update `.codex/plans/current.md` to one single bounded gate or slice. Do not treat later roadmap entries as planned active work.

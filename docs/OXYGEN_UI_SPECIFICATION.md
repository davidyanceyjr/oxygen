# Oxygen UI Specification — Version 0.3

**Status:** specified; not implemented or visually verified

**Purpose:** define Oxygen's visual language and presentation rules before
selecting an implementation roadmap.

**Scope:** this document defines target presentation and interaction behavior
for later, separately planned slices. It does not claim that those behaviors
exist, and it does not permit a visual-only slice to change provider, cache,
location, unit, accessibility, or navigation semantics unless that named slice
explicitly owns the behavioral change.

**Primary platform:** Android with Kotlin and Jetpack Compose.

## 0. Review and audit resolutions

This section resolves the implementation-blocking ambiguities found in Draft
0.1 and reviewed in Draft 0.2. The detailed sections below incorporate these
decisions. They are specified design intent, not evidence of implementation or
verification.

### 0.1 Authority prerequisite and forecast horizons

The intended Standard Home contract is a prominent Now experience, a rolling
**72-hour hourly forecast**, and a **ten-day daily forecast**. The corresponding
MVP and 1.0-success horizons are aligned in
`docs/OXYGEN_FULL_SPECIFICATION.md`; this document does not silently override
that higher authority.

The first implementation slice must make the active Open-Meteo request ask for
72 hours and ten days. Provider adapters, repository/cache paths, and domain
mapping preserve every valid returned entry in chronological order. Home
presentation selects actual entries in the rolling 72-hour interval and the
first ten distinct local forecast dates; it does not retain the current
12-hour presentation cap.

MET Norway has no client-selected horizon and can return less-dense timesteps
beyond its short-range hourly period. The fallback path therefore uses only
actual returned timestamps inside the same 72-hour interval and actual daily
dates derived from them. Neither provider path pads, interpolates, repeats, or
fabricates entries. A shorter or less-dense valid horizon is a partial horizon,
and the UI states the final available local hour or date. Ordered, duplicate,
null-field, short, sparse, and empty results require focused tests.

### 0.2 Exact page and sub-page model

Standard Home has four global semantic pages:

```text
Now -> Hourly -> Daily -> Details
```

Simple remains `Now -> Forecast` unless a separate layout-behavior slice
changes it. Global page identity, count, and position are visible and exposed
as meaningful semantics.

Hourly contains up to twelve chronological six-entry windows. Windows are
anchored at the first valid forecast entry, so a rolling 72-hour interval can
span three or four local dates. A visible window names its inclusive local
range, for example `Tue, 12 AM–5 AM`; a range crossing midnight names both
dates. It presents up to six actual entries in earliest-to-latest order. At
normal supported sizes, the window uses a stable two-column by three-row
compact composition. Each entry contains local time, condition text and
supporting mark, temperature or `Temperature unavailable`, and precipitation
probability when available. Visible Earlier/Later controls change one window;
one local-date control per represented date selects the first window containing
that date. Hourly has no nested horizontal swipe.

Daily contains two chronological five-day windows. A window names its inclusive
date range and presents five stable comparison rows with date, condition,
numeric low/high, and precipitation meaning when available. A range bar is
supplemental. Visible Earlier/Later controls change one daily window. Fewer
than ten returned days retain their real order and state the final available
date; an empty list is a distinct unavailable-page state.

### 0.3 Navigation, gestures, Back, and scrolling

The outer Home pager is the default and sole horizontal-swipe owner. Static
page/background taps do not advance a page. Buttons, refresh/retry, location,
settings, alerts, links, and card actions retain their own gestures. Hourly and
Daily use their visible controls rather than nested horizontal pagers. A later
chart may consume horizontal gestures only inside declared chart bounds and
must provide an equivalent visible/semantic control.

Android Back closes a dialog, alert detail, Settings/supporting surface, or
location surface according to its existing return contract. From a non-Now
Standard Home global page it moves to the previous global page. From Now it
uses normal Android Back behavior. Forecast-window selection never consumes
Back.

At 360x640dp and font scale 1.0 or 1.3, Standard Home Now, Hourly, Daily, and
the first Details composition must be usable without primary page-level
vertical scrolling. Font scale 2.0, unusually long localized text, and long
safety/legal content may use localized vertical overflow to prevent clipping,
overlap, hidden controls, or excessively reduced text. Overflow must preserve
logical focus order and make all content reachable.

### 0.4 Card and page contracts

Cards are modular presentation units, not feature owners. Each receives only a
typed provider-neutral presentation model, stable card key, explicit
availability/freshness state, resolved appearance tokens, and semantic UI
action callbacks. A card must not receive a repository, provider DTO, cache,
persistence object, ViewModel/state holder, formatted-string parser, or theme
identifier. It must not fetch, convert canonical units, decide navigation, or
reinterpret weather/alert semantics.

The initial typed contracts are `NowCardModel`, `HourlyWindowModel`,
`DailyWindowModel`, `MetricCardModel`, `AlertSummaryCardModel`,
`SourceCardModel`, and `UnavailableCardModel`. Page composition owns card
placement; a navigation reducer owns page/window/retry/open-alert intents; a
card only emits those semantic events.

### 0.5 Theme hot-swap contract

A theme is immutable declarative data, not card-owned logic:

```text
ThemeDefinition + ContrastLevel + EffectsLevel + system motion policy
    -> ResolvedAppearance
    -> MaterialTheme bridge and Oxygen composition locals
    -> page/card/mark/scene renderers
```

`ResolvedAppearance` includes semantic colors, typography, dimensions, shapes,
surfaces, chart style, weather-mark style, scene parameters, and motion. Theme
ids do not appear in card branches. New visual literals are not added to cards
except deliberate internal geometry in a programmatic mark/chart renderer.

Built-in Oxygen, Paper, and Terminal themes may hot-swap by replacing the
resolved appearance after a confirmed preference write. A swap must not refetch
weather, alter canonical values, reset forecast-window selection without an
explicit contract, or change weather, alert, provenance, accessibility, or
navigation meaning. High contrast is an overlay, not a fourth theme. Effects
Off resolves opaque, static, complete surfaces and content; it requires no
scene, gradient, transparency, blur, or continuous animation.

The initial appearance implementation supports built-in themes only. Oxygen
must not execute theme/plugin code or download themes, fonts, imagery, or icon
packs at runtime.

A later community theme requires a versioned declarative schema, local
installation, required-token and contrast validation, license/attribution
metadata, offline fallback, and privacy review.

### 0.6 Data availability and state rules

Details may show only values available in normalized presentation data. The
initial candidate set is apparent temperature, humidity, wind, pressure, cloud
cover, precipitation, sun information, source, update, and provenance. Dew
point, visibility, UV, charts, compass, arcs, and other visualizations require
a provider-neutral domain field, mapper contract, missing-value rule, textual
equivalent, and focused test before a card is specified. UV is currently
deferred because the core weather model has no UV field.

Every global page/window distinguishes: loading with no cache; ready; restored
cache; offline/refresh failure with cache; offline/failure without cache;
partial horizon; missing field; no active alert; active alert; and unavailable
alert lookup. Missing data is omitted only when optional or named honestly;
it is never represented by zero or a plausible placeholder. Partial horizons
name the final available local hour/date.

### 0.7 Accessibility and evidence rules

Important weather facts are visible text and meaningful semantics. Decorative
marks/scenes are hidden only when adjacent merged semantics provide equivalent
meaning. Forecast entries expose one concise provider-neutral spoken summary.
Targets are at least 48dp where applicable. In LTR and RTL, forecast data and
accessibility traversal remain earliest-to-latest; physical directional control
placement and global swipe meaning mirror with layout direction, and controls
say Earlier/Later rather than relying on arrows alone.

Every implementation slice must name one production boundary, one visible
behavior, exact fixture/real-path state and horizon, focused automated
assertion, installed-app or screenshot evidence, relevant viewport/font/RTL/
appearance conditions, and out-of-scope limits. Compilation, previews,
symbol-only tests, and screenshots alone are not acceptance evidence.

Roadmap sequencing may begin from the accepted decisions above. The first
implementation slice is the forecast-horizon data contract only: request and
preserve the 72-hour/ten-day target and define truthful partial-provider
output. It excludes Hourly UI redesign, Back behavior, card extraction, and
theme work.

## 1. Authority and use

This specification translates three existing authorities into one UI contract:

1. The product and technical requirements in
   `docs/OXYGEN_FULL_SPECIFICATION.md`.
2. The visual direction in
   `docs/assets/oxygen-weather-visual-language-base-art-sheet-v0.2.png`.
3. The rendered-app development and evidence loop in
   `docs/UI_DEVELOPMENT_WORKFLOW.md`.

The full specification remains authoritative when this specification is
silent. The art sheet establishes visual direction, not runtime assets or
exact final measurements. The workflow establishes how a visual rule becomes
verified behavior. A later implementation roadmap must turn this specification
into bounded, independently observable slices.

This document is not a generic permission to redesign every surface at once.
Each implementation slice must name the surface or component it changes and
carry its own visual objectives, functional invariants, layout constraints,
and evidence boundary.

## 2. Product experience

Oxygen's central experience is:

> The weather is the artwork. The data is the interface.

The weather state should shape the atmosphere of the application, but the user
must never need the atmosphere to understand the forecast. The interface
should feel calm, intentional, and editorial rather than dashboard-like. It
should make the current weather immediately legible, then support quick
comparison and deliberate inspection.

The default presentation should be distinctive without requiring setup. It
should also remain complete and understandable when animation, gradients,
transparency, atmospheric scenes, or color are unavailable.

### 2.1 Design principles

- **Data first:** temperature, condition, time, precipitation, alerts, and
  provenance have clear visual priority over decoration.
- **Weather-state expression:** weather marks and procedural atmosphere reflect
  provider-neutral condition identity; they do not invent or reinterpret data.
- **Calm hierarchy:** use scale, spacing, grouping, and restrained contrast to
  guide attention instead of filling every region with ornament.
- **Editorial precision:** typography and alignment should make weather feel
  composed and trustworthy, not noisy or gamified.
- **Semantic stability:** themes, effects, layout presets, and decorative
  treatments may change presentation but never weather meaning or interaction
  semantics.
- **Progressive detail:** the Home surface answers “what is happening now?”
  first, “what happens next?” second, and exposes secondary measurements only
  when they are useful.

## 3. Visual language

### 3.1 Overall composition

The visual language combines atmospheric depth with precise information
surfaces:

- a weather-state background or field may provide depth and mood;
- a small number of structured surfaces group related data;
- strong display numerals establish current conditions at a glance;
- thin outlines, restrained highlights, and consistent symbols provide
  recognition without competing with the data;
- tertiary source and update information remains visible but quiet.

The art sheet's dark atmospheric examples are the primary reference for the
Oxygen direction. They are not a requirement that every theme use dark glass,
gradients, photographic imagery, or identical shapes.

### 3.2 Palette and semantic roles

The art sheet supplies these initial Oxygen palette references:

| Reference role | Initial value | Intended use |
| --- | --- | --- |
| Sky Top | `#07151D` | upper atmospheric field |
| Sky Bottom | `#153444` | lower atmospheric field |
| Atmospheric Glow | `#86E4F0` | restrained environmental highlight |
| Glass | `#23414D` | secondary grouped surface |
| Glass Strong | `#17313C` | primary grouped surface |
| Outline | `#7FC1CE` | surface and control boundary |
| Chart Accent | `#8DE7F1` | data visualization emphasis |
| Precipitation | `#79BFFF` | precipitation data, never the only meaning |
| Warning | `#FFB4BA` | alert emphasis paired with text and structure |

These values are references, not final accessibility approval. Implementation
must resolve colors through semantic roles and verify the resulting contrast.
Raw color literals must not be scattered through Composables.

Required semantic roles include, at minimum:

- background and atmospheric field;
- primary, secondary, and tertiary content;
- primary and secondary surfaces;
- outline and divider;
- primary action and action content;
- weather-state accent;
- precipitation;
- warning, danger, and informational alert roles;
- chart series and chart baseline;
- disabled and unavailable content.

Color must not be the only carrier of condition, precipitation, severity,
selection, stale state, or enabled state. Pair color with text, symbols,
structure, position, or an explicit label.

### 3.3 Typography

The art sheet establishes three typographic roles:

- **Display numerals:** prominent, calm, highly legible temperature and range
  values with enough scale to read at a glance.
- **Section headings:** editorial headings that separate Now, Hourly, Daily,
  Details, and related groups without overwhelming the data.
- **Labels and body copy:** compact uppercase labels for short metadata and
  clear body text for weather meaning, source information, and alerts.

Typography must be specified by semantic role rather than local font-size
choices. The implementation roadmap must select actual typefaces, weights,
line heights, and fallback behavior as a separate tokenization task. The art
sheet is typographic direction; it does not mandate an unlicensed font or
permit text to be rendered as an image.

Text must remain legible at large system font scales. Longer locations,
conditions, provider names, stale messages, and alert text must wrap or scroll
according to their reading nature without clipping or overlapping neighboring
content.

### 3.4 Shape, surface, and depth

Surface hierarchy should be visible through more than opacity alone:

- primary surfaces hold the main weather story or a high-priority warning;
- secondary surfaces group comparable forecast or metric data;
- ambient surfaces support context and atmosphere without becoming content;
- outlines and spacing clarify boundaries when transparency is reduced or
  removed.

Rounded, glass-like surfaces are part of the initial Oxygen direction, but the
shape vocabulary must remain restrained and consistent. Surface treatment may
vary by theme. A surface must not become unreadable when transparency is
disabled, and content must not depend on a background photograph or scene for
contrast.

### 3.5 Weather marks and symbols

Weather marks use a consistent line-based visual family inspired by the art
sheet: sun, cloud, rain, showers, snow, sleet, hail, thunderstorm, freezing
rain, fog, and unknown should be visually distinguishable.

Symbols are supporting representations of provider-neutral condition identity.
They must:

- have a text equivalent wherever they convey weather meaning;
- remain understandable in monochrome or high contrast;
- avoid animation as the only indication of state;
- be implemented as Compose/vector/procedural graphics unless a later asset
  decision explicitly authorizes a runtime bitmap;
- remain decorative in accessibility semantics when the adjacent weather
  description already communicates the same meaning.

### 3.6 Atmospheric scene language

The art sheet presents five scene directions: Clear, Cloudy, Rain, Storm, and
Snow. These establish a family of weather-state environments rather than a
library of downloaded backgrounds.

Scene treatment should be:

- procedurally rendered or otherwise locally controlled;
- subordinate to current weather data and alert content;
- visually different enough to establish weather mood;
- restrained enough that text, controls, and warnings remain dominant;
- removable without changing layout or meaning.

The initial implementation may use a smaller scene set than the full visual
direction. Unknown or unsupported states must fall back to a calm neutral
presentation rather than a fabricated weather scene.

## 4. Information architecture and page composition

The existing Standard Home model remains the presentation structure:

```text
Now -> Hourly -> Daily -> Details
```

These are semantic viewport-oriented pages, not one continuous dashboard. A
visual overhaul may change composition within a page, but it must preserve the
page model and named accessible movement between pages unless a separate
behavior slice changes that contract.

### 4.1 Now

Now is the visual anchor and should answer the immediate question: “What is it
like here now?”

Recommended hierarchy:

1. location and page identity;
2. current temperature as the dominant numeric element;
3. current condition and apparent temperature;
4. daily high/low and immediately relevant precipitation;
5. important alert or stale/error state;
6. source, update, and provenance context.

The weather mark and atmosphere support the current condition but must not
displace the current temperature, condition text, or alert meaning. The
composition should feel spacious on a normal viewport without requiring page-
level vertical traversal.

### 4.2 Hourly

Hourly answers: “What happens next?” It should optimize chronological scanning
and near-term comparison.

It presents the rolling 72-hour interval through the window and control model
in Section 0.2. Each actual provider entry is a stable, recognizable group
containing the time, condition mark/text, temperature, and precipitation
meaning when available. The row or card treatment may be atmospheric, but
repeated entries must remain easy to compare. The current hour should be
identifiable by text or structure, not color alone. A partial or sparse horizon
uses the truthful treatment in Sections 0.1 and 0.6.

### 4.3 Daily

Daily answers: “How do the coming days compare?” It should prioritize a compact
comparison of day, condition, high/low, and precipitation. Range visualization
may communicate relative temperature, but the numeric high and low remain
available as text. It presents up to ten actual local forecast dates through
the two-window model in Section 0.2 and states a shorter final available date.

### 4.4 Details

Details presents secondary measurements and provenance from the normalized
presentation fields allowed by Section 0.6. The initial candidate set is
apparent temperature, humidity, wind, pressure, cloud cover, precipitation,
sun information, source, update, and provenance. Visibility, dew point, UV,
charts, a compass, an arc, and other visualizations become eligible only after
their provider-neutral field, mapper, missing-value, text-equivalent, and test
contracts exist.

Every visualization requires a readable numeric or textual equivalent. Details
should feel related to Home without becoming a dense analytics dashboard.

### 4.5 Settings and supporting surfaces

Settings, location entry, alert detail, disclosures, and licenses should use
the same semantic typography, spacing, action, surface, and state roles as
Home. Their hierarchy is task-oriented rather than atmospheric: controls,
labels, statuses, confirmation text, and navigation actions take priority over
decorative scenes.

The art sheet does not define a separate Settings visual language. The
implementation roadmap must extend the shared design tokens without copying
Home's atmospheric composition into every supporting surface.

## 5. Themes, contrast, layout, and effects

These are independent presentation axes.

### 5.1 Built-in themes

The installed app currently supports Oxygen, Paper, and Terminal. The UI
specification preserves those identities:

- **Oxygen:** atmospheric, luminous, editorial, and weather-expressive;
- **Paper:** warm, material, quiet, and print/editorial in character;
- **Terminal:** crisp, technical, compact, and visibly distinct from Oxygen.

Themes share information hierarchy, semantics, content meaning, touch-target
requirements, and page structure. They may differ in palette, surface
treatment, shape vocabulary, typography treatment, chart styling, icon family,
and atmosphere. A theme must not own business logic or hide required data.

### 5.2 High contrast

High contrast is an independent presentation overlay, not a fourth theme. It
may replace palette, surface, outline, and supporting-content roles while
retaining theme identity, typography, sizing, shapes, weather marks, and
information layout.

High contrast must remain usable with atmospheric scenes, transparency,
gradients, and animation disabled. Warning severity, selected state, and stale
state must remain explicit without relying on hue or luminance differences.

### 5.3 Layout presets

Standard and Simple layouts are independent of theme and effects. A visual
overhaul may improve either layout, but must preserve their intended
information model:

- Standard: Now, Hourly, Daily, and Details pages;
- Simple: Now and Forecast pages with Hourly/Daily choices.

No visual treatment may force a whole-dashboard vertical scroll at ordinary
supported configurations. Local scrolling remains acceptable for genuinely
long content and accessibility overflow.

### 5.4 Effects

Effects levels are Off, Subtle, and Full. Effects can influence motion,
atmospheric rendering, and decorative treatment, but never information
availability or semantics.

- **Off:** complete static presentation with no required scene, animation,
  gradient, or transparency;
- **Subtle:** restrained motion or atmosphere that does not compete with data;
- **Full:** richer procedural weather expression within the same readability,
  motion-safety, and accessibility boundaries.

The implementation must honor Android disabled-animation behavior even when a
saved preference requests effects. The effective visual result may be Off
without rewriting the saved preference.

## 6. State presentation

Every major surface must have a visual treatment for the states already
supported by the production path:

| State | Presentation requirement |
| --- | --- |
| Loading | show clear progress/context without implying weather values are available |
| Ready | prioritize current weather, forecast content, and source/update context |
| Stale cached data | keep useful weather visible and clearly label freshness/source context |
| Offline | explain the limitation while retaining available cached information |
| Refresh or provider failure | preserve useful cached content when available; make retry explicit |
| No active alert | use calm, truthful status; do not imply an alert exists |
| Active alert | make severity, affected meaning, source, and action visually prominent; never rely on color alone |
| Missing field | omit or label the value honestly; never display fabricated zeroes or placeholders that look real |

State treatment must not change the underlying weather values or provider
semantics. Empty, unavailable, and unknown states need distinct enough copy and
structure that users can tell them apart.

## 7. Interaction and accessibility rules

Visual design is successful only when it remains usable at the accessibility
boundary.

- Important weather meaning is available as text and meaningful semantics.
- Decorative weather marks and scenes do not create redundant announcements.
- Named page movement actions remain discoverable and operable.
- Interactive targets are at least 48dp where the platform and existing
  contracts require a touch target.
- Focus and reading order follow the information hierarchy.
- Text remains legible with large system font settings, including font scale
  2.0 where the relevant surface requires it.
- LTR and RTL layouts preserve chronology, grouping, and action meaning.
- Content does not clip, overlap, or become unreachable at compact supported
  widths.
- Effects Off, disabled animations, no transparency, and high contrast retain
  the same information and behavior.
- Charts and visualizations expose text alternatives or equivalent readable
  values.
- Alerts, stale state, selected state, and unavailable values are not conveyed
  by color alone.

TalkBack service traversal remains an unverified project-level boundary, but
these UI obligations still apply to every slice. A visual change must not
weaken the existing semantics contract merely to match the art sheet.

## 8. Asset and implementation policy

The art sheet is a reviewable design artifact, not an application runtime
asset. It may guide implementation of:

- Compose/vector weather marks;
- procedural atmospheric scenes;
- semantic surfaces and outlines;
- typography and spacing roles;
- charts, ranges, and metric visualizations;
- translation across Oxygen, Paper, and Terminal.

Do not introduce downloaded photographic weather backgrounds, production bitmap
icons, hidden text, fabricated values, provider-specific UI fields, or
unlicensed fonts as an interpretation of the art sheet. Any future asset
exception needs its own contract covering license, runtime use, fallback,
accessibility, and offline behavior.

## 9. UI slice contract for the implementation roadmap

Each later roadmap slice must fill in this template before implementation:

### Slice identity

- **Surface/component:**
- **Theme/layout/effects scope:**
- **Out of scope:**

### Functional invariants

State what must not change, including as applicable:

- weather values and condition identity;
- source, update, stale, and provenance meaning;
- navigation and page actions;
- refresh and retry behavior;
- provider, repository, cache, and persistence behavior;
- accessibility semantics and spoken weather meaning.

### Visual objectives

State the intended rendered result in observable terms, for example:

- establish the current-weather hierarchy;
- make comparable forecast entries scan consistently;
- reduce tertiary visual weight;
- distinguish alert severity structurally;
- improve spacing rhythm and information grouping;
- express the selected theme without changing semantic roles.

### Layout constraints

Specify the relevant boundary:

- compact device width and height;
- long location, condition, provider, or alert text;
- large font scale;
- LTR and RTL;
- Standard and/or Simple layout;
- Effects Off and disabled animations;
- high contrast;
- no clipping, sibling overlap, or unintended horizontal scrolling;
- required touch-target and focus bounds.

### Evidence

The visual workflow for each slice is:

```text
establish objective
-> capture installed baseline
-> inspect relevant Compose code
-> make one bounded change
-> compile/build as needed
-> install and launch
-> capture the rendered result
-> inspect and iterate
-> run focused checks
-> capture final evidence
-> run applicable broad checks
-> commit
```

The installed application through the real presentation path is the rendering
authority. Final screenshots and any supporting hierarchy or test records must
be saved under `.codex/test-artifacts/<cycle-id>/` and referenced by the
active plan and cycle history. Compilation alone is not visual evidence.

## 10. Decisions delegated to bounded implementation slices

The review/audit decisions above are sufficient to begin roadmap sequencing.
The following implementation details remain deliberately unresolved. Each must
be selected and verified before the slice that first depends on it, but none
blocks the forecast-horizon data-contract slice:

- final typeface and fallback policy for display, heading, label, and body
  roles;
- canonical spacing, sizing, corner, outline, and elevation tokens;
- exact responsive behavior for compact, normal, and large layouts;
- final Oxygen/Paper/Terminal palette and surface mapping;
- minimum weather-mark and scene coverage for the first visual slices;
- chart and metric visualization grammar;
- loading, stale, offline, error, alert, and missing-field component designs;
- baseline screenshot matrix and the minimum installed states for each slice;
- which existing visual baselines are preserved, revised, or superseded;
- the bounded component order after the forecast-horizon contract.

These are slice-design questions, not permission to widen the first
implementation slice. Once resolved and rendered successfully, repeated visual
decisions should be promoted into versioned design tokens and the ordered
implementation roadmap.

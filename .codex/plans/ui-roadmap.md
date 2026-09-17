# Oxygen Home UI v0.3 Roadmap

**Status:** active roadmap; UI-01's `planned` selection is tracked only in
`.codex/plans/current.md`. All unselected roadmap candidates remain
`specified`.
**Roadmap ID:** `ui-home-v0.3-2026-09-reviewed-draft`
**Source authority:** [`docs/OXYGEN_UI_SPECIFICATION.md`](../../docs/OXYGEN_UI_SPECIFICATION.md)
**Higher authority:** [`docs/OXYGEN_FULL_SPECIFICATION.md`](../../docs/OXYGEN_FULL_SPECIFICATION.md)
**Evidence authority:** [`docs/UI_DEVELOPMENT_WORKFLOW.md`](../../docs/UI_DEVELOPMENT_WORKFLOW.md)
**Reviewed:** 2026-09-16

This roadmap delivers the UI Specification v0.3 Home program and extends its
shared presentation roles to the existing supporting surfaces. It is not the
complete Oxygen 1.0 roadmap. A candidate remains `specified` until
`.codex/plans/current.md` selects it as the one active bounded slice and marks
it `planned`.

The installed app already has the Standard and Simple Home shells, named Home
pages, persisted theme/contrast/layout/effects choices, initial Home design
roles, weather marks, a weather scene, operational states, Settings routes,
location entry, and alert detail. Candidates below name a delta from that
implemented baseline; they do not downgrade existing behavior to `specified`.

## Program limits

This roadmap does not complete or claim Oxygen 1.0. Higher-authority work that
remains outside this program includes:

- custom-unit editing;
- the provider-neutral UV presentation prerequisite not yet available to the
  Home presentation model;
- Detailed and Meteorologist layout behavior;
- icon-pack settings, maps, radar, air quality, widgets, notifications,
  localization completion, release packaging, signing, and publication;
- service-level TalkBack traversal, which remains an optional unverified
  project boundary without waiving per-slice accessibility obligations.

Any later roadmap that claims 1.0 coverage must own or explicitly reconcile
these gaps with the full product specification.

## Operating and evidence rules

- Preserve the production selected-location, provider/fallback, Room cache,
  units, alert, privacy, and disclosure paths unless a selected slice explicitly
  owns a named change.
- Keep the existing Standard `Now -> Hourly -> Daily -> Details` and Simple
  `Now -> Forecast` shells. A slice changes only its named behavior or surface.
- Each production slice owns one user-visible surface, one interaction state
  machine, or one production prerequisite. Page-specific loading, cache,
  failure, partial, missing, alert, layout, and accessibility obligations remain
  in the page slice that renders them; they are not deferred to final polish.
- Before a rendered change, retain an installed baseline for the affected
  surface. After convergence, retain the final installed rendering and one
  focused automated assertion of the changed behavior. Screenshots do not prove
  actions, state transitions, no-refetch behavior, ordering, or semantics.
- Rare provider, alert, failure, sparse, and empty states may use deterministic
  fixtures through the real Compose/presentation boundary. The installed
  selected-location production route remains the authority for the ordinary
  ready path. Sample/preview data never enters production Home.
- Use one emulator session per task and install once per APK change. Do not
  repeat retained theme, contrast, RTL, font, or state cases unless the selected
  production change can affect them.
- The active plan assigns a minimal orthogonal matrix rather than a Cartesian
  product. Default visual evidence is the compact supported viewport with
  Effects Off; add font scale 2.0, RTL, High Contrast, another built-in theme,
  disabled animations, or long text only where the changed boundary can affect
  that condition. Stay within the default eight connected cases.
- Every two completed production-changing slices are followed immediately by a
  test-only and documentation-sync checkpoint. Repairs and refactors count;
  documentation-only and checkpoint cycles do not. A repair discovered at a
  checkpoint is selected separately, then the checkpoint resumes.
- Final documentation closure inventories existing evidence and runs only
  applicable checks. It does not repeat passing journeys merely to create a
  larger artifact set.

## Ordered candidates

### UI-01 — Forecast-horizon transport and preservation

**Status:** verified
**Boundary:** production Open-Meteo request plus provider-neutral mapper,
repository/fallback, and Room-cache preservation of forecast entries.
**Observable delta:** Open-Meteo requests 72 forecast hours and ten forecast
days. Open-Meteo and MET Norway paths preserve every valid returned entry in
chronological order through repository and cache boundaries, without padding,
interpolation, repetition, or fabricated values. Short, sparse, duplicate,
null-field, and empty responses remain truthful domain output.
**Downstream behavior enabled:** Hourly can select the rolling 72-hour interval,
and Daily can select the first ten distinct local dates.
**Focused evidence:** 49 named core JVM tests pass for the request, Open-Meteo
and MET Norway mapping, fallback pass-through, and cache readback boundaries.
One connected API-37 `oxygen_starter` case passes through the installed factory,
real Open-Meteo parser/repository, fallback/cache wrappers, Room readback, and
no-alert merge, observing all 72 hourly and ten daily rows. Artifacts are under
`.codex/test-artifacts/2026-09-16-ui-01-forecast-horizon-transport/`.
**Out of scope:** Home window selection, partial-horizon copy, page controls,
visual redesign, themes, scenes, and new providers.
**Sizing:** medium; one data-transport/preservation concern exercised through
the existing production repository path.

### UI-02 — Standard Home pager and Android Back contract

**Status:** verified
**Dependencies:** existing Standard Home shell; UI-01 is not required.
**Boundary:** outer Standard Home pager, visible/semantic page navigation, and
Android Back dispatch.
**Observable delta:** named `Now`, `Hourly`, `Daily`, and `Details` navigation
retains one outer horizontal-swipe owner and visible direct controls; static
background taps do not advance pages; Back from a non-Now page moves to the
previous global page; Back from Now retains normal Android behavior. Child
actions keep their gestures and later forecast-window selection cannot consume
Back.
**Focused evidence:** deterministic connected assertions for visible names,
semantic position/actions, static-tap behavior, child-action isolation, and
Back transitions; one installed Standard journey through all pages and Back at
the compact viewport with Effects Off.
**Verified evidence:** three named API-37 connected cases passed 1/1 with
fresh XML, instrumentation logs, and textproto results; the installed
selected-Chicago journey retained Now through Details and returned Details →
Daily → Hourly → Now through Android Back, with Back from Now falling through
to the host. Artifacts are under
`.codex/test-artifacts/2026-09-16-ui-02-standard-pager-back/`.
**Out of scope:** Hourly/Daily windows, page visual redesign, location/supporting
surface Back contracts, and new navigation destinations.
**Sizing:** small-to-medium; one platform navigation state machine over the
existing page shell.

### UI-03 — Checkpoint A

**Status:** specified
**Dependencies:** UI-01 and UI-02
**Boundary:** test-only evidence and authority reconciliation after two
production-changing slices.
**Acceptance:** review focused results and artifacts, run only broader checks
made relevant by UI-01/UI-02, and reconcile the plan, roadmap, history, README,
and affected specifications. Any defect becomes a separately selected repair.

### UI-04 — Now hierarchy and first proven shared tokens

**Status:** specified
**Dependencies:** UI-02
**Boundary:** Standard Home Now composition and only the semantic typography,
spacing, shape, surface, and action roles proven by that composition.
**Observable delta:** current conditions are the dominant hierarchy; location,
condition, apparent temperature, high/low, immediate precipitation, alert or
operational state, source, update, and provenance remain calm and legible.
Ready, stale/cache, failure-with-cache, missing-current, no-alert, active-alert,
and unavailable-alert lookup states remain structurally distinct. Active alert
severity and action are explicit without color alone.
**Focused evidence:** presentation/card tests for the named states, including a
real active-alert fixture rather than an “alert-compatible” placeholder; one
focused Compose test for hierarchy/semantics/actions; installed before/after
rendering of the production ready path plus the smallest deterministic state
set needed to demonstrate the visual delta.
**Layout boundary:** 360x640dp at font scale 1.0/1.3 without primary page
scrolling; localized overflow at 2.0; long text, RTL, High Contrast, and Effects
Off checked only where the changed composition can affect them.
**Out of scope:** all-at-once design-system replacement, Hourly/Daily/Details,
scenes, supporting surfaces, and new weather fields.
**Sizing:** medium; one page surface establishes reusable tokens only after
their installed rendering succeeds.

### UI-05 — Hourly rolling windows and controls

**Status:** specified
**Dependencies:** UI-01, UI-02, and the shared roles proven by UI-04
**Boundary:** Hourly presentation selection, one six-entry window state machine,
and the Standard Hourly page.
**Observable delta:** Hourly selects actual entries inside the rolling 72-hour
interval and exposes up to twelve chronological windows anchored at the first
valid entry. The visible inclusive local range, stable two-column by three-row
composition, Earlier/Later actions, and one date control per represented local
date operate without nested horizontal swipe or provider refetch. Partial,
sparse, missing-temperature, missing-precipitation, and empty results name the
truthful final available hour or unavailable state.
**Focused evidence:** mapper/window tests for timezone anchoring, DST, ordering,
date selection, partial/sparse/empty data, and missing values; one connected
action test proving Earlier/Later/date transitions, semantics, chronology, and
no-refetch behavior; installed before/after rendering through the production
selected-location path.
**Layout boundary:** compact 2x3 stability, long local-range text, font overflow,
RTL chronology and mirrored controls, 48dp targets, and Effects Off.
**Out of scope:** charts, Daily, provider changes, and gesture changes outside
the Hourly window.
**Sizing:** medium; one forecast-window state machine and its page surface.

### UI-06 — Checkpoint B

**Status:** specified
**Dependencies:** UI-04 and UI-05
**Boundary:** recurring test-only and documentation-sync checkpoint.
**Acceptance:** apply the checkpoint contract from UI-03 to the two completed
production slices and reset the implementation-slice count.

### UI-07 — Daily comparison windows and controls

**Status:** specified
**Dependencies:** UI-01, UI-02, and the shared roles proven by UI-04
**Boundary:** Daily local-date selection, one five-row window state machine, and
the Standard Daily page.
**Observable delta:** Daily presents up to ten actual distinct local forecast
dates in two chronological five-row windows. Every available row exposes date,
condition, numeric low/high or honest unavailability, and precipitation meaning;
the range bar remains supplemental. Earlier/Later changes one window without
nested swipe or refetch. Partial results state the final date; empty results are
visibly unavailable.
**Focused evidence:** mapping/window tests for timezone/DST date grouping,
duplicates, ordering, partial/empty and missing values; one connected action
test proving window transition, semantics, chronology, and no-refetch behavior;
installed before/after rendering through the production selected-location path.
**Layout boundary:** stable five-row comparison, compact width, long dates,
localized font overflow, RTL chronology, 48dp controls, High Contrast, and
Effects Off.
**Out of scope:** historical/long-range forecasts, charts, alert detail, and
provider/cache changes.
**Sizing:** medium; one comparison-window state machine and its page surface.

### UI-08 — Details typed cards and available metrics

**Status:** specified
**Dependencies:** UI-02 and the shared roles proven by UI-04
**Boundary:** Standard Details page plus typed metric, source, and unavailable
card presentation models.
**Observable delta:** Details renders only normalized values currently available
to presentation: apparent temperature, humidity, wind, pressure, cloud cover,
precipitation, visibility, dew point, sun information, source, update, and
provenance. Cards have stable keys, explicit availability/freshness, resolved
appearance roles, and semantic callbacks; they do not receive providers,
repositories, cache objects, state holders, theme ids, or formatted-string
parsers.
**Focused evidence:** presentation tests for each available/missing group and
source/freshness state; one Compose test for stable card semantics, unavailable
treatment, and readable provenance; installed before/after rendering at compact
width and one representative font-scale-2.0 overflow state.
**Out of scope:** UV, charts, compass, arcs, history, and other fields lacking
the prerequisite normalized contracts.
**Sizing:** medium; one typed presentation boundary and one page surface.

### UI-09 — Checkpoint C

**Status:** specified
**Dependencies:** UI-07 and UI-08
**Boundary:** recurring test-only and documentation-sync checkpoint.
**Acceptance:** apply the checkpoint contract from UI-03 to the two completed
production slices and reset the implementation-slice count.

### UI-10 — Simple layout parity

**Status:** specified
**Dependencies:** UI-04, UI-05, and UI-07
**Boundary:** existing Simple `Now -> Forecast` composition and Hourly/Daily
choice behavior.
**Observable delta:** Simple reuses the accepted Now and forecast presentation
contracts without provider refetch, while preserving its two-page information
model, visible Hourly/Daily choices, units, operational states, accessibility,
and appearance behavior.
**Focused evidence:** deterministic tests for choice/page state retention,
shared truthful partial/unavailable behavior, and no-refetch invariants; one
installed Simple production journey covering both Forecast choices and return.
**Out of scope:** Simple-only weather semantics, custom-unit editing, new layout
presets, scenes, widgets, and notifications.
**Sizing:** medium; one existing alternate-layout composition boundary.

### UI-11 — Task-oriented alert-detail presentation roles

**Status:** specified
**Dependencies:** shared roles proven by UI-04
**Boundary:** existing alert-detail surface only.
**Observable delta:** alert detail adopts task-oriented semantic typography,
spacing, surface, warning, action, and state roles without atmospheric Home
composition. Complete official text, issuer, effective/expiry, affected area,
instructions, source action, multi-alert selection, and return behavior remain
unchanged and reachable.
**Focused evidence:** retained alert tests plus focused assertions for any
changed semantics/layout; installed deterministic long-alert before/after
rendering at RTL or font scale 2.0 with High Contrast and Effects Off.
**Out of scope:** alert persistence, polling, notifications, paraphrasing,
provider changes, and live-alert availability claims.
**Sizing:** small-to-medium; one supporting surface using existing behavior.

### UI-12 — Checkpoint D

**Status:** specified
**Dependencies:** UI-10 and UI-11
**Boundary:** recurring test-only and documentation-sync checkpoint.
**Acceptance:** apply the checkpoint contract from UI-03 to the two completed
production slices and reset the implementation-slice count.

### UI-13 — Provider-neutral weather-mark family

**Status:** specified
**Dependencies:** UI-04, UI-05, UI-07, and UI-10
**Boundary:** Compose/vector weather marks and condition-to-mark mapping.
**Observable delta:** clear day/night, mostly clear, partly cloudy, cloudy, fog,
drizzle, freezing precipitation, rain/showers, snow/showers, sleet, hail,
thunderstorm variants, and unknown use one coherent family. Marks remain
recognizable in monochrome/High Contrast and decorative when adjacent text
already communicates the condition.
**Focused evidence:** exhaustive provider-neutral condition-to-mark/fallback
mapping tests and focused semantics assertions; installed representative size
and theme rendering for the smallest set that proves line-family consistency,
monochrome readability, and unknown fallback.
**Out of scope:** provider-code remapping, bitmap packs, icon-pack settings,
continuous animation, and atmospheric scenes.
**Sizing:** medium; one programmatic symbol system with deterministic mapping.

### UI-14 — Procedural Subtle scenes and effective Off

**Status:** specified
**Dependencies:** UI-04 and UI-13
**Boundary:** the production scene renderer reached by the existing persisted
Subtle choice and the existing effective-Off motion override.
**Observable delta:** representative clear, cloudy, rain, storm, and snow
conditions produce restrained local procedural atmosphere under Subtle. Effects
Off and Android disabled animations render complete opaque/static information
without required scene, gradient, transparency, blur, or motion. Unknown falls
back to a neutral scene.
**Focused evidence:** condition-to-scene, unknown-fallback, effective-Off, and
semantic-invariance tests; installed before/after representative Subtle
rendering plus Effects Off/disabled-animation rendering through the real Home
composition. Screenshots supplement rather than replace state assertions.
**Out of scope:** downloaded imagery, provider weather-code changes, Full
preference persistence, and unrelated page redesign.
**Sizing:** medium; one reachable visual-effects renderer and its Off fallback.

### UI-15 — Checkpoint E

**Status:** specified
**Dependencies:** UI-13 and UI-14
**Boundary:** recurring test-only and documentation-sync checkpoint.
**Acceptance:** apply the checkpoint contract from UI-03 to the two completed
production slices and reset the implementation-slice count.

### UI-16 — Full effects preference and richer scene behavior

**Status:** specified
**Dependencies:** UI-14
**Boundary:** existing effects preference transaction plus the production scene
renderer’s Full mode.
**Observable delta:** Settings offers Off, Subtle, and Full as persisted
single-choice values with the existing confirmed-write, restoration, failure,
retry, and disabled-animation contracts. Full produces a visibly richer but
readable version of the established procedural scenes; effective Off never
rewrites the saved Full choice or changes information/navigation semantics.
**Focused evidence:** codec/storage/state tests and a focused connected
selection/restoration/failure/no-refetch test; one installed Full selection,
Home rendering, disabled-animation effective-Off check, and saved-choice
restoration journey.
**Out of scope:** icon packs, downloaded assets, new weather semantics, and
community themes.
**Sizing:** medium; one existing preference state machine extended end-to-end to
reachable production behavior.

### UI-17 — Settings and disclosure-family presentation roles

**Status:** specified
**Dependencies:** shared roles proven by UI-04; UI-16 for the final effects row
**Boundary:** existing Settings root, Appearance, Units, and disclosure-family
screens implemented through the shared Settings surface.
**Observable delta:** these task-oriented screens use shared typography,
spacing, surface, action, selected, unavailable, loading, saved, failure, and
warning roles without copying Home atmosphere. Existing routes, preference
transactions, attribution/license text, fixed Back behavior, and disclosure
meaning remain unchanged.
**Focused evidence:** focused Compose assertions only for changed shared roles
and retained transaction semantics; installed before/after Settings journey
covering one preference write and one disclosure route at the minimal compact/
large-text condition affected by the change.
**Out of scope:** rewriting disclosures, new settings, custom units, diagnostics,
localization completion, and alert/location surfaces.
**Sizing:** medium; one existing task-oriented Settings implementation family.

### UI-18 — Checkpoint F

**Status:** specified
**Dependencies:** UI-16 and UI-17
**Boundary:** recurring test-only and documentation-sync checkpoint.
**Acceptance:** apply the checkpoint contract from UI-03 to the two completed
production slices and reset the implementation-slice count.

### UI-19 — Location-entry presentation roles

**Status:** specified
**Dependencies:** shared roles proven by UI-04
**Boundary:** existing first-run/change-location search, saved-list, save, select,
remove, confirmation, loading, empty, and failure surface.
**Observable delta:** location entry adopts task-oriented shared roles without
changing optional permission behavior, geocoding, saved-location identity,
selection concurrency, deletion confirmation, forecast/cache behavior, or
return navigation.
**Focused evidence:** focused assertions for changed layout/semantics while
retaining search/select/save/remove behavior; one installed manual-location
journey without a location grant at compact width, with a targeted RTL or long-
text case if the changed layout can affect it.
**Out of scope:** location-provider changes, background location, automatic
relocation, saved-location reordering, and new permission requests.
**Sizing:** medium; one supporting surface and its existing production path.

### UI-20 — UI evidence and documentation closure

**Status:** specified
**Dependencies:** all selected roadmap implementation slices and repairs
**Boundary:** evidence inventory and authority synchronization for this Home UI
v0.3 roadmap only.
**Observable outcome:** retained artifacts cover every accepted delta; plan,
roadmap, cycle history, README, and affected specifications distinguish
specified, covered, implemented, verified, committed, and remaining work.
Passing journeys are not rerun unless intervening changes invalidate them.
**Focused evidence:** artifact/result inventory, applicable broad checks,
`git diff --check`, and source/status audit. If UI-19 is followed by another
production-changing slice before closure, the mandatory next checkpoint occurs
before this documentation cycle.
**Out of scope:** new product behavior, 1.0 completion, release readiness,
signing/publication, localization completion, and TalkBack service claims.
**Sizing:** small-to-medium; documentation/evidence only.

## Active selection

UI-02 is verified in `.codex/plans/current.md`. UI-03 is the next specified
test-only/documentation checkpoint after the two production-changing slices;
UI-05 and UI-07 remain unselected until their dependencies are verified.

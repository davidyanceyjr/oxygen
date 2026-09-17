# Oxygen Home UI v0.3 Roadmap

**Status:** active roadmap; UI-01 through UI-05 are verified. UI-06 and later remain specified until selected in .codex/plans/current.md.
**Roadmap ID:** ui-home-v0.3-2026-09-small-slice-audit
**Source authority:** docs/OXYGEN_UI_SPECIFICATION.md
**Higher authority:** docs/OXYGEN_FULL_SPECIFICATION.md
**Evidence authority:** docs/UI_DEVELOPMENT_WORKFLOW.md
**Audited:** 2026-09-17

This is a Home/UI program, not an Oxygen 1.0 roadmap. The installed app already has Standard and Simple Home shells, persisted appearance choices, selected-location/provider/cache paths, weather marks, basic scenes, Details groups, Settings, location entry, and alert detail. A candidate names only a measured delta from that baseline.

## Program limits

Custom-unit editing, UV presentation, Detailed/Meteorologist layouts, icon packs, maps, radar, air quality, widgets, notifications, localization, release packaging/signing/publication, and service-level TalkBack traversal are outside this program. The optional deferred TalkBack audit does not waive per-slice accessibility obligations.

## Operating contract

- A production slice changes one visible page section or one interaction transition. Its other page sections are regression invariants. A prerequisite is allowed only when it exercises the production path and immediately enables the next named visible section.
- The hard estimate ceiling is **3/10 (33%)** of one implementation session. Discovery, rendered iteration, verification, review, and closure are outside that estimate. Split before coding if a selected plan needs a page restyle, new presentation mapping, and another state machine.
- Preserve selected-location, provider/fallback, cache, units, alert, privacy, disclosure, pager, and Back behavior unless the selected slice names the change. Sample/preview data never enters production Home.
- Every rendered slice captures an installed baseline and final PNG for the ordinary production route. It also has one deterministic runner-backed Compose method at a named fixture, viewport, font, layout direction, contrast, and effects state. That method writes a PNG and asserts its exact measurable contract: text/state, item count/order, action result, bounds/no overlap, target size, or request count. Screenshots are reviewed visual evidence, not an unimplemented pixel-golden claim and not proof by themselves.
- Use the smallest orthogonal condition set. Effects Off at 360x640dp is the default; add RTL, font 2.0, High Contrast, long text, another theme, or disabled animation only when that section can be affected. Stay within eight connected cases.
- **Failure stop rule:** after a focused or connected acceptance failure, inspect the fresh result once, make at most one bounded relevant fix, then rerun that failed acceptance once. If it still fails, or a platform attempt times out, record commands, artifacts, diagnosis, and attempted fix; mark the slice blocked; update the active plan and cycle history; and stop work on that slice. Do not widen scope, retry unchanged input, run broad checks, or claim verification. A separately selected repair may resume it.
- After every two production-changing slices, complete the named checkpoint. It inventories evidence and status, runs only applicable broader checks, and resets the count. A discovered defect is a separately selected repair.

## Audit disposition and approach

The previous Hourly, Daily, Details, Simple, scenes, Settings, and location candidates bundled whole pages with mapping, state machines, exceptional states, or multiple conditions. They exceeded the ceiling and are replaced below. The existing production weather-mark family, existing Effects-Off path, and existing Details groups are not candidates by themselves. A future repair there needs a measured rendered defect.

The approach is **one page section at a time**: establish a compact forecast window, add one navigation mechanism, then render truthful exceptional states. This gives each slice a visible, usable result and an exact test boundary.

## Verified baseline

### UI-01 — Forecast-horizon transport and preservation

**Status:** verified. Open-Meteo requests 72 hours and ten days; valid entries survive mapper, fallback, and Room paths truthfully. Evidence: 49 core tests and one API-37 installed factory/repository/cache case under .codex/test-artifacts/2026-09-16-ui-01-forecast-horizon-transport/.

### UI-02 — Standard Home pager and Android Back contract

**Status:** verified. Standard Home owns the outer pager and Back moves Details → Daily → Hourly → Now. Evidence: three connected cases and installed journey under .codex/test-artifacts/2026-09-16-ui-02-standard-pager-back/.

### UI-03 — Checkpoint A

**Status:** verified. Retained UI-01/UI-02 evidence and applicable checks were reconciled; production-slice count reset.

### UI-04 — Standard Now hierarchy and truthful alert state

**Status:** verified. Standard Now has fixed current-weather hierarchy, local supporting overflow, and typed alert lookup states; Simple Now is unchanged. Evidence: five connected cases, installed route, and focused/broad checks under .codex/test-artifacts/2026-09-17-ui-04-now-hierarchy/.

## Ordered candidates

### UI-05 — Hourly first/next compact window

**Dependencies:** UI-01, UI-02, UI-04
**Status:** verified. The Standard Hourly production path now presents a
truthful local-date range over up to six actual entries, with Later/Earlier
covering the two mapped windows without refetch. Simple Forecast remains on
its prior composition. Evidence: mapper tests, two runner-backed connected
cases, and installed Chicago PNG/XML under
`.codex/test-artifacts/2026-09-17-ui-05-hourly-window/`.
**Outcome:** Standard Hourly shows exactly six actual chronological entries in a stable 2×3 window with an inclusive local-time range. Later exposes the next six and Earlier returns to the first; both are 48dp and do not refetch.
**Measure:** a 12-entry fixture proves two windows, six ordered tags/range text, actions, unchanged request count, and 2×3 non-overlap; retain deterministic and installed Hourly PNGs.
**Out:** later windows, date jump, exceptional states, Daily, Simple. **Size:** 3/10.

### UI-06 — Checkpoint B

**Dependencies:** UI-04, UI-05
**Outcome/measure:** test-only evidence matrix accounts for each UI-04/UI-05 method, PNG, installed route, command result, and status claim; applicable broad checks and git diff --check pass or record an exact discrepancy.

### UI-07 — Hourly rolling-window reach

**Dependencies:** UI-05
**Outcome:** Earlier/Later reaches every six-entry window in the actual 72-hour list, disables at endpoints, and preserves outer-pager ownership.
**Measure:** 72-entry fixture proves twelve windows, first/last entries and range, endpoint state, chronology, and no refetch; retain final-window and installed-first-window PNGs.
**Out:** date jump and exceptional copy. **Size:** 3/10.

### UI-08 — Hourly date jump controls

**Dependencies:** UI-07
**Outcome:** one control per represented local date selects that date's first Hourly window; DST and overnight ranges remain chronological.
**Measure:** date/DST fixture proves control-to-window mapping, 48dp bounds, and no refetch; retain selected-date PNG.
**Out:** exceptional states. **Size:** 3/10.

### UI-09 — Checkpoint C

**Dependencies:** UI-07, UI-08
**Outcome/measure:** apply UI-06 checkpoint contract to UI-07/UI-08 and reset count.

### UI-10 — Hourly truthful exceptional states

**Dependencies:** UI-08
**Outcome:** Hourly separately renders sparse/partial final hour, missing temperature, missing precipitation, and an empty timeline without zeros, padding, or invented dates.
**Measure:** four fixture states assert exact truthful copy/absence; retain one compact sparse PNG and one font-2.0 RTL PNG where overflow occurs.
**Size:** 3/10.

### UI-11 — Daily first/next comparison window

**Dependencies:** UI-01, UI-02, UI-04
**Outcome:** Standard Daily shows one five-row chronological comparison window; Later shows the next five actual dates and Earlier returns. Rows include date, condition, numeric low/high, and precipitation meaning.
**Measure:** ten-date fixture proves two five-row windows, order, 48dp controls, compact non-overlap, and no refetch; retain deterministic and installed PNGs.
**Out:** direct date controls and exceptional states. **Size:** 3/10.

### UI-12 — Checkpoint D

**Dependencies:** UI-10, UI-11
**Outcome/measure:** apply UI-06 checkpoint contract to UI-10/UI-11 and reset count.

### UI-13 — Daily date jump controls

**Dependencies:** UI-11
**Outcome:** a local-date control selects its five-row window without consuming the outer pager gesture.
**Measure:** ten-date/DST fixture proves mapping, chronology, 48dp bounds, and no refetch; retain selected-window PNG.
**Out:** exceptional states. **Size:** 2/10.

### UI-14 — Daily truthful exceptional states

**Dependencies:** UI-13
**Outcome:** Daily names the real final available date for partial data and honestly renders missing low/high, missing precipitation, and an empty list.
**Measure:** four fixture states assert final-date/unavailability text and no placeholder number; retain compact and font-2.0 RTL PNGs when needed.
**Size:** 3/10.

### UI-15 — Checkpoint E

**Dependencies:** UI-13, UI-14
**Outcome/measure:** apply UI-06 checkpoint contract to UI-13/UI-14 and reset count.

### UI-16 — Details: current-feel metric group

**Dependencies:** UI-02, UI-04
**Outcome:** only apparent temperature, humidity, dew point, and wind receive the selected Details hierarchy; other sections are unchanged.
**Measure:** fixture proves four stable keys/order, available/missing treatment, spoken values, and compact non-overlap; retain deterministic and installed Details PNGs.
**Out:** source, sun, atmospheric metrics. **Size:** 3/10.

### UI-17 — Details: atmospheric metric group

**Dependencies:** UI-16
**Outcome:** only pressure, visibility, cloud cover, and precipitation receive the proven Details treatment.
**Measure:** fixture proves order, unavailable treatment, and no fabricated zero with a compact PNG.
**Out:** source and sun. **Size:** 2/10.

### UI-18 — Checkpoint F

**Dependencies:** UI-16, UI-17
**Outcome/measure:** apply UI-06 checkpoint contract to UI-16/UI-17 and reset count.

### UI-19 — Details: sun-information section

**Dependencies:** UI-16
**Outcome:** only sunrise/sunset gains its final compact presentation and honest absence state.
**Measure:** available/absent fixtures prove label/order/text, no empty card, and compact bounds; retain PNG.
**Size:** 2/10.

### UI-20 — Details: source and provenance section

**Dependencies:** UI-16
**Outcome:** only source, update, attribution, and provenance gain the final Details treatment; source meaning and external-link behavior are unchanged.
**Measure:** fresh/cached fixtures prove source/update order, link action, and font-2.0 long-provenance reachability; retain deterministic and installed PNGs.
**Size:** 3/10.

### UI-21 — Checkpoint G

**Dependencies:** UI-19, UI-20
**Outcome/measure:** apply UI-06 checkpoint contract to UI-19/UI-20 and reset count.

### UI-22 — Simple Forecast: Hourly choice

**Dependencies:** UI-10
**Outcome:** Simple's existing Hourly choice renders the accepted Hourly window contract without changing its two-page model or refetching.
**Measure:** choice/return fixture proves selection, first-window order, and unchanged request count; retain compact deterministic and installed PNGs.
**Size:** 3/10.

### UI-23 — Simple Forecast: Daily choice

**Dependencies:** UI-14, UI-22
**Outcome:** Simple's existing Daily choice renders the accepted Daily window contract and returns to Hourly without refetching.
**Measure:** choice/return fixture proves five-row order and unchanged request count with compact PNG.
**Size:** 2/10.

### UI-24 — Checkpoint H

**Dependencies:** UI-22, UI-23
**Outcome/measure:** apply UI-06 checkpoint contract to UI-22/UI-23 and reset count.

### UI-25 — Alert detail: status header

**Dependencies:** UI-04
**Outcome:** only event, explicit text severity, issuer, and effective/expiry header adopts task-oriented hierarchy; official wording and selection stay exact.
**Measure:** multi-alert fixture proves selected header order, non-color severity, and 48dp return target with PNG.
**Size:** 3/10.

### UI-26 — Alert detail: official body reachability

**Dependencies:** UI-25
**Outcome:** only affected area, official description, and instructions gain readable long-form composition without paraphrase or clipping.
**Measure:** long-alert RTL/font-2.0 fixture proves full text/logical scroll reachability and writes PNG.
**Out:** source action. **Size:** 3/10.

### UI-27 — Checkpoint I

**Dependencies:** UI-25, UI-26
**Outcome/measure:** apply UI-06 checkpoint contract to UI-25/UI-26 and reset count.

### UI-28 — Settings root navigation presentation

**Dependencies:** UI-04
**Outcome:** only the Settings route list gains task-oriented treatment; route order and Back remain unchanged.
**Measure:** route-list fixture proves all destinations/order and 48dp targets, with compact installed before/final PNGs.
**Size:** 2/10.

### UI-29 — Settings Appearance: theme and contrast rows

**Dependencies:** UI-28
**Outcome:** only Theme and Contrast rows gain selected/pending/saved/failure presentation; their persistence transactions do not change.
**Measure:** write-success/failure fixture proves row state and retry action, with screenshot.
**Out:** layout/effects rows. **Size:** 3/10.

### UI-30 — Checkpoint J

**Dependencies:** UI-28, UI-29
**Outcome/measure:** apply UI-06 checkpoint contract to UI-28/UI-29 and reset count.

### UI-31 — Settings Appearance: layout and effects rows

**Dependencies:** UI-29
**Outcome:** only Layout and existing Off/Subtle Effects rows gain the same treatment; confirmed state, retry, restoration, and disabled-animation semantics remain unchanged.
**Measure:** transaction assertions and Effects-Off/large-font PNG prove row state, targets, and semantic invariance.
**Size:** 3/10.

### UI-32 — Settings Units presentation

**Dependencies:** UI-28
**Outcome:** only the Units choice surface gains task-oriented treatment; unit conversion and confirmed-write behavior remain unchanged.
**Measure:** unit-choice fixture proves selected/saved/failure states and the existing immediate Home remap, with PNG.
**Size:** 3/10.

### UI-33 — Checkpoint K

**Dependencies:** UI-31, UI-32
**Outcome/measure:** apply UI-06 checkpoint contract to UI-31/UI-32 and reset count.

### UI-34 — Disclosure pages presentation

**Dependencies:** UI-28
**Outcome:** only shared Data Sources, Privacy, Licenses, and About text layout gains task-oriented treatment; supplied legal/attribution text and Back stay exact.
**Measure:** long-content fixture proves text order and font-2.0 reachability, with a disclosure PNG.
**Size:** 3/10.

### UI-35 — Location entry: search and results

**Dependencies:** UI-04
**Outcome:** only manual search input, loading, error, and result rows gain treatment; optional permission and geocoding do not change.
**Measure:** loading/error/results fixtures prove row order and 48dp selection targets; retain a no-location-grant installed PNG.
**Size:** 3/10.

### UI-36 — Checkpoint L

**Dependencies:** UI-34, UI-35
**Outcome/measure:** apply UI-06 checkpoint contract to UI-34/UI-35 and reset count.

### UI-37 — Location entry: saved locations and deletion

**Dependencies:** UI-35
**Outcome:** only saved rows, current marker, save action, removal confirmation, and empty state gain treatment; save/select/delete/return do not change.
**Measure:** saved/empty/delete-confirmation fixtures prove action results and target bounds with PNG.
**Size:** 3/10.

### UI-38 — Scenes: Subtle clear and cloud

**Dependencies:** UI-04
**Outcome:** only clear and cloudy conditions gain measured Subtle atmosphere; Effects Off remains opaque/static.
**Measure:** condition fixture proves scene selection and semantic invariance; retain clear/cloud Subtle and Effects-Off PNGs.
**Out:** wet/snow/storm, Full. **Size:** 3/10.

### UI-39 — Checkpoint M

**Dependencies:** UI-37, UI-38
**Outcome/measure:** apply UI-06 checkpoint contract to UI-37/UI-38 and reset count.

### UI-40 — Scenes: Subtle precipitation and storm

**Dependencies:** UI-38
**Outcome:** only rain, snow, and thunderstorm gain Subtle treatment; unknown is neutral and Effects Off stays complete/static.
**Measure:** representative fixture proves mapping and semantic invariance with Subtle/Off PNGs.
**Size:** 3/10.

### UI-41 — Full effects: selection and clear/cloud rendering

**Dependencies:** UI-31, UI-38
**Outcome:** existing Effects gains persisted Full plus clear/cloud richer rendering. Disabled animations resolve effective Off without changing saved Full.
**Measure:** storage/transaction and connected selection/restore/effective-Off assertions prove saved value, no refetch, and visual mode; retain installed PNGs.
**Out:** wet/snow/storm Full treatment. **Size:** 3/10.

### UI-42 — Checkpoint N

**Dependencies:** UI-40, UI-41
**Outcome/measure:** apply UI-06 checkpoint contract to UI-40/UI-41 and reset count.

### UI-43 — Full effects: precipitation and storm rendering

**Dependencies:** UI-40, UI-41
**Outcome:** only rain, snow, and storm gain established Full treatment; readability, weather meaning, and effective Off remain unchanged.
**Measure:** representative fixture proves mapping and semantic invariance with Full/Off PNGs.
**Size:** 3/10.

### UI-44 — UI evidence and documentation closure

**Dependencies:** all selected implementation slices and repairs
**Outcome:** evidence inventory reconciles accepted deltas, PNGs, test outputs, plans, roadmap, history, README, and authority status without rerunning valid journeys.
**Measure:** every verified slice has assertion result, artifact path, installed route evidence, and separate status/commit claim; git diff --check and source/status audit pass.
**Size:** 3/10; documentation only.

## Active selection

UI-06 is the next candidate. It remains specified until a new active plan selects it.

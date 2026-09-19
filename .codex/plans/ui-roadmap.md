# Oxygen Standard Now — Celestial Dial UI Overhaul Roadmap

**Status:** active roadmap; design selected; CD-00 through CD-08A verified;
CD-09 is the planned active checkpoint; CD-08B remains specified
**Roadmap ID:** `now-celestial-dial-v0.1`
**Selected design:** concept F, Celestial Dial
**Selected:** 2026-09-17 by user decision
**Concept board:**
`docs/assets/oxygen-now-celestial-dial-concepts-v0.1.png` (F, center-right)
**Visual authority:**
`docs/assets/oxygen-weather-visual-language-base-art-sheet-v0.2.png`
**Specification authority:** `docs/OXYGEN_FULL_SPECIFICATION.md`, then
`docs/OXYGEN_UI_SPECIFICATION.md`

## Target outcome

The installed Standard Now page becomes recognizably Celestial Dial:

- one dominant circular current-weather dial;
- a warm-gold condition mark, editorial temperature numeral, condition text,
  and apparent temperature at its center;
- high and low satellites above the dial;
- near-term precipitation and wind satellites below it;
- a locally rendered atmospheric field and halo for enabled effects;
- compact glass treatment for precipitation, alerts, and tertiary context;
- existing named page navigation and Home actions presented as coherent lower
  chrome.

The concept board is a composition reference, not a runtime bitmap and not a
source of weather values. Production UI remains Compose/vector/procedural and
uses only the installed provider-neutral presentation path.

## Progress and anti-slop contract

- A production slice counts as UI-overhaul progress only after its named visual
  delta is visible in the installed app on the real Standard Now route.
- Every production slice captures the same-route installed baseline and final
  PNG. A deterministic Compose case asserts the component's measurable
  geometry, content, semantics, and relevant action behavior.
- A screenshot is rejected when the named component is not obviously changed
  in a side-by-side inspection. Compilation, test tags, tests, documentation,
  or screenshot existence cannot substitute for the rendered delta.
- Checkpoints are test/documentation cycles. They do not count as visual
  implementation slices and may not claim that the overhaul advanced.
- No slice may consume more than 60% of a session context window. This roadmap
  caps estimates at 45%. Discovery, implementation, visual iteration, focused
  evidence, broad checks, review, and closure are included in the estimate.
  Split before coding if the selected work is reassessed above 60%.
- The installed target is 360x640dp, font scale 1.0, LTR, Oxygen theme,
  Standard layout. Each slice adds only the orthogonal conditions its component
  can affect.
- Use one emulator session per slice, one installation per APK change, and the
  minimum connected acceptance cases required by the changed boundary.

## Program invariants

- Preserve actual weather values, units, condition identity, selected location,
  source, freshness, provenance, and alert meaning.
- Preserve provider, repository, fallback, Room cache, persistence, refresh,
  retry, and request-count behavior.
- Preserve the Standard `Now -> Hourly -> Daily -> Details` pager, named actions,
  swipe ownership, and Android Back behavior.
- Keep Simple Now unchanged unless a later roadmap explicitly selects it.
- Keep weather marks and scenes decorative when adjacent text already conveys
  their meaning. Preserve the current spoken current-weather description.
- Never fabricate a satellite value. Missing high, low, precipitation, or wind
  uses the slice's explicit truthful absence layout.
- Effects Off is static, opaque, complete, and readable without a scene,
  gradient, transparency, blur, or animation.
- Paper, Terminal, high contrast, large text, RTL, and disabled animations
  remain functional throughout; their deliberate visual convergence is owned
  by later named slices below.
- The art sheet and concept board remain documentation assets only.

## Ordered slices

### CD-00 — Pre-overhaul inherited checkpoint

**Status:** verified; checkpoint closed after CD-00R repair
**Dependencies:** verified UI-04 and UI-05 production slices
**Estimate:** 30%
**Outcome:** Close the repository-wide two-production-slice checkpoint that
remained due when the former UI roadmap and UI-06 were retired. Reconcile the
retained UI-04/UI-05 evidence and rerun the five UI-04 Home Compose cases on
the post-UI-05 shared Home implementation. Reset the implementation count only
after fresh accepted results and documentation closure. This checkpoint does
not advance the visual-overhaul count.
**Failure rule:** Apply the repository's bounded three-attempt diagnostic rule.
Preserve every failure, require a concrete hypothesis and material diagnostic,
input, or environment change before another attempt, and stop as soon as the
cause is established. After three unresolved failures, select a separate repair
or investigation slice; do not weaken coverage or start CD-01.

The initial post-UI-05 execution stopped after the compact Standard Now case
reported an overlap. CD-00R established valid rendered bounds after an
explicit Compose-idle barrier and corrected the deterministic test boundary.
The complete five-case rerun passed with fresh runner/XML/log/textproto
agreement for every case. Documentation is closed and the implementation count
has reset to 0 of 2; CD-01 remains `specified`.

### CD-00R — Standard Now compact overlap repair investigation

**Status:** verified; test-boundary repair accepted
**Dependency:** CD-00's retained normal failure
**Estimate:** 35% of one session
**Surface:** existing Standard Now compact location/current boundary
**Outcome:** Reproduce and identify the exact rendered bounds that caused
`home-section-location` and `home-section-current` to overlap at 360x640dp,
font scale 1.0, LTR, Oxygen, Effects Off. Correct only the established source:
production layout when the rendered app overlaps, or the deterministic harness
when production geometry is valid and the fixture/assertion setup is wrong.
Do not weaken or remove the no-overlap contract.
**Acceptance:** The same named compact case completes once with fresh runner
XML/log/textproto evidence, 1/1 passing and zero failures/errors/skips. A
production layout correction also requires installed same-route before/final
PNG/XML evidence and the applicable focused and broad checks. A test-only
correction must retain evidence proving why production behavior was already
valid. After repair closure, return to CD-00 and rerun its complete five-case
set before resetting the count or selecting CD-01.
**Out:** Celestial Dial visuals, Hourly behavior, provider/repository/cache
paths, unrelated Home geometry, assertion weakening, and any CD-01 work.

### CD-01 — Central dial hero

**Status:** verified; committed
**Estimate:** 45% of one session
**Surface:** Standard Now current-condition hero
**Outcome:** Replace the current row/card hero with a code-native circular dial
containing the condition mark, current temperature, condition text, and apparent
temperature. Keep high/low in a temporary compact line below the dial until
CD-02. The first installed final must be unmistakably different from the
current rectangular hero.
**Acceptance:** At 360x640dp, Oxygen, Effects Off, the installed production Now
route shows one centered dial with all four current fields inside its bounds;
the dial, temporary range, location, lower status content, and footer do not
overlap. The existing current-weather spoken description remains one ordered
semantic unit. Missing current data renders an explicit unavailable dial state
without a fake temperature.
**Likely production scope:** `HomeLoadingScreen.kt`, a focused dial composable,
and reusable Now-only design roles promoted to `OxygenTheme.kt` when repeated.
**Out:** orbit satellites, atmosphere, lower glass panels, shared footer restyle,
Simple Now, provider or mapper changes.

### CD-02 — High and low orbit satellites

**Status:** verified; committed in `3500f1f`
**Dependencies:** CD-01
**Estimate:** 30%
**Surface:** upper dial constellation
**Outcome:** Move today's actual high and low from the temporary range line into
two stable circular satellites above the dial, matching concept F's upper arc.
**Acceptance:** Deterministic available, one-missing, and both-missing fixtures
prove correct labels/values, stable dial centering, no fabricated number, and no
sibling overlap at 360x640dp. The installed production final visibly shows the
upper constellation.
**Out:** precipitation/wind satellites, lower cards, atmosphere, range charts.

### CD-03 — Checkpoint A

**Status:** verified; committed in `ec49c5f`
**Dependencies:** CD-01, CD-02
**Estimate:** 15%
**Outcome:** Reconcile the two installed before/final pairs, focused connected
results, functional invariants, broad unit/build results, and status claims.
Recover any missing retained runner evidence before closing the reconciliation,
then reset the two-slice implementation count. This checkpoint does not advance
the visual-overhaul count.

CD-03A recovered the missing CD-01 focused XML, instrumentation log, textproto,
wrapper, and acceptance-marker bundles for the two accepted cases on one
API-37 `oxygen_starter` session. The bundles agree on 1/1 with zero failures,
errors, and skips; the retained CD-02 bundles and both slices' broad checks
remain consistent. The count is now 1 of 2; CD-04 is verified and CD-05 remains
`specified`.

### CD-04 — Precipitation and wind orbit satellites

**Status:** verified; committed in `17c7a60`
**Dependencies:** CD-02
**Estimate:** 45%
**Surface:** lower dial constellation and its typed presentation mapping
**Outcome:** Add available near-term forecast precipitation and current wind
satellites below the dial. Introduce only the typed presentation fields needed
to render those values; UI code must not parse display strings. Missing values
collapse to a balanced truthful layout rather than a fabricated zero or
placeholder weather; a reported zero remains visible.
**Acceptance:** Available, precipitation-missing, wind-missing, and both-missing
fixtures prove exact displayed values, text equivalents, stable geometry, and
unchanged canonical inputs. The installed final visibly completes the four-
satellite constellation without a provider request.
**Evidence:** mapper tests, two accepted API-37 `oxygen_starter` connected
bundles, and same-route cache-backed baseline/final PNG/XML are retained under
`.codex/test-artifacts/2026-09-18-cd-04-precipitation-wind-satellites/`.
**Out:** new provider/domain fields, charts, compass, humidity/pressure or other
Details metrics; CD-05 remains the next specified candidate and is not selected.

### CD-05 — Celestial halo and Now-local atmosphere

**Status:** verified; committed in `e8a24a1`
**Dependencies:** CD-01
**Estimate:** 45%
**Surface:** dial halo and Standard Now backdrop
**Outcome:** Add the concentric gold/cyan dial rings and a restrained,
condition-aware procedural atmospheric field behind Standard Now when effects
are enabled. Scope any new scene treatment so Hourly, Daily, Details, and Simple
do not silently change.
**Acceptance:** Installed Subtle and Effects-Off captures of the same route show
the intended atmospheric difference while preserving identical text, controls,
geometry, and semantics. Effects Off remains complete and opaque. Disabled
animations resolve to the Off result without changing the saved preference.
**Evidence:** Same-route Room-cache Chicago baseline/final PNG/XML, the
`WeatherSceneTest` profile matrix, two accepted API-37 `oxygen_starter`
connected bundles, app/core unit suites, Kotlin compilation, debug
assembly/install, and `git diff --check` are retained under
`.codex/test-artifacts/2026-09-18-cd-05-celestial-halo-now-atmosphere/`.
**Out:** downloaded imagery, continuous mandatory animation, Full-effects
expansion, other Home pages.

### CD-06 — Checkpoint B

**Status:** verified; test/documentation checkpoint closed
**Dependencies:** CD-04, CD-05
**Estimate:** 15%
**Outcome:** Apply the CD-03 checkpoint contract to CD-04/CD-05 and reset the
implementation count. It does not count as visual progress.

CD-06 reconciled the retained CD-04 and CD-05 installed baseline/final pairs,
focused and connected results, broad checks, functional limits, and commit
ancestry. Both production slices remain verified; the implementation count was
reset to 0 of 2. CD-07 was then selected, verified, and committed in `74e4b33`,
bringing the count to 1 of 2. CD-08A is verified and CD-09 is selected in
`.codex/plans/current.md`; CD-08B remains specified until after CD-09.

### CD-07 — Precipitation and provenance glass panel

**Status:** verified; committed in `74e4b33`
**Dependencies:** CD-04, CD-05
**Estimate:** 45%
**Surface:** first lower glass panel beneath the constellation
**Outcome:** Present the existing near-term precipitation aggregate as one
compact opaque GlassPanel on normal-font Standard Now. Typed probability and
amount fields drive the panel and lower satellite; the compatibility summary,
source/update context, spoken meaning, and provenance remain intact.
**Acceptance:** Verified at 360x640dp, font 1.0, LTR, Oxygen, Standard, Effects
Off. Mapper coverage proves canonical six-row aggregation, resolved units,
reported zero, probability-only, and absent truthfulness. Two accepted API-37
connected methods prove the named panel, ordered semantics, 150dp dial,
220x64dp lower constellation, existing spoken descriptions, context, controls,
and omission behavior. The installed selected Chicago/Open-Meteo route shows
the final panel with `Up to 10%` and `0.0 mm`; actual Details evidence retains
Open-Meteo fetched time, model-estimate type, and license provenance. Evidence
is retained under
`.codex/test-artifacts/2026-09-18-cd-07-precipitation-provenance-glass-panel/`.
**Out:** alert treatment, stale/error status, source navigation changes,
responsive/RTL/theme convergence, localization, TalkBack traversal, and
release checks.

### CD-08A — Non-active alert lookup glass panel

**Status:** verified; implementation remains uncommitted
**Dependencies:** CD-07
**Estimate:** 48%
**Surface:** second lower glass panel, non-active lookup states
**Outcome:** Render the existing no-alert, not-checked, unavailable,
unable-to-check, and delayed alert lookup outcomes as one opaque GlassPanel on
normal-font Standard Now. Preserve every existing string, time, selected
location, source/update context, action omission, and compact/large-font
branch. Active alerts remain on their current card/action presentation.
**Acceptance:** Installed selected-location no-alert evidence at 360x640dp,
font scale 1.0, LTR, Oxygen, Effects Off shows the named glass panel.
One deterministic normal-font case cycles all five typed non-active states and
proves exact text/times, ordered semantics, no actions/count, supporting-
viewport reachability, and unchanged fixed dial/lower-constellation geometry.
A focused large-font non-active regression and the existing active-alert action
case prove the preserved branches remain readable and functional. The
multi-line lookup body must remain vertically ordered inside GlassPanel; no
direct sibling placement may overlap its children.
**Out:** active-alert severity/icon redesign, alert persistence, notifications,
official wording changes, alert detail-screen redesign, responsive/RTL/theme
convergence, and release checks.

### CD-08B — Active alert glass panel and severity mark

**Status:** specified
**Dependencies:** CD-08A and CD-09
**Estimate:** 42%
**Surface:** active-alert lower glass panel
**Outcome:** Move the existing active-alert summary into the Celestial Dial
glass language and add a code-native severity mark plus structural severity
label. Preserve exact detail/source destinations, 48dp targets, attribution,
count, and existing alert-detail behavior; severity is never conveyed by
color alone.
**Acceptance:** Deterministic active-alert evidence proves the visible mark,
severity/event/issuer/source meaning, ordered semantics, unchanged actions and
URI, and non-overlap with the precipitation panel and context. An installed
active-alert route is optional and cannot be fabricated when live traffic is
unavailable.
**Out:** alert persistence, notifications, official wording changes, alert
detail-screen redesign, provider changes, and release checks.

### CD-09 — Checkpoint C

**Status:** planned; selected in `.codex/plans/current.md`; test/documentation only
**Dependencies:** CD-07, CD-08A
**Estimate:** 15%
**Outcome:** Apply the CD-03 checkpoint contract to CD-07/CD-08 and reset the
implementation count. It does not count as visual progress.

### CD-10 — Home footer and action chrome

**Status:** specified
**Dependencies:** CD-05
**Estimate:** 40%
**Surface:** shared page selector and Location/Refresh/Settings action row
**Outcome:** Align the lower Home chrome with concept F: restrained glass page
tabs, unmistakable cyan selected state, and code-native action marks with text
labels. Because this chrome is shared, all Standard pages retain identical
footer geometry during page movement.
**Acceptance:** Installed Now plus one deterministic all-page case proves no
footer jump, unchanged page selection/refresh/location/settings actions, 48dp
targets, named selected semantics, and no additional repository request.
**Out:** pager behavior, Back behavior, page content outside Now, new actions.

### CD-11 — Responsive dial composition

**Status:** specified
**Dependencies:** CD-04, CD-07, CD-08, CD-10
**Estimate:** 45%
**Surface:** Standard Now compact, large-font, and RTL layouts
**Outcome:** Replace ad-hoc shrinking with explicit Celestial Dial responsive
variants: full constellation at normal scale, compact constellation at font
1.3, and a readable linear/local-overflow arrangement at font 2.0. RTL mirrors
placement without changing high/low or forecast chronology meaning.
**Acceptance:** 360x640dp at font 1.0 and 1.3 needs no page-level scroll; font
2.0 RTL keeps dial meaning, alert actions, and footer reachable through the
allowed local overflow. Bounds prove no clipping, sibling overlap, unintended
horizontal scrolling, or sub-48dp controls.
**Out:** Simple layout, tablets, landscape, localization completion.

### CD-12 — Checkpoint D

**Status:** specified; test/documentation only
**Dependencies:** CD-10, CD-11
**Estimate:** 15%
**Outcome:** Apply the CD-03 checkpoint contract to CD-10/CD-11 and reset the
implementation count. It does not count as visual progress.

### CD-13 — Theme and high-contrast translation

**Status:** specified
**Dependencies:** CD-11
**Estimate:** 45%
**Surface:** Celestial Dial under Paper, Terminal, and High contrast
**Outcome:** Resolve the selected composition through shared semantic design
roles while preserving distinct theme identities: warm editorial Paper, crisp
technical Terminal, and structural high contrast. No card branches on theme id.
**Acceptance:** Deterministic Paper, Terminal, Oxygen-high, and Paper-high
captures show the same information hierarchy and geometry with distinct token
treatments. Weather marks, rings, text, selected state, and warning boundaries
remain discernible without relying on transparency or hue alone.
**Out:** new themes, theme plugins, downloaded fonts, settings persistence.

### CD-14 — Operational and unavailable state treatment

**Status:** specified
**Dependencies:** CD-07, CD-08, CD-13
**Estimate:** 45%
**Surface:** Now refresh, cached/stale, failed-refresh, missing-current, and empty
provider states
**Outcome:** Finish the selected visual language for existing operational states
without replacing useful weather or implying unavailable values. State copy
and retry behavior remain exact; status receives a distinct structural role
below or within the dial composition.
**Acceptance:** Deterministic state fixtures prove visible differentiation,
retained cached weather, explicit missing/empty treatment, unchanged retry, no
fabricated zeroes, and compact/large-font reachability. Retain final stale and
missing-current PNGs.
**Out:** new error semantics, provider changes, background work, notifications.

### CD-15 — Final Celestial Dial acceptance checkpoint

**Status:** specified; test/documentation only
**Dependencies:** CD-13, CD-14 and all earlier checkpoints
**Estimate:** 25%
**Outcome:** Reconcile every production slice and capture the integrated
installed Standard Now result on the real selected-location/provider/cache
path. Compare it side by side with the pre-overhaul baseline and concept F.
The checkpoint may declare the Standard Now Celestial Dial overhaul verified
only when the defining composition is plainly present; otherwise it records the
specific visual discrepancy and selects a bounded repair.
**Minimum final evidence:** Oxygen Subtle installed final; Oxygen Effects Off;
Paper; Terminal; one high-contrast state; font-1.3 compact; font-2.0 RTL local
overflow; no-alert production route; deterministic active-alert and stale
states; applicable unit/build checks; `git diff --check`.

## Selection state

CD-07 was selected by `.codex/plans/current.md` after verified CD-06 reset the
implementation count to 0 of 2 and is committed in `74e4b33`. The first
revised CD-08 draft exceeded the 60% context ceiling because it combined two
independent alert presentation boundaries. CD-08A is verified through the
normal-font layout repair and named preservation cases, with evidence under
`.codex/test-artifacts/2026-09-19-cd-08ar2-standard-now-layout-repair/`.
CD-09 is now selected in `.codex/plans/current.md`; CD-08B follows CD-09.

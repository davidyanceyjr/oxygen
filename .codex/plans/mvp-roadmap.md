# Oxygen MVP Release Map

Status: specified
Roadmap ID: mvp-2026-08
Source authority: `docs/OXYGEN_FULL_SPECIFICATION.md`
Created: 2026-08-18
Revised: 2026-09-10
Reconciled against local `origin/main` ref: `82cf281`
Synchronized through Slice 29B implementation `441d05d`, test follow-up
`86e696c`, and evidence sync `86f046b`

Repository audit cleanup and live-history compression were completed in commit
6cab109; the active queue remains unchanged.

Planning note: This roadmap specifies candidate MVP slices. Only `.codex/plans/current.md` may mark one bounded implementation slice as planned.

Completed and superseded slice contracts are retained in
`.codex/plans/archive/mvp-roadmap-through-2026-09-10-before-repo-audit-cleanup.md`.
This live file keeps the operating rules, current queue, and remaining candidate
contracts needed for normal discovery.

## Current Implementation Queue

Gate 30's first draft combined independent surfaces and Android conditions.
Implement these bounded candidates in order; 30A1 and 30A2 are committed,
30A3A1 evidence is complete, 30A3B2 is committed at `fb51f7b` as the
documentation sync, 30B1A1 is committed at `63ed25a` as the RTL semantic page
navigation contract, 30B1A2 is committed at `20b6ddc`, and 30B1A3A1 is
committed at `74675e2`. The former 30B1A3 draft is decomposed into
independently observable sub-slices; `30B1A3A2` is committed at `9390601`,
`30B1A3A3` is committed at `91974b2`, and `30B1A3B1` is the current candidate
selected by `.codex/plans/current.md`:

1. Slice 30A1 — Home Spoken-Weather Semantics
2. Slice 30A2 — Home Compact and Large-Font Resilience
3. Slice 30A3A1 — Home Speech/Layout Evidence
4. Slice 30A3B2 — Home Accessibility Evidence Document Sync
5. Slice 30B1A1 — RTL Semantic Page Navigation Contract
6. Slice 30B1A2 — RTL Directional Affordances and Gesture Behavior
7. Slice 30B1A3A1 — Standard Home RTL Hourly Chronology
8. Slice 30B1A3A2 — Standard Home RTL Daily Chronology
9. Slice 30B1A3A3 — Simple Home RTL Forecast Chronology
10. Slice 30B1A3B1 — RTL/LTR Spoken-Meaning Equivalence
11. Slice 30B1A4 — RTL Compact Layout and No-Refetch Evidence
12. Gate 30B1B1 — RTL Installed Evidence and Documentation Sync
13. Slice 30B2 — Home Reduced-Motion and Appearance Invariance
14. Gate 30B3 — Home Environment Evidence and Document Sync
15. Slice 30C1 — Official-Alert Summary Accessibility
16. Slice 30C2 — Official-Alert Detail Accessibility
17. Gate 30C3 — Alert Accessibility Evidence and Document Sync
18. Slice 30D1 — Appearance Control Semantics
19. Slice 30D2 — Appearance Layout and Environment Resilience
20. Gate 30D3 — Appearance Accessibility Evidence and Document Sync
21. Gate 30E — Installed TalkBack and Accessibility Closure

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

Status: committed at `1a8e14f`

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

### Slice 30A3A1: Home Speech/Layout Evidence

Status: evidence complete on 2026-09-10; documentation sync committed in 30A3B2

Prerequisites: Slices 30A1 and 30A2.

Mode: required third-cycle test-only evidence collection. Do not change
production behavior or authoritative documents in this slice.

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
- exact command results, environment, screenshots, hierarchies, skips, and
  blockers are retained under one cycle artifact directory.

A failing production boundary blocks this slice and creates a specifically
named Home repair slice; it is not fixed here.

### Slice 30A3B2: Home Accessibility Evidence Document Sync

Status: committed at `fb51f7b` on 2026-09-10

Prerequisite: Slice 30A3A1 with green focused and installed evidence.

Mode: documentation-only closure of the required third-cycle gate.

Must prove:

- append a self-contained history entry with exact evidence, artifacts, skips,
  blockers, and commit state;
- reconcile this plan, the roadmap queue, and the specification/README only
  where the retained evidence directly supports the claim;
- preserve explicit limits for TalkBack traversal, RTL, reduced motion,
  theme/contrast invariance, alerts, localization, and release readiness;
- Slice 30B1A1 was selected after the evidence and document diff review passed.

No Kotlin, Compose, provider, persistence, resource, manifest, dependency, or
production behavior changes are allowed. A missing or contradictory artifact
blocks closure and leaves 30A3B2 specified.

### Slices 30B1A1–30B1B1: Home RTL Navigation and Chronology

These replace the original combined 30B1 candidate. They preserve its release
intent while separating state semantics, direction affordances, chronological
meaning, compact-layout regression risk, and installed/documentation closure.

#### Slice 30B1A1: RTL Semantic Page Navigation Contract

Status: committed at `63ed25a`

Prerequisite: Slice 30A3B2.

Implement and test semantic page identity plus named previous/next custom
actions in RTL for Standard and Simple Home. The action meaning remains
chronological backward/forward movement, independent of physical left/right
direction. Test RTL through a Compose-local layout direction so historical
tests and the device configuration remain unchanged. Add behavior coverage
before any production correction; if the existing index-derived semantics pass,
this is deliberately a test-only implementation slice.

Focused boundary: exactly two named connected cases covering complete Standard
(`Now -> Hourly -> Daily -> Details`) and Simple (`Now -> Forecast`)
action progression, including exact first/intermediate/final action sets,
titles, positions, handled actions, and destinations. Production scope, only
after behavior-red evidence, is limited to `HomeLoadingScreen.kt`. Swipe
direction, visual mirroring, chronology, compact layout, request counts, and
installed evidence remain owned by 30B1A2–30B1B1.

The implementation/test commit passed its exact focused and broad checks. The
live cycle history records the evidence and limits. Review README and the
specification for conflict without claiming complete RTL support before Gate
30B1B1.

#### Slice 30B1A2: RTL Directional Affordances and Gesture Behavior

Status: committed at `20b6ddc`

Prerequisite: Slice 30B1A1.

This remains one bounded implementation slice: one existing Home pager path,
one Compose-local RTL condition, and two focused connected cases. Do not split
it into sub-slices or add a parallel navigation model.

Begin with red boundary assertions, then prove that visible controls and pager
swipes mirror for RTL while preserving the semantic action contract from
30B1A1. The logical page order remains Standard `Now -> Hourly -> Daily ->
Details` and Simple `Now -> Forecast`; the visible selector order is mirrored
to Standard `Details, Daily, Hourly, Now` and Simple `Forecast, Now` from left
to right. In RTL, a rightward swipe advances to the next semantic page and a
leftward swipe returns to the previous page. Boundary swipes do not overrun
the available pages. All exercised controls remain at least 48dp.

Limit production changes, only if a failing assertion requires them, to
`HomeLoadingScreen.kt`. No provider, domain, repository, cache, preference,
resource, manifest, dependency, new gesture framework, or accessibility
framework changes are allowed.

Focused boundary: exactly two named connected cases in
`HomeDashboardUiTest.kt`, `rtlStandardHomeDirectionalAffordancesMirrorAndGestures`
and `rtlSimpleHomeDirectionalAffordancesMirrorAndGestures`. They use the
existing deterministic fixture and Compose-local `LayoutDirection.Rtl`, assert
physical selector bounds, 48dp targets, exact semantic actions, settled page
titles/positions/selection, forward and reverse gestures, and first/final page
boundaries. No device RTL, installed journey, screenshot, chronology,
compact/refetch, or provider evidence is part of this slice. The exact
focused and broad checks passed; evidence and limits are recorded in the live
cycle history and cycle artifact directory.

Closure requires the focused two-case command, applicable compile/unit/assemble
checks, and `git diff --check`, with exact artifacts and limits recorded before
the roadmap/active-plan/history document sync. Review README and the
specification without claiming complete RTL or TalkBack coverage before Gate
30B1B1/30E.

#### Slice 30B1A3A1: Standard Home RTL Hourly Chronology

Status: committed at `74675e2`

Prerequisite: Slice 30B1A2.

Prove the Standard Home Hourly page preserves the logical earliest-to-latest
order and mapper-produced time labels under Compose-local RTL. The rendered
RTL sequence must equal the rendered LTR sequence for the same deterministic
fixture. Physical left-to-right placement is not the chronology oracle because
RTL mirroring is owned by 30B1A2. Use the production `HomeLoadingScreen`
composition. Do not sort rendered output in the assertion, rewrite provider
data, or change localized strings.

Focused boundary: one named connected case,
`rtlStandardHomeHourlyPreservesChronologicalRenderedOrder`, observing all six
rendered Hourly entries and their logical semantics/traversal order. Require
exact `6 AM` through `11 AM` labels, no omission/duplication, and the first
rendered payload `6 AM`, `Rain`, `64 deg F` plus the last payload `11 AM`,
`Rain showers`, `71 deg F`. Effects are Off and RTL is Compose-local;
no device-level RTL, installed journey, screenshot, TalkBack, or refetch
evidence is claimed.

Production scope, only after a red rendered boundary, is limited to the
smallest Hourly rendering correction in `HomeLoadingScreen.kt`. If the
baseline passes, this is a test-only implementation slice.

The focused connected case passed with exactly 1 completed, 0 skipped, and 0
failed on `oxygen_starter` / `emulator-5554` using Compose-local RTL. Debug
compile, app/core unit tests, debug assembly, and `git diff --check` passed.
No production correction was needed. Evidence is retained under
`.codex/test-artifacts/2026-09-10-slice-30b1a3a1-rtl-standard-hourly-chronology/`.
Device-wide RTL, installed/manual RTL, screenshots, TalkBack service
traversal, and later chronology/spoken-meaning boundaries remain out of scope.

#### Slice 30B1A3A2: Standard Home RTL Daily Chronology

Status: committed at `9390601`

Prerequisite: Slice 30B1A3A1.

Prove the Standard Home Daily page preserves the logical earliest-to-latest
order and mapper-produced date labels under Compose-local RTL. Physical
mirroring remains separate from chronology. Use the existing deterministic
fixture and production Home composition, and assert the six rendered dates
(`Sat, Aug 22` through `Thu, Aug 27`) in logical semantics/traversal order
without sorting the result or changing provider data/localization.

Focused boundary: one named connected case,
`rtlStandardHomeDailyPreservesChronologicalRenderedOrder`, with every Daily
entry present exactly once and first/last condition and low/high availability
still attached to the corresponding rendered rows. Effects are Off; no device
RTL, installed journey, screenshots, TalkBack, provider, or refetch evidence.
Any production correction is limited to the Daily rendering boundary in
`HomeLoadingScreen.kt` and is allowed only after a failing rendered assertion.

The focused connected case passed with exactly 1 completed, 0 skipped, and 0
failed on `oxygen_starter` / `emulator-5554` using Compose-local RTL. Debug
compile, app/core unit tests, debug assembly, and `git diff --check` passed.
No production correction was needed. Evidence is retained under
`.codex/test-artifacts/2026-09-10-slice-30b1a3a2-rtl-standard-daily-chronology/`.
Device-wide RTL, installed/manual RTL, screenshots, TalkBack service traversal,
and later chronology/spoken-meaning boundaries remain out of scope.

#### Slice 30B1A3A3: Simple Home RTL Forecast Chronology

Status: committed at `91974b2`

Prerequisite: Slice 30B1A3A2.

Prove the Simple Home Forecast page retains logical earliest-to-latest order
when the user selects either Hourly or Daily under Compose-local RTL. The
existing visible Hourly/Daily choice controls remain the selection mechanism;
their placement and gesture semantics are already covered by 30B1A2. The
selected Hourly view must expose `6 AM` through `11 AM`; the selected Daily view
must expose `Sat, Aug 22` through `Thu, Aug 27`, each in logical
semantics/traversal order and exactly once.

Focused boundary: one named connected case,
`rtlSimpleHomeForecastChoicesPreserveChronologicalRenderedOrder`, which
selects both choices and asserts the rendered labels and representative
condition/value association for each. It must not assert repository request
counts; that belongs to 30B1A4. Any production correction is limited to the
existing Simple Forecast/Hourly/Daily rendering boundary in
`HomeLoadingScreen.kt`, only after a red assertion.

The focused connected case passed with exactly 1 completed, 0 skipped, and 0
failed on `oxygen_starter` / `emulator-5554`, API 37, 420 dpi, font scale 1.0.
It exercised both Simple Forecast choices through the production Home
composition with Compose-local RTL, and retained exact rendered chronology,
first/last visible payloads, and mapper-owned descriptions. Debug compile,
app/core unit tests, debug assembly, and `git diff --check` passed. No
production correction was needed. Evidence is retained under
`.codex/test-artifacts/2026-09-10-slice-30b1a3a3-simple-rtl-forecast-chronology/`.
Device-wide or installed/manual RTL, screenshots, UI hierarchies, TalkBack
service traversal, spoken-meaning equivalence, compact/refetch, provider, and
release evidence remain out of scope.

#### Slice 30B1A3B1: RTL/LTR Spoken-Meaning Equivalence

Status: specified

Prerequisite: Slices 30B1A3A1–30B1A3A3.

Prove that RTL does not alter mapper-owned spoken weather meaning. Compare the
complete rendered Hourly and Daily item descriptions under Compose-local RTL
with the corresponding LTR rendering of the same deterministic fixture. Cover
Standard Hourly and Daily and both Simple Forecast choices through the
production Home composition. Compare exact content descriptions and visible
time/date labels; do not reconstruct descriptions from visible text and do not
test only mapper objects.

Focused boundary: at most two named connected cases,
`rtlStandardForecastSpokenMeaningMatchesLtr` and
`rtlSimpleForecastSpokenMeaningMatchesLtr`, with all six Hourly and all six
Daily items captured. Missing-value and precipitation semantics must remain
unchanged because the descriptions are compared, not regenerated. Any
production correction is limited to the rendered semantics boundary in
`HomeLoadingScreen.kt`, only after a red assertion; mapper, provider, and
localized resource changes are out of scope.

The four 30B1A3 sub-slices deliberately separate three chronology surfaces
from the cross-layout spoken-meaning contract. Each gets its own focused
evidence, artifact directory, result ledger, commit, and next-plan handoff.

#### Slice 30B1A4: RTL Compact Layout and No-Refetch Evidence

Status: specified

Prerequisite: Slices 30B1A3A1–30B1A3B1.

Prove the 360x640 dp, font-scale-1.3, long-location path retains usable page
controls, 48dp targets, no horizontal overlap, overflow reachability, and no
unrelated persistence or forecast refetch when changing page/layout in RTL.

Focused boundary: up to two named connected cases, using canonical request
counts and bounds/overlap assertions. Preserve the existing production
selected-location path.

#### Gate 30B1B1: RTL Installed Evidence and Documentation Sync

Status: specified

Prerequisites: Slices 30B1A1, 30B1A2, 30B1A3A1–30B1A3B1, and 30B1A4.

This is the documentation-only third-cycle closure gate. Run the minimum
combined focused filter and one installed RTL Home journey with screenshots and
UI hierarchies, restoring device direction afterward. Reconcile the active
plan, this roadmap, `README.md`, `docs/OXYGEN_FULL_SPECIFICATION.md`, and the
live cycle history only to the evidence actually retained. Record skipped
checks and limits. No Kotlin, Compose, provider, persistence, resource,
manifest, dependency, or production behavior changes.

All 30B1 implementation sub-slices remain out of scope for font-scale-2.0 work
owned by 30A2, reduced motion, theme/contrast matrix, alerts, Settings,
localization changes, provider/state changes, and TalkBack service traversal.
30B2 now depends on Gate 30B1B1.

### Slice 30B2: Home Reduced-Motion and Appearance Invariance

Status: specified

Prerequisites: Slice 30A3B2 and Gate 30B1B1.

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

Prerequisites: Gate 30B1B1 and Slice 30B2.

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
35. Slice 30A3A1 — Home Speech/Layout Evidence
36. Slice 30A3B2 — Home Accessibility Evidence Document Sync
37. Slice 30B1A1 — RTL Semantic Page Navigation Contract
38. Slice 30B1A2 — RTL Directional Affordances and Gesture Behavior
39. Slice 30B1A3A1 — Standard Home RTL Hourly Chronology
40. Slice 30B1A3A2 — Standard Home RTL Daily Chronology
41. Slice 30B1A3A3 — Simple Home RTL Forecast Chronology
42. Slice 30B1A3B1 — RTL/LTR Spoken-Meaning Equivalence
43. Slice 30B1A4 — RTL Compact Layout and No-Refetch Evidence
44. Gate 30B1B1 — RTL Installed Evidence and Documentation Sync
45. Slice 30B2 — Home Reduced-Motion and Appearance Invariance
46. Gate 30B3 — Home Environment Evidence and Document Sync
47. Slice 30C1 — Official-Alert Summary Accessibility
48. Slice 30C2 — Official-Alert Detail Accessibility
49. Gate 30C3 — Alert Accessibility Evidence and Document Sync
50. Slice 30D1 — Appearance Control Semantics
51. Slice 30D2 — Appearance Layout and Environment Resilience
52. Gate 30D3 — Appearance Accessibility Evidence and Document Sync
53. Gate 30E — Installed TalkBack and Accessibility Closure
51. Slice 33A — Dependency and Manifest Privacy Audit
52. Slice 33B — Provider Disclosure and Local Data Privacy Audit
53. Gate 34A — Settings and About Release Check
54. Gate 34B — Data-Source Release Check
55. Gate 35A — MVP Core Behavior Verification
56. Gate 35B — MVP Presentation and Accessibility Verification
57. Gate 35C — Release Candidate Decision

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
is committed at `1a8e14f`; 30A3A1's evidence is complete; and 30A3B2 is the
committed documentation-sync boundary at `fb51f7b`. Slice 30B1A1 is committed
at `63ed25a`; Slice 30B1A2 is committed at `20b6ddc`; and Slice 30B1A3A1 is
committed at `74675e2`. Slice 30B1A3A2 is the current candidate in
`.codex/plans/current.md`. It is the next bounded
slice of the split Gate 30
accessibility boundary. Slice 29B is committed at `441d05d` with test
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
-> Slice 30A2 Home compact/large-font resilience committed at `1a8e14f`
-> Slice 30A3A1 Home speech/layout evidence verified on 2026-09-10
-> Slice 30A3B2 Home accessibility evidence document sync committed at `fb51f7b`
-> Slice 30B1A1 RTL semantic page navigation contract committed at `63ed25a`
-> Slice 30B1A2 RTL directional affordances and gesture behavior committed at `20b6ddc`
-> Slice 30B1A3A1 Standard Home RTL Hourly chronology committed at `74675e2`
-> Slice 30B1A3A2 Standard Home RTL Daily chronology is the active planned slice
```

Gate 25, Slice 27A, committed 27B1/27B2, Slice 28B1, Slice 28B2, Slice 29A,
Slice 29B, Slice 30A1, Slice 30A2, and the 30A3B2 documentation sync are
complete. Gate 30 is split into the bounded 30A1–30E queue at the head of this
roadmap; 30A3A1 evidence is complete, Slices 30B1A1, 30B1A2, and 30B1A3A1
are committed, and Slice 30B1A3A2 is the active planned candidate after
30A3B2 closure. Release
work and release-candidate claims remain outside this boundary.

Do not reopen 18F, insert new 18F.x slices, or create a new pre-18G visual gate.
Those implementation boundaries are historical and already committed. Slice
18J-R was a provider-path recovery slice required by the blocked Slice 18J
evidence boundary, not a new visual gate.

Do not treat later roadmap entries as active implementation work until a new
bounded plan selects one.

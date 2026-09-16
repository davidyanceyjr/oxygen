# Oxygen UI Specification — Version 0.3

**Status:** specified; roadmap-readiness correction committed; not
implemented or visually verified
**Mode:** documentation-only UI contract and authority alignment
**Cycle ID:** `2026-09-16-ui-specification-review-resolution`
**Active roadmap:** none during specification finalization. The intended next
roadmap is `ui-roadmap.md` (not yet created); `mvp-roadmap.md` is inactive.
**Implementation slices since UI checkpoint:** 0; this documentation cycle
does not increment the count.
**Baseline:** committed application state through `5c6ed6b`; UI Specification
v0.3 is committed in `aafefe6` with post-commit sync at `c2d28a4`; Gate 30E
remains deferred and unverified.
**Next action:** derive `.codex/plans/ui-roadmap.md` from
[`OXYGEN_UI_SPECIFICATION.md`](../../docs/OXYGEN_UI_SPECIFICATION.md), beginning
with the bounded forecast-horizon data contract.

## Selected work

Finalize the UI specification with the minimum changes directed by its
review/audit section:

- align the higher-level product authority to a rolling 72-hour hourly and
  ten-day daily target;
- define truthful Open-Meteo and MET Norway horizon behavior without padding,
  interpolation, or implementation claims;
- make the exact Standard Home page/window, gesture, Back, card, appearance,
  availability, accessibility, and evidence contracts internally consistent;
- distinguish decisions required now from visual details delegated to later
  bounded implementation slices;
- keep global page names visible while making numeric page count and position
  semantic-only rather than rendered page or card content;
- clarify slice sizing, proportional evidence, checkpoint counting, commit
  status, and documentation closure in `AGENTS.md`; leave implementation
  sequencing to the future UI roadmap.

## Acceptance boundary

This documentation cycle is ready when the UI specification and affected
higher-level/provider authorities agree on forecast horizons and navigation,
the fallback contract handles sparse/partial results truthfully, and the first
implementation boundary is unambiguous. Named pages must remain visually clear
without rendering numeric page ordinals, while accessibility semantics retain
page identity, count, and position. It must not claim that the forecast horizon,
Home redesign, theme architecture, Back behavior, or any new rendered result is
implemented or verified.

The slice rules must support independently observable prerequisites without
scaffolding-only completion, include necessary verification in size estimates,
and define a checkpoint after two production-changing slices. Commit identity
must not imply verified behavior. This is a bounded documentation correction;
no implementation slice is selected.

## Intended tracked files

- `AGENTS.md`
- `docs/OXYGEN_UI_SPECIFICATION.md`
- `.codex/plans/current.md`
- `.codex/cycles/history.md` after review

Product/provider authority alignment remains in the committed v0.3 baseline.

## Evidence and checks

Retained evidence for the committed v0.3 baseline:

- Read the repository authorities, UI workflow, active roadmap sections, and
  recent cycle state.
- Inspected Base Art Sheet v0.2.
- Checked the installed request/presentation limits and provider-neutral
  mapping/cache paths without changing production code.
- Rechecked official Open-Meteo forecast-hour parameters and MET Norway
  short-/medium-range timestep behavior.
- `git diff --check`, the untracked-new-file whitespace check, and the focused
  terminology/authority search passed after the documentation edits.

Current correction: reviewed `AGENTS.md`, UI specification sections 0.1, 0.2,
0.7, 1, 7, 9, and 10, and this plan for consistent authority, sizing, evidence,
cadence, and status wording. `git diff --check`, focused `rg` terminology review,
and full documentation diff review passed, including the semantic-only
page-ordinal correction. Recheck whitespace before commit.

No Android build, unit test, connected test, emulator install, or screenshot
acceptance is required because production and test source are unchanged.

## Out of scope

- implementation-roadmap sequencing beyond naming the first bounded contract;
- Kotlin, Compose, resource, test, theme-token, or asset changes;
- installed forecast-horizon, Home, theme, effects, card, or Back behavior;
- final token values, typefaces, component rendering, or screenshot matrices;
- release, MVP, or Gate 30E status changes;

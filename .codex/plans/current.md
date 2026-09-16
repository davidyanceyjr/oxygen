# Oxygen UI Specification — Version 0.3

**Status:** specified; documentation committed in `aafefe6`; not implemented
or visually verified
**Mode:** documentation-only UI contract and authority alignment
**Cycle ID:** `2026-09-16-ui-specification-review-resolution`
**Baseline:** committed application state through `5c6ed6b`; this documentation
cycle is committed in `aafefe6`; Gate 30E remains deferred and unverified.
**Next action:** derive an ordered implementation roadmap from
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
  bounded implementation slices.

## Acceptance boundary

This documentation cycle is ready when the UI specification and affected
higher-level/provider authorities agree on forecast horizons and navigation,
the fallback contract handles sparse/partial results truthfully, and the first
implementation boundary is unambiguous. It must not claim that the forecast
horizon, Home redesign, theme architecture, Back behavior, or any new rendered
result is implemented or verified.

## Intended tracked files

- `docs/OXYGEN_UI_SPECIFICATION.md`
- `docs/OXYGEN_FULL_SPECIFICATION.md`
- `docs/data-sources/OPEN_METEO_FORECAST.md`
- `docs/data-sources/MET_NORWAY_FORECAST.md`
- `.codex/plans/current.md`
- `.codex/cycles/history.md` after review

## Evidence and checks

- Read the repository authorities, UI workflow, active roadmap sections, and
  recent cycle state.
- Inspected Base Art Sheet v0.2.
- Checked the installed request/presentation limits and provider-neutral
  mapping/cache paths without changing production code.
- Rechecked official Open-Meteo forecast-hour parameters and MET Norway
  short-/medium-range timestep behavior.
- `git diff --check`, the untracked-new-file whitespace check, and the focused
  terminology/authority search passed after the documentation edits.

No Android build, unit test, connected test, emulator install, or screenshot
acceptance is required because production and test source are unchanged.

## Out of scope

- implementation-roadmap sequencing beyond naming the first bounded contract;
- Kotlin, Compose, resource, test, theme-token, or asset changes;
- installed forecast-horizon, Home, theme, effects, card, or Back behavior;
- final token values, typefaces, component rendering, or screenshot matrices;
- release, MVP, or Gate 30E status changes;

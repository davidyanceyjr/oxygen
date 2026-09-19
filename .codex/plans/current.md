# Oxygen Standard Now — CD-07 Precipitation and Provenance Glass Panel

**Status:** verified; committed in `74e4b33`
**Cycle ID:** `2026-09-18-cd-07-precipitation-provenance-glass-panel`
**Active roadmap:** `.codex/plans/ui-roadmap.md`
**Implementation-slice count:** 1 of 2 since the CD-06 checkpoint reset

**Next action:** no additional product slice is selected. CD-08 remains the
next specified roadmap candidate until a new bounded slice is selected here.

## Completed behavior

Normal-font Standard Now presents the existing six-hour precipitation
aggregate as one opaque `GlassPanel` below the fixed lower constellation. The
mapper exposes `HomeNearTermPrecipitationPresentation` with canonical
millimetres, maximum probability, compact typed probability/amount text, the
existing summary, and the existing spoken text. The lower satellite derives
from that same value; the compatibility `precipitationSummary` projection
remains. Reported zero and probability-only data stay visible; wholly absent
data omits the panel.

Simple, large-font, current-weather and satellite spoken descriptions,
source/update context, provenance, tabs, actions, request behavior, provider,
cache, persistence, navigation, and core models are unchanged.

## Acceptance and evidence

- Focused mapper coverage passed for canonical six-row aggregation, resolved
  units, reported zero, probability-only, absent data, satellite projection,
  summary, and spoken text.
- `standardNowPrecipitationGlassPanelUsesTypedAggregateAndRetainsContext`
  passed 1/1 on API-37 `oxygen_starter`; it proves ordered panel semantics,
  no action, 150dp dial, 220x64dp lower constellation, spoken descriptions,
  source/update text, provenance reachability, controls, and non-overlap.
- `standardNowPrecipitationGlassPanelOmitsOnlyAbsentAggregate` passed 1/1 on
  the same emulator; it proves zero, probability-only, and absent behavior.
- The installed selected Chicago/Open-Meteo route visibly shows the panel with
  `Up to 10%` and `0.0 mm`; Details visibly retains Open-Meteo, fetched time,
  model-estimate type, and license provenance.
- Evidence root and verification ledger:
  `.codex/test-artifacts/2026-09-18-cd-07-precipitation-provenance-glass-panel/`
- Broad checks passed: app/core unit suites, app Kotlin compilation, debug
  assembly, and `git diff --check`.

## Limits

This slice does not cover alerts, stale/error treatment, source navigation,
responsive/RTL/theme convergence, localization, TalkBack service traversal,
Simple/large-font visual convergence, provider/repository/cache changes, or
release checks. The first available connected attempt timed out without a
result bundle; a later test-harness assertion issue was repaired, and the
final rerun passed. Both dispositions are recorded in the ledger.

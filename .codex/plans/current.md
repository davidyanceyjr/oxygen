# Oxygen Standard Now — CD-03 Checkpoint A

**Status:** verified; ready for commit
**Cycle ID:** `2026-09-18-cd-03-checkpoint-a`
**Active roadmap:** `.codex/plans/ui-roadmap.md`
**Implementation-slice count:** 0 of 2 since CD-00; CD-03A recovered the missing CD-01 evidence

## Selected behavior and acceptance boundary

Reconcile the verified CD-01 and CD-02 work at the repository and evidence
boundaries without changing production behavior. Confirm that both installed
same-route before/final pairs visibly show their named deltas, that the focused
connected results and retained broad checks agree, and that the documented
functional invariants and scope limits remain accurate.

Acceptance is complete:

- CD-01 and CD-02 baseline/final PNG/XML pairs are present and attributable to
  the Standard Now route at 360x640dp, Oxygen, Standard, Effects Off;
- visual inspection shows the CD-01 dial delta and CD-02 high/low satellite
  delta, with retained current semantics, source/update context, footer, and
  no documented invariant regression;
- the recovered CD-01 and retained CD-02 focused connected cases each have
  fresh, runner-backed 1/1 results with zero failures, errors, and skips;
- the retained CD-01 and CD-02 broad logs show the applicable compile, unit,
  assembly, and diff checks passed;
- `.codex/plans/current.md`, `.codex/plans/ui-roadmap.md`, `README.md`, and
  `.codex/cycles/history.md` use status claims supported by the reconciled
  evidence; and
- the implementation-slice count is reset from 2 of 2 to 0 of 2, with CD-04
  and CD-05 remaining `specified` until a later production slice is selected.

## Evidence and verification

- CD-01 installed pair: `.codex/test-artifacts/2026-09-17-cd-01-central-dial/`;
  CD-02 installed pair:
  `.codex/test-artifacts/2026-09-17-cd-02-high-low-satellites/`.
- CD-01 focused recovery ledger and accepted bundles:
  `.codex/test-artifacts/2026-09-18-cd-03a-cd-01-evidence-recovery/`.
  `standardNowCompactHierarchyUsesFixedHeroAndNonOverflowingSupport` and
  `standardNowAlertLookupOutcomesAreTruthfulAndActionFree` each completed 1/1
  with matching XML, instrumentation log, textproto, wrapper status 0, and
  acceptance marker.
- CD-02 retained focused bundles and ledger:
  `.codex/test-artifacts/2026-09-17-cd-02-high-low-satellites/`.
- Retained broad checks for both production slices passed compile, app/core
  unit tests, debug assembly, and `git diff --check`.
- Commits `3cf07b9`, `3500f1f`, and `54c53a6` are in the current ancestry; no
  app or core source changed during CD-03A recovery.
- The recovery used one API-37 `oxygen_starter` session, stopped after both
  cases, and did not change production or test source.

## Limits and next action

README and the full/UI specifications remain accurate without edits. No
provider, repository, cache, persistence, mapper, navigation, theme, Simple
layout, compact/large-font responsive behavior, RTL/theme translation,
precipitation/wind satellite, atmosphere, TalkBack, or release behavior was
added or newly verified.

Next: select a later specified production slice, such as CD-04 or CD-05, only
after this documentation checkpoint is committed.

Sizing rationale: CD-03 used the planned 15% checkpoint budget; the missing
CD-01 result bundle required one bounded 10% evidence-recovery exercise and no
independent production concern.

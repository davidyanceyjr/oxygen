# Oxygen Home UI v0.3 Roadmap — Reviewed Draft

**Status:** specified; reviewed UI roadmap draft created; no implementation
slice selected
**Mode:** documentation-only roadmap review and sequencing
**Cycle ID:** `2026-09-16-ui-roadmap-first-draft`
**Active roadmap:** `.codex/plans/ui-roadmap.md` (reviewed draft; candidates
remain specified until selected here)
**Implementation slices since UI checkpoint:** 0; this documentation cycle
does not increment the count.
**Baseline:** UI Specification v0.3 and the committed installed-app baseline;
this cycle changes roadmap/tracking documentation only and does not implement
or verify a roadmap delta.
**Next action:** accept the reviewed roadmap, then select UI-01 here with exact
fixtures, production-repository Android exercise, verification budget, and
timeout limit before implementation.

## Selected work

Revise the first ordered candidate roadmap after audit so it names deltas from
the installed baseline, removes overlapping/catch-all slices, assigns omitted
UI obligations, bounds evidence cost, and preserves the full product
specification as higher authority.

## Acceptance boundary

`.codex/plans/ui-roadmap.md` must identify bounded, non-overlapping candidate
deltas; distinguish the existing verified UI baseline from new work; distribute
page-specific state/accessibility obligations into their owning slices; split
navigation, forecast windows, marks, scenes, and supporting surfaces at
meaningful acceptance boundaries; schedule every required two-slice checkpoint;
and define minimal functional-plus-rendered evidence without cross-product
matrices. It must state that this is not the complete 1.0 roadmap and recommend
UI-01 without marking it `planned`.

## Intended tracked files

- `.codex/plans/ui-roadmap.md`
- `.codex/plans/current.md`
- `.codex/cycles/history.md`

## Evidence and checks

- Reviewed the UI Specification, full product specification, UI workflow,
  README, provider template, recent cycle history, and current app/core
  production boundaries.
- Audited the first draft for specification gaps, blockers, existing-behavior
  restatement, LLM slop, and verification/token waste, then revised its scope,
  sequence, acceptance boundaries, and evidence policy.
- `git diff --check`, explicit trailing-whitespace review for the untracked
  roadmap, and focused roadmap terminology/dependency/checkpoint review passed.
- No Android build, unit test, connected test, emulator, install, or screenshot
  checks are applicable: this cycle changes documentation and planning files
  only, so no UI behavior is implemented or verified.

## Out of scope

- selecting or implementing UI-01 or any later candidate;
- Kotlin, Compose, resource, provider, repository, cache, test, or asset changes;
- changing the UI Specification, full product authority, or inactive MVP roadmap;
- complete 1.0 scope, release, MVP, Gate 30E, or visual acceptance claims.

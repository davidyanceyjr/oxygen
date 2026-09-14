# Gate 30D3 — Appearance Accessibility Evidence and Documentation Sync

**Status:** committed
**Cycle ID:** `2026-09-14-gate-30d3-appearance-accessibility-evidence-doc-sync`
**Prerequisite:** Slice 30D2, committed at `2955aa5`

## Selected behavior

Reconcile and publish the evidence-supported accessibility status of the
production Settings / Appearance path after Slice 30D2. This is a
documentation-only closure gate. It does not add behavior, tests, UI, or
platform integration.

## Acceptance boundary

The gate is ready when:

1. The retained 30D1 and 30D2 artifacts and cycle entries are audited against
   the committed production and test changes.
2. README, `docs/OXYGEN_FULL_SPECIFICATION.md`,
   `.codex/plans/mvp-roadmap.md`, this plan, and
   `.codex/cycles/history.md` agree on the committed Appearance semantics and
   layout/environment evidence.
3. Documentation distinguishes deterministic Compose/Android semantics and
   installed evidence from unverified TalkBack service traversal, localization,
   automatic contrast, storage-failure injection, numeric installed request
   counts, and release readiness.
4. No claim is upgraded beyond evidence actually retained for 30D1 and 30D2,
   and Gate 30E remains the next candidate.

## Evidence to audit

Review, without rerunning unchanged checks:

- `.codex/test-artifacts/2026-09-13-slice-30d1-appearance-control-semantics/`
- `.codex/test-artifacts/2026-09-13-slice-30d2-appearance-layout-environment-resilience/`
- the 30D1 and 30D2 entries in `.codex/cycles/history.md`
- commits `78ecb84` and `2955aa5`, including their changed-file boundaries

Confirm the six accepted 30D2 connected cases, the five accepted 30D1 cases,
the API-37 emulator identity, installed LTR/font-scale/RTL/reduced-motion and
theme/contrast evidence, and all recorded limitations.

## Intended files

- `README.md` — correct only evidence-supported maturity/accessibility claims.
- `docs/OXYGEN_FULL_SPECIFICATION.md` — synchronize Appearance evidence and
  remaining accessibility limits if stale.
- `.codex/plans/mvp-roadmap.md` — mark 30D3 committed only after this gate's
  documentation commit; leave Gate 30E as the next specified candidate.
- `.codex/plans/current.md` — retain this plan until closure, then record the
  final commit and evidence.
- `.codex/cycles/history.md` — append one self-contained closure entry.

Do not modify production code, tests, provider behavior, preference/state
models, artifacts, or the unrelated untracked archive.

## Workflow and checks

Use the documentation-only workflow:

`discover -> contract/document -> review -> ready`

Planned checks:

- inspect retained artifacts, commit diffs, and current documentation;
- review the Markdown diff for factual status, scope, and limit consistency;
- run `git diff --check` after editing;
- run `git status --short` and preserve unrelated changes.

Android compilation, unit tests, connected tests, emulator startup/install, and
assembly are intentionally skipped because this gate changes Markdown only and
relies on unchanged retained evidence. If the audit finds an evidence
contradiction or production defect, stop the documentation gate and create a
separately named repair slice rather than rewriting the claim.

## Completion record

Documentation commit: to be filled after the documentation commit.

Changed files: `README.md`, `docs/OXYGEN_FULL_SPECIFICATION.md`,
`.codex/plans/mvp-roadmap.md`, `.codex/plans/current.md`, and
`.codex/cycles/history.md`.

Retained evidence audited: `.codex/test-artifacts/2026-09-13-slice-30d1-appearance-control-semantics/`
and `.codex/test-artifacts/2026-09-13-slice-30d2-appearance-layout-environment-resilience/`.
Five D1 and six D2 accepted connected cases, API-37 `oxygen_starter` identity,
and installed LTR/font-scale-2.0/RTL/Effects-Off/Terminal-High evidence are
reconciled. TalkBack service traversal, localization, automatic contrast,
injected storage failures, numeric installed request counts, and release
checks remain unverified. Gate 30E remains out of scope and is the next
specified roadmap candidate.

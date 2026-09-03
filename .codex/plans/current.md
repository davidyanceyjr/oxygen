# Active Cycle

Status: ready
Cycle ID: 2026-09-03-art-sheet-version-doc-cleanup
Mode: documentation-only
Goal: Resolve the active documentation drift where the tracked visual-language
asset path and specification referenced Base Art Sheet v0.1 while the visible
source image title used v0.2.
Roadmap context: Slice 18J is committed and Slice 19A remains the next
implementation candidate. This cleanup is documentation and asset-governance
only; it does not reopen any Standard Home visual slice.

## Contract

Selected Behavior:
- The reviewable Base Art Sheet source artifact is named v0.2 to match the
  visible title in the image.
- Active specification and roadmap references point to the v0.2 artifact and
  no longer report unresolved art-sheet version drift.
- Slice 19A remains the next implementation candidate.

Acceptance Boundary:
- `docs/OXYGEN_FULL_SPECIFICATION.md` references
  `docs/assets/oxygen-weather-visual-language-base-art-sheet-v0.2.png` and
  names Base Art Sheet v0.2.
- `.codex/plans/mvp-roadmap.md` references the v0.2 artifact and records the
  naming cleanup as resolved.
- `.codex/cycles/history.md` recent state summary no longer lists active
  art-sheet documentation drift.
- `git diff --check` passes.

Out of Scope:
- Kotlin, Compose, Gradle, manifest, provider, cache, selected-location,
  saved-location, unit preference, alert, appearance setting, release, or MVP
  behavior changes.
- Reopening or re-verifying Slice 18G, Slice 18H, Slice 18I, or Slice 18J.
- Rewriting historical cycle entries that accurately described the old drift
  at the time they were written.

## Phase Results

- specified: The recent cycle summary identified active documentation drift:
  the tracked art-sheet path/spec referenced v0.1 while the visible image title
  used v0.2.
- planned: This cycle is limited to the asset filename/reference cleanup and
  status record.
- covered: `git diff --check` is the applicable documentation-only formatting
  check.
- implemented: The art-sheet asset was renamed to v0.2, active spec/roadmap
  references were updated, and the live recent history summary now reports no
  active drift.
- verified: `git diff --check` passed.
- committed: not committed in this turn.
- artifacts target:
  `.codex/test-artifacts/2026-09-03-art-sheet-version-doc-cleanup/`.
- evidence:
  `.codex/test-artifacts/2026-09-03-art-sheet-version-doc-cleanup/git-diff-check.log`.
- deferred: Slice 19A remains the next implementation candidate.

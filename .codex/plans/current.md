# Slice 27A — Simple Layout Definition

**Status:** planned
**Cycle ID:** `2026-09-07-slice-27a-simple-layout-definition`
**Planning basis:** local `main` at `98a26d3`; Slice 26 implementation at
`c7b578a`; Gate 25 implementation at `23a9d49`.

## Selected behavior and stopping boundary

Define the MVP Simple layout as a distinct layout contract before it becomes
selectable. Simple must keep required weather meaning, source/stale/alert
reachability, and logical page semantics. It must not be Standard with
arbitrary content removed.

Stop after the Simple layout definition and its direct presentation tests are
in place. Do not add persisted layout selection, theme work, effects work,
high contrast, custom units, provider changes, or a broader Home redesign in
this slice.

## Authorities and dependency check

Read/reconcile `AGENTS.md`, `README.md`,
`docs/OXYGEN_FULL_SPECIFICATION.md`,
`docs/data-sources/PROVIDER_TEMPLATE.md`,
`.codex/plans/mvp-roadmap.md`,
and the relevant `app`/`core` build files before implementation.

Current dependency facts:

- Slice 26 is committed at `c7b578a`; the effects baseline is not the next
  active slice.
- Slice 27A is the roadmap candidate for layout definition.
- Simple layout must remain independent from effects, theme, and layout
  persistence.

## Planned evidence

- Focused app or Compose tests that encode the Simple layout contract.
- One connected UI check only if the layout boundary requires it.
- Broad compile/test checks limited to the changed code path after
  implementation.

## Out of scope

- Persisted layout selection.
- Theme translation/selection.
- Effects changes.
- High contrast.
- Custom units.
- Provider changes or data-source changes.
- Home redesign beyond the Simple layout contract.

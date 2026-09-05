# Active Cycle

Status: ready
Cycle ID: 2026-09-04-post-20-0-authority-sync
Mode: documentation-only
Slice: Post-20-0 Authority Sync
Commit: not committed in this changeset

## Objective

Record completed Gate 20-0 evidence in the active authorities and advance the
next implementation candidate to Slice 20B: Unit Conversion Presentation
Boundary.

## Scope

Update only:

- `docs/OXYGEN_FULL_SPECIFICATION.md`;
- `.codex/plans/mvp-roadmap.md`;
- `.codex/plans/current.md`;
- `.codex/cycles/history.md`.

## Contract

- Gate 20-0 is documented as committed at `587b0ad`.
- Slice 20B is the next bounded implementation candidate.
- Slice 20B remains limited to converting canonical weather values for Home
  presentation.
- Persisted unit preferences, Settings unit controls, provider request unit
  changes, Room/DataStore/cache schema changes, and MVP/release readiness stay
  out of scope.
- No Kotlin, Compose, Gradle, manifest, provider, persistence, cache, or
  installed-app behavior changes are included.

## Acceptance Evidence

- Stale next-candidate references to Gate 20-0 are removed from the active
  specification, roadmap startup guidance, active plan, and recent history.
- `git diff --check` passes.

## Verification

- Targeted stale next-candidate search returned no matches in the active
  specification, roadmap, current plan, or live recent history.
- `git diff --check` passed.
- Artifact:
  `.codex/test-artifacts/2026-09-04-post-20-0-authority-sync/git-diff-check.log`.

Android compile, unit, connected, assemble, emulator, install, and screenshot
commands were not run because this is a Markdown-only authority sync.

## Out of Scope

Unit conversion implementation, conversion formulas, persisted unit preference
storage, Settings/Home controls, provider request units, Room schema, DataStore
format, forecast-cache format, saved-location behavior, installed-app runtime
behavior, alert lookup, air quality, radar/maps, appearance settings, widgets,
notifications, release readiness, and MVP-readiness claims.

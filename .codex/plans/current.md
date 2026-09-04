# Active Cycle

Status: ready
Cycle ID: 2026-09-04-gate-19f-saved-locations-doc-sync
Mode: documentation-only
Slice: Gate 19F, Saved Locations Documentation Sync
Commit: uncommitted

Goal: Align README, specification, roadmap, disclosure/status history, and
active-cycle state with the saved-location behavior verified and committed
through Slice 19E without changing app behavior or making MVP/release-readiness
claims.

Basis:
- Slice 19E, Remove Saved Location UI, is committed at `00cb88a`.
- `.codex/plans/mvp-roadmap.md` defines Gate 19F after Slice 19E to align
  saved-location documentation and status surfaces.
- README already lists saved-location removal UI as implemented and no longer
  lists it as not implemented.
- `AboutDisclosureStateHolderTest` asserts the stale
  `saved-location save/remove UI` phrase is absent.
- `docs/OXYGEN_FULL_SPECIFICATION.md`, `.codex/plans/mvp-roadmap.md`, and the
  live `.codex/cycles/history.md` summary still referenced Slice 19E as next or
  uncommitted.

## Contract

Selected documentation behavior:
- Specification section 53 must identify Gate 19F as the current sync and
  Slice 20A, Unit Preference Contract, as the next implementation candidate
  after the sync.
- Roadmap saved-location status must mark Slice 19E committed at `00cb88a` and
  Gate 19F ready in this changeset, without upgrading later slices.
- Roadmap sequencing and next-candidate guidance must move from 19E to Slice
  20A after Gate 19F.
- Live history summary must identify Slice 19E as the last committed
  implementation slice and Gate 19F as the current documentation sync.
- README, data-source, privacy, cache, provider, and About disclosure claims
  must remain truthful and unchanged unless review finds saved-location drift.

Acceptance boundary:
- Documentation-only changes are allowed in:
  - `docs/OXYGEN_FULL_SPECIFICATION.md`
  - `.codex/plans/mvp-roadmap.md`
  - `.codex/plans/current.md`
  - `.codex/cycles/history.md`
- No Kotlin, Compose, Gradle, manifest, provider request, Room schema,
  DataStore format, forecast-cache format, or production app behavior changes
  are allowed.
- Evidence belongs under
  `.codex/test-artifacts/2026-09-04-gate-19f-saved-locations-doc-sync/`.

Out of scope:
- Unit preferences, unit conversion, device-location permission flow, official
  alert lookup, persisted appearance settings, provider changes, radar/maps,
  air quality, widgets, background refresh, notifications, release readiness,
  or MVP-readiness claims.
- Reopening Slice 19E implementation or changing saved-location production
  behavior.

## Workflow

Discover:
- Read required authorities and inspect stale saved-location/next-candidate
  references.
- Confirm clean worktree and local commit evidence for Slice 19E.

Document:
- Replace this active plan with Gate 19F scope and evidence.
- Update spec, roadmap, and live history summary to reflect Slice 19E commit
  `00cb88a`, Gate 19F documentation sync, and Slice 20A as the next
  implementation candidate.
- Append a concise Gate 19F history entry.

Review:
- Run `git diff --check`.
- Inspect the diff for accidental behavior changes or unearned status claims.

## Phase Results

- specified: Gate 19F is defined in the MVP roadmap as the saved-locations
  documentation sync after Slice 19E.
- planned: Bounded to documentation/status alignment across the specification,
  roadmap, active plan, and live history.
- documented: Specification, roadmap, active plan, and live history were
  aligned to Slice 19E committed at `00cb88a`, Gate 19F ready, and Slice 20A as
  the next implementation candidate.
- verified: `git diff --check` passed; log saved under
  `.codex/test-artifacts/2026-09-04-gate-19f-saved-locations-doc-sync/`.
- ready: Documentation-only sync is ready for review and commit.

Skipped commands:
- Android compile, unit, connected, assemble, emulator, and install commands
  were not run because this cycle changed only Markdown documentation/status
  files and did not change production or test code.

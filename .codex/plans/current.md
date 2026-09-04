# Active Cycle

Status: verified
Cycle ID: 2026-09-04-pre-19d-authority-drift-cleanup
Mode: documentation-only
Slice: Pre-19D authority drift cleanup

Goal: Reconcile specification, roadmap, and live cycle-history drift before
starting Slice 19D Save Search Result UI implementation.

Basis:
- `AGENTS.md` puts `docs/OXYGEN_FULL_SPECIFICATION.md` above the roadmap and
  active plan in the authority order.
- `docs/OXYGEN_FULL_SPECIFICATION.md` section 53 named Slice 19A as the next
  implementation candidate even though Slices 19A, 19B, 19C, 31A, 31B, and 32
  are committed in local history.
- `.codex/plans/mvp-roadmap.md` correctly identified Slice 19D as the next
  candidate after committed Slice 32, but its tail contained stale Slice-32
  startup wording and "committed in this changeset" placeholders.
- `.codex/cycles/history.md` said no documentation drift was known while the
  active planning review found specification and roadmap drift, and the live
  file carried four recent cycle entries despite the recent-history contract's
  normal limit of three.
- `README.md` already reports search-result save UI and saved-location removal
  UI as not implemented, so no README status upgrade is needed for this cleanup.

## Contract

Selected documentation behavior:
- Specification section 53 no longer instructs future work to start at already
  committed Slice 19A.
- Specification section 53 acknowledges the committed saved-location storage,
  selection, list/select UI, installed fallback, fallback cache provenance, and
  fallback real-path verification slices only at the level already supported by
  repository history.
- The specification identifies Slice 19D Save Search Result UI as the next
  implementation candidate without marking it covered, implemented, verified,
  committed, released, MVP-ready, or release-candidate-ready.
- The roadmap tail no longer instructs starting Slice 32 after Slice 31B and no
  longer uses "committed in this changeset" for already committed Slice 32
  evidence.
- The live cycle-history summary acknowledges the drift during this cleanup or
  returns to "none known" only after the spec and roadmap corrections are made.
- The live cycle history keeps only the three most recent cycle entries after
  archiving the pre-compression live file under `.codex/cycles/archive/`.

Acceptance boundary:
- Documentation edits are limited to `docs/OXYGEN_FULL_SPECIFICATION.md`,
  `.codex/plans/mvp-roadmap.md`, `.codex/cycles/history.md`, optional archive
  creation under `.codex/cycles/archive/`, and this active plan.
- No Kotlin, Compose, Gradle, manifest, provider request, Room schema,
  DataStore format, forecast-cache format, UI behavior, saved-location
  behavior, provider behavior, unit preference, alert, air quality, radar,
  release, or MVP behavior changes.
- `git diff --check` is the required verification command for this
  documentation-only cleanup.
- Any Android compile, unit, connected, or assemble command not run must be
  explicitly skipped as unrelated to Markdown-only authority cleanup.
- Save evidence under
  `.codex/test-artifacts/2026-09-04-pre-19d-authority-drift-cleanup/`.

Out of scope:
- Implementing Slice 19D.
- Adding search-result save controls.
- Adding saved-location removal UI.
- Changing saved-location persistence, selected-location persistence, Home
  forecast state, forecast cache behavior, fallback behavior, provider
  disclosures, unit preferences, alerts, appearance settings, release-candidate
  status, MVP-readiness claims, or any Android runtime behavior.

## Design

- Edit specification section 53 to summarize the current committed baseline and
  name Slice 19D as the next candidate.
- Edit the roadmap tail to replace stale Slice-32 startup wording with
  post-Slice-32 / pre-19D wording and concrete Slice 32 commit evidence.
- Archive the current live cycle history before compressing it, then retain the
  reading contract, recent summary, and only the three most recent cycle
  entries.
- Update the live history summary so its drift statement matches the corrected
  documentation state.
- Keep the follow-on Slice 19D implementation boundary explicit in the roadmap:
  app state tests in `HomeForecastStateHolderTest`, Compose tests in
  `HomeDashboardUiTest` or a dedicated location-entry UI test, connected Room
  evidence through `RoomSavedLocationStorageFactory.create(...)`, stable tags
  such as `location-entry-result-save-0`, and assertions for saved-list refresh,
  failure text, and `Use now` independence.

## Workflow

Baseline:
- `git status --short`
- Inspect `docs/OXYGEN_FULL_SPECIFICATION.md` section 53,
  `.codex/plans/mvp-roadmap.md` Slice 19D and tail sections, and the live
  `.codex/cycles/history.md` summary/recent entries.

Document:
- Update specification section 53 to remove stale Slice 19A next-candidate
  language and align the immediate next candidate with Slice 19D.
- Update the roadmap tail to remove stale Slice 32 startup language and replace
  "committed in this changeset" placeholders with concrete commit evidence.
- Archive and compress live cycle history to the three most recent entries,
  then update the summary drift statement.

Review:
- `git diff --check`
- Review `git diff -- docs/OXYGEN_FULL_SPECIFICATION.md .codex/plans/mvp-roadmap.md .codex/cycles/history.md .codex/plans/current.md`.

Artifacts target:
- `.codex/test-artifacts/2026-09-04-pre-19d-authority-drift-cleanup/`

Planned artifact target:
- `.codex/test-artifacts/2026-09-04-pre-19d-authority-drift-cleanup/git-diff-check.log`

## Phase Results

- specified: The cleanup is defined as a documentation-only authority
  reconciliation before Slice 19D implementation.
- planned: Bounded to spec, roadmap, live history, and active-plan corrections
  with `git diff --check` verification.
- implemented: Updated `docs/OXYGEN_FULL_SPECIFICATION.md` section 53 to name
  Slice 19D as the next candidate, updated `.codex/plans/mvp-roadmap.md` to
  record Slice 32 commit evidence at `9b9d706`, archived the pre-compression
  live history, and kept `.codex/cycles/history.md` to three recent entries.
- verified: `git diff --check` passed.

Artifacts:
- `.codex/test-artifacts/2026-09-04-pre-19d-authority-drift-cleanup/git-diff-check.log`

Skipped:
- Android compile, unit, connected, and assemble commands were not run because
  this cycle changed only Markdown authority/history files.

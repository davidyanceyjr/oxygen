# AGENTS.md

## Mission

Build Oxygen as a privacy-respecting Android weather app whose behavior is observable, tested, and reproducible.

Optimize for:

- one small, complete vertical slice at a time;
- externally observable behavior over repository appearance;
- meaningful tests at the Android, Compose, provider, or repository boundary;
- focused diffs and traceable version-control history;
- factual status backed by commands actually run.

Repository appearance is not progress.

## Project Context

This repository is an Android/Kotlin/Jetpack Compose scaffold for Oxygen Weather.

Read these authorities before changing behavior:

```text
AGENTS.md
README.md
docs/OXYGEN_FULL_SPECIFICATION.md
docs/data-sources/PROVIDER_TEMPLATE.md
.codex/plans/current.md
.codex/cycles/history.md
settings.gradle.kts
build.gradle.kts
app/build.gradle.kts
core/build.gradle.kts
scripts/android-env.sh
```

For `.codex/cycles/history.md`, normal discovery must read only the live recent
history contract, summary, and at most the most recent three cycle entries. Do
not read the full archived ledger unless a specific implementation detail,
status claim, artifact path, regression, commit, or authority conflict requires
older evidence.

Current modules:

```text
:app   Android application, Compose UI, sample screen, theme, weather scene
:core  Provider-neutral domain models and provider interfaces
```

The installed app uses the production selected-location, provider, Room-cache,
and fallback paths. `SampleWeather.bundle` remains preview/scaffold data only;
it is not a real provider integration and must not enter the production Home
path.

## Tracking Workflow

Use these components to track project state:

- `git commit` records a versioned change; its identity does not establish verification.
- `.codex/plans/current.md` holds the active slice and its next action.
- The active roadmap named in `.codex/plans/current.md` lists ordered candidate
  slices. Record when no roadmap is active. For the UI program,
  `.codex/plans/ui-roadmap.md` is the intended roadmap; until it is created and
  selected, UI specification finalization remains the active work.
  `.codex/plans/mvp-roadmap.md` is inactive for this program.
- `.codex/cycles/history.md` records completed slices and evidence.
- `.codex/cycles/archive/` holds older live-history material when the live file grows too large.

Every commit must use a descriptive subject and a concise body. The body must
summarize the changed behavior or documentation, evidence and verification
actually run, and important limits or skipped checks. Do not use subject-only
messages for slice, fix, or documentation-sync commits.

Keep the actionable item near the top of operational files. Prefer minimal reads of the current slice, roadmap candidate section, and recent history summary. Archive older live-history content before the readable tail gets too large.

## Authority Order

Use this precedence unless a more specific nested `AGENTS.md` exists:

1. Current user instructions.
2. More specific nested `AGENTS.md` or override instructions.
3. `docs/OXYGEN_FULL_SPECIFICATION.md`.
4. `README.md` and data-source provider contracts.
5. `docs/OXYGEN_UI_SPECIFICATION.md` for presentation and interaction requirements,
   subject to the full product specification and provider contracts above.
6. Active cycle state in `.codex/plans/current.md` and its named active roadmap.
7. Existing tests and production behavior.
8. Historical notes in `.codex/cycles/history.md`.

Tests and current behavior are evidence of implementation, not permission to silently contradict the intended contract.

The specification constrains behavior and architecture; it does not choose or sequence slices. Slice selection comes from the active plan and roadmap, which should be derived from the planning sources you trust.

Roadmap entries are ordered candidates and remain `specified` until selected
in `.codex/plans/current.md`. Only that plan selects one bounded active slice
as `planned`; roadmap presence alone does not authorize implementation.

When authorities materially conflict, stop implementation, identify the exact conflict, and resolve or update the higher-level authority before coding.

## No-SLOP Rule

SLOP is ceremony, scaffolding, abstraction, documentation, or confident status language that creates the appearance of implementation without verified behavior.

Do not substitute any of the following for working behavior:

- plans, specifications, roadmaps, diagrams, ADRs, or checklists;
- interfaces, models, fake repositories, screens, controls, routes, or configuration for behavior the app cannot perform;
- TODOs, placeholder returns, empty implementations, dead feature flags, or commented-out implementations;
- mocked, fabricated, hard-coded, or sample success in production paths;
- tests that only prove files, symbols, constructors, mocks, snapshots, or help text exist;
- compilation, lint, type checking, coverage, schema validation, or builds presented as functional proof;
- broad refactoring, dependency churn, or cleanup unrelated to the selected behavior;
- completion claims based on commands that were not run.

A behavior is implemented only when its production path exists. It is verified only when the intended behavior has been exercised at an observable boundary.

When blocked, report the exact blocker. Do not replace failed implementation with future-work prose or a polished completion summary.

## Slice Size Rule

- One bounded, independently observable outcome per slice. Product slices
  deliver user-visible behavior. Prerequisite slices must exercise an existing
  production path and name the downstream behavior they enable. Refactoring,
  test-only, and documentation cycles use their applicable acceptance boundary.
- Prefer one primary production path and one primary acceptance boundary.
- Split a slice when it needs multiple independent state machines, new persistence layers, unrelated UI surfaces, or platform adapters.
- Keep doc-sync work separate from product scope; use it for accurate closure
  without widening the implementation outcome.
- Retain roughly 40% of available session context as a planning target for
  discovery, contract, design, implementation, focused evidence, real-path
  exercise, broad checks, review, documentation closure, and handoff. Record
  a brief sizing rationale; this is an estimate, not a precise measurement.
- If a planned slice is likely to exceed that context target, split it before
  coding even when the resulting work creates more roadmap entries.
- Split by independently observable boundaries. A slice should usually include
  no more than one independent high-context implementation concern: a new
  persistence format, state-machine transition set, user-facing UI surface,
  provider/network path, or platform adapter. Necessary tests and real-path
  exercise for that same outcome belong in its size estimate; they do not
  automatically constitute another implementation concern. Standalone
  evidence journeys and multi-authority documentation sync have their own
  bounded outcomes. Reassess scope when another independent acceptance
  boundary emerges during work.
- Do not split by file, architectural layer, model, or interface alone. Every
  resulting slice must have its own meaningful acceptance boundary; unused
  scaffolding is not a completed prerequisite.
- Keep `.codex/plans/current.md` short enough to read with normal discovery:
  selected behavior, acceptance boundary, intended files, focused evidence,
  broad checks, and explicit out-of-scope limits. Put historical rationale and
  future sequencing in the roadmap or cycle history instead of copying it into
  the active plan.

## Status Vocabulary

Use these states literally:

```text
specified    intended behavior is defined
planned      one bounded slice is selected
covered      a meaningful automated test encodes the behavior
implemented  production code exists for the behavior
verified     real behavior and applicable checks passed
committed    the identified change exists in version-control history
released     verified work is included in a release
```

Track behavior status and commit identity separately. Coverage and
implementation can be established in either order; each claim needs its own
evidence. A commit does not imply coverage, verification, or release. A
documentation change may be reviewed and committed while the behavior it
specifies remains `specified`; test-only work may cover existing behavior
without introducing new production behavior. Verification still requires
exercised production behavior and applicable checks, and release claims require
verified behavior in a release.

## Implementation Workflow

For feature or fix work:

```text
discover -> contract -> design-if-needed -> red-or-baseline -> build
         -> focused-green -> real-path-exercise -> broad-checks
         -> review -> ready
```

For documentation-only work:

```text
discover -> contract/document -> review -> ready
```

For behavior-preserving refactoring:

```text
discover -> baseline-green -> design-if-needed -> build
         -> focused-green -> broad-checks -> review -> ready
```

Keep `.codex/plans/current.md` focused on one bounded active slice. Append concise completion evidence to `.codex/cycles/history.md` when a cycle is ready or committed.

After every commit, review the active plan, live cycle history, and affected
repository authorities for consistency with the commit's actual state. Correct
stale claims before closure; if they are already accurate, record that outcome
in the handoff without creating another documentation commit. A documentation
commit does not require a follow-up commit solely to record its own hash.

Cycle history entries must be self-contained, concise, and appended at the end
of the live history file. Before replacing, compressing, or otherwise rewriting
the live history file, archive its previous content under `.codex/cycles/archive/`.
Do not create a full duplicate archive before ordinary append-only writes; Git
history and the archive file preserve previous ledger states.

## Engineering Rules

- Prefer existing package structure and Kotlin/Compose idioms already used in `app` and `core`.
- Keep provider-neutral domain types in `:core`; keep Android UI, resources, and platform behavior in `:app`.
- Do not add a weather provider without a matching Markdown provider contract under `docs/data-sources/`.
- Keep provider URLs and attribution configurable or isolated; do not scatter service literals through UI code.
- Preserve privacy guarantees: no ads, no advertising SDKs, no behavioral tracking, no mandatory analytics, and no account requirement.
- Location permission must remain optional. Manually selected locations must be able to provide full weather functionality.
- Keep sample data explicitly marked as sample/scaffold data until a real provider path exists.
- Do not introduce generated build outputs, SDK files, Gradle caches, emulator state, or local runtime directories into source control.

## Android And Compose Guidance

- Build UI as the actual usable app surface, not a landing page.
- Weather data semantics must remain readable when decorative effects, animation, gradients, transparency, or atmospheric scenes are disabled.
- Accessibility cannot be disabled by a theme.
- Keep text legible with large accessibility font settings and avoid layout overlap on narrow screens.
- Use stable dimensions for fixed-format UI such as cards, charts, rows, controls, and weather-scene containers so dynamic content does not shift the layout unexpectedly.
- Do not make a single visual theme dominate all future components; Oxygen supports multiple appearance directions.
- Preserve user-facing UI obligations inside each slice; do not defer relevant readability, accessibility, layout, or evidence work to a final polish phase.

## Codex UI Working Rules

- For visual UI work, evaluate the installed rendered application rather than Kotlin source alone.
- Capture a baseline screenshot before meaningful visual changes.
- Work on one bounded component or visual concern at a time.
- Do not alter weather semantics, provider behavior, repository behavior, navigation, presentation values, or accessibility behavior merely to achieve a visual result.
- Use the fast edit/build/install/capture/inspect loop in `docs/UI_DEVELOPMENT_WORKFLOW.md` until the visual acceptance criteria are met.
- Treat rendered screenshots from the installed app as presentation evidence.
- Compilation alone is not evidence that a visual change succeeded.
- Promote successful repeated dimensions, shapes, surface treatments, typography decisions, and other styling values into Oxygen design-system tokens instead of leaving duplicated magic numbers.
- Preserve complete readability and functionality with decorative effects disabled.
- Run focused and broader verification after visual convergence rather than after every tiny visual edit.
- Keep final UI evidence with the normal Codex cycle/test-artifact workflow when the active slice requires visual evidence.
- Codex is allowed and expected to reject its own first visual attempt and iterate when the screenshot does not meet the stated objective.

## Verification Commands

### Verification Budget and Emulator Lifecycle

Before running checks, select the minimum evidence set for the slice: focused
tests, one connected suite when the acceptance boundary requires it, and the
applicable broad build/checks. Record the selected commands in the active plan
or cycle artifact directory.

Match evidence to the changed production boundary: provider/repository
exercise for data changes, installed rendering for visual changes, and Android
evidence for platform behavior. Record why other evidence is inapplicable.
Keep the real-path exercise needed for the selected outcome within that slice;
do not defer required acceptance evidence merely to meet its size target.

- Do not rerun a passing command unless production code, test inputs, or the
  execution environment changed in a way that could affect its result.
- Set a practical time and token budget before verification. When the budget is
  exhausted, report the evidence collected and the remaining gap instead of
  continuing with repetitive checks.
- Start one emulator session per task. Confirm that ADB is ready, then install
  once per APK change. Relaunch or force-stop the app with ADB when needed; do
  not restart the emulator for ordinary retries.
- If a real-path attempt reaches a bounded platform timeout, record the exact
  outcome as a blocker and stop repeating the same attempt. Do not convert a
  platform limitation into mock success.
- Maintain a short verification ledger containing each command, result, and
  reason for any rerun. A passing check is evidence, not a reason to repeat it.
- If the user asks to stop testing or verification, stop immediately.

Test-volume policy:

- Treat connected tests as scarce evidence. For an implementation slice, run
  the minimum focused unit tests for changed behavior and no more than eight
  relevant connected test cases by default. Count test cases, not Gradle tasks
  or test classes.
- Do not run an entire connected test class merely because it contains related
  historical coverage. Exceed the eight-case default only when the acceptance
  boundary or a documented regression risk requires it, and record the reason
  in the active plan or verification ledger.
- After two completed implementation slices, the next roadmap cycle is a
  dedicated test-only and documentation-sync session. Count slices that change
  production code, including repairs and refactors; count slices, not commits
  or sessions. Documentation-only and test-only cycles do not increment the
  count. Record the count in the active plan and reset it after the dedicated
  cycle completes. That cycle may run broader connected and repository suites
  and reconcile the active plan, cycle history, active roadmap, and affected
  repository authorities. If it discovers a defect, select a bounded repair
  slice and then return to the pending cycle; do not silently add product work
  to the test-only cycle.
- Do not defer all testing until that session: every implementation slice still
  requires focused evidence at its changed state or Android boundary.

Use the repo-local environment wrapper for Android commands:

```bash
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

Save screenshots and test logs from verification under:

```text
.codex/test-artifacts/<cycle-id>/
```

Keep artifact payloads out of source control. Record the saved project-local
paths in `.codex/plans/current.md` and `.codex/cycles/history.md` when they are
used as evidence.

For emulator/manual verification:

```bash
scripts/list-avds.sh
scripts/start-emulator.sh
scripts/install-debug.sh
```

Run `scripts/start-emulator.sh` in one terminal and `scripts/install-debug.sh` in another. Use `OXYGEN_EMULATOR_WINDOW=1` when a visible emulator window is needed.

## Completion Standard

Before reporting work as ready:

- state the selected behavior and acceptance boundary;
- identify changed production and test files;
- report focused evidence and broad verification commands actually run;
- if the work was committed, report the post-commit consistency review and any
  required documentation corrections;
- call out any commands not run and why;
- leave unrelated user changes untouched.

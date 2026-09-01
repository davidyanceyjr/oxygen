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

The screen currently uses `SampleWeather.bundle`. Treat it as scaffold data, not a real provider integration.

## Authority Order

Use this precedence unless a more specific nested `AGENTS.md` exists:

1. Current user instructions.
2. More specific nested `AGENTS.md` or override instructions.
3. `docs/OXYGEN_FULL_SPECIFICATION.md`.
4. `README.md` and data-source provider contracts.
5. Active cycle state in `.codex/plans/current.md`.
6. Existing tests and production behavior.
7. Historical notes in `.codex/cycles/history.md`.

Tests and current behavior are evidence of implementation, not permission to silently contradict the intended contract.

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

## Status Vocabulary

Use these states literally:

```text
specified    intended behavior is defined
planned      one bounded slice is selected
covered      a meaningful automated test encodes the behavior
implemented  production code exists for the behavior
verified     real behavior and applicable checks passed
committed    verified work exists in version-control history
released     verified work is included in a release
```

Do not collapse states or report a later state without evidence for earlier states.

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

Keep `.codex/plans/current.md` current for substantial implementation cycles. Append completed cycle evidence to `.codex/cycles/history.md` when a cycle is ready or committed.

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

## Verification Commands

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

Run `scripts/start-emulator.sh` in one terminal, then `scripts/install-debug.sh` in another. Use `OXYGEN_EMULATOR_WINDOW=1` when a visible emulator window is needed.

## Completion Standard

Before reporting work as ready:

- state the selected behavior and acceptance boundary;
- identify changed production and test files;
- report focused evidence and broad verification commands actually run;
- call out any commands not run and why;
- leave unrelated user changes untouched.

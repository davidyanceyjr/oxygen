# Active Cycle

Status: committed
Cycle ID: 2026-08-28-repository-engineering-gate
Mode: documentation-and-repository-maintenance
Goal: Complete the Repository Engineering Gate before major persistence work.
Roadmap slice: Repository Engineering Gate.
Branch or work context: local `oxygen` Android scaffold after project-review roadmap integration.

Specification anchors:
- `AGENTS.md`
- `README.md`
- `.codex/plans/mvp-roadmap.md`
- `.codex/cycles/history.md`
- `.codex/review/findings.md`
- `LICENSE`
- `LICENSE-TODO.md`
- `NOTICE`
- `THIRD_PARTY_LICENSES.md`
- `.gitignore`
- `.github/workflows/`

Acceptance criteria:
- The repository license contradiction is resolved by explicit project-owner decision: GPL-3.0-or-later is the intended source license, the current root `LICENSE` contains GPL-3.0-or-later text, and related notices distinguish Oxygen source licensing from weather-data/provider attribution.
- A baseline GitHub CI workflow is added for compile, unit tests, assemble, and whitespace checks using the repo-local Android environment wrapper or an equivalent hosted Android/JDK/Gradle setup.
- At least one hosted CI run passes before CI is cited as durable verification evidence.
- `main` branch protection is configured after CI exists, requiring baseline CI checks, blocking force pushes and deletion, and using pull requests before merge. Mandatory second-party review remains optional for solo maintenance.
- README maturity/status remains accurate and does not imply MVP, beta, release-candidate, offline, saved-location, alert, unit, or installed-app fallback completion.
- Evidence-retention expectations remain documented: raw local logs can stay ignored, but roadmap/release/readiness claims must be reproducible through CI or retained in reviewable project artifacts.

Acceptance boundary: The Repository Engineering Gate is complete when license status, baseline CI, hosted CI evidence, branch protection, README status, and evidence-retention policy are all resolved and recorded. This gate does not add app behavior, provider behavior, Room, DataStore, offline launch, saved locations, unit preferences, alerts, installed-app fallback, presentation settings, or release readiness.

## Implementation Plan

1. Resolve the source-license contradiction with an explicit project-owner decision before editing license files.
2. Add the smallest baseline GitHub CI workflow that can run the required Android checks in hosted CI.
3. Run local broad verification for the CI/documentation changes.
4. Record hosted CI outcome or the exact hosted-run blocker.
5. Configure or document `main` branch protection once CI status checks exist.
6. Review README/status language and evidence-retention wording for unsupported claims.
7. Append completion evidence to `.codex/cycles/history.md` only when the gate is actually verified or committed.

## Phase Results

- planned: Selected Repository Engineering Gate as the next roadmap item after project-review integration. No CI, branch protection, hosted CI evidence, or full gate completion has been completed yet.
- implemented: Project owner selected GPL-3.0-or-later for Oxygen source code on 2026-08-28. Root `LICENSE`, `LICENSE-TODO.md`, `NOTICE`, `THIRD_PARTY_LICENSES.md`, and `README.md` now record the license decision while keeping weather-data/provider attribution separate.
- implemented: Added baseline GitHub Actions workflow at `.github/workflows/android-ci.yml` with hosted Android/JDK/Gradle setup and separate compile, debug unit test, assemble, and whitespace steps.
- verified: Local broad checks passed on 2026-08-28 through the repo-local Android environment wrapper. Evidence saved under `.codex/test-artifacts/2026-08-28-repository-engineering-gate/`: `compile-debug-kotlin.log`, `debug-unit-tests.log`, `assemble-debug.log`, and `git-diff-check.log`.
- verified: Hosted GitHub Actions run `33175936627` passed on commit `e7dbc75a9b2113f43b6fa0a3ab0c10c6b3f37535`; job `Android checks` passed compile, debug unit tests, assemble, and whitespace steps. Run URL: `https://github.com/davidyanceyjr/oxygen/actions/runs/33175936627`.
- implemented: `main` branch protection was configured after hosted CI passed. Protection requires strict status check `Android checks`, blocks force pushes, blocks deletion, enforces protection for administrators, and requires pull-request review settings with `required_approving_review_count` set to `0` for solo maintenance.
- committed: Repository Engineering Gate evidence was committed and pushed through `main` history. Final record updates are being merged through protected-branch PR workflow.

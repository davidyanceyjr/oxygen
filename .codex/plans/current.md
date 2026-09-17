# Oxygen Standard Now — inherited CD-00 checkpoint

**Status:** verified; committed
**Cycle ID:** `2026-09-17-cd-00-pre-overhaul-checkpoint-rerun`
**Active roadmap:** `.codex/plans/ui-roadmap.md`
**Implementation-slice count:** 0 of 2 since CD-00; checkpoint reset after all
five cases passed and documentation closure

## Selected outcome and acceptance boundary

Closed the inherited repository-wide two-production-slice checkpoint after the
CD-00R repair. Each of the five named UI-04 Home Compose cases passed
individually against the current post-UI-05 Home implementation with fresh
runner/XML/log/textproto evidence: 1/1 passed, zero failures, errors, and skips.

The repaired compact Standard Now case now waits for Compose idle before
measuring the fixed location/current/precipitation hierarchy. Its diagnostic
run recorded valid bounds: location `Rect(18,92–342,140)`, current
`Rect(18,150–342,297)`, precipitation `Rect(18,307–342,379)`.

## Evidence and scope

- CD-00R repair artifacts:
  `.codex/test-artifacts/2026-09-17-cd-00r-standard-now-compact-overlap/`.
- CD-00 rerun artifacts:
  `.codex/test-artifacts/2026-09-17-cd-00-pre-overhaul-checkpoint-rerun/`.
- One recovered API-37 `oxygen_starter` emulator session ran all five methods
  individually; every wrapper returned 0 and the emulator was stopped.
- Post-checkpoint `:app:testDebugUnitTest :core:testDebugUnitTest`,
  `:app:assembleDebug`, and `git diff --check` passed. The Gradle log is at
  `.codex/test-artifacts/2026-09-17-cd-00-pre-overhaul-checkpoint-rerun/broad-checks.log`.

## Limits

No Celestial Dial production work, provider/repository/cache/persistence change,
navigation/theme change, new weather value, or Simple layout change. CD-01
remains `specified`; selecting it is a separate user decision.

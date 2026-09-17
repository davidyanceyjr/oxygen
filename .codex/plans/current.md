# Oxygen Standard Now — inherited CD-00 checkpoint

**Status:** planned; repaired compact boundary accepted, checkpoint rerun selected
**Cycle ID:** `2026-09-17-cd-00-pre-overhaul-checkpoint-rerun`
**Active roadmap:** `.codex/plans/ui-roadmap.md`
**Implementation-slice count:** 2 of 2 since UI-03; reset only after all five
CD-00 cases pass and documentation closes

## Selected outcome and acceptance boundary

Close the inherited repository-wide two-production-slice checkpoint after the
CD-00R repair. Run each of the five named UI-04 Home Compose cases individually
against the current post-UI-05 Home implementation, accepting only fresh
runner/XML/log/textproto evidence with 1/1 passed, zero failures, errors, and
skips per case.

The repaired compact Standard Now case now waits for Compose idle before
measuring the fixed location/current/precipitation hierarchy. Its diagnostic
run recorded valid bounds: location `Rect(18,92–342,140)`, current
`Rect(18,150–342,297)`, precipitation `Rect(18,307–342,379)`.

## Evidence and scope

- CD-00R repair artifacts:
  `.codex/test-artifacts/2026-09-17-cd-00r-standard-now-compact-overlap/`.
- CD-00 rerun artifacts:
  `.codex/test-artifacts/2026-09-17-cd-00-pre-overhaul-checkpoint-rerun/`.
- Use one recovered API-37 `oxygen_starter` emulator session and one APK
  installation for this checkpoint; run the five methods individually.
- After the five cases, run `:app:testDebugUnitTest :core:testDebugUnitTest`,
  `:app:assembleDebug`, and `git diff --check` if source or documentation
  changes remain.

## Limits

No Celestial Dial production work, provider/repository/cache/persistence change,
navigation/theme change, new weather value, or Simple layout change. CD-01
remains `specified` until this checkpoint is fully verified and documented.

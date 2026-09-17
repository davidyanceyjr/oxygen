# Oxygen Standard Now — compact overlap repair investigation

**Status:** planned; bounded repair slice selected
**Cycle ID:** `2026-09-17-cd-00r-standard-now-compact-overlap`
**Active roadmap:** `.codex/plans/ui-roadmap.md`
**Implementation-slice count:** 2 of 2 since UI-03; CD-00 did not reset the
count because its first current-state case failed

## Selected outcome and acceptance boundary

Determine why the existing deterministic Standard Now compact boundary renders
`home-section-location` and `home-section-current` with overlapping semantic
bounds at 360x640dp, font scale 1.0, LTR, Oxygen, Effects Off. Preserve the
current red runner/XML evidence, capture the exact two bounds and rendered
state, then correct only the established cause.

If the installed/Compose production layout overlaps, make the smallest Standard
Now layout correction and retain installed same-route before/final PNG/XML. If
the visible production geometry is valid and the deterministic harness or
assertion setup is wrong, correct only that test boundary and retain evidence
that distinguishes it from a production defect. Do not weaken, delete, or
bypass the no-overlap contract.

The repair is accepted only when
`standardNowCompactHierarchyUsesFixedHeroAndNonOverflowingSupport` completes
once with fresh XML, instrumentation log, textproto, and wrapper agreement:
1/1 passed, zero failures, zero errors, and zero skips. Then run the applicable
focused tests and broad checks for the changed boundary. Return to CD-00 after
repair closure; CD-00 must still run its complete five-case set before the
implementation count resets or CD-01 can be selected.

## Failure evidence and sizing

- CD-00 artifact directory:
  `.codex/test-artifacts/2026-09-17-cd-00-pre-overhaul-checkpoint/`.
- The recovered API-37 `oxygen_starter` session passed health preflight.
- The compact case completed 1/1 with zero skips and one normal assertion
  failure: `home-section-location should not overlap home-section-current` at
  `HomeDashboardUiTest.kt:4447`.
- The other four CD-00 cases were not run and the emulator was stopped.
- Estimated repair usage is 30–35% of one context window: reproduction and
  bounds diagnosis 10%, one bounded correction 10–15%, and focused/broad/
  documentation closure 10%. Split before coding if the cause introduces a
  second independent acceptance boundary or exceeds 60%.

## Intended scope and evidence

Likely files, conditional on the established cause:

- `app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt`
- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`
- current plan, UI roadmap, cycle history, and README status note

Evidence budget:

1. Inspect the retained failure XML/log/textproto and record the two exact
   semantic bounds without changing production behavior.
2. Capture the current rendered fixture and, if production is implicated, the
   installed selected-location route at the same compact conditions.
3. Run the one failed connected method after the correction. Do not run the
   other four CD-00 methods inside this repair slice.
4. Run focused tests for any changed mapper/UI contract, Android-test
   compilation when the test source changes, app/core unit suites, debug
   assembly, and `git diff --check` as applicable.
5. Preserve one emulator session and one install per APK change. A normal
   failure or bounded timeout ends the repair attempt without repeated retries.

## Limits

No Celestial Dial component, visual overhaul progress, Hourly behavior,
provider, repository, cache, persistence, navigation, theme, new weather value,
or Simple layout change is authorized. CD-01 remains `specified`, not selected.
The checkpoint count remains 2 of 2 until the repaired boundary returns to and
passes the full CD-00 evidence set.

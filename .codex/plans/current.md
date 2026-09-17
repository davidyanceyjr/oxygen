# Oxygen Standard Now — CD-01 central dial hero

**Status:** verified; committed
**Cycle ID:** `2026-09-17-cd-01-central-dial`
**Active roadmap:** `.codex/plans/ui-roadmap.md`
**Implementation-slice count:** 1 of 2 since CD-00

## Selected outcome and acceptance boundary

Implement CD-01's central Standard Now hero: replace the current rectangular
hero with one centered code-native circular dial containing the condition mark,
current temperature, condition text, and apparent temperature. Keep today's
high/low in a temporary compact line below the dial until CD-02.

At 360x640dp, Oxygen, Standard layout, Effects Off, the installed production
Now route must show an unmistakable centered dial. Location, dial/current
content, temporary range, lower status content, and footer must remain readable
and non-overlapping. The existing current-weather spoken description remains
one ordered semantic unit. Missing current data shows an explicit unavailable
dial state without a fabricated temperature.

## Intended scope

- Production: `app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt`;
  add a focused dial composable there unless a small reusable component is
  needed after inspection. Promote only repeated Now-local styling to
  `OxygenTheme.kt`.
- Tests: extend the focused Standard Now Compose coverage in
  `HomeDashboardUiTest.kt` for dial geometry/content/semantics and unavailable
  current state if the existing boundary does not already cover it.
- Evidence: baseline and final same-route installed PNG/XML under
  `.codex/test-artifacts/2026-09-17-cd-01-central-dial/`; one focused connected
  case at the changed boundary; applicable unit/build checks; `git diff --check`.

## Verification budget and limits

Use one emulator session and one install per APK change. Do not rerun passing
checks without a source, test-input, or environment change. The primary
acceptance boundary is installed rendering plus the focused Compose geometry,
content, semantics, and missing-current assertion; provider, repository,
cache, persistence, navigation, footer, Simple layout, Hourly/Daily/Details,
new weather values, orbit satellites, atmosphere, and theme redesign are out
of scope.

Sizing rationale: this is one user-visible hero composition concern with one
focused Android boundary and visual iteration; estimated 45% of the session,
within the roadmap ceiling.

## Evidence and result

- Replaced the normal-font rectangular Standard Now current hero with a
  150dp centered code-native circular dial containing the weather mark,
  temperature, condition, and apparent temperature. High/low remains in the
  temporary line below the dial. The compact-font path remains unchanged for
  CD-11's later responsive slice.
- Added an explicit unavailable circular state with no fabricated temperature.
- Focused connected cases passed individually on one API-37 `oxygen_starter`
  session: `standardNowCompactHierarchyUsesFixedHeroAndNonOverflowingSupport`
  and `standardNowAlertLookupOutcomesAreTruthfulAndActionFree`; each completed
  1/1 with zero failures, errors, and skips.
- Installed same-route evidence is under
  `.codex/test-artifacts/2026-09-17-cd-01-central-dial/`, including
  `home-baseline-route.png`, `home-final-clean-2.png`, and their XML dumps.
  Final XML records separated location, dial, precipitation, supporting, and
  footer bounds with the current spoken summary inside the dial.
- Broad checks passed in `broad-checks.log`:
  `:app:testDebugUnitTest :core:testDebugUnitTest :app:assembleDebug`; compile
  and `git diff --check` also passed.

## Limits and next action

No provider, repository, cache, persistence, navigation, footer, Simple layout,
Hourly/Daily/Details, new weather value, orbit satellite, atmosphere, or theme
redesign changed. CD-02 remains the next specified roadmap slice; the
two-slice implementation count is now 1 of 2.

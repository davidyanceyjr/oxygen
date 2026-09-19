# Oxygen Standard Now — CD-07 Precipitation and Provenance Glass Panel

**Status:** planned; CD-06 checkpoint verified and closed
**Cycle ID:** `2026-09-18-cd-07-precipitation-provenance-glass-panel`
**Active roadmap:** `.codex/plans/ui-roadmap.md`
**Implementation-slice count:** 0 of 2 since the CD-06 checkpoint reset

## Selected behavior

Present the existing near-term precipitation summary as the first compact
Celestial Dial glass panel beneath the constellation. Keep source, update,
and provenance visible as readable tertiary context without changing their
meaning or the existing actions.

## Acceptance boundary

At 360x640dp on the installed Standard Now route, a wet-weather fixture shows
the distinct precipitation panel and a no-near-term-precipitation fixture
omits it truthfully. Deterministic coverage proves exact precipitation values,
exact source/update/provenance meaning, stable dial space, no sibling overlap,
and no text derived by reparsing the lower satellite label. The installed wet
route visibly shows the panel while preserving current values, controls,
semantics, and request behavior.

## Intended files

- `app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt`
- `app/src/test/kotlin/com/oxygen/weather/app/HomeForecastPresentationMapperTest.kt`
- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`

Additional files are in scope only if the existing presentation contract
requires them. No provider, repository, Room, persistence, or navigation
change is selected.

## Evidence and checks

- Capture a same-route installed Standard Now baseline and wet-weather final;
  retain PNG/XML under `.codex/test-artifacts/2026-09-18-cd-07-precipitation-provenance-glass-panel/`.
- Add focused deterministic mapper/presentation coverage for wet and absent
  precipitation states and run the minimum named connected cases.
- Run applicable app/core unit suites, Kotlin compilation, debug assembly,
  install, and `git diff --check` after visual convergence.

## Limits and sizing

Alerts, stale/error treatment, source navigation, provider/repository/cache
behavior, other pages, responsive/RTL variants, other themes, localization,
TalkBack service traversal, and release checks remain out of scope.

Estimated at 35% of one session: one lower glass presentation boundary,
existing typed data, one installed route, focused tests, and required broad
checks; no new persistence, provider, or platform adapter.

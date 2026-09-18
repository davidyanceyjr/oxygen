# Oxygen Standard Now — CD-02 high and low orbit satellites

**Status:** verified; committed in `3500f1f`
**Cycle ID:** `2026-09-17-cd-02-high-low-satellites`
**Active roadmap:** `.codex/plans/ui-roadmap.md`
**Implementation-slice count:** 2 of 2 since CD-00; CD-03 is next and resets it

## Selected behavior and acceptance boundary

The normal-font Standard Now route now replaces CD-01's temporary Today range
line with a static upper constellation. High and Low render the existing
formatted presentation values in separate 52dp circular satellites above the
unchanged centered 150dp dial. Missing values omit only their named satellite;
both missing omits the constellation. The merged current spoken summary,
weather semantics, units, provider behavior, and all other Home actions remain
unchanged.

Acceptance passed at 360x640dp, Oxygen, Standard, Effects Off through the
installed selected-location route plus two deterministic connected Compose
methods covering available, high-missing, low-missing, and both-missing states.

## Changed files

- `app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt`:
  added `HighLowSatelliteConstellation` and `DialSatellite`; removed only the
  normal-font temporary range rendering. Compact, unavailable-current, and
  Simple paths remain unchanged.
- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`:
  added the two focused methods and a font-scale parameter to the existing
  dynamic test helper so the sparse fixture exercises the normal-font route.
- `README.md`, `.codex/plans/ui-roadmap.md`, and this plan: synchronized the
  verified CD-02 behavior and selected CD-03 checkpoint.

## Evidence and verification

- Installed baseline/final: `.codex/test-artifacts/2026-09-17-cd-02-high-low-satellites/baseline/`
  and `final/`. The real selected Chicago route had actual high/low values;
  final PNG visibly shows the two upper satellites and final XML retains the
  spoken summary, source/update context, and footer bounds.
- Focused connected evidence: `focused-available/` and
  `focused-sparse-final/`; each fresh runner bundle is 1/1 with zero failures,
  errors, and skips. The failed fixture and diagnostic triage remain in
  `focused-sparse/` and `focused-sparse-diagnostic/`.
- Broad evidence: `broad-checks.log` records passing `:app:compileDebugKotlin`,
  app/core unit tests, `:app:assembleDebug`, and `git diff --check`.
- The Android-test compile initially exposed and then passed after correcting a
  missing local `width` import. The sparse fixture initially permitted the
  mapper's intentional next-daily fallback; constraining other fixture rows to
  missing high/low made both-missing behavior truthful without production data
  changes.

## Limits and next action

No provider, repository, cache, persistence, mapper production logic, units,
navigation, footer, Simple layout, compact/large-font responsive behavior,
RTL/theme translation, precipitation/wind satellites, atmosphere, or other
Celestial Dial surface changed. Provider/repository exercise is inapplicable.

Next: execute CD-03, the required test/documentation checkpoint for the two
production slices since CD-00. Reconcile the CD-01/CD-02 installed pairs,
focused results, functional invariants, and broad checks, then reset the
implementation count before selecting CD-04 or CD-05.

Sizing rationale: CD-02 stayed within the planned 50% session estimate as one
normal-font Now composition concern with two focused Android cases, one
installed visual boundary, and bounded documentation closure.

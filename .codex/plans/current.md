# Oxygen CD-09 — CD-07/CD-08A checkpoint

**Status:** planned; CD-08A behavior is verified and committed in `a6db648`.
**Next action:** reconcile the CD-07/CD-08A evidence and reset the
implementation-slice count. Do not select CD-08B until this checkpoint closes.
**Cycle ID:** `2026-09-19-cd-09-checkpoint`
**Active roadmap:** `.codex/plans/ui-roadmap.md`
**Implementation-slice count:** 2 of 2 since CD-06; this checkpoint resets it.

## Selected boundary and limits

Documentation and evidence reconciliation for the verified CD-08A normal-font
Standard Now non-active alert lookup panel and the preceding CD-07 slice.
Confirm that the production layout repair, five typed outcomes, large-font
non-active branch, active-alert RTL action branch, installed Chicago route,
artifact ledger, and broad checks agree with the repository authorities.
Provider/cache, alert persistence, CD-08B, responsive convergence beyond the
named cases, TalkBack service traversal, and release work remain out of scope.

## CD-08A completion evidence

Production: `app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt`.
Tests: `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`.
Evidence root:
`.codex/test-artifacts/2026-09-19-cd-08ar2-standard-now-layout-repair/`.

- The normal-font five-state runner passed with accepted XML, instrumentation
  log, textproto, exact copy/time checks, action absence, semantic order,
  positive local viewport, non-overlap, and unchanged 150dp dial/220x64dp
  lower constellation geometry.
- `compactLargeFontDashboardSectionsHaveReadableRenderedBounds` and
  `standardNowLargeFontRtlOverflowKeepsHeroNavigationAndAlertActionsReachable`
  each passed with accepted runner evidence.
- `simpleNowContractRemainsScrollableAndUnchanged` passed after the final
  shared-container guard was narrowed to Standard Now, preserving Simple.
- The installed selected Chicago route passed through real search/save/select
  behavior and captured PNG/XML. Its actual live state was an active `Flood
  Watch`; it is retained as active-state evidence and is not relabeled as a
  no-alert route. Deterministic no-alert PNG/semantics were pulled from the
  test app while it was installed.
- App/core unit suites, app Kotlin and Android-test compilation, debug
  assembly, and `git diff --check` passed.

The required checkpoint ledger is the cycle artifact's
`verification-ledger.md`. CD-08A active-alert treatment remains the later
CD-08B candidate.

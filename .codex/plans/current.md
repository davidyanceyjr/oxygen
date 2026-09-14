# Gate 35B — Connected Evidence Repair

**Status:** planned
**Cycle ID:** `2026-09-14-gate-35b-connected-evidence-repair`
**Prerequisite:** Gate 35A's core acceptance records are verified; Gate 35C
remains specified and blocked on this repair.

## Selected slice

Repair the five incomplete focused Gate 35B connected-test records without
changing product or test behavior. The original eight-case outcome set has
runner-backed `1/1 completed` records only for cases 2, 7, and 8. The required
API-37 `oxygen_starter` acceptance boundary is one session where each of these
exact methods reports `1/1 completed`, zero skipped, and zero failed:

1. `HomeDashboardUiTest#standardCompactHomeAtFontScale13KeepsLongContentAndAllPagesReachable`
2. `HomeDashboardUiTest#rtlStandardHomeCompactLongContentKeepsControlsReachableWithoutRefetch`
3. `HomeDashboardUiTest#officialAlertDetailLongContentRemainsReachableInRtlLargeFont`
4. `HomeDashboardUiTest#paperHighContrastDisabledMotionPreservesHomeMeaning`
5. `AppearanceSemanticsUiTest#appearanceCompactControlsRemainScrollReachable`

Use the existing exact-method runner with one fresh case directory per method
under `.codex/test-artifacts/2026-09-14-gate-35b-connected-evidence-repair/`.
Before each next invocation, retain and inspect the Gradle log, JUnit XML,
`test-results.log`, and `test-result.textproto`; completion count rather than
Gradle exit code decides acceptance. Do not rerun cases 2, 7, or 8, broad
checks, or installed/manual journeys without a changed input.

## Boundary and limits

Preserve the already-tested production presentation contracts for compact and
large-font Home, RTL controls and no-refetch behavior, long official-alert
detail, Paper/High with disabled motion, and compact Appearance controls. Do
not change production code, test assertions, providers, persistence, manifest,
permissions, UI semantics, or fixtures to obtain a passing run. If a method
fails, times out, or completes zero tests, retain that result and stop; select a
separate product or harness repair as appropriate.

Start one recovery emulator only if ADB is not ready, keep it for all five
methods, and stop it after the session. Record commands, serial, raw paths,
completion/skip/failure counts, and exit status in the evidence ledger. Run
`git diff --check` once after documentation changes. Existing compile, unit,
assembly, and retained installed evidence are not fresh verification here.

After five accepted records, update the history append-only, roadmap, README,
specification, and active plan to the evidence actually obtained; Gate 35C is
not part of this slice. TalkBack traversal, localization, automatic contrast,
live-alert entry, operational failure reproduction, alert persistence, and
release readiness remain out of scope.

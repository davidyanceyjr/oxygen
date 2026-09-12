# Slice 30B1A4-A7 — Simple RTL Compact Completion

**Status:** planned
**Cycle ID:** `2026-09-12-slice-30b1a4-a7-simple-rtl-compact-completion`
**Prerequisites:** Slice 30B1A4 implementation `d95d268`, recovery runner
`1972972`, and verified Standard fixture alignment `8b5647b`.
**Mode:** bounded one-method Android connected verification

## Selected behavior and acceptance boundary

The existing Simple Home RTL compact path must retain its long-content
reachability, 48dp controls, no-overlap geometry, layout-change behavior, and
unchanged recording-repository request list at 360x640 dp and font scale 1.3.

The only acceptance boundary is:

`HomeDashboardUiTest#rtlSimpleHomeCompactLayoutAndForecastChoicesDoNotRefetch`

Run it once through the bounded connected-method runner. It must produce a
fresh result with exactly one completed test, zero skipped, and zero failed.

## Contract and scope

The committed test already exercises the production-backed
`OxygenAppStateHolder`, RTL test composition, Settings layout transition, both
Simple forecast choices, long provider/location content, compact bounds,
touch-target/overlap checks, and unchanged repository request list. No source
change is planned unless this rendered boundary supplies a new, distinct red
failure; a timeout or interrupted/missing result is terminal for this slice.

Intended evidence directory:

- `.codex/test-artifacts/2026-09-12-slice-30b1a4-a7-simple-rtl-compact-completion/`
  — ignored recovery preflight, runner result, and verification ledger.

The verified A6 broad checks may be reused because no source or environment
change is planned between its commit and this one-method execution.

## Execution and focused evidence

1. Inspect the committed A6 test diff and the retained A6 passing result. Do
   not alter production Home, providers, persistence, navigation, strings,
   resources, dependencies, or manifests.
2. Start one recovery emulator session and run only:

   ```sh
   scripts/start-emulator.sh --recover \
     --artifact-dir .codex/test-artifacts/2026-09-12-slice-30b1a4-a7-simple-rtl-compact-completion/emulator
   OXYGEN_A7_SERIAL=$(tr -d '\r\n' < .codex/test-artifacts/2026-09-12-slice-30b1a4-a7-simple-rtl-compact-completion/emulator/serial.txt)
   scripts/run-connected-method.sh --serial "$OXYGEN_A7_SERIAL" \
     com.oxygen.weather.app.ui.home.HomeDashboardUiTest#rtlSimpleHomeCompactLayoutAndForecastChoicesDoNotRefetch \
     .codex/test-artifacts/2026-09-12-slice-30b1a4-a7-simple-rtl-compact-completion/simple
   ```

3. Record the result and stop the recovery emulator. Do not rerun the method,
   run the Standard method, run the full class, or substitute an installed RTL
   journey.
4. If it passes, review the worktree and select the separately bounded Gate
   30B1B1 documentation/installed-evidence plan. If it fails, preserve the
   exact artifact and select a new bounded diagnosis or repair plan; do not
   claim A4 complete.

## Out of scope

Source changes without a fresh red boundary, Standard reruns, full connected
classes, installed device-wide RTL, screenshots, UI-hierarchy capture,
TalkBack, font-scale-2.0 coverage, reduced motion, theme/contrast matrices,
localization, alerts, Gate 30B1B1, and release readiness.

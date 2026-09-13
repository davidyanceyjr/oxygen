# Slice 30D1 — Appearance Control Semantics

**Status:** verified
**Cycle ID:** `2026-09-13-slice-30d1-appearance-control-semantics`
**Prerequisite:** Gate 30C3 — Alert Accessibility Evidence and Documentation Sync

## Selected behavior

Make the existing Settings / Appearance surface semantically usable through the
production `OxygenApp` path. Theme, Layout, Effects, and Contrast must each
identify their group, choices, current selection, interaction availability, and
preference transaction status without relying on color or decoration.

The supported choices remain exactly those already implemented:

- Theme: Oxygen, Paper, Terminal.
- Layout: Simple, Standard.
- Effects: Off, Subtle. Full remains unavailable.
- Contrast: Standard, High.

This is a presentation/accessibility slice. The existing DataStore formats,
preference interfaces, state holders, callbacks, weather data, and forecast
request behavior are not being redesigned.

## Baseline findings and contract

Existing JVM and Settings UI tests already cover most persistence transactions:
confirmed values, conservative read failure, failed-write retention, retry, and
no forecast refetch. The implementation gap is the rendered contract in
`SettingsScreen`: group headings and status nodes are inconsistently exposed,
Effects uses a wrapper semantics node, and choice enabled/selected semantics are
not expressed consistently across all four groups. Establish a red/baseline
semantics dump before editing so any correction is tied to an observed gap.

The production contract after this slice is:

1. Each managed group has one readable group heading and every supported choice
   has its visible label, a single-choice/control role, selected state, and
   click action in the merged semantics tree. Selected meaning must not depend
   on chip color, icon, or position.
2. While a preference read is Loading, all choices in that group are
   non-actionable and the conservative effective/current value remains visible.
   A read failure states that the saved value could not be read, identifies the
   value being used when one is known, and exposes a named Retry action.
3. While a write is pending, the confirmed/effective presentation and its
   selected semantics remain unchanged; the pending target is stated in a
   readable Saving status and duplicate/competing writes are blocked. Existing
   Effects behavior that retries a failed write by selecting the same choice is
   preserved unless the baseline proves it is unreachable; Theme, Layout, and
   Contrast retain their existing explicit Retry path.
4. After a successful write, the new choice becomes effective and selected, the
   group exposes saved/confirmed status, and the other preference values,
   selected location, forecast presentation, and repository request count are
   unchanged. A write failure retains the confirmed value, states the failure,
   and leaves the existing retry behavior operable.
5. Retry and Back controls have readable labels, button/action semantics, and
   measured targets of at least 48dp. Status text is supplementary evidence,
   not color-only or an unlabeled test tag. Do not use `clearAndSetSemantics`
   in a way that hides visible labels or actions.

## Production implementation boundary

Inspect and change only the existing Settings presentation path unless a
focused test demonstrates that a state-holder correction is necessary.

- `app/src/main/kotlin/com/oxygen/weather/app/ui/settings/SettingsScreen.kt`
  should provide the group-heading, choice, status, Retry, and Back semantics;
  preserve existing visible copy and stable test tags where possible; add
  localized resources only when the current copy cannot express the required
  state.
- `app/src/main/kotlin/com/oxygen/weather/app/OxygenApp.kt` remains the real
  callback/state wiring boundary. Change it only if the rendered semantic
  state is not receiving the already-existing holder state.
- `app/src/main/kotlin/com/oxygen/weather/app/OxygenAppStateHolder.kt` is
  changed only to repair an evidenced transaction/state mismatch. Preserve the
  four existing preference state types, confirmed-write timing, failed-target
  retention, retry behavior, preference independence, and no-refetch behavior.
- Do not modify the four preference storage files, add a second state model or
  persistence format, enable Full effects, add automatic system contrast, or
  change Home/provider behavior.

## Focused tests and evidence

First run a baseline semantics dump from the existing managed `OxygenApp`
Appearance path. Then make the smallest production correction and keep the
focused connected set to these five named cases (one case per existing
preference group plus the cross-group semantics case):

1. `appearanceGroupsAndChoicesExposeMeaningfulSemantics` — all four headings
   and all supported choices expose labels, choice/control roles, selected and
   unselected states, click actions, and non-color meaning; Back is named.
2. `themePreferenceSemanticsRetainConfirmedChoiceThroughPendingAndRetry` —
   loading, pending, successful write, read failure, write failure, exact-target
   Retry, target sizes, and no forecast refetch for Theme.
3. `layoutPreferenceSemanticsRetainConfirmedChoiceThroughPendingAndRetry` —
   the same boundary for Simple/Standard Layout, including the existing
   session/persistence distinction.
4. `effectsPreferenceSemanticsRetainConfirmedChoiceThroughPendingAndRetry` —
   the existing Off/Subtle transaction, conservative read failure, same-choice
   write retry, Android disabled-motion message, and no forecast refetch.
5. `contrastPreferenceSemanticsRetainConfirmedChoiceThroughPendingAndRetry` —
   Standard/High transaction, independent theme/layout/effects values, retry,
   target sizes, and no forecast refetch.

These can extend the existing methods in
`app/src/androidTest/kotlin/com/oxygen/weather/app/ui/settings/` rather than
creating duplicate fixtures. The first case may live in a focused settings
test file; it must still render through `OxygenApp`, not a test-only control.
Use semantics configuration and bounds assertions, not only text or test-tag
existence. Keep the connected run at five cases; do not run full historical
classes merely because they contain related coverage.

Retain and extend the existing focused JVM coverage in:

- `app/src/test/kotlin/com/oxygen/weather/app/ThemePreferenceStateHolderTest.kt`
- `app/src/test/kotlin/com/oxygen/weather/app/LayoutPreferenceStateHolderTest.kt`
- `app/src/test/kotlin/com/oxygen/weather/app/EffectsPreferenceStateHolderTest.kt`
- `app/src/test/kotlin/com/oxygen/weather/app/ContrastPreferenceStateHolderTest.kt`

Add assertions only for a state transition exposed by the implementation or
for a regression guard needed by the connected contract. These tests must
continue to prove canonical forecast/state preservation and zero additional
weather requests; they must not become source-shape or constructor tests.

## Real-path and broad verification

Use one emulator session for the task. Before source changes, capture the
installed Appearance baseline screenshot and UI hierarchy if the existing APK
and emulator are available. After focused green, compile/install once for the
changed APK and exercise the installed production path: open Settings /
Appearance, inspect the loaded groups, make one supported choice, verify the
saved state and return to Home without a forecast refetch, then capture the
resulting Appearance screenshot and hierarchy. This installed journey does not
claim storage-failure behavior; read/write failures are injected only at the
bounded `OxygenApp` connected boundary unless a real platform failure is
observed.

Record commands, results, reruns, and limits in a short ledger under:
`.codex/test-artifacts/2026-09-13-slice-30d1-appearance-control-semantics/`.

Selected checks, in order:

```sh
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests 'com.oxygen.weather.app.ThemePreferenceStateHolderTest' --tests 'com.oxygen.weather.app.LayoutPreferenceStateHolderTest' --tests 'com.oxygen.weather.app.EffectsPreferenceStateHolderTest' --tests 'com.oxygen.weather.app.ContrastPreferenceStateHolderTest'
. scripts/android-env.sh && ./gradlew :app:compileDebugAndroidTestKotlin
scripts/list-avds.sh
scripts/start-emulator.sh --recover --artifact-dir .codex/test-artifacts/2026-09-13-slice-30d1-appearance-control-semantics/emulator
scripts/run-connected-method.sh --serial <recovery-serial> <fully-qualified-class>#<method> .codex/test-artifacts/2026-09-13-slice-30d1-appearance-control-semantics/<case>
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
scripts/install-debug.sh
git diff --check
```

Run the connected-method command once per the five named cases, using the
single recorded recovery serial. If a bounded platform timeout occurs, retain
the exact result and stop repeating that same attempt; do not convert a
timeout into acceptance. Stop and split a repair slice if a production defect
is found rather than weakening the assertion.

## Required documentation closure after implementation

Documentation changes are part of the post-implementation handoff, not a
substitute for the connected evidence. After the slice is actually verified
and committed, reconcile only claims supported by the retained artifacts in:

- `README.md`: add or adjust the installed Appearance accessibility/status
  claim, without claiming TalkBack service traversal, localization, Full
  effects, automatic contrast, or release readiness.
- `docs/OXYGEN_FULL_SPECIFICATION.md`: update the Appearance/accessibility
  contract and section 53 status to describe the four supported groups,
  confirmed-write semantics, non-color choice/status semantics, and the exact
  verification limits.
- `.codex/plans/mvp-roadmap.md`: mark Slice 30D1 only at the evidence-supported
  state, retain Slice 30D2 as the next specified candidate, and correct the
  active handoff if needed.
- `.codex/plans/current.md`: record final status, changed files, artifacts,
  commands actually run, skipped checks, and the next bounded slice.
- `.codex/cycles/history.md`: append one self-contained D1 entry with result,
  five accepted connected cases, JVM/broad checks, installed evidence,
  artifacts, limits, and commit state. Append only; archive is required only
  if the live-history reading contract is later compressed or rewritten.

No provider contract, data-source disclosure, privacy, dependency, license, or
release claim changes in this slice. Do not modify `AGENTS.md`, Gradle
configuration, provider contracts, or archived history for routine D1 closure.

## Out of scope

Large-font/RTL layout resilience (Slice 30D2), installed accessibility-service
TalkBack traversal (Gate 30E), new preferences, automatic system contrast,
Full effects, icon packs, persistence formats, Home/alert/provider behavior,
visual redesign, localization, notifications, and release readiness.

## Completion evidence

Production changed only in `app/src/main/kotlin/com/oxygen/weather/app/ui/settings/SettingsScreen.kt`:
managed Theme, Contrast, Layout, and Effects headings now expose heading
semantics; every supported choice exposes a visible label, RadioButton role,
selected state, click action, and disabled state when unavailable; Effects no
longer hides its action behind a wrapper; pending Effects retains the confirmed
selection; and Effects status nodes are named. Existing preference state,
storage, callbacks, weather data, and forecast behavior were preserved.

Focused connected evidence is retained under
`.codex/test-artifacts/2026-09-13-slice-30d1-appearance-control-semantics/`.
The five accepted named cases, each completed once with zero skipped and zero
failed on API-37 `oxygen_starter` / `emulator-5554`, are:

- `appearanceGroupsAndChoicesExposeMeaningfulSemantics`
- `themePreferenceSemanticsRetainConfirmedChoiceThroughPendingAndRetry`
- `layoutPreferenceSemanticsRetainConfirmedChoiceThroughPendingAndRetry`
- `effectsPreferenceSemanticsRetainConfirmedChoiceThroughPendingAndRetry`
- `contrastPreferenceSemanticsRetainConfirmedChoiceThroughPendingAndRetry`

The Layout case needed one test-only viewport repair after the added headings
moved status nodes below the initial viewport; its final rerun passed. The
pre-change Effects read-failure semantics dump is retained at
`baseline-effects-read-failure/appearance-read-failure-semantics.txt`.

Checks passed:

- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests 'com.oxygen.weather.app.ThemePreferenceStateHolderTest' --tests 'com.oxygen.weather.app.LayoutPreferenceStateHolderTest' --tests 'com.oxygen.weather.app.EffectsPreferenceStateHolderTest' --tests 'com.oxygen.weather.app.ContrastPreferenceStateHolderTest'`
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin :app:compileDebugAndroidTestKotlin`
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`

Installed production evidence used one APK install and one API-37 emulator
session: Chicago was selected through the real Open-Meteo manual path without
location permission; Appearance loaded all four groups; Paper became selected
and reported `Theme saved`; Back returned to Home retaining Chicago, Now/Page
1 of 4, source, update, and provenance text. Final screenshots and Android UI
hierarchies are retained as `installed-appearance-saved.png`,
`installed-appearance-saved.xml`, and `installed-home-return.xml`.

The installed journey does not prove a numeric request count; fixture-backed
connected tests prove no forecast refetch. TalkBack service traversal,
large-font/RTL resilience, localization, and release checks remain out of
scope. The next bounded slice is Slice 30D2 — Appearance Layout and Environment
Resilience.

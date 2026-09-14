# Slice 30D2 — Appearance Layout and Environment Resilience

**Status:** committed at `2955aa5`
**Cycle ID:** `2026-09-13-slice-30d2-appearance-layout-environment-resilience`
**Prerequisite:** Slice 30D1, committed at `78ecb84`; documentation sync committed at `021d58e`

## Selected behavior

Keep the production Settings / Appearance surface readable and reachable when
the environment changes. The four existing groups—Theme, Contrast, Layout, and
Effects—must remain usable at compact size, large accessibility font scales,
RTL direction, reduced motion, and supported theme/contrast combinations.

This is a presentation and environment-resilience slice. The existing choice
set, preference transactions, state holders, storage formats, callbacks,
selected location, weather presentation, provider behavior, and forecast
request behavior remain unchanged.

## Acceptance boundary

The installed `OxygenApp` Appearance path must prove:

1. At 360x640dp, every group, supported choice, loading/error/saved status,
   Retry action, and Back action is reachable by scrolling. No visible text,
   control, status, or action is clipped, overlapped, or hidden behind a fixed
   sibling.
2. At font scales 1.3 and 2.0, the same controls remain scroll-reachable and
   their labels/statuses remain readable. Fixed-format controls retain usable
   bounds; dynamic text may wrap or require scrolling but must not overlap
   neighboring content.
3. RTL preserves logical label/control order and keeps the semantic meaning of
   selected, pending, disabled, error, saved, Retry, and Back states intact.
   No directional icon or alignment reverses in a way that contradicts the
   active layout direction.
4. With Effects Off and system animation scale disabled, the screen still
   exposes the same visible and semantic preference state. Disabling animation
   does not rewrite the saved Effects preference, hide status, or alter
   selection/restoration behavior.
5. Oxygen, Paper, and Terminal paired with Standard and High Contrast retain
   readable group/choice/status meaning and visible selected/disabled/action
   affordances. Contrast or theme changes do not alter the four preference
   values, selected location, weather data, or forecast request count.

The D1 semantics contract remains the invariant: headings, visible choice
labels, RadioButton roles, selected state, actions, disabled state, statuses,
Retry, and Back must remain present without relying on color or decoration.

## Baseline and red phase

Before changing production code:

- Confirm one API-37 `oxygen_starter` emulator and record serial, resolution,
  density, locale/layout direction, font scale, animation scale, and focused
  package in the cycle artifact directory.
- Install the current committed APK once if needed, open Settings / Appearance
  through the real app path, and capture baseline screenshots and UI
  hierarchies for default LTR, font scale 1.3, font scale 2.0, and RTL.
- Capture the current Effects-Off / animation-disabled state and one
  representative theme/contrast pair. Record any missing, clipped, overlapped,
  or incorrectly ordered node as a failing boundary assertion.
- Add or extend only the focused connected assertions below. Run the failing
  assertion against the baseline before implementing a repair. If the
  baseline is already green for a case, retain it as a regression guard and do
  not invent a production change.

All baseline and final artifacts belong under:

`.codex/test-artifacts/2026-09-13-slice-30d2-appearance-layout-environment-resilience/`

## Production boundary

Inspect and change the existing Appearance presentation path only:

- `app/src/main/kotlin/com/oxygen/weather/app/ui/settings/SettingsScreen.kt`
  is the primary implementation boundary for scroll containers, spacing,
  wrapping, ordering, fixed control dimensions, and environment-safe layout.
- `app/src/main/kotlin/com/oxygen/weather/app/ui/theme/OxygenTheme.kt` and
  `app/src/main/kotlin/com/oxygen/weather/app/ui/theme/OxygenAppearance.kt`
  may change only if a failing theme/contrast boundary proves a presentation
  token or palette issue. Preserve the existing theme identities and contrast
  behavior.
- `app/src/main/kotlin/com/oxygen/weather/app/OxygenApp.kt` may change only if
  a failing installed or connected boundary proves that environment state is
  not reaching the existing presentation path.
- `OxygenAppStateHolder.kt` and preference storage remain out of scope unless
  a test demonstrates an existing transaction/state regression caused by the
  presentation change. No new state model or persistence format is allowed.

Promote any repeated successful spacing, control, or surface values into the
existing design-token approach rather than duplicating magic numbers. Do not
change weather semantics, provider behavior, navigation, accessibility meaning,
or preference transaction timing to make a screenshot pass.

## Focused connected evidence

Keep the connected set to no more than six named cases. Extend
`app/src/androidTest/kotlin/com/oxygen/weather/app/ui/settings/AppearanceSemanticsUiTest.kt`
and the existing settings fixtures where possible; every case must render
through real `OxygenApp` wiring and use bounds/semantics assertions in addition
to screenshots or text checks.

1. `appearanceCompactControlsRemainScrollReachable` — at 360x640dp and
   default font scale, scroll to each group and prove every choice, status,
   Retry, and Back node is displayed, non-overlapping, and within usable
   bounds.
2. `appearanceFontScale13ControlsRemainReadableAndReachable` — at font scale
   1.3, prove wrapped labels/statuses, control bounds, scroll reachability, and
   no sibling overlap for all four groups.
3. `appearanceFontScale20ControlsRemainReadableAndReachable` — repeat the
   boundary at font scale 2.0, including the lowest status and Back action.
4. `appearanceRtlPreservesLogicalLabelControlOrder` — force RTL through the
   supported emulator/test mechanism, capture hierarchy and screenshot, and
   assert logical label/control ordering plus unchanged D1 semantics.
5. `appearanceReducedMotionOffPreservesEffectsMeaning` — set animation scale
   to zero, exercise Effects Off and restoration through the real callback
   path, and prove the saved preference, selected state, status, and forecast
   request count remain unchanged.
6. `appearanceThemeContrastPairsPreserveMeaning` — exercise a compact pairwise
   matrix covering Oxygen/Paper/Terminal with Standard/High Contrast, proving
   readable labels/statuses and selected/disabled/action meaning without
   changing other preference values or weather requests.

Use existing preference state-holder tests only when a failing presentation
boundary exposes a real state regression. Do not turn these tests into source,
constructor, or screenshot-only checks. Do not run full historical connected
classes merely because they contain related tests.

## Real-path exercise

Use one emulator session for the task and install once per changed APK. After
focused green:

- Exercise the installed Settings / Appearance path in default LTR at 360x640dp
  and font scale 2.0; capture final screenshots and UI hierarchies after
  scrolling to the bottom.
- Exercise the installed RTL path and retain one screenshot plus hierarchy.
- Exercise one representative reduced-motion Effects-Off state and one
  representative theme/contrast pair. Confirm the four choices, saved state,
  selected location, Home weather presentation, source/update/provenance, and
  navigation remain unchanged.
- Treat installed screenshots and hierarchies as presentation evidence. They
  do not by themselves prove request counts or injected storage failures;
  those remain connected-fixture evidence unless the platform exposes them.

If a platform timeout occurs, retain the exact command/result and stop repeating
that same attempt. If an installed failure is production behavior rather than
an environment limitation, stop and split a repair slice instead of weakening
the assertion.

## Verification commands

Maintain a short ledger in the cycle artifact directory with each command,
result, rerun reason, and limitation. Use the repository Android environment
wrapper for Gradle commands.

Baseline and environment setup:

```sh
scripts/list-avds.sh
scripts/start-emulator.sh --recover --artifact-dir .codex/test-artifacts/2026-09-13-slice-30d2-appearance-layout-environment-resilience/emulator
```

Focused checks after the red phase:

```sh
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests 'com.oxygen.weather.app.ThemePreferenceStateHolderTest' --tests 'com.oxygen.weather.app.LayoutPreferenceStateHolderTest' --tests 'com.oxygen.weather.app.EffectsPreferenceStateHolderTest' --tests 'com.oxygen.weather.app.ContrastPreferenceStateHolderTest'
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin :app:compileDebugAndroidTestKotlin
scripts/run-connected-method.sh --serial <recorded-serial> <fully-qualified-class>#<method> .codex/test-artifacts/2026-09-13-slice-30d2-appearance-layout-environment-resilience/<case>
```

Run the connected command once for each of the six named cases, using the same
emulator session and recorded serial. Then run the applicable broad checks:

```sh
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
scripts/install-debug.sh
git diff --check
```

Compilation and assembly are broad checks, not functional proof. Do not rerun
a passing command unless source, test inputs, or the environment changed in a
way that can affect it.

## Documentation closure after verification

After production work is verified and committed, synchronize only evidence-
supported claims in `README.md`, `docs/OXYGEN_FULL_SPECIFICATION.md`,
`.codex/plans/mvp-roadmap.md`, `.codex/plans/current.md`, and append one
self-contained entry to `.codex/cycles/history.md`. Record final artifact paths,
the exact connected cases, installed LTR/RTL/font-scale evidence, broad checks,
and all unverified limits. Do not claim TalkBack service traversal, automatic
contrast, localization, release readiness, or a numeric installed request count
unless separately evidenced.

## Out of scope

D1 semantic contract changes, new preferences, state/storage redesign, Full
effects, automatic system contrast, Home/alert/provider behavior, navigation
redesign, visual redesign, localization, notifications, TalkBack service
traversal, release readiness, and broad device-matrix certification.

## Completion evidence

Production repair: `SettingsScreen.kt` keeps the existing Appearance content
scrollable with bottom separation from the fixed Back action and names the
disabled-motion status for stable verification. `AppearanceSemanticsUiTest.kt`
adds six real-`OxygenApp` connected cases:

- `appearanceCompactControlsRemainScrollReachable`
- `appearanceFontScale13ControlsRemainReadableAndReachable`
- `appearanceFontScale20ControlsRemainReadableAndReachable`
- `appearanceRtlPreservesLogicalLabelControlOrder`
- `appearanceReducedMotionOffPreservesEffectsMeaning`
- `appearanceThemeContrastPairsPreserveMeaning`

All six passed once on API-37 `oxygen_starter` / `emulator-5554`. Installed
evidence is retained under
`.codex/test-artifacts/2026-09-13-slice-30d2-appearance-layout-environment-resilience/`
for compact LTR, font scale 2.0, RTL, disabled-animation Effects Off, and a
Terminal/High pair. Focused state-holder tests, app/core unit tests, debug
compile, debug assembly, `scripts/install-debug.sh`, and `git diff --check`
passed. The temporary compact display override was restored. Numeric installed
request counts, injected storage failures, TalkBack service traversal,
localization, automatic contrast, and release checks remain unverified or out
of scope.

Commit: `2955aa5` (`Repair Appearance overflow resilience`). Gate 30D3 is the
next bounded documentation/evidence candidate.

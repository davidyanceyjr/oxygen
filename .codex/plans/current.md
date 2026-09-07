# Slice 26 — Persisted Effects Off/Subtle Baseline

**Status:** committed; focused and broad verification passed, but the
installed real-path Home forecast exercise remains incomplete.
**Cycle ID:** `2026-09-06-slice-26-effects-preference`
**Planning basis:** local `main` at `af16a9f`; Gate 25 implementation at
`23a9d49`, authority sync at `99097d9`, recent summary at `af16a9f`.
**Execution state:** production and test changes are committed at `c7b578a`.
The required single emulator was used for baseline, connected tests, installed
persistence checks, and motion-policy checks.
The pre-existing `.codex/review/findings.md` edit must remain untouched.

## Selected behavior and stopping boundary

Intended result: a user can open Settings / Appearance, select Off or Subtle, return to the same
weather session, and retain that choice across activity/process restart.
Effective Off uses the existing undecorated Home rendering, preserves every
weather/alert/source/stale meaning, and honors Android's disabled-animation
signal without overwriting the user's stored choice. Subtle remains the default.

Stop after this one preference works through production DataStore, app state,
Settings, Home, and Android motion policy and passes the evidence below. This
is the roadmap's Slice 26 Effects Off baseline, not the complete appearance
system. Specification section 25 still specifies OFF/SUBTLE/FULL; richer Full
behavior remains specified and must not be advertised as implemented by this
baseline. Do not expose a Full selector that merely aliases Subtle.

Implementation budget: target at most 12,000 agent context tokens end-to-end,
and always less than 33% of the executing context window; use the smaller cap.
Verification budget: 60 minutes and at most 6,000 of those tokens. If the slice
cannot fit, record the exact remaining boundary and replan before expanding;
do not omit required evidence or label incomplete work verified.

## Authorities and dependency check

Read/reconcile `AGENTS.md`, README, provider template, module/root Gradle files,
`scripts/android-env.sh`, roadmap Slice 26 and next-candidate section, and
specification sections 20–26, 31.3, 34, 37, 44, 49, 51, and 53.
Use `docs/UI_DEVELOPMENT_WORKFLOW.md` for the installed visual loop. Recent
history discovery was limited to its contract/summary and last three entries;
no archived ledger was read.

Dependency readiness is supported by source inspection and commit history,
not fresh runtime verification:

- Slice 18I exists at `02f7012`; Standard Home and its existing Off rendering
  remain in `HomeLoadingScreen.kt`. Do not reopen historical visual work.
- Slice 25A exists at `2484e90`; `SettingsScreen.kt` has a reachable Appearance
  destination and `OxygenApp.kt` wires it through the actual Settings route.
- Small-state persistence exists: `UnitPreferenceStorage.kt` implements a
  dedicated Preferences DataStore; `MainActivity.kt` injects it and
  `OxygenAppStateHolder.kt` restores/writes it. DataStore is already an app
  dependency. Effects storage itself does not exist.
- Gate 25 is committed at `23a9d49` with recent history recording its evidence
  and post-commit authority sync. This session did not rerun that evidence.
- No prerequisite requires Simple, Full, theme selection, or high contrast.

Cadence: the test-volume rule was introduced at `a4c4e56`, after Slice 25A.
Gate 25 is the first subsequent implementation cycle (it changed runtime code);
Slice 26 is the second. Reserve the third session after this slice for test-only
verification and documentation sync before starting another implementation
slice. This is the prospective counting basis, not a claim that older broad
runs satisfied the new policy. Immediate documentation sync is also required
by this slice's new installed preference/persistence behavior.

## Actual code facts and assumptions corrected

Paths below are relative to `app/src/main/kotlin/com/oxygen/weather/`.

- `app/ui/theme/OxygenAppearance.kt` defines OFF/SUBTLE/FULL, with SUBTLE default,
  independently of theme/layout/icon pack. Enum presence proves no preference.
- `app/OxygenApp.kt` takes an injected appearance value and passes it to Settings
  and Home. Installed `MainActivity` supplies no persisted appearance. The
  injected preview/test parameter must not override the installed preference.
- `app/ui/settings/SettingsScreen.kt` shows a read-only effective summary and
  explicitly says appearance selection is unavailable. It has no effects action.
- `app/ui/home/HomeLoadingScreen.kt` omits `home-weather-scene` when Off and
  substitutes opaque surface roles. `app/ui/weather/WeatherScene.kt` draws a
  static gradient/glow/cloud Canvas. There is no continuous scene animation;
  Full and Subtle currently enter the same rendering branch. No new animation
  engine is necessary, and no battery/FPS savings are claimed.
- Home uses `animateScrollToPage` for tabs/previous/next. These finite user
  interactions are distinct from continuous decoration; their motion must also
  respect the platform signal while retaining the same destination semantics.
- No reduced-motion adapter was found in the inspected production UI path.
- Unit persistence runs on an executor; effects changes need no canonical
  weather remapping. Several state-holder transitions construct a new
  `OxygenAppPresentationState`; simply adding a default field would risk
  resetting effects during location/forecast transitions. Preserve it explicitly.
- Existing `HomeDashboardUiTest` cases inject Off into Home/alert/Settings
  fixtures. They encode useful rendering invariants but do not prove installed
  selection, persistence, restart, or platform motion handling. Existing unit
  DataStore instrumentation proves only its separate units storage boundary.
- Repository status prose still contains historical drift (e.g. root AGENTS'
  sample-screen sentence versus `MainActivity`'s installed repository wiring;
  the roadmap sequence introduction mentions 24B as latest despite later gate
  entries). These are not permission to substitute sample data. They do not
  conflict with the selected effects contract; avoid unrelated cleanup here.

## Acceptance boundary

All criteria must hold on the same final revision:

1. Settings / Appearance is reachable from both first-run and Home. It provides
   labelled Off and Subtle choices, selected-state semantics, at least 48-dp
   targets, readable save/error feedback, and existing in-surface/Android Back.
   Theme and Standard layout remain truthful read-only summaries.
2. Missing preference defaults to Subtle. Use stable stored values, not enum
   ordinals. Unknown/version-invalid records fall back without a crash or
   destructive automatic rewrite. Read failure leaves weather usable, uses a
   conservative effective Off fallback, and exposes local-storage failure in
   Appearance; it must not be described as a successfully restored choice.
3. A successful write changes effective presentation and selected-state feedback
   without recreating the app state holder or fetching weather/alerts/geocoding.
   Failed writes retain the last confirmed choice and show a retryable message;
   choosing again retries. Pending writes cannot silently claim persistence.
   Serialize or disable overlapping selections so stale completions cannot win.
4. Restore before rendering decorated Home; no transient Subtle scene when a
   stored Off preference is still loading. Startup without a selected location
   also restores effects. The choice survives new storage/state-holder instances,
   `ActivityScenario.recreate()`, force-stop/relaunch, and location/forecast
   transitions. The connected and Compose evidence below must cover the first
   rendered state, not only the settled state after restoration.
5. Effective Off omits atmospheric gradient/glow/cloud decoration and uses the
   existing opaque Home roles. No continuous decorative animation is introduced.
   Current/hourly/daily/details values, missing-value meaning, alert summary and
   details, attribution, source/update/provenance, stale/failure context, refresh,
   and navigation remain usable. Toggling preserves the weather session and units.
6. Android disabled animators force effective Off, including when Subtle was
   saved. Sample the signal before decorated content and on every foreground
   resume; changes made in Android Settings must apply on return. Keep the saved
   choice intact, show why effective Off differs, and restore Subtle when the
   system allows motion again. With reduced motion, tab/previous/next navigation
   changes pages without animated programmatic scrolling; the connected evidence
   must observe the transition while it could still be moving, not only after
   settling. No permission request or app write to system settings is needed.
7. Appearance and representative Home/alert/stale content remain readable at
   360dp x 640dp, density 1, font scale 1.3. Controls wrap/scroll without overlap;
   selection/error/override meaning is textual and announced in logical order.
   Inspect installed screenshots and semantics; preserve existing theme tokens.
8. Deterministic state/Compose evidence proves zero additional repository calls
   from preference actions, unchanged canonical forecast/cache/alert state, and
   independence from unit/theme/layout/icon values. Installed manual evidence
   proves real persistence/rendering; it cannot by itself prove request counts.

Platform contract: use the public API-26-compatible
[`ValueAnimator.areAnimatorsEnabled()`](https://developer.android.com/reference/android/animation/ValueAnimator#areAnimatorsEnabled())
for the bounded Android signal (official reference reviewed 2026-09-06).
It reports system animation enablement, including duration-scale-zero behavior;
this is not a claim to detect every OEM accessibility setting. A small
lifecycle-bound adapter with initial/resume sampling suffices for the normal
Settings round trip; broader background monitoring is outside this slice.

## Intended production changes

Use existing packages and small concrete pieces; no dependency/schema change.

- Add `app/EffectsPreferenceStorage.kt`: the actual Preferences DataStore
  implementation with an application-context singleton delegate, isolated file
  and stable versioned Off/Subtle values. Keep existing unit/location stores
  untouched; expose read/write failures to the app boundary. No generic
  preferences framework or persisted future appearance fields.
- Extend `app/OxygenAppStateHolder.kt` with load/pending/error/confirmed effects
  state and a guarded Appearance selection action. Perform I/O off the main
  thread, publish restore results for first-run as well as Home, preserve effects
  in every state reconstruction, and reject stale writes. While the initial read
  is pending, resolve the conservative effective Off state so Home cannot render
  a transient decorated frame; expose read failure separately from a confirmed
  choice. Effects changes copy presentation state only; they do not remap or
  refresh canonical weather.
- Add a small `app/AndroidMotionPreferenceSource.kt` (or equivalently bounded
  activity-local adapter) that reads the platform signal at initial/resume time.
  Keep the effective-effects rule testable and separate from persisted choice.
- Wire real storage and the motion signal in `MainActivity.kt`; resolve effective
  appearance in `app/OxygenApp.kt` using the existing appearance value as the
  base, copying only effects. Preserve theme/layout/icon inputs for previews and
  tests, with explicit test setup for confirmed preferences and system policy.
- Replace the read-only effects row in `app/ui/settings/SettingsScreen.kt` with
  the working selector and feedback, including explicit selected semantics,
  retryable write/read errors, and wording that distinguishes effective Off from
  an unconfirmed restored choice. Retain the current destination/navigation
  architecture and scrollable Settings surface; remove obsolete selection copy.
- In `app/ui/home/HomeLoadingScreen.kt`, retain the existing Off branch and
  make programmatic page motion conditional on reduced motion. Change
  `app/ui/theme/OxygenAppearance.kt` only for the small effective policy if useful.
  Do not rebuild `WeatherScene.kt`, charts, symbols, or Home composition.

## Focused tests and evidence

Focused tests were added and run green. The names below describe the selected
evidence boundary; they are no longer merely planned targets.

Unit scope: `EffectsPreferenceStorageTest` and `EffectsPreferenceStateHolderTest`
under `app/src/test/kotlin/com/oxygen/weather/app/`:

- Default, valid Off/Subtle, unsupported record, and read/write failure behavior
  through storage/state boundaries, not only codec round trips.
- Pending/success/failure/retry and serialized rapid selection; first-run startup,
  stored Off before Home, delayed restore, and forecast completion/location
  change while a preference write is pending must preserve the confirmed choice.
- Requested/effective motion-policy combinations, system override restoration,
  and unchanged theme/layout/icons/units/canonical weather/alerts. Count provider
  calls across preference actions after initial forecast settling.

Connected budget: the executed final filtered run covered seven cases, below the
eight-case default:

1. New `EffectsPreferenceInstrumentedTest#dataStoreReadbackAndRecreationKeepEffects`:
   real production DataStore and production Activity lifecycle. Set and read back
   Off/Subtle, call `ActivityScenario.recreate()` on the production Activity,
   assert the selected choice and effective scene after restoration, and verify
   existing unit/location values remain retained; restore prior local settings in
   cleanup. This is the explicit Activity-recreation evidence, not only a new
   storage/state-holder pair.
2. New `EffectsPreferenceUiTest#selectionPreservesStaleWeatherAndAlerts`:
   use the real OxygenApp resolver/wiring with a controllably delayed effects
   read while Home weather is already available; assert no
   `home-weather-scene` before releasing a stored Off read and after Off resolves,
   then select Subtle and verify the scene appears. At compact/large font,
   assert labelled Off/Subtle controls expose selected semantics and at least
   48-dp targets; navigate all four pages and alert detail; source/stale/values
   preserved, including missing-value text and refresh-failure context. Record
   zero repository request delta for the preference actions, then exercise one
   explicit refresh and assert only that refresh's expected request delta and
   resulting failure meaning. Fixtures prove deterministic semantics and the
   no-flash first rendered state.
3. New `EffectsPreferenceUiTest#firstRunSaveFailureCanRetryAndReturn`:
   real Settings route with injected storage failure; selected state, feedback,
   retry, Back, selected semantics, 48-dp targets, and no location
   permission/geocoding request. The failed write retains the last confirmed
   choice until a retry succeeds.
4. New `EffectsPreferenceInstrumentedTest#systemMotionOverrideKeepsStoredChoice`:
   actual Android motion source plus production effective resolver, persisted
   Subtle, system scale zero and return/resume, then restoration; effective Off,
   truthful UI, stored value unchanged, and no animated programmatic page
   navigation. Under the reduced-motion state, use a controlled Compose frame
   clock or equivalent pager observer: after direct tab selection and both
   available previous/next accessibility actions, sample immediately and on the
   next frame, asserting the target is already current/settled with zero page
   offset and no intermediate page or animation frame. A settled label or
   `waitForIdle()` alone is insufficient. Restore system settings in finally.
   Do not substitute a fake signal for the Android adapter evidence; record a
   platform blocker if it cannot be exercised.
5. Existing `HomeDashboardUiTest#settingsRootReachesAllDestinationsAndLocationsBackWorksWithAndroidBack`:
   update obsolete read-only effects expectations while retaining route assertions.
6. Existing `HomeDashboardUiTest#oxygenAppUnitsSelectionReturnsHomeWithAlternateUnitsAndKeepsPagesReachable`:
   regression for adjacent persisted units and Home behavior.
7. New `EffectsPreferenceUiTest#initialReadFailureRendersConservativeOffAndRecovers`:
   through OxygenApp and the rendered Appearance route, inject an initial local
   preference read failure while representative weather is available. At compact
   and large font, assert effective Off, no scene, readable local-storage error
   text that does not claim a restored choice, labelled selected-state semantics,
   at least 48-dp targets, usable Back/weather navigation, and the defined retry
   action. Release the injected failure, retry the read, and assert recovery to
   the confirmed choice. Save `appearance-read-failure.png` plus hierarchy and
   semantics evidence under the cycle artifact directory; label this as
   deterministic injected-error UI evidence rather than live installed
   persistence evidence.

Executed focused commands:

```sh
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*EffectsPreferenceStorageTest' --tests '*EffectsPreferenceStateHolderTest'
. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class='com.oxygen.weather.app.EffectsPreferenceInstrumentedTest#dataStoreReadbackAndRecreationKeepEffects,com.oxygen.weather.app.ui.settings.EffectsPreferenceUiTest#selectionPreservesStaleWeatherAndAlerts,com.oxygen.weather.app.ui.settings.EffectsPreferenceUiTest#firstRunSaveFailureCanRetryAndReturn,com.oxygen.weather.app.EffectsPreferenceInstrumentedTest#systemMotionOverrideKeepsStoredChoice,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#settingsRootReachesAllDestinationsAndLocationsBackWorksWithAndroidBack,com.oxygen.weather.app.ui.home.HomeDashboardUiTest#oxygenAppUnitsSelectionReturnsHomeWithAlternateUnitsAndKeepsPagesReachable,com.oxygen.weather.app.ui.settings.EffectsPreferenceUiTest#initialReadFailureRendersConservativeOffAndRecovers'
```

No full historical connected class run. Screenshots establish appearance;
assertions establish semantic behavior; neither alone proves persistence or
continuous-animation absence. The read-failure screenshot and semantics dump
are deterministic injected-error evidence. Inspect the production animation
paths and record a short settled-screen observation, without claiming
energy/performance metrics.

## Required installed real-path exercise

At execution start create
`.codex/test-artifacts/2026-09-06-slice-26-effects-preference/ledger.md`.
Record each command/result, environment, artifact, and rerun reason. Start one
emulator using the repository wrapper/scripts, confirm ADB boot readiness, and
retain that emulator for baseline, final tests, and manual exercise:

```sh
scripts/list-avds.sh
scripts/start-emulator.sh
# In another terminal, after ADB is ready:
scripts/install-debug.sh
```

Before meaningful UI changes, save original size/density/font/motion settings;
configure 360x640, density 160, font scale 1.3 using the wrapped ADB environment.
Capture/inspect installed `appearance-before.png` and `home-subtle-before.png`
with `scripts/capture-screen.sh`. Use the existing real selected-location cache,
or obtain one forecast through normal manual search with no location grant.
Do not delete user data or seed production Home with sample/test success.

After visual convergence install each changed APK once; the final connected
run can supply the final install. Force-stop/relaunch that installed APK for
manual checks without another build/install:

1. Open normal Settings / Appearance, select Off, verify feedback, return to Home,
   visit Now/Hourly/Daily/Details, and inspect source/update/weather meaning.
2. Force-stop and relaunch (`adb shell am force-stop com.oxygen.weather`, then
   `adb shell am start -n com.oxygen.weather/.MainActivity`); verify Off selected
   and no scene. Repeat with Subtle to prove both persisted values and restoration.
3. With Subtle saved, set Android animator duration scale to zero, leave/reenter
   Oxygen to exercise resume, verify effective Off explanation and usable page
   navigation. Restore the original scale, return, and verify stored Subtle takes
   effect again. Record exact commands, original values, and cleanup. The
   override case may share the installed test's platform setup but must show the
   normal production activity path too.
4. Capture `appearance-off.png`, `home-off.png`, `appearance-system-override.png`,
   `home-subtle-restored.png`, and corresponding hierarchy/semantics evidence.
   Inspect readable labels/selected state, Back, and no clipping in the normal
   installed journey; inspect the connected `appearance-read-failure.png` and
   its hierarchy/semantics dump for rendered error state. Record TalkBack
   traversal if available; otherwise name that unverified condition.

No live severe alert is required: deterministic connected evidence supplies
active-alert/stale coexistence. Existing production cache is sufficient for the
manual effects boundary; live provider success is not a new provider acceptance
gate. If no real forecast can be obtained/restored within one bounded attempt,
record that exact blocker and leave installed Home evidence incomplete. Do not
repeat timed-out platform/provider attempts or replace them with fixture success.
Restore original device configuration and preference after evidence capture.

## Broad verification and documentation obligations

After visual convergence and focused evidence, run once on the final revision:

```sh
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

Assembly already used for the unchanged final APK counts; do not repeat it.
Repeat any passing check only for a relevant source/test/input/environment change,
recording why. Build success is not functional or visual proof.

Before ready, review the actual production/test diff, list changed files, and
record focused, connected, real-path, broad results and every skipped command.
Use `implemented` if code exists but required evidence is incomplete.

Update README's installed/not-implemented lists, specification section 53,
roadmap Slice 26 boundary/next sequencing, and installed Appearance wording to
match only verified Off/Subtle behavior. Retain specification section 25's Full
requirement as unfinished work, explicitly visible in roadmap follow-up scope;
do not imply all MVP effects levels or all appearance settings are complete.
Reconcile `PRIVACY.md` and in-app local-storage disclosure only as needed to
mention the added local preference. Providers/licenses/request terms do not change.
Append concise self-contained completion evidence to live cycle history when
ready/committed; do not rewrite/archive history for an ordinary append.

If implementation is committed, perform the required post-commit authoritative
doc sync of plan, history, README, roadmap, and affected specification/disclosure;
record the actual commit and verify `git diff --check` again for doc changes.
Schedule the third-session test-only/doc-sync gate before another feature slice.
No commit is requested by this planning task.

## Explicitly out of scope

Full scene richness/animation; Simple or other layout selection; theme/icon-pack
selection, Paper/Terminal completion, high contrast, custom units, Home redesign,
new design system, generic settings/persistence architecture, Room migration,
provider requests/fallback/cache/alert semantics, background observers/polling,
notifications, location acquisition/permissions, accounts/telemetry/dependencies,
release audits, and MVP/release-readiness claims. Do not fix unrelated review notes.

## Execution verification ledger

- Baseline: `scripts/list-avds.sh`, emulator boot, baseline install, constrained
  device settings, and baseline captures passed. Artifacts are under
  `.codex/test-artifacts/2026-09-06-slice-26-effects-preference/`.
- Focused unit tests passed for `EffectsPreferenceStorageTest` and
  `EffectsPreferenceStateHolderTest`.
- Connected tests passed for `EffectsPreferenceUiTest` read-failure recovery
  and selection/request preservation, `EffectsPreferenceInstrumentedTest`
  system-motion override, and the two planned HomeDashboard regressions.
- Broad checks passed: `:app:compileDebugKotlin`, app/core debug unit tests,
  `:app:assembleDebug`, and `git diff --check`. Logs are saved in the cycle
  artifact directory.
- Installed production checks passed for Settings Appearance readability,
  Off persistence across force-stop/relaunch, Subtle restoration, animator
  scale zero override, scale one restoration, and device-setting cleanup.
- One bounded live manual-search attempt did not reach a forecast because
  emulator text-entry/tap interaction left the query unchanged. No real Home,
  alert, or stale forecast screenshot is claimed; deterministic connected
  evidence covers those semantics. TalkBack traversal was not run.

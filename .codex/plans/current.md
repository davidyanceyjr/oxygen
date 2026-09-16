# Slice 30E-P2 — Home TalkBack Data-Focus Semantics

**Status:** implemented; focused summary and Hourly-card boundaries verified; installed TalkBack action traversal deferred for remote-control logistics
**Mode:** bounded Compose semantics repair, one connected regression case, and one installed TalkBack journey
**Cycle ID:** `2026-09-16-slice-30e-p2-home-talkback-data-focus-semantics`
**Baseline:** the worktree contains the uncommitted P2 source/test draft. Its
focused connected case completed 1/1 on API 37; this execution also confirmed
the Android TTS control and the installed Now summary focus on real Chicago /
Open-Meteo data, but did not verify the named Hourly action path.
**Next action:** preserve the focused automated verification and defer any
future installed TalkBack action retry; do not claim Gate 30E closure from the
remote-control limitation.

## Contract

On Standard Home with forecast data, Now has exactly one full-width,
non-actionable accessibility target containing the existing mapper-owned current
weather description. The decorative weather mark is rendered but contributes no
merged weather announcement. The pager retains its existing named
`Show next page: Hourly` action, and the first Hourly card remains its existing
single spoken target.

The installed TalkBack journey must focus the Now summary, invoke the named
Hourly action, and focus the first Hourly card. Each observed announcement must
match the visible weather meaning and must not repeat the decorative mark.
This slice does not change weather values, layout, page navigation, providers,
cache, location, effects, theme, contrast, or strings.

## 2026-09-16 execution result

- The focused connected case and broad checks were reused from the unchanged
  source state: the case passed 1/1 with 0 skipped and 0 failed; compile,
  unit-test, assembly, and `git diff --check` had already passed. They were
  not rerun because no production, test, or relevant environment input changed.
- One candidate install was performed with APK SHA-256
  `1a41d5bff0fa2a738d67321d50c5dbe2f5267a7fb41030a931b28b1efa7344cd`.
  Oxygen loaded real Chicago data through the production Open-Meteo path with
  visible `Cloudy`, `72 degrees Fahrenheit`, source/update text, and a native
  full-width summary description at non-empty bounds.
- TalkBack was enabled through Android Settings and the independent TTS
  control was heard by the user. The user then heard the Oxygen Now focus as
  `72 degrees` followed by the day's high and low and reported that it worked
  as intended. This is human speech evidence for the reported meaning only;
  no unreported condition or feels-like wording is inferred.
- The first horizontal gesture on the Home content was intercepted by the
  pager and moved directly to Hourly. DPAD focus recovery returned to Now, but
  no bounded route reached and activated the named `Show next page: Hourly`
  action. The installed action traversal therefore remains unverified; the
  direct page change is not installed TalkBack acceptance evidence. The
  deterministic focused test's named Hourly action and first Hourly-card
  boundary are recorded as verified, and the Lead Project Engineer concurred
  that the remaining gap is logistical remote control of the emulator rather
  than a product failure.
- Restoration ultimately passed in a cleanup-only emulator boot after the
  first empty-service settings command was rejected. TalkBack service,
  accessibility, and touch exploration ended disabled; Oxygen was force-stopped;
  the emulator was stopped; and final ADB had no online device.

Evidence and the command ledger are under
`.codex/test-artifacts/2026-09-16-slice-30e-p2-home-talkback-data-focus-semantics/installed-talkback-journey/`.
No production or test source changed during the installed attempt. The focused
Hourly-card test is verified; the installed TalkBack action traversal and Gate
30E remain unverified.

## Production implementation

In `app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt`,
make only these changes in `NowPage`:

1. Apply `testTag("home-current-summary")` and
   `clearAndSetSemantics { contentDescription = dashboard.current.spokenDescription }`
   to the existing full-width current-condition `Row`.
2. Keep `home-current-mark` as a rendered descendant, but remove its merged
   weather `contentDescription`.
3. Retain the existing hidden rendered condition, temperature, apparent
   temperature, and high/low text so the single summary remains mapper-owned.
4. Do not make the summary clickable or assign a role, focus request, custom
   action, new string, provider model, permission, dependency, service,
   manifest entry, analytics, or telemetry.

The current worktree draft is an implementation candidate, not accepted
evidence until the focused test and installed journey pass. If it exposes a
runtime defect, stop with the smallest reproduction and select a new repair
slice; do not widen this one.

## Focused automated evidence

Extend only
`currentWeatherExposesOneCompleteSpokenSummaryWithoutRedundantChildren` in
`app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`.
With the existing deterministic Standard Home fixture at 360x640 dp and Effects
Off, it must prove:

- one merged `home-current-summary` node has the existing full description and
  non-empty bounds;
- `home-current-mark` exists only in the unmerged tree; condition,
  temperature, and range descendants have no merged descriptions;
- the summary has no custom actions while `home-page-container` exposes only
  `Show next page: Hourly`;
- invoking that action reaches Hourly; entry 0 has non-empty bounds and its
  existing complete description.

Use the pre-change implementation/test mismatch as the red-or-baseline
evidence; do not revert working code merely to manufacture another red run.
Run exactly one connected case:

```sh
. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest#currentWeatherExposesOneCompleteSpokenSummaryWithoutRedundantChildren'
```

Save its runner output and any copied semantics payload under
`.codex/test-artifacts/2026-09-16-slice-30e-p2-home-talkback-data-focus-semantics/connected-current-summary/`.
If test-process teardown removes the package before `run-as` artifact
retrieval, retain the command output and record that limitation; do not rerun a
passing test solely to retrieve it.

## Installed acceptance boundary

Use one fresh visible API-37 `oxygen_starter` session. Capture an installed
Home baseline before installing the candidate APK, record APK hash/package,
ADB/device state, TalkBack/TTS/touch-exploration settings, volume, animation
scales, locale/layout, permissions, selected location, Appearance, and network
state. If no pre-existing installed Home can show production data, record that
baseline as unavailable rather than substituting sample content. Start the
visible emulator in one terminal and build/install once in another:

```sh
OXYGEN_EMULATOR_WINDOW=1 scripts/start-emulator.sh
scripts/install-debug.sh
```

Load the normal production selected-location path with real Open-Meteo data and
Room-cache identity; capture the visible current condition, temperature,
source/update/provenance, and native hierarchy. If no valid selected location
is present, manually search and select Chicago through the installed location
surface without granting location permission, then continue through the same
forecast/cache path. No sample data, fixture, hidden route, seeded alert,
coordinate-only navigation, or inferred transcript qualifies.

With TalkBack active and audible, establish focus visually and in the native
hierarchy on each target in order:

1. page identity;
2. Now summary, whose complete description and bounds identify the target;
3. the pager's `Show next page: Hourly` custom action, then activate it;
4. the first Hourly card, identified by its complete description and bounds.

Use TalkBack focus traversal and its action menu; do not use a coordinate tap
to advance the pager or to create an apparent focus result. Confirm the Android
TTS sample control is audible before the Oxygen journey.

For Now and Hourly, retain a focus screenshot, hierarchy, relevant logcat, and
the user's contemporaneous words heard. Speech must match visible condition,
temperature, time, and precipitation meaning where present; the Now mark must
not be announced separately. Capture the visible Hourly destination after the
custom action.

Restore every recorded setting and app state, force-stop Oxygen, stop the
emulator, and retain final ADB state. Save the ledger, screenshots,
hierarchies, logs, APK/package identity, production-state proof, human
observations, and restoration proof under:

`.codex/test-artifacts/2026-09-16-slice-30e-p2-home-talkback-data-focus-semantics/`

One emulator session is allowed. Stop after the first unchanged platform
timeout, wrong/duplicate announcement, or unreachable target. Record the
failure; do not claim P2 verification from hierarchy evidence alone.

## Verification budget

After source/test changes, run:

```sh
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin :app:compileDebugAndroidTestKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

Run the focused connected case once and no other connected case. Maintain the
command/result/rerun ledger. Do not rerun a passing command unless its inputs or
environment changed.

## Required documentation updates

Only after the implementation and installed boundary are complete:

1. Update this plan with exact command results, artifact paths, user-observed
   speech, restoration result, skipped checks, and final P2 status.
2. Append one concise self-contained completion or blocker entry to
   `.codex/cycles/history.md`.
3. Reconcile P2 and the user-designated complete/verified Gate 30E status in
   `.codex/plans/mvp-roadmap.md`, `README.md`, and
   `docs/OXYGEN_FULL_SPECIFICATION.md` with the retained evidence. Preserve
   every unrelated unverified limitation and do not claim release-candidate,
   MVP completion, signing, publication, or release readiness.
4. If committed, use a descriptive subject and body that state the semantic
   behavior, exact evidence, limits, and skipped checks; then perform the
   post-commit authority sync.

## Intended tracked files

- `app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt`
- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`
- `.codex/plans/current.md`
- `.codex/cycles/history.md` and `.codex/plans/mvp-roadmap.md` after evidence
- `README.md` and `docs/OXYGEN_FULL_SPECIFICATION.md` only for status sync

Generated artifacts remain untracked.

## Out of scope

- alert-detail, Appearance, Simple-layout, localization, automatic contrast, or
  broader TalkBack traversal;
- visual redesign, weather/provider/cache/location/persistence/notification/
  background behavior;
- focus hacks, custom accessibility services, TTS dependencies or voice/image
  changes, permissions, analytics, or telemetry;
- release-candidate, MVP-complete, signing, publication, or release claims.

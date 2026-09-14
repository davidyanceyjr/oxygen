# Gate 35B — MVP Presentation and Accessibility Verification

**Status:** committed
**Cycle ID:** `2026-09-14-gate-35b-mvp-presentation-accessibility-verification`
**Prerequisite:** Gate 35A and Gate 34B are verified in
`2026-09-14-gates-34b-35a-data-source-mvp-core-verification`.

## Selected slice

Close the remaining MVP presentation and accessibility evidence boundary for
the already-installed Oxygen production path. This is an evidence and
documentation slice, not a visual redesign or a new feature slice. The
production baseline already contains the Home, saved-location, Settings,
Units, disclosure, and official-alert surfaces; implementation work in this
slice is limited to a demonstrated test-harness or production correction that
is necessary to satisfy the boundary.

The acceptance boundary is the installed app on one API-37 `oxygen_starter`
session. It must show, with weather meaning and provider behavior unchanged:

- Standard Home Now, Hourly, Daily, and Details remain readable and usable;
- saved locations, Settings, Units, Data Sources/Privacy disclosures, alert
  summary, and alert detail remain reachable with truthful semantics;
- persisted Oxygen/Paper/Terminal theme, Standard/High contrast,
  Simple/Standard layout, and Off/Subtle effects restore without a forecast
  refetch or semantic change;
- compact width and font scales 1.3 and 2.0 do not clip, overlap, or require
  unsafe horizontal traversal;
- RTL preserves page meaning, chronology, control order, and navigation;
- Effects Off and Android disabled-animation behavior retain complete weather
  meaning; and
- the existing operational failure/stale presentation keeps source, update,
  stale, retry, and alert/no-alert meaning truthful.

Existing Gate 30B1B1, Gate 30B3, Gate 30C3, and Gate 30D3 artifacts remain
valid evidence for conditions they already cover. This gate must add only the
remaining installed-path evidence and must not claim TalkBack service
traversal, localization, automatic contrast, live alert detail entry, alert
persistence, background polling, or release readiness.

## Production contract and implementation boundary

Before editing, inspect the real production path in:

- `app/src/main/kotlin/com/oxygen/weather/app/OxygenApp.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/OxygenAppStateHolder.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/HomeForecastPresentationMapper.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/home/`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/settings/`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/units/`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/alerts/`
- `app/src/main/kotlin/com/oxygen/weather/app/AboutDisclosureContent.kt`

Do not add a provider, persistence format, permission, navigation surface,
preference, notification, or presentation abstraction. Do not change forecast
values, source/provenance, alert meaning, request behavior, or accessibility
semantics to make screenshots pass.

If focused or installed evidence identifies a real production defect, record
the exact failing boundary and stop this gate. Create a separately named,
bounded repair slice before changing production behavior. A test-only fixture
or harness correction may remain in this gate only when it fixes the test's
interaction with the existing observable contract and does not weaken the
assertion.

## Focused automated evidence

Run the smallest relevant existing cases first; do not run the full connected
classes by default. The planned connected set is capped at eight cases:

1. `HomeDashboardUiTest#standardCompactHomeAtFontScale13KeepsLongContentAndAllPagesReachable`
2. `HomeDashboardUiTest#standardDetailsAtFontScale20KeepsLongProviderContentScrollReachable`
3. `HomeDashboardUiTest#rtlStandardHomeCompactLongContentKeepsControlsReachableWithoutRefetch`
4. `HomeDashboardUiTest#officialAlertDetailLongContentRemainsReachableInRtlLargeFont`
5. `HomeDashboardUiTest#paperHighContrastDisabledMotionPreservesHomeMeaning`
6. `AppearanceSemanticsUiTest#appearanceCompactControlsRemainScrollReachable`
7. `AppearanceSemanticsUiTest#appearanceFontScale20ControlsRemainReadableAndReachable`
8. `AppearanceSemanticsUiTest#appearanceRtlPreservesLogicalLabelControlOrder`

Use the retained Gate 30D2 results for the other Effects Off and
theme/contrast matrix cases unless source or the execution environment changes.
If the alert-detail case cannot run because the production journey truthfully
has no active alert, retain the deterministic Compose boundary evidence from
Slice 30C2 and record the installed no-alert limitation; do not fabricate a
live alert.

Run focused app/core unit tests only for changed production or state-holder
behavior. Existing relevant boundaries include the Home forecast, theme,
contrast, effects, layout, and disclosure state-holder tests. A passing unit
task is supporting evidence, not proof of the installed presentation boundary.

## Installed real-path exercise

Save all evidence under:

`.codex/test-artifacts/2026-09-14-gate-35b-mvp-presentation-accessibility-verification/`

Use one API-37 `oxygen_starter` emulator session. Establish a baseline before
any edit; install only when the APK changes. Capture screenshots and, where
useful, UI hierarchies/semantic dumps for these bounded journeys:

1. Real manual selected Chicago location, then Standard Now, Hourly, Daily,
   and Details with Effects Off.
2. Saved Locations list with current marking and a return to Home; Settings
   root through Appearance, Units, Data Sources, Privacy, and About.
3. Deterministic or available installed alert summary/detail boundary. A live
   no-alert result is evidence of truthful no-alert behavior, not alert-detail
   coverage.
4. Restore a supported alternate appearance, including one theme/contrast
   pair, and confirm Home meaning and selected settings remain stable.
5. Reproduce compact width, font scale 1.3, font scale 2.0, RTL, and Android
   disabled animations in separate clearly labeled captures. Restore locale,
   direction, font scale, animation state, and saved preferences afterward.
6. Exercise the existing stale/refresh-failure or no-cache error state and
   verify source, update, stale, retry, disclosure, and no-alert semantics.

The verification ledger must contain the exact device/configuration command,
case name, artifact path, result, and any bounded timeout. A platform timeout
is a recorded limitation and must not be retried repeatedly or converted into
mock success.

## Execution order and checks

1. Review the active plan, roadmap Gate 35B contract, recent history, current
   production contracts, retained Gate 30 artifacts, and clean/dirty worktree.
2. Establish the installed baseline and select the eight-case focused run.
3. Run focused tests. If a real defect appears, stop and hand it off as a
   separate repair slice; otherwise continue with the installed journeys.
4. Run the installed real-path exercise and inspect every capture/semantic
   artifact against the acceptance boundary.
5. Run applicable broad checks once after focused evidence is green:

   ```sh
   . scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
   . scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
   . scripts/android-env.sh && ./gradlew :app:assembleDebug :app:assembleRelease
   git diff --check
   ```

   For the emulator, use `scripts/list-avds.sh`,
   `scripts/start-emulator.sh`, `scripts/install-debug.sh`, and
   `scripts/capture-screen.sh` as applicable. Do not rerun a passing command
   unless source, test input, or the execution environment changes.

## Required document updates after verified evidence

Append one self-contained entry to `.codex/cycles/history.md` containing the
status, exact acceptance result, focused cases and counts, installed journeys,
artifact directory, broad checks, limits, and commit state. Do not rewrite or
archive history for this append-only entry.

Synchronize `.codex/plans/mvp-roadmap.md` so Gate 35B reflects only the state
actually reached (`verified` before commit, `committed` after commit), and set
the next candidate to Gate 35C without implying release readiness.

Update `README.md` and `docs/OXYGEN_FULL_SPECIFICATION.md` only where their
current presentation/accessibility evidence or remaining-limit statements are
made stale. Preserve the early-app/non-release wording and explicitly retain
unverified TalkBack service traversal, localization, automatic contrast,
live-alert detail entry when unavailable, and release checks. Do not add
claims from screenshots that were not produced or from connected tests that
were not run.

If the slice is committed, reconcile this plan, the roadmap, README/spec
status claims, and the appended history entry with the commit before closing
the cycle. No Gate 35C release decision is part of this slice.

## Explicitly out of scope

- new product behavior, provider integration, persistence, permissions,
  notifications, background polling, or backup-policy changes;
- TalkBack service traversal and the deferred Gate 30E audio issue;
- localization, automatic contrast, alert persistence, and live-provider
  testing beyond the existing Gate 35A boundary;
- broad visual redesign or screenshot-only semantic changes; and
- release-candidate or MVP-complete status.

## Result and evidence

Gate 35B is verified on one API-37 `oxygen_starter` session. All eight planned
focused connected cases passed, each with 1 completed, 0 skipped, and 0
failed. Installed evidence covers real Chicago weather on Standard Now,
Hourly, Daily, and Details, saved-location current marking, Settings,
Appearance, Units, Data Sources, Privacy, About, Paper/High appearance, and
Effects Off with Android animation scales disabled. Compact width and
font-scale 1.3/2.0 captures, RTL capture, and restored device state are
retained.

The installed Chicago alert lookup returned no active alert; this verifies
truthful no-alert behavior only. Live alert-detail entry, operational
stale/error reproduction in this session, TalkBack service traversal,
localization, automatic contrast, alert persistence/background behavior, and
release readiness remain unverified. Existing Gate 30C3 and Gate 35A evidence
is retained for those boundaries.

Focused artifacts and installed captures are under
`.codex/test-artifacts/2026-09-14-gate-35b-mvp-presentation-accessibility-verification/`.
Broad checks passed: app/core debug unit tests, debug Kotlin compilation,
debug/release assembly, and `git diff --check`. No production or test source
changed; only tracking documentation was synchronized after verification.

Commit state: committed at `19347d9`; post-commit authority synchronization is
complete.

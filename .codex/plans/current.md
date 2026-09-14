# Gate 34A — Settings and About Release Check

**Status:** verified; ready for commit
**Cycle ID:** `2026-09-14-gate-34a-settings-about-release-check`
**Difficulty:** 3/10

## Selected behavior

The installed Settings/About surface must present the implemented preference
information architecture and release-facing disclosures truthfully: supported
Settings destinations match the current app behavior, Privacy and Open Source
Licenses remain reachable, source-code licensing is distinct from weather-data
attribution/licensing, and unfinished appearance modes are not exposed as
implemented choices.

This is a bounded release-check slice. Existing behavior is presumed correct
only after the Settings boundary and its disclosure state are exercised.

## Contract and acceptance boundary

Pass only when all of the following are demonstrated:

- The installed Settings root exposes exactly the implemented destinations:
  Appearance, Units, Locations, Data Sources, Privacy, Open Source Licenses,
  and About, grouped under the current Settings IA.
- Appearance exposes only implemented persisted choices: Oxygen, Paper, and
  Terminal themes; Standard and High contrast; Simple and Standard layout;
  and Off and Subtle effects. The unfinished Full effects mode is not exposed
  as a selectable option or described as implemented.
- Privacy and Open Source Licenses are reachable from the installed Settings
  root and remain readable at the bounded compact/large-font test surface.
- Open Source Licenses explicitly separates Oxygen's GPL-3.0-or-later source
  license from weather-data attribution/licensing and upstream dependency
  licenses; no unsupported complete-inventory claim is made.
- Returning from each tested disclosure/settings destination preserves the
  Settings surface without changing forecast semantics or causing a refetch.
- No provider, persistence, permission, manifest, navigation destination, or
  unfinished appearance behavior is added or changed merely to pass this gate.

## Discovery findings

- `SettingsDestination.entries` currently defines the seven implemented
  destinations and `SettingsScreen.kt` groups them as Appearance, Weather,
  Places, and Information.
- `SettingsScreen.kt` renders three theme choices, two contrast choices, two
  layout choices, and two effects choices. `EffectsLevel.FULL` exists as a
  domain/display value but is not rendered as a choice; this must be asserted
  at the boundary.
- `AboutDisclosureContent.kt` already contains Privacy, Open Source Licenses,
  and About state, including the source-code/weather-data distinction.
- Existing unit coverage verifies destination ordering and disclosure text;
  existing connected coverage reaches every Settings destination and checks
  disclosure reachability, links, no refetch, and no permission request.
- README and specification describe the same implemented Settings destinations
  and identify full effects behavior as unfinished. No authority conflict was
  found during planning.

## Intended files

Inspect first; modify only if focused evidence exposes a real mismatch:

- `app/src/main/kotlin/com/oxygen/weather/app/ui/settings/SettingsScreen.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/AboutDisclosureContent.kt`
- `app/src/test/kotlin/com/oxygen/weather/app/AboutDisclosureStateHolderTest.kt`
- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`
- `README.md`
- `docs/OXYGEN_FULL_SPECIFICATION.md`

Expected implementation is test-only if the existing production boundary is
confirmed. Documentation changes are allowed only to correct an observed
Settings/About status mismatch; do not rewrite unrelated release material.

## Execution sequence

1. Confirm the clean worktree and record the unchanged baseline. Run the
   focused disclosure unit test before changing tests.
2. Trace Settings destination construction, preference storage/presentation,
   appearance choice rendering, About disclosure state, and existing README/
   specification claims. Mark each as implemented, unfinished, or unsupported.
3. Add the smallest meaningful boundary assertion for the identified gap:
   connected Settings coverage should assert supported appearance choices and
   absence of the unfinished Full effects choice; reuse existing disclosure
   reachability and no-refetch coverage where it already proves the contract.
4. If a boundary test fails because production behavior or documentation is
   inconsistent, make the smallest scoped correction and keep all unrelated
   provider, persistence, permission, and navigation behavior unchanged.
5. Run focused unit evidence, then one bounded connected Settings journey on
   the existing API-37 emulator session if available. Save semantics/screenshots
   only for the Settings/About acceptance boundary under the cycle artifact
   directory.
6. Run applicable broad checks, review the diff for semantic drift/slop, and
   report any platform timeout or skipped release-candidate checks explicitly.

## Planned evidence and commands

Create artifacts under:

`.codex/test-artifacts/2026-09-14-gate-34a-settings-about-release-check/`

Focused baseline and tests:

- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*AboutDisclosureStateHolderTest*'`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- Install the resulting debug APK once if it changed, then run the smallest
  relevant connected cases from `HomeDashboardUiTest`:
  `settingsRootReachesAllDestinationsAndLocationsBackWorksWithAndroidBack` and
  `settingsDisclosuresShowActiveProviderLicenseAndPrivacyBaseline` (two test
  cases; within the eight-case default).
- Capture the Settings root, Appearance, Privacy, and Open Source Licenses
  semantics or screenshots needed to prove reachability, implemented choices,
  license separation, and absence of Full effects.

Broad checks after focused green:

- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug :app:assembleRelease`
- `git diff --check`

Do not rerun unchanged passing checks. Do not claim release-candidate status;
Gate 34B, Gate 35A, Gate 35B, and Gate 35C remain later work.

## Real-path limits and out of scope

- No live provider call, fallback verification, alert persistence, background
  polling, new provider, storage format, permission change, manifest change,
  or provider/network behavior change.
- No TalkBack service traversal, localization, automatic contrast audit, or
  release-candidate verification; Gate 30E remains deferred.
- If the emulator reaches a bounded platform timeout, retain the exact result
  in the verification ledger and stop repeating the same attempt.

## Completion handoff

When the acceptance boundary is verified, record changed files, exact focused
and broad commands, connected test-case results, artifact paths, skipped checks,
and any limits in this plan and append a concise self-contained entry to
`.codex/cycles/history.md`. If committed, synchronize this plan, roadmap,
README/specification status where affected, and cycle history after the commit.

## Result and evidence

- Existing production Settings/About behavior matched the planned contract; no
  production or disclosure-content correction was needed.
- Added connected boundary coverage for all nine supported Appearance controls
  and asserted that the unfinished `settings-effects-full` choice is absent.
  The fixture now supplies in-memory supported preference stores so the test
  exercises the managed persisted-choice surface.
- Focused unit baseline passed: `. scripts/android-env.sh && ./gradlew
  :app:testDebugUnitTest --tests '*AboutDisclosureStateHolderTest*'`.
- Connected API-37 `oxygen_starter` evidence passed: the Settings root and
  locations-back journey, plus the disclosure/provider-license/privacy
  journey, each completed 1/1 with no refetch and no permission request.
- Broad checks passed in one Gradle invocation:
  `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
  :app:testDebugUnitTest :core:testDebugUnitTest :app:assembleDebug
  :app:assembleRelease`, plus `git diff --check`.
- Artifacts: `.codex/test-artifacts/2026-09-14-gate-34a-settings-about-release-check/`;
  accepted connected results are under `settings-root-accepted/` and
  `settings-disclosures/`, with emulator preflight/logs under `emulator/`.
- The initial connected attempt exposed and fixed only test-fixture/query
  issues: ambiguous generic `Standard`/`Oxygen` text assertions and missing
  managed preference stores. Failed attempts remain retained under `settings-root/`,
  `settings-root-rerun/`, and `settings-root-final/`.

Limits: no live provider call, release-candidate verification, TalkBack service
traversal, localization, automatic contrast audit, or deferred Gate 30E work.
No commit was made in this execution; the verified changes are ready for the
user's normal commit decision.

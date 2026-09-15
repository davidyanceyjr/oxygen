# Gate 35C — Release-Candidate Audit, Disclosure Finalization, and Decision

**Status:** implemented; Gate 35C not verified/release-ready
**Cycle ID:** `2026-09-14-gate-35c-release-candidate-decision`
**Commit:** `001b7d5` (`Correct release-facing provider disclosure`)
**Next action:** obtain qualifying hosted CI for the candidate build inputs and
select a separately bounded emulator-platform follow-up before reconsidering
the release-candidate decision.

## Selected behavior and acceptance boundary

The installed Data Sources surface must describe active providers and actual
unfinished work without exposing internal release-candidate milestone wording.
The correction must not change provider selection, requests, forecast/cache
behavior, navigation, permissions, presentation state, or persisted settings.

Gate 35C is not release-ready unless the corrected disclosure, retained Gate
34B/35A/35B evidence, a fresh clean-state installed live-provider journey,
source/APK audits, applicable local checks, and qualifying hosted CI all pass
for the same candidate build inputs.

## Implemented change

- `app/src/main/kotlin/com/oxygen/weather/app/AboutDisclosureContent.kt`
  now names conditional GET/304 reuse and provider health/backoff as
  unfinished, and removes release-candidate wording from the roadmap-only
  disclosure.
- `app/src/test/kotlin/com/oxygen/weather/app/AboutDisclosureStateHolderTest.kt`
  asserts the remaining provider gaps and absence of milestone language.
- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`
  asserts the same facts at the installed Compose boundary while retaining
  link, no-refetch, and no-permission checks.

No provider, cache, location, appearance, navigation, manifest, dependency,
version, signing, or documentation-content change was made.

## Evidence and verification ledger

Artifacts: `.codex/test-artifacts/2026-09-14-gate-35c-release-candidate-decision/`.

- Baseline recorded HEAD `a33a5a3`, `origin/main` `a46cef9`, and the only
  pre-existing worktree change was this active plan.
- The red contract run failed as expected with one assertion for the missing
  old-production provider-health wording (`focused-jvm-red.exit-status=1`).
- Focused green: `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests com.oxygen.weather.app.AboutDisclosureStateHolderTest`
  passed (`focused-jvm-green.exit-status=0`).
- Broad checks passed once after the change:
  `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`,
  `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`,
  `. scripts/android-env.sh && ./gradlew :app:assembleDebug :app:assembleRelease`,
  and `git diff --check`.
- Final source audit found no production `SampleWeather`, no provider DTO
  references in `app/src/main/kotlin/com/oxygen/weather/app/ui`, no matching
  fabricated-zero patterns in the audited mapper/provider paths, and no
  release-candidate wording in production Data Sources content. The audit
  commands and paths are recorded in `static-source-audit-final-corrected.txt`.
- Release APK inspection used explicit repo-local Android tools. The unsigned
  APK is `app/build/outputs/apk/release/app-release-unsigned.apk`, package
  `com.oxygen.weather`, version `1`/`0.1.0`, min SDK 26, target SDK 37. It has
  Internet, network-state, and coarse-location permissions; one exported
  launcher activity; non-exported retained dependency components; and
  cleartext disabled. No debug/sample archive entries were found. The
  expected `apksigner` result is failure because the artifact is unsigned.
- The planned connected case passed once on API-37 x86_64 `oxygen_starter`:
  `HomeDashboardUiTest#settingsDisclosuresShowActiveProviderLicenseAndPrivacyBaseline`.
  The accepted XML, `test-results.log`, and `test-result.textproto` each show
  exactly one selected/completed test, zero skipped, zero failures, and zero
  errors under `connected-disclosure/`.
- Retained evidence was not rerun: Gate 34B/35A artifacts are under
  `2026-09-14-gates-34b-35a-data-source-mvp-core-verification/`; Gate 35A and
  35B repair records are under their named roots; Gate 35B presentation
  evidence is under `2026-09-14-gate-35b-mvp-presentation-accessibility-verification/`.
- Authorized hosted candidate attempt: PR `#17` at candidate SHA
  `fb4ab2319ce870ffec242b6b205187f12f3007cf` ran as Actions run
  `34917846117` and failed before project execution in
  `android-actions/setup-android@v3`; `sdkmanager` could not find the
  requested `tools` package. No hosted compile, unit-test, assemble, or
  whitespace result was produced.
- Hosted revalidation passed for the repaired candidate at SHA
  `826faed89556b9d4cb0d2ec61762a70a2d2c26d5` as Actions run
  `34918387599`: SDK setup, Gradle setup, debug compilation, app/core debug
  unit tests, debug APK assembly, and whitespace checks all passed in 1m29s.

## Gate blockers and limits

- The one authorized installed journey installed the debug APK successfully,
  cleared only Oxygen app data, confirmed coarse location was not granted,
  and started the app. The emulator immediately showed Android’s “Process
  system isn’t responding” dialog. Selecting “Wait” did not dismiss it, so
  first-run location entry, manual live search, real provider weather,
  corrected installed disclosures, and return-to-Home could not be exercised.
  Diagnostics are in `installed/platform-blocker-diagnostics.txt`; the
  emulator was stopped and confirmed offline. No retry or second install was
  made.
- The direct candidate push was rejected by protected `main`; the authorized
  PR trigger is open as `#17` from `candidate/gate-35c-ci-2026-09-14`.
- Hosted run `34917846117` remains a recorded setup failure for the original
  workflow, but qualifying revalidation passed as run `34918387599` after
  commit `86db325` moved to `android-actions/setup-android@v4` and requested
  only `platform-tools`. The emulator-platform follow-up remains separate.
- Gate 30E TalkBack traversal, localization, automatic contrast, live
  alert-detail availability, alert persistence/background behavior, signing,
  publication, and deferred provider/cache features remain unverified or out
  of scope.

README, `docs/OXYGEN_FULL_SPECIFICATION.md`, `DATA_SOURCES.md`, `PRIVACY.md`,
and `docs/data-sources/MET_NORWAY_FORECAST.md` were intentionally not changed:
the release-facing limitation remains factually applicable until the missing
installed journey is resolved.

## Post-commit synchronization

The active plan, roadmap, and live cycle history were synchronized after
commit `001b7d5`. The repository remains ahead of `origin/main`; no release,
publication, signed-artifact, or release-ready claim is made.

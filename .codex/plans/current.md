# Slice 33A — Dependency and Manifest Privacy Audit

**Status:** implemented; installed acceptance blocked by dependency-owned manifest findings
**Difficulty:** 1/10
**Cycle ID:** `2026-09-14-slice-33a-dependency-manifest-privacy-audit`

## Slice outcome

Establish and test the Android app’s privacy baseline at the dependency and
manifest boundary. The slice includes one safe production hardening change:
the application manifest explicitly disables cleartext traffic. Current
production endpoints are HTTPS, so this does not change the existing weather,
geocoding, alert, or external-link paths.

This slice does not change backup behavior. `allowBackup="true"` currently
exists, and the app stores selected location, saved locations, forecast cache,
and preferences locally. Changing backup or data-extraction policy would alter
device migration behavior and requires a separate product decision and repair
slice; 33A records the current behavior and its privacy implication.

## Implementation

1. In `app/src/main/AndroidManifest.xml`, add
   `android:usesCleartextTraffic="false"` to the application declaration.
2. Add `app/src/androidTest/kotlin/com/oxygen/weather/PrivacyManifestInstrumentedTest.kt`
   with focused installed-package checks for:
   - exactly the three currently declared permissions:
     `INTERNET`, `ACCESS_NETWORK_STATE`, and
     `ACCESS_COARSE_LOCATION`;
   - `usesCleartextTraffic == false`;
   - the launcher `MainActivity` being exported; and
   - no exported service, receiver, or provider being introduced.
3. Do not add a dependency, service, receiver, provider, permission, network
   security XML file, analytics SDK, or backup rule.
4. Use dependency output and the merged debug manifest to audit transitive
   dependencies, manifest merging, and component exposure. A prohibited SDK,
   unnecessary Play Services dependency, unused permission, background
   location path, or unexpected exported component is a finding. Do not fix a
   finding inside this slice unless it is the explicit cleartext hardening
   above; create a separately named repair slice.

## Acceptance boundary

The slice passes when all of these are true:

- the installed manifest test passes;
- the resolved `:app` and `:core` dependency trees contain no advertising,
  analytics, telemetry, account, cloud-sync, or unnecessary Play Services
  dependency;
- permission declarations match current production use, with location still
  optional and requested only after the user action;
- only the launcher activity is exported;
- no cleartext traffic is permitted and no custom network-security file is
  present;
- backup behavior is recorded as `allowBackup=true`, with its migration/privacy
  implication explicitly left for a separate decision; and
- no finding is silently downgraded to documentation work.

The result is an implementation and audit of this boundary, not a release
claim. Provider disclosures, provider terms, Settings navigation, and release-
candidate verification remain outside 33A.

## Files

Expected production/test changes:

- `app/src/main/AndroidManifest.xml`
- `app/src/androidTest/kotlin/com/oxygen/weather/PrivacyManifestInstrumentedTest.kt`

Inspection inputs:

- `app/build.gradle.kts`
- `core/build.gradle.kts`
- `gradle/libs.versions.toml`
- production location, provider, cache, and persistence code as needed to
  justify permission and backup findings

## Verification and evidence

Save command output, the merged manifest, the dependency audit, screenshots or
UI hierarchy only if an installed check needs them, and a short findings report
under:

`.codex/test-artifacts/2026-09-14-slice-33a-dependency-manifest-privacy-audit/`

Run once per unchanged environment:

```text
. scripts/android-env.sh && ./gradlew :app:dependencies :core:dependencies
. scripts/android-env.sh && ./gradlew :app:processDebugMainManifest
. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.PrivacyManifestInstrumentedTest
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
. scripts/android-env.sh && ./gradlew :app:lintDebug
git diff --check
```

The connected run is four or fewer focused test cases on one existing API-37
emulator session. Install once after the APK changes. If the emulator or
runner reaches a bounded timeout, record it and do not convert source review
into installed-test success.

## Required document updates after evidence

- `.codex/plans/current.md`: record the actual result, commands, artifact path,
  findings, and next action.
- `.codex/plans/mvp-roadmap.md`: mark 33A committed only after the change and
  evidence are committed; then identify 33B as the next specified candidate.
- `README.md`: add only verified privacy-boundary claims that are currently
  absent, including explicit cleartext blocking if the product-status section
  needs it; retain the backup limitation accurately.
- `docs/OXYGEN_FULL_SPECIFICATION.md`: update only if the audit exposes a
  specification mismatch or if the explicit HTTPS/backup policy is adopted by
  the project; do not silently turn an audit result into a new product rule.
- `.codex/cycles/history.md`: append a concise self-contained 33A result with
  changed files, tests, artifact path, limits, and commit state.

No document may claim that backup is disabled, TalkBack is verified, provider
disclosure is complete, or the app is release-ready.

## Next action

Select a separately named dependency/manifest exposure repair slice for the
injected permission and exported AndroidX components before claiming 33A
verified or committing this slice. Do not change backup policy or repair the
lint API-level finding under 33A.

## Execution result

Implemented the planned production hardening and test boundary:

- `app/src/main/AndroidManifest.xml` now explicitly sets
  `android:usesCleartextTraffic="false"`.
- Added `app/src/androidTest/kotlin/com/oxygen/weather/PrivacyManifestInstrumentedTest.kt`
  with four installed-package checks.
- Cleartext enforcement passed on API 37. The permission, exported-activity,
  and exported-component checks failed against the actual merged/package
  manifest; these are retained as concrete findings, not downgraded to docs.

## Evidence

Artifacts: `.codex/test-artifacts/2026-09-14-slice-33a-dependency-manifest-privacy-audit/`

- Dependency resolution and debug manifest processing passed.
- Debug assembly/install passed; one `oxygen_starter` API-37 emulator session
  was used and stopped cleanly.
- Connected result: 4 tests completed, 1 passed and 3 failed. The failures
  found `com.oxygen.weather.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`, exported
  `androidx.activity.ComponentActivity`, and exported
  `androidx.profileinstaller.ProfileInstallReceiver` (protected by `DUMP`).
- App/core debug unit tests passed.
- `git diff --check` passed.
- `:app:lintDebug` failed on the pre-existing min-SDK/API-30
  `LocationManager.getCurrentLocation` call in
  `AndroidDeviceLocationSource.kt:46`, plus 13 warnings; no lint repair was
  performed.

No production/test change is committed. README, specification, and roadmap
remain unchanged by this execution because the slice acceptance boundary is
red; backup remains `allowBackup=true`, and no release or privacy-complete
claim is made.

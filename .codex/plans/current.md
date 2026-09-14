# Slice 33B — Dependency-Owned Manifest Exposure Repair

**Status:** committed; installed privacy boundary verified
**Cycle ID:** `2026-09-14-slice-33b-dependency-manifest-exposure-repair`
**Commit:** `02f668f`

## Result

The production debug merge now removes AndroidX Core's generated dynamic-
receiver permission and Compose `ui-test-manifest`'s exported
`androidx.activity.ComponentActivity`. ProfileInstaller remains available, but
its receiver is explicitly non-exported. The installed privacy test requires
exactly `INTERNET`, `ACCESS_NETWORK_STATE`, and `ACCESS_COARSE_LOCATION`, and
allows only `com.oxygen.weather.MainActivity` to be exported.

Changed files:

- `app/src/main/AndroidManifest.xml`
- `app/src/androidTest/kotlin/com/oxygen/weather/PrivacyManifestInstrumentedTest.kt`

## Evidence

Artifacts: `.codex/test-artifacts/2026-09-14-slice-33b-dependency-manifest-exposure-repair/`

- Manifest merger ownership and dependency resolution passed. The retained
  report shows the three dependency findings rejected/overridden by the app
  manifest; the merged manifest contains only the three requested permissions.
- `:app:compileDebugAndroidTestKotlin` passed.
- Installed API-37 `PrivacyManifestInstrumentedTest`: 4 completed, 0 skipped,
  0 failed.
- Installed selected-cache production path: 1 completed, 0 skipped, 0 failed.
- Installed manual-location selection path: 1 completed, 0 skipped, 0 failed.
- `:app:testDebugUnitTest :core:testDebugUnitTest`, `:app:assembleDebug`, and
  `git diff --check` passed.
- Fresh APK installation succeeded. Package output confirms the exact
  permissions, launcher-only exported activity, and non-exported AndroidX
  receiver/provider/service entries.

The direct headless `MainActivity` launch was limited by a system “Process
system isn't responding” dialog before an app surface could be inspected. It
was recorded once and not repeated under the bounded emulator-timeout rule.
This does not upgrade the result to a visual launch claim. Backup policy,
TalkBack, provider disclosure, release readiness, and the pre-existing API-30
location lint finding remain outside this slice.

## Next action

Select the next bounded candidate from the roadmap; no later implementation
slice is implicitly started by this handoff.

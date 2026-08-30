# Active Cycle

Status: verified
Cycle ID: 2026-08-30-live-success-cache-failure-display
Mode: fix
Goal: Keep a live provider forecast visible when local forecast-cache persistence fails.
Roadmap context: Repair after Slice 18A and before Slice 18B Now visual baseline.
Branch or work context: local `main` ahead of `origin/main`.

## Contract

Selected behavior:
- A successful provider refresh remains displayable even if the local forecast cache cannot write or read back the bundle.
- When cache write/readback succeeds, the repository continues to emit the Room/storage readback bundle.
- Failed-refresh cache-read failures may still surface as `ForecastError.LocalCacheFailure`.
- Provider failures without usable cache keep the existing retryable no-cache path.
- No provider, selected-location, weather-value, UI paging, or sample-data behavior changes.

Acceptance boundary:
- `CachedWeatherRepository` emits provider success for successful live refreshes when local cache write/readback fails.
- Existing Room-backed success readback and stale-cache retention behavior remains covered.
- Installed app can show a live Now forecast after a stale/incompatible Room database prevents local cache access.

## Evidence

- focused: `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*CachedWeatherRepositoryTest'` passed.
- focused: `. scripts/android-env.sh && ./gradlew :core:connectedDebugAndroidTest` passed on `oxygen_starter(AVD) - 17`.
- real-path: cleared app data, selected `Florence, Alabama, United States` through the installed app, and captured a live successful Now page at `.codex/test-artifacts/2026-08-30-now-page-visual-baseline/live-after-clean-db-select-florence.png`.
- real-path: installed the modified app, deliberately corrupted `room_master_table.identity_hash` while preserving selected location, relaunched `com.oxygen.weather/.MainActivity`, and captured a live successful Now page at `.codex/test-artifacts/2026-08-30-live-success-cache-failure-display/live-with-corrupt-room-after-fallback-fix.png`.
- real-path: reset emulator app data after corrupt-DB verification, reselected `Florence, Alabama, United States`, confirmed one cached location in Room, and captured the normal live Now state at `.codex/test-artifacts/2026-08-30-live-success-cache-failure-display/live-after-reset-valid-cache.png`.
- broad: `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin` passed.
- broad: `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest` passed.
- broad: `. scripts/android-env.sh && ./gradlew :app:assembleDebug` passed.
- broad: `git diff --check` passed.

Artifact logs:
- `.codex/test-artifacts/2026-08-30-live-success-cache-failure-display/focused-core-cache.log`
- `.codex/test-artifacts/2026-08-30-live-success-cache-failure-display/core-room-connected.log`
- `.codex/test-artifacts/2026-08-30-live-success-cache-failure-display/broad-compile-debug-kotlin.log`
- `.codex/test-artifacts/2026-08-30-live-success-cache-failure-display/broad-unit-tests.log`
- `.codex/test-artifacts/2026-08-30-live-success-cache-failure-display/broad-assemble-debug.log`
- `.codex/test-artifacts/2026-08-30-live-success-cache-failure-display/git-diff-check.log`

## Phase Results

- covered: Focused core tests now encode provider-success fallback when cache write/readback fails.
- implemented: `CachedWeatherRepository` falls back to the provider success bundle only for successful live refreshes whose cache write/readback fails.
- verified: Focused core, Android Room boundary, installed-app corrupt-Room real-path exercise, and broad checks passed.

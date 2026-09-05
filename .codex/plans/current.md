# Slice 21 — Optional Device Location

Status: verified — implementation and verification complete; commit pending.

Execution started 2026-09-05. Cycle artifacts:
`.codex/test-artifacts/2026-09-05-slice-21-device-location/`.
Discovery confirms the existing selected-location/cache schemas can be retained.
Production and verification work are complete; commit pending.

Verification evidence:

- Focused unit tests passed: `:core:testDebugUnitTest --tests '*OpenMeteoTimeZoneResolverTest*'`;
  `:app:testDebugUnitTest --tests '*FirstRunLocationStateHolderTest*' --tests '*DeviceLocationSourceTest*'`;
  `:app:testDebugUnitTest :core:testDebugUnitTest`.
- Connected installed persistence test passed:
  `:app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.OfflineLaunchPersistenceInstrumentedTest`.
- Broad checks passed: `:app:compileDebugKotlin`; `:app:assembleDebug`; `git diff --check`.
- Installed screenshots saved under
  `.codex/test-artifacts/2026-09-05-slice-21-device-location/`:
  `entry-baseline.png`, `permission-or-progress.png`, `after-permission-grant.png`,
  `after-test-provider-start.png`, and `large-font-restored.png`.

Reviewed against local `main` at `1b527189fed7182c239f4fb800127217525cfec4` (2026-09-05). This is the active implementation authority for the bounded Slice 21 cycle. The completed Slice 20C record remains in `.codex/cycles/history.md`.

## Intent and acceptance boundary

After an explicit **Use my location** action, Oxygen may request coarse foreground location, obtain one approximate device point, resolve its IANA timezone, persist it as the selected `WeatherLocation`, and load Home through the existing installed forecast/cache path. Manual search remains completely usable before, during, and after any device-location outcome. No launch, refresh, restart, or background path requests permission or obtains a new fix.

This slice ends at a selected approximate device position and normal Home behavior. It does not turn the app into a continuously updating “current location” product.

## Verified prerequisites

- Slice 21’s only roadmap prerequisite, saved-location selection, is present: manual candidates and saved rows write `DataStoreSelectedLocationStorage` before `OxygenAppStateHolder.startHomeForecastLoad`; Room forecasts are keyed by `LocationId`.
- The installed `MainActivity` composes `OxygenAppStateHolder` with `DataStoreSelectedLocationStorage`, Room saved-location/cache storage, and `InstalledForecastRepositoryFactory`. The latter retains the active Open-Meteo → eligible MET Norway fallback/cache composition.
- `WeatherLocation` already requires a local `LocationId`, WGS84 point, nullable elevation, and IANA `ZoneId`; its current DataStore and Room records need no field or schema change.
- `OpenMeteoGeocodingMapper` already demonstrates the required stable-local-ID rule (4-decimal normalized coordinates and SHA-256); manual IDs must not change.
- `OxygenAppStateHolder` has only a scaffold permission command/result: grant currently shows `LocationLookupNotConnected`. `AndroidManifest.xml` declares neither location permission, and `MainActivity` has neither an Activity Result launcher nor a location adapter.
- `activeForecastRequestId` protects forecast emissions only. A device-location attempt needs a separate guard so a late permission/fix/timezone callback cannot write selected-location storage.
- Review live request on 2026-09-05: `GET /v1/forecast?latitude=43.0731&longitude=-89.4012&timezone=auto` returned metadata including `timezone: America/Chicago`; no weather variables were requested. The provider’s returned coordinates are grid metadata and are not the device point.

## Authoritative references

- `AGENTS.md`; `.codex/plans/mvp-roadmap.md` (Slice 21); `.codex/plans/current.md`; and the live recent-history contract in `.codex/cycles/history.md`.
- `docs/OXYGEN_FULL_SPECIFICATION.md` §§1, 10, 19, 39, 40, and 46: permission is optional and action-triggered; manual locations work fully; no Google Play Services core requirement; `WeatherLocation` owns an IANA zone; remote weather never uses the phone zone.
- `README.md`, `DATA_SOURCES.md`, `PRIVACY.md`, and `docs/data-sources/OPEN_METEO_FORECAST.md`; the provider-template obligations apply when extending the active Open-Meteo request shape.
- [Open-Meteo Forecast API](https://open-meteo.com/en/docs): `timezone=auto` resolves the coordinate’s IANA zone. [Android LocationManager](https://developer.android.com/reference/android/location/LocationManager): coarse permission can return an obfuscated position and `getCurrentLocation` is API 30+.

## Current repository boundary

Extend, do not duplicate:

- `app/src/main/kotlin/com/oxygen/weather/MainActivity.kt` and `OxygenApp.kt` for the Android permission-result boundary and lifecycle ownership.
- `OxygenAppStateHolder.kt` and `ui/firstrun/FirstRunLocationEntryScreen.kt` for one device-attempt state, accessible progress/error UI, and the existing selected-location/Home handoff.
- `app/src/main/AndroidManifest.xml` for `ACCESS_COARSE_LOCATION` only.
- `core/.../provider/WeatherProviders.kt` and `core/.../provider/openmeteo/` for a deliberately small provider-neutral coordinate-to-zone contract and its Open-Meteo implementation. Reuse `OpenMeteoHttpTransport`; do not force the full forecast parser/client to parse a metadata-only response.
- `SelectedLocationStorage`, Room cache/saved-location storage, `InstalledForecastRepositoryFactory`, and `WeatherLocation` unchanged. Do not add a DataStore key, Room entity/migration, cache namespace/table, or saved-place row.

## Implementation steps

1. **Document the active request before wiring it.** Extend `docs/data-sources/OPEN_METEO_FORECAST.md` with the metadata-only `latitude`, `longitude`, `timezone=auto` resolver request; required `timezone` response; error classification; no-cache/no-forecast semantics; coordinate privacy; attribution/license; and refreshed terms-review date. Correct only its stale activation/failover wording necessary to match the already active installed forecast path. Update the matching implemented-behavior/request disclosures in `DATA_SOURCES.md`, `PRIVACY.md`, `README.md`, and `AboutDisclosureContent.kt`: coarse permission is optional; a device point is obtained only after an explicit action; its coordinates are sent to Open-Meteo first for timezone resolution and then for normal forecast requests; the selected approximate position is stored locally; no background acquisition occurs.

2. **Add the narrow timezone-resolution boundary in `:core`.** Define one provider-neutral coordinate-to-`ZoneId` result/error contract, then implement `OpenMeteoTimeZoneResolver` beside the existing Open-Meteo clients. It must validate finite WGS84 input and the returned IANA identifier, map transport/HTTP/error-body/malformed/missing-zone outcomes to provider-neutral failures, and retain the original input point. It uses only the verified metadata request above; it must not emit weather, enter `WeatherRepository`, populate the forecast cache, or alter fallback selection. Add fixtures under `core/src/test/resources/providers/openmeteo/` for success, malformed/missing/invalid timezone, error body, and relevant HTTP failures.

3. **Add one cancellable Android point source in `:app`.** Inject an app-local `DeviceLocationSource` into `OxygenAppStateHolder`; its production `LocationManager` implementation is constructed by `MainActivity`, uses no Play Services, and returns exactly one result or a provider-neutral unavailable/failure/cancel outcome. Declare/request only `ACCESS_COARSE_LOCATION`. On each explicit action, `MainActivity` checks the actual grant: an existing grant goes directly to the same result path; otherwise use `ActivityResultContracts.RequestPermission`. Use `getCurrentLocation` with `CancellationSignal` on API 30+, and an API 26–29 one-shot listener with the same cleanup guarantees. Select only an enabled compatible provider; reject null, non-finite/out-of-range, or stale fixes. Bound the attempt to 20 seconds, cancel/remove callbacks on result, timeout, user cancellation, `onStop`, and destruction, and never fall back to arbitrary last-known coordinates. Do not request fine/background permission, add a foreground service, or add a dependency.

4. **Complete the existing state/UI path.** Replace `LocationLookupNotConnected` with a single attempt token spanning permission result, point acquisition, timezone resolution, selected-location write, and Home start. The entry screen shows locating/resolving status and an explicit cancel action; duplicate taps are coalesced. Search, saved-row selection, candidate selection, Back, About, cancellation, and activity stop invalidate the token; late callbacks do nothing. On denial/dismissal, unavailable provider/fix, timeout, resolver failure, or selected-location write failure, preserve the query/results/saved rows/previous selection and show a concise provider-neutral manual-search/retry message.

   On success, make `WeatherLocation(displayName = "Approximate device location", elevationMeters = null)` from the original device point and validated zone. Give it a deterministic `device-` local ID from the existing four-decimal coordinate normalization plus zone in a device-only hash namespace; do not change manual-location IDs or use a constant device ID. Write it through the existing selected-location storage before publishing it or calling `startHomeForecastLoad`. Do not automatically save it to the saved-locations list. On restart, restore that stored location/cache normally without permission or a new fix; a new device action is required to relocate.

5. **Keep the entry surface finished.** Preserve the existing 48dp controls and manual-search disclosure. Progress and failure must be readable text/semantics, not color alone; the approximate qualifier must remain visible and must not imply a street address, precise GPS, a supplied accuracy radius, or phone timezone. Capture a baseline and final installed entry-screen screenshot; inspect the completed flow on a compact display and large font.

## Exact acceptance criteria

- Ordinary fresh launch, restored launch, Home refresh, and manual search make no permission request or platform location call. Manual search can still select a real forecast with permission denied.
- Only an explicit tap can cause one coarse-permission request. Denial/dismissal/repeated denial neither loops nor opens Settings automatically; it leaves the manual surface usable and does not mutate selected storage/cache/Home.
- A granted tap obtains at most one bounded foreground point. Existing coarse grant avoids a new dialog. No fine/background permission, periodic listener, background work, or Google Play Services dependency is present.
- The selected device location uses the original valid approximate point, an Open-Meteo-validated IANA zone, the fixed approximate display name, null elevation, and a stable device-only local ID. Invalid/missing zone or a bad/stale point never produces a `WeatherLocation`; neither UTC nor the handset zone may substitute.
- A successful device selection commits selected-location storage before the existing `startHomeForecastLoad`; normal Open-Meteo/MET Norway fallback, cache, provenance, unit presentation, and stale behavior then apply unchanged. No resolver response becomes forecast/cache data.
- A manual/saved selection or exit while device resolution is pending wins permanently: a late permission, point, timeout, resolver, or forecast callback cannot overwrite selected storage, selected Home, or its error state.
- Restart after a successful device selection restores the approximate location and matching cached forecast offline without a permission prompt/acquisition. The device position is not added to saved locations, and existing manual/saved IDs, selection, save, and remove behavior remain compatible.

## Focused tests and evidence

- Core resolver parser/client/repository tests: exact metadata-only query; input and IANA-zone validation; original-point preservation; malformed/missing/error-body, 4xx/429/5xx, and offline mapping; no forecast fields/cache effects.
- App state-holder tests in `FirstRunLocationStateHolderTest`: no-action baseline; permission command/result; progress, denial, unavailable, timeout, resolver/storage failure; success commit-before-Home; manual/saved/Back/cancel/activity-stop races; duplicate tap coalescing; and late callbacks cannot persist over selection B.
- App-local device-source tests behind a fakeable platform facade: one completion, provider absence, null/stale/invalid fix, timeout, cancellation/cleanup, and both API 26–29 listener and API 30+ current-location branches. A fake point must be labelled controlled input.
- Extend `OfflineLaunchPersistenceInstrumentedTest` with production DataStore + Room: persisted device `WeatherLocation`/label/cache restores offline without source invocation or permission flow. Re-run saved-location regression coverage to show the device selection was not saved and existing saved behavior survives.
- Add/extend Compose tests for disabled duplicate action, locating/resolving/cancel, error/retry/manual-search availability, approximation text, and 48dp/semantic control behavior. Retain focused logs and screenshots under `.codex/test-artifacts/<actual-cycle-id>/`.

## Real-path evidence

On an installed debug build, start with no grant, tap **Use my location**, grant coarse permission, and prove the production chain: Android `LocationManager` → Open-Meteo metadata resolver → selected DataStore write → installed forecast factory → ready Home with the approximate label. Repeat with permission denied and with location services/provider unavailable, confirming manual search still reaches Home. Capture baseline/final screenshots and record device/API, permission state, provider availability, request outcome, and observed UI. If the emulator cannot supply a genuine platform fix, exercise on an available physical device; that limitation is a blocker, not a passed real-path test.

## Broad verification

```sh
. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*OpenMeteoTimeZone*'
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*FirstRunLocationStateHolderTest*'
. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest --tests '*OfflineLaunchPersistenceInstrumentedTest*'
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

Use `scripts/start-emulator.sh` and `scripts/install-debug.sh` for installed evidence. Record commands actually run and any unavailable API-26–29 or physical-device condition; compilation and assembly alone are not slice evidence.

## Explicitly out of scope

Fine/background location, continuous tracking, geofencing, location history, foreground services, Play Services, reverse geocoding/city naming, saved-device locations, elevation/altitude, location settings deep links, provider/cache schema changes, a new forecast provider, changes to manual IDs, Home composition, fallback rules, units, alerts, maps/radar, widgets, notifications, translations beyond touched strings, release readiness, and MVP claims.

## Completion gate

Do not mark this slice covered, implemented, verified, or committed until the production path and every acceptance criterion have corresponding focused evidence, installed real-path evidence, accurate disclosures, and clean broad checks. Keep artifacts ignored under the actual cycle directory; record their paths and exact outcomes in the activated current plan and then append the concise history entry. Leave unrelated existing changes untouched.

## Unresolved blockers

None at planning time. The API-26–29 compatibility branch and a real coarse-fix device/emulator remain verification obligations; report an unavailable runtime capability as a blocker rather than replacing it with a mock success.

## Review Resolution

- Rebased the plan from `bfb2970` to current `1b52718`; removed the obsolete claim that saved-location and unit prerequisites were not shipped.
- Corrected the architecture to the actual permission scaffold, selected-location DataStore, Room cache, installed fallback factory, and forecast-only request client.
- Replaced speculative full-forecast timezone lookup with the verified metadata-only Open-Meteo `timezone=auto` request and required provider-neutral validation.
- Added the missing lifecycle/attempt-token race boundary, persistence-before-publish rule, and precise no-schema-change constraint.
- Narrowed verification to resolver, Android source, state, persistence, Compose, and an installed production chain; removed unsupported completion claims and made real device-fix availability explicit.

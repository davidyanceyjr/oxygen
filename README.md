# Oxygen Weather

Oxygen is a free, open-source, no-ads Android weather application built with Kotlin and Jetpack Compose.

This repository is an early Android app, not an MVP, beta, release candidate,
or finished weather product.

Oxygen source code is licensed under GPL-3.0-or-later. Weather data,
geocoding data, third-party dependency licenses, and provider attribution are
tracked separately in `DATA_SOURCES.md`, `THIRD_PARTY_LICENSES.md`, `NOTICE`,
and `docs/data-sources/`.

## Implemented in the installed app

- Manual location search through the Open-Meteo geocoding path.
- Explicit selected-location Open-Meteo forecast retrieval.
- Provider-neutral Home loading, error/retry, success, source, update,
  provenance, and disclosure presentation.
- Settings/About surfaces for Data Sources, Privacy, and Open Source Licenses.
- Oxygen package/application identity, theme foundation, and Compose Home UI.

## Implemented but not active in the installed app

- MET Norway forecast provider path and core fallback-selection behavior.
- Repository-level forecast cache/stale behavior through
  `FileForecastCacheStorage`.
- Provider-neutral Home stale-success presentation coverage for cached
  refresh-failure states.

## Not implemented yet

- Installed-app offline forecast restoration.
- Saved-location persistence.
- Unit preferences.
- Official weather alert lookup.
- Persisted appearance/effects/layout settings.
- Installed-app MET Norway fallback wiring.
- Release-candidate verification.

## Repository structure

- `:app` Android application, Compose UI, Home surface, About surfaces, theme,
  and sample/scaffold preview path.
- `:core` provider-neutral domain models, provider interfaces, Open-Meteo and
  MET Norway provider paths, fallback selection, and cache infrastructure.
- `docs/OXYGEN_FULL_SPECIFICATION.md` is the implementation authority.

## Build

On Linux/macOS:

```bash
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
```

The helper script sets `JAVA_HOME`, Android SDK paths, Android user directories,
and repo-local Gradle state. This project includes its own local Android SDK,
AVD home, runtime directory, and standard Gradle 9.7.0 wrapper JAR.

## Run

This is an Android app. To run it with the local emulator setup:

```bash
scripts/list-avds.sh
scripts/start-emulator.sh
scripts/install-debug.sh
```

Run `scripts/start-emulator.sh` in one terminal, then run
`scripts/install-debug.sh` in another terminal. The install script waits for the
emulator to finish booting, installs the Oxygen debug APK, and launches
`com.oxygen.weather/.MainActivity`.

`scripts/start-emulator.sh` defaults to the repo-local `oxygen_starter` AVD
in headless mode. To force a visible emulator window on a desktop machine:

```bash
OXYGEN_EMULATOR_WINDOW=1 scripts/start-emulator.sh
```

The older compatible variable also works:

```bash
SPACE_GAME_EMULATOR_WINDOW=1 scripts/start-emulator.sh
```

## Important

The installed app uses active Open-Meteo production paths for manual location
search and selected-location forecasts. The retained sample weather bundle is
scaffold/preview data only, not the production Home path.

MET Norway is implemented as a forecast provider path with verified core
fallback-selection behavior, but it is not wired as the active installed-app
forecast fallback in this build.

Core now includes a repository-level forecast cache wrapper that can persist one
provider-served forecast bundle and emit the stored current/hourly/daily rows
back through the repository boundary. When that wrapper is used, a foreground
refresh failure can retain the same selected location's cached forecast as a
stale success with explicit refresh-failed metadata. The durable cache wrapper
is not wired into the installed app yet.

Saved-location persistence, installed-app offline forecast cache behavior, unit
preferences, alerts, air quality, and radar are not implemented yet.

## Specification

Read:

```text
docs/OXYGEN_FULL_SPECIFICATION.md
```

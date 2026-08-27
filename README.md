# Oxygen Weather — Android Scaffold

Oxygen is a free, open-source, no-ads Android weather application built with Kotlin and Jetpack Compose.

This scaffold was derived from an Android/Kotlin/Compose build structure while
intentionally excluding unrelated application code and generated build outputs.

## What is included

- The same baseline Android/Compose build-version family as the source scaffold.
- `:app` + `:core` project structure.
- Oxygen package/application identity.
- Provider-neutral weather domain models.
- Forecast, alert, air-quality, geocoding, radar, and repository interfaces.
- An Oxygen theme engine with three initial visual directions: Oxygen, Paper, and Terminal.
- A procedural Compose weather scene proof of concept.
- A polished sample home screen using explicitly synthetic weather data.
- `docs/OXYGEN_FULL_SPECIFICATION.md` as the implementation authority.

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
back through the repository boundary. It is not wired into the installed app yet.

Saved-location persistence, installed-app offline forecast cache behavior, unit
preferences, alerts, air quality, and radar are not implemented yet.

## Specification

Read:

```text
docs/OXYGEN_FULL_SPECIFICATION.md
```

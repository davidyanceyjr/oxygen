# Oxygen Weather

Oxygen is a free, open-source, no-ads Android weather application built with
Kotlin and Jetpack Compose. Its intended use is simple: make useful weather
information available to everyone without advertising, behavioral tracking,
subscriptions, mandatory accounts, or a single locked-in weather vendor.

This repository is an early Android app, not an MVP, beta, release candidate,
or finished weather product.

Oxygen source code is licensed under GPL-3.0-or-later. Weather data,
geocoding data, third-party dependency licenses, and provider attribution are
tracked separately in `DATA_SOURCES.md`, `THIRD_PARTY_LICENSES.md`, `NOTICE`,
and `docs/data-sources/`.

## Implemented in the installed app

- Manual location search through the Open-Meteo geocoding path.
- Manual selected-location change from the installed Home screen.
- Explicit selected-location Open-Meteo forecast retrieval.
- Last selected location persistence through the local DataStore path.
- Offline restoration of the last forecast for the selected location through the
  local Room forecast-cache path.
- Foreground refresh failure handling that keeps a useful cached forecast
  visible with stale/source/update context where available.
- Provider-neutral Home loading, error/retry, success, source, update,
  provenance, and disclosure presentation.
- Standard Home paged interaction foundation with Now, Hourly, Daily, and
  Details pages.
- Standard Home Now, Hourly, and Daily visual baselines.
- Settings/About surfaces for Data Sources, Privacy, and Open Source Licenses.
- Oxygen package/application identity, theme foundation, and Compose Home UI.

## Implemented but not active in the installed app

- MET Norway forecast provider path and core fallback-selection behavior.
- File-backed forecast cache storage retained as a core repository boundary
  implementation, while the installed app uses Room storage.

## Not implemented yet

- Multiple saved locations and saved-location switching/removal.
- Unit preferences.
- Official weather alert lookup.
- Details visual baseline for the Standard Home paged UI.
- Persisted appearance/effects/layout settings.
- Installed-app MET Norway fallback wiring.
- Release-candidate verification.

## Repository structure

- `:app` Android application, Compose UI, Home surface, About surfaces, theme,
  and sample/scaffold preview path.
- `:core` provider-neutral domain models, provider interfaces, Open-Meteo and
  MET Norway provider paths, fallback selection, and cache infrastructure.
- `docs/OXYGEN_FULL_SPECIFICATION.md` is the implementation authority.

## Requirements

To clone and build this project, use:

- Git.
- Linux or macOS shell environment.
- JDK 26, or another JDK compatible with the Android Gradle Plugin used by this
  repository. `scripts/android-env.sh` defaults `JAVA_HOME` to
  `/usr/lib/jvm/java-26-openjdk` when `JAVA_HOME` is not already set.
- Android SDK command-line tools and platform packages. `scripts/android-env.sh`
  expects the SDK at `.android-sdk` by default and adds
  `.android-sdk/platform-tools`, `.android-sdk/emulator`, and
  `.android-sdk/cmdline-tools/latest/bin` to `PATH`.
- Android SDK packages for the current build: `platforms;android-37.0`,
  `build-tools;37.0.0`, and `platform-tools`. The repo-local emulator scripts
  also require `emulator` and an Android 37 Google APIs x86_64 system image.
- Repo-local runtime/cache directories `.android-sdk`, `.android`,
  `.android-runtime`, and `.gradle` are intentionally ignored and must not be
  committed.
- Network access for the first dependency/provider fetches unless the required
  Gradle and Android SDK artifacts are already cached locally.

The repository includes the Gradle wrapper and uses these Android module
settings:

- `:app`: Android application, `compileSdk = 37`, `minSdk = 26`,
  `targetSdk = 37`.
- `:core`: Android library, `compileSdk = 37`, `minSdk = 26`.

## Build Instructions

On Linux/macOS:

```bash
git clone <repository-url>
cd oxygen
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
```

The helper script sets `JAVA_HOME`, Android SDK paths, Android user directories,
and repo-local Gradle state. A prepared workspace may already have the local SDK
and AVD directories populated, but a fresh clone must provision the required
Android SDK packages before the Gradle commands can run. The project includes
the standard Gradle 9.7.0 wrapper JAR.

## Run Instructions

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

The installed app persists the last selected location locally and wraps the
active Open-Meteo forecast path with a Room-backed forecast cache. It can
restore the selected location's last cached forecast when launched without
network, and a foreground refresh failure can retain the same selected
location's cached forecast as a stale success with explicit refresh-failed
metadata. If a live provider refresh succeeds while local forecast-cache
persistence fails, the live provider forecast remains displayable.

Multiple saved locations, unit preferences, alerts, air quality, radar, and
installed-app MET Norway fallback behavior are not implemented yet.

## Specification

Read:

```text
docs/OXYGEN_FULL_SPECIFICATION.md
```

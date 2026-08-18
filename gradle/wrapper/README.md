# Wrapper note

The source `space_game` repository contains the normal `gradle-wrapper.jar` for Gradle 9.7.0.
The connected GitHub interface allowed the scaffold to inspect that binary but did not expose it as a mountable local file for the generated ZIP.

The root `gradlew` in this scaffold therefore bootstraps the same Gradle 9.7.0 distribution on Linux/macOS if the JAR is absent.

For exact standard-wrapper behavior, copy:

`space_game/gradle/wrapper/gradle-wrapper.jar`

into this directory.

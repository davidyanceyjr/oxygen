# Slice 33B — Provider Disclosure and Local Data Privacy Audit

**Status:** verified; ready for commit
**Cycle ID:** `2026-09-14-slice-33b-provider-disclosure-local-privacy-audit`
**Difficulty:** 4/10

## Selected behavior

The installed Settings disclosure surfaces must truthfully describe only the
providers that can currently fetch or serve data through production paths, and
must make applicable attribution, licensing, privacy implications, and
reachability observable without changing forecast semantics, provider
selection, persistence, permissions, or the repaired manifest boundary.

This is a consistency-and-evidence slice. Existing behavior is presumed
correct only after it is traced from the installed wiring to the disclosure
text and exercised at the Settings boundary.

## Acceptance boundary

Pass only when all of the following are demonstrated:

- The active-provider inventory matches production wiring for forecast,
  fallback forecast, geocoding, timezone resolution, and official alerts.
- `DATA_SOURCES.md`, provider contracts, specification status, and in-app Data
  Sources/Privacy/Open Source Licenses content agree on active versus
  roadmap-only providers, attribution, licenses, request data, and known
  limitations.
- Data Sources, Privacy, and Open Source Licenses are reachable from the
  installed Settings surface and their meaningful disclosure text and links
  are visible/semantically exposed.
- No provider is called active/current solely because it is specified, has a
  contract, or has a test double; it must be able to fetch or serve data in a
  production path.
- Existing manifest permissions, exported-component restrictions, provider
  provenance, cache behavior, and forecast/alert request behavior remain
  unchanged.

## Intended files

Inspect first; modify only where an observed inconsistency requires it:

- `DATA_SOURCES.md`
- `docs/data-sources/OPEN_METEO_FORECAST.md`
- `docs/data-sources/OPEN_METEO_GEOCODING.md`
- `docs/data-sources/MET_NORWAY_FORECAST.md`
- `docs/data-sources/NWS_ALERTS.md`
- `app/src/main/kotlin/com/oxygen/weather/app/AboutDisclosureContent.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/settings/SettingsScreen.kt`
- `app/src/debug/AndroidManifest.xml`
- `app/src/test/kotlin/com/oxygen/weather/app/AboutDisclosureStateHolderTest.kt`
- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`

Do not add a provider, alter network/repository behavior, introduce new
storage, rename existing settings destinations, or rewrite unrelated release
documentation.

## Execution sequence

1. Capture a clean baseline: working-tree status, current disclosure tests,
   and the existing focused app/core unit-test baseline if the environment is
   unchanged.
2. Build an evidence matrix from production constructors/wiring, provider
   contracts, `DATA_SOURCES.md`, specification disclosures, and the three
   installed disclosure destinations. Mark each statement as active,
   roadmap-only, or unsupported; do not infer activity from documentation.
3. Add or adjust only meaningful boundary tests for the matrix gaps. Tests
   must assert user-visible disclosure state/reachability and provider
   attribution semantics, not source-file strings or symbol existence alone.
4. Make the smallest production or Markdown correction required by a failing
   boundary. Keep provider-neutral models and existing Settings navigation
   intact.
5. Run focused unit tests, then one bounded connected Settings/disclosure
   journey on the existing emulator session if available. Capture screenshots
   and hierarchy/XML evidence only for the acceptance boundary.
6. Run applicable broad checks, review the diff for semantic drift and slop,
   and stop if a provider-term or installed-path fact cannot be established.

## Planned evidence and commands

Focused:

- `./gradlew :app:testDebugUnitTest --tests '*AboutDisclosureStateHolderTest*'`
- the smallest relevant named connected disclosure test(s), capped at eight
  test cases, using the existing emulator lifecycle and saved artifacts under
  `.codex/test-artifacts/2026-09-14-slice-33b-provider-disclosure-local-privacy-audit/`

Broad, after focused green:

- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`

If source or test inputs do not change, reuse passing evidence rather than
rerunning it. If the emulator reaches a bounded platform timeout, retain the
exact result and do not claim installed verification.

## Out of scope

TalkBack service traversal, localization, new provider integration, provider
conditional requests/304 handling, alert persistence/background polling,
release-candidate verification, backup-policy changes, manifest changes, and
any deferred Gate 30E work remain outside this slice.

## Result and evidence

- Existing production provider wiring and disclosure contracts agree for
  Open-Meteo forecast/timezone, Open-Meteo/GeoNames geocoding, MET Norway
  fallback forecast, and foreground NOAA/NWS alerts. No disclosure text or
  provider behavior change was needed.
- The installed Settings journey reached Data Sources, Privacy, and Open
  Source Licenses, exposed the required attribution/license/privacy text and
  links, opened all five configured disclosure URLs through the injected URI
  boundary, preserved the forecast request count, and made no permission
  request: 1 completed, 0 skipped, 0 failed.
- The journey initially exposed a regression from the prior manifest repair:
  Compose's test-only `ComponentActivity` was absent from the target debug
  app. The final fix scopes a non-exported declaration to
  `app/src/debug/AndroidManifest.xml`; it is absent from the release APK.
- Focused unit baseline passed: `:app:testDebugUnitTest --tests
  '*AboutDisclosureStateHolderTest*'`.
- Broad checks passed: `:app:compileDebugKotlin`, app/core debug unit tests,
  `:app:assembleDebug`, `:app:assembleRelease`, and `git diff --check`.
- Artifacts: `.codex/test-artifacts/2026-09-14-slice-33b-provider-disclosure-local-privacy-audit/`.

Limits: provider terms were reviewed against the repository's dated provider
contracts; no live provider call or release-candidate verification was added.
TalkBack service traversal, localization, new providers, alert persistence,
conditional requests, backup-policy changes, and Gate 30E remain out of scope.

## Handoff

On success, append a self-contained cycle-history entry with changed files,
actual evidence, artifacts, limits, and commit state; then synchronize this
plan and affected disclosure authorities after any commit. Do not mark the
slice implemented or verified from documentation or compilation alone.

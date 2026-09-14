# Gates 34B + 35A — Data-Source and MVP Core Verification Draft

**Status:** verified; combined execution session with separate acceptance gates
**Cycle ID:** `2026-09-14-gates-34b-35a-data-source-mvp-core-verification`
**Difficulty:** 7/10

## Schedule

Run Gate 34B and Gate 35A in one emulator/build session to reuse the installed
APK, provider disclosures, provenance observations, and core weather journey.
Keep separate evidence folders, acceptance results, and status entries because
34B verifies release-facing data-source claims while 35A verifies user-visible
MVP behavior.

Order:

1. Gate 34B — Data-Source Release Check.
2. Gate 35A — MVP Core Behavior Verification.
3. Stop and record any production defect as a separately named repair slice;
   do not silently widen either gate.

## Gate 34B selected behavior and acceptance boundary

The installed Data Sources surface must truthfully identify active providers
and expose the matching attribution, privacy, and licensing information.

Pass only when:

- active providers are limited to Open-Meteo forecast, Open-Meteo/GeoNames
  geocoding, MET Norway fallback forecast, and NWS official alerts;
- forecast, geocoding, alert, fallback, attribution, privacy, and license
  claims match the provider contracts and installed production wiring;
- required provider and attribution/privacy links are reachable through the
  installed URI boundary;
- provider disclosure navigation preserves the selected forecast, alert
  semantics, and request counts; and
- no inactive, sample, or unsupported provider is described as active.

## Gate 35A selected behavior and acceptance boundary

The installed app must support the MVP core journey without permission,
provider, cache, or provenance shortcuts.

Pass only when the bounded installed and focused evidence demonstrates:

- manual first-run location search without location permission;
- current, hourly, and daily weather from the Open-Meteo default path;
- explicit refresh and truthful source/update/provenance state;
- eligible Open-Meteo failure serving MET Norway fallback data;
- offline restoration and stale-after-refresh-failure behavior;
- saved-location add, select, and remove;
- unit selection/remapping without mutating canonical forecast data;
- official-alert lookup remains independent of forecast fallback and retains
  truthful no-alert or available-alert state; and
- no sample data, fabricated values, provider DTOs, or provider-specific
  errors enter the production Home presentation path.

## Intended files and evidence

Inspect first; modify only for a demonstrated production mismatch:

- `app/src/main/kotlin/com/oxygen/weather/app/AboutDisclosureContent.kt`
- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`
- `app/src/androidTest/kotlin/com/oxygen/weather/app/InstalledFallbackRepositoryInstrumentedTest.kt`
- `app/src/androidTest/kotlin/com/oxygen/weather/PrivacyManifestInstrumentedTest.kt`
- focused repository/state-holder tests under `app/src/test/`
- `docs/data-sources/*.md`, `README.md`, and
  `docs/OXYGEN_FULL_SPECIFICATION.md` only if an observed claim is wrong

Artifacts:

`.codex/test-artifacts/2026-09-14-gates-34b-35a-data-source-mvp-core-verification/`

Keep `gate-34b/` and `gate-35a/` evidence separate, with one verification
ledger for the shared emulator session.

## Execution and verification budget

1. Inspect the clean worktree, current provider wiring, disclosure content,
   and existing focused tests.
2. Run only the focused unit tests needed for disclosure, repository fallback,
   cache/provenance, saved locations, units, alerts, and Home state behavior.
3. Build/install once on the existing API-37 emulator session if the APK
   changes. Run the smallest relevant connected cases, capped at eight test
   cases per implementation slice unless a gate requires more and the ledger
   explains why.
4. Exercise the installed Data Sources journey, then the core Home journey;
   capture screenshots or semantic dumps only where they prove the two gates.
5. Run applicable compile, app/core unit, debug/release assembly, and
   `git diff --check` commands once after focused evidence is green.

Planned baseline/broad commands:

```sh
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:assembleDebug :app:assembleRelease
git diff --check
```

## Out of scope and limits

- No new provider, provider contract, persistence format, permission, backup
  policy, alert persistence, background polling, notification, or UI feature.
- No release-candidate claim; Gate 35B and Gate 35C remain later gates.
- No TalkBack service traversal, localization, automatic contrast audit, or
  deferred Gate 30E work.
- If the emulator reaches a bounded platform timeout, record the exact result
  and stop repeating that attempt.

## Handoff

Record each gate's status independently. If both pass, append self-contained
history entries and synchronize the roadmap, README/specification claims, and
this plan with the evidence before selecting Gate 35B.

## Result and evidence

Gate 34B passed. The installed Data Sources journey reached the active provider,
privacy, and Open Source Licenses disclosures, opened all five configured URI
links through the injected URI boundary, preserved the forecast request count,
and made no permission request. The disclosure claims matched the active
Open-Meteo forecast/timezone and geocoding, GeoNames, MET Norway fallback, and
NWS alert contracts. No production or disclosure-content change was needed.

Gate 35A passed. The eight planned connected cases passed 1/1 on API-37
`oxygen_starter`, covering manual location search, eligible MET Norway fallback,
Room fallback restoration and stale refresh failure, later Open-Meteo
replacement, offline launch restoration, units, and saved-location selection
and removal. The gate-specific no-alert case also passed 1/1 to prove the
truthful no-alert Home boundary; this was the documented ninth case because
the gate explicitly requires official-alert behavior. Fallback cases retained
provider provenance and no-alert state; no sample data entered Home.

Focused evidence passed:

- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`

Broad checks passed:

- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin :app:assembleDebug :app:assembleRelease`
- `git diff --check`

Connected artifacts are retained under
`.codex/test-artifacts/2026-09-14-gates-34b-35a-data-source-mvp-core-verification/`,
with the shared emulator session under `emulator/`, Gate 34B under `gate-34b/`,
and Gate 35A cases under the `gate-35a-*` directories. The debug APK was
installed once after emulator recovery. No provider, persistence, manifest,
or product files changed; README and specification claims remain accurate.

Limits: no live-provider/manual network journey, release-candidate decision,
TalkBack service traversal, localization, automatic contrast audit, alert
persistence/background polling, or deferred Gate 30E work. The next planned
candidate is Gate 35B.

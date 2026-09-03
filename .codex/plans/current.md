# Active Cycle

Status: committed
Cycle ID: 2026-09-02-standard-home-visual-convergence
Mode: feature
Goal: Resume Slice 18J, Standard Home Visual Convergence, after verified
Slice 18J-R restored the installed Open-Meteo ready forecast path. This resumed
cycle is limited to completing the remaining installed-app visual evidence,
review, and status record for the already in-progress 18J Home presentation
slice.
Roadmap context: Slice 18J is the final Standard Home visual convergence gate
before Slice 19A saved-location persistence. Slice 18J-R verified that a real
manual Madison Open-Meteo selection now reaches a ready Home forecast and cached
stale restore path, so the prior 18J installed-evidence blocker is no longer
active. Slice 19A remains blocked until Slice 18J is committed.

## Contract

Selected Behavior:
- The installed Standard Oxygen Home surface is verified through the real
  Open-Meteo manual-location path for Now, Hourly, Daily, Details, and a
  restored/stale or refresh-failed cached Home state.
- The default Home presentation remains weather-first, atmospheric, and
  recognizably Oxygen compared with the committed Slice 18H/18I reference while
  preserving semantic page structure and operational state copy.
- Effects-disabled rendering remains complete and understandable where feasible
  to exercise during this evidence cycle.
- Existing provider, repository, Room cache, DataStore selected-location,
  manual-location, refresh/retry, stale/cache, provenance, data-source
  disclosure, compact phone, large-font, TalkBack, touch-target, and
  one-handed ergonomics behavior remains intact.
- The verified Slice 18J-R Open-Meteo parser/repository recovery remains part
  of the working tree until committed with this 18J recovery/evidence sequence.

Acceptance Boundary:
- Installed-app evidence from `oxygen_starter` shows a real manual
  Open-Meteo-selected location reaching ready Home states for Now, Hourly,
  Daily, and Details without `SampleWeather.bundle`, mocked provider success,
  fabricated fallback data, or UI suppression of provider errors.
- Installed evidence also shows one cached stale/restored or refresh-failed
  Home state for the same selected location.
- Focused Home presentation/state and Open-Meteo recovery tests pass for the
  behavior already changed in Slice 18J and Slice 18J-R.
- Broad verification passes:
  `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
  `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
  `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
  `git diff --check`
- Review records concrete visual improvements over the committed Slice 18H/18I
  reference and confirms no unresolved regression against accessibility,
  behavior, provider, persistence, cache, or provenance baselines.

Out of Scope:
- Saved-location persistence, saved-location list/select/remove UI, or
  concurrency behavior.
- Unit preference persistence or conversion.
- Device-location expansion.
- Official alert provider implementation.
- Persisted theme/layout/effects settings.
- Paper/Terminal theme completion.
- New weather-provider fields added solely for richer decoration.
- Radar, maps, air quality, pollen, widgets, background refresh, notifications,
  release readiness, or MVP readiness.
- Additional Open-Meteo recovery behavior beyond preserving the verified 18J-R
  fix.
- Rewriting completed 18-series history.

## Implementation Plan

1. Confirm resumed baseline:
   - Read Slice 18J roadmap/spec sections, active plan, live recent cycle
     history, prior 18J blocked artifacts, and verified 18J-R artifacts.
   - Record `git status --short` before any production edits and preserve
     unrelated existing changes.

2. Real-path evidence:
   - Build/install the current debug app on `oxygen_starter`.
   - Use the real manual location path to search/select Madison, Wisconsin,
     United States, unless provider availability requires a documented alternate
     real result.
   - Capture installed screenshots and hierarchies for Now, Hourly, Daily, and
     Details under
     `.codex/test-artifacts/2026-09-02-standard-home-visual-convergence/`.
   - Exercise one cached stale/restored or refresh-failed Home state for the
     same selected location and capture screenshot/hierarchy evidence.
   - Capture Effects Off evidence if reachable without adding new behavior.

3. Focused checks:
   - Run focused Home presentation/state checks relevant to 18J.
   - Run focused Open-Meteo checks to preserve the 18J-R unblocker.
   - Save logs under the cycle artifact directory.

4. Broad checks:
   - Run the required broad verification commands and save logs under the cycle
     artifact directory.

5. Review and handoff:
   - Compare installed 18J evidence with committed Slice 18H/18I reference
     screenshots and record visible improvements.
   - Confirm no scope leakage into saved locations, unit preferences,
     provider behavior beyond 18J-R, fallback wiring, alerts, air quality,
     radar/maps, widgets, background refresh, release, or MVP readiness.
   - If verified, update this file, append concise cycle history evidence, and
     leave Slice 18J ready for commit. Slice 19A remains deferred until that
     commit exists.

## Phase Results

- specified: Slice 18J is specified in `.codex/plans/mvp-roadmap.md` and
  `docs/OXYGEN_FULL_SPECIFICATION.md` as the Standard Home visual convergence
  gate before saved-location persistence.
- planned: This document resumes only the remaining Slice 18J installed-app
  visual evidence and review after Slice 18J-R removed the provider-path
  blocker.
- covered: Focused HomeForecast state checks, focused Home Compose
  instrumentation, and focused Open-Meteo recovery checks passed for the
  already implemented 18J presentation behavior and the 18J-R provider-path
  unblocker.
- implemented: Slice 18J Home presentation behavior was already in progress at
  commit `15fc10e`; this resumed cycle made no new Home production-code changes.
  The 18J-R Open-Meteo parser/repository recovery remains in the working tree
  and is required for the installed 18J real-path evidence.
- verified: The installed app on `oxygen_starter` used the real manual
  Open-Meteo path to search `Madison`, selected `Madison, Wisconsin, United
  States`, reached ready Now, Hourly, Daily, and Details Home states, and
  restored the same selected forecast from Room cache with refresh-failed stale
  context after emulator network was disabled. Focused and broad checks passed.
- committed: This commit records the verified Slice 18J-R provider recovery and
  resumed Slice 18J installed-app evidence/review.
- artifacts target:
  `.codex/test-artifacts/2026-09-02-standard-home-visual-convergence/`.
- focused evidence:
  `.codex/test-artifacts/2026-09-02-standard-home-visual-convergence/focused-homeforecast-resumed.log`;
  `.codex/test-artifacts/2026-09-02-standard-home-visual-convergence/focused-home-compose-instrumentation-resumed.log`;
  `.codex/test-artifacts/2026-09-02-standard-home-visual-convergence/focused-openmeteo-resumed.log`.
- broad evidence:
  `.codex/test-artifacts/2026-09-02-standard-home-visual-convergence/broad-compile-debug-kotlin-resumed.log`;
  `.codex/test-artifacts/2026-09-02-standard-home-visual-convergence/broad-debug-unit-tests-resumed.log`;
  `.codex/test-artifacts/2026-09-02-standard-home-visual-convergence/broad-assemble-debug-resumed.log`;
  `.codex/test-artifacts/2026-09-02-standard-home-visual-convergence/git-diff-check-resumed.log`.
- installed evidence:
  `.codex/test-artifacts/2026-09-02-standard-home-visual-convergence/installed-resumed-first-run.png`;
  `.codex/test-artifacts/2026-09-02-standard-home-visual-convergence/installed-resumed-first-run.xml`;
  `.codex/test-artifacts/2026-09-02-standard-home-visual-convergence/installed-resumed-search-results.png`;
  `.codex/test-artifacts/2026-09-02-standard-home-visual-convergence/installed-resumed-search-results.xml`;
  `.codex/test-artifacts/2026-09-02-standard-home-visual-convergence/installed-resumed-home-now.png`;
  `.codex/test-artifacts/2026-09-02-standard-home-visual-convergence/installed-resumed-home-now.xml`;
  `.codex/test-artifacts/2026-09-02-standard-home-visual-convergence/installed-resumed-home-hourly.png`;
  `.codex/test-artifacts/2026-09-02-standard-home-visual-convergence/installed-resumed-home-hourly.xml`;
  `.codex/test-artifacts/2026-09-02-standard-home-visual-convergence/installed-resumed-home-daily.png`;
  `.codex/test-artifacts/2026-09-02-standard-home-visual-convergence/installed-resumed-home-daily.xml`;
  `.codex/test-artifacts/2026-09-02-standard-home-visual-convergence/installed-resumed-home-details.png`;
  `.codex/test-artifacts/2026-09-02-standard-home-visual-convergence/installed-resumed-home-details.xml`;
  `.codex/test-artifacts/2026-09-02-standard-home-visual-convergence/installed-resumed-home-offline-restored.png`;
  `.codex/test-artifacts/2026-09-02-standard-home-visual-convergence/installed-resumed-home-offline-restored.xml`.
- review: Compared with the committed Slice 18I reference evidence, the resumed
  18J installed screenshots show the Home surface reading less like a standard
  Material scaffold: the scene foundation is visible behind all pages, Now is
  dominated by the condition mark and current temperature, Hourly and Daily are
  scan-friendly forecast grids/lists, Details groups semantic metrics with
  source/update context, and the bottom navigation/actions remain reachable and
  subdued. No overlap, provider-path regression, stale-cache regression, or
  accessibility-test regression was found in the captured evidence.
- deferred: Slice 19A remains deferred until Slice 18J is committed.

# Slice 30E-P2 — TalkBack/TTS Runtime Readiness

**Status:** planned; platform speech passed; final Oxygen semantic attempt was
invalid for the required data-focus boundary
**Mode:** bounded installed test/evidence and documentation slice; no production
change is planned
**Cycle ID:** `2026-09-15-slice-30e-p2-talkback-tts-runtime-readiness`
**Baseline:** current `HEAD` (`5661877`); Slice 30E-P1 has verified, uncommitted
installed evidence; Gate 30E remains deferred and unverified
**Next action:** define one corrected diagnostic traversal that keeps TalkBack
active while moving accessibility focus from page identity/page control into
the Now weather-data node and then the Hourly destination/data node; do not
claim an Oxygen defect until the visible focus target and spoken result are
captured in the same active session

## 2026-09-16 retry-3 final confirmation result

- One fresh API-37 `oxygen_starter` session installed the unchanged APK once,
  loaded real Chicago/Open-Meteo data, enabled TalkBack, and restored/shut down
  cleanly. Evidence is under
  `.codex/test-artifacts/2026-09-15-slice-30e-p2-talkback-tts-runtime-readiness/retry-3-final-confirmation/`.
- The user reports that each TalkBack activation announced the application name
  `Oxygen`, followed by the right-aligned page identity (`page 1 of 4`, etc.).
  Page changes announced `Now` and `Hourly`, but the weather data was never
  confirmed as focused while TalkBack remained active.
- The user answered NO to both required semantic blockers: Now current
  condition/temperature and Hourly named destination. This attempt is not an
  accurate semantic test because it stopped at page-level focus/activation.
- The result is diagnostic only. It does not establish whether the test
  navigation failed to enter the data region or Oxygen's data semantics are
  absent/incorrect. No production or test source changed, and Gate 30E remains
  deferred and unverified.

## 2026-09-16 execution result

- Host preflight passed: `DISPLAY=:0` opened, PulseAudio 17.0 exposed the
  default sink and monitor, `oxygen_starter` was available, and ADB began with
  no online device.
- One visible API-37 `oxygen_starter` session reached
  `emulator-5554`, boot completion, and package-manager readiness. Baseline
  device, package, locale, layout, volume, animation, accessibility, and
  permission evidence is under
  `.codex/test-artifacts/2026-09-15-slice-30e-p2-talkback-tts-runtime-readiness/`.
  The attempted Oxygen app-data snapshots are zero-byte files, so this session
  did not independently retain selected-location, saved-location, or
  Appearance values; Oxygen was not opened or changed.
- TalkBack was enabled through Android Accessibility Settings and was enabled,
  bound, and touch-exploring. Google TTS initialized and emitted synthesis
  requests after the retained startup `TTS is not ready` messages.
- The normal Android Text-to-speech Settings `Play` control produced Google
  TTS synthesis and accessibility audio-focus logcat events. The monitor
  capture was effectively silent: 2,115,156 bytes over 11.99 seconds,
  `mean_volume=-91.0 dB`, `max_volume=-67.4 dB`; however, the user later
  directly confirmed hearing the audio and synthesized voice. The human
  platform-control requirement is therefore satisfied; the monitor result is
  retained as a non-authoritative diagnostic.
- Oxygen was not installed or exercised after the failed platform control; no
  app, core, manifest, dependency, test, provider, or APK input changed.
- Restoration passed: TalkBack, bound/enabled services, and touch exploration
  returned to disabled baseline values; TTS/settings, locale, volumes,
  animation scales, permissions, and app data were unchanged. The emulator
  stopped and final `adb devices -l` showed no online device.
- Commands run: bounded display/PulseAudio/AVD/ADB preflight; one emulator
  startup; Android Settings UI actions; one platform TTS control; hierarchy,
  screenshot, logcat, audio capture/analysis, final-state, and shutdown
  commands; no Gradle or connected tests; final `git diff --check` passed and
  `git status --short` is recorded in the artifact directory.

The first session was not verified because Oxygen was not exercised in that
session. The retry-2 result below completed the production semantic/action
boundary but still awaits the user's explicit Oxygen speech observation. No
Oxygen defect is established. The complete command/result ledger and artifact
payloads are in the cycle directory above.

## 2026-09-16 retry-2 execution result

- A fresh visible API-37 `oxygen_starter` session passed host/device preflight.
  TalkBack was enabled through Android Accessibility Settings, bound, and
  touch-exploring. Google TTS initialized after the expected startup messages.
- The Android Text-to-speech Settings `Play` control was repeated, and the
  user confirmed hearing the audio and synthesized voice. This satisfies the
  independent platform speech boundary; the low-level monitor remains only
  corroborating evidence.
- The unchanged debug APK was installed exactly once. Oxygen launched through
  the production path to real Chicago Home data: Cloudy, 70°F, feels like 74°F,
  Open-Meteo provenance, and six visible hourly entries after navigation.
- TalkBack focus navigation reached the combined Now summary and the named
  Hourly page action. Activating the focused action visibly reached `Hourly,
  Page 2 of 4`; the focused nodes, screenshots, logcat, APK hash, and complete
  retry ledger are under
  `.codex/test-artifacts/2026-09-15-slice-30e-p2-talkback-tts-runtime-readiness/retry-2/`.
- Retry-2 preference contents were byte-identical before and after the run.
  TalkBack, touch exploration, and bound/enabled services were restored;
  Oxygen was force-stopped; the emulator stopped; and ADB ended with no online
  device.
- The user reports that every announcement began with the application name,
  followed by speech dependent on the focused card/page. This is useful
  non-verbatim human evidence, but the user did not separately recall whether
  the Now announcement included the visible current-condition meaning or
  whether the Hourly announcement named its destination. No transcript is
  fabricated, so P2 remains unverified even though its production
  semantic/action boundary passed.

## Selected behavior

On the supported API-37 `oxygen_starter` runtime, the real TalkBack service can
speak Oxygen's existing Home Now weather summary and a named Home action after
the Android TTS engine has initialized. The spoken meaning must be confirmed by
a human and must agree with the visible production weather state.

This slice proves runtime readiness only. It does not close Gate 30E or cover
the gate's full Home, alert, and Appearance traversal. Oxygen does not own
TalkBack or its TTS engine, so P2 must not add app-managed speech, a TTS
dependency/service, a permission, a manifest query, or a fallback announcement.

Retained evidence contains early TalkBack `TTS is not ready` messages followed
later by Google TTS synthesis requests and accessibility audio focus. The early
message is therefore a startup observation, not proof of a persistent engine
failure. P2 must use a successful non-Oxygen spoken control before attributing
any failure to Oxygen.

## Acceptance boundary

P2 is verified only when one fresh, bounded API-37 `oxygen_starter` session
records all of the following:

1. Preflight captures `DISPLAY=:0` access, PulseAudio server/default sink and
   monitor state, ADB/API/package readiness, TalkBack and selected TTS package
   versions, locale/layout direction, media and accessibility volume, animation
   scales, accessibility service state, Oxygen permission state, selected
   location, saved locations, and saved Appearance values.
2. TalkBack is enabled through Android Accessibility Settings and is shown as
   enabled and bound with touch exploration active. After initialization, a
   normal Android Settings or TTS sample control produces speech that a human
   hears and records. Service state, logcat synthesis/audio-focus evidence, and
   PulseAudio activity corroborate the observation but do not replace it.
3. The current debug APK is installed no more than once. Oxygen launches
   through the production selected-location, provider, and Room-cache path to a
   real loaded Home. No preview/sample bundle, fixture, seeded alert, hidden
   route, or fabricated transcript may satisfy the boundary.
4. Using TalkBack focus navigation and activation, not coordinate-only taps,
   focus reaches the Home Now weather summary and the named action that moves
   from Now to Hourly. A human records the speech actually heard for both; it
   matches the visible weather meaning and action destination, with no omitted
   current-condition meaning or duplicate decorative announcement. Hourly is
   visibly reached after activation.
5. The focused nodes/hierarchy, Now and Hourly screenshots, relevant TalkBack/
   TTS/audio logcat window, APK hash, production location/provider/cache
   identity, human observation, and command/result ledger are saved under
   `.codex/test-artifacts/2026-09-15-slice-30e-p2-talkback-tts-runtime-readiness/`.
6. TalkBack, touch exploration, TTS choice/settings, volumes, animation scales,
   locale/layout direction, permissions, location/saved-location state,
   Appearance values, display/network state, and app state are restored to the
   captured baseline. The emulator is stopped and ADB shows no online device.

A platform-control speech failure, Oxygen production-path failure, missing
human confirmation, unchanged-condition timeout, or failed restoration is a
blocked/failed attempt, not a verified slice.

## Implementation and diagnosis

1. Create the artifact directory and verification ledger before startup. Set a
   single-session budget: one emulator boot, one APK install, one TalkBack
   enablement, one platform speech control, and one Now-to-Hourly Oxygen
   traversal. Stop after the first bounded platform timeout; do not repeat an
   unchanged condition.
2. Start the emulator only after bounded display and PulseAudio checks pass.
   Capture all pre-change state before enabling TalkBack. Do not install or
   update packages, download a different voice, change the AVD image, or alter
   host audio configuration inside this slice.
3. Enable TalkBack through the normal Settings UI, wait for TTS initialization,
   and exercise the independent platform speech control. Treat startup
   `TTS is not ready` lines as diagnostic only; readiness requires later
   successful synthesis plus human-heard speech.
4. If the platform control passes, install and launch Oxygen once, confirm the
   loaded production state and provenance, clear the relevant logcat window,
   and perform only the Now-summary and named Now-to-Hourly action traversal.
5. Classify the first failure without broadening P2:
   - if the platform control cannot speak, record the host/Android/TalkBack/TTS
     blocker and make no Oxygen source change;
   - if platform speech works but Oxygen has no focusable node, wrong spoken
     meaning, duplicate output, or an inoperable named action, retain the
     smallest reproduction and failing semantic node, leave P2 unverified, and
     select a separately named Oxygen repair slice;
   - if both controls pass, complete restoration and documentation sync.

No speculative product repair is permitted in P2. An Oxygen repair requires a
known failing boundary, its own red test, the narrowest production correction,
and focused plus installed re-verification under a new active plan.

## Test and evidence plan

The primary focused test is the installed service-level comparison:

- control: human-confirmed speech from a non-Oxygen Android surface after TTS
  initialization;
- subject: human-confirmed Oxygen Now summary and Now-to-Hourly action speech;
- oracle: visible production meaning, focused-node semantics, successful page
  activation, and the human observation agree.

Retain and map the existing deterministic coverage for
`currentWeatherExposesOneCompleteSpokenSummaryWithoutRedundantChildren` and
`homePagerExposesNamedAccessibilityPageMovementActions`; those Compose tests
prove semantics, not TalkBack/TTS speech. Do not rerun them when their APK and
inputs are unchanged. Plan zero new connected cases and zero Gradle reruns for
the expected evidence-only path.

If P2 exposes an Oxygen defect, do not weaken or rewrite retained tests to fit
the output. The follow-up repair plan must add or tighten one focused Compose
test at the failing semantic/action boundary and run no more than the directly
relevant connected cases before its installed TalkBack recheck.

## Broad verification

For the expected documentation/evidence-only result, run:

```sh
git diff --check
git status --short
```

Do not present compilation, assembly, screenshots, hierarchy dumps, logcat, or
non-silent audio as substitutes for the installed speech boundary. Do not rerun
compile, unit, connected, or assembly tasks when Kotlin, resources, manifest,
dependencies, tests, and APK inputs are unchanged.

## Required documentation updates

After the attempt:

- update this plan with the exact result, evidence paths, restoration result,
  commands actually run, skipped checks, and next action;
- append one concise, self-contained entry to `.codex/cycles/history.md`;
- update the 30E-P2 status and sequencing text in
  `.codex/plans/mvp-roadmap.md` only when the recorded result supports it.

P2 success does not satisfy the broader README/specification statement that
TalkBack service traversal remains unverified, so `README.md` and
`docs/OXYGEN_FULL_SPECIFICATION.md` require no claim change. Change them only
if a demonstrated inconsistency must be corrected; never imply Gate 30E,
accessibility closure, MVP completion, release readiness, or release status.
Provider contracts, `DATA_SOURCES.md`, privacy/license documents, Gradle files,
and the manifest are unaffected.

If the completed work is committed, use a descriptive subject and body that
records behavior/evidence/limits, then perform the required post-commit sync of
the active plan, roadmap, and live history before calling the cycle closed.

## Intended tracked files

Expected evidence/documentation path:

- `.codex/plans/current.md`
- `.codex/cycles/history.md`
- `.codex/plans/mvp-roadmap.md` only for supported status/sequence sync

Artifact payloads remain untracked. No `app`, `core`, manifest, dependency,
provider-contract, or build-script change is planned.

## Out of scope

- Gate 30E closure; complete Home, live-alert summary/detail, or Appearance
  traversal; localization; automatic contrast; and universal accessibility;
- changes to weather/provider/cache/location behavior or presentation values;
- app-owned speech, custom accessibility services, TTS package/voice changes,
  emulator-image changes, permissions, dependencies, analytics, or telemetry;
- release-candidate, MVP-complete, release-ready, signing, publication, or
  released claims;
- broad refactoring, unrelated test reruns, fabricated transcripts, fixture-
  backed runtime claims, and repeated retries after an unchanged blocker.

# Gate 30E — Installed TalkBack and Accessibility Closure

**Status:** blocked after partial installed run; Gate 30E remains unverified
**Cycle ID:** `2026-09-15-gate-30e-installed-talkback-accessibility-closure`
**Baseline:** `fe09078`; Gates 30A3, 30B3, 30C3, and 30D3 are complete.
The prior visible-emulator investigation confirmed TalkBack and Google TTS on
API-37 `oxygen_starter`, but host PulseAudio was unavailable, so no audible
speech or service traversal was verified.
**Next action:** if Gate 30E is resumed, preselect one genuine active NWS
location before one fresh bounded session and obtain human-confirmed speech
transcript evidence; do not repeat the completed Chicago journey.

## Initial preflight result

- `timeout 5 xdpyinfo` failed because `DISPLAY` was unset; the explicit
  fallback `timeout 5 env DISPLAY=:0 xdpyinfo` passed.
- `timeout 5 pactl info` failed with no `/run/user/1000/pulse` directory and
  `Connection refused`. This is the exact platform blocker required to stop
  before emulator startup.
- `. scripts/android-env.sh && adb devices` passed with no online device. No
  emulator was started, no APK was built or installed, and no TalkBack state
  was changed.
- The command ledger is retained at
  `.codex/test-artifacts/2026-09-15-gate-30e-installed-talkback-accessibility-closure/verification-ledger.md`.

Gate 30E remains unverified. No product defect or accessibility result was
established.

## 2026-09-15 partial installed run

- The previously blocked host prerequisite was recovered: this Codex session
  saw `/run/user/1000/pulse/native` and `pactl info` passed against the real
  PulseAudio 17.0 server. `oxygen_starter` started visibly on `:0`, reached
  API-37 `emulator-5554` boot completion, and the debug APK built, installed,
  and launched once.
- TalkBack was enabled through Android Accessibility Settings, its service was
  bound, and the Oxygen production Home showed real Chicago weather with
  current/hourly/daily data, source/update/provenance, and mapper-owned spoken
  descriptions. Focused activation verified Now to Hourly, Daily, and Details;
  DPAD-left activation returned Details to Daily.
- Settings / Appearance was entered. Paper was selected and `Theme saved` was
  observed, Oxygen was restored, and Back returned through Settings to Home.
- A PulseAudio monitor capture was non-silent (`mean_volume -34.6 dB`,
  `max_volume -15.1 dB`) during TalkBack/Oxygen launch. This proves host audio
  reached the sink monitor, not that a human-heard transcript was independently
  confirmed.
- Chicago exposed no active official alert summary, so live alert-detail
  traversal was unavailable. TalkBack was restored to disabled, Oxygen was
  force-stopped, the emulator was stopped, and ADB ended with no online device.

The detailed evidence and limits are in the cycle artifact ledger below.

## Selected behavior

Resolve Gate 30E by exercising the installed production app with Android
TalkBack and proving that focus order, spoken weather and alert meaning, named
actions, and Appearance controls remain usable. This is one installed
test/evidence/documentation gate. It does not change app behavior.

The primary acceptance boundary is observable TalkBack speech and operation on
the installed app. Compose tests, source inspection, compilation, screenshots,
and UI hierarchies may corroborate the result but cannot replace that boundary.

## Acceptance boundary

Gate 30E is verified only when all of the following are recorded from one
bounded API-37 `oxygen_starter` session:

1. Host display and audio preflight pass before emulator startup. TalkBack and
   the TTS engine are positively identified, TalkBack is enabled and bound, and
   audible speech is confirmed before Oxygen is installed.
2. Device, APK, locale, layout direction, display/font/animation settings,
   accessibility settings, and saved Appearance values are captured before any
   temporary change. The current debug APK is installed once after ADB and the
   package manager are ready.
3. Oxygen reaches a real ready, cached, or truthfully stale Home through the
   production selected-location path. `SampleWeather`, seeded alerts, test-only
   routes, and fabricated values are prohibited.
4. TalkBack traverses Standard Home Now, Hourly, Daily, and Details in logical
   order. It announces page identity, current conditions once, chronological
   hourly/daily meaning, measurements, and source/update/provenance without
   duplicate decorative announcements. Named forward and backward movement is
   focused and activated successfully.
5. A genuine production official-alert outcome is recorded. For full closure,
   an active alert is traversed from Home summary through detail, including
   event, severity, issuer, timing, affected area, instructions, Back, and the
   official-source action. One currently active NWS location may be identified
   before the emulator session and selected through Oxygen's normal location
   search; no fixture, production seeding, or repeated location hunt is allowed.
   If no genuine active alert can be exercised, Gate 30E remains unverified.
6. Settings / Appearance is traversed with TalkBack. Theme, Contrast, Layout,
   and Effects group and selected-state meaning are announced without relying
   on color. One reversible non-current choice is activated, its saved state is
   observed, Back returns to Home, and the original value is restored. Retry
   behavior remains supported by its retained deterministic evidence unless a
   real write failure occurs; no failure is injected into production.
7. A focus-by-focus transcript records speech actually heard, visible meaning,
   activation result, order, and any duplicate or omitted announcement.
   Screenshots, UI hierarchies, service state, and an execution ledger accompany
   the transcript. Retained compact/large-font, RTL, long-content, unit,
   touch-target, reduced-motion, Effects-Off, theme, contrast, and retry
   evidence is mapped by exact condition without claiming an untested
   cross-product.
8. TalkBack, touch exploration, selected location, Appearance choice, and every
   temporary emulator/display setting are restored to their recorded pre-run
   values. The emulator is stopped and ADB confirms no online device.
9. README, specification, roadmap, active plan, and cycle history report only
   the exercised result and retained limits. Passing this gate does not imply
   localization, automatic contrast, release readiness, signing, publication,
   or universal accessibility.

## Execution plan

1. Audit the retained Gate 30A3/30B/30C/30D evidence and create an
   artifact-local coverage map. Do not rerun unchanged passing tests.
2. In the logged-in desktop environment, run bounded `xdpyinfo` and `pactl`
   checks. Preserve that desktop session's display/audio environment; if
   `DISPLAY` is unset, verify the previously working `DISPLAY=:0` explicitly.
   Start one visible emulator only after both checks pass. Capture pre-change
   device, package, service, TTS, accessibility, display, locale, and preference
   state.
3. Enable only the already installed TalkBack service through Android Settings,
   verify its bound state with Android diagnostics, and confirm audible speech.
   Build/install once, then perform the Home, live-alert, and Appearance journey
   using TalkBack focus and activation rather than coordinate taps.
4. Save the transcript, screenshots, hierarchies, diagnostics, alert provenance,
   before/after state, and command/result ledger under
   `.codex/test-artifacts/2026-09-15-gate-30e-installed-talkback-accessibility-closure/`.
5. Restore all temporary state and stop the emulator. Review the evidence
   against every acceptance item before changing status.
6. On a clean pass, mark Gate 30E verified and synchronize `README.md`,
   `docs/OXYGEN_FULL_SPECIFICATION.md`, `.codex/plans/mvp-roadmap.md`, this
   plan, and an append-only `.codex/cycles/history.md` entry. Run the focused
   documentation review and `git diff --check`. If the closure is committed,
   use a descriptive subject/body and perform the required post-commit
   authoritative document sync before calling the work closed.

## Defect and blocker rules

- An Oxygen crash, unreachable control, incorrect/duplicated/omitted speech,
  illogical focus order, or inoperable action is a product defect. Preserve the
  smallest reproduction, leave Gate 30E unverified, and select one separately
  named repair slice with a meaningful failing boundary. Do not patch product
  code opportunistically inside this gate.
- Missing or inaudible audio, an unavailable service, unsafe service enablement,
  or a bounded platform timeout is an environment blocker. Record it once and
  stop; do not convert deterministic semantics evidence into TalkBack success.
- Lack of a genuine active alert is an evidence gap, not app success or failure.
  Record it and leave the gate unverified rather than fabricating alert data.
- No partial result may be described as verified, committed, release-ready, or
  complete.

## Verification budget

Use one visible emulator session, one APK install, one TalkBack enablement, one
Home/alert/Appearance journey, and at most one preselected live-alert location.
Stop after the first bounded platform timeout or repeated identical failure.

Planned commands and evidence:

```sh
timeout 5 xdpyinfo
timeout 5 env DISPLAY=:0 xdpyinfo
timeout 5 pactl info
scripts/list-avds.sh
DISPLAY=:0 OXYGEN_EMULATOR_WINDOW=1 OXYGEN_EMULATOR_NETWORK=0 scripts/start-emulator.sh
scripts/install-debug.sh
git diff --check
git status --short
```

The ledger must record the resolved display/audio environment and all bounded
ADB/service/settings commands actually run. `scripts/install-debug.sh` supplies
the required debug assembly. App/core unit tests and connected tests are not
rerun when production/test inputs are unchanged because they cannot prove
audible TalkBack traversal. If a repair changes production or test inputs, the
repair slice owns focused red/green evidence and applicable compile, unit,
connected, assembly, and whitespace checks.

## Intended tracked files

Successful gate closure is limited to documentation/status files:

- `.codex/plans/current.md`
- `.codex/plans/mvp-roadmap.md`
- `.codex/cycles/history.md`
- `README.md`
- `docs/OXYGEN_FULL_SPECIFICATION.md`

No Kotlin, Compose, provider, repository, persistence, preference, manifest,
permission, dependency, Gradle, or emulator-script change belongs to this gate.
Do not add an accessibility abstraction, placeholder implementation, test-only
screen, mocked production success, seeded production alert, downloaded service,
or broad cleanup. Leave all unrelated worktree changes untouched.

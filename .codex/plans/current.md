# Gate 30B1B1 — RTL Installed Evidence and Documentation Sync

**Status:** committed
**Cycle ID:** `2026-09-12-gate-30b1b1-rtl-installed-evidence-doc-sync`
**Prerequisites:** Slice 30B1A4's Standard boundary at `8b5647b` and its
Simple boundary verified in
`.codex/test-artifacts/2026-09-12-slice-30b1a4-a7-simple-rtl-compact-completion/`.
**Mode:** bounded installed RTL evidence and documentation-sync gate

## Selected behavior and acceptance boundary

On one installed Android journey with device-wide RTL temporarily enabled, the
installed Home surface must retain the already-covered Home meaning and
navigation through the selected minimum flow. Capture the required screenshots
and UI hierarchies, restore the prior device direction, and reconcile project
authorities only to the retained evidence.

## Contract and scope

This third-cycle closure gate owns the minimum combined focused filter, one
installed RTL Home journey, visual and hierarchy evidence, restoration of
device direction, and the resulting documentation sync for 30B1. It must not
alter Kotlin, Compose, providers, persistence, resources, manifests, or
dependencies.

The A4 Simple boundary is verified: its one bounded method completed with one
test, zero skipped, and zero failed. Its recovery preflight, runner result,
and ledger are retained under
`.codex/test-artifacts/2026-09-12-slice-30b1a4-a7-simple-rtl-compact-completion/`.

## Completed evidence

The retained focused filter is the two already-passing 30B1A4 methods; it was
not rerun because no source or execution-environment change occurred after the
A6 compile/unit/assembly evidence and the A7 Simple result. This follows the
selected minimum-evidence budget rather than spending further connected-test
capacity.

One `oxygen_starter` API-37 emulator journey used the installed debug APK and
a real manual Open-Meteo location selection for Chicago, Illinois. Device-wide
RTL was confirmed as `ldrtl` by temporarily selecting `ar-SA` and enabling the
Android RTL flag, then restarting the activity. The installed Standard Home
journey retained Now, Hourly, Daily, and Details in semantic order; the
mirrored selector was `Details`, `Daily`, `Hourly`, `Now` from left to right.
Each page preserved its page title/position and Home content in the captured UI
hierarchy.

- Screenshots: `rtl-home-now-device-wide.png`,
  `rtl-home-hourly-device-wide.png`, `rtl-home-daily-device-wide.png`, and
  `rtl-home-details-device-wide.png`.
- UI hierarchies: the matching `*.xml` files in
  `.codex/test-artifacts/2026-09-12-gate-30b1b1-rtl-installed-evidence-doc-sync/`.
- Device-direction evidence: `device-direction-before.txt`,
  `device-direction-method.txt`, and
  `restoration-emulator/restoration-verified.txt`. The latter confirms the
  original `en-US`, no global `debug.force_rtl` setting, false RTL property,
  and `ldltr` configuration after restoration.

The initial direct setting write did not update Android's active configuration
(`ldltr`), so its launch/location captures are retained only as diagnostic
records and are not RTL acceptance evidence. Restarting the no-data-wipe
emulator once was required to apply and verify the restored direction; it was
then stopped.

## Verification ledger

- Reused: A6 `:app:compileDebugKotlin`, app/core unit tests, and
  `:app:assembleDebug`; A7's one passing Simple method. No source or relevant
  environment changed.
- Ran: emulator health preflight, debug APK install, real selected-location
  Home journey, four screenshot/UI-hierarchy captures, device-direction
  restoration/verification, `git diff --check`, and tracking-document review.
- Skipped: all Gradle compile/unit/assembly and connected-test reruns (reused
  evidence); TalkBack, Simple installed journey, font-scale-2.0, reduced
  motion, appearance matrix, alerts, localization, and release checks (outside
  this gate).

## Next action

Select the separately specified Slice 30B2; do not imply its reduced-motion
or appearance-invariance behavior from this RTL gate.

## Out of scope

New production behavior, additional RTL implementation slices, TalkBack service
traversal, font-scale-2.0 work, reduced motion, appearance matrices,
localization, alerts, and release readiness.

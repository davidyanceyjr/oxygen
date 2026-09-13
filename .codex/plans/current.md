# Gate 30B3 — Home Environment Evidence and Documentation Sync

**Status:** committed
**Cycle ID:** `2026-09-12-gate-30b3-home-environment-evidence-doc-sync`
**Prerequisites:** Gate 30B1B1 (`e1c15e6`) and Slice 30B2 (`728f4c2`).
**Mode:** required third-cycle test-only and documentation-sync gate; no
production behavior change

## Selected behavior and acceptance boundary

Reconcile the retained RTL and reduced-motion/appearance Home evidence without
rerunning whole Android test classes or changing production code. The gate is
closed only for the supported combination of deterministic and installed
evidence; it does not claim TalkBack service traversal or untested
cross-products.

## Retained evidence

- RTL: the two 30B1A4 compact/no-refetch methods each completed once with one
  test, zero skipped, and zero failed. The installed API-37 `ldrtl` Standard
  Home journey used a real manual Chicago selection and retained Now, Hourly,
  Daily, and Details titles/positions, mirrored controls, screenshots, and UI
  hierarchies. Device direction was restored.
- Reduced motion and appearance: six selected 30B2 methods cover all
  Oxygen/Paper/Terminal and Standard/High pairs at the deterministic 360x640
  dp, font-scale-1.3 fixture boundary. Each has accepted one-test, zero-skipped,
  zero-failed Android instrumentation evidence. Case 6's original wrapper
  timeout followed completed matching XML, instrumentation-log, and textproto
  results; `run-connected-method.sh` now accepts that terminal evidence before
  bounded cleanup, and its isolated ADB-preflight fixture passed
  `pass-after-runner-cleanup`.
- The installed API-37 disabled-animation journey used a production manual
  Chicago Open-Meteo selection. All three Android animation scales were zeroed,
  the app was force-stopped/relaunched, Appearance reported effective Off while
  the saved Subtle choice remained selected, and Standard Now, Hourly, Daily,
  and Details were captured with page identity, live weather, and
  source/update/provenance. The original scale values and saved appearance were
  restored before the emulator stopped.

Artifacts are retained below:

- `.codex/test-artifacts/2026-09-12-gate-30b1b1-rtl-installed-evidence-doc-sync/`
- `.codex/test-artifacts/2026-09-12-slice-30b2-reduced-motion-appearance-invariance/`
- `.codex/test-artifacts/2026-09-12-slice-30b2-runner-finalization-repair/`

## Verification ledger

- Focused Android evidence: retained selected RTL results and six selected 30B2
  results; no connected case was rerun after runner repair.
- Final source checks: `sh -n scripts/run-connected-method.sh`, the isolated
  runner fixture, `:app:compileDebugAndroidTestKotlin`,
  `:app:compileDebugKotlin`, app/core debug unit tests, `:app:assembleDebug`,
  and `git diff --check` passed.
- Installed evidence: one recovered `oxygen_starter` API-37 session, APK
  install, real manual Chicago forecast path, effective-Off Appearance check,
  four Standard Home captures, and restoration of system animation scales and
  saved appearance.

## Documentation sync

README and specification sections 46 and 53 distinguish deterministic
theme/contrast evidence from the installed disabled-animation journey. The
roadmap, live history, and this plan identify Gate 30B3 as committed and leave
Slice 30C1 as the next specified candidate.

## Out of scope

TalkBack service traversal/pronunciation, Simple installed RTL, full
theme/contrast or font-scale cross-products, localization, official-alert
accessibility, new preferences, provider/cache behavior, and release readiness.

## Next action

Select the separately specified Slice 30C1; do not infer its official-alert
summary accessibility behavior from this Home-only gate.

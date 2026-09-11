# Slice 30B1A3B1 — RTL/LTR Spoken-Meaning Equivalence

**Status:** planned
**Cycle ID:** `2026-09-10-slice-30b1a3b1-rtl-ltr-spoken-meaning-equivalence`
**Umbrella:** Slice 30B1A3 — RTL Chronology and Spoken-Meaning Preservation
**Mode:** bounded Home RTL/LTR rendered spoken-meaning implementation

## Selected behavior

Under Compose-local `LayoutDirection.Rtl`, Home weather meaning must remain
identical to the corresponding LTR rendering. The production Home composition
must preserve mapper-owned content descriptions and visible time/date labels
for Standard Hourly and Daily pages and both Simple Forecast choices. Missing
temperature and precipitation values must remain honest and attached to the
same rendered entries.

The acceptance boundary is the production `HomeLoadingScreen` composition with
the deterministic provider-neutral full-weather fixture. Tests must collect
complete rendered unmerged semantics for each exercised choice and compare RTL
with LTR; they must not reconstruct descriptions from visible text or test only
the mapper.

## Next action and focused evidence

Inspect the existing chronology tests and choose at most two named connected
cases covering Standard Hourly/Daily and Simple Hourly/Daily through the
production Home path. Begin with red rendered assertions, then run the focused
cases once on one ADB-ready emulator using Compose-local RTL. Retain command
output, result counts, rendered semantics, emulator metadata, and a concise
ledger under:

`.codex/test-artifacts/2026-09-10-slice-30b1a3b1-rtl-ltr-spoken-meaning-equivalence/`

Any production correction is allowed only after a failing rendered assertion
and is limited to the existing rendered semantics boundary in
`HomeLoadingScreen.kt`. Keep provider, mapper, localized-resource, repository,
cache, persistence, and device-wide RTL behavior unchanged.

After focused green, run once:

```sh
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

## Limits

Compact layout, large-font/refetch behavior, installed/manual or device-wide
RTL, screenshots, UI hierarchies, TalkBack service traversal, reduced motion,
theme/contrast matrices, localization, alerts, Settings, provider/network
behavior, persistence, release readiness, and MVP completion remain out of
scope. The prior Simple chronology slice is committed at `91974b2`; evidence
is retained under
`.codex/test-artifacts/2026-09-10-slice-30b1a3a3-simple-rtl-forecast-chronology/`.

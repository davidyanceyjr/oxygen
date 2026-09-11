# Slice 30B1A4 — RTL Compact Layout and No-Refetch Evidence

**Status:** planned
**Cycle ID:** `2026-09-11-slice-30b1a4-rtl-compact-layout-no-refetch`
**Umbrella:** Slice 30B1 — Home RTL Navigation and Chronology
**Mode:** bounded Home RTL compact-layout and request-count implementation

## Selected behavior

Under Compose-local `LayoutDirection.Rtl`, the 360x640 dp, font-scale-1.3
Home path must keep page controls usable and weather content reachable without
horizontal overlap or accidental provider refetch when changing Home pages,
layout, or Simple Hourly/Daily choices.

The acceptance boundary is the production `OxygenApp` selected-location path
with the deterministic repository fixture. Tests must assert canonical
forecast/request counts, 48dp controls, readable bounds, and no overlap while
exercising RTL Standard and Simple Home choices.

## Next action and focused evidence

Inspect the existing compact/refetch tests and choose at most two named
connected cases. Begin with red geometry/request-count assertions, then run
the focused cases once on the existing ADB-ready emulator using Compose-local
RTL. Retain command output, result counts, rendered semantics or hierarchy
where captured, emulator metadata, and a concise ledger under:

`.codex/test-artifacts/2026-09-11-slice-30b1a4-rtl-compact-layout-no-refetch/`

Any production correction is allowed only after a failing rendered boundary
and is limited to the existing Home layout/request boundary. Keep provider,
mapper, localized-resource, cache, persistence, and device-wide RTL behavior
unchanged.

After focused green, run once:

```sh
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

## Limits

Device-wide or installed/manual RTL, screenshots unless required by the
acceptance boundary, TalkBack service traversal, reduced motion,
theme/contrast matrices, localization, alerts, provider/network behavior,
persistence schema, release readiness, and MVP completion remain out of
scope. Slice 30B1A3B1 is committed at `26b32b8`; its paired rendered semantics
artifacts and verification ledger are retained under
`.codex/test-artifacts/2026-09-10-slice-30b1a3b1-rtl-ltr-spoken-meaning-equivalence/`.

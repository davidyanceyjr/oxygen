# Slice 30B1A3A3 — Simple Home RTL Forecast Chronology

**Status:** planned
**Cycle ID:** `2026-09-10-slice-30b1a3a3-simple-rtl-forecast-chronology`
**Umbrella:** Slice 30B1A3 — RTL Chronology and Spoken-Meaning Preservation
**Mode:** bounded Simple Home RTL presentation-contract implementation

## Active slice

Under Compose-local `LayoutDirection.Rtl`, Simple Home must preserve the
mapper-produced chronological order for both Hourly and Daily forecast
choices. Add one focused connected Compose test of the production
`HomeLoadingScreen` path that selects each Simple forecast choice, collects
rendered tagged entries in semantic/traversal order, and compares exact
expected labels and first/last row meaning under LTR and RTL.

Use the existing full-weather fixture, provider-neutral presentation state,
and `EffectsLevel.OFF`. Keep physical mirroring, spoken-description
equivalence, compact/refetch behavior, device-wide RTL, screenshots, TalkBack
service traversal, provider/network/cache behavior, and installed/manual
journeys out of scope. Production changes are authorized only after a failing
rendered boundary and must be limited to the Simple forecast rendering
boundary.

Focused case: `rtlSimpleHomeForecastPreservesChronologicalRenderedOrder`.
Do not sort rendered output or parse display strings back into weather values.
Save command output, result XML, rendered semantics, emulator metadata, and a
concise ledger under
`.codex/test-artifacts/2026-09-10-slice-30b1a3a3-simple-rtl-forecast-chronology/`.
Require one completed, zero skipped, and zero failed. After focused green,
run the standard compile, app/core unit, assemble, and `git diff --check`
commands. Commit with a descriptive body and synchronize the plan, roadmap,
cycle history, and specification progress note.

## Completed Slice 30B1A3A2 — RTL Standard Daily Chronology

Status: committed at `9390601`; evidence is retained under
`.codex/test-artifacts/2026-09-10-slice-30b1a3a2-rtl-standard-daily-chronology/`.

## Selected behavior and acceptance boundary

Under Compose-local `LayoutDirection.Rtl`, the Standard Home Daily page must
preserve the forecast list’s logical earliest-to-latest order and its existing
mapper-produced date labels. RTL may mirror physical placement; it must not
reverse the forecast data or the semantic traversal order. The six entries from
the deterministic full-weather fixture must remain ordered as the mapper
presents them: Sat, Aug 22 through Thu, Aug 27. The rendered RTL sequence must
match the rendered LTR sequence for this same fixture.

The acceptance boundary is one connected Compose test of the production
`HomeLoadingScreen` path with provider-neutral presentation state. It must
inspect rendered nodes and their semantics, not only the fixture, mapper, or
source ordering. This sub-slice owns Daily chronology only; Simple Forecast
chronology and spoken-description equivalence are later `30B1A3` sub-slices.

## Discovery and decomposition decision

The production path already renders Standard Daily through `DailyPage`, which
iterates `dashboard.daily.take(6)` in mapper order and gives each row a stable
`home-daily-entry-0` through `home-daily-entry-5` tag. `DailyEntry` exposes the
mapper-owned spoken description while retaining visible date, condition,
precipitation, low, and high content in the rendered row. The existing full
fixture contains the six required dates, including nullable high/low and
precipitation cases.

No additional implementation sub-slices are warranted. This is one bounded
rendered-list behavior, one existing Home surface, one connected acceptance
case, and no new persistence format, state machine, provider path, platform
adapter, or installed/manual journey. `30B1A3A3`, `30B1A3B1`, `30B1A4`, and
`30B1B1` remain separate roadmap boundaries.

## First-draft implementation plan

1. Establish the LTR baseline from the same fixture and record the current
   rendered Daily node order and labels before editing production code.
2. Add a red boundary assertion that collects all six rendered Daily entries
   in semantic/traversal order and asserts the exact expected date-label list.
   The assertion must distinguish logical order from RTL left-to-right bounds;
   physical mirroring is already covered by 30B1A2.
3. Run the focused test before any production correction. If the baseline is
   green, keep production unchanged. If it is red, correct only the smallest
   Daily rendering boundary in `HomeLoadingScreen.kt`, preserving the
   provider-neutral mapper output, row values, page navigation, gestures,
   and physical RTL affordances.
4. Keep the test sensitive to an accidental reversal, omission, duplicate, or
   changed date label. Do not derive the expected order by sorting the rendered
   result inside the assertion.

## Intended files

Expected:

- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`

Conditional only for a failing rendered-behavior assertion:

- `app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt`

No provider, domain, repository, cache, preference, resource, localization,
manifest, dependency, navigation, gesture, or production-only test-hook change
is authorized by this plan.

## Focused test and evidence

Add one named connected case:

`rtlStandardHomeDailyPreservesChronologicalRenderedOrder`

Use `EffectsLevel.OFF`, the existing full-weather fixture, and the production
Home composition. In the same test, collect the LTR baseline, recompose with
Compose-local `LayoutDirection.Rtl`, and collect the RTL result; do not change
device-wide RTL. Navigate to the Standard Daily page through its existing
visible tab, then assert:

- all six Daily entries are present exactly once;
- rendered date labels in logical semantics/traversal order are exactly
  `Sat, Aug 22` through `Thu, Aug 27`;
- the same ordered labels collected under LTR are identical to the RTL labels;
- the first rendered row retains `Rain showers`, `High 73 deg F`, and `Low 54
  deg F`, while the last retains `Rain`, `High 67 deg F`, and `Low unavailable`,
  proving those values remain attached to their corresponding rows; and
- the page remains Standard Daily with its existing page position and page
  controls, without repeating the complete 30B1A1/30B1A2 contracts.

Use the existing test tags and unmerged semantics as needed to observe visible
labels. Do not use screen x-coordinates as the chronology oracle, and do not
parse display strings back into weather values. A test helper may be added only
to collect already-rendered semantics and must remain local to the test file.

Run only the named case on one ADB-ready emulator session:

```sh
. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest \
  '-Pandroid.testInstrumentationRunnerArguments.class=com.oxygen.weather.app.ui.home.HomeDashboardUiTest#rtlStandardHomeDailyPreservesChronologicalRenderedOrder'
```

Require one completed, zero skipped, and zero failed test. Save the command
output, result XML, rendered semantics artifact, and a concise verification
ledger under:

`.codex/test-artifacts/2026-09-10-slice-30b1a3a2-rtl-standard-daily-chronology/`

The artifact must state the emulator/API/density/font scale, that RTL was
Compose-local, and any rerun reason. Do not claim device-level RTL, TalkBack
service traversal, screenshots, or installed RTL behavior from this test.

## Broad checks and real-path boundary

After focused green, run once:

```sh
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

The connected test exercises the production Home rendering path with a
deterministic provider-neutral state. No live provider request or separate
manual APK installation is required for this sub-slice; installed/device RTL
evidence belongs to Gate 30B1B1 after all chronology and compact-layout
sub-slices. Do not rerun a
passing command unless production code, test input, or the environment changes.

## Required document updates at completion

This is an implementation sub-slice, not a documentation-only gate. When the
focused and broad checks are green and the diff is reviewed:

- commit with a descriptive subject and body naming the changed behavior,
  evidence actually run, and limits or skipped checks;
- perform the required post-commit sync of `.codex/plans/current.md`,
  `.codex/plans/mvp-roadmap.md`, `.codex/cycles/history.md`, and the
  progress note in `docs/OXYGEN_FULL_SPECIFICATION.md`;
- mark only `30B1A3A2` with the evidence and commit; select the next specified
  sub-slice as the following active plan; and
- leave `README.md` unchanged because this sub-slice does not change an
  installed-app claim. Keep the specification progress note synchronized to
  the next selected sub-slice, but do not claim complete RTL support there;
  the final installed-evidence gate owns that status reconciliation.

The history entry must be self-contained and include the focused command,
broad commands, artifact path, exact result counts, production files changed
or explicitly unchanged, and limits. No history entry or completion status is
to be written before evidence exists.

## Decomposed 30B1A3 sequence

The umbrella is intentionally split into four independently observable
sub-slices:

1. `30B1A3A1` — Standard Home Hourly chronology (committed).
2. `30B1A3A2` — Standard Home Daily chronology (**active**).
3. `30B1A3A3` — Simple Home Forecast chronology for Hourly and Daily choices.
4. `30B1A3B1` — RTL/LTR spoken-description equivalence for Standard and Simple
   forecast content.

Only the second item is selected by this active plan. The following items must
not be implemented in the same cycle or silently folded into this test.

## Out of scope and completion gate

Out of scope: Simple Forecast chronology, spoken-meaning comparison,
provider/network/cache behavior, persistence or refetch counts,
device-wide RTL, installed/manual journeys, screenshots, UI hierarchies,
TalkBack service traversal, font-scale-2.0 evidence, reduced motion,
theme/contrast matrices, localization changes, alerts, Settings, release
readiness, and MVP completion.

This sub-slice is ready only after its named connected case passes with the
required result counts, the applicable broad checks pass, artifacts and the
verification ledger are retained, and the post-commit operational documents
are synchronized to facts actually established.

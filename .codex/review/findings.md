# Slice 24B plan review findings

Reviewed 2026-09-06 against the active plan, the current app state and Compose
seams, the full specification, the provider contract, README, disclosures, and
the recent Slice 24A completion record. No production code or plan content was
changed by this review.

## Blockers before implementation

1. **Failed-refresh retention is broader than the behavior the plan says it
   preserves.** The plan requires a failed refresh to retain the current detail
   (lines 101-103 and 137-138) while also preserving existing stale-forecast
   behavior (lines 46-49). Today,
   `retainVisibleCacheAfterRefreshFailure` only retains a `ForecastReady` value
   whose freshness is already cache-derived; a fresh live result returns `null`
   and becomes `NoCacheError` ([OxygenAppStateHolder.kt](../../app/src/main/kotlin/com/oxygen/weather/app/OxygenAppStateHolder.kt#L802)).
   Decide which contract applies before coding: either constrain the detail
   failure test and promise to the existing restored/stale-cache case, or
   deliberately retain a fresh in-memory dashboard after a later failure and
   update the forecast-freshness semantics and regression scope accordingly.
   The latter is behavior beyond alert navigation, so it cannot be smuggled in
   under the current compatibility claim.

2. **The refresh-start transition must explicitly preserve `AlertDetail`.** The
   plan correctly specifies that loading remains on the selected detail (lines
   97-103 and 134-136), but the current refresh start path directly assigns an
   `OxygenAppScreen.Home` at lines 448-455 of
   [OxygenAppStateHolder.kt](../../app/src/main/kotlin/com/oxygen/weather/app/OxygenAppStateHolder.kt#L431).
   Only the later result path uses `withVisibleOrReturnScreen` (lines 786-790).
   State that `startHomeForecastLoad` must update the detail route's
   `returnHome`, rather than replace the visible screen, and make the focused
   test assert the screen remains `AlertDetail` immediately after the loading
   emission. Otherwise the promised retained-selection flow cannot occur.

## Material gaps

1. **The multi-alert selector needs an explicit selected-state contract.** The
   plan asks for accessible, individually labelled selector rows (lines 14-15
   and 116-117), but does not require an exposed selected state or test it.
   Add a semantic `selected` state (and a readable selected/current label where
   needed) for the active alert, then assert it changes when alert two is
   selected. An event/severity/expiry label alone does not tell a screen-reader
   user which long alert body they are reading.

2. **The plan should name the exact route update rules, not only the generic
   helper.** `visibleOrReturnScreen` and `withVisibleOrReturnScreen` presently
   understand only `About` ([OxygenAppStateHolder.kt](../../app/src/main/kotlin/com/oxygen/weather/app/OxygenAppStateHolder.kt#L1145)).
   The new route needs both helpers to recurse through `AlertDetail` while
   preserving its selected ID and replacing only its immutable `returnHome`.
   Specify that behavior alongside the proposed screen type; otherwise unit
   remapping, refresh results, location-entry return handling, and About's
   existing nesting can each receive subtly different implementations. The
   proposed unit tests cover some outcomes but not this common routing rule.

3. **The detailed screen’s provenance labels need fixture assertions, not just
   the source URI.** The plan requires issuer, attribution, source-check time,
   and an alert-specific semantic label, and it already tests the selected
   alert's URL/text well. Add assertions that the rendered detail keeps the
   selected alert's issuer and the `AlertLookupStatus.Available.metadata`
   source-check time, with NOAA/NWS attribution. This protects the required
   distinction between an alert's effective/sent time and its lookup time; the
   NWS contract expressly forbids substituting one for the other.

## Authority drift

The installed Slice 24A summary is correctly described in README's feature
list and in `DATA_SOURCES.md`, `PRIVACY.md`, the About disclosure, and the full
specification. Two lower-authority statements remain stale:

- README line 182 still says installed official-alert presentation is absent.
- `docs/data-sources/NWS_ALERTS.md` lines 4-8, 142-143, and 184-190 still call
  NWS roadmap-only and say installed presentation is out of scope.

The active plan now includes both corrections at lines 185-197. That resolves
the scope omission noted in the previous review; make the contract change in
the same post-implementation authority sync, retaining detail navigation as
future work until this slice is actually verified and committed. No provider
request or terms change is warranted by this documentation drift.

## Size and LLM-slop assessment

The active plan is 203 lines, 1,487 words, and 11,833 bytes. A practical
planning-token estimate is about 2,900-3,600 tokens, roughly 21-26% of its
14,000-token implementation budget. The plan is still usable, but the budget
is tight once state-machine work, connected instrumentation, screenshot
inspection, and authority sync are included. Do not add ceremony before the two
blockers are resolved.

The plan is largely concrete rather than performative: it defines a real
installed/connected boundary, names source and state transitions, gives exact
fixtures, and prohibits mock-only proof. No fake production path, TODO-only
claim, status inflation, or test-existence-only substitute was found.

The removable padding is repeated negative scope. The same exclusions appear
in the slice boundary (lines 30-33), compatibility rules (37-39), focused-test
tail (149-150), and documentation paragraph (193-197). Keep the first list
and the one compatibility sentence that protects repository behavior; collapse
the later repetitions into a short completion boundary. This is modest cleanup,
not a reason to rewrite an otherwise behavior-oriented plan.

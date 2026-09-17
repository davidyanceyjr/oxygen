# Oxygen UI v0.3 — UI-03 Checkpoint A

**Status:** verified
**Commit state:** committed with this change
**Mode:** bounded test-only evidence review and documentation synchronization
**Cycle ID:** `2026-09-16-ui-03-checkpoint-a`
**Active roadmap:** `.codex/plans/ui-roadmap.md`
**Selected slice:** UI-03 — Checkpoint A
**Dependencies:** UI-01 and UI-02 are verified and committed at `3f1742a`
and `4216d83`; the working tree contains no change in production or test
source.
**Implementation slices since last checkpoint:** 0; reset after this checkpoint
closed.

## Slice outcome and acceptance boundary

Reconcile the verified UI-01 forecast-horizon transport and UI-02 Standard
Home pager/Back behavior across the active plan, UI roadmap, cycle history,
README, and any directly affected higher-authority contract. Preserve the
existing production and test implementation. This slice introduces no product
behavior, test behavior, provider behavior, persistence behavior, or runtime
configuration.

The checkpoint is complete only when all of the following are true:

- the retained UI-01 and UI-02 artifacts are present, readable, and sufficient
  for the claims made about each accepted boundary;
- commit subjects, changed-file scope, focused results, broad results, limits,
  and artifact paths agree with the two committed changes;
- the roadmap identified UI-03 as the checkpoint selected through this plan,
  now records it verified, keeps UI-01/UI-02 verified, and leaves later
  candidates specified;
- the README and any affected contract describe only the verified behavior and
  preserve all stated gaps and out-of-scope limits;
- the new cycle ledger records the audit, checks, result, limits, and commit
  state; and
- the final repository review finds no stale status, unsupported completion
  claim, whitespace error, or untracked generated output.

If any retained result is missing, ambiguous, contradictory, or weaker than
the corresponding claim, stop UI-03 at that boundary. Record the exact gap and
select the smallest separate repair or evidence slice; do not repair product
code, rewrite a failed result as success, or close the checkpoint through
documentation alone.

## Implementation and document work

Perform the work in this order:

1. Confirm the dependency commits and inspect the retained UI-01 and UI-02
   artifacts, including their verification ledgers/results, screenshots and
   hierarchies where applicable, and recorded limits. Confirm that the current
   source is the committed source exercised by those artifacts.
2. Build a short claim matrix in the cycle artifact ledger covering, at
   minimum, each slice's boundary, production/test files, focused evidence,
   broad checks, real-path evidence, out-of-scope limits, commit, and status.
   Use existing evidence; do not rerun a passing journey solely to enlarge the
   artifact set.
3. Reconcile `.codex/plans/ui-roadmap.md`: correct its stale top-level and
   active-selection wording to identify UI-03 as the checkpoint selected by
   `current.md`, retain UI-01/UI-02 as verified, and keep UI-04 onward
   `specified`. Do not mark a later candidate planned or claim UI-03 verified
   before its checks and ledger are complete.
4. Reconcile `README.md` only for claims directly affected by UI-01/UI-02.
   Preserve the existing statements that the app is not release-ready and
   that unrelated accessibility, localization, alert, and release gaps remain
   unverified.
5. Inspect `docs/OXYGEN_FULL_SPECIFICATION.md`,
   `docs/OXYGEN_UI_SPECIFICATION.md`, and the Open-Meteo/MET Norway provider
   contracts for factual drift. Update one only if it contradicts the already
   verified UI-01/UI-02 behavior; otherwise record that it was reviewed and
   unchanged in the ledger.
6. After evidence and checks pass, update this plan to the factual completion
   state, append one self-contained UI-03 entry to
   `.codex/cycles/history.md`, and perform the final status/claim/path review.
   Do not create a second documentation change solely to record a future commit
   hash.

## Focused evidence and tests

Retain, inspect, and reference these existing acceptance artifacts:

- UI-01: `.codex/test-artifacts/2026-09-16-ui-01-forecast-horizon-transport/`
- UI-02: `.codex/test-artifacts/2026-09-16-ui-02-standard-pager-back/`

The UI-03 cycle ledger belongs at:

`.codex/test-artifacts/2026-09-16-ui-03-checkpoint-a/verification-ledger.md`

The ledger must record each command, exit/result interpretation, whether it
was reused or run, and why connected/emulator evidence is or is not applicable.
It must distinguish wrapper failures from underlying command results where the
retained UI-01 ledger already documents that condition.

Run these checks against the current committed implementation after the audit:

```bash
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

The first two checks are the minimum broad regression checks for the two
changed production boundaries. `git diff --check` covers the documentation
diff. Do not run connected tests, start an emulator, install an APK, make live
provider requests, or capture new screenshots for this documentation-only
checkpoint: UI-01 and UI-02 already have their required provider/Room and
installed Android acceptance, and no runtime boundary changes here can affect
those results. If source, test inputs, or the execution environment changes,
reassess this decision and record the reason before rerunning affected
evidence.

## Files in scope

- `.codex/plans/current.md` — this active slice plan and final completion state;
- `.codex/plans/ui-roadmap.md` — stale UI-03 selection/status wording only;
- `.codex/cycles/history.md` — one completion entry at closure;
- `README.md` — directly affected verified-behavior claims only;
- `docs/OXYGEN_FULL_SPECIFICATION.md`,
  `docs/OXYGEN_UI_SPECIFICATION.md`, and provider contracts — review and
  update only if factual drift is found;
- `.codex/test-artifacts/2026-09-16-ui-03-checkpoint-a/` — ignored evidence
  ledger and command output, if produced.

No production source, Android test source, Gradle configuration, provider
implementation, fixture, schema, emulator state, or unrelated documentation
is in scope.

## Explicit limits

This checkpoint does not implement or verify Hourly/Daily window selection,
visual redesign, Simple Back, supporting-surface Back, new providers, alert
persistence, TalkBack service traversal, localization completion, release
packaging, or Oxygen 1.0 completeness. It does not turn retained screenshots,
compilation, or documentation into proof beyond the acceptance boundaries
they actually cover.

## Sizing and next action

Sizing: 4/10, small-to-medium. The slice has one documentation/evidence
boundary, two retained acceptance records, a claim matrix, three bounded
checks, and multi-authority closure. No product implementation or new Android
journey is included. A missing or contradictory acceptance record is a separate
repair and is not absorbed into this estimate.

Result: the dependency/artifact audit, claim matrix, authority review, three
required checks, and final status/claim/path review passed. UI-01 and UI-02
remain verified and committed; UI-03 is verified and introduces no product,
test, provider, persistence, or runtime behavior.

Next action: select UI-04 as the next bounded production slice. UI-04 remains
specified until selected by a new active plan.

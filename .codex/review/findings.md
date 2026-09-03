# Slice 18J Plan Review Findings

Review date: 2026-09-02
Reviewed files:

- `.codex/plans/current.md`
- `.codex/cycles/history.md`
- `.codex/plans/mvp-roadmap.md`
- `docs/OXYGEN_FULL_SPECIFICATION.md`
- `README.md`
- current Home presentation/UI source references

This file records review findings only. It does not mark Slice 18J covered,
implemented, verified, ready, or committed.

## Findings

### High: Slice 18J is likely too broad for one small vertical slice

The active plan combines atmospheric scene foundation, Now hierarchy, mark
integration, custom navigation, surface vocabulary, Hourly/Daily comparison
visualization, Details semantic grouping, effects-off preservation, and
regression coverage in one cycle.

This matches the roadmap direction, but it creates a real risk of a large
visual rewrite plus presentation-model refactor instead of one small, complete
vertical slice.

Suggested next-session tightening:

- Make semantic metric identity the first mandatory checkpoint.
- Limit the first visual pass to one bounded surface concern, such as Now
  hierarchy plus scene foundation, unless the implementation proves the broader
  scope remains small.
- Keep Hourly/Daily visualization optional unless semantic numeric presentation
  data is already straightforward.

### Medium: Acceptance language is partly subjective

The plan uses terms such as "visibly improve," "recognizably Oxygen," and
"faster than reading independent generic cards." These are useful visual goals,
but they can invite confident status language without enough evidence.

Suggested next-session tightening:

- Require named baseline and final screenshot pairs for Now, Hourly, Daily, and
  Details.
- Record concrete before/after observations, not just a pass/fail visual claim.
- Treat screenshots as presentation evidence, not pixel-perfect automated proof.

### Medium: Current production code has real semantic drift

The plan correctly identifies an existing implementation problem:

- `HomeLoadingScreen.kt` promotes Now context metrics by exact English labels
  such as `"Humidity"` and `"Wind"`.
- `HomeLoadingScreen.kt` groups Details metrics by exact English display labels.
- `HomeMetricPresentation` currently carries only `label` and `value`, so
  production Home lacks semantic metric identity for grouping or prominence.

This is not speculative cleanup. It should be fixed before visual decisions are
expanded, otherwise new visual hierarchy may deepen the label-dependency.

Suggested next-session tightening:

- Add `HomeMetricIdentity` or equivalent semantic identity to the Home
  presentation model.
- Cover grouping/prominence by semantic identity in focused tests.
- Add a focused static or unit boundary proving production Home does not group,
  promote, choose iconography, visualize, or parse numeric values from localized
  display labels or formatted weather strings.

### Medium: Token/read size remains a process risk

Current measured sizes during review:

- `.codex/plans/current.md`: 208 lines, 1,487 words.
- `.codex/cycles/history.md`: 359 lines, 1,826 words.
- `.codex/plans/mvp-roadmap.md`: 1,734 lines, 7,601 words.
- `docs/OXYGEN_FULL_SPECIFICATION.md`: 1,896 lines, 6,206 words.
- Normal authority set checked in this review totals about 19,194 words before
  source-code discovery.

The tail-limited history contract is helping. The roadmap is still large enough
that future discovery should continue using targeted section reads/searches
rather than loading the full file unless an authority conflict requires it.

### Low: Recent history has minor status wording drift

Git history confirms Slice 18I is committed at `02f7012 Complete Slice 18I
mobile ergonomics`, and the current plan/roadmap summary correctly treats 18I
as committed.

However, the live recent history entry for
`2026-09-02-mobile-one-handed-home-ergonomics` still says:

```text
Commit: not committed in this turn
```

This is not a blocker, but it can confuse future status reads. A small
documentation sync can update that entry or add a concise correction note.

### Low: Art-sheet version drift remains open

Recent history still records the known drift where the tracked path/spec say
Base Art Sheet v0.1 while the visible image title says v0.2.

This is not an 18J blocker unless Slice 18J cites the art sheet as exact asset
authority. It remains a documentation/asset-governance cleanup item.

## Blockers

No hard blocker was found for starting Slice 18J.

Slice 19A remains correctly blocked until Slice 18J is committed with
installed-app visual evidence and no unresolved regression against the Slice
18H/18I accessibility and behavior baseline.

## LLM-Slop Risks To Guard Against

- Claiming "visual convergence" from compilation, source review, or plan text.
- Making broad color/layout changes without installed-app before/after
  screenshot evidence.
- Adding visual decoration that changes or obscures required weather meaning.
- Treating sample/scaffold weather data as production evidence.
- Adding provider, persistence, saved-location, unit, alert, release, or MVP
  readiness behavior under a visual slice.
- Adding tests that only prove symbols, constructors, tags, or static text exist.

## Evidence From This Review

Commands run:

```sh
git status --short
git log --oneline --decorate -12
git diff --check
wc -l -w -c .codex/plans/current.md .codex/cycles/history.md .codex/plans/mvp-roadmap.md docs/OXYGEN_FULL_SPECIFICATION.md AGENTS.md README.md
rg searches over plan/spec/history and Home source/test files
```

Result:

- `git diff --check` passed.
- No Kotlin, Android unit, instrumentation, assemble, install, or emulator
  commands were run because this was a plan review, not an implementation
  cycle.

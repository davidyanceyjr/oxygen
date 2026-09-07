# Slice 26 plan review

Reviewed 2026-09-06 against local `main` at `af16a9f`, including the uncommitted
active plan. This is a plan/source review, not functional verification. Only
this findings file was changed; the active plan and implementation were left
untouched. The current user's request to write this file supersedes the plan's
line 9 instruction to preserve the previous review.

Slice 26 is dependency-ready and correctly selected. The roadmap names it as
the next candidate after committed Gate 25 and requires Slice 18I, Slice 25A,
and small-state persistence. Git history confirms `02f7012`, `2484e90`, and
`23a9d49`; production Home, Settings routing, MainActivity injection, and the
existing Preferences DataStore implementation support those prerequisites.
The six proposed connected targets include two existing methods whose names
match the repository. New effects storage, selection, and motion handling
remain planned, not implemented or verified.

The production boundary is appropriately narrow: one persisted Off/Subtle
preference, effective motion policy, Settings feedback, and the existing Home
rendering/navigation path. The current scene is static; adding an animation
engine or claiming battery savings would be unsupported. Deferring Full is
faithful to the roadmap's **Off baseline**, provided the specified third level
remains explicit unfinished scope as the plan requires. No prerequisite calls
for Simple, theme selection, or high contrast in this slice.

The plan is close, but three evidence gaps prevent implementation readiness.
They can be corrected within the proposed test budget without expanding the
product scope.

| Acceptance requirement | Planned work and evidence | Review |
| --- | --- | --- |
| 1. Reachable, accessible Off/Subtle selector and Back | SettingsScreen and OxygenApp wiring; connected cases 2, 3, 5; installed baseline/final captures | Covered in plan; make the new controls' selected semantics and 48-dp bounds explicit assertions in those cases. |
| 2. Default, invalid records, read failure | Concrete effects DataStore; focused storage/state tests | Storage behavior is addressed; rendered read-failure evidence is missing (finding 2). |
| 3. Confirmed/pending/error/retry and ordering | Guarded state-holder action, off-main-thread writes; focused race tests and connected write-failure case | Covered in plan. Preserve the distinction between confirmed choice and effective fallback. |
| 4. Restore before decoration and across lifecycle/transitions | Startup/state reconstruction work; delayed-restore unit tests, DataStore recreation case, installed force-stop/relaunch | Persistence is addressed; first-render and actual Activity recreation checks need clarification (finding 1). |
| 5. Off preserves weather, alerts, source, stale/failure meaning, units and navigation | Existing Off branch; connected stale/alert journey and units regression; installed four-page journey | Appropriately bounded. Include missing-value and refresh-failure text in the existing fixture assertions, and exercise refresh once after selection with the expected request delta. |
| 6. Android override and unanimated navigation | Initial/resume adapter, effective resolver, conditional Home page motion; actual system-setting connected/manual exercise | Real platform input is required correctly; the temporal navigation assertion is unspecified (finding 3). |
| 7. Compact/large-font readability and accessible feedback | 360dp x 640dp/font 1.3 evidence, baseline/final screenshots and semantics | Normal and override paths are addressed; failure feedback needs the rendered evidence in finding 2. |
| 8. No additional requests or changes to canonical data/other preferences | Focused recording repositories/state tests and connected call-count delta after initial settling | Correct separation of deterministic call-count proof from installed visual evidence. |

1. **Cover the promised first rendered state and name actual Activity recreation.**
   Plan lines 116–119 require no transient decorated Home while stored Off is
   loading and survival across Activity recreation. Lines 188–190 put delayed
   restore in unit scope; connected case 1 says only “recreated app boundary,”
   and the manual journey checks the settled result after force-stop. A correct
   state-holder policy does not prove OxygenApp applies it before the first
   decorated frame. The existing unit-preference instrumentation constructs a
   new storage/state-holder pair; it does not recreate an Activity
   (`OfflineLaunchPersistenceInstrumentedTest.kt:155`).

   Minimum change: explicitly make connected case 1 recreate the production
   Activity (for example, `ActivityScenario.recreate()`) and assert the selected
   choice/effective scene after restoration. Extend the planned Compose case
   with a controllably delayed effects read while Home weather is already
   available: assert scene absence before releasing the read, then after Off
   resolves. Use the real OxygenApp resolver/wiring. This separates storage
   readback, Activity lifecycle, and the no-flash guarantee without adding a
   new persistence architecture or another full suite.

2. **Exercise read-failure feedback through the rendered Appearance route.**
   Plan lines 108–110 require a usable weather path, conservative effective Off,
   and a visible local-storage error without falsely claiming restoration.
   The unit list covers read failure, but connected case 3 injects a *write*
   failure. Those are different states: a failed initial read has no confirmed
   stored choice. The installed steps never induce either failure, yet lines
   269–270 ask the resulting captures to establish readable error state.
   Successful-selection screenshots cannot establish that claim.

   Minimum change: add an initial read-failure phase to the planned error UI
   evidence, or add one focused connected case (seven remains below the
   eight-case default). Through OxygenApp, assert effective Off, explicit
   unconfirmed/error wording, usable Back/weather navigation, and the defined
   recovery action. At compact/large font, retain a rendered failure capture
   and semantics with selector targets, selected-state meaning, error text,
   and logical traversal. Label injected-error evidence as deterministic UI
   evidence; keep the normal installed journey focused on real persistence
   and rendering. Do not require deliberate corruption of user data.

3. **Define an observable test for the absence of page-transition motion.**
   Plan lines 129–130 and connected case 4 promise unanimated programmatic
   navigation, but specify only the resulting condition. Home currently calls
   `animateScrollToPage` in its previous/next accessibility actions and tab
   callback (`HomeLoadingScreen.kt:247`, `:256`, `:282`). Settled page labels,
   screenshots, or `waitForIdle()` can pass after an animated transition too.
   Checking the Android source and effective enum alone leaves the pager
   behavior unproved.

   Minimum change: state the temporal assertion in case 4 (or the existing
   Compose journey): use controlled frames or an equivalent observable pager
   transition check to establish destination change without intermediate
   animated scrolling, for direct tabs and both available accessibility page
   actions under reduced motion. Retain the actual Android zero-scale/resume
   exercise and stored-Subtle readback; a fake signal may supplement it but
   cannot replace it. No animation framework or performance benchmark is
   needed.

The Android API assumption itself is supported: `areAnimatorsEnabled()` is
available from API 26 and reports system animation disablement, including
zero animator duration scale. The proposed initial/resume sampling is a
bounded implementation choice, not proof of every OEM accessibility signal.
[Official Android ValueAnimator reference](https://developer.android.com/reference/android/animation/ValueAnimator#areAnimatorsEnabled()).

Broad verification, artifact retention, one-emulator lifecycle, timeout policy,
and out-of-scope limits are otherwise concrete and appropriate. The plan names
focused unit commands, a filtered six-case connected command, compile, full
app/core debug unit suites, assemble, and whitespace validation. It correctly
credits an unchanged final assembly rather than requiring another build.
README, specification section 53, roadmap boundary/sequencing, privacy/local
storage disclosure, append-only cycle evidence, and post-commit authoritative
doc sync are all assigned. The prospective cadence count is supported by
`a4c4e56` introducing the test-volume policy after Slice 25A; the next session
after Slice 26 is explicitly reserved for test-only/documentation sync.

Known historical sample-screen/sequence-summary drift does not block this
preference contract and should not trigger unrelated cleanup. The prior Gate
25 review is not a current effects-slice blocker list. No unsupported claim of
new runtime verification was found in the active plan.

Review evidence: read the plan, relevant roadmap/specification sections, root
AGENTS/README/provider template, Gradle/environment/UI workflow files, current
production/tests, the live history contract/summary and latest three entries,
Git commit history/status, and the retained Gate 25 ledger. Checked the official
Android API reference. Ran `git diff --check` after writing this review.
Not run: Android compile, unit tests, connected tests, assemble, emulator,
install/capture, or live providers; this task explicitly excludes implementation
and does not require re-verifying previously committed behavior. No commit or
post-commit sync was performed.

NEEDS REVISION

Blocking issues: add rendered delayed-restore and explicit Activity-recreation
evidence; add rendered read-failure/accessibility evidence; specify a temporal
assertion for reduced-motion page navigation.

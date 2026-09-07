# Gate 25 - Disclosure Baseline Check

**Status:** committed
**Commit state:** implementation committed at `23a9d49`; authority sync committed in this documentation sync
**Cycle ID:** `2026-09-06-gate-25-disclosure-baseline-check`
**Planning basis:** local `main` at `a4c4e56`, with Slice 25A committed at
`2484e90` and its authority sync committed at `87457bd`. The worktree was clean
at discovery before this plan replaced the completed Slice 25A plan.
**Review state:** revised after the 2026-09-06 Gate 25 plan review. The eight
blocking findings and three minimum-evidence corrections were resolved before
implementation; execution evidence is recorded below.

## Selection rationale

The roadmap explicitly identifies Gate 25 as the next candidate after committed
Slice 25A and says not to activate Slice 26 or later appearance work first.
Gate 25 is dependency-ready:

- the Repository Engineering Gate is `ready`, satisfying Gate 25's stated
  prerequisite;
- Slice 25A is committed and supplies independently reachable Data Sources,
  Privacy, Open Source Licenses, and About destinations;
- active Open-Meteo forecast/geocoding/timezone, MET Norway fallback, and NWS
  alert paths already exist in production code and have prior committed cycle
  evidence;
- the root disclosure documents and provider contracts already exist, so this
  gate can stop at reconciliation and installed reachability rather than adding
  a new disclosure architecture.

This plan selects Gate 25 only. It does not make the gate covered, implemented,
verified, committed, or ready.

## Repository facts observed during planning

- `InstalledForecastRepositoryFactory` currently composes Open-Meteo as the
  default forecast repository, MET Norway as fallback, a Room-backed forecast
  cache supplied by `MainActivity`, and `NwsAlertProvider` through
  `AlertMergingWeatherRepository`.
- `MainActivity` supplies production Room saved-location/forecast storage and
  DataStore selected-location/unit storage. Its permission launcher requests
  coarse location only through the explicit location action callback.
- The source manifest declares `INTERNET`, `ACCESS_NETWORK_STATE`, and
  `ACCESS_COARSE_LOCATION`; it does not declare fine or background location.
- An unretained planning inspection of the release runtime tree and production
  source found only the expected AndroidX, Kotlin, Room, and serialization roots
  and no obvious advertising, analytics, telemetry, account, Firebase, Facebook,
  Adjust, Appsflyer, or Play Services path. This is discovery only. Gate evidence
  must be regenerated with the exact commands and retained artifacts below.
- `LICENSE`, `NOTICE`, `THIRD_PARTY_LICENSES.md`, `DATA_SOURCES.md`, and
  `PRIVACY.md` are present. `README.md`, `PRIVACY.md`, and the installed
  privacy copy currently describe the active providers, optional coarse
  foreground location, local persistence, no ads/tracking, and no mandatory
  account in a way consistent with the inspected production paths.
- The installed Settings Data Sources copy names the active providers but gives
  explicit data-license text only for Open-Meteo timezone resolution. It does
  not state the Open-Meteo forecast/geocoding, GeoNames, or MET Norway license
  terms even though the About copy says this surface discloses current provider
  licenses.
- Root `DATA_SOURCES.md` describes MET Norway data only as NLOD 2.0. The provider
  contract and the first-party MET Norway pages reviewed on 2026-09-06 describe
  NLOD 2.0 and CC BY 4.0 together, while production `MetNoForecastMapper` emits
  `NLOD-2.0 OR CC-BY-4.0`. This conflict requires the bounded provenance
  correction below before disclosure copy is changed.
- `docs/data-sources/OPEN_METEO_GEOCODING.md` still says its client and fixtures
  are future and that the contract does not make geocoding active, despite the
  installed manual-search production path. The Open-Meteo forecast and MET
  Norway contracts also retain isolated pre-implementation wording, and the NWS
  contract retains text saying installed alert presentation is future despite
  committed Slices 24A and 24B.
- The installed Open Source Licenses copy names the repository `LICENSE` but
  does not state the repository's declared `GPL-3.0-or-later` license identifier.
- Specification section 44 still nests Data Sources, Open Source Licenses, and
  Privacy under About Oxygen, while Slice 25A exposes them as direct Settings
  destinations. Section 53 also still calls the units path
  `Settings / About / Units`. Both are higher-authority conflicts that must be
  reconciled before production work.
- The active provider contracts require provider links, including distinct
  Open-Meteo and GeoNames links for geocoding, but the installed disclosure
  model can render only static paragraphs.
- `MetNoForecastClient` already sends `If-Modified-Since` when given a cached
  Last-Modified value and classifies HTTP 304 as `NotModified`.
  `MetNoWeatherRepository` does not supply that cache metadata and cannot reuse
  a cached forecast after 304, so only installed end-to-end revalidation remains
  deferred.
- Installed Data Sources copy says NWS lookup follows any terminal forecast
  result. `AlertMergingWeatherRepository` performs lookup only after forecast
  success; loading and failure pass through without alert lookup.
- The roadmap body names Gate 25 as next and records Slice 25A as committed, but
  its header still says it is synchronized only through `cab3b29`, before the
  Slice 25A implementation and authority-sync commits.
- Existing disclosure unit tests mostly assert selected substrings. Existing
  connected coverage proves Settings destination reachability, but does not
  prove the missing provider-license matrix is visible through the Compose
  surface.

No Android test, connected test, emulator exercise, or live provider request was
run while preparing or revising this plan. During execution, the plan review's
official-page access date remained 2026-09-06 and no live provider request was
required. The plan review opened the
Open-Meteo licence page and both named MET Norway licensing pages on 2026-09-06;
that access date must be recorded in the implementation ledger without changing
older provider-contract review dates.

## Pre-implementation authority resolution (resolved)

These two conflicts were resolved in order before red-first implementation.

1. Amend specification section 44 so Data Sources, Privacy, and Open Source
   Licenses are required as direct Settings destinations alongside About. The
   disclosure obligations and five required root files remain unchanged. Amend
   section 53's obsolete Units path in the same authority-first edit, review the
   Markdown diff, and only then change production code. This explicitly aligns
   the higher authority with the already committed Slice 25A architecture.
2. Treat the first-party MET Norway wording reviewed on 2026-09-06 and the
   provider contract's conjunctive wording as governing. Add a failing mapper
   assertion, change new MET Norway provenance to
   `NLOD-2.0 AND CC-BY-4.0`, and normalize only the exact legacy MET Norway
   `NLOD-2.0 OR CC-BY-4.0` value at the Home presentation boundary. This avoids
   a Room schema rewrite while keeping an existing cached forecast truthful.
   Do not alter provider selection, requests, weather values, cache identity, or
   unrelated provenance. Reconcile the provider contract and disclosures to the
   same wording before proceeding to link/copy work.

## Acceptance boundary

Gate 25 is complete only when all of the following are true at the same
revision:

1. Specification sections 44 and 53 explicitly match the committed Slice 25A
   direct Settings hierarchy before production disclosure changes begin.
2. The five required root files remain present, internally consistent, and
   accurate for the inspected repository and installed app:
   `LICENSE`, `NOTICE`, `THIRD_PARTY_LICENSES.md`, `DATA_SOURCES.md`, and
   `PRIVACY.md`.
3. Active/current provider disclosure matches the production composition:
   Open-Meteo forecast and timezone resolution, Open-Meteo geocoding based on
   GeoNames, MET Norway forecast fallback after eligible Open-Meteo failures,
   and foreground selected-point NOAA/NWS alerts after forecast success.
   Loading or failed forecasts are not described as triggering alert lookup,
   and roadmap-only providers remain clearly non-active.
4. Data Sources states the applicable license/attribution baseline for each
   active data path without presenting provider or government data as Oxygen
   source code or implying endorsement:
   Open-Meteo forecast/timezone and geocoding under CC BY 4.0; GeoNames under
   its documented Creative Commons attribution terms; MET Norway under NLOD
   2.0 and CC BY 4.0; and the NWS public-information and requested-credit/
   attribution qualification from its contract. Accessible URI actions are
   present for Open-Meteo forecast/timezone, Open-Meteo geocoding, GeoNames,
   MET Norway, and NOAA/NWS using the URLs isolated in disclosure content.
5. New and legacy-cached MET Norway forecasts present the same corrected
   conjunctive license wording without changing forecast or cache behavior.
6. Privacy disclosure names the data each active request sends, including
   selected coordinates/timezone/weather variables, typed search text,
   optional altitude, identifying provider headers where required, and normal
   network metadata. It also remains explicit that manual search needs no
   location grant, device location is optional coarse foreground acquisition
   after an explicit action, and alert lookup is foreground/process-local with
   no alert persistence or background polling.
7. The installed Open Source Licenses destination identifies Oxygen's declared
   source license as `GPL-3.0-or-later`, describes upstream software
   dependencies truthfully, and keeps software licensing distinct from weather,
   geocoding, and alert data terms.
8. Installed Settings can reach Data Sources, Privacy, and Open Source Licenses
   from the normal app entry; the reconciled text is readable by scrolling at
   the existing compact `360dp x 640dp`, density `1`, font scale `1.3`
   configuration, provider-link controls expose readable labels and link
   semantics, and Back returns to Settings. A deterministic connected journey
   proves its repository request-count delta is zero. The installed manual
   journey proves only observable navigation, scrolling, Back, external-link
   handoff, and absence of a location-permission prompt.
9. Provider contracts no longer describe already active clients, fixtures, or
   installed UI as future work. They state that the MET Norway client-level
   `If-Modified-Since` and 304 result boundary are implemented while installed
   repository/cache revalidation and reuse remain deferred, together with
   provider health/backoff, alert persistence, background polling, and release
   verification.
10. README, specification next-task text, roadmap status/next-candidate metadata,
   active plan, and recent cycle history agree with the completed gate without
   claiming Slice 26, MVP readiness, release readiness, or a complete Slice 33
   audit.

The stopping boundary is corrected disclosure plus observable installed
reachability. The MET Norway provenance wording and accessible disclosure links
are the only runtime changes. No provider request, selection, fallback,
persistence schema, permission, forecast value, alert lookup, location, unit,
appearance, or navigation behavior is added or changed.

## Accuracy source order

Use these sources for the gate audit in this order:

1. Production composition, manifest, Gradle dependency declarations, and
   provider provenance constants for what the current build actually does.
2. `docs/OXYGEN_FULL_SPECIFICATION.md` sections 43 through 45 for privacy,
   attribution, and license obligations.
3. Active provider contracts for request data, attribution, license, privacy,
   and deferred behavior.
4. Prior committed cycle evidence for installed fallback, alert, Settings,
   location, cache, and units reachability.
5. Official primary provider terms/license/privacy pages only for validating
   legal or policy wording. Record the pages and access date actually reviewed;
   do not advance a terms-review date without reading the source.

If current official provider material conflicts materially with the
specification or an existing provider contract, stop the gate and reconcile the
higher authority before changing installed copy. Do not guess at license or
privacy terms.

## Intended production changes

Keep production changes to the disclosure/link surface and the one factual MET
Norway provenance correction:

- add concise license/attribution text for each active provider/data path to the
  Data Sources destination;
- replace the false NWS "terminal forecast result" statement with the exact
  forecast-success condition;
- make the Open Source Licenses destination state `GPL-3.0-or-later` explicitly
  and preserve the source-code versus provider-data distinction;
- correct privacy wording only if the code/contract matrix proves a factual
  mismatch;
- add a small immutable disclosure-link model beside the existing section model
  and keep all labels and HTTPS URIs centralized in
  `AboutDisclosureContent.kt`;
- render those links as labelled, minimum-48-dp actions in `SettingsScreen`
  using Compose `LocalUriHandler`, matching the existing Home/alert external-URI
  approach. Each action must expose button/link semantics and its provider name;
- correct `MetNoForecastMapper` to emit
  `NLOD-2.0 AND CC-BY-4.0` and normalize only the exact old MET Norway `OR`
  value in `HomeForecastPresentationMapper` so restored caches display the
  corrected terms without a database migration;
- keep paragraphs within the existing compact disclosure length guard;
- retain the current destination model, Settings route, scrolling layout, and
  Back behavior.

Do not add a license library/generator, navigation abstraction, new screen,
WebView, dynamic provider discovery, or generic link framework. External URI
actions are required only for the active-provider attribution rows in this
gate.

Expected production files:

- `app/src/main/kotlin/com/oxygen/weather/app/AboutDisclosureContent.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/settings/SettingsScreen.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/HomeForecastPresentationMapper.kt`
- `core/src/main/kotlin/com/oxygen/weather/core/provider/metno/MetNoForecastMapper.kt`

No changes are intended in `OxygenAppStateHolder`, provider clients or
repositories, manifests, Gradle files, Room, DataStore, or other runtime
behavior. If the audit requires anything beyond the files and exact legacy
normalization above, stop and select a separate implementation slice instead of
expanding Gate 25.

## Intended document changes

- Reconcile `DATA_SOURCES.md`, especially MET Norway's conjunctive license
  wording, provider links, and the per-provider active/deferred boundary.
- Reconcile stale implementation-status wording in the four active provider
  contracts without changing provider behavior. For MET Norway, state precisely
  that the client supports conditional request input and 304 classification,
  while installed repository-to-cache revalidation/reuse is deferred.
- Update `THIRD_PARTY_LICENSES.md`, `NOTICE`, and `PRIVACY.md` only where the
  audit identifies a concrete omission or false statement. Do not replace the
  later Slice 33 dependency/privacy audit with an exhaustive transitive-license
  inventory here.
- Keep `LICENSE` unchanged unless the repository's already-declared
  GPL-3.0-or-later intent is proven inconsistent. Any license-policy change is
  outside this gate and requires explicit authority.
- Resolve specification section 44 and the obsolete section 53 Settings path in
  the pre-implementation authority edit. After completion evidence exists,
  record Gate 25 completion and Slice 26 as the next candidate.
- Update the roadmap synchronization metadata and Gate 25 status only after the
  gate is verified and committed. Do not mark Slice 26 planned there.
- Update README only if the audit finds a factual status mismatch; static copy
  clarification alone does not add an implemented feature.

## Execution evidence

Gate 25 is implemented, verified, and committed at `23a9d49`.

Production changes:

- `AboutDisclosureContent.kt` adds the immutable disclosure-link model, active
  provider/license/privacy text, and centralized HTTPS attribution links.
- `SettingsScreen.kt` renders labelled, minimum-48-dp external-link actions
  through `LocalUriHandler`.
- `HomeForecastPresentationMapper.kt` normalizes only the exact legacy MET
  Norway `NLOD-2.0 OR CC-BY-4.0` value.
- `MetNoForecastMapper.kt` emits `NLOD-2.0 AND CC-BY-4.0` for new forecasts.

Focused evidence passed:

- MET Norway client/mapper/repository, disclosure, Home mapper, and installed
  factory unit tests passed in the focused command recorded in the cycle
  artifact ledger.
- The two-case connected run passed on `oxygen_starter`: disclosure
  reachability/link/scroll/Back/zero-call/zero-permission coverage and the
  installed MET Norway fallback provenance regression.

Real-path evidence passed on one emulator session:

- Baseline APK from `a4c4e56` and final APK were installed without restarting
  the emulator. The compact profile was 360x640, density 160, font scale 1.3.
- Data Sources, Privacy, and Open Source Licenses were manually reached and
  scrolled; an Open-Meteo link handed off to Chrome and returned to Oxygen.
- Saved artifacts are under
  `.codex/test-artifacts/2026-09-06-gate-25-disclosure-baseline-check/`.

Bounded audit evidence passed: release runtime dependencies, merged release
manifest summary, classified prohibited-path source search, required root-file
presence, `:app:compileDebugKotlin`, full app/core debug unit tests,
`:app:assembleDebug`, and `git diff --check`. No live provider request,
complete transitive-license audit, or release-candidate verification was run.

Next candidate after this committed gate: Slice 26, Effects Preference.

## Red-first focused evidence

1. Extend `AboutDisclosureStateHolderTest` first so the current test fails on
   the missing disclosure matrix. Assert active provider role, request-data
   facts, provider-specific license/attribution terms, source/data license
   separation, explicit GPL identifier, the NWS forecast-success condition,
   exact link labels/HTTPS URIs, the client-versus-installed conditional-request
   boundary, deferred provider separation, and the existing paragraph-length
   boundary.
2. Add red assertions to `MetNoForecastMapperTest` for the conjunctive license
   value and to `HomeForecastPresentationMapperTest` for exact legacy MET Norway
   normalization plus non-MET/unrelated-license preservation.
3. Keep `InstalledForecastRepositoryFactoryTest` as a regression for the
   injected fallback/cache/alert composition semantics it actually covers. Do
   not claim it proves the factory's concrete default providers. Record the
   Open-Meteo, MET Norway, Room-cache, and NWS default wiring by direct inspection
   of `InstalledForecastRepositoryFactory` and `MainActivity` in the ledger.
4. Add or narrow one `HomeDashboardUiTest` case that opens all three disclosure
   destinations through `OxygenApp`; records the repository call count after
   initial state settles; reaches each destination's final required fact;
   invokes every provider link through a recording `LocalUriHandler`; asserts
   readable labels, URI action/semantics, compact scroll reachability, and Back;
   and finally asserts the repository-call count is unchanged. Run only that
   test case, not the full connected class.
5. Produce a concise audit ledger mapping each accepted statement to production
   code, provider contract, official primary source where reviewed, or prior
   committed cycle evidence. File-presence checks alone are not acceptance
   evidence.

Primary test files:

- `app/src/test/kotlin/com/oxygen/weather/app/AboutDisclosureStateHolderTest.kt`
- `app/src/test/kotlin/com/oxygen/weather/app/HomeForecastPresentationMapperTest.kt`
- `app/src/test/kotlin/com/oxygen/weather/app/InstalledForecastRepositoryFactoryTest.kt`
- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`
- `app/src/androidTest/kotlin/com/oxygen/weather/app/InstalledFallbackRepositoryInstrumentedTest.kt`
- `core/src/test/kotlin/com/oxygen/weather/core/provider/metno/MetNoForecastClientTest.kt`
- `core/src/test/kotlin/com/oxygen/weather/core/provider/metno/MetNoForecastMapperTest.kt`
- `core/src/test/kotlin/com/oxygen/weather/core/provider/metno/MetNoWeatherRepositoryTest.kt`

Focused command budget:

```sh
. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*MetNoForecastClientTest' --tests '*MetNoForecastMapperTest' --tests '*MetNoWeatherRepositoryTest'
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*AboutDisclosureStateHolderTest' --tests '*HomeForecastPresentationMapperTest' --tests '*InstalledForecastRepositoryFactoryTest'
. scripts/android-env.sh && ./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class='com.oxygen.weather.app.ui.home.HomeDashboardUiTest#settingsDisclosuresShowActiveProviderLicenseAndPrivacyBaseline,com.oxygen.weather.app.InstalledFallbackRepositoryInstrumentedTest#eligibleOpenMeteoFailureRendersMetNorwayHomeReadyThroughInstalledFactory'
```

The connected budget is two test cases: one disclosure/interaction case and one
installed-factory MET Norway provenance regression made necessary by the
license correction. Run them in one connected task so the final app/test APKs
are installed once. Use one emulator session, one baseline app install, the one
final connected install, 75 minutes, and 12,000 agent tokens for verification.
Stop at the budget with the exact evidence and gap recorded; do not add or rerun
a connected case without recording the changed input or specific regression
risk first.

## Required real-path exercise

Create
`.codex/test-artifacts/2026-09-06-gate-25-disclosure-baseline-check/ledger.md`
when implementation begins. Use one emulator session. Before any production UI
change, configure the emulator to `360dp x 640dp`, density `1`, font scale `1.3`,
install the baseline APK, navigate to Data Sources, and capture
`data-sources-before.png`. Record the exact configuration commands and original
device values in the ledger. This is the required comparable visual baseline.

```sh
. scripts/android-env.sh
adb shell wm size
adb shell wm density
adb shell settings get system font_scale
adb shell wm size 360x640
adb shell wm density 160
adb shell settings put system font_scale 1.3
scripts/capture-screen.sh .codex/test-artifacts/2026-09-06-gate-25-disclosure-baseline-check/data-sources-before.png
```

After the final APK change, install once more without restarting the emulator
and repeat the same route and device configuration.

From a fresh normal installed-app entry, without selecting sample data or using
a test-only screen:

1. Open Settings.
2. Open Data Sources and scroll through the active-provider and license text.
3. Return to Settings, open Privacy, and confirm the optional-location and
   active-request disclosures are reachable.
4. Return to Settings, open Open Source Licenses, and confirm the explicit
   Oxygen source license and provider-data separation are reachable.
5. From Data Sources, activate one provider attribution link and observe the
   external URI handoff, then return to Oxygen.
6. Return to the originating app state and confirm no location permission prompt
   appeared.

Capture `data-sources-after.png` at the same state as the baseline plus final UI
hierarchy/semantics dumps for Data Sources, Privacy, and Open Source Licenses.
The installed journey proves only reachability, presentation, scrolling, Back,
external URI handoff, and absence of a permission prompt. It does not prove
network or repository call counts; the deterministic connected test supplies
the zero-additional-call evidence. Unit/composition evidence and the audit
ledger prove factual alignment. A live weather-provider request is not required
because this gate changes no provider request behavior and prior committed
cycles are prerequisite evidence for active paths.

## Reproducible privacy and dependency evidence

Use these exact commands once against the final changeset, saving their outputs
under the cycle artifact directory. If the Android Gradle Plugin changes the
merged-manifest output path, record the resolved path and reason rather than
silently substituting a different input.

```sh
gate_artifact_dir=.codex/test-artifacts/2026-09-06-gate-25-disclosure-baseline-check
mkdir -p "$gate_artifact_dir"
. scripts/android-env.sh && ./gradlew :app:dependencies --configuration releaseRuntimeClasspath > "$gate_artifact_dir/release-runtime-dependencies.txt"
. scripts/android-env.sh && ./gradlew :app:processReleaseMainManifest
cp app/build/intermediates/merged_manifest/release/processReleaseMainManifest/AndroidManifest.xml "$gate_artifact_dir/merged-release-AndroidManifest.xml"
rg -n '<uses-permission|<permission|<application|<activity|<activity-alias|<service|<receiver|<provider|android:exported|usesCleartextTraffic|networkSecurityConfig|allowBackup|dataExtractionRules|fullBackupContent' "$gate_artifact_dir/merged-release-AndroidManifest.xml" > "$gate_artifact_dir/merged-release-manifest-summary.txt"
rg -n -i --glob '*.kt' --glob '*.kts' --glob '*.xml' '(firebase|appmeasurement|analytics|telemetry|advertisingid|advertising sdk|facebook|appsflyer|adjust sdk|accountmanager|credentialmanager|oauth|sign[ -]?in|login|tracking|uploader|upload)' app/src/main core/src/main app/build.gradle.kts core/build.gradle.kts gradle/libs.versions.toml > "$gate_artifact_dir/privacy-prohibited-path-source-audit.txt" || test $? -eq 1
```

The ledger must classify every source-audit match rather than equating a search
exit code with proof. Map no-ad/tracking SDK claims to the dependency tree,
Gradle declarations, and classified source search; optional foreground location
and no background component claims to the merged manifest plus `MainActivity`;
no mandatory account to the classified production-source search and installed
entry surface; active request-data claims to the concrete clients and provider
contracts; and local persistence claims to the installed storage composition.
These are bounded Gate 25 checks, not an exhaustive Slice 33 audit.

Record these primary pages and their actual plan-review access date of
2026-09-06 in the ledger: `https://open-meteo.com/en/licence`,
`https://api.met.no/doc/License`, and
`https://www.met.no/en/free-meteorological-data/Licensing-and-crediting`. Do not
advance any provider contract's older last-review date unless its own full
required source set is re-reviewed during implementation.

## Broad verification

After focused evidence and the installed exercise pass, run once against the
final changeset:

```sh
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

Do not rerun a passing command unless relevant source, tests, inputs, or the
execution environment changed. Record every command, result, and rerun reason in
the ledger. If provider-source review or the one installed exercise reaches a
bounded external/platform failure, record the exact gap and do not convert it to
mock success.

## Documentation and commit obligations

Before declaring the gate verified:

- review the diff for unsupported provider, license, privacy, dependency, or
  release claims;
- list every production, test, and authority file actually changed;
- record focused, connected, real-path, and broad commands actually run;
- name any skipped command and why;
- leave Gate 25 `planned` or `implemented` if required evidence is missing.

After the verified implementation/documentation changes are committed, perform
the required authoritative doc sync:

- append a concise self-contained Gate 25 entry to `.codex/cycles/history.md`;
- update this plan to the actual evidence and commit state;
- mark Gate 25 committed and advance roadmap next-candidate guidance to Slice
  26 only if the commit and evidence support it;
- confirm specification sections 44 and 53 remain reconciled and update any
  affected README status;
- run `git diff --check` for the post-commit documentation sync and commit that
  sync separately if it changes tracked files.

## Explicitly out of scope

- Effects Off persistence or any Slice 26 behavior.
- Simple, Standard, Detailed, or Meteorologist layout selection.
- Paper, Terminal, additional theme, dark/light, or high-contrast selection.
- Any appearance control, disabled placeholder, preference key, or migration.
- Provider request, fallback eligibility, cache behavior or schema, alert merge,
  or geocoding behavior changes. The exact MET Norway license-provenance
  correction and legacy presentation normalization specified above are the sole
  provenance exceptions.
- Location permission flow, device acquisition, saved-location, unit, Room, or
  DataStore changes.
- Installed conditional revalidation/cache reuse, provider health/backoff, alert
  persistence, background work, notifications, air quality, radar, maps,
  widgets, or accounts. Existing client-level conditional request and 304
  classification support remains intact and is documented accurately.
- A complete transitive dependency/license inventory or the broader manifest,
  exported-component, backup, cleartext, Play Services, and network-security
  audit reserved for Slice 33.
- Gate 34 release disclosure verification, Gate 35 broad release-candidate
  verification, MVP readiness, release readiness, or release claims.
- Changing Oxygen's source-license policy or importing third-party license
  tooling/assets.

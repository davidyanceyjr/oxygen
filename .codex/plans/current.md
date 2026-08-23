# Active Cycle

Status: verified
Cycle ID: 2026-08-23-met-norway-fixtures-dto-parsing
Mode: feature
Goal: Implement Slice 13A by parsing representative MET Norway Locationforecast compact fixtures into provider-specific DTOs without adding mapping, client transport, repository fallback, cache, or UI behavior.
Roadmap slice: Slice 13A: MET Norway Fixtures and DTO Parsing from `.codex/plans/mvp-roadmap.md`.
Branch or work context: local `oxygen` Android scaffold.
Specification anchors:
- `docs/OXYGEN_FULL_SPECIFICATION.md` sections 1, 4, 5, 6.2, 6.3, 12, 16, 17, 39, 40, 44, 46, and 48
- `.codex/plans/mvp-roadmap.md` Slice 13A and Forecast Provider Scope
- `docs/data-sources/MET_NORWAY_FORECAST.md`
- `AGENTS.md`

Acceptance criteria:
- Add MET Norway Locationforecast compact DTOs under `:core` provider implementation code.
- Add a parser that reads only the first Home-path compact response fields defined by `docs/data-sources/MET_NORWAY_FORECAST.md`.
- Required envelope validation fails deterministically for missing or malformed `type`, `geometry.coordinates`, `properties.meta.updated_at`, `properties.meta.units`, or `properties.timeseries`.
- Nullable weather values remain null and are not fabricated as zero.
- Symbol codes are parsed and preserved for Slice 13B mapping, including unknown and documented typo symbol-code values.
- Time strings and provider unit metadata are parsed as provider data only; timestamp conversion and unit validation/mapping are left to Slice 13B unless required for deterministic parser validation.
- MET Norway DTOs remain isolated from UI/domain consumers.

Acceptance boundary: Slice 13A is complete when fixture-backed core parser tests prove normal compact parsing, missing optional/null preservation, malformed envelope failure, unexpected unit metadata preservation, unknown symbol preservation, documented typo symbol preservation, and UTC/time metadata parsing. Slice 13A is not verified by live provider fetches, mapper behavior, client transport, repository fallback, cache behavior, UI display, or active-provider disclosure changes.

Boundary decisions:
- Do not add MET Norway client transport, mapper, repository, fallback selection, cache schema, active provider disclosures, Compose/UI state, Gradle dependencies, or app behavior.
- Do not update `DATA_SOURCES.md` to list MET Norway as active/current.
- Keep provider-specific DTOs and parser exceptions inside `core.provider.metno`.
- Fixtures live under `core/src/test/resources/providers/metno/`.

Focused evidence:
- `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*MetNo*Parser*'`

Real-path command or procedure:
- None. This parser slice is verified with offline fixtures only. Do not perform live MET Norway fetches as proof of implementation.

Broad verification commands:
- `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin`
- `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest`
- `. scripts/android-env.sh && ./gradlew :app:assembleDebug`
- `git diff --check`

Current gate: verified
Current phase: ready
Last result: Added provider-local MET Norway Locationforecast compact DTOs, parser, representative offline fixtures, and focused parser tests. Focused and broad verification passed; logs are saved under `.codex/test-artifacts/2026-08-23-met-norway-fixtures-dto-parsing/`.
Blocker: none.

## Implementation Plan

1. Mirror existing Open-Meteo parser style for a provider-local MET Norway compact DTO/parser boundary.
2. Add representative compact JSON fixtures for normal Home-path data, missing optional values, malformed envelope, unexpected units, unknown symbol, documented typo symbol, and timezone-sensitive UTC timestamps.
3. Add focused parser tests proving only provider-specific parsing behavior.
4. Run focused parser tests and broad verification commands.
5. Review the diff for SLOP risks and update current/history evidence only with commands actually run.

## Phase Results

- planned: Selected Slice 13A as the next bounded implementation slice after the committed MET Norway provider contract.
- covered: Added `MetNoForecastParserTest` with fixture-backed coverage for normal compact parsing, missing optional/null preservation, malformed envelope failure, unexpected unit metadata preservation, unknown symbol preservation, documented typo symbol preservation, and UTC time metadata parsing.
- implemented: Added provider-local DTOs and parser under `core/src/main/kotlin/com/oxygen/weather/core/provider/metno/`, plus fixtures under `core/src/test/resources/providers/metno/`.
- verified: `. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*MetNo*Parser*'` passed; log saved at `.codex/test-artifacts/2026-08-23-met-norway-fixtures-dto-parsing/focused-metno-parser.log`.
- verified: `. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin` passed; log saved at `.codex/test-artifacts/2026-08-23-met-norway-fixtures-dto-parsing/app-compile-debug-kotlin.log`.
- verified: `. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest` passed; log saved at `.codex/test-artifacts/2026-08-23-met-norway-fixtures-dto-parsing/app-core-test-debug-unit.log`.
- verified: `. scripts/android-env.sh && ./gradlew :app:assembleDebug` passed; log saved at `.codex/test-artifacts/2026-08-23-met-norway-fixtures-dto-parsing/app-assemble-debug.log`.
- verified: `git diff --check` passed with no output; log saved at `.codex/test-artifacts/2026-08-23-met-norway-fixtures-dto-parsing/git-diff-check.log`.
- reviewed: Static boundary review found no MET Norway parser types in app/UI or provider-neutral core surfaces; root disclosure still lists MET Norway as specified roadmap fallback only. Log saved at `.codex/test-artifacts/2026-08-23-met-norway-fixtures-dto-parsing/review-boundary.log`.

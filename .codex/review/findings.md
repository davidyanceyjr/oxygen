# Project Review Findings

Source report: `~/Downloads/Oxygen Project Review — Problems, Recommended Solutions, Roadmap Integration, and Next Actions.md`
Review date: 2026-08-28

This file classifies the validated review findings for roadmap integration. It
does not mark any item planned, covered, implemented, verified, ready, or
committed.

## Roadmap Gates And Prerequisites

- Repository Engineering Gate: license contradiction, baseline GitHub CI,
  hosted CI evidence, `main` branch protection, README maturity/status, and
  evidence-retention policy.
- Persistence Architecture Gate: Room/DataStore decision, lifecycle-aware
  state boundary, provider-neutral persistence, provenance persistence, and
  explicit role/removal path for `FileForecastCacheStorage`.
- Slice 18 Offline Launch depends on the Persistence Architecture Gate and
  must verify installed-app behavior rather than repository-only cache behavior.
- Slice 19 Saved Locations depends on the Persistence Architecture Gate and
  Slice 18, and must reuse the production local location model.
- Slice 20 Unit Preferences depends on DataStore behavior from the Persistence
  Architecture Gate and saved-location foundations.
- Slice 31 and Slice 32 installed-app fallback/cache/provenance work depends on
  the Persistence Architecture Gate, Slice 18, and saved-location foundations.

## Bounded Implementation Slices

- Slice 17B: explicit Home refresh control.
- Slice 17C: Home presentation accessibility evidence baseline.
- Slice 18: offline launch from last forecast after persistence foundations.
- Slice 19: saved-location persistence.
- Slice 20: unit preferences and conversion.
- Slice 21: optional device location after manual/saved-location behavior.
- Slice 22 through Slice 24: official alert contract, provider path, merge, and
  UI while keeping alerts independent from forecast fallback.
- Slice 26 through Slice 29: persisted effects, layout, theme, and
  high-contrast settings using DataStore.

## Amendments To Existing Slices

- README/disclosure updates should occur only when verified installed-app
  behavior changes require wording changes.
- Offline behavior must include online/no-cache, online/cache,
  offline/useful-cache, offline/no-cache, failed-refresh/cache, and
  failed-refresh/no-cache cases.
- Unit conversion belongs at the presentation boundary; canonical stored values
  remain unchanged and missing values stay unavailable.
- Device location remains optional and must flow through the same persistence
  and forecast paths as manual locations.
- Alert-provider failure must not destroy otherwise useful forecast content.

## Release-Gate Requirements

- Release-candidate claims require default Open-Meteo and installed-app MET
  Norway fallback evidence with truthful provenance.
- Release-candidate claims require offline restoration, saved locations, units,
  official alerts, presentation settings, accessibility, privacy, dependency,
  disclosure, and broad verification evidence.
- Data Sources must not list a provider as active/current until the installed
  production path can fetch or serve data.

## Repository-Maintenance Tasks

- Resolve `LICENSE` versus `LICENSE-TODO.md` by project-owner decision.
- Add baseline GitHub CI and record at least one hosted passing run.
- Protect `main` after CI exists.
- Keep raw `.codex/test-artifacts/*` ignored while retaining durable evidence
  through CI or selected reviewable artifacts.

## Validated Local Status

- `OxygenAppStateHolder` still defaults to `OpenMeteoWeatherRepository()`.
- MET Norway provider and fallback classes exist in `:core`, but installed-app
  fallback wiring is not active by default.
- The file-backed forecast cache exists in `:core`, but installed-app durable
  offline behavior is not wired.
- README, `DATA_SOURCES.md`, and `PRIVACY.md` continue to avoid active
  installed-app fallback/offline/saved-location/unit/alert claims.

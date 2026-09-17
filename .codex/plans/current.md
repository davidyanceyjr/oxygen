# Oxygen UI — visual overhaul reset after UI-05

**Status:** verified prior slice; roadmap retired by user decision
**Cycle ID:** `2026-09-17-ui-05-hourly-window`
**Active roadmap:** none; `.codex/plans/ui-roadmap.md` was retired by user decision

**Selected behavior:** Standard Home Hourly presents up to six actual
chronological entries in a stable 2×3 grid with a selected-location local-date
range. Later shows the second six-entry window and Earlier restores the first.
The controls are local UI state and do not refetch or consume Android Back.

## Acceptance result

The mapper stores each hourly entry's selected-zone `LocalDate` and formats
same-day and midnight-crossing ranges without reparsing display text. Standard
Hourly has its own window composition and controls; Simple Forecast continues
to use the prior six-entry composition. The deterministic 12-entry fixture
proved the first and second ranges, entry order and speech, 2×3 non-overlap,
48dp controls, outer pager semantics, no-refetch behavior, and Back to Now.

The installed API-37 Chicago route was manually searched with location
permission denied. It showed `Thu, Sep 17, 1 PM–6 PM`, Later showed the next
actual six through `7 PM–12 AM`, and Earlier restored the first window. The
installed route retained `Hourly, Page 2 of 4`.

## Changed files

- `app/src/main/kotlin/com/oxygen/weather/app/HomeForecastPresentationMapper.kt`
- `app/src/main/kotlin/com/oxygen/weather/app/ui/home/HomeLoadingScreen.kt`
- `app/src/main/res/values/strings.xml`
- `app/src/test/kotlin/com/oxygen/weather/app/HomeForecastPresentationMapperTest.kt`
- `app/src/androidTest/kotlin/com/oxygen/weather/app/ui/home/HomeDashboardUiTest.kt`

## Evidence

Artifact bundle: `.codex/test-artifacts/2026-09-17-ui-05-hourly-window/`.

- Mapper focused test passed.
- Both named connected methods passed once on the recovered `oxygen_starter`
  API-37 session: 1/1 completed, zero skipped and zero failures each.
- `:app:compileDebugAndroidTestKotlin` passed.
- `:app:testDebugUnitTest :core:testDebugUnitTest` passed.
- `:app:assembleDebug` passed; final APK SHA-256 is recorded in
  `installed/apk-sha256.txt`.
- Baseline/final installed PNG/XML and the Later capture are under `baseline/`
  and `installed/`.
- `git diff --check` passed after this documentation closure.

## Limits and next action

This slice retains the 12-entry mapper ceiling and does not claim 72-hour
presentation reach, later windows, date jumps, sparse/empty treatment, Simple
changes, localization, TalkBack traversal, release, or a completed visual
overhaul. The UI roadmap and its UI-06 checkpoint are retired. The next
product action must be a visibly distinct Standard Now hero visual slice;
no replacement roadmap is active. No provider, repository, cache, core,
navigation, or settings behavior changes are needed for this reset.

**TalkBack test policy:** Per the 2026-09-17 user decision, resource-intensive
TalkBack service/speech tests are canceled for this early development cycle.
They are not a required roadmap or release gate. Per-slice Compose accessibility
semantics, target sizes, labels, non-color meaning, and readability obligations
remain in scope.

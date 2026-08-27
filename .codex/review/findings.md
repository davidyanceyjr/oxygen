# Slice 17 Diff Review Solutions

## Review Metadata

- Review date: 2026-08-27
- Reviewer: Codex
- Repository: `/home/opsman/project_git/oxygen`
- Reviewed commit: `bc7834c` (`Retain cached forecast after failed refresh`)
- Scope: Diff review for gaps, blockers, and LLM slop in the last commit
- Build/test commands run during review: `git diff --check HEAD~1 HEAD`

## Recommended Solutions

1. Fix stale-cache copy so the UI does not contradict itself.

   Problem: `HomeForecastMessage.NetworkUnavailable` includes no-cache wording:
   "No cached forecast is available yet." The stale cached forecast card renders
   that same message after a cache was successfully retained, so the UI can show
   both "Cached forecast" and "No cached forecast is available yet."

   Recommended solution: split no-cache error copy from stale-refresh failure
   copy.

   - Keep `HomeForecastMessage` for terminal `NoCacheError` states.
   - Add a separate provider-neutral stale refresh message enum or text mapper
     for `HomeForecastFreshness.StaleAfterFailedRefresh`.
   - For `ForecastError.NetworkUnavailable` in stale mode, use copy like:
     `Refresh could not reach the weather service or network.`
   - Keep the existing no-cache message for
     `HomeForecastPresentationState.NoCacheError`.
   - Add a focused test that fails if stale cached state contains
     `No cached forecast is available yet`.

   Minimal implementation shape:

   ```kotlin
   enum class HomeRefreshFailureMessage(val text: String) {
       NetworkUnavailable("Refresh could not reach the weather service or network."),
       RateLimited("Weather refresh is temporarily rate-limited. Try again shortly."),
       ProviderUnavailable("Weather refresh is temporarily unavailable. Try again shortly."),
       InvalidResponse("Weather refresh returned data Oxygen could not read. Try again later."),
       UnexpectedFailure("Weather refresh failed unexpectedly. Try again."),
   }
   ```

   Then have `ForecastFreshness.StaleAfterFailedRefresh` map to this
   stale-specific message instead of reusing `HomeForecastMessage`.

2. Broaden and test local cache failure handling for non-runtime storage
   failures.

   Problem: `CachedWeatherRepository` catches only `RuntimeException` around
   `ForecastCacheStorage` reads/writes. The file-backed storage can fail with
   non-runtime I/O exceptions from stream creation, serialization, or
   `readObject`. Those failures can escape the repository sequence instead of
   becoming `WeatherRepositoryResult.Failure(ForecastError.LocalCacheFailure)`,
   despite the Slice 17 contract claiming local cache read failure mapping.

   Recommended solution: make storage failure handling match the repository
   contract.

   - Catch `Exception` around `storage.replaceBundle(...)` and
     `storage.readBundle(...)`, not only `RuntimeException`.
   - Do not catch `Error`.
   - On success write/readback failure, emit `Failure(LocalCacheFailure)` as
     today.
   - On failed-refresh retention read failure, emit `Failure(LocalCacheFailure)`.
   - Keep null readback as the missing-cache signal that preserves the original
     provider failure.
   - Add focused tests using storage test doubles that throw a non-runtime
     `IOException`.

   Suggested explicit handling:

   ```kotlin
   val cachedBundle = try {
       storage.readBundle(location.id)
   } catch (_: Exception) {
       return WeatherRepositoryResult.Failure(ForecastError.LocalCacheFailure)
   } ?: return failure
   ```

3. Tighten the tests around the two edge cases.

   Recommended test additions:

   - App state test: stale cache after `ForecastError.NetworkUnavailable`
     exposes refresh-failed copy but does not expose
     `No cached forecast is available yet`.
   - Compose or visible text test if Compose test infrastructure is already
     available: render `HomeLoadingScreen` with stale freshness and assert the
     visible stale card text is internally consistent.
   - Core repository test: failed-refresh retention read throws a non-runtime
     `IOException` and returns `Failure(LocalCacheFailure)`.
   - Core repository test: provider success write/readback throws a non-runtime
     `IOException` and returns `Failure(LocalCacheFailure)`.

4. Keep documentation claims scoped after the fix.

   Existing README, DATA_SOURCES, PRIVACY, and About wording is mostly scoped
   correctly: it says repository/app-state foreground stale retention exists and
   installed-app durable cache/offline launch remains unimplemented. After
   changing behavior, update docs only if the user-visible copy or contract terms
   change.

## Blocker Status

Blocker until fixed: the stale-cache UI copy contradiction is user-visible and
violates the no-SLOP rule.

Blocker until fixed or explicitly narrowed: non-runtime local storage failures
are not mapped as claimed. Either broaden the catch/test behavior or narrow the
Slice 17 claim to runtime storage failures only. Broadening is the better fit
for the repository contract.

## Verification To Run After Fix

```bash
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest --tests '*HomeForecastStateHolderTest'
. scripts/android-env.sh && ./gradlew :core:testDebugUnitTest --tests '*CachedWeatherRepositoryTest'
. scripts/android-env.sh && ./gradlew :app:compileDebugKotlin
. scripts/android-env.sh && ./gradlew :app:testDebugUnitTest :core:testDebugUnitTest
. scripts/android-env.sh && ./gradlew :app:assembleDebug
git diff --check
```

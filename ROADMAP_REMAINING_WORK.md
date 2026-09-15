# Remaining Oxygen Roadmap Work and Release Readiness

| Item | What it does | Status |
|---|---|---|
| Release packaging | Produces the final distributable app artifact and release metadata. | Not complete |
| Signing | Applies a trusted Android signing key so users and devices can install the release build. | Not complete |
| Publication | Distributes the signed app through the intended release channel, such as an app store or download site. | Not complete |
| Alert persistence/background behavior | Saves alerts and refreshes or notifies users when the app is not open. | Not implemented |
| Conditional GET / `304` handling | Reuses unchanged provider responses to reduce bandwidth and requests. | Deferred |
| Provider health/backoff | Temporarily avoids repeatedly failing providers and controls retry timing. | Not implemented |
| Live alert-detail evidence | Confirms the installed app can encounter and open a real active alert, not only deterministic test fixtures. | Unverified |
| Stale/error reproduction | Exercises real network/provider failures and confirms truthful stale or error states. | Unverified |
| Localization | Verifies translated UI, formatting, layout, and accessibility behavior across supported languages. | Unverified |
| Automatic contrast | Confirms the app responds correctly to system contrast/accessibility settings. | Unverified |
| TalkBack traversal | Tests actual screen-reader focus, order, announcements, and actions using the TalkBack service. | Deferred/optional |
| MVP completion | Final confirmation that all required MVP behaviors and release obligations are satisfied together. | Not complete |

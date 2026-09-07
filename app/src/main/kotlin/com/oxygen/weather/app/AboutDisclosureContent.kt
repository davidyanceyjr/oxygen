package com.oxygen.weather.app

enum class SettingsDestination(
    val title: String,
) {
    Appearance("Appearance"),
    Units("Units"),
    Locations("Locations"),
    DataSources("Data Sources"),
    Privacy("Privacy"),
    OpenSourceLicenses("Open Source Licenses"),
    About("About"),
}

data class SettingsDestinationState(
    val title: String = "Settings",
    val sections: List<AboutSection> = aboutOverviewSections,
)

data class AboutSection(
    val heading: String,
    val body: List<String>,
    val links: List<AboutDisclosureLink> = emptyList(),
)

data class AboutDisclosureLink(
    val label: String,
    val uri: String,
)

val settingsDestinationOptions: List<SettingsDestination> = SettingsDestination.entries

fun settingsDestinationState(destination: SettingsDestination?): SettingsDestinationState =
    when (destination) {
        null -> SettingsDestinationState()
        SettingsDestination.Appearance -> SettingsDestinationState(title = SettingsDestination.Appearance.title, sections = emptyList())
        SettingsDestination.Units -> SettingsDestinationState(title = SettingsDestination.Units.title, sections = emptyList())
        SettingsDestination.Locations -> SettingsDestinationState(title = SettingsDestination.Locations.title, sections = emptyList())
        SettingsDestination.DataSources -> SettingsDestinationState(
            title = SettingsDestination.DataSources.title,
            sections = dataSourceSections,
        )
        SettingsDestination.Privacy -> SettingsDestinationState(
            title = SettingsDestination.Privacy.title,
            sections = privacySections,
        )
        SettingsDestination.OpenSourceLicenses -> SettingsDestinationState(
            title = SettingsDestination.OpenSourceLicenses.title,
            sections = openSourceLicenseSections,
        )
        SettingsDestination.About -> SettingsDestinationState(
            title = SettingsDestination.About.title,
            sections = aboutOverviewSections,
        )
    }

private val aboutOverviewSections = listOf(
    AboutSection(
        heading = "Oxygen",
        body = listOf(
            "Privacy-respecting weather with no ads, no behavioral tracking, no mandatory account, and optional location permission.",
            "This app surface discloses the data providers and licenses for the behavior currently implemented in this build.",
        ),
    ),
)

private val dataSourceSections = listOf(
    AboutSection(
        heading = "Active App Providers",
        body = listOf(
            "Forecasts: Open-Meteo is the installed-app default forecast provider for selected locations.",
            "Open-Meteo forecast and timezone data: CC BY 4.0.",
            "MET Norway can serve Home forecasts after eligible Open-Meteo terminal forecast failures.",
            "MET Norway data: NLOD 2.0 and CC BY 4.0.",
            "Location search: Open-Meteo Geocoding API, based on GeoNames data, powers manual place search.",
            "Open-Meteo geocoding data: CC BY 4.0.",
            "GeoNames data: Creative Commons attribution license.",
            "Timezone resolution by Open-Meteo.com (CC BY 4.0): an explicitly requested approximate device point is resolved with a metadata-only timezone=auto request before its normal forecast request.",
            "Official alerts: selected-point NOAA/National Weather Service active alerts are requested in the foreground after forecast success.",
            "NWS information is public information; requested credits apply and third-party page content may have separate terms.",
            "NOAA/National Weather Service does not endorse Oxygen.",
        ),
        links = listOf(
            AboutDisclosureLink("Open-Meteo forecast and timezone documentation", "https://open-meteo.com/en/docs"),
            AboutDisclosureLink("MET Norway licensing and attribution", "https://api.met.no/doc/License"),
            AboutDisclosureLink("Open-Meteo geocoding documentation", "https://open-meteo.com/en/docs/geocoding-api"),
            AboutDisclosureLink("GeoNames licensing and attribution", "https://www.geonames.org/about.html"),
            AboutDisclosureLink("NOAA/National Weather Service information", "https://www.weather.gov/"),
        ),
    ),
    AboutSection(
        heading = "Implemented Provider Paths",
        body = listOf(
            "MET Norway fallback is wired through the provider-neutral installed Home forecast path.",
            "Open-Meteo success, offline/network failure, and provider-rejected requests do not call MET Norway.",
            "Core forecast-cache persistence and foreground failed-refresh stale retention are implemented at the repository and app-state boundary.",
            "Saved-location storage, list display, current-location marking, saved-location selection, search-result save, and confirmed saved-location removal are implemented through the installed location-entry surface.",
            "Installed-app durable Room cache wiring and offline cache launch behavior are implemented for the selected forecast path.",
            "Provider-specific MET Norway cache headers are persisted with cached fallback forecasts and cached fallback provenance remains provider-neutral.",
            "The Home Now page shows one official-alert summary, detail navigation, total count, NOAA/NWS attribution, source-check time, and an external source link.",
            "Conditional GET requests, 304 not-modified handling, and release-candidate fallback verification are not implemented yet.",
        ),
    ),
    AboutSection(
        heading = "Roadmap Only",
        body = listOf(
            "Environment and Climate Change Canada alerts and Open-Meteo/CAMS air quality are roadmap-only here.",
            "Alert persistence/cache, background polling, notifications, custom unit editing, air quality, radar, and release-candidate fallback behavior are not implemented.",
        ),
    ),
)

private val privacySections = listOf(
    AboutSection(
        heading = "Privacy Baseline",
        body = listOf(
            "Oxygen contains no advertising SDK, behavioral tracking, mandatory analytics, marketing attribution, or mandatory account system.",
            "Location permission is optional. Manual search works without Android location permission.",
            "Use my location requests coarse foreground permission only after your tap and obtains one approximate position. There is no background acquisition or automatic relocation on launch, restart, or refresh.",
            "The selected approximate position is stored locally and restored for offline weather. It is not automatically added to saved places.",
            "The selected Off/Subtle effects preference is stored locally. Android disabled-animation policy can temporarily make the effective presentation Off without changing that saved choice.",
        ),
    ),
    AboutSection(
        heading = "Active Requests",
        body = listOf(
            "Open-Meteo forecast requests send the selected coordinates, timezone, requested weather variables, and normal network metadata such as IP address.",
            "Device coordinates first go to Open-Meteo with timezone=auto to resolve the IANA timezone, then follow the normal forecast request path. The phone timezone is never substituted.",
            "MET Norway fallback requests send selected coordinates, optional altitude, an identifying User-Agent/contact header, and normal network metadata.",
            "Open-Meteo geocoding requests send the typed place query, bounded result count, optional locale/filter parameters where implemented, and normal network metadata such as IP address.",
            "Open-Meteo geocoding data is based on GeoNames.",
            "Foreground selected-point NWS alert requests send the selected coordinates, required Oxygen User-Agent/contact header, and normal network metadata such as IP address.",
            "NWS alert results are kept in process memory for the foreground rate-limit window; Oxygen does not persist or background-poll alert data.",
        ),
    ),
    AboutSection(
        heading = "MET Norway Fallback",
        body = listOf(
            "MET Norway is used only after eligible Open-Meteo terminal forecast failures.",
            "MET Norway provider logs and privacy handling are governed by its service terms and privacy statement.",
        ),
    ),
)

private val openSourceLicenseSections = listOf(
    AboutSection(
        heading = "Oxygen Source Code",
        body = listOf(
            "Oxygen source code is licensed under GPL-3.0-or-later; see the repository LICENSE file.",
            "This product includes software developed for Oxygen Weather. Android, Jetpack, Kotlin, and Gradle dependencies remain subject to their upstream licenses.",
        ),
    ),
    AboutSection(
        heading = "Weather Data",
        body = listOf(
            "Weather-data attribution and licensing are separate from Oxygen source-code licensing.",
            "Open-Meteo forecast and geocoding disclosures are tracked in DATA_SOURCES.md and provider contracts. GeoNames attribution applies to the geocoding data source.",
            "NOAA/National Weather Service official-alert attribution is shown with the Home summary and its source link; NWS does not endorse Oxygen.",
            "Provider or government data attribution does not imply endorsement of Oxygen.",
        ),
    ),
)

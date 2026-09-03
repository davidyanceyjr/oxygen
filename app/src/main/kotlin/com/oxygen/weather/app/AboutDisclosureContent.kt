package com.oxygen.weather.app

enum class AboutSurfaceId(
    val title: String,
) {
    DataSources("Data Sources"),
    Privacy("Privacy"),
    OpenSourceLicenses("Open Source Licenses"),
}

data class AboutSurfaceState(
    val title: String = "Settings / About",
    val sections: List<AboutSection> = aboutOverviewSections,
)

data class AboutSection(
    val heading: String,
    val body: List<String>,
)

val aboutSurfaceOptions: List<AboutSurfaceId> = AboutSurfaceId.entries

fun aboutSurfaceState(surfaceId: AboutSurfaceId?): AboutSurfaceState =
    when (surfaceId) {
        null -> AboutSurfaceState()
        AboutSurfaceId.DataSources -> AboutSurfaceState(
            title = AboutSurfaceId.DataSources.title,
            sections = dataSourceSections,
        )
        AboutSurfaceId.Privacy -> AboutSurfaceState(
            title = AboutSurfaceId.Privacy.title,
            sections = privacySections,
        )
        AboutSurfaceId.OpenSourceLicenses -> AboutSurfaceState(
            title = AboutSurfaceId.OpenSourceLicenses.title,
            sections = openSourceLicenseSections,
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
            "Location search: Open-Meteo Geocoding API, based on GeoNames data, powers manual place search.",
        ),
    ),
    AboutSection(
        heading = "Implemented Provider Paths",
        body = listOf(
            "MET Norway forecast is implemented as a provider path and covered as a core fallback-selection capability.",
            "MET Norway is not wired as the active installed-app forecast fallback in this build.",
            "Core forecast-cache persistence and foreground failed-refresh stale retention are implemented at the repository and app-state boundary.",
            "Saved-location storage, list display, current-location marking, and saved-location selection are implemented for existing saved rows.",
            "Installed-app durable cache wiring, fallback wiring, fallback Home UI verification, and offline cache launch behavior are not implemented or verified yet.",
        ),
    ),
    AboutSection(
        heading = "Roadmap Only",
        body = listOf(
            "NOAA/NWS alerts, Environment and Climate Change Canada alerts, and Open-Meteo/CAMS air quality are roadmap-only here.",
            "Alerts, air quality, radar, saved-location save/remove UI, unit settings, and installed-app forecast fallback behavior are not implemented in this app build.",
        ),
    ),
)

private val privacySections = listOf(
    AboutSection(
        heading = "Privacy Baseline",
        body = listOf(
            "Oxygen contains no advertising SDK, behavioral tracking, mandatory analytics, marketing attribution, or mandatory account system.",
            "Location permission is optional. Manual search works without Android location permission.",
        ),
    ),
    AboutSection(
        heading = "Active Requests",
        body = listOf(
            "Open-Meteo forecast requests send the selected coordinates, timezone, requested weather variables, and normal network metadata such as IP address.",
            "Open-Meteo geocoding requests send the typed place query, bounded result count, optional locale/filter parameters where implemented, and normal network metadata such as IP address.",
            "Open-Meteo geocoding data is based on GeoNames.",
        ),
    ),
    AboutSection(
        heading = "Implemented MET Norway Capability",
        body = listOf(
            "The implemented MET Norway forecast path would send selected coordinates, optional altitude when present, an identifying User-Agent/contact header, and normal network metadata such as IP address.",
            "MET Norway provider logs and privacy handling are governed by its service terms and privacy statement. This build does not use MET Norway as active installed-app forecast fallback.",
        ),
    ),
)

private val openSourceLicenseSections = listOf(
    AboutSection(
        heading = "Oxygen Source Code",
        body = listOf(
            "Oxygen source code is licensed under the repository LICENSE file.",
            "This product includes software developed for Oxygen Weather. Android, Jetpack, Kotlin, and Gradle dependencies remain subject to their upstream licenses.",
        ),
    ),
    AboutSection(
        heading = "Weather Data",
        body = listOf(
            "Weather-data attribution and licensing are separate from Oxygen source-code licensing.",
            "Open-Meteo forecast and geocoding disclosures are tracked in DATA_SOURCES.md and provider contracts. GeoNames attribution applies to the geocoding data source.",
            "Provider or government data attribution does not imply endorsement of Oxygen.",
        ),
    ),
)

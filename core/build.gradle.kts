plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.oxygen.weather.core"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    testImplementation(libs.junit)
}

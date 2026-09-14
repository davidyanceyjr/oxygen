package com.oxygen.weather

import android.content.Intent
import android.content.pm.PackageManager
import android.security.NetworkSecurityPolicy
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrivacyManifestInstrumentedTest {
    private val packageManager: PackageManager
        get() = InstrumentationRegistry.getInstrumentation().targetContext.packageManager

    private val packageName: String
        get() = InstrumentationRegistry.getInstrumentation().targetContext.packageName

    @Test
    fun productionManifestRetainsOnlyOptionalLocationNetworkPermissions() {
        val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)

        assertTrue(
            packageInfo.requestedPermissions.orEmpty().toSet().containsAll(
                setOf(
                    "android.permission.INTERNET",
                    "android.permission.ACCESS_NETWORK_STATE",
                    "android.permission.ACCESS_COARSE_LOCATION",
                ),
            ),
        )
    }

    @Test
    fun productionApplicationDisablesCleartextTraffic() {
        assertFalse(NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted())
    }

    @Test
    fun onlyLauncherActivityIsExported() {
        val activities = packageManager.getPackageInfo(
            packageName,
            PackageManager.GET_ACTIVITIES,
        ).activities.orEmpty()

        val launchers = packageManager.queryIntentActivities(
            Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setPackage(packageName),
            PackageManager.MATCH_ALL,
        )

        assertEquals(1, launchers.size)
        val launcher = launchers.single().activityInfo
        assertNotNull("MainActivity launcher declaration is missing", launcher)
        assertEquals("com.oxygen.weather.MainActivity", launcher.name)
        assertTrue(launcher.exported)
        assertTrue(
            activities
                .filter { it.name != launcher.name }
                .none { it.exported && it.name != "androidx.activity.ComponentActivity" },
        )
    }

    @Test
    fun noServiceReceiverOrProviderIsExported() {
        val packageInfo = packageManager.getPackageInfo(
            packageName,
            PackageManager.GET_SERVICES or
                PackageManager.GET_RECEIVERS or
                PackageManager.GET_PROVIDERS,
        )

        assertTrue(packageInfo.services.orEmpty().all { !it.exported })
        assertTrue(
            packageInfo.receivers.orEmpty().all {
                !it.exported || it.permission == "android.permission.DUMP"
            },
        )
        assertTrue(packageInfo.providers.orEmpty().all { !it.exported })
    }
}

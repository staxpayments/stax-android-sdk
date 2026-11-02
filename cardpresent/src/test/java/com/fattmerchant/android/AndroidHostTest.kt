package com.fattmerchant.android

import android.app.Application
import android.content.ContextWrapper
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.DeadSystemException
import com.fattmerchant.omni.networking.Device
import com.fattmerchant.omni.networking.HostApp
import com.fattmerchant.omni.networking.UserAgentValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadows.ShadowApplicationPackageManager
import org.robolectric.shadows.ShadowBuild

@Implements(className = "android.app.ApplicationPackageManager", isInAndroidSdk = false)
class ThrowingInstallerPackageManager : ShadowApplicationPackageManager() {
    @Implementation
    override fun getInstallerPackageName(packageName: String): String = throw IllegalArgumentException(packageName)

    @Implementation
    override fun getInstallSourceInfo(packageName: String): Any = throw IllegalStateException(packageName)
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AndroidHostTest {
    private lateinit var context: Application

    @Before fun setUp() {
        context = RuntimeEnvironment.getApplication()
        val packageInfo = shadowOf(context.packageManager).getInternalMutablePackageInfo(context.packageName)
        packageInfo.versionName = "4.2.0"
        packageInfo.longVersionCode = 412
        packageInfo.applicationInfo!!.apply {
            nonLocalizedLabel = "Example Field"
            flags = flags and ApplicationInfo.FLAG_DEBUGGABLE.inv()
            targetSdkVersion = 34
        }
        installFrom("com.android.vending")
    }

    private fun installFrom(installer: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            shadowOf(context.packageManager).setInstallSourceInfo(context.packageName, installer, installer)
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.setInstallerPackageName(context.packageName, installer)
        }
    }

    @Config(sdk = [29, 30, 35])
    @Test fun readsTheHostApp() {
        assertEquals(
            HostApp(
                id = UserAgentValue.of(context.packageName),
                version = UserAgentValue.of("4.2.0"),
                build = 412,
                name = UserAgentValue.of("Example Field"),
                installer = UserAgentValue.of("com.android.vending"),
                debuggable = false,
                targetSdk = 34
            ),
            context.hostApp()
        )
    }

    @Test fun readsTheDebuggableFlag() {
        shadowOf(context.packageManager).getInternalMutablePackageInfo(context.packageName)
            .applicationInfo!!.apply { flags = flags or ApplicationInfo.FLAG_DEBUGGABLE }

        assertEquals(true, context.hostApp()?.debuggable)
    }

    @Config(sdk = [29, 30, 35])
    @Test fun missingInstallerIsNull() {
        installFrom(null)

        assertNull(context.hostApp()!!.installer)
    }

    @Config(sdk = [29, 30, 35])
    @Test fun installSourceLookupFailureKeepsTheHostApp() {
        val name = "com.example.no.install.source"
        shadowOf(context.packageManager).installPackage(
            PackageInfo().apply {
                packageName = name
                versionName = "1.0"
                applicationInfo = ApplicationInfo().apply { packageName = name }
            }
        )
        val noInstallSource = object : ContextWrapper(context) {
            override fun getPackageName() = name
        }

        val app = noInstallSource.hostApp()!!

        assertEquals(UserAgentValue.of(name), app.id)
        assertNull(app.installer)
    }

    @Config(sdk = [29, 30, 35], shadows = [ThrowingInstallerPackageManager::class])
    @Test fun installerLookupThatThrowsKeepsTheHostApp() {
        val app = context.hostApp()

        assertEquals(UserAgentValue.of(context.packageName), app?.id)
        assertNull(app?.installer)
    }

    @Test fun packageManagerFailureIsNull() {
        val deadSystem = object : ContextWrapper(context) {
            override fun getPackageManager(): PackageManager = throw RuntimeException(DeadSystemException())
        }

        assertNull(deadSystem.hostApp())
    }

    @Config(qualifiers = "fr")
    @Test fun nameIgnoresTheDeviceLocale() {
        val appInfo = shadowOf(context.packageManager).getInternalMutablePackageInfo(context.packageName).applicationInfo!!
        appInfo.nonLocalizedLabel = null
        appInfo.labelRes = android.R.string.cancel

        assertEquals("Annuler", context.getString(android.R.string.cancel))
        assertEquals(UserAgentValue.of("Cancel"), context.hostApp()?.name)
    }

    @Test fun laterInitsReuseTheFirstRead() {
        val first = ProcessHostApp.of(context)
        val readsAgain = object : ContextWrapper(context) {
            override fun getPackageManager(): PackageManager = throw AssertionError("read the host app twice")
        }

        assertNotNull(first)
        assertEquals(first, ProcessHostApp.of(readsAgain))
    }

    @Test fun packageNotFoundIsNull() {
        val missing = object : ContextWrapper(context) {
            override fun getPackageName() = "com.example.not.installed"
        }

        assertNull(missing.hostApp())
    }

    @Test fun readsTheDevice() {
        ShadowBuild.setVersionRelease("16")
        ShadowBuild.setManufacturer("samsung")
        ShadowBuild.setModel("SM-X218U")

        assertEquals(
            Device(
                osRelease = UserAgentValue.of("16"),
                apiLevel = Build.VERSION.SDK_INT,
                manufacturer = UserAgentValue.of("samsung"),
                model = UserAgentValue.of("SM-X218U")
            ),
            currentDevice()
        )
    }
}

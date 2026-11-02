package com.fattmerchant.android

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import com.fattmerchant.BuildConfig
import com.fattmerchant.omni.networking.Device
import com.fattmerchant.omni.networking.HostApp
import com.fattmerchant.omni.networking.SdkUserAgent
import com.fattmerchant.omni.networking.UserAgentValue
import java.util.Locale

internal fun sdkUserAgent(context: Context?): String =
    SdkUserAgent(UserAgentValue.of(BuildConfig.SDK_VERSION), context?.let(ProcessHostApp::of), currentDevice()).headerValue()

// The host app cannot change inside one process, so only the first init pays for the PackageManager calls.
internal object ProcessHostApp {
    private var read = false
    private var value: HostApp? = null

    @Synchronized fun of(context: Context): HostApp? {
        if (!read) {
            value = context.hostApp()
            read = true
        }
        return value
    }
}

// The user agent is diagnostic. No PackageManager failure may make Omni.initialize fail.
internal fun Context.hostApp(): HostApp? =
    try {
        @Suppress("DEPRECATION")
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val appInfo = packageInfo.applicationInfo ?: applicationInfo
        HostApp(
            id = UserAgentValue.of(packageName),
            version = UserAgentValue.of(packageInfo.versionName),
            build = packageInfo.longVersionCode,
            name = UserAgentValue.of(labelOf(appInfo)),
            installer = packageManager.installerOf(packageName)?.let(UserAgentValue::of),
            debuggable = (appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0,
            targetSdk = appInfo.targetSdkVersion
        )
    } catch (e: Exception) {
        null
    }

internal fun currentDevice() = Device(
    osRelease = UserAgentValue.of(Build.VERSION.RELEASE),
    apiLevel = Build.VERSION.SDK_INT,
    manufacturer = UserAgentValue.of(Build.MANUFACTURER),
    model = UserAgentValue.of(Build.MODEL)
)

// loadLabel follows the device locale. A fixed locale gives one name for each app on every device.
private fun Context.labelOf(appInfo: ApplicationInfo): String {
    appInfo.nonLocalizedLabel?.let { return it.toString() }
    if (appInfo.labelRes != 0) {
        val rootLocale = Configuration(resources.configuration).apply { setLocale(Locale.ROOT) }
        return createConfigurationContext(rootLocale).getString(appInfo.labelRes)
    }
    return appInfo.loadLabel(packageManager).toString()
}

private fun PackageManager.installerOf(packageName: String): String? =
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getInstallSourceInfo(packageName).installingPackageName
        } else {
            @Suppress("DEPRECATION")
            getInstallerPackageName(packageName)
        }
    } catch (e: Exception) {
        null
    }

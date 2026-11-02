package com.fattmerchant.omni.networking

import okhttp3.Headers
import org.junit.Assert.assertEquals
import org.junit.Test

class SdkUserAgentTest {
    private val app = HostApp(
        id = UserAgentValue.of("com.example.field"),
        version = UserAgentValue.of("4.2.0"),
        build = 412,
        name = UserAgentValue.of("Example Field"),
        installer = UserAgentValue.of("com.android.vending"),
        debuggable = false,
        targetSdk = 36
    )

    private val device = Device(
        osRelease = UserAgentValue.of("16"),
        apiLevel = 36,
        manufacturer = UserAgentValue.of("samsung"),
        model = UserAgentValue.of("SM-X218U")
    )

    private val version = UserAgentValue.of("2.7.5")

    @Test fun appKnownSendsEveryField() = assertEquals(
        "stax-sdk-android/2.7.5 (app=com.example.field; version=4.2.0; build=412; name=Example Field; " +
            "installer=com.android.vending; debug=0; target=36; os=Android 16/36; device=samsung SM-X218U)",
        SdkUserAgent(version, app, device).headerValue()
    )

    @Test fun appUnknownSendsOnlyTheDevice() = assertEquals(
        "stax-sdk-android/2.7.5 (os=Android 16/36; device=samsung SM-X218U)",
        SdkUserAgent(version, app = null, device = device).headerValue()
    )

    @Test fun debuggableAppSendsDebugOne() = assertEquals(
        "stax-sdk-android/2.7.5 (app=com.example.field; version=4.2.0; build=412; name=Example Field; " +
            "installer=com.android.vending; debug=1; target=36; os=Android 16/36; device=samsung SM-X218U)",
        SdkUserAgent(version, app.copy(debuggable = true), device).headerValue()
    )

    @Test fun missingInstallerSendsNone() = assertEquals(
        "stax-sdk-android/2.7.5 (app=com.example.field; version=4.2.0; build=412; name=Example Field; " +
            "installer=none; debug=0; target=36; os=Android 16/36; device=samsung SM-X218U)",
        SdkUserAgent(version, app.copy(installer = null), device).headerValue()
    )

    @Test fun okHttpAcceptsTheHeaderForHostileInput() {
        val hostileApp = app.copy(
            version = UserAgentValue.of("1.0;(x)=y"),
            name = UserAgentValue.of("Café POS\n\u007f")
        )
        val hostileDevice = device.copy(model = UserAgentValue.of("Pixel 9 🚀"))
        Headers.Builder().add("User-Agent", SdkUserAgent(version, hostileApp, hostileDevice).headerValue())
    }

    @Test fun sdkVersionIsCleaned() = assertEquals(
        "stax-sdk-android/2.7.5_ (os=Android 16/36; device=samsung SM-X218U)",
        SdkUserAgent(UserAgentValue.of("2.7.5)\u00e9"), app = null, device = device).headerValue()
    )
}

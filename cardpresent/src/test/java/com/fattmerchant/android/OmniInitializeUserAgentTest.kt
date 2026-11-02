package com.fattmerchant.android

import android.os.Looper
import com.fattmerchant.BuildConfig
import com.fattmerchant.omni.Environment
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class OmniInitializeUserAgentTest {
    @Test fun firstRequestOfInitSendsTheHostApp() {
        val context = RuntimeEnvironment.getApplication()
        val engine = MockEngine {
            respond("{}", headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }

        Omni.initialize(
            mapOf("apiKey" to "key", "appContext" to context),
            Environment.DEV,
            completion = {},
            error = {},
            usbListener = null,
            engine = engine
        )
        val deadline = System.currentTimeMillis() + 5_000
        while (engine.requestHistory.isEmpty() && System.currentTimeMillis() < deadline) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(10)
        }

        val userAgent = engine.requestHistory.firstOrNull()?.headers?.get(HttpHeaders.UserAgent).orEmpty()
        val expectedStart = "stax-sdk-android/${BuildConfig.SDK_VERSION} (app=${context.packageName}; version="
        assertTrue(userAgent, userAgent.startsWith(expectedStart))
    }
}

package com.fattmerchant.omni.networking

import com.fattmerchant.BuildConfig
import com.fattmerchant.android.currentDevice
import com.fattmerchant.omni.data.models.Transaction
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class OmniApiUserAgentTest {
    private val engine = MockEngine {
        respond("{}", headers = headersOf(HttpHeaders.ContentType, "application/json"))
    }
    private val api = OmniApi(engine)

    private val appKnown = "stax-sdk-android/2.7.5 (app=com.example.field; version=4.2.0; build=412; name=Example Field; " +
        "installer=com.android.vending; debug=0; target=36; os=Android 16/36; device=samsung SM-X218U)"

    private fun sentUserAgents(): List<String?> = runBlocking {
        api.getSelf { fail(it.message) }
        api.createTransaction(Transaction()) { fail(it.message) }
        engine.requestHistory.takeLast(2).map { it.headers[HttpHeaders.UserAgent] }
    }

    @Test fun beforeInitSendsTheDeviceOnly() {
        val deviceOnly = SdkUserAgent(UserAgentValue.of(BuildConfig.SDK_VERSION), app = null, device = currentDevice()).headerValue()

        assertEquals(listOf(deviceOnly, deviceOnly), sentUserAgents())
    }

    @Test fun afterInitSendsTheValueThatInitSet() {
        sentUserAgents()
        api.userAgent = appKnown

        assertEquals(listOf(appKnown, appKnown), sentUserAgents())
    }
}

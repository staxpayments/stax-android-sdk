package com.fattmerchant.omni

import com.fattmerchant.omni.data.MobileReader
import com.fattmerchant.omni.data.MobileReaderDriver
import com.fattmerchant.omni.data.models.MobileReaderDetails
import com.fattmerchant.omni.data.models.Transaction
import com.fattmerchant.omni.data.repository.MobileReaderDriverRepository
import com.fattmerchant.omni.networking.OmniApi
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.reflect.Proxy

class OmniInitializeTest {

    private val json = headersOf(HttpHeaders.ContentType, "application/json")

    private class CountingRepository(private val driver: MobileReaderDriver) : MobileReaderDriverRepository {
        override suspend fun getDrivers() = listOf(driver)
        override suspend fun getInitializedDrivers() = listOf(driver)
        override suspend fun getDriverFor(transaction: Transaction): MobileReaderDriver? = driver
        override suspend fun getDriverFor(mobileReader: MobileReader?): MobileReaderDriver? = driver
    }

    @Test fun driversInitializeOnceWhenReaderSettingsReturnData() = runBlocking {
        val engine = MockEngine { request ->
            when {
                request.url.encodedPath.endsWith("self") ->
                    respond("""{"merchant":{"options":{"emv_password":"self-key"}}}""", headers = json)
                else ->
                    respond("""{"nmi":{"security_key":"settings-key"}}""", headers = json)
            }
        }

        var initializeCalls = 0
        var nmiKeySeen: String? = null
        val driver = Proxy.newProxyInstance(
            MobileReaderDriver::class.java.classLoader,
            arrayOf(MobileReaderDriver::class.java)
        ) { _, method, args ->
            when (method.name) {
                "initialize" -> {
                    initializeCalls++
                    @Suppress("UNCHECKED_CAST")
                    val driverArgs = args[0] as Map<String, Any>
                    nmiKeySeen = (driverArgs["nmi"] as MobileReaderDetails.NMIDetails).securityKey
                    true
                }
                "getSource" -> "test"
                else -> null
            }
        } as MobileReaderDriver

        val omni = Omni(OmniApi(engine))
        omni.mobileReaderDriverRepository = CountingRepository(driver)
        omni.coroutineScope = CoroutineScope(Dispatchers.Unconfined)

        val done = CompletableDeferred<Unit>()
        omni.initialize(emptyMap(), { done.complete(Unit) }, { done.completeExceptionally(it) })
        withTimeout(5_000) { done.await() }

        assertEquals(1, initializeCalls)
        assertEquals("settings-key", nmiKeySeen)
    }
}

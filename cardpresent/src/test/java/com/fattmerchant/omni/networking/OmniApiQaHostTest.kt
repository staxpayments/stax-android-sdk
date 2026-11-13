package com.fattmerchant.omni.networking

import com.fattmerchant.omni.Environment
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.fail
import org.junit.Test

class OmniApiQaHostTest {

    @Test fun qaBuildHashWithADotIsRejected() {
        assertThrows(IllegalArgumentException::class.java) { Environment.QA("abc.evil.example") }
    }

    @Test fun qaBuildHashWithASlashIsRejected() {
        assertThrows(IllegalArgumentException::class.java) { Environment.QA("abc/evil") }
    }

    @Test fun emptyQaBuildHashIsRejected() {
        assertThrows(IllegalArgumentException::class.java) { Environment.QA("") }
    }

    @Test fun validQaBuildHashTargetsTheQaBuildsHost() = runBlocking {
        val engine = MockEngine {
            respond("{}", headers = headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val api = OmniApi(engine)
        api.environment = Environment.QA("abc-123")

        api.getSelf { fail(it.message) }

        assertEquals("api-qa-abc-123.qabuilds.fattpay.com", engine.requestHistory.last().url.host)
    }
}

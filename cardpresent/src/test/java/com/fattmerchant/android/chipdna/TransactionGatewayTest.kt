package com.fattmerchant.android.chipdna

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpMethod
import io.ktor.http.content.OutgoingContent
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionGatewayTest {

    @Test fun securityKeyIsSentInThePostBodyNotTheUrl() = runBlocking {
        val engine = MockEngine { respond("<nm_response></nm_response>") }

        runCatching { TransactionGateway.getTransactionCcExpiration("test-key-123", "txn-1", engine) }

        val request = engine.requestHistory.single()
        assertEquals(HttpMethod.Post, request.method)
        assertEquals("/api/query.php", request.url.encodedPath)
        assertFalse(request.url.toString().contains("test-key-123"))
        assertTrue(request.url.parameters.isEmpty())
        val body = (request.body as OutgoingContent.ByteArrayContent).bytes().decodeToString()
        assertTrue(body.contains("security_key=test-key-123"))
        assertTrue(body.contains("transaction_id=txn-1"))
    }
}

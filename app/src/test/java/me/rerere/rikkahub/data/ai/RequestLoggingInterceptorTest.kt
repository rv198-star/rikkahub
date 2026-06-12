package me.rerere.rikkahub.data.ai

import me.rerere.common.android.Logging
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test

class RequestLoggingInterceptorTest {
    @Test
    fun `request logs redact sensitive headers`() {
        Logging.clear()
        val client = OkHttpClient.Builder()
            .addInterceptor(RequestLoggingInterceptor())
            .addInterceptor { chain ->
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .header("Authorization", "Bearer response-secret")
                    .header("Set-Cookie", "session=secret")
                    .body("{}".toResponseBody("application/json".toMediaType()))
                    .build()
            }
            .build()
        val request = Request.Builder()
            .url("https://brainypal.test/api/v1/child/practice-tasks")
            .header("Authorization", "Bearer brainypal-local")
            .header("X-Api-Key", "child-secret")
            .header("X-Request-Id", "visible-request-id")
            .build()

        client.newCall(request).execute().close()

        val log = Logging.getRequestLogs().single()
        assertEquals("<redacted>", log.requestHeaders["Authorization"])
        assertEquals("<redacted>", log.requestHeaders["X-Api-Key"])
        assertEquals("visible-request-id", log.requestHeaders["X-Request-Id"])
        assertEquals("<redacted>", log.responseHeaders["Authorization"])
        assertEquals("<redacted>", log.responseHeaders["Set-Cookie"])
    }
}

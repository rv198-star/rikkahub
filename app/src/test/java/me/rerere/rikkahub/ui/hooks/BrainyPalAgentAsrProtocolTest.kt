package me.rerere.rikkahub.ui.hooks

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrainyPalAgentAsrProtocolTest {
    @Test
    fun `start event matches Aliyun NLS SpeechTranscriber command shape`() {
        val config = BrainyPalAgentAsrSessionConfig(
            websocketUrl = "wss://nls-gateway.aliyuncs.com/ws/v1?token=redacted",
            appKey = "app-key",
            namespace = "SpeechTranscriber",
            startCommand = "StartTranscription",
            stopCommand = "StopTranscription",
            audioFormat = "pcm",
            sampleRate = 16000,
        )

        val event = Json.parseToJsonElement(
            BrainyPalAgentAsrProtocol.startTranscriptionEvent(
                config = config,
                taskId = "task-1",
                messageId = "message-1",
            )
        ).jsonObject

        val header = event.getValue("header").jsonObject
        val payload = event.getValue("payload").jsonObject
        assertEquals("app-key", header.getValue("appkey").jsonPrimitive.content)
        assertEquals("SpeechTranscriber", header.getValue("namespace").jsonPrimitive.content)
        assertEquals("StartTranscription", header.getValue("name").jsonPrimitive.content)
        assertEquals("pcm", payload.getValue("format").jsonPrimitive.content)
        assertEquals("16000", payload.getValue("sample_rate").jsonPrimitive.content)
    }

    @Test
    fun `server result events produce partial and final transcript updates`() {
        val partial = BrainyPalAgentAsrProtocol.parseServerEvent(
            """
            {
              "header": {"name": "TranscriptionResultChanged", "status": 20000000},
              "payload": {"index": 0, "result": "下一"}
            }
            """.trimIndent()
        )
        val final = BrainyPalAgentAsrProtocol.parseServerEvent(
            """
            {
              "header": {"name": "SentenceEnd", "status": 20000000},
              "payload": {"index": 0, "result": "下一题"}
            }
            """.trimIndent()
        )

        assertEquals(BrainyPalAgentAsrServerEvent.Transcript("0", "下一", isFinal = false), partial)
        assertEquals(BrainyPalAgentAsrServerEvent.Transcript("0", "下一题", isFinal = true), final)
    }

    @Test
    fun `transcript buffer only notifies command callback for final sentence`() {
        val buffer = BrainyPalAgentAsrTranscriptBuffer()

        val partial = buffer.accept(
            BrainyPalAgentAsrServerEvent.Transcript("0", "下一", isFinal = false)
        )
        val final = buffer.accept(
            BrainyPalAgentAsrServerEvent.Transcript("0", "下一题", isFinal = true)
        )

        assertEquals("下一", partial.transcript)
        assertFalse(partial.shouldNotifyCommand)
        assertEquals("下一题", final.transcript)
        assertTrue(final.shouldNotifyCommand)
    }

    @Test
    fun `transcript buffer command callback uses latest final sentence only`() {
        val buffer = BrainyPalAgentAsrTranscriptBuffer()

        val next = buffer.accept(
            BrainyPalAgentAsrServerEvent.Transcript("0", "下一个", isFinal = true)
        )
        val repeat = buffer.accept(
            BrainyPalAgentAsrServerEvent.Transcript("1", "再听一次", isFinal = true)
        )

        assertEquals("下一个", next.transcript)
        assertEquals("下一个", next.commandTranscript)
        assertTrue(next.shouldNotifyCommand)
        assertEquals("下一个 再听一次", repeat.transcript)
        assertEquals("再听一次", repeat.commandTranscript)
        assertTrue(repeat.shouldNotifyCommand)
    }

    @Test
    fun `failed server event exposes user readable error`() {
        val event = BrainyPalAgentAsrProtocol.parseServerEvent(
            """
            {
              "header": {"name": "TaskFailed", "status": 40000000, "status_message": "bad token"},
              "payload": {}
            }
            """.trimIndent()
        )

        assertEquals(BrainyPalAgentAsrServerEvent.Error("bad token"), event)
    }

    @Test
    fun `completed event is recognized`() {
        assertTrue(
            BrainyPalAgentAsrProtocol.parseServerEvent(
                """{"header":{"name":"TranscriptionCompleted","status":20000000},"payload":{}}"""
            ) is BrainyPalAgentAsrServerEvent.Completed
        )
    }
}

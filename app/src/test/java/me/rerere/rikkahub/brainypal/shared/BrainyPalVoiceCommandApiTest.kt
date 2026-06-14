package me.rerere.rikkahub.brainypal.shared

import kotlinx.serialization.encodeToString
import me.rerere.rikkahub.utils.JsonInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrainyPalVoiceCommandApiTest {
    @Test
    fun `voice command request encodes agent transcript context and locale`() {
        val request = BrainyPalInterpretVoiceCommandRequest(
            transcript = "再听一遍吧",
            context = "dictation",
            locale = "zh-CN",
        )

        val json = JsonInstant.encodeToString(request)

        assertTrue(json.contains("\"transcript\":\"再听一遍吧\""))
        assertTrue(json.contains("\"context\":\"dictation\""))
        assertTrue(json.contains("\"locale\":\"zh-CN\""))
    }

    @Test
    fun `voice command response decodes safe agent action and tts copy`() {
        val body = """
            {
              "intent": "repeat",
              "confidence": "high",
              "child_label": "再听一次",
              "requires_clarification": false,
              "raw_transcript_persistent": false,
              "tts": {
                "text": "好的，我再说一遍。",
                "context": "dictation",
                "locale": "zh-CN"
              },
              "provider": {
                "provider": "fake_text",
                "latency_ms": 12,
                "failure_reason": null
              }
            }
        """.trimIndent()

        val response = JsonInstant.decodeFromString<BrainyPalVoiceCommandResponse>(body)

        assertEquals("repeat", response.intent)
        assertEquals(BrainyPalDictationCommand.REPEAT, response.toDictationCommand())
        assertEquals("再听一次", response.childLabel)
        assertFalse(response.requiresClarification)
        assertFalse(response.rawTranscriptPersistent)
        assertEquals("好的，我再说一遍。", response.tts.text)
        assertEquals("fake_text", response.provider.provider)
        assertEquals(12, response.provider.latencyMs)
        assertEquals(null, response.provider.failureReason)
    }

    @Test
    fun `unknown voice command asks clarification and does not mutate dictation session`() {
        val body = """
            {
              "intent": "unknown",
              "confidence": "low",
              "child_label": "没听清你的操作，可以再说一次，或者直接点按钮。",
              "requires_clarification": true,
              "raw_transcript_persistent": false,
              "tts": {
                "text": "没听清你的操作，可以再说一次。",
                "context": "dictation",
                "locale": "zh-CN"
              },
              "provider": {
                "provider": "aliyun_nls",
                "latency_ms": 3200,
                "failure_reason": "asr_timeout"
              }
            }
        """.trimIndent()

        val response = JsonInstant.decodeFromString<BrainyPalVoiceCommandResponse>(body)

        assertEquals(BrainyPalDictationCommand.UNKNOWN, response.toDictationCommand())
        assertTrue(response.requiresClarification)
        assertFalse(response.rawTranscriptPersistent)
        assertEquals("asr_timeout", response.provider.failureReason)
    }
}

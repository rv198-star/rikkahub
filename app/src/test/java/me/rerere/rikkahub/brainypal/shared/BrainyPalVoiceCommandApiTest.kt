package me.rerere.rikkahub.brainypal.shared

import kotlinx.serialization.encodeToString
import kotlinx.coroutines.runBlocking
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
    fun `ask help voice command maps to generic action without mutating dictation`() {
        val body = """
            {
              "intent": "ask_help",
              "confidence": "medium",
              "child_label": "我给你一个方向提示。",
              "requires_clarification": false,
              "raw_transcript_persistent": false,
              "tts": {
                "text": "先看已知条件。",
                "context": "practice",
                "locale": "zh-CN"
              },
              "provider": {
                "provider": "fake_text",
                "latency_ms": 18,
                "failure_reason": null
              }
            }
        """.trimIndent()

        val response = JsonInstant.decodeFromString<BrainyPalVoiceCommandResponse>(body)

        assertEquals(BrainyPalVoiceAction.ASK_HELP, response.toVoiceAction())
        assertEquals(BrainyPalDictationCommand.UNKNOWN, response.toDictationCommand())
    }

    @Test
    fun `voice interpreter prefers agent intent over local fallback`() = runBlocking {
        val api = RecordingVoiceApi(
            response = BrainyPalVoiceCommandResponse(
                intent = "ask_help",
                confidence = "medium",
                childLabel = "我给你一个方向提示。",
            )
        )

        val state = BrainyPalVoiceCommandInterpreter.interpret(
            api = api,
            transcript = "给我一个提示",
            context = "practice",
            audioPermissionGranted = true,
            fallbackAction = BrainyPalVoiceAction.NEXT,
        )

        assertEquals("给我一个提示", api.lastRequest?.transcript)
        assertEquals("practice", api.lastRequest?.context)
        assertEquals(BrainyPalVoiceAction.ASK_HELP, state.action)
        assertEquals(BrainyPalDictationCommand.UNKNOWN, state.dictationCommand)
        assertFalse(state.showButtonFallback)
    }

    @Test
    fun `voice interpreter fallback does not execute local action when agent call fails`() = runBlocking {
        val api = ThrowingVoiceApi

        val state = BrainyPalVoiceCommandInterpreter.interpret(
            api = api,
            transcript = "下一题",
            context = "practice",
            audioPermissionGranted = true,
            fallbackAction = BrainyPalVoiceAction.NEXT,
        )

        assertEquals(BrainyPalVoiceControlPhase.FALLBACK, state.phase)
        assertEquals(BrainyPalVoiceAction.UNKNOWN, state.action)
        assertEquals(BrainyPalDictationCommand.UNKNOWN, state.dictationCommand)
        assertTrue(state.showButtonFallback)
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

        assertEquals(BrainyPalVoiceAction.UNKNOWN, response.toVoiceAction())
        assertEquals(BrainyPalDictationCommand.UNKNOWN, response.toDictationCommand())
        assertTrue(response.requiresClarification)
        assertFalse(response.rawTranscriptPersistent)
        assertEquals("asr_timeout", response.provider.failureReason)
    }

    private class RecordingVoiceApi(
        private val response: BrainyPalVoiceCommandResponse,
    ) : BrainyPalVoiceApi {
        var lastRequest: BrainyPalInterpretVoiceCommandRequest? = null

        override suspend fun interpretVoiceCommand(
            request: BrainyPalInterpretVoiceCommandRequest,
        ): BrainyPalVoiceCommandResponse {
            lastRequest = request
            return response
        }
    }

    private object ThrowingVoiceApi : BrainyPalVoiceApi {
        override suspend fun interpretVoiceCommand(
            request: BrainyPalInterpretVoiceCommandRequest,
        ): BrainyPalVoiceCommandResponse {
            error("voice api unavailable")
        }
    }
}

package me.rerere.rikkahub.brainypal.shared

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrainyPalVoiceControlShellTest {
    @Test
    fun `successful command becomes executable voice control state`() {
        val state = BrainyPalVoiceControlShell.fromCommandResponse(
            response = BrainyPalVoiceCommandResponse(
                intent = "next",
                confidence = "high",
                childLabel = "进入下一条",
            ),
            audioPermissionGranted = true,
        )

        assertEquals(BrainyPalVoiceControlPhase.EXECUTING, state.phase)
        assertEquals(BrainyPalVoiceAction.NEXT, state.action)
        assertEquals(BrainyPalDictationCommand.NEXT, state.dictationCommand)
        assertTrue(state.canUseVoice)
        assertFalse(state.showButtonFallback)
    }

    @Test
    fun `ask help command is executable without dictation mutation`() {
        val state = BrainyPalVoiceControlShell.fromCommandResponse(
            response = BrainyPalVoiceCommandResponse(
                intent = "ask_help",
                confidence = "medium",
                childLabel = "我给你一个方向提示。",
            ),
            audioPermissionGranted = true,
        )

        assertEquals(BrainyPalVoiceControlPhase.EXECUTING, state.phase)
        assertEquals(BrainyPalVoiceAction.ASK_HELP, state.action)
        assertEquals(BrainyPalDictationCommand.UNKNOWN, state.dictationCommand)
        assertFalse(state.showButtonFallback)
    }

    @Test
    fun `provider failure keeps button fallback available`() {
        val state = BrainyPalVoiceControlShell.fromCommandResponse(
            response = BrainyPalVoiceCommandResponse(
                intent = "unknown",
                confidence = "low",
                childLabel = "语音服务刚才不稳定，可以直接点按钮继续。",
                requiresClarification = true,
                provider = BrainyPalVoiceProviderPayload(
                    provider = "aliyun_nls",
                    latencyMs = 3200,
                    failureReason = "asr_timeout",
                ),
            ),
            audioPermissionGranted = true,
        )

        assertEquals(BrainyPalVoiceControlPhase.FAILED, state.phase)
        assertEquals(BrainyPalVoiceAction.UNKNOWN, state.action)
        assertEquals(BrainyPalDictationCommand.UNKNOWN, state.dictationCommand)
        assertTrue(state.showButtonFallback)
        assertEquals("语音服务刚才不稳定，可以直接点按钮继续。", state.childMessage)
    }

    @Test
    fun `clarification response does not execute recognized action`() {
        val state = BrainyPalVoiceControlShell.fromCommandResponse(
            response = BrainyPalVoiceCommandResponse(
                intent = "next",
                confidence = "low",
                childLabel = "我没听清，可以点按钮继续。",
                requiresClarification = true,
            ),
            audioPermissionGranted = true,
        )

        assertEquals(BrainyPalVoiceControlPhase.FALLBACK, state.phase)
        assertEquals(BrainyPalVoiceAction.UNKNOWN, state.action)
        assertEquals(BrainyPalDictationCommand.UNKNOWN, state.dictationCommand)
        assertTrue(state.showButtonFallback)
    }

    @Test
    fun `missing microphone permission explains fallback before listening`() {
        val state = BrainyPalVoiceControlShell.ready(audioPermissionGranted = false)

        assertEquals(BrainyPalVoiceControlPhase.FALLBACK, state.phase)
        assertFalse(state.canUseVoice)
        assertTrue(state.showButtonFallback)
    }
}

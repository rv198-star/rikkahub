package me.rerere.rikkahub.brainypal.child

import me.rerere.asr.ASRStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrainyPalDictationVoiceControlUiPolicyTest {
    @Test
    fun `button remains tappable to request microphone permission before ASR is ready`() {
        val model = BrainyPalDictationVoiceControlUiPolicy.model(
            asrStatus = ASRStatus.Idle,
            asrAvailable = false,
            audioPermissionGranted = false,
        )

        assertTrue(model.enabled)
        assertEquals("允许麦克风后使用语音", model.label)
    }

    @Test
    fun `button is not presented as a dead control when ASR provider is missing`() {
        val model = BrainyPalDictationVoiceControlUiPolicy.model(
            asrStatus = ASRStatus.Idle,
            asrAvailable = false,
            audioPermissionGranted = true,
        )

        assertFalse(model.enabled)
        assertEquals("语音识别未配置", model.label)
        assertEquals("当前没有可用 ASR，可以先用按钮继续。", model.helperText)
    }

    @Test
    fun `button can start and stop voice control when ASR is available`() {
        val idle = BrainyPalDictationVoiceControlUiPolicy.model(
            asrStatus = ASRStatus.Idle,
            asrAvailable = true,
            audioPermissionGranted = true,
        )
        val listening = BrainyPalDictationVoiceControlUiPolicy.model(
            asrStatus = ASRStatus.Listening,
            asrAvailable = true,
            audioPermissionGranted = true,
        )

        assertTrue(idle.enabled)
        assertEquals("开启语音控制", idle.label)
        assertTrue(listening.enabled)
        assertEquals("停止语音控制", listening.label)
    }
}

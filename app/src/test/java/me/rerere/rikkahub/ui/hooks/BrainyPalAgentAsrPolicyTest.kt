package me.rerere.rikkahub.ui.hooks

import me.rerere.asr.ASRProviderSetting
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildConnectionConfig
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.uuid.Uuid

class BrainyPalAgentAsrPolicyTest {
    @Test
    fun `configured BrainyPal child connection enables agent ASR fallback`() {
        val enabled = BrainyPalAgentAsrPolicy.shouldUseAgentAsr(
            selectedProvider = null,
            connection = BrainyPalChildConnectionConfig(
                baseUrl = "http://192.168.5.80:8000/rikka/v1",
                apiKey = "brainypal-local",
            ),
        )

        assertTrue(enabled)
    }

    @Test
    fun `explicit ASR provider takes precedence over agent ASR fallback`() {
        val enabled = BrainyPalAgentAsrPolicy.shouldUseAgentAsr(
            selectedProvider = ASRProviderSetting.OpenAIRealtime(
                id = Uuid.random(),
                name = "Manual ASR",
                apiKey = "manual-key",
            ),
            connection = BrainyPalChildConnectionConfig(
                baseUrl = "http://192.168.5.80:8000/rikka/v1",
                apiKey = "brainypal-local",
            ),
        )

        assertFalse(enabled)
    }

    @Test
    fun `blank ASR provider does not block BrainyPal agent ASR fallback`() {
        val enabled = BrainyPalAgentAsrPolicy.shouldUseAgentAsr(
            selectedProvider = ASRProviderSetting.OpenAIRealtime(
                id = Uuid.random(),
                name = "Incomplete Manual ASR",
                apiKey = "",
            ),
            connection = BrainyPalChildConnectionConfig(
                baseUrl = "http://192.168.5.80:8000/rikka/v1",
                apiKey = "brainypal-local",
            ),
        )

        assertTrue(enabled)
    }
}

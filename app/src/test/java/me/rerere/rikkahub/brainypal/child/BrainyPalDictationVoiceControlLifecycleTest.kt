package me.rerere.rikkahub.brainypal.child

import me.rerere.asr.ASRStatus
import me.rerere.rikkahub.brainypal.shared.BrainyPalDictationSessionState
import me.rerere.rikkahub.brainypal.shared.BrainyPalDictationSessionStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrainyPalDictationVoiceControlLifecycleTest {
    @Test
    fun `finished dictation session requests ASR stop while voice control is active`() {
        val finished = BrainyPalDictationSessionState(
            itemIds = listOf("dictation_1"),
            currentIndex = 1,
            status = BrainyPalDictationSessionStatus.FINISHED,
        )

        assertTrue(
            BrainyPalDictationVoiceControlLifecycle.shouldStopAfterSessionUpdate(
                isDictation = true,
                session = finished,
                asrStatus = ASRStatus.Listening,
            )
        )
        assertTrue(
            BrainyPalDictationVoiceControlLifecycle.shouldStopAfterSessionUpdate(
                isDictation = true,
                session = finished,
                asrStatus = ASRStatus.Connecting,
            )
        )
    }

    @Test
    fun `unfinished or idle dictation session does not request ASR stop`() {
        val waiting = BrainyPalDictationSessionState(
            itemIds = listOf("dictation_1"),
            status = BrainyPalDictationSessionStatus.WAITING,
        )
        val finished = waiting.copy(
            currentIndex = 1,
            status = BrainyPalDictationSessionStatus.FINISHED,
        )

        assertFalse(
            BrainyPalDictationVoiceControlLifecycle.shouldStopAfterSessionUpdate(
                isDictation = true,
                session = waiting,
                asrStatus = ASRStatus.Listening,
            )
        )
        assertFalse(
            BrainyPalDictationVoiceControlLifecycle.shouldStopAfterSessionUpdate(
                isDictation = true,
                session = finished,
                asrStatus = ASRStatus.Idle,
            )
        )
    }
}

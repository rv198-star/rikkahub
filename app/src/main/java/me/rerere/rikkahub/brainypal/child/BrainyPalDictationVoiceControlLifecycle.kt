package me.rerere.rikkahub.brainypal.child

import me.rerere.asr.ASRStatus
import me.rerere.rikkahub.brainypal.shared.BrainyPalDictationSessionState

object BrainyPalDictationVoiceControlLifecycle {
    fun shouldStopAfterSessionUpdate(
        isDictation: Boolean,
        session: BrainyPalDictationSessionState,
        asrStatus: ASRStatus,
    ): Boolean {
        return isDictation && session.isFinished && asrStatus.isRecording()
    }

    private fun ASRStatus.isRecording(): Boolean {
        return this == ASRStatus.Connecting ||
            this == ASRStatus.Listening ||
            this == ASRStatus.Stopping
    }
}

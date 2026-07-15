package me.rerere.rikkahub.brainypal.child

import me.rerere.rikkahub.brainypal.shared.BrainyPalDictationSessionStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class BrainyPalDictationPrimaryButtonPolicyTest {
    @Test
    fun `primary button label follows dictation session status`() {
        assertEquals(
            "开始听写",
            BrainyPalDictationPrimaryButtonPolicy.label(BrainyPalDictationSessionStatus.IDLE),
        )
        assertEquals(
            "重新开始",
            BrainyPalDictationPrimaryButtonPolicy.label(BrainyPalDictationSessionStatus.WAITING),
        )
        assertEquals(
            "继续",
            BrainyPalDictationPrimaryButtonPolicy.label(BrainyPalDictationSessionStatus.PAUSED),
        )
        assertEquals(
            "已播完",
            BrainyPalDictationPrimaryButtonPolicy.label(BrainyPalDictationSessionStatus.FINISHED),
        )
    }
}

package me.rerere.rikkahub.brainypal.child

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BrainyPalDictationPlaybackCountdownPolicyTest {
    @Test
    fun `shows step and seconds while waiting for the next utterance`() {
        val model = BrainyPalDictationPlaybackCountdownPolicy.model(
            stepIndex = 1,
            stepCount = 4,
            millisUntilNext = 1_600L,
            waitingForNextSegment = true,
        )

        assertEquals("片段 2 / 4", model?.stepLabel)
        assertEquals("2 秒后继续", model?.countdownLabel)
        assertEquals(0.5f, model?.progress ?: 0f, 0.001f)
    }

    @Test
    fun `shows playback state without countdown during the current utterance`() {
        val model = BrainyPalDictationPlaybackCountdownPolicy.model(
            stepIndex = 0,
            stepCount = 3,
            millisUntilNext = 0L,
            waitingForNextSegment = false,
        )

        assertEquals("片段 1 / 3", model?.stepLabel)
        assertEquals("正在播放", model?.countdownLabel)
        assertEquals(1f / 3f, model?.progress ?: 0f, 0.001f)
    }

    @Test
    fun `marks the final utterance without a waiting countdown`() {
        val model = BrainyPalDictationPlaybackCountdownPolicy.model(
            stepIndex = 2,
            stepCount = 3,
            millisUntilNext = 0L,
            waitingForNextSegment = false,
        )

        assertEquals("片段 3 / 3", model?.stepLabel)
        assertEquals("最后一段", model?.countdownLabel)
        assertEquals(1f, model?.progress ?: 0f, 0.001f)
    }

    @Test
    fun `hides countdown for single utterance plans`() {
        assertNull(
            BrainyPalDictationPlaybackCountdownPolicy.model(
                stepIndex = 0,
                stepCount = 1,
                millisUntilNext = 0L,
                waitingForNextSegment = false,
            )
        )
    }

    @Test
    fun `counts only dictation content utterances and ignores prefix or closing prompts`() {
        val utterances = listOf("第 2 条", "观察", "观察", "观察", "写完后拍照批改")

        val firstContent = BrainyPalDictationPlaybackCountdownPolicy.modelForUtterance(
            utterances = utterances,
            utteranceIndex = 1,
            millisUntilNext = 0L,
            waitingForNextSegment = false,
        )
        val waitingAfterSecondContent = BrainyPalDictationPlaybackCountdownPolicy.modelForUtterance(
            utterances = utterances,
            utteranceIndex = 2,
            millisUntilNext = 1_200L,
            waitingForNextSegment = true,
        )

        assertNull(
            BrainyPalDictationPlaybackCountdownPolicy.modelForUtterance(
                utterances = utterances,
                utteranceIndex = 0,
                millisUntilNext = 0L,
                waitingForNextSegment = false,
            )
        )
        assertEquals("片段 1 / 3", firstContent?.stepLabel)
        assertEquals(1f / 3f, firstContent?.progress ?: 0f, 0.001f)
        assertEquals("片段 2 / 3", waitingAfterSecondContent?.stepLabel)
        assertEquals("2 秒后继续", waitingAfterSecondContent?.countdownLabel)
        assertNull(
            BrainyPalDictationPlaybackCountdownPolicy.modelForUtterance(
                utterances = utterances,
                utteranceIndex = 4,
                millisUntilNext = 0L,
                waitingForNextSegment = false,
            )
        )
    }
}

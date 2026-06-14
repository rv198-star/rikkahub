package me.rerere.rikkahub.brainypal.shared

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BrainyPalDictationSessionTest {
    @Test
    fun `voice command matcher accepts fuzzy child phrases`() {
        assertEquals(
            BrainyPalDictationCommand.REPEAT,
            BrainyPalDictationVoiceCommandMatcher.match("刚才没听清，再来一遍", waitingForChild = true),
        )
        assertEquals(
            BrainyPalDictationCommand.NEXT,
            BrainyPalDictationVoiceCommandMatcher.match("写好了，下一个", waitingForChild = true),
        )
        assertEquals(
            BrainyPalDictationCommand.DONT_KNOW,
            BrainyPalDictationVoiceCommandMatcher.match("这个我不会，先跳过", waitingForChild = true),
        )
        assertEquals(
            BrainyPalDictationCommand.PAUSE,
            BrainyPalDictationVoiceCommandMatcher.match("等一下，暂停", waitingForChild = true),
        )
        assertEquals(
            BrainyPalDictationCommand.RESUME,
            BrainyPalDictationVoiceCommandMatcher.match("接着来", waitingForChild = false),
        )
    }

    @Test
    fun `ambiguous short commands only advance while waiting for child`() {
        assertEquals(
            BrainyPalDictationCommand.NEXT,
            BrainyPalDictationVoiceCommandMatcher.match("过", waitingForChild = true),
        )
        assertEquals(
            BrainyPalDictationCommand.UNKNOWN,
            BrainyPalDictationVoiceCommandMatcher.match("过", waitingForChild = false),
        )
        assertEquals(
            BrainyPalDictationCommand.UNKNOWN,
            BrainyPalDictationVoiceCommandMatcher.match("好", waitingForChild = false),
        )
    }

    @Test
    fun `session start repeat next and finish produce playback targets`() {
        var state = BrainyPalDictationSessionState(itemIds = listOf("dictation_1", "dictation_2"))

        assertEquals("dictation_1", state.currentItemId)
        assertFalse(state.isActiveItem("dictation_1"))

        val start = BrainyPalDictationSession.reduce(state, BrainyPalDictationCommand.START)
        assertEquals(BrainyPalDictationSessionStatus.WAITING, start.state.status)
        assertEquals("dictation_1", start.playbackItemId)
        assertTrue(start.state.isActiveItem("dictation_1"))
        state = start.state

        val repeat = BrainyPalDictationSession.reduce(state, BrainyPalDictationCommand.REPEAT)
        assertEquals("dictation_1", repeat.playbackItemId)
        assertEquals(1, repeat.state.repeatCountFor("dictation_1"))
        state = repeat.state

        val next = BrainyPalDictationSession.reduce(state, BrainyPalDictationCommand.NEXT)
        assertEquals(1, next.state.currentIndex)
        assertEquals("dictation_2", next.playbackItemId)
        state = next.state

        val finish = BrainyPalDictationSession.reduce(state, BrainyPalDictationCommand.NEXT)
        assertEquals(BrainyPalDictationSessionStatus.FINISHED, finish.state.status)
        assertNull(finish.playbackItemId)
        assertTrue(finish.state.isFinished)
    }

    @Test
    fun `dont know advances and records process signal without answer evidence`() {
        val initial = BrainyPalDictationSessionState(itemIds = listOf("dictation_1", "dictation_2"))
        val started = BrainyPalDictationSession.reduce(initial, BrainyPalDictationCommand.START).state

        val update = BrainyPalDictationSession.reduce(started, BrainyPalDictationCommand.DONT_KNOW)

        assertEquals(1, update.state.currentIndex)
        assertEquals("dictation_2", update.playbackItemId)
        assertEquals(1, update.state.dontKnowCountFor("dictation_1"))
        assertFalse(update.state.affectsAnswerEvidence)
    }

    @Test
    fun `pause and resume do not advance item`() {
        val started = BrainyPalDictationSession.reduce(
            BrainyPalDictationSessionState(itemIds = listOf("dictation_1")),
            BrainyPalDictationCommand.START,
        ).state

        val paused = BrainyPalDictationSession.reduce(started, BrainyPalDictationCommand.PAUSE)
        assertEquals(BrainyPalDictationSessionStatus.PAUSED, paused.state.status)
        assertEquals(0, paused.state.currentIndex)
        assertFalse(paused.state.isActiveItem("dictation_1"))
        assertNull(paused.playbackItemId)

        val resumed = BrainyPalDictationSession.reduce(paused.state, BrainyPalDictationCommand.RESUME)
        assertEquals(BrainyPalDictationSessionStatus.WAITING, resumed.state.status)
        assertTrue(resumed.state.isActiveItem("dictation_1"))
        assertEquals("dictation_1", resumed.playbackItemId)
    }
}

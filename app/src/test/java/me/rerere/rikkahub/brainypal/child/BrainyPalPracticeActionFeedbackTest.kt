package me.rerere.rikkahub.brainypal.child

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BrainyPalPracticeActionFeedbackTest {
    @Test
    fun `help request exposes a visible pending message`() {
        val status = BrainyPalPracticeActionFeedback.pendingStatus(
            BrainyPalPracticeActionFeedback.HELP_PENDING_MESSAGE
        )

        assertEquals("BrainyPal 正在想提示...", status.message)
        assertFalse(status.error)
    }

    @Test
    fun `server help message becomes an item scoped hint`() {
        val hint = BrainyPalPracticeActionFeedback.helpHintFor(
            helpItemId = "item_2",
            helpMessage = "可以先圈出题目在问什么。",
            previousHint = null,
        )

        assertEquals("item_2", hint?.itemId)
        assertEquals("可以先圈出题目在问什么。", hint?.message)
    }

    @Test
    fun `pending help request shows an item scoped loading hint`() {
        val hint = BrainyPalPracticeActionFeedback.pendingHelpHintFor(
            helpItemId = "item_2",
            previousHint = null,
        )

        assertEquals("item_2", hint?.itemId)
        assertEquals(BrainyPalPracticeActionFeedback.HELP_PENDING_MESSAGE, hint?.message)
    }

    @Test
    fun `blank help response is reported as an error instead of success`() {
        val status = BrainyPalPracticeActionFeedback.resultStatus(
            successMessage = BrainyPalPracticeActionFeedback.HELP_SUCCESS_MESSAGE,
            needsMoreEffort = false,
            helpItemId = "item_2",
            helpMessage = " ",
        )

        assertEquals(BrainyPalPracticeActionFeedback.HELP_EMPTY_MESSAGE, status.message)
        assertTrue(status.error)
    }

    @Test
    fun `blank server help message keeps the previous hint`() {
        val previousHint = BrainyPalPracticeTaskHelpHint(
            itemId = "item_1",
            message = "旧提示",
        )

        val hint = BrainyPalPracticeActionFeedback.helpHintFor(
            helpItemId = "item_2",
            helpMessage = " ",
            previousHint = previousHint,
        )

        assertEquals(previousHint, hint)
    }

    @Test
    fun `help hint is absent when no item was requested`() {
        val hint = BrainyPalPracticeActionFeedback.helpHintFor(
            helpItemId = null,
            helpMessage = "可以先圈出题目在问什么。",
            previousHint = null,
        )

        assertNull(hint)
    }
}

package me.rerere.rikkahub.brainypal.child

import org.junit.Assert.assertEquals
import org.junit.Test

class BrainyPalPracticeTaskCopyTest {
    @Test
    fun `item count label follows child task type`() {
        assertEquals("2 题", BrainyPalPracticeTaskCopy.itemCountLabel("wrong_question_practice", 2))
        assertEquals("3 条", BrainyPalPracticeTaskCopy.itemCountLabel("dictation", 3))
        assertEquals("1 段", BrainyPalPracticeTaskCopy.itemCountLabel("recitation", 1))
    }
}

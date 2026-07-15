package me.rerere.rikkahub.brainypal.child

import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskDetail
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskItem
import me.rerere.rikkahub.brainypal.shared.BrainyPalPracticeAttemptAnswer
import me.rerere.rikkahub.brainypal.shared.BrainyPalPracticeTaskItemResult
import me.rerere.rikkahub.brainypal.shared.BrainyPalPracticeTaskResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrainyPalPracticeResultReviewTest {
    @Test
    fun `result review keeps original answer read only and opens separate correction`() {
        val rows = BrainyPalPracticeResultReview.rows(
            detail = BrainyPalChildPracticeTaskDetail(
                taskId = "task-1",
                title = "方程练习",
                taskType = "practice",
                status = "submitted",
                helpLimit = 2,
                helpUsed = 1,
                items = listOf(
                    BrainyPalChildPracticeTaskItem(
                        itemId = "q1",
                        prompt = "2x = 10",
                    )
                ),
                result = BrainyPalPracticeTaskResult(
                    status = "completed",
                    itemResults = mapOf(
                        "q1" to BrainyPalPracticeTaskItemResult(
                            status = "incorrect",
                            childFeedback = "这题先标记为需要订正，复盘时看关键步骤。",
                            correctionPrompt = "先保留原答案，再在订正区重新写一次。",
                            expectedAnswer = "5",
                            wrongQuestionRef = "practice_task://task-1/items/q1",
                        )
                    ),
                ),
                answers = mapOf(
                    "q1" to BrainyPalPracticeAttemptAnswer(
                        itemId = "q1",
                        value = "6",
                        source = "app",
                    )
                ),
            )
        )

        val row = rows.single()
        assertEquals("第 1 题", row.title)
        assertEquals("需要订正", row.statusLabel)
        assertEquals("6", row.originalAnswer)
        assertTrue(row.originalAnswerReadOnly)
        assertTrue(row.canCorrectNow)
        assertEquals("正确答案：5", row.expectedAnswerLabel)
        assertEquals("先保留原答案，再在订正区重新写一次。", row.correctionPrompt)
    }

    @Test
    fun `needs review result asks parent confirmation instead of immediate correction`() {
        val rows = BrainyPalPracticeResultReview.rows(
            detail = BrainyPalChildPracticeTaskDetail(
                taskId = "task-2",
                title = "听写",
                taskType = "dictation",
                status = "submitted",
                helpLimit = 2,
                helpUsed = 0,
                items = listOf(BrainyPalChildPracticeTaskItem(itemId = "w1", prompt = "清澈")),
                result = BrainyPalPracticeTaskResult(
                    status = "completed",
                    itemResults = mapOf(
                        "w1" to BrainyPalPracticeTaskItemResult(
                            status = "needs_review",
                            childFeedback = "这题的照片识别不够确定。",
                            correctionPrompt = "先确认照片区域，再决定是否订正。",
                        )
                    ),
                ),
            )
        )

        val row = rows.single()
        assertEquals("需要确认", row.statusLabel)
        assertFalse(row.canCorrectNow)
        assertTrue(row.needsParentOrOcrReview)
    }
}

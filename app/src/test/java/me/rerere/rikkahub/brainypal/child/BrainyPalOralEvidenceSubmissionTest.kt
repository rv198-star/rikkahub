package me.rerere.rikkahub.brainypal.child

import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskDetail
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskItem
import org.junit.Assert.assertEquals
import org.junit.Test

class BrainyPalOralEvidenceSubmissionTest {
    @Test
    fun `oral evidence request only includes attempted items`() {
        val request = oralEvidenceRequest(
            detail = oralTask(attemptSessionId = "attempt_reading_1"),
            drafts = BrainyPalPracticeDrafts()
                .edit("line_1", "4", "第二句换气"),
            rereadCount = 2,
            textHiddenDuringAttempt = false,
        )

        assertEquals("attempt_reading_1", request?.attemptSessionId)
        assertEquals(listOf("line_1"), request?.items?.map { it.itemId })
        assertEquals(4, request?.items?.single()?.selfRating)
        assertEquals(2, request?.items?.single()?.rereadCount)
        assertEquals(listOf("第二句换气"), request?.items?.single()?.stuckPoints)
    }

    @Test
    fun `oral evidence request is not created without attempt session`() {
        val request = oralEvidenceRequest(
            detail = oralTask(attemptSessionId = null),
            drafts = BrainyPalPracticeDrafts()
                .edit("line_1", "4", ""),
            rereadCount = 1,
            textHiddenDuringAttempt = true,
        )

        assertEquals(null, request)
    }

    private fun oralTask(attemptSessionId: String?): BrainyPalChildPracticeTaskDetail {
        return BrainyPalChildPracticeTaskDetail(
            rawTaskId = "reading-task",
            attemptSessionId = attemptSessionId,
            rawTitle = "朗读短文",
            legacyTaskType = "reading",
            status = "in_progress",
            helpUsed = 0,
            legacyItems = listOf(
                BrainyPalChildPracticeTaskItem(
                    itemId = "line_1",
                    prompt = "春天来了，小草从土里探出头。",
                ),
                BrainyPalChildPracticeTaskItem(
                    itemId = "line_2",
                    prompt = "花儿也慢慢开了。",
                ),
            ),
        )
    }
}

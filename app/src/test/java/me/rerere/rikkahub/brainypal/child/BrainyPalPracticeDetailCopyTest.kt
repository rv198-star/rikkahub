package me.rerere.rikkahub.brainypal.child

import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskDetail
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskItem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrainyPalPracticeDetailCopyTest {
    @Test
    fun `recitation detail copy avoids hint ticket framing`() {
        val detail = detail(taskType = "recitation", helpLimit = 2, helpUsed = 1)
        val copy = BrainyPalPracticeDetailCopy.supportingText(
            detail = detail,
            interactionPlan = BrainyPalChildTaskInteraction.plan(detail),
        )

        assertTrue(copy.contains("1 段"))
        assertFalse(copy.contains("提示券"))
    }

    @Test
    fun `practice detail copy keeps hint ticket framing`() {
        val detail = detail(taskType = "wrong_question_practice", helpLimit = 3, helpUsed = 1)
        val copy = BrainyPalPracticeDetailCopy.supportingText(
            detail = detail,
            interactionPlan = BrainyPalChildTaskInteraction.plan(detail),
        )

        assertTrue(copy.contains("1 题"))
        assertTrue(copy.contains("提示券 2/3"))
    }

    private fun detail(
        taskType: String,
        helpLimit: Int,
        helpUsed: Int,
    ): BrainyPalChildPracticeTaskDetail {
        return BrainyPalChildPracticeTaskDetail(
            taskId = "task-1",
            title = "任务",
            taskType = taskType,
            status = "accepted",
            helpLimit = helpLimit,
            helpUsed = helpUsed,
            items = listOf(
                BrainyPalChildPracticeTaskItem(
                    itemId = "item-1",
                    prompt = "材料",
                )
            ),
        )
    }
}

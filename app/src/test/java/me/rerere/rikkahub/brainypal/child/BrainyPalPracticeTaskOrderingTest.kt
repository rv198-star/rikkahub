package me.rerere.rikkahub.brainypal.child

import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskSummary
import org.junit.Assert.assertEquals
import org.junit.Test

class BrainyPalPracticeTaskOrderingTest {
    @Test
    fun `child task list prefers currently actionable tasks before submitted feedback`() {
        val tasks = listOf(
            task("submitted-1", "submitted"),
            task("available-1", "available"),
            task("submitted-2", "reviewing"),
        )

        assertEquals(
            listOf("available-1", "submitted-1", "submitted-2"),
            BrainyPalPracticeTaskOrdering.forChild(tasks).map { it.taskId },
        )
    }

    @Test
    fun `child task list resumes in progress before starting new work`() {
        val tasks = listOf(
            task("assigned-1", "assigned"),
            task("in-progress-1", "in_progress"),
            task("available-1", "available"),
        )

        assertEquals(
            listOf("in-progress-1", "available-1", "assigned-1"),
            BrainyPalPracticeTaskOrdering.forChild(tasks).map { it.taskId },
        )
    }

    @Test
    fun `child task list keeps feedback order when no actionable task exists`() {
        val tasks = listOf(
            task("submitted-1", "submitted"),
            task("reviewing-1", "reviewing"),
            task("completed-1", "completed"),
        )

        assertEquals(
            listOf("submitted-1", "reviewing-1", "completed-1"),
            BrainyPalPracticeTaskOrdering.forChild(tasks).map { it.taskId },
        )
    }

    private fun task(
        taskId: String,
        status: String,
        blankOrLowEffort: Boolean = false,
    ) = BrainyPalChildPracticeTaskSummary(
        taskId = taskId,
        title = taskId,
        taskType = "wrong_question_practice",
        status = status,
        itemCount = 1,
        helpLimit = 3,
        helpUsed = 0,
        blankOrLowEffort = blankOrLowEffort,
    )
}

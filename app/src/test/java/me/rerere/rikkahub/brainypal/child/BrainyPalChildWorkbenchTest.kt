package me.rerere.rikkahub.brainypal.child

import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.brainypal.shared.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrainyPalChildWorkbenchTest {
    @Test
    fun `unconfigured workbench routes parent setup as primary action`() {
        val workbench = BrainyPalChildWorkbench.from(
            connection = BrainyPalChildConnectionConfig(),
            practiceTasks = emptyList(),
            reviewOffer = null,
            chatScreen = Screen.Chat("chat-id"),
        )

        assertFalse(workbench.configured)
        assertEquals("需要家长配置 BrainyPal", workbench.connectionStatus)
        assertEquals("配置连接", workbench.practiceAction.label)
        assertEquals(Screen.BrainyPalConnection, workbench.practiceAction.target)
        assertEquals(Screen.Chat("chat-id"), workbench.chatAction.target)
        assertFalse(workbench.showReviewOffer)
    }

    @Test
    fun `configured workbench hides connection internals and summarizes new tasks`() {
        val workbench = BrainyPalChildWorkbench.from(
            connection = BrainyPalChildConnectionConfig(
                baseUrl = "http://192.168.1.20:8000/rikka/v1",
                apiKey = "brainypal-local",
            ),
            practiceTasks = listOf(
                BrainyPalChildPracticeTaskSummary(
                    taskId = "task-1",
                    title = "今日练习",
                    taskType = "wrong_question_practice",
                    status = "assigned",
                    itemCount = 2,
                    helpLimit = 3,
                    helpUsed = 1,
                )
            ),
            reviewOffer = null,
            chatScreen = Screen.Chat("chat-id"),
        )

        assertTrue(workbench.configured)
        assertEquals("BrainyPal 已准备好", workbench.connectionStatus)
        assertEquals("开始今日任务", workbench.practiceAction.label)
        assertEquals(Screen.BrainyPalPractice, workbench.practiceAction.target)
        assertEquals("1 个任务等你开始", workbench.practiceSummary)
    }

    @Test
    fun `workbench summarizes in progress tasks as continuation`() {
        val workbench = BrainyPalChildWorkbench.from(
            connection = BrainyPalChildConnectionConfig(
                baseUrl = "http://192.168.1.20:8000/rikka/v1",
                apiKey = "brainypal-local",
            ),
            practiceTasks = listOf(
                BrainyPalChildPracticeTaskSummary(
                    taskId = "task-1",
                    title = "错题复盘",
                    taskType = "wrong_question_practice",
                    status = "in_progress",
                    itemCount = 2,
                    helpLimit = 3,
                    helpUsed = 1,
                )
            ),
            reviewOffer = null,
            chatScreen = Screen.Chat("chat-id"),
        )

        assertEquals("1 个任务可以继续", workbench.practiceSummary)
        assertEquals("继续今日任务", workbench.practiceAction.label)
    }

    @Test
    fun `workbench treats pending agent tasks as startable`() {
        val workbench = BrainyPalChildWorkbench.from(
            connection = BrainyPalChildConnectionConfig(
                baseUrl = "http://192.168.1.20:8000/rikka/v1",
                apiKey = "brainypal-local",
            ),
            practiceTasks = listOf(
                BrainyPalChildPracticeTaskSummary(
                    taskId = "task-pending",
                    title = "语文生字听写",
                    taskType = "dictation",
                    status = "pending",
                    itemCount = 4,
                    helpLimit = 3,
                    helpUsed = 0,
                )
            ),
            reviewOffer = null,
            chatScreen = Screen.Chat("chat-id"),
        )

        assertEquals("开始今日任务", workbench.practiceAction.label)
        assertEquals("1 个任务等你开始", workbench.practiceSummary)
    }

    @Test
    fun `task action labels follow task status`() {
        fun task(status: String) = BrainyPalChildPracticeTaskSummary(
            taskId = "task-$status",
            title = "练习",
            taskType = "wrong_question_practice",
            status = status,
            itemCount = 1,
            helpLimit = 3,
            helpUsed = 0,
        )

        assertEquals("开始", BrainyPalChildWorkbench.taskActionLabel(task("assigned")))
        assertEquals("继续", BrainyPalChildWorkbench.taskActionLabel(task("in_progress")))
        assertEquals("看反馈", BrainyPalChildWorkbench.taskActionLabel(task("submitted")))
        assertEquals("看反馈", BrainyPalChildWorkbench.taskActionLabel(task("completed")))
        assertEquals("已结束", BrainyPalChildWorkbench.taskActionLabel(task("expired")))
    }

    @Test
    fun `workbench shows actionable review offer`() {
        val offer = BrainyPalReviewOfferResponse(
            shouldOffer = true,
            childMessage = "要不要试一小步？",
            event = BrainyPalReviewOfferEvent(
                relatedQuestionId = "wq_due_001",
                strategyVersionId = "strategy_review_prompt_1",
                evidenceRefs = listOf("wrong_question:wq_due_001"),
            ),
        )

        val workbench = BrainyPalChildWorkbench.from(
            connection = BrainyPalChildConnectionConfig(
                baseUrl = "http://192.168.1.20:8000/rikka/v1",
                apiKey = "brainypal-local",
            ),
            practiceTasks = emptyList(),
            reviewOffer = offer,
            chatScreen = Screen.Chat("chat-id"),
        )

        assertTrue(workbench.showReviewOffer)
        assertEquals("要不要试一小步？", workbench.reviewMessage)
        assertEquals("复习一下", workbench.reviewAction.label)
        assertEquals(Screen.BrainyPalPractice, workbench.reviewAction.target)
    }
}

package me.rerere.rikkahub.brainypal

import me.rerere.rikkahub.Screen
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
    fun `configured workbench prefers native practice route and summarizes tasks`() {
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
        assertEquals("192.168.1.20:8000/rikka/v1", workbench.connectionStatus)
        assertEquals("今日练习", workbench.practiceAction.label)
        assertEquals(Screen.BrainyPalPractice, workbench.practiceAction.target)
        assertEquals("1 个任务等你开始", workbench.practiceSummary)
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

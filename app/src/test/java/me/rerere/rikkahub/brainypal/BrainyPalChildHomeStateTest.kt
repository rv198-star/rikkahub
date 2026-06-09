package me.rerere.rikkahub.brainypal

import kotlinx.coroutines.runBlocking
import me.rerere.rikkahub.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrainyPalChildHomeStateTest {
    @Test
    fun `unconfigured state does not require a child api`() {
        var apiCreated = false

        val state = runBlocking { BrainyPalChildHomeState.from(
            connection = BrainyPalChildConnectionConfig(),
            apiFactory = { _, _ ->
                apiCreated = true
                error("API should not be created before BrainyPal is configured")
            },
            chatScreen = Screen.Chat("chat-id"),
        ) }

        assertFalse(apiCreated)
        assertFalse(state.workbench.configured)
        assertEquals("配置连接", state.workbench.practiceAction.label)
        assertEquals(emptyList<BrainyPalChildPracticeTaskSummary>(), state.practiceTasks)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `configured state loads practice tasks and review offer from agent service`() {
        val connection = BrainyPalChildConnectionConfig(
            baseUrl = "http://192.168.1.20:8000/rikka/v1",
            apiKey = "brainypal-local",
        )
        val api = FakeBrainyPalChildApi(
            tasks = listOf(
                BrainyPalChildPracticeTaskSummary(
                    taskId = "task-1",
                    title = "错题小练习",
                    taskType = "wrong_question_practice",
                    status = "assigned",
                    itemCount = 3,
                    helpLimit = 3,
                    helpUsed = 1,
                )
            ),
            reviewOffer = BrainyPalReviewOfferResponse(
                shouldOffer = true,
                childMessage = "先复习一道相似题？",
                event = BrainyPalReviewOfferEvent(
                    relatedQuestionId = "wq_due_001",
                    strategyVersionId = "strategy_review_prompt_1",
                ),
            )
        )

        val state = runBlocking { BrainyPalChildHomeState.from(
            connection = connection,
            apiFactory = { rootUrl, apiKey ->
                assertEquals("http://192.168.1.20:8000", rootUrl)
                assertEquals("brainypal-local", apiKey)
                api
            },
            chatScreen = Screen.Chat("chat-id"),
        ) }

        assertTrue(state.workbench.configured)
        assertEquals("1 个任务等你开始", state.workbench.practiceSummary)
        assertTrue(state.workbench.showReviewOffer)
        assertEquals("先复习一道相似题？", state.workbench.reviewMessage)
        assertEquals(listOf("task-1"), state.practiceTasks.map { it.taskId })
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `configured state keeps local actions when agent service fails`() {
        val state = runBlocking { BrainyPalChildHomeState.from(
            connection = BrainyPalChildConnectionConfig(
                baseUrl = "http://192.168.1.20:8000/rikka/v1",
                apiKey = "brainypal-local",
            ),
            apiFactory = { _, _ -> error("offline") },
            chatScreen = Screen.Chat("chat-id"),
        ) }

        assertTrue(state.workbench.configured)
        assertEquals(Screen.BrainyPalPractice, state.workbench.practiceAction.target)
        assertEquals(emptyList<BrainyPalChildPracticeTaskSummary>(), state.practiceTasks)
        assertEquals("暂时连不上 BrainyPal，可以稍后重试", state.errorMessage)
    }

    private class FakeBrainyPalChildApi(
        private val tasks: List<BrainyPalChildPracticeTaskSummary>,
        private val reviewOffer: BrainyPalReviewOfferResponse,
    ) : BrainyPalChildApi {
        override suspend fun listPracticeTasks(): BrainyPalChildPracticeTaskListResponse {
            return BrainyPalChildPracticeTaskListResponse(items = tasks)
        }

        override suspend fun getReviewOffer(remainingMinutes: Int?): BrainyPalReviewOfferResponse {
            return reviewOffer
        }
    }
}

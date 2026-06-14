package me.rerere.rikkahub.brainypal.child

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.brainypal.shared.*
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
        assertEquals(BrainyPalChildConnectionConfig(), state.connection)
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
        assertEquals(connection, state.connection)
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

    @Test
    fun `configured state rethrows coroutine cancellation`() {
        val caught = try {
            runBlocking { BrainyPalChildHomeState.from(
                connection = BrainyPalChildConnectionConfig(
                    baseUrl = "http://192.168.1.20:8000/rikka/v1",
                    apiKey = "brainypal-local",
                ),
                apiFactory = { _, _ -> CancellingBrainyPalChildApi },
                chatScreen = Screen.Chat("chat-id"),
            ) }
            null
        } catch (error: CancellationException) {
            error
        }

        assertEquals("cancelled", caught?.message)
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

        override suspend fun getPracticeTask(taskId: String): BrainyPalChildPracticeTaskDetail {
            error("practice detail should not be loaded by home state")
        }

        override suspend fun createPracticeTaskHandoffCode(
            taskId: String,
            request: BrainyPalCreatePracticeHandoffCodeRequest,
        ): BrainyPalPracticeHandoffCodeResponse {
            error("practice handoff code should not be created by home state")
        }

        override suspend fun acceptPracticeTask(
            taskId: String,
            request: BrainyPalAcceptPracticeTaskRequest,
        ): BrainyPalChildPracticeTaskDetail {
            error("practice task should not be accepted by home state")
        }

        override suspend fun recordPracticeTaskAnswer(
            taskId: String,
            itemId: String,
            request: BrainyPalRecordPracticeTaskAnswerRequest,
        ): BrainyPalChildPracticeTaskDetail {
            error("practice answer should not be saved by home state")
        }

        override suspend fun requestPracticeTaskHelp(
            taskId: String,
            request: BrainyPalRequestPracticeTaskHelpRequest,
        ): BrainyPalPracticeHintResponse {
            error("practice help should not be requested by home state")
        }

        override suspend fun submitPracticeTask(
            taskId: String,
            request: BrainyPalSubmitPracticeTaskRequest,
        ): BrainyPalChildPracticeTaskDetail {
            error("practice task should not be submitted by home state")
        }

        override suspend fun submitDictationOcrEvidence(
            taskId: String,
            request: BrainyPalSubmitDictationOcrEvidenceRequest,
        ): BrainyPalChildPracticeTaskDetail {
            error("dictation OCR evidence should not be submitted by home state")
        }

        override suspend fun submitOralEvidence(
            taskId: String,
            request: BrainyPalSubmitOralEvidenceRequest,
        ): BrainyPalPracticeAttemptSessionResponse {
            error("oral evidence should not be submitted by home state")
        }

        override suspend fun confirmDictationOcrEvidence(
            taskId: String,
            itemId: String,
            request: BrainyPalConfirmDictationOcrEvidenceRequest,
        ): BrainyPalChildPracticeTaskDetail {
            error("dictation OCR evidence should not be confirmed by home state")
        }
    }

    private object CancellingBrainyPalChildApi : BrainyPalChildApi {
        override suspend fun listPracticeTasks(): BrainyPalChildPracticeTaskListResponse {
            throw CancellationException("cancelled")
        }

        override suspend fun getReviewOffer(remainingMinutes: Int?): BrainyPalReviewOfferResponse {
            error("review offer should not be loaded after cancellation")
        }

        override suspend fun getPracticeTask(taskId: String): BrainyPalChildPracticeTaskDetail {
            error("practice detail should not be loaded by home state")
        }

        override suspend fun createPracticeTaskHandoffCode(
            taskId: String,
            request: BrainyPalCreatePracticeHandoffCodeRequest,
        ): BrainyPalPracticeHandoffCodeResponse {
            error("practice handoff code should not be created by home state")
        }

        override suspend fun acceptPracticeTask(
            taskId: String,
            request: BrainyPalAcceptPracticeTaskRequest,
        ): BrainyPalChildPracticeTaskDetail {
            error("practice task should not be accepted by home state")
        }

        override suspend fun recordPracticeTaskAnswer(
            taskId: String,
            itemId: String,
            request: BrainyPalRecordPracticeTaskAnswerRequest,
        ): BrainyPalChildPracticeTaskDetail {
            error("practice answer should not be saved by home state")
        }

        override suspend fun requestPracticeTaskHelp(
            taskId: String,
            request: BrainyPalRequestPracticeTaskHelpRequest,
        ): BrainyPalPracticeHintResponse {
            error("practice help should not be requested by home state")
        }

        override suspend fun submitPracticeTask(
            taskId: String,
            request: BrainyPalSubmitPracticeTaskRequest,
        ): BrainyPalChildPracticeTaskDetail {
            error("practice task should not be submitted by home state")
        }

        override suspend fun submitDictationOcrEvidence(
            taskId: String,
            request: BrainyPalSubmitDictationOcrEvidenceRequest,
        ): BrainyPalChildPracticeTaskDetail {
            error("dictation OCR evidence should not be submitted by home state")
        }

        override suspend fun submitOralEvidence(
            taskId: String,
            request: BrainyPalSubmitOralEvidenceRequest,
        ): BrainyPalPracticeAttemptSessionResponse {
            error("oral evidence should not be submitted by home state")
        }

        override suspend fun confirmDictationOcrEvidence(
            taskId: String,
            itemId: String,
            request: BrainyPalConfirmDictationOcrEvidenceRequest,
        ): BrainyPalChildPracticeTaskDetail {
            error("dictation OCR evidence should not be confirmed by home state")
        }
    }
}

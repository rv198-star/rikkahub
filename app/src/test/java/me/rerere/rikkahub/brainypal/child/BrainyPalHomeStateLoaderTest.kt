package me.rerere.rikkahub.brainypal.child

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildApi
import me.rerere.rikkahub.brainypal.shared.BrainyPalAcceptPracticeTaskRequest
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildConnectionConfig
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskListResponse
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskSummary
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskDetail
import me.rerere.rikkahub.brainypal.shared.BrainyPalConfirmDictationOcrEvidenceRequest
import me.rerere.rikkahub.brainypal.shared.BrainyPalCreatePracticeHandoffCodeRequest
import me.rerere.rikkahub.brainypal.shared.BrainyPalPracticeHandoffCodeResponse
import me.rerere.rikkahub.brainypal.shared.BrainyPalPracticeHintResponse
import me.rerere.rikkahub.brainypal.shared.BrainyPalRecordPracticeTaskAnswerRequest
import me.rerere.rikkahub.brainypal.shared.BrainyPalRequestPracticeTaskHelpRequest
import me.rerere.rikkahub.brainypal.shared.BrainyPalReviewOfferResponse
import me.rerere.rikkahub.brainypal.shared.BrainyPalPracticeAttemptSessionResponse
import me.rerere.rikkahub.brainypal.shared.BrainyPalSubmitDictationOcrEvidenceRequest
import me.rerere.rikkahub.brainypal.shared.BrainyPalSubmitOralEvidenceRequest
import me.rerere.rikkahub.brainypal.shared.BrainyPalSubmitPracticeTaskRequest
import me.rerere.rikkahub.data.datastore.Settings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BrainyPalHomeStateLoaderTest {
    @Test
    fun `home state loader skips dummy settings before creating api`() {
        val realSettings = Settings(
            brainyPalChildConnection = BrainyPalChildConnectionConfig(
                baseUrl = "http://192.168.1.20:8000/rikka/v1",
                apiKey = "brainypal-local",
            )
        )
        val settings = flowOf(Settings.dummy(), realSettings)
        val createdRoots = mutableListOf<String>()

        val state = runBlocking {
            settings.loadBrainyPalChildHomeState(
                apiFactory = { rootUrl, _ ->
                    createdRoots += rootUrl
                    FakeBrainyPalChildApi
                },
                chatScreen = Screen.Chat("chat-id"),
            )
        }

        assertEquals(listOf("http://192.168.1.20:8000"), createdRoots)
        assertTrue(state.workbench.configured)
        assertEquals("1 个任务等你开始", state.workbench.practiceSummary)
    }

    @Test
    fun `home state stream reloads when BrainyPal connection changes`() {
        val realSettings = Settings(
            brainyPalChildConnection = BrainyPalChildConnectionConfig(
                baseUrl = "http://192.168.1.20:8000/rikka/v1",
                apiKey = "brainypal-local",
            )
        )
        val settings = flowOf(
            Settings.dummy(),
            Settings(brainyPalChildConnection = BrainyPalChildConnectionConfig()),
            realSettings,
        )
        val createdRoots = mutableListOf<String>()

        val states = runBlocking {
            settings.brainyPalChildHomeStates(
                apiFactory = { rootUrl, _ ->
                    createdRoots += rootUrl
                    FakeBrainyPalChildApi
                },
                chatScreen = Screen.Chat("chat-id"),
            ).toList()
        }

        assertEquals(2, states.size)
        assertEquals(false, states[0].workbench.configured)
        assertEquals(true, states[1].workbench.configured)
        assertEquals(listOf("http://192.168.1.20:8000"), createdRoots)
    }

    private object FakeBrainyPalChildApi : BrainyPalChildApi {
        override suspend fun listPracticeTasks(): BrainyPalChildPracticeTaskListResponse {
            return BrainyPalChildPracticeTaskListResponse(
                items = listOf(
                    BrainyPalChildPracticeTaskSummary(
                        taskId = "task-1",
                        title = "今日练习",
                        taskType = "wrong_question_practice",
                        status = "assigned",
                        itemCount = 1,
                        helpLimit = 3,
                        helpUsed = 0,
                    )
                )
            )
        }

        override suspend fun getReviewOffer(remainingMinutes: Int?): BrainyPalReviewOfferResponse {
            return BrainyPalReviewOfferResponse(
                shouldOffer = false,
                childMessage = "",
                event = null,
            )
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

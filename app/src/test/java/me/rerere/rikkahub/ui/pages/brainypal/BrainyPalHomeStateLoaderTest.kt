package me.rerere.rikkahub.ui.pages.brainypal

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.brainypal.BrainyPalChildApi
import me.rerere.rikkahub.brainypal.BrainyPalChildConnectionConfig
import me.rerere.rikkahub.brainypal.BrainyPalChildPracticeTaskListResponse
import me.rerere.rikkahub.brainypal.BrainyPalChildPracticeTaskSummary
import me.rerere.rikkahub.brainypal.BrainyPalReviewOfferResponse
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
    }
}

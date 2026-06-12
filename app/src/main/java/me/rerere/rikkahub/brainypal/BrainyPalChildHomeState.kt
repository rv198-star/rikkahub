package me.rerere.rikkahub.brainypal

import me.rerere.rikkahub.Screen
import kotlinx.coroutines.CancellationException

data class BrainyPalChildHomeState(
    val connection: BrainyPalChildConnectionConfig,
    val workbench: BrainyPalChildWorkbench,
    val practiceTasks: List<BrainyPalChildPracticeTaskSummary>,
    val reviewOffer: BrainyPalReviewOfferResponse?,
    val errorMessage: String?,
) {
    companion object {
        suspend fun from(
            connection: BrainyPalChildConnectionConfig,
            apiFactory: (rootUrl: String, apiKey: String) -> BrainyPalChildApi,
            chatScreen: Screen,
        ): BrainyPalChildHomeState {
            if (!connection.isConfigured()) {
                return build(
                    connection = connection,
                    practiceTasks = emptyList(),
                    reviewOffer = null,
                    errorMessage = null,
                    chatScreen = chatScreen,
                )
            }

            return try {
                val api = apiFactory(
                    BrainyPalChildModePolicy.agentServiceRootUrl(connection),
                    connection.apiKey,
                )
                val practiceTasks = api.listPracticeTasks().items
                val reviewOffer = api.getReviewOffer()
                build(
                    connection = connection,
                    practiceTasks = practiceTasks,
                    reviewOffer = reviewOffer,
                    errorMessage = null,
                    chatScreen = chatScreen,
                )
            } catch (error: Throwable) {
                if (error is CancellationException) {
                    throw error
                }
                build(
                    connection = connection,
                    practiceTasks = emptyList(),
                    reviewOffer = null,
                    errorMessage = "暂时连不上 BrainyPal，可以稍后重试",
                    chatScreen = chatScreen,
                )
            }
        }

        private fun build(
            connection: BrainyPalChildConnectionConfig,
            practiceTasks: List<BrainyPalChildPracticeTaskSummary>,
            reviewOffer: BrainyPalReviewOfferResponse?,
            errorMessage: String?,
            chatScreen: Screen,
        ): BrainyPalChildHomeState {
            return BrainyPalChildHomeState(
                connection = connection,
                workbench = BrainyPalChildWorkbench.from(
                    connection = connection,
                    practiceTasks = practiceTasks,
                    reviewOffer = reviewOffer,
                    chatScreen = chatScreen,
                ),
                practiceTasks = practiceTasks,
                reviewOffer = reviewOffer,
                errorMessage = errorMessage,
            )
        }
    }
}

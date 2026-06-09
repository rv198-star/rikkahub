package me.rerere.rikkahub.brainypal

import me.rerere.rikkahub.Screen

data class BrainyPalChildWorkbenchAction(
    val label: String,
    val target: Screen,
)

data class BrainyPalChildWorkbench(
    val configured: Boolean,
    val connectionStatus: String,
    val chatAction: BrainyPalChildWorkbenchAction,
    val practiceAction: BrainyPalChildWorkbenchAction,
    val practiceSummary: String,
    val showReviewOffer: Boolean,
    val reviewMessage: String,
    val reviewAction: BrainyPalChildWorkbenchAction,
) {
    companion object {
        fun from(
            connection: BrainyPalChildConnectionConfig,
            practiceTasks: List<BrainyPalChildPracticeTaskSummary>,
            reviewOffer: BrainyPalReviewOfferResponse?,
            chatScreen: Screen,
        ): BrainyPalChildWorkbench {
            val configured = connection.isConfigured()
            val actionableReviewOffer = reviewOffer?.takeIf { it.isActionable }
            return BrainyPalChildWorkbench(
                configured = configured,
                connectionStatus = if (configured) {
                    BrainyPalChildModePolicy.connectionSummary(connection)
                } else {
                    "需要家长配置 BrainyPal"
                },
                chatAction = BrainyPalChildWorkbenchAction(
                    label = "问问 BrainyPal",
                    target = chatScreen,
                ),
                practiceAction = BrainyPalChildWorkbenchAction(
                    label = if (configured) "今日练习" else "配置连接",
                    target = if (configured) Screen.BrainyPalPractice else Screen.BrainyPalConnection,
                ),
                practiceSummary = practiceTasks.summaryText(),
                showReviewOffer = actionableReviewOffer != null,
                reviewMessage = actionableReviewOffer?.childMessage.orEmpty(),
                reviewAction = BrainyPalChildWorkbenchAction(
                    label = "复习一下",
                    target = Screen.BrainyPalPractice,
                ),
            )
        }

        private fun List<BrainyPalChildPracticeTaskSummary>.summaryText(): String {
            val actionableCount = count { it.status != "completed" && it.status != "expired" }
            return when {
                actionableCount > 0 -> "$actionableCount 个任务等你开始"
                isNotEmpty() -> "今天的任务都处理完了"
                else -> "现在没有新的今日练习"
            }
        }
    }
}

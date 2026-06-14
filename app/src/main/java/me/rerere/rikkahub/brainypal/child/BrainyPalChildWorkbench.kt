package me.rerere.rikkahub.brainypal.child

import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildConnectionConfig
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskSummary
import me.rerere.rikkahub.brainypal.shared.BrainyPalReviewOfferResponse

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
                    "BrainyPal 已准备好"
                } else {
                    "需要家长配置 BrainyPal"
                },
                chatAction = BrainyPalChildWorkbenchAction(
                    label = "找 BrainyPal 聊聊",
                    target = chatScreen,
                ),
                practiceAction = BrainyPalChildWorkbenchAction(
                    label = if (configured) practiceTasks.primaryPracticeActionLabel() else "配置连接",
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

        fun taskActionLabel(task: BrainyPalChildPracticeTaskSummary): String {
            return when (task.status) {
                "in_progress" -> "继续"
                "submitted", "reviewing", "completed" -> "看反馈"
                "expired" -> "已结束"
                else -> "开始"
            }
        }

        private fun List<BrainyPalChildPracticeTaskSummary>.primaryPracticeActionLabel(): String {
            return when {
                any { it.status == "in_progress" || it.needsMoreEffort } -> "继续今日任务"
                any { it.status in startableTaskStatuses } -> "开始今日任务"
                isNotEmpty() -> "查看今日任务"
                else -> "看看今日任务"
            }
        }

        private fun List<BrainyPalChildPracticeTaskSummary>.summaryText(): String {
            val activeTasks = filter { it.status in startableTaskStatuses }
            val feedbackCount = count { it.status in setOf("submitted", "reviewing") }
            return when {
                activeTasks.any { it.status == "in_progress" || it.needsMoreEffort } ->
                    "${activeTasks.size} 个任务可以继续"

                activeTasks.isNotEmpty() -> "${activeTasks.size} 个任务等你开始"
                feedbackCount > 0 -> "$feedbackCount 个任务等待反馈"
                isNotEmpty() -> "今天的任务都处理完了"
                else -> "现在没有新的今日任务"
            }
        }

        private val startableTaskStatuses = setOf(
            "pending",
            "assigned",
            "accepted",
            "in_progress",
            "paused",
        )
    }
}

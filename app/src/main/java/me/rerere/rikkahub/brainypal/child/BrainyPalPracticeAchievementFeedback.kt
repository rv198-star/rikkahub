package me.rerere.rikkahub.brainypal.child

import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskDetail

data class BrainyPalPracticeAchievementFeedbackModel(
    val sectionTitle: String,
    val headline: String,
    val body: String,
    val statusLine: String?,
)

object BrainyPalPracticeAchievementFeedback {
    private val visibleStatuses = setOf(
        "submitted",
        "reviewing",
        "completed",
        "done",
        "graded",
    )

    fun model(detail: BrainyPalChildPracticeTaskDetail): BrainyPalPracticeAchievementFeedbackModel? {
        if (detail.needsMoreEffort || detail.status !in visibleStatuses) {
            return null
        }
        val moment = detail.achievementMoment ?: return null
        val headline = moment.title.trim().ifBlank { "收到稳定信号" }
        val body = moment.body.trim().ifBlank { return null }
        return BrainyPalPracticeAchievementFeedbackModel(
            sectionTitle = "勇气空间站",
            headline = headline,
            body = body,
            statusLine = statusLine(detail, moment.status),
        )
    }

    private fun statusLine(detail: BrainyPalChildPracticeTaskDetail, momentStatus: String): String? {
        val dailyCount = detail.stationState?.daily?.visibleAcknowledgements ?: 0
        return when {
            dailyCount > 0 -> "今日已记录 $dailyCount 次稳定行动"
            momentStatus.isNotBlank() -> statusLabel(momentStatus)
            else -> null
        }
    }

    private fun statusLabel(status: String): String {
        return when (status) {
            "stable" -> "空间站状态稳定"
            "warming_up" -> "空间站正在升温"
            "needs_maintenance" -> "空间站等待下一次补给"
            else -> "空间站信号已记录"
        }
    }
}

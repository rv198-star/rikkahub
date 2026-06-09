package me.rerere.rikkahub.brainypal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface BrainyPalChildApi {
    @GET("/api/v1/child/practice-tasks")
    suspend fun listPracticeTasks(): BrainyPalChildPracticeTaskListResponse

    @GET("/api/v1/child/review-offer")
    suspend fun getReviewOffer(
        @Query("remaining_minutes") remainingMinutes: Int? = null,
    ): BrainyPalReviewOfferResponse
}

@Serializable
data class BrainyPalChildPracticeTaskListResponse(
    val items: List<BrainyPalChildPracticeTaskSummary> = emptyList(),
)

@Serializable
data class BrainyPalChildPracticeTaskSummary(
    @SerialName("task_id")
    val taskId: String,
    val title: String,
    @SerialName("task_type")
    val taskType: String,
    val status: String,
    @SerialName("item_count")
    val itemCount: Int,
    @SerialName("help_limit")
    val helpLimit: Int,
    @SerialName("help_used")
    val helpUsed: Int,
    @SerialName("due_at")
    val dueAt: String? = null,
    @SerialName("blank_or_low_effort")
    val blankOrLowEffort: Boolean = false,
) {
    val remainingHelp: Int
        get() = (helpLimit - helpUsed).coerceAtLeast(0)

    val needsMoreEffort: Boolean
        get() = blankOrLowEffort

    val statusLabel: String
        get() = when (status) {
            "assigned" -> "待开始"
            "in_progress" -> "进行中"
            "submitted" -> "已提交"
            "reviewing" -> "讲评中"
            "completed" -> "已完成"
            "expired" -> "已过期"
            else -> "待查看"
        }
}

@Serializable
data class BrainyPalReviewOfferResponse(
    @SerialName("should_offer")
    val shouldOffer: Boolean,
    @SerialName("child_message")
    val childMessage: String,
    val event: BrainyPalReviewOfferEvent? = null,
) {
    val isActionable: Boolean
        get() = shouldOffer && event != null
}

@Serializable
data class BrainyPalReviewOfferEvent(
    @SerialName("related_question_id")
    val relatedQuestionId: String,
    @SerialName("strategy_version_id")
    val strategyVersionId: String,
    @SerialName("evidence_refs")
    val evidenceRefs: List<String> = emptyList(),
)

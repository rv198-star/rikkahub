package me.rerere.rikkahub.brainypal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BrainyPalChildApi {
    @GET("/api/v1/child/practice-tasks")
    suspend fun listPracticeTasks(): BrainyPalChildPracticeTaskListResponse

    @GET("/api/v1/child/practice-tasks/{task_id}")
    suspend fun getPracticeTask(
        @Path("task_id") taskId: String,
    ): BrainyPalChildPracticeTaskDetail

    @PATCH("/api/v1/child/practice-tasks/{task_id}/items/{item_id}/answer")
    suspend fun recordPracticeTaskAnswer(
        @Path("task_id") taskId: String,
        @Path("item_id") itemId: String,
        @Body request: BrainyPalRecordPracticeTaskAnswerRequest,
    ): BrainyPalChildPracticeTaskDetail

    @POST("/api/v1/child/practice-tasks/{task_id}/help-requests")
    suspend fun requestPracticeTaskHelp(
        @Path("task_id") taskId: String,
        @Body request: BrainyPalRequestPracticeTaskHelpRequest,
    ): BrainyPalChildPracticeTaskDetail

    @POST("/api/v1/child/practice-tasks/{task_id}/submit")
    suspend fun submitPracticeTask(
        @Path("task_id") taskId: String,
        @Body request: BrainyPalSubmitPracticeTaskRequest = BrainyPalSubmitPracticeTaskRequest(),
    ): BrainyPalChildPracticeTaskDetail

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
        get() = practiceTaskStatusLabel(status)
}

@Serializable
data class BrainyPalChildPracticeTaskDetail(
    @SerialName("task_id")
    val taskId: String,
    val title: String,
    @SerialName("task_type")
    val taskType: String,
    val status: String,
    @SerialName("help_limit")
    val helpLimit: Int,
    @SerialName("help_used")
    val helpUsed: Int,
    @SerialName("help_message")
    val helpMessage: String? = null,
    val items: List<BrainyPalChildPracticeTaskItem> = emptyList(),
    @SerialName("due_at")
    val dueAt: String? = null,
    @SerialName("submitted_at")
    val submittedAt: String? = null,
    @SerialName("completed_at")
    val completedAt: String? = null,
    @SerialName("parent_summary")
    val parentSummary: String? = null,
    @SerialName("blank_or_low_effort")
    val blankOrLowEffort: Boolean = false,
    @SerialName("agent_policy_snapshot")
    val agentPolicySnapshot: BrainyPalPracticeTaskAgentPolicySnapshot = BrainyPalPracticeTaskAgentPolicySnapshot(),
) {
    val remainingHelp: Int
        get() = (helpLimit - helpUsed).coerceAtLeast(0)

    val needsMoreEffort: Boolean
        get() = blankOrLowEffort

    val statusLabel: String
        get() = practiceTaskStatusLabel(status)

    val canSubmit: Boolean
        get() = status in setOf("assigned", "in_progress")
}

@Serializable
data class BrainyPalChildPracticeTaskItem(
    @SerialName("item_id")
    val itemId: String,
    @SerialName("source_ref")
    val sourceRef: String? = null,
    val prompt: String,
    @SerialName("expected_answer")
    val expectedAnswer: String? = null,
    @SerialName("scoring_hint")
    val scoringHint: String? = null,
    @SerialName("child_answer")
    val childAnswer: String? = null,
    @SerialName("attempt_evidence")
    val attemptEvidence: String? = null,
    val result: String = "pending",
    @SerialName("correction_status")
    val correctionStatus: String = "not_started",
    @SerialName("blank_or_low_effort")
    val blankOrLowEffort: Boolean = false,
    @SerialName("correction_note")
    val correctionNote: String? = null,
) {
    val needsMoreEffort: Boolean
        get() = blankOrLowEffort
}

@Serializable
data class BrainyPalPracticeTaskAgentPolicySnapshot(
    @SerialName("task_phase")
    val taskPhase: String = "attempt",
    @SerialName("allowed_help_actions")
    val allowedHelpActions: List<String> = emptyList(),
    @SerialName("help_budget_state")
    val helpBudgetState: String = "available",
    @SerialName("attempt_evidence_status")
    val attemptEvidenceStatus: String = "not_submitted",
    @SerialName("post_submit_review_allowed")
    val postSubmitReviewAllowed: Boolean = false,
    @SerialName("blank_guard_threshold")
    val blankGuardThreshold: Float = 0.8f,
    @SerialName("strategy_version_id")
    val strategyVersionId: String? = null,
)

@Serializable
data class BrainyPalRecordPracticeTaskAnswerRequest(
    @SerialName("child_answer")
    val childAnswer: String? = null,
    @SerialName("attempt_evidence")
    val attemptEvidence: String? = null,
)

@Serializable
data class BrainyPalRequestPracticeTaskHelpRequest(
    @SerialName("item_id")
    val itemId: String? = null,
    @SerialName("requested_action")
    val requestedAction: String = "hint",
)

@Serializable
data class BrainyPalSubmitPracticeTaskRequest(
    @SerialName("submitted_at")
    val submittedAt: String? = null,
)

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

private fun practiceTaskStatusLabel(status: String): String {
    return when (status) {
        "assigned" -> "待开始"
        "in_progress" -> "进行中"
        "submitted" -> "已提交"
        "reviewing" -> "讲评中"
        "completed" -> "已完成"
        "expired" -> "已过期"
        else -> "待查看"
    }
}

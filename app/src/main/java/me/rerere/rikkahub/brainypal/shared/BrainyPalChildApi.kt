package me.rerere.rikkahub.brainypal.shared

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

    @POST("/api/v1/child/practice-tasks/{task_id}/handoff-codes")
    suspend fun createPracticeTaskHandoffCode(
        @Path("task_id") taskId: String,
        @Body request: BrainyPalCreatePracticeHandoffCodeRequest = BrainyPalCreatePracticeHandoffCodeRequest(),
    ): BrainyPalPracticeHandoffCodeResponse

    @POST("/api/v1/child/practice-tasks/{task_id}/accept")
    suspend fun acceptPracticeTask(
        @Path("task_id") taskId: String,
        @Body request: BrainyPalAcceptPracticeTaskRequest = BrainyPalAcceptPracticeTaskRequest(),
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
    ): BrainyPalPracticeHintResponse

    @POST("/api/v1/child/practice-tasks/{task_id}/submit")
    suspend fun submitPracticeTask(
        @Path("task_id") taskId: String,
        @Body request: BrainyPalSubmitPracticeTaskRequest = BrainyPalSubmitPracticeTaskRequest(),
    ): BrainyPalChildPracticeTaskDetail

    @POST("/api/v1/child/practice-tasks/{task_id}/dictation-ocr-evidence")
    suspend fun submitDictationOcrEvidence(
        @Path("task_id") taskId: String,
        @Body request: BrainyPalSubmitDictationOcrEvidenceRequest,
    ): BrainyPalChildPracticeTaskDetail

    @POST("/api/v1/child/practice-tasks/{task_id}/items/{item_id}/dictation-ocr-confirmation")
    suspend fun confirmDictationOcrEvidence(
        @Path("task_id") taskId: String,
        @Path("item_id") itemId: String,
        @Body request: BrainyPalConfirmDictationOcrEvidenceRequest,
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
data class BrainyPalCreatePracticeHandoffCodeRequest(
    val channel: String = "web",
    @SerialName("ttl_seconds")
    val ttlSeconds: Int = 900,
)

@Serializable
data class BrainyPalPracticeHandoffCodeResponse(
    @SerialName("task_id")
    val taskId: String,
    val channel: String,
    @SerialName("handoff_code")
    val handoffCode: String,
    @SerialName("expires_at")
    val expiresAt: String,
    @SerialName("join_path")
    val joinPath: String,
    @SerialName("join_url")
    val joinUrl: String,
)

@Serializable
data class BrainyPalChildPracticeTaskSummary(
    @SerialName("task_id")
    val taskId: String,
    val title: String,
    @SerialName("task_type")
    private val legacyTaskType: String? = null,
    val mode: String? = null,
    val status: String,
    @SerialName("item_count")
    private val legacyItemCount: Int? = null,
    @SerialName("total_items")
    val totalItems: Int? = null,
    @SerialName("answered_items")
    val answeredItems: Int = 0,
    @SerialName("help_limit")
    private val legacyHelpLimit: Int? = null,
    @SerialName("help_used")
    val helpUsed: Int = 0,
    @SerialName("remaining_help")
    private val remainingHelpValue: Int? = null,
    @SerialName("submit_available")
    val submitAvailable: Boolean = false,
    @SerialName("attempt_session_id")
    val attemptSessionId: String? = null,
    @SerialName("due_at")
    val dueAt: String? = null,
    @SerialName("blank_or_low_effort")
    val blankOrLowEffort: Boolean = false,
) {
    val taskType: String
        get() = mode ?: legacyTaskType ?: "practice"

    val itemCount: Int
        get() = totalItems ?: legacyItemCount ?: 0

    val helpLimit: Int
        get() = legacyHelpLimit ?: (helpUsed + (remainingHelpValue ?: 0))

    val remainingHelp: Int
        get() = remainingHelpValue ?: (helpLimit - helpUsed).coerceAtLeast(0)

    val needsMoreEffort: Boolean
        get() = blankOrLowEffort

    val statusLabel: String
        get() = practiceTaskStatusLabel(status)

    constructor(
        taskId: String,
        title: String,
        taskType: String,
        status: String,
        itemCount: Int,
        helpLimit: Int,
        helpUsed: Int,
        dueAt: String? = null,
        blankOrLowEffort: Boolean = false,
    ) : this(
        taskId = taskId,
        title = title,
        legacyTaskType = taskType,
        mode = null,
        status = status,
        legacyItemCount = itemCount,
        totalItems = null,
        answeredItems = 0,
        legacyHelpLimit = helpLimit,
        helpUsed = helpUsed,
        remainingHelpValue = null,
        submitAvailable = false,
        attemptSessionId = null,
        dueAt = dueAt,
        blankOrLowEffort = blankOrLowEffort,
    )
}

@Serializable
data class BrainyPalChildPracticeTaskDetail(
    @SerialName("task_id")
    private val rawTaskId: String? = null,
    @SerialName("attempt_session_id")
    val attemptSessionId: String? = null,
    val channel: String? = null,
    @SerialName("title")
    private val rawTitle: String = "",
    @SerialName("task_type")
    private val legacyTaskType: String? = null,
    val status: String,
    @SerialName("help_budget")
    private val helpBudget: Int? = null,
    @SerialName("help_limit")
    private val legacyHelpLimit: Int? = null,
    @SerialName("help_used")
    val helpUsed: Int,
    @SerialName("remaining_help")
    private val remainingHelpValue: Int? = null,
    @SerialName("total_items")
    val totalItems: Int? = null,
    @SerialName("answered_items")
    val answeredItems: Int = 0,
    @SerialName("submit_available")
    val submitAvailable: Boolean? = null,
    @SerialName("help_message")
    val helpMessage: String? = null,
    val task: BrainyPalChildPracticeTaskPayload? = null,
    @SerialName("task_spec")
    val taskSpec: BrainyPalAgentTaskSpec? = null,
    @SerialName("items")
    private val legacyItems: List<BrainyPalChildPracticeTaskItem> = emptyList(),
    val answers: Map<String, BrainyPalPracticeAttemptAnswer> = emptyMap(),
    val result: BrainyPalPracticeTaskResult? = null,
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
    val taskId: String
        get() = rawTaskId ?: task?.taskId.orEmpty()

    val title: String
        get() = task?.title ?: rawTitle

    val subject: String?
        get() = task?.subject

    val taskType: String
        get() = task?.mode ?: legacyTaskType ?: "practice"

    val helpLimit: Int
        get() = helpBudget ?: legacyHelpLimit ?: 0

    val remainingHelp: Int
        get() = remainingHelpValue ?: (helpLimit - helpUsed).coerceAtLeast(0)

    val items: List<BrainyPalChildPracticeTaskItem>
        get() = (task?.items ?: legacyItems).map { item ->
            val answer = answers[item.itemId]
            if (answer == null) {
                item
            } else {
                item.copy(
                    childAnswer = answer.value,
                    attemptEvidence = answer.source,
                )
            }
        }

    val needsMoreEffort: Boolean
        get() = blankOrLowEffort

    val statusLabel: String
        get() = practiceTaskStatusLabel(status)

    val taskKindLabel: String
        get() = practiceTaskKindLabel(taskType)

    val canSubmit: Boolean
        get() = submitAvailable ?: (status in setOf("pending", "assigned", "accepted", "in_progress", "paused"))

    val canEditAttempt: Boolean
        get() = status in setOf("accepted", "in_progress", "paused") && attemptSessionId != null

    constructor(
        taskId: String,
        title: String,
        taskType: String,
        status: String,
        helpLimit: Int,
        helpUsed: Int,
        helpMessage: String? = null,
        taskSpec: BrainyPalAgentTaskSpec? = null,
        items: List<BrainyPalChildPracticeTaskItem> = emptyList(),
        dueAt: String? = null,
        submittedAt: String? = null,
        completedAt: String? = null,
        parentSummary: String? = null,
        blankOrLowEffort: Boolean = false,
        agentPolicySnapshot: BrainyPalPracticeTaskAgentPolicySnapshot = BrainyPalPracticeTaskAgentPolicySnapshot(),
        answers: Map<String, BrainyPalPracticeAttemptAnswer> = emptyMap(),
        result: BrainyPalPracticeTaskResult? = null,
    ) : this(
        rawTaskId = taskId,
        attemptSessionId = null,
        channel = null,
        rawTitle = title,
        legacyTaskType = taskType,
        status = status,
        helpBudget = null,
        legacyHelpLimit = helpLimit,
        helpUsed = helpUsed,
        remainingHelpValue = null,
        totalItems = null,
        answeredItems = 0,
        submitAvailable = null,
        helpMessage = helpMessage,
        task = null,
        taskSpec = taskSpec,
        legacyItems = items,
        answers = answers,
        result = result,
        dueAt = dueAt,
        submittedAt = submittedAt,
        completedAt = completedAt,
        parentSummary = parentSummary,
        blankOrLowEffort = blankOrLowEffort,
        agentPolicySnapshot = agentPolicySnapshot,
    )
}

@Serializable
data class BrainyPalChildPracticeTaskPayload(
    @SerialName("task_id")
    val taskId: String,
    val title: String,
    val subject: String? = null,
    val mode: String = "practice",
    val instructions: String = "",
    val items: List<BrainyPalChildPracticeTaskItem> = emptyList(),
)

@Serializable
data class BrainyPalChildPracticeTaskItem(
    @SerialName("item_id")
    val itemId: String,
    @SerialName("source_ref")
    val sourceRef: String? = null,
    @SerialName("source_refs")
    val sourceRefs: List<String> = emptyList(),
    val kind: String = "short_answer",
    val prompt: String,
    val choices: List<String> = emptyList(),
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
    @SerialName("ocr_evidence")
    val ocrEvidence: BrainyPalDictationOcrEvidence? = null,
) {
    val needsMoreEffort: Boolean
        get() = blankOrLowEffort
}

@Serializable
data class BrainyPalDictationOcrBoundingBox(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
)

@Serializable
data class BrainyPalPracticeAttemptAnswer(
    @SerialName("item_id")
    val itemId: String,
    val value: String = "",
    val source: String = "app",
    @SerialName("updated_at")
    val updatedAt: String = "",
)

@Serializable
data class BrainyPalPracticeTaskResult(
    val status: String,
    @SerialName("child_summary")
    val childSummary: String = "",
    @SerialName("parent_summary")
    val parentSummary: String = "",
    @SerialName("item_results")
    val itemResults: Map<String, BrainyPalPracticeTaskItemResult> = emptyMap(),
    @SerialName("review_blocks")
    val reviewBlocks: List<BrainyPalPracticeReviewBlock> = emptyList(),
    @SerialName("learning_record")
    val learningRecord: BrainyPalPracticeLearningRecord? = null,
    @SerialName("created_at")
    val createdAt: String = "",
)

@Serializable
data class BrainyPalPracticeTaskItemResult(
    val status: String,
    @SerialName("child_feedback")
    val childFeedback: String = "",
    @SerialName("parent_note")
    val parentNote: String = "",
    @SerialName("evidence_source")
    val evidenceSource: String? = null,
    val confidence: Float? = null,
    @SerialName("correction_prompt")
    val correctionPrompt: String? = null,
    @SerialName("expected_answer")
    val expectedAnswer: String? = null,
    @SerialName("wrong_question_ref")
    val wrongQuestionRef: String? = null,
)

@Serializable
data class BrainyPalPracticeReviewBlock(
    val kind: String,
    val title: String,
    val body: String,
    @SerialName("item_ids")
    val itemIds: List<String> = emptyList(),
)

@Serializable
data class BrainyPalPracticeLearningRecord(
    @SerialName("record_type")
    val recordType: String,
    val subject: String? = null,
    @SerialName("source_refs")
    val sourceRefs: List<String> = emptyList(),
    @SerialName("child_summary")
    val childSummary: String = "",
    @SerialName("parent_summary")
    val parentSummary: String = "",
    @SerialName("knowledge_points")
    val knowledgePoints: List<String> = emptyList(),
    @SerialName("strategy_version_id")
    val strategyVersionId: String? = null,
)

@Serializable
data class BrainyPalDictationOcrEvidence(
    @SerialName("image_ref")
    val imageRef: String,
    @SerialName("raw_text")
    val rawText: String = "",
    @SerialName("recognized_text")
    val recognizedText: String = "",
    val confidence: Float? = null,
    @SerialName("bounding_box")
    val boundingBox: BrainyPalDictationOcrBoundingBox? = null,
    @SerialName("crop_ref")
    val cropRef: String? = null,
    @SerialName("confirmation_status")
    val confirmationStatus: String = "unconfirmed",
    @SerialName("error_attribution")
    val errorAttribution: String? = null,
)

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
data class BrainyPalAgentTaskSpec(
    val subject: String? = null,
    @SerialName("child_brief")
    val childBrief: String = "",
    @SerialName("material_refs")
    val materialRefs: List<String> = emptyList(),
    val steps: List<BrainyPalAgentTaskStep> = emptyList(),
    @SerialName("tts_policy")
    val ttsPolicy: BrainyPalTaskTtsPolicy = BrainyPalTaskTtsPolicy(),
    @SerialName("evidence_policy")
    val evidencePolicy: BrainyPalTaskEvidencePolicy = BrainyPalTaskEvidencePolicy(),
    val guardrails: BrainyPalTaskGuardrails = BrainyPalTaskGuardrails(),
    val priority: Int = 2,
)

@Serializable
data class BrainyPalAgentTaskStep(
    @SerialName("step_id")
    val stepId: String,
    val title: String,
    val action: String,
)

@Serializable
data class BrainyPalTaskTtsPolicy(
    @SerialName("language_mode")
    val languageMode: String = "general",
    @SerialName("repeat_count")
    val repeatCount: Int = 1,
    @SerialName("phrase_count")
    val phraseCount: Int? = null,
    @SerialName("interval_seconds")
    val intervalSeconds: Int = 3,
    @SerialName("slow_rate")
    val slowRate: Float = 0.85f,
)

@Serializable
data class BrainyPalTaskEvidencePolicy(
    @SerialName("requires_ocr")
    val requiresOcr: Boolean = false,
    @SerialName("preserve_image_evidence")
    val preserveImageEvidence: Boolean = false,
    @SerialName("manual_confirmation_required")
    val manualConfirmationRequired: String = "none",
    @SerialName("allow_source_region_review")
    val allowSourceRegionReview: Boolean = true,
)

@Serializable
data class BrainyPalTaskGuardrails(
    @SerialName("reveal_answer_before_submit")
    val revealAnswerBeforeSubmit: Boolean = false,
    @SerialName("allow_direct_answer")
    val allowDirectAnswer: Boolean = false,
    @SerialName("help_limit")
    val helpLimit: Int? = null,
    @SerialName("can_skip")
    val canSkip: Boolean = true,
)

@Serializable
data class BrainyPalAcceptPracticeTaskRequest(
    val channel: String = "app",
)

@Serializable
data class BrainyPalRecordPracticeTaskAnswerRequest(
    @SerialName("attempt_session_id")
    val attemptSessionId: String = "",
    val answer: String? = null,
    val source: String = "app",
    @SerialName("child_answer")
    val childAnswer: String? = null,
    @SerialName("attempt_evidence")
    val attemptEvidence: String? = null,
)

@Serializable
data class BrainyPalRequestPracticeTaskHelpRequest(
    @SerialName("attempt_session_id")
    val attemptSessionId: String = "",
    @SerialName("item_id")
    val itemId: String? = null,
    @SerialName("requested_action")
    val requestedAction: String = "hint",
)

@Serializable
data class BrainyPalSubmitPracticeTaskRequest(
    @SerialName("attempt_session_id")
    val attemptSessionId: String = "",
    @SerialName("submitted_at")
    val submittedAt: String? = null,
)

@Serializable
data class BrainyPalPracticeHintResponse(
    @SerialName("item_id")
    val itemId: String,
    val hint: String = "",
    @SerialName("help_used")
    val helpUsed: Int = 0,
    @SerialName("remaining_help")
    val remainingHelp: Int = 0,
    @SerialName("waiting_label")
    val waitingLabel: String = "",
)

@Serializable
data class BrainyPalSubmitDictationOcrEvidenceItemRequest(
    @SerialName("item_id")
    val itemId: String,
    @SerialName("recognized_text")
    val recognizedText: String = "",
    val confidence: Float? = null,
    @SerialName("bounding_box")
    val boundingBox: BrainyPalDictationOcrBoundingBox? = null,
    @SerialName("crop_ref")
    val cropRef: String? = null,
)

@Serializable
data class BrainyPalSubmitDictationOcrEvidenceRequest(
    @SerialName("image_ref")
    val imageRef: String,
    @SerialName("recognized_text")
    val recognizedText: String = "",
    val items: List<BrainyPalSubmitDictationOcrEvidenceItemRequest> = emptyList(),
)

@Serializable
data class BrainyPalConfirmDictationOcrEvidenceRequest(
    val confirmation: String,
    val note: String? = null,
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
        "pending" -> "待领取"
        "assigned" -> "待开始"
        "accepted" -> "已领取"
        "in_progress" -> "进行中"
        "paused" -> "已暂停"
        "submitted" -> "已提交"
        "reviewing" -> "讲评中"
        "completed" -> "已完成"
        "skipped" -> "已跳过"
        "expired" -> "已过期"
        else -> "待查看"
    }
}

fun practiceTaskKindLabel(taskType: String): String {
    return when (taskType) {
        "dictation" -> "听写任务"
        "reading" -> "朗读任务"
        "recitation" -> "背诵任务"
        "knowledge_review" -> "复习任务"
        "wrong_question_practice" -> "错题任务"
        else -> "练习任务"
    }
}

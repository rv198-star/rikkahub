package me.rerere.rikkahub.brainypal.shared

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.rerere.rikkahub.utils.JsonInstant
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.Part
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BrainyPalParentApi {
    @GET("/api/v1/parent/task-workbench")
    suspend fun getTaskWorkbench(): BrainyPalParentTaskWorkbenchResponse

    @POST("/api/v1/parent/materials/import-text")
    suspend fun importTextMaterial(
        @Body request: BrainyPalImportTextMaterialRequest,
    ): BrainyPalParentMaterial

    @POST("/api/v1/parent/materials/search-web")
    suspend fun searchWebMaterials(
        @Body request: BrainyPalParentWebMaterialSearchRequest,
    ): BrainyPalParentWebMaterialSearchResponse

    @POST("/api/v1/parent/chat-triggers")
    suspend fun createChatTrigger(
        @Body request: BrainyPalParentChatTriggerRequest,
    ): BrainyPalParentChatTriggerResponse

    @Multipart
    @POST("/api/v1/parent/photo-scans")
    suspend fun createPhotoScan(
        @Part file: MultipartBody.Part,
    ): BrainyPalParentPhotoScanSnapshot

    @GET("/api/v1/parent/photo-scans/{scan_id}")
    suspend fun getPhotoScan(
        @Path("scan_id") scanId: String,
    ): BrainyPalParentPhotoScanSnapshot

    @POST("/api/v1/parent/photo-scans/{scan_id}/confirm")
    suspend fun confirmPhotoScan(
        @Path("scan_id") scanId: String,
        @Body request: BrainyPalConfirmPhotoScanRequest,
    ): BrainyPalConfirmPhotoScanResponse

    @POST("/api/v1/parent/import-sessions")
    suspend fun createImportSession(
        @Body request: BrainyPalCreateParentImportSessionRequest,
    ): BrainyPalParentImportSession

    @POST("/api/v1/parent/import-sessions/{session_id}/pending-task")
    suspend fun createPendingTaskFromImportSession(
        @Path("session_id") sessionId: String,
        @Body request: BrainyPalCreatePendingTaskFromImportSessionRequest = BrainyPalCreatePendingTaskFromImportSessionRequest(),
    ): BrainyPalParentPracticeTaskView

    @POST("/api/v1/parent/pending-tasks/{task_id}/send")
    suspend fun sendPendingTask(
        @Path("task_id") taskId: String,
        @Body request: BrainyPalSendPendingTaskRequest = BrainyPalSendPendingTaskRequest(),
    ): BrainyPalParentPracticeTaskView

    @PATCH("/api/v1/parent/pending-tasks/{task_id}")
    suspend fun updatePendingTask(
        @Path("task_id") taskId: String,
        @Body request: BrainyPalUpdatePendingTaskRequest,
    ): BrainyPalParentPracticeTaskView

    @POST("/api/v1/parent/pending-tasks/{task_id}/archive")
    suspend fun archivePendingTask(
        @Path("task_id") taskId: String,
    ): BrainyPalParentPracticeTaskView

    @DELETE("/api/v1/parent/pending-tasks/{task_id}")
    suspend fun deletePendingTask(
        @Path("task_id") taskId: String,
    )

    @POST("/api/v1/parent/materials/{material_id}/confirm")
    suspend fun confirmMaterial(
        @Path("material_id") materialId: String,
        @Body request: BrainyPalConfirmParentMaterialRequest = BrainyPalConfirmParentMaterialRequest(),
    ): BrainyPalParentMaterial

    @POST("/api/v1/parent/materials/{material_id}/tasks")
    suspend fun createTaskFromMaterial(
        @Path("material_id") materialId: String,
        @Body request: BrainyPalCreateTaskFromMaterialRequest = BrainyPalCreateTaskFromMaterialRequest(),
    ): BrainyPalChildPracticeTaskDetail

    @GET("/api/v1/parent/practice-tasks")
    suspend fun listPracticeTasks(): BrainyPalParentPracticeTaskListResponse

    @POST("/api/v1/parent/practice-tasks")
    suspend fun createUnifiedPracticeTask(
        @Body request: BrainyPalCreateParentPracticeTaskRequest,
    ): BrainyPalParentPracticeTaskView

    @GET("/api/v1/parent/practice-tasks/summary")
    suspend fun getUnifiedPracticeTaskSummary(): BrainyPalParentPracticeTaskSummaryBoardResponse

    @GET("/api/v1/parent/practice-tasks/{task_id}")
    suspend fun getPracticeTask(
        @Path("task_id") taskId: String,
    ): BrainyPalChildPracticeTaskDetail

    @GET("/api/v1/parent/practice-tasks/{task_id}/summary")
    suspend fun getPracticeTaskSummary(
        @Path("task_id") taskId: String,
    ): BrainyPalParentPracticeTaskSummaryResponse

    @GET("/api/v1/parent/practice-tasks/{task_id}/result")
    suspend fun getPracticeTaskResult(
        @Path("task_id") taskId: String,
    ): BrainyPalParentPracticeTaskResultDetailResponse

    @GET("/api/v1/parent/learning-records/summary")
    suspend fun getLearningRecordsSummary(
        @Query("limit") limit: Int = 20,
    ): BrainyPalParentLearningRecordsSummaryResponse

    @GET("/api/v1/parent/strategies")
    suspend fun listStrategies(): BrainyPalListStrategiesResponse

    @POST("/api/v1/parent/strategies")
    suspend fun createStrategy(
        @Body request: BrainyPalCreateStrategyRequest,
    ): BrainyPalCreateStrategyResponse

    @POST("/api/v1/parent/strategies/{version_id}/activate")
    suspend fun activateStrategy(
        @Path("version_id") versionId: String,
    ): BrainyPalStrategyVersion

    @POST("/api/v1/parent/strategies/{version_id}/pause")
    suspend fun pauseStrategy(
        @Path("version_id") versionId: String,
    ): BrainyPalStrategyVersion

    @POST("/api/v1/parent/practice-tasks/dictation")
    suspend fun createDictationPracticeTask(
        @Body request: BrainyPalCreateDictationPracticeTaskRequest,
    ): BrainyPalChildPracticeTaskDetail

    @POST("/api/v1/parent/practice-tasks/from-wrong-questions")
    suspend fun createPracticeTaskFromWrongQuestions(
        @Body request: BrainyPalCreatePracticeTaskFromWrongQuestionsRequest,
    ): BrainyPalChildPracticeTaskDetail

    @GET("/api/v1/parent/wrong-questions/due-reviews")
    suspend fun listDueWrongQuestionReviews(
        @Query("limit") limit: Int = 20,
        @Query("include_future") includeFuture: Boolean = false,
    ): BrainyPalDueWrongQuestionReviewsResponse

    @POST("/api/v1/child/practice-tasks/{task_id}/items/{item_id}/dictation-ocr-confirmation")
    suspend fun confirmDictationOcrEvidence(
        @Path("task_id") taskId: String,
        @Path("item_id") itemId: String,
        @Body request: BrainyPalConfirmDictationOcrEvidenceRequest,
    ): BrainyPalChildPracticeTaskDetail
}

@Serializable
data class BrainyPalParentPracticeTaskListResponse(
    val items: List<BrainyPalChildPracticeTaskDetail> = emptyList(),
)

@Serializable
data class BrainyPalParentPracticeEvidenceBoundingBoxView(
    val page: Int = 1,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
)

@Serializable
data class BrainyPalParentPracticeOcrEvidenceView(
    @SerialName("evidence_id")
    val evidenceId: String,
    @SerialName("source_image_ref")
    val sourceImageRef: String,
    @SerialName("recognized_answer")
    val recognizedAnswer: String,
    val confidence: Float? = null,
    @SerialName("bounding_box")
    val boundingBox: BrainyPalParentPracticeEvidenceBoundingBoxView? = null,
)

@Serializable
data class BrainyPalParentPracticeOralEvidenceView(
    @SerialName("evidence_id")
    val evidenceId: String,
    @SerialName("self_rating")
    val selfRating: Int,
    @SerialName("reread_count")
    val rereadCount: Int = 0,
    @SerialName("stuck_points")
    val stuckPoints: List<String> = emptyList(),
    @SerialName("audio_ref")
    val audioRef: String? = null,
    @SerialName("text_hidden_during_attempt")
    val textHiddenDuringAttempt: Boolean = false,
)

@Serializable
data class BrainyPalParentPracticeItemEvidenceView(
    @SerialName("answer_value")
    val answerValue: String? = null,
    @SerialName("answer_source")
    val answerSource: String? = null,
    @SerialName("ocr_evidence")
    val ocrEvidence: List<BrainyPalParentPracticeOcrEvidenceView> = emptyList(),
    @SerialName("oral_evidence")
    val oralEvidence: BrainyPalParentPracticeOralEvidenceView? = null,
)

@Serializable
data class BrainyPalParentPracticeResultItemView(
    @SerialName("item_id")
    val itemId: String,
    val prompt: String,
    val kind: String,
    @SerialName("result_status")
    val resultStatus: String,
    @SerialName("parent_note")
    val parentNote: String = "",
    @SerialName("correction_prompt")
    val correctionPrompt: String? = null,
    @SerialName("expected_answer")
    val expectedAnswer: String? = null,
    @SerialName("wrong_question_ref")
    val wrongQuestionRef: String? = null,
    val evidence: BrainyPalParentPracticeItemEvidenceView = BrainyPalParentPracticeItemEvidenceView(),
)

@Serializable
data class BrainyPalParentPracticeNextActionView(
    val kind: String,
    val title: String,
    val body: String,
    @SerialName("item_ids")
    val itemIds: List<String> = emptyList(),
)

@Serializable
data class BrainyPalParentPracticeTaskResultDetailResponse(
    @SerialName("task_id")
    val taskId: String,
    val title: String,
    val subject: String? = null,
    val mode: String = "practice",
    val status: String,
    @SerialName("parent_summary")
    val parentSummary: String,
    @SerialName("result_status")
    val resultStatus: String,
    val items: List<BrainyPalParentPracticeResultItemView> = emptyList(),
    @SerialName("next_actions")
    val nextActions: List<BrainyPalParentPracticeNextActionView> = emptyList(),
)

@Serializable
data class BrainyPalParentLearningRecordSummaryView(
    @SerialName("record_id")
    val recordId: String,
    @SerialName("record_type")
    val recordType: String,
    val subject: String? = null,
    @SerialName("captured_at")
    val capturedAt: String,
    @SerialName("source_refs")
    val sourceRefs: List<String> = emptyList(),
    @SerialName("knowledge_points")
    val knowledgePoints: List<String> = emptyList(),
    @SerialName("parent_summary")
    val parentSummary: String,
    @SerialName("strategy_version_id")
    val strategyVersionId: String? = null,
    @SerialName("wiki_path")
    val wikiPath: String,
)

@Serializable
data class BrainyPalParentLearningRecordsSummaryResponse(
    @SerialName("total_count")
    val totalCount: Int = 0,
    @SerialName("record_type_counts")
    val recordTypeCounts: Map<String, Int> = emptyMap(),
    @SerialName("knowledge_points")
    val knowledgePoints: List<String> = emptyList(),
    @SerialName("latest_records")
    val latestRecords: List<BrainyPalParentLearningRecordSummaryView> = emptyList(),
)

@Serializable
data class BrainyPalCreateStrategyRequest(
    @SerialName("parent_goal_text")
    val parentGoalText: String,
    @SerialName("evidence_refs")
    val evidenceRefs: List<String>,
    @SerialName("remaining_minutes")
    val remainingMinutes: Int? = null,
    @SerialName("mood_signal")
    val moodSignal: String? = null,
)

@Serializable
data class BrainyPalChildGuidancePlan(
    val intensity: String = "",
    @SerialName("child_message")
    val childMessage: String = "",
    @SerialName("suggested_action")
    val suggestedAction: String = "",
    @SerialName("opt_out_allowed")
    val optOutAllowed: Boolean = true,
)

@Serializable
data class BrainyPalStrategyVersion(
    @SerialName("version_id")
    val versionId: String,
    val scope: String,
    val status: String,
    @SerialName("parent_goal_text")
    val parentGoalText: String,
    @SerialName("child_facing_goal")
    val childFacingGoal: String,
    val rationale: String,
    @SerialName("evidence_refs")
    val evidenceRefs: List<String> = emptyList(),
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("active_from")
    val activeFrom: String,
    @SerialName("active_until")
    val activeUntil: String,
    @SerialName("parent_confirmed_at")
    val parentConfirmedAt: String? = null,
    @SerialName("retired_at")
    val retiredAt: String? = null,
)

@Serializable
data class BrainyPalCreateStrategyResponse(
    val status: String,
    val strategy: BrainyPalStrategyVersion,
    @SerialName("child_plan")
    val childPlan: BrainyPalChildGuidancePlan = BrainyPalChildGuidancePlan(),
    @SerialName("parent_message")
    val parentMessage: String = "",
    @SerialName("filtered_terms")
    val filteredTerms: List<String> = emptyList(),
)

@Serializable
data class BrainyPalListStrategiesResponse(
    val items: List<BrainyPalStrategyVersion> = emptyList(),
)

@Serializable
data class BrainyPalCreateParentPracticeTaskRequest(
    val title: String,
    val subject: String? = null,
    val mode: String = "practice",
    val instructions: String = "",
    @SerialName("help_budget")
    val helpBudget: Int = 2,
    @SerialName("source_refs")
    val sourceRefs: List<String> = emptyList(),
    val activate: Boolean = true,
    val items: List<BrainyPalParentPracticeTaskItemRequest> = emptyList(),
)

@Serializable
data class BrainyPalParentPracticeTaskItemRequest(
    @SerialName("item_id")
    val itemId: String,
    val kind: String = "short_answer",
    val prompt: String,
    @SerialName("expected_answer")
    val expectedAnswer: String? = null,
    val choices: List<String> = emptyList(),
    val explanation: String? = null,
    @SerialName("source_refs")
    val sourceRefs: List<String> = emptyList(),
)

@Serializable
data class BrainyPalParentPracticeTaskView(
    @SerialName("task_id")
    val taskId: String,
    val title: String,
    val subject: String? = null,
    val mode: String = "practice",
    val status: String = "draft",
    @SerialName("parent_status_label")
    val parentStatusLabel: String? = null,
    @SerialName("source_refs")
    val sourceRefs: List<String> = emptyList(),
    @SerialName("total_items")
    val totalItems: Int = 0,
    @SerialName("child_visible")
    val childVisible: Boolean = false,
) {
    val kindLabel: String
        get() = practiceTaskKindLabel(mode)

    val statusLabel: String
        get() = parentStatusLabel ?: when (status) {
            "draft" -> "待发任务"
            "active" -> "已下发"
            "completed" -> "待复盘"
            else -> "待处理"
        }
}

@Serializable
data class BrainyPalParentPracticeTaskSummaryBoardResponse(
    @SerialName("draft_count")
    val draftCount: Int = 0,
    @SerialName("active_count")
    val activeCount: Int = 0,
    @SerialName("completed_count")
    val completedCount: Int = 0,
    @SerialName("total_count")
    val totalCount: Int = 0,
    @SerialName("latest_tasks")
    val latestTasks: List<BrainyPalParentPracticeTaskView> = emptyList(),
)

@Serializable
data class BrainyPalParentTaskWorkbenchResponse(
    @SerialName("draft_materials")
    val draftMaterials: List<BrainyPalParentMaterial> = emptyList(),
    @SerialName("confirmed_materials")
    val confirmedMaterials: List<BrainyPalParentMaterial> = emptyList(),
    @SerialName("pending_tasks")
    val pendingTasks: List<BrainyPalParentPracticeTaskView> = emptyList(),
    @SerialName("recent_tasks")
    val recentTasks: List<BrainyPalChildPracticeTaskDetail> = emptyList(),
    val counts: Map<String, Int> = emptyMap(),
)

@Serializable
data class BrainyPalCreateParentImportSessionRequest(
    @SerialName("entry_goal")
    val entryGoal: String,
    @SerialName("input_mode")
    val inputMode: String = "paste",
    @SerialName("default_use")
    val defaultUse: String = "prepare_task",
    val title: String,
    val subject: String? = null,
    @SerialName("raw_text")
    val rawText: String,
    @SerialName("source_refs")
    val sourceRefs: List<String> = emptyList(),
)

@Serializable
data class BrainyPalParentWebMaterialSearchRequest(
    val query: String,
    val subject: String? = null,
    @SerialName("grade_band")
    val gradeBand: String? = null,
    val language: String = "zh-CN",
    @SerialName("source_set")
    val sourceSet: String = "public_domain",
    @SerialName("max_candidates")
    val maxCandidates: Int = 3,
)

@Serializable
data class BrainyPalParentWebMaterialSearchResponse(
    val query: String,
    val items: List<BrainyPalParentMaterial> = emptyList(),
)

@Serializable
data class BrainyPalParentWebMaterialSource(
    @SerialName("source_url")
    val sourceUrl: String,
    val title: String,
    @SerialName("source_type")
    val sourceType: String,
    val snippet: String,
    @SerialName("uncertainty_note")
    val uncertaintyNote: String,
)

@Serializable
data class BrainyPalParentChatTriggerRequest(
    val message: String,
    val title: String? = null,
    val subject: String? = null,
)

@Serializable
data class BrainyPalParentChatStructuredAction(
    val type: String,
    val label: String,
    @SerialName("requires_confirmation")
    val requiresConfirmation: Boolean,
)

@Serializable
data class BrainyPalParentChatStatusSummary(
    @SerialName("privacy_level")
    val privacyLevel: String = "task_summary_only",
    val headline: String,
    val metrics: Map<String, Int> = emptyMap(),
    @SerialName("learning_blockers")
    val learningBlockers: List<String> = emptyList(),
    @SerialName("evidence_scope")
    val evidenceScope: String = "task_related_summary",
)

@Serializable
data class BrainyPalParentChatStrategyCandidate(
    val status: String = "needs_confirmation",
    @SerialName("parent_goal_text")
    val parentGoalText: String,
    @SerialName("allowed_effects")
    val allowedEffects: List<String> = emptyList(),
    @SerialName("child_answer_policy")
    val childAnswerPolicy: String = "no_final_answers_before_submission",
    @SerialName("confirmation_label")
    val confirmationLabel: String = "确认应用策略",
)

@Serializable
data class BrainyPalParentChatTriggerResponse(
    val intent: String,
    @SerialName("requires_confirmation")
    val requiresConfirmation: Boolean,
    @SerialName("structured_action")
    val structuredAction: BrainyPalParentChatStructuredAction? = null,
    @SerialName("import_session")
    val importSession: BrainyPalParentImportSession? = null,
    @SerialName("status_summary")
    val statusSummary: BrainyPalParentChatStatusSummary? = null,
    @SerialName("strategy_candidate")
    val strategyCandidate: BrainyPalParentChatStrategyCandidate? = null,
    val message: String,
)

@Serializable
data class BrainyPalParentPhotoScanSnapshot(
    @SerialName("scan_id")
    val scanId: String,
    @SerialName("captured_at")
    val capturedAt: String,
    val candidates: List<BrainyPalParentPhotoScanCandidate> = emptyList(),
)

@Serializable
data class BrainyPalParentPhotoScanCandidate(
    @SerialName("candidate_id")
    val candidateId: String,
    @SerialName("question_number")
    val questionNumber: String? = null,
    @SerialName("question_text")
    val questionText: String,
    @SerialName("child_answer")
    val childAnswer: String? = null,
    @SerialName("work_observed")
    val workObserved: String? = null,
    val status: String,
    val recommendation: String,
    val confidence: Float,
    val verification: BrainyPalParentPhotoScanVerification? = null,
)

@Serializable
data class BrainyPalParentPhotoScanVerification(
    @SerialName("reference_answer")
    val referenceAnswer: String? = null,
    val judgement: String,
    val explanation: String,
    val confidence: Float,
    @SerialName("requires_parent_review")
    val requiresParentReview: Boolean = false,
)

@Serializable
data class BrainyPalConfirmPhotoScanRequest(
    @SerialName("candidate_ids")
    val candidateIds: List<String>? = null,
    val candidates: List<BrainyPalConfirmPhotoScanCandidateRequest>? = null,
    @SerialName("parent_note")
    val parentNote: String? = null,
)

@Serializable
data class BrainyPalConfirmPhotoScanCandidateRequest(
    @SerialName("candidate_id")
    val candidateId: String,
    @SerialName("question_text")
    val questionText: String? = null,
    @SerialName("child_answer")
    val childAnswer: String? = null,
    @SerialName("work_observed")
    val workObserved: String? = null,
    val status: String? = null,
)

@Serializable
data class BrainyPalConfirmPhotoScanResponse(
    @SerialName("scan_id")
    val scanId: String,
    @SerialName("written_questions")
    val writtenQuestions: List<BrainyPalWrittenQuestionResponse> = emptyList(),
    @SerialName("skipped_candidates")
    val skippedCandidates: List<String> = emptyList(),
)

@Serializable
data class BrainyPalWrittenQuestionResponse(
    @SerialName("candidate_id")
    val candidateId: String,
    @SerialName("question_id")
    val questionId: String,
    @SerialName("wiki_path")
    val wikiPath: String,
)

@Serializable
data class BrainyPalCreatePendingTaskFromImportSessionRequest(
    @SerialName("help_limit")
    val helpLimit: Int = 3,
)

@Serializable
data class BrainyPalSendPendingTaskRequest(
    @SerialName("confirm_overload")
    val confirmOverload: Boolean = false,
)

@Serializable
data class BrainyPalUpdatePendingTaskRequest(
    val title: String? = null,
    val instructions: String? = null,
)

@Serializable
data class BrainyPalParentWorkloadGuardConflict(
    val message: String = "",
    @SerialName("active_tasks")
    val activeTasks: Int = 0,
    @SerialName("estimated_minutes")
    val estimatedMinutes: Int = 0,
    @SerialName("active_task_warning_limit")
    val activeTaskWarningLimit: Int = 0,
    @SerialName("estimated_minutes_warning_limit")
    val estimatedMinutesWarningLimit: Int = 0,
) {
    companion object {
        fun fromErrorBody(body: String?): BrainyPalParentWorkloadGuardConflict? {
            if (body.isNullOrBlank()) return null
            return runCatching {
                val envelope = JsonInstant.decodeFromString<BrainyPalParentApiErrorEnvelope>(body)
                val error = envelope.error ?: return null
                if (error.code != "workload_guard_requires_confirmation") return null
                error.details.copy(
                    message = error.details.message.ifBlank { error.message },
                )
            }.getOrNull()
        }
    }
}

@Serializable
private data class BrainyPalParentApiErrorEnvelope(
    val error: BrainyPalParentApiError? = null,
)

@Serializable
private data class BrainyPalParentApiError(
    val code: String = "",
    val message: String = "",
    val details: BrainyPalParentWorkloadGuardConflict = BrainyPalParentWorkloadGuardConflict(),
)

@Serializable
data class BrainyPalParentImportSession(
    @SerialName("session_id")
    val sessionId: String,
    val status: String = "needs_confirmation",
    @SerialName("entry_goal")
    val entryGoal: String,
    @SerialName("input_mode")
    val inputMode: String = "paste",
    @SerialName("default_use")
    val defaultUse: String = "prepare_task",
    val title: String,
    val subject: String? = null,
    @SerialName("raw_text")
    val rawText: String = "",
    @SerialName("source_refs")
    val sourceRefs: List<String> = emptyList(),
    @SerialName("risk_flags")
    val riskFlags: List<String> = emptyList(),
    val candidates: List<BrainyPalParentImportSessionCandidate> = emptyList(),
    val preview: BrainyPalParentImportSessionPreview = BrainyPalParentImportSessionPreview(),
    @SerialName("pending_task_id")
    val pendingTaskId: String? = null,
)

@Serializable
data class BrainyPalParentImportSessionCandidate(
    @SerialName("candidate_id")
    val candidateId: String,
    val kind: String,
    val prompt: String,
    @SerialName("ai_reference_answer")
    val aiReferenceAnswer: String? = null,
    @SerialName("ai_explanation")
    val aiExplanation: String? = null,
    @SerialName("source_answer")
    val sourceAnswer: String? = null,
    val confidence: Float? = null,
    @SerialName("risk_flags")
    val riskFlags: List<String> = emptyList(),
    @SerialName("source_refs")
    val sourceRefs: List<String> = emptyList(),
)

@Serializable
data class BrainyPalParentImportSessionPreview(
    @SerialName("task_type")
    val taskType: String = "practice",
    @SerialName("child_mode")
    val childMode: String = "app",
    @SerialName("requires_ocr_return")
    val requiresOcrReturn: Boolean = false,
    @SerialName("estimated_minutes")
    val estimatedMinutes: Int = 10,
    @SerialName("send_label")
    val sendLabel: String = "保存为待发任务",
)

@Serializable
data class BrainyPalImportTextMaterialRequest(
    @SerialName("raw_text")
    val rawText: String,
    val subject: String? = null,
    @SerialName("grade_band")
    val gradeBand: String? = null,
    val language: String? = null,
    val title: String? = null,
)

@Serializable
data class BrainyPalConfirmParentMaterialRequest(
    val title: String? = null,
    val items: List<BrainyPalParentMaterialItem>? = null,
    @SerialName("parent_note")
    val parentNote: String? = null,
)

@Serializable
data class BrainyPalCreateTaskFromMaterialRequest(
    @SerialName("task_type")
    val taskType: String? = null,
    val title: String? = null,
    @SerialName("help_limit")
    val helpLimit: Int = 3,
    @SerialName("due_at")
    val dueAt: String? = null,
    val activate: Boolean = true,
)

@Serializable
data class BrainyPalParentMaterial(
    @SerialName("material_id")
    val materialId: String,
    val status: String = "draft",
    @SerialName("input_mode")
    val inputMode: String = "paste_text",
    @SerialName("material_type")
    val materialType: String,
    val subject: String? = null,
    @SerialName("grade_band")
    val gradeBand: String? = null,
    val language: String = "zh-CN",
    val title: String,
    @SerialName("raw_text")
    val rawText: String? = null,
    val items: List<BrainyPalParentMaterialItem> = emptyList(),
    val sentences: List<String> = emptyList(),
    @SerialName("suggested_questions")
    val suggestedQuestions: List<String> = emptyList(),
    @SerialName("candidate_task_types")
    val candidateTaskTypes: List<String> = emptyList(),
    @SerialName("source_refs")
    val sourceRefs: List<String> = emptyList(),
    @SerialName("source_candidates")
    val sourceCandidates: List<BrainyPalParentWebMaterialSource> = emptyList(),
    @SerialName("search_query")
    val searchQuery: String? = null,
    val confidence: Float? = null,
    @SerialName("uncertainty_note")
    val uncertaintyNote: String? = null,
    @SerialName("confirm_url")
    val confirmUrl: String? = null,
    @SerialName("requires_parent_confirmation")
    val requiresParentConfirmation: Boolean = true,
    @SerialName("parent_note")
    val parentNote: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
) {
    val statusLabel: String
        get() = when (status) {
            "confirmed" -> "已确认"
            "archived" -> "已归档"
            else -> "待确认"
        }

    val typeLabel: String
        get() = when (materialType) {
            "dictation" -> "听写材料"
            "reading_passage" -> "阅读材料"
            "recitation_passage" -> "背诵材料"
            "practice_set", "practice_set_candidates" -> "练习题"
            "wrong_question_candidates" -> "错题材料"
            else -> "学习材料"
        }

    val previewText: String
        get() = items.joinToString("、") { it.text }.ifBlank { rawText.orEmpty() }
}

@Serializable
data class BrainyPalParentMaterialItem(
    @SerialName("item_id")
    val itemId: String,
    val text: String,
    @SerialName("expected_answer")
    val expectedAnswer: String? = null,
    @SerialName("question_number")
    val questionNumber: String? = null,
    @SerialName("knowledge_hint")
    val knowledgeHint: String? = null,
    @SerialName("source_ref")
    val sourceRef: String? = null,
    @SerialName("source_region")
    val sourceRegion: BrainyPalParentMaterialSourceRegion? = null,
)

@Serializable
data class BrainyPalParentMaterialSourceRegion(
    @SerialName("image_ref")
    val imageRef: String,
    @SerialName("bounding_box")
    val boundingBox: BrainyPalDictationOcrBoundingBox? = null,
    @SerialName("crop_ref")
    val cropRef: String? = null,
    @SerialName("raw_text")
    val rawText: String? = null,
)

data class BrainyPalParentWorkbench(
    val materialSummary: String,
    val taskSummary: String,
    val chatEntryLabel: String,
    val structuredEntryLabel: String,
    val needsMaterialConfirmation: Boolean,
    val primaryEntryKind: String = "structured",
    val primaryEntryLabel: String = structuredEntryLabel,
    val secondaryChatLabel: String = chatEntryLabel,
) {
    companion object {
        fun from(response: BrainyPalParentTaskWorkbenchResponse): BrainyPalParentWorkbench {
            val draftCount = response.draftMaterials.size
            val confirmedCount = response.confirmedMaterials.size
            val pendingTaskCount = response.pendingTasks.size
            val taskCount = response.recentTasks.size
            return BrainyPalParentWorkbench(
                materialSummary = when {
                    draftCount > 0 -> "$draftCount 份材料待确认"
                    confirmedCount > 0 -> "$confirmedCount 份材料可复用"
                    else -> "还没有导入材料"
                },
                taskSummary = when {
                    pendingTaskCount > 0 -> "$pendingTaskCount 个待发任务"
                    taskCount > 0 -> "$taskCount 个任务正在追踪"
                    else -> "还没有下发任务"
                },
                chatEntryLabel = "简单说一下需求",
                structuredEntryLabel = "导入作业材料",
                needsMaterialConfirmation = draftCount > 0,
                primaryEntryKind = "structured",
                primaryEntryLabel = "导入作业材料",
                secondaryChatLabel = "简单说一下需求",
            )
        }
    }
}

@Serializable
data class BrainyPalCreateDictationPracticeTaskRequest(
    val entries: List<String>,
    val title: String = "今日听写",
    @SerialName("help_limit")
    val helpLimit: Int = 3,
    @SerialName("due_at")
    val dueAt: String? = null,
)

@Serializable
data class BrainyPalCreatePracticeTaskFromWrongQuestionsRequest(
    @SerialName("question_ids")
    val questionIds: List<String>,
    val title: String = "今日错题练习",
    @SerialName("help_limit")
    val helpLimit: Int = 3,
    @SerialName("due_at")
    val dueAt: String? = null,
)

@Serializable
data class BrainyPalDueWrongQuestionReviewsResponse(
    val items: List<BrainyPalDueWrongQuestionReviewItem> = emptyList(),
)

@Serializable
data class BrainyPalDueWrongQuestionReviewItem(
    @SerialName("question_id")
    val questionId: String,
    val subject: String? = null,
    @SerialName("question_text")
    val questionText: String = "",
    @SerialName("parent_summary")
    val parentSummary: String = "",
    @SerialName("review_due_status")
    val reviewDueStatus: String = "",
    @SerialName("suggested_actions")
    val suggestedActions: List<String> = emptyList(),
    @SerialName("review_schedule")
    val reviewSchedule: BrainyPalWrongQuestionReviewSchedule = BrainyPalWrongQuestionReviewSchedule(),
) {
    val dueStatusLabel: String
        get() = when (reviewDueStatus) {
            "overdue" -> "已到期"
            "today" -> "今天到期"
            "future" -> "未到期"
            else -> "待复习"
        }
}

@Serializable
data class BrainyPalWrongQuestionReviewSchedule(
    @SerialName("next_review_due_at")
    val nextReviewDueAt: String? = null,
)

@Serializable
data class BrainyPalParentPracticeTaskSummaryResponse(
    @SerialName("task_id")
    val taskId: String,
    val status: String,
    @SerialName("help_used")
    val helpUsed: Int,
    @SerialName("help_limit")
    val helpLimit: Int,
    val results: Map<String, Int> = emptyMap(),
    @SerialName("parent_summary")
    val parentSummary: String = "",
)

@Serializable
data class BrainyPalParentTaskSummary(
    val taskId: String,
    val title: String,
    val statusLabel: String,
    val kindLabel: String,
    val helpUsageLabel: String,
    val itemCountLabel: String,
    val parentSummary: String,
) {
    companion object {
        fun from(task: BrainyPalChildPracticeTaskDetail): BrainyPalParentTaskSummary {
            return BrainyPalParentTaskSummary(
                taskId = task.taskId,
                title = task.title,
                statusLabel = task.statusLabel,
                kindLabel = task.taskKindLabel,
                helpUsageLabel = "${task.helpUsed}/${task.helpLimit}",
                itemCountLabel = "${task.items.size} ${itemUnit(task.taskType)}",
                parentSummary = task.parentSummary.orEmpty(),
            )
        }

        private fun itemUnit(taskType: String): String {
            return when (taskType) {
                "recitation" -> "段"
                "wrong_question_practice" -> "题"
                else -> "条"
            }
        }
    }
}

object BrainyPalParentImportSessionComposer {
    fun textRequest(
        entryGoal: String,
        title: String,
        subject: String,
        rawText: String,
        defaultUse: String = defaultUseForEntryGoal(entryGoal),
    ): BrainyPalCreateParentImportSessionRequest {
        return BrainyPalCreateParentImportSessionRequest(
            entryGoal = entryGoal.trim().ifEmpty { "practice" },
            inputMode = "paste",
            defaultUse = defaultUse,
            title = title.trim().ifEmpty { "导入作业材料" },
            subject = subject.trim().ifEmpty { null },
            rawText = rawText.trim(),
        )
    }

    private fun defaultUseForEntryGoal(entryGoal: String): String {
        return when (entryGoal) {
            "dictation" -> "dictation_material"
            "reading", "recitation" -> "reading_material"
            "review" -> "extract_review"
            else -> "prepare_task"
        }
    }
}

object BrainyPalParentTaskComposer {
    private val entrySeparators = Regex("[\\n,，、;；]+")

    fun dictationRequest(
        title: String,
        rawEntries: String,
        helpLimit: Int,
        dueAt: String? = null,
    ): BrainyPalCreateDictationPracticeTaskRequest? {
        val entries = rawEntries
            .split(entrySeparators)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        if (entries.isEmpty()) return null
        return BrainyPalCreateDictationPracticeTaskRequest(
            entries = entries,
            title = title.trim().ifEmpty { "今日听写" },
            helpLimit = helpLimit.coerceAtLeast(0),
            dueAt = dueAt,
        )
    }

    fun wrongQuestionRequest(
        reviews: List<BrainyPalDueWrongQuestionReviewItem>,
        title: String,
        helpLimit: Int,
        dueAt: String? = null,
    ): BrainyPalCreatePracticeTaskFromWrongQuestionsRequest? {
        val questionIds = reviews
            .map { it.questionId.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
        if (questionIds.isEmpty()) return null
        return BrainyPalCreatePracticeTaskFromWrongQuestionsRequest(
            questionIds = questionIds,
            title = title.trim().ifEmpty { "今日错题练习" },
            helpLimit = helpLimit.coerceAtLeast(0),
            dueAt = dueAt,
        )
    }
}

object BrainyPalParentMaterialComposer {
    fun importTextRequest(
        title: String,
        subject: String,
        rawText: String,
        gradeBand: String? = null,
        language: String? = null,
    ): BrainyPalImportTextMaterialRequest? {
        val text = rawText.trim()
        if (text.isEmpty()) return null
        return BrainyPalImportTextMaterialRequest(
            rawText = text,
            subject = subject.trim().ifEmpty { null },
            gradeBand = gradeBand?.trim()?.ifEmpty { null },
            language = language?.trim()?.ifEmpty { null },
            title = title.trim().ifEmpty { null },
        )
    }

    fun taskRequest(
        title: String,
        taskType: String? = null,
        helpLimit: Int = 3,
        dueAt: String? = null,
        activate: Boolean = true,
    ): BrainyPalCreateTaskFromMaterialRequest {
        return BrainyPalCreateTaskFromMaterialRequest(
            taskType = taskType?.trim()?.ifEmpty { null },
            title = title.trim().ifEmpty { null },
            helpLimit = helpLimit.coerceAtLeast(0),
            dueAt = dueAt,
            activate = activate,
        )
    }

    fun confirmRequest(
        title: String? = null,
        items: List<BrainyPalParentMaterialItem>? = null,
        note: String? = null,
    ): BrainyPalConfirmParentMaterialRequest {
        return BrainyPalConfirmParentMaterialRequest(
            title = title?.trim()?.ifEmpty { null },
            items = items?.takeIf { it.isNotEmpty() },
            parentNote = note?.trim()?.ifEmpty { null },
        )
    }
}

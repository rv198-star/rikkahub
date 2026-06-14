package me.rerere.rikkahub.brainypal.shared

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
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
        get() = when (status) {
            "draft" -> "草稿"
            "active" -> "已下发"
            "completed" -> "已完成"
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
    @SerialName("recent_tasks")
    val recentTasks: List<BrainyPalChildPracticeTaskDetail> = emptyList(),
    val counts: Map<String, Int> = emptyMap(),
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
            val taskCount = response.recentTasks.size
            return BrainyPalParentWorkbench(
                materialSummary = when {
                    draftCount > 0 -> "$draftCount 份材料待确认"
                    confirmedCount > 0 -> "$confirmedCount 份材料可复用"
                    else -> "还没有导入材料"
                },
                taskSummary = when {
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
    ): BrainyPalCreateTaskFromMaterialRequest {
        return BrainyPalCreateTaskFromMaterialRequest(
            taskType = taskType?.trim()?.ifEmpty { null },
            title = title.trim().ifEmpty { null },
            helpLimit = helpLimit.coerceAtLeast(0),
            dueAt = dueAt,
        )
    }
}

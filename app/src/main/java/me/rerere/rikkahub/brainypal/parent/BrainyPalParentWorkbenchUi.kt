package me.rerere.rikkahub.brainypal.parent

import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskDetail
import me.rerere.rikkahub.brainypal.shared.BrainyPalDictationOcrBoundingBox
import me.rerere.rikkahub.brainypal.shared.BrainyPalDictationOcrConfirmationAction
import me.rerere.rikkahub.brainypal.shared.BrainyPalDictationOcrReview
import me.rerere.rikkahub.brainypal.shared.BrainyPalParentMaterial
import me.rerere.rikkahub.brainypal.shared.BrainyPalParentImportSession
import me.rerere.rikkahub.brainypal.shared.BrainyPalParentImportSessionPreview
import me.rerere.rikkahub.brainypal.shared.BrainyPalParentPracticeTaskView

data class BrainyPalParentSupplyEntry(
    val id: String,
    val label: String,
    val supportingText: String,
    val statusLabel: String,
    val enabled: Boolean,
    val structuredPrimary: Boolean = false,
)

data class BrainyPalParentSummaryChip(
    val id: String,
    val label: String,
    val count: Int,
)

data class BrainyPalParentTaskGroup(
    val id: String,
    val label: String,
    val tasks: List<BrainyPalChildPracticeTaskDetail>,
)

data class BrainyPalParentPendingTaskCard(
    val taskId: String,
    val title: String,
    val statusLabel: String,
    val kindLabel: String,
    val itemCountLabel: String,
    val childVisibilityLabel: String,
    val actionLabels: List<String>,
)

data class BrainyPalParentImportConfirmationSection(
    val id: String,
    val label: String,
    val detail: String,
    val expanded: Boolean,
)

data class BrainyPalParentImportConfirmationAction(
    val id: String,
    val label: String,
    val primary: Boolean,
)

data class BrainyPalParentOcrEvidenceCard(
    val taskId: String,
    val itemId: String,
    val title: String,
    val resultLabel: String,
    val evidenceLine: String,
    val sourceRegionLabel: String,
    val previewImageRef: String,
    val previewBoundingBox: BrainyPalDictationOcrBoundingBox?,
    val previewActionLabel: String,
    val guidanceLabel: String?,
    val requiresManualConfirmation: Boolean,
    val actions: List<BrainyPalDictationOcrConfirmationAction>,
    val actionLabels: List<String>,
) {
    val hasSourceRegionOverlay: Boolean
        get() = previewBoundingBox != null
}

object BrainyPalParentWorkbenchUi {
    private val activeTaskStatuses = setOf(
        "pending",
        "assigned",
        "accepted",
        "in_progress",
        "paused",
        "reviewing",
    )
    private val completedTaskStatuses = setOf("submitted", "completed", "expired", "skipped")

    fun supplyEntries(configured: Boolean): List<BrainyPalParentSupplyEntry> {
        return listOf(
            BrainyPalParentSupplyEntry(
                id = "practice_questions",
                label = "练习题",
                supportingText = "导入题目，生成待发练习",
                statusLabel = if (configured) "可用" else "需连接",
                enabled = configured,
                structuredPrimary = true,
            ),
            BrainyPalParentSupplyEntry(
                id = "dictation",
                label = "听写",
                supportingText = "字词、短句先确认再下发",
                statusLabel = if (configured) "可用" else "需连接",
                enabled = configured,
                structuredPrimary = true,
            ),
            BrainyPalParentSupplyEntry(
                id = "reading",
                label = "阅读导读",
                supportingText = "材料分段，准备阅读任务",
                statusLabel = if (configured) "可用" else "需连接",
                enabled = configured,
                structuredPrimary = true,
            ),
            BrainyPalParentSupplyEntry(
                id = "recitation",
                label = "背诵",
                supportingText = "课文或段落拆成背诵任务",
                statusLabel = if (configured) "可用" else "需连接",
                enabled = configured,
                structuredPrimary = true,
            ),
            BrainyPalParentSupplyEntry(
                id = "wrong_questions",
                label = "错题复练",
                supportingText = "从到期错题生成今日练习",
                statusLabel = if (configured) "可用" else "需连接",
                enabled = configured,
                structuredPrimary = true,
            ),
            BrainyPalParentSupplyEntry(
                id = "paste_text",
                label = "粘贴材料",
                supportingText = "还没想清类型时先粘贴",
                statusLabel = if (configured) "可用" else "需连接",
                enabled = configured,
            ),
            BrainyPalParentSupplyEntry(
                id = "photo_scan",
                label = "拍照扫描",
                supportingText = "拍作业页，先生成可确认材料",
                statusLabel = "待接入",
                enabled = false,
            ),
            BrainyPalParentSupplyEntry(
                id = "chat_light",
                label = "简单说一下",
                supportingText = "不确定时用聊天生成候选",
                statusLabel = if (configured) "可用" else "需连接",
                enabled = configured,
            ),
        )
    }

    fun summaryChips(
        draftMaterials: List<BrainyPalParentMaterial>,
        pendingTasks: List<BrainyPalParentPracticeTaskView> = emptyList(),
        tasks: List<BrainyPalChildPracticeTaskDetail>,
    ): List<BrainyPalParentSummaryChip> {
        return listOf(
            BrainyPalParentSummaryChip(
                id = "draft_materials",
                label = "待确认材料",
                count = draftMaterials.size,
            ),
            BrainyPalParentSummaryChip(
                id = "pending_tasks",
                label = "待发任务",
                count = pendingTasks.size,
            ),
            BrainyPalParentSummaryChip(
                id = "ocr_confirmation",
                label = "待确认 OCR",
                count = tasks.count { task -> hasPendingOcrConfirmation(task) },
            ),
            BrainyPalParentSummaryChip(
                id = "active_tasks",
                label = "进行中任务",
                count = tasks.count { task ->
                    !hasPendingOcrConfirmation(task) && task.status in activeTaskStatuses
                },
            ),
        )
    }

    fun pendingTaskCards(
        pendingTasks: List<BrainyPalParentPracticeTaskView>,
    ): List<BrainyPalParentPendingTaskCard> {
        return pendingTasks.map { task ->
            BrainyPalParentPendingTaskCard(
                taskId = task.taskId,
                title = task.title,
                statusLabel = task.statusLabel,
                kindLabel = task.kindLabel,
                itemCountLabel = "${task.totalItems} ${itemUnit(task.mode)}",
                childVisibilityLabel = if (task.childVisible) "孩子已可见" else "孩子暂不可见",
                actionLabels = listOf("检查", "下发"),
            )
        }
    }

    fun importConfirmationSections(
        session: BrainyPalParentImportSession,
    ): List<BrainyPalParentImportConfirmationSection> {
        val hasCandidateRisk = session.riskFlags.any {
            it in setOf(
                "missing_reference_answer",
                "answer_conflict",
                "needs_parent_review",
                "uncertain_task_type",
            )
        } || session.candidates.any { it.riskFlags.isNotEmpty() }
        val hasOcrRisk = session.riskFlags.any {
            it in setOf("low_confidence_ocr", "handwriting_present")
        }
        val needsChildPreview = session.preview.taskType in setOf("reading", "mixed") ||
            session.entryGoal in setOf("reading", "recitation") ||
            session.preview.requiresOcrReturn
        return listOf(
            BrainyPalParentImportConfirmationSection(
                id = "ai_judgement",
                label = "AI 判断",
                detail = "${session.preview.taskTypeLabel()} · ${session.riskSummary()}",
                expanded = true,
            ),
            BrainyPalParentImportConfirmationSection(
                id = "candidate_content",
                label = "候选内容",
                detail = "${session.candidates.size} 项候选",
                expanded = hasCandidateRisk || hasOcrRisk,
            ),
            BrainyPalParentImportConfirmationSection(
                id = "child_preview",
                label = "孩子体验预览",
                detail = "${session.preview.childModeLabel()} · 约 ${session.preview.estimatedMinutes} 分钟",
                expanded = needsChildPreview,
            ),
            BrainyPalParentImportConfirmationSection(
                id = "send_settings",
                label = "下发设置",
                detail = session.preview.sendLabel,
                expanded = false,
            ),
        )
    }

    fun importConfirmationActions(
        session: BrainyPalParentImportSession,
    ): List<BrainyPalParentImportConfirmationAction> {
        return listOf(
            BrainyPalParentImportConfirmationAction(
                id = "save_pending",
                label = session.preview.sendLabel,
                primary = false,
            ),
            BrainyPalParentImportConfirmationAction(
                id = "send_now",
                label = "确认并立即下发",
                primary = true,
            ),
        )
    }

    fun taskGroups(tasks: List<BrainyPalChildPracticeTaskDetail>): List<BrainyPalParentTaskGroup> {
        val pendingConfirmation = tasks.filter(::hasPendingOcrConfirmation)
        val active = tasks.filter { task ->
            !hasPendingOcrConfirmation(task) && task.status in activeTaskStatuses
        }
        val completed = tasks.filter { task ->
            !hasPendingOcrConfirmation(task) && task.status in completedTaskStatuses
        }
        return listOf(
            BrainyPalParentTaskGroup(
                id = "pending_confirmation",
                label = "待确认",
                tasks = pendingConfirmation,
            ),
            BrainyPalParentTaskGroup(
                id = "active",
                label = "进行中",
                tasks = active,
            ),
            BrainyPalParentTaskGroup(
                id = "completed",
                label = "已完成",
                tasks = completed,
            ),
        ).filter { it.tasks.isNotEmpty() }
    }

    fun ocrEvidenceCards(task: BrainyPalChildPracticeTaskDetail): List<BrainyPalParentOcrEvidenceCard> {
        return BrainyPalDictationOcrReview.rows(task).map { row ->
            val evidenceParts = listOfNotNull(
                "识别：${row.recognizedText.ifBlank { "空" }}",
                row.confidenceLabel,
            )
            BrainyPalParentOcrEvidenceCard(
                taskId = task.taskId,
                itemId = row.itemId,
                title = row.title,
                resultLabel = row.resultLabel,
                evidenceLine = evidenceParts.joinToString(" · "),
                sourceRegionLabel = row.sourceRegionLabel,
                previewImageRef = row.previewImageRef,
                previewBoundingBox = row.previewBoundingBox,
                previewActionLabel = row.previewButtonLabel,
                guidanceLabel = row.guidanceLabel,
                requiresManualConfirmation = row.requiresManualConfirmation,
                actions = row.confirmationActions,
                actionLabels = row.confirmationActions.map { it.label },
            )
        }
    }

    private fun hasPendingOcrConfirmation(task: BrainyPalChildPracticeTaskDetail): Boolean {
        return BrainyPalDictationOcrReview.rows(task).any { it.requiresManualConfirmation }
    }

    private fun itemUnit(taskType: String): String {
        return when (taskType) {
            "reading", "recitation" -> "段"
            "dictation" -> "条"
            else -> "题"
        }
    }

    private fun BrainyPalParentImportSession.riskSummary(): String {
        return if (riskFlags.isEmpty()) {
            "无需额外确认"
        } else {
            "需确认 ${riskFlags.size} 项风险"
        }
    }

    private fun BrainyPalParentImportSessionPreview.taskTypeLabel(): String {
        return when (taskType) {
            "dictation" -> "听写"
            "reading" -> "阅读"
            "mixed" -> "混合任务"
            else -> "练习"
        }
    }

    private fun BrainyPalParentImportSessionPreview.childModeLabel(): String {
        return when (childMode) {
            "web" -> "电脑完成"
            "pdf" -> "打印完成"
            "mixed" -> "混合完成"
            else -> "App 完成"
        }
    }
}

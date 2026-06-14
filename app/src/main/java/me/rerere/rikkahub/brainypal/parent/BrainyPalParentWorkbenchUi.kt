package me.rerere.rikkahub.brainypal.parent

import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskDetail
import me.rerere.rikkahub.brainypal.shared.BrainyPalDictationOcrBoundingBox
import me.rerere.rikkahub.brainypal.shared.BrainyPalDictationOcrConfirmationAction
import me.rerere.rikkahub.brainypal.shared.BrainyPalDictationOcrReview
import me.rerere.rikkahub.brainypal.shared.BrainyPalParentMaterial

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
                id = "paste_text",
                label = "粘贴材料",
                supportingText = "粘贴听写、题目或阅读材料",
                statusLabel = if (configured) "可用" else "需连接",
                enabled = configured,
                structuredPrimary = true,
            ),
            BrainyPalParentSupplyEntry(
                id = "wrong_questions",
                label = "错题复习",
                supportingText = "从到期错题生成今日练习",
                statusLabel = if (configured) "可用" else "需连接",
                enabled = configured,
                structuredPrimary = true,
            ),
            BrainyPalParentSupplyEntry(
                id = "photo_scan",
                label = "拍照扫描",
                supportingText = "拍作业页，先生成可确认材料",
                statusLabel = "待接入",
                enabled = false,
            ),
            BrainyPalParentSupplyEntry(
                id = "ai_material_search",
                label = "问 AI 找材料",
                supportingText = "按年级、课文或单元找候选",
                statusLabel = "待接入",
                enabled = false,
            ),
        )
    }

    fun summaryChips(
        draftMaterials: List<BrainyPalParentMaterial>,
        tasks: List<BrainyPalChildPracticeTaskDetail>,
    ): List<BrainyPalParentSummaryChip> {
        return listOf(
            BrainyPalParentSummaryChip(
                id = "draft_materials",
                label = "待确认材料",
                count = draftMaterials.size,
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
}

package me.rerere.rikkahub.brainypal.parent

import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskDetail
import me.rerere.rikkahub.brainypal.shared.BrainyPalDictationOcrBoundingBox
import me.rerere.rikkahub.brainypal.shared.BrainyPalDictationOcrConfirmationAction
import me.rerere.rikkahub.brainypal.shared.BrainyPalDictationOcrReview
import me.rerere.rikkahub.brainypal.shared.BrainyPalParentMaterial
import me.rerere.rikkahub.brainypal.shared.BrainyPalParentImportSession
import me.rerere.rikkahub.brainypal.shared.BrainyPalParentImportSessionPreview
import me.rerere.rikkahub.brainypal.shared.BrainyPalParentChatTriggerResponse
import me.rerere.rikkahub.brainypal.shared.BrainyPalParentLearningRecordsSummaryResponse
import me.rerere.rikkahub.brainypal.shared.BrainyPalParentPhotoScanSnapshot
import me.rerere.rikkahub.brainypal.shared.BrainyPalParentPracticeTaskView
import me.rerere.rikkahub.brainypal.shared.BrainyPalParentPracticeTaskResultDetailResponse
import me.rerere.rikkahub.brainypal.shared.BrainyPalParentWorkloadGuardConflict
import me.rerere.rikkahub.brainypal.shared.BrainyPalStrategyVersion

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

data class BrainyPalParentWorkloadGuardPrompt(
    val taskId: String,
    val taskTitle: String,
    val title: String,
    val message: String,
    val loadSummary: String,
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
    val confirmationSummaryLabel: String?,
    val requiresManualConfirmation: Boolean,
    val actions: List<BrainyPalDictationOcrConfirmationAction>,
    val actionLabels: List<String>,
) {
    val hasSourceRegionOverlay: Boolean
        get() = previewBoundingBox != null
}

data class BrainyPalParentWebMaterialCandidateCard(
    val materialId: String,
    val title: String,
    val typeLabel: String,
    val sourceLabel: String,
    val confidenceLabel: String,
    val uncertaintyLabel: String,
    val actionLabels: List<String>,
    val canDirectSend: Boolean,
)

data class BrainyPalParentChatTriggerCard(
    val title: String,
    val body: String,
    val primaryActionLabel: String,
    val requiresConfirmation: Boolean,
    val canDirectSend: Boolean,
)

data class BrainyPalParentPhotoScanCandidateCard(
    val candidateId: String,
    val numberLabel: String,
    val questionText: String,
    val childAnswerLabel: String,
    val confidenceLabel: String,
    val verificationLabel: String,
    val actionLabels: List<String>,
)

data class BrainyPalParentInfoCard(
    val title: String,
    val body: String,
    val metadata: String? = null,
    val statusLabel: String = "",
    val actionLabels: List<String> = emptyList(),
)

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
                statusLabel = if (configured) "可用" else "需连接",
                enabled = configured,
            ),
            BrainyPalParentSupplyEntry(
                id = "web_search",
                label = "联网找材料",
                supportingText = "按年级、课文或单元找候选",
                statusLabel = if (configured) "可用" else "需连接",
                enabled = configured,
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
                actionLabels = listOf("检查", "编辑", "下发", "归档", "删除"),
            )
        }
    }

    fun workloadGuardPrompt(
        task: BrainyPalParentPracticeTaskView,
        guard: BrainyPalParentWorkloadGuardConflict,
    ): BrainyPalParentWorkloadGuardPrompt {
        return BrainyPalParentWorkloadGuardPrompt(
            taskId = task.taskId,
            taskTitle = task.title,
            title = "先确认孩子今天的负载",
            message = guard.message.ifBlank { "今天已经有较多待完成任务，确认后仍可下发。" },
            loadSummary = "当前还有 ${guard.activeTasks} 个进行中任务，预计约 ${guard.estimatedMinutes} 分钟。",
            actionLabels = listOf("先放待发任务", "仍然下发"),
        )
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
                previewActionLabel = if (row.requiresManualConfirmation) {
                    row.previewButtonLabel
                } else {
                    "重新查看证据"
                },
                guidanceLabel = row.guidanceLabel,
                confirmationSummaryLabel = if (row.requiresManualConfirmation) {
                    null
                } else {
                    "已确认：${row.resultLabel}"
                },
                requiresManualConfirmation = row.requiresManualConfirmation,
                actions = if (row.requiresManualConfirmation) row.confirmationActions else emptyList(),
                actionLabels = if (row.requiresManualConfirmation) {
                    row.confirmationActions.map { it.label }
                } else {
                    emptyList()
                },
            )
        }
    }

    fun webMaterialCandidateCards(
        materials: List<BrainyPalParentMaterial>,
    ): List<BrainyPalParentWebMaterialCandidateCard> {
        return materials
            .filter { it.inputMode == "web_search" || it.sourceCandidates.isNotEmpty() }
            .map { material ->
                val source = material.sourceCandidates.firstOrNull()
                BrainyPalParentWebMaterialCandidateCard(
                    materialId = material.materialId,
                    title = material.title,
                    typeLabel = material.typeLabel,
                    sourceLabel = when {
                        source != null -> "${source.title} · ${source.sourceUrl}"
                        material.sourceRefs.isNotEmpty() -> material.sourceRefs.first()
                        else -> "来源待确认"
                    },
                    confidenceLabel = material.confidence?.let {
                        "置信度 ${(it * 100).toInt()}%"
                    } ?: "置信度待确认",
                    uncertaintyLabel = material.uncertaintyNote
                        ?: source?.uncertaintyNote
                        ?: "来源和版本需要家长确认。",
                    actionLabels = listOf("确认入库", "确认并生成待发任务", "拒绝"),
                    canDirectSend = false,
                )
            }
    }

    fun chatTriggerCard(response: BrainyPalParentChatTriggerResponse): BrainyPalParentChatTriggerCard {
        val actionLabel = response.structuredAction?.label ?: "继续"
        return when (response.intent) {
            "prepare_import" -> BrainyPalParentChatTriggerCard(
                title = "导入确认候选",
                body = response.importSession?.title ?: response.message,
                primaryActionLabel = actionLabel,
                requiresConfirmation = true,
                canDirectSend = false,
            )
            "child_status_query" -> BrainyPalParentChatTriggerCard(
                title = "学习摘要",
                body = response.statusSummary?.headline ?: response.message,
                primaryActionLabel = actionLabel,
                requiresConfirmation = false,
                canDirectSend = false,
            )
            "strategy_proposal" -> BrainyPalParentChatTriggerCard(
                title = "策略候选",
                body = response.strategyCandidate?.parentGoalText ?: response.message,
                primaryActionLabel = actionLabel,
                requiresConfirmation = true,
                canDirectSend = false,
            )
            else -> BrainyPalParentChatTriggerCard(
                title = "家长对话",
                body = response.message,
                primaryActionLabel = "知道了",
                requiresConfirmation = false,
                canDirectSend = false,
            )
        }
    }

    fun photoScanCandidateCards(
        snapshot: BrainyPalParentPhotoScanSnapshot,
    ): List<BrainyPalParentPhotoScanCandidateCard> {
        return snapshot.candidates.mapIndexed { index, candidate ->
            BrainyPalParentPhotoScanCandidateCard(
                candidateId = candidate.candidateId,
                numberLabel = candidate.questionNumber ?: "第 ${index + 1} 题",
                questionText = candidate.questionText,
                childAnswerLabel = candidate.childAnswer?.let { "孩子答案：$it" } ?: "未识别到孩子答案",
                confidenceLabel = "置信度 ${(candidate.confidence * 100).toInt()}%",
                verificationLabel = when {
                    candidate.verification?.requiresParentReview == true -> "需要家长确认"
                    candidate.verification?.judgement == "correct" -> "AI 初判正确"
                    candidate.verification?.judgement == "incorrect" -> "AI 初判可能有错"
                    candidate.verification != null -> "AI 已给参考判断"
                    else -> "等待家长确认"
                },
                actionLabels = listOf("写入错题", "编辑后写入", "跳过"),
            )
        }
    }

    fun learningSummaryCards(
        summary: BrainyPalParentLearningRecordsSummaryResponse,
    ): List<BrainyPalParentInfoCard> {
        val overview = BrainyPalParentInfoCard(
            title = "学习记录 ${summary.totalCount}",
            body = summary.recordTypeCounts
                .toList()
                .sortedBy { (type, _) -> type }
                .joinToString(" · ") { (type, count) -> "${recordTypeLabel(type)} $count" }
                .ifBlank { "还没有学习记录" },
            metadata = summary.knowledgePoints.take(3).joinToString(" · ").ifBlank { null },
        )
        val latest = summary.latestRecords.map { record ->
            BrainyPalParentInfoCard(
                title = listOfNotNull(record.subject, recordTypeLabel(record.recordType))
                    .joinToString(" · "),
                body = record.parentSummary,
                metadata = record.knowledgePoints.joinToString(" · ").ifBlank { null },
            )
        }
        return listOf(overview) + latest
    }

    fun resultDetailCards(
        detail: BrainyPalParentPracticeTaskResultDetailResponse,
    ): List<BrainyPalParentInfoCard> {
        val summary = BrainyPalParentInfoCard(
            title = detail.title,
            body = detail.parentSummary,
            metadata = recordTypeLabel(detail.mode),
            statusLabel = detail.resultStatus,
        )
        val itemCards = detail.items.mapIndexed { index, item ->
            val oralEvidence = item.evidence.oralEvidence
            val bodyParts = if (oralEvidence != null) {
                listOfNotNull(
                    "自评 ${oralEvidence.selfRating}/5",
                    "重听 ${oralEvidence.rereadCount} 次",
                    oralEvidence.stuckPoints.takeIf { it.isNotEmpty() }?.joinToString(
                        prefix = "卡点：",
                        separator = "；",
                    ),
                    item.parentNote.takeIf { it.isNotBlank() },
                )
            } else {
                listOfNotNull(
                    item.parentNote.takeIf { it.isNotBlank() },
                    item.evidence.answerValue?.let { "孩子答案：$it" },
                )
            }
            BrainyPalParentInfoCard(
                title = "第 ${index + 1} 条 · ${evidenceTypeLabel(item.kind, oralEvidence != null)}",
                body = bodyParts.joinToString(" · ").ifBlank { item.prompt },
                metadata = item.correctionPrompt,
                statusLabel = item.resultStatus,
            )
        }
        val actions = detail.nextActions.map { action ->
            BrainyPalParentInfoCard(
                title = action.title,
                body = action.body,
                metadata = action.itemIds.joinToString(" · ").ifBlank { null },
                statusLabel = action.kind,
            )
        }
        return listOf(summary) + itemCards + actions
    }

    fun strategyCards(
        strategies: List<BrainyPalStrategyVersion>,
    ): List<BrainyPalParentInfoCard> {
        return strategies.map { strategy ->
            val statusLabel = when (strategy.status) {
                "draft" -> "待确认策略"
                "active" -> "已启用策略"
                "paused" -> "已暂停策略"
                "expired" -> "已过期策略"
                else -> "策略候选"
            }
            val actions = when (strategy.status) {
                "draft" -> listOf("确认启用")
                "active" -> listOf("暂停")
                else -> emptyList()
            }
            BrainyPalParentInfoCard(
                title = strategy.childFacingGoal,
                body = strategy.parentGoalText,
                metadata = strategy.rationale,
                statusLabel = statusLabel,
                actionLabels = actions,
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

    private fun recordTypeLabel(recordType: String): String {
        return when (recordType) {
            "practice",
            "wrong_question_practice" -> "练习"
            "dictation" -> "听写"
            "reading" -> "朗读"
            "recitation" -> "背诵"
            else -> "记录"
        }
    }

    private fun evidenceTypeLabel(kind: String, hasOralEvidence: Boolean): String {
        return if (hasOralEvidence) {
            when (kind) {
                "reading" -> "朗读证据"
                "recitation" -> "背诵证据"
                else -> "口语证据"
            }
        } else {
            "结果"
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

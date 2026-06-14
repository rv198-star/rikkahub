package me.rerere.rikkahub.brainypal.parent

import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskDetail
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskItem
import me.rerere.rikkahub.brainypal.shared.BrainyPalDictationOcrBoundingBox
import me.rerere.rikkahub.brainypal.shared.BrainyPalDictationOcrEvidence
import me.rerere.rikkahub.brainypal.shared.BrainyPalParentMaterial
import me.rerere.rikkahub.brainypal.shared.BrainyPalParentImportSession
import me.rerere.rikkahub.brainypal.shared.BrainyPalParentImportSessionCandidate
import me.rerere.rikkahub.brainypal.shared.BrainyPalParentImportSessionPreview
import me.rerere.rikkahub.brainypal.shared.BrainyPalParentPracticeTaskView
import me.rerere.rikkahub.brainypal.shared.BrainyPalParentTaskSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrainyPalParentWorkbenchUiTest {
    @Test
    fun `supply entries start from parent source modes`() {
        val entries = BrainyPalParentWorkbenchUi.supplyEntries(configured = true)

        assertEquals(
            listOf("练习题", "听写", "阅读导读", "背诵", "错题复练", "粘贴材料", "拍照扫描", "简单说一下"),
            entries.map { it.label },
        )
        assertTrue(entries[0].structuredPrimary)
        assertTrue(entries[1].structuredPrimary)
        assertTrue(entries[2].structuredPrimary)
        assertTrue(entries[3].structuredPrimary)
        assertTrue(entries[4].structuredPrimary)
        assertTrue(entries.first { it.id == "practice_questions" }.enabled)
        assertTrue(entries.first { it.id == "dictation" }.enabled)
        assertTrue(entries.first { it.id == "wrong_questions" }.enabled)
        assertTrue(entries.first { it.id == "paste_text" }.enabled)
        assertFalse(entries.first { it.id == "photo_scan" }.enabled)
        assertEquals("待接入", entries.first { it.id == "photo_scan" }.statusLabel)
        assertTrue(entries.first { it.id == "chat_light" }.enabled)
    }

    @Test
    fun `workbench presents structured import as primary and chat as simple fallback`() {
        val workbench = me.rerere.rikkahub.brainypal.shared.BrainyPalParentWorkbench.from(
            me.rerere.rikkahub.brainypal.shared.BrainyPalParentTaskWorkbenchResponse()
        )

        assertEquals("导入作业材料", workbench.primaryEntryLabel)
        assertEquals("简单说一下需求", workbench.secondaryChatLabel)
        assertEquals("structured", workbench.primaryEntryKind)
    }

    @Test
    fun `workbench chips prioritize pending material ocr confirmation and active tasks`() {
        val chips = BrainyPalParentWorkbenchUi.summaryChips(
            draftMaterials = listOf(material("m1"), material("m2")),
            pendingTasks = listOf(pendingTask()),
            tasks = listOf(
                dictationTask(
                    taskId = "ocr-task",
                    status = "reviewing",
                    confirmationStatus = "unconfirmed",
                ),
                practiceTask(taskId = "active-task", status = "in_progress"),
                practiceTask(taskId = "done-task", status = "completed"),
            ),
        )

        assertEquals(
            listOf("待确认材料 2", "待发任务 1", "待确认 OCR 1", "进行中任务 1"),
            chips.map { "${it.label} ${it.count}" },
        )
    }

    @Test
    fun `pending task cards use parent status label instead of draft copy`() {
        val cards = BrainyPalParentWorkbenchUi.pendingTaskCards(
            listOf(pendingTask(title = "口算待发任务"))
        )

        assertEquals("口算待发任务", cards.single().title)
        assertEquals("待发任务", cards.single().statusLabel)
        assertEquals("2 题", cards.single().itemCountLabel)
        assertEquals(listOf("检查", "下发"), cards.single().actionLabels)
        assertFalse(cards.single().statusLabel.contains("草稿"))
    }

    @Test
    fun `import confirmation expands candidate content when answer is missing`() {
        val sections = BrainyPalParentWorkbenchUi.importConfirmationSections(
            importSession(riskFlags = listOf("missing_reference_answer"))
        )

        assertEquals(
            listOf("AI 判断", "候选内容", "孩子体验预览", "下发设置"),
            sections.map { it.label },
        )
        assertTrue(sections.first { it.id == "candidate_content" }.expanded)
        assertFalse(sections.first { it.id == "child_preview" }.expanded)
        assertEquals(
            listOf("保存为待发任务", "确认并立即下发"),
            BrainyPalParentWorkbenchUi.importConfirmationActions(importSession()).map { it.label },
        )
    }

    @Test
    fun `import confirmation stays compact for high confidence dictation`() {
        val sections = BrainyPalParentWorkbenchUi.importConfirmationSections(
            importSession(
                entryGoal = "dictation",
                riskFlags = emptyList(),
                preview = BrainyPalParentImportSessionPreview(taskType = "dictation"),
                candidates = listOf(
                    BrainyPalParentImportSessionCandidate(
                        candidateId = "candidate_1",
                        kind = "dictation_entry",
                        prompt = "认真",
                    )
                ),
            )
        )

        assertTrue(sections.first { it.id == "ai_judgement" }.expanded)
        assertFalse(sections.first { it.id == "candidate_content" }.expanded)
        assertFalse(sections.first { it.id == "send_settings" }.expanded)
    }

    @Test
    fun `task groups put ocr confirmation before active and completed work`() {
        val groups = BrainyPalParentWorkbenchUi.taskGroups(
            listOf(
                practiceTask(taskId = "active-task", title = "今日练习", status = "in_progress"),
                practiceTask(taskId = "done-task", title = "已完成练习", status = "completed"),
                dictationTask(
                    taskId = "ocr-task",
                    title = "听写确认",
                    status = "reviewing",
                    confirmationStatus = "unconfirmed",
                ),
            )
        )

        assertEquals(listOf("待确认", "进行中", "已完成"), groups.map { it.label })
        assertEquals(listOf("听写确认"), groups[0].tasks.map { it.title })
        assertEquals(listOf("今日练习"), groups[1].tasks.map { it.title })
        assertEquals(listOf("已完成练习"), groups[2].tasks.map { it.title })
    }

    @Test
    fun `ocr evidence card requires visible source evidence before attribution`() {
        val card = BrainyPalParentWorkbenchUi.ocrEvidenceCards(
            dictationTask(
                taskId = "ocr-task",
                status = "reviewing",
                confirmationStatus = "unconfirmed",
                confidence = 0.62f,
                recognizedText = "roket",
            )
        ).single()

        assertEquals("第 1 条", card.title)
        assertEquals("识别：roket · 置信度 62%", card.evidenceLine)
        assertEquals("照片区域", card.sourceRegionLabel)
        assertEquals("查看照片区域", card.previewActionLabel)
        assertEquals(
            listOf("孩子写错了", "OCR 识别错了", "照片不清楚", "其实是对的"),
            card.actionLabels,
        )
        assertTrue(card.requiresManualConfirmation)
        assertTrue(card.hasSourceRegionOverlay)
    }

    @Test
    fun `parent task summary uses child task units by type`() {
        assertEquals("1 段", BrainyPalParentTaskSummary.from(recitationTask()).itemCountLabel)
        assertEquals("2 条", BrainyPalParentTaskSummary.from(dictationTask(itemCount = 2)).itemCountLabel)
        assertEquals("3 题", BrainyPalParentTaskSummary.from(practiceTask(itemCount = 3)).itemCountLabel)
    }

    private fun material(materialId: String): BrainyPalParentMaterial {
        return BrainyPalParentMaterial(
            materialId = materialId,
            materialType = "dictation",
            title = "听写材料 $materialId",
        )
    }

    private fun pendingTask(
        taskId: String = "task_pending_1",
        title: String = "待发练习",
    ): BrainyPalParentPracticeTaskView {
        return BrainyPalParentPracticeTaskView(
            taskId = taskId,
            title = title,
            subject = "数学",
            mode = "practice",
            status = "draft",
            parentStatusLabel = "待发任务",
            totalItems = 2,
            childVisible = false,
        )
    }

    private fun importSession(
        entryGoal: String = "practice",
        riskFlags: List<String> = emptyList(),
        preview: BrainyPalParentImportSessionPreview = BrainyPalParentImportSessionPreview(
            taskType = "practice",
        ),
        candidates: List<BrainyPalParentImportSessionCandidate> = listOf(
            BrainyPalParentImportSessionCandidate(
                candidateId = "candidate_1",
                kind = "question",
                prompt = "1+1=?",
                riskFlags = riskFlags,
            )
        ),
    ): BrainyPalParentImportSession {
        return BrainyPalParentImportSession(
            sessionId = "import_1",
            entryGoal = entryGoal,
            title = "口算练习",
            subject = "数学",
            rawText = "1. 1+1=?",
            riskFlags = riskFlags,
            candidates = candidates,
            preview = preview,
        )
    }

    private fun practiceTask(
        taskId: String = "practice-task",
        title: String = "练习任务",
        status: String = "in_progress",
        itemCount: Int = 1,
    ): BrainyPalChildPracticeTaskDetail {
        return BrainyPalChildPracticeTaskDetail(
            taskId = taskId,
            title = title,
            taskType = "wrong_question_practice",
            status = status,
            helpLimit = 3,
            helpUsed = 0,
            items = (1..itemCount).map { index ->
                BrainyPalChildPracticeTaskItem(
                    itemId = "practice_$index",
                    prompt = "题目 $index",
                )
            },
        )
    }

    private fun dictationTask(
        taskId: String = "dictation-task",
        title: String = "听写任务",
        status: String = "reviewing",
        confirmationStatus: String = "confirmed",
        confidence: Float = 0.96f,
        recognizedText: String = "planet",
        itemCount: Int = 1,
    ): BrainyPalChildPracticeTaskDetail {
        return BrainyPalChildPracticeTaskDetail(
            taskId = taskId,
            title = title,
            taskType = "dictation",
            status = status,
            helpLimit = 3,
            helpUsed = 0,
            items = (1..itemCount).map { index ->
                BrainyPalChildPracticeTaskItem(
                    itemId = "dictation_$index",
                    prompt = "word_$index",
                    result = if (confirmationStatus == "unconfirmed") {
                        "needs_manual_review"
                    } else {
                        "correct"
                    },
                    ocrEvidence = BrainyPalDictationOcrEvidence(
                        imageRef = "https://placehold.co/800x1000/png",
                        recognizedText = recognizedText,
                        confidence = confidence,
                        boundingBox = BrainyPalDictationOcrBoundingBox(
                            x = 0.18f,
                            y = 0.38f,
                            width = 0.42f,
                            height = 0.1f,
                        ),
                        confirmationStatus = confirmationStatus,
                        errorAttribution = if (confirmationStatus == "confirmed") {
                            "correct"
                        } else {
                            null
                        },
                    ),
                )
            },
        )
    }

    private fun recitationTask(): BrainyPalChildPracticeTaskDetail {
        return BrainyPalChildPracticeTaskDetail(
            taskId = "recitation-task",
            title = "背诵任务",
            taskType = "recitation",
            status = "in_progress",
            helpLimit = 2,
            helpUsed = 0,
            items = listOf(
                BrainyPalChildPracticeTaskItem(
                    itemId = "recitation_1",
                    prompt = "春天来了，小草从土里探出头。",
                )
            ),
        )
    }
}

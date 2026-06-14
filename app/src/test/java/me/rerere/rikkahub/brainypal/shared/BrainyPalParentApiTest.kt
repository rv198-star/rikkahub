package me.rerere.rikkahub.brainypal.shared

import kotlinx.serialization.encodeToString
import me.rerere.rikkahub.utils.JsonInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrainyPalParentApiTest {
    @Test
    fun `dictation request trims pasted entries and keeps parent defaults`() {
        val request = BrainyPalParentTaskComposer.dictationRequest(
            title = " 今日听写 ",
            rawEntries = " apple\nbanana、观察, 锋利\n  ",
            helpLimit = 2,
        )

        assertEquals("今日听写", request?.title)
        assertEquals(listOf("apple", "banana", "观察", "锋利"), request?.entries)
        assertEquals(2, request?.helpLimit)
        assertEquals(null, request?.dueAt)
    }

    @Test
    fun `dictation request returns null without entries`() {
        val request = BrainyPalParentTaskComposer.dictationRequest(
            title = "",
            rawEntries = " \n 、, ",
            helpLimit = 3,
        )

        assertEquals(null, request)
    }

    @Test
    fun `material import request keeps parent pasted homework as candidate`() {
        val request = BrainyPalParentMaterialComposer.importTextRequest(
            title = " 本周听写 ",
            subject = "语文",
            rawText = " 听写词语：观察、勇敢。 ",
        )

        assertEquals("本周听写", request?.title)
        assertEquals("语文", request?.subject)
        assertEquals("听写词语：观察、勇敢。", request?.rawText)
    }

    @Test
    fun `material import request returns null without raw text`() {
        val request = BrainyPalParentMaterialComposer.importTextRequest(
            title = "听写",
            subject = "语文",
            rawText = "  \n ",
        )

        assertEquals(null, request)
    }

    @Test
    fun `wrong question request uses selected due review ids`() {
        val request = BrainyPalParentTaskComposer.wrongQuestionRequest(
            reviews = listOf(
                BrainyPalDueWrongQuestionReviewItem(
                    questionId = "wq_overdue",
                    subject = "数学",
                    questionText = "x - (-y - z)",
                    parentSummary = "括号前是负号时每一项都要变号。",
                    reviewDueStatus = "overdue",
                    suggestedActions = listOf("先口述规则，再做一道同类题。"),
                ),
                BrainyPalDueWrongQuestionReviewItem(
                    questionId = "wq_future",
                    subject = "语文",
                    questionText = "默写观察",
                    parentSummary = "偏旁容易写错。",
                    reviewDueStatus = "future",
                ),
            ),
            title = "",
            helpLimit = 3,
        )

        assertEquals("今日错题练习", request?.title)
        assertEquals(listOf("wq_overdue", "wq_future"), request?.questionIds)
        assertEquals(3, request?.helpLimit)
    }

    @Test
    fun `parent due reviews response decodes review supply fields`() {
        val body = """
            {
              "items": [
                {
                  "question_id": "wq_overdue",
                  "subject": "数学",
                  "question_text": "x - (-y - z)",
                  "parent_summary": "括号前是负号时每一项都要变号。",
                  "review_due_status": "overdue",
                  "suggested_actions": ["先口述规则，再做一道同类题。"],
                  "review_schedule": {
                    "next_review_due_at": "2026-06-09T10:15:00Z"
                  }
                }
              ]
            }
        """.trimIndent()

        val response = JsonInstant.decodeFromString<BrainyPalDueWrongQuestionReviewsResponse>(body)
        val item = response.items.single()

        assertEquals("wq_overdue", item.questionId)
        assertEquals("数学", item.subject)
        assertEquals("已到期", item.dueStatusLabel)
        assertEquals("2026-06-09T10:15:00Z", item.reviewSchedule.nextReviewDueAt)
        assertTrue(item.suggestedActions.single().contains("口述规则"))
    }

    @Test
    fun `parent task workbench decodes draft materials and recent tasks`() {
        val body = """
            {
              "draft_materials": [
                {
                  "material_id": "material_1",
                  "status": "draft",
                  "input_mode": "paste_text",
                  "material_type": "dictation",
                  "language": "zh-CN",
                  "title": "听写材料",
                  "items": [
                    {
                      "item_id": "dictation_1",
                      "text": "观察",
                      "expected_answer": "观察",
                      "source_ref": "paste_text:dictation:1"
                    }
                  ],
                  "sentences": ["我先观察，再记录。"],
                  "candidate_task_types": ["dictation"],
                  "source_refs": [],
                  "requires_parent_confirmation": true,
                  "created_at": "2026-06-13T10:00:00Z",
                  "updated_at": "2026-06-13T10:00:00Z"
                }
              ],
              "confirmed_materials": [],
              "pending_tasks": [
                {
                  "task_id": "task_pending_1",
                  "title": "口算待发任务",
                  "subject": "数学",
                  "mode": "practice",
                  "status": "draft",
                  "parent_status_label": "待发任务",
                  "source_refs": ["parent-import://import_1"],
                  "total_items": 2,
                  "child_visible": false
                }
              ],
              "recent_tasks": [
                {
                  "task_id": "practice_task_1",
                  "title": "今日听写",
                  "task_type": "dictation",
                  "status": "assigned",
                  "source_refs": ["material:material_1"],
                  "help_limit": 3,
                  "help_used": 0,
                  "items": []
                }
              ],
              "counts": {
                "draft_materials": 1,
                "confirmed_materials": 0,
                "pending_tasks": 1,
                "recent_tasks": 1
              }
            }
        """.trimIndent()

        val response = JsonInstant.decodeFromString<BrainyPalParentTaskWorkbenchResponse>(body)
        val workbench = BrainyPalParentWorkbench.from(response)

        assertEquals("1 份材料待确认", workbench.materialSummary)
        assertEquals("导入作业材料", workbench.structuredEntryLabel)
        assertEquals("听写材料", response.draftMaterials.single().title)
        assertEquals("观察", response.draftMaterials.single().items.single().text)
        assertEquals("口算待发任务", response.pendingTasks.single().title)
        assertEquals("待发任务", response.pendingTasks.single().statusLabel)
        assertFalse(response.pendingTasks.single().childVisible)
        assertEquals("今日听写", response.recentTasks.single().title)
    }

    @Test
    fun `parent import session encodes and decodes goal first contract`() {
        val request = BrainyPalParentImportSessionComposer.textRequest(
            entryGoal = "practice",
            title = "口算练习",
            subject = "数学",
            rawText = "1. 1+1=?\n2. 2+3=?",
        )

        val requestJson = JsonInstant.encodeToString(request)

        assertTrue(requestJson.contains("\"entry_goal\":\"practice\""))
        assertTrue(requestJson.contains("\"default_use\":\"prepare_task\""))
        assertTrue(requestJson.contains("\"raw_text\":\"1. 1+1=?\\n2. 2+3=?\""))

        val responseBody = """
            {
              "session_id": "import_1",
              "status": "needs_confirmation",
              "entry_goal": "practice",
              "input_mode": "paste",
              "default_use": "prepare_task",
              "title": "口算练习",
              "subject": "数学",
              "raw_text": "1. 1+1=?\n2. 2+3=?",
              "source_refs": ["parent-import://import_1"],
              "risk_flags": ["missing_reference_answer"],
              "candidates": [
                {
                  "candidate_id": "candidate_1",
                  "kind": "question",
                  "prompt": "1+1=?",
                  "risk_flags": ["missing_reference_answer"],
                  "source_refs": ["parent-import://import_1#candidate_1"]
                }
              ],
              "preview": {
                "task_type": "practice",
                "child_mode": "app",
                "requires_ocr_return": false,
                "estimated_minutes": 6,
                "send_label": "保存为待发任务"
              }
            }
        """.trimIndent()

        val session = JsonInstant.decodeFromString<BrainyPalParentImportSession>(responseBody)

        assertEquals("import_1", session.sessionId)
        assertEquals("保存为待发任务", session.preview.sendLabel)
        assertEquals("missing_reference_answer", session.riskFlags.single())
        assertEquals("1+1=?", session.candidates.single().prompt)
    }

    @Test
    fun `web material search decodes source uncertainty and confirmation contract`() {
        val requestJson = JsonInstant.encodeToString(
            BrainyPalParentWebMaterialSearchRequest(
                query = "web-search-public-domain-poem",
                subject = "语文",
                gradeBand = "小学",
                maxCandidates = 2,
            )
        )
        assertTrue(requestJson.contains("\"query\":\"web-search-public-domain-poem\""))
        assertTrue(requestJson.contains("\"grade_band\":\"小学\""))
        assertTrue(requestJson.contains("\"max_candidates\":2"))

        val body = """
            {
              "query": "web-search-public-domain-poem",
              "items": [
                {
                  "material_id": "material_web_1",
                  "status": "candidate",
                  "input_mode": "web_search",
                  "material_type": "reading_passage",
                  "subject": "语文",
                  "language": "zh-CN",
                  "title": "春晓（孟浩然）",
                  "raw_text": "春眠不觉晓\n处处闻啼鸟",
                  "items": [
                    {
                      "item_id": "item_1",
                      "text": "春眠不觉晓",
                      "expected_answer": "春眠不觉晓",
                      "source_ref": "https://zh.wikisource.org/wiki/春晓_(孟浩然)#line-1"
                    }
                  ],
                  "candidate_task_types": ["reading", "recitation"],
                  "source_refs": ["https://zh.wikisource.org/wiki/春晓_(孟浩然)"],
                  "source_candidates": [
                    {
                      "source_url": "https://zh.wikisource.org/wiki/春晓_(孟浩然)",
                      "title": "Wikisource：春晓",
                      "source_type": "public_domain_wikisource",
                      "snippet": "春眠不觉晓；处处闻啼鸟",
                      "uncertainty_note": "课本版本、标点和注释可能不同。"
                    }
                  ],
                  "search_query": "web-search-public-domain-poem",
                  "confidence": 0.72,
                  "uncertainty_note": "这是联网来源候选，请家长确认后再入库。",
                  "confirm_url": "/api/v1/parent/materials/material_web_1/confirm",
                  "requires_parent_confirmation": true
                }
              ]
            }
        """.trimIndent()

        val response = JsonInstant.decodeFromString<BrainyPalParentWebMaterialSearchResponse>(body)
        val material = response.items.single()

        assertEquals("web-search-public-domain-poem", response.query)
        assertEquals("candidate", material.status)
        assertEquals("web_search", material.inputMode)
        assertEquals("春晓（孟浩然）", material.title)
        assertEquals("reading", material.candidateTaskTypes.first())
        assertEquals(0.72f, material.confidence)
        assertTrue(material.uncertaintyNote.orEmpty().contains("家长确认"))
        assertEquals("/api/v1/parent/materials/material_web_1/confirm", material.confirmUrl)
        assertEquals("Wikisource：春晓", material.sourceCandidates.single().title)
        assertTrue(material.sourceCandidates.single().sourceUrl.startsWith("https://"))
    }

    @Test
    fun `parent chat trigger decodes confirmable import status and strategy cards`() {
        val requestJson = JsonInstant.encodeToString(
            BrainyPalParentChatTriggerRequest(
                message = "帮我布置第 12 课听写：观察、勇敢",
                subject = "语文",
            )
        )
        assertTrue(requestJson.contains("\"message\":\"帮我布置第 12 课听写：观察、勇敢\""))

        val importBody = """
            {
              "intent": "prepare_import",
              "requires_confirmation": true,
              "structured_action": {
                "type": "import_session",
                "label": "打开导入确认",
                "requires_confirmation": true
              },
              "import_session": {
                "session_id": "import_chat_1",
                "status": "needs_confirmation",
                "entry_goal": "dictation",
                "input_mode": "chat",
                "default_use": "dictation_material",
                "title": "聊天导入听写",
                "subject": "语文",
                "raw_text": "观察、勇敢",
                "preview": {
                  "task_type": "dictation",
                  "child_mode": "app",
                  "requires_ocr_return": true,
                  "estimated_minutes": 6,
                  "send_label": "保存为待发任务"
                }
              },
              "message": "我先整理成确认方案，家长确认后才会成为待发任务。"
            }
        """.trimIndent()
        val importTrigger = JsonInstant.decodeFromString<BrainyPalParentChatTriggerResponse>(importBody)

        assertEquals("prepare_import", importTrigger.intent)
        assertTrue(importTrigger.requiresConfirmation)
        assertEquals("import_session", importTrigger.structuredAction?.type)
        assertEquals("import_chat_1", importTrigger.importSession?.sessionId)
        assertEquals("dictation", importTrigger.importSession?.preview?.taskType)

        val strategyBody = """
            {
              "intent": "strategy_proposal",
              "requires_confirmation": true,
              "structured_action": {
                "type": "strategy_candidate",
                "label": "确认引导策略",
                "requires_confirmation": true
              },
              "strategy_candidate": {
                "status": "needs_confirmation",
                "parent_goal_text": "以后提示慢一点，多鼓励，但不要直接告诉答案。",
                "allowed_effects": ["hint_pacing", "encouragement_tone"],
                "child_answer_policy": "no_final_answers_before_submission",
                "confirmation_label": "确认应用策略"
              },
              "message": "我会先生成策略候选，家长确认后才会生效。"
            }
        """.trimIndent()
        val strategyTrigger = JsonInstant.decodeFromString<BrainyPalParentChatTriggerResponse>(strategyBody)

        assertEquals("strategy_proposal", strategyTrigger.intent)
        assertEquals("needs_confirmation", strategyTrigger.strategyCandidate?.status)
        assertTrue(strategyTrigger.strategyCandidate?.allowedEffects.orEmpty().contains("hint_pacing"))
        assertFalse(JsonInstant.encodeToString(strategyTrigger).contains("activated_strategy_id"))
    }

    @Test
    fun `parent photo scan decodes candidates and encodes selected confirmation`() {
        val body = """
            {
              "scan_id": "scan_20260606_093000",
              "captured_at": "2026-06-06T09:30:00Z",
              "candidates": [
                {
                  "candidate_id": "q1",
                  "question_number": "第1题",
                  "question_text": "a - (b - c + d)",
                  "child_answer": "a-b+c-d",
                  "work_observed": "有去括号步骤",
                  "status": "diagnosable",
                  "recommendation": "recommended",
                  "confidence": 0.95,
                  "verification": {
                    "reference_answer": "a-b+c-d",
                    "judgement": "correct",
                    "explanation": "孩子去括号结果与参考答案一致。",
                    "confidence": 0.92,
                    "requires_parent_review": false
                  }
                }
              ]
            }
        """.trimIndent()

        val snapshot = JsonInstant.decodeFromString<BrainyPalParentPhotoScanSnapshot>(body)
        val candidate = snapshot.candidates.single()

        assertEquals("scan_20260606_093000", snapshot.scanId)
        assertEquals("q1", candidate.candidateId)
        assertEquals("第1题", candidate.questionNumber)
        assertEquals("recommended", candidate.recommendation)
        assertEquals("correct", candidate.verification?.judgement)
        assertEquals(0.92f, candidate.verification?.confidence)

        val confirmationJson = JsonInstant.encodeToString(
            BrainyPalConfirmPhotoScanRequest(
                candidateIds = listOf("q1"),
                parentNote = "确认写入错题候选。",
            )
        )

        assertTrue(confirmationJson.contains("\"candidate_ids\":[\"q1\"]"))
        assertTrue(confirmationJson.contains("\"parent_note\":\"确认写入错题候选。\""))
    }

    @Test
    fun `pending task send request keeps overload confirmation explicit`() {
        val json = JsonInstant.encodeToString(
            BrainyPalSendPendingTaskRequest(confirmOverload = true)
        )

        assertTrue(json.contains("\"confirm_overload\":true"))
    }

    @Test
    fun `pending task update request encodes editable parent fields`() {
        val json = JsonInstant.encodeToString(
            BrainyPalUpdatePendingTaskRequest(
                title = "周末口算 8 题",
                instructions = "孩子先独立完成，提交后马上订正。",
            )
        )

        assertTrue(json.contains("\"title\":\"周末口算 8 题\""))
        assertTrue(json.contains("\"instructions\":\"孩子先独立完成，提交后马上订正。\""))
    }

    @Test
    fun `workload guard conflict parses agent error envelope`() {
        val body = """
            {
              "error": {
                "code": "workload_guard_requires_confirmation",
                "message": "今天已经有较多待完成任务，确认后仍可下发。",
                "details": {
                  "message": "今天已经有较多待完成任务，确认后仍可下发。",
                  "active_tasks": 3,
                  "estimated_minutes": 25,
                  "active_task_warning_limit": 3,
                  "estimated_minutes_warning_limit": 45
                }
              }
            }
        """.trimIndent()

        val conflict = BrainyPalParentWorkloadGuardConflict.fromErrorBody(body)

        assertEquals("今天已经有较多待完成任务，确认后仍可下发。", conflict?.message)
        assertEquals(3, conflict?.activeTasks)
        assertEquals(25, conflict?.estimatedMinutes)
        assertEquals(3, conflict?.activeTaskWarningLimit)
        assertEquals(45, conflict?.estimatedMinutesWarningLimit)
    }

    @Test
    fun `parent practice task creation encodes unified agent contract`() {
        val request = BrainyPalCreateParentPracticeTaskRequest(
            title = "第 12 课生字听写",
            subject = "语文",
            mode = "dictation",
            instructions = "听写时不显示答案，完成后拍照批改。",
            sourceRefs = listOf("material://paste/chinese-lesson-12"),
            activate = true,
            items = listOf(
                BrainyPalParentPracticeTaskItemRequest(
                    itemId = "w1",
                    kind = "dictation",
                    prompt = "溪流",
                    expectedAnswer = "溪流",
                    sourceRefs = listOf("material://paste/chinese-lesson-12#line-1"),
                )
            ),
        )

        val json = JsonInstant.encodeToString(request)

        assertTrue(json.contains("\"mode\":\"dictation\""))
        assertTrue(json.contains("\"source_refs\":[\"material://paste/chinese-lesson-12\"]"))
        assertTrue(json.contains("\"activate\":true"))
        assertTrue(json.contains("\"item_id\":\"w1\""))
    }

    @Test
    fun `parent practice task summary decodes unified agent board`() {
        val body = """
            {
              "draft_count": 1,
              "active_count": 2,
              "completed_count": 0,
              "total_count": 3,
              "latest_tasks": [
                {
                  "task_id": "task_abc",
                  "title": "第 12 课生字听写",
                  "subject": "语文",
                  "mode": "dictation",
                  "status": "active",
                  "source_refs": ["material://paste/chinese-lesson-12"],
                  "total_items": 2,
                  "child_visible": true
                }
              ]
            }
        """.trimIndent()

        val summary = JsonInstant.decodeFromString<BrainyPalParentPracticeTaskSummaryBoardResponse>(body)
        val task = summary.latestTasks.single()

        assertEquals(1, summary.draftCount)
        assertEquals(2, summary.activeCount)
        assertEquals(3, summary.totalCount)
        assertEquals("task_abc", task.taskId)
        assertEquals("听写任务", task.kindLabel)
        assertTrue(task.childVisible)
    }

    @Test
    fun `parent task detail decodes status and ocr review without api key`() {
        val body = """
            {
              "task_id": "task-dictation-1",
              "title": "今日听写",
              "task_type": "dictation",
              "status": "reviewing",
              "source_refs": ["inline:dictation"],
              "help_limit": 3,
              "help_used": 1,
              "parent_summary": "word_1 需要确认。",
              "items": [
                {
                  "item_id": "word_1",
                  "prompt": "apple",
                  "child_answer": "appel",
                  "result": "needs_manual_review",
                  "ocr_evidence": {
                    "image_ref": "local-cache://dictation/photo-1.jpg",
                    "recognized_text": "appel",
                    "confidence": 0.69,
                    "crop_ref": "local-cache://dictation/photo-1-word-1.jpg",
                    "confirmation_status": "unconfirmed"
                  }
                }
              ]
            }
        """.trimIndent()

        val task = JsonInstant.decodeFromString<BrainyPalChildPracticeTaskDetail>(body)
        val row = BrainyPalDictationOcrReview.rows(task).single()
        val summary = BrainyPalParentTaskSummary.from(task)

        assertEquals("讲评中", summary.statusLabel)
        assertEquals("听写任务", summary.kindLabel)
        assertEquals("1/3", summary.helpUsageLabel)
        assertEquals("查看裁剪区域", row.previewButtonLabel)
        assertTrue(row.requiresManualConfirmation)
        assertFalse(JsonInstant.encodeToString(summary).contains("brainypal-local"))
        assertFalse(JsonInstant.encodeToString(summary).contains("apiKey"))
    }
}

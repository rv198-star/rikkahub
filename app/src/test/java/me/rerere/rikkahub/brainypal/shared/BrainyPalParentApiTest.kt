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
        assertEquals("今日听写", response.recentTasks.single().title)
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

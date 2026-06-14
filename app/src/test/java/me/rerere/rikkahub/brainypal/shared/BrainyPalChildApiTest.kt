package me.rerere.rikkahub.brainypal.shared

import me.rerere.rikkahub.utils.JsonInstant
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrainyPalChildApiTest {
    @Test
    fun `practice task summary decodes child visible fields`() {
        val body = """
            {
              "items": [
                {
                  "task_id": "task-1",
                  "title": "今日练习",
                  "task_type": "wrong_question_practice",
                  "status": "assigned",
                  "item_count": 2,
                  "help_limit": 3,
                  "help_used": 1,
                  "blank_or_low_effort": false
                }
              ]
            }
        """.trimIndent()

        val response = JsonInstant.decodeFromString<BrainyPalChildPracticeTaskListResponse>(body)
        val task = response.items.single()

        assertEquals("task-1", task.taskId)
        assertEquals("今日练习", task.title)
        assertEquals(2, task.itemCount)
        assertEquals(2, task.remainingHelp)
        assertEquals("待开始", task.statusLabel)
        assertFalse(task.needsMoreEffort)
    }

    @Test
    fun `practice task detail decodes child workflow fields`() {
        val body = """
            {
              "task_id": "task-1",
              "title": "今日练习",
              "task_type": "wrong_question_practice",
              "status": "in_progress",
              "help_limit": 3,
              "help_used": 1,
              "help_message": "可以先想这个方向：括号前是负号时，括号内每一项都变号。",
              "blank_or_low_effort": true,
              "agent_policy_snapshot": {
                "task_phase": "attempt",
                "allowed_help_actions": ["read_question", "direction_hint"],
                "help_budget_state": "available",
                "attempt_evidence_status": "insufficient",
                "post_submit_review_allowed": false
              },
              "items": [
                {
                  "item_id": "item_1",
                  "prompt": "计算 x - (-y - z)",
                  "child_answer": "x+y+z",
                  "attempt_evidence": "我把括号里的符号变了。",
                  "result": "pending",
                  "correction_status": "not_started",
                  "blank_or_low_effort": true
                }
              ]
            }
        """.trimIndent()

        val task = JsonInstant.decodeFromString<BrainyPalChildPracticeTaskDetail>(body)

        assertEquals("task-1", task.taskId)
        assertEquals("进行中", task.statusLabel)
        assertEquals("可以先想这个方向：括号前是负号时，括号内每一项都变号。", task.helpMessage)
        assertEquals(2, task.remainingHelp)
        assertTrue(task.needsMoreEffort)
        assertEquals(false, task.agentPolicySnapshot.postSubmitReviewAllowed)
        assertEquals(listOf("read_question", "direction_hint"), task.agentPolicySnapshot.allowedHelpActions)
        assertEquals("item_1", task.items.single().itemId)
        assertEquals("计算 x - (-y - z)", task.items.single().prompt)
        assertEquals("x+y+z", task.items.single().childAnswer)
        assertEquals("我把括号里的符号变了。", task.items.single().attemptEvidence)
        assertTrue(task.items.single().needsMoreEffort)
    }

    @Test
    fun `review offer is actionable only with event`() {
        val actionableBody = """
            {
              "should_offer": true,
              "child_message": "要不要试一小步？",
              "event": {
                "related_question_id": "wq_due_001",
                "strategy_version_id": "strategy_review_prompt_1",
                "evidence_refs": ["wrong_question:wq_due_001"]
              }
            }
        """.trimIndent()
        val inactiveBody = """
            {
              "should_offer": true,
              "child_message": "要不要试一小步？",
              "event": null
            }
        """.trimIndent()

        val actionable = JsonInstant.decodeFromString<BrainyPalReviewOfferResponse>(actionableBody)
        val inactive = JsonInstant.decodeFromString<BrainyPalReviewOfferResponse>(inactiveBody)

        assertTrue(actionable.isActionable)
        assertEquals("wq_due_001", actionable.event?.relatedQuestionId)
        assertEquals("strategy_review_prompt_1", actionable.event?.strategyVersionId)
        assertEquals(listOf("wrong_question:wq_due_001"), actionable.event?.evidenceRefs)
        assertFalse(inactive.isActionable)
    }

    @Test
    fun `practice handoff code decodes web join fields`() {
        val body = """
            {
              "task_id": "task-1",
              "channel": "web",
              "handoff_code": "A1B2C3",
              "expires_at": "2026-06-14T09:15:00Z",
              "join_path": "/child/join?code=A1B2C3",
              "join_url": "http://192.168.5.80:8000/child/join?code=A1B2C3"
            }
        """.trimIndent()

        val response = JsonInstant.decodeFromString<BrainyPalPracticeHandoffCodeResponse>(body)

        assertEquals("task-1", response.taskId)
        assertEquals("web", response.channel)
        assertEquals("A1B2C3", response.handoffCode)
        assertEquals("/child/join?code=A1B2C3", response.joinPath)
        assertEquals("http://192.168.5.80:8000/child/join?code=A1B2C3", response.joinUrl)
    }

    @Test
    fun `practice task list decodes unified agent service contract`() {
        val body = """
            {
              "items": [
                {
                  "task_id": "task-equations",
                  "title": "一元一次方程练习",
                  "subject": "数学",
                  "mode": "practice",
                  "status": "available",
                  "total_items": 2,
                  "answered_items": 0,
                  "remaining_help": 2,
                  "submit_available": false,
                  "attempt_session_id": null
                }
              ]
            }
        """.trimIndent()

        val response = JsonInstant.decodeFromString<BrainyPalChildPracticeTaskListResponse>(body)
        val task = response.items.single()

        assertEquals("task-equations", task.taskId)
        assertEquals("一元一次方程练习", task.title)
        assertEquals("practice", task.taskType)
        assertEquals(2, task.itemCount)
        assertEquals(0, task.answeredItems)
        assertEquals(2, task.remainingHelp)
        assertFalse(task.submitAvailable)
        assertEquals("待查看", task.statusLabel)
    }

    @Test
    fun `practice task detail can edit in progress even when submit is not available`() {
        val body = """
            {
              "task_id": "task-equations",
              "attempt_session_id": "attempt_123",
              "status": "in_progress",
              "channel": "app",
              "help_budget": 2,
              "help_used": 0,
              "remaining_help": 2,
              "total_items": 2,
              "answered_items": 0,
              "submit_available": false,
              "task": {
                "task_id": "task-equations",
                "title": "一元一次方程练习",
                "subject": "数学",
                "mode": "practice",
                "instructions": "先独立完成",
                "items": []
              },
              "answers": {},
              "evidence_by_item": {},
              "result": null
            }
        """.trimIndent()

        val task = JsonInstant.decodeFromString<BrainyPalChildPracticeTaskDetail>(body)

        assertTrue(task.canEditAttempt)
        assertFalse(task.canSubmit)
    }

    @Test
    fun `practice task detail decodes unified attempt result contract`() {
        val body = """
            {
              "task_id": "task-equations",
              "attempt_session_id": "attempt_123",
              "status": "submitted",
              "channel": "app",
              "help_budget": 2,
              "help_used": 1,
              "remaining_help": 1,
              "total_items": 2,
              "answered_items": 2,
              "submit_available": false,
              "task": {
                "task_id": "task-equations",
                "title": "一元一次方程练习",
                "subject": "数学",
                "mode": "practice",
                "instructions": "先独立完成",
                "items": [
                  {
                    "item_id": "q1",
                    "kind": "short_answer",
                    "prompt": "解方程：x + 3 = 7",
                    "choices": [],
                    "source_refs": ["wrong_question://wq-1"]
                  }
                ]
              },
              "answers": {
                "q1": {
                  "item_id": "q1",
                  "value": "4",
                  "source": "app",
                  "updated_at": "2026-06-14T09:00:00+08:00"
                }
              },
              "evidence_by_item": {},
              "result": {
                "status": "completed",
                "child_summary": "第 1 题做对了。",
                "parent_summary": "本次练习 1 / 2 题已正确。",
                "item_results": {
                  "q1": {
                    "status": "correct",
                    "child_feedback": "这题思路和答案都对上了。",
                    "parent_note": "答案匹配标准答案。",
                    "evidence_source": "app",
                    "confidence": null,
                    "correction_prompt": null,
                    "expected_answer": "4",
                    "wrong_question_ref": "wrong_question://wq-1"
                  }
                },
                "review_blocks": [
                  {
                    "kind": "encouragement",
                    "title": "先看到已经完成的部分",
                    "body": "这次已经有 1 题对上了。",
                    "item_ids": ["q1"]
                  }
                ],
                "learning_record": {
                  "record_type": "practice",
                  "subject": "数学",
                  "source_refs": ["practice_task://task-equations"],
                  "child_summary": "第 1 题做对了。",
                  "parent_summary": "本次练习 1 / 2 题已正确。",
                  "knowledge_points": [],
                  "strategy_version_id": "strategy_math"
                },
                "created_at": "2026-06-14T09:01:00+08:00"
              }
            }
        """.trimIndent()

        val task = JsonInstant.decodeFromString<BrainyPalChildPracticeTaskDetail>(body)

        assertEquals("task-equations", task.taskId)
        assertEquals("attempt_123", task.attemptSessionId)
        assertEquals("一元一次方程练习", task.title)
        assertEquals("practice", task.taskType)
        assertEquals("数学", task.subject)
        assertEquals(2, task.helpLimit)
        assertEquals(1, task.remainingHelp)
        assertEquals(2, task.answeredItems)
        assertFalse(task.canEditAttempt)
        assertFalse(task.canSubmit)
        assertEquals("4", task.items.single().childAnswer)
        assertEquals("correct", task.result?.itemResults?.get("q1")?.status)
        assertEquals("4", task.result?.itemResults?.get("q1")?.expectedAnswer)
        assertEquals("wrong_question://wq-1", task.result?.itemResults?.get("q1")?.wrongQuestionRef)
        assertEquals("encouragement", task.result?.reviewBlocks?.single()?.kind)
        assertEquals("practice", task.result?.learningRecord?.recordType)
        assertEquals("strategy_math", task.result?.learningRecord?.strategyVersionId)
    }

    @Test
    fun `practice task action requests include attempt session id for agent service`() {
        val answerJson = JsonInstant.encodeToString(
            BrainyPalRecordPracticeTaskAnswerRequest(
                attemptSessionId = "attempt_123",
                answer = "4",
                source = "app",
            )
        )
        val helpJson = JsonInstant.encodeToString(
            BrainyPalRequestPracticeTaskHelpRequest(
                attemptSessionId = "attempt_123",
                itemId = "q1",
            )
        )
        val submitJson = JsonInstant.encodeToString(
            BrainyPalSubmitPracticeTaskRequest(attemptSessionId = "attempt_123")
        )

        assertTrue(answerJson.contains("\"attempt_session_id\":\"attempt_123\""))
        assertTrue(answerJson.contains("\"answer\":\"4\""))
        assertTrue(helpJson.contains("\"attempt_session_id\":\"attempt_123\""))
        assertTrue(submitJson.contains("\"attempt_session_id\":\"attempt_123\""))
    }

    @Test
    fun `agent task spec decodes dictation orchestration policy`() {
        val body = """
            {
              "task_id": "task-dictation-1",
              "title": "语文生字听写",
              "task_type": "dictation",
              "status": "pending",
              "help_limit": 3,
              "help_used": 0,
              "task_spec": {
                "subject": "chinese",
                "child_brief": "听写时不会显示答案，写完后再拍照批改。",
                "steps": [
                  {"step_id": "listen", "title": "听 BrainyPal 读", "action": "listen"},
                  {"step_id": "write", "title": "写在纸上", "action": "write"}
                ],
                "tts_policy": {
                  "language_mode": "chinese_vocab",
                  "repeat_count": 2,
                  "phrase_count": 4,
                  "interval_seconds": 4
                },
                "evidence_policy": {
                  "requires_ocr": true,
                  "preserve_image_evidence": true,
                  "manual_confirmation_required": "low_confidence_or_wrong"
                },
                "guardrails": {
                  "reveal_answer_before_submit": false,
                  "allow_direct_answer": false
                }
              },
              "items": [
                {"item_id": "dictation_1", "prompt": "观察"}
              ]
            }
        """.trimIndent()

        val task = JsonInstant.decodeFromString<BrainyPalChildPracticeTaskDetail>(body)

        assertEquals("待领取", task.statusLabel)
        assertEquals("语文生字听写", task.title)
        assertEquals("听写任务", task.taskKindLabel)
        assertEquals("chinese", task.taskSpec?.subject)
        assertEquals("chinese_vocab", task.taskSpec?.ttsPolicy?.languageMode)
        assertEquals(2, task.taskSpec?.ttsPolicy?.repeatCount)
        assertEquals(4, task.taskSpec?.ttsPolicy?.phraseCount)
        assertEquals(true, task.taskSpec?.evidencePolicy?.requiresOcr)
        assertEquals(false, task.taskSpec?.guardrails?.revealAnswerBeforeSubmit)
    }
}

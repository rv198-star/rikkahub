package me.rerere.rikkahub.brainypal

import me.rerere.rikkahub.utils.JsonInstant
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
}

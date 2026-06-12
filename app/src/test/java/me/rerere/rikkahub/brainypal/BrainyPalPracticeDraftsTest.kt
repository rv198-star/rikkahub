package me.rerere.rikkahub.brainypal

import org.junit.Assert.assertEquals
import org.junit.Test

class BrainyPalPracticeDraftsTest {
    @Test
    fun `server refresh preserves unsaved draft on another item`() {
        val detail = practiceDetail(
            item("item_1", childAnswer = "server answer 1", attemptEvidence = "server evidence 1"),
            item("item_2", childAnswer = null, attemptEvidence = null),
        )
        val drafts = BrainyPalPracticeDrafts()
            .replaceFromDetail(detail)
            .edit(
                itemId = "item_2",
                answer = "local answer 2",
                evidence = "local evidence 2",
            )

        val refreshedDetail = practiceDetail(
            item("item_1", childAnswer = "saved answer 1", attemptEvidence = "saved evidence 1"),
            item("item_2", childAnswer = null, attemptEvidence = null),
        )

        val refreshedDrafts = drafts.replaceFromDetail(refreshedDetail)

        assertEquals("saved answer 1", refreshedDrafts.get("item_1").answer)
        assertEquals("saved evidence 1", refreshedDrafts.get("item_1").evidence)
        assertEquals("local answer 2", refreshedDrafts.get("item_2").answer)
        assertEquals("local evidence 2", refreshedDrafts.get("item_2").evidence)
    }

    @Test
    fun `marking saved lets server value replace the saved item`() {
        val detail = practiceDetail(
            item("item_1", childAnswer = null, attemptEvidence = null),
        )
        val drafts = BrainyPalPracticeDrafts()
            .replaceFromDetail(detail)
            .edit(
                itemId = "item_1",
                answer = "local answer",
                evidence = "local evidence",
            )
            .markSaved(
                itemId = "item_1",
                savedAnswer = "local answer",
                savedEvidence = "local evidence",
            )

        val refreshedDrafts = drafts.replaceFromDetail(
            practiceDetail(
                item("item_1", childAnswer = "server answer", attemptEvidence = "server evidence"),
            )
        )

        assertEquals("server answer", refreshedDrafts.get("item_1").answer)
        assertEquals("server evidence", refreshedDrafts.get("item_1").evidence)
    }

    @Test
    fun `marking saved keeps newer local edits dirty`() {
        val detail = practiceDetail(
            item("item_1", childAnswer = null, attemptEvidence = null),
        )
        val drafts = BrainyPalPracticeDrafts()
            .replaceFromDetail(detail)
            .edit(
                itemId = "item_1",
                answer = "sent answer",
                evidence = "sent evidence",
            )
            .edit(
                itemId = "item_1",
                answer = "newer local answer",
                evidence = "newer local evidence",
            )
            .markSaved(
                itemId = "item_1",
                savedAnswer = "sent answer",
                savedEvidence = "sent evidence",
            )

        val refreshedDrafts = drafts.replaceFromDetail(
            practiceDetail(
                item("item_1", childAnswer = "server sent answer", attemptEvidence = "server sent evidence"),
            )
        )

        assertEquals("newer local answer", refreshedDrafts.get("item_1").answer)
        assertEquals("newer local evidence", refreshedDrafts.get("item_1").evidence)
    }

    private fun practiceDetail(vararg items: BrainyPalChildPracticeTaskItem): BrainyPalChildPracticeTaskDetail {
        return BrainyPalChildPracticeTaskDetail(
            taskId = "task-1",
            title = "今日练习",
            taskType = "wrong_question_practice",
            status = "in_progress",
            helpLimit = 3,
            helpUsed = 0,
            items = items.toList(),
        )
    }

    private fun item(
        itemId: String,
        childAnswer: String?,
        attemptEvidence: String?,
    ): BrainyPalChildPracticeTaskItem {
        return BrainyPalChildPracticeTaskItem(
            itemId = itemId,
            prompt = "题目 $itemId",
            childAnswer = childAnswer,
            attemptEvidence = attemptEvidence,
        )
    }
}

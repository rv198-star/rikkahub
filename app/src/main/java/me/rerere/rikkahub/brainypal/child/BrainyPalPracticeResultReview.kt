package me.rerere.rikkahub.brainypal.child

import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskDetail
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskItem
import me.rerere.rikkahub.brainypal.shared.BrainyPalPracticeTaskItemResult

data class BrainyPalPracticeResultReviewRow(
    val itemId: String,
    val title: String,
    val prompt: String,
    val status: String,
    val statusLabel: String,
    val feedback: String,
    val originalAnswer: String,
    val originalAnswerReadOnly: Boolean,
    val canCorrectNow: Boolean,
    val needsParentOrOcrReview: Boolean,
    val correctionPrompt: String,
    val expectedAnswerLabel: String?,
    val wrongQuestionRef: String?,
)

object BrainyPalPracticeResultReview {
    fun rows(detail: BrainyPalChildPracticeTaskDetail): List<BrainyPalPracticeResultReviewRow> {
        val result = detail.result ?: return emptyList()
        return detail.items.mapIndexed { index, item ->
            val itemResult = result.itemResults[item.itemId]
            row(
                index = index,
                item = item,
                itemResult = itemResult,
                originalAnswer = detail.answers[item.itemId]?.value ?: item.childAnswer.orEmpty(),
            )
        }
    }

    private fun row(
        index: Int,
        item: BrainyPalChildPracticeTaskItem,
        itemResult: BrainyPalPracticeTaskItemResult?,
        originalAnswer: String,
    ): BrainyPalPracticeResultReviewRow {
        val status = itemResult?.status ?: "ungraded"
        val needsReview = status == "needs_review"
        val canCorrect = status == "incorrect"
        return BrainyPalPracticeResultReviewRow(
            itemId = item.itemId,
            title = "第 ${index + 1} 题",
            prompt = item.prompt,
            status = status,
            statusLabel = statusLabel(status),
            feedback = itemResult?.childFeedback.orEmpty(),
            originalAnswer = originalAnswer,
            originalAnswerReadOnly = true,
            canCorrectNow = canCorrect,
            needsParentOrOcrReview = needsReview,
            correctionPrompt = itemResult?.correctionPrompt
                ?: if (canCorrect) "把原答案留着，在订正区重新写一次。" else "",
            expectedAnswerLabel = itemResult?.expectedAnswer
                ?.takeIf { it.isNotBlank() }
                ?.let { "正确答案：$it" },
            wrongQuestionRef = itemResult?.wrongQuestionRef,
        )
    }

    fun statusLabel(status: String): String {
        return when (status) {
            "correct" -> "做对了"
            "incorrect" -> "需要订正"
            "needs_review" -> "需要确认"
            else -> "待确认"
        }
    }
}

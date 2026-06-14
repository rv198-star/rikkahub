package me.rerere.rikkahub.brainypal.child

object BrainyPalPracticeActionFeedback {
    const val HELP_PENDING_MESSAGE = "BrainyPal 正在想提示..."
    const val HELP_SUCCESS_MESSAGE = "提示已显示在题目下方"
    const val HELP_EMPTY_MESSAGE = "这次没有拿到新的提示，请稍后再试。"
    const val SAVE_PENDING_MESSAGE = "正在保存答案..."
    const val SUBMIT_PENDING_MESSAGE = "正在提交练习..."
    private const val LOW_EFFORT_MESSAGE = "先写一个已知条件、尝试答案或卡住点，再提交。"

    fun pendingStatus(message: String): BrainyPalPracticeTaskActionStatus {
        return BrainyPalPracticeTaskActionStatus(message = message)
    }

    fun pendingHelpHintFor(
        helpItemId: String?,
        previousHint: BrainyPalPracticeTaskHelpHint?,
    ): BrainyPalPracticeTaskHelpHint? {
        val itemId = helpItemId ?: return previousHint
        return BrainyPalPracticeTaskHelpHint(
            itemId = itemId,
            message = HELP_PENDING_MESSAGE,
        )
    }

    fun helpHintFor(
        helpItemId: String?,
        helpMessage: String?,
        previousHint: BrainyPalPracticeTaskHelpHint?,
    ): BrainyPalPracticeTaskHelpHint? {
        val message = helpMessage?.takeIf { it.isNotBlank() } ?: return previousHint
        val itemId = helpItemId ?: return null
        return BrainyPalPracticeTaskHelpHint(
            itemId = itemId,
            message = message,
        )
    }

    fun resultStatus(
        successMessage: String,
        needsMoreEffort: Boolean,
        helpItemId: String?,
        helpMessage: String?,
    ): BrainyPalPracticeTaskActionStatus {
        if (needsMoreEffort) {
            return BrainyPalPracticeTaskActionStatus(
                message = LOW_EFFORT_MESSAGE,
                error = true,
            )
        }
        if (helpItemId != null && helpMessage.isNullOrBlank()) {
            return BrainyPalPracticeTaskActionStatus(
                message = HELP_EMPTY_MESSAGE,
                error = true,
            )
        }
        return BrainyPalPracticeTaskActionStatus(
            message = successMessage,
            error = false,
        )
    }
}

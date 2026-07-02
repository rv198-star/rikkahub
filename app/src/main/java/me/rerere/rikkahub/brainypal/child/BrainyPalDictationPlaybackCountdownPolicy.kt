package me.rerere.rikkahub.brainypal.child

data class BrainyPalDictationPlaybackCountdown(
    val stepLabel: String,
    val countdownLabel: String,
    val progress: Float,
)

object BrainyPalDictationPlaybackCountdownPolicy {
    fun modelForUtterance(
        utterances: List<String>,
        utteranceIndex: Int,
        millisUntilNext: Long,
        waitingForNextSegment: Boolean,
    ): BrainyPalDictationPlaybackCountdown? {
        val contentIndices = utterances.mapIndexedNotNull { index, utterance ->
            if (utterance.trim().isDictationContentUtterance()) index else null
        }
        val contentStepIndex = contentIndices.indexOf(utteranceIndex)
        if (contentStepIndex < 0) return null
        return model(
            stepIndex = contentStepIndex,
            stepCount = contentIndices.size,
            millisUntilNext = millisUntilNext,
            waitingForNextSegment = waitingForNextSegment,
        )
    }

    fun model(
        stepIndex: Int,
        stepCount: Int,
        millisUntilNext: Long,
        waitingForNextSegment: Boolean,
    ): BrainyPalDictationPlaybackCountdown? {
        if (stepCount <= 1) return null
        val normalizedIndex = stepIndex.coerceIn(0, stepCount - 1)
        val stepNumber = normalizedIndex + 1
        val countdownLabel = when {
            waitingForNextSegment && normalizedIndex < stepCount - 1 -> {
                val seconds = ((millisUntilNext.coerceAtLeast(1L) + 999L) / 1_000L).toInt()
                "$seconds 秒后继续"
            }

            normalizedIndex == stepCount - 1 -> "最后一段"
            else -> "正在播放"
        }
        return BrainyPalDictationPlaybackCountdown(
            stepLabel = "片段 $stepNumber / $stepCount",
            countdownLabel = countdownLabel,
            progress = (stepNumber.toFloat() / stepCount.toFloat()).coerceIn(0f, 1f),
        )
    }

    private fun String.isDictationContentUtterance(): Boolean {
        return isNotBlank() &&
            !isDictationIndexPrompt() &&
            !startsWith("写完后") &&
            !contains("拍照批改") &&
            !contains("播放完成")
    }

    private fun String.isDictationIndexPrompt(): Boolean {
        return startsWith("第 ") && endsWith(" 条")
    }
}

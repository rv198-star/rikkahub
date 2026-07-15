package me.rerere.rikkahub.brainypal.child

import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskDetail
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskItem

internal enum class BrainyPalOralTaskPhase {
    Reading,
    SentencePractice,
    Check,
}

internal data class BrainyPalOralTaskLine(
    val rawText: String,
    val visibleText: String,
    val isActive: Boolean,
    val isMasked: Boolean,
)

internal data class BrainyPalOralTaskUiModel(
    val phase: BrainyPalOralTaskPhase,
    val title: String,
    val hint: String,
    val lines: List<BrainyPalOralTaskLine>,
    val primaryActionLabel: String,
    val completeActionLabel: String,
    val showSourcePeek: Boolean,
    val showReflection: Boolean,
) {
    companion object {
        fun build(
            detail: BrainyPalChildPracticeTaskDetail,
            phase: BrainyPalOralTaskPhase,
            currentSentenceIndex: Int,
            revealCurrentSentence: Boolean,
            showSourcePeek: Boolean,
            reflectionUnlocked: Boolean,
        ): BrainyPalOralTaskUiModel {
            val sentences = splitSentences(detail.items)
                .ifEmpty { listOf(detail.title.takeIf { it.isNotBlank() } ?: "先试一小句") }
            val activeIndex = currentSentenceIndex.coerceIn(0, sentences.lastIndex)
            val lines = sentences.mapIndexed { index, sentence ->
                val isCurrent = index == activeIndex
                val isMasked = when (phase) {
                    BrainyPalOralTaskPhase.Reading -> false
                    BrainyPalOralTaskPhase.SentencePractice -> isCurrent && !revealCurrentSentence
                    BrainyPalOralTaskPhase.Check -> !showSourcePeek
                }
                BrainyPalOralTaskLine(
                    rawText = sentence,
                    visibleText = if (isMasked) "" else sentence,
                    isActive = phase == BrainyPalOralTaskPhase.SentencePractice && isCurrent,
                    isMasked = isMasked,
                )
            }

            return BrainyPalOralTaskUiModel(
                phase = phase,
                title = detail.title,
                hint = defaultHint(
                    taskType = detail.taskType,
                    phase = phase,
                    reflectionUnlocked = reflectionUnlocked,
                ),
                lines = lines,
                primaryActionLabel = if (detail.taskType == "recitation") "开始背诵" else "开始朗读",
                completeActionLabel = if (detail.taskType == "recitation") "我背完了" else "我读完了",
                showSourcePeek = showSourcePeek,
                showReflection = reflectionUnlocked,
            )
        }
    }
}

internal data class BrainyPalOralRecordingUiModel(
    val actionLabel: String,
    val supportingText: String,
) {
    companion object {
        fun build(
            isRecording: Boolean,
            audioUploaded: Boolean,
        ): BrainyPalOralRecordingUiModel {
            return when {
                isRecording -> BrainyPalOralRecordingUiModel(
                    actionLabel = "结束并上传",
                    supportingText = "正在录音，读完后点这里上传。",
                )

                audioUploaded -> BrainyPalOralRecordingUiModel(
                    actionLabel = "重新录音",
                    supportingText = "录音已上传，提交后会自动识别。",
                )

                else -> BrainyPalOralRecordingUiModel(
                    actionLabel = "开始录音",
                    supportingText = "准备好后录一遍，读完会自动上传。",
                )
            }
        }
    }
}

internal fun splitSentences(items: List<BrainyPalChildPracticeTaskItem>): List<String> {
    return items
        .flatMap { item ->
            item.prompt
                .lines()
                .flatMap { line ->
                    line.trim()
                        .split(Regex("""(?<=[，。！？；,.!?;])"""))
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                }
        }
}

private fun defaultHint(
    taskType: String,
    phase: BrainyPalOralTaskPhase,
    reflectionUnlocked: Boolean,
): String {
    if (reflectionUnlocked) {
        return "写一句自评，再记下哪里卡住。"
    }
    return when (phase) {
        BrainyPalOralTaskPhase.Reading -> "先看一遍，再试一句。"
        BrainyPalOralTaskPhase.SentencePractice -> "先抓住这一句，不着急。"
        BrainyPalOralTaskPhase.Check -> {
            if (taskType == "recitation") {
                "准备好再背，想看时点一下原文。"
            } else {
                "准备好再读，想看时点一下原文。"
            }
        }
    }
}

package me.rerere.rikkahub.brainypal.shared

object BrainyPalDictationSpeech {
    fun build(
        detail: BrainyPalChildPracticeTaskDetail,
        item: BrainyPalChildPracticeTaskItem,
        index: Int,
    ): String {
        val policy = detail.taskSpec?.ttsPolicy ?: BrainyPalTaskTtsPolicy()
        val repeatCount = policy.repeatCount.coerceAtLeast(1)
        val label = languageLabel(policy.languageMode)
        val prompt = item.prompt.trim()
        return buildString {
            append("第 ")
            append(index + 1)
            append(" 条，听")
            append(label)
            append("。")
            repeat(repeatCount) {
                append(prompt)
                append("。")
            }
            append("请写在纸上。")
        }
    }

    private fun languageLabel(languageMode: String): String {
        return when (languageMode) {
            "chinese_vocab" -> "中文生字"
            "english_word" -> "英语单词"
            else -> "听写内容"
        }
    }
}

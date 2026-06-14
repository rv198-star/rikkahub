package me.rerere.rikkahub.brainypal.shared

object BrainyPalRecitationSpeech {
    fun build(detail: BrainyPalChildPracticeTaskDetail): String {
        return buildString {
            appendSentence(detail.title)
            detail.taskSpec
                ?.childBrief
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?.let { appendSentence(it) }
            val steps = detail.taskSpec
                ?.steps
                ?.map { it.title.trim() }
                ?.filter { it.isNotBlank() }
                .orEmpty()
            if (steps.isNotEmpty()) {
                append("步骤：")
                append(steps.joinToString("；"))
                append("。")
            }
            val material = detail.items
                .joinToString("。") { it.prompt.trim() }
                .trim()
            if (material.isNotBlank()) {
                append("材料：")
                append(cleanTerminalPunctuation(material))
                append("。")
            }
        }
    }

    private fun StringBuilder.appendSentence(text: String) {
        val clean = cleanTerminalPunctuation(text.trim())
        if (clean.isBlank()) return
        append(clean)
        append("。")
    }

    private fun cleanTerminalPunctuation(text: String): String {
        return text.trim().trimEnd('。', '.', '！', '!', '？', '?')
    }
}

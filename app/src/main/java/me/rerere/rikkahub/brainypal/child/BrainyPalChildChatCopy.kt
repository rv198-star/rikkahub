package me.rerere.rikkahub.brainypal.child

object BrainyPalChildChatCopy {
    const val emptyTitle = "把想法说出来就好"
    const val emptyDetail = "可以先告诉我题目，也可以只说你想到的一步。"

    val starterPrompts = listOf(
        "我从哪里开始想？",
        "我这样想对吗？",
        "我想先听一个小提示",
    )

    fun isWarmHintPrompt(prompt: String): Boolean {
        return prompt.contains("提示")
    }
}

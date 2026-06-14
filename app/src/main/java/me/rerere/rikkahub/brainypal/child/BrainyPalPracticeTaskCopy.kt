package me.rerere.rikkahub.brainypal.child

object BrainyPalPracticeTaskCopy {
    fun itemCountLabel(taskType: String, itemCount: Int): String {
        return "$itemCount ${itemUnit(taskType)}"
    }

    private fun itemUnit(taskType: String): String {
        return when (taskType) {
            "dictation" -> "条"
            "recitation" -> "段"
            else -> "题"
        }
    }
}

package me.rerere.rikkahub.brainypal.child

object BrainyPalPracticeTaskLifecycle {
    fun shouldAcceptOnOpen(status: String): Boolean {
        return status in setOf("pending", "assigned")
    }
}

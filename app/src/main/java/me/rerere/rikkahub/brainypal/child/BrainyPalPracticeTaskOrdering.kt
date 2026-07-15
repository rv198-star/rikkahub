package me.rerere.rikkahub.brainypal.child

import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskSummary

object BrainyPalPracticeTaskOrdering {
    private val resumableStatuses = setOf("in_progress", "paused", "accepted")
    private val startableStatuses = setOf("available", "pending", "assigned")
    private val feedbackStatuses = setOf("submitted", "reviewing")
    val actionableStatuses: Set<String> = resumableStatuses + startableStatuses

    fun forChild(tasks: List<BrainyPalChildPracticeTaskSummary>): List<BrainyPalChildPracticeTaskSummary> {
        return tasks
            .mapIndexed { index, task -> IndexedTask(index, task) }
            .sortedWith(
                compareBy<IndexedTask> { rank(it.task) }
                    .thenBy { it.index }
            )
            .map { it.task }
    }

    fun isActionable(task: BrainyPalChildPracticeTaskSummary): Boolean {
        return task.status in actionableStatuses || task.needsMoreEffort
    }

    private fun rank(task: BrainyPalChildPracticeTaskSummary): Int {
        return when {
            task.status == "in_progress" || task.needsMoreEffort -> 0
            task.status == "paused" || task.status == "accepted" -> 1
            task.status == "available" -> 2
            task.status == "pending" -> 3
            task.status == "assigned" -> 4
            task.status in feedbackStatuses -> 5
            else -> 6
        }
    }

    private data class IndexedTask(
        val index: Int,
        val task: BrainyPalChildPracticeTaskSummary,
    )
}

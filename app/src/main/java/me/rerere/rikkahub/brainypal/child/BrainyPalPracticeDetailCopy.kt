package me.rerere.rikkahub.brainypal.child

import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskDetail

object BrainyPalPracticeDetailCopy {
    fun supportingText(
        detail: BrainyPalChildPracticeTaskDetail,
        interactionPlan: BrainyPalChildTaskInteractionPlan,
    ): String {
        val countLabel = BrainyPalPracticeTaskCopy.itemCountLabel(
            taskType = detail.taskType,
            itemCount = detail.items.size,
        )
        val helpLabel = if (detail.taskType == "recitation") {
            ""
        } else {
            " · 提示券 ${detail.remainingHelp}/${detail.helpLimit}"
        }
        return "${detail.statusLabel} · $countLabel$helpLabel\n${interactionPlan.brief}"
    }

}

package me.rerere.rikkahub.brainypal.child

import me.rerere.rikkahub.brainypal.shared.BrainyPalChildConnectionConfig
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildModePolicy
import me.rerere.rikkahub.brainypal.shared.BrainyPalPracticeHandoffCodeResponse

data class BrainyPalPracticeHandoffDisplay(
    val title: String,
    val code: String,
    val instruction: String,
    val joinUrl: String,
)

object BrainyPalPracticeExternalWork {
    fun printablePdfUrl(
        config: BrainyPalChildConnectionConfig,
        taskId: String,
    ): String {
        return "${BrainyPalChildModePolicy.agentServiceRootUrl(config)}" +
            "/api/v1/child/practice-tasks/$taskId/printable.pdf"
    }

    fun handoffDisplay(response: BrainyPalPracticeHandoffCodeResponse): BrainyPalPracticeHandoffDisplay {
        return BrainyPalPracticeHandoffDisplay(
            title = "电脑完成",
            code = response.handoffCode,
            instruction = "电脑打开 ${response.joinUrl}，输入 ${response.handoffCode} 继续作答。",
            joinUrl = response.joinUrl,
        )
    }
}

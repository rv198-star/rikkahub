package me.rerere.rikkahub.brainypal

data class BrainyPalChildTextBlock(
    val title: String,
    val detail: String,
)

object BrainyPalChildUiText {
    fun connectionStatus(config: BrainyPalChildConnectionConfig): BrainyPalChildTextBlock {
        if (!config.isConfigured()) {
            return BrainyPalChildTextBlock(
                title = "需要家长连接 BrainyPal",
                detail = "配置后这里会显示今天的任务。",
            )
        }
        return BrainyPalChildTextBlock(
            title = "已连接 Agent Service",
            detail = config.baseUrl.trim(),
        )
    }

    fun homeErrorRecovery(errorMessage: String?): String {
        return if (errorMessage.isNullOrBlank()) {
            ""
        } else {
            "今日练习暂时没取到，聊天仍可用。请点刷新，或请家长检查服务。"
        }
    }

    fun practiceEmptyMessage(errorMessage: String?): BrainyPalChildTextBlock {
        if (!errorMessage.isNullOrBlank()) {
            return BrainyPalChildTextBlock(
                title = "今日练习暂时没取到",
                detail = "请点刷新，或请家长检查服务。",
            )
        }
        return BrainyPalChildTextBlock(
            title = "现在没有新的今日练习",
            detail = "可以先去问问 BrainyPal，或者晚点刷新任务。",
        )
    }
}

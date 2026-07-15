package me.rerere.rikkahub.brainypal.child

import me.rerere.rikkahub.brainypal.shared.BrainyPalAchievementStationHistoryItem
import me.rerere.rikkahub.brainypal.shared.BrainyPalAchievementStationModule
import me.rerere.rikkahub.brainypal.shared.BrainyPalAchievementStationResponse
import me.rerere.rikkahub.brainypal.shared.theme.BrainyPalTokens

enum class BrainyPalStationVisualTone {
    STEADY_CYAN,
    REPAIR_AMBER,
    QUIET,
}

enum class BrainyPalStationPulse {
    IDLE,
    SIGNAL,
    SPARK,
}

data class BrainyPalStationMotion(
    val visualTone: BrainyPalStationVisualTone,
    val pulse: BrainyPalStationPulse,
    val durationMillis: Int,
)

data class BrainyPalStationDisplay(
    val title: String,
    val subtitle: String,
    val dailyHeadline: String,
    val dailyBody: String,
    val dailyStatus: String,
    val visibleAcknowledgements: Int,
    val modules: List<BrainyPalStationModuleDisplay>,
    val history: List<BrainyPalStationHistoryDisplay>,
    val historyEmptyText: String,
    val animationCue: String,
    val motion: BrainyPalStationMotion,
) {
    val childSafeText: String
        get() = buildString {
            append(title)
            append(subtitle)
            append(dailyHeadline)
            append(dailyBody)
            modules.forEach { module ->
                append(module.label)
                append(module.currentRungLabel)
                append(module.nextRungHint.orEmpty())
            }
            history.forEach { item ->
                append(item.title)
                append(item.body)
                append(item.sourceLabel)
            }
        }
}

data class BrainyPalStationModuleDisplay(
    val moduleId: String,
    val label: String,
    val status: String,
    val visibleCount: Int,
    val currentRungLabel: String,
    val nextRungHint: String?,
    val rungLabels: List<String>,
    val completedRungCount: Int,
    val totalRungCount: Int,
    val progress: Float,
    val animationCue: String,
    val visualTone: BrainyPalStationVisualTone,
    val motion: BrainyPalStationMotion,
)

data class BrainyPalStationHistoryDisplay(
    val eventId: String,
    val title: String,
    val body: String,
    val moduleLabel: String,
    val sourceLabel: String,
    val occurredAt: String,
    val animationCue: String,
    val visualTone: BrainyPalStationVisualTone,
    val motion: BrainyPalStationMotion,
)

object BrainyPalStationUiModel {
    private val moduleFallbackLabels = mapOf(
        "bravery_core" to "勇气核心",
        "navigation" to "导航模块",
        "modeling" to "建模模块",
        "repair" to "修复模块",
        "communication" to "沟通模块",
    )

    fun from(response: BrainyPalAchievementStationResponse): BrainyPalStationDisplay {
        val modules = response.modules.map(::moduleDisplay)
        val moduleLabels = modules.associate { it.moduleId to it.label }
        val animationCue = response.animationCue.ifBlank { "station_idle" }
        return BrainyPalStationDisplay(
            title = response.title.ifBlank { BrainyPalTokens.stationFullName },
            subtitle = response.subtitle.ifBlank {
                "记录愿意开始、说出卡点和提示后再试的小信号。"
            },
            dailyHeadline = response.daily.headline.ifBlank { "今天还在待机" },
            dailyBody = response.daily.body.ifBlank { "从一个很小的开始就可以。" },
            dailyStatus = response.daily.status,
            visibleAcknowledgements = response.daily.visibleAcknowledgements,
            modules = modules,
            history = response.history.map { historyItem ->
                historyDisplay(historyItem, moduleLabels)
            },
            historyEmptyText = "先完成一次小任务，空间站就会留下可回看的记录。",
            animationCue = animationCue,
            motion = motionForCue(animationCue),
        )
    }

    private fun moduleDisplay(module: BrainyPalAchievementStationModule): BrainyPalStationModuleDisplay {
        val total = module.rungs.size.coerceAtLeast(1)
        val completed = module.rungs.count { it.state == "completed" }
        val animationCue = module.animationCue.ifBlank { "module_idle" }
        val motion = motionForCue(animationCue)
        return BrainyPalStationModuleDisplay(
            moduleId = module.moduleId,
            label = module.label.ifBlank { moduleFallbackLabels[module.moduleId] ?: "成长模块" },
            status = module.status,
            visibleCount = module.visibleCount,
            currentRungLabel = module.currentRung.label.ifBlank { "先试一步" },
            nextRungHint = module.nextRungHint?.takeIf { it.isNotBlank() },
            rungLabels = module.rungs.map { it.label.ifBlank { "下一小步" } },
            completedRungCount = completed,
            totalRungCount = total,
            progress = (completed.toFloat() / total.toFloat()).coerceIn(0f, 1f),
            animationCue = animationCue,
            visualTone = motion.visualTone,
            motion = motion,
        )
    }

    private fun historyDisplay(
        item: BrainyPalAchievementStationHistoryItem,
        moduleLabels: Map<String, String>,
    ): BrainyPalStationHistoryDisplay {
        val animationCue = item.animationCue.ifBlank { "signal_pulse" }
        val motion = motionForCue(animationCue)
        return BrainyPalStationHistoryDisplay(
            eventId = item.eventId,
            title = item.title.ifBlank { "收到稳定信号" },
            body = item.body.ifBlank { "记录了一次愿意继续的小动作。" },
            moduleLabel = moduleLabels[item.moduleId] ?: moduleFallbackLabels[item.moduleId] ?: "成长模块",
            sourceLabel = item.sourceLabel.ifBlank { "学习任务" },
            occurredAt = item.occurredAt,
            animationCue = animationCue,
            visualTone = motion.visualTone,
            motion = motion,
        )
    }

    fun motionForCue(cue: String): BrainyPalStationMotion {
        return when (cue) {
            "repair_spark" -> BrainyPalStationMotion(
                visualTone = BrainyPalStationVisualTone.REPAIR_AMBER,
                pulse = BrainyPalStationPulse.SPARK,
                durationMillis = 900,
            )

            "navigation_ping",
            "signal_wave",
            "module_pulse",
            "stable_signal" -> BrainyPalStationMotion(
                visualTone = BrainyPalStationVisualTone.STEADY_CYAN,
                pulse = BrainyPalStationPulse.SIGNAL,
                durationMillis = 1200,
            )

            else -> BrainyPalStationMotion(
                visualTone = BrainyPalStationVisualTone.QUIET,
                pulse = BrainyPalStationPulse.IDLE,
                durationMillis = 1600,
            )
        }
    }
}

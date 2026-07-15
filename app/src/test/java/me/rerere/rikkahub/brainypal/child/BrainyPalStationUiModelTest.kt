package me.rerere.rikkahub.brainypal.child

import me.rerere.rikkahub.brainypal.shared.BrainyPalAchievementStationDaily
import me.rerere.rikkahub.brainypal.shared.BrainyPalAchievementStationHistoryItem
import me.rerere.rikkahub.brainypal.shared.BrainyPalAchievementStationModule
import me.rerere.rikkahub.brainypal.shared.BrainyPalAchievementStationResponse
import me.rerere.rikkahub.brainypal.shared.BrainyPalAchievementStationRung
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class BrainyPalStationUiModelTest {
    @Test
    fun `station display model keeps daily modules and history child safe`() {
        val display = BrainyPalStationUiModel.from(
            BrainyPalAchievementStationResponse(
                title = "勇气空间站",
                subtitle = "记录愿意开始和提示后再试的小信号。",
                daily = BrainyPalAchievementStationDaily(
                    status = "stable",
                    visibleAcknowledgements = 2,
                    headline = "今天收到 2 次稳定信号",
                    body = "空间站已经记录了你愿意继续的一小步。",
                ),
                modules = listOf(
                    BrainyPalAchievementStationModule(
                        moduleId = "repair",
                        label = "修复模块",
                        status = "stable",
                        visibleCount = 1,
                        currentRung = BrainyPalAchievementStationRung(
                            rungId = "repair.retry_after_hint",
                            label = "提示后再试一次",
                            state = "completed",
                        ),
                        nextRungHint = "下一步：完成一次订正",
                        rungs = listOf(
                            BrainyPalAchievementStationRung(
                                rungId = "repair.retry_after_hint",
                                label = "提示后再试一次",
                                state = "completed",
                            ),
                            BrainyPalAchievementStationRung(
                                rungId = "repair.first_correction",
                                label = "完成一次订正",
                                state = "locked",
                            ),
                        ),
                        animationCue = "repair_spark",
                    )
                ),
                history = listOf(
                    BrainyPalAchievementStationHistoryItem(
                        eventId = "evt_retry",
                        title = "收到稳定信号",
                        body = "你用了提示后又试了一次。",
                        moduleId = "repair",
                        status = "stable",
                        occurredAt = "2026-06-15T09:10:00+00:00",
                        sourceLabel = "练习任务",
                        taskId = "task-1",
                        animationCue = "repair_spark",
                    )
                ),
                animationCue = "stable_signal",
            )
        )

        assertEquals("勇气空间站", display.title)
        assertEquals("今天收到 2 次稳定信号", display.dailyHeadline)
        assertEquals("repair", display.modules.single().moduleId)
        assertEquals(0.5f, display.modules.single().progress)
        assertEquals("提示后再试一次", display.modules.single().currentRungLabel)
        assertEquals(BrainyPalStationVisualTone.REPAIR_AMBER, display.modules.single().visualTone)
        assertEquals(BrainyPalStationPulse.SPARK, display.modules.single().motion.pulse)
        assertEquals("evt_retry", display.history.single().eventId)
        assertEquals(BrainyPalStationVisualTone.REPAIR_AMBER, display.history.single().visualTone)
        assertFalse(display.childSafeText.contains("积分"))
        assertFalse(display.childSafeText.contains("金币"))
        assertFalse(display.childSafeText.contains("排行榜"))
    }

    @Test
    fun `station display model provides gentle empty history copy`() {
        val display = BrainyPalStationUiModel.from(
            BrainyPalAchievementStationResponse(
                daily = BrainyPalAchievementStationDaily(
                    visibleAcknowledgements = 0,
                    headline = "今天还在待机",
                    body = "从一个很小的开始就可以。",
                ),
            )
        )

        assertEquals("先完成一次小任务，空间站就会留下可回看的记录。", display.historyEmptyText)
        assertEquals("station_idle", display.animationCue)
        assertEquals(BrainyPalStationVisualTone.QUIET, display.motion.visualTone)
        assertEquals(BrainyPalStationPulse.IDLE, display.motion.pulse)
    }

    @Test
    fun `station animation cues map to stable visual motion semantics`() {
        assertEquals(
            BrainyPalStationMotion(
                visualTone = BrainyPalStationVisualTone.REPAIR_AMBER,
                pulse = BrainyPalStationPulse.SPARK,
                durationMillis = 900,
            ),
            BrainyPalStationUiModel.motionForCue("repair_spark"),
        )
        assertEquals(
            BrainyPalStationMotion(
                visualTone = BrainyPalStationVisualTone.STEADY_CYAN,
                pulse = BrainyPalStationPulse.SIGNAL,
                durationMillis = 1200,
            ),
            BrainyPalStationUiModel.motionForCue("module_pulse"),
        )
        assertEquals(
            BrainyPalStationMotion(
                visualTone = BrainyPalStationVisualTone.QUIET,
                pulse = BrainyPalStationPulse.IDLE,
                durationMillis = 1600,
            ),
            BrainyPalStationUiModel.motionForCue("unknown_cue"),
        )
    }
}

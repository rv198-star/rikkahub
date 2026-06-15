package me.rerere.rikkahub.brainypal.child

import me.rerere.rikkahub.brainypal.shared.BrainyPalAchievementMoment
import me.rerere.rikkahub.brainypal.shared.BrainyPalBraveryStationDailyState
import me.rerere.rikkahub.brainypal.shared.BrainyPalBraveryStationState
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskDetail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BrainyPalPracticeAchievementFeedbackTest {
    @Test
    fun `feedback is visible for submitted task with achievement moment`() {
        val model = BrainyPalPracticeAchievementFeedback.model(
            detail = detail(
                status = "submitted",
                achievementMoment = BrainyPalAchievementMoment(
                    title = "收到稳定信号",
                    body = "你今天完成了这一步，空间站已经记录。",
                    moduleId = "maintenance_loop",
                    status = "stable",
                    tags = listOf("daily"),
                ),
                stationState = BrainyPalBraveryStationState(
                    childId = "default-child",
                    daily = BrainyPalBraveryStationDailyState(
                        day = "2026-06-15",
                        visibleAcknowledgements = 2,
                        status = "stable",
                    ),
                ),
            )
        )

        assertEquals("勇气空间站", model?.sectionTitle)
        assertEquals("收到稳定信号", model?.headline)
        assertEquals("你今天完成了这一步，空间站已经记录。", model?.body)
        assertEquals("今日已记录 2 次稳定行动", model?.statusLine)
    }

    @Test
    fun `feedback is hidden when task needs more effort`() {
        val model = BrainyPalPracticeAchievementFeedback.model(
            detail = detail(
                status = "submitted",
                blankOrLowEffort = true,
                achievementMoment = BrainyPalAchievementMoment(
                    title = "不应展示",
                    body = "低努力提交不应被客户端强行鼓励。",
                    moduleId = "maintenance_loop",
                    status = "stable",
                ),
            )
        )

        assertNull(model)
    }

    @Test
    fun `feedback is hidden before submit even when stale moment exists`() {
        val model = BrainyPalPracticeAchievementFeedback.model(
            detail = detail(
                status = "in_progress",
                achievementMoment = BrainyPalAchievementMoment(
                    title = "收到稳定信号",
                    body = "这可能是旧缓存，进行中不展示。",
                    moduleId = "maintenance_loop",
                    status = "stable",
                ),
            )
        )

        assertNull(model)
    }

    private fun detail(
        status: String,
        blankOrLowEffort: Boolean = false,
        achievementMoment: BrainyPalAchievementMoment? = null,
        stationState: BrainyPalBraveryStationState? = null,
    ): BrainyPalChildPracticeTaskDetail {
        return BrainyPalChildPracticeTaskDetail(
            taskId = "task-1",
            title = "任务",
            taskType = "practice",
            status = status,
            helpLimit = 2,
            helpUsed = 0,
            blankOrLowEffort = blankOrLowEffort,
            achievementMoment = achievementMoment,
            stationState = stationState,
        )
    }
}

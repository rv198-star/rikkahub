package me.rerere.rikkahub.brainypal.child

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrainyPalHomePageVisualSemanticsTest {
    @Test
    fun `home sections keep companion station actions and task order`() {
        val semantics = BrainyPalHomePageVisualSemantics.default

        assertEquals(
            listOf("companion", "yongqi_station", "primary_actions", "review_offer", "today_tasks", "stable_navigation"),
            semantics.sectionOrder,
        )
        assertEquals("yongqi_station", semantics.courageStationSectionId)
        assertEquals("today_tasks", semantics.todayTaskSectionId)
        assertEquals("warm_companion", semantics.heroTone)
    }

    @Test
    fun `home visual semantics caps task preview density and avoids reward language`() {
        val semantics = BrainyPalHomePageVisualSemantics.default

        assertEquals(3, semantics.maxPreviewTasks)
        assertEquals("calm_primary", semantics.primaryActionTone)
        assertEquals("gentle_secondary", semantics.secondaryActionTone)
        assertFalse(semantics.childSafeText.contains("积分"))
        assertFalse(semantics.childSafeText.contains("金币"))
        assertFalse(semantics.childSafeText.contains("排行榜"))
    }

    @Test
    fun `home root does not expose a dead back button`() {
        val semantics = BrainyPalHomePageVisualSemantics.default

        assertFalse(semantics.showBackNavigation)
        assertTrue(semantics.showParentEntry)
    }
}

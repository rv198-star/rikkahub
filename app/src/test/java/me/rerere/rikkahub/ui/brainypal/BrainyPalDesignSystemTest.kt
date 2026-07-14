package me.rerere.rikkahub.ui.brainypal

import me.rerere.rikkahub.ui.brainypal.designsystem.BrainyPalContentState
import me.rerere.rikkahub.ui.brainypal.designsystem.BrainyPalMotion
import me.rerere.rikkahub.ui.brainypal.designsystem.BrainyPalSizes
import me.rerere.rikkahub.ui.brainypal.designsystem.components.accessibleLabel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BrainyPalDesignSystemTest {
    @Test
    fun `touch and icon tokens follow the Android contract`() {
        assertTrue(BrainyPalSizes.MinimumTouchTarget.value >= 48f)
        assertEquals(24f, BrainyPalSizes.IconDefault.value)
        assertEquals(840f, BrainyPalSizes.TabletBreakpoint.value)
    }

    @Test
    fun `motion durations stay within the approved scale`() {
        assertEquals(listOf(120, 180, 240), listOf(
            BrainyPalMotion.FastMillis,
            BrainyPalMotion.StandardMillis,
            BrainyPalMotion.EmphasisMillis,
        ))
    }

    @Test
    fun `every base state has a non-empty spoken label`() {
        BrainyPalContentState.entries.forEach { state ->
            assertTrue(state.accessibleLabel.isNotBlank())
        }
    }
}

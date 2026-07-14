package me.rerere.rikkahub.brainypal.child

import org.junit.Assert.assertEquals
import org.junit.Test

class BrainyPalChildNavigationPolicyTest {
    @Test
    fun `phones use stable bottom navigation`() {
        assertEquals(
            BrainyPalChildNavigationMode.BottomBar,
            BrainyPalChildNavigationPolicy.modeForWidth(839f),
        )
    }

    @Test
    fun `tablets use navigation rail at breakpoint`() {
        assertEquals(
            BrainyPalChildNavigationMode.Rail,
            BrainyPalChildNavigationPolicy.modeForWidth(840f),
        )
    }
}

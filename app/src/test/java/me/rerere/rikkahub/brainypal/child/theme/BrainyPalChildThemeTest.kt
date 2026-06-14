package me.rerere.rikkahub.brainypal.child.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class BrainyPalChildThemeTest {
    @Test
    fun `light palette blends night navigation base with amber action energy`() {
        val scheme = BrainyPalChildTheme.lightColorScheme

        assertEquals(Color(0xFFF5F7FC), scheme.background)
        assertEquals(Color(0xFF172554), scheme.primary)
        assertEquals(Color(0xFF06B6D4), BrainyPalChildTheme.cyanAccent)
        assertEquals(Color(0xFFF59E0B), BrainyPalChildTheme.amberAccent)
        assertEquals(Color(0xFFFFF1D6), scheme.tertiaryContainer)
        assertNotEquals(
            "Amber should stay an accent instead of becoming the main action color.",
            BrainyPalChildTheme.amberAccent,
            scheme.primary,
        )
    }

    @Test
    fun `dark palette keeps amber as warm accent on a deep study surface`() {
        val scheme = BrainyPalChildTheme.darkColorScheme

        assertEquals(Color(0xFF08111F), scheme.background)
        assertEquals(Color(0xFFB7C7FF), scheme.primary)
        assertEquals(Color(0xFFF6C86C), scheme.tertiary)
        assertEquals(Color(0xFF704000), scheme.tertiaryContainer)
    }
}

package me.rerere.rikkahub.brainypal.child.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class BrainyPalChildThemeTest {
    @Test
    fun `light palette follows BrainyPal Orbit design card`() {
        val scheme = BrainyPalChildTheme.lightColorScheme

        assertEquals(Color(0xFFF5F8FF), scheme.background)
        assertEquals(Color(0xFF254EDB), scheme.primary)
        assertEquals(Color(0xFF18B8C6), BrainyPalChildTheme.cyanAccent)
        assertEquals(Color(0xFFF6B93B), BrainyPalChildTheme.amberAccent)
        assertEquals(Color(0xFFE86F50), BrainyPalChildTheme.reviewCoral)
        assertEquals(Color(0xFF2ABFA3), BrainyPalChildTheme.clearMint)
        assertEquals(Color(0xFFFFE9B8), scheme.tertiaryContainer)
        assertNotEquals(
            "Amber should stay an accent instead of becoming the main action color.",
            BrainyPalChildTheme.amberAccent,
            scheme.primary,
        )
        assertNotEquals("Old navy base should not return.", Color(0xFF172554), scheme.primary)
    }

    @Test
    fun `dark palette keeps Orbit accents on a deep space surface`() {
        val scheme = BrainyPalChildTheme.darkColorScheme

        assertEquals(Color(0xFF07111F), scheme.background)
        assertEquals(Color(0xFF86A8FF), scheme.primary)
        assertEquals(Color(0xFF49D5E8), scheme.secondary)
        assertEquals(Color(0xFFFFD36E), scheme.tertiary)
        assertEquals(Color(0xFF5F4100), scheme.tertiaryContainer)
        assertNotEquals("Old dark base should not return.", Color(0xFF08111F), scheme.background)
    }

    @Test
    fun `child theme exposes BrainyPal specific semantic tokens`() {
        assertEquals(Color(0xFFF5F8FF), BrainyPalChildTheme.orbitPaper)
        assertEquals(Color(0xFF07111F), BrainyPalChildTheme.deepSpace)
        assertEquals(Color(0xFFEEF4FF), BrainyPalChildTheme.orbitPanel)
        assertEquals(Color(0xFF13213A), BrainyPalChildTheme.orbitText)
        assertEquals(Color(0xFFD7E2F0), BrainyPalChildTheme.orbitLine)
        assertEquals(16.dp, BrainyPalChildTheme.pagePadding)
        assertEquals(14.dp, BrainyPalChildTheme.sectionSpacing)
        assertEquals(18.dp, BrainyPalChildTheme.heroPadding)
        assertEquals(0.16f, BrainyPalChildTheme.signalContainerAlpha)
        assertEquals(Color(0xFFD7F7FB), BrainyPalChildTheme.signalContainer)
        assertEquals(Color(0xFFFFE9B8), BrainyPalChildTheme.gentleFocusContainer)
    }
}

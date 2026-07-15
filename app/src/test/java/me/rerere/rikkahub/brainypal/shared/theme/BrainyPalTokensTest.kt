package me.rerere.rikkahub.brainypal.shared.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class BrainyPalTokensTest {
    @Test
    fun `brand tokens match Yongqi station design card`() {
        assertEquals("BrainyPal", BrainyPalTokens.brandName)
        assertEquals("勇气号", BrainyPalTokens.stationName)
        assertEquals("勇气号空间站", BrainyPalTokens.stationFullName)
        assertEquals(Color(0xFF254EDB), BrainyPalTokens.orbitPrimary)
        assertEquals(Color(0xFF18B8C6), BrainyPalTokens.signalCyan)
        assertEquals(Color(0xFFF6B93B), BrainyPalTokens.solarAmber)
        assertEquals(Color(0xFFF5F8FF), BrainyPalTokens.orbitPaper)
        assertEquals(Color(0xFF07111F), BrainyPalTokens.deepSpace)
        assertEquals(16.dp, BrainyPalTokens.pagePadding)
        assertEquals(22.dp, BrainyPalTokens.cardRadius)
    }

    @Test
    fun `parent and child principles avoid extrinsic reward framing`() {
        val copy = BrainyPalTokens.parentConfirmationPrinciple + BrainyPalTokens.childTrustPrinciple

        assertEquals("导入作业材料", BrainyPalTokens.parentPrimaryHeadline)
        assertEquals("简单说一下需求", BrainyPalTokens.parentSecondaryHeadline)
        assertFalse(copy.contains("积分"))
        assertFalse(copy.contains("金币"))
        assertFalse(copy.contains("排行榜"))
    }
}

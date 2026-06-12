package me.rerere.rikkahub.brainypal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrainyPalChildUiTextTest {
    @Test
    fun `connection status shows full configured url to avoid hidden scheme confusion`() {
        val status = BrainyPalChildUiText.connectionStatus(
            BrainyPalChildConnectionConfig(
                baseUrl = "http://192.168.5.104:8000/rikka/v1",
                apiKey = "brainypal-local",
            )
        )

        assertEquals("已连接 Agent Service", status.title)
        assertEquals("http://192.168.5.104:8000/rikka/v1", status.detail)
        assertFalse(status.detail.contains("brainypal-local"))
    }

    @Test
    fun `home error explains recovery without blocking chat`() {
        val message = BrainyPalChildUiText.homeErrorRecovery("暂时连不上 BrainyPal，可以稍后重试")

        assertTrue(message.contains("今日练习"))
        assertTrue(message.contains("刷新"))
        assertTrue(message.contains("检查服务"))
        assertTrue(message.contains("聊天仍可用"))
    }

    @Test
    fun `empty practice state stays separate from connectivity errors`() {
        val message = BrainyPalChildUiText.practiceEmptyMessage(errorMessage = null)

        assertEquals("现在没有新的今日练习", message.title)
        assertEquals("可以先去问问 BrainyPal，或者晚点刷新任务。", message.detail)
        assertFalse(message.detail.contains("连不上"))
    }
}

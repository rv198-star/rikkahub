package me.rerere.rikkahub.brainypal.child

import me.rerere.rikkahub.brainypal.shared.BrainyPalChildConnectionConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrainyPalChildUiTextTest {
    @Test
    fun `parent connection status shows full configured url to avoid hidden scheme confusion`() {
        val status = BrainyPalChildUiText.connectionStatus(
            BrainyPalChildConnectionConfig(
                baseUrl = "http://192.168.5.80:8000/rikka/v1",
                apiKey = "brainypal-local",
            )
        )

        assertEquals("已连接 Agent Service", status.title)
        assertEquals("http://192.168.5.80:8000/rikka/v1", status.detail)
        assertFalse(status.detail.contains("brainypal-local"))
    }

    @Test
    fun `child connection status hides service internals`() {
        val status = BrainyPalChildUiText.childConnectionStatus(
            BrainyPalChildConnectionConfig(
                baseUrl = "http://192.168.5.80:8000/rikka/v1",
                apiKey = "brainypal-local",
            )
        )

        assertEquals("BrainyPal 已准备好", status.title)
        assertEquals("家长已完成连接，今天可以问问题和做任务。", status.detail)
        assertFalse(status.detail.contains("192.168"))
        assertFalse(status.detail.contains("rikka"))
        assertFalse(status.detail.contains("brainypal-local"))
    }

    @Test
    fun `chat empty state prompts child to explain thinking rather than request answers`() {
        assertEquals("把想法说出来就好", BrainyPalChildChatCopy.emptyTitle)
        assertTrue(BrainyPalChildChatCopy.emptyDetail.contains("直接给答案").not())
        assertEquals(3, BrainyPalChildChatCopy.starterPrompts.size)
        BrainyPalChildChatCopy.starterPrompts.forEach { prompt ->
            assertFalse(prompt.contains("答案"))
            assertTrue(prompt.contains("我") || prompt.contains("哪里"))
        }
    }

    @Test
    fun `hint starter prompt is marked as warm accent`() {
        assertFalse(BrainyPalChildChatCopy.isWarmHintPrompt("我从哪里开始想？"))
        assertFalse(BrainyPalChildChatCopy.isWarmHintPrompt("我这样想对吗？"))
        assertTrue(BrainyPalChildChatCopy.isWarmHintPrompt("我想先听一个小提示"))
    }

    @Test
    fun `home error explains recovery without blocking chat`() {
        val message = BrainyPalChildUiText.homeErrorRecovery("暂时连不上 BrainyPal，可以稍后重试")

        assertTrue(message.contains("今日任务"))
        assertTrue(message.contains("刷新"))
        assertTrue(message.contains("检查服务"))
        assertTrue(message.contains("聊天仍可用"))
    }

    @Test
    fun `empty practice state stays separate from connectivity errors`() {
        val message = BrainyPalChildUiText.practiceEmptyMessage(errorMessage = null)

        assertEquals("现在没有新的今日任务", message.title)
        assertEquals("可以先去问问 BrainyPal，或者晚点刷新任务。", message.detail)
        assertFalse(message.detail.contains("连不上"))
    }
}

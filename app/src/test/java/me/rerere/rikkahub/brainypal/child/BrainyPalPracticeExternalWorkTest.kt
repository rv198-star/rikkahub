package me.rerere.rikkahub.brainypal.child

import me.rerere.rikkahub.brainypal.shared.BrainyPalChildConnectionConfig
import me.rerere.rikkahub.brainypal.shared.BrainyPalPracticeHandoffCodeResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BrainyPalPracticeExternalWorkTest {
    @Test
    fun `printable pdf url uses agent service root and task id`() {
        val url = BrainyPalPracticeExternalWork.printablePdfUrl(
            config = BrainyPalChildConnectionConfig(
                baseUrl = "http://192.168.5.80:8000/rikka/v1",
                apiKey = "brainypal-local",
            ),
            taskId = "task-1",
        )

        assertEquals(
            "http://192.168.5.80:8000/api/v1/child/practice-tasks/task-1/printable.pdf",
            url,
        )
    }

    @Test
    fun `handoff display keeps code prominent and explains computer flow`() {
        val display = BrainyPalPracticeExternalWork.handoffDisplay(
            BrainyPalPracticeHandoffCodeResponse(
                taskId = "task-1",
                channel = "web",
                handoffCode = "A1B2C3",
                expiresAt = "2026-06-14T09:15:00Z",
                joinPath = "/child/join?code=A1B2C3",
                joinUrl = "http://192.168.5.80:8000/child/join?code=A1B2C3",
            )
        )

        assertEquals("A1B2C3", display.code)
        assertEquals("电脑完成", display.title)
        assertTrue(display.instruction.contains("电脑打开"))
        assertTrue(display.instruction.contains("A1B2C3"))
        assertEquals("http://192.168.5.80:8000/child/join?code=A1B2C3", display.joinUrl)
    }
}

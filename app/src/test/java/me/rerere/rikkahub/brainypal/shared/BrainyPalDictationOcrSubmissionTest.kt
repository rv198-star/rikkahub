package me.rerere.rikkahub.brainypal.shared

import org.junit.Assert.assertEquals
import org.junit.Test

class BrainyPalDictationOcrSubmissionTest {
    @Test
    fun `builds evidence request by aligning ocr lines with dictation items`() {
        val detail = BrainyPalChildPracticeTaskDetail(
            taskId = "task_1",
            title = "今日听写",
            taskType = "dictation",
            status = "reviewing",
            helpLimit = 3,
            helpUsed = 0,
            items = listOf(
                BrainyPalChildPracticeTaskItem(itemId = "word_1", prompt = "apple"),
                BrainyPalChildPracticeTaskItem(itemId = "word_2", prompt = "banana"),
            ),
        )

        val request = BrainyPalDictationOcrSubmission.request(
            detail = detail,
            imageRef = "file:///cache/photo.jpg",
            rawOcrText = """
                <image_file_ocr>
                apple
                bananna
                </image_file_ocr>
                * The image_file_ocr tag contains a description.
            """.trimIndent(),
        )

        assertEquals("file:///cache/photo.jpg", request.imageRef)
        assertEquals("apple\nbananna", request.recognizedText)
        assertEquals("word_1", request.items[0].itemId)
        assertEquals("apple", request.items[0].recognizedText)
        assertEquals("word_2", request.items[1].itemId)
        assertEquals("bananna", request.items[1].recognizedText)
    }
}

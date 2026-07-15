package me.rerere.rikkahub.brainypal.shared

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrainyPalDictationOcrReviewTest {
    @Test
    fun `builds review rows from traceable dictation ocr evidence`() {
        val detail = BrainyPalChildPracticeTaskDetail(
            taskId = "task_1",
            title = "今日听写",
            taskType = "dictation",
            status = "reviewing",
            helpLimit = 3,
            helpUsed = 0,
            items = listOf(
                BrainyPalChildPracticeTaskItem(
                    itemId = "word_1",
                    prompt = "apple",
                    childAnswer = "apple",
                    result = "correct",
                    correctionStatus = "not_needed",
                    ocrEvidence = BrainyPalDictationOcrEvidence(
                        imageRef = "local-cache://dictation/photo-1.jpg",
                        recognizedText = "apple",
                        confidence = 0.96f,
                        cropRef = "local-cache://dictation/photo-1-word-1.jpg",
                        confirmationStatus = "confirmed",
                        errorAttribution = "correct",
                    ),
                ),
                BrainyPalChildPracticeTaskItem(
                    itemId = "word_2",
                    prompt = "banana",
                    childAnswer = "bananna",
                    result = "needs_manual_review",
                    correctionStatus = "needs_correction",
                    ocrEvidence = BrainyPalDictationOcrEvidence(
                        imageRef = "local-cache://dictation/photo-1.jpg",
                        recognizedText = "bananna",
                        confidence = 0.72f,
                        boundingBox = BrainyPalDictationOcrBoundingBox(
                            x = 0.1f,
                            y = 0.35f,
                            width = 0.4f,
                            height = 0.1f,
                        ),
                        confirmationStatus = "unconfirmed",
                    ),
                ),
            ),
        )

        val rows = BrainyPalDictationOcrReview.rows(detail)

        assertEquals(2, rows.size)
        assertEquals("第 1 条", rows[0].title)
        assertEquals("apple", rows[0].recognizedText)
        assertEquals("已确认正确", rows[0].resultLabel)
        assertFalse(rows[0].requiresManualConfirmation)
        assertEquals("裁剪图", rows[0].sourceRegionLabel)
        assertEquals("local-cache://dictation/photo-1-word-1.jpg", rows[0].previewImageRef)
        assertEquals("查看裁剪区域", rows[0].previewButtonLabel)

        assertEquals("第 2 条", rows[1].title)
        assertEquals("bananna", rows[1].recognizedText)
        assertEquals("local-cache://dictation/photo-1.jpg", rows[1].imageRef)
        assertEquals("local-cache://dictation/photo-1.jpg", rows[1].previewImageRef)
        assertEquals("查看照片区域", rows[1].previewButtonLabel)
        assertEquals(
            BrainyPalDictationOcrBoundingBox(
                x = 0.1f,
                y = 0.35f,
                width = 0.4f,
                height = 0.1f,
            ),
            rows[1].previewBoundingBox,
        )
        assertTrue(rows[1].hasSourceRegionOverlay)
        assertEquals("需要确认", rows[1].resultLabel)
        assertTrue(rows[1].requiresManualConfirmation)
        assertEquals("照片区域", rows[1].sourceRegionLabel)
        assertEquals("识别不太确定，先看照片再确认。", rows[1].guidanceLabel)
        assertEquals(
            listOf("孩子写错了", "OCR 识别错了", "照片不清楚", "其实是对的"),
            rows[1].confirmationActions.map { it.label },
        )
    }

    @Test
    fun `does not show ocr review for non dictation task`() {
        val detail = BrainyPalChildPracticeTaskDetail(
            taskId = "task_1",
            title = "今日练习",
            taskType = "wrong_question_practice",
            status = "reviewing",
            helpLimit = 3,
            helpUsed = 0,
            items = listOf(
                BrainyPalChildPracticeTaskItem(
                    itemId = "item_1",
                    prompt = "1 + 1",
                )
            ),
        )

        assertTrue(BrainyPalDictationOcrReview.rows(detail).isEmpty())
    }

    @Test
    fun `confirmed ocr attribution does not keep asking parent to confirm`() {
        val detail = BrainyPalChildPracticeTaskDetail(
            taskId = "task_1",
            title = "今日听写",
            taskType = "dictation",
            status = "reviewing",
            helpLimit = 3,
            helpUsed = 0,
            items = listOf(
                BrainyPalChildPracticeTaskItem(
                    itemId = "word_1",
                    prompt = "orange",
                    childAnswer = "ornage",
                    result = "needs_manual_review",
                    correctionStatus = "needs_correction",
                    ocrEvidence = BrainyPalDictationOcrEvidence(
                        imageRef = "local-cache://dictation/photo-1.jpg",
                        recognizedText = "ornage",
                        confidence = 0.65f,
                        cropRef = "local-cache://dictation/photo-1-word-1.jpg",
                        confirmationStatus = "confirmed",
                        errorAttribution = "ocr_recognized_wrong",
                    ),
                )
            ),
        )

        val row = BrainyPalDictationOcrReview.rows(detail).single()

        assertFalse(row.requiresManualConfirmation)
        assertEquals("OCR 识别错了", row.resultLabel)
        assertEquals(null, row.guidanceLabel)
    }
}

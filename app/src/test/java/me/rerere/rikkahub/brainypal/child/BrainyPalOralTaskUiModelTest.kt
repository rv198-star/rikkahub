package me.rerere.rikkahub.brainypal.child

import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskDetail
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrainyPalOralTaskUiModelTest {
    @Test
    fun `recitation opens in reading phase with full text visible`() {
        val model = BrainyPalOralTaskUiModel.build(
            detail = recitationDetail(),
            phase = BrainyPalOralTaskPhase.Reading,
            currentSentenceIndex = 0,
            revealCurrentSentence = false,
            showSourcePeek = false,
            reflectionUnlocked = false,
        )

        assertEquals(BrainyPalOralTaskPhase.Reading, model.phase)
        assertEquals(4, model.lines.size)
        assertTrue(model.lines.all { it.visibleText.isNotBlank() })
        assertTrue(model.lines.none { it.isMasked })
        assertFalse(model.showReflection)
        assertEquals("开始背诵", model.primaryActionLabel)
        assertEquals("我背完了", model.completeActionLabel)
    }

    @Test
    fun `sentence practice masks only current sentence and keeps context visible`() {
        val model = BrainyPalOralTaskUiModel.build(
            detail = recitationDetail(),
            phase = BrainyPalOralTaskPhase.SentencePractice,
            currentSentenceIndex = 1,
            revealCurrentSentence = false,
            showSourcePeek = false,
            reflectionUnlocked = false,
        )

        assertFalse(model.lines[0].isMasked)
        assertTrue(model.lines[1].isMasked)
        assertTrue(model.lines[1].isActive)
        assertFalse(model.lines[2].isMasked)
        assertFalse(model.lines[3].isMasked)
        assertTrue(model.lines[0].visibleText.isNotBlank())
        assertTrue(model.lines[2].visibleText.isNotBlank())
    }

    @Test
    fun `sentence practice can reveal the active sentence`() {
        val model = BrainyPalOralTaskUiModel.build(
            detail = recitationDetail(),
            phase = BrainyPalOralTaskPhase.SentencePractice,
            currentSentenceIndex = 2,
            revealCurrentSentence = true,
            showSourcePeek = false,
            reflectionUnlocked = false,
        )

        assertFalse(model.lines[2].isMasked)
        assertEquals("夜来风雨声，", model.lines[2].visibleText)
    }

    @Test
    fun `recitation check hides source until peek is enabled`() {
        val hidden = BrainyPalOralTaskUiModel.build(
            detail = recitationDetail(),
            phase = BrainyPalOralTaskPhase.Check,
            currentSentenceIndex = 0,
            revealCurrentSentence = false,
            showSourcePeek = false,
            reflectionUnlocked = false,
        )
        val peek = BrainyPalOralTaskUiModel.build(
            detail = recitationDetail(),
            phase = BrainyPalOralTaskPhase.Check,
            currentSentenceIndex = 0,
            revealCurrentSentence = false,
            showSourcePeek = true,
            reflectionUnlocked = false,
        )

        assertTrue(hidden.lines.all { it.visibleText.isBlank() && it.isMasked })
        assertTrue(peek.lines.all { it.visibleText.isNotBlank() && !it.isMasked })
    }

    @Test
    fun `reflection stays hidden until oral attempt is marked done`() {
        val hidden = BrainyPalOralTaskUiModel.build(
            detail = readingDetail(),
            phase = BrainyPalOralTaskPhase.Check,
            currentSentenceIndex = 0,
            revealCurrentSentence = false,
            showSourcePeek = false,
            reflectionUnlocked = false,
        )
        val shown = BrainyPalOralTaskUiModel.build(
            detail = readingDetail(),
            phase = BrainyPalOralTaskPhase.Check,
            currentSentenceIndex = 0,
            revealCurrentSentence = false,
            showSourcePeek = false,
            reflectionUnlocked = true,
        )

        assertFalse(hidden.showReflection)
        assertTrue(shown.showReflection)
        assertEquals("开始朗读", shown.primaryActionLabel)
        assertEquals("我读完了", shown.completeActionLabel)
    }

    @Test
    fun `recording action label follows recorder state`() {
        assertEquals(
            "开始录音",
            BrainyPalOralRecordingUiModel.build(
                isRecording = false,
                audioUploaded = false,
            ).actionLabel,
        )
        assertEquals(
            "结束并上传",
            BrainyPalOralRecordingUiModel.build(
                isRecording = true,
                audioUploaded = false,
            ).actionLabel,
        )
        assertEquals(
            "重新录音",
            BrainyPalOralRecordingUiModel.build(
                isRecording = false,
                audioUploaded = true,
            ).actionLabel,
        )
    }

    private fun recitationDetail(): BrainyPalChildPracticeTaskDetail {
        return BrainyPalChildPracticeTaskDetail(
            taskId = "task-recitation",
            title = "背诵《春晓》",
            taskType = "recitation",
            status = "accepted",
            helpLimit = 2,
            helpUsed = 0,
            items = listOf(
                BrainyPalChildPracticeTaskItem(
                    itemId = "recitation_1",
                    prompt = "春眠不觉晓，处处闻啼鸟。夜来风雨声，花落知多少。",
                )
            ),
        )
    }

    private fun readingDetail(): BrainyPalChildPracticeTaskDetail {
        return BrainyPalChildPracticeTaskDetail(
            taskId = "task-reading",
            title = "朗读短文",
            taskType = "reading",
            status = "accepted",
            helpLimit = 2,
            helpUsed = 0,
            items = listOf(
                BrainyPalChildPracticeTaskItem(
                    itemId = "reading_1",
                    prompt = "小河边有一棵树。树下有一只猫。",
                )
            ),
        )
    }
}

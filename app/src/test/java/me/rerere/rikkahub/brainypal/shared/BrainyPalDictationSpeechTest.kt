package me.rerere.rikkahub.brainypal.shared

import org.junit.Assert.assertEquals
import org.junit.Test

class BrainyPalDictationSpeechTest {
    @Test
    fun `chinese vocab speech follows language mode and repeat policy`() {
        val detail = BrainyPalChildPracticeTaskDetail(
            taskId = "task_1",
            title = "语文生字听写",
            taskType = "dictation",
            status = "pending",
            helpLimit = 3,
            helpUsed = 0,
            taskSpec = BrainyPalAgentTaskSpec(
                subject = "chinese",
                ttsPolicy = BrainyPalTaskTtsPolicy(
                    languageMode = "chinese_vocab",
                    repeatCount = 2,
                    phraseCount = 4,
                    intervalSeconds = 4,
                ),
            ),
        )
        val item = BrainyPalChildPracticeTaskItem(
            itemId = "dictation_1",
            prompt = "观察",
        )

        val speech = BrainyPalDictationSpeech.build(detail, item, index = 0)

        assertEquals("第 1 条，听中文生字。观察。观察。请写在纸上。", speech)
    }

    @Test
    fun `english word speech keeps the word as the only answer payload`() {
        val detail = BrainyPalChildPracticeTaskDetail(
            taskId = "task_1",
            title = "英语单词听写",
            taskType = "dictation",
            status = "pending",
            helpLimit = 3,
            helpUsed = 0,
            taskSpec = BrainyPalAgentTaskSpec(
                subject = "english",
                ttsPolicy = BrainyPalTaskTtsPolicy(
                    languageMode = "english_word",
                    repeatCount = 2,
                ),
            ),
        )
        val item = BrainyPalChildPracticeTaskItem(
            itemId = "dictation_2",
            prompt = "apple",
            expectedAnswer = "apple",
            scoringHint = "a fruit",
        )

        val speech = BrainyPalDictationSpeech.build(detail, item, index = 1)

        assertEquals("第 2 条，听英语单词。apple。apple。请写在纸上。", speech)
    }
}

package me.rerere.rikkahub.brainypal.shared

import org.junit.Assert.assertEquals
import org.junit.Test

class BrainyPalDictationSpeechTest {
    @Test
    fun `chinese vocab speech uses concise local phrases and repeat policy`() {
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
            prompt = "始",
        )

        val speech = BrainyPalDictationSpeech.build(detail, item, index = 0)
        val repeat = BrainyPalDictationSpeech.buildRepeat(detail, item)

        assertEquals("第 1 条。始。开始。始终。原始。始。开始。始终。原始。", speech)
        assertEquals("始。开始。始终。原始。始。开始。始终。原始。", repeat)
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
        val repeat = BrainyPalDictationSpeech.buildRepeat(detail, item)

        assertEquals("第 2 条。apple。apple。", speech)
        assertEquals("apple。apple。", repeat)
    }

    @Test
    fun `single chinese character gets local phrases even without explicit language mode`() {
        val detail = BrainyPalChildPracticeTaskDetail(
            taskId = "task_2",
            title = "今日听写",
            taskType = "dictation",
            status = "pending",
            helpLimit = 3,
            helpUsed = 0,
            taskSpec = BrainyPalAgentTaskSpec(
                ttsPolicy = BrainyPalTaskTtsPolicy(
                    repeatCount = 1,
                    phraseCount = 2,
                ),
            ),
        )
        val item = BrainyPalChildPracticeTaskItem(
            itemId = "dictation_1",
            prompt = "勇",
        )

        val speech = BrainyPalDictationSpeech.build(detail, item, index = 0)

        assertEquals("第 1 条。勇。勇气。勇敢。", speech)
    }

    @Test
    fun `chinese word speech repeats the whole word three times without per character phrases`() {
        val detail = BrainyPalChildPracticeTaskDetail(
            taskId = "task_3",
            title = "语文词语听写",
            taskType = "dictation",
            status = "pending",
            helpLimit = 3,
            helpUsed = 0,
            taskSpec = BrainyPalAgentTaskSpec(
                subject = "chinese",
                ttsPolicy = BrainyPalTaskTtsPolicy(
                    languageMode = "chinese_vocab",
                    repeatCount = 1,
                    phraseCount = 3,
                    intervalSeconds = 4,
                ),
            ),
        )
        val item = BrainyPalChildPracticeTaskItem(
            itemId = "dictation_2",
            prompt = "观察",
        )

        val plan = BrainyPalDictationSpeech.plan(detail, item, index = 1)
        val repeat = BrainyPalDictationSpeech.repeatPlan(detail, item)

        assertEquals(
            listOf("第 2 条", "观察", "观察", "观察"),
            plan.utterances,
        )
        assertEquals(listOf("观察", "观察", "观察"), repeat.utterances)
        assertEquals(2_000L, plan.pauseMillis)
        assertEquals("第 2 条。观察。观察。观察。", plan.text)
    }
}

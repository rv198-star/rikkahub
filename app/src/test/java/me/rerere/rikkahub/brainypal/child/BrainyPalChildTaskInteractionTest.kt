package me.rerere.rikkahub.brainypal.child

import me.rerere.rikkahub.brainypal.shared.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrainyPalChildTaskInteractionTest {
    @Test
    fun `dictation plan hides answers and exposes listening controls`() {
        val detail = BrainyPalChildPracticeTaskDetail(
            taskId = "task-dictation",
            title = "语文生字听写",
            taskType = "dictation",
            status = "pending",
            helpLimit = 3,
            helpUsed = 0,
            taskSpec = BrainyPalAgentTaskSpec(
                subject = "chinese",
                childBrief = "听写时不会显示答案，写完后再拍照批改。",
                ttsPolicy = BrainyPalTaskTtsPolicy(
                    languageMode = "chinese_vocab",
                    repeatCount = 2,
                    phraseCount = 4,
                    intervalSeconds = 4,
                ),
                evidencePolicy = BrainyPalTaskEvidencePolicy(
                    requiresOcr = true,
                    preserveImageEvidence = true,
                ),
            ),
            items = listOf(
                BrainyPalChildPracticeTaskItem(
                    itemId = "dictation_1",
                    prompt = "观察",
                )
            ),
        )

        val plan = BrainyPalChildTaskInteraction.plan(detail)

        assertEquals("听写任务", plan.kindLabel)
        assertEquals("开始听写", plan.primaryActionLabel)
        assertEquals("写下你听到的内容", plan.answerLabel)
        assertEquals("拍照 OCR 批改，或写下不确定的地方", plan.evidenceLabel)
        assertTrue(plan.brief.contains("不会显示答案"))
        assertTrue(plan.brief.contains("读 2 遍"))
        assertTrue(plan.brief.contains("组 4 个词"))
        assertEquals(
            listOf(
                "repeat" to "再听一次",
                "next" to "下一个",
                "dont_know" to "不会，先下一个",
            ),
            plan.quickActions,
        )
        assertFalse(plan.revealAnswerBeforeSubmit)
    }

    @Test
    fun `recitation plan uses recitation language and no hint ticket framing`() {
        val detail = BrainyPalChildPracticeTaskDetail(
            taskId = "task-recitation",
            title = "背诵课文第一段",
            taskType = "recitation",
            status = "accepted",
            helpLimit = 2,
            helpUsed = 0,
            taskSpec = BrainyPalAgentTaskSpec(
                subject = "chinese",
                childBrief = "先听一遍，再自己背一小段。",
            ),
            items = listOf(
                BrainyPalChildPracticeTaskItem(
                    itemId = "recitation_1",
                    prompt = "春天来了，小草从土里探出头。",
                )
            ),
        )

        val plan = BrainyPalChildTaskInteraction.plan(detail)

        assertEquals("背诵任务", plan.kindLabel)
        assertEquals("开始背诵", plan.primaryActionLabel)
        assertEquals("背完后写一句自评", plan.answerLabel)
        assertEquals("哪里卡住了，或拍照/录音留证据", plan.evidenceLabel)
        assertTrue(plan.brief.contains("先听一遍"))
        assertEquals(emptyList<Pair<String, String>>(), plan.quickActions)
        assertTrue(plan.usesDedicatedFlow)
    }
}

package me.rerere.rikkahub.brainypal.shared

import org.junit.Assert.assertEquals
import org.junit.Test

class BrainyPalRecitationSpeechTest {
    @Test
    fun `builds guided recitation speech from task steps and paragraph`() {
        val detail = BrainyPalChildPracticeTaskDetail(
            taskId = "task-recitation",
            title = "背诵课文第一段",
            taskType = "recitation",
            status = "accepted",
            helpLimit = 2,
            helpUsed = 0,
            taskSpec = BrainyPalAgentTaskSpec(
                childBrief = "先听一遍，再自己背一小段。",
                steps = listOf(
                    BrainyPalAgentTaskStep(
                        stepId = "listen",
                        title = "先听一遍",
                        action = "listen",
                    ),
                    BrainyPalAgentTaskStep(
                        stepId = "recite",
                        title = "自己背一小段",
                        action = "recite",
                    ),
                ),
            ),
            items = listOf(
                BrainyPalChildPracticeTaskItem(
                    itemId = "recitation_1",
                    prompt = "春天来了，小草从土里探出头。",
                )
            ),
        )

        val speech = BrainyPalRecitationSpeech.build(detail)

        assertEquals(
            "背诵课文第一段。先听一遍，再自己背一小段。步骤：先听一遍；自己背一小段。材料：春天来了，小草从土里探出头。",
            speech,
        )
    }
}

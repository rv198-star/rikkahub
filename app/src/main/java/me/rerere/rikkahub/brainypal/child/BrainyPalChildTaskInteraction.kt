package me.rerere.rikkahub.brainypal.child

import me.rerere.rikkahub.brainypal.shared.BrainyPalAgentTaskSpec
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskDetail
import me.rerere.rikkahub.brainypal.shared.BrainyPalTaskTtsPolicy

data class BrainyPalChildTaskInteractionPlan(
    val kindLabel: String,
    val primaryActionLabel: String,
    val brief: String,
    val answerLabel: String,
    val evidenceLabel: String,
    val submitLabel: String,
    val quickActions: List<Pair<String, String>>,
    val revealAnswerBeforeSubmit: Boolean,
    val usesDedicatedFlow: Boolean = false,
)

object BrainyPalChildTaskInteraction {
    fun plan(detail: BrainyPalChildPracticeTaskDetail): BrainyPalChildTaskInteractionPlan {
        val spec = detail.taskSpec
        return when (detail.taskType) {
            "dictation" -> dictationPlan(detail, spec)
            "reading" -> readingPlan(detail, spec)
            "recitation" -> recitationPlan(detail, spec)
            "knowledge_review" -> reviewPlan(detail, spec)
            else -> practicePlan(detail, spec)
        }
    }

    private fun dictationPlan(
        detail: BrainyPalChildPracticeTaskDetail,
        spec: BrainyPalAgentTaskSpec?,
    ): BrainyPalChildTaskInteractionPlan {
        val tts = spec?.ttsPolicy ?: BrainyPalTaskTtsPolicy()
        val phraseText = tts.phraseCount
            ?.takeIf { it > 0 }
            ?.let { "，组 $it 个词" }
            .orEmpty()
        val intervalText = tts.intervalSeconds
            .takeIf { it > 0 }
            ?.let { "，间隔 ${it} 秒" }
            .orEmpty()
        val baseBrief = spec?.childBrief?.takeIf { it.isNotBlank() }
            ?: "听写时不会显示答案，写完后再拍照批改。"
        return BrainyPalChildTaskInteractionPlan(
            kindLabel = detail.taskKindLabel,
            primaryActionLabel = "开始听写",
            brief = "$baseBrief BrainyPal 会读 ${tts.repeatCount} 遍$phraseText$intervalText。",
            answerLabel = "写下你听到的内容",
            evidenceLabel = if (spec?.evidencePolicy?.requiresOcr == true) {
                "拍照 OCR 批改，或写下不确定的地方"
            } else {
                "写下不确定的地方"
            },
            submitLabel = "提交听写",
            quickActions = listOf(
                "repeat" to "再听一次",
                "next" to "下一个",
                "dont_know" to "不会，先下一个",
            ),
            revealAnswerBeforeSubmit = spec?.guardrails?.revealAnswerBeforeSubmit ?: false,
            usesDedicatedFlow = true,
        )
    }

    private fun recitationPlan(
        detail: BrainyPalChildPracticeTaskDetail,
        spec: BrainyPalAgentTaskSpec?,
    ): BrainyPalChildTaskInteractionPlan {
        return BrainyPalChildTaskInteractionPlan(
            kindLabel = detail.taskKindLabel,
            primaryActionLabel = "开始背诵",
            brief = spec?.childBrief?.takeIf { it.isNotBlank() } ?: "先听一遍，再自己背一小段。",
            answerLabel = "背完后写一句自评",
            evidenceLabel = "哪里卡住了，或拍照/录音留证据",
            submitLabel = "提交背诵结果",
            quickActions = emptyList(),
            revealAnswerBeforeSubmit = spec?.guardrails?.revealAnswerBeforeSubmit ?: false,
            usesDedicatedFlow = true,
        )
    }

    private fun readingPlan(
        detail: BrainyPalChildPracticeTaskDetail,
        spec: BrainyPalAgentTaskSpec?,
    ): BrainyPalChildTaskInteractionPlan {
        return BrainyPalChildTaskInteractionPlan(
            kindLabel = detail.taskKindLabel,
            primaryActionLabel = "开始朗读",
            brief = spec?.childBrief?.takeIf { it.isNotBlank() } ?: "先听一遍，再自己朗读一段。",
            answerLabel = "朗读后给自己 1-5 分",
            evidenceLabel = "哪里读得不顺，或录音留证据",
            submitLabel = "提交朗读结果",
            quickActions = emptyList(),
            revealAnswerBeforeSubmit = true,
            usesDedicatedFlow = true,
        )
    }

    private fun reviewPlan(
        detail: BrainyPalChildPracticeTaskDetail,
        spec: BrainyPalAgentTaskSpec?,
    ): BrainyPalChildTaskInteractionPlan {
        return BrainyPalChildTaskInteractionPlan(
            kindLabel = detail.taskKindLabel,
            primaryActionLabel = "开始复习",
            brief = spec?.childBrief?.takeIf { it.isNotBlank() } ?: "先自己回忆，再看一点提示。",
            answerLabel = "先写下你记得的内容",
            evidenceLabel = "你怎么想的，或哪里卡住了",
            submitLabel = "提交复习",
            quickActions = emptyList(),
            revealAnswerBeforeSubmit = spec?.guardrails?.revealAnswerBeforeSubmit ?: false,
        )
    }

    private fun practicePlan(
        detail: BrainyPalChildPracticeTaskDetail,
        spec: BrainyPalAgentTaskSpec?,
    ): BrainyPalChildTaskInteractionPlan {
        return BrainyPalChildTaskInteractionPlan(
            kindLabel = detail.taskKindLabel,
            primaryActionLabel = if (detail.status == "in_progress") "继续任务" else "开始任务",
            brief = spec?.childBrief?.takeIf { it.isNotBlank() } ?: "先写下自己的想法，需要时再用提示券。",
            answerLabel = "你的答案",
            evidenceLabel = "你怎么想的，或哪一步卡住了",
            submitLabel = "提交任务",
            quickActions = emptyList(),
            revealAnswerBeforeSubmit = spec?.guardrails?.revealAnswerBeforeSubmit ?: false,
        )
    }
}

package me.rerere.rikkahub.brainypal.shared

data class BrainyPalDictationSpeechPlan(
    val utterances: List<String>,
    val pauseMillis: Long,
) {
    val text: String
        get() = utterances.joinToString(separator = "。", postfix = "。")
}

object BrainyPalDictationSpeech {
    fun plan(
        detail: BrainyPalChildPracticeTaskDetail,
        item: BrainyPalChildPracticeTaskItem,
        index: Int,
    ): BrainyPalDictationSpeechPlan {
        return buildPlan(detail, item, prefix = "第 ${index + 1} 条")
    }

    fun repeatPlan(
        detail: BrainyPalChildPracticeTaskDetail,
        item: BrainyPalChildPracticeTaskItem,
    ): BrainyPalDictationSpeechPlan {
        return buildPlan(detail, item, prefix = "")
    }

    fun build(
        detail: BrainyPalChildPracticeTaskDetail,
        item: BrainyPalChildPracticeTaskItem,
        index: Int,
    ): String {
        return plan(detail, item, index).text
    }

    fun buildRepeat(
        detail: BrainyPalChildPracticeTaskDetail,
        item: BrainyPalChildPracticeTaskItem,
    ): String {
        return repeatPlan(detail, item).text
    }

    private fun buildPlan(
        detail: BrainyPalChildPracticeTaskDetail,
        item: BrainyPalChildPracticeTaskItem,
        prefix: String,
    ): BrainyPalDictationSpeechPlan {
        val policy = detail.taskSpec?.ttsPolicy ?: BrainyPalTaskTtsPolicy()
        val payload = speechPayload(policy, item)
        val utterances = buildList {
            if (prefix.isNotBlank()) add(prefix)
            repeat(payload.repeatCount) {
                addAll(payload.segments)
            }
        }
        return BrainyPalDictationSpeechPlan(
            utterances = utterances,
            pauseMillis = policy.intervalSeconds.coerceIn(1, 2) * 1000L,
        )
    }

    private data class SpeechPayload(
        val segments: List<String>,
        val repeatCount: Int,
    )

    private fun speechPayload(
        policy: BrainyPalTaskTtsPolicy,
        item: BrainyPalChildPracticeTaskItem,
    ): SpeechPayload {
        val term = primaryTerm(item.prompt).ifBlank { item.expectedAnswer?.trim().orEmpty() }
        val policyRepeatCount = policy.repeatCount.coerceAtLeast(1)
        if (term.isBlank()) {
            return SpeechPayload(listOf("听写内容"), policyRepeatCount)
        }
        if (!term.isChineseWord()) {
            return SpeechPayload(listOf(term), policyRepeatCount)
        }
        if (!term.isSingleChineseCharacter()) {
            return SpeechPayload(listOf(term), CHINESE_WORD_REPEAT_COUNT)
        }
        val phraseLimit = (policy.phraseCount ?: 3).coerceIn(2, 3)
        return SpeechPayload(
            segments = listOf(term) + phrasesForCharacter(term, item, phraseLimit),
            repeatCount = policyRepeatCount,
        )
    }

    private fun phrasesForCharacter(
        character: String,
        item: BrainyPalChildPracticeTaskItem,
        phraseLimit: Int,
    ): List<String> {
        return (
            phraseCandidates(item.prompt) +
                phraseCandidates(item.scoringHint.orEmpty()) +
                phraseCandidates(item.expectedAnswer.orEmpty()) +
                chinesePhraseBank[character].orEmpty()
            )
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() && it != character && it.contains(character) }
            .distinct()
            .take(phraseLimit)
            .toList()
    }

    private fun primaryTerm(text: String): String {
        return phraseCandidates(text).firstOrNull().orEmpty()
    }

    private fun phraseCandidates(text: String): List<String> {
        return text
            .replace("：", " ")
            .replace(":", " ")
            .split(phraseSeparator)
            .map { it.trim().trim('。', '.', '，', ',', '、', ';', '；') }
            .filter { it.isNotBlank() }
    }

    private fun String.isChineseWord(): Boolean {
        return isNotBlank() && all { it in '\u4e00'..'\u9fff' }
    }

    private fun String.isSingleChineseCharacter(): Boolean {
        return length == 1 && first() in '\u4e00'..'\u9fff'
    }

    private const val CHINESE_WORD_REPEAT_COUNT = 3

    private val phraseSeparator = Regex("[\\s,，、/;；]+")

    private val chinesePhraseBank = mapOf(
        "始" to listOf("开始", "始终", "原始"),
        "勇" to listOf("勇气", "勇敢", "英勇"),
        "气" to listOf("空气", "天气", "气球"),
        "观" to listOf("观察", "观看", "景观"),
        "察" to listOf("观察", "察觉", "检察"),
        "清" to listOf("清水", "清楚", "清晨"),
        "澈" to listOf("清澈", "澄澈"),
        "溪" to listOf("小溪", "溪流"),
        "流" to listOf("流水", "流动", "溪流"),
        "认" to listOf("认真", "认识", "认字"),
        "真" to listOf("认真", "真正", "真心"),
        "专" to listOf("专心", "专门", "专注"),
        "心" to listOf("专心", "心情", "用心"),
        "学" to listOf("学习", "学校", "学生"),
        "习" to listOf("学习", "练习", "习惯"),
        "写" to listOf("写字", "书写", "听写"),
        "听" to listOf("听写", "听见", "听讲"),
        "读" to listOf("读书", "朗读", "阅读"),
        "背" to listOf("背诵", "背书", "背影"),
        "诵" to listOf("背诵", "朗诵", "诵读"),
    )
}

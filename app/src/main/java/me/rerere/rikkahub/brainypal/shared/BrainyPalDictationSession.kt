package me.rerere.rikkahub.brainypal.shared

enum class BrainyPalDictationCommand {
    START,
    REPEAT,
    NEXT,
    DONT_KNOW,
    PAUSE,
    RESUME,
    UNKNOWN,
}

enum class BrainyPalDictationSessionStatus {
    IDLE,
    WAITING,
    PAUSED,
    FINISHED,
}

data class BrainyPalDictationSessionState(
    val itemIds: List<String>,
    val currentIndex: Int = 0,
    val status: BrainyPalDictationSessionStatus = BrainyPalDictationSessionStatus.IDLE,
    val repeatCounts: Map<String, Int> = emptyMap(),
    val dontKnowCounts: Map<String, Int> = emptyMap(),
) {
    val currentItemId: String?
        get() = itemIds.getOrNull(currentIndex)

    val isFinished: Boolean
        get() = status == BrainyPalDictationSessionStatus.FINISHED

    val isWaitingForChild: Boolean
        get() = status == BrainyPalDictationSessionStatus.WAITING

    val affectsAnswerEvidence: Boolean
        get() = false

    fun isActiveItem(itemId: String): Boolean {
        return status == BrainyPalDictationSessionStatus.WAITING && currentItemId == itemId
    }

    fun repeatCountFor(itemId: String): Int = repeatCounts[itemId] ?: 0

    fun dontKnowCountFor(itemId: String): Int = dontKnowCounts[itemId] ?: 0
}

data class BrainyPalDictationSessionUpdate(
    val state: BrainyPalDictationSessionState,
    val playbackItemId: String? = null,
)

object BrainyPalDictationSession {
    fun reduce(
        state: BrainyPalDictationSessionState,
        command: BrainyPalDictationCommand,
    ): BrainyPalDictationSessionUpdate {
        return when (command) {
            BrainyPalDictationCommand.START -> start(state)
            BrainyPalDictationCommand.REPEAT -> repeat(state)
            BrainyPalDictationCommand.NEXT -> advance(state, markDontKnow = false)
            BrainyPalDictationCommand.DONT_KNOW -> advance(state, markDontKnow = true)
            BrainyPalDictationCommand.PAUSE -> pause(state)
            BrainyPalDictationCommand.RESUME -> resume(state)
            BrainyPalDictationCommand.UNKNOWN -> BrainyPalDictationSessionUpdate(state)
        }
    }

    private fun start(state: BrainyPalDictationSessionState): BrainyPalDictationSessionUpdate {
        val firstItemId = state.itemIds.firstOrNull()
        val nextState = state.copy(
            currentIndex = 0,
            status = if (firstItemId == null) {
                BrainyPalDictationSessionStatus.FINISHED
            } else {
                BrainyPalDictationSessionStatus.WAITING
            },
        )
        return BrainyPalDictationSessionUpdate(nextState, firstItemId)
    }

    private fun repeat(state: BrainyPalDictationSessionState): BrainyPalDictationSessionUpdate {
        val itemId = state.currentItemId ?: return BrainyPalDictationSessionUpdate(state)
        val nextState = state.copy(
            status = BrainyPalDictationSessionStatus.WAITING,
            repeatCounts = state.repeatCounts.increment(itemId),
        )
        return BrainyPalDictationSessionUpdate(nextState, itemId)
    }

    private fun advance(
        state: BrainyPalDictationSessionState,
        markDontKnow: Boolean,
    ): BrainyPalDictationSessionUpdate {
        val currentItemId = state.currentItemId
        val nextIndex = state.currentIndex + 1
        val nextItemId = state.itemIds.getOrNull(nextIndex)
        val nextState = state.copy(
            currentIndex = nextIndex.coerceAtMost(state.itemIds.size),
            status = if (nextItemId == null) {
                BrainyPalDictationSessionStatus.FINISHED
            } else {
                BrainyPalDictationSessionStatus.WAITING
            },
            dontKnowCounts = if (markDontKnow && currentItemId != null) {
                state.dontKnowCounts.increment(currentItemId)
            } else {
                state.dontKnowCounts
            },
        )
        return BrainyPalDictationSessionUpdate(nextState, nextItemId)
    }

    private fun pause(state: BrainyPalDictationSessionState): BrainyPalDictationSessionUpdate {
        if (state.isFinished) return BrainyPalDictationSessionUpdate(state)
        return BrainyPalDictationSessionUpdate(
            state.copy(status = BrainyPalDictationSessionStatus.PAUSED)
        )
    }

    private fun resume(state: BrainyPalDictationSessionState): BrainyPalDictationSessionUpdate {
        val itemId = state.currentItemId ?: return BrainyPalDictationSessionUpdate(
            state.copy(status = BrainyPalDictationSessionStatus.FINISHED)
        )
        return BrainyPalDictationSessionUpdate(
            state.copy(status = BrainyPalDictationSessionStatus.WAITING),
            itemId,
        )
    }

    private fun Map<String, Int>.increment(itemId: String): Map<String, Int> {
        return this + (itemId to ((this[itemId] ?: 0) + 1))
    }
}

object BrainyPalDictationVoiceCommandMatcher {
    fun match(text: String, waitingForChild: Boolean): BrainyPalDictationCommand {
        val normalized = normalize(text)
        if (normalized.isBlank()) return BrainyPalDictationCommand.UNKNOWN

        if (pausePhrases.any(normalized::contains)) return BrainyPalDictationCommand.PAUSE
        if (!waitingForChild && resumePhrases.any(normalized::contains)) {
            return BrainyPalDictationCommand.RESUME
        }
        if (repeatPhrases.any(normalized::contains)) return BrainyPalDictationCommand.REPEAT
        if (dontKnowPhrases.any(normalized::contains)) return BrainyPalDictationCommand.DONT_KNOW
        if (nextPhrases.any(normalized::contains)) return BrainyPalDictationCommand.NEXT
        if (waitingForChild && normalized in ambiguousNextPhrases) return BrainyPalDictationCommand.NEXT
        return BrainyPalDictationCommand.UNKNOWN
    }

    private fun normalize(text: String): String {
        return text
            .lowercase()
            .filter { it.isLetterOrDigit() || it in chineseRange }
    }

    private val chineseRange = '\u4e00'..'\u9fff'

    private val repeatPhrases = listOf(
        "再听一次",
        "再读一遍",
        "重来",
        "再来一遍",
        "没听清",
        "刚才没听清",
    )
    private val nextPhrases = listOf(
        "下一个",
        "下一题",
        "写好了",
        "好了下一个",
        "继续",
    )
    private val dontKnowPhrases = listOf(
        "不会",
        "我不会",
        "不知道",
        "跳过",
        "先跳过",
        "这个不会",
    )
    private val pausePhrases = listOf(
        "暂停",
        "等一下",
        "停一下",
    )
    private val resumePhrases = listOf(
        "继续",
        "开始",
        "接着来",
    )
    private val ambiguousNextPhrases = setOf("过", "好")
}

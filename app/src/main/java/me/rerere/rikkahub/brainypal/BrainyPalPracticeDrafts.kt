package me.rerere.rikkahub.brainypal

data class BrainyPalPracticeDraft(
    val answer: String = "",
    val evidence: String = "",
    val dirty: Boolean = false,
)

data class BrainyPalPracticeDrafts(
    private val values: Map<String, BrainyPalPracticeDraft> = emptyMap(),
) {
    fun get(itemId: String): BrainyPalPracticeDraft {
        return values[itemId] ?: BrainyPalPracticeDraft()
    }

    fun edit(itemId: String, answer: String, evidence: String): BrainyPalPracticeDrafts {
        return copy(
            values = values + (
                itemId to BrainyPalPracticeDraft(
                    answer = answer,
                    evidence = evidence,
                    dirty = true,
                )
            )
        )
    }

    fun markSaved(
        itemId: String,
        savedAnswer: String,
        savedEvidence: String,
    ): BrainyPalPracticeDrafts {
        val draft = values[itemId] ?: return this
        if (draft.answer != savedAnswer || draft.evidence != savedEvidence) {
            return this
        }
        return copy(values = values + (itemId to draft.copy(dirty = false)))
    }

    fun replaceFromDetail(detail: BrainyPalChildPracticeTaskDetail): BrainyPalPracticeDrafts {
        val next = detail.items.associate { item ->
            val current = values[item.itemId]
            item.itemId to if (current?.dirty == true) {
                current
            } else {
                BrainyPalPracticeDraft(
                    answer = item.childAnswer.orEmpty(),
                    evidence = item.attemptEvidence.orEmpty(),
                    dirty = false,
                )
            }
        }
        return BrainyPalPracticeDrafts(values = next)
    }
}

package me.rerere.rikkahub.brainypal.child

import me.rerere.rikkahub.brainypal.shared.BrainyPalDictationSessionStatus

object BrainyPalDictationPrimaryButtonPolicy {
    fun label(status: BrainyPalDictationSessionStatus): String {
        return when (status) {
            BrainyPalDictationSessionStatus.IDLE -> "开始听写"
            BrainyPalDictationSessionStatus.WAITING -> "重新开始"
            BrainyPalDictationSessionStatus.PAUSED -> "继续"
            BrainyPalDictationSessionStatus.FINISHED -> "已播完"
        }
    }
}

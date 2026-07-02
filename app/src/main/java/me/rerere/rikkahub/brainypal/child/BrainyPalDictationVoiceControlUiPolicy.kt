package me.rerere.rikkahub.brainypal.child

import me.rerere.asr.ASRStatus

data class BrainyPalDictationVoiceControlUiModel(
    val enabled: Boolean,
    val label: String,
    val helperText: String?,
)

object BrainyPalDictationVoiceControlUiPolicy {
    fun model(
        asrStatus: ASRStatus,
        asrAvailable: Boolean,
        audioPermissionGranted: Boolean,
    ): BrainyPalDictationVoiceControlUiModel {
        if (!audioPermissionGranted) {
            return BrainyPalDictationVoiceControlUiModel(
                enabled = asrStatus != ASRStatus.Connecting && asrStatus != ASRStatus.Stopping,
                label = "允许麦克风后使用语音",
                helperText = null,
            )
        }
        if (!asrAvailable && asrStatus != ASRStatus.Listening) {
            return BrainyPalDictationVoiceControlUiModel(
                enabled = false,
                label = "语音识别未配置",
                helperText = "当前没有可用 ASR，可以先用按钮继续。",
            )
        }
        return BrainyPalDictationVoiceControlUiModel(
            enabled = asrStatus != ASRStatus.Connecting && asrStatus != ASRStatus.Stopping,
            label = when (asrStatus) {
                ASRStatus.Listening -> "停止语音控制"
                ASRStatus.Connecting -> "正在连接语音控制"
                ASRStatus.Stopping -> "正在停止语音控制"
                ASRStatus.Idle,
                ASRStatus.Error -> "开启语音控制"
            },
            helperText = null,
        )
    }
}

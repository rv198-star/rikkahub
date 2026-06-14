package me.rerere.rikkahub.brainypal.shared

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface BrainyPalVoiceApi {
    @POST("/api/v1/voice/commands")
    suspend fun interpretVoiceCommand(
        @Body request: BrainyPalInterpretVoiceCommandRequest,
    ): BrainyPalVoiceCommandResponse
}

class BrainyPalVoiceApiFactory(
    private val okHttpClient: OkHttpClient,
    private val json: Json,
) {
    fun create(rootUrl: String, apiKey: String): BrainyPalVoiceApi {
        val client = okHttpClient.newBuilder()
            .addInterceptor(Interceptor { chain ->
                val original = chain.request()
                val request = if (apiKey.isNotBlank()) {
                    original.newBuilder()
                        .header("Authorization", "Bearer $apiKey")
                        .build()
                } else {
                    original
                }
                chain.proceed(request)
            })
            .build()

        return Retrofit.Builder()
            .baseUrl(rootUrl.trimEnd('/') + "/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json; charset=UTF8".toMediaType()))
            .build()
            .create(BrainyPalVoiceApi::class.java)
    }
}

@Serializable
data class BrainyPalInterpretVoiceCommandRequest(
    val transcript: String? = null,
    val context: String = "chat",
    val locale: String = "zh-CN",
    val provider: String = "fake_text",
    @SerialName("provider_failure_reason")
    val providerFailureReason: String? = null,
    @SerialName("provider_latency_ms")
    val providerLatencyMs: Int? = null,
)

@Serializable
data class BrainyPalVoiceCommandResponse(
    val intent: String,
    val confidence: String = "low",
    @SerialName("child_label")
    val childLabel: String = "",
    @SerialName("requires_clarification")
    val requiresClarification: Boolean = false,
    @SerialName("raw_transcript_persistent")
    val rawTranscriptPersistent: Boolean = false,
    val tts: BrainyPalVoiceTtsPayload = BrainyPalVoiceTtsPayload(),
    val provider: BrainyPalVoiceProviderPayload = BrainyPalVoiceProviderPayload(),
) {
    fun toVoiceAction(): BrainyPalVoiceAction {
        return when (intent) {
            "repeat" -> BrainyPalVoiceAction.REPEAT
            "next" -> BrainyPalVoiceAction.NEXT
            "dont_know" -> BrainyPalVoiceAction.DONT_KNOW
            "pause" -> BrainyPalVoiceAction.PAUSE
            "resume" -> BrainyPalVoiceAction.RESUME
            "ask_help" -> BrainyPalVoiceAction.ASK_HELP
            else -> BrainyPalVoiceAction.UNKNOWN
        }
    }

    fun toDictationCommand(): BrainyPalDictationCommand {
        return when (toVoiceAction()) {
            BrainyPalVoiceAction.REPEAT -> BrainyPalDictationCommand.REPEAT
            BrainyPalVoiceAction.NEXT -> BrainyPalDictationCommand.NEXT
            BrainyPalVoiceAction.DONT_KNOW -> BrainyPalDictationCommand.DONT_KNOW
            BrainyPalVoiceAction.PAUSE -> BrainyPalDictationCommand.PAUSE
            BrainyPalVoiceAction.RESUME -> BrainyPalDictationCommand.RESUME
            else -> BrainyPalDictationCommand.UNKNOWN
        }
    }
}

enum class BrainyPalVoiceAction {
    REPEAT,
    NEXT,
    DONT_KNOW,
    PAUSE,
    RESUME,
    ASK_HELP,
    UNKNOWN,
}

fun BrainyPalVoiceAction.toDictationCommand(): BrainyPalDictationCommand {
    return when (this) {
        BrainyPalVoiceAction.REPEAT -> BrainyPalDictationCommand.REPEAT
        BrainyPalVoiceAction.NEXT -> BrainyPalDictationCommand.NEXT
        BrainyPalVoiceAction.DONT_KNOW -> BrainyPalDictationCommand.DONT_KNOW
        BrainyPalVoiceAction.PAUSE -> BrainyPalDictationCommand.PAUSE
        BrainyPalVoiceAction.RESUME -> BrainyPalDictationCommand.RESUME
        else -> BrainyPalDictationCommand.UNKNOWN
    }
}

fun BrainyPalDictationCommand.toVoiceAction(): BrainyPalVoiceAction {
    return when (this) {
        BrainyPalDictationCommand.REPEAT -> BrainyPalVoiceAction.REPEAT
        BrainyPalDictationCommand.NEXT -> BrainyPalVoiceAction.NEXT
        BrainyPalDictationCommand.DONT_KNOW -> BrainyPalVoiceAction.DONT_KNOW
        BrainyPalDictationCommand.PAUSE -> BrainyPalVoiceAction.PAUSE
        BrainyPalDictationCommand.RESUME -> BrainyPalVoiceAction.RESUME
        else -> BrainyPalVoiceAction.UNKNOWN
    }
}

object BrainyPalVoiceCommandLocalMatcher {
    fun match(text: String): BrainyPalVoiceAction {
        val normalized = text.lowercase()
            .replace(" ", "")
            .replace("，", "")
            .replace("。", "")
            .replace(",", "")
            .replace(".", "")
        if (normalized.isBlank()) return BrainyPalVoiceAction.UNKNOWN
        if (listOf("提示", "帮我", "help", "hint").any(normalized::contains)) {
            return BrainyPalVoiceAction.ASK_HELP
        }
        if (listOf("再听", "再说", "重复", "repeat", "again").any(normalized::contains)) {
            return BrainyPalVoiceAction.REPEAT
        }
        if (listOf("下一", "下一个", "下一题", "next").any(normalized::contains)) {
            return BrainyPalVoiceAction.NEXT
        }
        if (listOf("不会", "不知道", "dontknow", "don'tknow").any(normalized::contains)) {
            return BrainyPalVoiceAction.DONT_KNOW
        }
        if (listOf("暂停", "停一下", "pause", "holdon").any(normalized::contains)) {
            return BrainyPalVoiceAction.PAUSE
        }
        if (listOf("继续", "开始", "resume", "goon").any(normalized::contains)) {
            return BrainyPalVoiceAction.RESUME
        }
        return BrainyPalVoiceAction.UNKNOWN
    }
}

object BrainyPalVoiceCommandInterpreter {
    suspend fun interpret(
        api: BrainyPalVoiceApi,
        transcript: String,
        context: String,
        audioPermissionGranted: Boolean,
        fallbackAction: BrainyPalVoiceAction = BrainyPalVoiceAction.UNKNOWN,
        locale: String = "zh-CN",
        provider: String = "app_asr",
    ): BrainyPalVoiceControlState {
        return try {
            val response = api.interpretVoiceCommand(
                BrainyPalInterpretVoiceCommandRequest(
                    transcript = transcript,
                    context = context,
                    locale = locale,
                    provider = provider,
                )
            )
            BrainyPalVoiceControlShell.fromCommandResponse(
                response = response,
                audioPermissionGranted = audioPermissionGranted,
            )
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            fallbackState(
                fallbackAction = fallbackAction,
                audioPermissionGranted = audioPermissionGranted,
            )
        }
    }

    fun fallbackState(
        fallbackAction: BrainyPalVoiceAction,
        audioPermissionGranted: Boolean,
    ): BrainyPalVoiceControlState {
        val hasAction = fallbackAction != BrainyPalVoiceAction.UNKNOWN
        return BrainyPalVoiceControlState(
            phase = BrainyPalVoiceControlPhase.FALLBACK,
            childMessage = if (hasAction) {
                "语音理解服务暂时不稳定，可以点按钮继续，避免误操作。"
            } else {
                "语音理解服务暂时不稳定，可以直接点按钮继续。"
            },
            canUseVoice = audioPermissionGranted,
            showButtonFallback = true,
            action = BrainyPalVoiceAction.UNKNOWN,
            dictationCommand = BrainyPalDictationCommand.UNKNOWN,
        )
    }
}

@Serializable
data class BrainyPalVoiceTtsPayload(
    val text: String = "",
    val context: String = "chat",
    val locale: String = "zh-CN",
)

@Serializable
data class BrainyPalVoiceProviderPayload(
    val provider: String = "fake_text",
    @SerialName("latency_ms")
    val latencyMs: Int = 0,
    @SerialName("failure_reason")
    val failureReason: String? = null,
)

enum class BrainyPalVoiceControlPhase {
    READY,
    LISTENING,
    RECOGNIZING,
    EXECUTING,
    PLAYING,
    FAILED,
    FALLBACK,
}

data class BrainyPalVoiceControlState(
    val phase: BrainyPalVoiceControlPhase,
    val childMessage: String,
    val canUseVoice: Boolean,
    val showButtonFallback: Boolean,
    val action: BrainyPalVoiceAction = BrainyPalVoiceAction.UNKNOWN,
    val dictationCommand: BrainyPalDictationCommand = BrainyPalDictationCommand.UNKNOWN,
)

object BrainyPalVoiceControlShell {
    fun ready(audioPermissionGranted: Boolean): BrainyPalVoiceControlState {
        return if (audioPermissionGranted) {
            BrainyPalVoiceControlState(
                phase = BrainyPalVoiceControlPhase.READY,
                childMessage = "可以说“再听一次”“下一个”“不会”“暂停”“继续”。",
                canUseVoice = true,
                showButtonFallback = false,
            )
        } else {
            BrainyPalVoiceControlState(
                phase = BrainyPalVoiceControlPhase.FALLBACK,
                childMessage = "没有麦克风权限时，可以直接用按钮继续。",
                canUseVoice = false,
                showButtonFallback = true,
            )
        }
    }

    fun fromCommandResponse(
        response: BrainyPalVoiceCommandResponse,
        audioPermissionGranted: Boolean,
    ): BrainyPalVoiceControlState {
        val providerFailed = response.provider.failureReason != null
        val fallback = providerFailed || response.requiresClarification || !audioPermissionGranted
        val executableAction = if (fallback) {
            BrainyPalVoiceAction.UNKNOWN
        } else {
            response.toVoiceAction()
        }
        return BrainyPalVoiceControlState(
            phase = when {
                providerFailed -> BrainyPalVoiceControlPhase.FAILED
                fallback -> BrainyPalVoiceControlPhase.FALLBACK
                response.intent == "unknown" -> BrainyPalVoiceControlPhase.FALLBACK
                else -> BrainyPalVoiceControlPhase.EXECUTING
            },
            childMessage = response.childLabel.ifBlank {
                if (fallback) "可以直接点按钮继续。" else "收到。"
            },
            canUseVoice = audioPermissionGranted && !providerFailed,
            showButtonFallback = fallback,
            action = executableAction,
            dictationCommand = executableAction.toDictationCommand(),
        )
    }
}

package me.rerere.rikkahub.brainypal.shared

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
    fun toDictationCommand(): BrainyPalDictationCommand {
        return when (intent) {
            "repeat" -> BrainyPalDictationCommand.REPEAT
            "next" -> BrainyPalDictationCommand.NEXT
            "dont_know" -> BrainyPalDictationCommand.DONT_KNOW
            "pause" -> BrainyPalDictationCommand.PAUSE
            "resume" -> BrainyPalDictationCommand.RESUME
            else -> BrainyPalDictationCommand.UNKNOWN
        }
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
        return BrainyPalVoiceControlState(
            phase = when {
                providerFailed -> BrainyPalVoiceControlPhase.FAILED
                response.intent == "unknown" -> BrainyPalVoiceControlPhase.FALLBACK
                else -> BrainyPalVoiceControlPhase.EXECUTING
            },
            childMessage = response.childLabel.ifBlank {
                if (fallback) "可以直接点按钮继续。" else "收到。"
            },
            canUseVoice = audioPermissionGranted && !providerFailed,
            showButtonFallback = fallback,
            dictationCommand = response.toDictationCommand(),
        )
    }
}

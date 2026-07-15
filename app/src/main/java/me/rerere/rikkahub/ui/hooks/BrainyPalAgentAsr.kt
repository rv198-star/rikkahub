package me.rerere.rikkahub.ui.hooks

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.Collections
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import me.rerere.asr.ASRController
import me.rerere.asr.ASRProviderSetting
import me.rerere.asr.ASRState
import me.rerere.asr.ASRStatus
import me.rerere.asr.appendAmplitude
import me.rerere.asr.calculateRmsAmplitude
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildConnectionConfig
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildModePolicy
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString.Companion.toByteString

private const val TAG = "BrainyPalAgentAsr"
private const val MAX_WEBSOCKET_QUEUE_BYTES = 100_000L
private const val NLS_SUCCESS_STATUS = 20_000_000

@Serializable
data class BrainyPalAgentAsrSessionConfig(
    val provider: String = "aliyun_nls_speech_transcriber",
    @SerialName("websocket_url")
    val websocketUrl: String,
    @SerialName("app_key")
    val appKey: String,
    val namespace: String = "SpeechTranscriber",
    @SerialName("start_command")
    val startCommand: String = "StartTranscription",
    @SerialName("stop_command")
    val stopCommand: String = "StopTranscription",
    @SerialName("audio_format")
    val audioFormat: String = "pcm",
    @SerialName("sample_rate")
    val sampleRate: Int = 16000,
    @SerialName("token_expires_at")
    val tokenExpiresAt: Long? = null,
)

object BrainyPalAgentAsrPolicy {
    fun shouldUseAgentAsr(
        selectedProvider: ASRProviderSetting?,
        connection: BrainyPalChildConnectionConfig,
    ): Boolean {
        return connection.isConfigured() && !selectedProvider.isConfigured()
    }

    private fun ASRProviderSetting?.isConfigured(): Boolean {
        return when (this) {
            null -> false
            is ASRProviderSetting.OpenAIRealtime -> apiKey.isNotBlank()
            is ASRProviderSetting.DashScope -> apiKey.isNotBlank()
            is ASRProviderSetting.Volcengine -> apiKey.isNotBlank()
        }
    }
}

sealed interface BrainyPalAgentAsrServerEvent {
    data object Started : BrainyPalAgentAsrServerEvent
    data object Completed : BrainyPalAgentAsrServerEvent
    data object Ignored : BrainyPalAgentAsrServerEvent
    data class Transcript(
        val key: String,
        val text: String,
        val isFinal: Boolean,
    ) : BrainyPalAgentAsrServerEvent
    data class Error(val message: String) : BrainyPalAgentAsrServerEvent
}

data class BrainyPalAgentAsrTranscriptUpdate(
    val transcript: String,
    val commandTranscript: String,
    val shouldNotifyCommand: Boolean,
)

class BrainyPalAgentAsrTranscriptBuffer {
    private val completedTranscripts = mutableListOf<String>()
    private val partialTranscripts = mutableMapOf<String, String>()

    fun clear() {
        completedTranscripts.clear()
        partialTranscripts.clear()
    }

    fun accept(event: BrainyPalAgentAsrServerEvent.Transcript): BrainyPalAgentAsrTranscriptUpdate {
        if (event.isFinal) {
            partialTranscripts.remove(event.key)
            completedTranscripts.add(event.text)
        } else {
            partialTranscripts[event.key] = event.text
        }
        return BrainyPalAgentAsrTranscriptUpdate(
            transcript = transcript(),
            commandTranscript = event.text,
            shouldNotifyCommand = event.isFinal,
        )
    }

    private fun transcript(): String {
        return (completedTranscripts + partialTranscripts.values)
            .filter { it.isNotBlank() }
            .joinToString(" ")
    }
}

object BrainyPalAgentAsrProtocol {
    fun startTranscriptionEvent(
        config: BrainyPalAgentAsrSessionConfig,
        taskId: String,
        messageId: String,
    ): String {
        return commandEvent(
            config = config,
            taskId = taskId,
            messageId = messageId,
            command = config.startCommand,
            payload = buildJsonObject {
                put("format", config.audioFormat)
                put("sample_rate", config.sampleRate)
                put("enable_intermediate_result", true)
                put("enable_punctuation_prediction", true)
                put("enable_inverse_text_normalization", true)
            },
        )
    }

    fun stopTranscriptionEvent(
        config: BrainyPalAgentAsrSessionConfig,
        taskId: String,
        messageId: String,
    ): String {
        return commandEvent(
            config = config,
            taskId = taskId,
            messageId = messageId,
            command = config.stopCommand,
            payload = buildJsonObject {},
        )
    }

    fun parseServerEvent(text: String): BrainyPalAgentAsrServerEvent {
        val root = runCatching {
            Json.parseToJsonElement(text).jsonObject
        }.getOrNull() ?: return BrainyPalAgentAsrServerEvent.Ignored
        val header = root["header"]?.jsonObject ?: return BrainyPalAgentAsrServerEvent.Ignored
        val name = header["name"]?.jsonPrimitive?.contentOrNull.orEmpty()
        val status = header["status"]?.jsonPrimitive?.intOrNull ?: NLS_SUCCESS_STATUS
        val statusMessage = header["status_message"]?.jsonPrimitive?.contentOrNull

        if (name == "TaskFailed" || status != NLS_SUCCESS_STATUS) {
            return BrainyPalAgentAsrServerEvent.Error(statusMessage ?: "ASR transcription failed")
        }

        val payload = root["payload"]?.jsonObject
        return when (name) {
            "TranscriptionStarted" -> BrainyPalAgentAsrServerEvent.Started
            "TranscriptionResultChanged" -> transcriptEvent(payload, isFinal = false)
            "SentenceEnd" -> transcriptEvent(payload, isFinal = true)
            "TranscriptionCompleted" -> BrainyPalAgentAsrServerEvent.Completed
            else -> BrainyPalAgentAsrServerEvent.Ignored
        }
    }

    private fun commandEvent(
        config: BrainyPalAgentAsrSessionConfig,
        taskId: String,
        messageId: String,
        command: String,
        payload: kotlinx.serialization.json.JsonObject,
    ): String {
        return buildJsonObject {
            put(
                "header",
                buildJsonObject {
                    put("appkey", config.appKey)
                    put("message_id", messageId)
                    put("task_id", taskId)
                    put("namespace", config.namespace)
                    put("name", command)
                },
            )
            put("payload", payload)
        }.toString()
    }

    private fun transcriptEvent(
        payload: kotlinx.serialization.json.JsonObject?,
        isFinal: Boolean,
    ): BrainyPalAgentAsrServerEvent {
        val result = payload?.get("result")?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
        if (result.isEmpty()) return BrainyPalAgentAsrServerEvent.Ignored
        val key = payload?.get("index")?.jsonPrimitive?.contentOrNull
            ?: payload?.get("sentence_id")?.jsonPrimitive?.contentOrNull
            ?: "default"
        return BrainyPalAgentAsrServerEvent.Transcript(key, result, isFinal)
    }
}

class BrainyPalAgentAsrController(
    private val context: Context,
    private val httpClient: OkHttpClient,
    private val connection: BrainyPalChildConnectionConfig,
) : ASRController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val json = Json { ignoreUnknownKeys = true }
    private val _state = MutableStateFlow(ASRState(isAvailable = true))
    override val state: StateFlow<ASRState> = _state.asStateFlow()

    private var webSocket: WebSocket? = null
    private var recorderJob: Job? = null
    private var audioRecord: AudioRecord? = null
    private var activeConfig: BrainyPalAgentAsrSessionConfig? = null
    private var activeTaskId: String? = null
    private var onTranscriptChange: ((String) -> Unit)? = null
    private val transcriptBuffer = BrainyPalAgentAsrTranscriptBuffer()

    override fun start(onTranscriptChange: (String) -> Unit) {
        if (state.value.isRecording) return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            setError("Microphone permission is required")
            return
        }

        this.onTranscriptChange = onTranscriptChange
        transcriptBuffer.clear()
        _state.update {
            ASRState(
                status = ASRStatus.Connecting,
                isAvailable = true,
            )
        }

        scope.launch(Dispatchers.IO) {
            val config = runCatching { fetchSessionConfig() }.getOrElse {
                Log.e(TAG, "Failed to fetch BrainyPal ASR session", it)
                setError(it.message ?: "BrainyPal ASR session is unavailable")
                return@launch
            }
            activeConfig = config
            val taskId = newNlsId()
            activeTaskId = taskId
            val request = Request.Builder()
                .url(config.websocketUrl)
                .build()

            webSocket = httpClient.newWebSocket(
                request,
                createWebSocketListener(config, taskId),
            )
        }
    }

    override fun stop() {
        recorderJob?.cancel()
        releaseRecorder()
        val socket = webSocket
        val config = activeConfig
        val taskId = activeTaskId
        if (socket != null) {
            _state.update { it.copy(status = ASRStatus.Stopping) }
            if (config != null && taskId != null) {
                socket.send(
                    BrainyPalAgentAsrProtocol.stopTranscriptionEvent(
                        config = config,
                        taskId = taskId,
                        messageId = newNlsId(),
                    )
                )
            }
            scope.launch {
                delay(500)
                socket.close(1000, "stop")
                if (webSocket === socket) {
                    webSocket = null
                    _state.update { it.copy(status = ASRStatus.Idle) }
                }
            }
        } else {
            _state.update { it.copy(status = ASRStatus.Idle) }
        }
    }

    override fun dispose() {
        stop()
        scope.cancel()
    }

    private fun fetchSessionConfig(): BrainyPalAgentAsrSessionConfig {
        val endpoint = "${BrainyPalChildModePolicy.agentServiceRootUrl(connection)}/api/v1/voice/asr/session"
        val requestBuilder = Request.Builder()
            .url(endpoint)
            .get()
        if (connection.apiKey.isNotBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer ${connection.apiKey}")
        }
        httpClient.newCall(requestBuilder.build()).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("BrainyPal ASR session failed: HTTP ${response.code}")
            }
            return json.decodeFromString(response.body.string())
        }
    }

    private fun createWebSocketListener(
        config: BrainyPalAgentAsrSessionConfig,
        taskId: String,
    ): WebSocketListener {
        return object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                webSocket.send(
                    BrainyPalAgentAsrProtocol.startTranscriptionEvent(
                        config = config,
                        taskId = taskId,
                        messageId = newNlsId(),
                    )
                )
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleServerEvent(webSocket, config, text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "BrainyPal ASR websocket failed", t)
                releaseRecorder()
                setError(t.message ?: "BrainyPal ASR websocket failed")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                releaseRecorder()
                if (this@BrainyPalAgentAsrController.webSocket === webSocket) {
                    this@BrainyPalAgentAsrController.webSocket = null
                }
                _state.update {
                    it.copy(
                        status = ASRStatus.Idle,
                        errorMessage = null,
                    )
                }
            }
        }
    }

    private fun handleServerEvent(
        socket: WebSocket,
        config: BrainyPalAgentAsrSessionConfig,
        text: String,
    ) {
        when (val event = BrainyPalAgentAsrProtocol.parseServerEvent(text)) {
            BrainyPalAgentAsrServerEvent.Started -> {
                _state.update { it.copy(status = ASRStatus.Listening, errorMessage = null) }
                if (recorderJob == null) startRecorder(config, socket)
            }

            is BrainyPalAgentAsrServerEvent.Transcript -> {
                publishTranscript(transcriptBuffer.accept(event))
            }

            is BrainyPalAgentAsrServerEvent.Error -> setError(event.message)

            BrainyPalAgentAsrServerEvent.Completed -> {
                releaseRecorder()
                _state.update { it.copy(status = ASRStatus.Idle, errorMessage = null) }
            }

            BrainyPalAgentAsrServerEvent.Ignored -> Log.v(TAG, "Ignored NLS event")
        }
    }

    @SuppressLint("MissingPermission")
    private fun startRecorder(
        config: BrainyPalAgentAsrSessionConfig,
        socket: WebSocket,
    ) {
        recorderJob?.cancel()
        recorderJob = scope.launch(Dispatchers.IO) {
            val minBufferSize = AudioRecord.getMinBufferSize(
                config.sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
            )
            val chunkSize = (config.sampleRate * 2 * 200 / 1000)
                .coerceAtLeast(minBufferSize)
                .coerceAtLeast(4096)

            val recorder = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                config.sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                chunkSize * 2,
            )
            audioRecord = recorder

            try {
                recorder.startRecording()
                val buffer = ByteArray(chunkSize)
                while (isActive) {
                    val read = recorder.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        val amplitude = calculateRmsAmplitude(buffer, read)
                        _state.update { it.copy(amplitudes = it.amplitudes.appendAmplitude(amplitude)) }
                        if (socket.queueSize() < MAX_WEBSOCKET_QUEUE_BYTES) {
                            socket.send(buffer.toByteString(0, read))
                        } else {
                            Log.w(TAG, "WebSocket queue full, dropping audio frame")
                        }
                    } else if (read < 0) {
                        throw IllegalStateException("AudioRecord read error: $read")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Audio recording failed", e)
                setError(e.message ?: "Audio recording failed")
            } finally {
                releaseRecorder()
            }
        }
    }

    private fun publishTranscript(update: BrainyPalAgentAsrTranscriptUpdate) {
        _state.update { it.copy(transcript = update.transcript, errorMessage = null) }
        if (update.shouldNotifyCommand) {
            scope.launch {
                onTranscriptChange?.invoke(update.commandTranscript)
            }
        }
    }

    private fun setError(message: String) {
        _state.update {
            it.copy(
                status = ASRStatus.Error,
                isAvailable = true,
                errorMessage = message,
            )
        }
    }

    private fun releaseRecorder() {
        recorderJob = null
        runCatching { audioRecord?.stop() }
        runCatching { audioRecord?.release() }
        audioRecord = null
    }

    private fun newNlsId(): String = UUID.randomUUID().toString().replace("-", "")
}

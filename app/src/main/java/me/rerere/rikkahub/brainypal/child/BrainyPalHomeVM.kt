package me.rerere.rikkahub.brainypal.child

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildApiFactory
import me.rerere.rikkahub.brainypal.child.BrainyPalChildHomeState
import me.rerere.rikkahub.brainypal.shared.BrainyPalAchievementStationResponse
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildModePolicy
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskDetail
import me.rerere.rikkahub.brainypal.shared.BrainyPalConfirmDictationOcrEvidenceRequest
import me.rerere.rikkahub.brainypal.shared.BrainyPalCreatePracticeHandoffCodeRequest
import me.rerere.rikkahub.brainypal.child.BrainyPalPracticeDrafts
import me.rerere.rikkahub.brainypal.shared.BrainyPalRecordPracticeTaskAnswerRequest
import me.rerere.rikkahub.brainypal.shared.BrainyPalRequestPracticeTaskHelpRequest
import me.rerere.rikkahub.brainypal.shared.BrainyPalSubmitDictationOcrEvidenceRequest
import me.rerere.rikkahub.brainypal.shared.BrainyPalSubmitOralEvidenceRequest
import me.rerere.rikkahub.brainypal.shared.BrainyPalVoiceAction
import me.rerere.rikkahub.brainypal.shared.BrainyPalVoiceApiFactory
import me.rerere.rikkahub.brainypal.shared.BrainyPalVoiceCommandInterpreter
import me.rerere.rikkahub.brainypal.shared.BrainyPalVoiceControlState
import me.rerere.rikkahub.data.datastore.SettingsStore
import me.rerere.rikkahub.utils.UiState
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import kotlin.uuid.Uuid

data class BrainyPalPracticeTaskActionStatus(
    val message: String,
    val error: Boolean = false,
)

data class BrainyPalPracticeTaskHelpHint(
    val itemId: String,
    val message: String,
)

data class BrainyPalPracticeTaskDetailState(
    val selectedTaskId: String? = null,
    val selectedItemIndex: Int = 0,
    val detail: UiState<BrainyPalChildPracticeTaskDetail> = UiState.Idle,
    val drafts: BrainyPalPracticeDrafts = BrainyPalPracticeDrafts(),
    val helpHint: BrainyPalPracticeTaskHelpHint? = null,
    val handoffDisplay: BrainyPalPracticeHandoffDisplay? = null,
    val oralAudioRefs: Map<String, String> = emptyMap(),
    val actionInProgress: Boolean = false,
    val actionStatus: BrainyPalPracticeTaskActionStatus? = null,
)

class BrainyPalHomeVM(
    private val settingsStore: SettingsStore,
    private val apiFactory: BrainyPalChildApiFactory,
    private val voiceApiFactory: BrainyPalVoiceApiFactory,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val chatScreen = Screen.Chat(id = Uuid.random().toString())
    private val _state = MutableStateFlow<UiState<BrainyPalChildHomeState>>(UiState.Loading)
    val state: StateFlow<UiState<BrainyPalChildHomeState>> = _state.asStateFlow()
    private val _practiceDetailState = MutableStateFlow(
        BrainyPalPracticeTaskDetailState(
            selectedTaskId = savedStateHandle[KEY_SELECTED_TASK],
            selectedItemIndex = savedStateHandle[KEY_SELECTED_ITEM_INDEX] ?: 0,
            drafts = savedStateHandle.get<String>(KEY_DRAFTS)
                ?.let { runCatching { Json.decodeFromString<BrainyPalPracticeDrafts>(it) }.getOrNull() }
                ?: BrainyPalPracticeDrafts(),
        )
    )
    val practiceDetailState: StateFlow<BrainyPalPracticeTaskDetailState> = _practiceDetailState.asStateFlow()
    private val _stationState = MutableStateFlow<UiState<BrainyPalAchievementStationResponse>>(UiState.Idle)
    val stationState: StateFlow<UiState<BrainyPalAchievementStationResponse>> = _stationState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsStore.settingsFlow
                .brainyPalChildHomeStates(
                    apiFactory = apiFactory::create,
                    chatScreen = chatScreen,
                )
                .collect { state ->
                    _state.value = UiState.Success(state)
                }
        }
        viewModelScope.launch {
            _practiceDetailState.drop(1).collect(::persistPracticeState)
        }
        _practiceDetailState.value.selectedTaskId?.let(::selectPracticeTask)
    }

    fun refresh() {
        viewModelScope.launch {
            val settings = settingsStore.settingsFlow
                .filter { !it.init }
                .first()
            _state.value = UiState.Loading
            _state.value = UiState.Success(
                BrainyPalChildHomeState.from(
                    connection = settings.brainyPalChildConnection,
                    apiFactory = apiFactory::create,
                    chatScreen = chatScreen,
                )
            )
        }
    }

    fun refreshStation(historyLimit: Int = 20) {
        viewModelScope.launch {
            _stationState.value = UiState.Loading
            runCatching {
                practiceApi().getAchievementStation(historyLimit = historyLimit)
            }.onSuccess { station ->
                _stationState.value = UiState.Success(station)
            }.onFailure { error ->
                if (error is CancellationException) {
                    throw error
                }
                _stationState.value = UiState.Error(error)
            }
        }
    }

    fun selectPracticeTask(taskId: String) {
        val current = _practiceDetailState.value
        val preserveDrafts = savedStateHandle.get<String>(KEY_RETAINED_TASK) == taskId
        _practiceDetailState.value = BrainyPalPracticeTaskDetailState(
            selectedTaskId = taskId,
            selectedItemIndex = if (preserveDrafts) current.selectedItemIndex else 0,
            detail = UiState.Loading,
            drafts = if (preserveDrafts) current.drafts else BrainyPalPracticeDrafts(),
            handoffDisplay = if (preserveDrafts) current.handoffDisplay else null,
        )
        savedStateHandle[KEY_RETAINED_TASK] = taskId
        persistPracticeState(_practiceDetailState.value)
        viewModelScope.launch {
            runCatching {
                val api = practiceApi()
                val detail = api.getPracticeTask(taskId)
                if (BrainyPalPracticeTaskLifecycle.shouldAcceptOnOpen(detail.status)) {
                    api.acceptPracticeTask(taskId)
                } else {
                    detail
                }
            }.onSuccess { detail ->
                if (!isSelectedPracticeTask(taskId)) return@onSuccess
                val activeState = _practiceDetailState.value
                _practiceDetailState.value = activeState.copy(
                    detail = UiState.Success(detail),
                    drafts = activeState.drafts.replaceFromDetail(detail),
                    selectedItemIndex = activeState.selectedItemIndex
                        .coerceIn(0, (detail.items.size - 1).coerceAtLeast(0)),
                )
                refresh()
            }.onFailure { error ->
                if (error is CancellationException) {
                    throw error
                }
                if (!isSelectedPracticeTask(taskId)) return@onFailure
                _practiceDetailState.value = _practiceDetailState.value.copy(
                    detail = UiState.Error(error),
                    actionStatus = BrainyPalPracticeTaskActionStatus(
                        message = "暂时连不上 BrainyPal，可以稍后再试",
                        error = true,
                    ),
                )
            }
        }
    }

    fun closePracticeTask() {
        _practiceDetailState.value = _practiceDetailState.value.copy(
            selectedTaskId = null,
            detail = UiState.Idle,
            helpHint = null,
            handoffDisplay = null,
            actionInProgress = false,
            actionStatus = null,
        )
        persistPracticeState(_practiceDetailState.value)
    }

    fun selectPracticeItem(index: Int) {
        val detail = (_practiceDetailState.value.detail as? UiState.Success)?.data ?: return
        _practiceDetailState.value = _practiceDetailState.value.copy(
            selectedItemIndex = index.coerceIn(0, (detail.items.size - 1).coerceAtLeast(0)),
            actionStatus = null,
        )
        persistPracticeState(_practiceDetailState.value)
    }

    fun savePracticeAnswer(taskId: String, itemId: String, answer: String, evidence: String) {
        if (!isSelectedPracticeTask(taskId)) return
        updatePracticeDraft(itemId = itemId, answer = answer, evidence = evidence)
        val attemptSessionId = currentPracticeAttemptSessionId()
        updatePracticeTask(
            taskId = taskId,
            successMessage = BrainyPalPracticeActionFeedback.SAVE_SUCCESS_MESSAGE,
            pendingMessage = BrainyPalPracticeActionFeedback.SAVE_PENDING_MESSAGE,
            savedAnswer = SavedPracticeAnswer(
                itemId = itemId,
                answer = answer,
                evidence = evidence,
            ),
        ) {
            recordPracticeTaskAnswer(
                taskId = taskId,
                itemId = itemId,
                request = BrainyPalRecordPracticeTaskAnswerRequest(
                    attemptSessionId = attemptSessionId.orEmpty(),
                    answer = answer,
                    source = "app",
                    childAnswer = answer,
                    attemptEvidence = evidence,
                ),
            )
        }
    }

    fun updatePracticeDraft(itemId: String, answer: String, evidence: String) {
        _practiceDetailState.value = _practiceDetailState.value.copy(
            drafts = _practiceDetailState.value.drafts.edit(
                itemId = itemId,
                answer = answer,
                evidence = evidence,
            )
        )
        persistPracticeState(_practiceDetailState.value)
    }

    fun requestPracticeHelp(taskId: String, itemId: String, requestedAction: String = "hint") {
        if (!isSelectedPracticeTask(taskId)) return
        val attemptSessionId = currentPracticeAttemptSessionId()
        viewModelScope.launch {
            if (!isSelectedPracticeTask(taskId)) return@launch
            val previousHint = _practiceDetailState.value.helpHint
            val previousDrafts = _practiceDetailState.value.drafts
            _practiceDetailState.value = _practiceDetailState.value.copy(
                selectedTaskId = taskId,
                actionInProgress = true,
                helpHint = BrainyPalPracticeActionFeedback.pendingHelpHintFor(
                    helpItemId = itemId,
                    previousHint = previousHint,
                ),
                actionStatus = BrainyPalPracticeActionFeedback.pendingStatus(
                    BrainyPalPracticeActionFeedback.HELP_PENDING_MESSAGE,
                ),
            )
            runCatching {
                val api = practiceApi()
                val hint = api.requestPracticeTaskHelp(
                    taskId = taskId,
                    request = BrainyPalRequestPracticeTaskHelpRequest(
                        attemptSessionId = attemptSessionId.orEmpty(),
                        itemId = itemId,
                        requestedAction = requestedAction,
                    ),
                )
                hint to api.getPracticeTask(taskId)
            }.onSuccess { (hint, detail) ->
                if (!isSelectedPracticeTask(taskId)) return@onSuccess
                _practiceDetailState.value = _practiceDetailState.value.copy(
                    detail = UiState.Success(detail),
                    drafts = previousDrafts.replaceFromDetail(detail),
                    helpHint = BrainyPalPracticeTaskHelpHint(
                        itemId = hint.itemId,
                        message = hint.hint.ifBlank { hint.waitingLabel },
                    ),
                    actionInProgress = false,
                    actionStatus = BrainyPalPracticeTaskActionStatus(
                        message = BrainyPalPracticeActionFeedback.HELP_SUCCESS_MESSAGE,
                    ),
                )
                refresh()
            }.onFailure { error ->
                if (error is CancellationException) {
                    throw error
                }
                if (!isSelectedPracticeTask(taskId)) return@onFailure
                val helpExhausted = error is HttpException && error.code() == 409
                _practiceDetailState.value = _practiceDetailState.value.copy(
                    actionInProgress = false,
                    helpHint = previousHint,
                    actionStatus = BrainyPalPracticeTaskActionStatus(
                        message = if (helpExhausted) {
                            "提示次数已经用完，可以先写下已知条件或卡住的地方。"
                        } else {
                            "暂时连不上 BrainyPal，可以稍后再试"
                        },
                        error = true,
                    ),
                )
            }
        }
    }

    fun createPracticeHandoffCode(taskId: String) {
        if (!isSelectedPracticeTask(taskId)) return
        viewModelScope.launch {
            if (!isSelectedPracticeTask(taskId)) return@launch
            val previous = _practiceDetailState.value
            _practiceDetailState.value = previous.copy(
                selectedTaskId = taskId,
                actionInProgress = true,
                actionStatus = BrainyPalPracticeTaskActionStatus(
                    message = "正在生成电脑接力码...",
                ),
            )
            runCatching {
                practiceApi().createPracticeTaskHandoffCode(
                    taskId = taskId,
                    request = BrainyPalCreatePracticeHandoffCodeRequest(channel = "web"),
                )
            }.onSuccess { handoff ->
                if (!isSelectedPracticeTask(taskId)) return@onSuccess
                _practiceDetailState.value = _practiceDetailState.value.copy(
                    handoffDisplay = BrainyPalPracticeExternalWork.handoffDisplay(handoff),
                    actionInProgress = false,
                    actionStatus = BrainyPalPracticeTaskActionStatus(
                        message = "电脑接力码已生成",
                    ),
                )
            }.onFailure { error ->
                if (error is CancellationException) {
                    throw error
                }
                if (!isSelectedPracticeTask(taskId)) return@onFailure
                _practiceDetailState.value = previous.copy(
                    actionInProgress = false,
                    actionStatus = BrainyPalPracticeTaskActionStatus(
                        message = "暂时生成不了电脑接力码，可以稍后再试",
                        error = true,
                    ),
                )
            }
        }
    }

    fun submitPracticeTask(taskId: String) {
        val attemptSessionId = currentPracticeAttemptSessionId()
        updatePracticeTask(
            taskId = taskId,
            successMessage = "已提交练习",
            pendingMessage = BrainyPalPracticeActionFeedback.SUBMIT_PENDING_MESSAGE,
            focusLowEffortOnResponse = true,
        ) {
            submitPracticeTask(
                taskId = taskId,
                request = me.rerere.rikkahub.brainypal.shared.BrainyPalSubmitPracticeTaskRequest(
                    attemptSessionId = attemptSessionId.orEmpty(),
                ),
            )
        }
    }

    fun submitDictationOcrEvidence(
        taskId: String,
        request: BrainyPalSubmitDictationOcrEvidenceRequest,
    ) {
        updatePracticeTask(
            taskId = taskId,
            successMessage = "OCR 批改已生成，请确认需要人工判断的地方",
            pendingMessage = "正在提交 OCR 批改结果...",
        ) {
            submitDictationOcrEvidence(taskId = taskId, request = request)
        }
    }

    fun uploadOralAudio(
        taskId: String,
        itemId: String,
        file: File,
    ) {
        viewModelScope.launch {
            _practiceDetailState.value = _practiceDetailState.value.copy(
                selectedTaskId = taskId,
                actionInProgress = true,
                actionStatus = BrainyPalPracticeActionFeedback.pendingStatus("正在上传录音..."),
            )
            runCatching {
                practiceApi().uploadOralAudio(
                    taskId = taskId,
                    itemId = itemId.toRequestBody("text/plain".toMediaType()),
                    file = oralAudioPart(file),
                )
            }.onSuccess { upload ->
                _practiceDetailState.value = _practiceDetailState.value.copy(
                    selectedTaskId = taskId,
                    oralAudioRefs = _practiceDetailState.value.oralAudioRefs + (itemId to upload.audioRef),
                    actionInProgress = false,
                    actionStatus = BrainyPalPracticeTaskActionStatus("录音已上传，提交后会自动识别"),
                )
            }.onFailure { error ->
                if (error is CancellationException) {
                    throw error
                }
                _practiceDetailState.value = _practiceDetailState.value.copy(
                    selectedTaskId = taskId,
                    actionInProgress = false,
                    actionStatus = BrainyPalPracticeTaskActionStatus(
                        message = "录音上传失败，可以稍后再试",
                        error = true,
                    ),
                )
            }
        }
    }

    fun submitOralEvidence(
        taskId: String,
        request: BrainyPalSubmitOralEvidenceRequest,
    ) {
        updatePracticeTask(
            taskId = taskId,
            successMessage = "已提交朗读/背诵结果，可以看复盘了",
            pendingMessage = "正在提交朗读/背诵结果...",
        ) {
            submitOralEvidence(taskId = taskId, request = request).toTaskDetail()
        }
    }

    fun confirmDictationOcrEvidence(
        taskId: String,
        itemId: String,
        confirmation: String,
        note: String? = null,
    ) {
        updatePracticeTask(
            taskId = taskId,
            successMessage = "确认结果已保存",
            pendingMessage = "正在保存确认结果...",
        ) {
            confirmDictationOcrEvidence(
                taskId = taskId,
                itemId = itemId,
                request = BrainyPalConfirmDictationOcrEvidenceRequest(
                    confirmation = confirmation,
                    note = note,
                ),
            )
        }
    }

    suspend fun interpretVoiceCommand(
        context: String,
        transcript: String,
        fallbackAction: BrainyPalVoiceAction,
        audioPermissionGranted: Boolean,
    ): BrainyPalVoiceControlState {
        val settings = settingsStore.settingsFlow
            .filter { !it.init }
            .first()
        val connection = settings.brainyPalChildConnection
        if (!connection.isConfigured()) {
            return BrainyPalVoiceCommandInterpreter.fallbackState(
                fallbackAction = fallbackAction,
                audioPermissionGranted = audioPermissionGranted,
            )
        }
        return BrainyPalVoiceCommandInterpreter.interpret(
            api = voiceApiFactory.create(
                BrainyPalChildModePolicy.agentServiceRootUrl(connection),
                connection.apiKey,
            ),
            transcript = transcript,
            context = context,
            audioPermissionGranted = audioPermissionGranted,
            fallbackAction = fallbackAction,
        )
    }

    private fun updatePracticeTask(
        taskId: String,
        successMessage: String,
        pendingMessage: String? = null,
        savedAnswer: SavedPracticeAnswer? = null,
        helpItemId: String? = null,
        focusLowEffortOnResponse: Boolean = false,
        action: suspend me.rerere.rikkahub.brainypal.shared.BrainyPalChildApi.() -> BrainyPalChildPracticeTaskDetail,
    ) {
        if (!isSelectedPracticeTask(taskId)) return
        viewModelScope.launch {
            if (!isSelectedPracticeTask(taskId)) return@launch
            val previousHint = _practiceDetailState.value.helpHint
            _practiceDetailState.value = _practiceDetailState.value.copy(
                actionInProgress = true,
                helpHint = BrainyPalPracticeActionFeedback.pendingHelpHintFor(
                    helpItemId = helpItemId,
                    previousHint = previousHint,
                ),
                actionStatus = pendingMessage?.let(BrainyPalPracticeActionFeedback::pendingStatus),
            )
            runCatching {
                practiceApi().action()
            }.onSuccess { detail ->
                if (!isSelectedPracticeTask(taskId)) return@onSuccess
                val currentDrafts = savedAnswer
                    ?.let {
                        _practiceDetailState.value.drafts.markSaved(
                            itemId = it.itemId,
                            savedAnswer = it.answer,
                            savedEvidence = it.evidence,
                        )
                    }
                    ?: _practiceDetailState.value.drafts
                val message = if (detail.needsMoreEffort) {
                    "先写一个已知条件、尝试答案或卡住点，再提交。"
                } else {
                    successMessage
                }
                val helpHint = BrainyPalPracticeActionFeedback.helpHintFor(
                    helpItemId = helpItemId,
                    helpMessage = detail.helpMessage,
                    previousHint = previousHint,
                )
                val actionStatus = BrainyPalPracticeActionFeedback.resultStatus(
                    successMessage = message,
                    needsMoreEffort = detail.needsMoreEffort,
                    helpItemId = helpItemId,
                    helpMessage = detail.helpMessage,
                )
                val selectedItemIndex = if (focusLowEffortOnResponse && detail.needsMoreEffort) {
                    detail.items.indexOfFirst { it.needsMoreEffort }.takeIf { it >= 0 }
                        ?: _practiceDetailState.value.selectedItemIndex
                } else {
                    _practiceDetailState.value.selectedItemIndex
                }
                _practiceDetailState.value = _practiceDetailState.value.copy(
                    detail = UiState.Success(detail),
                    drafts = currentDrafts.replaceFromDetail(detail),
                    helpHint = helpHint,
                    handoffDisplay = _practiceDetailState.value.handoffDisplay,
                    oralAudioRefs = _practiceDetailState.value.oralAudioRefs,
                    selectedItemIndex = selectedItemIndex,
                    actionInProgress = false,
                    actionStatus = actionStatus,
                )
                refresh()
            }.onFailure { error ->
                if (error is CancellationException) {
                    throw error
                }
                if (!isSelectedPracticeTask(taskId)) return@onFailure
                _practiceDetailState.value = _practiceDetailState.value.copy(
                    actionInProgress = false,
                    helpHint = if (helpItemId != null) previousHint else _practiceDetailState.value.helpHint,
                    actionStatus = BrainyPalPracticeTaskActionStatus(
                        message = "暂时连不上 BrainyPal，可以稍后再试",
                        error = true,
                    ),
                )
            }
        }
    }

    private suspend fun practiceApi(): me.rerere.rikkahub.brainypal.shared.BrainyPalChildApi {
        val settings = settingsStore.settingsFlow
            .filter { !it.init }
            .first()
        val connection = settings.brainyPalChildConnection
        return apiFactory.create(
            BrainyPalChildModePolicy.agentServiceRootUrl(connection),
            connection.apiKey,
        )
    }

    private fun currentPracticeAttemptSessionId(): String? {
        return when (val detail = _practiceDetailState.value.detail) {
            is UiState.Success -> detail.data.attemptSessionId
            else -> null
        }
    }

    private fun isSelectedPracticeTask(taskId: String): Boolean {
        return _practiceDetailState.value.selectedTaskId == taskId
    }

    private fun persistPracticeState(state: BrainyPalPracticeTaskDetailState) {
        savedStateHandle[KEY_SELECTED_TASK] = state.selectedTaskId
        savedStateHandle[KEY_SELECTED_ITEM_INDEX] = state.selectedItemIndex
        savedStateHandle[KEY_DRAFTS] = Json.encodeToString(state.drafts)
    }

    private companion object {
        const val KEY_SELECTED_TASK = "brainypal.practice.selectedTask"
        const val KEY_RETAINED_TASK = "brainypal.practice.retainedTask"
        const val KEY_SELECTED_ITEM_INDEX = "brainypal.practice.selectedItemIndex"
        const val KEY_DRAFTS = "brainypal.practice.drafts"
    }
}

private data class SavedPracticeAnswer(
    val itemId: String,
    val answer: String,
    val evidence: String,
)

private fun oralAudioPart(file: File): MultipartBody.Part {
    val body = file.asRequestBody(oralAudioMediaType(file).toMediaType())
    return MultipartBody.Part.createFormData("file", file.name, body)
}

private fun oralAudioMediaType(file: File): String {
    return when (file.extension.lowercase()) {
        "wav" -> "audio/wav"
        "mp3" -> "audio/mpeg"
        "ogg" -> "audio/ogg"
        "opus" -> "audio/opus"
        "webm" -> "audio/webm"
        "pcm" -> "audio/L16"
        else -> "audio/mp4"
    }
}

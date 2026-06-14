package me.rerere.rikkahub.brainypal.child

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildApiFactory
import me.rerere.rikkahub.brainypal.child.BrainyPalChildHomeState
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildModePolicy
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskDetail
import me.rerere.rikkahub.brainypal.shared.BrainyPalConfirmDictationOcrEvidenceRequest
import me.rerere.rikkahub.brainypal.shared.BrainyPalCreatePracticeHandoffCodeRequest
import me.rerere.rikkahub.brainypal.child.BrainyPalPracticeDrafts
import me.rerere.rikkahub.brainypal.shared.BrainyPalRecordPracticeTaskAnswerRequest
import me.rerere.rikkahub.brainypal.shared.BrainyPalRequestPracticeTaskHelpRequest
import me.rerere.rikkahub.brainypal.shared.BrainyPalSubmitDictationOcrEvidenceRequest
import me.rerere.rikkahub.data.datastore.SettingsStore
import me.rerere.rikkahub.utils.UiState
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
    val detail: UiState<BrainyPalChildPracticeTaskDetail> = UiState.Idle,
    val drafts: BrainyPalPracticeDrafts = BrainyPalPracticeDrafts(),
    val helpHint: BrainyPalPracticeTaskHelpHint? = null,
    val handoffDisplay: BrainyPalPracticeHandoffDisplay? = null,
    val actionInProgress: Boolean = false,
    val actionStatus: BrainyPalPracticeTaskActionStatus? = null,
)

class BrainyPalHomeVM(
    private val settingsStore: SettingsStore,
    private val apiFactory: BrainyPalChildApiFactory,
) : ViewModel() {
    private val chatScreen = Screen.Chat(id = Uuid.random().toString())
    private val _state = MutableStateFlow<UiState<BrainyPalChildHomeState>>(UiState.Loading)
    val state: StateFlow<UiState<BrainyPalChildHomeState>> = _state.asStateFlow()
    private val _practiceDetailState = MutableStateFlow(BrainyPalPracticeTaskDetailState())
    val practiceDetailState: StateFlow<BrainyPalPracticeTaskDetailState> = _practiceDetailState.asStateFlow()

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

    fun selectPracticeTask(taskId: String) {
        viewModelScope.launch {
            _practiceDetailState.value = BrainyPalPracticeTaskDetailState(
                selectedTaskId = taskId,
                detail = UiState.Loading,
            )
            runCatching {
                val api = practiceApi()
                val detail = api.getPracticeTask(taskId)
                if (BrainyPalPracticeTaskLifecycle.shouldAcceptOnOpen(detail.status)) {
                    api.acceptPracticeTask(taskId)
                } else {
                    detail
                }
            }.onSuccess { detail ->
                _practiceDetailState.value = BrainyPalPracticeTaskDetailState(
                    selectedTaskId = taskId,
                    detail = UiState.Success(detail),
                    drafts = BrainyPalPracticeDrafts().replaceFromDetail(detail),
                )
                refresh()
            }.onFailure { error ->
                if (error is CancellationException) {
                    throw error
                }
                _practiceDetailState.value = BrainyPalPracticeTaskDetailState(
                    selectedTaskId = taskId,
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
        _practiceDetailState.value = BrainyPalPracticeTaskDetailState()
    }

    fun savePracticeAnswer(taskId: String, itemId: String, answer: String, evidence: String) {
        updatePracticeDraft(itemId = itemId, answer = answer, evidence = evidence)
        val attemptSessionId = currentPracticeAttemptSessionId()
        updatePracticeTask(
            taskId = taskId,
            successMessage = "答案已保存",
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
    }

    fun requestPracticeHelp(taskId: String, itemId: String, requestedAction: String = "hint") {
        viewModelScope.launch {
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
                        attemptSessionId = currentPracticeAttemptSessionId().orEmpty(),
                        itemId = itemId,
                        requestedAction = requestedAction,
                    ),
                )
                hint to api.getPracticeTask(taskId)
            }.onSuccess { (hint, detail) ->
                _practiceDetailState.value = BrainyPalPracticeTaskDetailState(
                    selectedTaskId = taskId,
                    detail = UiState.Success(detail),
                    drafts = previousDrafts.replaceFromDetail(detail),
                    helpHint = BrainyPalPracticeTaskHelpHint(
                        itemId = hint.itemId,
                        message = hint.hint.ifBlank { hint.waitingLabel },
                    ),
                    handoffDisplay = _practiceDetailState.value.handoffDisplay,
                    actionStatus = BrainyPalPracticeTaskActionStatus(
                        message = BrainyPalPracticeActionFeedback.HELP_SUCCESS_MESSAGE,
                    ),
                )
                refresh()
            }.onFailure { error ->
                if (error is CancellationException) {
                    throw error
                }
                _practiceDetailState.value = _practiceDetailState.value.copy(
                    selectedTaskId = taskId,
                    actionInProgress = false,
                    helpHint = previousHint,
                    actionStatus = BrainyPalPracticeTaskActionStatus(
                        message = "暂时连不上 BrainyPal，可以稍后再试",
                        error = true,
                    ),
                )
            }
        }
    }

    fun createPracticeHandoffCode(taskId: String) {
        viewModelScope.launch {
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
                _practiceDetailState.value = _practiceDetailState.value.copy(
                    selectedTaskId = taskId,
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
                _practiceDetailState.value = previous.copy(
                    selectedTaskId = taskId,
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

    private fun updatePracticeTask(
        taskId: String,
        successMessage: String,
        pendingMessage: String? = null,
        savedAnswer: SavedPracticeAnswer? = null,
        helpItemId: String? = null,
        action: suspend me.rerere.rikkahub.brainypal.shared.BrainyPalChildApi.() -> BrainyPalChildPracticeTaskDetail,
    ) {
        viewModelScope.launch {
            val previousHint = _practiceDetailState.value.helpHint
            _practiceDetailState.value = _practiceDetailState.value.copy(
                selectedTaskId = taskId,
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
                _practiceDetailState.value = BrainyPalPracticeTaskDetailState(
                    selectedTaskId = taskId,
                    detail = UiState.Success(detail),
                    drafts = currentDrafts.replaceFromDetail(detail),
                    helpHint = helpHint,
                    handoffDisplay = _practiceDetailState.value.handoffDisplay,
                    actionStatus = actionStatus,
                )
                refresh()
            }.onFailure { error ->
                if (error is CancellationException) {
                    throw error
                }
                _practiceDetailState.value = _practiceDetailState.value.copy(
                    selectedTaskId = taskId,
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
}

private data class SavedPracticeAnswer(
    val itemId: String,
    val answer: String,
    val evidence: String,
)

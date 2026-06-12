package me.rerere.rikkahub.ui.pages.brainypal

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
import me.rerere.rikkahub.brainypal.BrainyPalChildApiFactory
import me.rerere.rikkahub.brainypal.BrainyPalChildHomeState
import me.rerere.rikkahub.brainypal.BrainyPalChildModePolicy
import me.rerere.rikkahub.brainypal.BrainyPalChildPracticeTaskDetail
import me.rerere.rikkahub.brainypal.BrainyPalPracticeDrafts
import me.rerere.rikkahub.brainypal.BrainyPalRecordPracticeTaskAnswerRequest
import me.rerere.rikkahub.brainypal.BrainyPalRequestPracticeTaskHelpRequest
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
                practiceApi().getPracticeTask(taskId)
            }.onSuccess { detail ->
                _practiceDetailState.value = BrainyPalPracticeTaskDetailState(
                    selectedTaskId = taskId,
                    detail = UiState.Success(detail),
                    drafts = BrainyPalPracticeDrafts().replaceFromDetail(detail),
                )
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
        updatePracticeTask(
            taskId = taskId,
            successMessage = "答案已保存",
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

    fun requestPracticeHelp(taskId: String, itemId: String) {
        updatePracticeTask(
            taskId = taskId,
            successMessage = "提示已显示在题目下方",
            helpItemId = itemId,
        ) {
            requestPracticeTaskHelp(
                taskId = taskId,
                request = BrainyPalRequestPracticeTaskHelpRequest(itemId = itemId),
            )
        }
    }

    fun submitPracticeTask(taskId: String) {
        updatePracticeTask(
            taskId = taskId,
            successMessage = "已提交练习",
        ) {
            submitPracticeTask(taskId = taskId)
        }
    }

    private fun updatePracticeTask(
        taskId: String,
        successMessage: String,
        savedAnswer: SavedPracticeAnswer? = null,
        helpItemId: String? = null,
        action: suspend me.rerere.rikkahub.brainypal.BrainyPalChildApi.() -> BrainyPalChildPracticeTaskDetail,
    ) {
        viewModelScope.launch {
            _practiceDetailState.value = _practiceDetailState.value.copy(
                selectedTaskId = taskId,
                actionInProgress = true,
                actionStatus = null,
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
                val helpHint = detail.helpMessage
                    ?.takeIf { it.isNotBlank() }
                    ?.let { message ->
                        helpItemId?.let { itemId ->
                            BrainyPalPracticeTaskHelpHint(
                                itemId = itemId,
                                message = message,
                            )
                        }
                    }
                    ?: _practiceDetailState.value.helpHint
                _practiceDetailState.value = BrainyPalPracticeTaskDetailState(
                    selectedTaskId = taskId,
                    detail = UiState.Success(detail),
                    drafts = currentDrafts.replaceFromDetail(detail),
                    helpHint = helpHint,
                    actionStatus = BrainyPalPracticeTaskActionStatus(
                        message = message,
                        error = detail.needsMoreEffort,
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
                    actionStatus = BrainyPalPracticeTaskActionStatus(
                        message = "暂时连不上 BrainyPal，可以稍后再试",
                        error = true,
                    ),
                )
            }
        }
    }

    private suspend fun practiceApi(): me.rerere.rikkahub.brainypal.BrainyPalChildApi {
        val settings = settingsStore.settingsFlow
            .filter { !it.init }
            .first()
        val connection = settings.brainyPalChildConnection
        return apiFactory.create(
            BrainyPalChildModePolicy.agentServiceRootUrl(connection),
            connection.apiKey,
        )
    }
}

private data class SavedPracticeAnswer(
    val itemId: String,
    val answer: String,
    val evidence: String,
)

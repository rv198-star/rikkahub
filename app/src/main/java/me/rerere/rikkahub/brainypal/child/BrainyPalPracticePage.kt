package me.rerere.rikkahub.brainypal.child

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.rerere.asr.ASRStatus
import me.rerere.ai.ui.UIMessagePart
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.ArrowRight01
import me.rerere.hugeicons.stroke.Book03
import me.rerere.hugeicons.stroke.BubbleChatQuestion
import me.rerere.hugeicons.stroke.Cancel01
import me.rerere.hugeicons.stroke.Camera01
import me.rerere.hugeicons.stroke.FloppyDisk
import me.rerere.hugeicons.stroke.Image02
import me.rerere.hugeicons.stroke.MessageQuestion
import me.rerere.hugeicons.stroke.Refresh03
import me.rerere.hugeicons.stroke.ServerStack01
import me.rerere.hugeicons.stroke.Tick01
import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.brainypal.child.BrainyPalChildHomeState
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskDetail
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskItem
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskSummary
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildConnectionConfig
import me.rerere.rikkahub.brainypal.child.BrainyPalChildTaskInteraction
import me.rerere.rikkahub.brainypal.child.BrainyPalChildTaskInteractionPlan
import me.rerere.rikkahub.brainypal.child.BrainyPalChildUiText
import me.rerere.rikkahub.brainypal.child.BrainyPalChildWorkbench
import me.rerere.rikkahub.brainypal.shared.BrainyPalDictationCommand
import me.rerere.rikkahub.brainypal.shared.BrainyPalDictationOcrReview
import me.rerere.rikkahub.brainypal.shared.BrainyPalDictationOcrReviewRow
import me.rerere.rikkahub.brainypal.shared.BrainyPalDictationSpeech
import me.rerere.rikkahub.brainypal.shared.BrainyPalDictationSession
import me.rerere.rikkahub.brainypal.shared.BrainyPalDictationSessionState
import me.rerere.rikkahub.brainypal.shared.BrainyPalDictationSessionStatus
import me.rerere.rikkahub.brainypal.shared.BrainyPalDictationOcrSubmission
import me.rerere.rikkahub.brainypal.shared.BrainyPalDictationVoiceCommandMatcher
import me.rerere.rikkahub.brainypal.shared.BrainyPalRecitationSpeech
import me.rerere.rikkahub.brainypal.child.BrainyPalPracticeDrafts
import me.rerere.rikkahub.brainypal.shared.BrainyPalSubmitDictationOcrEvidenceRequest
import me.rerere.rikkahub.data.ai.transformers.OcrTransformer
import me.rerere.rikkahub.ui.components.nav.BackButton
import me.rerere.rikkahub.ui.components.ui.CardGroup
import me.rerere.rikkahub.ui.components.ui.permission.PermissionManager
import me.rerere.rikkahub.ui.components.ui.permission.PermissionCamera
import me.rerere.rikkahub.ui.components.ui.permission.PermissionRecordAudio
import me.rerere.rikkahub.ui.components.ui.permission.rememberPermissionState
import me.rerere.rikkahub.ui.context.LocalASRState
import me.rerere.rikkahub.ui.context.LocalNavController
import me.rerere.rikkahub.ui.context.LocalTTSState
import me.rerere.rikkahub.brainypal.child.theme.BrainyPalChildTheme
import me.rerere.rikkahub.utils.UiState
import me.rerere.rikkahub.utils.plus
import java.io.File
import org.koin.androidx.compose.koinViewModel

@Composable
fun BrainyPalPracticePage(vm: BrainyPalHomeVM = koinViewModel()) {
    val navController = LocalNavController.current
    val state by vm.state.collectAsStateWithLifecycle()
    val practiceDetailState by vm.practiceDetailState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("今日任务") },
                navigationIcon = { BackButton() },
                colors = BrainyPalChildTheme.topAppBarColors(),
                scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        when (val current = state) {
            UiState.Loading,
            UiState.Idle -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is UiState.Error -> {
                BrainyPalPracticeContent(
                    innerPadding = innerPadding,
                    state = null,
                    errorMessage = current.error.message ?: "暂时连不上 BrainyPal，可以稍后重试",
                    practiceDetailState = practiceDetailState,
                    onNavigate = { navController.navigate(it) },
                    onRefresh = vm::refresh,
                    onSelectTask = vm::selectPracticeTask,
                    onCloseTask = vm::closePracticeTask,
                    onUpdateDraft = vm::updatePracticeDraft,
                    onSaveAnswer = vm::savePracticeAnswer,
                    onRequestHelp = vm::requestPracticeHelp,
                    onCreateHandoffCode = vm::createPracticeHandoffCode,
                    onSubmitTask = vm::submitPracticeTask,
                    onSubmitOcrEvidence = vm::submitDictationOcrEvidence,
                    onConfirmOcrEvidence = vm::confirmDictationOcrEvidence,
                )
            }

            is UiState.Success -> {
                BrainyPalPracticeContent(
                    innerPadding = innerPadding,
                    state = current.data,
                    errorMessage = current.data.errorMessage,
                    practiceDetailState = practiceDetailState,
                    onNavigate = { navController.navigate(it) },
                    onRefresh = vm::refresh,
                    onSelectTask = vm::selectPracticeTask,
                    onCloseTask = vm::closePracticeTask,
                    onUpdateDraft = vm::updatePracticeDraft,
                    onSaveAnswer = vm::savePracticeAnswer,
                    onRequestHelp = vm::requestPracticeHelp,
                    onCreateHandoffCode = vm::createPracticeHandoffCode,
                    onSubmitTask = vm::submitPracticeTask,
                    onSubmitOcrEvidence = vm::submitDictationOcrEvidence,
                    onConfirmOcrEvidence = vm::confirmDictationOcrEvidence,
                )
            }
        }
    }
}

@Composable
private fun BrainyPalPracticeContent(
    innerPadding: PaddingValues,
    state: BrainyPalChildHomeState?,
    errorMessage: String?,
    practiceDetailState: BrainyPalPracticeTaskDetailState,
    onNavigate: (Screen) -> Unit,
    onRefresh: () -> Unit,
    onSelectTask: (String) -> Unit,
    onCloseTask: () -> Unit,
    onUpdateDraft: (String, String, String) -> Unit,
    onSaveAnswer: (String, String, String, String) -> Unit,
    onRequestHelp: (String, String, String) -> Unit,
    onCreateHandoffCode: (String) -> Unit,
    onSubmitTask: (String) -> Unit,
    onSubmitOcrEvidence: (String, BrainyPalSubmitDictationOcrEvidenceRequest) -> Unit,
    onConfirmOcrEvidence: (String, String, String, String?) -> Unit,
) {
    val listState = rememberLazyListState()
    val selectedTaskId = practiceDetailState.selectedTaskId

    LaunchedEffect(selectedTaskId) {
        if (selectedTaskId != null && state?.workbench?.configured == true && state.practiceTasks.isNotEmpty()) {
            listState.animateScrollToItem(BrainyPalPracticeDetailPlacement.DETAIL_ITEM_INDEX)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = innerPadding + PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (state == null) {
            val emptyMessage = BrainyPalChildUiText.practiceEmptyMessage(errorMessage)
            item {
                PracticeEmptyCard(
                    headline = emptyMessage.title,
                    supporting = emptyMessage.detail,
                    primaryLabel = "重试",
                    onPrimary = onRefresh,
                )
            }
            return@LazyColumn
        }

        if (!state.workbench.configured) {
            item {
                PracticeEmptyCard(
                    headline = "需要家长配置 BrainyPal",
                    supporting = "配置后这里会显示今天的任务",
                    primaryLabel = "配置连接",
                    onPrimary = { onNavigate(Screen.BrainyPalConnection) },
                )
            }
            return@LazyColumn
        }

        item {
            CardGroup(
                title = { Text("任务概览") },
            ) {
                item(
                    leadingContent = {
                        Icon(
                            imageVector = HugeIcons.Book03,
                            contentDescription = null,
                            tint = BrainyPalChildTheme.cyanAccent,
                        )
                    },
                    headlineContent = { Text(state.workbench.practiceSummary) },
                    supportingContent = { Text("听写、背诵、复习都会从这里开始。") },
                )
            }
        }

        if (state.practiceTasks.isEmpty()) {
            val emptyMessage = BrainyPalChildUiText.practiceEmptyMessage(errorMessage)
            item {
                PracticeEmptyCard(
                    headline = emptyMessage.title,
                    supporting = emptyMessage.detail,
                    primaryLabel = state.workbench.chatAction.label,
                    onPrimary = { onNavigate(state.workbench.chatAction.target) },
                )
            }
        } else {
            item {
                CardGroup(
                    title = { Text("今日任务") },
                ) {
                    state.practiceTasks.forEach { task ->
                        taskItem(
                            task = task,
                            selected = practiceDetailState.selectedTaskId == task.taskId,
                            onClick = { onSelectTask(task.taskId) },
                        )
                    }
                }
            }
        }

        if (practiceDetailState.selectedTaskId != null) {
            val selectedTaskId = practiceDetailState.selectedTaskId
            item {
                PracticeTaskDetailPane(
                    connection = state.connection,
                    detailState = practiceDetailState,
                    onRetry = { onSelectTask(selectedTaskId) },
                    onClose = onCloseTask,
                    onNavigate = onNavigate,
                    onUpdateDraft = onUpdateDraft,
                    onSaveAnswer = onSaveAnswer,
                    onRequestHelp = onRequestHelp,
                    onCreateHandoffCode = onCreateHandoffCode,
                    onSubmitTask = onSubmitTask,
                    onSubmitOcrEvidence = onSubmitOcrEvidence,
                    onConfirmOcrEvidence = onConfirmOcrEvidence,
                )
            }
        }

        item {
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                onClick = onRefresh,
            ) {
                Icon(HugeIcons.Refresh03, null)
                Text(
                    text = "刷新任务",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun PracticeTaskDetailPane(
    connection: BrainyPalChildConnectionConfig,
    detailState: BrainyPalPracticeTaskDetailState,
    onRetry: () -> Unit,
    onClose: () -> Unit,
    onNavigate: (Screen) -> Unit,
    onUpdateDraft: (String, String, String) -> Unit,
    onSaveAnswer: (String, String, String, String) -> Unit,
    onRequestHelp: (String, String, String) -> Unit,
    onCreateHandoffCode: (String) -> Unit,
    onSubmitTask: (String) -> Unit,
    onSubmitOcrEvidence: (String, BrainyPalSubmitDictationOcrEvidenceRequest) -> Unit,
    onConfirmOcrEvidence: (String, String, String, String?) -> Unit,
) {
    when (val detail = detailState.detail) {
        UiState.Idle,
        UiState.Loading -> {
            CardGroup(
                title = { Text("练习详情") },
            ) {
                item(
                    leadingContent = {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    },
                    headlineContent = { Text("正在加载练习") },
                    supportingContent = { Text("请稍等一下") },
                )
            }
        }

        is UiState.Error -> {
            PracticeEmptyCard(
                headline = "练习详情暂时没取到",
                supporting = "暂时连不上 BrainyPal，可以稍后再试",
                primaryLabel = "重试",
                onPrimary = onRetry,
            )
        }

        is UiState.Success -> {
            PracticeTaskDetailContent(
                connection = connection,
                detail = detail.data,
                drafts = detailState.drafts,
                helpHint = detailState.helpHint,
                handoffDisplay = detailState.handoffDisplay,
                actionInProgress = detailState.actionInProgress,
                actionStatus = detailState.actionStatus,
                onClose = onClose,
                onNavigate = onNavigate,
                onUpdateDraft = onUpdateDraft,
                onSaveAnswer = onSaveAnswer,
                onRequestHelp = onRequestHelp,
                onCreateHandoffCode = onCreateHandoffCode,
                onSubmitTask = onSubmitTask,
                onSubmitOcrEvidence = onSubmitOcrEvidence,
                onConfirmOcrEvidence = onConfirmOcrEvidence,
            )
        }
    }
}

@Composable
private fun PracticeTaskDetailContent(
    connection: BrainyPalChildConnectionConfig,
    detail: BrainyPalChildPracticeTaskDetail,
    drafts: BrainyPalPracticeDrafts,
    helpHint: BrainyPalPracticeTaskHelpHint?,
    handoffDisplay: BrainyPalPracticeHandoffDisplay?,
    actionInProgress: Boolean,
    actionStatus: BrainyPalPracticeTaskActionStatus?,
    onClose: () -> Unit,
    onNavigate: (Screen) -> Unit,
    onUpdateDraft: (String, String, String) -> Unit,
    onSaveAnswer: (String, String, String, String) -> Unit,
    onRequestHelp: (String, String, String) -> Unit,
    onCreateHandoffCode: (String) -> Unit,
    onSubmitTask: (String) -> Unit,
    onSubmitOcrEvidence: (String, BrainyPalSubmitDictationOcrEvidenceRequest) -> Unit,
    onConfirmOcrEvidence: (String, String, String, String?) -> Unit,
) {
    val interactionPlan = BrainyPalChildTaskInteraction.plan(detail)
    val isDictation = detail.taskType == "dictation"
    val isRecitation = detail.taskType == "recitation"
    val tts = LocalTTSState.current
    val asr = LocalASRState.current
    val asrState by asr.state.collectAsStateWithLifecycle()
    val asrPermission = rememberPermissionState(PermissionRecordAudio)
    val cameraPermission = rememberPermissionState(PermissionCamera)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    if (isDictation) {
        PermissionManager(permissionState = asrPermission)
        PermissionManager(permissionState = cameraPermission)
    }
    var dictationSession by remember(detail.taskId, detail.items.map { it.itemId }) {
        mutableStateOf(
            BrainyPalDictationSessionState(
                itemIds = detail.items.map { it.itemId },
            )
        )
    }
    var dictationMessage by remember(detail.taskId) {
        mutableStateOf("点“开始听写”，或打开语音控制后说“开始”。")
    }
    var ocrInProgress by remember(detail.taskId) { mutableStateOf(false) }
    var ocrMessage by remember(detail.taskId) {
        mutableStateOf("写完后拍照，BrainyPal 会先识别，再让你确认不确定的地方。")
    }
    var recitationMessage by remember(detail.taskId) {
        mutableStateOf("先听一遍材料，再自己背一小段；卡住了也可以标记出来。")
    }
    var correctionDrafts by remember(detail.taskId, detail.result?.createdAt) {
        mutableStateOf<Map<String, String>>(emptyMap())
    }
    var cameraOutputFile by remember { mutableStateOf<File?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val file = cameraOutputFile
        if (success && file != null) {
            scope.launch {
                submitDictationOcrImage(
                    detail = detail,
                    imageFile = file,
                    onMessage = { ocrMessage = it },
                    onProgress = { ocrInProgress = it },
                    onSubmitOcrEvidence = onSubmitOcrEvidence,
                )
            }
        } else {
            ocrMessage = "没有拍到照片，可以重新拍一次。"
        }
    }
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val file = withContext(Dispatchers.IO) {
                copyUriToDictationOcrCache(context = context, uri = uri)
            }
            submitDictationOcrImage(
                detail = detail,
                imageFile = file,
                onMessage = { ocrMessage = it },
                onProgress = { ocrInProgress = it },
                onSubmitOcrEvidence = onSubmitOcrEvidence,
            )
        }
    }

    fun launchDictationCamera() {
        if (!cameraPermission.allRequiredPermissionsGranted) {
            cameraPermission.requestPermissions()
            return
        }
        val file = newDictationOcrCacheFile(context.cacheDir)
        cameraOutputFile = file
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        cameraLauncher.launch(uri)
    }

    fun playDictationItem(itemId: String) {
        val item = detail.items.firstOrNull { it.itemId == itemId } ?: return
        val index = detail.items.indexOf(item).coerceAtLeast(0)
        tts.setSpeed(1.0f)
        tts.speak(BrainyPalDictationSpeech.build(detail, item, index), flushCalled = true)
    }

    fun applyDictationCommand(command: BrainyPalDictationCommand) {
        val update = BrainyPalDictationSession.reduce(dictationSession, command)
        dictationSession = update.state
        when (command) {
            BrainyPalDictationCommand.PAUSE -> {
                tts.pause()
                dictationMessage = "已暂停。说“继续”或点继续。"
            }

            BrainyPalDictationCommand.RESUME -> {
                update.playbackItemId?.let(::playDictationItem) ?: tts.resume()
                dictationMessage = "继续听写。"
            }

            BrainyPalDictationCommand.DONT_KNOW -> {
                update.playbackItemId?.let(::playDictationItem)
                dictationMessage = if (update.state.isFinished) {
                    "听写播放完成，拍照后再批改。"
                } else {
                    "这条先放一放，继续下一个。"
                }
            }

            BrainyPalDictationCommand.NEXT -> {
                update.playbackItemId?.let(::playDictationItem)
                dictationMessage = if (update.state.isFinished) {
                    "听写播放完成，拍照后再批改。"
                } else {
                    "进入下一条。"
                }
            }

            BrainyPalDictationCommand.START,
            BrainyPalDictationCommand.REPEAT -> {
                update.playbackItemId?.let(::playDictationItem)
                dictationMessage = "正在播放当前听写。"
            }

            BrainyPalDictationCommand.UNKNOWN -> {
                dictationMessage = "我没听清，可以再说一遍或点按钮。"
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CardGroup(
            title = { Text("任务详情") },
        ) {
            item(
                leadingContent = { Icon(HugeIcons.Book03, null) },
                headlineContent = { Text("${interactionPlan.kindLabel} · ${detail.title}") },
                supportingContent = {
                    Text(
                        BrainyPalPracticeDetailCopy.supportingText(
                            detail = detail,
                            interactionPlan = interactionPlan,
                        )
                    )
                },
            )
        }

        PracticeExternalWorkCard(
            detail = detail,
            connection = connection,
            handoffDisplay = handoffDisplay,
            actionInProgress = actionInProgress,
            onCreateHandoffCode = { onCreateHandoffCode(detail.taskId) },
            onOpenPrintable = {
                onNavigate(
                    Screen.WebView(
                        url = BrainyPalPracticeExternalWork.printablePdfUrl(
                            config = connection,
                            taskId = detail.taskId,
                        )
                    )
                )
            },
        )

        if (isDictation) {
            DictationControlCard(
                session = dictationSession,
                message = dictationMessage,
                asrStatus = asrState.status,
                asrAvailable = asrState.isAvailable || asrState.isRecording,
                audioPermissionGranted = asrPermission.allRequiredPermissionsGranted,
                onCommand = ::applyDictationCommand,
                onRequestAudioPermission = { asrPermission.requestPermissions() },
                onStartVoiceControl = {
                    asr.start { transcript ->
                        val command = BrainyPalDictationVoiceCommandMatcher.match(
                            text = transcript,
                            waitingForChild = dictationSession.isWaitingForChild,
                        )
                        applyDictationCommand(command)
                    }
                },
                onStopVoiceControl = asr::stop,
            )
            DictationOcrReviewCard(
                rows = BrainyPalDictationOcrReview.rows(detail),
                message = ocrMessage,
                inProgress = ocrInProgress || actionInProgress,
                onTakePhoto = ::launchDictationCamera,
                onPickImage = { imagePickerLauncher.launch("image/*") },
                onConfirm = { itemId, confirmation, label ->
                    onConfirmOcrEvidence(
                        detail.taskId,
                        itemId,
                        confirmation,
                        "孩子确认：$label",
                    )
                },
            )
        }

        if (isRecitation) {
            val recitationItem = detail.items.firstOrNull()
            RecitationFlowCard(
                detail = detail,
                interactionPlan = interactionPlan,
                draft = recitationItem?.let { drafts.get(it.itemId) },
                message = recitationMessage,
                actionInProgress = actionInProgress,
                onListen = {
                    tts.setSpeed(1.0f)
                    tts.speak(BrainyPalRecitationSpeech.build(detail), flushCalled = true)
                    recitationMessage = "正在读材料。听完以后，可以自己试着背。"
                },
                onRepeat = {
                    tts.setSpeed(1.0f)
                    tts.speak(BrainyPalRecitationSpeech.build(detail), flushCalled = true)
                    recitationMessage = "再听一遍，不着急，抓住意思就好。"
                },
                onMarkStuck = {
                    recitationItem?.let { item ->
                        val draft = drafts.get(item.itemId)
                        val nextEvidence = draft.evidence.ifBlank { "我卡住了：" }
                        onUpdateDraft(item.itemId, draft.answer, nextEvidence)
                    }
                    recitationMessage = "已经标记卡点，写一点哪里不顺就可以。"
                },
                onMarkDone = {
                    recitationItem?.let { item ->
                        val draft = drafts.get(item.itemId)
                        val nextAnswer = draft.answer.ifBlank { "我背完了，可以复盘。" }
                        onUpdateDraft(item.itemId, nextAnswer, draft.evidence)
                    }
                    recitationMessage = "很好，补一句自评后就可以提交复盘。"
                },
                onUpdateDraft = { answer, evidence ->
                    recitationItem?.let { item ->
                        onUpdateDraft(item.itemId, answer, evidence)
                    }
                },
                onSave = { answer, evidence ->
                    recitationItem?.let { item ->
                        onSaveAnswer(detail.taskId, item.itemId, answer, evidence)
                    }
                },
            )
        }

        if (actionStatus != null) {
            PracticeActionStatusCard(actionStatus)
        }

        if (!isRecitation) detail.items.forEachIndexed { index, item ->
            PracticeTaskQuestionCard(
                taskId = detail.taskId,
                index = index + 1,
                item = item,
                draft = drafts.get(item.itemId),
                interactionPlan = interactionPlan,
                helpMessage = helpHint
                    ?.takeIf { it.itemId == item.itemId }
                    ?.message,
                remainingHelp = detail.remainingHelp,
                canEdit = detail.canSubmit,
                actionInProgress = actionInProgress,
                isDictation = isDictation,
                isCurrentDictationItem = dictationSession.isActiveItem(item.itemId),
                onUpdateDraft = onUpdateDraft,
                onSaveAnswer = onSaveAnswer,
                onRequestHelp = onRequestHelp,
            )
        }

        if (detail.result != null) {
            PracticeResultReviewCard(
                rows = BrainyPalPracticeResultReview.rows(detail),
                correctionDrafts = correctionDrafts,
                onCorrectionChange = { itemId, value ->
                    correctionDrafts = correctionDrafts + (itemId to value)
                },
            )
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp),
            enabled = detail.canSubmit && !actionInProgress,
            onClick = { onSubmitTask(detail.taskId) },
        ) {
            Icon(HugeIcons.Tick01, null)
            Text(
                text = if (detail.canSubmit) interactionPlan.submitLabel else "已提交",
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        OutlinedButton(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            enabled = !actionInProgress,
            onClick = onClose,
        ) {
            Icon(HugeIcons.Cancel01, null)
            Text(
                text = "收起详情",
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun PracticeResultReviewCard(
    rows: List<BrainyPalPracticeResultReviewRow>,
    correctionDrafts: Map<String, String>,
    onCorrectionChange: (String, String) -> Unit,
) {
    if (rows.isEmpty()) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "复盘与订正",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            rows.forEachIndexed { index, row ->
                if (index > 0) {
                    androidx.compose.material3.HorizontalDivider()
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${row.title} · ${row.statusLabel}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                    Text(
                        text = row.feedback.ifBlank { row.prompt },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                    row.expectedAnswerLabel?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    }
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = row.originalAnswer,
                        onValueChange = {},
                        readOnly = row.originalAnswerReadOnly,
                        label = { Text("原答案") },
                        minLines = 1,
                        maxLines = 3,
                    )
                    when {
                        row.canCorrectNow -> {
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = correctionDrafts[row.itemId].orEmpty(),
                                onValueChange = { onCorrectionChange(row.itemId, it) },
                                label = { Text("马上订正") },
                                placeholder = { Text(row.correctionPrompt) },
                                minLines = 2,
                                maxLines = 4,
                            )
                        }

                        row.needsParentOrOcrReview -> {
                            Text(
                                text = "这题先确认照片识别，再决定是否订正。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PracticeExternalWorkCard(
    detail: BrainyPalChildPracticeTaskDetail,
    connection: BrainyPalChildConnectionConfig,
    handoffDisplay: BrainyPalPracticeHandoffDisplay?,
    actionInProgress: Boolean,
    onCreateHandoffCode: () -> Unit,
    onOpenPrintable: () -> Unit,
) {
    CardGroup(
        title = { Text("换个方式完成") },
    ) {
        item(
            onClick = if (actionInProgress) null else onCreateHandoffCode,
            leadingContent = {
                Icon(
                    imageVector = HugeIcons.ServerStack01,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            headlineContent = {
                Text(handoffDisplay?.let { "接力码 ${it.code}" } ?: "电脑完成")
            },
            supportingContent = {
                Text(
                    handoffDisplay?.instruction
                        ?: "生成 6 位接力码，在电脑上继续完成 ${detail.title}。"
                )
            },
            trailingContent = {
                Text(if (handoffDisplay == null) "生成" else "已生成")
            },
        )
        item(
            onClick = onOpenPrintable,
            leadingContent = {
                Icon(
                    imageVector = HugeIcons.Image02,
                    contentDescription = null,
                    tint = BrainyPalChildTheme.cyanAccent,
                )
            },
            headlineContent = { Text("打印完成") },
            supportingContent = {
                Text(
                    "打开打印版 PDF，写完后拍照 OCR 批改。${
                        BrainyPalPracticeExternalWork.printablePdfUrl(
                            config = connection,
                            taskId = detail.taskId,
                        ).substringAfter("://")
                    }"
                )
            },
            trailingContent = {
                Icon(HugeIcons.ArrowRight01, contentDescription = null)
            },
        )
    }
}

@Composable
private fun RecitationFlowCard(
    detail: BrainyPalChildPracticeTaskDetail,
    interactionPlan: BrainyPalChildTaskInteractionPlan,
    draft: me.rerere.rikkahub.brainypal.child.BrainyPalPracticeDraft?,
    message: String,
    actionInProgress: Boolean,
    onListen: () -> Unit,
    onRepeat: () -> Unit,
    onMarkStuck: () -> Unit,
    onMarkDone: () -> Unit,
    onUpdateDraft: (String, String) -> Unit,
    onSave: (String, String) -> Unit,
) {
    val steps = detail.taskSpec
        ?.steps
        ?.map { it.title }
        ?.filter { it.isNotBlank() }
        ?.takeIf { it.isNotEmpty() }
        ?: listOf("先听一遍", "自己背一小段", "标记哪里卡住")
    val material = detail.items.joinToString("\n") { it.prompt }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "背诵流程",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            steps.forEachIndexed { index, step ->
                Text(
                    text = "${index + 1}. $step",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "材料",
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = material.ifBlank { detail.title },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                enabled = !actionInProgress,
                onClick = onListen,
            ) {
                Icon(HugeIcons.Book03, null)
                Text(
                    text = "听一遍材料",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    enabled = !actionInProgress,
                    onClick = onRepeat,
                ) {
                    Text("再听一遍")
                }
                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    enabled = !actionInProgress,
                    onClick = onMarkStuck,
                ) {
                    Text("卡住了")
                }
            }
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp),
                enabled = !actionInProgress,
                onClick = onMarkDone,
            ) {
                Icon(HugeIcons.Tick01, null)
                Text(
                    text = "我背完了",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = draft?.answer.orEmpty(),
                onValueChange = { onUpdateDraft(it, draft?.evidence.orEmpty()) },
                enabled = !actionInProgress,
                label = { Text(interactionPlan.answerLabel) },
                minLines = 2,
                maxLines = 4,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = draft?.evidence.orEmpty(),
                onValueChange = { onUpdateDraft(draft?.answer.orEmpty(), it) },
                enabled = !actionInProgress,
                label = { Text(interactionPlan.evidenceLabel) },
                minLines = 2,
                maxLines = 4,
            )
            FilledTonalButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                enabled = !actionInProgress && draft != null,
                onClick = { onSave(draft?.answer.orEmpty(), draft?.evidence.orEmpty()) },
            ) {
                Icon(HugeIcons.FloppyDisk, null)
                Text(
                    text = "保存自评",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun PracticeActionStatusCard(status: BrainyPalPracticeTaskActionStatus) {
    val colors = if (status.error) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        )
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = colors,
    ) {
        Text(
            text = status.message,
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun PracticeTaskQuestionCard(
    taskId: String,
    index: Int,
    item: BrainyPalChildPracticeTaskItem,
    draft: me.rerere.rikkahub.brainypal.child.BrainyPalPracticeDraft,
    interactionPlan: BrainyPalChildTaskInteractionPlan,
    helpMessage: String?,
    remainingHelp: Int,
    canEdit: Boolean,
    actionInProgress: Boolean,
    isDictation: Boolean,
    isCurrentDictationItem: Boolean,
    onUpdateDraft: (String, String, String) -> Unit,
    onSaveAnswer: (String, String, String, String) -> Unit,
    onRequestHelp: (String, String, String) -> Unit,
) {
    val actionsEnabled = canEdit && !actionInProgress

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "第 $index 条",
            color = BrainyPalChildTheme.cyanAccent,
            style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = if (isDictation) {
                    if (isCurrentDictationItem) "正在听这一条，写在纸上" else "听到后写在纸上"
                } else {
                    item.prompt
                },
                style = MaterialTheme.typography.titleMedium,
            )
            if (item.needsMoreEffort) {
                Text(
                    text = "这题需要再写一点自己的尝试",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            helpMessage?.takeIf { it.isNotBlank() }?.let { message ->
                PracticeHelpHintCard(message)
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = draft.answer,
                onValueChange = { onUpdateDraft(item.itemId, it, draft.evidence) },
                enabled = canEdit,
                label = { Text(interactionPlan.answerLabel) },
                minLines = 2,
                maxLines = 5,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = draft.evidence,
                onValueChange = { onUpdateDraft(item.itemId, draft.answer, it) },
                enabled = canEdit,
                label = { Text(interactionPlan.evidenceLabel) },
                minLines = 2,
                maxLines = 5,
            )
            if (!isDictation) interactionPlan.quickActions.forEach { (action, label) ->
                FilledTonalButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp),
                    enabled = actionsEnabled,
                    onClick = { onRequestHelp(taskId, item.itemId, action) },
                ) {
                    Icon(HugeIcons.MessageQuestion, null)
                    Text(
                        text = label,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                enabled = actionsEnabled,
                onClick = { onSaveAnswer(taskId, item.itemId, draft.answer, draft.evidence) },
            ) {
                Icon(HugeIcons.FloppyDisk, null)
                Text(
                    text = "保存答案",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            FilledTonalButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                enabled = actionsEnabled && remainingHelp > 0,
                onClick = { onRequestHelp(taskId, item.itemId, "hint") },
            ) {
                Icon(HugeIcons.BubbleChatQuestion, null)
                Text(
                    text = if (remainingHelp > 0) "提示一下，还剩 $remainingHelp 次" else "提示次数已用完",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun DictationControlCard(
    session: BrainyPalDictationSessionState,
    message: String,
    asrStatus: ASRStatus,
    asrAvailable: Boolean,
    audioPermissionGranted: Boolean,
    onCommand: (BrainyPalDictationCommand) -> Unit,
    onRequestAudioPermission: () -> Unit,
    onStartVoiceControl: () -> Unit,
    onStopVoiceControl: () -> Unit,
) {
    val currentNumber = (session.currentIndex + 1).coerceAtMost(session.itemIds.size)
    val total = session.itemIds.size
    val controlsActive = session.status != BrainyPalDictationSessionStatus.IDLE &&
        session.status != BrainyPalDictationSessionStatus.FINISHED
    val statusText = when (session.status) {
        BrainyPalDictationSessionStatus.IDLE -> "还没开始"
        BrainyPalDictationSessionStatus.WAITING -> "第 $currentNumber / $total 条"
        BrainyPalDictationSessionStatus.PAUSED -> "已暂停"
        BrainyPalDictationSessionStatus.FINISHED -> "已播完"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "听写控制 · $statusText",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            if (session.dontKnowCounts.values.sum() > 0 || session.repeatCounts.values.sum() > 0) {
                Text(
                    text = "过程记录：再听 ${session.repeatCounts.values.sum()} 次，不会 ${session.dontKnowCounts.values.sum()} 次",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                enabled = session.status != BrainyPalDictationSessionStatus.FINISHED && total > 0,
                onClick = {
                    onCommand(
                        if (session.status == BrainyPalDictationSessionStatus.PAUSED) {
                            BrainyPalDictationCommand.RESUME
                        } else {
                            BrainyPalDictationCommand.START
                        }
                    )
                },
            ) {
                Icon(HugeIcons.Book03, null)
                Text(
                    text = if (session.status == BrainyPalDictationSessionStatus.PAUSED) "继续" else "开始听写",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    enabled = controlsActive && session.currentItemId != null,
                    onClick = { onCommand(BrainyPalDictationCommand.REPEAT) },
                ) {
                    Text("再听一次")
                }
                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    enabled = controlsActive && session.currentItemId != null,
                    onClick = { onCommand(BrainyPalDictationCommand.NEXT) },
                ) {
                    Text("下一个")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = controlsActive && session.currentItemId != null,
                    onClick = { onCommand(BrainyPalDictationCommand.DONT_KNOW) },
                ) {
                    Text("不会，先下一个")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = session.status == BrainyPalDictationSessionStatus.WAITING,
                    onClick = { onCommand(BrainyPalDictationCommand.PAUSE) },
                ) {
                    Text("暂停")
                }
            }
            FilledTonalButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp),
                enabled = asrAvailable,
                onClick = {
                    when (asrStatus) {
                        ASRStatus.Listening -> onStopVoiceControl()
                        ASRStatus.Idle,
                        ASRStatus.Error -> {
                            if (audioPermissionGranted) {
                                onStartVoiceControl()
                            } else {
                                onRequestAudioPermission()
                            }
                        }

                        ASRStatus.Connecting,
                        ASRStatus.Stopping -> {}
                    }
                },
            ) {
                Icon(HugeIcons.MessageQuestion, null)
                Text(
                    text = when (asrStatus) {
                        ASRStatus.Listening -> "停止语音控制"
                        ASRStatus.Connecting -> "正在连接语音控制"
                        ASRStatus.Stopping -> "正在停止语音控制"
                        else -> "开启语音控制"
                    },
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            if (!asrAvailable && asrStatus == ASRStatus.Idle) {
                Text(
                    text = "配置 ASR 后，可以说“再听一次”“下一个”“不会”“暂停”“继续”。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}

@Composable
private fun DictationOcrReviewCard(
    rows: List<BrainyPalDictationOcrReviewRow>,
    message: String,
    inProgress: Boolean,
    onTakePhoto: () -> Unit,
    onPickImage: () -> Unit,
    onConfirm: (String, String, String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "拍照 OCR 批改",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    enabled = !inProgress,
                    onClick = onTakePhoto,
                ) {
                    Icon(HugeIcons.Camera01, null)
                    Text(
                        text = "拍照批改",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = !inProgress,
                    onClick = onPickImage,
                ) {
                    Icon(HugeIcons.Image02, null)
                    Text(
                        text = "选照片",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
            if (inProgress) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp))
                    Text("正在处理，请稍等")
                }
            }
            rows.forEach { row ->
                DictationOcrReviewRowCard(
                    row = row,
                    inProgress = inProgress,
                    onConfirm = onConfirm,
                )
            }
        }
    }
}

@Composable
private fun DictationOcrReviewRowCard(
    row: BrainyPalDictationOcrReviewRow,
    inProgress: Boolean,
    onConfirm: (String, String, String) -> Unit,
) {
    var showImagePreview by remember(row.imageRef) { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "${row.title} · ${row.resultLabel}",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = "识别：${row.recognizedText.ifBlank { "空" }}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = listOfNotNull(row.sourceRegionLabel, row.confidenceLabel)
                    .joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
            )
            row.guidanceLabel?.let { guidance ->
                Text(
                    text = guidance,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 40.dp),
                onClick = { showImagePreview = true },
            ) {
                Icon(HugeIcons.Image02, null)
                Text(
                    text = row.previewButtonLabel,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            if (row.requiresManualConfirmation) {
                row.confirmationActions.forEach { action ->
                    OutlinedButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 40.dp),
                        enabled = !inProgress,
                        onClick = { onConfirm(row.itemId, action.confirmation, action.label) },
                    ) {
                        Text(action.label)
                    }
                }
            }
        }
    }
    if (showImagePreview) {
        DictationOcrImagePreviewDialog(row = row) {
            showImagePreview = false
        }
    }
}

@Composable
private fun DictationOcrImagePreviewDialog(
    row: BrainyPalDictationOcrReviewRow,
    onDismissRequest: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            Image(
                painter = rememberAsyncImagePainter(row.previewImageRef),
                contentDescription = row.previewButtonLabel,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
            row.previewBoundingBox?.let { box ->
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .offset(
                                x = maxWidth * box.x,
                                y = maxHeight * box.y,
                            )
                            .width(maxWidth * box.width)
                            .height(maxHeight * box.height)
                            .border(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.error,
                                shape = RoundedCornerShape(6.dp),
                            )
                            .background(
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(6.dp),
                            )
                    )
                }
            }
            Text(
                text = "${row.title} · ${row.sourceRegionLabel}",
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}

private suspend fun submitDictationOcrImage(
    detail: BrainyPalChildPracticeTaskDetail,
    imageFile: File,
    onMessage: (String) -> Unit,
    onProgress: (Boolean) -> Unit,
    onSubmitOcrEvidence: (String, BrainyPalSubmitDictationOcrEvidenceRequest) -> Unit,
) {
    onProgress(true)
    onMessage("正在识别照片内容...")
    runCatching {
        val imageRef = imageFile.toURI().toString()
        val rawOcrText = withContext(Dispatchers.IO) {
            OcrTransformer.performOcr(UIMessagePart.Image(imageRef))
        }
        BrainyPalDictationOcrSubmission.request(
            detail = detail,
            imageRef = imageRef,
            rawOcrText = rawOcrText,
        )
    }.onSuccess { request ->
        onMessage("识别完成，正在生成批改结果...")
        onSubmitOcrEvidence(detail.taskId, request)
    }.onFailure {
        onMessage("照片识别失败，可以换一张更清楚的照片再试。")
    }
    onProgress(false)
}

private fun newDictationOcrCacheFile(cacheDir: File): File {
    return cacheDir.resolve("brainypal_dictation_ocr_${System.currentTimeMillis()}.jpg")
}

private fun copyUriToDictationOcrCache(context: Context, uri: Uri): File {
    val target = newDictationOcrCacheFile(context.cacheDir)
    context.contentResolver.openInputStream(uri).use { input ->
        requireNotNull(input) { "Cannot open image uri: $uri" }
        target.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    return target
}

@Composable
private fun PracticeHelpHintCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = HugeIcons.MessageQuestion,
                contentDescription = null,
                tint = BrainyPalChildTheme.amberText,
                modifier = Modifier.size(22.dp),
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "BrainyPal 提示",
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun PracticeEmptyCard(
    headline: String,
    supporting: String,
    primaryLabel: String,
    onPrimary: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CardGroup(
            title = { Text("今日任务") },
        ) {
            item(
                leadingContent = {
                    Icon(
                        imageVector = HugeIcons.ServerStack01,
                        contentDescription = null,
                        tint = BrainyPalChildTheme.cyanAccent,
                    )
                },
                headlineContent = { Text(headline) },
                supportingContent = { Text(supporting) },
            )
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            onClick = onPrimary,
        ) {
            Text(primaryLabel)
        }
    }
}

private fun me.rerere.rikkahub.ui.components.ui.CardGroupScope.taskItem(
    task: BrainyPalChildPracticeTaskSummary,
    selected: Boolean,
    onClick: () -> Unit,
) {
    item(
        onClick = onClick,
        leadingContent = {
            Icon(
                imageVector = HugeIcons.Book03,
                contentDescription = null,
                tint = if (selected || task.needsMoreEffort) {
                    BrainyPalChildTheme.amberText
                } else {
                    BrainyPalChildTheme.cyanAccent
                },
            )
        },
        headlineContent = { Text(task.title) },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "${task.statusLabel} · ${
                        BrainyPalPracticeTaskCopy.itemCountLabel(task.taskType, task.itemCount)
                    }"
                )
                Text("还能求助 ${task.remainingHelp} 次")
                if (selected) {
                    Text(
                        text = "已展开详情",
                        color = BrainyPalChildTheme.amberText,
                    )
                }
                if (task.needsMoreEffort) {
                    Text(
                        text = "这项需要再认真一点",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = BrainyPalChildWorkbench.taskActionLabel(task),
                    color = if (selected) BrainyPalChildTheme.amberText else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
                Icon(
                    imageVector = HugeIcons.ArrowRight01,
                    contentDescription = null,
                    tint = if (selected) BrainyPalChildTheme.amberText else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(18.dp),
                )
            }
        },
    )
}

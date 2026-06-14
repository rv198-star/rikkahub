package me.rerere.rikkahub.brainypal.parent

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.rememberAsyncImagePainter
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.Book03
import me.rerere.hugeicons.stroke.Camera01
import me.rerere.hugeicons.stroke.Image02
import me.rerere.hugeicons.stroke.Refresh03
import me.rerere.hugeicons.stroke.Lock
import me.rerere.hugeicons.stroke.ServerStack01
import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildConnectionConfig
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildModePolicy
import me.rerere.rikkahub.brainypal.child.BrainyPalChildUiText
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskDetail
import me.rerere.rikkahub.brainypal.shared.BrainyPalConfirmDictationOcrEvidenceRequest
import me.rerere.rikkahub.brainypal.shared.BrainyPalDueWrongQuestionReviewItem
import me.rerere.rikkahub.brainypal.shared.BrainyPalManagementPin
import me.rerere.rikkahub.brainypal.shared.BrainyPalParentApi
import me.rerere.rikkahub.brainypal.shared.BrainyPalParentApiFactory
import me.rerere.rikkahub.brainypal.shared.BrainyPalParentMaterial
import me.rerere.rikkahub.brainypal.shared.BrainyPalParentMaterialComposer
import me.rerere.rikkahub.brainypal.shared.BrainyPalParentTaskComposer
import me.rerere.rikkahub.brainypal.shared.BrainyPalParentTaskSummary
import me.rerere.rikkahub.brainypal.shared.BrainyPalParentTaskWorkbenchResponse
import me.rerere.rikkahub.brainypal.shared.BrainyPalParentWorkbench
import me.rerere.rikkahub.brainypal.shared.BrainyPalPinAttemptGate
import me.rerere.rikkahub.data.datastore.Settings
import me.rerere.rikkahub.ui.components.nav.BackButton
import me.rerere.rikkahub.ui.context.LocalNavController
import me.rerere.rikkahub.ui.pages.setting.SettingVM
import me.rerere.rikkahub.brainypal.child.theme.BrainyPalChildTheme
import me.rerere.rikkahub.utils.plus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import kotlin.uuid.Uuid

@Composable
fun BrainyPalConnectionPage(
    vm: SettingVM = koinViewModel(),
    parentApiFactory: BrainyPalParentApiFactory = koinInject(),
) {
    val navController = LocalNavController.current
    val settings by vm.settings.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val savedPin = settings.brainyPalManagementPin
    val gate = remember(savedPin?.hash) { BrainyPalPinAttemptGate() }

    var unlocked by remember(savedPin?.hash) { mutableStateOf(savedPin == null) }
    var pinCandidate by remember(savedPin?.hash) { mutableStateOf("") }
    var newPin by remember(savedPin?.hash) { mutableStateOf("") }
    var baseUrl by remember(settings.brainyPalChildConnection.baseUrl) {
        mutableStateOf(settings.brainyPalChildConnection.baseUrl)
    }
    var apiKey by remember(settings.brainyPalChildConnection.apiKey) {
        mutableStateOf(settings.brainyPalChildConnection.apiKey)
    }
    var message by remember { mutableStateOf<String?>(null) }
    var parentMessage by remember { mutableStateOf<String?>(null) }
    var parentBusy by remember { mutableStateOf(false) }
    var dictationTitle by remember { mutableStateOf("今日听写") }
    var dictationEntries by remember { mutableStateOf("") }
    var materialTitle by remember { mutableStateOf("") }
    var materialSubject by remember { mutableStateOf("语文") }
    var materialRawText by remember { mutableStateOf("") }
    var draftMaterials by remember { mutableStateOf<List<BrainyPalParentMaterial>>(emptyList()) }
    var confirmedMaterials by remember { mutableStateOf<List<BrainyPalParentMaterial>>(emptyList()) }
    var parentWorkbench by remember {
        mutableStateOf(BrainyPalParentWorkbench.from(BrainyPalParentTaskWorkbenchResponse()))
    }
    var wrongQuestionTitle by remember { mutableStateOf("今日错题练习") }
    var dueReviews by remember { mutableStateOf<List<BrainyPalDueWrongQuestionReviewItem>>(emptyList()) }
    var parentTasks by remember { mutableStateOf<List<BrainyPalChildPracticeTaskDetail>>(emptyList()) }
    var showConnectionSettings by remember {
        mutableStateOf(!settings.brainyPalChildConnection.isConfigured())
    }
    var activeParentSection by remember { mutableStateOf("supply") }
    var activeSupplyEntryId by remember { mutableStateOf("paste_text") }
    var previewOcrCard by remember { mutableStateOf<BrainyPalParentOcrEvidenceCard?>(null) }

    fun updateWorkbench(
        drafts: List<BrainyPalParentMaterial> = draftMaterials,
        confirmed: List<BrainyPalParentMaterial> = confirmedMaterials,
        tasks: List<BrainyPalChildPracticeTaskDetail> = parentTasks,
    ) {
        parentWorkbench = BrainyPalParentWorkbench.from(
            BrainyPalParentTaskWorkbenchResponse(
                draftMaterials = drafts,
                confirmedMaterials = confirmed,
                recentTasks = tasks,
            )
        )
    }

    fun runParentAction(
        successMessage: String,
        action: suspend (BrainyPalParentApi) -> List<BrainyPalChildPracticeTaskDetail>?,
    ) {
        val connection = settings.brainyPalChildConnection
        if (!connection.isConfigured()) {
            parentMessage = "请先保存 BrainyPal 服务连接"
            return
        }
        scope.launch {
            parentBusy = true
            parentMessage = "正在连接 BrainyPal..."
            try {
                val api = parentApiFactory.create(
                    BrainyPalChildModePolicy.agentServiceRootUrl(connection),
                    connection.apiKey,
                )
                action(api)?.let { parentTasks = it }
                parentMessage = successMessage
            } catch (error: Throwable) {
                if (error is CancellationException) {
                    throw error
                }
                parentMessage = error.message ?: "暂时连不上 BrainyPal"
            } finally {
                parentBusy = false
            }
        }
    }

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("父母工作台") },
                navigationIcon = { BackButton() },
                colors = BrainyPalChildTheme.topAppBarColors(),
                scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding + PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (!unlocked && savedPin != null) {
                item {
                    Card {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                imageVector = HugeIcons.Lock,
                                contentDescription = null,
                                tint = BrainyPalChildTheme.amberText,
                            )
                            Text("输入家长 PIN", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "这里保护家长工作台、作业下发和孩子任务状态。",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            OutlinedTextField(
                                value = pinCandidate,
                                onValueChange = { pinCandidate = it },
                                label = { Text("家长 PIN") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                modifier = Modifier.fillMaxWidth(),
                            )
                            message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                            Button(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp),
                                onClick = {
                                    when {
                                        gate.isCoolingDown() -> {
                                            message = "PIN 尝试过多，请稍后再试"
                                        }

                                        gate.verify(savedPin, pinCandidate) -> {
                                            unlocked = true
                                            message = null
                                        }

                                        else -> {
                                            message = "PIN 不正确"
                                        }
                                    }
                                }
                            ) {
                                Text("解锁")
                            }
                        }
                    }
                }
            } else {
                val configured = settings.brainyPalChildConnection.isConfigured()
                val supplyEntries = BrainyPalParentWorkbenchUi.supplyEntries(configured)
                val summaryChips = BrainyPalParentWorkbenchUi.summaryChips(
                    draftMaterials = draftMaterials,
                    tasks = parentTasks,
                )
                item {
                    ParentWorkbenchOverviewCard(
                        configured = configured,
                        busy = parentBusy,
                        workbench = parentWorkbench,
                        summaryChips = summaryChips,
                        onChat = {
                            navController.navigate(
                                Screen.Chat(
                                    id = Uuid.random().toString(),
                                    text = "我想给孩子布置作业：",
                                )
                            )
                        },
                        onStructuredImport = {
                            activeParentSection = "supply"
                            activeSupplyEntryId = "paste_text"
                            scope.launch { listState.animateScrollToItem(3) }
                        },
                        onRefresh = {
                            runParentAction("已刷新父母工作台") { api ->
                                val response = api.getTaskWorkbench()
                                draftMaterials = response.draftMaterials
                                confirmedMaterials = response.confirmedMaterials
                                parentTasks = response.recentTasks
                                parentWorkbench = BrainyPalParentWorkbench.from(response)
                                response.recentTasks
                            }
                        },
                    )
                }
                if (showConnectionSettings || !configured) {
                    item {
                        ParentConnectionSettingsCard(
                            savedPin = savedPin,
                            baseUrl = baseUrl,
                            apiKey = apiKey,
                            newPin = newPin,
                            message = message,
                            onBaseUrlChange = { baseUrl = it },
                            onApiKeyChange = { apiKey = it },
                            onNewPinChange = { newPin = it },
                            onSave = {
                                val config = BrainyPalChildConnectionConfig(
                                    baseUrl = baseUrl.trim(),
                                    apiKey = apiKey,
                                )
                                val pin = savedPin ?: newPin
                                    .takeIf { it.length >= 4 }
                                    ?.let(BrainyPalChildModePolicy::createManagementPin)

                                if (!config.isConfigured()) {
                                    message = "请填写 Base URL 和 API Key"
                                    return@ParentConnectionSettingsCard
                                }
                                if (pin == null) {
                                    message = "请设置至少 4 位管理 PIN"
                                    return@ParentConnectionSettingsCard
                                }

                                vm.updateSettings(settings.withBrainyPalConnection(config, pin))
                                message = "已保存"
                                showConnectionSettings = false
                            },
                        )
                    }
                } else {
                    item {
                        ParentConnectionStatusCard(
                            configured = configured,
                            connection = settings.brainyPalChildConnection,
                            showSettings = showConnectionSettings,
                            onSettings = { showConnectionSettings = true },
                            onLock = if (savedPin != null) {
                                {
                                    unlocked = false
                                    pinCandidate = ""
                                    message = null
                                }
                            } else {
                                null
                            },
                        )
                    }
                }
                item {
                    ParentSectionSwitcher(
                        activeSection = activeParentSection,
                        onSectionSelected = { activeParentSection = it },
                    )
                }
                when (activeParentSection) {
                    "review" -> {
                        item {
                            ParentPendingReviewCard(
                                busy = parentBusy,
                                draftMaterials = draftMaterials,
                                tasks = parentTasks,
                                onConfirm = { material ->
                                    runParentAction("已确认材料：${material.title}") { api ->
                                        val confirmed = api.confirmMaterial(material.materialId)
                                        draftMaterials = draftMaterials.filterNot {
                                            it.materialId == confirmed.materialId
                                        }
                                        confirmedMaterials = listOf(confirmed) + confirmedMaterials
                                            .filterNot { it.materialId == confirmed.materialId }
                                        updateWorkbench(drafts = draftMaterials, confirmed = confirmedMaterials)
                                        null
                                    }
                                },
                                onConfirmAndDispatch = { material ->
                                    runParentAction("已确认并下发：${material.title}") { api ->
                                        val confirmed = api.confirmMaterial(material.materialId)
                                        val task = api.createTaskFromMaterial(
                                            confirmed.materialId,
                                            BrainyPalParentMaterialComposer.taskRequest(title = confirmed.title),
                                        )
                                        draftMaterials = draftMaterials.filterNot {
                                            it.materialId == confirmed.materialId
                                        }
                                        confirmedMaterials = listOf(confirmed) + confirmedMaterials
                                            .filterNot { it.materialId == confirmed.materialId }
                                        val tasks = listOf(task) + parentTasks.filterNot { it.taskId == task.taskId }
                                        parentTasks = tasks
                                        updateWorkbench(
                                            drafts = draftMaterials,
                                            confirmed = confirmedMaterials,
                                            tasks = tasks,
                                        )
                                        tasks
                                    }
                                },
                                onPreviewOcr = { previewOcrCard = it },
                                onConfirmOcr = { taskId, itemId, confirmation, label ->
                                    runParentAction("已确认：$label") { api ->
                                        val updated = api.confirmDictationOcrEvidence(
                                            taskId = taskId,
                                            itemId = itemId,
                                            request = BrainyPalConfirmDictationOcrEvidenceRequest(
                                                confirmation = confirmation,
                                                note = "家长确认：$label",
                                            ),
                                        )
                                        val tasks = parentTasks.map {
                                            if (it.taskId == updated.taskId) updated else it
                                        }
                                        updateWorkbench(tasks = tasks)
                                        tasks
                                    }
                                },
                            )
                        }
                    }

                    "status" -> {
                        item {
                            ParentTaskStatusCard(
                                busy = parentBusy,
                                tasks = parentTasks,
                                onRefresh = {
                                    runParentAction("已刷新任务状态") { api ->
                                        val tasks = api.listPracticeTasks().items
                                        updateWorkbench(tasks = tasks)
                                        tasks
                                    }
                                },
                                onPreviewOcr = { previewOcrCard = it },
                                onConfirmOcr = { taskId, itemId, confirmation, label ->
                                    runParentAction("已确认：$label") { api ->
                                        val updated = api.confirmDictationOcrEvidence(
                                            taskId = taskId,
                                            itemId = itemId,
                                            request = BrainyPalConfirmDictationOcrEvidenceRequest(
                                                confirmation = confirmation,
                                                note = "家长确认：$label",
                                            ),
                                        )
                                        val tasks = parentTasks.map {
                                            if (it.taskId == updated.taskId) updated else it
                                        }
                                        updateWorkbench(tasks = tasks)
                                        tasks
                                    }
                                },
                            )
                        }
                    }

                    "strategy" -> {
                        item {
                            ParentStrategyCard(
                                configured = configured,
                                onOpenSettings = { showConnectionSettings = true },
                            )
                        }
                    }

                    else -> {
                        item {
                            ParentSupplyEntryList(
                                entries = supplyEntries,
                                activeEntryId = activeSupplyEntryId,
                                onEntrySelected = { activeSupplyEntryId = it },
                            )
                        }
                        when (activeSupplyEntryId) {
                            "photo_scan" -> {
                                item {
                                    ParentComingSoonSourceCard(
                                        title = "拍照扫描导入",
                                        body = "后续会把作业页照片转成可确认材料，并保留原图区域证据。当前先走粘贴材料和错题复习。",
                                    )
                                }
                            }

                            "ai_material_search" -> {
                                item {
                                    ParentComingSoonSourceCard(
                                        title = "问 AI 找材料",
                                        body = "这个入口会支持按年级、课文、单元联网找候选材料，家长确认后再生成任务。",
                                    )
                                }
                            }
                        }
                item {
                    ParentMaterialImportCard(
                        configured = configured,
                        busy = parentBusy,
                        title = materialTitle,
                        subject = materialSubject,
                        rawText = materialRawText,
                        draftMaterials = draftMaterials,
                        onTitleChange = { materialTitle = it },
                        onSubjectChange = { materialSubject = it },
                        onRawTextChange = { materialRawText = it },
                        onImport = {
                            val request = BrainyPalParentMaterialComposer.importTextRequest(
                                title = materialTitle,
                                subject = materialSubject,
                                rawText = materialRawText,
                            )
                            if (request == null) {
                                parentMessage = "请先粘贴或输入作业材料"
                                return@ParentMaterialImportCard
                            }
                            runParentAction("已生成材料候选，请确认后下发") { api ->
                                val material = api.importTextMaterial(request)
                                draftMaterials = listOf(material) + draftMaterials
                                    .filterNot { it.materialId == material.materialId }
                                updateWorkbench(drafts = draftMaterials)
                                null
                            }
                        },
                        onConfirm = { material ->
                            runParentAction("已确认材料：${material.title}") { api ->
                                val confirmed = api.confirmMaterial(material.materialId)
                                draftMaterials = draftMaterials.filterNot {
                                    it.materialId == confirmed.materialId
                                }
                                confirmedMaterials = listOf(confirmed) + confirmedMaterials
                                    .filterNot { it.materialId == confirmed.materialId }
                                updateWorkbench(drafts = draftMaterials, confirmed = confirmedMaterials)
                                null
                            }
                        },
                        onConfirmAndDispatch = { material ->
                            runParentAction("已确认并下发：${material.title}") { api ->
                                val confirmed = api.confirmMaterial(material.materialId)
                                val task = api.createTaskFromMaterial(
                                    confirmed.materialId,
                                    BrainyPalParentMaterialComposer.taskRequest(title = confirmed.title),
                                )
                                draftMaterials = draftMaterials.filterNot {
                                    it.materialId == confirmed.materialId
                                }
                                confirmedMaterials = listOf(confirmed) + confirmedMaterials
                                    .filterNot { it.materialId == confirmed.materialId }
                                val tasks = listOf(task) + parentTasks.filterNot { it.taskId == task.taskId }
                                parentTasks = tasks
                                updateWorkbench(
                                    drafts = draftMaterials,
                                    confirmed = confirmedMaterials,
                                    tasks = tasks,
                                )
                                tasks
                            }
                        },
                    )
                }
                item {
                    ParentTaskSupplyCard(
                        configured = configured,
                        busy = parentBusy,
                        message = parentMessage,
                        dictationTitle = dictationTitle,
                        dictationEntries = dictationEntries,
                        onDictationTitleChange = { dictationTitle = it },
                        onDictationEntriesChange = { dictationEntries = it },
                        onCreateDictation = {
                            val request = BrainyPalParentTaskComposer.dictationRequest(
                                title = dictationTitle,
                                rawEntries = dictationEntries,
                                helpLimit = 3,
                            )
                            if (request == null) {
                                parentMessage = "请先输入要听写的字词或句子"
                                return@ParentTaskSupplyCard
                            }
                            runParentAction("已下发听写任务，孩子端刷新后可见") { api ->
                                val task = api.createDictationPracticeTask(request)
                                val tasks = listOf(task) + parentTasks.filterNot { it.taskId == task.taskId }
                                updateWorkbench(tasks = tasks)
                                tasks
                            }
                        },
                    )
                }
                item {
                    ParentWrongQuestionSupplyCard(
                        busy = parentBusy,
                        reviews = dueReviews,
                        title = wrongQuestionTitle,
                        onTitleChange = { wrongQuestionTitle = it },
                        onRefresh = {
                            runParentAction("已刷新待复习错题") { api ->
                                dueReviews = api.listDueWrongQuestionReviews(limit = 6).items
                                null
                            }
                        },
                        onCreateFromReviews = { reviews ->
                            val request = BrainyPalParentTaskComposer.wrongQuestionRequest(
                                reviews = reviews,
                                title = wrongQuestionTitle,
                                helpLimit = 3,
                            )
                            if (request == null) {
                                parentMessage = "暂无可下发的错题"
                                return@ParentWrongQuestionSupplyCard
                            }
                            runParentAction("已创建错题练习，孩子端刷新后可见") { api ->
                                val task = api.createPracticeTaskFromWrongQuestions(request)
                                val tasks = listOf(task) + parentTasks.filterNot { it.taskId == task.taskId }
                                updateWorkbench(tasks = tasks)
                                tasks
                            }
                        },
                    )
                }
                    }
                }
            }
        }
    }
    previewOcrCard?.let { card ->
        ParentOcrImagePreviewDialog(card = card) {
            previewOcrCard = null
        }
    }
}

@Composable
private fun ParentWorkbenchOverviewCard(
    configured: Boolean,
    busy: Boolean,
    workbench: BrainyPalParentWorkbench?,
    summaryChips: List<BrainyPalParentSummaryChip>,
    onChat: () -> Unit,
    onStructuredImport: () -> Unit,
    onRefresh: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = HugeIcons.ServerStack01,
                    contentDescription = null,
                    tint = BrainyPalChildTheme.cyanAccent,
                )
                Text("父母工作台", style = MaterialTheme.typography.titleMedium)
            }
            Text("今天要给孩子准备什么？", style = MaterialTheme.typography.titleMedium)
            Text(
                text = listOfNotNull(workbench?.materialSummary, workbench?.taskSummary)
                    .joinToString(" · ")
                    .ifBlank { "刷新后查看待确认材料、OCR 证据和已下发任务" },
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                summaryChips.forEach { chip ->
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(chip.count.toString(), style = MaterialTheme.typography.titleMedium)
                            Text(chip.label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            workbench?.let {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onStructuredImport,
                    ) {
                        Text(it.primaryEntryLabel)
                    }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onChat,
                    ) {
                        Text(it.secondaryChatLabel)
                    }
                }
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                enabled = configured && !busy,
                onClick = onRefresh,
            ) {
                Icon(HugeIcons.Refresh03, null)
                Text(
                    text = "刷新工作台",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun ParentConnectionStatusCard(
    configured: Boolean,
    connection: BrainyPalChildConnectionConfig,
    showSettings: Boolean,
    onSettings: () -> Unit,
    onLock: (() -> Unit)?,
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = HugeIcons.ServerStack01,
                        contentDescription = null,
                        tint = if (configured) BrainyPalChildTheme.cyanAccent else MaterialTheme.colorScheme.error,
                    )
                    Column {
                        Text(
                            text = if (configured) "BrainyPal 已连接" else "需要连接 BrainyPal",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = BrainyPalChildUiText.connectionStatus(connection).detail,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                TextButton(onClick = onSettings) {
                    Text(if (showSettings) "收起" else "设置")
                }
            }
            onLock?.let {
                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 40.dp),
                    onClick = it,
                ) {
                    Icon(HugeIcons.Lock, null)
                    Text(
                        text = "锁定家长工作台",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ParentConnectionSettingsCard(
    savedPin: BrainyPalManagementPin?,
    baseUrl: String,
    apiKey: String,
    newPin: String,
    message: String?,
    onBaseUrlChange: (String) -> Unit,
    onApiKeyChange: (String) -> Unit,
    onNewPinChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = HugeIcons.ServerStack01,
                    contentDescription = null,
                    tint = BrainyPalChildTheme.cyanAccent,
                )
                Text("BrainyPal 服务连接", style = MaterialTheme.typography.titleMedium)
            }
            Text(
                text = "这里由家长管理服务地址、API Key 和安全 PIN。",
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedTextField(
                value = baseUrl,
                onValueChange = onBaseUrlChange,
                label = { Text("Base URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = apiKey,
                onValueChange = onApiKeyChange,
                label = { Text("API Key") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            if (savedPin == null) {
                OutlinedTextField(
                    value = newPin,
                    onValueChange = onNewPinChange,
                    label = { Text("设置管理 PIN") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Text(
                text = "当前连接：${BrainyPalChildUiText.connectionStatus(currentDraftConnection(baseUrl, apiKey)).detail}",
                style = MaterialTheme.typography.bodySmall,
            )
            message?.let {
                Text(
                    text = it,
                    color = if (it == "已保存") {
                        BrainyPalChildTheme.cyanAccent
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                onClick = onSave,
            ) {
                Text("保存连接")
            }
        }
    }
}

@Composable
private fun ParentSectionSwitcher(
    activeSection: String,
    onSectionSelected: (String) -> Unit,
) {
    val sections = listOf(
        "supply" to "供给",
        "review" to "待确认",
        "status" to "状态",
        "strategy" to "策略",
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        sections.forEach { (id, label) ->
            val selected = id == activeSection
            if (selected) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { onSectionSelected(id) },
                ) {
                    Text(label)
                }
            } else {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { onSectionSelected(id) },
                ) {
                    Text(label)
                }
            }
        }
    }
}

@Composable
private fun ParentSupplyEntryList(
    entries: List<BrainyPalParentSupplyEntry>,
    activeEntryId: String,
    onEntrySelected: (String) -> Unit,
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("导入与下发入口", style = MaterialTheme.typography.titleMedium)
            entries.forEach { entry ->
                val selected = entry.id == activeEntryId
                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                    onClick = { onEntrySelected(entry.id) },
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(entry.label, style = MaterialTheme.typography.labelLarge)
                        Text(entry.supportingText, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        text = if (selected) "当前" else entry.statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (entry.enabled) BrainyPalChildTheme.cyanAccent else BrainyPalChildTheme.amberText,
                    )
                }
            }
        }
    }
}

@Composable
private fun ParentComingSoonSourceCard(
    title: String,
    body: String,
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium)
            Text("待接入", style = MaterialTheme.typography.labelLarge, color = BrainyPalChildTheme.amberText)
        }
    }
}

@Composable
private fun ParentPendingReviewCard(
    busy: Boolean,
    draftMaterials: List<BrainyPalParentMaterial>,
    tasks: List<BrainyPalChildPracticeTaskDetail>,
    onConfirm: (BrainyPalParentMaterial) -> Unit,
    onConfirmAndDispatch: (BrainyPalParentMaterial) -> Unit,
    onPreviewOcr: (BrainyPalParentOcrEvidenceCard) -> Unit,
    onConfirmOcr: (taskId: String, itemId: String, confirmation: String, label: String) -> Unit,
) {
    val ocrCards = tasks.flatMap { task ->
        BrainyPalParentWorkbenchUi.ocrEvidenceCards(task).filter { it.requiresManualConfirmation }
    }
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = HugeIcons.Camera01,
                    contentDescription = null,
                    tint = BrainyPalChildTheme.amberText,
                )
                Text("待确认队列", style = MaterialTheme.typography.titleMedium)
            }
            if (draftMaterials.isEmpty() && ocrCards.isEmpty()) {
                Text(
                    text = "当前没有待确认材料或 OCR 结果。刷新工作台后会在这里集中处理。",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            draftMaterials.forEachIndexed { index, material ->
                if (index > 0) HorizontalDivider()
                ParentMaterialCandidateRow(
                    material = material,
                    busy = busy,
                    onConfirm = { onConfirm(material) },
                    onConfirmAndDispatch = { onConfirmAndDispatch(material) },
                )
            }
            if (draftMaterials.isNotEmpty() && ocrCards.isNotEmpty()) {
                HorizontalDivider()
            }
            ocrCards.forEachIndexed { index, card ->
                if (index > 0) HorizontalDivider()
                ParentOcrEvidenceReviewCard(
                    card = card,
                    busy = busy,
                    onPreview = { onPreviewOcr(card) },
                    onConfirm = onConfirmOcr,
                )
            }
        }
    }
}

@Composable
private fun ParentStrategyCard(
    configured: Boolean,
    onOpenSettings: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("孩子引导策略", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "这里会承接家长对 AI 的长期要求：求助边界、鼓励方式、复盘重点和成就系统方向。当前先保持轻量入口，避免打断作业供给主流程。",
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                enabled = configured,
                onClick = onOpenSettings,
            ) {
                Text("检查服务连接")
            }
        }
    }
}

@Composable
private fun ParentMaterialImportCard(
    configured: Boolean,
    busy: Boolean,
    title: String,
    subject: String,
    rawText: String,
    draftMaterials: List<BrainyPalParentMaterial>,
    onTitleChange: (String) -> Unit,
    onSubjectChange: (String) -> Unit,
    onRawTextChange: (String) -> Unit,
    onImport: () -> Unit,
    onConfirm: (BrainyPalParentMaterial) -> Unit,
    onConfirmAndDispatch: (BrainyPalParentMaterial) -> Unit,
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = HugeIcons.Book03,
                    contentDescription = null,
                    tint = BrainyPalChildTheme.amberText,
                )
                Text("导入作业材料", style = MaterialTheme.typography.titleMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("材料标题") },
                    singleLine = true,
                    modifier = Modifier.weight(1.4f),
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = onSubjectChange,
                    label = { Text("科目") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }
            OutlinedTextField(
                value = rawText,
                onValueChange = onRawTextChange,
                label = { Text("粘贴/手输材料") },
                placeholder = { Text("听写词语：观察、勇敢\n练习题：1. 计算 3/4 + 5/8。") },
                minLines = 5,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                enabled = configured && !busy,
                onClick = onImport,
            ) {
                Text("生成候选材料")
            }
            if (draftMaterials.isEmpty()) {
                Text(
                    text = "候选材料会先停在这里，确认后才会下发给孩子。",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                draftMaterials.take(5).forEachIndexed { index, material ->
                    if (index > 0) HorizontalDivider()
                    ParentMaterialCandidateRow(
                        material = material,
                        busy = busy,
                        onConfirm = { onConfirm(material) },
                        onConfirmAndDispatch = { onConfirmAndDispatch(material) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ParentMaterialCandidateRow(
    material: BrainyPalParentMaterial,
    busy: Boolean,
    onConfirm: () -> Unit,
    onConfirmAndDispatch: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "${material.typeLabel} · ${material.statusLabel}",
            style = MaterialTheme.typography.labelLarge,
            color = BrainyPalChildTheme.amberText,
        )
        Text(material.title, style = MaterialTheme.typography.titleSmall)
        Text(
            text = material.previewText.take(120),
            style = MaterialTheme.typography.bodySmall,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                enabled = !busy,
                onClick = onConfirm,
            ) {
                Text("确认")
            }
            Button(
                modifier = Modifier.weight(1f),
                enabled = !busy,
                onClick = onConfirmAndDispatch,
            ) {
                Text("确认并下发")
            }
        }
    }
}

@Composable
private fun ParentTaskSupplyCard(
    configured: Boolean,
    busy: Boolean,
    message: String?,
    dictationTitle: String,
    dictationEntries: String,
    onDictationTitleChange: (String) -> Unit,
    onDictationEntriesChange: (String) -> Unit,
    onCreateDictation: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = HugeIcons.Book03,
                    contentDescription = null,
                    tint = BrainyPalChildTheme.cyanAccent,
                )
                Text("下发今日听写", style = MaterialTheme.typography.titleMedium)
            }
            Text(
                text = "输入字词、单词或短句，孩子端会按听写流程播放并隐藏答案。",
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedTextField(
                value = dictationTitle,
                onValueChange = onDictationTitleChange,
                label = { Text("任务标题") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = dictationEntries,
                onValueChange = onDictationEntriesChange,
                label = { Text("听写内容") },
                placeholder = { Text("apple\nbanana\n观察、锋利") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )
            ParentBusyMessage(busy = busy, message = message)
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                enabled = configured && !busy,
                onClick = onCreateDictation,
            ) {
                Text("下发听写任务")
            }
        }
    }
}

@Composable
private fun ParentWrongQuestionSupplyCard(
    busy: Boolean,
    reviews: List<BrainyPalDueWrongQuestionReviewItem>,
    title: String,
    onTitleChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onCreateFromReviews: (List<BrainyPalDueWrongQuestionReviewItem>) -> Unit,
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = HugeIcons.Refresh03,
                    contentDescription = null,
                    tint = BrainyPalChildTheme.amberText,
                )
                Text("错题复习供给", style = MaterialTheme.typography.titleMedium)
            }
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("练习标题") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    enabled = !busy,
                    onClick = onRefresh,
                ) {
                    Text("刷新待复习")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    enabled = !busy && reviews.isNotEmpty(),
                    onClick = { onCreateFromReviews(reviews) },
                ) {
                    Text("全部下发")
                }
            }
            if (reviews.isEmpty()) {
                Text(
                    text = "暂无待复习错题，刷新后会显示已到期项目。",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                reviews.forEachIndexed { index, review ->
                    if (index > 0) HorizontalDivider()
                    ParentDueReviewRow(
                        review = review,
                        busy = busy,
                        onCreate = { onCreateFromReviews(listOf(review)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ParentDueReviewRow(
    review: BrainyPalDueWrongQuestionReviewItem,
    busy: Boolean,
    onCreate: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "${review.subject ?: "错题"} · ${review.dueStatusLabel}",
            style = MaterialTheme.typography.labelLarge,
            color = BrainyPalChildTheme.amberText,
        )
        Text(review.questionText, style = MaterialTheme.typography.bodyMedium)
        if (review.parentSummary.isNotBlank()) {
            Text(review.parentSummary, style = MaterialTheme.typography.bodySmall)
        }
        review.suggestedActions.firstOrNull()?.let {
            Text("建议：$it", style = MaterialTheme.typography.bodySmall)
        }
        OutlinedButton(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp),
            enabled = !busy,
            onClick = onCreate,
        ) {
            Text("下发这题")
        }
    }
}

@Composable
private fun ParentTaskStatusCard(
    busy: Boolean,
    tasks: List<BrainyPalChildPracticeTaskDetail>,
    onRefresh: () -> Unit,
    onPreviewOcr: (BrainyPalParentOcrEvidenceCard) -> Unit,
    onConfirmOcr: (taskId: String, itemId: String, confirmation: String, label: String) -> Unit,
) {
    val groups = BrainyPalParentWorkbenchUi.taskGroups(tasks)
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = HugeIcons.Camera01,
                    contentDescription = null,
                    tint = BrainyPalChildTheme.cyanAccent,
                )
                Text("任务状态与 OCR 确认", style = MaterialTheme.typography.titleMedium)
            }
            FilledTonalButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                enabled = !busy,
                onClick = onRefresh,
            ) {
                Icon(HugeIcons.Refresh03, null)
                Text(
                    text = "刷新任务状态",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            if (groups.isEmpty()) {
                Text(
                    text = "还没有加载任务。刷新后可以查看孩子完成状态和需要家长确认的 OCR 结果。",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                groups.forEachIndexed { groupIndex, group ->
                    if (groupIndex > 0) HorizontalDivider()
                    Text(group.label, style = MaterialTheme.typography.titleSmall)
                    group.tasks.take(8).forEachIndexed { index, task ->
                        if (index > 0) HorizontalDivider()
                        ParentTaskStatusRow(
                            task = task,
                            busy = busy,
                            onPreviewOcr = onPreviewOcr,
                            onConfirmOcr = onConfirmOcr,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ParentTaskStatusRow(
    task: BrainyPalChildPracticeTaskDetail,
    busy: Boolean,
    onPreviewOcr: (BrainyPalParentOcrEvidenceCard) -> Unit,
    onConfirmOcr: (taskId: String, itemId: String, confirmation: String, label: String) -> Unit,
) {
    val summary = BrainyPalParentTaskSummary.from(task)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(summary.title, style = MaterialTheme.typography.titleSmall)
        Text(
            text = "${summary.kindLabel} · ${summary.statusLabel} · ${summary.itemCountLabel} · 求助 ${summary.helpUsageLabel}",
            style = MaterialTheme.typography.bodySmall,
        )
        if (summary.parentSummary.isNotBlank()) {
            Text("家长摘要：${summary.parentSummary}", style = MaterialTheme.typography.bodySmall)
        }
        BrainyPalParentWorkbenchUi.ocrEvidenceCards(task).forEach { card ->
            ParentOcrEvidenceReviewCard(
                card = card,
                busy = busy,
                onPreview = { onPreviewOcr(card) },
                onConfirm = onConfirmOcr,
            )
        }
    }
}

@Composable
private fun ParentOcrEvidenceReviewCard(
    card: BrainyPalParentOcrEvidenceCard,
    busy: Boolean,
    onPreview: () -> Unit,
    onConfirm: (taskId: String, itemId: String, confirmation: String, label: String) -> Unit,
) {
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
                text = "${card.title} · ${card.resultLabel}",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(card.evidenceLine, style = MaterialTheme.typography.bodyMedium)
            Text(card.sourceRegionLabel, style = MaterialTheme.typography.bodySmall)
            card.guidanceLabel?.let { guidance ->
                Text(
                    text = guidance,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 40.dp),
                onClick = onPreview,
            ) {
                Icon(HugeIcons.Image02, null)
                Text(
                    text = card.previewActionLabel,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            if (card.requiresManualConfirmation) {
                card.actions.forEach { action ->
                    OutlinedButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 40.dp),
                        enabled = !busy,
                        onClick = {
                            onConfirm(card.taskId, card.itemId, action.confirmation, action.label)
                        },
                    ) {
                        Text(action.label)
                    }
                }
            }
        }
    }
}

@Composable
private fun ParentOcrImagePreviewDialog(
    card: BrainyPalParentOcrEvidenceCard,
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
                painter = rememberAsyncImagePainter(card.previewImageRef),
                contentDescription = card.previewActionLabel,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
            card.previewBoundingBox?.let { box ->
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
                text = "${card.title} · ${card.sourceRegionLabel}",
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

@Composable
private fun ParentBusyMessage(
    busy: Boolean,
    message: String?,
) {
    if (busy) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp))
            Text("正在处理，请稍等", style = MaterialTheme.typography.bodySmall)
        }
    }
    message?.let {
        Text(
            text = it,
            color = if (it.contains("已")) {
                BrainyPalChildTheme.cyanAccent
            } else {
                MaterialTheme.colorScheme.error
            },
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

private fun currentDraftConnection(
    baseUrl: String,
    apiKey: String,
) = BrainyPalChildConnectionConfig(
    baseUrl = baseUrl,
    apiKey = apiKey,
)

private fun Settings.withBrainyPalConnection(
    config: BrainyPalChildConnectionConfig,
    pin: BrainyPalManagementPin,
): Settings {
    val provider = BrainyPalChildModePolicy.brainyPalProvider(config)
    val modelId = provider.models.single().id

    return copy(
        brainyPalChildConnection = config,
        brainyPalManagementPin = pin,
        providers = listOf(provider) + providers.filterNot { it.name == provider.name },
        chatModelId = modelId,
        fastModelId = modelId,
        translateModeId = modelId,
        compressModelId = modelId,
        enableWebSearch = false,
        mcpServers = emptyList(),
        modeInjections = emptyList(),
        lorebooks = emptyList(),
        quickMessages = emptyList(),
    )
}

package me.rerere.rikkahub.ui.pages.brainypal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.ArrowRight01
import me.rerere.hugeicons.stroke.Book03
import me.rerere.hugeicons.stroke.BubbleChatQuestion
import me.rerere.hugeicons.stroke.Cancel01
import me.rerere.hugeicons.stroke.FloppyDisk
import me.rerere.hugeicons.stroke.MessageQuestion
import me.rerere.hugeicons.stroke.Refresh03
import me.rerere.hugeicons.stroke.Tick01
import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.brainypal.BrainyPalChildHomeState
import me.rerere.rikkahub.brainypal.BrainyPalChildPracticeTaskDetail
import me.rerere.rikkahub.brainypal.BrainyPalChildPracticeTaskItem
import me.rerere.rikkahub.brainypal.BrainyPalChildPracticeTaskSummary
import me.rerere.rikkahub.brainypal.BrainyPalChildUiText
import me.rerere.rikkahub.brainypal.BrainyPalPracticeDrafts
import me.rerere.rikkahub.ui.brainypal.designsystem.BrainyPalContentState
import me.rerere.rikkahub.ui.brainypal.designsystem.BrainyPalTheme
import me.rerere.rikkahub.ui.brainypal.designsystem.components.BrainyPalStatePane
import me.rerere.rikkahub.ui.brainypal.navigation.BrainyPalChildDestination
import me.rerere.rikkahub.ui.brainypal.navigation.BrainyPalChildScaffold
import me.rerere.rikkahub.ui.components.ui.CardGroup
import me.rerere.rikkahub.ui.context.LocalNavController
import me.rerere.rikkahub.utils.UiState
import me.rerere.rikkahub.utils.base64Encode
import me.rerere.rikkahub.utils.plus
import org.koin.androidx.compose.koinViewModel

@Composable
fun BrainyPalPracticePage(vm: BrainyPalHomeVM = koinViewModel()) {
    val navController = LocalNavController.current
    val state by vm.state.collectAsStateWithLifecycle()
    val practiceDetailState by vm.practiceDetailState.collectAsStateWithLifecycle()
    val askTarget = (state as? UiState.Success)?.data?.workbench?.chatAction?.target
    val selectedDetail = (practiceDetailState.detail as? UiState.Success)?.data
    val selectedItem = selectedDetail?.items?.getOrNull(practiceDetailState.selectedItemIndex)
    val contextualAskTarget = (askTarget as? Screen.Chat)?.let { target ->
        selectedItem?.let { item ->
            target.copy(
                text = "我在做这道题：${item.prompt}\n请先帮我理清思路，不要直接给答案。".base64Encode(),
            )
        } ?: target
    }

    BrainyPalTheme {
        BrainyPalChildScaffold(
            title = "今日练习",
            selectedDestination = BrainyPalChildDestination.Practice,
            askEnabled = askTarget != null,
            onNavigate = { destination ->
                when (destination) {
                    BrainyPalChildDestination.Home -> navController.navigate(Screen.BrainyPalHome)
                    BrainyPalChildDestination.Ask -> contextualAskTarget?.let(navController::navigate)
                    BrainyPalChildDestination.Practice -> Unit
                }
            },
        ) { innerPadding ->
            when (val current = state) {
                UiState.Loading,
                UiState.Idle -> {
                    BrainyPalStatePane(
                        state = BrainyPalContentState.Loading,
                        title = "正在取回练习",
                        detail = "已经写下的内容会继续保留",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    )
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
                        onSelectItem = vm::selectPracticeItem,
                        onUpdateDraft = vm::updatePracticeDraft,
                        onSaveAnswer = vm::savePracticeAnswer,
                        onRequestHelp = vm::requestPracticeHelp,
                        onSubmitTask = vm::submitPracticeTask,
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
                        onSelectItem = vm::selectPracticeItem,
                        onUpdateDraft = vm::updatePracticeDraft,
                        onSaveAnswer = vm::savePracticeAnswer,
                        onRequestHelp = vm::requestPracticeHelp,
                        onSubmitTask = vm::submitPracticeTask,
                    )
                }
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
    onSelectItem: (Int) -> Unit,
    onUpdateDraft: (String, String, String) -> Unit,
    onSaveAnswer: (String, String, String, String) -> Unit,
    onRequestHelp: (String, String) -> Unit,
    onSubmitTask: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
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
                BrainyPalStatePane(
                    state = BrainyPalContentState.Restricted,
                    title = "请家长检查连接",
                    detail = "检查好以后，点这里再取回练习。",
                    primaryActionLabel = "重新加载",
                    onPrimaryAction = onRefresh,
                )
            }
            return@LazyColumn
        }

        item {
            CardGroup(
                title = { Text("任务概览") },
            ) {
                item(
                    leadingContent = { Icon(HugeIcons.Book03, null) },
                    headlineContent = { Text(state.workbench.practiceSummary) },
                    supportingContent = { Text("选择一个任务，从当前进度继续。") },
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
            val continuing = state.practiceTasks.filter { it.status == "in_progress" }
            val waiting = state.practiceTasks.filter { it.status == "assigned" }
            val completed = state.practiceTasks.filter { it.status !in setOf("in_progress", "assigned") }
            practiceTaskGroup("继续做", continuing, practiceDetailState.selectedTaskId, onSelectTask)
            practiceTaskGroup("待开始", waiting, practiceDetailState.selectedTaskId, onSelectTask)
            practiceTaskGroup("已完成", completed, practiceDetailState.selectedTaskId, onSelectTask)
        }

        if (practiceDetailState.selectedTaskId != null) {
            val selectedTaskId = practiceDetailState.selectedTaskId
            item {
                PracticeTaskDetailPane(
                    detailState = practiceDetailState,
                    onRetry = { onSelectTask(selectedTaskId) },
                    onClose = onCloseTask,
                    onSelectItem = onSelectItem,
                    onUpdateDraft = onUpdateDraft,
                    onSaveAnswer = onSaveAnswer,
                    onRequestHelp = onRequestHelp,
                    onSubmitTask = onSubmitTask,
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
    detailState: BrainyPalPracticeTaskDetailState,
    onRetry: () -> Unit,
    onClose: () -> Unit,
    onSelectItem: (Int) -> Unit,
    onUpdateDraft: (String, String, String) -> Unit,
    onSaveAnswer: (String, String, String, String) -> Unit,
    onRequestHelp: (String, String) -> Unit,
    onSubmitTask: (String) -> Unit,
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
                detail = detail.data,
                drafts = detailState.drafts,
                helpHint = detailState.helpHint,
                actionInProgress = detailState.actionInProgress,
                actionStatus = detailState.actionStatus,
                selectedItemIndex = detailState.selectedItemIndex,
                onClose = onClose,
                onSelectItem = onSelectItem,
                onUpdateDraft = onUpdateDraft,
                onSaveAnswer = onSaveAnswer,
                onRequestHelp = onRequestHelp,
                onSubmitTask = onSubmitTask,
            )
        }
    }
}

@Composable
private fun PracticeTaskDetailContent(
    detail: BrainyPalChildPracticeTaskDetail,
    drafts: BrainyPalPracticeDrafts,
    helpHint: BrainyPalPracticeTaskHelpHint?,
    actionInProgress: Boolean,
    actionStatus: BrainyPalPracticeTaskActionStatus?,
    selectedItemIndex: Int,
    onClose: () -> Unit,
    onSelectItem: (Int) -> Unit,
    onUpdateDraft: (String, String, String) -> Unit,
    onSaveAnswer: (String, String, String, String) -> Unit,
    onRequestHelp: (String, String) -> Unit,
    onSubmitTask: (String) -> Unit,
) {
    var showSubmitSummary by rememberSaveable(detail.taskId) { mutableStateOf(false) }
    val currentIndex = selectedItemIndex.coerceIn(0, (detail.items.size - 1).coerceAtLeast(0))
    val currentItem = detail.items.getOrNull(currentIndex)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CardGroup(
            title = { Text("练习详情") },
        ) {
            item(
                leadingContent = { Icon(HugeIcons.Book03, null) },
                headlineContent = { Text(detail.title) },
                supportingContent = {
                    Text("${detail.statusLabel} · ${detail.items.size} 题 · 还能提示 ${detail.remainingHelp} 次")
                },
            )
        }

        if (actionStatus != null) {
            PracticeActionStatusCard(actionStatus)
        }

        if (currentItem != null) {
            PracticeTaskQuestionCard(
                taskId = detail.taskId,
                index = currentIndex + 1,
                total = detail.items.size,
                item = currentItem,
                draft = drafts.get(currentItem.itemId),
                helpMessage = helpHint
                    ?.takeIf { it.itemId == currentItem.itemId }
                    ?.message,
                remainingHelp = detail.remainingHelp,
                canEdit = detail.canSubmit,
                actionInProgress = actionInProgress,
                onUpdateDraft = onUpdateDraft,
                onSaveAnswer = onSaveAnswer,
                onRequestHelp = onRequestHelp,
                focusEvidence = currentItem.needsMoreEffort && actionStatus?.error == true,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    enabled = currentIndex > 0 && !actionInProgress,
                    onClick = { onSelectItem(currentIndex - 1) },
                ) { Text("上一题") }
                FilledTonalButton(
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    enabled = currentIndex < detail.items.lastIndex && !actionInProgress,
                    onClick = { onSelectItem(currentIndex + 1) },
                ) { Text("下一题") }
            }
        }

        if (showSubmitSummary && detail.canSubmit) {
            PracticeSubmitSummary(
                detail = detail,
                drafts = drafts,
                actionInProgress = actionInProgress,
                onConfirm = { onSubmitTask(detail.taskId) },
                onContinue = { showSubmitSummary = false },
            )
        } else {
            Button(
                modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                enabled = detail.canSubmit && !actionInProgress,
                onClick = { showSubmitSummary = true },
            ) {
                Icon(HugeIcons.Tick01, null)
                Text(
                    text = if (detail.canSubmit) "查看并提交" else "已提交",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
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
private fun PracticeActionStatusCard(status: BrainyPalPracticeTaskActionStatus) {
    val colors = if (status.error) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
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
    total: Int,
    item: BrainyPalChildPracticeTaskItem,
    draft: me.rerere.rikkahub.brainypal.BrainyPalPracticeDraft,
    helpMessage: String?,
    remainingHelp: Int,
    canEdit: Boolean,
    actionInProgress: Boolean,
    onUpdateDraft: (String, String, String) -> Unit,
    onSaveAnswer: (String, String, String, String) -> Unit,
    onRequestHelp: (String, String) -> Unit,
    focusEvidence: Boolean,
) {
    val actionsEnabled = canEdit && !actionInProgress
    val evidenceFocusRequester = androidx.compose.runtime.remember(item.itemId) { FocusRequester() }
    LaunchedEffect(focusEvidence) {
        if (focusEvidence) evidenceFocusRequester.requestFocus()
    }

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
                text = "第 $index / $total 题",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = item.prompt,
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
                modifier = Modifier.fillMaxWidth().focusRequester(evidenceFocusRequester),
                value = draft.answer,
                onValueChange = { onUpdateDraft(item.itemId, it, draft.evidence) },
                enabled = canEdit,
                label = { Text("你的答案") },
                minLines = 2,
                maxLines = 5,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = draft.evidence,
                onValueChange = { onUpdateDraft(item.itemId, draft.answer, it) },
                enabled = canEdit,
                label = { Text("你怎么想的，或哪一步卡住了") },
                minLines = 2,
                maxLines = 5,
            )
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
                onClick = { onRequestHelp(taskId, item.itemId) },
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
private fun PracticeHelpHintCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
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
private fun PracticeSubmitSummary(
    detail: BrainyPalChildPracticeTaskDetail,
    drafts: BrainyPalPracticeDrafts,
    actionInProgress: Boolean,
    onConfirm: () -> Unit,
    onContinue: () -> Unit,
) {
    val answeredCount = detail.items.count { drafts.get(it.itemId).answer.isNotBlank() }
    val evidenceCount = detail.items.count { drafts.get(it.itemId).evidence.isNotBlank() }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("提交前看一眼", style = MaterialTheme.typography.titleMedium)
            Text(
                "已填写答案 $answeredCount / ${detail.items.size}，已写下思路或卡点 $evidenceCount / ${detail.items.size}。",
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                enabled = !actionInProgress,
                onClick = onConfirm,
            ) {
                if (actionInProgress) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Icon(HugeIcons.Tick01, null)
                }
                Text(
                    text = if (actionInProgress) "正在提交" else "确认提交",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            TextButton(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                enabled = !actionInProgress,
                onClick = onContinue,
            ) {
                Text("继续检查")
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
            title = { Text("今日练习") },
        ) {
            item(
                leadingContent = { Icon(HugeIcons.Book03, null) },
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

private fun LazyListScope.practiceTaskGroup(
    title: String,
    tasks: List<BrainyPalChildPracticeTaskSummary>,
    selectedTaskId: String?,
    onSelectTask: (String) -> Unit,
) {
    if (tasks.isEmpty()) return
    item {
        CardGroup(title = { Text(title) }) {
            tasks.forEach { task ->
                taskItem(
                    task = task,
                    selected = selectedTaskId == task.taskId,
                    onClick = { onSelectTask(task.taskId) },
                )
            }
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
        leadingContent = { Icon(HugeIcons.Book03, null) },
        headlineContent = { Text(task.title) },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${task.statusLabel} · ${task.itemCount} 题")
                Text("还能求助 ${task.remainingHelp} 次")
                if (selected) {
                    Text(
                        text = "已展开详情",
                        color = MaterialTheme.colorScheme.primary,
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
                    text = practiceTaskActionLabel(task),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
                Icon(
                    imageVector = HugeIcons.ArrowRight01,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(18.dp),
                )
            }
        },
    )
}

private fun practiceTaskActionLabel(task: BrainyPalChildPracticeTaskSummary): String {
    return when (task.status) {
        "submitted", "reviewing", "completed", "expired" -> "查看"
        else -> "开始"
    }
}

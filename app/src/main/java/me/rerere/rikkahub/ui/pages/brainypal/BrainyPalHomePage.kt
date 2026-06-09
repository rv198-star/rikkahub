package me.rerere.rikkahub.ui.pages.brainypal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.Book03
import me.rerere.hugeicons.stroke.BubbleChatQuestion
import me.rerere.hugeicons.stroke.Refresh03
import me.rerere.hugeicons.stroke.ServerStack01
import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.brainypal.BrainyPalChildHomeState
import me.rerere.rikkahub.brainypal.BrainyPalChildPracticeTaskSummary
import me.rerere.rikkahub.ui.components.nav.BackButton
import me.rerere.rikkahub.ui.components.ui.CardGroup
import me.rerere.rikkahub.ui.context.LocalNavController
import me.rerere.rikkahub.ui.theme.CustomColors
import me.rerere.rikkahub.utils.UiState
import me.rerere.rikkahub.utils.plus
import org.koin.androidx.compose.koinViewModel

@Composable
fun BrainyPalHomePage(vm: BrainyPalHomeVM = koinViewModel()) {
    val navController = LocalNavController.current
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("BrainyPal") },
                navigationIcon = { BackButton() },
                actions = {
                    IconButton(onClick = vm::refresh) {
                        Icon(HugeIcons.Refresh03, null)
                    }
                },
                colors = CustomColors.topBarColors,
                scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
            )
        },
        containerColor = CustomColors.topBarColors.containerColor,
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
                BrainyPalHomeContent(
                    innerPadding = innerPadding,
                    state = null,
                    errorMessage = current.error.message ?: "暂时连不上 BrainyPal，可以稍后重试",
                    onNavigate = { navController.navigate(it) },
                )
            }

            is UiState.Success -> {
                BrainyPalHomeContent(
                    innerPadding = innerPadding,
                    state = current.data,
                    errorMessage = current.data.errorMessage,
                    onNavigate = { navController.navigate(it) },
                )
            }
        }
    }
}

@Composable
private fun BrainyPalHomeContent(
    innerPadding: PaddingValues,
    state: BrainyPalChildHomeState?,
    errorMessage: String?,
    onNavigate: (Screen) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = innerPadding + PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (state != null) {
            item {
                CardGroup(
                    title = { Text("连接") },
                ) {
                    item(
                        leadingContent = { Icon(HugeIcons.ServerStack01, null) },
                        headlineContent = { Text(state.workbench.connectionStatus) },
                        supportingContent = {
                            Text(if (state.workbench.configured) "Agent Service 已配置" else "需要家长完成服务连接")
                        },
                        onClick = { onNavigate(Screen.BrainyPalConnection) },
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(state.workbench.chatAction.target) },
                    ) {
                        Icon(HugeIcons.BubbleChatQuestion, null)
                        Text(
                            text = state.workbench.chatAction.label,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                    FilledTonalButton(
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(state.workbench.practiceAction.target) },
                    ) {
                        Icon(HugeIcons.Book03, null)
                        Text(
                            text = state.workbench.practiceAction.label,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }

            if (state.workbench.showReviewOffer) {
                item {
                    CardGroup(
                        title = { Text("复习建议") },
                    ) {
                        item(
                            leadingContent = { Icon(HugeIcons.Book03, null) },
                            headlineContent = { Text(state.workbench.reviewMessage) },
                            supportingContent = { Text("用一小步把记忆接回来") },
                            trailingContent = {
                                Text(
                                    text = state.workbench.reviewAction.label,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            },
                            onClick = { onNavigate(state.workbench.reviewAction.target) },
                        )
                    }
                }
            }

            item {
                CardGroup(
                    title = { Text("今日练习") },
                ) {
                    item(
                        leadingContent = { Icon(HugeIcons.Book03, null) },
                        headlineContent = { Text(state.workbench.practiceSummary) },
                        supportingContent = { Text("查看今天的任务、帮助次数和状态") },
                        onClick = { onNavigate(state.workbench.practiceAction.target) },
                    )
                    state.practiceTasks.take(3).forEach { task ->
                        practiceTaskSummaryItem(task = task, onClick = { onNavigate(Screen.BrainyPalPractice) })
                    }
                }
            }
        }

        errorMessage?.let { message ->
            item {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        item {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigate(Screen.BrainyPalConnection) },
            ) {
                Icon(HugeIcons.ServerStack01, null)
                Text(
                    text = "家长连接设置",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

private fun me.rerere.rikkahub.ui.components.ui.CardGroupScope.practiceTaskSummaryItem(
    task: BrainyPalChildPracticeTaskSummary,
    onClick: () -> Unit,
) {
    item(
        leadingContent = { Icon(HugeIcons.Book03, null) },
        headlineContent = { Text(task.title) },
        supportingContent = {
            Text(
                "${task.statusLabel} · ${task.itemCount} 题 · 还能求助 ${task.remainingHelp} 次" +
                    if (task.needsMoreEffort) " · 需要再认真一点" else ""
            )
        },
        onClick = onClick,
    )
}

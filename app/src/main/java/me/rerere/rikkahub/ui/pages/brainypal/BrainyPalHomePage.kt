package me.rerere.rikkahub.ui.pages.brainypal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
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
import me.rerere.rikkahub.brainypal.BrainyPalChildUiText
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
                    onRefresh = vm::refresh,
                    onNavigate = { navController.navigate(it) },
                )
            }

            is UiState.Success -> {
                BrainyPalHomeContent(
                    innerPadding = innerPadding,
                    state = current.data,
                    errorMessage = current.data.errorMessage,
                    onRefresh = vm::refresh,
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
    onRefresh: () -> Unit,
    onNavigate: (Screen) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = innerPadding + PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (state != null) {
            item {
                val connectionStatus = BrainyPalChildUiText.connectionStatus(
                    state.connection
                )
                CardGroup(
                    title = { Text("连接") },
                ) {
                    item(
                        leadingContent = { Icon(HugeIcons.ServerStack01, null) },
                        headlineContent = { Text(connectionStatus.title) },
                        supportingContent = {
                            Text(
                                if (state.workbench.configured) {
                                    "家长可在连接设置中查看完整地址"
                                } else {
                                    connectionStatus.detail
                                }
                            )
                        },
                        onClick = { onNavigate(Screen.BrainyPalConnection) },
                    )
                }
            }

            item {
                PrimaryActionStack(
                    primaryLabel = state.workbench.chatAction.label,
                    secondaryLabel = state.workbench.practiceAction.label,
                    onPrimary = { onNavigate(state.workbench.chatAction.target) },
                    onSecondary = { onNavigate(state.workbench.practiceAction.target) },
                )
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

        val recoveryMessage = BrainyPalChildUiText.homeErrorRecovery(errorMessage)
        if (recoveryMessage.isNotBlank()) {
            item {
                RecoveryCard(
                    message = recoveryMessage,
                    onRefresh = onRefresh,
                    onSettings = { onNavigate(Screen.BrainyPalConnection) },
                )
            }
        }

        item {
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
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

@Composable
private fun PrimaryActionStack(
    primaryLabel: String,
    secondaryLabel: String,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            onClick = onPrimary,
        ) {
            Icon(HugeIcons.BubbleChatQuestion, null)
            Text(
                text = primaryLabel,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        FilledTonalButton(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            onClick = onSecondary,
        ) {
            Icon(HugeIcons.Book03, null)
            Text(
                text = secondaryLabel,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun RecoveryCard(
    message: String,
    onRefresh: () -> Unit,
    onSettings: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
            FilledTonalButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                onClick = onRefresh,
            ) {
                Icon(HugeIcons.Refresh03, null)
                Text(
                    text = "刷新",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                onClick = onSettings,
            ) {
                Icon(HugeIcons.ServerStack01, null)
                Text(
                    text = "家长检查服务",
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

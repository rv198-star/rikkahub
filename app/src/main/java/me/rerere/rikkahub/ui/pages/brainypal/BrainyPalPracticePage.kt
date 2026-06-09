package me.rerere.rikkahub.ui.pages.brainypal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
fun BrainyPalPracticePage(vm: BrainyPalHomeVM = koinViewModel()) {
    val navController = LocalNavController.current
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("今日练习") },
                navigationIcon = { BackButton() },
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
                BrainyPalPracticeContent(
                    innerPadding = innerPadding,
                    state = null,
                    errorMessage = current.error.message ?: "暂时连不上 BrainyPal，可以稍后重试",
                    onNavigate = { navController.navigate(it) },
                    onRefresh = vm::refresh,
                )
            }

            is UiState.Success -> {
                BrainyPalPracticeContent(
                    innerPadding = innerPadding,
                    state = current.data,
                    errorMessage = current.data.errorMessage,
                    onNavigate = { navController.navigate(it) },
                    onRefresh = vm::refresh,
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
    onNavigate: (Screen) -> Unit,
    onRefresh: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = innerPadding + PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (state == null) {
            item {
                PracticeEmptyCard(
                    headline = "暂时没有练习数据",
                    supporting = errorMessage ?: "稍后再试一次",
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
                    leadingContent = { Icon(HugeIcons.Book03, null) },
                    headlineContent = { Text(state.workbench.practiceSummary) },
                    supportingContent = { Text(state.workbench.connectionStatus) },
                )
            }
        }

        if (state.practiceTasks.isEmpty()) {
            item {
                PracticeEmptyCard(
                    headline = "现在没有新的今日练习",
                    supporting = errorMessage ?: "可以先去问问 BrainyPal",
                    primaryLabel = state.workbench.chatAction.label,
                    onPrimary = { onNavigate(state.workbench.chatAction.target) },
                )
            }
        } else {
            item {
                CardGroup(
                    title = { Text("任务") },
                ) {
                    state.practiceTasks.forEach { task ->
                        taskItem(task)
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
                onClick = onRefresh,
            ) {
                Text("刷新任务")
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
    CardGroup(
        title = { Text("今日练习") },
    ) {
        item(
            leadingContent = { Icon(HugeIcons.ServerStack01, null) },
            headlineContent = { Text(headline) },
            supportingContent = { Text(supporting) },
        )
    }
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        onClick = onPrimary,
    ) {
        Text(primaryLabel)
    }
}

private fun me.rerere.rikkahub.ui.components.ui.CardGroupScope.taskItem(
    task: BrainyPalChildPracticeTaskSummary,
) {
    item(
        leadingContent = { Icon(HugeIcons.Book03, null) },
        headlineContent = { Text(task.title) },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${task.statusLabel} · ${task.itemCount} 题")
                Text("还能求助 ${task.remainingHelp} 次")
                if (task.needsMoreEffort) {
                    Text(
                        text = "这项需要再认真一点",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        trailingContent = {
            Text(
                text = "查看",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
            )
        },
    )
}

package me.rerere.rikkahub.brainypal.child

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.Book03
import me.rerere.hugeicons.stroke.BubbleChatQuestion
import me.rerere.hugeicons.stroke.Refresh03
import me.rerere.hugeicons.stroke.Sparkles
import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.brainypal.child.BrainyPalChildHomeState
import me.rerere.rikkahub.brainypal.shared.BrainyPalChildPracticeTaskSummary
import me.rerere.rikkahub.brainypal.child.BrainyPalChildUiText
import me.rerere.rikkahub.ui.components.nav.BackButton
import me.rerere.rikkahub.ui.components.ui.CardGroup
import me.rerere.rikkahub.ui.context.LocalNavController
import me.rerere.rikkahub.brainypal.child.theme.BrainyPalChildTheme
import me.rerere.rikkahub.brainypal.shared.components.BrainyPalSignalMark
import me.rerere.rikkahub.brainypal.shared.theme.BrainyPalTokens
import me.rerere.rikkahub.utils.UiState
import me.rerere.rikkahub.utils.plus
import org.koin.androidx.compose.koinViewModel

data class BrainyPalHomeVisualSemantics(
    val sectionOrder: List<String>,
    val courageStationSectionId: String,
    val todayTaskSectionId: String,
    val heroTone: String,
    val primaryActionTone: String,
    val secondaryActionTone: String,
    val maxPreviewTasks: Int,
    val childSafeText: String,
    val showBackNavigation: Boolean,
)

object BrainyPalHomePageVisualSemantics {
    val default = BrainyPalHomeVisualSemantics(
        sectionOrder = listOf(
            "companion",
            "yongqi_station",
            "primary_actions",
            "review_offer",
            "today_tasks",
            "stable_navigation",
        ),
        courageStationSectionId = "yongqi_station",
        todayTaskSectionId = "today_tasks",
        heroTone = "warm_companion",
        primaryActionTone = "calm_primary",
        secondaryActionTone = "gentle_secondary",
        maxPreviewTasks = 3,
        childSafeText = "今天先从一个小问题开始。" +
            "${BrainyPalTokens.stationFullName}记录愿意开始、说出卡点和提示后再试的小信号。",
        showBackNavigation = false,
    )
}

@Composable
fun BrainyPalHomePage(vm: BrainyPalHomeVM = koinViewModel()) {
    val navController = LocalNavController.current
    val state by vm.state.collectAsStateWithLifecycle()
    val visualSemantics = BrainyPalHomePageVisualSemantics.default
    val successState = (state as? UiState.Success)?.data
    val reviewOfferKey = successState?.reviewOffer?.event?.let { event ->
        "${event.relatedQuestionId}:${event.strategyVersionId}"
    }
    val blockedRouteNotice = navController.blockedRouteNotice
    var dismissedReviewOfferKey by rememberSaveable { mutableStateOf<String?>(null) }

    BrainyPalChildNavigationScaffold(
        selectedDestination = BrainyPalChildDestination.Home,
        askEnabled = successState?.workbench?.configured == true,
        practiceEnabled = successState?.workbench?.configured == true,
        onNavigate = { destination ->
            when (destination) {
                BrainyPalChildDestination.Home -> Unit
                BrainyPalChildDestination.Ask -> {
                    successState?.workbench?.chatAction?.target?.let(navController::navigate)
                }
                BrainyPalChildDestination.Practice -> navController.navigate(Screen.BrainyPalPractice)
            }
        },
    ) {
        Scaffold(
            topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(BrainyPalTokens.stationName) },
                navigationIcon = {
                    if (visualSemantics.showBackNavigation) {
                        BackButton()
                    }
                },
                actions = {
                    IconButton(onClick = vm::refresh) {
                        Icon(HugeIcons.Refresh03, null)
                    }
                },
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
                BrainyPalHomeContent(
                    innerPadding = innerPadding,
                    state = null,
                    errorMessage = current.error.message ?: "暂时连不上 BrainyPal，可以稍后重试",
                    reviewDismissed = false,
                    onDismissReview = {},
                    blockedRouteNotice = blockedRouteNotice,
                    onDismissBlockedRoute = navController::clearBlockedRouteNotice,
                    onRefresh = vm::refresh,
                    onNavigate = { navController.navigate(it) },
                )
            }

            is UiState.Success -> {
                BrainyPalHomeContent(
                    innerPadding = innerPadding,
                    state = current.data,
                    errorMessage = current.data.errorMessage,
                    reviewDismissed = reviewOfferKey != null && dismissedReviewOfferKey == reviewOfferKey,
                    onDismissReview = { dismissedReviewOfferKey = reviewOfferKey },
                    blockedRouteNotice = blockedRouteNotice,
                    onDismissBlockedRoute = navController::clearBlockedRouteNotice,
                    onRefresh = vm::refresh,
                    onNavigate = { navController.navigate(it) },
                )
            }
            }
        }
    }
}

@Composable
private fun BrainyPalHomeContent(
    innerPadding: PaddingValues,
    state: BrainyPalChildHomeState?,
    errorMessage: String?,
    reviewDismissed: Boolean,
    onDismissReview: () -> Unit,
    blockedRouteNotice: String?,
    onDismissBlockedRoute: () -> Unit,
    onRefresh: () -> Unit,
    onNavigate: (Screen) -> Unit,
) {
    val visualSemantics = BrainyPalHomePageVisualSemantics.default
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = innerPadding + PaddingValues(BrainyPalChildTheme.pagePadding),
        verticalArrangement = Arrangement.spacedBy(BrainyPalChildTheme.sectionSpacing),
    ) {
        if (blockedRouteNotice != null) {
            item {
                DismissibleNoticeCard(
                    message = blockedRouteNotice,
                    onDismiss = onDismissBlockedRoute,
                )
            }
        }

        if (state != null) {
            if (!state.workbench.configured) {
                item {
                    RecoveryCard(
                        message = "请家长检查连接",
                        onRefresh = onRefresh,
                    )
                }
                return@LazyColumn
            }

            item {
                BrainyPalCompanionCard(
                    configured = true,
                    practiceSummary = state.workbench.practiceSummary,
                )
            }

            item {
                CardGroup(
                    title = { Text("长期养成") },
                ) {
                    item(
                        leadingContent = {
                            Icon(
                                imageVector = HugeIcons.Sparkles,
                                contentDescription = null,
                                tint = BrainyPalChildTheme.amberText,
                            )
                        },
                        headlineContent = { Text(BrainyPalTokens.stationFullName) },
                        supportingContent = { Text("看看今天接收到的稳定信号、技能天梯和最近记录") },
                        trailingContent = {
                            Text(
                                text = "进入",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        },
                        onClick = { onNavigate(Screen.BrainyPalStation) },
                    )
                }
            }

            item {
                PrimaryActionStack(
                    primaryLabel = state.workbench.primaryAction.label,
                    secondaryLabel = state.workbench.secondaryAction.label,
                    primaryIsPractice = state.workbench.primaryAction.target == Screen.BrainyPalPractice,
                    secondaryIsPractice = state.workbench.secondaryAction.target == Screen.BrainyPalPractice,
                    onPrimary = { onNavigate(state.workbench.primaryAction.target) },
                    onSecondary = { onNavigate(state.workbench.secondaryAction.target) },
                )
            }

            if (state.workbench.showReviewOffer && !reviewDismissed) {
                item {
                    ReviewOfferCard(
                        message = state.workbench.reviewMessage,
                        actionLabel = state.workbench.reviewAction.label,
                        onAccept = { onNavigate(state.workbench.reviewAction.target) },
                        onDismiss = onDismissReview,
                    )
                }
            }

            item {
                CardGroup(
                    title = { Text("今日任务") },
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
                        supportingContent = { Text("先写下自己的想法，需要时再用提示券。") },
                        onClick = { onNavigate(state.workbench.practiceAction.target) },
                    )
                    state.practiceTasks.take(visualSemantics.maxPreviewTasks).forEach { task ->
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
                )
            }
        }

    }
}

@Composable
private fun BrainyPalCompanionCard(
    configured: Boolean,
    practiceSummary: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = BrainyPalChildTheme.heroContainer,
            contentColor = BrainyPalChildTheme.heroContent,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(BrainyPalChildTheme.heroPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = HugeIcons.Sparkles,
                contentDescription = null,
                tint = BrainyPalChildTheme.amberText,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BrainyPalSignalMark(size = 52.dp)
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = if (configured) "${BrainyPalTokens.stationName}在线" else "等待大人连接",
                        style = MaterialTheme.typography.labelLarge,
                        color = BrainyPalChildTheme.cyanAccent,
                    )
                    Text(
                        text = "清华/MIT 大哥哥陪你拆下一小步",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = if (configured) "今天先接收一个小信号" else "请大人先完成连接",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = if (configured) {
                    "我会先陪你想，再给提示，不急着直接给答案。${BrainyPalTokens.childTrustPrinciple} $practiceSummary。"
                } else {
                    "连接好以后，${BrainyPalTokens.stationName}会陪你聊天、复习和完成今日任务。"
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun PrimaryActionStack(
    primaryLabel: String,
    secondaryLabel: String,
    primaryIsPractice: Boolean,
    secondaryIsPractice: Boolean,
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
            Icon(if (primaryIsPractice) HugeIcons.Book03 else HugeIcons.BubbleChatQuestion, null)
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
            Icon(if (secondaryIsPractice) HugeIcons.Book03 else HugeIcons.BubbleChatQuestion, null)
            Text(
                text = secondaryLabel,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
private fun ReviewOfferCard(
    message: String,
    actionLabel: String,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BrainyPalChildTheme.gentleFocusContainer),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("复习建议", style = MaterialTheme.typography.labelLarge)
            Text(message, style = MaterialTheme.typography.titleMedium)
            Text(
                "用一小步把记忆接回来",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                onClick = onAccept,
            ) {
                Icon(HugeIcons.Book03, null)
                Text(actionLabel, modifier = Modifier.padding(start = 8.dp))
            }
            TextButton(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                onClick = onDismiss,
            ) {
                Text("暂时跳过")
            }
        }
    }
}

@Composable
private fun DismissibleNoticeCard(
    message: String,
    onDismiss: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(message, style = MaterialTheme.typography.bodyLarge)
            TextButton(
                modifier = Modifier.heightIn(min = 48.dp),
                onClick = onDismiss,
            ) {
                Text("知道了")
            }
        }
    }
}

@Composable
private fun RecoveryCard(
    message: String,
    onRefresh: () -> Unit,
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
        }
    }
}

private fun me.rerere.rikkahub.ui.components.ui.CardGroupScope.practiceTaskSummaryItem(
    task: BrainyPalChildPracticeTaskSummary,
    onClick: () -> Unit,
) {
    item(
        leadingContent = {
            Icon(
                imageVector = HugeIcons.Book03,
                contentDescription = null,
                tint = if (task.needsMoreEffort) {
                    BrainyPalChildTheme.amberText
                } else {
                    BrainyPalChildTheme.cyanAccent
                },
            )
        },
        headlineContent = { Text(task.title) },
        supportingContent = {
            Text(
                "${task.statusLabel} · ${
                    BrainyPalPracticeTaskCopy.itemCountLabel(task.taskType, task.itemCount)
                } · 还能求助 ${task.remainingHelp} 次" +
                    if (task.needsMoreEffort) " · 需要再认真一点" else ""
            )
        },
        onClick = onClick,
    )
}

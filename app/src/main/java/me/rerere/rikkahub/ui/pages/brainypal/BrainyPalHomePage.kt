package me.rerere.rikkahub.ui.pages.brainypal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.Book03
import me.rerere.hugeicons.stroke.BubbleChatQuestion
import me.rerere.hugeicons.stroke.Refresh03
import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.brainypal.BrainyPalChildHomeState
import me.rerere.rikkahub.brainypal.BrainyPalChildPracticeTaskSummary
import me.rerere.rikkahub.ui.brainypal.designsystem.BrainyPalContentState
import me.rerere.rikkahub.ui.brainypal.designsystem.BrainyPalTheme
import me.rerere.rikkahub.ui.brainypal.designsystem.components.BrainyPalStatePane
import me.rerere.rikkahub.ui.brainypal.navigation.BrainyPalChildDestination
import me.rerere.rikkahub.ui.brainypal.navigation.BrainyPalChildScaffold
import me.rerere.rikkahub.ui.components.ui.CardGroup
import me.rerere.rikkahub.ui.context.LocalNavController
import me.rerere.rikkahub.utils.UiState
import me.rerere.rikkahub.utils.plus
import org.koin.androidx.compose.koinViewModel

@Composable
fun BrainyPalHomePage(vm: BrainyPalHomeVM = koinViewModel()) {
    val navController = LocalNavController.current
    val state by vm.state.collectAsStateWithLifecycle()
    val successState = (state as? UiState.Success)?.data
    val askTarget = successState
        ?.takeIf { it.workbench.configured }
        ?.workbench
        ?.chatAction
        ?.target
    val reviewOfferKey = successState?.reviewOffer?.event?.relatedQuestionId
    val blockedRouteNotice = navController.blockedRouteNotice
    var dismissedReviewOfferKey by rememberSaveable { mutableStateOf<String?>(null) }

    BrainyPalTheme {
        BrainyPalChildScaffold(
            title = "BrainyPal",
            selectedDestination = BrainyPalChildDestination.Home,
            askEnabled = askTarget != null,
            practiceEnabled = successState?.workbench?.configured == true,
            onNavigate = { destination ->
                when (destination) {
                    BrainyPalChildDestination.Home -> Unit
                    BrainyPalChildDestination.Ask -> askTarget?.let(navController::navigate)
                    BrainyPalChildDestination.Practice -> navController.navigate(Screen.BrainyPalPractice)
                }
            },
            actions = {
                IconButton(onClick = vm::refresh) {
                    Icon(HugeIcons.Refresh03, contentDescription = "刷新首页")
                }
            },
        ) { innerPadding ->
            when (val current = state) {
                UiState.Loading,
                UiState.Idle -> {
                    BrainyPalStatePane(
                        state = BrainyPalContentState.Loading,
                        title = "正在准备今天的内容",
                        detail = "练习和可用的帮助会马上出现",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    )
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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = innerPadding + PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (blockedRouteNotice != null) {
            item {
                Card {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(blockedRouteNotice, style = MaterialTheme.typography.bodyLarge)
                        TextButton(
                            modifier = Modifier.heightIn(min = 48.dp),
                            onClick = onDismissBlockedRoute,
                        ) {
                            Text("知道了")
                        }
                    }
                }
            }
        }
        if (state == null) {
            item {
                BrainyPalStatePane(
                    state = BrainyPalContentState.Error,
                    title = "今天的内容暂时没取到",
                    detail = "可以重新试一次；已经完成的内容不会受影响。",
                    primaryActionLabel = "重新加载",
                    onPrimaryAction = onRefresh,
                )
            }
            return@LazyColumn
        }

        if (!state.workbench.configured) {
            item {
                BrainyPalStatePane(
                    state = BrainyPalContentState.Restricted,
                    title = "请家长检查连接",
                    detail = "检查好以后，点这里再试一次。",
                    primaryActionLabel = "重新加载",
                    onPrimaryAction = onRefresh,
                )
            }
            return@LazyColumn
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "今天想先做哪一件？",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = "可以问一个问题，也可以从今天的练习继续。",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
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
                    onAccept = { onNavigate(state.workbench.reviewAction.target) },
                    onDismiss = onDismissReview,
                )
            }
        }

        if (!errorMessage.isNullOrBlank()) {
            item {
                BrainyPalStatePane(
                    state = BrainyPalContentState.Offline,
                    title = "练习列表暂时没更新",
                    detail = "问一问仍然可以使用，也可以重新加载练习。",
                    primaryActionLabel = "重新加载",
                    onPrimaryAction = onRefresh,
                )
            }
        } else if (state.practiceTasks.isEmpty()) {
            item {
                BrainyPalStatePane(
                    state = BrainyPalContentState.Empty,
                    title = "现在没有新的练习",
                    detail = "可以先去问一个问题，或者晚一点再回来看看。",
                    primaryActionLabel = state.workbench.chatAction.label,
                    onPrimaryAction = { onNavigate(state.workbench.chatAction.target) },
                )
            }
        } else {
            item {
                CardGroup(title = { Text("今日练习") }) {
                    state.practiceTasks.take(3).forEach { task ->
                        practiceTaskSummaryItem(task = task, onClick = { onNavigate(Screen.BrainyPalPractice) })
                    }
                }
            }
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
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
) {
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "想复习一小步吗？",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
            )
            Text(text = message, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "可以现在做，也可以这次先跳过。",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            FilledTonalButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                onClick = onAccept,
            ) {
                Icon(HugeIcons.Book03, null)
                Text(
                    text = "复习一下",
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            TextButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                onClick = onDismiss,
            ) {
                Text(text = "这次先跳过")
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

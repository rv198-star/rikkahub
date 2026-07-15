package me.rerere.rikkahub.brainypal.child

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.Book03
import me.rerere.hugeicons.stroke.Refresh03
import me.rerere.hugeicons.stroke.ServerStack01
import me.rerere.hugeicons.stroke.Sparkles
import me.rerere.rikkahub.brainypal.child.theme.BrainyPalChildTheme
import me.rerere.rikkahub.brainypal.shared.components.BrainyPalSignalMark
import me.rerere.rikkahub.brainypal.shared.theme.BrainyPalTokens
import me.rerere.rikkahub.ui.components.nav.BackButton
import me.rerere.rikkahub.utils.UiState
import me.rerere.rikkahub.utils.plus
import org.koin.androidx.compose.koinViewModel

@Composable
fun BrainyPalStationPage(vm: BrainyPalHomeVM = koinViewModel()) {
    val state by vm.stationState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.refreshStation()
    }

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(BrainyPalTokens.stationFullName) },
                navigationIcon = { BackButton() },
                actions = {
                    IconButton(onClick = { vm.refreshStation() }) {
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
            UiState.Idle,
            UiState.Loading -> {
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
                StationError(
                    innerPadding = innerPadding,
                    message = current.error.message ?: "暂时连不上 BrainyPal，可以稍后再看空间站",
                    onRefresh = { vm.refreshStation() },
                )
            }

            is UiState.Success -> {
                StationContent(
                    innerPadding = innerPadding,
                    display = BrainyPalStationUiModel.from(current.data),
                )
            }
        }
    }
}

@Composable
private fun StationContent(
    innerPadding: PaddingValues,
    display: BrainyPalStationDisplay,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = innerPadding + PaddingValues(BrainyPalChildTheme.pagePadding),
        verticalArrangement = Arrangement.spacedBy(BrainyPalChildTheme.sectionSpacing),
    ) {
        item {
            StationHeroCard(display = display)
        }

        item {
            SectionTitle(
                title = "技能天梯",
                subtitle = "每个模块只记录愿意继续的一小步",
            )
        }

        items(display.modules, key = { it.moduleId }) { module ->
            SkillLadderModuleCard(module = module)
        }

        item {
            SectionTitle(
                title = "成就历史",
                subtitle = "可以回看最近被空间站接收到的稳定信号",
            )
        }

        if (display.history.isEmpty()) {
            item {
                EmptyHistoryCard(text = display.historyEmptyText)
            }
        } else {
            items(display.history, key = { it.eventId }) { item ->
                StationHistoryCard(item = item)
            }
        }
    }
}

@Composable
private fun StationHeroCard(display: BrainyPalStationDisplay) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BrainyPalSignalMark(size = 42.dp)
                StationSignalDot(motion = display.motion)
            }
            Text(
                text = display.title,
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = display.subtitle,
                style = MaterialTheme.typography.bodyMedium,
            )
            HorizontalDivider(
                color = BrainyPalChildTheme.heroContent.copy(
                    alpha = BrainyPalChildTheme.signalContainerAlpha,
                ),
            )
            Text(
                text = display.dailyHeadline,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = display.dailyBody,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun StationSignalDot(motion: BrainyPalStationMotion) {
    val transition = rememberInfiniteTransition(label = "station_signal")
    val pulse by transition.animateFloat(
        initialValue = 0.86f,
        targetValue = if (motion.pulse == BrainyPalStationPulse.IDLE) 0.94f else 1.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(motion.durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "station_signal_scale",
    )
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(stationToneColor(motion.visualTone).copy(alpha = BrainyPalChildTheme.signalContainerAlpha)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .scale(pulse)
                .clip(CircleShape)
                .background(stationToneColor(motion.visualTone))
        )
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = subtitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun SkillLadderModuleCard(module: BrainyPalStationModuleDisplay) {
    val animatedProgress by animateFloatAsState(
        targetValue = module.progress,
        animationSpec = tween(500),
        label = "station_module_progress",
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ModuleSignal(motion = module.motion)
                    Text(
                        text = module.label,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Text(
                    text = "${module.completedRungCount}/${module.totalRungCount}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            StationProgressBar(
                progress = animatedProgress,
                modifier = Modifier.fillMaxWidth(),
                color = stationToneColor(module.visualTone),
            )
            Text(
                text = "当前：${module.currentRungLabel}",
                style = MaterialTheme.typography.bodyMedium,
            )
            AnimatedVisibility(visible = module.nextRungHint != null) {
                Text(
                    text = module.nextRungHint.orEmpty(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (module.rungLabels.isNotEmpty()) {
                Text(
                    text = module.rungLabels.joinToString(" · "),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun StationProgressBar(
    progress: Float,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(6.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(6.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(color)
        )
    }
}

@Composable
private fun ModuleSignal(motion: BrainyPalStationMotion) {
    val color = stationToneColor(motion.visualTone)
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = HugeIcons.Sparkles,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun stationToneColor(tone: BrainyPalStationVisualTone): androidx.compose.ui.graphics.Color {
    return when (tone) {
        BrainyPalStationVisualTone.REPAIR_AMBER -> BrainyPalChildTheme.amberAccent
        BrainyPalStationVisualTone.STEADY_CYAN -> BrainyPalChildTheme.cyanAccent
        BrainyPalStationVisualTone.QUIET -> MaterialTheme.colorScheme.outline
    }
}

@Composable
private fun StationHistoryCard(item: BrainyPalStationHistoryDisplay) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = HugeIcons.Book03,
                contentDescription = null,
                tint = stationToneColor(item.visualTone),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = item.body,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "${item.moduleLabel} · ${item.sourceLabel}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun EmptyHistoryCard(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = HugeIcons.Sparkles,
                contentDescription = null,
                tint = BrainyPalChildTheme.cyanAccent,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun StationError(
    innerPadding: PaddingValues,
    message: String,
    onRefresh: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = HugeIcons.ServerStack01,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
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
                        text = "刷新空间站",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}

package me.rerere.rikkahub.brainypal.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.rerere.rikkahub.brainypal.parent.theme.BrainyPalParentTheme

@Composable
fun BrainyPalParentWeeklySummaryCard(
    card: BrainyPalParentAchievementWeeklySummaryCard,
    busy: Boolean,
    error: Boolean,
    onPrefillStrategy: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDetails by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BrainyPalParentTheme.workbenchPanel),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = card.title,
                style = MaterialTheme.typography.titleMedium,
                color = BrainyPalParentTheme.workbenchInk,
            )
            Text(
                text = if (error) "暂时没有读到周总结" else card.headline,
                style = MaterialTheme.typography.titleSmall,
                color = BrainyPalParentTheme.signal,
            )
            Text(
                text = if (error) "可以稍后刷新。任务状态和作业导入不受影响。" else card.body,
                style = MaterialTheme.typography.bodyMedium,
                color = BrainyPalParentTheme.workbenchMuted,
            )

            if (!error && card.trendRows.isNotEmpty()) {
                card.trendRows.forEachIndexed { index, row ->
                    if (index > 0) HorizontalDivider()
                    WeeklyTrendRow(row)
                }
            }

            Text(
                text = card.privacyLabel,
                style = MaterialTheme.typography.bodySmall,
                color = BrainyPalParentTheme.signal,
            )

            if (!error && card.suggestedWording.isNotEmpty()) {
                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp),
                    enabled = !busy,
                    onClick = { showDetails = true },
                ) {
                    Text(card.primaryActionLabel ?: "查看建议话术")
                }
            }

            if (!error && card.strategyActionLabel != null && card.strategyCandidateText != null) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp),
                    enabled = !busy,
                    onClick = { onPrefillStrategy(card.strategyCandidateText) },
                ) {
                    Text(card.strategyActionLabel)
                }
            }
        }
    }

    if (showDetails) {
        WeeklySummaryDetailDialog(
            card = card,
            onDismiss = { showDetails = false },
            onPrefillStrategy = onPrefillStrategy,
        )
    }
}

@Composable
private fun WeeklyTrendRow(row: BrainyPalParentAchievementTrendRow) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = row.categoryLabel,
                style = MaterialTheme.typography.titleSmall,
                color = BrainyPalParentTheme.workbenchInk,
            )
            Text(
                text = row.countLabel,
                style = MaterialTheme.typography.labelMedium,
                color = BrainyPalParentTheme.signal,
            )
        }
        Text(
            text = row.statusLabel,
            style = MaterialTheme.typography.labelMedium,
            color = BrainyPalParentTheme.signal,
        )
        Text(
            text = row.summary,
            style = MaterialTheme.typography.bodySmall,
            color = BrainyPalParentTheme.workbenchMuted,
        )
    }
}

@Composable
private fun WeeklySummaryDetailDialog(
    card: BrainyPalParentAchievementWeeklySummaryCard,
    onDismiss: () -> Unit,
    onPrefillStrategy: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("建议话术") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (card.suggestedWording.isEmpty()) {
                    Text(
                        text = "还没有可直接使用的话术。先完成几次任务后，再看稳定趋势。",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    card.suggestedWording.forEachIndexed { index, line ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "话术 ${index + 1}",
                                style = MaterialTheme.typography.labelLarge,
                                color = BrainyPalParentTheme.signal,
                            )
                            Text(text = line, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                card.strategyCandidateText?.let { strategy ->
                    HorizontalDivider()
                    Text(
                        text = "候选策略",
                        style = MaterialTheme.typography.labelLarge,
                        color = BrainyPalParentTheme.signal,
                    )
                    Text(text = strategy, style = MaterialTheme.typography.bodyMedium)
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onPrefillStrategy(strategy) },
                    ) {
                        Text(card.strategyActionLabel ?: "带入策略页确认")
                    }
                }
                Text(
                    text = card.privacyLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = BrainyPalParentTheme.signal,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
    )
}

package me.rerere.rikkahub.brainypal.child

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.Book03
import me.rerere.hugeicons.stroke.FloppyDisk
import me.rerere.hugeicons.stroke.Mic01
import me.rerere.hugeicons.stroke.Tick01
import me.rerere.rikkahub.brainypal.child.theme.BrainyPalChildTheme

@Composable
internal fun BrainyPalOralTaskCard(
    model: BrainyPalOralTaskUiModel,
    interactionPlan: BrainyPalChildTaskInteractionPlan,
    draft: BrainyPalPracticeDraft?,
    recordingModel: BrainyPalOralRecordingUiModel,
    actionInProgress: Boolean,
    message: String,
    onListen: () -> Unit,
    onEnterSentencePractice: () -> Unit,
    onRevealSentence: () -> Unit,
    onNextSentence: () -> Unit,
    onExitSentencePractice: () -> Unit,
    onEnterCheck: () -> Unit,
    onToggleSourcePeek: () -> Unit,
    onMarkDone: () -> Unit,
    onToggleRecording: () -> Unit,
    onUpdateDraft: (String, String) -> Unit,
    onSave: (String, String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = message.ifBlank { model.hint },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            ArticleBlock(model = model)

            when (model.phase) {
                BrainyPalOralTaskPhase.Reading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilledTonalButton(
                            modifier = Modifier.weight(1f),
                            enabled = !actionInProgress,
                            onClick = onListen,
                        ) {
                            Text("听一遍")
                        }
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            enabled = !actionInProgress,
                            onClick = onEnterSentencePractice,
                        ) {
                            Text("逐句练")
                        }
                    }
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        enabled = !actionInProgress,
                        onClick = onEnterCheck,
                    ) {
                        Text(model.primaryActionLabel)
                    }
                }

                BrainyPalOralTaskPhase.SentencePractice -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilledTonalButton(
                            modifier = Modifier.weight(1f),
                            enabled = !actionInProgress,
                            onClick = onRevealSentence,
                        ) {
                            Text(if (model.lines.any { it.isActive && it.isMasked }) "看这句" else "遮住这句")
                        }
                        FilledTonalButton(
                            modifier = Modifier.weight(1f),
                            enabled = !actionInProgress,
                            onClick = onNextSentence,
                        ) {
                            Text("下一句")
                        }
                    }
                    OutlinedButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 46.dp),
                        enabled = !actionInProgress,
                        onClick = onExitSentencePractice,
                    ) {
                        Text("退出逐句练")
                    }
                }

                BrainyPalOralTaskPhase.Check -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilledTonalButton(
                            modifier = Modifier.weight(1f),
                            enabled = !actionInProgress,
                            onClick = onListen,
                        ) {
                            Text("听一遍")
                        }
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            enabled = !actionInProgress,
                            onClick = onToggleSourcePeek,
                        ) {
                            Text(if (model.showSourcePeek) "收起原文" else "看一眼原文")
                        }
                    }
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        enabled = !actionInProgress,
                        onClick = onMarkDone,
                    ) {
                        Icon(HugeIcons.Tick01, null)
                        Text(
                            text = model.completeActionLabel,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                    OutlinedButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 46.dp),
                        enabled = !actionInProgress,
                        onClick = onToggleRecording,
                    ) {
                        Icon(HugeIcons.Mic01, null)
                        Text(
                            text = recordingModel.actionLabel,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                    Text(
                        text = recordingModel.supportingText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (model.showReflection) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "完成后简单记一下",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = draft?.answer.orEmpty(),
                        onValueChange = { onUpdateDraft(it, draft?.evidence.orEmpty()) },
                        enabled = !actionInProgress,
                        label = { Text(interactionPlan.answerLabel) },
                        minLines = 1,
                        maxLines = 2,
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
                            text = "保存这次复盘",
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleBlock(model: BrainyPalOralTaskUiModel) {
    val containerShape = RoundedCornerShape(24.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, containerShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, containerShape)
            .padding(14.dp),
    ) {
        if (model.phase == BrainyPalOralTaskPhase.Check && !model.showSourcePeek) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "原文已收起",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "准备好了再试，想看时点一下原文。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                model.lines.forEach { line ->
                    val lineBackground = when {
                        line.isActive -> BrainyPalChildTheme.signalContainer.copy(alpha = 0.55f)
                        else -> Color.Transparent
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(lineBackground, RoundedCornerShape(18.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Text(
                            text = if (line.isMasked) maskedLineText(line.rawText) else line.visibleText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (line.isMasked) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        )
                    }
                }
            }
        }
    }
}

private fun maskedLineText(rawText: String): String {
    val length = rawText.count { !it.isWhitespace() }.coerceAtLeast(6)
    return "•".repeat(length)
}

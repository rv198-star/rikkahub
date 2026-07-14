package me.rerere.rikkahub.ui.brainypal.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import me.rerere.rikkahub.ui.brainypal.designsystem.BrainyPalContentState
import me.rerere.rikkahub.ui.brainypal.designsystem.BrainyPalSizes
import me.rerere.rikkahub.ui.brainypal.designsystem.BrainyPalSpacing

@Composable
fun BrainyPalStatePane(
    state: BrainyPalContentState,
    title: String,
    detail: String,
    modifier: Modifier = Modifier,
    primaryActionLabel: String? = null,
    onPrimaryAction: (() -> Unit)? = null,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite }
            .padding(BrainyPalSpacing.Xl),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(BrainyPalSpacing.Md),
    ) {
        if (state == BrainyPalContentState.Loading || state == BrainyPalContentState.Processing) {
            CircularProgressIndicator()
        }
        Text(
            text = state.accessibleLabel,
            color = state.contentColor(),
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = title,
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = detail,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
        if (primaryActionLabel != null && onPrimaryAction != null) {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = BrainyPalSizes.MinimumTouchTarget),
                onClick = onPrimaryAction,
            ) {
                Text(primaryActionLabel)
            }
        }
        if (secondaryActionLabel != null && onSecondaryAction != null) {
            FilledTonalButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = BrainyPalSizes.MinimumTouchTarget),
                onClick = onSecondaryAction,
            ) {
                Text(secondaryActionLabel)
            }
        }
    }
}

val BrainyPalContentState.accessibleLabel: String
    get() = when (this) {
        BrainyPalContentState.Loading -> "正在加载"
        BrainyPalContentState.Empty -> "暂无内容"
        BrainyPalContentState.Error -> "需要处理"
        BrainyPalContentState.Offline -> "当前离线"
        BrainyPalContentState.Processing -> "正在处理"
        BrainyPalContentState.Restricted -> "此处受保护"
    }

@Composable
private fun BrainyPalContentState.contentColor() = when (this) {
    BrainyPalContentState.Error -> MaterialTheme.colorScheme.error
    BrainyPalContentState.Offline,
    BrainyPalContentState.Restricted -> MaterialTheme.colorScheme.tertiary
    BrainyPalContentState.Loading,
    BrainyPalContentState.Empty,
    BrainyPalContentState.Processing -> MaterialTheme.colorScheme.primary
}

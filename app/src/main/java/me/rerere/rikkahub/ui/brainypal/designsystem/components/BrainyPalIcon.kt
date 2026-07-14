package me.rerere.rikkahub.ui.brainypal.designsystem.components

import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import me.rerere.rikkahub.ui.brainypal.designsystem.BrainyPalSizes

@Composable
fun BrainyPalIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = BrainyPalSizes.IconDefault,
    decorative: Boolean = false,
) {
    require(decorative || !contentDescription.isNullOrBlank()) {
        "Interactive or meaningful BrainyPal icons require a content description"
    }
    Icon(
        imageVector = imageVector,
        contentDescription = if (decorative) null else contentDescription,
        modifier = modifier.then(Modifier.size(size)),
    )
}

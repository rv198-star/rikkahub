package me.rerere.rikkahub.brainypal.shared.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.rerere.rikkahub.R

@Composable
fun BrainyPalSignalMark(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    val useDarkSurfaceAsset = MaterialTheme.colorScheme.background.luminance() < 0.45f
    val imageRes = if (useDarkSurfaceAsset) {
        R.drawable.brainypal_app_icon
    } else {
        R.drawable.brainypal_signal_mark_light
    }
    Image(
        painter = painterResource(id = imageRes),
        contentDescription = "BrainyPal",
        modifier = modifier.size(size),
        contentScale = ContentScale.Fit,
    )
}

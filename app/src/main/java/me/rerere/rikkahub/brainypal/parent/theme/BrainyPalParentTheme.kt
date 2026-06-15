package me.rerere.rikkahub.brainypal.parent.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import me.rerere.rikkahub.brainypal.shared.theme.BrainyPalTokens

object BrainyPalParentTheme {
    val workbenchPanel = BrainyPalTokens.parentPanel
    val workbenchInk = BrainyPalTokens.parentInk
    val workbenchMuted = BrainyPalTokens.parentMuted
    val primary = BrainyPalTokens.orbitPrimary
    val signal = BrainyPalTokens.signalCyan
    val amber = BrainyPalTokens.solarAmber
    val quietSurface = Color(0xFFFFFFFF)
    val quietSurfaceDark = BrainyPalTokens.deepSpacePanel

    @Composable
    fun topAppBarColors() = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.background,
        scrolledContainerColor = MaterialTheme.colorScheme.background,
    )
}

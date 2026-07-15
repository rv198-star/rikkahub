package me.rerere.rikkahub.brainypal.child.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import me.rerere.rikkahub.brainypal.shared.theme.BrainyPalTokens

object BrainyPalChildTheme {
    val orbitPaper = BrainyPalTokens.orbitPaper
    val deepSpace = BrainyPalTokens.deepSpace
    val orbitPrimary = BrainyPalTokens.orbitPrimary
    val signalCyan = BrainyPalTokens.signalCyan
    val solarAmber = BrainyPalTokens.solarAmber
    val reviewCoral = BrainyPalTokens.reviewCoral
    val clearMint = BrainyPalTokens.clearMint
    val orbitPanel = BrainyPalTokens.orbitPanel
    val orbitText = BrainyPalTokens.ink
    val orbitMuted = BrainyPalTokens.mutedInk
    val orbitLine = BrainyPalTokens.orbitLine

    val cyanAccent = signalCyan
    val amberAccent = solarAmber
    val amberText = Color(0xFF8A5200)
    val heroContainer = orbitPanel
    val heroContent = orbitText
    val signalContainer = Color(0xFFD7F7FB)
    val gentleFocusContainer = Color(0xFFFFE9B8)

    val pagePadding = BrainyPalTokens.pagePadding
    val sectionSpacing = BrainyPalTokens.sectionSpacing
    val heroPadding = BrainyPalTokens.heroPadding
    val signalContainerAlpha = BrainyPalTokens.signalContainerAlpha

    val lightColorScheme: ColorScheme = androidx.compose.material3.lightColorScheme(
        primary = orbitPrimary,
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = heroContainer,
        onPrimaryContainer = heroContent,
        secondary = signalCyan,
        onSecondary = Color(0xFF062E35),
        secondaryContainer = signalContainer,
        onSecondaryContainer = Color(0xFF063E48),
        tertiary = amberText,
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = gentleFocusContainer,
        onTertiaryContainer = Color(0xFF6B3A00),
        error = reviewCoral,
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF7C1E10),
        background = orbitPaper,
        onBackground = orbitText,
        surface = orbitPaper,
        onSurface = orbitText,
        surfaceVariant = Color(0xFFE7EDF7),
        onSurfaceVariant = orbitMuted,
        outline = Color(0xFF8090AA),
        outlineVariant = orbitLine,
        scrim = Color(0xFF000000),
        inverseSurface = deepSpace,
        inverseOnSurface = Color(0xFFF8FAFC),
        inversePrimary = Color(0xFF86A8FF),
        surfaceDim = Color(0xFFDCE6F5),
        surfaceBright = Color(0xFFFFFFFF),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFFFFFFF),
        surfaceContainer = orbitPaper,
        surfaceContainerHigh = Color(0xFFE9F1FF),
        surfaceContainerHighest = orbitPanel,
    )

    val darkColorScheme: ColorScheme = androidx.compose.material3.darkColorScheme(
        primary = Color(0xFF86A8FF),
        onPrimary = Color(0xFF001B5C),
        primaryContainer = Color(0xFF193B9A),
        onPrimaryContainer = orbitPanel,
        secondary = Color(0xFF49D5E8),
        onSecondary = Color(0xFF00363F),
        secondaryContainer = Color(0xFF064F59),
        onSecondaryContainer = signalContainer,
        tertiary = Color(0xFFFFD36E),
        onTertiary = Color(0xFF4A2B00),
        tertiaryContainer = Color(0xFF5F4100),
        onTertiaryContainer = Color(0xFFFFECC2),
        error = Color(0xFFFF9A7B),
        onError = Color(0xFF5E1609),
        errorContainer = Color(0xFF7D2A19),
        onErrorContainer = Color(0xFFFFDAD6),
        background = deepSpace,
        onBackground = Color(0xFFE8EFFA),
        surface = deepSpace,
        onSurface = Color(0xFFE8EFFA),
        surfaceVariant = Color(0xFF25354C),
        onSurfaceVariant = Color(0xFFB8C4D8),
        outline = Color(0xFF8190A6),
        outlineVariant = Color(0xFF25354C),
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFFE8EFFA),
        inverseOnSurface = orbitText,
        inversePrimary = orbitPrimary,
        surfaceDim = deepSpace,
        surfaceBright = Color(0xFF18263A),
        surfaceContainerLowest = Color(0xFF030A14),
        surfaceContainerLow = Color(0xFF0E1A2B),
        surfaceContainer = deepSpace,
        surfaceContainerHigh = Color(0xFF16243A),
        surfaceContainerHighest = Color(0xFF1F3048),
    )

    fun colorScheme(dark: Boolean): ColorScheme = if (dark) darkColorScheme else lightColorScheme

    @Composable
    fun topAppBarColors() = TopAppBarDefaults.topAppBarColors(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        scrolledContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
    )
}

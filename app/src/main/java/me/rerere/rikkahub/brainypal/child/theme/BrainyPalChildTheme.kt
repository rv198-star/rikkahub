package me.rerere.rikkahub.brainypal.child.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object BrainyPalChildTheme {
    val cyanAccent = Color(0xFF06B6D4)
    val amberAccent = Color(0xFFF59E0B)
    val amberText = Color(0xFF8A5200)
    val heroContainer = Color(0xFFEAF0FF)
    val heroContent = Color(0xFF102045)

    val lightColorScheme: ColorScheme = androidx.compose.material3.lightColorScheme(
        primary = Color(0xFF172554),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = heroContainer,
        onPrimaryContainer = heroContent,
        secondary = Color(0xFF006B7A),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD7F6FB),
        onSecondaryContainer = Color(0xFF063E48),
        tertiary = amberText,
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFF1D6),
        onTertiaryContainer = Color(0xFF6B3A00),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF93000A),
        background = Color(0xFFF5F7FC),
        onBackground = Color(0xFF111827),
        surface = Color(0xFFF5F7FC),
        onSurface = Color(0xFF111827),
        surfaceVariant = Color(0xFFE2E8F0),
        onSurfaceVariant = Color(0xFF475569),
        outline = Color(0xFF64748B),
        outlineVariant = Color(0xFFCBD5E1),
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFF1F2937),
        inverseOnSurface = Color(0xFFF8FAFC),
        inversePrimary = Color(0xFFB7C7FF),
        surfaceDim = Color(0xFFDDE5EF),
        surfaceBright = Color(0xFFFFFFFF),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF0F4FA),
        surfaceContainer = Color(0xFFF5F7FC),
        surfaceContainerHigh = Color(0xFFEAF0F7),
        surfaceContainerHighest = Color(0xFFDCE5F0),
    )

    val darkColorScheme: ColorScheme = androidx.compose.material3.darkColorScheme(
        primary = Color(0xFFB7C7FF),
        onPrimary = Color(0xFF001A4D),
        primaryContainer = Color(0xFF1E3A8A),
        onPrimaryContainer = Color(0xFFEAF0FF),
        secondary = Color(0xFF7DE3F4),
        onSecondary = Color(0xFF00363F),
        secondaryContainer = Color(0xFF074E5A),
        onSecondaryContainer = Color(0xFFD7F6FB),
        tertiary = Color(0xFFF6C86C),
        onTertiary = Color(0xFF4A2B00),
        tertiaryContainer = Color(0xFF704000),
        onTertiaryContainer = Color(0xFFFFF1D6),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF08111F),
        onBackground = Color(0xFFE2E8F0),
        surface = Color(0xFF08111F),
        onSurface = Color(0xFFE2E8F0),
        surfaceVariant = Color(0xFF334155),
        onSurfaceVariant = Color(0xFFCBD5E1),
        outline = Color(0xFF94A3B8),
        outlineVariant = Color(0xFF334155),
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFFE2E8F0),
        inverseOnSurface = Color(0xFF111827),
        inversePrimary = Color(0xFF172554),
        surfaceDim = Color(0xFF08111F),
        surfaceBright = Color(0xFF1E293B),
        surfaceContainerLowest = Color(0xFF030712),
        surfaceContainerLow = Color(0xFF0F172A),
        surfaceContainer = Color(0xFF08111F),
        surfaceContainerHigh = Color(0xFF1E293B),
        surfaceContainerHighest = Color(0xFF273549),
    )

    fun colorScheme(dark: Boolean): ColorScheme = if (dark) darkColorScheme else lightColorScheme

    @Composable
    fun topAppBarColors() = TopAppBarDefaults.topAppBarColors(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        scrolledContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
    )
}

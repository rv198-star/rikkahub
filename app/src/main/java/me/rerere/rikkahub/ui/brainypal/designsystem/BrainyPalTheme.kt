package me.rerere.rikkahub.ui.brainypal.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BrainyPalLightColors = lightColorScheme(
    primary = Color(0xFFB7462B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFCEBE5),
    onPrimaryContainer = Color(0xFF762711),
    secondary = Color(0xFF176B5B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE4F2ED),
    onSecondaryContainer = Color(0xFF074D40),
    tertiary = Color(0xFF2463A6),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE4EFFB),
    onTertiaryContainer = Color(0xFF153E6A),
    error = Color(0xFFB42318),
    onError = Color.White,
    errorContainer = Color(0xFFFDE7E5),
    onErrorContainer = Color(0xFF7A1710),
    background = Color(0xFFF7F8F5),
    onBackground = Color(0xFF17211E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF17211E),
    surfaceVariant = Color(0xFFEEF2EF),
    onSurfaceVariant = Color(0xFF5D6B66),
    outline = Color(0xFF697771),
    outlineVariant = Color(0xFFD8E0DC),
)

private val BrainyPalDarkColors = darkColorScheme(
    primary = Color(0xFFF18C6D),
    onPrimary = Color(0xFF571707),
    primaryContainer = Color(0xFF752C1A),
    onPrimaryContainer = Color(0xFFFFDAD0),
    secondary = Color(0xFF6CC4AD),
    onSecondary = Color(0xFF00382E),
    secondaryContainer = Color(0xFF155B4D),
    onSecondaryContainer = Color(0xFFB7EEDD),
    tertiary = Color(0xFF9DCBFF),
    onTertiary = Color(0xFF003258),
    tertiaryContainer = Color(0xFF164C78),
    onTertiaryContainer = Color(0xFFD0E4FF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF111614),
    onBackground = Color(0xFFF3F6F4),
    surface = Color(0xFF18201D),
    onSurface = Color(0xFFF3F6F4),
    surfaceVariant = Color(0xFF222C28),
    onSurfaceVariant = Color(0xFFAAB8B2),
    outline = Color(0xFF8D9B95),
    outlineVariant = Color(0xFF34403B),
)

private val BrainyPalTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        letterSpacing = 0.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
    ),
)

private val BrainyPalShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(8.dp),
)

@Composable
fun BrainyPalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) BrainyPalDarkColors else BrainyPalLightColors,
        typography = BrainyPalTypography,
        shapes = BrainyPalShapes,
        content = content,
    )
}

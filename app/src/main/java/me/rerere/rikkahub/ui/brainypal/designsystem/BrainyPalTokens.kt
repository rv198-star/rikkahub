package me.rerere.rikkahub.ui.brainypal.designsystem

import androidx.compose.ui.unit.dp

object BrainyPalSpacing {
    val Xs = 4.dp
    val Sm = 8.dp
    val Md = 12.dp
    val Lg = 16.dp
    val Xl = 24.dp
    val Xxl = 32.dp
    val Xxxl = 48.dp
}

object BrainyPalSizes {
    val IconSmall = 16.dp
    val IconCompact = 20.dp
    val IconDefault = 24.dp
    val IconLarge = 32.dp
    val MinimumTouchTarget = 48.dp
    val PhoneGutter = 16.dp
    val LargePhoneGutter = 20.dp
    val TabletGutter = 24.dp
    val TabletBreakpoint = 840.dp
}

object BrainyPalMotion {
    const val FastMillis = 120
    const val StandardMillis = 180
    const val EmphasisMillis = 240
}

enum class BrainyPalContentState {
    Loading,
    Empty,
    Error,
    Offline,
    Processing,
    Restricted,
}

package me.rerere.rikkahub.brainypal.shared.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.rerere.rikkahub.brainypal.shared.theme.BrainyPalTokens

@Composable
fun BrainyPalSignalMark(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    primary: Color = BrainyPalTokens.orbitPrimary,
    signal: Color = BrainyPalTokens.signalCyan,
    amber: Color = BrainyPalTokens.solarAmber,
) {
    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = this.size.minDimension * 0.13f
            val orbitStroke = this.size.minDimension * 0.035f
            val dotRadius = this.size.minDimension * 0.075f
            val center = Offset(this.size.width * 0.5f, this.size.height * 0.5f)
            drawArc(
                color = primary.copy(alpha = 0.42f),
                startAngle = 202f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(this.size.width * 0.08f, this.size.height * 0.14f),
                size = Size(this.size.width * 0.84f, this.size.height * 0.68f),
                style = Stroke(width = orbitStroke, cap = StrokeCap.Round),
            )
            drawArc(
                color = primary,
                startAngle = 180f,
                sweepAngle = 228f,
                useCenter = false,
                topLeft = Offset(this.size.width * 0.18f, this.size.height * 0.14f),
                size = Size(this.size.width * 0.54f, this.size.height * 0.54f),
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            drawArc(
                color = signal,
                startAngle = 35f,
                sweepAngle = 175f,
                useCenter = false,
                topLeft = Offset(this.size.width * 0.18f, this.size.height * 0.28f),
                size = Size(this.size.width * 0.66f, this.size.height * 0.54f),
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            drawCircle(
                color = primary,
                radius = dotRadius * 1.6f,
                center = center,
            )
            drawCircle(
                color = BrainyPalTokens.stationShell,
                radius = dotRadius * 0.82f,
                center = center,
            )
            drawCircle(
                color = amber,
                radius = dotRadius,
                center = Offset(this.size.width * 0.22f, this.size.height * 0.70f),
            )
            drawCircle(
                color = signal,
                radius = dotRadius * 1.05f,
                center = Offset(this.size.width * 0.34f, this.size.height * 0.82f),
            )
            drawCircle(
                color = primary,
                radius = dotRadius * 0.92f,
                center = Offset(this.size.width * 0.78f, this.size.height * 0.72f),
            )
        }
    }
}

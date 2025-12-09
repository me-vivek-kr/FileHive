package com.viv3k.filehive.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CircularProgressArc(
    size: Dp,
    progress: Float,
    stroke: Dp,
    modifier: Modifier = Modifier,
    animationDuration: Int = 1200,
    animationDelay: Int = 0
) {
    val target = progress.coerceIn(0f, 1f)
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(target) {
        animatedProgress.animateTo(
            targetValue = target,
            animationSpec = tween(
                durationMillis = animationDuration,
                delayMillis = animationDelay,
                easing = FastOutSlowInEasing
            )
        )
    }

    Canvas(modifier = modifier.size(size)) {
        val diameter = size.toPx()
        val strokePx = stroke.toPx()
        val radius = diameter / 2f
        val center = Offset(radius, radius)

        val arcRadius = radius - strokePx / 2f
        val topLeft = Offset(center.x - arcRadius, center.y - arcRadius)
        val arcSize = Size(arcRadius * 2, arcRadius * 2)

        val startAngle = -270f
        val rawSweep = 360f * animatedProgress.value

        val minSweepToDraw = 1f

        val sweepAngle = when {
            rawSweep <= minSweepToDraw -> 0f
            rawSweep >= 360f -> 359.9999f
            else -> rawSweep
        }

        // Background Circle
        drawArc(
            color = Color(0xFF2A2B2E),
            startAngle = startAngle,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokePx, cap = StrokeCap.Round),
            topLeft = topLeft,
            size = arcSize
        )

        if (sweepAngle > 0f) {
            val sweepBrush = Brush.sweepGradient(
                colors = listOf(Color(0xFF3e94a2), Color(0xFF2A2B2E)),
                center = center
            )


            withTransform({
                rotate(degrees = startAngle, pivot = center)
            }) {
                drawArc(
                    brush = sweepBrush,
                    startAngle = 0f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                    topLeft = topLeft,
                    size = arcSize
                )
            }

            // Draw knob at the arc end
            val endAngleRad = Math.toRadians((startAngle + sweepAngle).toDouble())
            val knobCenter = Offset(
                x = center.x + arcRadius * cos(endAngleRad).toFloat(),
                y = center.y + arcRadius * sin(endAngleRad).toFloat()
            )

            drawCircle(
                color = Color(0xFF171717),
                radius = strokePx * 1.3f,
                center = knobCenter
            )
            drawCircle(
                color = Color(0xFF449da8),
                radius = strokePx * 0.6f,
                center = knobCenter
            )
        }
    }
}



@Preview
@Composable
private fun CircularProgressArcPreview() {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        CircularProgressArc(
            size = 220.dp,
            stroke = 10.dp,
            progress = 0.64f
        )
    }
}

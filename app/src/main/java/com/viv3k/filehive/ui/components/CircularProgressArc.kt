package com.viv3k.filehive.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp

@Composable
fun CircularProgressArc(
    size: Dp,
    progress: Float,
    stroke: Dp
) {
    val clamped = progress.coerceIn(0f, 1f)
    val sweep = 360f * clamped

    Canvas(modifier = Modifier.size(size)) {
        val diameter = size.toPx()
        val thickness = stroke.toPx()

        // background circle
        drawArc(
            color = Color(0xFF2A2B2E),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = thickness, cap = StrokeCap.Round),
            topLeft = Offset(0f, 0f),
            size = Size(diameter, diameter)
        )

        // progress arc
        drawArc(
            brush = Brush.sweepGradient(
                listOf(Color(0xFF6C5CE7), Color(0xFF00B4D8))
            ),
            startAngle = -90f,
            sweepAngle = sweep,
            useCenter = false,
            style = Stroke(width = thickness, cap = StrokeCap.Round),
            topLeft = Offset(0f, 0f),
            size = Size(diameter, diameter)
        )
    }
}

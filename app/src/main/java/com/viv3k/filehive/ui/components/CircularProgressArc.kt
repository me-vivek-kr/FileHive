package com.viv3k.filehive.ui.components


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viv3k.filehive.R
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StorageUsageCard(
    size: Dp,
    stroke: Dp,
    used: Float,
    total: Float,
    modifier: Modifier = Modifier
) {
    val progress = used / total

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressArc(
            size = size,
            stroke = stroke,
            progress = progress
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.folder_new), // <-- add folder icon in drawable
                contentDescription = null,
                modifier = Modifier.size(34.dp)
            )
            Spacer(Modifier.height(6.dp))

            Text(
                text = "Used Storage",
                fontSize = 15.sp,
                color = Color(0xFF9FA0A2)
            )
            Spacer(Modifier.height(2.dp))

            Text(
                text = "${String.format("%.2f", used)} GB",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))

            Text(
                text = "of ${total.toInt()} GB",
                color = Color(0xFF757678),
                fontSize = 15.sp
            )
        }
    }
}

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
        val sweepAngle = rawSweep.coerceIn(0f, 360f)

        // Background arc (Keep this Round if you want rounded ends on the grey track)
        drawArc(
            color = Color(0x002A2B2E), // Transparent/Dark background
            startAngle = startAngle,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokePx, cap = StrokeCap.Round),
            topLeft = topLeft,
            size = arcSize
        )

        if (animatedProgress.value > 0f) {
            val gradientBrush = Brush.sweepGradient(
                colors = listOf(Color(0xFF0C0F15), Color(0xFF3e94a2)),
                center = center
            )

            withTransform({
                rotate(startAngle, center)
            }) {
                drawArc(
                    brush = gradientBrush,
                    startAngle = 0f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Butt),
                    topLeft = topLeft,
                    size = arcSize
                )
            }

            val endAngleRad = Math.toRadians((startAngle + sweepAngle).toDouble())
            val knobPos = Offset(
                x = center.x + arcRadius * cos(endAngleRad).toFloat(),
                y = center.y + arcRadius * sin(endAngleRad).toFloat()
            )

            // Draw the knob (Outer ring + Inner circle)
            drawCircle(Color(0xFF171717), strokePx * 1.4f, knobPos)
            drawCircle(Color(0xFF449da8), strokePx * 0.7f, knobPos)

        }
    }
}






@Preview(showBackground = true, backgroundColor = 0xFF0F0F10)
@Composable
fun PreviewFixed() {
    StorageUsageCard(
        size = 240.dp,
        stroke = 12.dp,
        used = 64.20f,
        total = 128f
    )
}

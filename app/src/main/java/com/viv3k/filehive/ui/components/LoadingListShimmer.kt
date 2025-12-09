package com.viv3k.filehive.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@Composable
fun LoadingListShimmer(
    rows: Int = 7,
    rowHeight: Dp = 72.dp,
    spacing: Dp = 4.dp
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        repeat(rows) {
            ShimmerRow(rowHeight)
            Divider(color = Color(0xFF1E2024))
        }
    }
}

@Composable
private fun ShimmerRow(height: Dp) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition()
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart)
    )

    // dark-theme tuned colors
    val base = Color(0xFF121314)
    val mid = Color(0xFF1A1C1F)
    val highlight = Color(0xFF2A2C30)

    // compute moving gradient across measured width
    val width = size.width.coerceAtLeast(1)
    val gradientWidth = (width * 0.5f).coerceAtLeast(40f)
    val startX = -gradientWidth + (width + gradientWidth * 2) * progress
    val endX = startX + gradientWidth

    val brush = Brush.linearGradient(
        colors = listOf(base, mid, highlight, mid, base),
        start = Offset(x = startX, y = 0f),
        end = Offset(x = endX, y = size.height.toFloat())
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .onSizeChanged { size = it }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // folder icon placeholder
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(brush)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // title & subtitle placeholders
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.45f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // timestamp placeholder
        Box(
            modifier = Modifier
                .width(90.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brush)
        )
    }
}
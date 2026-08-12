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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@Composable
fun LoadingShimmer(
    isGrid: Boolean,
    modifier: Modifier = Modifier
) {
    if (isGrid) {
        LoadingGridShimmer(modifier = modifier)
    } else {
        LoadingListShimmer(modifier = modifier)
    }
}

@Composable
fun LoadingListShimmer(
    rows: Int = 7,
    rowHeight: Dp = 80.dp,
    spacing: Dp = 12.dp,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(spacing),
        contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp, start = 8.dp, end = 8.dp)
    ) {
        repeat(rows) {
            item {
                ShimmerListRow(rowHeight)
            }
        }
    }
}

@Composable
fun LoadingGridShimmer(
    itemCount: Int = 8,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp, top = 18.dp, start = 20.dp, end = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        repeat(itemCount) {
            item {
                ShimmerGridCard()
            }
        }
    }
}

@Composable
private fun ShimmerListRow(height: Dp) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        color = Color.White,
        shape = RoundedCornerShape(50.dp),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShimmerBox(
                modifier = Modifier.size(56.dp),
                shape = CircleShape
            )

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .height(16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.44f)
                        .height(12.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            ShimmerBox(
                modifier = Modifier.size(28.dp),
                shape = CircleShape
            )
        }
    }
}

@Composable
private fun ShimmerGridCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(34.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(125.dp),
                shape = RoundedCornerShape(32.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(18.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.38f)
                        .height(10.dp)
                )

                ShimmerBox(
                    modifier = Modifier
                        .width(44.dp)
                        .height(10.dp)
                )
            }
        }
    }
}

@Composable
private fun ShimmerBox(
    modifier: Modifier,
    shape: Shape = RoundedCornerShape(50.dp)
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "folder-loading-shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart),
        label = "folder-loading-shimmer-progress"
    )

    val base = Color(0xFFE8EBF0)
    val mid = Color(0xFFF0F2F5)
    val highlight = Color(0xFFFAFBFC)

    val width = size.width.coerceAtLeast(1)
    val gradientWidth = (width * 0.5f).coerceAtLeast(40f)
    val startX = -gradientWidth + (width + gradientWidth * 2) * progress
    val endX = startX + gradientWidth

    val brush = Brush.linearGradient(
        colors = listOf(base, mid, highlight, mid, base),
        start = Offset(x = startX, y = 0f),
        end = Offset(x = endX, y = size.height.toFloat())
    )

    Box(
        modifier = modifier
            .onSizeChanged { size = it }
            .clip(shape)
            .background(brush)
    )
}
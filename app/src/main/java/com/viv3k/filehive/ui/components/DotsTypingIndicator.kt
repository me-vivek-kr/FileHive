package com.viv3k.filehive.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DotsTypingIndicator(
    modifier: Modifier = Modifier,
    dotCount: Int = 3,
    dotSize: Dp = 12.dp,
    dotSpacing: Dp = 12.dp,
    color: Color = Color(0xFF5C7A84),
    bounceDistance: Dp = 6.dp,
    animationDuration: Int = 600,
    delayBetweenDots: Int = 150
) {
    val density = LocalDensity.current
    val bouncePx = with(density) { bounceDistance.toPx() }

    val transition = rememberInfiniteTransition()

    Row(
        modifier = modifier.semantics { contentDescription = "Loading" },
        horizontalArrangement = Arrangement.spacedBy(dotSpacing)
    ) {
        for (i in 0 until dotCount) {
            // Use the public factory StartOffset(Int) to stagger each dot
            val anim = transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = animationDuration,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(i * delayBetweenDots)
                )
            )

            Surface(
                modifier = Modifier
                    .size(dotSize)
                    .graphicsLayer {
                        translationY = -anim.value * bouncePx
                        scaleX = 0.85f + 0.3f * anim.value
                        scaleY = 0.85f + 0.3f * anim.value
                    }
                    .alpha(0.6f + 0.4f * anim.value),
                shape = CircleShape,
                color = color
            ) {}
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewDotsTypingIndicator() {
    DotsTypingIndicator()
}

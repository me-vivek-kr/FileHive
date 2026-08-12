package com.viv3k.filehive.ui.utils

import android.graphics.Paint
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.viv3k.filehive.R

private val ChipShape = RoundedCornerShape(50)
private val DefaultSoftShape = RoundedCornerShape(32.dp)

@Composable
fun SoftButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    shape: Shape = ChipShape,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
// Shadow layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 6.dp, y = 6.dp)
                .blur(radius = 16.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                .background(Color(0x2D423357).copy(alpha = 0.4f), shape)
        )
// Background layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.White, shape)
        )
// Content
        content()
    }
}

fun Modifier.softCardShadow(
    color: Color = Color(0x2D423357),
    cornerRadius: Dp = 32.dp,
    blurRadius: Dp = 26.dp,
    offsetY: Dp = 10.dp
): Modifier = this.drawBehind {
    val shadowColorArgb = color.toArgb()
    val transparentArgb = color.copy(alpha = 0f).toArgb()

    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            this.color = transparentArgb
            setShadowLayer(
                blurRadius.toPx(),
                0f,
                offsetY.toPx(),
                shadowColorArgb
            )
        }
        canvas.nativeCanvas.drawRoundRect(
            0f,
            0f,
            size.width,
            size.height,
            cornerRadius.toPx(),
            cornerRadius.toPx(),
            paint
        )
    }


}

fun Modifier.soft(
    shape: Shape = DefaultSoftShape,
    backgroundColor: Color = Color(0xFFF4F6FA),
    shadowColor: Color = Color(0x2D423357),
    cornerRadius: Dp = 32.dp,
    blurRadius: Dp = 26.dp,
    offsetY: Dp = 10.dp,
    pressedScale: Float = 0.96f,
    interactionSource: MutableInteractionSource? = null
): Modifier = composed {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val isPressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "softScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .softCardShadow(
            color = shadowColor,
            cornerRadius = cornerRadius,
            blurRadius = blurRadius,
            offsetY = offsetY
        )
        .clip(shape)
        .background(backgroundColor, shape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.65f),
                    Color.Transparent
                )
            ),
            shape = shape
        )


}

fun Modifier.softClickable(
    onClick: () -> Unit,
    shape: Shape = DefaultSoftShape,
    backgroundColor: Color = Color(0xFFF4F6FA),
    shadowColor: Color = Color(0x2D423357),
    cornerRadius: Dp = 32.dp,
    blurRadius: Dp = 26.dp,
    offsetY: Dp = 10.dp,
    pressedScale: Float = 0.96f,
    enabled: Boolean = true
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }

    this
        .soft(
            shape = shape,
            backgroundColor = backgroundColor,
            shadowColor = shadowColor,
            cornerRadius = cornerRadius,
            blurRadius = blurRadius,
            offsetY = offsetY,
            pressedScale = pressedScale,
            interactionSource = interactionSource
        )
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )


}

@Composable
fun NeomorphicIcon(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFF4F6FA),
    size: Dp = 40.dp,
    icon: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .soft(
                shape = RoundedCornerShape(12.dp),
                cornerRadius = 12.dp,
                blurRadius = 8.dp,
                offsetY = 4.dp,
                backgroundColor = backgroundColor
            ),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

@Composable
fun NeomorphicSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackColor by animateColorAsState(
        targetValue = if (checked) Color(0xFF2D3282) else Color(0xFFD1D9E6),
        label = "trackColor"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 22.dp else 0.dp,
        label = "thumbOffset"
    )

    Box(
        modifier = modifier
            .width(50.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(trackColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(20.dp)
                .soft(
                    shape = RoundedCornerShape(50),
                    cornerRadius = 10.dp,
                    blurRadius = 4.dp,
                    offsetY = 2.dp,
                    backgroundColor = Color.White
                )
        )
    }
}

@Composable
fun NeomorphicViewSwitcher(
    isGrid: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val offset by animateDpAsState(targetValue = if (isGrid) 0.dp else 40.dp, label = "switcher")

    Box(
        modifier = Modifier
            .width(88.dp)
            .height(42.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFE8EBF0))
            .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .offset(x = offset)
                .size(40.dp)
                .shadow(elevation = 2.dp, shape = CircleShape)
                .background(Color.White, CircleShape)
        )

        Row(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggle(true) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.grid),
                    contentDescription = "Grid",
                    tint = if (isGrid) Color(0xFF5051D8) else Color(0xFF9AA0A6),
                    modifier = Modifier.size(22.dp)
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onToggle(false) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.list),
                    contentDescription = "List",
                    tint = if (!isGrid) Color(0xFF5051D8) else Color(0xFF9AA0A6),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun NeomorphicFilterButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.height(42.dp).width(52.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(R.drawable.ic_filter), // Replace with filter icon if available
                contentDescription = "Filter",
                tint = Color(0xFF5051D8),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun NeomorphicFAB(onClick: () -> Unit) {
    Box(
        modifier = Modifier.soft()
            .size(56.dp)
            .shadow(elevation = 8.dp, shape = CircleShape)
            .background(Color.White, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.plus),
            contentDescription = "Add",
            tint = Color(0xFF5051D8),
            modifier = Modifier.size(28.dp)
        )
    }
}
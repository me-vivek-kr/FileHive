package com.viv3k.filehive.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.viv3k.filehive.R

/**

- Neumorphic circular badge, corrected to match the target design:
-
-
- Background is a real diagonal gradient (white -> soft gray), not just an
- edge shadow. Modifier.blur() inner-shadow tricks do NOT render inside
- Android Studio's static @Preview canvas (RenderEffect needs a real
- device/emulator on API 31+), so relying on blur alone made the badge
- look flat in Preview even though it might eventually look right on a
- real device. An actual Brush.linearGradient fixes this everywhere,
- consistently, with zero API-level risk.
-
-
- Icon is gradient-filled (light periwinkle -> deep indigo), not a flat
- tint, matching the target clapperboard icon. Icon() only supports a
- single solid tint, so the icon is drawn once as an alpha mask and the
- gradient is composited on top with BlendMode.SrcAtop inside an
- offscreen graphicsLayer.
*/

private val BADGE_SIZE = 48.dp

private val BACKGROUND_GRADIENT = Brush.linearGradient(
colors = listOf(
Color(0xFFFFFFFF), // top-left: near white
Color(0xFFDDE0E3)  // bottom-right: soft gray (\~#E0E3E5 shaded)
)
)

private val ICON_GRADIENT = Brush.linearGradient(
colors = listOf(
Color(0xFF7B7FE3), // top-left: lighter periwinkle
Color(0xFF4648D4)  // bottom-right: deep indigo (Figma spec color)
)
)

@Composable
fun NeumorphicIconBadge(
    icon: Painter,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
    preserveIconColors: Boolean = false,
) {
    Box(
        modifier = modifier
            .size(BADGE_SIZE)
        .clip(CircleShape)
        .background(BACKGROUND_GRADIENT),
    contentAlignment = Alignment.Center
    ) {
        if (preserveIconColors) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(iconSize)
            )
        } else {
            GradientIcon(
                painter = icon,
                gradient = ICON_GRADIENT,
            modifier = Modifier.size(iconSize)
            )
        }
    }
}

/**

- Draws [painter] once (tinted white, so its shape becomes a solid alpha
- mask) inside an offscreen-composited layer, then paints [gradient] over it
- with BlendMode.SrcAtop so the gradient only shows where the icon has
- pixels — producing a true gradient-filled icon instead of a flat tint.
*/

@Composable
private fun GradientIcon(
    painter: Painter,
    gradient: Brush,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                drawRect(brush = gradient, blendMode = BlendMode.SrcAtop)
            }
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = Color.White, // acts as the alpha mask; replaced by the gradient above
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun NeumorphicIconBadgePreview() {
    Row(
        modifier = Modifier.padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        NeumorphicIconBadge(icon = painterResource(id = R.drawable.video))
        NeumorphicIconBadge(icon = painterResource(id = R.drawable.category_documents))
    }
}
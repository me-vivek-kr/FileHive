package com.viv3k.filehive.ui.screens.utils

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RoundedIconButton(
    icon: Int,
    onClick: () -> Unit,
    backgroundColor: Color = Color(0xFF1A1C21),
    iconTint: Color = Color.White,
    shape: Shape = CircleShape,
    elevation: Dp = 32.dp
) {
    Surface(
        shape = shape,
        color = backgroundColor,
        tonalElevation = elevation
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = iconTint
            )
        }
    }
}
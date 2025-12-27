package com.viv3k.filehive.ui.screens.utils

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun RoundedIconButton(
    icon: Int,
    onClick: () -> Unit,
    backgroundColor: Color = Color(0xFF1A1C21),
    iconTint: Color = Color.White
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        tonalElevation = 2.dp
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}
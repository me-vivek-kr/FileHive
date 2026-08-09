package com.viv3k.filehive.ui.components

import android.graphics.Color.alpha
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viv3k.filehive.ui.screens.homescreen.clickableNoRipple
import com.viv3k.filehive.ui.utils.soft
import com.viv3k.filehive.R

@Composable
fun StorageChip(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    iconRes: Int,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .soft(
                shape = RoundedCornerShape(32.dp),
                cornerRadius = 32.dp,
                backgroundColor = Color.White,
                blurRadius = 16.dp,
                offsetY = 6.dp
            )
            .clickableNoRipple { onClick() }
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
//            Box(
//                modifier = Modifier
//                    .size(48.dp)
//                    .soft(
//                        shape = CircleShape,
//                        cornerRadius = 24.dp,
//                        blurRadius = 10.dp,
//                        offsetY = 4.dp,
//                        backgroundColor = Color(0xFFF4F6FA),
//                        shadowColor = Color(0xFF6366F1).copy(alpha = 0.15f)
//                    ),
//                contentAlignment = Alignment.Center)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0x2C2D3281))
                    .border(
                        width = 1.dp,
                        brush = verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.1f),
                                Color.White.copy(alpha = 0.5f)
                            )
                        ),
                        shape = CircleShape
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            )
            {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = Color(0xFF2D3282),
                    modifier = Modifier.size(22.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewStorageChip(){
    StorageChip(
        title = "Internal Storage",
        subtitle = "128 GB Free",
        iconRes = R.drawable.list
    )
}
package com.viv3k.filehive.ui.screens.recyclebin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.viv3k.filehive.R

@Composable
fun RecycleConfirmationDialog(
    title: String,
    subtitle: String,
    message: String,
    confirmText: String,
    confirmColor: Color,
    iconRes: Int, // Resource ID for icon
    isWarning: Boolean, // Determines if the icon background is red or neutral
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF16181D), // Dark Dialog Background
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                // Header Row (Icon + Title)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Icon Circle
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isWarning) Color(0xFF2B1C1C) // Dark Red bg
                                else Color(0xFF1A2835) // Dark Blue bg
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // If you don't have R.drawable.alert_triangle, use Icons.Rounded.Warning
                        if (isWarning && iconRes == R.drawable.alert_triangle) {
                            Icon(
                                imageVector = Icons.Rounded.Warning,
                                contentDescription = null,
                                tint = Color(0xFFEF5350),
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = iconRes),
                                contentDescription = null,
                                tint = if (isWarning) Color(0xFFEF5350) else Color(0xFF29B6F6),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            color = Color(0xFF9AA0A6),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Body Message
                Text(
                    text = message,
                    color = Color(0xFFB0B3B8),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2B2D31), // Dark Grey
                            contentColor = Color.White
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Medium)
                    }

                    // Confirm Button (Delete or Restore)
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = confirmColor,
                            contentColor = Color.White
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(confirmText, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun previewRecycleDialog(){
    RecycleConfirmationDialog(
        title = "Empty Recycle Bin?",
        subtitle = "This action cannot be undone",
        message = "All 2 files will be permanently deleted.",
        confirmText = "Delete All",
        confirmColor = Color(0xFFD32F2F),
        iconRes = R.drawable.alert_triangle, // Or standard warning icon
        isWarning = true,
        onDismiss = {  },
        onConfirm = {  }
    )
}
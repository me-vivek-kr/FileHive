package com.viv3k.filehive.ui.screens.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog


@Composable
fun DeleteConfirmationDialog(
    fileName: String,
    onDismiss: () -> Unit,
    onConfirm: (isPermanent: Boolean) -> Unit,
) {
    // State to track if "Permanently delete" is checked
    var isPermanentDelete by remember { mutableStateOf(false) }

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
                            .background(Color(0xFF2B1C1C)), // Dark Red bg
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = null,
                            tint = Color(0xFFEF5350), // Red Tint
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Delete Item?",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isPermanentDelete) "Action cannot be undone" else "Move to Recycle Bin",
                            color = Color(0xFF9AA0A6),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Body Message
                Text(
                    text = "Are you sure you want to delete \"$fileName\"?",
                    color = Color(0xFFB0B3B8),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Permanent Delete Option Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                        .clickable { isPermanentDelete = !isPermanentDelete }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isPermanentDelete,
                        onCheckedChange = { isPermanentDelete = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFFEF5350),
                            uncheckedColor = Color(0xFF9AA0A6),
                            checkmarkColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Permanently delete",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2B2D31),
                            contentColor = Color.White
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Medium)
                    }

                    // Confirm Button
                    Button(
                        onClick = { onConfirm(isPermanentDelete) },
                        colors = ButtonDefaults.buttonColors(
                            // Change color based on severity (Red for permanent, Blue/Primary for recycle)
                            containerColor = if (isPermanentDelete) Color(0xFFD32F2F) else Color(0xFF29B6F6),
                            contentColor = Color.White
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = if (isPermanentDelete) "Delete" else "Move to Bin",
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewDeleteConfirmationDialog() {
    DeleteConfirmationDialog(
        fileName = "MyDocument.pdf",
        onDismiss = {},
        onConfirm = {}
    )
}
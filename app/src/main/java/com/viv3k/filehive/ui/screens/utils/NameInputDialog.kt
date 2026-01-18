package com.viv3k.filehive.ui.screens.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.viv3k.filehive.R

@Composable
fun NameInputDialog(
    title: String? = null,
    defaultName: String = "",
    isCreatingFolder: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(defaultName)}

    Dialog(onDismissRequest = onDismiss) {
        // Use a Box to allow the icon to overlap the top edge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp), // Space for the overlapping icon
            contentAlignment = Alignment.TopCenter
        ) {
            // Main Dialog Content
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color(0xFF16181D),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1A1C21))
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 50.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
//                    Text(
//                        text = if (isCreatingFolder) "Create New Folder" else "Create New File",
//                        color = Color.White,
//                        fontSize = 20.sp,
//                        fontWeight = FontWeight.Bold
//                    )
                    Text(
                        // If custom title provided use it, otherwise use default
                        text = title ?: if (isCreatingFolder) "Create New Folder" else "Create New File",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = {
                            Text("Enter folder name...",
                                color = Color.Gray,
                                fontSize = 12.sp
                            ) },
                        modifier = Modifier.fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3F5955),
                            unfocusedBorderColor = Color(0xFF3A3A3A),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF3F5955)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF424242)),
                            shape = RoundedCornerShape(25.dp)
                        ) {
                            Text("Cancel",
                                color = Color.White
                            )
                        }

                        // Create Button with Gradient-like color
                        Button(
                            onClick = { if (text.isNotBlank()) onConfirm(text) },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
                            shape = RoundedCornerShape(25.dp)
                        ) {
                            Text(if (title != null) "Rename" else "Create", color = Color.White)
                        }
                    }
                }
            }

            // Overlapping Icon Box
            Box(
                modifier = Modifier
                    .offset(y = (-40).dp) // Move icon up to "cut" into the top edge
                    .size(80.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF26A69A), Color(0xFF00695C))
                        ),
                        shape = CircleShape
                    )
                    .border(4.dp, Color(0xFF1E1E1E), CircleShape), // Thick border to match background
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = when {
                            title != null -> R.drawable.rename
                            // Otherwise, we are creating
                            isCreatingFolder -> R.drawable.folder_plus
                            else -> R.drawable.file_add
                        }
                    ),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Composable
@Preview
fun PreviewNameInputDialog() {
    NameInputDialog(
        onDismiss = {},
        isCreatingFolder = false,
        onConfirm = {}
    )
}
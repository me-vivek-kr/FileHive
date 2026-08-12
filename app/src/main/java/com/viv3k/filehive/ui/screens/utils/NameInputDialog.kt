package com.viv3k.filehive.ui.screens.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.viv3k.filehive.R
import com.viv3k.filehive.ui.utils.soft

@Composable
fun NameInputDialog(
    title: String? = null,
    defaultName: String = "",
    isCreatingFolder: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        NameInputDialogContent(
            title = title,
            defaultName = defaultName,
            isCreatingFolder = isCreatingFolder,
            onDismiss = onDismiss,
            onConfirm = onConfirm
        )
    }
}

@Composable
private fun NameInputDialogContent(
    title: String? = null,
    defaultName: String = "",
    isCreatingFolder: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(defaultName) }

    Box(
        modifier = Modifier
            .width(340.dp)
            .padding(top = 40.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 28.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color.Black.copy(alpha = 0.18f),
                    spotColor = Color.Black.copy(alpha = 0.24f)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 58.dp, start = 28.dp, end = 28.dp, bottom = 28.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title ?: if (isCreatingFolder) "Create New Folder" else "Create New File",
                    color = Color.Black,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(22.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = {
                        Text(
                            "Enter name...",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(36.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF5051D8),
                        unfocusedBorderColor = Color(0xFFE0E5EC),
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color(0xFF5051D8)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    DialogActionButton(
                        text = "Cancel",
                        containerColor = Color(0xFFEFF3F2),
                        contentColor = Color.Black,
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    )

                    DialogActionButton(
                        text = if (title != null) "Rename" else "Create",
                        containerColor = Color(0xFF5F5AE7),
                        contentColor = Color.White,
                        onClick = { if (text.isNotBlank()) onConfirm(text) },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .offset(y = (-40).dp)
                .size(80.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    ambientColor = Color(0xFF5051D8).copy(alpha = 0.24f),
                    spotColor = Color(0xFF5051D8).copy(alpha = 0.36f)
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF6C63FF), Color(0xFF5051D8))
                    ),
                    shape = CircleShape
                )
                .border(4.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(
                    id = when {
                        title != null -> R.drawable.rename
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

@Composable
private fun DialogActionButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .soft(
                shape = RoundedCornerShape(32.dp),
                cornerRadius = 32.dp,
                backgroundColor = containerColor,
                blurRadius = 12.dp,
                offsetY = 5.dp
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
@Preview
fun PreviewNameInputDialog() {
    Box(
        modifier = Modifier
            .background(Color(0xFFF7F9FB))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        NameInputDialogContent(
            onDismiss = {},
            isCreatingFolder = false,
            onConfirm = {}
        )
    }
}
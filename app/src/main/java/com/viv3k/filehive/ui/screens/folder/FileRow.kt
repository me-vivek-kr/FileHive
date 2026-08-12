package com.viv3k.filehive.ui.screens.folder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viv3k.filehive.R
import com.viv3k.filehive.data.model.FolderModel
import com.viv3k.filehive.ui.components.FileThumbnail
import com.viv3k.filehive.ui.screens.utils.FileOptionsDropdownMenu
import com.viv3k.filehive.ui.utils.readableFileSize
import java.io.File

@Composable
fun FileRow(
    entry: FolderModel,
    onClick: () -> Unit,
    onDeleteOptionClick: () -> Unit,
    onRenameOptionClick: () -> Unit,
    onLockOptionClick: () -> Unit = {}
) {
    val pillShape = RoundedCornerShape(50.dp)
    val expanded = remember { mutableStateOf(false) }
    val isSelected = expanded.value

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
                onLongClick = { expanded.value = true }
            ),
        color = if (isSelected) Color(0xFFEBEDF2) else Color.White,
        shape = pillShape,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val extension = entry.file.extension.lowercase()
            val isMedia = extension in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "mp4", "mkv", "avi", "mov", "webm")
            val thumbnailPadding = if (isMedia) 2.dp else 12.dp

            // Recessed circular icon container
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8EBF0))
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
                    .padding(thumbnailPadding),
                contentAlignment = Alignment.Center
            ) {
                FileThumbnail(
                    file = entry.file,
                    modifier = Modifier.fillMaxSize(),
                    iconSize = 32.dp
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Name + info (Vertically stacked)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = entry.file.name.ifEmpty { entry.file.absolutePath },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val infoText = if (entry.file.isDirectory) {
                    if (entry.fileCount == 0) "0 items"
                    else "${entry.fileCount} items"
                } else {
                    readableFileSize(entry.totalSize)
                }

                Text(
                    text = infoText,
                    fontSize = 14.sp,
                    color = Color(0xFF757B84),
                    maxLines = 1
                )
            }

            // Three-dot vertical menu icon
            Box {
                IconButton(
                    onClick = { expanded.value = true }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.more_vertical),
                        contentDescription = "Options",
                        tint = Color.Gray,
                    )
                }

                FileOptionsDropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false },
                    onCutClick = { expanded.value = false },
                    onCopyClick = { expanded.value = false },
                    onDeleteClick = {
                        expanded.value = false
                        onDeleteOptionClick()
                    },
                    onRenameClick = {
                        expanded.value = false
                        onRenameOptionClick()
                    },
                    onLockClick = {
                        expanded.value = false
                        onLockOptionClick()
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFileRow(){
    FileRow(
        entry = FolderModel(File("/storage/emulated/0"), 5, 1024000, System.currentTimeMillis()),
        onClick = {},
        onDeleteOptionClick = {},
        onRenameOptionClick = {},
        onLockOptionClick = {}
    )
}
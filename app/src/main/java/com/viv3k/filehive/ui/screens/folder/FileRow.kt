package com.viv3k.filehive.ui.screens.folder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viv3k.filehive.R
import com.viv3k.filehive.data.model.FolderModel
import com.viv3k.filehive.ui.components.FileIcons
import com.viv3k.filehive.ui.screens.utils.FileOptionsDropdownMenu
import com.viv3k.filehive.ui.utils.formatTimestamp
import com.viv3k.filehive.ui.utils.readableFileSize
import java.io.File

@Composable
fun FileRow(
    entry: FolderModel,
    onClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(16.dp)
    val expanded = remember { mutableStateOf(false) }
    val isSelected = expanded.value

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { expanded.value = true }
            ),
//        color = Color(0xFF1A1C21),
        color = if (isSelected) Color(0xFF0F6FFC).copy(alpha = 0.3f) else Color(0xFF1A1C21),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 3.dp,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .padding(
                    start = 12.dp,
                    top = 10.dp,
                    bottom = 10.dp
                )
                .clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Icon
            Icon(
                painter = painterResource(id = FileIcons.getIcon(entry.file)),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Name + info (LEFT)
            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = entry.file.name.ifEmpty { entry.file.absolutePath },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                val infoText = if (entry.file.isDirectory) {
                    if (entry.fileCount == 0)
                        "0 items"
                    else
                        "${entry.fileCount} items • ${readableFileSize(entry.totalSize)}"
                } else {
                    readableFileSize(entry.totalSize)
                }

                Text(
                    text = infoText,
                    fontSize = 12.sp,
                    color = Color(0xFF9AA0A6),
                    maxLines = 1
                )
            }

            // Timestamp (RIGHT)
            Text(
                text = formatTimestamp(entry.lastModified),
                fontSize = 10.sp,
                color = Color(0xFF9AA0A6)
            )

               // 👈 THIS pushes menu to right

            Box {
                IconButton(onClick = { expanded.value = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.more_vertical),
                        contentDescription = "Options",
                        tint = Color(0xFF9AA0A6),
                    )
                }

                FileOptionsDropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false },
                    onCutClick = {},
                    onCopyClick = {},
                    onDeleteClick = {}
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewFileRow(){
    FileRow(
        entry = FolderModel(File("/storage/emulated/0"), 5, 1024000, System.currentTimeMillis()),
        onClick = {}
    )
}
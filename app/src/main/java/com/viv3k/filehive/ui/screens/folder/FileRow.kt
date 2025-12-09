package com.viv3k.filehive.ui.screens.folder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viv3k.filehive.data.model.FolderModel
import com.viv3k.filehive.ui.utils.readableFileSize
import com.viv3k.filehive.ui.components.FileIcons
import com.viv3k.filehive.ui.utils.formatTimestamp
import kotlin.text.ifEmpty

@Composable
fun FileRow(
    entry: FolderModel,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = entry.file.isDirectory) { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {


        Icon(
            painter = painterResource(id = FileIcons.getIcon(entry.file)),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(32.dp)
        )

        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = entry.file.name.ifEmpty { entry.file.absolutePath },
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                val leftInfo = if (entry.file.isDirectory) {
                    if (entry.fileCount == 0) "Empty" else "${entry.fileCount} files • ${readableFileSize(entry.totalSize)}"
                } else {
                    readableFileSize(entry.totalSize)
                }
                Text(
                    text = leftInfo,
                    fontSize = 12.sp,
                    color = Color(0xFF9AA0A6),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = formatTimestamp(entry.lastModified),
                    fontSize = 12.sp,
                    color = Color(0xFF9AA0A6)
                )
            }
        }
    }
}
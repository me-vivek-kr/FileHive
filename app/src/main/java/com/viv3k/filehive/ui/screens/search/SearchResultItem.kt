package com.viv3k.filehive.ui.screens.search

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viv3k.filehive.R
import com.viv3k.filehive.data.common.FileOpener
import com.viv3k.filehive.ui.components.FileThumbnail
import com.viv3k.filehive.ui.screens.utils.FileOptionsDropdownMenu
import com.viv3k.filehive.ui.utils.formatTimestamp
import com.viv3k.filehive.ui.utils.readableFileSize

@Composable
fun SearchResultItem(
    result: SearchResult,
    onDeleteOptionClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val expanded = remember { mutableStateOf(false) }
    val isSelected = expanded.value
    val cardShape = RoundedCornerShape(16.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .combinedClickable(
                onClick = {
                    if (result.file.isDirectory) {
                        // Handle folder navigation if needed
                    } else {
                         FileOpener.openFile(context, result.file)
                    }
                },
                onLongClick = { expanded.value = true }
            ),
        color = if (isSelected) Color(0xFF0F6FFC).copy(alpha = 0.3f) else Color(0xFF1A1C21),
        shape = cardShape,
        tonalElevation = 3.dp,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            FileThumbnail(
                file = result.file,
                modifier = Modifier.size(36.dp),
                iconSize = 54.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Name + Path Info (LEFT)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = result.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Show Path + Size
                Text(
                    text = "${readableFileSize(result.size)} • ${result.path}",
                    fontSize = 12.sp,
                    color = Color(0xFF9AA0A6),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Timestamp (RIGHT)
            Text(
                text = formatTimestamp(result.lastModified),
                fontSize = 10.sp,
                color = Color(0xFF9AA0A6)
            )

            // Options
            Box {
                IconButton(onClick = { expanded.value = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.more_vertical),
                        contentDescription = "Options",
                        tint = if (isSelected) Color.White else Color(0xFF9AA0A6),
                    )
                }

                FileOptionsDropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false },
                    onCutClick = { expanded.value = false },
                    onCopyClick = { expanded.value = false },
                    onRenameClick = { expanded.value = false },
                    onDeleteClick = {
                        expanded.value = false
                        onDeleteOptionClick()
                    }
                )
            }
        }
    }
}
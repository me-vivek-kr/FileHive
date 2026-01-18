package com.viv3k.filehive.ui.screens.recyclebin

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.tooling.preview.Preview
import com.viv3k.filehive.R
import com.viv3k.filehive.ui.components.FileThumbnail
import java.io.File

@Composable
fun RecycleBinItem(
    item: RealBinItem,
    onRestoreClick: () -> Unit,
    onDeleteForeverClick: () -> Unit,
    onPropertiesClick: () -> Unit
) {
    val expanded = remember { mutableStateOf(false) }
    val isSelected = expanded.value
    val cardShape = RoundedCornerShape(16.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .combinedClickable(
                onClick = { /* Optional: Show detail logic */ },
                onLongClick = { expanded.value = true }
            ),
        // Match selection color from FileRow
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
            FileThumbnail(
                file = item.file,
                modifier = Modifier.size(36.dp),
                iconSize = 54.dp,
                displayName = item.displayName
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // File Name
                Text(
                    text = item.displayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // File Path (Original Location)
                Text(
                    text = item.absolutePath,
                    fontSize = 12.sp,
                    color = Color(0xFF9AA0A6),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 3. Deleted Time (Right side)
            Text(
                text = item.deletedTimeFormatted,
                fontSize = 10.sp,
                color = Color(0xFF9AA0A6),
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // 4. More Options Menu
            Box {
                IconButton(
                    onClick = { expanded.value = true }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.more_vertical),
                        contentDescription = "Options",
                        tint = if (isSelected) Color.White else Color(0xFF9AA0A6),
                    )
                }

                DropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false },
                    containerColor = Color(0xFF16181D),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    DropdownMenuItem(
                        text = { Text("Restore", color = Color.White) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.clock),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        onClick = {
                            expanded.value = false
                            onRestoreClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Permanently", color = Color(0xFFEF5350)) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.trash),
                                contentDescription = null,
                                tint = Color(0xFFEF5350),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        onClick = {
                            expanded.value = false
                            onDeleteForeverClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Properties", color = Color.White)},
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.info),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        onClick = {
                            expanded.value = false
                            onPropertiesClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun PreviewRecycleBinItem() {
    Box(modifier = Modifier.background(Color(0xFF0F1115)).padding(16.dp)) {
        RecycleBinItem(
            item = RealBinItem(
                file = File("/storage/emulated/0/Documents/Report.pdf"),
                displayName = "Report.pdf",
                originalTimestamp = System.currentTimeMillis(),
                deletedTimeFormatted = "Deleted Today",
                type = FileType.DOCUMENT,
                absolutePath = "/storage/emulated/0/Documents/Report.pdf",
                isFolder = false
            ),
            onRestoreClick = {},
            onDeleteForeverClick = {},
            onPropertiesClick = {}
        )
    }
}
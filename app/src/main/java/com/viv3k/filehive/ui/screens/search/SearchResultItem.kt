package com.viv3k.filehive.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.viv3k.filehive.ui.components.FileIcons
import com.viv3k.filehive.ui.screens.utils.FileOptionsDropdownMenu
import com.viv3k.filehive.ui.utils.readableFileSize
import com.viv3k.filehive.ui.utils.soft

@Composable
fun SearchResultItem(
    result: SearchResult,
    onDeleteOptionClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val expanded = remember { mutableStateOf(false) }
    val isSelected = expanded.value
    val cardShape = RoundedCornerShape(24.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .soft(
                shape = cardShape,
                cornerRadius = 24.dp,
                backgroundColor = if (isSelected) Color(0xFFEEF2FF) else Color.White,
                blurRadius = 14.dp,
                offsetY = 6.dp
            )
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    if (result.file.isDirectory) {
                        // Directory navigation handled if needed
                    } else {
                        FileOpener.openFile(context, result.file)
                    }
                },
                onLongClick = { expanded.value = true }
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon container with soft blue tint background
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFEEF2FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = FileIcons.getIcon(result.file)),
                    contentDescription = null,
                    tint = Color(0xFF5051D8),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // File Name + Subtitle (Path and Size)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = result.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${readableFileSize(result.size)} • ${result.path}",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Options 3-dots button
            Box {
                IconButton(
                    onClick = { expanded.value = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.more_vertical),
                        contentDescription = "Options",
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(18.dp)
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
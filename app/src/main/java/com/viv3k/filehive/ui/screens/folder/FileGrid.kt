package com.viv3k.filehive.ui.screens.folder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.viv3k.filehive.ui.components.FileIcons
import com.viv3k.filehive.ui.components.FileThumbnail
import com.viv3k.filehive.ui.screens.utils.FileOptionsDropdownMenu
import com.viv3k.filehive.ui.utils.readableFileSize
import com.viv3k.filehive.ui.utils.soft
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FileGrid(
    files: List<FolderModel>,
    onFolderClick: (String) -> Unit,
    onImageClick: (String) -> Unit,
    onFileClick: (File) -> Unit,
    onRenameOptionClick: (File) -> Unit,
    onDeleteOptionClick: (File) -> Unit,
    modifier: Modifier = Modifier,
    onLockOptionClick: (File) -> Unit = {}
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // Redesigned to 2 columns
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp, top = 18.dp, start = 20.dp, end = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        items(files) { entry ->
            FileGridItem(
                entry = entry,
                onClick = {
                    val extension = entry.file.extension.lowercase()
                    val isImage = extension in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "heic")

                    if (entry.file.isDirectory) {
                        onFolderClick(entry.file.absolutePath)
                    } else if (isImage) {
                        onImageClick(entry.file.absolutePath)
                    } else {
                        onFileClick(entry.file)
                    }
                },
                onRenameOptionClick = { onRenameOptionClick(entry.file) },
                onDeleteOptionClick = { onDeleteOptionClick(entry.file) },
                onLockOptionClick = { onLockOptionClick(entry.file) }
            )
        }
    }
}

@Composable
fun FileGridItem(
    entry: FolderModel,
    onClick: () -> Unit,
    onRenameOptionClick: (File) -> Unit,
    onDeleteOptionClick: (File) -> Unit,
    onLockOptionClick: (File) -> Unit = {}
){
    val expanded = remember { mutableStateOf(false) }
    val cardShape = RoundedCornerShape(34.dp)
    val previewShape = RoundedCornerShape(32.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val extension = entry.file.extension.lowercase()
    val isImage = extension in imageExtensions
    val isVideo = extension in videoExtensions
    val showTypeBadge = isImage || isVideo
    val previewIcon = if (entry.file.isDirectory || extension.isBlank()) {
        R.drawable.folder_new
    } else {
        FileIcons.getIcon(entry.file)
    }

// Anchor box for the dropdown menu
    Box {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .soft(
                    shape = cardShape,
                    cornerRadius = 34.dp,
                    backgroundColor = Color.White,
                    blurRadius = 18.dp,
                    offsetY = 8.dp,
                    interactionSource = interactionSource
                )
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                    onLongClick = { expanded.value = true }
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(125.dp)
//                        .clip(previewShape)
//                        .background(
//                            Brush.verticalGradient(
//                                colors = listOf(
//                                    Color(0xFFEFF3F7),
//                                    Color(0xFFF9FBFC)
//                                )
//                            )
//                        ),
//                    contentAlignment = Alignment.Center
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(125.dp)
                        .clip(previewShape)
                        .background(Color(0xFFE8EBF0))
                        .border(
                            width = 1.dp,
                            brush = verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.1f),
                                    Color.White.copy(alpha = 0.5f)
                                )
                            ),
                            shape = previewShape
                        ),
//                        .padding(thumbnailPadding),
                    contentAlignment = Alignment.Center
                ) {
                    if (isImage || isVideo) {
                        FileThumbnail(
                            file = entry.file,
                            modifier = Modifier.fillMaxSize(),
                            iconSize = 72.dp,
                            mediaShape = previewShape
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = previewIcon),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(58.dp)
                        )
                    }

                    if (showTypeBadge) {
                        FileTypeBadge(
                            icon = if (isVideo) R.drawable.video else R.drawable.image,
                            tint = if (isVideo) Color(0xFFC92A2A) else Color(0xFF4648D4),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = entry.file.name,
                    color = Color(0xFF20242A),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (entry.file.isDirectory) "${entry.fileCount} items" else relativeModifiedTime(entry.lastModified),
                        color = Color(0xFF7D8191),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = readableFileSize(entry.totalSize),
                        color = Color(0xFF7D8191),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        }

        // Dropdown Menu
        FileOptionsDropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false },
            onCutClick = { expanded.value = false },
            onCopyClick = { expanded.value = false },
            onDeleteClick = {
                expanded.value = false
                onDeleteOptionClick(entry.file)
            },
            onRenameClick = {
                expanded.value = false
                onRenameOptionClick(entry.file)
            },
            onLockClick = {
                expanded.value = false
                onLockOptionClick(entry.file)
            }
        )
    }
}

@Composable
private fun FileTypeBadge(
    icon: Int,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .soft(
                shape = CircleShape,
                cornerRadius = 18.dp,
                backgroundColor = Color.White,
                blurRadius = 8.dp,
                offsetY = 3.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

private val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "heif")
private val videoExtensions = setOf("mp4", "mkv", "avi", "mov", "webm", "3gp", "mpeg", "mpg", "m4v")

private fun relativeModifiedTime(lastModified: Long): String {
    val diffMillis = (System.currentTimeMillis() - lastModified).coerceAtLeast(0L)
    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour

    return when {
        diffMillis < minute -> "now"
        diffMillis < hour -> "${diffMillis / minute}m ago"
        diffMillis < day -> "${diffMillis / hour}h ago"
        diffMillis < 7 * day -> "${diffMillis / day}d ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(lastModified))
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFF5F7FB)
fun PreviewGrid() {
// Create some mock data to visualize the grid
    val mockFiles = List(9) { index ->
        FolderModel(
            file = File("/storage/emulated/0/Folder $index"),
            fileCount = index * 12,
            totalSize = index * 1024L * 1024L,
            lastModified = System.currentTimeMillis()
        )
    }

    FileGrid(
        files = mockFiles,
        onFolderClick = {},
        onImageClick = {},
        onFileClick = {},
        onRenameOptionClick = {},
        onDeleteOptionClick = {},
        onLockOptionClick = {}
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFF5F7FB)
fun PreviewGridItem() {
    Box(modifier = Modifier.padding(16.dp)) { // Add padding to see it clearly
        FileGridItem(
            entry = FolderModel(File("/storage/emulated/0/Downloads"), 5, 1024000, System.currentTimeMillis()),
            onClick = {},
            onRenameOptionClick = {},
            onDeleteOptionClick = {},
            onLockOptionClick = {}
        )
    }
}
package com.viv3k.filehive.ui.screens.folder

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viv3k.filehive.data.model.FolderModel
import com.viv3k.filehive.ui.components.FileThumbnail
import com.viv3k.filehive.ui.screens.utils.FileOptionsDropdownMenu
import com.viv3k.filehive.ui.utils.readableFileSize
import com.viv3k.filehive.ui.utils.soft
import java.io.File

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
        contentPadding = PaddingValues(bottom = 100.dp, top = 16.dp, start = 12.dp, end = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
    val cardShape = RoundedCornerShape(32.dp)
    val interactionSource = remember { MutableInteractionSource() }

// Anchor box for the dropdown menu
    Box {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .soft(
                    shape = cardShape,
                    backgroundColor = Color.White,
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
                val extension = entry.file.extension.lowercase()
                val isMedia = extension in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "mp4", "mkv", "avi", "mov", "webm")

                // Icon recessed container
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF0F2F5)), // Recessed background color
                    contentAlignment = Alignment.Center
                ) {
                    if (isMedia) {
                        FileThumbnail(
                            file = entry.file,
                            modifier = Modifier.fillMaxSize().padding(12.dp).clip(CircleShape),
                            iconSize = 32.dp
                        )
                    } else {
                        FileThumbnail(
                            file = entry.file,
                            modifier = Modifier.size(32.dp),
                            iconSize = 32.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bold folder/file name
                Text(
                    text = entry.file.name,
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Footer row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // "X items" on the left
                    Text(
                        text = if (entry.file.isDirectory) "${entry.fileCount} items" else "1 file",
                        color = Color(0xFF9AA0A6),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    // "Size" on the right
                    Text(
                        text = readableFileSize(entry.totalSize),
                        color = Color(0xFF9AA0A6),
                        fontSize = 11.sp,
                        textAlign = TextAlign.End
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
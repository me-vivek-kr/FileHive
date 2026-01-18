package com.viv3k.filehive.ui.screens.folder

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
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
import java.io.File

@Composable
fun FileGrid(
    files: List<FolderModel>,
    onFolderClick: (String) -> Unit,
    onImageClick: (String) -> Unit,
    onFileClick: (File) -> Unit,
    onRenameOptionClick: (File) -> Unit,
    onDeleteOptionClick: (File) -> Unit

) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3), // 3 Columns as per image
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                onDeleteOptionClick = { onDeleteOptionClick(entry.file) }
            )
        }
    }
}

@Composable
fun FileGridItem(
    entry: FolderModel,
    onClick: () -> Unit,
    onRenameOptionClick: (File) -> Unit,
    onDeleteOptionClick: (File) -> Unit
){
    val expanded = remember { mutableStateOf(false) }
    val isSelected = expanded.value

    // Background color logic: Dark Card (Normal) vs Blueish-Grey (Selected)
    val backgroundColor = if (isSelected) Color(0xFF2B2D31) else Color(0xFF16181D)

    // Anchor box for the dropdown menu
    Box {
        Surface(
            modifier = Modifier
                .aspectRatio(0.85f) // Makes the card slightly taller than wide
                .clip(RoundedCornerShape(16.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { expanded.value = true }
                ),
            color = backgroundColor,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
//                // Folder/File Icon
//                Icon(
//                    painter = painterResource(id = FileIcons.getIcon(entry.file)),
//                    contentDescription = null,
//                    tint = Color.Unspecified, // Keep original icon colors
//                    modifier = Modifier.size(54.dp)
//                )

                val extension = entry.file.extension.lowercase()
                val isMedia = extension in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "heic", "mp4", "mkv", "avi", "mov", "webm")

                if (isMedia) {
                    // Full Card Preview for Images/Videos
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        FileThumbnail(
                            file = entry.file,
                            modifier = Modifier.fillMaxSize(),
                            iconSize = 54.dp
                        )
                    }
                } else {
                    // Standard Icon Size for Folders and Documents
                    Box(
                        modifier = Modifier.size(54.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FileThumbnail(
                            file = entry.file,
                            modifier = Modifier.fillMaxSize(),
                            iconSize = 54.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Name
                Text(
                    text = entry.file.name,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }
        }

        // Dropdown Menu (Hidden until long-press)
        FileOptionsDropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false },
            onCutClick = { expanded.value = false },
            onCopyClick = { expanded.value = false },
            onDeleteClick = {
                expanded.value = false
                onDeleteOptionClick(entry.file)
            },
            onRenameClick = { // Add this
                expanded.value = false
                onRenameOptionClick(entry.file)
            }
        )
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFF0F1115) // Added dark background
fun PreviewGrid() {
    // Create some mock data to visualize the grid
    val mockFolder = FolderModel(File("/storage/emulated/0/Music"), 5, 1024, System.currentTimeMillis())
    val mockFiles = List(9) { index ->
        FolderModel(File("/storage/emulated/0/Folder $index"), 0, 0, System.currentTimeMillis())
    }

    FileGrid(
        files = mockFiles,
        onFolderClick = {},
        onImageClick = {},
        onFileClick = {},
        onRenameOptionClick = {},
        onDeleteOptionClick = {}
    )
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFF0F1115)
fun PreviewGridItem() {
    Box(modifier = Modifier.padding(16.dp)) { // Add padding to see it clearly
        FileGridItem(
            entry = FolderModel(File("/storage/emulated/0/Downloads"), 5, 1024000, System.currentTimeMillis()),
            onClick = {},
            onRenameOptionClick = {},
            onDeleteOptionClick = {}
        )
    }
}
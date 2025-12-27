package com.viv3k.filehive.ui.screens.folder

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viv3k.filehive.R
import com.viv3k.filehive.data.model.FolderModel
import com.viv3k.filehive.data.storage.FolderStatsCache
import com.viv3k.filehive.ui.components.DotsTypingIndicator
import com.viv3k.filehive.ui.components.LoadingListShimmer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun FolderScreen(
    folderPath: String,
    onBackClick: () -> Unit,
    onFolderClick: (String) -> Unit,
    previewData: List<FolderModel>? = null
) {

    var children by remember { mutableStateOf(previewData ?: emptyList()) }
    var loading by remember { mutableStateOf(previewData == null) }

    var exists by remember { mutableStateOf(true) }
    var menuExpanded by remember { mutableStateOf(false) }

    var isGrid by remember { mutableStateOf(false) }

    LaunchedEffect(folderPath) {
        if (previewData != null) return@LaunchedEffect
        loading = true

        val list = withContext(Dispatchers.IO) {
            val folder = File(folderPath)
            exists = folder.exists() && folder.isDirectory
            if (!exists) return@withContext emptyList<FolderModel>()
            folder.listFiles()
                ?.sortedWith(
                    compareBy<File> { !it.isDirectory }
                        .thenBy { it.name.lowercase() }
                )
                ?.map { file ->
                if (file.isDirectory) {
                    val (count, size, last) = FolderStatsCache.getStats(file)
                    FolderModel(file, count, size, last)
                } else {
                    FolderModel(file, 0, file.length(), file.lastModified())
                }
            } ?: emptyList()
        }
        children = list
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1115))
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        val displayTitle = remember(folderPath) {
            if (folderPath == "/storage/emulated/0") "Internal Storage"
            else File(folderPath).name
        }
        FolderTopBar(
            title = displayTitle,
            onBackClick = onBackClick,
            onSearchClick = { /* TODO */ },
            expanded = menuExpanded,
            onExpandChange = { menuExpanded = it }
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .graphicsLayer {
                    if (menuExpanded) {
                        renderEffect = android.graphics.RenderEffect
                            .createBlurEffect(18f, 18f, android.graphics.Shader.TileMode.CLAMP)
                            .asComposeRenderEffect()
                        alpha = 0.6f
                    }
                }
        ) {
            Column {
//                Box(
//                    Modifier.padding(horizontal = 16.dp)
//                ) {
//                    Text(
//                        text = folderPath,
//                        fontSize = 12.sp,
//                        color = Color(0xFF9AA0A6)
//                    )
//                }
//
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 4.dp, vertical = 4.dp),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//
//                        IconButton(onClick = { isGrid = false }) {
//                            Icon(
//                                painter = painterResource(id = R.drawable.list),
//                                contentDescription = "List",
//                                tint = if (!isGrid) Color.White else Color(0xFF9AA0A6),
//                                modifier = Modifier.size(18.dp)
//                            )
//                        }
//
//                        Spacer(modifier = Modifier.width(4.dp))
//
//                        IconButton(onClick = { isGrid = true }) {
//                            Icon(
//                                painter = painterResource(id = R.drawable.grid),
//                                contentDescription = "Grid",
//                                tint = if (isGrid) Color.White else Color(0xFF9AA0A6),
//                                modifier = Modifier.size(18.dp)
//                            )
//                        }
//                    }
//                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                ) {
                    Text(
                        text = folderPath,
                        fontSize = 12.sp,
                        color = Color(0xFF9AA0A6)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(onClick = { isGrid = false }) {
                        Icon(
                            painter = painterResource(id = R.drawable.list),
                            contentDescription = "List",
                            tint = if (!isGrid) Color.White else Color(0xFF9AA0A6),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = { isGrid = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.grid),
                            contentDescription = "Grid",
                            tint = if (isGrid) Color.White else Color(0xFF9AA0A6),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingListShimmer()
//                            DotsTypingIndicator()
                        }
                    }

                    !exists -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Folder does not exist", color = Color(0xFF9AA0A6))
                        }
                    }

                    children.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Folder is empty", color = Color(0xFF9AA0A6))
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(children) { entry ->
                                FileRow(
                                    entry = entry,
                                    onClick = {
                                        if (entry.file.isDirectory) {
                                            onFolderClick(entry.file.absolutePath)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (menuExpanded) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.25f))
                .clickable { menuExpanded = false }
        )
    }
}


@RequiresApi(Build.VERSION_CODES.S)
@Preview(showBackground = true)
@Composable
fun FolderScreenPreview() {
    val mockFolders = listOf(
        FolderModel(File("/storage/emulated/0"), 5, 1024000, System.currentTimeMillis()),
        FolderModel(File("/storage/emulated/0"), 12, 5242880, System.currentTimeMillis()),
        FolderModel(File("/storage/emulated/0/Pictures"), 25, 10485760, System.currentTimeMillis()),
        // Add a mock file to see how it renders
        FolderModel(File("/storage/emulated/0/report.pdf"), 0, 85000, System.currentTimeMillis() - 86400000)
    )

    // Call FolderScreen and pass the mock data to the previewData parameter
    FolderScreen(
        folderPath = "/storage/emulated/0",
        onBackClick = {},
        onFolderClick = {},
        previewData = mockFolders // This is the crucial part
    )
}

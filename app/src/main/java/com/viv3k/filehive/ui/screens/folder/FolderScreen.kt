package com.viv3k.filehive.ui.screens.folder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viv3k.filehive.R
import com.viv3k.filehive.data.model.FolderModel
import com.viv3k.filehive.data.storage.computeFolderStats
import com.viv3k.filehive.ui.components.DotsTypingIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun FolderScreen(
    folderPath: String,
    onBackClick: () -> Unit,
    onFolderClick: (String) -> Unit
) {
    var children by remember { mutableStateOf<List<FolderModel>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var exists by remember { mutableStateOf(true) }

    LaunchedEffect(folderPath) {
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
                        val (count, size, last) = computeFolderStats(file)
                        FolderModel(file, count, size, last)
                    } else {
                        FolderModel(file, 0, file.length(), file.lastModified())
                    }
                }
                ?: emptyList()
        }
        children = list
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1115))
            .padding(16.dp)
            .statusBarsPadding()
    ) {
        // Top bar
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_left),
                    tint = Color.Unspecified,
                    contentDescription = "Back"
                )
            }
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = File(folderPath).name.ifEmpty { folderPath },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = folderPath,
                    fontSize = 10.sp,
                    color = Color(0xFF9AA0A6)
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
                    //LoadingListShimmer()
                    DotsTypingIndicator()
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
                        Divider(color = Color(0xFF1E2024))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FolderScreenPreview() {
    FolderScreen(
        folderPath = "/storage/emulated/0",
        onBackClick = {},
        onFolderClick = {}
    )
}

package com.viv3k.filehive.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun LocalScreen(
    modifier: Modifier = Modifier,
    onFolderClick: (path: String) -> Unit
) {
    var loading by remember { mutableStateOf(true) }
    var folders by remember { mutableStateOf<List<File>>(emptyList()) }

    LaunchedEffect(Unit) {
        loading = true
        folders = loadTopLevelFolders()
        loading = false
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            loading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Loading folders...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            folders.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No folders found",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(folders) { folder ->
                        FolderRow(
                            folder = folder,
                            onClick = { onFolderClick(folder.absolutePath) }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderRow(
    folder: File,
    onClick: () -> Unit
) {
    val name = folder.name.ifEmpty { folder.absolutePath }
    val count = folder.listFiles()?.size ?: 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$count items",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private suspend fun loadTopLevelFolders(): List<File> = withContext(Dispatchers.IO) {
    // Change this root to whatever you actually want to list
    val root = File("/storage/emulated/0")

    if (!root.exists() || !root.isDirectory) {
        return@withContext emptyList<File>()
    }

    root.listFiles()
        ?.filter { it.isDirectory }
        ?.sortedBy { it.name.lowercase() }
        ?: emptyList()
}

@Preview
@Composable
fun LocalScreenPreview() {
    LocalScreen(onFolderClick = {})
}


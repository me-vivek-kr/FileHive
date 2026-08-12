package com.viv3k.filehive.ui.screens.folder

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.viv3k.filehive.data.common.FileOpener
import com.viv3k.filehive.data.database.AppDatabase
import com.viv3k.filehive.data.database.FolderCacheRepository
import com.viv3k.filehive.data.model.FolderModel
import com.viv3k.filehive.data.model.FolderUiState
import com.viv3k.filehive.data.model.FolderViewModel
import com.viv3k.filehive.ui.components.BreadcrumbNavigation
import com.viv3k.filehive.ui.components.LoadingShimmer
import com.viv3k.filehive.ui.components.NeomorphicBottomNav
import com.viv3k.filehive.ui.screens.utils.DeleteConfirmationDialog
import com.viv3k.filehive.ui.screens.utils.NameInputDialog
import com.viv3k.filehive.ui.utils.NeomorphicFAB
import com.viv3k.filehive.ui.utils.NeomorphicFilterButton
import com.viv3k.filehive.ui.utils.NeomorphicViewSwitcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FolderScreen(
    folderPath: String,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onFolderClick: (String) -> Unit,
    onImageClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    previewData: List<FolderModel>? = null,
    onRecycleBinClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: FolderViewModel = viewModel()
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val repository = remember { FolderCacheRepository(db.folderCacheDao()) }
    val scope = rememberCoroutineScope()

    var isGrid by remember { mutableStateOf(true) }
    var exists by remember { mutableStateOf(true) }
    var menuExpanded by remember { mutableStateOf(false) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<File?>(null) }

    var showCreateOptionsDialog by remember { mutableStateOf(false) }
    var showNameInputDialog by remember { mutableStateOf(false) }
    var isCreatingFolder by remember { mutableStateOf(true) }

    var showRenameDialog by remember { mutableStateOf(false) }
    var fileToRename by remember { mutableStateOf<File?>(null) }
    var showSortSheet by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf(FolderSortOption.Name) }
    var sortAscending by remember { mutableStateOf(true) }
    var foldersFirst by remember { mutableStateOf(true) }

    val refreshTrigger by viewModel.refreshTrigger.collectAsState()
    val uiState by viewModel.state.collectAsState()

    var loading = uiState is FolderUiState.Loading

    val children = when(val state = uiState) {
        is FolderUiState.Loaded -> state.files
        else -> emptyList()
    }
    val visibleChildren = remember(children, sortOption, sortAscending, foldersFirst) {
        children.sortedWith { first, second ->
            if (foldersFirst) {
                val folderCompare = compareValues(!first.file.isDirectory, !second.file.isDirectory)
                if (folderCompare != 0) return@sortedWith folderCompare
            }

            val result = when (sortOption) {
                FolderSortOption.Name -> first.file.name.lowercase().compareTo(second.file.name.lowercase())
                FolderSortOption.DateModified -> first.lastModified.compareTo(second.lastModified)
                FolderSortOption.Size -> first.totalSize.compareTo(second.totalSize)
            }

            if (sortAscending) result else -result
        }
    }

    val handleRename: (String) -> Unit = { newName ->
        fileToRename?.let { oldFile ->
            val parent = oldFile.parentFile
            val newFile = File(parent, newName)

            scope.launch(Dispatchers.IO) {
                try {
                    val success = oldFile.renameTo(newFile)
                    withContext(Dispatchers.Main) {
                        if (success) {
                            viewModel.onFileOperationCompleted(parent ?: File(folderPath))
                            showRenameDialog = false
                            Toast.makeText(context, "Renamed successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Rename failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    if (showRenameDialog && fileToRename != null) {
        NameInputDialog(
            title = "Rename ${if (fileToRename!!.isDirectory) "Folder" else "File"}",
            defaultName = fileToRename!!.name,
            isCreatingFolder = fileToRename!!.isDirectory,
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName -> handleRename(newName) }
        )
    }

    LaunchedEffect(folderPath, previewData) {
        viewModel.loadFolder(File(folderPath),previewData)
    }

    LaunchedEffect(folderPath) {
        val normalizedPath = folderPath.trimEnd('/')
        val cache = withContext(Dispatchers.IO) {
            repository.getCache(normalizedPath)
        }
        if (cache != null) {
            isGrid = cache.viewMode == "grid"
        }
    }

    LaunchedEffect(folderPath, refreshTrigger) {
        if (previewData != null) return@LaunchedEffect

        val list = withContext(Dispatchers.IO) {
            val folder = File(folderPath)
            exists = folder.exists() && folder.isDirectory
            if (!exists) return@withContext emptyList<FolderModel>()

            val files = folder.listFiles()
                ?.sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() })

            files?.map { file ->
                if (file.isDirectory) {
                    val cache = repository.getFolderInfo(file)
                    FolderModel(file, cache.fileCount, cache.sizeBytes, cache.lastModified)
                } else {
                    FolderModel(file, 0, file.length(), file.lastModified())
                }
            } ?: emptyList()
        }

        viewModel.setFileList(list)
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FB))
            .statusBarsPadding()
    ) {
        val displayTitle = remember(folderPath) {
            if (folderPath == "/storage/emulated/0") "Internal Storage"
            else File(folderPath).name
        }

        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            FolderTopBar(
                title = displayTitle,
                onBackClick = onBackClick,
//                onSearchClick = onSearchClick,
                expanded = menuExpanded,
                onExpandChange = { menuExpanded = it },
                onNavigateTo = { route ->
                    when (route) {
                        "recycle_bin" -> onRecycleBinClick()
                        "home" -> onHomeClick()
                    }
                },
                onNewClick = {
                    showCreateOptionsDialog = true
                    menuExpanded = false
                }
            )
        }

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
                // Breadcrumbs on one line - Edge to Edge scroll
                BreadcrumbNavigation(
                    folderPath = folderPath,
                    onPathClick = { clickedPath ->
                        onFolderClick(clickedPath)
                    },
                    onHomeClick = onHomeClick
                )

                // View Switcher and Filter on a separate line, scrollable and aligned to the right
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp) // Space for shadows
                ) {
                    item {
                        NeomorphicViewSwitcher(
                            isGrid = isGrid,
                            onToggle = { grid ->
                                isGrid = grid
                                scope.launch {
                                    repository.setViewMode(folderPath.trimEnd('/'), grid)
                                }
                            }
                        )
                    }

                    item {
                        NeomorphicFilterButton(onClick = {
                            showSortSheet = true
                        })
                    }
                }

//                Spacer(modifier = Modifier.height(16.dp))

                when {
                    loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                        LoadingShimmer(isGrid = isGrid)
                    }

                    !exists -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("Folder does not exist", color = Color(0xFF9AA0A6))
                    }

                    visibleChildren.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("Folder is empty", color = Color(0xFF9AA0A6))
                    }

                    else -> {
                        if (isGrid) {
                            FileGrid(
                                files = visibleChildren,
                                onFolderClick = onFolderClick,
                                onImageClick = onImageClick,
                                onFileClick = { file ->
                                    FileOpener.openFile(context, file)
                                },
                                onRenameOptionClick = { file ->
                                    fileToRename = file
                                    showRenameDialog = true
                                },
                                onDeleteOptionClick = { file ->
                                    fileToDelete = file
                                    showDeleteDialog = true
                                },
                                onLockOptionClick = { file ->
                                    viewModel.lockFolder(context, file)
                                    Toast.makeText(context, "Locked", Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp)
                            ) {
                                items(
                                    items = visibleChildren,
                                    key = { it.file.absolutePath }
                                ) { entry ->
                                    Box(modifier = Modifier.animateItem()) {
                                        FileRow(
                                            entry = entry,
                                            onClick = {
                                                val extension = entry.file.extension.lowercase()
                                                val isImage = extension in listOf("jpg", "jpeg", "png", "webp", "bmp", "heic")

                                                if (entry.file.isDirectory) {
                                                    onFolderClick(entry.file.absolutePath)
                                                } else if (isImage) {
                                                    onImageClick(entry.file.absolutePath)
                                                } else {
                                                    FileOpener.openFile(context, entry.file)
                                                }
                                            },
                                            onDeleteOptionClick = {
                                                fileToDelete = entry.file
                                                showDeleteDialog = true
                                            },
                                            onRenameOptionClick = {
                                                fileToRename = entry.file
                                                showRenameDialog = true
                                            },
                                            onLockOptionClick = {
                                                viewModel.lockFolder(context, entry.file)
                                                Toast.makeText(context, "Locked", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 120.dp, end = 16.dp)
            ) {
                NeomorphicFAB(onClick = {
                    showCreateOptionsDialog = true
                })
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
            ) {
                NeomorphicBottomNav(
                    activeTab = "files",
                    onTabClick = { tab ->
                        when (tab) {
                            "home" -> onHomeClick()
                            "settings" -> onSettingsClick()
                            "search" -> onSearchClick()
                            "files" -> { /* Already here */ }
                        }
                    }
                )
            }
        }
    }

    if (showDeleteDialog && fileToDelete != null) {
        DeleteConfirmationDialog(
            fileName = fileToDelete!!.name,
            onDismiss = {
                showDeleteDialog = false
                fileToDelete = null
            },
            onConfirm = { isPermanent ->
                viewModel.deleteFile(fileToDelete!!, isPermanent)
                showDeleteDialog = false
                fileToDelete = null
            }
        )
    }

    if(showCreateOptionsDialog){
        NewFileFolderDialog(
            onDismissRequest = {
                showCreateOptionsDialog = false
            },
            onNewFileClick = {
                isCreatingFolder = false
                showCreateOptionsDialog = false
                showNameInputDialog = true
            },
            onNewFolderClick = {
                isCreatingFolder = true
                showCreateOptionsDialog = false
                showNameInputDialog = true
            }
        )
    }

    if (showNameInputDialog) {
        NameInputDialog(
            isCreatingFolder = isCreatingFolder,
            onDismiss = { showNameInputDialog = false },
            onConfirm = { name ->
                if (name.isNotBlank()) {
                    val parent = File(folderPath)
                    val newFile = File(parent, name)

                    scope.launch(Dispatchers.IO) {
                        try {
                            val success = if (isCreatingFolder) {
                                newFile.mkdir()
                            } else {
                                newFile.createNewFile()
                            }

                            withContext(Dispatchers.Main) {
                                if (success) {
                                    viewModel.onFileOperationCompleted(parent)
                                    showNameInputDialog = false
                                    Toast.makeText(context, "Created successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed: Path already exists or Access Denied", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        )
    }

    if (menuExpanded) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.25f))
                .clickable { menuExpanded = false }
        )
    }

    if (showSortSheet) {
        SortFilesBottomSheet(
            selectedSort = sortOption,
            ascending = sortAscending,
            foldersFirst = foldersFirst,
            onSortSelected = { sortOption = it },
            onAscendingChange = { sortAscending = it },
            onFoldersFirstChange = { foldersFirst = it },
            onDismiss = { showSortSheet = false },
            onApply = { showSortSheet = false }
        )
    }
}



@Preview(showBackground = true)
@Composable
fun FolderScreenPreview() {
    val mockFolders = listOf(
        FolderModel(File("/storage/emulated/0"), 5, 1024000, System.currentTimeMillis()),
        FolderModel(File("/storage/emulated/0"), 12, 5242880, System.currentTimeMillis()),
        FolderModel(File("/storage/emulated/0/Pictures"), 25, 10485760, System.currentTimeMillis()),
        FolderModel(File("/storage/emulated/0/report.pdf"), 0, 85000, System.currentTimeMillis() - 86400000)
    )

    FolderScreen(
        folderPath = "/storage/emulated/0",
        onBackClick = {},
        onFolderClick = {},
        onSearchClick = {},
        previewData = mockFolders,
        onRecycleBinClick = {},
        onSettingsClick = {},
        onHomeClick = {},
        onImageClick = {},
    )
}
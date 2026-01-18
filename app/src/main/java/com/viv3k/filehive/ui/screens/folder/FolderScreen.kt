package com.viv3k.filehive.ui.screens.folder

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.viv3k.filehive.R
import com.viv3k.filehive.data.common.FileOpener
import com.viv3k.filehive.data.database.AppDatabase
import com.viv3k.filehive.data.database.FolderCacheRepository
import com.viv3k.filehive.data.model.FolderModel
import com.viv3k.filehive.data.model.FolderUiState
import com.viv3k.filehive.data.model.FolderViewModel
import com.viv3k.filehive.ui.components.BreadcrumbNavigation
import com.viv3k.filehive.ui.components.LoadingListShimmer
import com.viv3k.filehive.ui.screens.utils.DeleteConfirmationDialog
import com.viv3k.filehive.ui.screens.utils.NameInputDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.S)
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
    viewModel: FolderViewModel = viewModel()
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val repository = remember { FolderCacheRepository(db.folderCacheDao()) }
    val scope = rememberCoroutineScope()

    //Folder View-Mode from DB
    var isGrid by remember { mutableStateOf(false) }
    var exists by remember { mutableStateOf(true) }
    var menuExpanded by remember { mutableStateOf(false) }

    //Add State for the Dialog
    var showDeleteDialog by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<File?>(null) }

    // New Creation Dialog States
    var showCreateOptionsDialog by remember { mutableStateOf(false) }
    var showNameInputDialog by remember { mutableStateOf(false) }
    var isCreatingFolder by remember { mutableStateOf(true) } // true = folder, false = file

    // Rename File/Folder
    var showRenameDialog by remember { mutableStateOf(false) }
    var fileToRename by remember { mutableStateOf<File?>(null) }


    // Collect refresh trigger
    val refreshTrigger by viewModel.refreshTrigger.collectAsState()


    // Collect UI state from ViewModel
    val uiState by viewModel.state.collectAsState()

    var loading = uiState is FolderUiState.Loading

    // Instead, derive the list from uiState
    val children = when(val state = uiState) {
        is FolderUiState.Loaded -> state.files
        else -> emptyList()
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

    //Load saved View-Mode from DB when folderPath changes
    LaunchedEffect(folderPath) {
        val normalizedPath = folderPath.trimEnd('/')
        val cache = withContext(Dispatchers.IO) {
            repository.getCache(normalizedPath)
        }
        if (cache != null) {
            isGrid = cache.viewMode == "grid"
        }
    }

    //Load Files List (Using Repo Cache Stats)
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
            onSearchClick = onSearchClick,
            expanded = menuExpanded,
            onExpandChange = { menuExpanded = it },
            onNavigateTo = { route ->
                when (route) {
                    "recycle_bin" -> onRecycleBinClick()
                    "home" -> onHomeClick()
                }
                // handle other
            },
            onNewClick = {
                // Open the Selection Dialog
                showCreateOptionsDialog = true
                menuExpanded = false
            }
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
                //PATH Text + View Mode Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                ) {
//                    Text(
//                        text = folderPath,
//                        fontSize = 12.sp,
//                        color = Color(0xFF9AA0A6)
//                    )
                    Box(modifier = Modifier.weight(1f)) {
                        BreadcrumbNavigation(
                            folderPath = folderPath,
                            onPathClick = { clickedPath ->
                                onFolderClick(clickedPath)
                            }
                        )
                    }

                    IconButton(onClick = {
                        isGrid = false
                        scope.launch {
                            // Ensure consistency
                            repository.setViewMode(folderPath.trimEnd('/'), false)
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.list),
                            contentDescription = "List",
                            tint = if (!isGrid) colorResource(id = R.color.green) else Color(0xFF9AA0A6),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    IconButton(onClick = {
                        isGrid = true
                        scope.launch {
                            repository.setViewMode(folderPath.trimEnd('/'), true)
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.grid),
                            contentDescription = "Grid",
                            tint = if (isGrid) Color.White else Color(0xFF9AA0A6),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                //Content State Handle
                when {
                    loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                        LoadingListShimmer()
                    }

                    !exists -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("Folder does not exist", color = Color(0xFF9AA0A6))
                    }

                    children.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("Folder is empty", color = Color(0xFF9AA0A6))
                    }

                    else -> {
                        if (isGrid) {
                            FileGrid(
                                files = children,
                                onFolderClick = onFolderClick,
                                // Pass the image click logic to Grid as well
                                onImageClick = onImageClick,
                                onFileClick = { file ->
                                    FileOpener.openFile(context, file)
                                },
                                onRenameOptionClick = { file -> // Add this
                                    fileToRename = file
                                    showRenameDialog = true
                                },
                                onDeleteOptionClick = { file -> // Add this
                                    fileToDelete = file
                                    showDeleteDialog = true
                                }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Add key to items for animation to work
                                items(
                                    items = children,
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
                                            }
                                        )
                                    }
                                }
                                item {
                                    Spacer(modifier = Modifier.height(30.dp))
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

                            //Create Option Dialog
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

                            //Name Input Dialog
                            if (showNameInputDialog) {
                                NameInputDialog(
                                    isCreatingFolder = isCreatingFolder,
                                    onDismiss = { showNameInputDialog = false },
                                    onConfirm = { name ->
                                        if (name.isNotBlank()) {
                                            // Ensure we use the exact folderPath currently viewed
                                            val parent = File(folderPath)
                                            val newFile = File(parent, name)

                                            scope.launch(Dispatchers.IO) {
                                                try {
                                                    // Use mkdir() for single folder creation, createNewFile() for files
                                                    val success = if (isCreatingFolder) {
                                                        newFile.mkdir()
                                                    } else {
                                                        newFile.createNewFile()
                                                    }

                                                    withContext(Dispatchers.Main) {
                                                        if (success) {
                                                            // Important: Refresh logic
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
        onSearchClick = {},
        previewData = mockFolders,
        onRecycleBinClick = {},
        onHomeClick = {},
        onImageClick = {},
    )
}

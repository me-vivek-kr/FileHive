package com.viv3k.filehive.data.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viv3k.filehive.data.repository.FileRepository
import com.viv3k.filehive.data.storage.FolderStatsCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed class FolderUiState {
    object Idle : FolderUiState()
    object Loading : FolderUiState()
    data class Loaded(
        val files: List<FolderModel>,
        val fileCount: Int,
        val totalSize: Long,
        val latestModified: Long
    ) : FolderUiState()
    data class Error(val message: String) : FolderUiState()
}

class FolderViewModel : ViewModel() {
    private val _state = MutableStateFlow<FolderUiState>(FolderUiState.Idle)
    val state = _state.asStateFlow()

    // Add a trigger for reloading file lists
    private val _refreshTrigger = MutableStateFlow(0)
    val refreshTrigger = _refreshTrigger.asStateFlow()


    private var currentFileList = mutableListOf<FolderModel>()

    private fun updateStateWithList(list: List<FolderModel>) {
        currentFileList = list.toMutableList()
        val count = list.size
        val size = list.sumOf { it.totalSize }
        val lm = list.maxOfOrNull { it.lastModified } ?: System.currentTimeMillis()

        _state.value = FolderUiState.Loaded(list, count, size, lm)
    }

    fun loadFolder(folder: File, preLoadedData: List<FolderModel>? = null) {
        viewModelScope.launch {
            _state.value = FolderUiState.Loading

            // If we have preview data passed from screen (e.g. initial load), use it
            if (preLoadedData != null && preLoadedData.isNotEmpty()) {
                updateStateWithList(preLoadedData)
                return@launch
            }

            try {
                // Background loading
                FolderStatsCache.getStats(folder)
            } catch (t: Throwable) {
                _state.value = FolderUiState.Error(t.message ?: "Unknown error")
            }
        }
    }

    // Call this after creating/deleting/renaming files to force refresh
    fun onFileOperationCompleted(parentFolder: File, reload: Boolean = true) {
        FolderStatsCache.invalidate(parentFolder.absolutePath)
        if (reload) {
            _state.value = FolderUiState.Loading
            // Signal the UI to re-fetch the file list
            _refreshTrigger.value += 1
        }
    }

    // Call this from the Screen when it finishes loading files from disk
    fun setFileList(files: List<FolderModel>) {
        updateStateWithList(files)
    }


    // Example: perform delete on IO dispatcher, then invalidate and reload
    fun deleteFile(file: File, isPermanent: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val parent = file.parentFile

            val updateList = currentFileList.filter { it.file.absolutePath != file.absolutePath }
            updateStateWithList(updateList)

            // Use the repository logic
            val success = FileRepository.deleteFile(file, isPermanent)

            if (success && parent != null) {
                // Invalidate cache to force re-calculation of file count/sizes
                FolderStatsCache.invalidate(parent.absolutePath)

            } else {
                if (parent != null) {
                    // Here we DO want to load because our optimistic update was wrong
                    // (But you need to ensure loadFolder actually fetches data, or trigger
                    // a refresh event to the UI)
                }
            }
        }
    }
    fun lockFolder(context: android.content.Context, file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = com.viv3k.filehive.data.database.AppDatabase.getDatabase(context)
            val vaultRepo = com.viv3k.filehive.data.repository.VaultRepository(context, db.lockedFolderDao())
            
            val parent = file.parentFile
            val success = vaultRepo.lockFolder(file)
            
            if (success) {
                // Update local list
                val updateList = currentFileList.filter { it.file.absolutePath != file.absolutePath }
                updateStateWithList(updateList)
                
                if (parent != null) {
                    FolderStatsCache.invalidate(parent.absolutePath)
                }
            }
        }
    }
}
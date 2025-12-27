package com.viv3k.filehive.data.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viv3k.filehive.data.storage.FolderStatsCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed class FolderUiState {
    object Idle : FolderUiState()
    object Loading : FolderUiState()
    data class Loaded(val fileCount: Int, val totalSize: Long, val latestModified: Long) : FolderUiState()
    data class Error(val message: String) : FolderUiState()
}

class FolderViewModel : ViewModel() {
    private val _state = MutableStateFlow<FolderUiState>(FolderUiState.Idle)
    val state = _state.asStateFlow()


    fun loadFolder(folder: File) {
        viewModelScope.launch {
            _state.value = FolderUiState.Loading
            try {
                val (count, size, lm) = FolderStatsCache.getStats(folder)
                _state.value = FolderUiState.Loaded(count, size, lm)
            } catch (t: Throwable) {
                _state.value = FolderUiState.Error(t.message ?: "Unknown error")
            }
        }
    }

    // Call this after creating/deleting/renaming files to force refresh
    fun onFileOperationCompleted(parentFolder: File, reload: Boolean = true) {
        FolderStatsCache.invalidate(parentFolder.absolutePath)
        if (reload) loadFolder(parentFolder)
    }

    // Example: perform delete on IO dispatcher, then invalidate and reload
    fun deleteFile(file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            val parent = file.parentFile
            val ok = try { file.delete() } catch (_: Throwable) { false }
            if (parent != null) {
                // invalidate so next read recomputes
                FolderStatsCache.invalidate(parent.absolutePath)
                // switch to main thread to update UI via loadFolder
                loadFolder(parent)
            }
        }
    }
}
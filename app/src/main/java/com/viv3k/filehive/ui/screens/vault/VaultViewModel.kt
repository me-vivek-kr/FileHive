package com.viv3k.filehive.ui.screens.vault

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.viv3k.filehive.data.database.AppDatabase
import com.viv3k.filehive.data.database.LockedFolderEntity
import com.viv3k.filehive.data.repository.VaultRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VaultViewModel(private val vaultRepository: VaultRepository) : ViewModel() {

    val lockedFolders: StateFlow<List<LockedFolderEntity>> = vaultRepository.getAllLockedFolders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun unlockFolder(folder: LockedFolderEntity) {
        viewModelScope.launch {
            vaultRepository.unlockFolder(folder)
        }
    }
    
    // Not needed if we lock from the Folder screen directly, but can be added:
    // fun lockFolder(folder: File) 

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val context = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Context
                val dao = AppDatabase.getDatabase(context).lockedFolderDao()
                val repository = VaultRepository(context, dao)
                VaultViewModel(repository)
            }
        }
    }
}

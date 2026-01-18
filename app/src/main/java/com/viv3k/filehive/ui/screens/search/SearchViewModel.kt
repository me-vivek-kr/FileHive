package com.viv3k.filehive.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viv3k.filehive.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class SearchResult(
    val name: String,
    val path: String,
    val file: File,
    val iconRes: Int,
    val size: Long,
    val lastModified: Long
)

class SearchViewModel : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // Recent searches (In a real app, save this to Room/DataStore)
    private val _recentSearches = MutableStateFlow(listOf("Project files", "Vacation photos", "Music"))
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    private var searchJob: Job? = null

    // Root directory to start search from
    private val rootDir = File("/storage/emulated/0")

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery

        // Cancel previous search job if user keeps typing (Debounce)
        searchJob?.cancel()

        if (newQuery.length < 2) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(500) // Debounce: wait 500ms before starting search
            _isSearching.value = true
            performSearch(newQuery, _selectedFilter.value)
        }
    }

    fun onFilterSelected(filter: String) {
        _selectedFilter.value = filter
        // Re-run search with new filter if query exists
        val currentQuery = _searchQuery.value
        if (currentQuery.isNotEmpty()) {
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                _isSearching.value = true
                performSearch(currentQuery, filter)
            }
        }
    }

    fun onRecentSearchClick(query: String) {
        onQueryChange(query)
    }

    fun addToRecent(query: String) {
        val currentList = _recentSearches.value.toMutableList()
        if (!currentList.contains(query)) {
            currentList.add(0, query)
            if (currentList.size > 5) currentList.removeAt(currentList.lastIndex) // Keep only top 5
            _recentSearches.value = currentList
        }
    }

    private suspend fun performSearch(query: String, filter: String) {
        val results = withContext(Dispatchers.IO) {
            val foundFiles = mutableListOf<SearchResult>()
            recursiveSearch(rootDir, query.lowercase(), filter, foundFiles)
            foundFiles
        }
        _searchResults.value = results
        _isSearching.value = false
    }

    // 1️⃣ & 2️⃣ Recursive Search & Filter Logic
    private fun recursiveSearch(
        dir: File,
        query: String,
        filter: String,
        resultList: MutableList<SearchResult>
    ) {
        val files = dir.listFiles() ?: return

        for (file in files) {
            // Check if file name matches query
            if (file.name.lowercase().contains(query)) {
                if (isValidType(file, filter)) {
                    resultList.add(mapFileToResult(file))
                }
            }

            // Recursive call for directories
            // Optimization: Skip hidden folders or Android/data to prevent crashes/slowdowns
            if (file.isDirectory && !file.isHidden) {
                recursiveSearch(file, query, filter, resultList)
            }
        }
    }

    private fun isValidType(file: File, filter: String): Boolean {
        if (filter == "All") return true
        if (file.isDirectory) return false // Filters usually apply to files

        val ext = file.extension.lowercase()
        return when (filter) {
            "Documents" -> ext in listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt")
            "Images" -> ext in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
            "Videos" -> ext in listOf("mp4", "mkv", "avi", "mov", "3gp")
            "Audio" -> ext in listOf("mp3", "wav", "aac", "m4a", "flac")
            "Archives" -> ext in listOf("zip", "rar", "7z", "tar", "gz")
            else -> true
        }
    }

//    private fun mapFileToResult(file: File): SearchResult {
//        val icon = when {
//            file.isDirectory -> R.drawable.folder // Ensure you have folder icon
//            file.extension.lowercase() in listOf("jpg", "png") -> R.drawable.image
//            file.extension.lowercase() in listOf("mp4", "mkv") -> R.drawable.video
//            file.extension.lowercase() in listOf("mp3", "wav") -> R.drawable.music
//            file.extension.lowercase() in listOf("pdf", "doc") -> R.drawable.file
//            else -> R.drawable.file
//        }
//        return SearchResult(file.name, file.parent ?: "", file, icon)
//    }

    private fun mapFileToResult(file: File): SearchResult {
        val icon = R.drawable.file

        return SearchResult(
            name = file.name,
            path = file.parent ?: "",
            file = file,
            iconRes = icon,
            size = file.length(),
            lastModified = file.lastModified()
        )
    }

}
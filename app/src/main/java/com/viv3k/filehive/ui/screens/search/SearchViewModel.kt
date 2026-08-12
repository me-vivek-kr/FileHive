package com.viv3k.filehive.ui.screens.search

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.viv3k.filehive.ui.components.FileIcons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

data class SearchResult(
    val name: String,
    val path: String,
    val file: File,
    val iconRes: Int,
    val size: Long,
    val lastModified: Long
)

// Simple DataStore instance setup
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SearchViewModel(private val dataStore: DataStore<Preferences>) : ViewModel() {

    private val RECENT_SEARCH_KEY = stringPreferencesKey("recent_queries_ordered")
    private val LEGACY_RECENT_SEARCH_KEY = stringSetPreferencesKey("recent_queries")
    private val MAX_RECENT_SEARCHES = 3

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // Recent searches - observing from DataStore
    val recentSearches: StateFlow<List<String>> = dataStore.data
        .map { prefs ->
            prefs.getRecentSearches()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var searchJob: Job? = null

    // Root directory to start search from
    private val rootDir = File("/storage/emulated/0")

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
        searchJob?.cancel()
        if (newQuery.length < 2) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }
        searchJob = viewModelScope.launch {
            delay(500)
            _isSearching.value = true
            performSearch(newQuery, _selectedFilter.value)
        }
    }

    fun onFilterSelected(filter: String) {
        val nextFilter = if (_selectedFilter.value == filter) "All" else filter
        _selectedFilter.value = nextFilter
        // Re-run search with new filter if query exists
        val currentQuery = _searchQuery.value
        if (currentQuery.isNotEmpty()) {
            searchJob?.cancel()
            searchJob = viewModelScope.launch {
                _isSearching.value = true
                performSearch(currentQuery, nextFilter)
            }
        }
    }

    fun addToRecent(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            dataStore.edit { prefs ->
                val newList = prefs.getRecentSearches().toMutableList()
                newList.remove(query)
                newList.add(0, query)

                prefs.setRecentSearches(newList)
            }
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs.remove(RECENT_SEARCH_KEY)
                prefs.remove(LEGACY_RECENT_SEARCH_KEY)
            }
        }
    }

    fun deleteRecentSearch(query: String) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs.setRecentSearches(prefs.getRecentSearches().filterNot { it == query })
            }
        }
    }

    fun onRecentSearchClick(query: String) {
        onQueryChange(query)
        addToRecent(query)
    }

    private fun Preferences.getRecentSearches(): List<String> {
        val orderedSearches = this[RECENT_SEARCH_KEY]?.let { stored ->
            runCatching { Json.decodeFromString<List<String>>(stored) }.getOrDefault(emptyList())
        }

        return (orderedSearches ?: this[LEGACY_RECENT_SEARCH_KEY]?.toList().orEmpty())
            .take(MAX_RECENT_SEARCHES)
    }

    private fun MutablePreferences.setRecentSearches(searches: List<String>) {
        this[RECENT_SEARCH_KEY] = Json.encodeToString(searches.take(MAX_RECENT_SEARCHES))
        remove(LEGACY_RECENT_SEARCH_KEY)
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
            if (file.isDirectory && !file.isHidden) {
                recursiveSearch(file, query, filter, resultList)
            }
        }
    }

    private fun isValidType(file: File, filter: String): Boolean {
        if (filter == "All") return true
        if (file.isDirectory) return false

        val ext = file.extension.lowercase()
        return when (filter) {
            "Documents" -> ext in listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt")
            "Images" -> ext in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp")
            "Video" -> ext in listOf("mp4", "mkv", "avi", "mov", "3gp")
            "Audio" -> ext in listOf("mp3", "wav", "aac", "m4a", "flac")
            "Archives" -> ext in listOf("zip", "rar", "7z", "tar", "gz")
            else -> true
        }
    }

    private fun mapFileToResult(file: File): SearchResult {
        return SearchResult(
            name = file.name,
            path = file.parent ?: "",
            file = file,
            iconRes = FileIcons.getIcon(file),
            size = file.length(),
            lastModified = file.lastModified()
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val context = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Context
                SearchViewModel(context.dataStore)
            }
        }
    }

}

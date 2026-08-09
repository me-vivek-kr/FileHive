package com.viv3k.filehive.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.viv3k.filehive.R
import com.viv3k.filehive.ui.components.DotsTypingIndicator
import com.viv3k.filehive.ui.components.NeomorphicBottomNav
import com.viv3k.filehive.ui.utils.soft
import java.io.File

data class FilterItem(
    val name: String,
    val icon: Int
)

@Composable
fun SearchScreen(
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onFilesClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    viewModel: SearchViewModel = viewModel(factory = SearchViewModel.Factory)
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()

    SearchScreenContent(
        searchQuery = searchQuery,
        searchResults = searchResults,
        isSearching = isSearching,
        selectedFilter = selectedFilter,
        recentSearches = recentSearches,
        onQueryChange = viewModel::onQueryChange,
        onFilterSelected = viewModel::onFilterSelected,
        onRecentSearchClick = viewModel::onRecentSearchClick,
        onSearchAction = { viewModel.addToRecent(searchQuery) },
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
        onFilesClick = onFilesClick,
        onSettingsClick = onSettingsClick
    )
}

@Composable
fun SearchScreenContent(
    searchQuery: String,
    searchResults: List<SearchResult>,
    isSearching: Boolean,
    selectedFilter: String,
    recentSearches: List<String>,
    onQueryChange: (String) -> Unit,
    onFilterSelected: (String) -> Unit,
    onRecentSearchClick: (String) -> Unit,
    onSearchAction: () -> Unit,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onFilesClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val filters = listOf(
        FilterItem("Images", R.drawable.image),
        FilterItem("Video", R.drawable.video),
        FilterItem("Audio", R.drawable.music),
        FilterItem("Documents", R.drawable.file_text),
        FilterItem("Archives", R.drawable.archive)
    )

    val sampleTopResult = remember {
        SearchResult(
            name = "Hero_Banner_Final.png",
            path = "/Internal/Download/",
            file = File("/storage/emulated/0/Download/Hero_Banner_Final.png"),
            iconRes = R.drawable.image_file,
            size = (4.2 * 1024 * 1024).toLong(),
            lastModified = System.currentTimeMillis()
        )
    }

    val backgroundColor = Color(0xFFF7F9FB)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .soft(
                            shape = CircleShape,
                            cornerRadius = 21.dp,
                            backgroundColor = Color.White,
                            blurRadius = 12.dp,
                            offsetY = 4.dp
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_left),
                        contentDescription = "Back",
                        tint = Color(0xFF1F2937),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Search",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = onQueryChange,
                onClearClick = { onQueryChange("") },
                onSearchDone = {
                    onSearchAction()
                    keyboardController?.hide()
                },
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
            ) {
                items(filters) { filter ->
                    FilterChip(
                        text = filter.name,
                        icon = filter.icon,
                        isSelected = selectedFilter == filter.name,
                        onClick = { onFilterSelected(filter.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (searchQuery.isEmpty()) {
                // -- SHOW RECENT SEARCHES --
                Text(
                    text = "Recent Searches",
                    color = Color(0xFF4B5563),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                val displayRecentSearches = if (recentSearches.isEmpty()) {
                    listOf("Q3 Financial Reports 2023", "Design System Assets")
                } else {
                    recentSearches
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    displayRecentSearches.forEach { recent ->
                        RecentSearchItem(
                            text = recent,
                            onClick = { onRecentSearchClick(recent) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // -- TOP RESULTS --
                Text(
                    text = "Top Results",
                    color = Color(0xFF5051D8),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                SearchResultItem(
                    result = sampleTopResult,
                    onDeleteOptionClick = {}
                )
            } else {
                // -- ACTIVE SEARCH RESULTS LOGIC --
                if (isSearching && searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        DotsTypingIndicator()
                    }
                } else {
                    Text(
                        text = "Top Results (${searchResults.size})",
                        color = Color(0xFF5051D8),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        searchResults.forEach { result ->
                            SearchResultItem(
                                result = result,
                                onDeleteOptionClick = {}
                            )
                        }
                    }

                    if (isSearching) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            DotsTypingIndicator()
                        }
                    }
                }
            }
        }

        // Bottom Navigation Bar
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        ) {
            NeomorphicBottomNav(
                activeTab = "search",
                onTabClick = { tab ->
                    when (tab) {
                        "home" -> onHomeClick()
                        "files" -> onFilesClick()
                        "settings" -> onSettingsClick()
                        "search" -> { /* Already on search screen */ }
                    }
                }
            )
        }
    }
}

@Composable
fun RecentSearchItem(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .soft(
                shape = RoundedCornerShape(30.dp),
                cornerRadius = 30.dp,
                backgroundColor = Color.White,
                blurRadius = 14.dp,
                offsetY = 6.dp
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.clock),
                    contentDescription = null,
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = text,
                color = Color(0xFF1F2937),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            Icon(
                painter = painterResource(id = R.drawable.arrow_up_right),
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Preview
@Composable
fun PreviewSearch() {
    SearchScreenContent(
        searchQuery = "",
        searchResults = emptyList(),
        isSearching = false,
        selectedFilter = "All",
        recentSearches = listOf("Q3 Financial Reports 2023", "Design System Assets"),
        onQueryChange = {},
        onFilterSelected = {},
        onRecentSearchClick = {},
        onSearchAction = {},
        onBackClick = {},
        onHomeClick = {},
        onFilesClick = {},
        onSettingsClick = {}
    )
}

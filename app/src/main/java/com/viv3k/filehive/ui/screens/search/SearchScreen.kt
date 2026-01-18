package com.viv3k.filehive.ui.screens.search

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.viv3k.filehive.R
import com.viv3k.filehive.ui.components.DotsTypingIndicator

data class FilterItem(
    val name: String,
    val icon: Int
)

@Composable
fun SearchScreen(
    onBackClick: () -> Unit = {},
    viewModel: SearchViewModel = viewModel()
){
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current



    val filters = listOf(
        FilterItem("All", R.drawable.search),
        FilterItem("Documents", R.drawable.file_text),
        FilterItem("Images", R.drawable.image),
        FilterItem("Videos", R.drawable.video),
        FilterItem("Audio", R.drawable.ic_launcher_foreground),
        FilterItem("Archives", R.drawable.archive),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1115))
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        //Search Bar
        SearchBar(
            query = searchQuery,
            onQueryChange = { viewModel.onQueryChange(it) },
            onClearClick = { viewModel.onQueryChange("")  },
            onSearchDone = {
                viewModel.addToRecent(searchQuery)
                keyboardController?.hide()
            },
            onBackClick = onBackClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filters) { filter ->
                FilterChip(
                    text = filter.name,
                    icon = filter.icon,
                    isSelected = selectedFilter == filter.name,
                    onClick = { viewModel.onFilterSelected(filter.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Switch Content based on Query
        if (searchQuery.isEmpty()) {
            // -- SHOW RECENT SEARCHES --
            Text(
                text = "Recent Searches",
                color = Color(0xFF9AA0A6),
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(recentSearches) { recent ->
                    RecentSearchItem(
                        text = recent,
                        onClick = { viewModel.onRecentSearchClick(recent) }
                    )
                }
            }
        } else {
            // -- SEARCH RESULTS LOGIC --

            // Case 1: Searching but 0 results found yet -> Centered Loader
            if (isSearching && searchResults.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    DotsTypingIndicator()
                }
            }
            else {
                // SHOW SEARCH RESULTS
                Text(
                    text = "${searchResults.size} results for \"$searchQuery\"",
                    color = Color(0xFF9AA0A6),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults) { result ->
                        SearchResultItem(
                            result = result,
                            onDeleteOptionClick = {}
                        )
                    }

                    // Case 2: Searching and has results -> Loader at bottom
                    if (isSearching) {
                        item {
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
        }
    }
}

@Composable
fun RecentSearchItem(
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.clock),
            contentDescription = null,
            tint = Color(0xFF9AA0A6),
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp
        )
    }
}

@Preview
@Composable
fun PreviewSearch(){
    SearchScreen()
}
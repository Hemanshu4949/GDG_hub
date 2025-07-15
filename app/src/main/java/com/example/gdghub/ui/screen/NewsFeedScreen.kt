package com.example.gdghub.ui.screens // Replace with your actual package

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.semantics.error
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gdghub.model.NewsArticle
import com.example.gdghub.ui.screen.ClickableTagChip
import com.example.gdghub.ui.viewModel.NewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsFeedScreen(
    newsViewModel: NewsViewModel = viewModel(), // Obtain ViewModel
    onNavigateToDetail: (newsId: String) -> Unit
) {

    val filteredNews by newsViewModel.filteredNewsFeed.collectAsState()
    val isLoadingFeed by newsViewModel.isLoadingFeed.collectAsState() // For initial load
    val isFiltering by newsViewModel.isFiltering.collectAsState() // For Gemini filtering process
    val error by newsViewModel.error.collectAsState()
    val currentTags by newsViewModel.userPreferredTags.collectAsState()



    Scaffold(
        topBar = {
            TopAppBar(                title = { Text("GDG News " + if (currentTags.isNotEmpty()) "(${currentTags.joinToString()})" else "") })
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            if (currentTags.isNotEmpty()) {
                Button(
                    onClick = { newsViewModel.clearFiltersAndShowAllNews() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Clear Filter (Show All)")
                }
            }

            // Handle loading and error states
            if (isLoadingFeed && filteredNews.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                    Text(" Loading feed...")
                }
            } else if (isFiltering) {
                    Box(modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                        Text(" Personalizing feed with Gemini...")
                    }
                } else if (error != null) {
                    Box(modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Error: $error", color = MaterialTheme.colorScheme.error)
                    }
                } else if (filteredNews.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (currentTags.isNotEmpty()) "No news found for '${currentTags.joinToString()}'." else "No news articles available.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredNews, key = { article -> article.id }) { article ->
                        NewsListItem(
                            article = article,
                            onItemClick = { onNavigateToDetail(article.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NewsListItem(article: NewsArticle, onItemClick: () -> Unit , newsViewModel: NewsViewModel? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick), // Make the whole card clickable
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = article.title, style = MaterialTheme.typography.titleLarge)
            // ... other content ...
            if (article.tags.isNotEmpty() && newsViewModel != null) {
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    article.tags.forEach { tag ->
                        ClickableTagChip( // Re-use the composable
                            tag = tag,
                            onClick = { newsViewModel.filterNewsByTag(tag) }
                        )
                        Spacer(Modifier.width(8.dp))

                    }
                }
            }
            // Add more elements like author, date, tags if needed
        }
    }
}

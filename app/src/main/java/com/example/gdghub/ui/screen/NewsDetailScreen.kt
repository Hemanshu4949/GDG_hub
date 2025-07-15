package com.example.gdghub.ui.screen

import android.util.Log
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.gdghub.model.NewsArticle
import com.example.gdghub.ui.viewModel.NewsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    newsViewModel: NewsViewModel,
    newsId: String?, // newsId passed from navigation
    onBack: () -> Unit
) {
    val article by newsViewModel.detailedArticle.collectAsState()
    val isLoading by newsViewModel.isLoadingDetail.collectAsState()
    val error by newsViewModel.error.collectAsState() // This error state is shared, consider specific detail error if needed

    // Effect to load the article when newsId is present and changes
    LaunchedEffect(newsId) {
        if (!newsId.isNullOrBlank() && newsId != "null") { // Check for "null" string if passed as nav arg
            newsViewModel.loadNewsArticleById(newsId)
        } else if (newsId.isNullOrBlank()) {
            // Handle case where newsId is genuinely missing or invalid if needed
            // For now, it will show "No News ID provided" or specific error message from ViewModel
        }
    }

    // Effect to clear the article when the composable leaves the composition
    DisposableEffect(Unit) {
        onDispose {
            newsViewModel.clearDetailedArticle()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(article?.title ?: "News Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Box( // Use Box to easily center loading/error messages
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            contentAlignment = Alignment.Center // Center content like CircularProgressIndicator or error Text
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator()
                }
                error != null && article == null -> { // Show error only if article isn't loaded
                    Text(
                        text = "Error: $error",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                article != null -> {
                    // Display the full article details
                    // ...
                        // Display the full article details
                        NewsArticleDetailContent(
                            article = article!!,
                            newsViewModel = newsViewModel // <-- Add this line
                        )
                }
                newsId.isNullOrBlank() || newsId == "null" -> {
                    Text(
                        text = "No News ID provided or invalid.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                else -> {
                    // This case might be hit if newsId is valid but article is still null
                    // and not loading, and no specific error was set for "not found".
                    Text(
                        text = "Article details are not available.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun NewsArticleDetailContent(article: NewsArticle, newsViewModel: NewsViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize() // Take full size within the parent Box's padding
            .verticalScroll(rememberScrollState()) // Make content scrollable
    ) {
        // Display Image if URL exists
        Log.d("imagevalue" , article.imageUrl.toString())
        if (!article.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(article.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = article.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 250.dp), // Constrain image height
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Title
        Text(
            text = article.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Author and Timestamp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "By: ${article.author.ifBlank { "Unknown Author" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatTimestamp(article.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Content
        Text(
            text = article.content,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))


        if (article.tags.isNotEmpty()) {
            Text(
                text = "Tags:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()), // Make the Row of chips scrollable
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Spacing between chips
            ) {
                article.tags.forEach { tag ->
                    ClickableTagChip( // Use a new composable for clarity
                        tag = tag,
                        onClick = {
                            Log.d("NewsDetailScreen", "Tag clicked: $tag")
                            newsViewModel.filterNewsByTag(tag) // Call the ViewModel function
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        // Optional: Button to clear filters
        Button(onClick = { newsViewModel.clearFiltersAndShowAllNews() }) {
            Text("Show All News")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClickableTagChip(tag: String, onClick: () -> Unit) {
    SuggestionChip(
        onClick = onClick, // Make the chip itself clickable
        label = { Text(tag, style = MaterialTheme.typography.labelSmall) },
        modifier = Modifier // Add clickable modifier here if SuggestionChip's onClick isn't sufficient or for visual feedback
        // .clickable { onClick() } // Usually SuggestionChip's onClick is enough
    )
}


// Helper function to format timestamp (put in a utils file ideally)
fun formatTimestamp(timestamp: Long): String {
    if (timestamp == 0L) return "Date unknown"
    val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// Simple Chip Composable (you can customize this further)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChipView(text: String) {
    SuggestionChip(
        onClick = { /* Handle chip click if needed */ },
        label = { Text(text, style = MaterialTheme.typography.labelSmall) }
    )
}

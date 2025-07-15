package com.example.gdghub.ui.viewModel

import android.location.Geocoder.isPresent
import android.util.Log
import android.util.Log.e
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gdghub.data.FirebaseNewsRepository // Assuming this is your updated repo
import com.example.gdghub.data.NewsRepository
import com.example.gdghub.model.NewsArticle
import com.example.gdghub.ui.screen.NewsDetailScreen
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerateContentResponse
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update // For easier state updates
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray



class NewsViewModel(
    private val repository: NewsRepository = FirebaseNewsRepository() // Use DI in a real app
) : ViewModel() {

    private val _newsFeed = MutableStateFlow<List<NewsArticle>>(emptyList())
    val newsFeed: StateFlow<List<NewsArticle>> = _newsFeed.asStateFlow()

    // State for the currently selected/detailed news article
    private val _detailedArticle = MutableStateFlow<NewsArticle?>(null)
    val detailedArticle: StateFlow<NewsArticle?> = _detailedArticle.asStateFlow()

    private val _isLoadingFeed = MutableStateFlow(false)
    val isLoadingFeed: StateFlow<Boolean> = _isLoadingFeed.asStateFlow()

    private val _isLoadingDetail = MutableStateFlow(false)
    val isLoadingDetail: StateFlow<Boolean> = _isLoadingDetail.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Assume you have a way to get user preferences, e.g., from a DataStore or another service
    private val _userPreferredTags = MutableStateFlow<List<String>>(listOf())
    val userPreferredTags: StateFlow<List<String>> = _userPreferredTags.asStateFlow()

    private val _filteredNewsFeed = MutableStateFlow<List<NewsArticle>>(emptyList())
    val filteredNewsFeed: StateFlow<List<NewsArticle>> = _filteredNewsFeed.asStateFlow()

    private val _isFiltering = MutableStateFlow(false)
    val isFiltering: StateFlow<Boolean> = _isFiltering.asStateFlow()

    private val _errorDetail = MutableStateFlow<String?>(null) // Specific error for detail screen
    val errorDetail: StateFlow<String?> = _errorDetail.asStateFlow()

    private var generativeModel: GenerativeModel? = null

    init {
//         Initialize Gemini Model (Do this securely and potentially on a background thread)
         viewModelScope.launch {
             try {
                 generativeModel = Firebase.ai(backend = GenerativeBackend.googleAI())
                     .generativeModel("gemini-2.5-flash")
             } catch (e: Exception) {
                 Log.e("NewsViewModel", "Failed to initialize Gemini model", e)
                 _error.value = "Failed to initialize personalization service."
             }
         }
        loadNewsFeed() // Load feed when ViewModel is created

        viewModelScope.launch {
            _newsFeed.collect { allNews ->
                if (_userPreferredTags.value.isEmpty()) {
                    _filteredNewsFeed.value = allNews
                    Log.d("NewsViewModel", "No active filters, showing all news.")
                } else {
                    // If there ARE preferred tags, a filter operation should have already run
                    // or will be triggered by a specific action.
                    // This path ensures that if filters are cleared, we revert to all news.
                    Log.d(
                        "NewsViewModel",
                        "News feed updated, user tags exist: ${_userPreferredTags.value}. Filtering might be active."
                    )
                    // Consider if a re-filter is needed here or if it's handled by specific filter actions.
                    // For manual filtering, this might not need to do much if filterNewsWithGemini
                    // is called explicitly.
                }
            }
        }

//        viewModelScope.launch {
//            combine(_newsFeed, _userPreferredTags) { feed, tags ->
//                Pair(feed, tags)
//            }.debounce(500) // Avoid rapid refiltering
//                .collect { (feed, tags) ->
//                    if (feed.isNotEmpty() && tags.isNotEmpty() && generativeModel != null) {
//                        filterNewsWithGemini()
//                    } else if (generativeModel != null) {
//                        // If tags are empty, show all news or based on some default
//                        _filteredNewsFeed.value = feed
//                    }
//                }
//        }
    }





    fun filterNewsByTag(tag: String) {
        Log.d("NewsViewModel", "Filtering by single tag: $tag")
        if (tag.isBlank()) {
            clearFiltersAndShowAllNews()
            return
        }
        // Set the current preferred tags to just this one tag for filtering.
        // You could also implement logic to add/remove from a list of multiple filter tags.
        _userPreferredTags.value = listOf(tag)
        filterNewsWithGemini()
    }

    fun clearFiltersAndShowAllNews() {
        Log.d("NewsViewModel", "Clearing all filters.")
        _userPreferredTags.value = emptyList()
        _filteredNewsFeed.value = _newsFeed.value // Display the full, unfiltered feed
        _isFiltering.value = false // Ensure loading state is reset
    }

    fun filterNewsWithGemini() {
        val currentModel = generativeModel
        if (currentModel == null) {
            _error.value = "Personalization service not ready."
            Log.w("NewsViewModel", "Firebase Gemini model not initialized. Cannot filter.")
            _filteredNewsFeed.value = _newsFeed.value // Fallback
            return
        }

        if (_newsFeed.value.isEmpty()) {
            Log.i("NewsViewModel", "News feed is empty, nothing to filter.")
            _filteredNewsFeed.value = emptyList()
            return
        }

        val currentPreferredTags = _userPreferredTags.value
        if (currentPreferredTags.isEmpty()) {
            Log.i("NewsViewModel", "No preferred tags set for filtering. Showing all news.")
            _filteredNewsFeed.value = _newsFeed.value
            return
        }

        viewModelScope.launch {
            _isFiltering.value = true
            _error.value = null
            try {
                val allArticles = _newsFeed.value
//                val userPrefs = _userPreferredTags.value

                val prompt = constructGeminiPrompt(allArticles, currentPreferredTags)
                Log.d("NewsViewModel", "Firebase Gemini Prompt for manual filter: $prompt")
                // Make the call to Gemini API
                // This is a simplified example. Refer to official Gemini SDK docs for robust implementation.
                val response: GenerateContentResponse = withContext(Dispatchers.IO) { // Run network call on IO dispatcher
                    generativeModel!!.generateContent(prompt)
                }
                Log.d("NewsViewModel", "Gemini suggested article IDs:")
                val relevantArticleIds = parseGeminiResponse(response)
                Log.d("NewsViewModel", "Gemini suggested article IDs: $relevantArticleIds")
                val newFilteredList = allArticles.filter { article ->
                    relevantArticleIds.contains(article.id.trim())
                }
                Log.d("NewsViewModel", "Number of articles in newFilteredList: ${newFilteredList.size}")
                _filteredNewsFeed.value = newFilteredList

                if (newFilteredList.isEmpty() && relevantArticleIds.isNotEmpty()) {
                    Log.w("NewsViewModel", "Gemini suggested IDs but no matching articles found. IDs: $relevantArticleIds. Local IDs: ${allArticles.map { it.id }}")
                } else if (newFilteredList.isEmpty() && allArticles.isNotEmpty()) {
                    Log.i("NewsViewModel", "No articles matched Gemini suggestions from a non-empty list for tags: $currentPreferredTags")
                }

            } catch (e: Exception) {
                Log.e("NewsViewModel", "Failed to filter news with Firebase Gemini", e)
                _error.value = "Could not personalize news feed (Firebase Gemini): ${e.localizedMessage}"
                _filteredNewsFeed.value = _newsFeed.value // Fallback
            } finally {
                _isFiltering.value = false
            }
        }
    }


    private fun constructGeminiPrompt(articles: List<NewsArticle>, preferences: List<String>): String {
        val articlesString = articles.joinToString("\n") { article ->
            "Article ID: ${article.id}, Title: \"${article.title}\", Summary: \"${article.content.take(150)}...\", Tags: [${article.tags.joinToString(", ")}]"
        }
        // More sophisticated prompt engineering can yield better results.
        // You might want to instruct Gemini on the output format more explicitly (e.g., JSON).
        return """
        Given the following user preferences (tags they are interested in):
        [${preferences.joinToString(", ")}]

        And the following available news articles:
        $articlesString

        Please identify which articles are most relevant to the user's preferences.
        Return a JSON array of objects, where each object contains the "id" of the relevant article
        and a brief "reason" (1-2 sentences) why it's relevant.
        For example: [{"id": "some-id-123", "reason": "This article directly discusses AI, a user preference."}, ...]
        Only include articles that strongly match at least one of the user's preferred tags.
        If no articles are relevant, return an empty JSON array.
    """.trimIndent()
    }
    private fun parseGeminiResponse(response: GenerateContentResponse): Set<String> {
        try {
            // This parsing logic depends heavily on how you instruct Gemini to format the output.
            // The example prompt asks for JSON.
            val textResponse = response.text?.trim() ?: return emptySet()
            Log.d("NewsViewModel", "Raw Gemini Response: $textResponse")


            // Basic text cleaning if it's not perfect JSON
            val firstBracket = textResponse.indexOf('[')
            val lastBracket = textResponse.lastIndexOf(']')
            if (firstBracket == -1 || lastBracket == -1 || lastBracket < firstBracket) {
                Log.e("NewsViewModel", "Could not find JSON array in Gemini response.")
                return emptySet()
            }
            val jsonString = textResponse.substring(firstBracket, lastBracket + 1)
            val jsonArray = JSONArray(jsonString)
            val ids = mutableSetOf<String>()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                if (jsonObject.has("id")) {
                    ids.add(jsonObject.getString("id"))
                }
            }
            return ids
        } catch (e: Exception) {
            Log.e("NewsViewModel", "Error parsing Gemini response: ${e.message}", e)
            // Attempt to extract IDs even if reasoning or format is slightly off,
            // by looking for "Article ID: " patterns if JSON parsing fails.
            // This is a very basic fallback.
            val fallbackIds = mutableSetOf<String>()
            val idPattern = "Article ID: (\\S+)".toRegex()
            response.text?.let {
                idPattern.findAll(it).forEach { matchResult ->
                    fallbackIds.add(matchResult.groupValues[1].removeSuffix(","))
                }
            }
            if (fallbackIds.isNotEmpty()) {
                Log.w("NewsViewModel", "Used fallback ID extraction from Gemini response.")
                return fallbackIds
            }
            return emptySet()
        }
    }

    // Ensure you call loadNewsFeed to populate _newsFeed before attempting to filter
// You might also want to filter immediately after _newsFeed is updated and user preferences are known.
// Example of how you might observe changes to trigger filtering:
// (This is conceptual and needs proper coroutine context and flow combination)






    fun loadNewsFeed() {
        viewModelScope.launch {
            _isLoadingFeed.value = true
            _error.value = null // Clear previous errors
            try {
                _newsFeed.value = repository.getAllNews()
                if (_userPreferredTags.value.isEmpty()) {
                    _filteredNewsFeed.value = _newsFeed.value
                } else {
                    // If there are preferred tags from a previous session or default,
                    // you might want to trigger a filter here.
                    // However, for purely manual click-based filtering, this might not be needed.
                    // filterNewsWithGemini() // Or rely on the user to click a tag.
                }
                Log.i("NewsViewModel", "News feed loaded. Total articles: ${_newsFeed.value.size}")
            } catch (e: Exception) {
                Log.e("NewsViewModel", "Failed to load news feed", e)
                _error.value = "Failed to load news feed: ${e.localizedMessage}"
                _newsFeed.value = emptyList() // Ensure it's empty on error
                _filteredNewsFeed.value = emptyList()
            } finally {
                _isLoadingFeed.value = false
            }
        }
    }

    /**
     * Loads a specific news article by its ID and updates the detailedArticle StateFlow.
     */
    fun loadNewsArticleById(newsId: String) {
        viewModelScope.launch {
            Log.d("NewsViewModel", "Attempting to load article by ID: $newsId")
            _isLoadingDetail.value = true
            _errorDetail.value = null // Clear previous detail-specific errors
            _detailedArticle.value = null    // Clear previous article detail immediately

            try {
                val article = repository.getNewsById(newsId) // Assuming this is a suspend function
                if (article != null) {
                    Log.i("NewsViewModel", "Article found: ${article.title}")
                    _detailedArticle.value = article
                } else {
                    Log.w("NewsViewModel", "News article with ID '$newsId' not found in repository.")
                    _errorDetail.value = "News article not found."
                }
            } catch (e: Exception) {
                Log.e("NewsViewModel", "Failed to load news article with ID '$newsId'", e)
                _errorDetail.value = "Failed to load news details: ${e.localizedMessage}"
            } finally {
                _isLoadingDetail.value = false
                Log.d("NewsViewModel", "Finished loading article by ID: $newsId, isLoading: ${_isLoadingDetail.value}, article title: ${_detailedArticle.value?.title}")
            }
        }
    }


    /**
     * Clears the detailed article. Useful when navigating away from the detail screen.
     */
    fun clearDetailedArticle() {
        _detailedArticle.value = null
        _isLoadingDetail.value = false
        _errorDetail.value = null
    }


    fun addNewsArticle(article: NewsArticle) {
        viewModelScope.launch {
            // Simplified for brevity, assuming repository.addNewsArticle handles its own loading/error
            // and returns the ID or null
            val generatedId = repository.addNewsArticle(article)
            if (generatedId != null) {
                Log.i("NewsViewModel", "Article added with ID: $generatedId. Reloading feed.")

                loadNewsFeed() // Reload feed to see the new article
            } else {
                Log.e("NewsViewModel", "Failed to add article.")
                _error.value = "Failed to add the new article."
            }
        }
    }

    // exampleUsage might call addNewsArticle
    fun exampleUsage() {
        val newArticle = NewsArticle(
            imageUrl = "https://github.com/AntonioCardenas/GDG-ASSETS",
            title = "Gem AI",
            content = "Gem AI event by GDG",
            author = "GDG Organizers",
            tags = listOf("Event" , "GDG " , "Gemai" , "Ai"),
            // Ensure timestamp, imageUrl are set if needed by your model/Firestore rules
        )
        addNewsArticle(newArticle)
    }
}

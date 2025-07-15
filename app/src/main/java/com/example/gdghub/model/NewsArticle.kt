package com.example.gdghub.model

data class NewsArticle(
    var id: String = "", // Assuming you have an ID, make it mutable if you set it later
    val title: String = "",
    val content: String = "",
    val author: String = "",
    val timestamp: Long = System.currentTimeMillis(), // Optional: if you store a timestamp
    var imageUrl: String? = null, // Optional: if you have images
    val tags: List<String> = emptyList()
)
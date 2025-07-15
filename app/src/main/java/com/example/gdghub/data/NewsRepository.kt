package com.example.gdghub.data // Or your chosen package

import android.app.DownloadManager
import android.util.Log
import android.util.Log.e
import com.example.gdghub.model.NewsArticle
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlin.jvm.java


interface NewsRepository {
    suspend fun getAllNews(): List<NewsArticle>
    suspend fun getNewsById(newsId: String): NewsArticle?
    suspend fun addNewsArticle(article: NewsArticle): String?
}

class FirebaseNewsRepository : NewsRepository {
    private val db = FirebaseFirestore.getInstance()
    private val newsCollection = db.collection("news") // "news" is your collection name in Firestore


    override suspend fun getAllNews(): List<NewsArticle> {
        return try {
            val querySnapshot = newsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            querySnapshot.documents.mapNotNull { document ->
                val article = document.toObject(NewsArticle::class.java)
                article?.apply { this.id = document.id } // Assign Firestore document ID
            }
        } catch (e: Exception) {
            Log.e("FirebaseNewsRepository", "Error fetching all news", e)
            emptyList()
        }
    }


    override suspend fun getNewsById(newsId: String): NewsArticle? {
        return try {
            val documentSnapshot = newsCollection
                .document(newsId)
                .get()
                .await()
            if (documentSnapshot.exists()) {
                val article = documentSnapshot.toObject(NewsArticle::class.java)
                article?.apply { this.id = documentSnapshot.id } // Assign Firestore document ID
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FirebaseNewsRepository", "Error fetching news by ID: $newsId", e)
            null
        }
    }
  override  suspend fun addNewsArticle(article: NewsArticle): String? { // Returns generated ID
        return try {
            val documentReference = newsCollection.add(article).await()
            documentReference.id
        } catch (e: Exception) {
            Log.e("FirebaseNewsRepository", "Error adding news article", e)
            null
        }
    }
}
    
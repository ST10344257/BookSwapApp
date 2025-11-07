package com.example.bookswap.data.api

import com.example.bookswap.data.api_models.GoogleBookResponse // ✅ Updated import
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleBooksApi {
    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 40,
        @Query("orderBy") orderBy: String = "relevance",
        @Query("key") apiKey: String
    ): GoogleBookResponse // ✅ Updated return type
}

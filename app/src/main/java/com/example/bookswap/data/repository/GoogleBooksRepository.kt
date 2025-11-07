package com.example.bookswap.data.repository

import android.util.Log
import com.example.bookswap.data.Result
import com.example.bookswap.data.api.GoogleBooksApi
import com.example.bookswap.data.models.Book
import com.example.bookswap.data.models.BookCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GoogleBooksRepository {
    private val apiKey = "AIzaSyAB8Gn-UErTVe1PYgQsAmH-8AHIyq879Os"
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com/books/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val googleBooksApi = retrofit.create(GoogleBooksApi::class.java)

    suspend fun searchBooks(query: String): Result<List<Book>> {
        return withContext(Dispatchers.IO) {
            try {
                val searchQuery = if (query.isEmpty()) "textbook" else query
                val response = googleBooksApi.searchBooks(
                    query = searchQuery,
                    maxResults = 40,
                    apiKey = apiKey
                )

                val books = response.items?.mapNotNull { item ->
                    val volumeInfo = item.volumeInfo

                    if (volumeInfo.title.isEmpty()) return@mapNotNull null

                    // ✅ Get buyLink with fallback
                    val buyLink = item.saleInfo?.buyLink?.takeIf { it.isNotEmpty() }
                        ?: "https://books.google.com/books?id=${item.id}"

                    Log.d("GoogleBooks", "Book: ${volumeInfo.title}")
                    Log.d("GoogleBooks", "  ID: ${item.id}")
                    Log.d("GoogleBooks", "  BuyLink from API: ${item.saleInfo?.buyLink}")
                    Log.d("GoogleBooks", "  Final BuyLink: $buyLink")

                    Book(
                        id = item.id,
                        bookId = item.id,
                        title = volumeInfo.title,
                        author = volumeInfo.authors?.firstOrNull() ?: "Unknown Author",
                        description = volumeInfo.description ?: "No description available",
                        imageUrl = volumeInfo.imageLinks?.thumbnail?.replace("http://", "https://") ?: "",
                        price = extractPrice(item.saleInfo),
                        category = categorizeBook(volumeInfo.categories?.firstOrNull() ?: ""),
                        isGoogleBook = true,
                        publishedDate = volumeInfo.publishedDate ?: "",
                        buyLink = buyLink // ✅ Use the buyLink with fallback
                    )
                }?.distinctBy { it.id } ?: emptyList()

                Log.d("GoogleBooks", "Found ${books.size} books total")
                Result.Success(books)

            } catch (e: Exception) {
                Log.e("GoogleBooks", "Error: ${e.message}", e)
                Result.Error(e)
            }
        }
    }

    private fun extractPrice(saleInfo: com.example.bookswap.data.api_models.SaleInfo?): Double {
        return try {
            val price = saleInfo?.retailPrice?.amount ?: saleInfo?.listPrice?.amount
            price ?: -1.0
        } catch (e: Exception) {
            -1.0
        }
    }

    private fun categorizeBook(googleCategory: String): BookCategory {
        return when {
            googleCategory.contains("Technology", ignoreCase = true) -> BookCategory.TECH
            googleCategory.contains("Computers", ignoreCase = true) -> BookCategory.TECH
            googleCategory.contains("Programming", ignoreCase = true) -> BookCategory.TECH
            googleCategory.contains("Law", ignoreCase = true) -> BookCategory.LAW
            googleCategory.contains("Legal", ignoreCase = true) -> BookCategory.LAW
            googleCategory.contains("Business", ignoreCase = true) -> BookCategory.BUSINESS
            googleCategory.contains("Economics", ignoreCase = true) -> BookCategory.BUSINESS
            googleCategory.contains("Science", ignoreCase = true) -> BookCategory.SCIENCE
            googleCategory.contains("Nature", ignoreCase = true) -> BookCategory.SCIENCE
            else -> BookCategory.OTHER
        }
    }
}
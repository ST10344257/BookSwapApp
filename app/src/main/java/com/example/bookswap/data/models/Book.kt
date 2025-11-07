package com.example.bookswap.data.models

import android.os.Parcelable
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Book(
    val id: String = "", // Changed from bookId for Google Books compatibility
    val bookId: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val title: String = "",
    val author: String = "",
    val isbn: String = "",
    val edition: String = "",
    val courseCode: String = "",
    val category: BookCategory = BookCategory.OTHER,
    val price: Double = 0.0,
    val condition: BookCondition = BookCondition.GOOD,
    val description: String = "",
    val photoUrls: List<String> = emptyList(),
    val status: BookStatus = BookStatus.AVAILABLE,
    val views: Int = 0,
    val listedAt: Long = System.currentTimeMillis(),
    val institution: String = "",
    val location: String = "",
    val ownerId: String = "",
    val imageUrl: String = "",

    // ✅ Google Books fields - only @get: annotation needed for val properties
    @get:PropertyName("isGoogleBook")
    val isGoogleBook: Boolean = false,

    val buyLink: String = "",
    val publishedDate: String = ""
) : Parcelable

enum class BookCategory {
    TECH,
    LAW,
    BUSINESS,
    SCIENCE,
    HUMANITIES,
    ENGINEERING,
    MEDICAL,
    OTHER
}

enum class BookCondition(val displayName: String, val priceMultiplier: Double) {
    NEW("Like New", 1.0),
    EXCELLENT("Excellent", 0.85),
    GOOD("Good", 0.70),
    FAIR("Fair", 0.50),
    POOR("Poor", 0.30)
}

enum class BookStatus {
    AVAILABLE,
    PENDING,
    SOLD,
    REMOVED
}
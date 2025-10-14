package com.example.bookswap.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WishlistItem(
    val wishlistId: String = "",
    val userId: String = "",
    val bookTitle: String = "",
    val author: String = "",
    val isbn: String = "",
    val maxPrice: Double? = null,
    val preferredCondition: BookCondition? = null,
    val notificationsEnabled: Boolean = true,
    val addedAt: Long = System.currentTimeMillis()
) : Parcelable
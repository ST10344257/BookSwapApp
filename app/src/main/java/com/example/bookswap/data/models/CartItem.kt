package com.example.bookswap.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartItem(
    val bookId: String = "",
    var book: Book? = null,
    val addedAt: Long = System.currentTimeMillis(),
    @Transient var isAvailable: Boolean = true  // Add this field
) : Parcelable
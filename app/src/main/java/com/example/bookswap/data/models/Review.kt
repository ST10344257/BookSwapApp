package com.example.bookswap.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Review(
    val reviewId: String = "",
    val transactionId: String = "",
    val reviewerId: String = "",
    val reviewerName: String = "",
    val revieweeId: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    val reviewType: ReviewType = ReviewType.SELLER,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

enum class ReviewType {
    SELLER,  // Review of the seller
    BUYER,   // Review of the buyer
    BOOK     // Review of the book condition accuracy
}
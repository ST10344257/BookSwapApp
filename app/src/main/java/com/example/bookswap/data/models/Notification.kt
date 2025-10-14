package com.example.bookswap.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Notification(
    val notificationId: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.GENERAL,
    val relatedId: String = "", // bookId, transactionId, etc.
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

enum class NotificationType {
    WISHLIST_MATCH,
    ORDER_UPDATE,
    MESSAGE,
    REVIEW_RECEIVED,
    BOOK_SOLD,
    GENERAL
}
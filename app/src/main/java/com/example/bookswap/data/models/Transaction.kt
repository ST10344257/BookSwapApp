package com.example.bookswap.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Transaction(
    val transactionId: String = "",
    val bookId: String = "",
    val bookTitle: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val amount: Double = 0.0,
    val status: TransactionStatus = TransactionStatus.PENDING,
    val paymentMethod: PaymentMethod = PaymentMethod.WALLET,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val trackingInfo: TrackingInfo? = null
) : Parcelable

@Parcelize
data class TrackingInfo(
    val orderNumber: String = "",
    val estimatedDelivery: Long = 0L,
    val currentStatus: String = "Order Placed",
    val updates: List<TrackingUpdate> = emptyList()
) : Parcelable

@Parcelize
data class TrackingUpdate(
    val status: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val description: String = ""
) : Parcelable

enum class TransactionStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    COMPLETED,
    CANCELLED,
    REFUNDED
}

enum class PaymentMethod {
    WALLET,
    BANK_ACCOUNT,
    CASH_ON_DELIVERY
}
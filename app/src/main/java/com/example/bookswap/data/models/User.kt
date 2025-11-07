package com.example.bookswap.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uid: String = "",
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val institution: String = "",
    val profilePictureUrl: String = "",
    val rating: Double = 0.0,
    val ratingCount: Int = 0,
    val booksListed: Int = 0,
    val booksSold: Int = 0,
    val walletBalance: Double = 0.0,
    val paymentPreference: PaymentPreference = PaymentPreference.WALLET,
    val createdAt: Long = System.currentTimeMillis(),
    val fcmToken: String = ""
) : Parcelable

enum class PaymentPreference {
    WALLET,
    BANK_ACCOUNT
}
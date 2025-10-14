package com.example.bookswap.utils

import java.text.NumberFormat
import java.util.Locale

object PriceUtils {

    fun formatPrice(price: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        return format.format(price)
    }

    fun calculateDiscountedPrice(originalPrice: Double, condition: com.example.bookswap.data.models.BookCondition): Double {
        return originalPrice * condition.priceMultiplier
    }

    fun calculateTotal(prices: List<Double>): Double {
        return prices.sum()
    }
}
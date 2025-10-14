package com.example.bookswap.utils

object ValidationUtils {

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        // At least 6 characters
        return password.length >= 6
    }

    fun doPasswordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    fun isValidISBN(isbn: String): Boolean {
        val cleanIsbn = isbn.replace("-", "").replace(" ", "")
        return cleanIsbn.length == 10 || cleanIsbn.length == 13
    }

    fun isValidPrice(price: String): Boolean {
        return try {
            val priceValue = price.toDouble()
            priceValue > 0
        } catch (e: NumberFormatException) {
            false
        }
    }

    fun isValidName(name: String): Boolean {
        return name.trim().length >= 2
    }
}
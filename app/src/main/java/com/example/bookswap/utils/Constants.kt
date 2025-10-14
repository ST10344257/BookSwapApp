package com.example.bookswap.utils

object Constants {
    // Firebase Collections
    const val USERS_COLLECTION = "users"
    const val BOOKS_COLLECTION = "books"
    const val TRANSACTIONS_COLLECTION = "transactions"
    const val REVIEWS_COLLECTION = "reviews"
    const val WISHLIST_COLLECTION = "wishlist"
    const val NOTIFICATIONS_COLLECTION = "notifications"

    // Storage Paths
    const val BOOKS_IMAGES_PATH = "books"
    const val PROFILE_IMAGES_PATH = "profile_images"

    // SharedPreferences
    const val PREFS_NAME = "BookSwapPrefs"
    const val KEY_USER_ID = "userId"
    const val KEY_USER_EMAIL = "userEmail"
    const val KEY_IS_LOGGED_IN = "isLoggedIn"
    const val KEY_CART_ITEMS = "cartItems"

    // Request Codes
    const val RC_PICK_IMAGE = 1001
    const val RC_TAKE_PHOTO = 1002

    // Limits
    const val MAX_IMAGES_PER_BOOK = 5
    const val MAX_IMAGE_SIZE_MB = 5
    const val BOOKS_PER_PAGE = 20
}
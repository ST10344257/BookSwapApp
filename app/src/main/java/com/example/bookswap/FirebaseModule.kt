package com.example.bookswap

import com.example.bookswap.data.repository.AuthRepository
import com.example.bookswap.data.repository.BookRepository
import com.example.bookswap.data.repository.CartRepository
import com.example.bookswap.data.repository.TransactionRepository
import com.example.bookswap.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

/**
 * A simple singleton object to provide the same instance of Firebase services
 * and repositories throughout the app. This ensures that all parts of the app
 * share the same authentication state.
 */
object FirebaseModule {

    // Provide a single, shared instance of FirebaseAuth
    val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    // Provide a single, shared instance of FirebaseFirestore
    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    // Provide a single, shared instance of FirebaseStorage
    val storage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    // Provide repositories. They will use the shared instances from above.
    val authRepository: AuthRepository by lazy {
        AuthRepository()
    }

    val bookRepository: BookRepository by lazy {
        BookRepository()
    }

    val cartRepository: CartRepository by lazy {
        CartRepository()
    }

    val userRepository: UserRepository by lazy {
        UserRepository()
    }

    val transactionRepository: TransactionRepository by lazy {
        TransactionRepository()
    }
}

package com.example.bookswap.data.repository

import android.util.Log
import com.example.bookswap.data.Result
import com.example.bookswap.data.models.Book
import com.example.bookswap.data.models.BookStatus
import com.example.bookswap.data.models.CartItem
import com.example.bookswap.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await

class CartRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val bookRepository = BookRepository()

    suspend fun addToCart(userId: String, bookId: String): Result<Boolean> {
        return try {
            val cartItem = CartItem(
                bookId = bookId,
                addedAt = System.currentTimeMillis()
            )

            firestore.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .collection("cart")
                .document(bookId)
                .set(cartItem)
                .await()

            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getCartItems(userId: String): Result<List<CartItem>> {
        return try {
            val snapshot = firestore.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .collection("cart")
                .get()
                .await()

            val cartItems = snapshot.toObjects(CartItem::class.java).toMutableList()

            // Load full book details and check availability
            for (item in cartItems) {
                when (val result = bookRepository.getBook(item.bookId)) {
                    is Result.Success -> {
                        item.book = result.data
                    }
                    is Result.Error -> {
                        // Book might be deleted
                        item.book = null
                    }
                    is Result.Loading -> {}
                }
            }

            Result.Success(cartItems)
        } catch (e: FirebaseFirestoreException) {
            //offline error specifically
            if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                Log.d("CartRepository", "📵 Offline - returning empty cart (no cache)")
                Result.Success(emptyList()) // Return empty list when offline with no cache
            } else {
                Result.Error(e)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun removeFromCart(userId: String, bookId: String): Result<Boolean> {
        return try {
            firestore.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .collection("cart")
                .document(bookId)
                .delete()
                .await()

            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun clearCart(userId: String): Result<Boolean> {
        return try {
            val snapshot = firestore.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .collection("cart")
                .get()
                .await()

            snapshot.documents.forEach { it.reference.delete() }

            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun isInCart(userId: String, bookId: String): Result<Boolean> {
        return try {
            val doc = firestore.collection(Constants.USERS_COLLECTION)
                .document(userId)
                .collection("cart")
                .document(bookId)
                .get()
                .await()

            Result.Success(doc.exists())
        } catch (e: FirebaseFirestoreException) {
            // Handles offline error - assume NOT in cart when offline
            if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                Log.d("CartRepository", "📵 Offline - assuming item NOT in cart")
                Result.Success(false) // Safe default: assume not in cart
            } else {
                Result.Error(e)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
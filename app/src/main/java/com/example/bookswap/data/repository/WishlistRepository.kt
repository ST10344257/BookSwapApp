package com.example.bookswap.data.repository

import android.util.Log
import com.example.bookswap.data.Result
import com.example.bookswap.data.models.Book
import com.example.bookswap.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await

class WishlistRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun addToWishlist(userId: String, book: Book): Result<Boolean> {
        return try {
            val wishlistRef = firestore
                .collection(Constants.USERS_COLLECTION)
                .document(userId)
                .collection("wishlist")
                .document(book.id) // Use book.id for Google Books

            wishlistRef.set(book).await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun removeFromWishlist(userId: String, bookId: String): Result<Boolean> {
        return try {
            firestore
                .collection(Constants.USERS_COLLECTION)
                .document(userId)
                .collection("wishlist")
                .document(bookId)
                .delete()
                .await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getWishlist(userId: String): Result<List<Book>> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("wishlist")
                .get()
                .await()

            val books = snapshot.toObjects(Book::class.java)
            Result.Success(books)
        } catch (e: FirebaseFirestoreException) {
            // offline error specifically
            if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                Log.d("WishlistRepository", "📵 Offline - returning empty wishlist (no cache)")
                Result.Success(emptyList()) // Return empty list when offline with no cache
            } else {
                Result.Error(e)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun isInWishlist(userId: String, bookId: String): Result<Boolean> {
        return try {
            val doc = firestore.collection("users")
                .document(userId)
                .collection("wishlist")
                .document(bookId)
                .get()
                .await()

            Result.Success(doc.exists())
        } catch (e: FirebaseFirestoreException) {
            // offline error - assume NOT in wishlist when offline
            if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                Log.d("WishlistRepository", "📵 Offline - assuming item NOT in wishlist")
                Result.Success(false) // Safe default: assume not in wishlist
            } else {
                Result.Error(e)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun clearWishlist(userId: String): Result<Boolean> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("wishlist")
                .get()
                .await()

            snapshot.documents.forEach { it.reference.delete().await() }

            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

}
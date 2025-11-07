package com.example.bookswap.data.repository

import android.net.Uri
import com.example.bookswap.data.Result
import com.example.bookswap.data.models.Book
import com.example.bookswap.data.models.BookCategory
import com.example.bookswap.data.models.BookStatus
import com.example.bookswap.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class BookRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun addBook(book: Book, imageUris: List<Uri>): Result<String> {
        return try {
            // Generate book ID
            val bookRef = firestore.collection(Constants.BOOKS_COLLECTION).document()
            val bookId = bookRef.id

            // Upload images
            val photoUrls = uploadBookImages(bookId, imageUris)

            // Create book with photo URLs
            val bookWithPhotos = book.copy(bookId = bookId, photoUrls = photoUrls)

            // Save to Firestore
            bookRef.set(bookWithPhotos).await()

            Result.Success(bookId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private suspend fun uploadBookImages(bookId: String, imageUris: List<Uri>): List<String> {
        val urls = mutableListOf<String>()

        imageUris.forEachIndexed { index, uri ->
            val ref = storage.reference
                .child("${Constants.BOOKS_IMAGES_PATH}/$bookId/image_$index.jpg")

            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            urls.add(downloadUrl)
        }

        return urls
    }

    suspend fun getBook(bookId: String): Result<Book> {
        return try {
            val doc = firestore.collection(Constants.BOOKS_COLLECTION)
                .document(bookId)
                .get()
                .await()

            val book = doc.toObject(Book::class.java)
                ?: throw Exception("Book not found")

            // Increment view count
            incrementViews(bookId)

            Result.Success(book)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getAllBooks(limit: Int = 20): Result<List<Book>> {
        return try {
            // Get all books, filter client-side
            val snapshot = firestore.collection(Constants.BOOKS_COLLECTION)
                .limit(50) // Get more than needed
                .get()
                .await()

            val allBooks = snapshot.toObjects(Book::class.java)

            // Filter and sort in app
            val availableBooks = allBooks
                .filter { it.status == BookStatus.AVAILABLE }
                .sortedByDescending { it.listedAt }
                .take(limit)

            android.util.Log.d("BookRepository", "Found ${availableBooks.size} available books")

            Result.Success(availableBooks)
        } catch (e: Exception) {
            android.util.Log.e("BookRepository", "Error getting books", e)
            Result.Error(e)
        }
    }
    suspend fun getBooksByCategory(category: BookCategory, limit: Int = 20): Result<List<Book>> {
        return try {
            val snapshot = firestore.collection(Constants.BOOKS_COLLECTION)
                .whereEqualTo("category", category.name)
                .whereEqualTo("status", BookStatus.AVAILABLE.name)
                .limit(limit.toLong())
                .get()
                .await()

            val books = snapshot.toObjects(Book::class.java)
            Result.Success(books)
        } catch (e: Exception) {
            // Handle empty database gracefully
            if (e.message?.contains("FAILED_PRECONDITION") == true ||
                e.message?.contains("NOT_FOUND") == true) {
                Result.Success(emptyList())
            } else {
                Result.Error(e)
            }
        }
    }

    suspend fun searchBooks(query: String): Result<List<Book>> {
        return try {
            // Note: Firestore doesn't support full-text search well
            // For production, consider Algolia or similar service
            val snapshot = firestore.collection(Constants.BOOKS_COLLECTION)
                .whereEqualTo("status", BookStatus.AVAILABLE.name)
                .get()
                .await()

            val books = snapshot.toObjects(Book::class.java)
            val filtered = books.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.author.contains(query, ignoreCase = true) ||
                        it.isbn.contains(query, ignoreCase = true) ||
                        it.courseCode.contains(query, ignoreCase = true)
            }

            Result.Success(filtered)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getUserBooks(userId: String): Result<List<Book>> {
        return try {
            val snapshot = firestore.collection(Constants.BOOKS_COLLECTION)
                .whereEqualTo("sellerId", userId)
                .orderBy("listedAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val books = snapshot.toObjects(Book::class.java)
            Result.Success(books)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun updateBookStatus(bookId: String, status: BookStatus): Result<Boolean> {
        return try {
            firestore.collection(Constants.BOOKS_COLLECTION)
                .document(bookId)
                .update("status", status.name)
                .await()

            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun deleteBook(bookId: String): Result<Boolean> {
        return try {
            // Delete images from storage
            val storageRef = storage.reference.child("${Constants.BOOKS_IMAGES_PATH}/$bookId")
            storageRef.listAll().await().items.forEach { it.delete().await() }

            // Delete document
            firestore.collection(Constants.BOOKS_COLLECTION)
                .document(bookId)
                .delete()
                .await()

            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private suspend fun incrementViews(bookId: String) {
        try {
            val bookRef = firestore.collection(Constants.BOOKS_COLLECTION)
                .document(bookId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(bookRef)
                val currentViews = snapshot.getLong("views")?.toInt() ?: 0
                transaction.update(bookRef, "views", currentViews + 1)
            }.await()
        } catch (e: Exception) {
            // Fail silently for view counts
        }
    }
}
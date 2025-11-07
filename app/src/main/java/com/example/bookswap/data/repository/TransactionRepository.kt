package com.example.bookswap.data.repository

import com.example.bookswap.FirebaseModule
import com.example.bookswap.data.Result
import com.example.bookswap.data.models.BookStatus
import com.example.bookswap.data.models.Transaction
import com.example.bookswap.data.models.TransactionStatus
import com.example.bookswap.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class TransactionRepository {

    // single shared instances from FirebaseModule.
    // This ensures this repository has the correct authentication state and uses the same
    // instances of other repositories.
    private val firestore = FirebaseModule.firestore
    private val bookRepository = FirebaseModule.bookRepository
    private val userRepository = FirebaseModule.userRepository
    // --- END OF FIX ---

    private val transactionsCollection = firestore.collection(Constants.TRANSACTIONS_COLLECTION)

    suspend fun createTransaction(transaction: Transaction): Result<String> {
        return try {
            val transactionRef = transactionsCollection.document()
            val transactionId = transactionRef.id

            val transactionWithId = transaction.copy(transactionId = transactionId)

            // Run as a transaction to ensure consistency
            firestore.runTransaction { firestoreTransaction ->
                // Save transaction
                firestoreTransaction.set(transactionRef, transactionWithId)

                // Update book status
                val bookRef = firestore.collection(Constants.BOOKS_COLLECTION)
                    .document(transaction.bookId)
                firestoreTransaction.update(bookRef, "status", BookStatus.PENDING.name)
            }.await()

            sendBookSoldNotificationToSeller(transaction)

            Result.Success(transactionId)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private suspend fun sendBookSoldNotificationToSeller(transaction: Transaction) {
        try {
            // seller's FCM token from Firestore
            val sellerDoc = firestore.collection(Constants.USERS_COLLECTION)
                .document(transaction.sellerId)
                .get()
                .await()

            val sellerFcmToken = sellerDoc.getString("fcmToken")

            // notification document in Firestore (for in-app notifications)
            val notificationData = hashMapOf(
                "userId" to transaction.sellerId,
                "title" to "🎉 Your Book Has Been Bought!",
                "message" to "${transaction.buyerName} purchased your book: ${transaction.bookTitle}",
                "type" to "BOOK_SOLD",
                "relatedId" to transaction.bookId,
                "isRead" to false,
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("notifications")
                .add(notificationData)
                .await()

            android.util.Log.d("Transaction", "Notification created for seller ${transaction.sellerId}")

            // Note: Without Cloud Functions, we can't send push notifications directly from Android
            // But we can create a local notification if the seller is currently using the app

        } catch (e: Exception) {
            android.util.Log.e("Transaction", "Error sending notification: ${e.message}", e)
        }
    }

    suspend fun getUserTransactions(userId: String, asBuyer: Boolean = true): Result<List<Transaction>> {
        return try {
            val field = if (asBuyer) "buyerId" else "sellerId"

            val snapshot = transactionsCollection
                .whereEqualTo(field, userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val transactions = snapshot.toObjects(Transaction::class.java)
            Result.Success(transactions)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun updateTransactionStatus(
        transactionId: String,
        status: TransactionStatus
    ): Result<Boolean> {
        return try {
            val updates = mutableMapOf<String, Any>("status" to status.name)

            if (status == TransactionStatus.COMPLETED) {
                updates["completedAt"] = System.currentTimeMillis()
            }

            transactionsCollection
                .document(transactionId)
                .update(updates)
                .await()

            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun completeTransaction(transactionId: String): Result<Boolean> {
        return try {
            // Get transaction details
            val transactionDoc = transactionsCollection
                .document(transactionId)
                .get()
                .await()

            val transaction = transactionDoc.toObject(Transaction::class.java)
                ?: throw Exception("Transaction not found")

            // Update transaction status
            updateTransactionStatus(transactionId, TransactionStatus.COMPLETED)

            // Mark book as sold
            bookRepository.updateBookStatus(transaction.bookId, BookStatus.SOLD)

            // Update seller's wallet
            userRepository.updateWalletBalance(transaction.sellerId, transaction.amount)

            // Update seller's stats
            userRepository.incrementBookStats(transaction.sellerId, sold = true)

            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

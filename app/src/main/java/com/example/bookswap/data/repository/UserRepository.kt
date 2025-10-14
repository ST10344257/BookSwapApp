package com.example.bookswap.data.repository

import android.net.Uri
import com.example.bookswap.FirebaseModule // <-- IMPORT THE SHARED MODULE
import com.example.bookswap.data.Result
import com.example.bookswap.data.models.PaymentPreference
import com.example.bookswap.data.models.User
import com.example.bookswap.utils.Constants
import kotlinx.coroutines.tasks.await

class UserRepository {

    // --- THIS IS THE FIX ---
    // Use the single, shared instances from FirebaseModule.
    // This ensures this repository has the correct authentication state.
    private val firestore = FirebaseModule.firestore
    private val storage = FirebaseModule.storage
    // --- END OF FIX ---

    // This now uses the shared firestore instance
    private val usersCollection = firestore.collection(Constants.USERS_COLLECTION)

    suspend fun getUser(userId: String): Result<User> {
        return try {
            val doc = usersCollection.document(userId).get().await()

            val user = doc.toObject(User::class.java)
                ?: throw Exception("User not found")

            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun updateUser(userId: String, updates: Map<String, Any>): Result<Boolean> {
        return try {
            usersCollection.document(userId).update(updates).await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun updateUserName(userId: String, newName: String): Result<Boolean> {
        return updateUser(userId, mapOf("name" to newName))
    }

    suspend fun updateUserEmail(userId: String, newEmail: String): Result<Boolean> {
        return updateUser(userId, mapOf("email" to newEmail))
    }

    suspend fun uploadProfilePicture(userId: String, imageUri: Uri): Result<String> {
        return try {
            // The path here MUST match your Firebase Storage rules
            val ref = storage.reference.child("${Constants.PROFILE_IMAGES_PATH}/$userId/$userId.jpg")

            // 1. Upload to storage
            ref.putFile(imageUri).await()

            // 2. Get download URL
            val downloadUrl = ref.downloadUrl.await().toString()

            // 3. Update Firestore (This also requires correct Firestore rules)
            updateUser(userId, mapOf("profilePictureUrl" to downloadUrl))

            Result.Success(downloadUrl)
        } catch (e: Exception) {
            // This will catch permission errors from BOTH storage and firestore writes
            Result.Error(e)
        }
    }

    suspend fun updatePaymentPreference(
        userId: String,
        preference: PaymentPreference
    ): Result<Boolean> {
        return updateUser(userId, mapOf("paymentPreference" to preference.name))
    }

    suspend fun updateWalletBalance(userId: String, amount: Double): Result<Boolean> {
        return try {
            val userRef = usersCollection.document(userId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentBalance = snapshot.getDouble("walletBalance") ?: 0.0
                transaction.update(userRef, "walletBalance", currentBalance + amount)
            }.await()

            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun incrementBookStats(userId: String, sold: Boolean = false): Result<Boolean> {
        return try {
            val userRef = usersCollection.document(userId)
            val field = if (sold) "booksSold" else "booksListed"

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentCount = snapshot.getLong(field)?.toInt() ?: 0
                transaction.update(userRef, field, currentCount + 1)
            }.await()

            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}

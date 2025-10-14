package com.example.bookswap.data.repository

import com.example.bookswap.data.Result
import com.example.bookswap.data.models.User
import com.example.bookswap.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    suspend fun register(
        name: String,
        surname: String,
        email: String,
        password: String,
        institution: String = ""
    ): Result<User> {
        return try {
            // Create Firebase Auth user
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("User creation failed")

            // Create User object
            val user = User(
                uid = firebaseUser.uid,
                name = name,
                surname = surname,
                email = email,
                institution = institution
            )

            // Store user data in Firestore
            firestore.collection(Constants.USERS_COLLECTION)
                .document(firebaseUser.uid)
                .set(user)
                .await()

            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Login failed")

            // Fetch user data from Firestore
            val userDoc = firestore.collection(Constants.USERS_COLLECTION)
                .document(firebaseUser.uid)
                .get()
                .await()

            val user = userDoc.toObject(User::class.java)
                ?: throw Exception("User data not found")

            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Boolean> {
        return try {
            val user = auth.currentUser ?: throw Exception("No user logged in")
            val email = user.email ?: throw Exception("Email not found")

            // Re-authenticate user
            val credential = com.google.firebase.auth.EmailAuthProvider
                .getCredential(email, currentPassword)
            user.reauthenticate(credential).await()

            // Update password
            user.updatePassword(newPassword).await()

            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
}
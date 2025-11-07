package com.example.bookswap

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

object FCMHelper {

    private const val TAG = "FCMHelper"
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Initialize FCM and get the token
     */
    fun initializeFCM(context: Context) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Log.d(TAG, "FCM Token: $token")

            // Save to SharedPreferences
            saveTokenToPreferences(context, token)

            // Upload to Firestore
            uploadTokenToFirestore(token)
        }

        // Subscribe to general announcements
        subscribeToTopic("announcements")
    }

    /**
     * Save token to SharedPreferences
     */
    private fun saveTokenToPreferences(context: Context, token: String) {
        val prefs = context.getSharedPreferences("BookSwapPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()
        Log.d(TAG, "FCM token saved to SharedPreferences")
    }

    /**
     * Upload token to Firestore
     */
    fun uploadTokenToFirestore(token: String) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.w(TAG, "User not logged in, cannot save FCM token")
            return
        }

        val tokenData = hashMapOf(
            "fcmToken" to token,
            "userId" to userId,
            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
            "platform" to "android"
        )

        // Save to fcmTokens collection
        db.collection("fcmTokens")
            .document(userId)
            .set(tokenData)
            .addOnSuccessListener {
                Log.d(TAG, "FCM token uploaded successfully to fcmTokens collection")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error uploading FCM token", e)
            }

        // Also save to user document
        db.collection("users")
            .document(userId)
            .update("fcmToken", token)
            .addOnSuccessListener {
                Log.d(TAG, "FCM token updated in user document")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Could not update user document with token (may not exist yet)", e)
            }
    }

    /**
     * Subscribe to topic
     */
    fun subscribeToTopic(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Subscribed to topic: $topic")
                } else {
                    Log.e(TAG, "Failed to subscribe to topic: $topic", task.exception)
                }
            }
    }

    /**
     * Unsubscribe from topic
     */
    fun unsubscribeFromTopic(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Unsubscribed from topic: $topic")
                } else {
                    Log.e(TAG, "Failed to unsubscribe from topic: $topic", task.exception)
                }
            }
    }

    /**
     * Create a notification document in Firestore
     * This will trigger a Cloud Function to send the actual FCM message
     * (Only if you have Cloud Functions set up - otherwise just for logging)
     */
    fun createNotification(
        type: String,
        title: String,
        body: String,
        recipientId: String? = null,
        bookId: String? = null,
        orderId: String? = null
    ) {
        val notificationData = hashMapOf(
            "type" to type,
            "title" to title,
            "body" to body,
            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
            "read" to false
        )

        recipientId?.let { notificationData["recipientId"] = it }
        bookId?.let { notificationData["bookId"] = it }
        orderId?.let { notificationData["orderId"] = it }

        db.collection("notifications")
            .add(notificationData)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "Notification created with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error creating notification", e)
            }
    }

    /**
     * Send notification when a book is listed
     */
    fun sendBookListedNotification(bookId: String, bookTitle: String, category: String) {
        createNotification(
            type = "new_book_listing",
            title = "New Book Available!",
            body = "A new book '$bookTitle' has been listed in $category",
            bookId = bookId
        )
    }

    /**
     * Send notification when an order is placed
     */
    fun sendOrderPlacedNotification(sellerId: String, bookTitle: String, buyerName: String) {
        createNotification(
            type = "order_placed",
            title = "New Order!",
            body = "$buyerName wants to buy your book: $bookTitle",
            recipientId = sellerId
        )
    }

    /**
     * Send notification when order status changes
     */
    fun sendOrderStatusUpdateNotification(
        userId: String,
        orderId: String,
        bookTitle: String,
        newStatus: String
    ) {
        createNotification(
            type = "order_status_update",
            title = "Order Update",
            body = "Your order for '$bookTitle' is now $newStatus",
            recipientId = userId,
            orderId = orderId
        )
    }

    /**
     * Send notification for price drop
     */
    fun sendPriceDropNotification(
        userId: String,
        bookId: String,
        bookTitle: String,
        oldPrice: Double,
        newPrice: Double
    ) {
        createNotification(
            type = "price_drop",
            title = "Price Drop Alert!",
            body = "$bookTitle is now R$newPrice (was R$oldPrice)",
            recipientId = userId,
            bookId = bookId
        )
    }
}
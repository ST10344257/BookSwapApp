package com.example.bookswap

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class BookSwapMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "bookswap_notifications"
        private const val CHANNEL_NAME = "BookSwap Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for BookSwap app"
    }

    /**
     * Called when a new token is generated
     * Save this token to Firestore to send notifications to this specific device
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM token: $token")

        // Save the token to SharedPreferences
        saveTokenToPreferences(token)

        // Send token to your server/Firestore
        sendTokenToServer(token)
    }

    /**
     * Called when a message is received
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "Message received from: ${remoteMessage.from}")

        // Check if message contains a data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        // Check if message contains a notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            sendNotification(
                title = it.title ?: "BookSwap",
                body = it.body ?: "",
                data = remoteMessage.data
            )
        }
    }

    /**
     * Handle data payload messages
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val notificationType = data["type"] ?: "general"
        val title = data["title"] ?: "BookSwap"
        val body = data["body"] ?: "You have a new notification"
        val bookId = data["bookId"]
        val orderId = data["orderId"]

        when (notificationType) {
            "order_placed" -> {
                sendNotification(
                    title = title,
                    body = body,
                    data = data,
                    destinationActivity = OrderTrackingActivity::class.java
                )
            }
            "order_status_update" -> {
                sendNotification(
                    title = title,
                    body = body,
                    data = data,
                    destinationActivity = OrderTrackingActivity::class.java
                )
            }
            "new_book_listing" -> {
                sendNotification(
                    title = title,
                    body = body,
                    data = data,
                    destinationActivity = BookDetailActivity::class.java,
                    bookId = bookId
                )
            }
            "message" -> {
                sendNotification(
                    title = title,
                    body = body,
                    data = data,
                    destinationActivity = HomeActivity::class.java
                )
            }
            "price_drop" -> {
                sendNotification(
                    title = title,
                    body = body,
                    data = data,
                    destinationActivity = BookDetailActivity::class.java,
                    bookId = bookId
                )
            }
            else -> {
                sendNotification(title, body, data)
            }
        }
    }

    /**
     * Create and show a notification
     */
    private fun sendNotification(
        title: String,
        body: String,
        data: Map<String, String>,
        destinationActivity: Class<*>? = HomeActivity::class.java,
        bookId: String? = null,
        orderId: String? = null
    ) {
        val intent = Intent(this, destinationActivity).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            bookId?.let { putExtra("BOOK_ID", it) }
            orderId?.let { putExtra("ORDER_ID", it) }
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // You'll need to create this
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    /**
     * Save FCM token to SharedPreferences
     */
    private fun saveTokenToPreferences(token: String) {
        val prefs = getSharedPreferences("BookSwapPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()
        Log.d(TAG, "FCM token saved to SharedPreferences")
    }

    /**
     * Send FCM token to Firestore
     */
    private fun sendTokenToServer(token: String) {
        val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val tokenData = hashMapOf(
                "fcmToken" to token,
                "userId" to userId,
                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                "platform" to "android"
            )

            db.collection("fcmTokens")
                .document(userId)
                .set(tokenData)
                .addOnSuccessListener {
                    Log.d(TAG, "FCM token uploaded to Firestore")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error uploading FCM token", e)
                }
        }
    }
}
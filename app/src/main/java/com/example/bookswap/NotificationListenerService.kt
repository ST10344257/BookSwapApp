package com.example.bookswap

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.bookswap.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlin.random.Random

class NotificationListenerService : Service() {

    private val TAG = "NotificationListener"
    private var listenerRegistration: ListenerRegistration? = null
    private val firestore = FirebaseFirestore.getInstance()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val userId = intent?.getStringExtra("USER_ID")

        if (userId != null) {
            startListeningForNotifications(userId)
        } else {
            Log.e(TAG, "No user ID provided")
            stopSelf()
        }

        return START_STICKY
    }

    private fun startListeningForNotifications(userId: String) {
        Log.d(TAG, "Starting notification listener for user: $userId")

        // Listen for new notifications in real-time
        listenerRegistration = firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e(TAG, "Listen failed: ${error.message}", error)
                    return@addSnapshotListener
                }

                snapshots?.documentChanges?.forEach { change ->
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val data = change.document.data
                        val title = data["title"] as? String ?: "BookSwap"
                        val message = data["message"] as? String ?: "You have a new notification"
                        val type = data["type"] as? String ?: "GENERAL"
                        val relatedId = data["relatedId"] as? String

                        Log.d(TAG, "New notification: $title - $message")

                        // Show notification
                        showNotification(title, message, type, relatedId)

                        // Mark as read
                        change.document.reference.update("isRead", true)
                    }
                }
            }
    }

    private fun showNotification(title: String, message: String, type: String, relatedId: String?) {
        val channelId = "bookswap_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "BookSwap Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for book sales and updates"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent based on notification type
        val intent = when (type) {
            "BOOK_SOLD" -> Intent(this, BooksForSaleActivity::class.java)
            "NEW_BOOK_LISTING" -> Intent(this, HomeActivity::class.java)
            else -> Intent(this, HomeActivity::class.java)
        }.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(1000, 1000))
            .build()

        notificationManager.notify(Random.nextInt(), notification)

        Log.d(TAG, "Notification displayed: $title")
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerRegistration?.remove()
        Log.d(TAG, "Notification listener stopped")
    }
}
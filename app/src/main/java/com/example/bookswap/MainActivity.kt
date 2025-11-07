package com.example.bookswap

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

/**
 * Splash screen activity.
 * - If launched from a notification, it immediately forwards the intent data to WelcomeActivity and finishes.
 * - Otherwise, it shows a brief logo animation and then transitions to WelcomeActivity.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // --- DEEP LINKING LOGIC ---
        // Check if the activity was launched from a notification tap.
        val bookIdFromNotification = intent.extras?.getString("bookId")

        if (bookIdFromNotification != null && !bookIdFromNotification.isNullOrEmpty()) {
            // --- Case 1: Launched from a Notification ---
            Log.d(TAG, "Notification launch detected. Skipping splash delay.")

            // Immediately create a new intent for WelcomeActivity.
            val welcomeIntent = Intent(this, WelcomeActivity::class.java).apply {
                // IMPORTANT: Pass the notification data along to the next activity.
                putExtras(intent.extras!!)
            }

            startActivity(welcomeIntent)
            finish() // Finish this splash screen immediately.

        } else {
            // --- Case 2: Normal App Launch ---
            Log.d(TAG, "Normal app launch. Starting splash animation.")

            // small logo entrance animation
            findViewById<ImageView?>(R.id.logo)?.apply {
                alpha = 0f
                scaleX = 0.96f
                scaleY = 0.96f
                animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(550)
                    .start()
            }

            // After a short delay, go to WelcomeActivity with fade transition
            Handler(Looper.getMainLooper()).postDelayed({
                val welcomeIntent = Intent(this, WelcomeActivity::class.java)
                startActivity(welcomeIntent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish() // don't return to splash
            }, 2000L)
        }
    }
}

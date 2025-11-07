package com.example.bookswap

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookswap.databinding.ActivityNotificationSettingsBinding
import com.google.firebase.messaging.FirebaseMessaging

class NotificationSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationSettingsBinding
    private lateinit var prefs: android.content.SharedPreferences

    private val allCategories = listOf("TECH", "LAW", "BUSINESS", "SCIENCE", "HUMANITIES", "ENGINEERING", "MEDICAL", "OTHER")
    private val otherTopics = listOf("order_updates", "price_drops", "messages")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("BookSwapPrefs", Context.MODE_PRIVATE)

        loadSettings()
        setupClickListeners()
    }

    private fun loadSettings() {
        // Load saved notification preferences
        val allEnabled = prefs.getBoolean("notifications_enabled", true)
        binding.switchAllNotifications.isChecked = allEnabled

        binding.switchOrderUpdates.isChecked = prefs.getBoolean("topic_order_updates", true)
        binding.switchNewBooks.isChecked = prefs.getBoolean("topic_all_categories", true)
        binding.switchPriceDrops.isChecked = prefs.getBoolean("topic_price_drops", true)
        binding.switchMessages.isChecked = prefs.getBoolean("topic_messages", true)

        // Set initial enabled state for sub-switches
        updateSubSwitchStates(allEnabled)
    }

    private fun updateSubSwitchStates(isEnabled: Boolean) {
        binding.switchOrderUpdates.isEnabled = isEnabled
        binding.switchNewBooks.isEnabled = isEnabled
        binding.switchPriceDrops.isEnabled = isEnabled
        binding.switchMessages.isEnabled = isEnabled
    }

    private fun setupClickListeners() {
        binding.btnback.setOnClickListener {
            finish()
        }

        // Master switch for all notifications
        binding.switchAllNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
            updateSubSwitchStates(isChecked)

            // When the master switch is turned off, unsubscribe from everything.
            if (!isChecked) {
                unsubscribeFromAllTopics()
            }

            Toast.makeText(
                this,
                if (isChecked) "Notifications enabled" else "Notifications disabled",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Setup individual switches with safer topic management
        setupTopicSwitch(binding.switchOrderUpdates, "order_updates")
        setupTopicSwitch(binding.switchPriceDrops, "price_drops")
        setupTopicSwitch(binding.switchMessages, "messages")

        // --- THIS IS THE FIX ---
        // Robust handling for multiple category subscriptions
        binding.switchNewBooks.setOnCheckedChangeListener { buttonView, isChecked ->
            buttonView.isEnabled = false // Disable switch during operation
            val operation = if (isChecked) "Subscribing" else "Unsubscribing"
            Toast.makeText(this, "$operation to category notifications...", Toast.LENGTH_SHORT).show()

            // Get a list of all tasks to perform
            val tasks = allCategories.map { category ->
                val topic = "category_$category"
                if (isChecked) {
                    FirebaseMessaging.getInstance().subscribeToTopic(topic)
                } else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                }
            }

            // Wait for all tasks to complete
            com.google.android.gms.tasks.Tasks.whenAll(tasks).addOnCompleteListener {
                buttonView.isEnabled = true // Re-enable the switch

                if (it.isSuccessful) {
                    // All subscriptions/unsubscriptions succeeded
                    prefs.edit().putBoolean("topic_all_categories", isChecked).apply()
                    Log.d("FCM", "Successfully updated all category subscriptions.")
                    Toast.makeText(this, "Category settings updated!", Toast.LENGTH_SHORT).show()
                } else {
                    // At least one operation failed. Revert the UI switch.
                    buttonView.isChecked = !isChecked
                    Log.e("FCM", "Failed to update category subscriptions", it.exception)
                    Toast.makeText(this, "Failed. Please check connection and try again.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    /**
     * A robust listener that handles topic subscription and only saves the state on success.
     *
     * FIX: Changed the 'switch' parameter type from SwitchMaterial to the more general
     * androidx.appcompat.widget.SwitchCompat to match the type from the layout.
     */
    private fun setupTopicSwitch(switch: androidx.appcompat.widget.SwitchCompat, topic: String) {
        switch.setOnCheckedChangeListener { buttonView, isChecked ->
            // Disable the switch to prevent spamming while the operation is in progress
            buttonView.isEnabled = false
            val action = if (isChecked) "Subscribing to" else "Unsubscribing from"
            Toast.makeText(this, "$action $topic...", Toast.LENGTH_SHORT).show()

            val task = if (isChecked) {
                FirebaseMessaging.getInstance().subscribeToTopic(topic)
            } else {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            }

            task.addOnCompleteListener { taskResult ->
                // Re-enable the switch now that the operation is complete
                buttonView.isEnabled = true

                if (taskResult.isSuccessful) {
                    // Save the preference ONLY if the operation was successful
                    prefs.edit().putBoolean("topic_$topic", isChecked).apply()
                    Log.d("FCM", "Successfully ${if (isChecked) "subscribed to" else "unsubscribed from"} $topic")
                    Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show()
                } else {
                    // The operation failed. Revert the switch to its previous state.
                    buttonView.isChecked = !isChecked
                    Log.e("FCM", "Failed to change subscription for $topic", taskResult.exception)
                    Toast.makeText(this, "Failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun subscribeToTopic(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnSuccessListener { Log.d("FCM", "Subscribed to $topic") }
            .addOnFailureListener { Log.e("FCM", "Failed to subscribe to $topic", it) }
    }

    private fun unsubscribeFromTopic(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnSuccessListener { Log.d("FCM", "Unsubscribed from $topic") }
            .addOnFailureListener { Log.e("FCM", "Failed to unsubscribe from $topic", it) }
    }

    private fun unsubscribeFromAllTopics() {
        Toast.makeText(this, "Unsubscribing from all topics...", Toast.LENGTH_SHORT).show()
        (allCategories.map { "category_$it" } + otherTopics).forEach { topic ->
            unsubscribeFromTopic(topic)
        }
    }
}

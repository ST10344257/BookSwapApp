package com.example.bookswap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bookswap.databinding.ProfilePageBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth


class ProfileActivity : AppCompatActivity() {

    // Use ProfilePageBinding to match your XML file name
    private lateinit var binding: ProfilePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using View Binding for profile_page.xml
        binding = ProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // --- Top Bar ---
        binding.btnback.setOnClickListener {
            // Go back to the homepage
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        // --- Navigation Icons ---
        binding.trackOrdersIcon.setOnClickListener {
            startActivity(Intent(this, OrderTrackingActivity::class.java))
        }

        binding.personaldetails.setOnClickListener {
            startActivity(Intent(this, PersonalDetailsActivity::class.java))
        }

        binding.password.setOnClickListener {
            // This will open your existing PasswordChangeActivity
            startActivity(Intent(this, PasswordChangeActivity::class.java))
        }

        binding.books.setOnClickListener {
            startActivity(Intent(this, BooksForSaleActivity::class.java))
        }
        // --- Wallet/Account Buttons ---
        binding.btnsell.setOnClickListener {
            startActivity(Intent(this, AddBookActivity::class.java))
        }

        binding.btnaccount.setOnClickListener {
            Toast.makeText(this, "My Account details coming soon!", Toast.LENGTH_SHORT).show()
        }

        // --- Logout Button ---
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        // Sign out from Firebase
        Firebase.auth.signOut()

        // Clear any local user data you might have saved in SharedPreferences
        // Example:
        // val prefs = getSharedPreferences("BookSwapPrefs", Context.MODE_PRIVATE)
        // prefs.edit().clear().apply()

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Navigate back to the LoginActivity and clear all previous activities
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}


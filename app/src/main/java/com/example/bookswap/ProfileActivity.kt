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

    private lateinit var binding: ProfilePageBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // --- Top Bar ---
        binding.btnback.setOnClickListener {
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

        binding.wishlist.setOnClickListener {
            startActivity(Intent(this, WishlistActivity::class.java))
        }

        binding.googleBooks.setOnClickListener {
            startActivity(Intent(this, GoogleBooksActivity::class.java))
        }

        // --- Language Selection ---
        binding.languageIcon.setOnClickListener {
            startActivity(Intent(this, LanguageSelectionActivity::class.java))
        }

        // --- Notification Settings ---
        binding.notificationSettingsIcon.setOnClickListener {
            startActivity(Intent(this, NotificationSettingsActivity::class.java))
        }

        // --- Logout Button ---
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.profile_logout))
            .setMessage(getString(R.string.profile_logout_confirm_message))
            .setPositiveButton(getString(R.string.profile_logout)) { _, _ ->
                performLogout()
            }
            .setNegativeButton(getString(R.string.button_cancel), null)
            .show()
    }

    private fun performLogout() {
        // Sign out from Firebase
        Firebase.auth.signOut()

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Navigate back to the LoginActivity and clear all previous activities
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
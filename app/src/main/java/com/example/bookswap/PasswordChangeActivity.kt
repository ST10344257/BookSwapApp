package com.example.bookswap

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookswap.databinding.PasswordPageBinding
import com.example.bookswap.utils.ValidationUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import android.content.Context


class PasswordChangeActivity : AppCompatActivity() {

    // Use View Binding correctly
    private lateinit var binding: PasswordPageBinding
    private lateinit var auth: FirebaseAuth



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using View Binding
        binding = PasswordPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth

        setupClickListeners()
    }



    private fun setupClickListeners() {
        // Use binding to access views
        binding.btnback.setOnClickListener {
            finish() // Simply close this activity to go back
        }

        binding.btnchange.setOnClickListener {
            val currentPassword = binding.editCurrentPassword.text.toString().trim()
            val newPassword = binding.editnewPassword.text.toString().trim()
            val confirmPassword = binding.editConfirmPassword.text.toString().trim()

            if (validateInput(currentPassword, newPassword, confirmPassword)) {
                changePasswordWithFirebase(currentPassword, newPassword)
            }
        }
    }

    private fun changePasswordWithFirebase(currentPassword: String, newPassword: String) {
        val user = auth.currentUser
        if (user == null || user.email == null) {
            Toast.makeText(this, "Error: No user is logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

        user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
            if (reauthTask.isSuccessful) {
                user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        Toast.makeText(this, "Password changed successfully!", Toast.LENGTH_SHORT).show()
                        finish() // Go back to the profile page on success
                    } else {
                        Toast.makeText(this, "Failed to update password. ${updateTask.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "Authentication failed. Incorrect current password.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun validateInput(current: String, new: String, confirm: String): Boolean {
        return when {
            current.isEmpty() -> {
                binding.editCurrentPassword.error = "Current password is required"
                false
            }
            new.isEmpty() -> {
                binding.editnewPassword.error = "New password is required"
                false
            }
            !ValidationUtils.isValidPassword(new) -> {
                binding.editnewPassword.error = "Password must be at least 6 characters"
                false
            }
            !ValidationUtils.doPasswordsMatch(new, confirm) -> {
                binding.editConfirmPassword.error = "Passwords do not match"
                false
            }
            else -> true
        }
    }
}


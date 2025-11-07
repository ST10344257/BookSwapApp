package com.example.bookswap

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.bookswap.data.Result
import com.example.bookswap.databinding.PersonalDetailsBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.userProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PersonalDetailsActivity : AppCompatActivity() {

    private lateinit var binding: PersonalDetailsBinding
    // Assuming FirebaseModule provides your auth and repository instances
    private val auth = FirebaseModule.firebaseAuth
    private val userRepository = FirebaseModule.userRepository

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri ->
            binding.profileAvatarPreview.setImageURI(imageUri)
            uploadProfileImage(imageUri) // Upload to Firebase Storage
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PersonalDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (auth.currentUser == null) {
            Toast.makeText(this, "No user logged in.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        loadUserData()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnChangeProfilePic.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnChangeUsername.setOnClickListener {
            val newName = binding.etNewName.text.toString().trim()
            if (newName.isNotEmpty()) {
                updateUsername(newName)
            } else {
                Toast.makeText(this, "Please enter a new name.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnChangeEmail.setOnClickListener {
            val password = binding.etPasswordForEmailChange.text.toString().trim()
            val newEmail = binding.etNewEmail.text.toString().trim()
            val confirmEmail = binding.etConfirmNewEmail.text.toString().trim()

            if (password.isEmpty()) {
                Toast.makeText(this, "Password is required to change email.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newEmail.isEmpty() || newEmail != confirmEmail) {
                Toast.makeText(this, "Please check the new email fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updateEmail(password, newEmail)
        }
    }

    private fun loadUserData() {
        setLoading(true)
        val userId = auth.currentUser?.uid ?: return

        lifecycleScope.launch {
            when (val result = userRepository.getUser(userId)) {
                is Result.Success -> {
                    val user = result.data
                    binding.tvCurrentName.text = "Current Name: ${user.name}"

                    val imageUrl = user.profilePictureUrl
                    val glideRequest = if (imageUrl.isNullOrEmpty()) {
                        Glide.with(this@PersonalDetailsActivity).load(R.drawable.baseline_person_3_24)
                    } else {
                        Glide.with(this@PersonalDetailsActivity).load(imageUrl)
                    }
                    glideRequest
                        .placeholder(R.drawable.baseline_person_3_24)
                        .error(R.drawable.baseline_person_3_24)
                        .into(binding.profileAvatarPreview)
                }
                is Result.Error -> {
                    Toast.makeText(this@PersonalDetailsActivity, "Failed to load user data.", Toast.LENGTH_SHORT).show()
                    binding.tvCurrentName.text = "Current Name: ${auth.currentUser?.displayName ?: "Not Set"}"
                }
                else -> {}
            }
            setLoading(false)
        }
    }

    private fun uploadProfileImage(imageUri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        setLoading(true)
        lifecycleScope.launch {
            when (val result = userRepository.uploadProfilePicture(userId, imageUri)) {
                is Result.Success -> {
                    Toast.makeText(this@PersonalDetailsActivity, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                }
                is Result.Error -> {
                    Log.e("PersonalDetailsActivity", "Image upload failed", result.exception)
                    val errorMessage = when {
                        result.exception.message?.contains("permission") == true -> "Permission denied. Please check storage rules."
                        else -> "Failed to upload image."
                    }
                    Toast.makeText(this@PersonalDetailsActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
            setLoading(false)
        }
    }

    private fun updateUsername(newName: String) {
        val user = auth.currentUser ?: return
        setLoading(true)

        lifecycleScope.launch {
            try {
                val profileUpdates = userProfileChangeRequest { displayName = newName }
                user.updateProfile(profileUpdates).await()

                when (userRepository.updateUser(user.uid, mapOf("name" to newName))) {
                    is Result.Success -> {
                        binding.tvCurrentName.text = "Current Name: $newName"
                        binding.etNewName.text.clear()
                        Toast.makeText(this@PersonalDetailsActivity, "Username updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                    is Result.Error -> {
                        Toast.makeText(this@PersonalDetailsActivity, "Failed to update database.", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Toast.makeText(this@PersonalDetailsActivity, "Failed to update username: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun updateEmail(password: String, newEmail: String) {
        val user = auth.currentUser
        val currentEmail = user?.email
        if (currentEmail == null) {
            Toast.makeText(this, "Could not find current user's email.", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        val credential = EmailAuthProvider.getCredential(currentEmail, password)

        lifecycleScope.launch {
            try {
                user.reauthenticate(credential).await()
                user.verifyBeforeUpdateEmail(newEmail).await()

                userRepository.updateUser(user.uid, mapOf("email" to newEmail))

                Toast.makeText(this@PersonalDetailsActivity, "Verification email sent to $newEmail.", Toast.LENGTH_LONG).show()
                binding.etPasswordForEmailChange.text.clear()
                binding.etNewEmail.text.clear()
                binding.etConfirmNewEmail.text.clear()

            } catch (e: Exception) {
                Toast.makeText(this@PersonalDetailsActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        // You would ideally have a ProgressBar in your layout
        binding.btnChangeUsername.isEnabled = !isLoading
        binding.btnChangeEmail.isEnabled = !isLoading
        binding.btnChangeProfilePic.isEnabled = !isLoading
    }
}


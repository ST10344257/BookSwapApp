package com.example.bookswap

import android.app.Activity // --- ADDED ---
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log // --- ADDED ---
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts // --- ADDED ---
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bookswap.data.Result
import com.example.bookswap.data.repository.AuthRepository
import com.example.bookswap.databinding.LoginPageBinding
import com.example.bookswap.utils.Constants
import com.example.bookswap.utils.ValidationUtils
// --- ADDED these imports ---
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    // Use View Binding
    private lateinit var binding: LoginPageBinding
    private val authRepository = AuthRepository()

    // --- ADDED these variables ---
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val TAG = "LoginActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate layout with View Binding
        binding = LoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- ADDED Firebase/Google Auth setup ---
        // 1. Initialize Firebase Auth
        auth = Firebase.auth

        // 2. Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // --- END of ADDED setup ---

        setupClickListeners()

        val orderedIds = arrayOf(
            R.id.logo,
            R.id.txtlogin,
            R.id.txtregister,
            R.id.edtusername,
            R.id.edtpassword,
            R.id.btnlogin,
            R.id.btnGoogleSignIn // --- ADDED Google button to animation ---
        )
        staggerEnter(orderedIds)
    }

    private fun startNotificationListener(userId: String) {
        val intent = Intent(this, NotificationListenerService::class.java)
        intent.putExtra("USER_ID", userId)

        startService(intent)
    }

    private fun setupClickListeners() {
        // Your existing Login button
        binding.btnlogin.setOnClickListener {
            val email = binding.edtusername.text.toString().trim()
            val password = binding.edtpassword.text.toString().trim()

            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }

        // --- ADDED Google Sign-In button listener ---
        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        binding.txtregister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        // (This function is unchanged)
        return when {
            email.isEmpty() -> {
                binding.edtusername.error = "Email is required"; false
            }
            !ValidationUtils.isValidEmail(email) -> {
                binding.edtusername.error = "Please enter a valid email"; false
            }
            password.isEmpty() -> {
                binding.edtpassword.error = "Password is required"; false
            }
            else -> true
        }
    }

    private fun loginUser(email: String, password: String) {
        // (This function is unchanged)
        setLoading(true)
        lifecycleScope.launch {
            when (val result = authRepository.login(email, password)) {
                is Result.Success -> {
                    val user = result.data
                    saveLoginState(user.uid, user.email ?: "")
                    Toast.makeText(this@LoginActivity, "Welcome back, ${user.name}!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                    finishAffinity()
                }
                is Result.Error -> {
                    setLoading(false)
                    val errorMessage = "Login failed. Please check your credentials."
                    androidx.appcompat.app.AlertDialog.Builder(this@LoginActivity)
                        .setTitle("Login Failed")
                        .setMessage(errorMessage)
                        .setPositiveButton("OK", null)
                        .show()
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun saveLoginState(userId: String, email: String) {
        // (This function is unchanged)
        val prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean(Constants.KEY_IS_LOGGED_IN, true)
            putString(Constants.KEY_USER_ID, userId)
            putString(Constants.KEY_USER_EMAIL, email)
            apply()
        }
        /* //FCM INITIALIZE
                FCMHelper.initializeFCM(this@LoginActivity)

                // REQUEST NOTIFICATION PERMISSION
                NotificationPermissionHelper.requestNotificationPermission(this@LoginActivity)*/
        startNotificationListener(userId)
    }

    private fun setLoading(loading: Boolean) {
        // --- UPDATED this function ---
        binding.btnlogin.isEnabled = !loading
        binding.btnGoogleSignIn.isEnabled = !loading // --- ADDED ---
        binding.edtusername.isEnabled = !loading
        binding.edtpassword.isEnabled = !loading
        binding.btnlogin.text = if (loading) "Logging in..." else "Login"
    }

    private fun staggerEnter(ids: Array<Int>) {
        // (This function is unchanged)
        ids.forEachIndexed { idx, id ->
            val v: View? = findViewById(id)
            v ?: return@forEachIndexed
            v.alpha = 0f
            v.translationY = 20f
            v.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay((idx * 80).toLong())
                .setDuration(360)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    // --- ADDED all functions below for Google Sign-In ---

    // 1. Starts the Google Sign-In pop-up
    private fun signInWithGoogle() {
        // This forces account selection dialog
        googleSignInClient.signOut() // Signs out first
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    // 2. Handles the result from the Google Sign-In pop-up
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                // Google Sign-In was successful
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "Google Sign-In SUCCESS, getting Firebase credential")
                // Authenticate with Firebase
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign-In failed
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(this, "Google Sign-In Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            // User cancelled
            Log.w(TAG, "Google sign in cancelled by user")
            Toast.makeText(this, "Sign-In Canceled", Toast.LENGTH_SHORT).show()
        }
    }

    // 3. Takes the Google token and signs in to Firebase
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        // We are signing in, so show loading
        setLoading(true)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success!
                    Log.d(TAG, "Firebase signInWithCredential:success")
                    val firebaseUser = auth.currentUser!!

                    // --- We use YOUR existing success logic ---
                    saveLoginState(firebaseUser.uid, firebaseUser.email ?: "")
                    Toast.makeText(this, "Welcome, ${firebaseUser.displayName}!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finishAffinity()
                    // No need for setLoading(false) because we are leaving the activity

                } else {
                    // Sign in failed
                    Log.w(TAG, "Firebase signInWithCredential:failure", task.exception)
                    setLoading(false) // Re-enable buttons on failure
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
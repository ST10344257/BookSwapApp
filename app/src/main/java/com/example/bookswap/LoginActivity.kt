package com.example.bookswap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bookswap.data.Result
import com.example.bookswap.data.repository.AuthRepository
import com.example.bookswap.databinding.LoginPageBinding
import com.example.bookswap.utils.Constants
import com.example.bookswap.utils.ValidationUtils
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    // Use View Binding
    private lateinit var binding: LoginPageBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate layout with View Binding
        binding = LoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()

        val orderedIds = arrayOf(
            R.id.logo,
            R.id.txtlogin,
            R.id.txtregister,
            R.id.edtusername,
            R.id.edtpassword,
            R.id.btnlogin
        )
        staggerEnter(orderedIds)
    }

    private fun setupClickListeners() {
        binding.btnlogin.setOnClickListener {
            val email = binding.edtusername.text.toString().trim()
            val password = binding.edtpassword.text.toString().trim()

            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }

        binding.txtregister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
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
        val prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean(Constants.KEY_IS_LOGGED_IN, true)
            putString(Constants.KEY_USER_ID, userId)
            putString(Constants.KEY_USER_EMAIL, email)
            apply()
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnlogin.isEnabled = !loading
        binding.edtusername.isEnabled = !loading
        binding.edtpassword.isEnabled = !loading
        binding.btnlogin.text = if (loading) "Logging in..." else "Login"
    }

    private fun staggerEnter(ids: Array<Int>) {
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
}


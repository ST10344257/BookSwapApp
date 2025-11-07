package com.example.bookswap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bookswap.data.Result
import com.example.bookswap.data.repository.AuthRepository
import com.example.bookswap.utils.Constants
import com.example.bookswap.utils.ValidationUtils
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {



    private lateinit var edtName: EditText
    private lateinit var edtSurname: EditText
    private lateinit var edtPassword: EditText
    private lateinit var edtConfirm: EditText
    private lateinit var btnSignup: Button
    private lateinit var txtLogin: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var edtEmail: EditText

    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.register_page)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        edtName = findViewById(R.id.edtname)
        edtSurname = findViewById(R.id.edtsurname)
        edtPassword = findViewById(R.id.edtpassword)
        edtConfirm = findViewById(R.id.edtconfirm)
        txtLogin = findViewById(R.id.txtlogin)
        edtEmail = findViewById(R.id.edtemail)


        btnSignup = findViewById(R.id.btnsignup)


        progressBar = ProgressBar(this).apply {
            visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        btnSignup.setOnClickListener {
            val name = edtName.text.toString().trim()
            val surname = edtSurname.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()
            val confirmPassword = edtConfirm.text.toString().trim()

            if (validateInput(name, surname, email, password, confirmPassword)) {
                registerUser(name, surname, email, password)
            }
        }

        txtLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun validateInput(
        name: String,
        surname: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return when {
            name.isEmpty() -> {
                edtName.error = "Name is required"
                edtName.requestFocus()
                false
            }
            !ValidationUtils.isValidName(name) -> {
                edtName.error = "Name must be at least 2 characters"
                edtName.requestFocus()
                false
            }
            surname.isEmpty() -> {
                edtSurname.error = "Surname is required"
                edtSurname.requestFocus()
                false
            }
            !ValidationUtils.isValidName(surname) -> {
                edtSurname.error = "Surname must be at least 2 characters"
                edtSurname.requestFocus()
                false
            }
            email.isEmpty() -> {
                edtEmail.error = "Email is required"
                edtEmail.requestFocus()
                false
            }
            !ValidationUtils.isValidEmail(email) -> {
                edtEmail.error = "Please enter a valid email"
                edtEmail.requestFocus()
                false
            }
            password.isEmpty() -> {
                edtPassword.error = "Password is required"
                edtPassword.requestFocus()
                false
            }
            !ValidationUtils.isValidPassword(password) -> {
                edtPassword.error = "Password must be at least 6 characters"
                edtPassword.requestFocus()
                false
            }
            confirmPassword.isEmpty() -> {
                edtConfirm.error = "Please confirm your password"
                edtConfirm.requestFocus()
                false
            }
            !ValidationUtils.doPasswordsMatch(password, confirmPassword) -> {
                edtConfirm.error = "Passwords do not match"
                edtConfirm.requestFocus()
                false
            }
            else -> true
        }
    }

    private fun registerUser(name: String, surname: String, email: String, password: String) {
        setLoading(true)

        lifecycleScope.launch {
            when (val result = authRepository.register(name, surname, email, password)) {
                is Result.Success -> {
                    // Save login state
                    saveLoginState(result.data.uid, email)

                    Toast.makeText(
                        this@RegisterActivity,
                        "Account created successfully!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Navigate to HomeActivity
                    startActivity(Intent(this@RegisterActivity, HomeActivity::class.java))
                    finish()
                }
                is Result.Error -> {
                    setLoading(false)
                    val message = when {
                        result.exception.message?.contains("already in use") == true ->
                            "An account with this email already exists"
                        result.exception.message?.contains("network") == true ->
                            "Network error. Please check your connection"
                        else -> "Registration failed: ${result.exception.message}"
                    }
                    Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_LONG).show()
                }
                is Result.Loading -> { /* Already handled */ }
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
        btnSignup.isEnabled = !loading
        edtName.isEnabled = !loading
        edtSurname.isEnabled = !loading
        edtPassword.isEnabled = !loading
        edtConfirm.isEnabled = !loading
        btnSignup.text = if (loading) "Creating Account..." else "Sign Up"
    }
}


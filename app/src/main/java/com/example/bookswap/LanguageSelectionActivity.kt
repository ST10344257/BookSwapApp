package com.example.bookswap

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LanguageSelectionActivity : AppCompatActivity() {

    private lateinit var radioGroupLanguages: RadioGroup
    private lateinit var radioEnglish: RadioButton
    private lateinit var radioAfrikaans: RadioButton
    private lateinit var radioZulu: RadioButton
    private lateinit var radioSetswana: RadioButton
    private lateinit var btnApplyLanguage: Button
    private lateinit var btnBack: ImageView

    private val TAG = "LanguageSelection"
    private var currentLanguage = "en" // Store the language on load

    // Define the preference name ONE time, matching LocaleHelper
    private val PREFS_NAME = "LanguagePrefs"
    private val KEY_LANGUAGE = "selected_language"

    // *** attachBaseContext was DELETED ***

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_selection)

        initializeViews()
        loadCurrentLanguage()
        setupClickListeners()
    }

    private fun initializeViews() {
        radioGroupLanguages = findViewById(R.id.radioGroupLanguages)
        radioEnglish = findViewById(R.id.radioEnglish)
        radioAfrikaans = findViewById(R.id.radioAfrikaans)
        radioZulu = findViewById(R.id.radioZulu)
        radioSetswana = findViewById(R.id.radioSetswana)
        btnApplyLanguage = findViewById(R.id.btnApplyLanguage)
        btnBack = findViewById(R.id.btnback)
    }

    private fun loadCurrentLanguage() {
        // Read the language from the correct, shared preference file
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        currentLanguage = prefs.getString(KEY_LANGUAGE, "en") ?: "en"

        Log.d(TAG, "Current language loaded: $currentLanguage")
        when (currentLanguage) {
            "en" -> radioEnglish.isChecked = true
            "af" -> radioAfrikaans.isChecked = true
            "zu" -> radioZulu.isChecked = true
            "tn" -> radioSetswana.isChecked = true
            else -> radioEnglish.isChecked = true
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish() // Go back to ProfileActivity
        }

        btnApplyLanguage.setOnClickListener {
            applyLanguageChange()
        }
    }

    private fun applyLanguageChange() {
        val selectedId = radioGroupLanguages.checkedRadioButtonId

        if (selectedId == -1) {
            Toast.makeText(this, getString(R.string.error_field_required), Toast.LENGTH_SHORT).show()
            return
        }

        val selectedLanguage = when (selectedId) {
            R.id.radioEnglish -> "en"
            R.id.radioAfrikaans -> "af"
            R.id.radioZulu -> "zu"
            R.id.radioSetswana -> "tn"
            else -> "en"
        }

        Log.d(TAG, "Selected language: $selectedLanguage")

        // Check if the language is different from the one we loaded
        if (selectedLanguage != currentLanguage) {
            Log.d(TAG, "Language is different, saving and applying...")

            // Call the LocaleHelper to save the new language
            LocaleHelper.setLocale(this, selectedLanguage)

            // Show a success message
            val languageName = LocaleHelper.getLanguageName(selectedLanguage)
            Toast.makeText(this, "Language changed to $languageName", Toast.LENGTH_SHORT).show()

            // Recreate this activity to show the new language
            // This is the only line needed. No more manual restart.
            recreate()

        } else {
            // Language is the same, just go back
            Toast.makeText(this, "Language unchanged", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // *** restartApp() function was DELETED ***
}
package com.example.bookswap

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LocaleHelper {

    private const val PREFS_NAME = "LanguagePrefs" // Use one consistent name
    private const val KEY_LANGUAGE = "selected_language"
    private const val TAG = "LocaleHelper"

    // This is called by your LanguageSelectionActivity
    fun setLocale(context: Context, languageCode: String) {
        Log.d(TAG, "Setting and persisting locale to: $languageCode")

        // 1. Save the user's choice
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()

        // 2. Apply the language change to the app
        val appLocale = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
        // AppCompatDelegate handles all the restarts/reloads for you.
    }

    // This is called when the app first starts
    fun loadAndSetLocale(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val languageCode = prefs.getString(KEY_LANGUAGE, "en") ?: "en"

        Log.d(TAG, "Loading and setting saved locale: $languageCode")

        val appLocale = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    // This is a helper to get the display name
    fun getLanguageName(languageCode: String): String {
        return when (languageCode) {
            "en" -> "English"
            "af" -> "Afrikaans"
            "zu" -> "isiZulu"
            "tn" -> "Setswana"
            else -> "English"
        }
    }
}
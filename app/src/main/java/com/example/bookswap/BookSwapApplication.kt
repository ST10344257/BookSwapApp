package com.example.bookswap

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class BookSwapApplication : Application() {

    // Use onCreate(), NOT attachBaseContext()
    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // ENABLE OFFLINE PERSISTENCE
        enableFirestoreOfflineMode()

        Log.d("BookSwapApp", "Application initialized with offline support")

        // This is the safe place to load the language.
        // The 'this' context is fully initialized here.
        LocaleHelper.loadAndSetLocale(this)
        FCMHelper.initializeFCM(this)
    }

    private fun enableFirestoreOfflineMode() {
        val firestore = FirebaseFirestore.getInstance()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)  // ✅ Enable offline cache
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)  // ✅ Unlimited cache
            .build()

        firestore.firestoreSettings = settings

        Log.d("BookSwapApp", "✅ Firestore offline persistence enabled")
    }
}
package com.example.bookswap

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

/**
 * Simple splash/intro activity. Shows activity_main.xml briefly, then fades to WelcomeActivity.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // small logo entrance animation
        findViewById<ImageView?>(R.id.logo)?.apply {
            alpha = 0f
            scaleX = 0.96f
            scaleY = 0.96f
            animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(550)
                .start()
        }

        // After a short delay, go to WelcomeActivity with fade transition
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, WelcomeActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish() // don't return to splash
        }, 2000L)
    }
}

package com.example.bookswap

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.content.Context

/**
 * StartPageActivity - comes after InsightActivity. Contains btnprevious and btnlogin.
 */
class StartPageActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.start_page)

        val orderedIds = arrayOf(
            R.id.topImage,
            R.id.insightTitle,
            R.id.indicatorLayout,
            R.id.btnprevious,
            R.id.btnlogin,
            R.id.btnsignup
        )
        staggerEnter(orderedIds)

        // Back to InsightActivity
        findViewById<ImageView?>(R.id.btnprevious)?.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        // Move to LoginActivity
        findViewById<Button?>(R.id.btnlogin)?.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
        // Move to LoginActivity
        findViewById<Button?>(R.id.btnsignup)?.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    private fun staggerEnter(ids: Array<Int>) {
        ids.forEachIndexed { idx, id ->
            val v: View? = findViewById(id)
            v ?: return@forEachIndexed
            v.alpha = 0f
            v.translationY = 24f
            v.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay((idx * 70).toLong())
                .setDuration(420)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }
}

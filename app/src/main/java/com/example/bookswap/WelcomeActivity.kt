package com.example.bookswap

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.content.Context

/**
 * WelcomeActivity - entrance animations and "next" arrow to InsightActivity.
 */
class WelcomeActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.welcome_page)

        // The IDs that exist in welcome_page.xml (stagger in this order)
        val orderedIds = arrayOf(
            R.id.topImage,
            R.id.welcomeTitle,
            R.id.welcomeSubtitle,
            R.id.welcomeBody,
            R.id.indicatorLayout,
            R.id.btnright
        )

        staggerEnter(orderedIds)

        // Next arrow moves to InsightActivity
        findViewById<ImageView?>(R.id.btnright)?.setOnClickListener {
            startActivity(Intent(this, InsightActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    private fun staggerEnter(ids: Array<Int>) {
        ids.forEachIndexed { idx, id ->
            val v: View? = findViewById(id)
            v ?: return@forEachIndexed
            v.alpha = 0f
            v.translationY = 28f
            v.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay((idx * 80).toLong())
                .setDuration(420)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }
}

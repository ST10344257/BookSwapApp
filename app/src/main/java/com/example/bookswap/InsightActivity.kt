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
 * InsightActivity - sits after Welcome; has previous and next arrows.
 */
class InsightActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.insight_page)

        val orderedIds = arrayOf(
            R.id.topImage,
            R.id.insightTitle,
            R.id.welcomeBody,
            R.id.indicatorLayout,
            R.id.btnprevious,
            R.id.btnright
        )
        staggerEnter(orderedIds)

        findViewById<ImageView?>(R.id.btnprevious)?.setOnClickListener {
            // simply go back (to WelcomeActivity) with a fade
            onBackPressed()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        findViewById<ImageView?>(R.id.btnright)?.setOnClickListener {
            // Next goes to the "start" page in your layout (StartPageActivity)
            startActivity(Intent(this, StartPageActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    private fun staggerEnter(ids: Array<Int>) {
        ids.forEachIndexed { idx, id ->
            val v: View? = findViewById(id)
            v ?: return@forEachIndexed
            v.alpha = 0f
            v.translationY = 26f
            v.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay((idx * 70).toLong())
                .setDuration(400)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }
}

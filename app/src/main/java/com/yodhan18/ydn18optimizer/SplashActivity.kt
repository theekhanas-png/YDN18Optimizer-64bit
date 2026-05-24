package com.yodhan18.ydn18optimizer

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.yodhan18.ydn18optimizer.ui.GlowImageView
import com.yodhan18.ydn18optimizer.ui.ParticleView

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<GlowImageView>(R.id.splashLogo)
        val progress = findViewById<ProgressBar>(R.id.splashProgress)
        val loadingText = findViewById<TextView>(R.id.splashLoadingText)
        val particleView = findViewById<ParticleView>(R.id.particleView)

        // Logo pulse animation
        logo.scaleX = 0f
        logo.scaleY = 0f
        logo.alpha = 0f
        logo.animate()
            .scaleX(1f).scaleY(1f).alpha(1f)
            .setDuration(600)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        // Progress animation
        val loadingMessages = listOf(
            "Loading assets…",
            "Initializing optimizer…",
            "Checking device…",
            "Almost ready…"
        )
        val progressAnim = ValueAnimator.ofInt(0, 100).apply {
            duration = 2800
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { anim ->
                val v = anim.animatedValue as Int
                progress.progress = v
                val msgIdx = (v / 26).coerceIn(0, loadingMessages.size - 1)
                loadingText.text = loadingMessages[msgIdx]
            }
        }
        progressAnim.start()

        // Navigate after splash
        progress.postDelayed({
            particleView.stop()
            val prefs = getSharedPreferences("ydn18_prefs", MODE_PRIVATE)
            val firstLaunch = prefs.getBoolean("first_launch", true)
            if (firstLaunch) {
                startActivity(Intent(this, PermissionSetupActivity::class.java))
            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 3000)
    }
}

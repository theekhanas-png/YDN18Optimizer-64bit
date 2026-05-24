package com.yodhan18.ydn18optimizer.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class ParticleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var radius: Float,
        var alpha: Float,
        var color: Int
    )

    private val particles = mutableListOf<Particle>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var running = false
    private val random = Random.Default

    private val colors = intArrayOf(
        Color.parseColor("#FF6D00"),
        Color.parseColor("#FFD600"),
        Color.parseColor("#FF9100"),
        Color.parseColor("#00E5FF"),
        Color.parseColor("#FF1744")
    )

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0 && !running) {
            running = true
            repeat(50) { addParticle() }
            animate()
        }
    }

    private fun addParticle() {
        val cx = width / 2f
        val cy = height / 2f
        val angle = random.nextFloat() * (Math.PI * 2).toFloat()
        particles.add(
            Particle(
                x = cx + (random.nextFloat() - 0.5f) * 80f,
                y = cy + (random.nextFloat() - 0.5f) * 40f,
                vx = cos(angle) * (random.nextFloat() * 2f),
                vy = -(random.nextFloat() * 5f + 2f),
                radius = random.nextFloat() * 6f + 2f,
                alpha = 1f,
                color = colors[random.nextInt(colors.size)]
            )
        )
    }

    private fun animate() {
        if (!running) return
        postDelayed({
            updateParticles()
            invalidate()
            if (running) animate()
        }, 16)
    }

    private fun updateParticles() {
        val toRemove = mutableListOf<Particle>()
        for (p in particles) {
            p.x += p.vx
            p.y += p.vy
            p.vy += 0.05f
            p.alpha -= 0.015f
            p.radius -= 0.03f
            if (p.alpha <= 0f || p.radius <= 0f) toRemove.add(p)
        }
        particles.removeAll(toRemove.toSet())
        while (particles.size < 50) addParticle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (p in particles) {
            paint.color = p.color
            paint.alpha = (p.alpha * 255).toInt().coerceIn(0, 255)
            canvas.drawCircle(p.x, p.y, p.radius, paint)
        }
    }

    fun stop() {
        running = false
        particles.clear()
    }
}

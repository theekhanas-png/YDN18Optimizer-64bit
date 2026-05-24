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

    data class Particle(
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
    private var running = true
    private val random = Random.Default

    private val COLORS = intArrayOf(
        Color.parseColor("#FF6D00"),
        Color.parseColor("#FFD600"),
        Color.parseColor("#FF9100"),
        Color.parseColor("#00E5FF"),
        Color.parseColor("#FF1744")
    )

    init {
        post { spawnParticles() }
    }

    private fun spawnParticles() {
        if (width == 0 || height == 0) {
            postDelayed({ spawnParticles() }, 100)
            return
        }
        repeat(60) {
            addParticle()
        }
        animate()
    }

    private fun addParticle() {
        val cx = width / 2f
        val cy = height / 2f
        val angle = random.nextFloat() * Math.PI * 2
        val speed = random.nextFloat() * 4f + 1f
        particles.add(
            Particle(
                x = cx + (random.nextFloat() - 0.5f) * 80f,
                y = cy + (random.nextFloat() - 0.5f) * 40f,
                vx = cos(angle).toFloat() * speed * 0.5f,
                vy = -(random.nextFloat() * 5f + 2f),
                radius = random.nextFloat() * 6f + 2f,
                alpha = 1f,
                color = COLORS[random.nextInt(COLORS.size)]
            )
        )
    }

    private fun animate() {
        if (!running) return
        postDelayed({
            updateParticles()
            invalidate()
            animate()
        }, 16)
    }

    private fun updateParticles() {
        val toRemove = mutableListOf<Particle>()
        for (p in particles) {
            p.x += p.vx
            p.y += p.vy
            p.vy += 0.05f // slight gravity
            p.alpha -= 0.015f
            p.radius -= 0.03f
            if (p.alpha <= 0 || p.radius <= 0) {
                toRemove.add(p)
            }
        }
        particles.removeAll(toRemove)
        // Spawn new particles
        if (particles.size < 60) {
            repeat(3) { addParticle() }
        }
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
    }
}

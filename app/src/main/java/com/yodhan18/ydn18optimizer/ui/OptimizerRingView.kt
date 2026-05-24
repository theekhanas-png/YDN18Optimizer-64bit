package com.yodhan18.ydn18optimizer.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class OptimizerRingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 16f
        strokeCap = Paint.Cap.ROUND
    }
    private val bgRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 16f
        color = Color.parseColor("#1E2A4A")
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 24f
        strokeCap = Paint.Cap.ROUND
        alpha = 60
    }
    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00E5FF")
        textAlign = Paint.Align.CENTER
        textSize = 40f
        typeface = Typeface.DEFAULT_BOLD
    }
    private val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0D1227")
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00E5FF")
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private var rotation = 0f
    private var pulseScale = 1f

    private var rotAnim: ValueAnimator? = null
    private var pulseAnim: ValueAnimator? = null

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun startAnimation() {
        rotAnim?.cancel()
        pulseAnim?.cancel()

        rotAnim = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 1500
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { anim ->
                rotation = anim.animatedValue as Float
                invalidate()
            }
        }
        rotAnim!!.start()

        pulseAnim = ValueAnimator.ofFloat(0.9f, 1.1f, 0.9f).apply {
            duration = 1000
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { anim ->
                pulseScale = anim.animatedValue as Float
                invalidate()
            }
        }
        pulseAnim!!.start()
    }

    fun stopAnimation() {
        rotAnim?.cancel()
        pulseAnim?.cancel()
        rotAnim = null
        pulseAnim = null
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return

        val cx = width / 2f
        val cy = height / 2f
        val radius = (minOf(width, height) / 2f) - 20f
        val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

        // Background ring
        canvas.drawArc(rect, 0f, 360f, false, bgRingPaint)

        // Animated sweep gradient ring
        val shader = SweepGradient(
            cx, cy,
            intArrayOf(
                Color.parseColor("#00E5FF"),
                Color.parseColor("#FF6D00"),
                Color.parseColor("#FFD600"),
                Color.parseColor("#00E5FF")
            ),
            floatArrayOf(0f, 0.33f, 0.66f, 1f)
        )
        ringPaint.shader = shader
        glowPaint.shader = shader

        canvas.save()
        canvas.rotate(rotation, cx, cy)
        canvas.drawArc(rect, 0f, 300f, false, ringPaint)
        canvas.drawArc(rect, 0f, 300f, false, glowPaint)
        canvas.restore()

        // Center fill circle
        val innerRadius = radius * 0.45f * pulseScale
        canvas.drawCircle(cx, cy, innerRadius, innerPaint)

        // Border pulse
        borderPaint.alpha = (pulseScale * 180).toInt().coerceIn(0, 255)
        canvas.drawCircle(cx, cy, innerRadius, borderPaint)

        // Center text
        canvas.drawText(">>", cx, cy + 14f, iconPaint)
    }
}

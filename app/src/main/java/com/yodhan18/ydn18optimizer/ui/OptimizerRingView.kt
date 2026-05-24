package com.yodhan18.ydn18optimizer.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
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
        textSize = 48f
        typeface = Typeface.DEFAULT_BOLD
    }

    private var rotation = 0f
    private var progress = 0f
    private var pulseScale = 1f

    private val rotAnim = ValueAnimator.ofFloat(0f, 360f).apply {
        duration = 1500
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            rotation = it.animatedValue as Float
            invalidate()
        }
    }
    private val progressAnim = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 3000
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener {
            progress = it.animatedValue as Float
        }
    }
    private val pulseAnim = ValueAnimator.ofFloat(0.9f, 1.1f, 0.9f).apply {
        duration = 1000
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            pulseScale = it.animatedValue as Float
            invalidate()
        }
    }

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun startAnimation() {
        rotAnim.start()
        progressAnim.start()
        pulseAnim.start()
    }

    fun stopAnimation() {
        rotAnim.cancel()
        progressAnim.cancel()
        pulseAnim.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val radius = (minOf(width, height) / 2f) - 20f

        val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

        // Background ring
        canvas.drawArc(rect, 0f, 360f, false, bgRingPaint)

        // Animated gradient ring
        val shader = SweepShader(
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

        // Center pulse circle
        val innerRadius = radius * 0.45f * pulseScale
        val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0D1227")
        }
        canvas.drawCircle(cx, cy, innerRadius, innerPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00E5FF")
            style = Paint.Style.STROKE
            strokeWidth = 3f
            alpha = (pulseScale * 180).toInt().coerceIn(0, 255)
        }
        canvas.drawCircle(cx, cy, innerRadius, borderPaint)

        // Lightning bolt text icon
        canvas.drawText("⚡", cx, cy + 18f, iconPaint)
    }

    private fun SweepShader(cx: Float, cy: Float, colors: IntArray, positions: FloatArray): SweepGradient {
        return SweepGradient(cx, cy, colors, positions)
    }
}

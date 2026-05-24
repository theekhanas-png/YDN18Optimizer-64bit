package com.yodhan18.ydn18optimizer.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SweepGradient
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class CountdownRingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 12f
        color = Color.parseColor("#1E2A4A")
    }
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 12f
        strokeCap = Paint.Cap.ROUND
    }
    private val trailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 20f
        strokeCap = Paint.Cap.ROUND
        alpha = 40
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00E5FF")
        textAlign = Paint.Align.CENTER
        textSize = 52f
        typeface = Typeface.DEFAULT_BOLD
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#546E7A")
        textAlign = Paint.Align.CENTER
        textSize = 20f
    }

    private var sweepAngle = 360f
    private var secondsLeft = 10
    private var rotOffset = 0f
    private var rotAnim: ValueAnimator? = null
    private var tickAnim: ValueAnimator? = null

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun startCountdown(totalSeconds: Int, onTick: (Int) -> Unit, onFinish: () -> Unit) {
        secondsLeft = totalSeconds
        sweepAngle = 360f

        rotAnim?.cancel()
        tickAnim?.cancel()

        rotAnim = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { anim ->
                rotOffset = anim.animatedValue as Float
                invalidate()
            }
        }
        rotAnim!!.start()

        tickAnim = ValueAnimator.ofFloat(360f, 0f).apply {
            duration = (totalSeconds * 1000).toLong()
            interpolator = LinearInterpolator()
            addUpdateListener { anim ->
                sweepAngle = anim.animatedValue as Float
                val sec = ((sweepAngle / 360f) * totalSeconds).toInt() + 1
                if (sec != secondsLeft) {
                    secondsLeft = sec.coerceIn(0, totalSeconds)
                    onTick(secondsLeft)
                }
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    secondsLeft = 0
                    sweepAngle = 0f
                    rotAnim?.cancel()
                    invalidate()
                    onFinish()
                }
            })
        }
        tickAnim!!.start()
    }

    fun stopCountdown() {
        rotAnim?.cancel()
        tickAnim?.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return
        val cx = width / 2f
        val cy = height / 2f
        val radius = (minOf(width, height) / 2f) - 16f
        val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

        canvas.drawArc(rect, -90f, 360f, false, bgPaint)

        val shader = SweepGradient(
            cx, cy,
            intArrayOf(
                Color.parseColor("#00E5FF"),
                Color.parseColor("#2979FF"),
                Color.parseColor("#00E5FF")
            ),
            null
        )
        arcPaint.shader = shader
        trailPaint.shader = shader

        canvas.save()
        canvas.rotate(rotOffset, cx, cy)
        canvas.drawArc(rect, -90f, sweepAngle, false, trailPaint)
        canvas.restore()

        canvas.drawArc(rect, -90f, sweepAngle, false, arcPaint)

        canvas.drawText("$secondsLeft", cx, cy + 18f, textPaint)
        canvas.drawText("sec", cx, cy + 40f, labelPaint)
    }
}

package com.yodhan18.ydn18optimizer.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator

class ScoreView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1E2A4A")
        style = Paint.Style.STROKE
        strokeWidth = 14f
    }
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 14f
        strokeCap = Paint.Cap.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = 48f
        typeface = Typeface.DEFAULT_BOLD
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B0BEC5")
        textAlign = Paint.Align.CENTER
        textSize = 22f
    }
    private val goodPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00E676")
        textAlign = Paint.Align.CENTER
        textSize = 28f
        typeface = Typeface.DEFAULT_BOLD
    }

    private var score = 0
    private var displayScore = 0
    private var showAllGood = false

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun setScore(newScore: Int, animated: Boolean = true) {
        showAllGood = newScore >= 100
        if (animated) {
            val anim = ValueAnimator.ofInt(displayScore, newScore)
            anim.duration = 1200
            anim.interpolator = DecelerateInterpolator()
            anim.addUpdateListener {
                displayScore = it.animatedValue as Int
                score = displayScore
                invalidate()
            }
            anim.start()
        } else {
            score = newScore
            displayScore = newScore
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val radius = (minOf(width, height) / 2f) - 20f

        // Background arc
        val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
        canvas.drawArc(rect, 135f, 270f, false, bgPaint)

        // Colored arc based on score
        val color = when {
            score >= 80 -> Color.parseColor("#00E676")
            score >= 50 -> Color.parseColor("#FFD600")
            else -> Color.parseColor("#FF1744")
        }
        arcPaint.color = color
        arcPaint.shader = LinearGradient(
            cx - radius, cy, cx + radius, cy,
            Color.parseColor("#00E5FF"), color, Shader.TileMode.CLAMP
        )
        val sweep = (score / 100f) * 270f
        canvas.drawArc(rect, 135f, sweep, false, arcPaint)

        // Score text
        if (showAllGood) {
            canvas.drawText("100", cx, cy + 16f, textPaint.apply { color = Color.parseColor("#00E676") })
            canvas.drawText("All Good ✓", cx, cy + 48f, goodPaint)
        } else {
            canvas.drawText("$score", cx, cy + 16f, textPaint.apply { color = Color.WHITE })
            canvas.drawText("Mobile Score", cx, cy + 44f, labelPaint)
        }
    }
}

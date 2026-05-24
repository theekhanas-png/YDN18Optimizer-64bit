package com.yodhan18.ydn18optimizer.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatImageView

class GlowImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00E5FF")
        maskFilter = BlurMaskFilter(40f, BlurMaskFilter.Blur.OUTER)
    }
    private var glowAlpha = 0.3f

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        startGlowAnimation()
    }

    private fun startGlowAnimation() {
        val anim = ValueAnimator.ofFloat(0.3f, 1f, 0.3f)
        anim.duration = 2000
        anim.repeatCount = ValueAnimator.INFINITE
        anim.interpolator = LinearInterpolator()
        anim.addUpdateListener { animator ->
            glowAlpha = animator.animatedValue as Float
            invalidate()
        }
        anim.start()
    }

    override fun onDraw(canvas: Canvas) {
        glowPaint.alpha = (glowAlpha * 80).toInt().coerceIn(0, 255)
        val cx = width / 2f
        val cy = height / 2f
        canvas.drawCircle(cx, cy, width / 2f * 0.9f, glowPaint)
        super.onDraw(canvas)
    }
}

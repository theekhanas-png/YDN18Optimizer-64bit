package com.yodhan18.ydn18optimizer.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatImageView

class GlowImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var glowAlpha = 0f
    private val glowColor = Color.parseColor("#00E5FF")

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        startGlowAnimation()
    }

    private fun startGlowAnimation() {
        val anim = ValueAnimator.ofFloat(0.3f, 1f, 0.3f)
        anim.duration = 2000
        anim.repeatCount = ValueAnimator.INFINITE
        anim.interpolator = LinearInterpolator()
        anim.addUpdateListener {
            glowAlpha = it.animatedValue as Float
            invalidate()
        }
        anim.start()
    }

    override fun onDraw(canvas: Canvas) {
        // Draw glow halo
        glowPaint.color = glowColor
        glowPaint.alpha = (glowAlpha * 80).toInt()
        glowPaint.maskFilter = BlurMaskFilter(40f, BlurMaskFilter.Blur.OUTER)
        val cx = width / 2f
        val cy = height / 2f
        canvas.drawCircle(cx, cy, width / 2f * 0.9f, glowPaint)
        super.onDraw(canvas)
    }
}

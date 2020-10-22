package com.kolibree.android.sba.testbrushing.duringsession.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import com.kolibree.android.sba.R

internal class CarouselIndicatorView : View {

    private val background = Paint(Paint.ANTI_ALIAS_FLAG)
    private val foreground = Paint(Paint.ANTI_ALIAS_FLAG)
    private val indicator = Indicator(ITEMS)

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        initView()
    }

    private fun initView() {
        background.color = ContextCompat.getColor(context, R.color.colorPrimaryDisabled)
        foreground.color = ContextCompat.getColor(context, R.color.colorPrimaryDark)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (canvas != null) {
            render(canvas)
        }

        if (indicator.isAnimating()) {
            invalidate()
        }
    }

    private fun render(canvas: Canvas) {
        canvas.drawColor(background.color)
        indicator.update(canvas, foreground)
    }

    fun indicate(part: Int, withAnimation: Boolean) {
        indicator.indicate(part, withAnimation)
        invalidate()
    }

    companion object {
        const val ITEMS = 4
    }
}

private class Indicator(val items: Int) {

    private companion object {
        const val ANIMATION_DURATION = 300
    }

    private var currentPart = 0
    private var startAnimationTime: Long = 0
    private val interpolator = AccelerateDecelerateInterpolator()

    fun update(canvas: Canvas, paint: Paint) {
        val percent = animationPercent()

        val canvasWidth = canvas.width.toFloat()
        val indicatorWidth = canvasWidth / items
        val indicatorHeight = canvas.height.toFloat()
        val startPosition = currentPart * indicatorWidth
        val stopPosition = (currentPart + 1) * indicatorWidth
        val distance = (stopPosition - startPosition) * percent
        val currentPosition = startPosition + distance - indicatorWidth

        canvas.drawRect(
            currentPosition,
            0f,
            currentPosition + indicatorWidth,
            indicatorHeight,
            paint
        )

        if (currentPart == 0) {
            val lastPartDistance = indicatorWidth - distance
            val lastPartPosition = canvas.width - lastPartDistance
            canvas.drawRect(lastPartPosition, 0f, canvasWidth, indicatorHeight, paint)
        }
    }

    fun isAnimating() = animationPercent() < 1f

    private fun animationPercent(): Float {
        val timeElapsed = SystemClock.elapsedRealtime() - startAnimationTime
        val percent = timeElapsed / ANIMATION_DURATION.toFloat()
        val linearPercent = Math.min(1.0f, percent)
        return interpolator.getInterpolation(linearPercent)
    }

    fun indicate(part: Int, withAnimation: Boolean) {
        if (part != currentPart) {
            if (withAnimation) {
                startAnimationTime = SystemClock.elapsedRealtime()
            }
            currentPart = part
        }
    }
}

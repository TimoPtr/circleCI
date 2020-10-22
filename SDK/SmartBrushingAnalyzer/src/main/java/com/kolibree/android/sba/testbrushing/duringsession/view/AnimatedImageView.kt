package com.kolibree.android.sba.testbrushing.duringsession.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.annotation.RawRes
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.kolibree.android.sba.R
import kotlinx.android.synthetic.main.animated_image_view.view.*

internal class AnimatedImageView : LinearLayout {

    private var animatedRes: Int = 0

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        initView()
    }

    private fun initView() {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.animated_image_view, this, true)
    }

    fun setResource(@RawRes resId: Int, @ColorRes colorId: Int) {
        animatedRes = resId
        image.setBackgroundColor(ContextCompat.getColor(context, colorId))
        Glide.with(context)
            .load(animatedRes)
            .into(image)
    }

    /**
     * Put the translationX to the right of the image (hide it on the right of the screen)
     */
    fun translateToRight() {
        val width = image.width.toFloat()
        image.translationX = width
    }

    /**
     * Move the image from outside right to the 0 position (right to left movement)
     */
    fun slideIn() {
        translateToRight()
        image.animate()
            .translationX(0f)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    /**
     * Move the image from the 0 position to the outside left (right to left movement)
     */
    fun slideOut() {
        val width = image.width.toFloat()
        image.translationX = 0f
        image.animate()
            .translationX(-width)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    fun isOnScreen() = image.translationX == 0f

    fun shouldAnimate(@RawRes resId: Int) = isOnScreen() && resId != animatedRes
}

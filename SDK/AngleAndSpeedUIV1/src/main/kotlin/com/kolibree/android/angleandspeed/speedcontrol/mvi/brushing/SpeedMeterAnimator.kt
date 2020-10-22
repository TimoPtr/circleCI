/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.speedcontrol.mvi.brushing

import android.view.ViewPropertyAnimator
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.annotation.VisibleForTesting
import com.kolibree.android.commons.Weak
import org.threeten.bp.Duration

private const val DURATION = 3L // 3s duration make animation smoother and it's approx KML response frequency * 3
private const val ROTATION_OFFSET_RATIO = 0.08
private const val LOWEST_SCALE = -88f
private const val HIGHEST_SCALE = 88f
private const val NORMAL_SCALE = 0f

internal class SpeedMeterAnimator(pointer: ImageView) {
    private var pointer by Weak { pointer }
    private val duration: Duration = Duration.ofSeconds(DURATION)
    @VisibleForTesting
    lateinit var pointerAnimation: ViewPropertyAnimator

    fun init() {
        initPointer()
    }

    private fun initPointer() {
        pointer?.let {
            val rotationOffset = it.measuredHeight * ROTATION_OFFSET_RATIO
            it.pivotX = it.measuredWidth / 2.toFloat()
            it.pivotY = it.measuredHeight - rotationOffset.toFloat()
            pointerAnimation = it.animate()
            pointerAnimation.interpolator = DecelerateInterpolator()
            pointerAnimation.duration = duration.toMillis()
        }
    }

    @VisibleForTesting
    internal fun rotate(degree: Float) {
        cancel()
        pointerAnimation.rotation(degree)
    }

    fun cancel() {
        pointerAnimation.cancel()
    }

    fun showNormalSpeed() {
        rotate(NORMAL_SCALE)
    }

    fun showHighSpeed() {
        rotate(HIGHEST_SCALE)
    }

    fun showLowSpeed() {
        rotate(LOWEST_SCALE)
    }
}

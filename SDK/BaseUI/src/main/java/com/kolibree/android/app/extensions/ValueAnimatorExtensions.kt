/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.extensions

import android.animation.ValueAnimator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import androidx.annotation.Keep

@Keep
fun withValueAnimator(
    interpolator: Interpolator = AccelerateInterpolator(),
    duration: Long = DURATION,
    block: (Float) -> Unit
): ValueAnimator {
    val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
    valueAnimator.interpolator = interpolator
    valueAnimator.duration = duration
    valueAnimator.addUpdateListener { animation ->
        val progress = animation.animatedValue as Float
        block(progress)
    }
    valueAnimator.start()
    return valueAnimator
}

@Keep
fun withValueAnimatorDesc(
    interpolator: Interpolator = DecelerateInterpolator(),
    duration: Long = DURATION,
    block: (Float) -> Unit
): ValueAnimator = withValueAnimator(
    interpolator,
    duration
) { block(1f - it) }

private const val DURATION = 300L

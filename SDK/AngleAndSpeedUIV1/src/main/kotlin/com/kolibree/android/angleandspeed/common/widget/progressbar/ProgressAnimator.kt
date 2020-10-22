/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.angleandspeed.common.widget.progressbar

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar
import androidx.annotation.VisibleForTesting
import com.kolibree.android.commons.Weak
import org.threeten.bp.Duration

private const val MAX_PROGRESS = 300 // increase the max progress make animation more smoother

/**
 * Responsible for smoothing [ProgressBar] animation.
 * @see [ProgressState]
 */
@SuppressLint("ExperimentalClassUse")
internal class ProgressAnimator(
    progressBar: ProgressBar,
    @VisibleForTesting val maxDuration: Duration
) {
    @VisibleForTesting
    var progressBarReference by Weak { progressBar }

    @VisibleForTesting
    lateinit var progressAnimation: ValueAnimator

    fun init() {
        progressBarReference?.max = MAX_PROGRESS
        progressBarReference?.progress = 0
        progressAnimation = createAnimation()
    }

    @VisibleForTesting
    internal fun createAnimation(): ValueAnimator {
        val animator = ValueAnimator.ofFloat(0.toFloat(), MAX_PROGRESS.toFloat())
        animator.interpolator = LinearInterpolator()
        animator.duration = maxDuration.toMillis()
        animator.addUpdateListener { v ->
            val value = (v.animatedValue) as Float
            if (progressBarReference?.progress != MAX_PROGRESS) {
                progressBarReference?.progress = value.toInt()
            }
        }
        return animator
    }

    /**
     * start the animation
     */
    fun start() {
        if (progressAnimation.isStarted) {
            progressAnimation.resume()
        } else {
            progressAnimation.start()
        }
    }

    /**
     * pause the animation
     */
    fun pause() {
        progressAnimation.pause()
    }

    /**
     * reset the progressBar and animation and starts it again
     */
    fun reset() {
        progressAnimation.cancel()
        progressBarReference?.progress = 0
        progressAnimation.start()
    }

    /**
     * cancel the animation and clean the instance
     */
    fun destroy() {
        progressAnimation.cancel()
        progressBarReference = null
    }

    fun changeAnimatorState(state: ProgressState) {
        when (state) {
            ProgressState.START -> start()
            ProgressState.PAUSE -> pause()
            ProgressState.RESET -> reset()
            ProgressState.DESTROY -> destroy()
        }
    }
}

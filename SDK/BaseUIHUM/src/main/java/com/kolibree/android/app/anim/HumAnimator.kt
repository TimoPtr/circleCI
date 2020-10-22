/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.anim

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.Keep
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.app.widget.AnimatorGroup

/**
 * Collection of animators, which can be executed on a collection of views. Those animators
 * can be passed to [AnimatorGroup] for execution.
 */
@VisibleForApp
sealed class HumAnimator(
    private val animator: ValueAnimator,
    private val updateFunction: (View, Any) -> Unit
) {
    @VisibleForApp
    object Hide : HumAnimator(
        animator = ValueAnimator.ofInt(1, 0).withDuration(1),
        updateFunction = { view, _ -> view.alpha = 0f }
    )

    @VisibleForApp
    object Show : HumAnimator(
        animator = ValueAnimator.ofInt(0, 1).withDuration(1),
        updateFunction = { view, _ -> view.alpha = 1f }
    )

    @VisibleForApp
    class FadeIn(duration: Long) : HumAnimator(
        animator = ValueAnimator.ofFloat(0f, 1f).withDuration(duration),
        updateFunction = { view, animatedValue -> view.alpha = animatedValue as Float }
    )

    @VisibleForApp
    class FadeOut(duration: Long) : HumAnimator(
        animator = ValueAnimator.ofFloat(1f, 0f).withDuration(duration),
        updateFunction = { view, animatedValue -> view.alpha = animatedValue as Float }
    )

    @VisibleForApp
    class TranslateX(from: Float, to: Float, duration: Long) : HumAnimator(
        animator = ValueAnimator.ofFloat(from, to).withDuration(duration),
        updateFunction = { view, animatedValue -> view.translationX = animatedValue as Float }
    )

    @VisibleForApp
    class TranslateY(from: Float, to: Float, duration: Long) : HumAnimator(
        animator = ValueAnimator.ofFloat(from, to).withDuration(duration),
        updateFunction = { view, animatedValue -> view.translationY = animatedValue as Float }
    )

    fun asPair() = Pair(animator, updateFunction)

    fun start(view: View) {
        animator.addUpdateListener { valueAnimator ->
            updateFunction(view, valueAnimator.animatedValue)
        }
        animator.start()
    }
}

@Keep
fun AnimatorGroup.add(animator: HumAnimator) =
    addAnimator(animator.asPair())

private fun ValueAnimator.withDuration(duration: Long, startDelay: Long = duration * 2): ValueAnimator {
    this.interpolator = DecelerateInterpolator()
    this.duration = duration
    this.startDelay = startDelay
    return this
}

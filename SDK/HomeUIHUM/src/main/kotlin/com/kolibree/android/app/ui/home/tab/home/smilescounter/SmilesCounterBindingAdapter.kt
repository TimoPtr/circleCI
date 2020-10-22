/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.home.tab.home.smilescounter

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.FloatRange
import androidx.annotation.Keep
import androidx.core.animation.addListener
import androidx.databinding.BindingAdapter
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RelativeCornerSize
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.TriangleEdgeTreatment
import com.kolibree.android.app.widget.SlotMachineView
import com.kolibree.android.homeui.hum.R

@BindingAdapter(
    value = ["smilesCounterState", "duration", "startDelay", "fadeDuration", "fadeStartDelay"],
    requireAll = true
)
internal fun SlotMachineView.setSmilesCounterState(
    state: SmilesCounterState?,
    duration: Long,
    startDelay: Long,
    fadeDuration: Long,
    fadeStartDelay: Long
) {
    if (state != null && state != SmilesCounterState.NoInternet && isFullyVisible) {
        fadeIn(true, fadeDuration, startDelay)
    }
    when (state) {
        is SmilesCounterState.PlayIncrease -> animateReels(state, duration, startDelay, fadeDuration)
        is SmilesCounterState.PlayLanding -> playLanding(state)
        is SmilesCounterState.Idle -> idle(state)
        is SmilesCounterState.NoInternet -> fadeOut(fadeDuration, fadeStartDelay)
    }
}

private val SlotMachineView.isFullyVisible get() = visibility != View.VISIBLE || alpha < 1f

private fun SlotMachineView.animateReels(
    state: SmilesCounterState.PlayIncrease,
    duration: Long,
    startDelay: Long,
    fadeDuration: Long
) {
    startValue = state.initialPoints
    endValue = state.finalPoints
    alpha = 0f
    buildReelsAnimator(startDelay, duration, fadeDuration).start()
}

private const val DECELERATE_FACTOR = 3f

private fun SlotMachineView.buildReelsAnimator(
    startDelay: Long,
    duration: Long,
    fadeDuration: Long
): Animator =
    AnimatorSet().also { animatorSet ->
        animatorSet.startDelay = startDelay
        animatorSet.playTogether(
            ObjectAnimator.ofFloat(this, "position", 0f, 1f).apply {
                interpolator = DecelerateInterpolator(DECELERATE_FACTOR)
                setDuration(duration)
            },
            ObjectAnimator.ofFloat(this, "alpha", 0f, 1f)
                .setDuration(fadeDuration)
        )
    }

private fun SlotMachineView.playLanding(
    state: SmilesCounterState.PlayLanding
) {
    startValue = state.points
    endValue = state.points
}

private fun SlotMachineView.idle(state: SmilesCounterState.Idle) {
    startValue = state.points
    endValue = state.points
}

@Keep
@BindingAdapter(value = ["fadeIn", "fadeDuration", "startDelay", "replay"], requireAll = true)
fun View.fadeIn(doFadeIn: Boolean, duration: Long, startDelay: Long, replay: Boolean = false) {
    if (doFadeIn && (replay || visibility != View.VISIBLE)) {
        visibility = View.VISIBLE
        alpha = 0f
        animate()
            .alpha(1f)
            .setDuration(duration)
            .setStartDelay(startDelay)
            .start()
    }
}

@Keep
fun View.fadeOut(duration: Long, startDelay: Long) {
    if (visibility == View.VISIBLE) {
        animate()
            .alpha(0f)
            .setDuration(duration)
            .setStartDelay(startDelay)
            .start()
    }
}

@Keep
@BindingAdapter(value = ["noInternet", "noInternetDuration"], requireAll = true)
fun View.noInternet(noInternet: Boolean, fadeDuration: Long) {
    if (noInternet) {
        animate()
            .alpha(1f)
            .setDuration(fadeDuration)
            .start()
    } else {
        animate()
            .alpha(0f)
            .setDuration(fadeDuration)
            .start()
    }
}

@Keep
@BindingAdapter(value = ["noInternet", "fadeDuration"], requireAll = true)
fun LottieAnimationView.noInternet(noInternet: Boolean, fadeDuration: Long) {
    if (visibility != View.VISIBLE && noInternet) {
        visibility = View.VISIBLE
        alpha = 0f
        animate()
            .alpha(1f)
            .setDuration(fadeDuration)
            .start()
    }
}

@Keep
@BindingAdapter(
    value = ["speechBubbleShape", "speechBubbleCornerFraction", "speechBubbleStemSize"],
    requireAll = true
)
fun TextView.speechBubbleShape(
    @ColorInt color: Int,
    @FloatRange(from = 0.0, to = 1.0) cornerFraction: Float,
    @Dimension stemSize: Float
) {
    speechBubbleShape(ColorStateList.valueOf(color), cornerFraction, stemSize)
}

@Keep
@BindingAdapter(
    value = ["speechBubbleShape", "speechBubbleCornerFraction", "speechBubbleStemSize"],
    requireAll = true
)
fun TextView.speechBubbleShape(
    color: ColorStateList,
    @FloatRange(from = 0.0, to = 1.0) cornerFraction: Float,
    @Dimension stemSize: Float
) {
    background = MaterialShapeDrawable(
        ShapeAppearanceModel.builder()
            .setAllCornerSizes(RelativeCornerSize(cornerFraction))
            .setBottomEdge(TriangleEdgeTreatment(stemSize, false))
            .build()
    ).apply {
        fillColor = color
    }
}

private fun View.buildSpeechBubbleAnimator(startDelay: Long, duration: Long): Animator =
    AnimatorSet().also { animatorSet ->
        animatorSet.playTogether(
            buildAnimator("alpha", startDelay, duration, 0f, 1f),
            buildAnimator("translationY", startDelay, duration, height.toFloat(), 0f),
            buildAnimator("alpha", startDelay + (duration * 2), duration, 1f, 0f),
            buildAnimator(
                "translationY",
                startDelay + (duration * 2),
                duration,
                0f,
                height.toFloat()
            )
        )
    }

@Suppress("SpreadOperator")
private fun View.buildAnimator(
    propertyName: String,
    startDelay: Long,
    duration: Long,
    vararg values: Float
): Animator = buildAnimator(propertyName, startDelay, duration, null, *values)

@Suppress("SpreadOperator")
private fun View.buildAnimator(
    propertyName: String,
    startDelay: Long,
    duration: Long,
    interpolator: Interpolator? = null,
    vararg values: Float
): Animator =
    ObjectAnimator.ofFloat(this, propertyName, *values).apply {
        setStartDelay(startDelay)
        setDuration(duration)
        interpolator?.also {
            setInterpolator(interpolator)
        }
    }

@Keep
@BindingAdapter(
    value = ["speechBubbleAnimation"]
)
internal fun TextView.speechBubbleAnimation(animation: SpeechBubbleAnimation<*>?) {
    when (animation) {
        is SpeechBubbleAnimation.Hide -> speechBubbleHide(animation)
        is SpeechBubbleAnimation.Pending -> speechBubblePending(animation)
        is SpeechBubbleAnimation.PointsIncrease -> speechBubblePointsIncrease(animation)
        is SpeechBubbleAnimation.NoInternet -> speechBubbleNoInternet(animation)
    }
}

private fun TextView.speechBubbleHide(animation: SpeechBubbleAnimation.Hide) {
    animate().alpha(0f).setDuration(animation.getDuration(context)).start()
}

private fun TextView.speechBubblePending(animation: SpeechBubbleAnimation.Pending) {
    text = animation.getFormattedString(context)
    val duration = animation.getDuration(context)
    animate().alpha(1f).setDuration(duration).start()
    val currentAnimator = (getTag(R.id.pending_label_animator) as? Animator)
        ?: buildPendingAnimation(animation.getStartDelay(context), duration).also { animator ->
            animator.addListener(onEnd = {
                animator.start()
            })
        }
    currentAnimator.start()
}

private fun TextView.speechBubblePointsIncrease(animation: SpeechBubbleAnimation.PointsIncrease) {
    text = animation.getFormattedString(context)
    buildSpeechBubbleAnimator(
        animation.getStartDelay(context),
        animation.getDuration(context)
    ).start()
}

private fun TextView.speechBubbleNoInternet(animation: SpeechBubbleAnimation.NoInternet) {
    text = animation.getFormattedString(context)
    translationY = 0f
    (getTag(R.id.pending_label_animator) as? Animator)?.apply {
        removeAllListeners()
        cancel()
        setTag(R.id.pending_label_animator, null)
    }
    fadeIn(true, animation.getDuration(context), animation.getStartDelay(context), true)
}

private fun TextView.buildPendingAnimation(pendingStartDelay: Long, pendingDuration: Long) =
    AnimatorSet().also { animatorSet ->
        val interpolator = AccelerateDecelerateInterpolator()
        animatorSet.playTogether(
            buildAnimator("translationY", pendingStartDelay, pendingDuration, interpolator, 0f, height / 2f),
            buildAnimator(
                "translationY",
                pendingStartDelay + pendingDuration,
                pendingDuration,
                interpolator,
                height / 2f,
                0f
            )
        )
        setTag(R.id.pending_label_animator, animatorSet)
    }

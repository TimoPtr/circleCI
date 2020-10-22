/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.databinding.bindingadapter

import android.animation.ValueAnimator
import android.os.Parcelable
import androidx.annotation.IntegerRes
import androidx.annotation.Keep
import androidx.annotation.RawRes
import androidx.databinding.BindingAdapter
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.kolibree.databinding.R
import kotlinx.android.parcel.Parcelize

@Keep
@BindingAdapter("lottie_rawRes", "lottie_loop")
fun LottieAnimationView.setRawRes(@RawRes lottieRawRes: Int?, lottieLoop: Boolean = true) {
    lottieRawRes?.let {
        repeatCount = if (lottieLoop) LottieDrawable.INFINITE else 1
        setAnimation(lottieRawRes)
        playAnimation()
    }
}

private const val NO_FRAME = -1

@Keep
@SuppressWarnings("LongMethod")
/* The method itself isn't long */
@BindingAdapter(
    value = [
        "lottie_rawRes",
        "lottie_loop",
        "lottie_autoPlay",
        "lottie_delayedMinFrame",
        "lottie_delayedMaxFrame"
    ],
    requireAll = false
)
fun LottieAnimationView.setRawResDelayed(
    @RawRes lottieRawRes: Int?,
    lottieLoop: Boolean = true,
    lottieAutoPlay: Boolean = true,
    delayedMinFrame: Int = NO_FRAME,
    delayedMaxFrame: Int = NO_FRAME
) {
    lottieRawRes?.let {
        repeatCount = if (lottieLoop) LottieDrawable.INFINITE else 1
        setAnimation(lottieRawRes)
        if (lottieLoop && delayedMinFrame != NO_FRAME && delayedMaxFrame != NO_FRAME) {
            addAnimatorUpdateListener(UpdateListener(this, delayedMinFrame, delayedMaxFrame))
        }
        if (lottieAutoPlay) {
            playAnimation()
        }
    }
}

@SuppressWarnings("LongMethod")
@Keep
@BindingAdapter(value = ["lottie_delayedLoop", "lottie_restart"], requireAll = false)
fun LottieAnimationView.setDelayedLoop(
    lottieDelayedLoop: LottieDelayedLoop? = null,
    restart: Boolean = false
) {
    lottieDelayedLoop?.also { delayedLoop ->
        val animationChanged = delayedLoop.rawRes != getTag(R.id.lottieRawResTag)
        if (animationChanged || restart) {
            resetAll()
            if (animationChanged) {
                setAnimation(delayedLoop.rawRes)
                setTag(R.id.lottieRawResTag, delayedLoop.rawRes)
            }
            applyLoopListener(delayedLoop)
        }

        if (lottieDelayedLoop.isPlaying && !isAnimating) {
            resumeAnimation()
        } else if (!lottieDelayedLoop.isPlaying && isAnimating) {
            pauseAnimation()
        }
    }
}

private fun LottieAnimationView.resetAll() {
    cancelAnimation()
    removeAllUpdateListeners()
    setMinAndMaxProgress(0f, 1f)
    frame = 0
}

private fun LottieAnimationView.applyLoopListener(delayedLoop: LottieDelayedLoop) {
    val startFrame = with(delayedLoop) {
        if (loopStartFrameRes != NO_FRAME) context.resources.getInteger(loopStartFrameRes)
        else loopStartFrame
    }

    val endFrame = with(delayedLoop) {
        if (loopEndFrameRes != NO_FRAME) context.resources.getInteger(loopEndFrameRes)
        else loopEndFrame
    }

    if (startFrame != NO_FRAME && endFrame != NO_FRAME) {
        repeatCount = LottieDrawable.INFINITE
        addAnimatorUpdateListener(
            UpdateListener(this, startFrame, endFrame)
        )
    }
}

@Keep
@Parcelize
data class LottieDelayedLoop(
    @RawRes val rawRes: Int,
    val loopStartFrame: Int = NO_FRAME,
    val loopEndFrame: Int = NO_FRAME,
    @IntegerRes val loopStartFrameRes: Int = NO_FRAME,
    @IntegerRes val loopEndFrameRes: Int = NO_FRAME,
    val isPlaying: Boolean = true
) : Parcelable

private class UpdateListener(
    private val lottieAnimationView: LottieAnimationView,
    private val minFrame: Int,
    private val maxFrame: Int
) : ValueAnimator.AnimatorUpdateListener {

    override fun onAnimationUpdate(animation: ValueAnimator?) {
        if (lottieAnimationView.frame > minFrame) {
            lottieAnimationView.removeUpdateListener(this)
            lottieAnimationView.setMinAndMaxFrame(minFrame, maxFrame)
        }
    }
}

@Keep
@BindingAdapter("lottie_playAnimation")
fun LottieAnimationView.lottiePlayAnimation(shouldPlay: Boolean) {
    if (shouldPlay && !isAnimating) {
        playAnimation()
    }
}

/**
 * If you want to kind of placeholder you can use android:src in XML directly (no databinding needed)
 */
@Keep
@BindingAdapter("lottie_url", "lottie_loop")
fun LottieAnimationView.setAnimationUrl(lottieUrl: String?, lottieLoop: Boolean = true) {
    lottieUrl?.let {
        repeatCount = if (lottieLoop) LottieDrawable.INFINITE else 1
        // We use the url as cache key
        setAnimationFromUrl(lottieUrl, lottieUrl)

        playAnimation()
    }
}

@BindingAdapter("lottie_json")
internal fun LottieAnimationView.setAnimationJson(lottieAnimationJson: String) {
    setAnimationFromJson(lottieAnimationJson, null)
    playAnimation()
}

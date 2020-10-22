/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.tilt.animated

import android.os.SystemClock
import android.view.animation.DecelerateInterpolator
import androidx.annotation.VisibleForTesting
import com.kolibree.android.jaws.tilt.JawsTiltController
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

/** Animated world-rotations-driven [JawsTiltController] implementation */
internal interface AnimatedTiltController : JawsTiltController {

    /**
     * Make the jaws face the left side of the view
     */
    fun faceLeft()

    /**
     * Make the jaws face the right side of the view
     */
    fun faceRight()

    /**
     * Make the jaws face the sky
     */
    fun faceTop()

    /**
     * Make the jaws face the floor
     */
    fun faceBottom()

    /**
     * Resets jaws world rotation vector
     */
    fun faceCenter()
}

/** [AnimatedTiltController] implementation */
internal class AnimatedTiltControllerImpl @Inject constructor() : AnimatedTiltController {

    private val interpolator = DecelerateInterpolator(DECELERATION_FACTOR)

    private var animationStartTime = 0L
    private var startRotationX = 0f
    private var startRotationY = 0f
    private var stopRotationX = 0f
    private var stopRotationY = 0f
    private var startTranslationX = 0f
    private var stopTranslationX = 0f

    override fun faceLeft() = animateTo(
        worldRotationX = 0f,
        worldRotationY = FACE_LEFT_WORLD_ROTATION_Y,
        translationX = FACE_LEFT_TRANSLATION_X
    )

    override fun faceRight() = animateTo(
        worldRotationX = 0f,
        worldRotationY = FACE_RIGHT_WORLD_ROTATION_Y,
        translationX = FACE_RIGHT_TRANSLATION_X
    )

    override fun faceTop() = animateTo(
        worldRotationX = FACE_TOP_WORLD_ROTATION_X,
        worldRotationY = 0f,
        translationX = 0f
    )

    override fun faceBottom() = animateTo(
        worldRotationX = FACE_BOTTOM_WORLD_ROTATION_X,
        worldRotationY = 0f,
        translationX = 0f
    )

    override fun faceCenter() = animateTo(
        worldRotationX = 0f,
        worldRotationY = 0f,
        translationX = 0f
    )

    override fun getJawsRotationX() = currentRotationX()

    override fun getJawsRotationY() = currentRotationY()

    override fun getTranslationX() = currentTranslationX()

    override fun onPause() {
        // no-op
    }

    override fun onResume() {
        // no-op
    }

    private fun currentRotationX() = interpolatedValue(
        start = startRotationX,
        stop = stopRotationX,
        progress = interpolatedAnimationProgress()
    )

    private fun currentRotationY() = interpolatedValue(
        start = startRotationY,
        stop = stopRotationY,
        progress = interpolatedAnimationProgress()
    )

    private fun currentTranslationX() = interpolatedValue(
        start = startTranslationX,
        stop = stopTranslationX,
        progress = interpolatedAnimationProgress()
    )

    private fun animateTo(worldRotationX: Float, worldRotationY: Float, translationX: Float) {
        startRotationX = currentRotationX()
        startRotationY = currentRotationY()
        startTranslationX = currentTranslationX()
        stopRotationX = worldRotationX
        stopRotationY = worldRotationY
        stopTranslationX = translationX
        animationStartTime = SystemClock.elapsedRealtime()
    }

    private fun interpolatedAnimationProgress(): Float {
        val elapsedTime = SystemClock.elapsedRealtime() - animationStartTime
        val progress = elapsedTime / ANIMATION_DURATION_MILLIS.toFloat()
        return interpolator.getInterpolation(min(1f, progress))
    }

    private fun interpolatedValue(start: Float, stop: Float, progress: Float): Float {
        val max = max(start, stop)
        val min = min(start, stop)
        val distance = max - min
        val progressDistance = distance * progress
        return if (stop > start) start + progressDistance else start - progressDistance
    }

    companion object {

        @VisibleForTesting
        const val ANIMATION_DURATION_MILLIS = 1400L

        @VisibleForTesting
        const val DECELERATION_FACTOR = 2f

        @VisibleForTesting
        const val FACE_TOP_WORLD_ROTATION_X = -36f

        @VisibleForTesting
        const val FACE_BOTTOM_WORLD_ROTATION_X = 36f

        @VisibleForTesting
        const val FACE_LEFT_WORLD_ROTATION_Y = -42f

        @VisibleForTesting
        const val FACE_RIGHT_WORLD_ROTATION_Y = 42f

        @VisibleForTesting
        const val FACE_LEFT_TRANSLATION_X = -0.24f

        @VisibleForTesting
        const val FACE_RIGHT_TRANSLATION_X = 0.24f
    }
}

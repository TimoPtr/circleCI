/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.tilt.touch

import android.view.MotionEvent
import com.kolibree.android.jaws.tilt.JawsTiltController
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

/** Touch events driven [JawsTiltController] implementation */
internal interface TouchJawsTiltController : JawsTiltController {

    /**
     * To be called when the hosting jaws view experiences gesture events
     *
     * @param motionEvent [MotionEvent]
     */
    fun onMotionEvent(motionEvent: MotionEvent)
}

/** [TouchJawsTiltController] implementation */
internal class TouchJawsTiltControllerImpl @Inject constructor() : TouchJawsTiltController {

    private val rotationX = AtomicReference(0f)

    private val rotationY = AtomicReference(0f)

    private var originX: Float? = null

    private var originY: Float? = null

    override fun onMotionEvent(motionEvent: MotionEvent) {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> onTouchStart(motionEvent.x, motionEvent.y)
            MotionEvent.ACTION_MOVE -> onTouchMove(motionEvent.x, motionEvent.y)
            MotionEvent.ACTION_OUTSIDE,
            MotionEvent.ACTION_UP -> onTouchStop()
        }
    }

    override fun getJawsRotationX(): Float = safeXAngle(rotationX.get())

    override fun getJawsRotationY(): Float = safeYAngle(rotationY.get())

    override fun getTranslationX() = getJawsRotationY() * TRANSLATION_X_FACTOR

    override fun onPause() {
        // no-op
    }

    override fun onResume() {
        // no-op
    }

    private fun onTouchStart(x: Float, y: Float) {
        originX = x
        originY = y
    }

    private fun onTouchMove(x: Float, y: Float) {
        originX?.let { rotationY.set((x - it) * SMOOTH_FACTOR) }
        originY?.let { rotationX.set((y - it) * SMOOTH_FACTOR) }
    }

    private fun onTouchStop() {
        rotationX.set(0f)
        rotationY.set(0f)
        originX = null
        originY = null
    }

    private fun safeYAngle(angle: Float) = safeAngle(angle, MIN_Y_ROTATION, MAX_Y_ROTATION)

    private fun safeXAngle(angle: Float) = safeAngle(angle, MIN_X_ROTATION, MAX_X_ROTATION)

    private fun safeAngle(angle: Float, min: Float, max: Float) =
        if (angle < 0f) {
            max(min, angle)
        } else {
            min(max, angle)
        }

    companion object {

        private const val SMOOTH_FACTOR = 0.35f

        private const val MAX_Y_ROTATION = 61f

        private const val MIN_Y_ROTATION = -121f

        private const val MIN_X_ROTATION = -30f

        private const val MAX_X_ROTATION = 38f

        private const val TRANSLATION_X_FACTOR = 0.0025f
    }
}

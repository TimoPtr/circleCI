/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.tilt.gyroscopic

import android.view.animation.DecelerateInterpolator
import androidx.annotation.VisibleForTesting
import com.kolibree.android.jaws.tilt.JawsTiltController
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/** Gyroscope based [JawsTiltController] implementation */
internal interface GyroscopicJawsTiltController : JawsTiltController

/** [GyroscopicJawsTiltController] implementation */
internal class GyroscopicJawsTiltControllerImpl @Inject constructor(
    private val gyroscopeSensorInteractor: GyroscopeSensorInteractor,
    private val interpolator: DecelerateInterpolator
) : GyroscopicJawsTiltController {

    override fun getJawsRotationY() =
        smoothRoll(
            safeRoll(
                gyroscopeSensorInteractor.roll()
            )
        ) * ROLL_FACTOR

    override fun getJawsRotationX() = 0f

    override fun getTranslationX() = 0f

    override fun onPause() = gyroscopeSensorInteractor.unregisterListener()

    override fun onResume() = gyroscopeSensorInteractor.registerListener()

    // Keeps the roll value in a [-MAX_ANGLE_RADIANS, MAX_ANGLE_RADIANS] range
    // I might also kill for a change in this method
    @VisibleForTesting
    fun safeRoll(roll: Float) =
        when {
            // Device facing floor, jaws facing right
            roll < -Math.PI / 2f -> min(MAX_ANGLE_RADIANS, roll + Math.PI.toFloat())
            // Device facing sky, jaws facing left
            roll <= 0f -> max(-MAX_ANGLE_RADIANS, roll)
            // Device facing floor, jaws facing left
            roll > Math.PI / 2f -> -min(MAX_ANGLE_RADIANS, Math.PI.toFloat() - roll)
            // Device facing sky, jaws facing right
            else -> min(MAX_ANGLE_RADIANS, roll)
        }

    // Adds a smooth effect when we reach the edges
    @VisibleForTesting
    fun smoothRoll(safeRoll: Float): Float {
        val interpolatedValue = interpolator
            .getInterpolation(abs(safeRoll) / MAX_ANGLE_RADIANS) * MAX_ANGLE_RADIANS

        return if (safeRoll < 0f)
            -interpolatedValue
        else
            interpolatedValue
    }

    companion object {

        @VisibleForTesting
        const val ROLL_FACTOR = 52f

        @VisibleForTesting
        const val MAX_ANGLE_RADIANS = 0.42f
    }
}

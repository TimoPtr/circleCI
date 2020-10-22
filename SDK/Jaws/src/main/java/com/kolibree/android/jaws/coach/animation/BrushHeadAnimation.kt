/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.coach.animation

import com.kolibree.android.jaws.opengl.OptimizedVbo
import kotlin.math.cos
import kotlin.math.sin

/**
 * Plaqless brush head main animation.
 */
internal class BrushHeadAnimation : Animation {

    override fun durationMillis(): Long {
        return ANIMATION_DURATION_MILLIS
    }

    private fun zOffset(): Float {
        return ANIMATION_Z_OFFSET
    }

    override fun animate(vbo: OptimizedVbo, time: Float) {
        val animOffsetZ = sin(Math.toRadians(time * BRUSHING_MAIN_MOVEMENT_CYCLE_DEGREES)) -
            cos(Math.toRadians(time * BRUSHING_CIRCLE_MOVEMENT_CYCLE_DEGREES)) /
            BRUSHING_CIRCLE_MOVEMENT_AMPLITUDE_DIVIDER

        val animOffsetY = sin(Math.toRadians(time * BRUSHING_CIRCLE_MOVEMENT_CYCLE_DEGREES)) /
            BRUSHING_CIRCLE_MOVEMENT_AMPLITUDE_DIVIDER

        vbo.positionVector.offsetY(animOffsetY.toFloat())
        vbo.positionVector.offsetZ(animOffsetZ.toFloat() + zOffset())
    }

    companion object {
        private const val ANIMATION_DURATION_MILLIS = 12000L

        private const val ANIMATION_Z_OFFSET = 1.7f

        private const val BRUSHING_MAIN_MOVEMENT_CYCLE_DEGREES = 720.0

        private const val BRUSHING_CIRCLE_MOVEMENT_CYCLE_DEGREES = 5760.0

        private const val BRUSHING_CIRCLE_MOVEMENT_AMPLITUDE_DIVIDER = 3.5
    }
}

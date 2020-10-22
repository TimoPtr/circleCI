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
 * Brush head animation for UpIncInt and LoIncInt zones.
 */
internal class PlaqlessIncisorInteriorAnimation : TranslationAnimation() {

    override fun durationMillis(): Long {
        return ANIMATION_DURATION_MILLIS
    }

    override fun zOffset(): Float {
        return 0f
    }

    override fun animate(vbo: OptimizedVbo, time: Float) {
        val animOffsetZ = sin(Math.toRadians(time * BRUSHING_MAIN_MOVEMENT_CYCLE_DEGREES)) /
            BRUSHING_MAIN_MOVEMENT_AMPLITUDE_DIVIDER -
            cos(Math.toRadians(time * BRUSHING_CIRCLE_MOVEMENT_CYCLE_DEGREES)) /
            BRUSHING_CIRCLE_MOVEMENT_AMPLITUDE_DIVIDER

        val animOffsetX = sin(Math.toRadians(time * BRUSHING_CIRCLE_MOVEMENT_CYCLE_DEGREES)) /
            BRUSHING_CIRCLE_MOVEMENT_AMPLITUDE_DIVIDER

        vbo.positionVector.offsetX(animOffsetX.toFloat())
        vbo.positionVector.offsetZ(animOffsetZ.toFloat())
    }

    companion object {
        private const val ANIMATION_DURATION_MILLIS = 12000L

        private const val BRUSHING_MAIN_MOVEMENT_CYCLE_DEGREES = 720.0
        private const val BRUSHING_MAIN_MOVEMENT_AMPLITUDE_DIVIDER = 2.5

        private const val BRUSHING_CIRCLE_MOVEMENT_CYCLE_DEGREES = 5760.0
        private const val BRUSHING_CIRCLE_MOVEMENT_AMPLITUDE_DIVIDER = 4f
    }
}

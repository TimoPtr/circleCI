package com.kolibree.android.jaws.coach.animation

import com.kolibree.android.jaws.opengl.OptimizedVbo
import kotlin.math.sin

/**
 * Translation animation.
 */
internal open class TranslationAnimation : Animation {

    override fun durationMillis(): Long {
        return ANIMATION_DURATION_MILLIS
    }

    protected open fun zOffset(): Float {
        return DEFAULT_OFFSET
    }

    override fun animate(vbo: OptimizedVbo, time: Float) {
        val offset = sin(Math.toRadians(time * BRUSHING_MAIN_MOVEMENT_CYCLE_DEGREES))
        vbo.positionVector.offsetZ(offset.toFloat() + zOffset())
    }

    companion object {
        private const val DEFAULT_OFFSET = 1.6f

        private const val ANIMATION_DURATION_MILLIS = 4000L

        private const val BRUSHING_MAIN_MOVEMENT_CYCLE_DEGREES = 720.0
    }
}

package com.kolibree.android.jaws.coach.animation

import android.opengl.GLSurfaceView
import com.kolibree.android.jaws.opengl.OptimizedVbo

/**
 * Animator class that provide an interpolated repeated time reference that is not related to a
 * [GLSurfaceView] frame rate
 */
internal class Animator {

    @Synchronized
    fun animate(vbo: OptimizedVbo, animation: Animation) {
        animation.animate(vbo, computeTime(animation.durationMillis()))
    }

    private fun computeTime(durationMillis: Long): Float {
        return computeTimeOffset(durationMillis) / durationMillis.toFloat()
    }

    private fun computeTimeOffset(durationMillis: Long): Long {
        return System.currentTimeMillis() % durationMillis
    }
}

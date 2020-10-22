package com.kolibree.android.jaws.coach.animation

import com.kolibree.android.jaws.opengl.OptimizedVbo

/**
 * [OptimizedVbo] animation
 */
internal interface Animation {

    /**
     * Get the animation duration in milliseconds
     *
     * @return long duration in millis
     */
    fun durationMillis(): Long

    /**
     * Apply transformations to [OptimizedVbo] coordinates, rotation angles etc...
     *
     * @param vbo [OptimizedVbo]
     * @param time float interpolated time [0f, 1f]
     */
    fun animate(vbo: OptimizedVbo, time: Float)
}

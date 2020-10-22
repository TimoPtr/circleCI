/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.opengl

import android.opengl.GLES20
import androidx.annotation.ColorInt
import androidx.annotation.VisibleForTesting
import com.kolibree.android.jaws.opengl.colors.ColorConverter

/** Wrapper and helper for all OpenGL ES 2.0 calls */
internal interface OpenGles20Wrapper {

    /**
     * Prevent hidden faces from being drawn
     */
    fun enableFaceCulling()

    /**
     * Enable depth testing for hidden-surface elimination
     */
    fun enableDepthTesting()

    /**
     * Make the renderer able to draw translucent objects
     */
    fun enableAlphaLayer()

    /**
     * Adjust the rendering area bounds when the surface has changed
     *
     * @param x viewport x [Int]
     * @param y viewport y [Int]
     * @param width viewport width [Int]
     * @param height viewport height [Int]
     */
    fun adjustViewport(x: Int, y: Int, width: Int, height: Int)

    /**
     * Clear the surface with the current clear color
     */
    fun clearSurface()

    /**
     * Set the clear (background) color of the drawing area
     *
     * @param color [Int] ARGB color
     */
    fun setClearColor(@ColorInt color: Int)

    companion object {

        /**
         * Create a new [OpenGles20Wrapper] instance
         *
         * @return [OpenGles20Wrapper]
         */
        fun createInstance(): OpenGles20Wrapper = OpenGles20WrapperImpl()
    }
}

/** [OpenGles20Wrapper] implementation */
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal class OpenGles20WrapperImpl : OpenGles20Wrapper {

    override fun enableFaceCulling() =
        GLES20.glEnable(GLES20.GL_CULL_FACE)

    override fun enableDepthTesting() =
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

    override fun enableAlphaLayer() {
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
    }

    override fun adjustViewport(x: Int, y: Int, width: Int, height: Int) =
        GLES20.glViewport(x, y, width, height)

    override fun clearSurface() =
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

    @Suppress("MagicNumber")
    override fun setClearColor(color: Int) =
        ColorConverter
            .toEglHdr(color)
            .let {
                GLES20.glClearColor(it[0], it[1], it[2], it[3])
            }
}

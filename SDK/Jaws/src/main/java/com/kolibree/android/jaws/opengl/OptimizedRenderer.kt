/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.opengl

import android.opengl.GLSurfaceView
import androidx.annotation.ColorInt
import androidx.annotation.Keep

/** Optimized [GLSurfaceView] renderer, that can be used in hard environments like view pagers */
@Keep
interface OptimizedRenderer : GLSurfaceView.Renderer {

    /**
     * To be called when the context gets paused
     */
    fun pause()

    /**
     * To be called when the context gets resumed
     */
    fun resume()

    /**
     * Set the background color of the OpenGL scene
     *
     * @param color [ColorInt] [Int]
     */
    fun setEglBackgroundColor(@ColorInt color: Int)
}

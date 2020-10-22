/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.models

import android.graphics.Color
import com.kolibree.android.jaws.opengl.BaseOptimizedVbo
import java.nio.FloatBuffer

/** White brush head vertex buffer object */
internal class DefaultBrushHeadVbo(
    vertexBuffer: FloatBuffer,
    normalBuffer: FloatBuffer
) : BaseOptimizedVbo<Unit>(
    vertexBuffer,
    normalBuffer,
    faceMap
) {
    init {
        setAllMaterialsColor(Color.WHITE)
    }
}

// Don't change this ! Or don't call aurelien then
@Suppress("MagicNumber")
private val faceMap = mapOf(Unit to arrayOf(intArrayOf(0, 1513)))

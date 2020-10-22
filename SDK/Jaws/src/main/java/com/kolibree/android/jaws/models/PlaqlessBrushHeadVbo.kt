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
import androidx.annotation.ColorInt
import com.kolibree.android.jaws.opengl.BaseOptimizedVbo
import java.nio.FloatBuffer

/** Plaqless brush head vertex buffer object */
internal class PlaqlessBrushHeadVbo(
    vertexBuffer: FloatBuffer,
    normalBuffer: FloatBuffer
) : BaseOptimizedVbo<PlaqlessBrushHeadMaterial>(
    vertexBuffer,
    normalBuffer,
    faceMap
) {

    init {
        setMaterialColor(PlaqlessBrushHeadMaterial.BODY, Color.WHITE)
    }

    fun setLedColor(@ColorInt color: Int) =
        setMaterialColor(
            PlaqlessBrushHeadMaterial.LED,
            color
        )
}

// Don't change this ! Or don't call aurelien then
@Suppress("MagicNumber")
private val faceMap = mapOf(
    PlaqlessBrushHeadMaterial.LED to arrayOf(intArrayOf(0, 527)),
    PlaqlessBrushHeadMaterial.BODY to arrayOf(intArrayOf(528, 14696))
)

/** Plaqless brush head materials definition */
internal enum class PlaqlessBrushHeadMaterial {
    BODY,
    LED
}

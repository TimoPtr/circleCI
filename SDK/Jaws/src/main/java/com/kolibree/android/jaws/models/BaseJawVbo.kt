/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.models

import com.kolibree.android.jaws.opengl.BaseOptimizedVbo
import com.kolibree.kml.MouthZone16
import java.nio.FloatBuffer

/** Base [BaseOptimizedVbo] implementation for jaw models */
internal open class BaseJawVbo(
    vertexBuffer: FloatBuffer,
    normalBuffer: FloatBuffer,
    materialToFaceIndexMap: Map<MouthZone16, Array<IntArray>>
) : BaseOptimizedVbo<MouthZone16>(
    vertexBuffer,
    normalBuffer,
    materialToFaceIndexMap
) {

    /**
     * Set mouth zones colors
     *
     * @param zoneColors
     */
    fun setMouthZoneColors(zoneColors: Map<MouthZone16, Int>) = setMaterialColor(zoneColors)
}

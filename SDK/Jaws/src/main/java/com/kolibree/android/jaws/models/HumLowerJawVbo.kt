/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.models

import com.kolibree.kml.MouthZone16
import java.nio.FloatBuffer

/** Lower jaw 3D model vertex buffer object */
internal class HumLowerJawVbo(
    vertexBuffer: FloatBuffer,
    normalBuffer: FloatBuffer
) : BaseJawVbo(
    vertexBuffer,
    normalBuffer,
    faceMap
)

// I kill anyone changing one bit in the following array !
@Suppress("MagicNumber")
private val faceMap = mapOf(
    MouthZone16.LoIncInt to arrayOf(
        intArrayOf(0, 1765)
    ),
    MouthZone16.LoIncExt to arrayOf(
        intArrayOf(1766, 2437)
    ),
    MouthZone16.LoMolRiOcc to arrayOf(
        intArrayOf(2438, 3181)
    ),
    MouthZone16.LoMolLeOcc to arrayOf(
        intArrayOf(3182, 3923)
    ),
    MouthZone16.LoMolRiExt to arrayOf(
        intArrayOf(3924, 4459)
    ),
    MouthZone16.LoMolRiInt to arrayOf(
        intArrayOf(4460, 4915)
    ),
    MouthZone16.LoMolLeExt to arrayOf(
        intArrayOf(4916, 5465)
    ),
    MouthZone16.LoMolLeInt to arrayOf(
        intArrayOf(5466, 5873)
    )
)

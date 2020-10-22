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
internal class HumUpperJawVbo(
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
    MouthZone16.UpMolLeInt to arrayOf(
        intArrayOf(0, 453)
    ),
    MouthZone16.UpMolLeExt to arrayOf(
        intArrayOf(454, 786)
    ),
    MouthZone16.UpIncExt to arrayOf(
        intArrayOf(787, 1417)
    ),
    MouthZone16.UpMolRiInt to arrayOf(
        intArrayOf(1418, 1745)
    ),
    MouthZone16.UpMolRiExt to arrayOf(
        intArrayOf(1746, 2161)
    ),
    MouthZone16.UpMolLeOcc to arrayOf(
        intArrayOf(2162, 3027)
    ),
    MouthZone16.UpMolRiOcc to arrayOf(
        intArrayOf(3028, 3963)
    ),
    MouthZone16.UpIncInt to arrayOf(
        intArrayOf(3964, 5421)
    )
)

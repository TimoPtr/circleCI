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

/** Upper jaw 3D model vertex buffer object */
internal class UpperJawVbo(
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
    MouthZone16.UpMolLeOcc to arrayOf(
        intArrayOf(0, 243),
        intArrayOf(1810, 2045),
        intArrayOf(3580, 3811),
        intArrayOf(5206, 5441),
        intArrayOf(6226, 6463)
    ),
    MouthZone16.UpMolLeExt to arrayOf(
        intArrayOf(244, 376),
        intArrayOf(2046, 2175),
        intArrayOf(3312, 3449),
        intArrayOf(5442, 5573),
        intArrayOf(6464, 6601)
    ),
    MouthZone16.UpMolLeInt to arrayOf(
        intArrayOf(377, 509),
        intArrayOf(2176, 2305),
        intArrayOf(3450, 3579),
        intArrayOf(5574, 5703),
        intArrayOf(6104, 6225)
    ),
    MouthZone16.UpMolRiExt to arrayOf(
        intArrayOf(510, 647),
        intArrayOf(3046, 3178),
        intArrayOf(4448, 4579),
        intArrayOf(6838, 6967),
        intArrayOf(7458, 7595)
    ),
    MouthZone16.UpMolRiInt to arrayOf(
        intArrayOf(648, 777),
        intArrayOf(3179, 3311),
        intArrayOf(4580, 4709),
        intArrayOf(6968, 7219)
    ),
    MouthZone16.UpMolRiOcc to arrayOf(
        intArrayOf(778, 1009),
        intArrayOf(2802, 3045),
        intArrayOf(4212, 4447),
        intArrayOf(6602, 6837),
        intArrayOf(7220, 7457)
    ),
    MouthZone16.UpIncExt to arrayOf(
        intArrayOf(1010, 1171),
        intArrayOf(1410, 1571),
        intArrayOf(2622, 2801),
        intArrayOf(3812, 3943),
        intArrayOf(5026, 5205),
        intArrayOf(5704, 5835)
    ),
    MouthZone16.UpIncInt to arrayOf(
        intArrayOf(1172, 1409),
        intArrayOf(1572, 1809),
        intArrayOf(2306, 2621),
        intArrayOf(3944, 4211),
        intArrayOf(4710, 5025),
        intArrayOf(5836, 6103)
    )
)

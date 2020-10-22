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
internal class LowerJawVbo(
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
    MouthZone16.LoMolRiExt to arrayOf(
        intArrayOf(0, 129),
        intArrayOf(896, 1038),
        intArrayOf(2884, 3026),
        intArrayOf(3906, 4027),
        intArrayOf(6270, 6415)
    ),
    MouthZone16.LoMolRiInt to arrayOf(
        intArrayOf(130, 259),
        intArrayOf(1039, 1178),
        intArrayOf(3027, 3156),
        intArrayOf(3784, 3905),
        intArrayOf(6416, 6537)
    ),
    MouthZone16.LoMolRiOcc to arrayOf(
        intArrayOf(260, 495),
        intArrayOf(1179, 1393),
        intArrayOf(3157, 3383),
        intArrayOf(4028, 4279),
        intArrayOf(6538, 6767)
    ),
    MouthZone16.LoIncExt to arrayOf(
        intArrayOf(496, 619),
        intArrayOf(1394, 1565),
        intArrayOf(1890, 2061),
        intArrayOf(3384, 3507),
        intArrayOf(4778, 4949),
        intArrayOf(5774, 5945)
    ),
    MouthZone16.LoIncInt to arrayOf(
        intArrayOf(620, 895),
        intArrayOf(1566, 1889),
        intArrayOf(2062, 2385),
        intArrayOf(3508, 3783),
        intArrayOf(4950, 5273),
        intArrayOf(5946, 6269)
    ),
    MouthZone16.LoMolLeExt to arrayOf(
        intArrayOf(2386, 2531),
        intArrayOf(4280, 4422),
        intArrayOf(5274, 5416),
        intArrayOf(6890, 7011),
        intArrayOf(7264, 7393)
    ),
    MouthZone16.LoMolLeInt to arrayOf(
        intArrayOf(2532, 2653),
        intArrayOf(4423, 4562),
        intArrayOf(5417, 5546),
        intArrayOf(6768, 6889),
        intArrayOf(7394, 7523)
    ),
    MouthZone16.LoMolLeOcc to arrayOf(
        intArrayOf(2654, 2883),
        intArrayOf(4563, 4777),
        intArrayOf(5547, 5773),
        intArrayOf(7012, 7263),
        intArrayOf(7524, 7759)
    )
)

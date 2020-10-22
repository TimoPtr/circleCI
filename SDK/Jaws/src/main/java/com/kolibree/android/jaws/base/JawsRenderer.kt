/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.base

import androidx.annotation.Keep
import com.kolibree.android.jaws.color.ColorMouthZones
import com.kolibree.android.jaws.opengl.OptimizedRenderer
import com.kolibree.android.jaws.tilt.JawsTiltController
import com.kolibree.kml.MouthZone16

@Keep
interface JawsRenderer : OptimizedRenderer {

    /**
     * Set the colors of each zone
     *
     * @param colors [ColorMouthZones]
     */
    fun colorMouthZones(colors: ColorMouthZones)

    /**
     * Set a [JawsTiltController] that will apply pitch and roll animations to the jaws
     *
     * @param jawsTiltController [JawsTiltController]
     */
    fun setTiltController(jawsTiltController: JawsTiltController)

    /**
     * Get the color of the jaws as they were during the last frame rendering
     *
     * @return [MouthZone16] to [Int] [HashMap]
     */
    fun lastMouthZones(): HashMap<MouthZone16, Int>
}

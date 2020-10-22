/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.coach.animation

import com.kolibree.kml.MouthZone16

/** Brush head animation mapper that maps a zone to the corresponding kind of brush head animation */
internal interface BrushHeadAnimationMapper {

    /**
     * Maps a [MouthZone16] to a brush head [Animation]
     *
     * @param zone16 [MouthZone16]
     * @return [Animation]
     */
    fun getAnimationForZone(zone16: MouthZone16): Animation
}

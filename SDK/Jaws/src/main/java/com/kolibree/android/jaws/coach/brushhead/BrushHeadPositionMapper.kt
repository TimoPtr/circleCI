/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.jaws.coach.brushhead

import com.kolibree.kml.MouthZone16

/**
 * Helper for [MouthZone16] to 3D space position mapping
 *
 * First vector is euclidean position
 * Second one is world referential rotation
 * Third one (not present for all zones) is model center referential rotation
 */
internal interface BrushHeadPositionMapper {

    /**
     * Map a [MouthZone16] to a 3D space position
     *
     * @param mouthZone16 non null [MouthZone16]
     * @return non null float array (2 * 3 floats) that represents a position in the 3D space
     * the first vector is the object coordinates, the second one is the rotation angles in degrees
     */
    fun mapZoneToPositionMatrix(mouthZone16: MouthZone16): Array<FloatArray>
}

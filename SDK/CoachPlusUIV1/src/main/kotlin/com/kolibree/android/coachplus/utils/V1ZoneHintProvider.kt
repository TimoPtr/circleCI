/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.utils

import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import com.kolibree.android.coachplus.R
import com.kolibree.kml.MouthZone16
import javax.inject.Inject

/**
 * Helper for matching a [MouthZone16] with its human readable description
 */
internal class V1ZoneHintProvider @Inject constructor() : ZoneHintProvider {

    override fun provideHintForWrongZone(): Int = R.string.mouth_zone_wrong

    @StringRes
    override fun provideHintForZone(zone: MouthZone16): Int = mapZoneToHintRes(zone)

    @VisibleForTesting
    @Suppress("ComplexMethod")
    @StringRes
    fun mapZoneToHintRes(zone: MouthZone16): Int = when (zone) {
        MouthZone16.LoIncExt -> R.string.mouth_zone_bottom_incisor_exterior
        MouthZone16.LoIncInt -> R.string.mouth_zone_bottom_incisor_interior
        MouthZone16.LoMolLeExt -> R.string.mouth_zone_bottom_molar_left_exterior
        MouthZone16.LoMolLeInt -> R.string.mouth_zone_bottom_molar_left_interior
        MouthZone16.LoMolLeOcc -> R.string.mouth_zone_bottom_molar_left_occlusal
        MouthZone16.LoMolRiOcc -> R.string.mouth_zone_bottom_molar_right_occlusal
        MouthZone16.LoMolRiExt -> R.string.mouth_zone_bottom_molar_right_exterior
        MouthZone16.LoMolRiInt -> R.string.mouth_zone_bottom_molar_right_interior
        MouthZone16.UpIncExt -> R.string.mouth_zone_top_incisor_exterior
        MouthZone16.UpIncInt -> R.string.mouth_zone_top_incisor_interior
        MouthZone16.UpMolLeExt -> R.string.mouth_zone_top_molar_left_exterior
        MouthZone16.UpMolLeInt -> R.string.mouth_zone_top_molar_left_interior
        MouthZone16.UpMolLeOcc -> R.string.mouth_zone_top_molar_left_occlusal
        MouthZone16.UpMolRiExt -> R.string.mouth_zone_top_molar_right_exterior
        MouthZone16.UpMolRiInt -> R.string.mouth_zone_top_molar_right_interior
        MouthZone16.UpMolRiOcc -> R.string.mouth_zone_top_molar_right_occlusal
    }
}

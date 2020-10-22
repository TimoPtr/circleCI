/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.coachplus.utils

import com.kolibree.android.coachplus.R
import com.kolibree.kml.MouthZone16.LoIncExt
import com.kolibree.kml.MouthZone16.LoIncInt
import com.kolibree.kml.MouthZone16.LoMolLeExt
import com.kolibree.kml.MouthZone16.LoMolLeInt
import com.kolibree.kml.MouthZone16.LoMolLeOcc
import com.kolibree.kml.MouthZone16.LoMolRiExt
import com.kolibree.kml.MouthZone16.LoMolRiInt
import com.kolibree.kml.MouthZone16.LoMolRiOcc
import com.kolibree.kml.MouthZone16.UpIncExt
import com.kolibree.kml.MouthZone16.UpIncInt
import com.kolibree.kml.MouthZone16.UpMolLeExt
import com.kolibree.kml.MouthZone16.UpMolLeInt
import com.kolibree.kml.MouthZone16.UpMolLeOcc
import com.kolibree.kml.MouthZone16.UpMolRiExt
import com.kolibree.kml.MouthZone16.UpMolRiInt
import com.kolibree.kml.MouthZone16.UpMolRiOcc
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * [V1ZoneHintProvider] tests
 */
class V1ZoneHintProviderTest {

    private val zoneHintProvider: ZoneHintProvider =
        V1ZoneHintProvider()

    @Test
    fun provideHintForZone_returnGoodResourceForLoIncExt() {
        assertEquals(
            R.string.mouth_zone_bottom_incisor_exterior,
            zoneHintProvider.provideHintForZone(LoIncExt)
        )
    }

    @Test
    fun provideHintForZone_returnGoodResourceForLoIncInt() {
        assertEquals(
            R.string.mouth_zone_bottom_incisor_interior,
            zoneHintProvider.provideHintForZone(LoIncInt)
        )
    }

    @Test
    fun provideHintForZone_returnGoodResourceForLoMolLeExt() {
        assertEquals(
            R.string.mouth_zone_bottom_molar_left_exterior,
            zoneHintProvider.provideHintForZone(LoMolLeExt)
        )
    }

    @Test
    fun provideHintForZone_returnGoodResourceForLoMolLeInt() {
        assertEquals(
            R.string.mouth_zone_bottom_molar_left_interior,
            zoneHintProvider.provideHintForZone(LoMolLeInt)
        )
    }

    @Test
    fun provideHintForZone_returnGoodResourceForLoMolLeOcc() {
        assertEquals(
            R.string.mouth_zone_bottom_molar_left_occlusal,
            zoneHintProvider.provideHintForZone(LoMolLeOcc)
        )
    }

    @Test
    fun provideHintForZone_returnGoodResourceForLoMolRiOcc() {
        assertEquals(
            R.string.mouth_zone_bottom_molar_right_occlusal,
            zoneHintProvider.provideHintForZone(LoMolRiOcc)
        )
    }

    @Test
    fun provideHintForZone_returnGoodResourceForLoMolRiExt() {
        assertEquals(
            R.string.mouth_zone_bottom_molar_right_exterior,
            zoneHintProvider.provideHintForZone(LoMolRiExt)
        )
    }

    @Test
    fun provideHintForZone_returnGoodResourceForLoMolRiInt() {
        assertEquals(
            R.string.mouth_zone_bottom_molar_right_interior,
            zoneHintProvider.provideHintForZone(LoMolRiInt)
        )
    }

    @Test
    fun provideHintForZone_returnGoodResourceForUpIncExt() {
        assertEquals(
            R.string.mouth_zone_top_incisor_exterior,
            zoneHintProvider.provideHintForZone(UpIncExt)
        )
    }

    @Test
    fun provideHintForZone_returnGoodResourceUpIncInt() {
        assertEquals(
            R.string.mouth_zone_top_incisor_interior,
            zoneHintProvider.provideHintForZone(UpIncInt)
        )
    }

    @Test
    fun provideHintForZone_returnGoodResourceUpMolLeExt() {
        assertEquals(
            R.string.mouth_zone_top_molar_left_exterior,
            zoneHintProvider.provideHintForZone(UpMolLeExt)
        )
    }

    @Test
    fun provideHintForZone_returnGoodResourceUpMolLeInt() {
        assertEquals(
            R.string.mouth_zone_top_molar_left_interior,
            zoneHintProvider.provideHintForZone(UpMolLeInt)
        )
    }

    @Test
    fun provideHintForZone_returnGoodResourceUpMolLeOcc() {
        assertEquals(
            R.string.mouth_zone_top_molar_left_occlusal,
            zoneHintProvider.provideHintForZone(UpMolLeOcc)
        )
    }

    @Test
    fun provideHintForZone_returnGoodResourceUpMolRiExt() {
        assertEquals(
            R.string.mouth_zone_top_molar_right_exterior,
            zoneHintProvider.provideHintForZone(UpMolRiExt)
        )
    }

    @Test
    fun provideHintForZone_returnGoodResourceUpMolRiInt() {
        assertEquals(
            R.string.mouth_zone_top_molar_right_interior,
            zoneHintProvider.provideHintForZone(UpMolRiInt)
        )
    }

    @Test
    fun provideHintForZone_returnGoodResourceUpMolRiOcc() {
        assertEquals(
            R.string.mouth_zone_top_molar_right_occlusal,
            zoneHintProvider.provideHintForZone(UpMolRiOcc)
        )
    }
}

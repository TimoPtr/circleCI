/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.models

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.kml.MouthZone16
import com.kolibree.statsoffline.persistence.MouthZoneCheckupConverter
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class MouthZoneCheckupConverterTest : BaseInstrumentationTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val converter = MouthZoneCheckupConverter()

    @Test
    fun fromCheckupMap_converts_map_to_properly_formatted_json() {
        val mouthZone = MouthZone16.LoIncExt
        val value = 57f

        val mouthZone2 = MouthZone16.UpMolLeInt
        val value2 = 99f

        val expectedJson = """{"LoIncExt":57,"UpMolLeInt":99}"""

        val map: MutableMap<MouthZone16, Float> = mutableMapOf(mouthZone to value, mouthZone2 to value2)

        assertEquals(expectedJson, converter.fromCheckupMap(map))
    }

    @Test
    fun toCheckupMap_converts_json_to_map() {
        val mouthZone1 = MouthZone16.LoIncExt
        val value1 = 57f

        val mouthZone2 = MouthZone16.UpMolLeInt
        val value2 = 99f

        val json = """{"LoIncExt":57,"UpMolLeInt":99}"""

        val expectedMap: MutableMap<MouthZone16, Float> = mutableMapOf(mouthZone1 to value1, mouthZone2 to value2)

        assertEquals(expectedMap, converter.toCheckupMap(json))
    }
}

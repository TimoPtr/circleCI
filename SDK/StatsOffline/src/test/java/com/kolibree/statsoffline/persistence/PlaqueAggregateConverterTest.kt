/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.persistence

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.kml.MouthZone16
import com.kolibree.kml.PlaqueStatus
import com.kolibree.statsoffline.persistence.models.StatsPlaqueAggregate
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

class PlaqueAggregateConverterTest : BaseUnitTest() {
    private val converter = PlaqueAggregateConverter()

    @Test
    fun `fromPlaqueAggregateMap returns null if map is null`() {
        assertNull(converter.fromPlaqueAggregateMap(null))
    }

    @Test
    fun `fromPlaqueAggregateMap converts object to expected json`() {
        val statsPlaqueAggregateLoIncExt = StatsPlaqueAggregate(PlaqueStatus.Missed, 77)
        val statsPlaqueAggregateUpIncExt = StatsPlaqueAggregate(PlaqueStatus.NoPlaqueLeft, 42)

        val map = mapOf(
            MouthZone16.LoIncExt to statsPlaqueAggregateLoIncExt,
            MouthZone16.UpIncExt to statsPlaqueAggregateUpIncExt
        )

        val string = converter.fromPlaqueAggregateMap(map)

        val expectedString =
            """{"LoIncExt":{"status":"Missed","cleannessPercent":77},"UpIncExt":{"status":"NoPlaqueLeft","cleannessPercent":42}}"""

        assertEquals(expectedString, string)
    }

    @Test
    fun `toPlaqueAggregateMap returns null from null string`() {
        assertNull(converter.toPlaqueAggregateMap(null))
    }

    @Test
    fun `toPlaqueAggregateMap returns expected map from string`() {
        val sourceString =
            """{"LoIncExt":{"status":"Missed","cleannessPercent":77},"UpIncExt":{"status":"NoPlaqueLeft","cleannessPercent":42}}"""

        val statsPlaqueAggregateLoIncExt = StatsPlaqueAggregate(PlaqueStatus.Missed, 77)
        val statsPlaqueAggregateUpIncExt = StatsPlaqueAggregate(PlaqueStatus.NoPlaqueLeft, 42)

        val expectedMap = mapOf(
            MouthZone16.LoIncExt to statsPlaqueAggregateLoIncExt,
            MouthZone16.UpIncExt to statsPlaqueAggregateUpIncExt
        )

        assertEquals(expectedMap, converter.toPlaqueAggregateMap(sourceString))
    }
}

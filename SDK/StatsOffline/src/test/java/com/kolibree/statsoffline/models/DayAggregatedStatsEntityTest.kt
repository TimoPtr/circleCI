/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.models

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.kml.MouthZone16
import com.kolibree.statsoffline.persistence.models.createBrushingDayStat
import com.kolibree.statsoffline.test.DEFAULT_PROFILE_ID
import com.kolibree.statsoffline.test.createDayAggregatedStatsEntity
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import org.threeten.bp.YearMonth

class DayAggregatedStatsEntityTest : BaseUnitTest() {

    /*
    CONSTRUCTOR
     */
    @Test
    fun `helper method initiates DayAggregatedStatsEntity with correct month`() {
        val expectedYear = 2009
        val expectedMonth = Month.JULY
        val date = LocalDate.of(expectedYear, expectedMonth, 3)

        val dayStats = createBrushingDayStat(DEFAULT_PROFILE_ID, day = date)

        assertEquals(date, dayStats.day)
        assertEquals(YearMonth.of(expectedYear, expectedMonth), dayStats.month)
    }

    @Test
    fun `entity default average map contains all MouthZone16 values`() {
        val dayStats = createDayAggregatedStatsEntity()

        assertEquals(16, dayStats.averageCheckup.size)

        MouthZone16.values().forEach {
            dayStats.averageCheckup.containsKey(it)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor throws IllegalArgumentException if averageSurfaceMap parameter doesn't contain all MouthZone16 values`() {
        createDayAggregatedStatsEntity(averageCheckup = mapOf(MouthZone16.UpIncInt to 7f))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor throws IllegalArgumentException if averageSurfaceMap contains duplicated MouthZone16 values`() {
        createDayAggregatedStatsEntity(averageCheckup = MouthZone16.values().associate { MouthZone16.UpIncExt to 0f })
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor throws IllegalArgumentException if a value in averageSurfaceMap is NaN`() {
        createDayAggregatedStatsEntity(averageCheckup = MouthZone16.values().associate { it to Float.NaN })
    }
}

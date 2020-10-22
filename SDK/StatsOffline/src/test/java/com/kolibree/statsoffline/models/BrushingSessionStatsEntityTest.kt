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
import com.kolibree.android.clock.TrustedClock
import com.kolibree.kml.MouthZone16
import com.kolibree.statsoffline.persistence.models.BrushingSessionStatsEntity
import com.kolibree.statsoffline.test.DEFAULT_PROFILE_ID
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.threeten.bp.temporal.ChronoUnit

class BrushingSessionStatsEntityTest : BaseUnitTest() {

    @Test
    fun `entity default average map contains all MouthZone16 values`() {
        val dayStats = BrushingSessionStatsEntity(
            profileId = DEFAULT_PROFILE_ID,
            creationTime = TrustedClock.getNowLocalDateTime(),
            averageSurface = 0,
            duration = 3
        )

        assertEquals(16, dayStats.averageCheckup.size)

        MouthZone16.values().forEach {
            dayStats.averageCheckup.containsKey(it)
        }
    }

    /*
    Kolibree Day. See DateExtensions for explanation
     */
    @Test
    fun `constructor sets assignedDate to previous day if time is before 4AM`() {
        val currentDate = TrustedClock.getNowLocalDateTime().truncatedTo(ChronoUnit.DAYS)

        val entity = BrushingSessionStatsEntity(
            profileId = DEFAULT_PROFILE_ID,
            creationTime = currentDate,
            averageSurface = 0,
            duration = 3,
            _averageCheckupMap = emptyAverageCheckup()
        )

        val expectedAssignedDate = currentDate.minusDays(1).toLocalDate()
        assertEquals(expectedAssignedDate, entity.assignedDate)
    }

    @Test
    fun `constructor sets assignedDate to current day if time is after 4AM`() {
        val currentDate = TrustedClock.getNowLocalDateTime().truncatedTo(ChronoUnit.DAYS).withHour(4)

        val entity = BrushingSessionStatsEntity(
            profileId = DEFAULT_PROFILE_ID,
            creationTime = currentDate,
            averageSurface = 0,
            duration = 3,
            _averageCheckupMap = emptyAverageCheckup()
        )

        val expectedAssignedDate = currentDate.toLocalDate()
        assertEquals(expectedAssignedDate, entity.assignedDate)
    }

    /*
    Constructor validation
     */

    @Test(expected = IllegalArgumentException::class)
    fun `constructor throws IllegalArgumentException if averageSurfaceMap parameter doesn't contain all MouthZone16 values`() {
        BrushingSessionStatsEntity(
            profileId = DEFAULT_PROFILE_ID,
            creationTime = TrustedClock.getNowLocalDateTime(),
            averageSurface = 0,
            duration = 3,
            _averageCheckupMap = mapOf(MouthZone16.UpIncInt to 7f)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor throws IllegalArgumentException if averageSurfaceMap contains duplicated MouthZone16 values`() {
        BrushingSessionStatsEntity(
            profileId = DEFAULT_PROFILE_ID,
            creationTime = TrustedClock.getNowLocalDateTime(),
            averageSurface = 0,
            duration = 3,
            _averageCheckupMap = MouthZone16.values().associate { MouthZone16.UpIncExt to 0f }
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `constructor throws IllegalArgumentException if a value in averageSurfaceMap is NaN`() {
        BrushingSessionStatsEntity(
            profileId = DEFAULT_PROFILE_ID,
            creationTime = TrustedClock.getNowLocalDateTime(),
            averageSurface = 0,
            duration = 3,
            _averageCheckupMap = MouthZone16.values().associate { it to Float.NaN }
        )
    }
}

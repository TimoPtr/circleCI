/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.persistence.models

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.statsoffline.models.YearWeek
import com.kolibree.statsoffline.models.emptyAverageCheckup
import org.junit.Assert.assertEquals
import org.junit.Test

class WeekAggregatedStatsEntityTest : BaseUnitTest() {
    @Test
    fun `createWeekAggregatedStatsEntity creates empty WeekAggregatedStatsEntity with correct YearWeek`() {
        val date = TrustedClock.getNowLocalDate()

        val expectedYearWeek = YearWeek.from(date)

        val profileId = 434L
        val weekAggregatedStatsEntity = createWeekAggregatedStatsEntity(profileId, date)

        assertEquals(profileId, weekAggregatedStatsEntity.profileId)
        assertEquals(expectedYearWeek, weekAggregatedStatsEntity.week)

        assertEquals(0.0, weekAggregatedStatsEntity.averageSurface, 0.0)
        assertEquals(0.0, weekAggregatedStatsEntity.averageDuration, 0.0)
        assertEquals(emptyAverageCheckup(), weekAggregatedStatsEntity.averageCheckup)
    }
}

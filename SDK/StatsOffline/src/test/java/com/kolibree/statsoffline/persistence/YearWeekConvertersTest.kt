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
import com.kolibree.statsoffline.models.YearWeek
import junit.framework.TestCase.assertEquals
import org.junit.Test

class YearWeekConvertersTest : BaseUnitTest() {
    @Test
    fun `getYearWeekFrom returns expected week 01`() {
        val value = "2019-W01"

        val expectedYearWeek = YearWeek.of(2019, 1)

        assertEquals(expectedYearWeek, YearWeekConverters().getYearWeekFrom(value))
    }

    @Test
    fun `setYearWeekTo returns expected string`() {
        val expectedValue = "2019-W01"

        val yearWeek = YearWeek.of(2019, 1)

        assertEquals(expectedValue, YearWeekConverters().setYearWeekTo(yearWeek))
    }
}

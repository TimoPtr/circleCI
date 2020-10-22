/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.statsoffline.test.createDayWithSessions
import com.nhaarman.mockitokotlin2.mock
import junit.framework.TestCase.assertEquals
import org.junit.Test

class DaysWithSessionsMapExtensionsTest : BaseUnitTest() {

    /*
    toBrushingSessionStatsEntityList
     */
    @Test
    fun `toBrushingSessionStatsEntityList returns all BrushingSessionStatEntity in the list`() {
        val todayWithSessions = createDayWithSessions(
            sessions = listOf(mock(), mock())
        )
        val yesterdayWithSessions = createDayWithSessions(
            sessions = listOf(mock())
        )
        val tomorrowWithSessions = createDayWithSessions(
            sessions = listOf()
        )

        val groupedList = listOf(
            todayWithSessions,
            yesterdayWithSessions,
            tomorrowWithSessions
        ).toBrushingSessionStatsEntityList()

        val expectedList =
            todayWithSessions.sessions + yesterdayWithSessions.sessions + tomorrowWithSessions.sessions

        assertEquals(expectedList, groupedList)
    }
}

/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.gameprogress.data.api.model

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.test.extensions.setFixedDate
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class GameProgressResponseTest : BaseUnitTest() {

    @Test
    fun `toDomainGameProgress returns current time when updatedAt is null`() {
        TrustedClock.setFixedDate()
        val expectedID = "id"
        val expectedProgress = "progress"
        val expectedDate = TrustedClock.getNowZonedDateTimeUTC()
        val response = GameProgressResponse(expectedID, expectedProgress, null)
        val result = response.toDomainGameProgress()

        assertEquals(expectedID, result.gameId)
        assertEquals(expectedProgress, result.progress)
        assertEquals(expectedDate, result.updatedAt)
    }

    @Test
    fun `toDomainGameProgress returns given time when updatedAt is not null`() {
        val expectedID = "id"
        val expectedProgress = "progress"
        val expectedDate = TrustedClock.getNowZonedDateTimeUTC()
        val response = GameProgressResponse(expectedID, expectedProgress, expectedDate)
        val result = response.toDomainGameProgress()

        assertEquals(expectedID, result.gameId)
        assertEquals(expectedProgress, result.progress)
        assertEquals(expectedDate, result.updatedAt)
    }
}

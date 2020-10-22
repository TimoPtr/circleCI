/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.gameprogress.data.persistence.model

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import junit.framework.TestCase.assertEquals
import org.junit.Test

class GameProgressEntityTest : BaseUnitTest() {

    @Test
    fun `toGameProgress returns a valid GameProgress`() {
        val expectedGameId = "42"
        val expectedProgress = "progress"
        val expectedUpdatedAp = TrustedClock.getNowZonedDateTimeUTC()

        val entity = GameProgressEntity(1, expectedGameId, expectedProgress, expectedUpdatedAp)
        val result = entity.toGameProgress()

        assertEquals(expectedGameId, result.gameId)
        assertEquals(expectedProgress, result.progress)
        assertEquals(expectedUpdatedAp, result.updatedAt)
    }
}

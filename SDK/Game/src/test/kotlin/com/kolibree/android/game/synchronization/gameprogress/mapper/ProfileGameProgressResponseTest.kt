/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.synchronization.gameprogress.mapper

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.game.gameprogress.data.api.model.GameProgressResponse
import com.kolibree.android.game.gameprogress.data.api.model.ProfileGameProgressResponse
import com.kolibree.android.game.synchronization.gameprogress.model.ProfileGameProgressSynchronizableItem
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class ProfileGameProgressResponseTest : BaseUnitTest() {

    @Test
    fun `toSynchronizableItem returns ProfileGameProgressSynchronizableItem`() {
        val response = ProfileGameProgressResponse(
            1,
            2,
            listOf(
                GameProgressResponse("3", "good", TrustedClock.getNowZonedDateTimeUTC()),
                GameProgressResponse("4", "excellent", TrustedClock.getNowZonedDateTimeUTC())
            )
        )
        val result = response.toSynchronizableItem() as ProfileGameProgressSynchronizableItem
        assertEquals(response.profileId, result.kolibreeId)
        assertEquals(response.gamesProgress.size, result.gameProgress.size)
        assertEquals(response.gamesProgress[0].toDomainGameProgress(), result.gameProgress[0])
        assertEquals(response.gamesProgress[1].toDomainGameProgress(), result.gameProgress[1])
    }
}

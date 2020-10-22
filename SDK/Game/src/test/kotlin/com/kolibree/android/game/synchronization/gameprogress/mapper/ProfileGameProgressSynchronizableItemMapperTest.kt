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
import com.kolibree.android.game.gameprogress.domain.model.GameProgress
import com.kolibree.android.game.synchronization.gameprogress.model.ProfileGameProgressSynchronizableItem
import com.nhaarman.mockitokotlin2.mock
import java.util.UUID
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class ProfileGameProgressSynchronizableItemMapperTest : BaseUnitTest() {

    @Test
    fun `toPersistentEntities creates GameProgressEntities`() {
        val uuid = mock<UUID>()
        val profileGameProgressItem = ProfileGameProgressSynchronizableItem(
            1,
            listOf(
                GameProgress("2", "small", TrustedClock.getNowZonedDateTimeUTC()),
                GameProgress("3", "big", TrustedClock.getNowZonedDateTimeUTC())
            ),
            uuid = uuid
        )

        val result = profileGameProgressItem.toPersistentEntities()
        assertEquals(profileGameProgressItem.gameProgress.size, result.size)
        result.forEachIndexed { index, gameProgressEntity ->
            assertEquals(1, gameProgressEntity.profileId)
            assertEquals(profileGameProgressItem.gameProgress[index], gameProgressEntity.toGameProgress())
        }
    }
}

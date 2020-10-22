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
import com.kolibree.android.game.gameprogress.data.persistence.model.GameProgressEntity
import com.nhaarman.mockitokotlin2.mock
import java.util.UUID
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

internal class GameProgressEntityMapperTest : BaseUnitTest() {

    @Test
    fun `toSynchronizableItem returns null when is empty`() {
        val list = emptyList<GameProgressEntity>()

        assertNull(list.toSynchronizableItem())
    }

    @Test
    fun `toSynchronizableItem returns ProfileGameProgressSynchronizableItem using profileId and uuid from first item`() {
        val uuid1 = mock<UUID>()
        val uuid2 = mock<UUID>()
        val list = listOf(
            GameProgressEntity(1, "", "", TrustedClock.getNowZonedDateTimeUTC(), uuid1),
            GameProgressEntity(2, "", "", TrustedClock.getNowZonedDateTimeUTC(), uuid2)
        )

        val result = list.toSynchronizableItem()

        assertEquals(1, result!!.profileId)
        assertEquals(uuid1, result.uuid)
    }

    @Test
    fun `toSynchronizableItem returns ProfileGameProgressSynchronizableItem with GameProgress`() {
        val list = listOf(
            GameProgressEntity(1, "1", "bad", TrustedClock.getNowZonedDateTimeUTC()),
            GameProgressEntity(1, "2", "nice", TrustedClock.getNowZonedDateTimeUTC())
        )
        val result = list.toSynchronizableItem()
        assertEquals(list[0].toGameProgress(), result!!.gameProgress[0])
        assertEquals(list[1].toGameProgress(), result.gameProgress[1])
    }
}

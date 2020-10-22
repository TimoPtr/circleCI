/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.synchronization.gameprogress.model

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.game.gameprogress.domain.model.GameProgress
import java.util.UUID
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test
import org.threeten.bp.ZonedDateTime

internal class ProfileGameProgressSynchronizableItemTest : BaseUnitTest() {
    @Test
    fun `constructor sets uuid to null`() {
        assertNull(defaultGameProgress().uuid)
    }

    @Test
    fun `withUpdatedAt returns instance with new updatedAt value`() {
        val item = defaultGameProgress()

        val expectedUpdatedAt = ZonedDateTime.now().minusDays(3)

        assertEquals(
            expectedUpdatedAt,
            item.withUpdatedAt(expectedUpdatedAt).updatedAt
        )
    }

    @Test
    fun `withUuid returns instance with new updatedAt value`() {
        val item = defaultGameProgress()

        val expectedUuid = UUID.randomUUID()

        assertEquals(
            expectedUuid,
            item.withUuid(expectedUuid).uuid
        )
    }

    @Test
    fun `withKolibreeId returns instance with new updatedAt value`() {
        val item = defaultGameProgress()

        val expectedKolibreeId = 878798L

        assertEquals(
            expectedKolibreeId,
            item.withKolibreeId(expectedKolibreeId).kolibreeId
        )
    }

    /*
    updateFromLocalInstance
     */
    @Test
    fun `updateFromLocalInstance returns same instance`() {
        val remoteItem = defaultGameProgress()

        val localInstance = ProfileGameProgressSynchronizableItem(
            kolibreeId = 1,
            gameProgress = listOf(GameProgress("", "", TrustedClock.getNowZonedDateTime())),
            updatedAt = TrustedClock.getNowZonedDateTime()
        )

        assertEquals(remoteItem, remoteItem.updateFromLocalInstance(localInstance))
    }

    /*
    Utils
     */

    private fun defaultGameProgress(): ProfileGameProgressSynchronizableItem {
        return ProfileGameProgressSynchronizableItem(
            kolibreeId = 1,
            gameProgress = listOf(GameProgress("", "", TrustedClock.getNowZonedDateTimeUTC())),
            updatedAt = TrustedClock.getNowZonedDateTime()
        )
    }
}

/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.synchronization.personalchallenge.model

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.rewards.synchronization.personalchallenge.builder.v1PersonalChallenge
import java.util.UUID
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test
import org.threeten.bp.ZonedDateTime

class ProfilePersonalChallengeSynchronizableItemTest : BaseUnitTest() {
    @Test
    fun `constructor sets uuid to null`() {
        assertNull(defaultPersonalChallenge().uuid)
    }

    @Test
    fun `withUpdatedAt returns instance with new updatedAt value`() {
        val item = defaultPersonalChallenge()

        val expectedUpdatedAt = ZonedDateTime.now().minusDays(3)

        assertEquals(
            expectedUpdatedAt,
            item.withUpdatedAt(expectedUpdatedAt).updatedAt
        )
    }

    @Test
    fun `withUuid returns instance with new updatedAt value`() {
        val item = defaultPersonalChallenge()

        val expectedUuid = UUID.randomUUID()

        assertEquals(
            expectedUuid,
            item.withUuid(expectedUuid).uuid
        )
    }

    @Test
    fun `withKolibreeId returns instance with new updatedAt value`() {
        val item = defaultPersonalChallenge()

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
        val remoteItem = defaultPersonalChallenge()

        val localInstance = ProfilePersonalChallengeSynchronizableItem(
            backendId = null,
            kolibreeId = 1,
            challenge = v1PersonalChallenge(),
            updatedAt = TrustedClock.getNowZonedDateTime()
        )

        assertEquals(remoteItem, remoteItem.updateFromLocalInstance(localInstance))
    }

    /*
    Utils
     */

    private fun defaultPersonalChallenge(): ProfilePersonalChallengeSynchronizableItem {
        return ProfilePersonalChallengeSynchronizableItem(
            backendId = 1,
            kolibreeId = 1,
            challenge = v1PersonalChallenge(),
            updatedAt = TrustedClock.getNowZonedDateTime()
        )
    }
}

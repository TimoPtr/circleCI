/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.synchronization.shorttask.mapper

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.ShortTask
import com.kolibree.android.game.shorttask.data.persistence.model.ShortTaskEntity
import com.kolibree.android.test.extensions.setFixedDate
import com.nhaarman.mockitokotlin2.mock
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class ShortTaskEntityMapperTest : BaseUnitTest() {

    @Test
    fun `mapping from entity to synchronizable item works fine`() {
        TrustedClock.setFixedDate()

        val entity =
            ShortTaskEntity(
                10,
                ShortTask.TEST_YOUR_ANGLE,
                TrustedClock.getNowOffsetDateTime().plusDays(1),
                mock()
            )

        val item = entity.toSynchronizableItem()

        assertEquals(entity.profileId, item.profileId)
        assertEquals(entity.shortTask, item.shortTask)
        assertEquals(entity.creationDateTime.toZonedDateTime(), item.createdAt)
        assertEquals(TrustedClock.getNowZonedDateTime(), item.updatedAt)
        assertEquals(entity.uuid, item.uuid)
    }
}

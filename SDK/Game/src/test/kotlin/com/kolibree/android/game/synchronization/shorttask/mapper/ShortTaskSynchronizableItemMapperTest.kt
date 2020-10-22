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
import com.kolibree.android.game.synchronization.shorttask.model.ShortTaskSynchronizableItem
import com.kolibree.android.test.extensions.setFixedDate
import com.nhaarman.mockitokotlin2.mock
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.threeten.bp.temporal.ChronoUnit

internal class ShortTaskSynchronizableItemMapperTest : BaseUnitTest() {

    @Test
    fun `mapping from synchronizable to entity item works fine`() {
        TrustedClock.setFixedDate()

        val item =
            ShortTaskSynchronizableItem(
                ShortTask.TEST_YOUR_ANGLE,
                10,
                TrustedClock.getNowZonedDateTime(),
                TrustedClock.getNowZonedDateTime().plusDays(1),
                mock()
            )

        val entity = item.toPersistentEntities()

        assertEquals(item.profileId, entity.profileId)
        assertEquals(item.shortTask, entity.shortTask)
        assertEquals(
            item.createdAt.toOffsetDateTime().truncatedTo(ChronoUnit.SECONDS),
            entity.creationDateTime
        )
        assertEquals(item.uuid, entity.uuid)
    }
}

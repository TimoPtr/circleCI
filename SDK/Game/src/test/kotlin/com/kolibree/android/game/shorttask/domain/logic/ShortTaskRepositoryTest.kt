/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.shorttask.domain.logic

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.ShortTask
import com.kolibree.android.game.synchronization.shorttask.model.ShortTaskSynchronizableItem
import com.kolibree.android.synchronizator.Synchronizator
import com.kolibree.android.test.extensions.setFixedDate
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

@ExperimentalCoroutinesApi
internal class ShortTaskRepositoryTest : BaseUnitTest() {

    private val synchronizator: Synchronizator = mock()
    private val processor: ShortTaskProcessor = mock()

    @Test
    fun `creating a short task add it to the db and start a sync`() = runBlockingTest {
        TrustedClock.setFixedDate()

        val repo = ShortTaskRepositoryImpl(
            processor,
            synchronizator,
            CoroutineScope(Dispatchers.Unconfined)
        )
        val expectedShortTaskItem = ShortTaskSynchronizableItem(
            ShortTask.TEST_YOUR_ANGLE,
            10,
            TrustedClock.getNowZonedDateTime(),
            TrustedClock.getNowZonedDateTime()
        )

        whenever(synchronizator.create(expectedShortTaskItem)).thenReturn(Single.just(expectedShortTaskItem))

        repo.createShortTask(
            expectedShortTaskItem.profileId,
            expectedShortTaskItem.shortTask
        ).test().assertComplete()

        inOrder(synchronizator, processor) {
            verify(synchronizator).create(expectedShortTaskItem)
            verify(processor).onShortTaskCreated()
        }
    }
}

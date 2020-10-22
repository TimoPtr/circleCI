/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.shorttask.domain.logic

import androidx.annotation.VisibleForTesting
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.commons.ShortTask
import com.kolibree.android.game.synchronization.shorttask.model.ShortTaskSynchronizableItem
import com.kolibree.android.synchronizator.Synchronizator
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@VisibleForApp
interface ShortTaskRepository {
    fun createShortTask(profileId: Long, shortTask: ShortTask): Completable
}

internal class ShortTaskRepositoryImpl @VisibleForTesting constructor(
    private val processor: ShortTaskProcessor,
    private val synchronizator: Synchronizator,
    private val scope: CoroutineScope
) : ShortTaskRepository {

    @Inject
    constructor(processor: ShortTaskProcessor, synchronizator: Synchronizator) : this(
        processor,
        synchronizator,
        GlobalScope
    )

    override fun createShortTask(profileId: Long, shortTask: ShortTask): Completable =
        Single.defer {
            val item = ShortTaskSynchronizableItem(
                shortTask,
                profileId,
                TrustedClock.getNowZonedDateTime(),
                TrustedClock.getNowZonedDateTime()
            )
            synchronizator.create(item)
        }.doAfterSuccess {
            scope.launch { processor.onShortTaskCreated() }
        }.ignoreElement()
}

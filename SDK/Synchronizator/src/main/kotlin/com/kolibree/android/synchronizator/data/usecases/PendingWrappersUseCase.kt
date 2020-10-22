/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.data.usecases

import com.kolibree.android.synchronizator.SynchronizableItemBundle
import com.kolibree.android.synchronizator.models.SynchronizableItemWrapper
import com.kolibree.android.synchronizator.models.UploadStatus.IN_PROGRESS
import com.kolibree.android.synchronizator.models.UploadStatus.PENDING
import javax.inject.Inject

/**
 * UseCase to return [SynchronizableItemWrapper] with operations pending for a given [SynchronizableItemBundle]
 */
internal class PendingWrappersUseCase
@Inject constructor(
    private val readByUploadStatusUseCase: ReadByUploadStatusUseCase,
    private val filterPendingWrapperUseCase: FilterPendingWrapperUseCase
) {
    /**
     * @return [List]<[SynchronizableItemWrapper]> with CreateOrEdit operation pending
     */
    fun getPendingCreate(bundle: SynchronizableItemBundle): List<SynchronizableItemWrapper> {
        return getPending(bundle) { !isDeletedLocally }
    }

    /**
     * @return [List]<[SynchronizableItemWrapper]> with Delete operation pending
     */
    fun getPendingDelete(bundle: SynchronizableItemBundle): List<SynchronizableItemWrapper> {
        return getPending(bundle) { isDeletedLocally }
    }

    private inline fun getPending(
        bundle: SynchronizableItemBundle,
        filter: SynchronizableItemWrapper.() -> Boolean
    ): List<SynchronizableItemWrapper> {
        return bundle.run {
            readByUploadStatusUseCase.readByUploadStatus(bundle, PENDING, IN_PROGRESS)
                .filter(filter)
                .mapNotNull { filterPendingWrapperUseCase.nullUnlessPending(it) }
        }
    }
}

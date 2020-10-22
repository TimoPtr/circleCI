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
import com.kolibree.android.synchronizator.data.SynchronizableTrackingEntityDataStore
import com.kolibree.android.synchronizator.data.database.SynchronizableTrackingEntity
import com.kolibree.android.synchronizator.data.database.updateWrapper
import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.kolibree.android.synchronizator.models.SynchronizableItemWrapper
import com.kolibree.android.synchronizator.models.UploadStatus
import io.reactivex.Single
import javax.inject.Inject

/**
 * UseCase to facilitate updating the [UploadStatus] of a [SynchronizableItem] and gain access
 * to [SynchronizableItemWrapper]
 */
internal class UpdateUploadStatusUseCase
@Inject constructor(
    private val entityDataStore: SynchronizableTrackingEntityDataStore,
    private val wrapperProvider: SynchronizableItemWrapperProvider
) {

    /**
     * Updates or creates an [SynchronizableTrackingEntity] with uploadStatus = [newUploadStatus]
     *
     * If there wasn't an [SynchronizableTrackingEntity] associated to [item], this method creates
     * it
     *
     * @return [Single]<[SynchronizableItemWrapper]>
     */
    fun updateSingle(
        item: SynchronizableItem,
        bundle: SynchronizableItemBundle,
        newUploadStatus: UploadStatus
    ): Single<SynchronizableItemWrapper> {
        return Single.fromCallable { update(item, bundle, newUploadStatus) }
    }

    /**
     * Updates or creates a [SynchronizableTrackingEntity] with uploadStatus = [newUploadStatus]
     *
     * If there wasn't a [SynchronizableTrackingEntity] associated to [item], this method creates
     * it
     *
     * @return [SynchronizableItemWrapper]
     */
    fun update(
        item: SynchronizableItem,
        bundle: SynchronizableItemBundle,
        newUploadStatus: UploadStatus
    ): SynchronizableItemWrapper {
        val itemWrapper = wrapperProvider.provide(item, bundle)

        return itemWrapper.withUploadStatus(newUploadStatus)
            .also { entityDataStore.updateWrapper(it) }
    }

    /**
     * Updates a [SynchronizableItemWrapper] with uploadStatus = [newUploadStatus]
     *
     * @return [SynchronizableItemWrapper]
     */
    fun update(
        wrapper: SynchronizableItemWrapper,
        bundle: SynchronizableItemBundle,
        newUploadStatus: UploadStatus
    ): SynchronizableItemWrapper {
        return bundle.run {
            wrapper.withUploadStatus(newUploadStatus)
                .also { entityDataStore.updateWrapper(it) }
        }
    }
}

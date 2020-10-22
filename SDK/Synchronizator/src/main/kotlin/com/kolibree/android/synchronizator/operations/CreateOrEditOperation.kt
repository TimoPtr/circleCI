/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.operations

import com.kolibree.android.synchronizator.QueueOperationExecutor
import com.kolibree.android.synchronizator.SynchronizableItemBundle
import com.kolibree.android.synchronizator.SynchronizableItemDataStore
import com.kolibree.android.synchronizator.SynchronizationBundles.bundleForSynchronizableItem
import com.kolibree.android.synchronizator.data.database.SynchronizableTrackingEntity
import com.kolibree.android.synchronizator.data.usecases.UpdateUploadStatusUseCase
import com.kolibree.android.synchronizator.insert
import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.kolibree.android.synchronizator.models.SynchronizableItemWrapper
import com.kolibree.android.synchronizator.models.UploadStatus.IN_PROGRESS
import io.reactivex.Single
import javax.inject.Inject

/**
 * [SyncOperation] that will Create or Edit a [SynchronizableItem]
 *
 */
internal class CreateOrEditOperation @Inject constructor(
    private val updateUploadStatusUseCase: UpdateUploadStatusUseCase,
    private val remoteCreateOrEditOperationFactory: RemoteCreateOrEditQueueOperationFactory,
    private val queueOperationExecutor: QueueOperationExecutor
) : SyncOperation {

    /**
     * On subscription, it stores [synchronizableItem] in the local datastore and flags its UploadStatus
     * as [IN_PROGRESS].
     *
     * After success is emitted, it enqueues a RemoteCreateOrEditQueueOperation that will be run at
     * some point in the future
     *
     * @return Single that will emit [synchronizableItem] with UploadStatus [IN_PROGRESS]
     */
    fun run(synchronizableItem: SynchronizableItem): Single<SynchronizableItem> {
        return Single.fromCallable { bundleForSynchronizableItem(synchronizableItem) }
            .flatMap { bundle ->
                internalCreateOrEdit(synchronizableItem, bundle)
                    .flatMap { itemWrapper ->
                        bundleCreateOrEdit(wrapper = itemWrapper, bundle = bundle)
                            .doOnSuccess { enqueueRemoteCreateOrEdit(itemWrapper) }
                    }
            }
    }

    fun run(wrapper: SynchronizableItemWrapper) = run(wrapper.synchronizableItem)

    /**
     * Creates a [SynchronizableTrackingEntity] for [item] and flags it as [IN_PROGRESS]
     *
     * @return [Single]<[SynchronizableItemWrapper]> that will emit the [SynchronizableTrackingEntity]
     * associated to [item]
     */
    private fun internalCreateOrEdit(
        item: SynchronizableItem,
        bundle: SynchronizableItemBundle
    ): Single<SynchronizableItemWrapper> {
        return updateUploadStatusUseCase.updateSingle(
            item = item,
            bundle = bundle,
            newUploadStatus = IN_PROGRESS
        )
    }

    /**
     * Stores in [SynchronizableItemDataStore] a new instance of [wrapper] with updatedAt value
     * set to current time
     *
     * @return [Single] that will update [wrapper] on subscription
     */
    private fun bundleCreateOrEdit(
        wrapper: SynchronizableItemWrapper,
        bundle: SynchronizableItemBundle
    ): Single<SynchronizableItem> {
        return Single.fromCallable {
            bundle.dataStore.insert(wrapper.touchUpdatedAt())
        }
    }

    private fun enqueueRemoteCreateOrEdit(wrapper: SynchronizableItemWrapper) {
        queueOperationExecutor.enqueue(
            remoteCreateOrEditOperationFactory.create(wrapper)
        )
    }
}

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
import com.kolibree.android.synchronizator.models.SynchronizableItemWrapper
import com.kolibree.android.synchronizator.models.UploadStatus
import javax.inject.Inject

internal class ReadByUploadStatusUseCase
@Inject constructor(private val trackingEntityDatastore: SynchronizableTrackingEntityDataStore) {
    /**
     * @return [List]<[SynchronizableItemWrapper]> that can be handled by [bundle] and match
     * [uploadStatus] criteria
     */
    fun readByUploadStatus(
        bundle: SynchronizableItemBundle,
        vararg uploadStatus: UploadStatus
    ): List<SynchronizableItemWrapper> {
        return bundle.run {
            trackingEntityDatastore.readByUploadStatus(key(), *uploadStatus)
                .map { trackingEntity ->
                    val bundleItem = dataStore.getByUuid(trackingEntity.uuid)

                    SynchronizableItemWrapper(
                        synchronizableItem = bundleItem,
                        trackingEntity = trackingEntity
                    )
                }
        }
    }
}

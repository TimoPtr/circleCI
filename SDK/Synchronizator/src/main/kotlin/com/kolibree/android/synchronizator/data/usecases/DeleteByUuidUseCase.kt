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
import java.util.UUID
import javax.inject.Inject

/**
 * UseCase to delete every instance and record related to a [UUID]
 */
internal class DeleteByUuidUseCase
@Inject constructor(
    private val trackingEntityDataStore: SynchronizableTrackingEntityDataStore
) {
    fun delete(uuid: UUID, bundle: SynchronizableItemBundle) {
        bundle.dataStore.delete(uuid)
        trackingEntityDataStore.delete(uuid)
    }
}

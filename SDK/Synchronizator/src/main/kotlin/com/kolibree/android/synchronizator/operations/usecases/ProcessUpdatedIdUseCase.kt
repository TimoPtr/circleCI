/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.operations.usecases

import com.kolibree.android.synchronizator.DataStoreId
import com.kolibree.android.synchronizator.SynchronizableItemBundle
import javax.inject.Inject

/**
 * UseCase to encapsulate the actions needed to process an updatedId in the context of a
 * Synchronize operation
 *
 * https://kolibree.atlassian.net/wiki/spaces/SOF/pages/2755460/Synchronization+support
 */
internal class ProcessUpdatedIdUseCase
@Inject constructor(
    private val conflictResolutionUseCase: ConflictResolutionUseCase,
    private val readByKolibreeIdUseCase: ReadByKolibreeIdUseCase
) {
    /**
     * Fetches updatedId from the backend and processes the result of resolving the conflict, if any,
     * between the local and remote copy
     */
    fun process(kolibreeId: DataStoreId, bundle: SynchronizableItemBundle) {
        bundle.run {
            val remoteSynchronizable = api.get(kolibreeId)
            val localSynchronizable = readByKolibreeIdUseCase.read(kolibreeId, this)

            /*
             ConflictStrategy will determine if we insert, discard or merge the incoming changes
            */
            val conflictResolution =
                conflictStrategy.resolve(localSynchronizable, remoteSynchronizable)

            conflictResolutionUseCase.resolve(conflictResolution, bundle)
        }
    }
}

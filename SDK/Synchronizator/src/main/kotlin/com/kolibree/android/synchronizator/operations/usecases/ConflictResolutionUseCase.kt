/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.operations.usecases

import com.kolibree.android.synchronizator.ConflictResolution
import com.kolibree.android.synchronizator.SynchronizableItemBundle
import com.kolibree.android.synchronizator.data.usecases.UpdateUploadStatusUseCase
import com.kolibree.android.synchronizator.insert
import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.kolibree.android.synchronizator.models.SynchronizableItemWrapper
import com.kolibree.android.synchronizator.models.UploadStatus
import javax.inject.Inject

/**
 * Deals with ConflictResolution when synchronizing and the actions that need to be taken
 */
internal class ConflictResolutionUseCase
@Inject constructor(
    private val updateUploadStatusUseCase: UpdateUploadStatusUseCase
) {
    fun resolve(conflictResolution: ConflictResolution, bundle: SynchronizableItemBundle) {
        bundle.run {
            conflictResolution.synchronizableForDatastore()?.let {
                val wrapper = flagAsSyncCompleted(it)

                dataStore.insert(wrapper)
            }

            conflictResolution.synchronizableForBackend()?.let {
                // https://kolibree.atlassian.net/browse/KLTB002-10256
            }
        }
    }

    private fun SynchronizableItemBundle.flagAsSyncCompleted(item: SynchronizableItem): SynchronizableItemWrapper {
        return updateUploadStatusUseCase.update(item, this, UploadStatus.COMPLETED)
    }
}

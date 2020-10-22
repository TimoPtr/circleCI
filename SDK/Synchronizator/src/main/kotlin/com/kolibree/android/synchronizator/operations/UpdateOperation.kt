/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.operations

import com.kolibree.android.synchronizator.models.SynchronizableItem
import io.reactivex.Single
import javax.inject.Inject

internal class UpdateOperation
@Inject constructor(private val createOrEditOperation: CreateOrEditOperation) : SyncOperation {

    fun run(synchronizableItem: SynchronizableItem): Single<SynchronizableItem> {
        return Single.defer {
            if (synchronizableItem.uuid == null) {
                Single.error(IllegalArgumentException("Update doesn't support null uuid ($synchronizableItem)"))
            } else {
                createOrEditOperation.run(synchronizableItem)
            }
        }
    }
}

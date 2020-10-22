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
import com.kolibree.android.synchronizator.models.SynchronizableItemWrapper
import io.reactivex.Completable
import javax.inject.Inject

internal class DeleteOperation @Inject constructor() : SyncOperation {
    fun run(synchronizableItem: SynchronizableItem): Completable {
        return Completable.complete()
    }

    fun run(wrapper: SynchronizableItemWrapper) = run(wrapper.synchronizableItem)
}

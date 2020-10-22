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
import com.kolibree.android.synchronizator.data.usecases.SynchronizableItemWrapperProvider
import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.kolibree.android.synchronizator.models.SynchronizableItemWrapper
import java.util.UUID
import javax.inject.Inject

/**
 * Makes sure that given a [DataStoreId] and a [SynchronizableItemBundle], if a local
 * [SynchronizableItem] exists, it's matched with a [SynchronizableItemWrapper] and has a [UUID]
 * assigned
 */
internal class ReadByKolibreeIdUseCase
@Inject constructor(private val wrapperProvider: SynchronizableItemWrapperProvider) {
    fun read(kolIbreeId: DataStoreId, bundle: SynchronizableItemBundle): SynchronizableItem? {
        bundle.run {
            val localSynchronizable = dataStore.getByKolibreeId(kolIbreeId)

            return localSynchronizableWithUuid(localSynchronizable, this)
        }
    }

    /**
     * @return [SynchronizableItem]? with non-null uuid if [localSynchronizable] is not null. Null
     * otherwise
     */
    private fun localSynchronizableWithUuid(
        localSynchronizable: SynchronizableItem?,
        bundle: SynchronizableItemBundle
    ): SynchronizableItem? {
        return localSynchronizable?.let {
            wrapperProvider.provide(
                localSynchronizable,
                bundle
            )
        }?.synchronizableItem
    }
}

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
import com.kolibree.android.synchronizator.SynchronizableItemDataStore
import com.kolibree.android.synchronizator.data.SynchronizableTrackingEntityDataStore
import com.kolibree.android.synchronizator.data.database.SynchronizableTrackingEntity
import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.kolibree.android.synchronizator.models.SynchronizableItemWrapper
import javax.inject.Inject

/**
 * Facilitates access to [SynchronizableItemWrapper]
 */
internal class SynchronizableItemWrapperProvider
@Inject constructor(
    private val entityDataStore: SynchronizableTrackingEntityDataStore
) {

    /**
     * Updates or creates a [SynchronizableTrackingEntity] asso
     *
     * If there wasn't an [SynchronizableTrackingEntity] associated to [item], this method creates
     * it and updates [SynchronizableItemDataStore] with the new uuid value
     *
     * @return [SynchronizableItemWrapper]
     */
    fun provide(
        item: SynchronizableItem,
        bundle: SynchronizableItemBundle
    ): SynchronizableItemWrapper {
        return bundle.run {
            val entity = entityDataStore.fromSynchronizableItem(item, key())

            val itemToReturn = maybeUpdateSynchronizableItem(item, entity)

            SynchronizableItemWrapper(
                synchronizableItem = itemToReturn,
                trackingEntity = entity
            )
        }
    }

    private fun SynchronizableItemBundle.maybeUpdateSynchronizableItem(
        item: SynchronizableItem,
        entity: SynchronizableTrackingEntity
    ): SynchronizableItem {
        return if (item.uuid == null) {
            item.withUuid(entity.uuid)
                .run { dataStore.insert(this) }
        } else {
            item
        }
    }
}

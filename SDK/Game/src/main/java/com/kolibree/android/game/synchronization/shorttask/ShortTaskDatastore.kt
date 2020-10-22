/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.synchronization.shorttask

import com.kolibree.android.game.shorttask.data.persistence.ShortTaskDao
import com.kolibree.android.game.synchronization.shorttask.mapper.toPersistentEntities
import com.kolibree.android.game.synchronization.shorttask.mapper.toSynchronizableItem
import com.kolibree.android.game.synchronization.shorttask.model.ShortTaskSynchronizableItem
import com.kolibree.android.synchronizator.DataStoreId
import com.kolibree.android.synchronizator.SynchronizableItemDataStore
import com.kolibree.android.synchronizator.models.SynchronizableItem
import java.util.UUID
import javax.inject.Inject

internal class ShortTaskDatastore @Inject constructor(
    private val dao: ShortTaskDao
) : SynchronizableItemDataStore {

    override fun insert(synchronizable: SynchronizableItem): SynchronizableItem =
        when (synchronizable) {
            is ShortTaskSynchronizableItem -> insertInternal(synchronizable)
            else -> throw IllegalArgumentException("Cannot insert $synchronizable into ShortTaskDao")
        }

    private fun insertInternal(item: ShortTaskSynchronizableItem): SynchronizableItem {
        val entity = item.toPersistentEntities()
        dao.insert(entity)
        return entity.toSynchronizableItem()
    }

    /**
     * Kolibree ID is profile ID in case of short task
     * @see [ShortTaskSynchronizableItem.profileId]
     */
    override fun getByKolibreeId(kolibreeId: DataStoreId): SynchronizableItem? = null

    override fun getByUuid(uuid: UUID): SynchronizableItem = dao.getByUuid(uuid).toSynchronizableItem()

    override fun delete(uuid: UUID) {
        dao.delete(uuid)
    }

    override fun updateVersion(newVersion: Int) {
        // no-op
    }

    override fun canHandle(synchronizable: SynchronizableItem): Boolean =
        synchronizable is ShortTaskSynchronizableItem
}

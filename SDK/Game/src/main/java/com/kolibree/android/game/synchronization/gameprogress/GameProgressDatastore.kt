/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.game.synchronization.gameprogress

import androidx.annotation.VisibleForTesting
import com.kolibree.android.game.gameprogress.data.persistence.GameProgressDao
import com.kolibree.android.game.synchronization.GameSynchronizedVersions
import com.kolibree.android.game.synchronization.gameprogress.mapper.toPersistentEntities
import com.kolibree.android.game.synchronization.gameprogress.mapper.toSynchronizableItem
import com.kolibree.android.game.synchronization.gameprogress.model.ProfileGameProgressSynchronizableItem
import com.kolibree.android.synchronizator.DataStoreId
import com.kolibree.android.synchronizator.SynchronizableItemDataStore
import com.kolibree.android.synchronizator.models.SynchronizableItem
import java.util.UUID
import javax.inject.Inject

internal class GameProgressDatastore @Inject constructor(
    private val dao: GameProgressDao,
    private val gameSynchronizedVersions: GameSynchronizedVersions
) : SynchronizableItemDataStore {

    override fun insert(synchronizable: SynchronizableItem): SynchronizableItem =
        when (synchronizable) {
            is ProfileGameProgressSynchronizableItem -> insertInternal(synchronizable)
            else -> throw IllegalArgumentException("Cannot insert $synchronizable into GameProgressDao")
        }

    @VisibleForTesting
    fun insertInternal(item: ProfileGameProgressSynchronizableItem): SynchronizableItem {
        val entities = item.toPersistentEntities()
        dao.replaceEntities(item.profileId, entities)
        return entities.toSynchronizableItem()
            ?: throw IllegalStateException("Impossible to create synchronizable item from ProfileGameProgress")
    }

    /**
     * Kolibree ID is profile ID in case of gameprogress's
     * @see [ProfileGameProgressSynchronizableItem.profileId]
     */
    override fun getByKolibreeId(kolibreeId: DataStoreId): SynchronizableItem? =
        dao.getGameProgressEntitiesForProfile(kolibreeId).toSynchronizableItem()

    override fun getByUuid(uuid: UUID): SynchronizableItem = dao.getEntitiesByUuid(uuid).toSynchronizableItem()
        ?: throw IllegalStateException("Impossible to create synchronizable item from uuid")

    override fun delete(uuid: UUID) {
        dao.truncateForUuid(uuid)
    }

    override fun updateVersion(newVersion: Int) = gameSynchronizedVersions.setGameProgressVersion(newVersion)

    override fun canHandle(synchronizable: SynchronizableItem): Boolean =
        synchronizable is ProfileGameProgressSynchronizableItem
}

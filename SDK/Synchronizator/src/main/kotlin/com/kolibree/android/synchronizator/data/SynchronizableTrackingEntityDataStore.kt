/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.data

import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.synchronizator.data.database.SynchronizableTrackingEntity
import com.kolibree.android.synchronizator.data.database.SynchronizableTrackingEntityDao
import com.kolibree.android.synchronizator.data.database.SynchronizableTrackingEntityRepository
import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.kolibree.android.synchronizator.models.SynchronizableKey
import java.util.UUID
import javax.inject.Inject

internal class SynchronizableTrackingEntityDataStore
@Inject constructor(
    private val synchronizableDecorationEntityDao: SynchronizableTrackingEntityDao
) : SynchronizableTrackingEntityRepository by synchronizableDecorationEntityDao {

    /**
     * @return [SynchronizableTrackingEntity] associated to [item] or a new, persisted
     * [SynchronizableTrackingEntity] with default [SynchronizableTrackingEntity.uploadStatus]
     * and [SynchronizableTrackingEntity.isDeletedLocally]
     */
    fun fromSynchronizableItem(
        item: SynchronizableItem,
        key: SynchronizableKey
    ): SynchronizableTrackingEntity {
        val itemWithUuid = item.withSanitizeUuid()

        val uuid = itemWithUuid.uuid!!

        return synchronizableDecorationEntityDao.read(key, uuid) ?: persistNewEntity(key, uuid)
    }

    private fun persistNewEntity(
        key: SynchronizableKey,
        uuid: UUID
    ): SynchronizableTrackingEntity {
        return SynchronizableTrackingEntity.from(key, uuid)
            .also { newEntity ->
                synchronizableDecorationEntityDao.insert(newEntity)
            }
    }
}

/**
 * If uuid is null, return an instance with non-null uuid
 *
 * Otherwise, returns same instance
 *
 * @return [SynchronizableItem] same instance if uuid is not null. Otherwise, returns a ew instance
 * with non-null uuid
 */
private fun SynchronizableItem.withSanitizeUuid(): SynchronizableItem {
    return if (uuid == null) {
        val itemWithUuid = withUuid(UUID.randomUUID())

        /*
        Protect ourselves of buggy implementations of SynchronizableItem
         */
        FailEarly.failInConditionMet(
            itemWithUuid.uuid == null,
            "UUID shouldn't be null ($itemWithUuid)"
        )

        itemWithUuid
    } else {
        this
    }
}

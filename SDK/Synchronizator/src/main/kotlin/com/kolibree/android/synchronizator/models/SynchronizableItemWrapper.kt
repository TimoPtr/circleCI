/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.models

import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.synchronizator.DataStoreId
import com.kolibree.android.synchronizator.data.database.SynchronizableItemDecoration
import com.kolibree.android.synchronizator.data.database.SynchronizableTrackingEntity
import java.util.UUID
import org.threeten.bp.ZonedDateTime

/**
 * Wrapper of [SynchronizableItem] that adds [SynchronizableTrackingEntity] data, used internally
 * by Synchronizator module
 *
 * We don't want this class to implement Decorator Pattern because it should never be treated as a
 * [SynchronizableItem]. That way we ensure it never leaks out of the module
 */
internal data class SynchronizableItemWrapper(
    val synchronizableItem: SynchronizableItem,
    val trackingEntity: SynchronizableTrackingEntity
) : SynchronizableItemDecoration by trackingEntity {
    init {
        FailEarly.failInConditionMet(
            synchronizableItem.uuid != null && synchronizableItem.uuid != trackingEntity.uuid,
            "UUIDs should match. $synchronizableItem\n$trackingEntity"
        )
    }

    override val uuid: UUID = trackingEntity.uuid

    fun withUploadStatus(newUploadStatus: UploadStatus): SynchronizableItemWrapper {
        return copy(trackingEntity = trackingEntity.withUploadStatus(newUploadStatus))
    }

    fun withIsDeletedLocally(newIsDeletedLocally: Boolean): SynchronizableItemWrapper {
        return copy(
            trackingEntity = trackingEntity.withIsDeletedLocally(newIsDeletedLocally)
        )
    }

    val kolibreeId: DataStoreId? = synchronizableItem.kolibreeId
    val createdAt: ZonedDateTime = synchronizableItem.createdAt
    val updatedAt: ZonedDateTime = synchronizableItem.updatedAt

    fun withKolibreeId(kolibreeId: DataStoreId): SynchronizableItemWrapper {
        return copy(
            synchronizableItem = synchronizableItem.withKolibreeId(kolibreeId)
        )
    }

    fun touchUpdatedAt(): SynchronizableItemWrapper =
        withUpdatedAt(TrustedClock.getNowZonedDateTimeUTC())

    fun withUpdatedAt(updatedAt: ZonedDateTime): SynchronizableItemWrapper {
        return copy(
            synchronizableItem = synchronizableItem.withUpdatedAt(updatedAt)
        )
    }
}

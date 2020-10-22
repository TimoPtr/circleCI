/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.synchronizator.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kolibree.android.synchronizator.SynchronizableItemBundle
import com.kolibree.android.synchronizator.SynchronizationBundles
import com.kolibree.android.synchronizator.models.SynchronizableItem
import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.kolibree.android.synchronizator.models.UploadStatus
import java.util.UUID

/**
 * Class that allows Synchronizator module to track synchronization-domain information associated to
 * a [SynchronizableItem]
 */
@Entity(
    tableName = "synchronizable_item_tracking"
)
internal data class SynchronizableTrackingEntity constructor(
    /*
     * Stored as a 36 byte String. Not the most efficient as PK, but let's worry about that if we
     * ever hit performance issues, which I doubt
     */
    @PrimaryKey override val uuid: UUID,
    /**
     * Needed to be able to access the [SynchronizableItemBundle] from [SynchronizationBundles]
     * needed to deal with this instance
     */
    override val bundleKey: SynchronizableKey,
    override val uploadStatus: UploadStatus,
    override val isDeletedLocally: Boolean
) : SynchronizableItemDecoration {
    fun withUploadStatus(newUploadStatus: UploadStatus): SynchronizableTrackingEntity =
        copy(uploadStatus = newUploadStatus)

    fun withIsDeletedLocally(newIsDeletedLocally: Boolean): SynchronizableTrackingEntity =
        copy(isDeletedLocally = newIsDeletedLocally)

    companion object {
        fun from(bundleKey: SynchronizableKey, uuid: UUID): SynchronizableTrackingEntity {
            return SynchronizableTrackingEntity(
                uuid = uuid,
                bundleKey = bundleKey,
                uploadStatus = UploadStatus.PENDING,
                isDeletedLocally = false
            )
        }
    }
}

internal interface SynchronizableItemDecoration {
    val uuid: UUID
    val bundleKey: SynchronizableKey
    val uploadStatus: UploadStatus
    val isDeletedLocally: Boolean
}

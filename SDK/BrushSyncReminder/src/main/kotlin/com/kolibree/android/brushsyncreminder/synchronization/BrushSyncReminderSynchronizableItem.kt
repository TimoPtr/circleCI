/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushsyncreminder.synchronization

import com.kolibree.android.brushsyncreminder.data.BrushSyncReminderEntity
import com.kolibree.android.synchronizator.DataStoreId
import com.kolibree.android.synchronizator.models.SynchronizableItem
import java.util.UUID
import org.threeten.bp.ZonedDateTime

internal data class BrushSyncReminderSynchronizableItem(
    val brushReminder: BrushSyncReminderEntity,
    override val uuid: UUID? = brushReminder.uuid,
    override val kolibreeId: Long = brushReminder.profileId,
    override val createdAt: ZonedDateTime = brushReminder.createdAt.toZonedDateTime(),
    override val updatedAt: ZonedDateTime = brushReminder.updatedAt.toZonedDateTime()
) : SynchronizableItem {

    override fun withUpdatedAt(updatedAt: ZonedDateTime): SynchronizableItem {
        return copy(updatedAt = updatedAt)
    }

    override fun withUuid(uuid: UUID): SynchronizableItem {
        return copy(uuid = uuid)
    }

    override fun withKolibreeId(kolibreeId: DataStoreId): SynchronizableItem {
        return copy(kolibreeId = kolibreeId)
    }

    override fun updateFromLocalInstance(localItem: SynchronizableItem): SynchronizableItem {
        return this
    }
}

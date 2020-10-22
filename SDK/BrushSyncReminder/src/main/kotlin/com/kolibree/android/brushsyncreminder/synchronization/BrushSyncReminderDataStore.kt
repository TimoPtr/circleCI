/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushsyncreminder.synchronization

import com.kolibree.android.brushsyncreminder.data.BrushSyncReminderDao
import com.kolibree.android.brushsyncreminder.data.toEntity
import com.kolibree.android.brushsyncreminder.data.toSynchronizableItem
import com.kolibree.android.synchronizator.DataStoreId
import com.kolibree.android.synchronizator.SynchronizableItemDataStore
import com.kolibree.android.synchronizator.models.SynchronizableItem
import java.util.UUID
import javax.inject.Inject

internal class BrushSyncReminderDataStore @Inject constructor(
    private val brushSyncReminderSynchronizedVersions: BrushSyncReminderSynchronizedVersions,
    private val brushReminderDao: BrushSyncReminderDao
) : SynchronizableItemDataStore {

    override fun insert(synchronizable: SynchronizableItem): SynchronizableItem {
        if (synchronizable !is BrushSyncReminderSynchronizableItem) {
            error("$synchronizable not supported!")
        }

        val entity = synchronizable.toEntity()

        brushReminderDao
            .insertOrReplace(entity)
            .blockingAwait()

        return synchronizable
    }

    override fun getByKolibreeId(kolibreeId: DataStoreId): SynchronizableItem? {
        return brushReminderDao
            .findBy(kolibreeId)
            .blockingGet()
            ?.toSynchronizableItem()
    }

    override fun getByUuid(uuid: UUID): SynchronizableItem {
        return brushReminderDao
            .findBy(uuid)
            .blockingGet()
            ?.toSynchronizableItem()
            ?: error("Unable to find item by UUID!")
    }

    override fun delete(uuid: UUID) {
        brushReminderDao
            .deleteBy(uuid)
            .blockingAwait()
    }

    override fun updateVersion(newVersion: Int) {
        brushSyncReminderSynchronizedVersions.setVersion(newVersion)
    }

    override fun canHandle(synchronizable: SynchronizableItem): Boolean {
        return synchronizable is BrushSyncReminderSynchronizableItem
    }
}

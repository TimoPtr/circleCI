/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushsyncreminder.data

import com.kolibree.android.brushsyncreminder.synchronization.BrushSyncReminderSynchronizableItem

internal fun BrushSyncReminderEntity.toSynchronizableItem(): BrushSyncReminderSynchronizableItem {
    return BrushSyncReminderSynchronizableItem(
        brushReminder = this
    )
}

internal fun BrushSyncReminderSynchronizableItem.toEntity(): BrushSyncReminderEntity {
    return brushReminder.copy(
        uuid = uuid,
        createdAtTimestamp = createdAt.toEpochSecond(),
        createdAtZoneOffset = createdAt.offset,
        updatedAtTimestamp = updatedAt.toEpochSecond(),
        updatedAtZoneOffset = updatedAt.offset
    )
}

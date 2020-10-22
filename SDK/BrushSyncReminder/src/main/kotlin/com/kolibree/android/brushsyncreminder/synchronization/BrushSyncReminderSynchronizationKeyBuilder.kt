/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.brushsyncreminder.synchronization

import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.kolibree.android.synchronizator.models.SynchronizeAccountKeyBuilder
import javax.inject.Inject

internal class BrushSyncReminderSynchronizationKeyBuilder @Inject constructor(
    private val brushSyncReminderSynchronizedVersions: BrushSyncReminderSynchronizedVersions
) : SynchronizeAccountKeyBuilder(SynchronizableKey.BRUSH_SYNC_REMINDER) {

    override fun version(): Int = brushSyncReminderSynchronizedVersions.getVersion()
}

/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.hum.reminder.synchronization

import com.kolibree.android.brushsyncreminder.synchronization.BrushSyncReminderConflictResolutionStrategy
import com.kolibree.android.brushsyncreminder.synchronization.BrushSyncReminderDataStore
import com.kolibree.android.brushsyncreminder.synchronization.BrushSyncReminderSynchronizableApi
import com.kolibree.android.brushsyncreminder.synchronization.BrushSyncReminderSynchronizationKeyBuilder
import com.kolibree.android.synchronizator.Bundle
import com.kolibree.android.synchronizator.SynchronizableItemBundle
import com.kolibree.android.synchronizator.models.BundleCreator
import javax.inject.Inject

internal class BrushSyncReminderBundleCreator @Inject constructor(
    private val api: BrushSyncReminderSynchronizableApi,
    private val dataStore: BrushSyncReminderDataStore,
    private val synchronizeAccountKeyBuilderSync: BrushSyncReminderSynchronizationKeyBuilder
) : BundleCreator {

    override fun create(): Bundle = SynchronizableItemBundle(
        api = api,
        dataStore = dataStore,
        conflictStrategy = BrushSyncReminderConflictResolutionStrategy,
        synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilderSync
    )
}

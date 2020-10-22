/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.brushhead.sync

import com.kolibree.android.synchronizator.SynchronizableReadOnlyBundle
import com.kolibree.android.synchronizator.models.BundleCreator
import javax.inject.Inject

/**
 * BundleCreator for brush head status
 *
 * This Bundle is different than others. On login, we might be asked to sync for new data, but user
 * hasn't paired any Toothbrush. So it'll be a useless sync.
 *
 * @see BrushHeadStatusSynchronizableApi javadoc.
 */
internal class BrushHeadStatusSynchronizableCreator @Inject constructor(
    private val api: BrushHeadStatusSynchronizableApi,
    private val dataStore: BrushHeadStatusSynchronizableDataStore,
    private val synchronizationKeyBuilder: BrushHeadStatusSynchronizationKeyBuilder
) : BundleCreator {

    override fun create(): SynchronizableReadOnlyBundle = SynchronizableReadOnlyBundle(
        api = api,
        dataStore = dataStore,
        synchronizeAccountKeyBuilder = synchronizationKeyBuilder
    )
}

/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.charts.synchronization.inoff

import com.kolibree.android.synchronizator.Bundle
import com.kolibree.android.synchronizator.SynchronizableReadOnlyBundle
import com.kolibree.android.synchronizator.models.BundleCreator
import javax.inject.Inject

internal class InOffBrushingsCountBundleCreator @Inject constructor(
    private val api: InOffBrushingsCountSynchronizableReadOnlyApi,
    private val datastore: InOffBrushingsSynchronizableReadOnlyDatastore,
    private val synchronizeAccountKeyBuilder: InOffBurshingsCountSynchronizationKeyBuilder
) : BundleCreator {

    override fun create(): Bundle = SynchronizableReadOnlyBundle(
        api = api,
        dataStore = datastore,
        synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder
    )
}

/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.charts.synchronization.inoff

import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.kolibree.android.synchronizator.models.SynchronizeAccountKeyBuilder
import com.kolibree.charts.synchronization.StatsSynchronizedVersions
import javax.inject.Inject

internal class InOffBurshingsCountSynchronizationKeyBuilder @Inject constructor(
    private val versionsPersistence: StatsSynchronizedVersions
) : SynchronizeAccountKeyBuilder(SynchronizableKey.IN_OFF_BRUSHINGS_COUNT) {

    override fun version(): Int = versionsPersistence.inOffBrushingsCountVersion()
}

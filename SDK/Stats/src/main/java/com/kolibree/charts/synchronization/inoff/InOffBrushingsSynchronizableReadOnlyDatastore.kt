/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.charts.synchronization.inoff

import com.kolibree.android.synchronizator.SynchronizableReadOnlyDataStore
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly
import com.kolibree.charts.inoff.data.persistence.InOffBrushingsCountDao
import com.kolibree.charts.inoff.data.persistence.model.InOffBrushingsCountEntity
import com.kolibree.charts.synchronization.StatsSynchronizedVersions
import javax.inject.Inject
import timber.log.Timber

internal class InOffBrushingsSynchronizableReadOnlyDatastore @Inject constructor(
    private val inOffBrushingsCountDao: InOffBrushingsCountDao,
    private val versionsPersistence: StatsSynchronizedVersions
) : SynchronizableReadOnlyDataStore {

    override fun replace(synchronizable: SynchronizableReadOnly) {
        (synchronizable as? InOffBrushingsCountEntity)?.let {
            inOffBrushingsCountDao.insertOrReplace(it)
        } ?: Timber.e("Expected InOffBrushingsCountEntity, was $synchronizable")
    }

    override fun updateVersion(newVersion: Int) =
        versionsPersistence.setInOffBrushingsCountVersion(newVersion)
}

/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.brushhead.sync

import com.kolibree.android.app.ui.brushhead.repo.BrushHeadRepository
import com.kolibree.android.app.ui.brushhead.sync.model.BrushHeadInformationSet
import com.kolibree.android.synchronizator.SynchronizableReadOnlyDataStore
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly
import io.reactivex.Completable
import javax.inject.Inject
import timber.log.Timber

internal class BrushHeadStatusSynchronizableDataStore @Inject constructor(
    private val dao: BrushHeadRepository,
    private val versions: BrushHeadStatusSynchronizedVersions
) : SynchronizableReadOnlyDataStore {
    override fun replace(synchronizable: SynchronizableReadOnly) {
        (synchronizable as? BrushHeadInformationSet)?.let {
            val replaceDateCompletables = synchronizable.map { value ->
                dao.writeBrushHeadInfo(value)
            }

            Completable.mergeDelayError(replaceDateCompletables).blockingAwait()
        } ?: Timber.e("Expected BrushHeadReplacedDates, was %s", synchronizable)
    }

    override fun updateVersion(newVersion: Int) = versions.setBrushHeadUsageVersion(newVersion)
}

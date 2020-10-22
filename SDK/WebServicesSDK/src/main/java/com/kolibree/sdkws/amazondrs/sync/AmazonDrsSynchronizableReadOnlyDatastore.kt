/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.amazondrs.sync

import com.kolibree.android.synchronizator.SynchronizableReadOnlyDataStore
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly
import javax.inject.Inject

internal class AmazonDrsSynchronizableReadOnlyDatastore @Inject constructor(
    private val versionPersistence: AmazonDrsSynchronizedVersions
) : SynchronizableReadOnlyDataStore {

    override fun replace(synchronizable: SynchronizableReadOnly) {
        // TODO replace this by a real implementation https://kolibree.atlassian.net/browse/KLTB002-10813
        // It should do the the same as AccountSynchronizableReadOnlyDatastore
    }

    override fun updateVersion(newVersion: Int) = versionPersistence.setDrsVersion(newVersion)
}

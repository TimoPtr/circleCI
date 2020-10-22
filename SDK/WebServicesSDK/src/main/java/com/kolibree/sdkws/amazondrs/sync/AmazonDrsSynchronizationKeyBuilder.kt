/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.amazondrs.sync

import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.kolibree.android.synchronizator.models.SynchronizeAccountKeyBuilder
import javax.inject.Inject

internal class AmazonDrsSynchronizationKeyBuilder @Inject constructor(
    private val versionPersistence: AmazonDrsSynchronizedVersions
) : SynchronizeAccountKeyBuilder(SynchronizableKey.AMAZON_DRS_STATUS) {

    override fun version(): Int = versionPersistence.getDrsVersion()
}

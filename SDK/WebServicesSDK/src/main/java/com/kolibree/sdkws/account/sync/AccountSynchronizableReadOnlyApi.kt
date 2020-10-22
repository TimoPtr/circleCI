/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.account.sync

import androidx.annotation.VisibleForTesting
import com.kolibree.android.synchronizator.SynchronizableReadOnlyApi
import com.kolibree.android.synchronizator.models.SynchronizableReadOnly
import com.kolibree.sdkws.core.SynchronizationScheduler
import javax.inject.Inject

@VisibleForTesting
internal val item = object : SynchronizableReadOnly {}

internal class AccountSynchronizableReadOnlyApi @Inject constructor(
    private val synchronizationScheduler: SynchronizationScheduler
) : SynchronizableReadOnlyApi {

    override fun get(id: Long): SynchronizableReadOnly {
        // TODO replace this by a real implementation https://kolibree.atlassian.net/browse/KLTB002-10813
        synchronizationScheduler.syncNow()

        return item
    }
}

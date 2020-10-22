/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.account.sync

import com.kolibree.android.synchronizator.Bundle
import com.kolibree.android.synchronizator.SynchronizableReadOnlyBundle
import com.kolibree.android.synchronizator.models.BundleCreator
import javax.inject.Inject

internal class AccountBundleCreator @Inject constructor(
    private val api: AccountSynchronizableReadOnlyApi,
    private val datastore: AccountSynchronizableReadOnlyDatastore,
    private val synchronizeAccountKeyBuilder: AccountSynchronizationKeyBuilder
) : BundleCreator {

    override fun create(): Bundle = SynchronizableReadOnlyBundle(
        api = api,
        dataStore = datastore,
        synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder
    )
}

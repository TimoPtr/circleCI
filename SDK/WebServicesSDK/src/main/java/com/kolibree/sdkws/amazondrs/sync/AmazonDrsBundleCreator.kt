/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.amazondrs.sync

import com.kolibree.android.synchronizator.Bundle
import com.kolibree.android.synchronizator.SynchronizableReadOnlyBundle
import com.kolibree.android.synchronizator.models.BundleCreator
import com.kolibree.sdkws.account.sync.AccountSynchronizableReadOnlyApi
import javax.inject.Inject

/**
 * The AmazonDrsBundle under the hood trigger an update of the account,
 * it shares the api with AccountSynchronization and the datastore should do the same as the AccountDatastore,
 * we do this because this amazon drs key is updated when there is an update of the linking status and
 * on the backend side this flag is added to the account as the foreign key.
 */
internal class AmazonDrsBundleCreator @Inject constructor(
    private val api: AccountSynchronizableReadOnlyApi,
    private val datastore: AmazonDrsSynchronizableReadOnlyDatastore,
    private val synchronizeAccountKeyBuilder: AmazonDrsSynchronizationKeyBuilder
) : BundleCreator {

    override fun create(): Bundle = SynchronizableReadOnlyBundle(
        api = api,
        dataStore = datastore,
        synchronizeAccountKeyBuilder = synchronizeAccountKeyBuilder
    )
}

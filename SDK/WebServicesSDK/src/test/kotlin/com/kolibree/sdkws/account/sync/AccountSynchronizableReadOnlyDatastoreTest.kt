/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.account.sync

import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

internal class AccountSynchronizableReadOnlyDatastoreTest : BaseUnitTest() {

    @Test
    fun `updateVersion update versionPersistence to this new version`() {
        val versionPersistence = mock<AccountSynchronizedVersions>()
        val datastore = AccountSynchronizableReadOnlyDatastore(versionPersistence)

        datastore.updateVersion(10)

        verify(versionPersistence).setAccountVersion(10)
    }
}

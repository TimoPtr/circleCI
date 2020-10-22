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
import com.kolibree.sdkws.core.SynchronizationScheduler
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class AccountSynchronizableReadOnlyApiTest : BaseUnitTest() {

    @Test
    fun `get returns empty item and schedule sync`() {
        val scheduler = mock<SynchronizationScheduler>()
        val api = AccountSynchronizableReadOnlyApi(scheduler)

        assertEquals(item, api.get(1))
        verify(scheduler).syncNow()
    }
}

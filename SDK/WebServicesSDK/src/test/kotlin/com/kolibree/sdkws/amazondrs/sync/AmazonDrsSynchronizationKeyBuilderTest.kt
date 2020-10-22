/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.sdkws.amazondrs.sync

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class AmazonDrsSynchronizationKeyBuilderTest : BaseUnitTest() {

    @Test
    fun `versions returns version from persistence`() {
        val versionPersistence = mock<AmazonDrsSynchronizedVersions>()
        val keyBuilder = AmazonDrsSynchronizationKeyBuilder(versionPersistence)

        whenever(versionPersistence.getDrsVersion()).thenReturn(10)

        assertEquals(10, keyBuilder.version())
    }

    @Test
    fun `key returns AMAZON_DRS_STATUS`() {
        val versionPersistence = mock<AmazonDrsSynchronizedVersions>()
        val keyBuilder = AmazonDrsSynchronizationKeyBuilder(versionPersistence)

        assertEquals(SynchronizableKey.AMAZON_DRS_STATUS, keyBuilder.key)
    }
}

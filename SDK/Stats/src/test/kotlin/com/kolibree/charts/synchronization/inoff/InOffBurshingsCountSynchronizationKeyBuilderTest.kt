/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.charts.synchronization.inoff

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.kolibree.charts.synchronization.StatsSynchronizedVersions
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import org.junit.Test

internal class InOffBurshingsCountSynchronizationKeyBuilderTest : BaseUnitTest() {

    private val statsSynchronizedVersions: StatsSynchronizedVersions = mock()

    private lateinit var keyBuilderTest: InOffBurshingsCountSynchronizationKeyBuilder

    override fun setup() {
        super.setup()

        keyBuilderTest = InOffBurshingsCountSynchronizationKeyBuilder(statsSynchronizedVersions)
    }

    @Test
    fun `build returns SynchronizeAccountKey with value from statsSynchronizedVersions`() {
        val expectedValue = 543
        whenever(statsSynchronizedVersions.inOffBrushingsCountVersion()).thenReturn(expectedValue)

        val synchronizeKey = keyBuilderTest.build()

        assertEquals(SynchronizableKey.IN_OFF_BRUSHINGS_COUNT, synchronizeKey.key)
        assertEquals(expectedValue, synchronizeKey.version)
    }
}

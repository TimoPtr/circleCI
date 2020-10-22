/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.brushhead.sync

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.synchronizator.models.SynchronizableKey
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import org.junit.Test

class BrushHeadStatusSynchronizationKeyBuilderTest : BaseUnitTest() {
    private val synchronizedVersions: BrushHeadStatusSynchronizedVersions = mock()

    private val keyBuilder = BrushHeadStatusSynchronizationKeyBuilder(synchronizedVersions)

    @Test
    fun `key is BRUSH_HEAD_USAGE`() {
        assertEquals(SynchronizableKey.BRUSH_HEAD_STATUS, keyBuilder.key)
    }

    @Test
    fun `version reads version from synchronizedVersions`() {
        val expectedVersion = 654
        whenever(synchronizedVersions.brushHeadStatusVersion()).thenReturn(expectedVersion)

        assertEquals(expectedVersion, keyBuilder.version())
    }
}

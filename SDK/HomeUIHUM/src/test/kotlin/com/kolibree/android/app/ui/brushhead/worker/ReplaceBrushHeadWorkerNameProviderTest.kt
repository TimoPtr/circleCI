/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.toothbrushsettings.worker

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.test.MacGenerator
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotSame
import org.junit.Test

class ReplaceBrushHeadWorkerNameProviderTest : BaseUnitTest() {
    private val provider = ReplaceBrushHeadWorkerNameProvider()

    @Test
    fun `same input always returns same output`() {
        val mac = MacGenerator.generate()

        repeat(5) {
            assertEquals(provider.provide(mac), provider.provide(mac))
        }
    }

    @Test
    fun `different input returns different output`() {
        val mac1 = MacGenerator.generate()

        // attempt to prevent tests failing due to duplicate generated macs
        val mac2 = MacGenerator.generate().let { if (it == mac1) MacGenerator.generate() else it }

        assertNotSame(provider.provide(mac1), provider.provide(mac2))
    }
}

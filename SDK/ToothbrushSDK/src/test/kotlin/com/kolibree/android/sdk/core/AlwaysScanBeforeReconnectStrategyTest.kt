/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class AlwaysScanBeforeReconnectStrategyTest : BaseUnitTest() {
    private val scanBeforeReconnectStrategy = AlwaysScanBeforeReconnectStrategy

    @Test
    fun `shouldScanBeforeReconnect returns true for all models`() {
        ToothbrushModel.values()
            .forEach { model ->
                val connection = KLTBConnectionBuilder.createAndroidLess()
                    .withModel(model)
                    .build()

                assertTrue(scanBeforeReconnectStrategy.shouldScanBeforeReconnect(connection))
            }
    }
}

class NeverScanBeforeReconnectStrategyTest : BaseUnitTest() {
    private val scanBeforeReconnectStrategy = NeverScanBeforeReconnectStrategy

    @Test
    fun `shouldScanBeforeReconnect returns false for all models`() {
        ToothbrushModel.values()
            .forEach { model ->
                val connection = KLTBConnectionBuilder.createAndroidLess()
                    .withModel(model)
                    .build()

                assertFalse(scanBeforeReconnectStrategy.shouldScanBeforeReconnect(connection))
            }
    }
}

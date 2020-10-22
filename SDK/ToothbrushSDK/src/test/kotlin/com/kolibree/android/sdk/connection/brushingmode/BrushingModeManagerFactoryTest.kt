/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.brushingmode

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.state.ConnectionState
import com.kolibree.android.sdk.core.driver.ble.AraDriver
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertTrue
import org.junit.Test

/** [BrushingModeManagerFactory] tests */
class BrushingModeManagerFactoryTest : BaseUnitTest() {

    // We just need a mock that implements KLTBDriver and BleDriver
    private val driver = mock<AraDriver>()
    private val state = mock<ConnectionState>()

    @Test
    fun `createBrushingModeManager returns a NoBrushingModeManagerImpl for ARA`() =
        assertTrue(createBrushingModeManager(ToothbrushModel.ARA) is NoBrushingModeManagerImpl)

    @Test
    fun `createBrushingModeManager returns a NoBrushingModeManagerImpl for E1`() =
        assertTrue(createBrushingModeManager(ToothbrushModel.CONNECT_E1) is NoBrushingModeManagerImpl)

    @Test
    fun `createBrushingModeManager returns a NoBrushingModeManagerImpl for M1`() =
        assertTrue(createBrushingModeManager(ToothbrushModel.CONNECT_M1) is NoBrushingModeManagerImpl)

    @Test
    fun `createBrushingModeManager returns a NoBrushingModeManagerImpl for PLAQLESS`() =
        assertTrue(createBrushingModeManager(ToothbrushModel.PLAQLESS) is NoBrushingModeManagerImpl)

    @Test
    fun `createBrushingModeManager returns a BrushingModeManagerImpl for E2`() =
        assertTrue(createBrushingModeManager(ToothbrushModel.CONNECT_E2) is BrushingModeManagerImpl)

    @Test
    fun `createBrushingModeManager returns a BrushingModeManagerImpl for B1`() =
        assertTrue(createBrushingModeManager(ToothbrushModel.CONNECT_B1) is BrushingModeManagerImpl)

    /*
    Utils
     */

    private fun createBrushingModeManager(toothbrushModel: ToothbrushModel) =
        BrushingModeManagerFactory.createBrushingModeManager(
            toothbrushModel = toothbrushModel,
            driver = driver,
            connectionState = state
        )
}

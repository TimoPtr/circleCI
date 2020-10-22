/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.driver

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.core.driver.ble.AraDriver
import com.kolibree.android.sdk.core.driver.ble.CB1Driver
import com.kolibree.android.sdk.core.driver.ble.CE1Driver
import com.kolibree.android.sdk.core.driver.ble.CE2Driver
import com.kolibree.android.sdk.core.driver.ble.CM1Driver
import com.kolibree.android.sdk.core.driver.ble.GlintDriver
import com.kolibree.android.sdk.core.driver.ble.PlaqlessDriver
import com.kolibree.android.test.BaseInstrumentationTest
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KLTBDriverFactoryTest : BaseInstrumentationTest() {

    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val driverListener: KLTBDriverListener = mock()

    private val driverFactory = KLTBDriverFactory()

    /*
    createDriver
     */

    @Test
    fun createDriver_returnsAraDriver_whenModelIsARA() {
        assertTrue(createDriver(ToothbrushModel.ARA) is AraDriver)
    }

    @Test
    fun createDriver_returnsCE1Driver_whenModelIsCONNECT_E1() {
        assertTrue(createDriver(ToothbrushModel.CONNECT_E1) is CE1Driver)
    }

    @Test
    fun createDriver_returnsCE2Driver_whenModelIsCONNECT_E2() {
        assertTrue(createDriver(ToothbrushModel.CONNECT_E2) is CE2Driver)
    }

    @Test
    fun createDriver_returnsCM1Driver_whenModelIsCONNECT_M1() {
        assertTrue(createDriver(ToothbrushModel.CONNECT_M1) is CM1Driver)
    }

    @Test
    fun createDriver_returnsCB1Driver_whenModelIsCONNECT_B1() {
        assertTrue(createDriver(ToothbrushModel.CONNECT_B1) is CB1Driver)
    }

    @Test
    fun createDriver_returnsPlaqlessDriver_whenModelIsPLAQLESS() {
        assertTrue(createDriver(ToothbrushModel.PLAQLESS) is PlaqlessDriver)
    }

    @Test // https://kolibree.atlassian.net/browse/KLTB002-10822
    fun createDriver_returnsCE2Driver_whenModelIsHILINK() {
        assertTrue(createDriver(ToothbrushModel.HILINK) is CE2Driver)
    }

    @Test
    fun createDriver_returnsCE2Driver_whenModelIsHUM_ELECTRIC() {
        assertTrue(createDriver(ToothbrushModel.HUM_ELECTRIC) is CE2Driver)
    }

    @Test
    fun createDriver_returnsCB1Driver_whenModelIsHUM_BATTERY() {
        assertTrue(createDriver(ToothbrushModel.HUM_BATTERY) is CB1Driver)
    }

    @Test
    fun createDriver_returnsGlintDriver_whenModelIsGLINT() {
        assertTrue(createDriver(ToothbrushModel.GLINT) is GlintDriver)
    }

    /*
    Utils
     */

    private fun createDriver(toothbrushModel: ToothbrushModel) =
        driverFactory.create(context(), "mac", toothbrushModel, driverListener)
}

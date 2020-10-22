/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.driver.ble

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.core.driver.KLTBDriverListener
import com.kolibree.android.sdk.core.driver.ble.nordic.KLNordicBleManager
import com.kolibree.android.sdk.error.CommandNotSupportedException
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.TestScheduler
import junit.framework.TestCase.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CE1DriverTest : BaseUnitTest() {

    private val driverListener: KLTBDriverListener = mock()

    private val bleManager: KLNordicBleManager = mock()

    private val notificationCaster = CharacteristicNotificationStreamer()

    private lateinit var bleDriver: TestCE1Driver

    @Before
    @Throws(Exception::class)
    override fun setup() {
        super.setup()

        bleDriver =
            TestCE1Driver(
                bleManager,
                driverListener,
                Schedulers.io(),
                "mac",
                notificationCaster,
                TestScheduler()
            )
    }

    @Test
    fun `toothbrushModel returns CONNECT_E1`() {
        assertEquals(ToothbrushModel.CONNECT_E1, bleDriver.testGetToothbrushModel())
    }

    @Test
    fun `supportsReadingBootloader returns false`() {
        assertFalse(bleDriver.testSupportsReadingBootloader())
    }

    /*
    disableMultiUserMode
    */
    @Test
    fun `disableMultiUserMode calls setDeviceParameters with expected payload`() {
        bleDriver.disableMultiUserMode()

        verify(bleManager).setDeviceParameter(ParameterSet.disableMultiUserModePayload())
    }

    /*
    overpressureStateFlowable
     */

    @Test
    fun `overpressureStateFlowable emits CommandNotSupportedException`() {
        bleDriver.overpressureStateFlowable()
            .test()
            .assertError(CommandNotSupportedException::class.java)
    }
}

private class TestCE1Driver(
    bleManager: KLNordicBleManager,
    listener: KLTBDriverListener,
    bluetoothScheduler: Scheduler,
    mac: String,
    streamer: CharacteristicNotificationStreamer,
    notifyListenerScheduler: Scheduler
) : CE1Driver(bleManager, listener, bluetoothScheduler, mac, streamer, notifyListenerScheduler) {

    fun testGetToothbrushModel() = toothbrushModel()
    fun testSupportsReadingBootloader() = supportsReadingBootloader()
}

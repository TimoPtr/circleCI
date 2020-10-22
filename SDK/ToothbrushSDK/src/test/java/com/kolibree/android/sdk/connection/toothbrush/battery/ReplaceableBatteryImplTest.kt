/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.toothbrush.battery

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.toothbrush.battery.DiscreteBatteryLevel.BATTERY_FEW_WEEKS
import com.kolibree.android.sdk.connection.toothbrush.battery.DiscreteBatteryLevel.BATTERY_UNKNOWN
import com.kolibree.android.sdk.connection.toothbrush.battery.ReplaceableBatteryImpl.Companion.DISCRETE_BATTERY_LEVEL_OFFSET
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.kolibree.android.sdk.error.CommandNotSupportedException
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** [ReplaceableBatteryImpl] tests */
class ReplaceableBatteryImplTest : BaseUnitTest() {

    private val driver = mock<BleDriver>()

    private lateinit var battery: ReplaceableBatteryImpl

    override fun setup() {
        super.setup()

        battery = ReplaceableBatteryImpl(driver, false)
    }

    @Test
    fun `Value of DISCRETE_BATTERY_LEVEL_OFFSET is 9`() {
        assertEquals(9, DISCRETE_BATTERY_LEVEL_OFFSET)
    }

    @Test
    fun `isCharging always emits false`() {
        battery.isCharging.test().assertValue(false)
    }

    @Test
    fun `discreteBatteryLevel returns BATTERY_UNKNOWN if toothbrush is in bootloader`() {
        val batteryLevel: Byte = 0x02 // BATTERY FEW WEEKS
        whenever(driver.isRunningBootloader).thenReturn(true)

        battery.discreteBatteryLevel.test().assertValue(BATTERY_UNKNOWN)
    }

    @Test
    fun `discreteBatteryLevel parses the battery parameter and emits the discrete level`() {
        val batteryLevel: Byte = 0x02 // BATTERY FEW WEEKS
        whenever(driver.getDeviceParameter(any()))
            .thenReturn(PayloadReader(byteArrayOf(0, 1, 1, 1, 0, 1, 1, 1, 1, batteryLevel)))
        battery.discreteBatteryLevel.test().assertValue(BATTERY_FEW_WEEKS)
    }

    @Test
    fun `batteryLevel emits CommandNotSupportedException`() {
        whenever(driver.getDeviceParameter(any()))
            .thenThrow(CommandNotSupportedException(""))
        battery.batteryLevel.test().assertError(CommandNotSupportedException::class.java)
    }

    @Test
    fun `usesDiscreteLevels is always true`() {
        assertTrue(battery.usesDiscreteLevels)
    }

    /*
    isChargingFlowable
     */

    @Test
    fun `isChargingFlowable emits CommandNotSupportedException`() {
        whenever(driver.deviceParametersCharacteristicChangedStream())
            .thenReturn(Flowable.fromArray(byteArrayOf()))

        battery
            .isChargingFlowable()
            .test()
            .assertNotComplete()
            .assertNoValues()
            .assertError(CommandNotSupportedException::class.java)
    }
}

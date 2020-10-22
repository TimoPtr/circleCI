/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.toothbrush.battery

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.toothbrush.battery.RechargeableBatteryImpl.Companion.BATTERY_LEVEL_OFFSET
import com.kolibree.android.sdk.connection.toothbrush.battery.RechargeableBatteryImpl.Companion.CHARGING_FLAG_OFFSET
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.kolibree.android.sdk.error.CommandNotSupportedException
import com.kolibree.android.test.extensions.assertLastValue
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/** [RechargeableBatteryImpl] tests */
class RechargeableBatteryImplTest : BaseUnitTest() {

    private val driver = mock<BleDriver>()

    private lateinit var battery: RechargeableBatteryImpl

    override fun setup() {
        super.setup()

        battery = RechargeableBatteryImpl(driver, false)
    }

    @Test
    fun `Value of CHARGING_FLAG_OFFSET is 4`() {
        assertEquals(4, CHARGING_FLAG_OFFSET)
    }

    @Test
    fun `Value of BATTERY_LEVEL_OFFSET is 1`() {
        assertEquals(1, BATTERY_LEVEL_OFFSET)
    }

    @Test
    fun `isCharging emits false if toothbrush is in bootloader`() {
        whenever(driver.isRunningBootloader).thenReturn(true)

        battery.isCharging.test().assertValue(false)
    }

    @Test
    fun `isCharging emits true when the charging flag is set`() {
        whenever(driver.getDeviceParameter(any()))
            .thenReturn(PayloadReader(chargingPayload))
        battery.isCharging.test().assertValue(true)
    }

    @Test
    fun `isCharging emits false when the charging flag is not set`() {
        whenever(driver.getDeviceParameter(any()))
            .thenReturn(PayloadReader(notChargingPayload))
        battery.isCharging.test().assertValue(false)
    }

    @Test
    fun `batteryLevel returns 0 if toothbrush is in bootloader`() {
        whenever(driver.isRunningBootloader).thenReturn(true)

        battery.batteryLevel.test().assertValue(0)
    }

    @Test
    fun `batteryLevel parses the battery parameter and emits the level`() {
        val batteryLevel: Byte = 0x0A // 10 % battery
        whenever(driver.getDeviceParameter(any()))
            .thenReturn(PayloadReader(byteArrayOf(0, batteryLevel, 1, 1, 1, 0)))
        battery.batteryLevel.test().assertValue(batteryLevel.toInt())
    }

    @Test
    fun `discreteBatteryLevel emits CommandNotSupportedException`() {
        whenever(driver.getDeviceParameter(any()))
            .thenThrow(CommandNotSupportedException(""))
        battery.discreteBatteryLevel.test().assertError(CommandNotSupportedException::class.java)
    }

    @Test
    fun `usesDiscreteLevels is always false`() {
        assertFalse(battery.usesDiscreteLevels)
    }

    /*
    isChargingFlowable
     */

    @Test
    fun `isChargingFlowable parses and emits charging state from PARAMETERS CHAR`() {
        val parametersChar = PublishSubject.create<ByteArray>()
        whenever(driver.getDeviceParameter(any()))
            .thenReturn(PayloadReader(chargingPayload))
        whenever(driver.deviceParametersCharacteristicChangedStream())
            .thenReturn(parametersChar.toFlowable(BackpressureStrategy.BUFFER))

        val chargingObserver = battery.isChargingFlowable().test()
        chargingObserver.assertLastValue(true) // Current charging state

        parametersChar.onNext(notChargingPayload)
        chargingObserver.assertLastValue(false)

        parametersChar.onNext(chargingPayload)
        chargingObserver.assertLastValue(true)

        chargingObserver
            .assertNotComplete()
            .assertNoErrors()
    }

    @Test
    fun `isChargingFlowable starts with the current charging state (in this case, charging)`() {
        whenever(driver.getDeviceParameter(any()))
            .thenReturn(PayloadReader(chargingPayload))
        whenever(driver.deviceParametersCharacteristicChangedStream())
            .thenReturn(Flowable.never())

        battery.isChargingFlowable()
            .test()
            .assertNoErrors()
            .assertNotComplete()
            .assertValueCount(1)
            .assertValue(true)
    }

    companion object {

        private val chargingPayload = byteArrayOf(1, 1, 1, 1, 1)

        private val notChargingPayload = byteArrayOf(1, 1, 1, 1, 0)
    }
}

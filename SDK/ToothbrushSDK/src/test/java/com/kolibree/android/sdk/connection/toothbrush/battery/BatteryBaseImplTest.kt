/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.toothbrush.battery

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.connection.toothbrush.battery.BatteryBaseImpl.Companion.BATTERY_PAYLOAD
import com.kolibree.android.sdk.connection.toothbrush.battery.BatteryBaseImpl.Companion.BATTERY_PAYLOAD_COMPAT
import com.kolibree.android.sdk.connection.toothbrush.battery.DiscreteBatteryLevel.BATTERY_6_MONTHS
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.android.sdk.core.driver.ble.BleDriver
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Single
import org.junit.Assert.assertArrayEquals
import org.junit.Test

/** [BatteryBaseImpl] tests */
class BatteryBaseImplTest : BaseUnitTest() {

    private val driver = mock<BleDriver>()

    private lateinit var battery: BatteryBaseImpl

    @Test
    fun `The value of BATTERY_PAYLOAD_COMPAT is an array of 0x22, 0x01`() {
        assertArrayEquals(byteArrayOf(0x22, 0x01), BATTERY_PAYLOAD_COMPAT)
    }

    @Test
    fun `The value of BATTERY_PAYLOAD is an array of 0x22`() {
        assertArrayEquals(byteArrayOf(0x22), BATTERY_PAYLOAD)
    }

    @Test
    fun `batteryPayload() returns BATTERY_PAYLOAD when usesCompatPayload is false`() {
        battery = BatteryBaseImplStub(driver, false)
        assertArrayEquals(BATTERY_PAYLOAD, battery.batteryPayload())
    }

    @Test
    fun `batteryPayload() returns BATTERY_PAYLOAD_COMPAT when usesCompatPayload is true`() {
        battery = BatteryBaseImplStub(driver, true)
        assertArrayEquals(BATTERY_PAYLOAD_COMPAT, battery.batteryPayload())
    }

    @Test
    fun `batteryPayloadReader() calls good parameter and emits driver's PayloadReader`() {
        battery = BatteryBaseImplStub(driver)
        val payload = PayloadReader(byteArrayOf())
        whenever(driver.getDeviceParameter(BATTERY_PAYLOAD)).thenReturn(payload)
        battery.batteryPayloadReader().test().assertValue(payload)
    }

    @Test
    fun `batteryPayloadReader() emits driver's Exceptions`() {
        battery = BatteryBaseImplStub(driver)
        whenever(driver.getDeviceParameter(BATTERY_PAYLOAD)).thenThrow(Exception())
        battery.batteryPayloadReader().test().assertError(Exception::class.java)
    }
}

internal class BatteryBaseImplStub(
    driver: BleDriver,
    usesCompatPayload: Boolean = false
) : BatteryBaseImpl(driver, usesCompatPayload) {

    override val isCharging: Single<Boolean>
        get() = Single.just(false)
    override val batteryLevel: Single<Int>
        get() = Single.just(100)
    override val discreteBatteryLevel: Single<DiscreteBatteryLevel>
        get() = Single.just(BATTERY_6_MONTHS)
    override val usesDiscreteLevels: Boolean
        get() = false

    override fun isChargingFlowable(): Flowable<Boolean> =
        Flowable.never()
}

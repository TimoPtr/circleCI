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
import com.kolibree.android.sdk.core.driver.ble.gatt.GattCharacteristic
import io.reactivex.Flowable
import junit.framework.TestCase.assertEquals
import org.junit.Test

class CharacteristicNotificationCasterTest : BaseUnitTest() {
    private val caster = CharacteristicNotificationStreamer()

    /*
    characteristicStream
     */

    @Test
    fun `characteristicStream returns flowable that only emits GattCharacteristic data`() {
        val characteristic = GattCharacteristic.PLAQLESS_IMU_CHAR
        val observer = caster.characteristicStream(characteristic).test().assertEmpty()

        val expectedPayload = byteArrayOf(1, 2, 3)
        GattCharacteristic.values()
            .filterNot { it == characteristic }
            .forEach {
                caster.onNewData(BleNotificationData(it.UUID, expectedPayload))
            }

        observer.assertEmpty()

        caster.onNewData(BleNotificationData(characteristic.UUID, expectedPayload))

        observer.assertValue(expectedPayload)
    }

    @Test
    fun `characteristicStream multiple invocations return different instances`() {
        val characteristic = GattCharacteristic.PLAQLESS_IMU_CHAR
        val flowables = mutableListOf<Flowable<ByteArray>>().apply {
            add(caster.characteristicStream(characteristic))
            add(caster.characteristicStream(characteristic))
            add(caster.characteristicStream(characteristic))
            add(caster.characteristicStream(characteristic))
        }

        assertEquals(4, flowables.distinct().size)
    }

    @Test
    fun `characteristicStream invokes onSubscribe block`() {
        val characteristic = GattCharacteristic.PLAQLESS_IMU_CHAR

        var onSubscribeCounter = 0
        caster.characteristicStream(
            gattCharacteristic = characteristic,
            onSubscribeBlock = { onSubscribeCounter++ }
        )
            .test()

        assertEquals(1, onSubscribeCounter)
    }

    @Test
    fun `characteristicStream invokes onCancel block on dispose`() {
        val characteristic = GattCharacteristic.PLAQLESS_IMU_CHAR

        var onCancelCounter = 0
        val disposable = caster.characteristicStream(
            gattCharacteristic = characteristic,
            onCancelBlock = { onCancelCounter++ }
        )
            .test()

        assertEquals(0, onCancelCounter)

        disposable.dispose()

        assertEquals(1, onCancelCounter)
    }
}

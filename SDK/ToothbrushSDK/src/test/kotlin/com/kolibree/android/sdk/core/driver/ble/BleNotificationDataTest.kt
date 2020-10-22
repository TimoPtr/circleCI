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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class BleNotificationDataTest : BaseUnitTest() {
    /*
    Equals
     */

    @Test
    fun `equals is true if characteristicUuid is the same and data is empty array in both cases`() {
        val thizz = BleNotificationData(GattCharacteristic.PLAQLESS_IMU_CHAR.UUID, byteArrayOf())
        val other = BleNotificationData(GattCharacteristic.PLAQLESS_IMU_CHAR.UUID, byteArrayOf())

        assertEquals(thizz, other)
    }

    @Test
    fun `equals is true if characteristicUuid is the same and data is array with same content in both cases`() {
        val thizz =
            BleNotificationData(GattCharacteristic.PLAQLESS_IMU_CHAR.UUID, byteArrayOf(0x00))
        val other =
            BleNotificationData(GattCharacteristic.PLAQLESS_IMU_CHAR.UUID, byteArrayOf(0x00))

        assertEquals(thizz, other)
    }

    @Test
    fun `equals is false if characteristicUuid is different and data is empty array in both cases`() {
        val thizz = BleNotificationData(GattCharacteristic.PLAQLESS_IMU_CHAR.UUID, byteArrayOf())
        val other =
            BleNotificationData(GattCharacteristic.PLAQLESS_CONTROL_CHAR.UUID, byteArrayOf())

        assertNotEquals(thizz, other)
    }

    @Test
    fun `equals is false if characteristicUuid is different and data is array with same content in both cases`() {
        val thizz =
            BleNotificationData(GattCharacteristic.PLAQLESS_IMU_CHAR.UUID, byteArrayOf(0x00))
        val other =
            BleNotificationData(GattCharacteristic.PLAQLESS_CONTROL_CHAR.UUID, byteArrayOf(0x00))

        assertNotEquals(thizz, other)
    }

    @Test
    fun `equals is false if characteristicUuid is the same and data is array with different content in both cases`() {
        val thizz =
            BleNotificationData(GattCharacteristic.PLAQLESS_IMU_CHAR.UUID, byteArrayOf(0x00))
        val other =
            BleNotificationData(GattCharacteristic.PLAQLESS_IMU_CHAR.UUID, byteArrayOf(0x01))

        assertNotEquals(thizz, other)
    }
}

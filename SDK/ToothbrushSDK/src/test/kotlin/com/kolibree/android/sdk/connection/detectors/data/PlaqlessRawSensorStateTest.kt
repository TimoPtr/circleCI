/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.detectors.data

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.sdk.core.binary.PayloadWriter
import com.kolibree.android.test.utils.randomPositiveInt
import com.kolibree.android.test.utils.randomSigned16
import junit.framework.TestCase
import org.junit.Test

class PlaqlessRawSensorStateTest : BaseUnitTest() {

    @Test
    fun `parse BLE_GATT_PLAQLESS_DETECTOR_CHAR notification data`() {
        val relativeTimestamp = randomPositiveInt()
        val accelX: Short = randomSigned16()
        val accelY: Short = randomSigned16()
        val accelZ: Short = randomSigned16()
        val gyroX: Short = randomSigned16()
        val gyroY: Short = randomSigned16()
        val gyroZ: Short = randomSigned16()

        val payloadWriter = PayloadWriter(18)
        payloadWriter.writeInt32(relativeTimestamp)
        payloadWriter.writeInt16(accelX)
        payloadWriter.writeInt16(accelY)
        payloadWriter.writeInt16(accelZ)
        payloadWriter.writeInt16(gyroX)
        payloadWriter.writeInt16(gyroY)
        payloadWriter.writeInt16(gyroZ)

        val notificationData = payloadWriter.bytes

        val expectedPlaqlessData =
            PlaqlessRawSensorState(
                relativeTimestampMillis = relativeTimestamp.toLong(),
                accelX = accelX,
                accelY = accelY,
                accelZ = accelZ,
                gyroX = gyroX,
                gyroY = gyroY,
                gyroZ = gyroZ
            )

        TestCase.assertEquals(expectedPlaqlessData, notificationData.toPlaqlessRawSensorData())
    }
}

internal fun plaqlessRawSensorNotificationData(): ByteArray {
    val relativeTimestamp = randomPositiveInt()
    val accelX: Short = randomSigned16()
    val accelY: Short = randomSigned16()
    val accelZ: Short = randomSigned16()
    val gyroX: Short = randomSigned16()
    val gyroY: Short = randomSigned16()
    val gyroZ: Short = randomSigned16()

    val payloadWriter = PayloadWriter(18)
    payloadWriter.writeInt32(relativeTimestamp)
    payloadWriter.writeInt16(accelX)
    payloadWriter.writeInt16(accelY)
    payloadWriter.writeInt16(accelZ)
    payloadWriter.writeInt16(gyroX)
    payloadWriter.writeInt16(gyroY)
    payloadWriter.writeInt16(gyroZ)

    return payloadWriter.bytes
}

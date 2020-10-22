/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.detectors.data

import androidx.annotation.Keep
import com.kolibree.android.sdk.core.binary.PayloadReader
import com.kolibree.kml.RawData

/**
 * Represents a packet of Byte emitted by BLE_GATT_PLAQLESS_IMU_CHAR
 *
 * It's a [RawSensorState] without magnetometer information
 *
 * See https://kolibree.atlassian.net/wiki/spaces/SOF/pages/2755082/BLE+protocol+for+Plaqless+data+in+online+mode
 */
@Keep
data class PlaqlessRawSensorState(
    val relativeTimestampMillis: Long, // see documentation. Not an epoch timestamp)
    val accelX: Short,
    val accelY: Short,
    val accelZ: Short,
    val gyroX: Short,
    val gyroY: Short,
    val gyroZ: Short
) {
    fun convertToKmlRawData(): RawData =
        RawData(
            relativeTimestampMillis,
            gyroX.toFloat(),
            gyroY.toFloat(),
            gyroZ.toFloat(),
            accelX.toFloat(),
            accelY.toFloat(),
            accelZ.toFloat()
        )
}

internal fun ByteArray.toPlaqlessRawSensorData(): PlaqlessRawSensorState {
    val payloadReader = PayloadReader(this)

    val relativeTimestamp = payloadReader.readUnsignedInt32()
    val accelX = payloadReader.readInt16()
    val accelY = payloadReader.readInt16()
    val accelZ = payloadReader.readInt16()
    val gyroX = payloadReader.readInt16()
    val gyroY = payloadReader.readInt16()
    val gyroZ = payloadReader.readInt16()

    return PlaqlessRawSensorState(
        relativeTimestampMillis = relativeTimestamp,
        accelX = accelX,
        accelY = accelY,
        accelZ = accelZ,
        gyroX = gyroX,
        gyroY = gyroY,
        gyroZ = gyroZ
    )
}

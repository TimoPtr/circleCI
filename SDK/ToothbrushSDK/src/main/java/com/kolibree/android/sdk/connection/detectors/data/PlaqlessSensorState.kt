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
import com.kolibree.android.sdk.plaqless.PlaqlessError
import com.kolibree.android.sdk.plaqless.fromErrorCode
import com.kolibree.kml.Percentage
import com.kolibree.kml.PlaqlessData

/**
 * Represents a packet of Byte emitted by BLE_GATT_PLAQLESS_DETECTOR_CHAR
 *
 * See https://kolibree.atlassian.net/wiki/spaces/SOF/pages/2755082/BLE+protocol+for+Plaqless+data+in+online+mode
 */
@Keep
data class PlaqlessSensorState(
    val relativeTimestampMillis: Long, // see documentation. Not an epoch timestamp
    val alphaAngle: Int,
    val betaAngle: Int,
    val gammaAngle: Int,
    val brushedSurface1: Short,
    val brushedSurface2: Short,
    val cleanPercentage1: Short,
    val cleanPercentage2: Short,
    val mostProbableZone1: Short,
    val mostProbableZone2: Short,
    val mostProbableZone3: Short,
    val plaqlessError: PlaqlessError
) {
    fun convertToKmlPlaqlessData(): PlaqlessData =
        PlaqlessData(
            relativeTimestampMillis,
            alphaAngle,
            betaAngle,
            gammaAngle,
            brushedSurface1,
            Percentage(cleanPercentage1.toInt()),
            brushedSurface2,
            Percentage(cleanPercentage2.toInt()),
            mostProbableZone1,
            mostProbableZone2,
            mostProbableZone3,
            plaqlessError.code.toShort()
        )
}

internal fun ByteArray.toPlaqlessSensorData(): PlaqlessSensorState {
    val payloadReader = PayloadReader(this)

    val relativeTimestamp = payloadReader.readUnsignedInt32()
    val alphaAngle = payloadReader.readUnsignedInt16()
    val betaAngle = payloadReader.readUnsignedInt16()
    val gammaAngle = payloadReader.readUnsignedInt16()
    val brushedSurface1 = payloadReader.readUnsignedInt8()
    val cleanPercentage1 = payloadReader.readUnsignedInt8()
    val brushedSurface2 = payloadReader.readUnsignedInt8()
    val cleanPercentage2 = payloadReader.readUnsignedInt8()
    val mostProbableZone1 = payloadReader.readUnsignedInt8()
    val mostProbableZone2 = payloadReader.readUnsignedInt8()
    val mostProbableZone3 = payloadReader.readUnsignedInt8()

    val plaqlessError = fromErrorCode(payloadReader.readInt8())

    return PlaqlessSensorState(
        relativeTimestampMillis = relativeTimestamp,
        alphaAngle = alphaAngle,
        betaAngle = betaAngle,
        gammaAngle = gammaAngle,
        brushedSurface1 = brushedSurface1,
        brushedSurface2 = brushedSurface2,
        cleanPercentage1 = cleanPercentage1,
        cleanPercentage2 = cleanPercentage2,
        mostProbableZone1 = mostProbableZone1,
        mostProbableZone2 = mostProbableZone2,
        mostProbableZone3 = mostProbableZone3,
        plaqlessError = plaqlessError
    )
}

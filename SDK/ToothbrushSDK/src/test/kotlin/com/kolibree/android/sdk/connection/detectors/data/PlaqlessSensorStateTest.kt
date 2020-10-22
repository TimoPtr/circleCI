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
import com.kolibree.android.sdk.plaqless.NONE_CODE
import com.kolibree.android.sdk.plaqless.OUT_OF_MOUTH_CODE
import com.kolibree.android.sdk.plaqless.REPLACE_BRUSH_HEAD_CODE
import com.kolibree.android.sdk.plaqless.RINSE_BRUSH_HEAD_CODE
import com.kolibree.android.sdk.plaqless.UNKNOWN_CODE
import com.kolibree.android.sdk.plaqless.WRONG_HANDLE_CODE
import com.kolibree.android.sdk.plaqless.fromErrorCode
import com.kolibree.android.test.utils.randomPositiveInt
import com.kolibree.android.test.utils.randomUnsigned8
import com.kolibree.android.test.utils.randomUnsignedSigned16
import junit.framework.TestCase.assertEquals
import org.junit.Test

class PlaqlessSensorStateTest : BaseUnitTest() {
    @Test
    fun `parse BLE_GATT_PLAQLESS_DETECTOR_CHAR notification data`() {
        val relativeTimestamp = randomPositiveInt()
        val alphaAngle = randomUnsignedSigned16()
        val betaAngle = randomUnsignedSigned16()
        val gammaAngle = randomUnsignedSigned16()
        val brushedSurface1 = randomUnsigned8()
        val brushedSurface2 = randomUnsigned8()
        val cleanPercentage1 = randomUnsigned8()
        val cleanPercentage2 = randomUnsigned8()
        val mostProbableZone1 = randomUnsigned8()
        val mostProbableZone2 = randomUnsigned8()
        val mostProbableZone3 = randomUnsigned8()
        val errorCode = randomErrorCode()

        val payloadWriter = PayloadWriter(18)
        payloadWriter.writeInt32(relativeTimestamp)
        payloadWriter.writeUnsignedInt16(alphaAngle)
        payloadWriter.writeUnsignedInt16(betaAngle)
        payloadWriter.writeUnsignedInt16(gammaAngle)
        payloadWriter.writeUnsignedInt8(brushedSurface1)
        payloadWriter.writeUnsignedInt8(cleanPercentage1)
        payloadWriter.writeUnsignedInt8(brushedSurface2)
        payloadWriter.writeUnsignedInt8(cleanPercentage2)
        payloadWriter.writeUnsignedInt8(mostProbableZone1)
        payloadWriter.writeUnsignedInt8(mostProbableZone2)
        payloadWriter.writeUnsignedInt8(mostProbableZone3)
        payloadWriter.writeByte(errorCode)

        val notificationData = payloadWriter.bytes

        val expectedPlaqlessData =
            PlaqlessSensorState(
                relativeTimestampMillis = relativeTimestamp.toLong(),
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
                plaqlessError = fromErrorCode(errorCode)
            )

        assertEquals(expectedPlaqlessData, notificationData.toPlaqlessSensorData())
    }
}

internal fun plaqlessSensorNotificationData(): ByteArray {
    val relativeTimestamp = randomPositiveInt()
    val alphaAngle = randomUnsignedSigned16()
    val betaAngle = randomUnsignedSigned16()
    val gammaAngle = randomUnsignedSigned16()
    val brushedSurface1 = randomUnsigned8()
    val brushedSurface2 = randomUnsigned8()
    val cleanPercentage1 = randomUnsigned8()
    val cleanPercentage2 = randomUnsigned8()
    val mostProbableZone1 = randomUnsigned8()
    val mostProbableZone2 = randomUnsigned8()
    val mostProbableZone3 = randomUnsigned8()
    val errorCode = randomErrorCode()

    val payloadWriter = PayloadWriter(18)
    payloadWriter.writeInt32(relativeTimestamp)
    payloadWriter.writeUnsignedInt16(alphaAngle)
    payloadWriter.writeUnsignedInt16(betaAngle)
    payloadWriter.writeUnsignedInt16(gammaAngle)
    payloadWriter.writeUnsignedInt8(brushedSurface1)
    payloadWriter.writeUnsignedInt8(brushedSurface2)
    payloadWriter.writeUnsignedInt8(cleanPercentage1)
    payloadWriter.writeUnsignedInt8(cleanPercentage2)
    payloadWriter.writeUnsignedInt8(mostProbableZone1)
    payloadWriter.writeUnsignedInt8(mostProbableZone2)
    payloadWriter.writeUnsignedInt8(mostProbableZone3)
    payloadWriter.writeByte(errorCode)

    return payloadWriter.bytes
}

private fun randomErrorCode(): Byte {
    return byteArrayOf(
        NONE_CODE,
        OUT_OF_MOUTH_CODE,
        WRONG_HANDLE_CODE,
        RINSE_BRUSH_HEAD_CODE,
        REPLACE_BRUSH_HEAD_CODE,
        UNKNOWN_CODE
    ).random()
}

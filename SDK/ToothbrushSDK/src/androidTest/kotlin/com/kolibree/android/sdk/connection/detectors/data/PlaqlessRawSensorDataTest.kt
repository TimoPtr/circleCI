/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.connection.detectors.data

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.android.test.utils.randomSigned16
import com.kolibree.kml.Kml
import junit.framework.TestCase.assertEquals
import org.junit.Test

class PlaqlessRawSensorDataTest : BaseInstrumentationTest() {
    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    /*
    convertToKmlRawData
     */
    @Test
    fun convertToKmlRawData() {
        Kml.init()

        val accelX: Short = randomSigned16()
        val accelY: Short = randomSigned16()
        val accelZ: Short = randomSigned16()
        val gyroX: Short = randomSigned16()
        val gyroY: Short = randomSigned16()
        val gyroZ: Short = randomSigned16()

        val plaqlessData =
            PlaqlessRawSensorState(
                relativeTimestampMillis = 0,
                accelX = accelX,
                accelY = accelY,
                accelZ = accelZ,
                gyroX = gyroX,
                gyroY = gyroY,
                gyroZ = gyroZ
            )

        val rawData = plaqlessData.convertToKmlRawData()

        assertEquals(accelX.toFloat(), rawData.accelX)
        assertEquals(accelY.toFloat(), rawData.accelY)
        assertEquals(accelZ.toFloat(), rawData.accelZ)
        assertEquals(gyroX.toFloat(), rawData.gyroX)
        assertEquals(gyroY.toFloat(), rawData.gyroY)
        assertEquals(gyroZ.toFloat(), rawData.gyroZ)
    }
}

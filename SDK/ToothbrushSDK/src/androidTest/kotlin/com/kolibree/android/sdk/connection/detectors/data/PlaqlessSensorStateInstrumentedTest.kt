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
import com.kolibree.android.sdk.plaqless.PlaqlessError
import com.kolibree.android.test.BaseInstrumentationTest
import com.kolibree.android.test.utils.randomSigned16
import com.kolibree.android.test.utils.randomUnsigned8
import com.kolibree.android.test.utils.randomUnsignedSigned16
import com.kolibree.kml.Kml
import junit.framework.TestCase.assertEquals
import org.junit.Test

class PlaqlessSensorStateInstrumentedTest : BaseInstrumentationTest() {
    override fun context(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    /*
    convertToKmlPlaqlessData
     */
    @Test
    fun convertToKmlPlaqlessData() {
        Kml.init()

        val alphaAngle = randomUnsignedSigned16()
        val betaAngle = randomUnsignedSigned16()
        val gammaAngle = randomUnsignedSigned16()
        val brushedSurface1 = randomUnsigned8()
        val brushedSurface2 = randomUnsigned8()
        val cleanPercentage1 = randomSigned16()
        val cleanPercentage2 = randomSigned16()
        val error = PlaqlessError.REPLACE_BRUSH_HEAD
        val expectedMostProbableZone1: Short = 1
        val expectedMostProbableZone2: Short = 2
        val expectedMostProbableZone3: Short = 3

        val plaqlessSensorState =
            PlaqlessSensorState(
                relativeTimestampMillis = 0,
                alphaAngle = alphaAngle,
                betaAngle = betaAngle,
                gammaAngle = gammaAngle,
                brushedSurface1 = brushedSurface1,
                brushedSurface2 = brushedSurface2,
                cleanPercentage1 = cleanPercentage1,
                cleanPercentage2 = cleanPercentage2,
                mostProbableZone1 = expectedMostProbableZone1,
                mostProbableZone2 = expectedMostProbableZone2,
                mostProbableZone3 = expectedMostProbableZone3,
                plaqlessError = error
            )

        val plaqlessData = plaqlessSensorState.convertToKmlPlaqlessData()

        assertEquals(alphaAngle, plaqlessData.alphaAngle)
        assertEquals(betaAngle, plaqlessData.betaAngle)
        assertEquals(gammaAngle, plaqlessData.gammaAngle)
        assertEquals(brushedSurface1, plaqlessData.brushedSurface1)
        assertEquals(brushedSurface2, plaqlessData.brushedSurface2)
        assertEquals(cleanPercentage1.toInt(), plaqlessData.cleannessPercentage1.value())
        assertEquals(cleanPercentage2.toInt(), plaqlessData.cleannessPercentage2.value())
        assertEquals(cleanPercentage2.toInt(), plaqlessData.cleannessPercentage2.value())
        assertEquals(error.code.toShort(), plaqlessData.error)
    }
}

/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline.models

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.kml.MouthZone16
import com.kolibree.statsoffline.test.createAverageCheckup
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlin.random.Random
import org.junit.Test

internal class AverageCheckupTest : BaseUnitTest() {

    @Test(expected = IllegalArgumentException::class)
    fun `validate throws IllegalArgumentException if averageSurfaceMap parameter doesn't contain all MouthZone16 values`() {
        mapOf(MouthZone16.UpIncInt to 7f).validate()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `validate throws IllegalArgumentException if averageSurfaceMap contains duplicated MouthZone16 values`() {
        MouthZone16.values().associate { MouthZone16.UpIncExt to 0f }.validate()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `validate throws IllegalArgumentException if a value in averageSurfaceMap is NaN`() {
        MouthZone16.values().associate { it to Float.NaN }.validate()
    }

    @Test
    fun `validate doesn't throw any exception if averageSurfaceMap is empty`() {
        emptyMap<MouthZone16, Float>().validate()
    }

    /*
    emptyAverageCheckup
     */

    @Test
    fun `emptyAverageCheckup returns AverageCheckup with all zones and value 0`() {
        val emptyAverageCheckup = emptyAverageCheckup()

        assertEquals(MouthZone16.values().size, emptyAverageCheckup.size)

        MouthZone16.values().forEach { mouthZone ->
            assertEquals(0f, emptyAverageCheckup[mouthZone])
        }
    }

    /*
    calculateAverage
     */

    @Test
    fun `calculateAverage returns 0 for a sequence of empty AverageCheckups`() {
        val averageCheckupResult = sequenceOf(emptyAverageCheckup(), emptyAverageCheckup()).calculateAverageCheckup()

        assertEquals(MouthZone16.values().size, averageCheckupResult.size)

        MouthZone16.values().forEach { mouthZone ->
            assertEquals(0f, averageCheckupResult[mouthZone])
        }
    }

    @Test
    fun `calculateAverage returns average with single decimal roundedUp`() {
        val mouthZone1 = MouthZone16.LoIncInt
        val mouthZone2 = MouthZone16.UpMolRiExt
        val averageCheckup1 = createAverageCheckup(mapOf(mouthZone1 to 80f))
        val averageCheckup2 = createAverageCheckup(mapOf(mouthZone1 to 20f))
        val averageCheckup3 = createAverageCheckup(mapOf(mouthZone1 to 0.1f, mouthZone2 to 10f))

        val averageSequence = sequenceOf(averageCheckup1, averageCheckup2, averageCheckup3)
        val averageCheckupResult = averageSequence.calculateAverageCheckup()

        val expectedAverageZone1 = 33.4f
        val expectedAverageZone2 = 3.3f
        assertEquals(expectedAverageZone1, averageCheckupResult[mouthZone1])
        assertEquals(expectedAverageZone2, averageCheckupResult[mouthZone2])
    }

    @Test
    fun `calculateAverage returns average from all zones`() {
        val mouthZone1 = MouthZone16.LoIncInt
        val mouthZone2 = MouthZone16.LoMolLeInt
        val mouthZone3 = MouthZone16.UpMolRiExt
        val mouthZone4 = MouthZone16.UpMolRiOcc
        val averageCheckup1 = createAverageCheckup(mapOf(mouthZone1 to 80f, mouthZone2 to 5f, mouthZone3 to 2f))
        val averageCheckup2 = createAverageCheckup(mapOf(mouthZone1 to 23f, mouthZone2 to 78f, mouthZone4 to 10f))
        val averageCheckup3 = createAverageCheckup(mapOf(mouthZone1 to 100f))

        val averageSequence = sequenceOf(averageCheckup1, averageCheckup2, averageCheckup3)
        val averageCheckupResult = averageSequence.calculateAverageCheckup()

        val expectedAverageMouth1 = 67.7
        val expectedAverageMouth2 = 27.7
        val expectedAverageMouth3 = 0.7
        val expectedAverageMouth4 = 3.3

        averageCheckupResult.forEach {
            val expectedAverage = (when (it.key) {
                mouthZone1 -> expectedAverageMouth1
                mouthZone2 -> expectedAverageMouth2
                mouthZone3 -> expectedAverageMouth3
                mouthZone4 -> expectedAverageMouth4
                else -> 0.0
            }).toFloat()

            assertEquals(
                "${it.key} not returning expected value",
                expectedAverage,
                it.value
            )
        }
    }

    @Test
    fun `calculateAverage ignores empty AverageCheckup in sequence`() {
        val mouthZone1 = MouthZone16.LoIncInt
        val mouthZone2 = MouthZone16.LoMolLeInt
        val mouthZone3 = MouthZone16.UpMolRiExt
        val averageCheckup1 = createAverageCheckup(mapOf(mouthZone1 to 80f, mouthZone2 to 5f, mouthZone3 to 2f))

        val averageCheckupResult = sequenceOf(
            averageCheckup1,
            emptyAverageCheckup(),
            emptyAverageCheckup(),
            emptyAverageCheckup()
        ).calculateAverageCheckup()

        averageCheckupResult.forEach {
            val expectedAverage = when (it.key) {
                mouthZone1 -> 80f
                mouthZone2 -> 5f
                mouthZone3 -> 2f
                else -> 0f
            }

            assertEquals(
                "${it.key} not returning expected value",
                expectedAverage,
                it.value
            )
        }
    }

    /*
    hasCheckupData
     */

    @Test
    fun `hasCheckupData returns false if no zone has a value`() {
        assertFalse(emptyAverageCheckup().hasCheckupData())
    }

    @Test
    fun `hasCheckupData returns true if any zone has a value`() {
        val emptyAverageCheckup = emptyAverageCheckup()

        assertFalse(emptyAverageCheckup.hasCheckupData())

        MouthZone16.values().forEach { mouthZone ->
            val mutableCheckup = emptyAverageCheckup.toMutableMap()
            mutableCheckup[mouthZone] = Random.nextFloat()

            assertTrue(mutableCheckup.hasCheckupData())
        }
    }
}

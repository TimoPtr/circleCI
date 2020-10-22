/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.processedbrushings

import com.kolibree.android.test.mocks.zoneCheckupData
import com.kolibree.kml.MouthZone16
import io.kotlintest.matchers.doubles.shouldBeExactly
import io.kotlintest.matchers.doubles.shouldBeGreaterThanOrEqual
import io.kotlintest.matchers.doubles.shouldBeLessThanOrEqual
import io.kotlintest.properties.Gen
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.StringSpec
import kotlin.random.Random

class CheckupDataPropertyTest : StringSpec({
    "underSpeedPercentage is between 0 and 100, and calculated from all ZoneCheckupData" {
        assertAll(
            50,
            nonEmptyZoneCheckupDataMap
        ) { dataMap ->
            val expectedUnderSpeed: Double = dataMap.values
                .map { it.checkupZoneKpis()!!.underSpeedPercentage }
                .average()

            val checkupData = CheckupDataImpl(createKmlCheckupData(checkupData = dataMap))
            val underSpeed = checkupData.underSpeedAverage()
            underSpeed shouldBeGreaterThanOrEqual 0.0
            underSpeed shouldBeLessThanOrEqual 100.0

            underSpeed shouldBeExactly expectedUnderSpeed
        }
    }

    "overSpeedPercentage is between 0 and 100, and calculated from all ZoneCheckupData" {
        assertAll(
            50,
            nonEmptyZoneCheckupDataMap
        ) { dataMap ->
            val expectedOverSpeed: Double = dataMap.values
                .map { it.checkupZoneKpis()!!.overSpeedPercentage }
                .average()

            val checkupData = CheckupDataImpl(createKmlCheckupData(checkupData = dataMap))
            val overSpeed = checkupData.overSpeedAverage()
            overSpeed shouldBeGreaterThanOrEqual 0.0
            overSpeed shouldBeLessThanOrEqual 100.0

            overSpeed shouldBeExactly expectedOverSpeed
        }
    }

    "correctSpeedPercentage is between 0 and 100, and calculated from all ZoneCheckupData" {
        assertAll(
            50,
            nonEmptyZoneCheckupDataMap
        ) { dataMap ->
            val expectedCorrectSpeed: Double = dataMap.values
                .map { it.checkupZoneKpis()!!.correctSpeedPercentage }
                .average()

            val checkupData = CheckupDataImpl(createKmlCheckupData(checkupData = dataMap))
            val correctSpeed = checkupData.correctSpeedAverage()
            correctSpeed shouldBeGreaterThanOrEqual 0.0
            correctSpeed shouldBeLessThanOrEqual 100.0

            correctSpeed shouldBeExactly expectedCorrectSpeed
        }
    }

    "angleAverage is between 0 and 100, and calculated from all ZoneCheckupData" {
        assertAll(
            50,
            nonEmptyZoneCheckupDataMap
        ) { dataMap ->
            val expectedAngleAverage: Double = dataMap.values
                .map { it.checkupZoneKpis()!!.correctOrientationPercentage }
                .average()

            val checkupData = CheckupDataImpl(createKmlCheckupData(checkupData = dataMap))
            val angleAverage = checkupData.angleAverage()
            angleAverage shouldBeGreaterThanOrEqual 0.0
            angleAverage shouldBeLessThanOrEqual 100.0

            angleAverage shouldBeExactly expectedAngleAverage
        }
    }

    "movementAverage is between 0 and 100, and calculated from all ZoneCheckupData" {
        assertAll(
            50,
            nonEmptyZoneCheckupDataMap
        ) { dataMap ->
            val expectedMovementAverage: Double = dataMap.values
                .map { it.checkupZoneKpis()!!.correctMovementPercentage }
                .average()

            val checkupData = CheckupDataImpl(createKmlCheckupData(checkupData = dataMap))
            val movementAverage = checkupData.movementAverage()
            movementAverage shouldBeGreaterThanOrEqual 0.0
            movementAverage shouldBeLessThanOrEqual 100.0

            movementAverage shouldBeExactly expectedMovementAverage
        }
    }
})

private val zoneCheckupDataGen = object : Gen<ZoneCheckupData> {
    // no constants, let's always test random values
    override fun constants(): Iterable<ZoneCheckupData> = listOf()

    override fun random(): Sequence<ZoneCheckupData> = generateSequence {
        zoneCheckupData(
            checkupZoneKpis = CheckupZoneKpis(
                correctOrientationPercentage = randomPercentage(),
                correctMovementPercentage = randomPercentage(),
                correctSpeedPercentage = randomPercentage(),
                overSpeedPercentage = randomPercentage(),
                underSpeedPercentage = randomPercentage(),
                overpressurePercentage = randomPercentage()
            )
        )
    }

    private fun randomPercentage() = Random.nextInt(from = 0, until = 101) // until is exclusive
}

private val zoneCheckupDataMap: Gen<Map<MouthZone16, ZoneCheckupData>> =
    Gen.map(Gen.enum(), zoneCheckupDataGen)

private val nonEmptyZoneCheckupDataMap = zoneCheckupDataMap.filter { it.isNotEmpty() }

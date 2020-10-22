/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.statsoffline

import com.kolibree.statsoffline.models.DayWithSessions
import com.kolibree.statsoffline.models.threeTenLocalDateGen
import com.kolibree.statsoffline.persistence.models.BrushingSessionStatsEntity
import com.kolibree.statsoffline.test.createDayAggregatedStatsEntity
import com.kolibree.statsoffline.test.createDayWithSessions
import com.kolibree.statsoffline.test.createSessionStatsEntity
import com.kolibree.statsoffline.test.randomPercentageDouble
import com.kolibree.statsoffline.test.randomPercentageInt
import com.kolibree.statsoffline.test.randomPercentageLong
import io.kotlintest.matchers.doubles.shouldBeExactly
import io.kotlintest.matchers.doubles.shouldBeGreaterThanOrEqual
import io.kotlintest.matchers.doubles.shouldBeLessThanOrEqual
import io.kotlintest.properties.Gen
import io.kotlintest.properties.assertAll
import io.kotlintest.specs.StringSpec

internal class DaysWithSessionsMapExtensionsPropertyTest : StringSpec({
    "underSpeedPercentage is between 0 and 100, and calculated from all DayWithSessions" {
        assertAll(
            30,
            Gen.map(threeTenLocalDateGen(), dayWithSessionsGen)
        ) { dayWithSessionsMap ->
            val expectedUnderSpeed: Double =
                dayWithSessionsMap.values.map { it.underSpeedAverage }.calculateStatsAverage()

            val underSpeed = dayWithSessionsMap.calculateUnderSpeedAverage()
            underSpeed shouldBeGreaterThanOrEqual 0.0
            underSpeed shouldBeLessThanOrEqual 100.0

            underSpeed shouldBeExactly expectedUnderSpeed
        }
    }

    "correctSpeedPercentage is between 0 and 100, and calculated from all DayWithSessions" {
        assertAll(
            30,
            Gen.map(threeTenLocalDateGen(), dayWithSessionsGen)
        ) { dayWithSessionsMap ->
            val expectedCorrectSpeed: Double =
                dayWithSessionsMap.values.map { it.correctSpeedAverage }.calculateStatsAverage()

            val correctSpeed = dayWithSessionsMap.calculateCorrectSpeedAverage()
            correctSpeed shouldBeGreaterThanOrEqual 0.0
            correctSpeed shouldBeLessThanOrEqual 100.0

            correctSpeed shouldBeExactly expectedCorrectSpeed
        }
    }

    "overSpeedPercentage is between 0 and 100, and calculated from all DayWithSessions" {
        assertAll(
            30,
            Gen.map(threeTenLocalDateGen(), dayWithSessionsGen)
        ) { dayWithSessionsMap ->
            val expectedOverSpeed: Double =
                dayWithSessionsMap.values.map { it.overSpeedAverage }.calculateStatsAverage()

            val overSpeed = dayWithSessionsMap.calculateOverSpeedAverage()
            overSpeed shouldBeGreaterThanOrEqual 0.0
            overSpeed shouldBeLessThanOrEqual 100.0

            overSpeed shouldBeExactly expectedOverSpeed
        }
    }

    "correctOrientationPercentage is between 0 and 100, and calculated from all DayWithSessions" {
        assertAll(
            30,
            Gen.map(threeTenLocalDateGen(), dayWithSessionsGen)
        ) { dayWithSessionsMap ->
            val expected: Double =
                dayWithSessionsMap.values.map { it.correctOrientationAverage }
                    .calculateStatsAverage()

            val correctOrientation = dayWithSessionsMap.calculateCorrectOrientationAverage()
            correctOrientation shouldBeGreaterThanOrEqual 0.0
            correctOrientation shouldBeLessThanOrEqual 100.0

            correctOrientation shouldBeExactly expected
        }
    }

    "correctMovementPercentage is between 0 and 100, and calculated from all DayWithSessions" {
        assertAll(
            30,
            Gen.map(threeTenLocalDateGen(), dayWithSessionsGen)
        ) { dayWithSessionsMap ->
            val expected: Double =
                dayWithSessionsMap.values.map { it.correctMovementAverage }.calculateStatsAverage()

            val correctMovement = dayWithSessionsMap.calculateCorrectMovementAverage()
            correctMovement shouldBeGreaterThanOrEqual 0.0
            correctMovement shouldBeLessThanOrEqual 100.0

            correctMovement shouldBeExactly expected
        }
    }

    "averageDurationPercentage is between 0 and 100, and calculated from all DayWithSessions" {
        assertAll(
            30,
            Gen.map(threeTenLocalDateGen(), dayWithSessionsGen)
        ) { dayWithSessionsMap ->
            val expected: Double =
                dayWithSessionsMap.values.map { it.averageDuration }.calculateStatsAverage()

            val averageDuration = dayWithSessionsMap.calculateAverageDuration()
            averageDuration shouldBeGreaterThanOrEqual 0.0
            averageDuration shouldBeLessThanOrEqual 100.0

            averageDuration shouldBeExactly expected
        }
    }

    "averageSurfacePercentage is between 0 and 100, and calculated from all DayWithSessions" {
        assertAll(
            30,
            Gen.map(threeTenLocalDateGen(), dayWithSessionsGen)
        ) { dayWithSessionsMap ->
            val expected: Double =
                dayWithSessionsMap.values.map { it.averageSurface }.calculateStatsAverage()

            val averageSurface = dayWithSessionsMap.calculateAverageSurface()
            averageSurface shouldBeGreaterThanOrEqual 0.0
            averageSurface shouldBeLessThanOrEqual 100.0

            averageSurface shouldBeExactly expected
        }
    }
})

private val emptyDayEntity = createDayAggregatedStatsEntity()

private val dayWithSessionsGen = object : Gen<DayWithSessions> {
    // no constants, let's always test random values
    override fun constants(): Iterable<DayWithSessions> = listOf(createDayWithSessions())

    override fun random(): Sequence<DayWithSessions> = generateSequence {
        createDayWithSessions(
            dayAggregatedEntity = emptyDayEntity,
            sessions = Gen.list(brushingSessionStatsGen).filter { it.size < 50 }.random().first().toList()
        )
    }
}

private val brushingSessionStatsGen = object : Gen<BrushingSessionStatsEntity> {
    // no constants, let's always test random values
    override fun constants(): Iterable<BrushingSessionStatsEntity> = listOf()

    override fun random(): Sequence<BrushingSessionStatsEntity> = generateSequence {
        createSessionStatsEntity(
            correctMovementAverage = randomPercentageDouble(),
            underSpeedAverage = randomPercentageDouble(),
            correctSpeedAverage = randomPercentageDouble(),
            overSpeedAverage = randomPercentageDouble(),
            correctOrientationAverage = randomPercentageDouble(),
            averageSurface = randomPercentageInt(),
            duration = randomPercentageLong()
        )
    }
}

/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.domain.logic

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.rewards.personalchallenge.domain.model.BrushingStat
import com.kolibree.android.rewards.personalchallenge.domain.model.BrushingType
import com.kolibree.android.test.extensions.setFixedDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.threeten.bp.LocalDate

internal class ConditionCheckerTest : BaseUnitTest() {

    private val conditionChecker = ConditionChecker()

    @Test
    fun `maxBrushing returns true if there is at most max brushings`() {
        val brushings4 = listOf(
            createBrushingStat(),
            createBrushingStat(),
            createBrushingStat(),
            createBrushingStat()
        )
        val brushings3 = listOf(
            createBrushingStat(),
            createBrushingStat(),
            createBrushingStat()
        )
        val brushings2 = listOf(
            createBrushingStat(),
            createBrushingStat()
        )

        assertTrue(conditionChecker.maxBrushing(3, brushings3))
        assertTrue(conditionChecker.maxBrushing(3, brushings2))
        assertTrue(conditionChecker.maxBrushing(3, emptyList()))
        assertFalse(conditionChecker.maxBrushing(3, brushings4))
    }

    @Test
    fun `minBrushing returns true if there is at least min brushings`() {
        val brushings4 = listOf(
            createBrushingStat(),
            createBrushingStat(),
            createBrushingStat(),
            createBrushingStat()
        )
        val brushings3 = listOf(
            createBrushingStat(),
            createBrushingStat(),
            createBrushingStat()
        )
        val brushings2 = listOf(
            createBrushingStat(),
            createBrushingStat()
        )

        assertTrue(conditionChecker.minBrushing(3, brushings4))
        assertTrue(conditionChecker.minBrushing(3, brushings3))
        assertFalse(conditionChecker.minBrushing(3, brushings2))
        assertFalse(conditionChecker.minBrushing(3, emptyList()))
    }

    @Test
    fun `noBrushing returns true if there no brushings`() {
        assertTrue(conditionChecker.noBrushing(0))
        assertFalse(conditionChecker.noBrushing(2))
        assertFalse(conditionChecker.noBrushing(34))
    }

    @Test
    fun `noBrushing with predicate match predicate`() {
        val brushingStat = listOf(
            createBrushingStat(coverage = 50)
        )
        assertTrue(conditionChecker.noBrushing(brushingStat) { it.coverage > 50 })
        assertFalse(conditionChecker.noBrushing(brushingStat) { it.coverage == 50 })
    }

    @Test
    fun `atLeastBrushingPerDay returns true if there is at least brushing per day`() {
        val today = TrustedClock.getNowLocalDate()
        val twoBrushingPerDayInLastThreeDays = listOf(
            createBrushingStat(today),
            createBrushingStat(today),
            createBrushingStat(today),
            createBrushingStat(today.minusDays(1)),
            createBrushingStat(today.minusDays(1)),
            createBrushingStat(today.minusDays(2)),
            createBrushingStat(today.minusDays(2))
        )

        val noTwoBrushingPerDayInLastThreeDays = listOf(
            createBrushingStat(today),
            createBrushingStat(today),
            createBrushingStat(today.minusDays(1)),
            createBrushingStat(today.minusDays(1)),
            createBrushingStat(today.minusDays(1)),
            createBrushingStat(today.minusDays(1)),
            createBrushingStat(today.minusDays(2))
        )

        assertTrue(conditionChecker.atLeastBrushingPerDay(2, 3, twoBrushingPerDayInLastThreeDays))
        assertFalse(
            conditionChecker.atLeastBrushingPerDay(
                2,
                3,
                noTwoBrushingPerDayInLastThreeDays
            )
        )
    }

    @Test
    fun `firstBrushing returns true if date is in an appropriate period`() {
        val today = TrustedClock.getNowLocalDate()
        val firstBrushingDate = today.minusDays(3)
        val withFirstBrushingDate = listOf(
            createBrushingStat(today),
            createBrushingStat(today.minusDays(1)),
            createBrushingStat(today.minusDays(2)),
            createBrushingStat(today.minusDays(3))
        )
        val withoutFirstBrushingDate = listOf(
            createBrushingStat(today),
            createBrushingStat(today.minusDays(1)),
            createBrushingStat(today.minusDays(2))
        )

        assertTrue(conditionChecker.firstBrushing(firstBrushingDate, withFirstBrushingDate))
        assertFalse(conditionChecker.firstBrushing(firstBrushingDate, withoutFirstBrushingDate))
    }

    @Test
    fun `increaseFrequencyByFiftyPercent returns true if frequency is increased by at least 50%`() {
        val today = TrustedClock.getNowLocalDate()
        val before = listOf(
            createBrushingStat(today),
            createBrushingStat(today.minusDays(1)),
            createBrushingStat(today.minusDays(2)),
            createBrushingStat(today.minusDays(3))
        )
        val nowWithIncreaseFrequency = listOf(
            createBrushingStat(today),
            createBrushingStat(today.minusDays(1)),
            createBrushingStat(today.minusDays(1)),
            createBrushingStat(today.minusDays(2)),
            createBrushingStat(today.minusDays(2)),
            createBrushingStat(today.minusDays(3))
        )
        val nowWithoutIncreaseFrequency = listOf(
            createBrushingStat(today),
            createBrushingStat(today.minusDays(1)),
            createBrushingStat(today.minusDays(2)),
            createBrushingStat(today.minusDays(2)),
            createBrushingStat(today.minusDays(3))
        )

        assertTrue(
            conditionChecker.frequencyIncreasedByFiftyPercent(
                before,
                nowWithIncreaseFrequency
            )
        )
        assertFalse(
            conditionChecker.frequencyIncreasedByFiftyPercent(
                before,
                nowWithoutIncreaseFrequency
            )
        )
    }

    @Test
    fun `increaseFrequencyByHundredPercent returns true if frequency is increased by at least 100%`() {
        val today = TrustedClock.getNowLocalDate()
        val before = listOf(
            createBrushingStat(today),
            createBrushingStat(today.minusDays(1)),
            createBrushingStat(today.minusDays(2)),
            createBrushingStat(today.minusDays(3))
        )
        val nowWithIncreaseFrequency = listOf(
            createBrushingStat(today),
            createBrushingStat(today),
            createBrushingStat(today),
            createBrushingStat(today.minusDays(1)),
            createBrushingStat(today.minusDays(1)),
            createBrushingStat(today.minusDays(2)),
            createBrushingStat(today.minusDays(3)),
            createBrushingStat(today.minusDays(3))
        )
        val nowWithoutIncreaseFrequency = listOf(
            createBrushingStat(today),
            createBrushingStat(today.minusDays(1)),
            createBrushingStat(today.minusDays(2)),
            createBrushingStat(today.minusDays(2)),
            createBrushingStat(today.minusDays(3)),
            createBrushingStat(today.minusDays(3)),
            createBrushingStat(today.minusDays(3))
        )

        assertTrue(
            conditionChecker.frequencyIncreasedByHundredPercent(
                before,
                nowWithIncreaseFrequency
            )
        )
        assertFalse(
            conditionChecker.frequencyIncreasedByHundredPercent(
                before,
                nowWithoutIncreaseFrequency
            )
        )
    }

    @Test
    fun `aboveEightyPercentCoverage returns true if coverage is at least 80`() {
        val withCoverage = listOf(
            createBrushingStat(coverage = 80),
            createBrushingStat(coverage = 81),
            createBrushingStat(coverage = 100),
            createBrushingStat(coverage = 20)
        )
        val withoutCoverage = listOf(
            createBrushingStat(coverage = 79),
            createBrushingStat(coverage = 80),
            createBrushingStat(coverage = 99),
            createBrushingStat(coverage = 17)
        )

        assertTrue(conditionChecker.aboveGoodCoverage(3, withCoverage))
        assertFalse(conditionChecker.aboveGoodCoverage(3, withoutCoverage))
    }

    @Test
    fun lessThanAverageCoverage() {
        val stats = listOf(
            createBrushingStat(coverage = 100),
            createBrushingStat(coverage = 0)
        )
        assertTrue(conditionChecker.lessThanAverageCoverage(51, stats))
        assertFalse(conditionChecker.lessThanAverageCoverage(49, stats))
    }

    @Test
    fun `lessThanBrushingPerDay true case`() {
        TrustedClock.setFixedDate()
        val stats = listOf(
            createBrushingStat(
                date = TrustedClock.getNowLocalDate().minusDays(1)
            ),
            createBrushingStat(
                date = TrustedClock.getNowLocalDate().minusDays(1)
            ),
            createBrushingStat(
                date = TrustedClock.getNowLocalDate().plusDays(2)
            ),
            createBrushingStat(
                date = TrustedClock.getNowLocalDate().plusDays(2)
            ),
            createBrushingStat(
                date = TrustedClock.getNowLocalDate().plusDays(2)
            ),
            createBrushingStat(
                date = TrustedClock.getNowLocalDate().plusDays(3)
            ),
            createBrushingStat(
                date = TrustedClock.getNowLocalDate().plusDays(4)
            )

        )

        assertTrue(conditionChecker.lessThanBrushingPerDay(2, 3, stats))
    }

    @Test
    fun `lessThanBrushingPerDay false case`() {
        TrustedClock.setFixedDate()
        val stats = listOf(
            createBrushingStat(
                date = TrustedClock.getNowLocalDate().minusDays(1)
            ),
            createBrushingStat(
                date = TrustedClock.getNowLocalDate().minusDays(1)
            ),
            createBrushingStat(
                date = TrustedClock.getNowLocalDate().plusDays(2)
            ),
            createBrushingStat(
                date = TrustedClock.getNowLocalDate().plusDays(2)
            ),
            createBrushingStat(
                date = TrustedClock.getNowLocalDate().plusDays(3)
            ),
            createBrushingStat(
                date = TrustedClock.getNowLocalDate().plusDays(3)
            ),
            createBrushingStat(
                date = TrustedClock.getNowLocalDate().plusDays(100)
            )
        )

        assertFalse(conditionChecker.lessThanBrushingPerDay(2, 3, stats))
    }

    private fun createBrushingStat(
        date: LocalDate = TrustedClock.getNowLocalDate(),
        coverage: Int = 0,
        type: BrushingType = BrushingType.OfflineBrushing
    ) = BrushingStat(
        date = date,
        coverage = coverage,
        type = type
    )
}

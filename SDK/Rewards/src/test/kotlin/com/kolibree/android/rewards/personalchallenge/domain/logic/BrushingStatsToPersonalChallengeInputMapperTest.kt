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
import com.kolibree.android.rewards.personalchallenge.domain.model.AtLeastFiveBrushingsInLastWeek
import com.kolibree.android.rewards.personalchallenge.domain.model.AtLeastFiveDaysWithTwoBrushingsPerDayInLastTenDays
import com.kolibree.android.rewards.personalchallenge.domain.model.AtLeastTenBrushingInLastWeek
import com.kolibree.android.rewards.personalchallenge.domain.model.BetweenOneAndThreeBrushingsInLastWeek
import com.kolibree.android.rewards.personalchallenge.domain.model.BrushingStat
import com.kolibree.android.rewards.personalchallenge.domain.model.BrushingStats
import com.kolibree.android.rewards.personalchallenge.domain.model.BrushingType
import com.kolibree.android.rewards.personalchallenge.domain.model.LessThanFiveBrushingInLastWeek
import com.kolibree.android.rewards.personalchallenge.domain.model.LessThanFiveBrushingsInLastTenDays
import com.kolibree.android.rewards.personalchallenge.domain.model.LessThanSixtyAverageCoverageInLastWeek
import com.kolibree.android.rewards.personalchallenge.domain.model.LessThanTenBrushingInLastWeek
import com.kolibree.android.rewards.personalchallenge.domain.model.LessThanThreeDaysWithMoreThanTwoBrushingsPerDayInlastTenDays
import com.kolibree.android.rewards.personalchallenge.domain.model.MoreThanOneBrushingInLastWeek
import com.kolibree.android.rewards.personalchallenge.domain.model.MoreThanSixBrushingInLastWeek
import com.kolibree.android.rewards.personalchallenge.domain.model.NoBrushingInLastWeek
import com.kolibree.android.rewards.personalchallenge.domain.model.NoBrushings
import com.kolibree.android.rewards.personalchallenge.domain.model.NoCoachedBrushingInLastMonth
import com.kolibree.android.rewards.personalchallenge.domain.model.NoOfflineBrushingInLastMonth
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId

internal class BrushingStatsToPersonalChallengeInputMapperTest : BaseUnitTest() {

    private lateinit var mapper: BrushingStatsToPersonalChallengeInputMapper
    private val checker =
        ConditionChecker()
    private lateinit var today: LocalDate

    // TODO do test for check

    override fun setup() {
        super.setup()

        TrustedClock.utcClock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
        today = TrustedClock.getNowLocalDate()

        mapper = BrushingStatsToPersonalChallengeInputMapper(
            checker
        )
    }
/*
    @Test
    fun `result contains FirstSyncInLastWeek if first sync in last week`() {
        val stats = brushingStats(
            firstBrushingDate = today,
            lastMonth = listOf(
                brushingStat(date = today),
                brushingStat(date = today.minusDays(1)),
                brushingStat(date = today.minusDays(2)),
                brushingStat(date = today.minusDays(3))
            )
        )
        val result = mapper.map(stats)

        assertTrue(result.contains(FirstSyncInLastWeek))
    }


    @Test
    fun `result contains AtLeastTwoBrushingPerDayInLastThreeDays if at least two brushing per day in last 3 days`() {
        val stats = brushingStats(
            firstBrushingDate = today,
            lastThreeDays = listOf(
                brushingStat(date = today),
                brushingStat(date = today),
                brushingStat(date = today.minusDays(1)),
                brushingStat(date = today.minusDays(1)),
                brushingStat(date = today.minusDays(2)),
                brushingStat(date = today.minusDays(2))
            )
        )
        val result = mapper.map(stats)

        assertTrue(result.contains(AtLeastTwoBrushingPerDayInLastThreeDays))
    }

    @Test
    fun `result contains FirstSyncInSecondWeek if sync was in second week`() {
        val stats = brushingStats(
            firstBrushingDate = today.minusDays(8),
            secondWeek = listOf(
                brushingStat(date = today.minusDays(8)),
                brushingStat(date = today.minusDays(9)),
                brushingStat(date = today.minusDays(10))
            )
        )
        val result = mapper.map(stats)

        assertTrue(result.contains(FirstSyncInSecondWeek))
    }

    @Test
    fun `result contains AtLeastOneBrushingPerDayInLastThreeDays if at least one brushing per day in last 3 days`() {
        val stats = brushingStats(
            lastThreeDays = listOf(
                brushingStat(date = today),
                brushingStat(date = today.minusDays(1)),
                brushingStat(date = today.minusDays(2))
            )
        )
        val result = mapper.map(stats)

        assertTrue(result.contains(AtLeastOneBrushingPerDayInLastThreeDays))
    }

    @Test
    fun `result contains AtLeastOneBrushingPerDayInLastWeek if at least one brushing per day in last week`() {
        val stats = brushingStats(
            lastWeek = listOf(
                brushingStat(date = today.minusDays(1)),
                brushingStat(date = today.minusDays(2)),
                brushingStat(date = today.minusDays(3)),
                brushingStat(date = today.minusDays(4)),
                brushingStat(date = today.minusDays(5)),
                brushingStat(date = today.minusDays(6)),
                brushingStat(date = today.minusDays(7))
            )
        )
        val result = mapper.map(stats)

        assertTrue(result.contains(AtLeastOneBrushingPerDayInLastWeek))
    }

    @Test
    fun `result contains IncreaseFrequencyByFiftyPercent if increased frequency by 50%`() {
        val stats = brushingStats(
            lastWeek = listOf(
                brushingStat(date = today.minusDays(1)),
                brushingStat(date = today.minusDays(2)),
                brushingStat(date = today.minusDays(3))
            ),
            secondWeek = listOf(
                brushingStat(date = today.minusDays(12)),
                brushingStat(date = today.minusDays(13))
            )
        )
        val result = mapper.map(stats)

        assertTrue(result.contains(IncreaseFrequencyByFiftyPercent))
    }

    @Test
    fun `result contains IncreaseFrequencyByFiftyPercent if increased frequency by 100%`() {
        val stats = brushingStats(
            lastWeek = listOf(
                brushingStat(date = today.minusDays(2)),
                brushingStat(date = today.minusDays(5)),
                brushingStat(date = today.minusDays(3))
            ),
            secondWeek = listOf(
                brushingStat(date = today.minusDays(10))
            )
        )
        val result = mapper.map(stats)

        assertTrue(result.contains(IncreaseFrequencyByHundredPercent))
    }

    @Test
    fun `result contains AtLeastTwoBrushingPerDayInLastWeek if at least 2 brushing per day in last week`() {
        val stats = brushingStats(
            lastWeek = listOf(
                brushingStat(date = today.minusDays(1)),
                brushingStat(date = today.minusDays(1)),
                brushingStat(date = today.minusDays(2)),
                brushingStat(date = today.minusDays(2)),
                brushingStat(date = today.minusDays(3)),
                brushingStat(date = today.minusDays(3)),
                brushingStat(date = today.minusDays(4)),
                brushingStat(date = today.minusDays(4)),
                brushingStat(date = today.minusDays(5)),
                brushingStat(date = today.minusDays(5)),
                brushingStat(date = today.minusDays(6)),
                brushingStat(date = today.minusDays(6)),
                brushingStat(date = today.minusDays(7)),
                brushingStat(date = today.minusDays(7))
            )
        )
        val result = mapper.map(stats)

        assertTrue(result.contains(AtLeastTwoBrushingPerDayInLastWeek))
    }*/

    /*
    averageCoverageEvents
    */
    @Test
    fun `result LessThanSixtyAverageCoverageInLastWeek if average coverage in last week is less than sixty`() {
        val statsContainsEvent = brushingStats(
            lastMonth = listOf(
                brushingStat(date = today, coverage = 0),
                brushingStat(date = today, coverage = 50),
                brushingStat(date = today, coverage = 70)
            )
        )
        val statsDoesNotContainEvent = brushingStats(
            lastMonth = listOf(
                brushingStat(date = today, coverage = 61)
            )
        )

        assertTrue(mapper.map(statsContainsEvent).contains(LessThanSixtyAverageCoverageInLastWeek))
        assertFalse(
            mapper.map(statsDoesNotContainEvent).contains(LessThanSixtyAverageCoverageInLastWeek)
        )
    }

    /*
    brushingPerDayEvents
     */

    @Test
    fun `result contains AtLeastFiveDaysWithTwoBrushingsPerDayInLastTenDays if at least two brushing per day in 5 days last 10 days`() {
        val stats = listOf(
            brushingStat(date = today),
            brushingStat(date = today),
            brushingStat(date = today.minusDays(1)),
            brushingStat(date = today.minusDays(1)),
            brushingStat(date = today.minusDays(2)),
            brushingStat(date = today.minusDays(2)),
            brushingStat(date = today.minusDays(2)),
            brushingStat(date = today.minusDays(5)),
            brushingStat(date = today.minusDays(5)),
            brushingStat(date = today.minusDays(8)),
            brushingStat(date = today.minusDays(9)),
            brushingStat(date = today.minusDays(10))
        )

        val statsContainsEvent = brushingStats(
            lastMonth = stats.toMutableList().apply { add(brushingStat(date = today.minusDays(8))) }
        )

        val statsDoesNotContainEvent = brushingStats(
            lastMonth = stats
        )

        assertTrue(
            mapper.map(statsContainsEvent)
                .contains(AtLeastFiveDaysWithTwoBrushingsPerDayInLastTenDays)
        )
        assertFalse(
            mapper.map(statsDoesNotContainEvent)
                .contains(AtLeastFiveDaysWithTwoBrushingsPerDayInLastTenDays)
        )
    }

    @Test
    fun `result contains LessThanThreeDaysWithMoreThanTwoBrushingsPerDayInlastTenDays if less than 3 days with more than two brushing per day in last 10 days`() {
        val stats = listOf(
            brushingStat(date = today), // 1st day with 2 brushings
            brushingStat(date = today),
            brushingStat(date = today.minusDays(1)), // 2nd day with 2 brushings
            brushingStat(date = today.minusDays(1)),
            brushingStat(date = today.minusDays(2)),
            brushingStat(date = today.minusDays(3)),
            brushingStat(date = today.minusDays(4)),
            brushingStat(date = today.minusDays(5)),
            brushingStat(date = today.minusDays(6)),
            brushingStat(date = today.minusDays(7)),
            brushingStat(date = today.minusDays(8)),
            brushingStat(date = today.minusDays(9))
        )
        val statsContainsEvent = brushingStats(
            lastMonth = stats
        )

        val statsDoesNotContainEvent = brushingStats(
            lastMonth = stats.toMutableList().apply {
                add(brushingStat(date = today.minusDays(9))) }
        )

        assertTrue(
            mapper.map(statsContainsEvent)
                .contains(LessThanThreeDaysWithMoreThanTwoBrushingsPerDayInlastTenDays)
        )
        assertFalse(
            mapper.map(statsDoesNotContainEvent)
                .contains(LessThanThreeDaysWithMoreThanTwoBrushingsPerDayInlastTenDays)
        )
    }

    /*
    maxBrushingEvents
     */

    @Test
    fun `result contains LessThanFiveBrushingInLastWeek if less than 5 brushing in last week`() {
        val stats = listOf(
            brushingStat(date = today),
            brushingStat(date = today.minusDays(1)),
            brushingStat(date = today.minusDays(2)),
            brushingStat(date = today.minusDays(3))
        )

        val statsContainsEvent = brushingStats(
            lastMonth = stats
        )

        val statsDoesNotContainEvent = brushingStats(
            lastMonth = stats.toMutableList().apply { add(brushingStat(date = today.minusDays(2))) }
        )

        assertTrue(mapper.map(statsContainsEvent).contains(LessThanFiveBrushingInLastWeek))
        assertFalse(mapper.map(statsDoesNotContainEvent).contains(LessThanFiveBrushingInLastWeek))
    }

    @Test
    fun `result contains LessThanTenBrushingInLastWeek if less than 10 brushing in last week`() {
        val stats = listOf(
            brushingStat(date = today.minusDays(1)),
            brushingStat(date = today.minusDays(1)),
            brushingStat(date = today.minusDays(1)),
            brushingStat(date = today.minusDays(1)),
            brushingStat(date = today.minusDays(2)),
            brushingStat(date = today.minusDays(2)),
            brushingStat(date = today.minusDays(2)),
            brushingStat(date = today.minusDays(2)),
            brushingStat(date = today.minusDays(5))
        )

        val statsContainsEvent = brushingStats(
            lastMonth = stats
        )

        val statsDoesNotContainEvent = brushingStats(
            lastMonth = stats.toMutableList().apply { add(brushingStat(date = today.minusDays(2))) }
        )

        assertTrue(mapper.map(statsContainsEvent).contains(LessThanTenBrushingInLastWeek))
        assertFalse(mapper.map(statsDoesNotContainEvent).contains(LessThanTenBrushingInLastWeek))
    }

    @Test
    fun `result contains LessThanFiveBrushingsInLastTenDays if less than 5 brushings in last 10 days`() {
        val stats = listOf(
            brushingStat(date = today.minusDays(1)),
            brushingStat(date = today.minusDays(2)),
            brushingStat(date = today.minusDays(3)),
            brushingStat(date = today.minusDays(4)),
            brushingStat(date = today.minusDays(11))
        )

        val statsContainsEvent = brushingStats(
            lastMonth = stats
        )

        val statsDoesNotContainEvent = brushingStats(
            lastMonth = stats.toMutableList().apply { add(brushingStat(date = today.minusDays(2))) }
        )

        assertTrue(mapper.map(statsContainsEvent).contains(LessThanFiveBrushingsInLastTenDays))
        assertFalse(
            mapper.map(statsDoesNotContainEvent).contains(LessThanFiveBrushingsInLastTenDays)
        )
    }

    /*
    minBrushingEvents
     */

    @Test
    fun `result contains BetweenOneAndThreeBrushingsInLastWeek if no brushings`() {
        val stats = listOf(
            brushingStat(date = today),
            brushingStat(date = today),
            brushingStat(date = today)
        )

        val statsContainsEvent = brushingStats(
            lastMonth = stats
        )

        val statsDoesNotContainEvent = brushingStats(
            lastMonth = stats.toMutableList().apply {
                add(brushingStat(date = today))
            }
        )

        assertTrue(mapper.map(statsContainsEvent).contains(BetweenOneAndThreeBrushingsInLastWeek))
        assertFalse(
            mapper.map(statsDoesNotContainEvent).contains(BetweenOneAndThreeBrushingsInLastWeek)
        )
    }

    @Test
    fun `result contains AtLeastFiveBrushingInLastWeek if at least 5 brushing in last week`() {
        val stats = listOf(
            brushingStat(date = today),
            brushingStat(date = today.minusDays(2)),
            brushingStat(date = today.minusDays(3)),
            brushingStat(date = today.minusDays(4))
        )

        val statsContainsEvent = brushingStats(
            lastMonth = stats.toMutableList().apply {
                add(brushingStat(date = today.minusDays(5)))
            }
        )

        val statsDoesNotContainEvent = brushingStats(
            lastMonth = stats
        )

        assertTrue(mapper.map(statsContainsEvent).contains(AtLeastFiveBrushingsInLastWeek))
        assertFalse(
            mapper.map(statsDoesNotContainEvent).contains(AtLeastFiveBrushingsInLastWeek)
        )
    }

    @Test
    fun `result contains MoreThanOneBrushingInLastWeek if more than on brushing per day`() {
        val stats = listOf(
            brushingStat(date = today.minusDays(1))
        )

        val statsContainsEvent = brushingStats(
            lastMonth = stats.toMutableList().apply {
                add(brushingStat(date = today.minusDays(5)))
            }
        )

        val statsDoesNotContainEvent = brushingStats(
            lastMonth = stats
        )

        assertTrue(mapper.map(statsContainsEvent).contains(MoreThanOneBrushingInLastWeek))
        assertFalse(
            mapper.map(statsDoesNotContainEvent).contains(MoreThanOneBrushingInLastWeek)
        )
    }

    @Test
    fun `result contains MoreThanSixBrushingInLastWeek if more than 6 brushing in last week`() {
        val stats = listOf(
            brushingStat(date = today.minusDays(1)),
            brushingStat(date = today.minusDays(2)),
            brushingStat(date = today.minusDays(3)),
            brushingStat(date = today.minusDays(4)),
            brushingStat(date = today.minusDays(4))
        )

        val statsContainsEvent = brushingStats(
            lastMonth = stats.toMutableList().apply {
                add(brushingStat(date = today.minusDays(5)))
                add(brushingStat(date = today.minusDays(6)))
            }
        )

        val statsDoesNotContainEvent = brushingStats(
            lastMonth = stats
        )

        assertTrue(mapper.map(statsContainsEvent).contains(MoreThanSixBrushingInLastWeek))
        assertFalse(
            mapper.map(statsDoesNotContainEvent).contains(MoreThanSixBrushingInLastWeek)
        )
    }

    @Test
    fun `result contains AtLeastTenBrushingInLastWeek if more than 10 brushing in last week`() {
        val stats = listOf(
            brushingStat(date = today),
            brushingStat(date = today),
            brushingStat(date = today.minusDays(1)),
            brushingStat(date = today.minusDays(1)),
            brushingStat(date = today.minusDays(2)),
            brushingStat(date = today.minusDays(2)),
            brushingStat(date = today.minusDays(3)),
            brushingStat(date = today.minusDays(3)),
            brushingStat(date = today.minusDays(4))
        )

        val statsContainsEvent = brushingStats(
            lastMonth = stats.toMutableList().apply {
                add(brushingStat(date = today.minusDays(5)))
                add(brushingStat(date = today.minusDays(6)))
            }
        )

        val statsDoesNotContainEvent = brushingStats(
            lastMonth = stats
        )

        assertTrue(mapper.map(statsContainsEvent).contains(AtLeastTenBrushingInLastWeek))
        assertFalse(
            mapper.map(statsDoesNotContainEvent).contains(AtLeastTenBrushingInLastWeek)
        )
    }

    /*
    noBrushingEvents
     */
    @Test
    fun `result contains NoBrushings if no brushings`() {
        val statsContainsEvent = brushingStats(
            allBrushing = 0
        )

        val statsDoesNotContainEvent = brushingStats(
            allBrushing = 1
        )

        assertTrue(mapper.map(statsContainsEvent).contains(NoBrushings))
        assertFalse(
            mapper.map(statsDoesNotContainEvent).contains(NoBrushings)
        )
    }

    @Test
    fun `result contains NoBrushingInLastWeek if no brushings in last week`() {
        val stats = listOf(
            brushingStat(date = today.minusDays(15))
        )

        val statsContainsEvent = brushingStats(
            lastMonth = stats
        )

        val statsDoesNotContainEvent = brushingStats(
            lastMonth = stats.toMutableList().apply {
                add(brushingStat(date = today))
            }
        )

        assertTrue(mapper.map(statsContainsEvent).contains(NoBrushingInLastWeek))
        assertFalse(
            mapper.map(statsDoesNotContainEvent).contains(NoBrushingInLastWeek)
        )
    }

    @Test
    fun `result contains NoCoachedBrushingInLastMonth if there is no coach plus in last month`() {
        val stats = listOf(
            brushingStat(date = today.minusDays(1), type = BrushingType.OfflineBrushing)
        )

        val statsContainsEvent = brushingStats(
            lastMonth = stats
        )

        val statsDoesNotContainEvent = brushingStats(
            lastMonth = stats.toMutableList().apply {
                add(brushingStat(date = today, type = BrushingType.CoachedBrushing))
            }
        )

        assertTrue(mapper.map(statsContainsEvent).contains(NoCoachedBrushingInLastMonth))
        assertFalse(
            mapper.map(statsDoesNotContainEvent).contains(NoCoachedBrushingInLastMonth)
        )
    }

    @Test
    fun `result contains NoOfflineBrushingInLastMonth if there is no coach plus in last month`() {
        val stats = listOf(
            brushingStat(date = today.minusDays(1), type = BrushingType.CoachedBrushing)
        )

        val statsContainsEvent = brushingStats(
            lastMonth = stats
        )

        val statsDoesNotContainEvent = brushingStats(
            lastMonth = stats.toMutableList().apply {
                add(brushingStat(date = today, type = BrushingType.OfflineBrushing))
            }
        )

        assertTrue(mapper.map(statsContainsEvent).contains(NoOfflineBrushingInLastMonth))
        assertFalse(
            mapper.map(statsDoesNotContainEvent).contains(NoOfflineBrushingInLastMonth)
        )
    }

    private fun brushingStats(
        firstBrushingDate: LocalDate? = null,
        allBrushing: Int = 0,
        lastMonth: List<BrushingStat> = emptyList()
    ) = BrushingStats(
        firstBrushingDate = firstBrushingDate,
        allBrushing = allBrushing,
        lastMonth = lastMonth
    )

    private fun brushingStat(
        date: LocalDate = TrustedClock.getNowLocalDate(),
        coverage: Int = 0,
        type: BrushingType = BrushingType.OfflineBrushing
    ) = BrushingStat(type, date, coverage)
}

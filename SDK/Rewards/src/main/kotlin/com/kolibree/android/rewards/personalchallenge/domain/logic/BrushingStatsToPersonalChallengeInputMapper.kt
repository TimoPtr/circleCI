/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.domain.logic

import com.kolibree.android.rewards.personalchallenge.domain.model.AtLeastFiveBrushingsInLastWeek
import com.kolibree.android.rewards.personalchallenge.domain.model.AtLeastFiveDaysWithTwoBrushingsPerDayInLastTenDays
import com.kolibree.android.rewards.personalchallenge.domain.model.AtLeastFourteenBrushingWithGoodCoverageInLastWeek
import com.kolibree.android.rewards.personalchallenge.domain.model.AtLeastSevenBrushingWithGoodCoverageInLastWeek
import com.kolibree.android.rewards.personalchallenge.domain.model.AtLeastTenBrushingInLastWeek
import com.kolibree.android.rewards.personalchallenge.domain.model.AtLeastTwentyEightBrushingWithGoodCoverageInLastTwoWeeks
import com.kolibree.android.rewards.personalchallenge.domain.model.BetweenOneAndThreeBrushingsInLastWeek
import com.kolibree.android.rewards.personalchallenge.domain.model.BrushingEvent
import com.kolibree.android.rewards.personalchallenge.domain.model.BrushingStats
import com.kolibree.android.rewards.personalchallenge.domain.model.BrushingType
import com.kolibree.android.rewards.personalchallenge.domain.model.FirstSyncInLastWeek
import com.kolibree.android.rewards.personalchallenge.domain.model.FirstSyncInSecondWeek
import com.kolibree.android.rewards.personalchallenge.domain.model.IncreaseFrequencyByFiftyPercent
import com.kolibree.android.rewards.personalchallenge.domain.model.IncreaseFrequencyByHundredPercent
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
import javax.inject.Inject

@Suppress("MagicNumber")
internal class BrushingStatsToPersonalChallengeInputMapper @Inject constructor(
    private val checker: ConditionChecker
) {

    fun map(stats: BrushingStats): List<BrushingEvent> {
        val events = mutableListOf<BrushingEvent>()
        events += noBrushingEvents(stats)
        events += minBrushingEvents(stats)
        events += averageCoverageEvents(stats)
        events += brushingPerDayEvents(stats)
        events += maxBrushingEvents(stats)
        // Old events here if we ever need it again
        // events += firstBrushingEvents(stats)
        // events += aboveGoodCoverageEvents(stats)
        // events += increaseFrequencyEvents(stats)
        return events
    }

    private fun averageCoverageEvents(stats: BrushingStats): List<BrushingEvent> {
        val events = mutableListOf<BrushingEvent>()
        if (checker.lessThanAverageCoverage(60, stats.lastWeek)) {
            events += LessThanSixtyAverageCoverageInLastWeek
        }
        return events
    }

    private fun increaseFrequencyEvents(stats: BrushingStats): List<BrushingEvent> {
        val events = mutableListOf<BrushingEvent>()
        if (checker.frequencyIncreasedByFiftyPercent(stats.secondWeek, stats.lastWeek)) {
            events += IncreaseFrequencyByFiftyPercent
        }

        if (checker.frequencyIncreasedByHundredPercent(stats.secondWeek, stats.lastWeek)) {
            events += IncreaseFrequencyByHundredPercent
        }
        return events
    }

    private fun maxBrushingEvents(stats: BrushingStats): List<BrushingEvent> {
        val events = mutableListOf<BrushingEvent>()
        if (checker.maxBrushing(9, stats.lastWeek)) {
            events += LessThanTenBrushingInLastWeek
        }
        if (checker.maxBrushing(4, stats.lastWeek)) {
            events += LessThanFiveBrushingInLastWeek
        }
        if (checker.maxBrushing(4, stats.lastTenDays)) {
            events += LessThanFiveBrushingsInLastTenDays
        }
        return events
    }

    private fun brushingPerDayEvents(stats: BrushingStats): List<BrushingEvent> {
        val events = mutableListOf<BrushingEvent>()
        /*if (checker.atLeastBrushingPerDay(perDay = 1, days = 3, brushings = stats.lastThreeDays)) {
            events += AtLeastOneBrushingPerDayInLastThreeDays
        }
        if (checker.atLeastBrushingPerDay(perDay = 1, days = 7, brushings = stats.lastWeek)) {
            events += AtLeastOneBrushingPerDayInLastWeek
        }
        if (checker.atLeastBrushingPerDay(perDay = 2, days = 7, brushings = stats.lastWeek)) {
            events += AtLeastTwoBrushingPerDayInLastWeek
        }
        if (checker.atLeastBrushingPerDay(perDay = 2, days = 14, brushings = stats.lastTwoWeeks)) {
            events += AtLeastTwoBrushingPerDayInLastTwoWeeks
        }
        if (checker.atLeastBrushingPerDay(perDay = 2, days = 3, brushings = stats.lastThreeDays)) {
            events += AtLeastTwoBrushingPerDayInLastThreeDays
        }*/
        if (checker.atLeastBrushingPerDay(perDay = 2, days = 5, brushings = stats.lastTenDays)) {
            events += AtLeastFiveDaysWithTwoBrushingsPerDayInLastTenDays
        }
        if (checker.lessThanBrushingPerDay(perDay = 2, days = 3, brushings = stats.lastTenDays)) {
            events += LessThanThreeDaysWithMoreThanTwoBrushingsPerDayInlastTenDays
        }
        return events
    }

    @Suppress("LongMethod")
    private fun minBrushingEvents(stats: BrushingStats): List<BrushingEvent> {
        val events = mutableListOf<BrushingEvent>()
        if (checker.minBrushing(7, stats.lastWeek)) {
            events += MoreThanSixBrushingInLastWeek
        }
        if (checker.minBrushing(2, stats.lastWeek)) {
            events += MoreThanOneBrushingInLastWeek
        }
        if (checker.minBrushing(5, stats.lastWeek)) {
            events += AtLeastFiveBrushingsInLastWeek
        }
        if (checker.minBrushing(1, stats.lastWeek) && checker.maxBrushing(3, stats.lastWeek)) {
            events += BetweenOneAndThreeBrushingsInLastWeek
        }
        if (checker.minBrushing(10, stats.lastWeek)) {
            events += AtLeastTenBrushingInLastWeek
        }
        return events
    }

    private fun aboveGoodCoverageEvents(stats: BrushingStats): List<BrushingEvent> {
        val events = mutableListOf<BrushingEvent>()
        if (checker.aboveGoodCoverage(minBrushing = 7, days = stats.lastWeek)) {
            events += AtLeastFourteenBrushingWithGoodCoverageInLastWeek
        }
        if (checker.aboveGoodCoverage(minBrushing = 14, days = stats.lastWeek)) {
            events += AtLeastTwentyEightBrushingWithGoodCoverageInLastTwoWeeks
        }
        if (checker.aboveGoodCoverage(7, stats.lastWeek)) {
            events += AtLeastSevenBrushingWithGoodCoverageInLastWeek
        }
        return events
    }

    private fun firstBrushingEvents(stats: BrushingStats): List<BrushingEvent> {
        val events = mutableListOf<BrushingEvent>()
        if (checker.firstBrushing(stats.firstBrushingDate, stats.secondWeek)) {
            events += FirstSyncInSecondWeek
        }
        if (checker.firstBrushing(stats.firstBrushingDate, stats.lastWeek)) {
            events += FirstSyncInLastWeek
        }
        return events
    }

    private fun noBrushingEvents(stats: BrushingStats): List<BrushingEvent> {
        val events = mutableListOf<BrushingEvent>()
        if (checker.noBrushing(stats.allBrushing)) {
            events += NoBrushings
        }
        if (checker.noBrushing(stats.lastMonth) { it.type == BrushingType.CoachedBrushing }) {
            events += NoCoachedBrushingInLastMonth
        }
        if (checker.noBrushing(stats.lastMonth) { it.type == BrushingType.OfflineBrushing }) {
            events += NoOfflineBrushingInLastMonth
        }
        if (checker.noBrushing(stats.lastWeek.size)) {
            events += NoBrushingInLastWeek
        }
        return events
    }
}

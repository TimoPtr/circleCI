/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.domain.model

import com.kolibree.android.annotation.VisibleForApp

@VisibleForApp
sealed class BrushingEvent {
    operator fun plus(secondEvent: BrushingEvent): List<BrushingEvent> {
        return listOf(this, secondEvent)
    }
}

@VisibleForApp
object FirstSyncInLastWeek : BrushingEvent()

@VisibleForApp
object NoCoachedBrushingInLastMonth : BrushingEvent()

@VisibleForApp
object NoOfflineBrushingInLastMonth : BrushingEvent()

@VisibleForApp
object AtLeastTwoBrushingPerDayInLastThreeDays : BrushingEvent()

@VisibleForApp
object NoBrushings : BrushingEvent()

@VisibleForApp
object BetweenOneAndThreeBrushingsInLastWeek : BrushingEvent()

@VisibleForApp
object FirstSyncInSecondWeek : BrushingEvent()

@VisibleForApp
object AtLeastOneBrushingPerDayInLastThreeDays : BrushingEvent()

@VisibleForApp
object AtLeastOneBrushingPerDayInLastWeek : BrushingEvent()

@VisibleForApp
object AtLeastFiveBrushingsInLastWeek : BrushingEvent()

@VisibleForApp
object NoBrushingInLastWeek : BrushingEvent()

@VisibleForApp
object MoreThanOneBrushingInLastWeek : BrushingEvent()

@VisibleForApp
object LessThanFiveBrushingInLastWeek : BrushingEvent()

@VisibleForApp
object LessThanFiveBrushingsInLastTenDays : BrushingEvent()

@VisibleForApp
object MoreThanSixBrushingInLastWeek : BrushingEvent()

@VisibleForApp
object LessThanTenBrushingInLastWeek : BrushingEvent()

@VisibleForApp
object IncreaseFrequencyByFiftyPercent : BrushingEvent()

@VisibleForApp
object IncreaseFrequencyByHundredPercent : BrushingEvent()

@VisibleForApp
object AtLeastTwoBrushingPerDayInLastWeek : BrushingEvent()

@VisibleForApp
object AtLeastTwoBrushingPerDayInLastTwoWeeks : BrushingEvent()

@VisibleForApp
object AtLeastSevenBrushingWithGoodCoverageInLastWeek : BrushingEvent()

@VisibleForApp
object AtLeastTenBrushingInLastWeek : BrushingEvent()

@VisibleForApp
object AtLeastFourteenBrushingWithGoodCoverageInLastWeek : BrushingEvent()

@VisibleForApp
object AtLeastTwentyEightBrushingWithGoodCoverageInLastTwoWeeks : BrushingEvent()

@VisibleForApp
object LessThanSixtyAverageCoverageInLastWeek : BrushingEvent()

@VisibleForApp
object LessThanThreeDaysWithMoreThanTwoBrushingsPerDayInlastTenDays : BrushingEvent()

@VisibleForApp
object AtLeastFiveDaysWithTwoBrushingsPerDayInLastTenDays : BrushingEvent()

/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.model

import android.os.Parcelable
import androidx.annotation.VisibleForTesting
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.rewards.personalchallenge.domain.model.AtLeastFiveBrushingsInLastWeek
import com.kolibree.android.rewards.personalchallenge.domain.model.AtLeastFiveDaysWithTwoBrushingsPerDayInLastTenDays
import com.kolibree.android.rewards.personalchallenge.domain.model.BrushingEvent
import com.kolibree.android.rewards.personalchallenge.domain.model.LessThanFiveBrushingsInLastTenDays
import com.kolibree.android.rewards.personalchallenge.domain.model.LessThanSixtyAverageCoverageInLastWeek
import com.kolibree.android.rewards.personalchallenge.domain.model.LessThanThreeDaysWithMoreThanTwoBrushingsPerDayInlastTenDays
import com.kolibree.android.rewards.personalchallenge.domain.model.NoCoachedBrushingInLastMonth
import com.kolibree.android.rewards.personalchallenge.domain.model.NoOfflineBrushingInLastMonth
import com.kolibree.android.rewards.personalchallenge.domain.model.PersonalChallengeLevel
import com.kolibree.android.rewards.personalchallenge.domain.model.PersonalChallengePeriod
import com.kolibree.android.rewards.personalchallenge.domain.model.PersonalChallengeType
import com.kolibree.android.rewards.personalchallenge.domain.model.V1PersonalChallenge
import kotlinx.android.parcel.Parcelize

// Rules https://kolibree.atlassian.net/wiki/spaces/PROD/pages/312508437/Home+page+Hum#Rules

@Suppress("MagicNumber")
internal sealed class HumChallengeInternal(
    @VisibleForTesting
    val priority: Int, // this will help us to sort the challenge it can be change 0 is the most important one
    val period: PersonalChallengePeriod,
    @VisibleForTesting val level: PersonalChallengeLevel,
    @VisibleForTesting val type: PersonalChallengeType,
    val smiles: Int,
    @VisibleForTesting val requiredEvents: List<BrushingEvent>
) : Parcelable {

    /**
     * This method should be called to create a PersonalChallenge from a StreakChallenge,
     * in order to start it
     */
    fun toV1PersonalChallenge(): V1PersonalChallenge = V1PersonalChallenge(
        type,
        level,
        period,
        TrustedClock.getNowZonedDateTime(),
        null,
        0
    )

    @Parcelize
    object DiscoverGuidedBrushing : HumChallengeInternal(
        0,
        PersonalChallengePeriod.ONE_DAY,
        PersonalChallengeLevel.EASY,
        PersonalChallengeType.COACH_PLUS,
        1,
        listOf(NoCoachedBrushingInLastMonth)
    )

    @Parcelize
    object DiscoverOfflineBrushing : HumChallengeInternal(
        1,
        PersonalChallengePeriod.ONE_DAY,
        PersonalChallengeLevel.EASY,
        PersonalChallengeType.OFFLINE,
        1,
        listOf(NoOfflineBrushingInLastMonth)
    )

    @Parcelize
    object BrushFor5Days : HumChallengeInternal(
        2,
        PersonalChallengePeriod.FIVE_DAYS,
        PersonalChallengeLevel.EASY,
        PersonalChallengeType.STREAK,
        2,
        listOf(LessThanFiveBrushingsInLastTenDays)
    )

    @Parcelize
    object BrushFor5DaysAtLeast80Coverage : HumChallengeInternal(
        3,
        PersonalChallengePeriod.FIVE_DAYS,
        PersonalChallengeLevel.EASY,
        PersonalChallengeType.COVERAGE,
        5,
        listOf(
            AtLeastFiveBrushingsInLastWeek,
            LessThanSixtyAverageCoverageInLastWeek
        )
    )

    @Parcelize
    object BrushTwiceADayFor5Days : HumChallengeInternal(
        4,
        PersonalChallengePeriod.FIVE_DAYS,
        PersonalChallengeLevel.HARD,
        PersonalChallengeType.STREAK,
        4,
        listOf(LessThanThreeDaysWithMoreThanTwoBrushingsPerDayInlastTenDays)
    )

    @Parcelize
    object BrushTwiceADayFor5DaysAtLeast80Coverage : HumChallengeInternal(
        5,
        PersonalChallengePeriod.FIVE_DAYS,
        PersonalChallengeLevel.HARD,
        PersonalChallengeType.COVERAGE,
        10,
        listOf(
            AtLeastFiveDaysWithTwoBrushingsPerDayInLastTenDays,
            LessThanSixtyAverageCoverageInLastWeek
        )
    )

    companion object {

        fun fromEvents(events: List<BrushingEvent>): List<HumChallengeInternal> =
            allHumChallenge.filter { events.containsAll(it.requiredEvents) }

        fun fromV1PersonalChallenge(challenge: V1PersonalChallenge): HumChallengeInternal? =
            allHumChallenge.firstOrNull {
                it.level == challenge.difficultyLevel &&
                    it.period == challenge.period &&
                    it.type == challenge.objectiveType
            }
    }
}

internal val allHumChallenge = listOf(
    HumChallengeInternal.DiscoverGuidedBrushing,
    HumChallengeInternal.DiscoverOfflineBrushing,
    HumChallengeInternal.BrushFor5Days,
    HumChallengeInternal.BrushFor5DaysAtLeast80Coverage,
    HumChallengeInternal.BrushTwiceADayFor5Days,
    HumChallengeInternal.BrushTwiceADayFor5DaysAtLeast80Coverage
)

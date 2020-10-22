/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personallchallenge.model

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.android.rewards.personalchallenge.domain.model.AtLeastFiveBrushingsInLastWeek
import com.kolibree.android.rewards.personalchallenge.domain.model.AtLeastFiveDaysWithTwoBrushingsPerDayInLastTenDays
import com.kolibree.android.rewards.personalchallenge.domain.model.LessThanFiveBrushingsInLastTenDays
import com.kolibree.android.rewards.personalchallenge.domain.model.LessThanSixtyAverageCoverageInLastWeek
import com.kolibree.android.rewards.personalchallenge.domain.model.LessThanThreeDaysWithMoreThanTwoBrushingsPerDayInlastTenDays
import com.kolibree.android.rewards.personalchallenge.domain.model.NoCoachedBrushingInLastMonth
import com.kolibree.android.rewards.personalchallenge.domain.model.NoOfflineBrushingInLastMonth
import com.kolibree.android.rewards.personalchallenge.domain.model.PersonalChallengeLevel
import com.kolibree.android.rewards.personalchallenge.domain.model.PersonalChallengePeriod
import com.kolibree.android.rewards.personalchallenge.domain.model.PersonalChallengeType
import com.kolibree.android.rewards.personalchallenge.domain.model.V1PersonalChallenge
import com.kolibree.android.rewards.personalchallenge.model.HumChallengeInternal
import com.kolibree.android.rewards.personalchallenge.model.allHumChallenge
import com.kolibree.android.test.extensions.setFixedDate
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class HumChallengeInternalTest : BaseUnitTest() {

    @Test
    fun `check DiscoverGuidedBrushing well defined and creates PersonalChallenge`() {
        TrustedClock.setFixedDate()
        with(HumChallengeInternal.DiscoverGuidedBrushing) {
            assertEquals(0, priority)
            assertEquals(PersonalChallengePeriod.ONE_DAY, period)
            assertEquals(PersonalChallengeLevel.EASY, level)
            assertEquals(PersonalChallengeType.COACH_PLUS, type)
            assertEquals(1, smiles)
            assertEquals(listOf(NoCoachedBrushingInLastMonth), requiredEvents)

            assertEquals(
                V1PersonalChallenge(
                    PersonalChallengeType.COACH_PLUS,
                    PersonalChallengeLevel.EASY,
                    PersonalChallengePeriod.ONE_DAY,
                    TrustedClock.getNowZonedDateTime(),
                    null,
                    0
                ), toV1PersonalChallenge()
            )
        }
    }

    @Test
    fun `check DiscoverOfflineBrushing well defined and creates PersonalChallenge`() {
        TrustedClock.setFixedDate()
        with(HumChallengeInternal.DiscoverOfflineBrushing) {
            assertEquals(1, priority)
            assertEquals(PersonalChallengePeriod.ONE_DAY, period)
            assertEquals(PersonalChallengeLevel.EASY, level)
            assertEquals(PersonalChallengeType.OFFLINE, type)
            assertEquals(1, smiles)
            assertEquals(listOf(NoOfflineBrushingInLastMonth), requiredEvents)
            assertEquals(
                V1PersonalChallenge(
                    PersonalChallengeType.OFFLINE,
                    PersonalChallengeLevel.EASY,
                    PersonalChallengePeriod.ONE_DAY,
                    TrustedClock.getNowZonedDateTime(),
                    null,
                    0
                ), toV1PersonalChallenge()
            )
        }
    }

    @Test
    fun `check BrushFor5Days well defined and creates PersonalChallenge`() {
        TrustedClock.setFixedDate()
        with(HumChallengeInternal.BrushFor5Days) {
            assertEquals(2, priority)
            assertEquals(PersonalChallengePeriod.FIVE_DAYS, period)
            assertEquals(PersonalChallengeLevel.EASY, level)
            assertEquals(PersonalChallengeType.STREAK, type)
            assertEquals(2, smiles)
            assertEquals(listOf(LessThanFiveBrushingsInLastTenDays), requiredEvents)
            assertEquals(
                V1PersonalChallenge(
                    PersonalChallengeType.STREAK,
                    PersonalChallengeLevel.EASY,
                    PersonalChallengePeriod.FIVE_DAYS,
                    TrustedClock.getNowZonedDateTime(),
                    null,
                    0
                ), toV1PersonalChallenge()
            )
        }
    }

    @Test
    fun `check BrushFor5DaysAtLeast80Coverage well defined and creates PersonalChallenge`() {
        TrustedClock.setFixedDate()
        with(HumChallengeInternal.BrushFor5DaysAtLeast80Coverage) {
            assertEquals(3, priority)
            assertEquals(PersonalChallengePeriod.FIVE_DAYS, period)
            assertEquals(PersonalChallengeLevel.EASY, level)
            assertEquals(PersonalChallengeType.COVERAGE, type)
            assertEquals(5, smiles)
            assertEquals(
                listOf(
                    AtLeastFiveBrushingsInLastWeek,
                    LessThanSixtyAverageCoverageInLastWeek
                ), requiredEvents
            )
            assertEquals(
                V1PersonalChallenge(
                    PersonalChallengeType.COVERAGE,
                    PersonalChallengeLevel.EASY,
                    PersonalChallengePeriod.FIVE_DAYS,
                    TrustedClock.getNowZonedDateTime(),
                    null,
                    0
                ), toV1PersonalChallenge()
            )
        }
    }

    @Test
    fun `check BrushTwiceADayFor5Days well defined and creates PersonalChallenge`() {
        TrustedClock.setFixedDate()
        with(HumChallengeInternal.BrushTwiceADayFor5Days) {
            assertEquals(4, priority)
            assertEquals(PersonalChallengePeriod.FIVE_DAYS, period)
            assertEquals(PersonalChallengeLevel.HARD, level)
            assertEquals(PersonalChallengeType.STREAK, type)
            assertEquals(4, smiles)
            assertEquals(
                listOf(
                    LessThanThreeDaysWithMoreThanTwoBrushingsPerDayInlastTenDays
                ), requiredEvents
            )
            assertEquals(
                V1PersonalChallenge(
                    PersonalChallengeType.STREAK,
                    PersonalChallengeLevel.HARD,
                    PersonalChallengePeriod.FIVE_DAYS,
                    TrustedClock.getNowZonedDateTime(),
                    null,
                    0
                ), toV1PersonalChallenge()
            )
        }
    }

    @Test
    fun `check BrushTwiceADayFor5DaysAtLeast80Coverage well defined and creates PersonalChallenge`() {
        TrustedClock.setFixedDate()
        with(HumChallengeInternal.BrushTwiceADayFor5DaysAtLeast80Coverage) {
            assertEquals(5, priority)
            assertEquals(PersonalChallengePeriod.FIVE_DAYS, period)
            assertEquals(PersonalChallengeLevel.HARD, level)
            assertEquals(PersonalChallengeType.COVERAGE, type)
            assertEquals(10, smiles)
            assertEquals(
                listOf(
                    AtLeastFiveDaysWithTwoBrushingsPerDayInLastTenDays,
                    LessThanSixtyAverageCoverageInLastWeek
                ), requiredEvents
            )
            assertEquals(
                V1PersonalChallenge(
                    PersonalChallengeType.COVERAGE,
                    PersonalChallengeLevel.HARD,
                    PersonalChallengePeriod.FIVE_DAYS,
                    TrustedClock.getNowZonedDateTime(),
                    null,
                    0
                ), toV1PersonalChallenge()
            )
        }
    }

    @Test
    fun `check all challenges`() {
        assertTrue(allHumChallenge.contains(HumChallengeInternal.DiscoverGuidedBrushing))
        assertTrue(allHumChallenge.contains(HumChallengeInternal.DiscoverOfflineBrushing))
        assertTrue(allHumChallenge.contains(HumChallengeInternal.BrushFor5Days))
        assertTrue(allHumChallenge.contains(HumChallengeInternal.BrushFor5DaysAtLeast80Coverage))
        assertTrue(allHumChallenge.contains(HumChallengeInternal.BrushTwiceADayFor5Days))
        assertTrue(allHumChallenge.contains(HumChallengeInternal.BrushTwiceADayFor5DaysAtLeast80Coverage))
    }
}

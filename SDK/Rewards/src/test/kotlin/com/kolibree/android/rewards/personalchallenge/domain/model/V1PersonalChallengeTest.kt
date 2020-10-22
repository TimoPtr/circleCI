/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.rewards.personalchallenge.domain.model

import com.kolibree.android.app.test.BaseUnitTest
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.threeten.bp.ZonedDateTime

class V1PersonalChallengeTest : BaseUnitTest() {

    @Test
    fun `challenge is completed if completionDate exists`() {
        val challenge = challenge(
            creationDate = ZonedDateTime.now().minusDays(7),
            completionDate = ZonedDateTime.now(),
            progress = 0
        )
        assertTrue(challenge.completed)
    }

    @Test
    fun `challenge is incomplete if completionDate does not exist`() {
        val challenge = challenge(
            creationDate = ZonedDateTime.now().minusDays(7),
            completionDate = null,
            progress = 0
        )
        assertFalse(challenge.completed)
    }

    @Test
    fun `hasSameParams returns true if type, level and period are equal`() {
        val lhs = challenge(
            creationDate = ZonedDateTime.now().minusDays(8),
            completionDate = null,
            progress = 0
        )
        val rhs = challenge(
            creationDate = ZonedDateTime.now().minusDays(7),
            completionDate = ZonedDateTime.now(),
            progress = 100
        )
        assertTrue(lhs.hasSameParams(rhs))
    }

    @Test
    fun `hasSameParams returns false if type is different`() {
        val lhs = challenge(
            objectiveType = PersonalChallengeType.STREAK,
            difficultyLevel = PersonalChallengeLevel.EASY,
            period = PersonalChallengePeriod.SEVEN_DAYS
        )
        val rhs = challenge(
            objectiveType = PersonalChallengeType.COVERAGE,
            difficultyLevel = PersonalChallengeLevel.EASY,
            period = PersonalChallengePeriod.SEVEN_DAYS
        )
        assertFalse(lhs.hasSameParams(rhs))
    }

    @Test
    fun `hasSameParams returns false if level is different`() {
        val lhs = challenge(
            objectiveType = PersonalChallengeType.STREAK,
            difficultyLevel = PersonalChallengeLevel.EASY,
            period = PersonalChallengePeriod.SEVEN_DAYS
        )
        val rhs = challenge(
            objectiveType = PersonalChallengeType.STREAK,
            difficultyLevel = PersonalChallengeLevel.HARD,
            period = PersonalChallengePeriod.SEVEN_DAYS
        )
        assertFalse(lhs.hasSameParams(rhs))
    }

    @Test
    fun `hasSameParams returns false if period is different`() {
        val lhs = challenge(
            objectiveType = PersonalChallengeType.STREAK,
            difficultyLevel = PersonalChallengeLevel.EASY,
            period = PersonalChallengePeriod.SEVEN_DAYS
        )
        val rhs = challenge(
            objectiveType = PersonalChallengeType.STREAK,
            difficultyLevel = PersonalChallengeLevel.EASY,
            period = PersonalChallengePeriod.FOURTEEN_DAYS
        )
        assertFalse(lhs.hasSameParams(rhs))
    }

    private fun challenge(
        objectiveType: PersonalChallengeType = PersonalChallengeType.STREAK,
        difficultyLevel: PersonalChallengeLevel = PersonalChallengeLevel.EASY,
        period: PersonalChallengePeriod = PersonalChallengePeriod.SEVEN_DAYS,
        creationDate: ZonedDateTime = ZonedDateTime.now().minusDays(8),
        completionDate: ZonedDateTime? = null,
        progress: Int = 0
    ) = V1PersonalChallenge(
        objectiveType = objectiveType,
        difficultyLevel = difficultyLevel,
        period = period,
        creationDate = creationDate,
        completionDate = completionDate,
        progress = progress
    )
}
